package org.apache.catalina.ant.jmx;
import java.util.ArrayList;
import java.util.List;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.apache.tools.ant.BuildException;
public class JMXAccessorCreateTask extends JMXAccessorTask {
    private String className;
    private String classLoader;
    private List<Arg> args = new ArrayList<>();
    public String getClassLoader() {
        return classLoader;
    }
    public void setClassLoader ( String classLoaderName ) {
        this.classLoader = classLoaderName;
    }
    public String getClassName() {
        return className;
    }
    public void setClassName ( String className ) {
        this.className = className;
    }
    public void addArg ( Arg arg ) {
        args.add ( arg );
    }
    public List<Arg> getArgs() {
        return args;
    }
    public void setArgs ( List<Arg> args ) {
        this.args = args;
    }
    @Override
    public String jmxExecute ( MBeanServerConnection jmxServerConnection )
    throws Exception {
        if ( getName() == null ) {
            throw new BuildException ( "Must specify a 'name'" );
        }
        if ( ( className == null ) ) {
            throw new BuildException (
                "Must specify a 'className' for get" );
        }
        jmxCreate ( jmxServerConnection, getName() );
        return null;
    }
    protected void jmxCreate ( MBeanServerConnection jmxServerConnection,
                               String name ) throws Exception {
        Object argsA[] = null;
        String sigA[] = null;
        if ( args != null ) {
            argsA = new Object[ args.size()];
            sigA = new String[args.size()];
            for ( int i = 0; i < args.size(); i++ ) {
                Arg arg = args.get ( i );
                if ( arg.getType() == null ) {
                    arg.setType ( "java.lang.String" );
                    sigA[i] = arg.getType();
                    argsA[i] = arg.getValue();
                } else {
                    sigA[i] = arg.getType();
                    argsA[i] = convertStringToType ( arg.getValue(), arg.getType() );
                }
            }
        }
        if ( classLoader != null && !"".equals ( classLoader ) ) {
            if ( isEcho() ) {
                handleOutput ( "create MBean " + name + " from class "
                               + className + " with classLoader " + classLoader );
            }
            if ( args == null ) {
                jmxServerConnection.createMBean ( className, new ObjectName ( name ), new ObjectName ( classLoader ) );
            } else {
                jmxServerConnection.createMBean ( className, new ObjectName ( name ), new ObjectName ( classLoader ), argsA, sigA );
            }
        } else {
            if ( isEcho() ) {
                handleOutput ( "create MBean " + name + " from class "
                               + className );
            }
            if ( args == null ) {
                jmxServerConnection.createMBean ( className, new ObjectName ( name ) );
            } else {
                jmxServerConnection.createMBean ( className, new ObjectName ( name ), argsA, sigA );
            }
        }
    }
}
