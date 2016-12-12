package org.apache.catalina.ant.jmx;
import javax.management.ObjectName;
import org.apache.tools.ant.BuildException;
import javax.management.MBeanServerConnection;
import java.util.ArrayList;
import java.util.List;
public class JMXAccessorCreateTask extends JMXAccessorTask {
    private String className;
    private String classLoader;
    private List<Arg> args;
    public JMXAccessorCreateTask() {
        this.args = new ArrayList<Arg>();
    }
    public String getClassLoader() {
        return this.classLoader;
    }
    public void setClassLoader ( final String classLoaderName ) {
        this.classLoader = classLoaderName;
    }
    public String getClassName() {
        return this.className;
    }
    public void setClassName ( final String className ) {
        this.className = className;
    }
    public void addArg ( final Arg arg ) {
        this.args.add ( arg );
    }
    public List<Arg> getArgs() {
        return this.args;
    }
    public void setArgs ( final List<Arg> args ) {
        this.args = args;
    }
    @Override
    public String jmxExecute ( final MBeanServerConnection jmxServerConnection ) throws Exception {
        if ( this.getName() == null ) {
            throw new BuildException ( "Must specify a 'name'" );
        }
        if ( this.className == null ) {
            throw new BuildException ( "Must specify a 'className' for get" );
        }
        this.jmxCreate ( jmxServerConnection, this.getName() );
        return null;
    }
    protected void jmxCreate ( final MBeanServerConnection jmxServerConnection, final String name ) throws Exception {
        Object[] argsA = null;
        String[] sigA = null;
        if ( this.args != null ) {
            argsA = new Object[this.args.size()];
            sigA = new String[this.args.size()];
            for ( int i = 0; i < this.args.size(); ++i ) {
                final Arg arg = this.args.get ( i );
                if ( arg.getType() == null ) {
                    arg.setType ( "java.lang.String" );
                    sigA[i] = arg.getType();
                    argsA[i] = arg.getValue();
                } else {
                    sigA[i] = arg.getType();
                    argsA[i] = this.convertStringToType ( arg.getValue(), arg.getType() );
                }
            }
        }
        if ( this.classLoader != null && !"".equals ( this.classLoader ) ) {
            if ( this.isEcho() ) {
                this.handleOutput ( "create MBean " + name + " from class " + this.className + " with classLoader " + this.classLoader );
            }
            if ( this.args == null ) {
                jmxServerConnection.createMBean ( this.className, new ObjectName ( name ), new ObjectName ( this.classLoader ) );
            } else {
                jmxServerConnection.createMBean ( this.className, new ObjectName ( name ), new ObjectName ( this.classLoader ), argsA, sigA );
            }
        } else {
            if ( this.isEcho() ) {
                this.handleOutput ( "create MBean " + name + " from class " + this.className );
            }
            if ( this.args == null ) {
                jmxServerConnection.createMBean ( this.className, new ObjectName ( name ) );
            } else {
                jmxServerConnection.createMBean ( this.className, new ObjectName ( name ), argsA, sigA );
            }
        }
    }
}
