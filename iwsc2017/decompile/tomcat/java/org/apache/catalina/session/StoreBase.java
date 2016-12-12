package org.apache.catalina.session;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.util.CustomObjectInputStream;
import java.io.BufferedInputStream;
import java.io.ObjectInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.beans.PropertyChangeListener;
import org.apache.catalina.Manager;
import org.apache.tomcat.util.res.StringManager;
import java.beans.PropertyChangeSupport;
import org.apache.catalina.Store;
import org.apache.catalina.util.LifecycleBase;
public abstract class StoreBase extends LifecycleBase implements Store {
    protected static final String storeName = "StoreBase";
    protected final PropertyChangeSupport support;
    protected static final StringManager sm;
    protected Manager manager;
    public StoreBase() {
        this.support = new PropertyChangeSupport ( this );
    }
    public String getStoreName() {
        return "StoreBase";
    }
    @Override
    public void setManager ( final Manager manager ) {
        final Manager oldManager = this.manager;
        this.manager = manager;
        this.support.firePropertyChange ( "manager", oldManager, this.manager );
    }
    @Override
    public Manager getManager() {
        return this.manager;
    }
    @Override
    public void addPropertyChangeListener ( final PropertyChangeListener listener ) {
        this.support.addPropertyChangeListener ( listener );
    }
    @Override
    public void removePropertyChangeListener ( final PropertyChangeListener listener ) {
        this.support.removePropertyChangeListener ( listener );
    }
    public String[] expiredKeys() throws IOException {
        return this.keys();
    }
    public void processExpires() {
        String[] keys = null;
        if ( !this.getState().isAvailable() ) {
            return;
        }
        try {
            keys = this.expiredKeys();
        } catch ( IOException e ) {
            this.manager.getContext().getLogger().error ( "Error getting keys", e );
            return;
        }
        if ( this.manager.getContext().getLogger().isDebugEnabled() ) {
            this.manager.getContext().getLogger().debug ( this.getStoreName() + ": processExpires check number of " + keys.length + " sessions" );
        }
        final long timeNow = System.currentTimeMillis();
        for ( int i = 0; i < keys.length; ++i ) {
            try {
                final StandardSession session = ( StandardSession ) this.load ( keys[i] );
                if ( session != null ) {
                    final int timeIdle = ( int ) ( ( timeNow - session.getThisAccessedTime() ) / 1000L );
                    if ( timeIdle >= session.getMaxInactiveInterval() ) {
                        if ( this.manager.getContext().getLogger().isDebugEnabled() ) {
                            this.manager.getContext().getLogger().debug ( this.getStoreName() + ": processExpires expire store session " + keys[i] );
                        }
                        boolean isLoaded = false;
                        if ( this.manager instanceof PersistentManagerBase ) {
                            isLoaded = ( ( PersistentManagerBase ) this.manager ).isLoaded ( keys[i] );
                        } else {
                            try {
                                if ( this.manager.findSession ( keys[i] ) != null ) {
                                    isLoaded = true;
                                }
                            } catch ( IOException ex ) {}
                        }
                        if ( isLoaded ) {
                            session.recycle();
                        } else {
                            session.expire();
                        }
                        this.remove ( keys[i] );
                    }
                }
            } catch ( Exception e2 ) {
                this.manager.getContext().getLogger().error ( "Session: " + keys[i] + "; ", e2 );
                try {
                    this.remove ( keys[i] );
                } catch ( IOException e3 ) {
                    this.manager.getContext().getLogger().error ( "Error removing key", e3 );
                }
            }
        }
    }
    protected ObjectInputStream getObjectInputStream ( final InputStream is ) throws IOException {
        final BufferedInputStream bis = new BufferedInputStream ( is );
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        CustomObjectInputStream ois;
        if ( this.manager instanceof ManagerBase ) {
            final ManagerBase managerBase = ( ManagerBase ) this.manager;
            ois = new CustomObjectInputStream ( bis, classLoader, this.manager.getContext().getLogger(), managerBase.getSessionAttributeValueClassNamePattern(), managerBase.getWarnOnSessionAttributeFilterFailure() );
        } else {
            ois = new CustomObjectInputStream ( bis, classLoader );
        }
        return ois;
    }
    @Override
    protected void initInternal() {
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
    protected void destroyInternal() {
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( this.getClass().getName() );
        sb.append ( '[' );
        if ( this.manager == null ) {
            sb.append ( "Manager is null" );
        } else {
            sb.append ( this.manager );
        }
        sb.append ( ']' );
        return sb.toString();
    }
    static {
        sm = StringManager.getManager ( StoreBase.class );
    }
}
