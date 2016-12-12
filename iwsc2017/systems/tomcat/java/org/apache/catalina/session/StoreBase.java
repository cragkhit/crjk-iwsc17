package org.apache.catalina.session;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Manager;
import org.apache.catalina.Store;
import org.apache.catalina.util.CustomObjectInputStream;
import org.apache.catalina.util.LifecycleBase;
import org.apache.tomcat.util.res.StringManager;
public abstract class StoreBase extends LifecycleBase implements Store {
    protected static final String storeName = "StoreBase";
    protected final PropertyChangeSupport support = new PropertyChangeSupport ( this );
    protected static final StringManager sm = StringManager.getManager ( StoreBase.class );
    protected Manager manager;
    public String getStoreName() {
        return storeName;
    }
    @Override
    public void setManager ( Manager manager ) {
        Manager oldManager = this.manager;
        this.manager = manager;
        support.firePropertyChange ( "manager", oldManager, this.manager );
    }
    @Override
    public Manager getManager() {
        return this.manager;
    }
    @Override
    public void addPropertyChangeListener ( PropertyChangeListener listener ) {
        support.addPropertyChangeListener ( listener );
    }
    @Override
    public void removePropertyChangeListener ( PropertyChangeListener listener ) {
        support.removePropertyChangeListener ( listener );
    }
    public String[] expiredKeys() throws IOException {
        return keys();
    }
    public void processExpires() {
        String[] keys = null;
        if ( !getState().isAvailable() ) {
            return;
        }
        try {
            keys = expiredKeys();
        } catch ( IOException e ) {
            manager.getContext().getLogger().error ( "Error getting keys", e );
            return;
        }
        if ( manager.getContext().getLogger().isDebugEnabled() ) {
            manager.getContext().getLogger().debug ( getStoreName() + ": processExpires check number of " + keys.length + " sessions" );
        }
        long timeNow = System.currentTimeMillis();
        for ( int i = 0; i < keys.length; i++ ) {
            try {
                StandardSession session = ( StandardSession ) load ( keys[i] );
                if ( session == null ) {
                    continue;
                }
                int timeIdle = ( int ) ( ( timeNow - session.getThisAccessedTime() ) / 1000L );
                if ( timeIdle < session.getMaxInactiveInterval() ) {
                    continue;
                }
                if ( manager.getContext().getLogger().isDebugEnabled() ) {
                    manager.getContext().getLogger().debug ( getStoreName() + ": processExpires expire store session " + keys[i] );
                }
                boolean isLoaded = false;
                if ( manager instanceof PersistentManagerBase ) {
                    isLoaded = ( ( PersistentManagerBase ) manager ).isLoaded ( keys[i] );
                } else {
                    try {
                        if ( manager.findSession ( keys[i] ) != null ) {
                            isLoaded = true;
                        }
                    } catch ( IOException ioe ) {
                    }
                }
                if ( isLoaded ) {
                    session.recycle();
                } else {
                    session.expire();
                }
                remove ( keys[i] );
            } catch ( Exception e ) {
                manager.getContext().getLogger().error ( "Session: " + keys[i] + "; ", e );
                try {
                    remove ( keys[i] );
                } catch ( IOException e2 ) {
                    manager.getContext().getLogger().error ( "Error removing key", e2 );
                }
            }
        }
    }
    protected ObjectInputStream getObjectInputStream ( InputStream is ) throws IOException {
        BufferedInputStream bis = new BufferedInputStream ( is );
        CustomObjectInputStream ois;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if ( manager instanceof ManagerBase ) {
            ManagerBase managerBase = ( ManagerBase ) manager;
            ois = new CustomObjectInputStream ( bis, classLoader, manager.getContext().getLogger(),
                                                managerBase.getSessionAttributeValueClassNamePattern(),
                                                managerBase.getWarnOnSessionAttributeFilterFailure() );
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
        setState ( LifecycleState.STARTING );
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        setState ( LifecycleState.STOPPING );
    }
    @Override
    protected void destroyInternal() {
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( this.getClass().getName() );
        sb.append ( '[' );
        if ( manager == null ) {
            sb.append ( "Manager is null" );
        } else {
            sb.append ( manager );
        }
        sb.append ( ']' );
        return sb.toString();
    }
}
