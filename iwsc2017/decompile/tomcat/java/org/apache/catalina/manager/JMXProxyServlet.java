package org.apache.catalina.manager;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import javax.management.MBeanException;
import javax.management.OperationsException;
import javax.management.Attribute;
import java.util.Set;
import javax.management.QueryExp;
import org.apache.catalina.mbeans.MBeanDumper;
import javax.management.openmbean.CompositeData;
import javax.management.ObjectName;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import org.apache.tomcat.util.modeler.Registry;
import javax.management.MBeanServer;
import javax.servlet.http.HttpServlet;
public class JMXProxyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String[] NO_PARAMETERS;
    protected transient MBeanServer mBeanServer;
    protected transient Registry registry;
    public JMXProxyServlet() {
        this.mBeanServer = null;
    }
    public void init() throws ServletException {
        this.registry = Registry.getRegistry ( null, null );
        this.mBeanServer = Registry.getRegistry ( null, null ).getMBeanServer();
    }
    public void doGet ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException {
        response.setContentType ( "text/plain" );
        final PrintWriter writer = response.getWriter();
        if ( this.mBeanServer == null ) {
            writer.println ( "Error - No mbean server" );
            return;
        }
        String qry = request.getParameter ( "set" );
        if ( qry != null ) {
            final String name = request.getParameter ( "att" );
            final String val = request.getParameter ( "val" );
            this.setAttribute ( writer, qry, name, val );
            return;
        }
        qry = request.getParameter ( "get" );
        if ( qry != null ) {
            final String name = request.getParameter ( "att" );
            this.getAttribute ( writer, qry, name, request.getParameter ( "key" ) );
            return;
        }
        qry = request.getParameter ( "invoke" );
        if ( qry != null ) {
            final String opName = request.getParameter ( "op" );
            final String[] params = this.getInvokeParameters ( request.getParameter ( "ps" ) );
            this.invokeOperation ( writer, qry, opName, params );
            return;
        }
        qry = request.getParameter ( "qry" );
        if ( qry == null ) {
            qry = "*:*";
        }
        this.listBeans ( writer, qry );
    }
    public void getAttribute ( final PrintWriter writer, final String onameStr, final String att, final String key ) {
        try {
            final ObjectName oname = new ObjectName ( onameStr );
            Object value = this.mBeanServer.getAttribute ( oname, att );
            if ( null != key && value instanceof CompositeData ) {
                value = ( ( CompositeData ) value ).get ( key );
            }
            String valueStr;
            if ( value != null ) {
                valueStr = value.toString();
            } else {
                valueStr = "<null>";
            }
            writer.print ( "OK - Attribute get '" );
            writer.print ( onameStr );
            writer.print ( "' - " );
            writer.print ( att );
            if ( null != key ) {
                writer.print ( " - key '" );
                writer.print ( key );
                writer.print ( "'" );
            }
            writer.print ( " = " );
            writer.println ( MBeanDumper.escape ( valueStr ) );
        } catch ( Exception ex ) {
            writer.println ( "Error - " + ex.toString() );
            ex.printStackTrace ( writer );
        }
    }
    public void setAttribute ( final PrintWriter writer, final String onameStr, final String att, final String val ) {
        try {
            this.setAttributeInternal ( onameStr, att, val );
            writer.println ( "OK - Attribute set" );
        } catch ( Exception ex ) {
            writer.println ( "Error - " + ex.toString() );
            ex.printStackTrace ( writer );
        }
    }
    public void listBeans ( final PrintWriter writer, final String qry ) {
        Set<ObjectName> names = null;
        try {
            names = this.mBeanServer.queryNames ( new ObjectName ( qry ), null );
            writer.println ( "OK - Number of results: " + names.size() );
            writer.println();
        } catch ( Exception ex ) {
            writer.println ( "Error - " + ex.toString() );
            ex.printStackTrace ( writer );
            return;
        }
        final String dump = MBeanDumper.dumpBeans ( this.mBeanServer, names );
        writer.print ( dump );
    }
    public boolean isSupported ( final String type ) {
        return true;
    }
    private void invokeOperation ( final PrintWriter writer, final String onameStr, final String op, final String[] valuesStr ) {
        try {
            final Object retVal = this.invokeOperationInternal ( onameStr, op, valuesStr );
            if ( retVal != null ) {
                writer.println ( "OK - Operation " + op + " returned:" );
                this.output ( "", writer, retVal );
            } else {
                writer.println ( "OK - Operation " + op + " without return value" );
            }
        } catch ( Exception ex ) {
            writer.println ( "Error - " + ex.toString() );
            ex.printStackTrace ( writer );
        }
    }
    private String[] getInvokeParameters ( final String paramString ) {
        if ( paramString == null ) {
            return JMXProxyServlet.NO_PARAMETERS;
        }
        return paramString.split ( "," );
    }
    private void setAttributeInternal ( final String onameStr, final String attributeName, final String value ) throws OperationsException, MBeanException, ReflectionException {
        final ObjectName oname = new ObjectName ( onameStr );
        final String type = this.registry.getType ( oname, attributeName );
        final Object valueObj = this.registry.convertValue ( type, value );
        this.mBeanServer.setAttribute ( oname, new Attribute ( attributeName, valueObj ) );
    }
    private Object invokeOperationInternal ( final String onameStr, final String operation, final String[] parameters ) throws OperationsException, MBeanException, ReflectionException {
        final ObjectName oname = new ObjectName ( onameStr );
        final MBeanOperationInfo methodInfo = this.registry.getMethodInfo ( oname, operation );
        final MBeanParameterInfo[] signature = methodInfo.getSignature();
        final String[] signatureTypes = new String[signature.length];
        final Object[] values = new Object[signature.length];
        for ( int i = 0; i < signature.length; ++i ) {
            final MBeanParameterInfo pi = signature[i];
            signatureTypes[i] = pi.getType();
            values[i] = this.registry.convertValue ( pi.getType(), parameters[i] );
        }
        return this.mBeanServer.invoke ( oname, operation, values, signatureTypes );
    }
    private void output ( final String indent, final PrintWriter writer, final Object result ) {
        if ( result instanceof Object[] ) {
            for ( final Object obj : ( Object[] ) result ) {
                this.output ( "  " + indent, writer, obj );
            }
        } else {
            String strValue;
            if ( result != null ) {
                strValue = result.toString();
            } else {
                strValue = "<null>";
            }
            writer.println ( indent + strValue );
        }
    }
    static {
        NO_PARAMETERS = new String[0];
    }
}
