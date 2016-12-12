package org.apache.catalina.util;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.res.StringManager;
public abstract class LifecycleBase implements Lifecycle {
    private static final Log log = LogFactory.getLog ( LifecycleBase.class );
    private static final StringManager sm = StringManager.getManager ( LifecycleBase.class );
    private final List<LifecycleListener> lifecycleListeners = new CopyOnWriteArrayList<>();
    private volatile LifecycleState state = LifecycleState.NEW;
    private boolean throwOnFailure = true;
    public boolean getThrowOnFailure() {
        return throwOnFailure;
    }
    public void setThrowOnFailure ( boolean throwOnFailure ) {
        this.throwOnFailure = throwOnFailure;
    }
    @Override
    public void addLifecycleListener ( LifecycleListener listener ) {
        lifecycleListeners.add ( listener );
    }
    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return lifecycleListeners.toArray ( new LifecycleListener[0] );
    }
    @Override
    public void removeLifecycleListener ( LifecycleListener listener ) {
        lifecycleListeners.remove ( listener );
    }
    protected void fireLifecycleEvent ( String type, Object data ) {
        LifecycleEvent event = new LifecycleEvent ( this, type, data );
        for ( LifecycleListener listener : lifecycleListeners ) {
            listener.lifecycleEvent ( event );
        }
    }
    @Override
    public final synchronized void init() throws LifecycleException {
        if ( !state.equals ( LifecycleState.NEW ) ) {
            invalidTransition ( Lifecycle.BEFORE_INIT_EVENT );
        }
        try {
            setStateInternal ( LifecycleState.INITIALIZING, null, false );
            initInternal();
            setStateInternal ( LifecycleState.INITIALIZED, null, false );
        } catch ( Throwable t ) {
            handleSubClassException ( t, "lifecycleBase.initFail", toString() );
        }
    }
    protected abstract void initInternal() throws LifecycleException;
    @Override
    public final synchronized void start() throws LifecycleException {
        if ( LifecycleState.STARTING_PREP.equals ( state ) || LifecycleState.STARTING.equals ( state ) ||
                LifecycleState.STARTED.equals ( state ) ) {
            if ( log.isDebugEnabled() ) {
                Exception e = new LifecycleException();
                log.debug ( sm.getString ( "lifecycleBase.alreadyStarted", toString() ), e );
            } else if ( log.isInfoEnabled() ) {
                log.info ( sm.getString ( "lifecycleBase.alreadyStarted", toString() ) );
            }
            return;
        }
        if ( state.equals ( LifecycleState.NEW ) ) {
            init();
        } else if ( state.equals ( LifecycleState.FAILED ) ) {
            stop();
        } else if ( !state.equals ( LifecycleState.INITIALIZED ) &&
                    !state.equals ( LifecycleState.STOPPED ) ) {
            invalidTransition ( Lifecycle.BEFORE_START_EVENT );
        }
        try {
            setStateInternal ( LifecycleState.STARTING_PREP, null, false );
            startInternal();
            if ( state.equals ( LifecycleState.FAILED ) ) {
                stop();
            } else if ( !state.equals ( LifecycleState.STARTING ) ) {
                invalidTransition ( Lifecycle.AFTER_START_EVENT );
            } else {
                setStateInternal ( LifecycleState.STARTED, null, false );
            }
        } catch ( Throwable t ) {
            handleSubClassException ( t, "lifecycleBase.startFail", toString() );
        }
    }
    protected abstract void startInternal() throws LifecycleException;
    @Override
    public final synchronized void stop() throws LifecycleException {
        if ( LifecycleState.STOPPING_PREP.equals ( state ) || LifecycleState.STOPPING.equals ( state ) ||
                LifecycleState.STOPPED.equals ( state ) ) {
            if ( log.isDebugEnabled() ) {
                Exception e = new LifecycleException();
                log.debug ( sm.getString ( "lifecycleBase.alreadyStopped", toString() ), e );
            } else if ( log.isInfoEnabled() ) {
                log.info ( sm.getString ( "lifecycleBase.alreadyStopped", toString() ) );
            }
            return;
        }
        if ( state.equals ( LifecycleState.NEW ) ) {
            state = LifecycleState.STOPPED;
            return;
        }
        if ( !state.equals ( LifecycleState.STARTED ) && !state.equals ( LifecycleState.FAILED ) ) {
            invalidTransition ( Lifecycle.BEFORE_STOP_EVENT );
        }
        try {
            if ( state.equals ( LifecycleState.FAILED ) ) {
                fireLifecycleEvent ( BEFORE_STOP_EVENT, null );
            } else {
                setStateInternal ( LifecycleState.STOPPING_PREP, null, false );
            }
            stopInternal();
            if ( !state.equals ( LifecycleState.STOPPING ) && !state.equals ( LifecycleState.FAILED ) ) {
                invalidTransition ( Lifecycle.AFTER_STOP_EVENT );
            }
            setStateInternal ( LifecycleState.STOPPED, null, false );
        } catch ( Throwable t ) {
            handleSubClassException ( t, "lifecycleBase.stopFail", toString() );
        } finally {
            if ( this instanceof Lifecycle.SingleUse ) {
                setStateInternal ( LifecycleState.STOPPED, null, false );
                destroy();
            }
        }
    }
    protected abstract void stopInternal() throws LifecycleException;
    @Override
    public final synchronized void destroy() throws LifecycleException {
        if ( LifecycleState.FAILED.equals ( state ) ) {
            try {
                stop();
            } catch ( LifecycleException e ) {
                log.warn ( sm.getString (
                               "lifecycleBase.destroyStopFail", toString() ), e );
            }
        }
        if ( LifecycleState.DESTROYING.equals ( state ) || LifecycleState.DESTROYED.equals ( state ) ) {
            if ( log.isDebugEnabled() ) {
                Exception e = new LifecycleException();
                log.debug ( sm.getString ( "lifecycleBase.alreadyDestroyed", toString() ), e );
            } else if ( log.isInfoEnabled() && ! ( this instanceof Lifecycle.SingleUse ) ) {
                log.info ( sm.getString ( "lifecycleBase.alreadyDestroyed", toString() ) );
            }
            return;
        }
        if ( !state.equals ( LifecycleState.STOPPED ) && !state.equals ( LifecycleState.FAILED ) &&
                !state.equals ( LifecycleState.NEW ) && !state.equals ( LifecycleState.INITIALIZED ) ) {
            invalidTransition ( Lifecycle.BEFORE_DESTROY_EVENT );
        }
        try {
            setStateInternal ( LifecycleState.DESTROYING, null, false );
            destroyInternal();
            setStateInternal ( LifecycleState.DESTROYED, null, false );
        } catch ( Throwable t ) {
            handleSubClassException ( t, "lifecycleBase.destroyFail", toString() );
        }
    }
    protected abstract void destroyInternal() throws LifecycleException;
    @Override
    public LifecycleState getState() {
        return state;
    }
    @Override
    public String getStateName() {
        return getState().toString();
    }
    protected synchronized void setState ( LifecycleState state ) throws LifecycleException {
        setStateInternal ( state, null, true );
    }
    protected synchronized void setState ( LifecycleState state, Object data )
    throws LifecycleException {
        setStateInternal ( state, data, true );
    }
    private synchronized void setStateInternal ( LifecycleState state, Object data, boolean check )
    throws LifecycleException {
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "lifecycleBase.setState", this, state ) );
        }
        if ( check ) {
            if ( state == null ) {
                invalidTransition ( "null" );
                return;
            }
            if ( ! ( state == LifecycleState.FAILED ||
                     ( this.state == LifecycleState.STARTING_PREP &&
                       state == LifecycleState.STARTING ) ||
                     ( this.state == LifecycleState.STOPPING_PREP &&
                       state == LifecycleState.STOPPING ) ||
                     ( this.state == LifecycleState.FAILED &&
                       state == LifecycleState.STOPPING ) ) ) {
                invalidTransition ( state.name() );
            }
        }
        this.state = state;
        String lifecycleEvent = state.getLifecycleEvent();
        if ( lifecycleEvent != null ) {
            fireLifecycleEvent ( lifecycleEvent, data );
        }
    }
    private void invalidTransition ( String type ) throws LifecycleException {
        String msg = sm.getString ( "lifecycleBase.invalidTransition", type, toString(), state );
        throw new LifecycleException ( msg );
    }
    private void handleSubClassException ( Throwable t, String key, Object... args ) throws LifecycleException {
        ExceptionUtils.handleThrowable ( t );
        setStateInternal ( LifecycleState.FAILED, null, false );
        String msg = sm.getString ( key, args );
        if ( getThrowOnFailure() ) {
            if ( ! ( t instanceof LifecycleException ) ) {
                t = new LifecycleException ( msg, t );
            }
            throw ( LifecycleException ) t;
        } else {
            log.error ( msg, t );
        }
    }
}
