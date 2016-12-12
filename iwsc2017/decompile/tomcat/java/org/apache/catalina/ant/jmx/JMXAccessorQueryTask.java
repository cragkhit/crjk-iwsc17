package org.apache.catalina.ant.jmx;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import java.util.Iterator;
import java.util.Set;
import javax.management.QueryExp;
import javax.management.ObjectName;
import org.apache.tools.ant.BuildException;
import javax.management.MBeanServerConnection;
public class JMXAccessorQueryTask extends JMXAccessorTask {
    private boolean attributebinding;
    public JMXAccessorQueryTask() {
        this.attributebinding = false;
    }
    public boolean isAttributebinding() {
        return this.attributebinding;
    }
    public void setAttributebinding ( final boolean attributeBinding ) {
        this.attributebinding = attributeBinding;
    }
    @Override
    public String jmxExecute ( final MBeanServerConnection jmxServerConnection ) throws Exception {
        if ( this.getName() == null ) {
            throw new BuildException ( "Must specify a 'name'" );
        }
        return this.jmxQuery ( jmxServerConnection, this.getName() );
    }
    protected String jmxQuery ( final MBeanServerConnection jmxServerConnection, final String qry ) {
        final String isError = null;
        Set<ObjectName> names = null;
        final String resultproperty = this.getResultproperty();
        try {
            names = jmxServerConnection.queryNames ( new ObjectName ( qry ), null );
            if ( resultproperty != null ) {
                this.setProperty ( resultproperty + ".Length", Integer.toString ( names.size() ) );
            }
        } catch ( Exception e ) {
            if ( this.isEcho() ) {
                this.handleErrorOutput ( e.getMessage() );
            }
            return "Can't query mbeans " + qry;
        }
        if ( resultproperty != null ) {
            final Iterator<ObjectName> it = names.iterator();
            int oindex = 0;
            String pname = null;
            while ( it.hasNext() ) {
                final ObjectName oname = it.next();
                pname = resultproperty + "." + Integer.toString ( oindex ) + ".";
                ++oindex;
                this.setProperty ( pname + "Name", oname.toString() );
                if ( this.isAttributebinding() ) {
                    this.bindAttributes ( jmxServerConnection, pname, oname );
                }
            }
        }
        return isError;
    }
    protected void bindAttributes ( final MBeanServerConnection jmxServerConnection, final String pname, final ObjectName oname ) {
        try {
            final MBeanInfo minfo = jmxServerConnection.getMBeanInfo ( oname );
            final MBeanAttributeInfo[] attrs = minfo.getAttributes();
            Object value = null;
            for ( int i = 0; i < attrs.length; ++i ) {
                if ( attrs[i].isReadable() ) {
                    final String attName = attrs[i].getName();
                    if ( attName.indexOf ( 61 ) < 0 && attName.indexOf ( 58 ) < 0 ) {
                        if ( attName.indexOf ( 32 ) < 0 ) {
                            try {
                                value = jmxServerConnection.getAttribute ( oname, attName );
                            } catch ( Exception e ) {
                                if ( this.isEcho() ) {
                                    this.handleErrorOutput ( "Error getting attribute " + oname + " " + pname + attName + " " + e.toString() );
                                }
                                continue;
                            }
                            if ( value != null ) {
                                if ( !"modelerType".equals ( attName ) ) {
                                    this.createProperty ( pname + attName, value );
                                }
                            }
                        }
                    }
                }
            }
        } catch ( Exception ex ) {}
    }
}
