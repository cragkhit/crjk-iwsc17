package org.apache.catalina.valves;
import org.apache.catalina.Pipeline;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.LifecycleException;
import org.apache.juli.logging.Log;
import org.apache.catalina.Container;
import org.apache.tomcat.util.res.StringManager;
import org.apache.catalina.Valve;
import org.apache.catalina.Contained;
import org.apache.catalina.util.LifecycleMBeanBase;
public abstract class ValveBase extends LifecycleMBeanBase implements Contained, Valve {
    protected static final StringManager sm;
    protected boolean asyncSupported;
    protected Container container;
    protected Log containerLog;
    protected Valve next;
    public ValveBase() {
        this ( false );
    }
    public ValveBase ( final boolean asyncSupported ) {
        this.container = null;
        this.containerLog = null;
        this.next = null;
        this.asyncSupported = asyncSupported;
    }
    @Override
    public Container getContainer() {
        return this.container;
    }
    @Override
    public void setContainer ( final Container container ) {
        this.container = container;
    }
    @Override
    public boolean isAsyncSupported() {
        return this.asyncSupported;
    }
    public void setAsyncSupported ( final boolean asyncSupported ) {
        this.asyncSupported = asyncSupported;
    }
    @Override
    public Valve getNext() {
        return this.next;
    }
    @Override
    public void setNext ( final Valve valve ) {
        this.next = valve;
    }
    @Override
    public void backgroundProcess() {
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        this.containerLog = this.getContainer().getLogger();
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        this.setState ( LifecycleState.STARTING );
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        this.setState ( LifecycleState.STOPPING );
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( this.getClass().getName() );
        sb.append ( '[' );
        if ( this.container == null ) {
            sb.append ( "Container is null" );
        } else {
            sb.append ( this.container.getName() );
        }
        sb.append ( ']' );
        return sb.toString();
    }
    public String getObjectNameKeyProperties() {
        final StringBuilder name = new StringBuilder ( "type=Valve" );
        final Container container = this.getContainer();
        name.append ( container.getMBeanKeyProperties() );
        int seq = 0;
        final Pipeline p = container.getPipeline();
        if ( p != null ) {
            for ( final Valve valve : p.getValves() ) {
                if ( valve != null ) {
                    if ( valve == this ) {
                        break;
                    }
                    if ( valve.getClass() == this.getClass() ) {
                        ++seq;
                    }
                }
            }
        }
        if ( seq > 0 ) {
            name.append ( ",seq=" );
            name.append ( seq );
        }
        String className = this.getClass().getName();
        final int period = className.lastIndexOf ( 46 );
        if ( period >= 0 ) {
            className = className.substring ( period + 1 );
        }
        name.append ( ",name=" );
        name.append ( className );
        return name.toString();
    }
    public String getDomainInternal() {
        final Container c = this.getContainer();
        if ( c == null ) {
            return null;
        }
        return c.getDomain();
    }
    static {
        sm = StringManager.getManager ( ValveBase.class );
    }
}
