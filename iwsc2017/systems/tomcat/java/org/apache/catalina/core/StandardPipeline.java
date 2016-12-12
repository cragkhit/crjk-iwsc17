package org.apache.catalina.core;
import java.util.ArrayList;
import javax.management.ObjectName;
import org.apache.catalina.Contained;
import org.apache.catalina.Container;
import org.apache.catalina.JmxEnabled;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Valve;
import org.apache.catalina.util.LifecycleBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
public class StandardPipeline extends LifecycleBase
    implements Pipeline, Contained {
    private static final Log log = LogFactory.getLog ( StandardPipeline.class );
    public StandardPipeline() {
        this ( null );
    }
    public StandardPipeline ( Container container ) {
        super();
        setContainer ( container );
    }
    protected Valve basic = null;
    protected Container container = null;
    protected Valve first = null;
    @Override
    public boolean isAsyncSupported() {
        Valve valve = ( first != null ) ? first : basic;
        boolean supported = true;
        while ( supported && valve != null ) {
            supported = supported & valve.isAsyncSupported();
            valve = valve.getNext();
        }
        return supported;
    }
    @Override
    public Container getContainer() {
        return ( this.container );
    }
    @Override
    public void setContainer ( Container container ) {
        this.container = container;
    }
    @Override
    protected void initInternal() {
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        Valve current = first;
        if ( current == null ) {
            current = basic;
        }
        while ( current != null ) {
            if ( current instanceof Lifecycle ) {
                ( ( Lifecycle ) current ).start();
            }
            current = current.getNext();
        }
        setState ( LifecycleState.STARTING );
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        setState ( LifecycleState.STOPPING );
        Valve current = first;
        if ( current == null ) {
            current = basic;
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
        Valve[] valves = getValves();
        for ( Valve valve : valves ) {
            removeValve ( valve );
        }
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "Pipeline[" );
        sb.append ( container );
        sb.append ( ']' );
        return sb.toString();
    }
    @Override
    public Valve getBasic() {
        return ( this.basic );
    }
    @Override
    public void setBasic ( Valve valve ) {
        Valve oldBasic = this.basic;
        if ( oldBasic == valve ) {
            return;
        }
        if ( oldBasic != null ) {
            if ( getState().isAvailable() && ( oldBasic instanceof Lifecycle ) ) {
                try {
                    ( ( Lifecycle ) oldBasic ).stop();
                } catch ( LifecycleException e ) {
                    log.error ( "StandardPipeline.setBasic: stop", e );
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
        if ( getState().isAvailable() && valve instanceof Lifecycle ) {
            try {
                ( ( Lifecycle ) valve ).start();
            } catch ( LifecycleException e ) {
                log.error ( "StandardPipeline.setBasic: start", e );
                return;
            }
        }
        Valve current = first;
        while ( current != null ) {
            if ( current.getNext() == oldBasic ) {
                current.setNext ( valve );
                break;
            }
            current = current.getNext();
        }
        this.basic = valve;
    }
    @Override
    public void addValve ( Valve valve ) {
        if ( valve instanceof Contained ) {
            ( ( Contained ) valve ).setContainer ( this.container );
        }
        if ( getState().isAvailable() ) {
            if ( valve instanceof Lifecycle ) {
                try {
                    ( ( Lifecycle ) valve ).start();
                } catch ( LifecycleException e ) {
                    log.error ( "StandardPipeline.addValve: start: ", e );
                }
            }
        }
        if ( first == null ) {
            first = valve;
            valve.setNext ( basic );
        } else {
            Valve current = first;
            while ( current != null ) {
                if ( current.getNext() == basic ) {
                    current.setNext ( valve );
                    valve.setNext ( basic );
                    break;
                }
                current = current.getNext();
            }
        }
        container.fireContainerEvent ( Container.ADD_VALVE_EVENT, valve );
    }
    @Override
    public Valve[] getValves() {
        ArrayList<Valve> valveList = new ArrayList<>();
        Valve current = first;
        if ( current == null ) {
            current = basic;
        }
        while ( current != null ) {
            valveList.add ( current );
            current = current.getNext();
        }
        return valveList.toArray ( new Valve[0] );
    }
    public ObjectName[] getValveObjectNames() {
        ArrayList<ObjectName> valveList = new ArrayList<>();
        Valve current = first;
        if ( current == null ) {
            current = basic;
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
    public void removeValve ( Valve valve ) {
        Valve current;
        if ( first == valve ) {
            first = first.getNext();
            current = null;
        } else {
            current = first;
        }
        while ( current != null ) {
            if ( current.getNext() == valve ) {
                current.setNext ( valve.getNext() );
                break;
            }
            current = current.getNext();
        }
        if ( first == basic ) {
            first = null;
        }
        if ( valve instanceof Contained ) {
            ( ( Contained ) valve ).setContainer ( null );
        }
        if ( valve instanceof Lifecycle ) {
            if ( getState().isAvailable() ) {
                try {
                    ( ( Lifecycle ) valve ).stop();
                } catch ( LifecycleException e ) {
                    log.error ( "StandardPipeline.removeValve: stop: ", e );
                }
            }
            try {
                ( ( Lifecycle ) valve ).destroy();
            } catch ( LifecycleException e ) {
                log.error ( "StandardPipeline.removeValve: destroy: ", e );
            }
        }
        container.fireContainerEvent ( Container.REMOVE_VALVE_EVENT, valve );
    }
    @Override
    public Valve getFirst() {
        if ( first != null ) {
            return first;
        }
        return basic;
    }
}
