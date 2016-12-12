package org.apache.catalina.ant.jmx;
import java.util.ArrayList;
import java.util.List;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.apache.tools.ant.BuildException;
public class JMXAccessorInvokeTask extends JMXAccessorTask {
    private String operation ;
    private List<Arg> args = new ArrayList<>();
    public String getOperation() {
        return operation;
    }
    public void setOperation ( String operation ) {
        this.operation = operation;
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
        if ( ( operation == null ) ) {
            throw new BuildException (
                "Must specify a 'operation' for call" );
        }
        return  jmxInvoke ( jmxServerConnection, getName() );
    }
    protected String jmxInvoke ( MBeanServerConnection jmxServerConnection, String name ) throws Exception {
        Object result ;
        if ( args == null ) {
            result = jmxServerConnection.invoke ( new ObjectName ( name ),
                                                  operation, null, null );
        } else {
            Object argsA[] = new Object[ args.size()];
            String sigA[] = new String[args.size()];
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
            result = jmxServerConnection.invoke ( new ObjectName ( name ), operation, argsA, sigA );
        }
        if ( result != null ) {
            echoResult ( operation, result );
            createProperty ( result );
        }
        return null;
    }
}
