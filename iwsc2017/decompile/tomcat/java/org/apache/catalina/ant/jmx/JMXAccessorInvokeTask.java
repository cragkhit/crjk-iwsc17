package org.apache.catalina.ant.jmx;
import javax.management.ObjectName;
import org.apache.tools.ant.BuildException;
import javax.management.MBeanServerConnection;
import java.util.ArrayList;
import java.util.List;
public class JMXAccessorInvokeTask extends JMXAccessorTask {
    private String operation;
    private List<Arg> args;
    public JMXAccessorInvokeTask() {
        this.args = new ArrayList<Arg>();
    }
    public String getOperation() {
        return this.operation;
    }
    public void setOperation ( final String operation ) {
        this.operation = operation;
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
        if ( this.operation == null ) {
            throw new BuildException ( "Must specify a 'operation' for call" );
        }
        return this.jmxInvoke ( jmxServerConnection, this.getName() );
    }
    protected String jmxInvoke ( final MBeanServerConnection jmxServerConnection, final String name ) throws Exception {
        Object result;
        if ( this.args == null ) {
            result = jmxServerConnection.invoke ( new ObjectName ( name ), this.operation, null, null );
        } else {
            final Object[] argsA = new Object[this.args.size()];
            final String[] sigA = new String[this.args.size()];
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
            result = jmxServerConnection.invoke ( new ObjectName ( name ), this.operation, argsA, sigA );
        }
        if ( result != null ) {
            this.echoResult ( this.operation, result );
            this.createProperty ( result );
        }
        return null;
    }
}
