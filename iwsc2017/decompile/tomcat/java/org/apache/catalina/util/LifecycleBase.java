package org.apache.catalina.util;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.catalina.LifecycleException;
import java.util.Iterator;
import org.apache.catalina.LifecycleEvent;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.LifecycleListener;
import java.util.List;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.Lifecycle;
public abstract class LifecycleBase implements Lifecycle {
    private static final Log log;
    private static final StringManager sm;
    private final List<LifecycleListener> lifecycleListeners;
    private volatile LifecycleState state;
    private boolean throwOnFailure;
    public LifecycleBase() {
        this.lifecycleListeners = new CopyOnWriteArrayList<LifecycleListener>();
        this.state = LifecycleState.NEW;
        this.throwOnFailure = true;
    }
    public boolean getThrowOnFailure() {
        return this.throwOnFailure;
    }
    public void setThrowOnFailure ( final boolean throwOnFailure ) {
        this.throwOnFailure = throwOnFailure;
    }
    @Override
    public void addLifecycleListener ( final LifecycleListener listener ) {
        this.lifecycleListeners.add ( listener );
    }
    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return this.lifecycleListeners.toArray ( new LifecycleListener[0] );
    }
    @Override
    public void removeLifecycleListener ( final LifecycleListener listener ) {
        this.lifecycleListeners.remove ( listener );
    }
    protected void fireLifecycleEvent ( final String type, final Object data ) {
        final LifecycleEvent event = new LifecycleEvent ( this, type, data );
        for ( final LifecycleListener listener : this.lifecycleListeners ) {
            listener.lifecycleEvent ( event );
        }
    }
    @Override
    public final synchronized void init() throws LifecycleException {
        if ( !this.state.equals ( LifecycleState.NEW ) ) {
            this.invalidTransition ( "before_init" );
        }
        try {
            this.setStateInternal ( LifecycleState.INITIALIZING, null, false );
            this.initInternal();
            this.setStateInternal ( LifecycleState.INITIALIZED, null, false );
        } catch ( Throwable t ) {
            this.handleSubClassException ( t, "lifecycleBase.initFail", this.toString() );
        }
    }
    protected abstract void initInternal() throws LifecycleException;
    @Override
    public final synchronized void start() throws LifecycleException {
        if ( LifecycleState.STARTING_PREP.equals ( this.state ) || LifecycleState.STARTING.equals ( this.state ) || LifecycleState.STARTED.equals ( this.state ) ) {
            if ( LifecycleBase.log.isDebugEnabled() ) {
                final Exception e = new LifecycleException();
                LifecycleBase.log.debug ( LifecycleBase.sm.getString ( "lifecycleBase.alreadyStarted", this.toString() ), e );
            } else if ( LifecycleBase.log.isInfoEnabled() ) {
                LifecycleBase.log.info ( LifecycleBase.sm.getString ( "lifecycleBase.alreadyStarted", this.toString() ) );
            }
            return;
        }
        if ( this.state.equals ( LifecycleState.NEW ) ) {
            this.init();
        } else if ( this.state.equals ( LifecycleState.FAILED ) ) {
            this.stop();
        } else if ( !this.state.equals ( LifecycleState.INITIALIZED ) && !this.state.equals ( LifecycleState.STOPPED ) ) {
            this.invalidTransition ( "before_start" );
        }
        try {
            this.setStateInternal ( LifecycleState.STARTING_PREP, null, false );
            this.startInternal();
            if ( this.state.equals ( LifecycleState.FAILED ) ) {
                this.stop();
            } else if ( !this.state.equals ( LifecycleState.STARTING ) ) {
                this.invalidTransition ( "after_start" );
            } else {
                this.setStateInternal ( LifecycleState.STARTED, null, false );
            }
        } catch ( Throwable t ) {
            this.handleSubClassException ( t, "lifecycleBase.startFail", this.toString() );
        }
    }
    protected abstract void startInternal() throws LifecycleException;
    @Override
    public final synchronized void stop() throws LifecycleException {
        if ( LifecycleState.STOPPING_PREP.equals ( this.state ) || LifecycleState.STOPPING.equals ( this.state ) || LifecycleState.STOPPED.equals ( this.state ) ) {
            if ( LifecycleBase.log.isDebugEnabled() ) {
                final Exception e = new LifecycleException();
                LifecycleBase.log.debug ( LifecycleBase.sm.getString ( "lifecycleBase.alreadyStopped", this.toString() ), e );
            } else if ( LifecycleBase.log.isInfoEnabled() ) {
                LifecycleBase.log.info ( LifecycleBase.sm.getString ( "lifecycleBase.alreadyStopped", this.toString() ) );
            }
            return;
        }
        if ( this.state.equals ( LifecycleState.NEW ) ) {
            this.state = LifecycleState.STOPPED;
            return;
        }
        if ( !this.state.equals ( LifecycleState.STARTED ) && !this.state.equals ( LifecycleState.FAILED ) ) {
            this.invalidTransition ( "before_stop" );
        }
        try {
            if ( this.state.equals ( LifecycleState.FAILED ) ) {
                this.fireLifecycleEvent ( "before_stop", null );
            } else {
                this.setStateInternal ( LifecycleState.STOPPING_PREP, null, false );
            }
            this.stopInternal();
            if ( !this.state.equals ( LifecycleState.STOPPING ) && !this.state.equals ( LifecycleState.FAILED ) ) {
                this.invalidTransition ( "after_stop" );
            }
            this.setStateInternal ( LifecycleState.STOPPED, null, false );
        } catch ( Throwable t ) {
            this.handleSubClassException ( t, "lifecycleBase.stopFail", this.toString() );
        } finally {
            if ( this instanceof SingleUse ) {
                this.setStateInternal ( LifecycleState.STOPPED, null, false );
                this.destroy();
            }
        }
    }
    protected abstract void stopInternal() throws LifecycleException;
    @Override
    public final synchronized void destroy() throws LifecycleException {
        if ( LifecycleState.FAILED.equals ( this.state ) ) {
            try {
                this.stop();
            } catch ( LifecycleException e ) {
                LifecycleBase.log.warn ( LifecycleBase.sm.getString ( "lifecycleBase.destroyStopFail", this.toString() ), e );
            }
        }
        if ( LifecycleState.DESTROYING.equals ( this.state ) || LifecycleState.DESTROYED.equals ( this.state ) ) {
            if ( LifecycleBase.log.isDebugEnabled() ) {
                final Exception e2 = new LifecycleException();
                LifecycleBase.log.debug ( LifecycleBase.sm.getString ( "lifecycleBase.alreadyDestroyed", this.toString() ), e2 );
            } else if ( LifecycleBase.log.isInfoEnabled() && ! ( this instanceof SingleUse ) ) {
                LifecycleBase.log.info ( LifecycleBase.sm.getString ( "lifecycleBase.alreadyDestroyed", this.toString() ) );
            }
            return;
        }
        if ( !this.state.equals ( LifecycleState.STOPPED ) && !this.state.equals ( LifecycleState.FAILED ) && !this.state.equals ( LifecycleState.NEW ) && !this.state.equals ( LifecycleState.INITIALIZED ) ) {
            this.invalidTransition ( "before_destroy" );
        }
        try {
            this.setStateInternal ( LifecycleState.DESTROYING, null, false );
            this.destroyInternal();
            this.setStateInternal ( LifecycleState.DESTROYED, null, false );
        } catch ( Throwable t ) {
            this.handleSubClassException ( t, "lifecycleBase.destroyFail", this.toString() );
        }
    }
    protected abstract void destroyInternal() throws LifecycleException;
    @Override
    public LifecycleState getState() {
        return this.state;
    }
    @Override
    public String getStateName() {
        return this.getState().toString();
    }
    protected synchronized void setState ( final LifecycleState state ) throws LifecycleException {
        this.setStateInternal ( state, null, true );
    }
    protected synchronized void setState ( final LifecycleState state, final Object data ) throws LifecycleException {
        this.setStateInternal ( state, data, true );
    }
    private synchronized void setStateInternal ( final LifecycleState state, final Object data, final boolean check ) throws LifecycleException {
        if ( LifecycleBase.log.isDebugEnabled() ) {
            LifecycleBase.log.debug ( LifecycleBase.sm.getString ( "lifecycleBase.setState", this, state ) );
        }
        if ( check ) {
            if ( state == null ) {
                this.invalidTransition ( "null" );
                return;
            }
            if ( state != LifecycleState.FAILED && ( this.state != LifecycleState.STARTING_PREP || state != LifecycleState.STARTING ) && ( this.state != LifecycleState.STOPPING_PREP || state != LifecycleState.STOPPING ) && ( this.state != LifecycleState.FAILED || state != LifecycleState.STOPPING ) ) {
                this.invalidTransition ( state.name() );
            }
        }
        this.state = state;
        final String lifecycleEvent = state.getLifecycleEvent();
        if ( lifecycleEvent != null ) {
            this.fireLifecycleEvent ( lifecycleEvent, data );
        }
    }
    private void invalidTransition ( final String type ) throws LifecycleException {
        final String msg = LifecycleBase.sm.getString ( "lifecycleBase.invalidTransition", type, this.toString(), this.state );
        throw new LifecycleException ( msg );
    }
    private void handleSubClassException ( Throwable t, final String key, final Object... args ) throws LifecycleException {
        ExceptionUtils.handleThrowable ( t );
        this.setStateInternal ( LifecycleState.FAILED, null, false );
        final String msg = LifecycleBase.sm.getString ( key, args );
        if ( this.getThrowOnFailure() ) {
            if ( ! ( t instanceof LifecycleException ) ) {
                t = new LifecycleException ( msg, t );
            }
            throw ( LifecycleException ) t;
        }
        LifecycleBase.log.error ( msg, t );
    }
    static {
        log = LogFactory.getLog ( LifecycleBase.class );
        sm = StringManager.getManager ( LifecycleBase.class );
    }
}
