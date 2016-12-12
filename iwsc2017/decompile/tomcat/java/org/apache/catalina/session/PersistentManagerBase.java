package org.apache.catalina.session;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Lifecycle;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.apache.catalina.Session;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.AccessController;
import org.apache.catalina.security.SecurityUtil;
import org.apache.catalina.Manager;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.catalina.Store;
import org.apache.juli.logging.Log;
import org.apache.catalina.StoreManager;
public abstract class PersistentManagerBase extends ManagerBase implements StoreManager {
    private static final Log log;
    private static final String name = "PersistentManagerBase";
    private static final String PERSISTED_LAST_ACCESSED_TIME = "org.apache.catalina.session.PersistentManagerBase.persistedLastAccessedTime";
    protected Store store;
    protected boolean saveOnRestart;
    protected int maxIdleBackup;
    protected int minIdleSwap;
    protected int maxIdleSwap;
    private final Map<String, Object> sessionSwapInLocks;
    public PersistentManagerBase() {
        this.store = null;
        this.saveOnRestart = true;
        this.maxIdleBackup = -1;
        this.minIdleSwap = -1;
        this.maxIdleSwap = -1;
        this.sessionSwapInLocks = new HashMap<String, Object>();
    }
    public int getMaxIdleBackup() {
        return this.maxIdleBackup;
    }
    public void setMaxIdleBackup ( final int backup ) {
        if ( backup == this.maxIdleBackup ) {
            return;
        }
        final int oldBackup = this.maxIdleBackup;
        this.maxIdleBackup = backup;
        this.support.firePropertyChange ( "maxIdleBackup", oldBackup, ( Object ) this.maxIdleBackup );
    }
    public int getMaxIdleSwap() {
        return this.maxIdleSwap;
    }
    public void setMaxIdleSwap ( final int max ) {
        if ( max == this.maxIdleSwap ) {
            return;
        }
        final int oldMaxIdleSwap = this.maxIdleSwap;
        this.maxIdleSwap = max;
        this.support.firePropertyChange ( "maxIdleSwap", oldMaxIdleSwap, ( Object ) this.maxIdleSwap );
    }
    public int getMinIdleSwap() {
        return this.minIdleSwap;
    }
    public void setMinIdleSwap ( final int min ) {
        if ( this.minIdleSwap == min ) {
            return;
        }
        final int oldMinIdleSwap = this.minIdleSwap;
        this.minIdleSwap = min;
        this.support.firePropertyChange ( "minIdleSwap", oldMinIdleSwap, ( Object ) this.minIdleSwap );
    }
    public boolean isLoaded ( final String id ) {
        try {
            if ( super.findSession ( id ) != null ) {
                return true;
            }
        } catch ( IOException e ) {
            PersistentManagerBase.log.error ( "checking isLoaded for id, " + id + ", " + e.getMessage(), e );
        }
        return false;
    }
    @Override
    public String getName() {
        return "PersistentManagerBase";
    }
    public void setStore ( final Store store ) {
        ( this.store = store ).setManager ( this );
    }
    @Override
    public Store getStore() {
        return this.store;
    }
    public boolean getSaveOnRestart() {
        return this.saveOnRestart;
    }
    public void setSaveOnRestart ( final boolean saveOnRestart ) {
        if ( saveOnRestart == this.saveOnRestart ) {
            return;
        }
        final boolean oldSaveOnRestart = this.saveOnRestart;
        this.saveOnRestart = saveOnRestart;
        this.support.firePropertyChange ( "saveOnRestart", oldSaveOnRestart, ( Object ) this.saveOnRestart );
    }
    public void clearStore() {
        if ( this.store == null ) {
            return;
        }
        try {
            if ( SecurityUtil.isPackageProtectionEnabled() ) {
                try {
                    AccessController.doPrivileged ( ( PrivilegedExceptionAction<Object> ) new PrivilegedStoreClear() );
                } catch ( PrivilegedActionException ex ) {
                    final Exception exception = ex.getException();
                    PersistentManagerBase.log.error ( "Exception clearing the Store: " + exception, exception );
                }
            } else {
                this.store.clear();
            }
        } catch ( IOException e ) {
            PersistentManagerBase.log.error ( "Exception clearing the Store: " + e, e );
        }
    }
    @Override
    public void processExpires() {
        final long timeNow = System.currentTimeMillis();
        final Session[] sessions = this.findSessions();
        int expireHere = 0;
        if ( PersistentManagerBase.log.isDebugEnabled() ) {
            PersistentManagerBase.log.debug ( "Start expire sessions " + this.getName() + " at " + timeNow + " sessioncount " + sessions.length );
        }
        for ( int i = 0; i < sessions.length; ++i ) {
            if ( !sessions[i].isValid() ) {
                this.expiredSessions.incrementAndGet();
                ++expireHere;
            }
        }
        this.processPersistenceChecks();
        if ( this.getStore() instanceof StoreBase ) {
            ( ( StoreBase ) this.getStore() ).processExpires();
        }
        final long timeEnd = System.currentTimeMillis();
        if ( PersistentManagerBase.log.isDebugEnabled() ) {
            PersistentManagerBase.log.debug ( "End expire sessions " + this.getName() + " processingTime " + ( timeEnd - timeNow ) + " expired sessions: " + expireHere );
        }
        this.processingTime += timeEnd - timeNow;
    }
    public void processPersistenceChecks() {
        this.processMaxIdleSwaps();
        this.processMaxActiveSwaps();
        this.processMaxIdleBackups();
    }
    @Override
    public Session findSession ( final String id ) throws IOException {
        Session session = super.findSession ( id );
        if ( session != null ) {
            synchronized ( session ) {
                session = super.findSession ( session.getIdInternal() );
                if ( session != null ) {
                    session.access();
                    session.endAccess();
                }
            }
        }
        if ( session != null ) {
            return session;
        }
        session = this.swapIn ( id );
        return session;
    }
    @Override
    public void removeSuper ( final Session session ) {
        super.remove ( session, false );
    }
    @Override
    public void load() {
        this.sessions.clear();
        if ( this.store == null ) {
            return;
        }
        String[] ids = null;
        try {
            Label_0088: {
                if ( SecurityUtil.isPackageProtectionEnabled() ) {
                    try {
                        ids = AccessController.doPrivileged ( ( PrivilegedExceptionAction<String[]> ) new PrivilegedStoreKeys() );
                        break Label_0088;
                    } catch ( PrivilegedActionException ex ) {
                        final Exception exception = ex.getException();
                        PersistentManagerBase.log.error ( "Exception in the Store during load: " + exception, exception );
                        return;
                    }
                }
                ids = this.store.keys();
            }
        } catch ( IOException e ) {
            PersistentManagerBase.log.error ( "Can't load sessions from store, " + e.getMessage(), e );
            return;
        }
        final int n = ids.length;
        if ( n == 0 ) {
            return;
        }
        if ( PersistentManagerBase.log.isDebugEnabled() ) {
            PersistentManagerBase.log.debug ( PersistentManagerBase.sm.getString ( "persistentManager.loading", String.valueOf ( n ) ) );
        }
        for ( int i = 0; i < n; ++i ) {
            try {
                this.swapIn ( ids[i] );
            } catch ( IOException e2 ) {
                PersistentManagerBase.log.error ( "Failed load session from store, " + e2.getMessage(), e2 );
            }
        }
    }
    @Override
    public void remove ( final Session session, final boolean update ) {
        super.remove ( session, update );
        if ( this.store != null ) {
            this.removeSession ( session.getIdInternal() );
        }
    }
    protected void removeSession ( final String id ) {
        try {
            if ( SecurityUtil.isPackageProtectionEnabled() ) {
                try {
                    AccessController.doPrivileged ( ( PrivilegedExceptionAction<Object> ) new PrivilegedStoreRemove ( id ) );
                } catch ( PrivilegedActionException ex ) {
                    final Exception exception = ex.getException();
                    PersistentManagerBase.log.error ( "Exception in the Store during removeSession: " + exception, exception );
                }
            } else {
                this.store.remove ( id );
            }
        } catch ( IOException e ) {
            PersistentManagerBase.log.error ( "Exception removing session  " + e.getMessage(), e );
        }
    }
    @Override
    public void unload() {
        if ( this.store == null ) {
            return;
        }
        final Session[] sessions = this.findSessions();
        final int n = sessions.length;
        if ( n == 0 ) {
            return;
        }
        if ( PersistentManagerBase.log.isDebugEnabled() ) {
            PersistentManagerBase.log.debug ( PersistentManagerBase.sm.getString ( "persistentManager.unloading", String.valueOf ( n ) ) );
        }
        for ( int i = 0; i < n; ++i ) {
            try {
                this.swapOut ( sessions[i] );
            } catch ( IOException ex ) {}
        }
    }
    @Override
    public int getActiveSessionsFull() {
        int result = this.getActiveSessions();
        try {
            result += this.getStore().getSize();
        } catch ( IOException ioe ) {
            PersistentManagerBase.log.warn ( PersistentManagerBase.sm.getString ( "persistentManager.storeSizeException" ) );
        }
        return result;
    }
    @Override
    public Set<String> getSessionIdsFull() {
        final Set<String> sessionIds = new HashSet<String>();
        sessionIds.addAll ( this.sessions.keySet() );
        try {
            final String[] keys;
            final String[] storeKeys = keys = this.getStore().keys();
            for ( final String storeKey : keys ) {
                sessionIds.add ( storeKey );
            }
        } catch ( IOException e ) {
            PersistentManagerBase.log.warn ( PersistentManagerBase.sm.getString ( "persistentManager.storeKeysException" ) );
        }
        return sessionIds;
    }
    protected Session swapIn ( final String id ) throws IOException {
        if ( this.store == null ) {
            return null;
        }
        Object swapInLock = null;
        synchronized ( this ) {
            swapInLock = this.sessionSwapInLocks.get ( id );
            if ( swapInLock == null ) {
                swapInLock = new Object();
                this.sessionSwapInLocks.put ( id, swapInLock );
            }
        }
        Session session = null;
        synchronized ( swapInLock ) {
            session = this.sessions.get ( id );
            if ( session == null ) {
                try {
                    if ( SecurityUtil.isPackageProtectionEnabled() ) {
                        try {
                            session = AccessController.doPrivileged ( ( PrivilegedExceptionAction<Session> ) new PrivilegedStoreLoad ( id ) );
                        } catch ( PrivilegedActionException ex ) {
                            final Exception e = ex.getException();
                            PersistentManagerBase.log.error ( PersistentManagerBase.sm.getString ( "persistentManager.swapInException", id ), e );
                            if ( e instanceof IOException ) {
                                throw ( IOException ) e;
                            }
                            if ( e instanceof ClassNotFoundException ) {
                                throw ( ClassNotFoundException ) e;
                            }
                        }
                    } else {
                        session = this.store.load ( id );
                    }
                } catch ( ClassNotFoundException e2 ) {
                    final String msg = PersistentManagerBase.sm.getString ( "persistentManager.deserializeError", id );
                    PersistentManagerBase.log.error ( msg, e2 );
                    throw new IllegalStateException ( msg, e2 );
                }
                if ( session != null && !session.isValid() ) {
                    PersistentManagerBase.log.error ( PersistentManagerBase.sm.getString ( "persistentManager.swapInInvalid", id ) );
                    session.expire();
                    this.removeSession ( id );
                    session = null;
                }
                if ( session != null ) {
                    if ( PersistentManagerBase.log.isDebugEnabled() ) {
                        PersistentManagerBase.log.debug ( PersistentManagerBase.sm.getString ( "persistentManager.swapIn", id ) );
                    }
                    session.setManager ( this );
                    ( ( StandardSession ) session ).tellNew();
                    this.add ( session );
                    ( ( StandardSession ) session ).activate();
                    session.access();
                    session.endAccess();
                }
            }
        }
        synchronized ( this ) {
            this.sessionSwapInLocks.remove ( id );
        }
        return session;
    }
    protected void swapOut ( final Session session ) throws IOException {
        if ( this.store == null || !session.isValid() ) {
            return;
        }
        ( ( StandardSession ) session ).passivate();
        this.writeSession ( session );
        super.remove ( session, true );
        session.recycle();
    }
    protected void writeSession ( final Session session ) throws IOException {
        if ( this.store == null || !session.isValid() ) {
            return;
        }
        try {
            if ( SecurityUtil.isPackageProtectionEnabled() ) {
                try {
                    AccessController.doPrivileged ( ( PrivilegedExceptionAction<Object> ) new PrivilegedStoreSave ( session ) );
                } catch ( PrivilegedActionException ex ) {
                    final Exception exception = ex.getException();
                    if ( exception instanceof IOException ) {
                        throw ( IOException ) exception;
                    }
                    PersistentManagerBase.log.error ( "Exception in the Store during writeSession: " + exception, exception );
                }
            } else {
                this.store.save ( session );
            }
        } catch ( IOException e ) {
            PersistentManagerBase.log.error ( PersistentManagerBase.sm.getString ( "persistentManager.serializeError", session.getIdInternal(), e ) );
            throw e;
        }
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        super.startInternal();
        if ( this.store == null ) {
            PersistentManagerBase.log.error ( "No Store configured, persistence disabled" );
        } else if ( this.store instanceof Lifecycle ) {
            ( ( Lifecycle ) this.store ).start();
        }
        this.setState ( LifecycleState.STARTING );
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        if ( PersistentManagerBase.log.isDebugEnabled() ) {
            PersistentManagerBase.log.debug ( "Stopping" );
        }
        this.setState ( LifecycleState.STOPPING );
        if ( this.getStore() != null && this.saveOnRestart ) {
            this.unload();
        } else {
            final Session[] sessions = this.findSessions();
            for ( int i = 0; i < sessions.length; ++i ) {
                final StandardSession session = ( StandardSession ) sessions[i];
                if ( session.isValid() ) {
                    session.expire();
                }
            }
        }
        if ( this.getStore() instanceof Lifecycle ) {
            ( ( Lifecycle ) this.getStore() ).stop();
        }
        super.stopInternal();
    }
    protected void processMaxIdleSwaps() {
        if ( !this.getState().isAvailable() || this.maxIdleSwap < 0 ) {
            return;
        }
        final Session[] sessions = this.findSessions();
        if ( this.maxIdleSwap >= 0 ) {
            for ( int i = 0; i < sessions.length; ++i ) {
                final StandardSession session = ( StandardSession ) sessions[i];
                synchronized ( session ) {
                    if ( session.isValid() ) {
                        final int timeIdle = ( int ) ( session.getIdleTimeInternal() / 1000L );
                        if ( timeIdle >= this.maxIdleSwap && timeIdle >= this.minIdleSwap ) {
                            if ( session.accessCount == null || session.accessCount.get() <= 0 ) {
                                if ( PersistentManagerBase.log.isDebugEnabled() ) {
                                    PersistentManagerBase.log.debug ( PersistentManagerBase.sm.getString ( "persistentManager.swapMaxIdle", session.getIdInternal(), timeIdle ) );
                                }
                                try {
                                    this.swapOut ( session );
                                } catch ( IOException ex ) {}
                            }
                        }
                    }
                }
            }
        }
    }
    protected void processMaxActiveSwaps() {
        if ( !this.getState().isAvailable() || this.getMaxActiveSessions() < 0 ) {
            return;
        }
        final Session[] sessions = this.findSessions();
        final int limit = ( int ) ( this.getMaxActiveSessions() * 0.9 );
        if ( limit >= sessions.length ) {
            return;
        }
        if ( PersistentManagerBase.log.isDebugEnabled() ) {
            PersistentManagerBase.log.debug ( PersistentManagerBase.sm.getString ( "persistentManager.tooManyActive", sessions.length ) );
        }
        for ( int toswap = sessions.length - limit, i = 0; i < sessions.length && toswap > 0; ++i ) {
            final StandardSession session = ( StandardSession ) sessions[i];
            synchronized ( session ) {
                final int timeIdle = ( int ) ( session.getIdleTimeInternal() / 1000L );
                if ( timeIdle >= this.minIdleSwap ) {
                    if ( session.accessCount == null || session.accessCount.get() <= 0 ) {
                        if ( PersistentManagerBase.log.isDebugEnabled() ) {
                            PersistentManagerBase.log.debug ( PersistentManagerBase.sm.getString ( "persistentManager.swapTooManyActive", session.getIdInternal(), timeIdle ) );
                        }
                        try {
                            this.swapOut ( session );
                        } catch ( IOException ex ) {}
                        --toswap;
                    }
                }
            }
        }
    }
    protected void processMaxIdleBackups() {
        if ( !this.getState().isAvailable() || this.maxIdleBackup < 0 ) {
            return;
        }
        final Session[] sessions = this.findSessions();
        if ( this.maxIdleBackup >= 0 ) {
            for ( int i = 0; i < sessions.length; ++i ) {
                final StandardSession session = ( StandardSession ) sessions[i];
                synchronized ( session ) {
                    if ( session.isValid() ) {
                        final long lastAccessedTime = session.getLastAccessedTimeInternal();
                        final Long persistedLastAccessedTime = ( Long ) session.getNote ( "org.apache.catalina.session.PersistentManagerBase.persistedLastAccessedTime" );
                        if ( persistedLastAccessedTime == null || lastAccessedTime != persistedLastAccessedTime ) {
                            final int timeIdle = ( int ) ( session.getIdleTimeInternal() / 1000L );
                            if ( timeIdle >= this.maxIdleBackup ) {
                                if ( PersistentManagerBase.log.isDebugEnabled() ) {
                                    PersistentManagerBase.log.debug ( PersistentManagerBase.sm.getString ( "persistentManager.backupMaxIdle", session.getIdInternal(), timeIdle ) );
                                }
                                try {
                                    this.writeSession ( session );
                                } catch ( IOException ex ) {}
                                session.setNote ( "org.apache.catalina.session.PersistentManagerBase.persistedLastAccessedTime", lastAccessedTime );
                            }
                        }
                    }
                }
            }
        }
    }
    static {
        log = LogFactory.getLog ( PersistentManagerBase.class );
    }
    private class PrivilegedStoreClear implements PrivilegedExceptionAction<Void> {
        @Override
        public Void run() throws Exception {
            PersistentManagerBase.this.store.clear();
            return null;
        }
    }
    private class PrivilegedStoreRemove implements PrivilegedExceptionAction<Void> {
        private String id;
        PrivilegedStoreRemove ( final String id ) {
            this.id = id;
        }
        @Override
        public Void run() throws Exception {
            PersistentManagerBase.this.store.remove ( this.id );
            return null;
        }
    }
    private class PrivilegedStoreLoad implements PrivilegedExceptionAction<Session> {
        private String id;
        PrivilegedStoreLoad ( final String id ) {
            this.id = id;
        }
        @Override
        public Session run() throws Exception {
            return PersistentManagerBase.this.store.load ( this.id );
        }
    }
    private class PrivilegedStoreSave implements PrivilegedExceptionAction<Void> {
        private Session session;
        PrivilegedStoreSave ( final Session session ) {
            this.session = session;
        }
        @Override
        public Void run() throws Exception {
            PersistentManagerBase.this.store.save ( this.session );
            return null;
        }
    }
    private class PrivilegedStoreKeys implements PrivilegedExceptionAction<String[]> {
        @Override
        public String[] run() throws Exception {
            return PersistentManagerBase.this.store.keys();
        }
    }
}
