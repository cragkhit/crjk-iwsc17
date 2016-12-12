package org.apache.catalina.core;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.JmxEnabled;
import javax.management.ObjectName;
import java.util.ArrayList;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.Container;
import org.apache.catalina.Valve;
import org.apache.juli.logging.Log;
import org.apache.catalina.Contained;
import org.apache.catalina.Pipeline;
import org.apache.catalina.util.LifecycleBase;
public class StandardPipeline extends LifecycleBase implements Pipeline, Contained {
    private static final Log log;
    protected Valve basic;
    protected Container container;
    protected Valve first;
    public StandardPipeline() {
        this ( null );
    }
    public StandardPipeline ( final Container container ) {
        this.basic = null;
        this.container = null;
        this.first = null;
        this.setContainer ( container );
    }
    @Override
    public boolean isAsyncSupported() {
        Valve valve;
        boolean supported;
        for ( valve = ( ( this.first != null ) ? this.first : this.basic ), supported = true; supported && valve != null; supported &= valve.isAsyncSupported(), valve = valve.getNext() ) {}
        return supported;
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
    protected void initInternal() {
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        Valve current = this.first;
        if ( current == null ) {
            current = this.basic;
        }
        while ( current != null ) {
            if ( current instanceof Lifecycle ) {
                ( ( Lifecycle ) current ).start();
            }
            current = current.getNext();
        }
        this.setState ( LifecycleState.STARTING );
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        this.setState ( LifecycleState.STOPPING );
        Valve current = this.first;
        if ( current == null ) {
            current = this.basic;
        }
        while ( current != null ) {
            if ( current instanceof Lifecycle ) {
                ( ( Lifecycle ) current ).stop();
            }
            current = current.getNext();
        }
    }
    @Override
    protected void destroyInternal() {
        final Valve[] valves2;
        final Valve[] valves = valves2 = this.getValves();
        for ( final Valve valve : valves2 ) {
            this.removeValve ( valve );
        }
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "Pipeline[" );
        sb.append ( this.container );
        sb.append ( ']' );
        return sb.toString();
    }
    @Override
    public Valve getBasic() {
        return this.basic;
    }
    @Override
    public void setBasic ( final Valve valve ) {
        final Valve oldBasic = this.basic;
        if ( oldBasic == valve ) {
            return;
        }
        if ( oldBasic != null ) {
            if ( this.getState().isAvailable() && oldBasic instanceof Lifecycle ) {
                try {
                    ( ( Lifecycle ) oldBasic ).stop();
                } catch ( LifecycleException e ) {
                    StandardPipeline.log.error ( "StandardPipeline.setBasic: stop", e );
                }
            }
            if ( oldBasic instanceof Contained ) {
                try {
                    ( ( Contained ) oldBasic ).setContainer ( null );
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                }
            }
        }
        if ( valve == null ) {
            return;
        }
        if ( valve instanceof Contained ) {
            ( ( Contained ) valve ).setContainer ( this.container );
        }
        if ( this.getState().isAvailable() && valve instanceof Lifecycle ) {
            try {
                ( ( Lifecycle ) valve ).start();
            } catch ( LifecycleException e ) {
                StandardPipeline.log.error ( "StandardPipeline.setBasic: start", e );
                return;
            }
        }
        for ( Valve current = this.first; current != null; current = current.getNext() ) {
            if ( current.getNext() == oldBasic ) {
                current.setNext ( valve );
                break;
            }
        }
        this.basic = valve;
    }
    @Override
    public void addValve ( final Valve valve ) {
        if ( valve instanceof Contained ) {
            ( ( Contained ) valve ).setContainer ( this.container );
        }
        if ( this.getState().isAvailable() && valve instanceof Lifecycle ) {
            try {
                ( ( Lifecycle ) valve ).start();
            } catch ( LifecycleException e ) {
                StandardPipeline.log.error ( "StandardPipeline.addValve: start: ", e );
            }
        }
        if ( this.first == null ) {
            ( this.first = valve ).setNext ( this.basic );
        } else {
            for ( Valve current = this.first; current != null; current = current.getNext() ) {
                if ( current.getNext() == this.basic ) {
                    current.setNext ( valve );
                    valve.setNext ( this.basic );
                    break;
                }
            }
        }
        this.container.fireContainerEvent ( "addValve", valve );
    }
    @Override
    public Valve[] getValves() {
        final ArrayList<Valve> valveList = new ArrayList<Valve>();
        Valve current = this.first;
        if ( current == null ) {
            current = this.basic;
        }
        while ( current != null ) {
            valveList.add ( current );
            current = current.getNext();
        }
        return valveList.toArray ( new Valve[0] );
    }
    public ObjectName[] getValveObjectNames() {
        final ArrayList<ObjectName> valveList = new ArrayList<ObjectName>();
        Valve current = this.first;
        if ( current == null ) {
            current = this.basic;
        }
        while ( current != null ) {
            if ( current instanceof JmxEnabled ) {
                valveList.add ( ( ( JmxEnabled ) current ).getObjectName() );
            }
            current = current.getNext();
        }
        return valveList.toArray ( new ObjectName[0] );
    }
    @Override
    public void removeValve ( final Valve valve ) {
        Valve current;
        if ( this.first == valve ) {
            this.first = this.first.getNext();
            current = null;
        } else {
            current = this.first;
        }
        while ( current != null ) {
            if ( current.getNext() == valve ) {
                current.setNext ( valve.getNext() );
                break;
            }
            current = current.getNext();
        }
        if ( this.first == this.basic ) {
            this.first = null;
        }
        if ( valve instanceof Contained ) {
            ( ( Contained ) valve ).setContainer ( null );
        }
        if ( valve instanceof Lifecycle ) {
            if ( this.getState().isAvailable() ) {
                try {
                    ( ( Lifecycle ) valve ).stop();
                } catch ( LifecycleException e ) {
                    StandardPipeline.log.error ( "StandardPipeline.removeValve: stop: ", e );
                }
            }
            try {
                ( ( Lifecycle ) valve ).destroy();
            } catch ( LifecycleException e ) {
                StandardPipeline.log.error ( "StandardPipeline.removeValve: destroy: ", e );
            }
        }
        this.container.fireContainerEvent ( "removeValve", valve );
    }
    @Override
    public Valve getFirst() {
        if ( this.first != null ) {
            return this.first;
        }
        return this.basic;
    }
    static {
        log = LogFactory.getLog ( StandardPipeline.class );
    }
}
