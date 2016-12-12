package org.apache.catalina.ant.jmx;
import java.util.Iterator;
import java.util.Set;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.apache.tools.ant.BuildException;
public class JMXAccessorQueryTask extends JMXAccessorTask {
    private boolean attributebinding = false;
    public boolean isAttributebinding() {
        return attributebinding;
    }
    public void setAttributebinding ( boolean attributeBinding ) {
        this.attributebinding = attributeBinding;
    }
    @Override
    public String jmxExecute ( MBeanServerConnection jmxServerConnection )
    throws Exception {
        if ( getName() == null ) {
            throw new BuildException ( "Must specify a 'name'" );
        }
        return jmxQuery ( jmxServerConnection, getName() );
    }
    protected String jmxQuery ( MBeanServerConnection jmxServerConnection,
                                String qry ) {
        String isError = null;
        Set<ObjectName> names = null;
        String resultproperty = getResultproperty();
        try {
            names = jmxServerConnection.queryNames ( new ObjectName ( qry ), null );
            if ( resultproperty != null ) {
                setProperty ( resultproperty + ".Length", Integer.toString ( names.size() ) );
            }
        } catch ( Exception e ) {
            if ( isEcho() ) {
                handleErrorOutput ( e.getMessage() );
            }
            return "Can't query mbeans " + qry;
        }
        if ( resultproperty != null ) {
            Iterator<ObjectName> it = names.iterator();
            int oindex = 0;
            String pname = null;
            while ( it.hasNext() ) {
                ObjectName oname = it.next();
                pname = resultproperty + "." + Integer.toString ( oindex ) + ".";
                oindex++;
                setProperty ( pname + "Name", oname.toString() );
                if ( isAttributebinding() ) {
                    bindAttributes ( jmxServerConnection, pname, oname );
                }
            }
        }
        return isError;
    }
    protected void bindAttributes ( MBeanServerConnection jmxServerConnection, String pname, ObjectName oname ) {
        try {
            MBeanInfo minfo = jmxServerConnection.getMBeanInfo ( oname );
            MBeanAttributeInfo attrs[] = minfo.getAttributes();
            Object value = null;
            for ( int i = 0; i < attrs.length; i++ ) {
                if ( !attrs[i].isReadable() ) {
                    continue;
                }
                String attName = attrs[i].getName();
                if ( attName.indexOf ( '=' ) >= 0 || attName.indexOf ( ':' ) >= 0
                        || attName.indexOf ( ' ' ) >= 0 ) {
                    continue;
                }
                try {
                    value = jmxServerConnection
                            .getAttribute ( oname, attName );
                } catch ( Exception e ) {
                    if ( isEcho() )
                        handleErrorOutput ( "Error getting attribute "
                                            + oname + " " + pname + attName + " "
                                            + e.toString() );
                    continue;
                }
                if ( value == null ) {
                    continue;
                }
                if ( "modelerType".equals ( attName ) ) {
                    continue;
                }
                createProperty ( pname + attName, value );
            }
        } catch ( Exception e ) {
        }
    }
}
