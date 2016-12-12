package org.apache.catalina.valves;
import org.apache.catalina.Contained;
import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Valve;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.res.StringManager;
public abstract class ValveBase extends LifecycleMBeanBase implements Contained, Valve {
    protected static final StringManager sm = StringManager.getManager ( ValveBase.class );
    public ValveBase() {
        this ( false );
    }
    public ValveBase ( boolean asyncSupported ) {
        this.asyncSupported = asyncSupported;
    }
    protected boolean asyncSupported;
    protected Container container = null;
    protected Log containerLog = null;
    protected Valve next = null;
    @Override
    public Container getContainer() {
        return container;
    }
    @Override
    public void setContainer ( Container container ) {
        this.container = container;
    }
    @Override
    public boolean isAsyncSupported() {
        return asyncSupported;
    }
    public void setAsyncSupported ( boolean asyncSupported ) {
        this.asyncSupported = asyncSupported;
    }
    @Override
    public Valve getNext() {
        return next;
    }
    @Override
    public void setNext ( Valve valve ) {
        this.next = valve;
    }
    @Override
    public void backgroundProcess() {
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        containerLog = getContainer().getLogger();
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        setState ( LifecycleState.STARTING );
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        setState ( LifecycleState.STOPPING );
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( this.getClass().getName() );
        sb.append ( '[' );
        if ( container == null ) {
            sb.append ( "Container is null" );
        } else {
            sb.append ( container.getName() );
        }
        sb.append ( ']' );
        return sb.toString();
    }
    @Override
    public String getObjectNameKeyProperties() {
        StringBuilder name = new StringBuilder ( "type=Valve" );
        Container container = getContainer();
        name.append ( container.getMBeanKeyProperties() );
        int seq = 0;
        Pipeline p = container.getPipeline();
        if ( p != null ) {
            for ( Valve valve : p.getValves() ) {
                if ( valve == null ) {
                    continue;
                }
                if ( valve == this ) {
                    break;
                }
                if ( valve.getClass() == this.getClass() ) {
                    seq ++;
                }
            }
        }
        if ( seq > 0 ) {
            name.append ( ",seq=" );
            name.append ( seq );
        }
        String className = this.getClass().getName();
        int period = className.lastIndexOf ( '.' );
        if ( period >= 0 ) {
            className = className.substring ( period + 1 );
        }
        name.append ( ",name=" );
        name.append ( className );
        return name.toString();
    }
    @Override
    public String getDomainInternal() {
        Container c = getContainer();
        if ( c == null ) {
            return null;
        } else {
            return c.getDomain();
        }
    }
}
