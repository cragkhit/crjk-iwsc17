package org.apache.catalina.session;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Session;
import org.apache.catalina.Store;
import org.apache.catalina.StoreManager;
import org.apache.catalina.security.SecurityUtil;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public abstract class PersistentManagerBase extends ManagerBase
    implements StoreManager {
    private static final Log log = LogFactory.getLog ( PersistentManagerBase.class );
    private class PrivilegedStoreClear
        implements PrivilegedExceptionAction<Void> {
        PrivilegedStoreClear() {
        }
        @Override
        public Void run() throws Exception {
            store.clear();
            return null;
        }
    }
    private class PrivilegedStoreRemove
        implements PrivilegedExceptionAction<Void> {
        private String id;
        PrivilegedStoreRemove ( String id ) {
            this.id = id;
        }
        @Override
        public Void run() throws Exception {
            store.remove ( id );
            return null;
        }
    }
    private class PrivilegedStoreLoad
        implements PrivilegedExceptionAction<Session> {
        private String id;
        PrivilegedStoreLoad ( String id ) {
            this.id = id;
        }
        @Override
        public Session run() throws Exception {
            return store.load ( id );
        }
    }
    private class PrivilegedStoreSave
        implements PrivilegedExceptionAction<Void> {
        private Session session;
        PrivilegedStoreSave ( Session session ) {
            this.session = session;
        }
        @Override
        public Void run() throws Exception {
            store.save ( session );
            return null;
        }
    }
    private class PrivilegedStoreKeys
        implements PrivilegedExceptionAction<String[]> {
        PrivilegedStoreKeys() {
        }
        @Override
        public String[] run() throws Exception {
            return store.keys();
        }
    }
    private static final String name = "PersistentManagerBase";
    private static final String PERSISTED_LAST_ACCESSED_TIME =
        "org.apache.catalina.session.PersistentManagerBase.persistedLastAccessedTime";
    protected Store store = null;
    protected boolean saveOnRestart = true;
    protected int maxIdleBackup = -1;
    protected int minIdleSwap = -1;
    protected int maxIdleSwap = -1;
    private final Map<String, Object> sessionSwapInLocks = new HashMap<>();
    public int getMaxIdleBackup() {
        return maxIdleBackup;
    }
    public void setMaxIdleBackup ( int backup ) {
        if ( backup == this.maxIdleBackup ) {
            return;
        }
        int oldBackup = this.maxIdleBackup;
        this.maxIdleBackup = backup;
        support.firePropertyChange ( "maxIdleBackup",
                                     Integer.valueOf ( oldBackup ),
                                     Integer.valueOf ( this.maxIdleBackup ) );
    }
    public int getMaxIdleSwap() {
        return maxIdleSwap;
    }
    public void setMaxIdleSwap ( int max ) {
        if ( max == this.maxIdleSwap ) {
            return;
        }
        int oldMaxIdleSwap = this.maxIdleSwap;
        this.maxIdleSwap = max;
        support.firePropertyChange ( "maxIdleSwap",
                                     Integer.valueOf ( oldMaxIdleSwap ),
                                     Integer.valueOf ( this.maxIdleSwap ) );
    }
    public int getMinIdleSwap() {
        return minIdleSwap;
    }
    public void setMinIdleSwap ( int min ) {
        if ( this.minIdleSwap == min ) {
            return;
        }
        int oldMinIdleSwap = this.minIdleSwap;
        this.minIdleSwap = min;
        support.firePropertyChange ( "minIdleSwap",
                                     Integer.valueOf ( oldMinIdleSwap ),
                                     Integer.valueOf ( this.minIdleSwap ) );
    }
    public boolean isLoaded ( String id ) {
        try {
            if ( super.findSession ( id ) != null ) {
                return true;
            }
        } catch ( IOException e ) {
            log.error ( "checking isLoaded for id, " + id + ", " + e.getMessage(), e );
        }
        return false;
    }
    @Override
    public String getName() {
        return name;
    }
    public void setStore ( Store store ) {
        this.store = store;
        store.setManager ( this );
    }
    @Override
    public Store getStore() {
        return this.store;
    }
    public boolean getSaveOnRestart() {
        return saveOnRestart;
    }
    public void setSaveOnRestart ( boolean saveOnRestart ) {
        if ( saveOnRestart == this.saveOnRestart ) {
            return;
        }
        boolean oldSaveOnRestart = this.saveOnRestart;
        this.saveOnRestart = saveOnRestart;
        support.firePropertyChange ( "saveOnRestart",
                                     Boolean.valueOf ( oldSaveOnRestart ),
                                     Boolean.valueOf ( this.saveOnRestart ) );
    }
    public void clearStore() {
        if ( store == null ) {
            return;
        }
        try {
            if ( SecurityUtil.isPackageProtectionEnabled() ) {
                try {
                    AccessController.doPrivileged ( new PrivilegedStoreClear() );
                } catch ( PrivilegedActionException ex ) {
                    Exception exception = ex.getException();
                    log.error ( "Exception clearing the Store: " + exception,
                                exception );
                }
            } else {
                store.clear();
            }
        } catch ( IOException e ) {
            log.error ( "Exception clearing the Store: " + e, e );
        }
    }
    @Override
    public void processExpires() {
        long timeNow = System.currentTimeMillis();
        Session sessions[] = findSessions();
        int expireHere = 0 ;
        if ( log.isDebugEnabled() ) {
            log.debug ( "Start expire sessions " + getName() + " at " + timeNow + " sessioncount " + sessions.length );
        }
        for ( int i = 0; i < sessions.length; i++ ) {
            if ( !sessions[i].isValid() ) {
                expiredSessions.incrementAndGet();
                expireHere++;
            }
        }
        processPersistenceChecks();
        if ( getStore() instanceof StoreBase ) {
            ( ( StoreBase ) getStore() ).processExpires();
        }
        long timeEnd = System.currentTimeMillis();
        if ( log.isDebugEnabled() ) {
            log.debug ( "End expire sessions " + getName() + " processingTime " + ( timeEnd - timeNow ) + " expired sessions: " + expireHere );
        }
        processingTime += ( timeEnd - timeNow );
    }
    public void processPersistenceChecks() {
        processMaxIdleSwaps();
        processMaxActiveSwaps();
        processMaxIdleBackups();
    }
    @Override
    public Session findSession ( String id ) throws IOException {
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
        session = swapIn ( id );
        return session;
    }
    @Override
    public void removeSuper ( Session session ) {
        super.remove ( session, false );
    }
    @Override
    public void load() {
        sessions.clear();
        if ( store == null ) {
            return;
        }
        String[] ids = null;
        try {
            if ( SecurityUtil.isPackageProtectionEnabled() ) {
                try {
                    ids = AccessController.doPrivileged (
                              new PrivilegedStoreKeys() );
                } catch ( PrivilegedActionException ex ) {
                    Exception exception = ex.getException();
                    log.error ( "Exception in the Store during load: "
                                + exception, exception );
                    return;
                }
            } else {
                ids = store.keys();
            }
        } catch ( IOException e ) {
            log.error ( "Can't load sessions from store, " + e.getMessage(), e );
            return;
        }
        int n = ids.length;
        if ( n == 0 ) {
            return;
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "persistentManager.loading", String.valueOf ( n ) ) );
        }
        for ( int i = 0; i < n; i++ )
            try {
                swapIn ( ids[i] );
            } catch ( IOException e ) {
                log.error ( "Failed load session from store, " + e.getMessage(), e );
            }
    }
    @Override
    public void remove ( Session session, boolean update ) {
        super.remove ( session, update );
        if ( store != null ) {
            removeSession ( session.getIdInternal() );
        }
    }
    protected void removeSession ( String id ) {
        try {
            if ( SecurityUtil.isPackageProtectionEnabled() ) {
                try {
                    AccessController.doPrivileged ( new PrivilegedStoreRemove ( id ) );
                } catch ( PrivilegedActionException ex ) {
                    Exception exception = ex.getException();
                    log.error ( "Exception in the Store during removeSession: "
                                + exception, exception );
                }
            } else {
                store.remove ( id );
            }
        } catch ( IOException e ) {
            log.error ( "Exception removing session  " + e.getMessage(), e );
        }
    }
    @Override
    public void unload() {
        if ( store == null ) {
            return;
        }
        Session sessions[] = findSessions();
        int n = sessions.length;
        if ( n == 0 ) {
            return;
        }
        if ( log.isDebugEnabled() )
            log.debug ( sm.getString ( "persistentManager.unloading",
                                       String.valueOf ( n ) ) );
        for ( int i = 0; i < n; i++ )
            try {
                swapOut ( sessions[i] );
            } catch ( IOException e ) {
            }
    }
    @Override
    public int getActiveSessionsFull() {
        int result = getActiveSessions();
        try {
            result += getStore().getSize();
        } catch ( IOException ioe ) {
            log.warn ( sm.getString ( "persistentManager.storeSizeException" ) );
        }
        return result;
    }
    @Override
    public Set<String> getSessionIdsFull() {
        Set<String> sessionIds = new HashSet<>();
        sessionIds.addAll ( sessions.keySet() );
        String[] storeKeys;
        try {
            storeKeys = getStore().keys();
            for ( String storeKey : storeKeys ) {
                sessionIds.add ( storeKey );
            }
        } catch ( IOException e ) {
            log.warn ( sm.getString ( "persistentManager.storeKeysException" ) );
        }
        return sessionIds;
    }
    protected Session swapIn ( String id ) throws IOException {
        if ( store == null ) {
            return null;
        }
        Object swapInLock = null;
        synchronized ( this ) {
            swapInLock = sessionSwapInLocks.get ( id );
            if ( swapInLock == null ) {
                swapInLock = new Object();
                sessionSwapInLocks.put ( id, swapInLock );
            }
        }
        Session session = null;
        synchronized ( swapInLock ) {
            session = sessions.get ( id );
            if ( session == null ) {
                try {
                    if ( SecurityUtil.isPackageProtectionEnabled() ) {
                        try {
                            session = AccessController.doPrivileged (
                                          new PrivilegedStoreLoad ( id ) );
                        } catch ( PrivilegedActionException ex ) {
                            Exception e = ex.getException();
                            log.error ( sm.getString (
                                            "persistentManager.swapInException", id ),
                                        e );
                            if ( e instanceof IOException ) {
                                throw ( IOException ) e;
                            } else if ( e instanceof ClassNotFoundException ) {
                                throw ( ClassNotFoundException ) e;
                            }
                        }
                    } else {
                        session = store.load ( id );
                    }
                } catch ( ClassNotFoundException e ) {
                    String msg = sm.getString (
                                     "persistentManager.deserializeError", id );
                    log.error ( msg, e );
                    throw new IllegalStateException ( msg, e );
                }
                if ( session != null && !session.isValid() ) {
                    log.error ( sm.getString (
                                    "persistentManager.swapInInvalid", id ) );
                    session.expire();
                    removeSession ( id );
                    session = null;
                }
                if ( session != null ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( sm.getString ( "persistentManager.swapIn", id ) );
                    }
                    session.setManager ( this );
                    ( ( StandardSession ) session ).tellNew();
                    add ( session );
                    ( ( StandardSession ) session ).activate();
                    session.access();
                    session.endAccess();
                }
            }
        }
        synchronized ( this ) {
            sessionSwapInLocks.remove ( id );
        }
        return session;
    }
    protected void swapOut ( Session session ) throws IOException {
        if ( store == null || !session.isValid() ) {
            return;
        }
        ( ( StandardSession ) session ).passivate();
        writeSession ( session );
        super.remove ( session, true );
        session.recycle();
    }
    protected void writeSession ( Session session ) throws IOException {
        if ( store == null || !session.isValid() ) {
            return;
        }
        try {
            if ( SecurityUtil.isPackageProtectionEnabled() ) {
                try {
                    AccessController.doPrivileged ( new PrivilegedStoreSave ( session ) );
                } catch ( PrivilegedActionException ex ) {
                    Exception exception = ex.getException();
                    if ( exception instanceof IOException ) {
                        throw ( IOException ) exception;
                    }
                    log.error ( "Exception in the Store during writeSession: "
                                + exception, exception );
                }
            } else {
                store.save ( session );
            }
        } catch ( IOException e ) {
            log.error ( sm.getString
                        ( "persistentManager.serializeError", session.getIdInternal(), e ) );
            throw e;
        }
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        super.startInternal();
        if ( store == null ) {
            log.error ( "No Store configured, persistence disabled" );
        } else if ( store instanceof Lifecycle ) {
            ( ( Lifecycle ) store ).start();
        }
        setState ( LifecycleState.STARTING );
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        if ( log.isDebugEnabled() ) {
            log.debug ( "Stopping" );
        }
        setState ( LifecycleState.STOPPING );
        if ( getStore() != null && saveOnRestart ) {
            unload();
        } else {
            Session sessions[] = findSessions();
            for ( int i = 0; i < sessions.length; i++ ) {
                StandardSession session = ( StandardSession ) sessions[i];
                if ( !session.isValid() ) {
                    continue;
                }
                session.expire();
            }
        }
        if ( getStore() instanceof Lifecycle ) {
            ( ( Lifecycle ) getStore() ).stop();
        }
        super.stopInternal();
    }
    protected void processMaxIdleSwaps() {
        if ( !getState().isAvailable() || maxIdleSwap < 0 ) {
            return;
        }
        Session sessions[] = findSessions();
        if ( maxIdleSwap >= 0 ) {
            for ( int i = 0; i < sessions.length; i++ ) {
                StandardSession session = ( StandardSession ) sessions[i];
                synchronized ( session ) {
                    if ( !session.isValid() ) {
                        continue;
                    }
                    int timeIdle = ( int ) ( session.getIdleTimeInternal() / 1000L );
                    if ( timeIdle >= maxIdleSwap && timeIdle >= minIdleSwap ) {
                        if ( session.accessCount != null &&
                                session.accessCount.get() > 0 ) {
                            continue;
                        }
                        if ( log.isDebugEnabled() )
                            log.debug ( sm.getString
                                        ( "persistentManager.swapMaxIdle",
                                          session.getIdInternal(),
                                          Integer.valueOf ( timeIdle ) ) );
                        try {
                            swapOut ( session );
                        } catch ( IOException e ) {
                        }
                    }
                }
            }
        }
    }
    protected void processMaxActiveSwaps() {
        if ( !getState().isAvailable() || getMaxActiveSessions() < 0 ) {
            return;
        }
        Session sessions[] = findSessions();
        int limit = ( int ) ( getMaxActiveSessions() * 0.9 );
        if ( limit >= sessions.length ) {
            return;
        }
        if ( log.isDebugEnabled() )
            log.debug ( sm.getString
                        ( "persistentManager.tooManyActive",
                          Integer.valueOf ( sessions.length ) ) );
        int toswap = sessions.length - limit;
        for ( int i = 0; i < sessions.length && toswap > 0; i++ ) {
            StandardSession session = ( StandardSession ) sessions[i];
            synchronized ( session ) {
                int timeIdle = ( int ) ( session.getIdleTimeInternal() / 1000L );
                if ( timeIdle >= minIdleSwap ) {
                    if ( session.accessCount != null &&
                            session.accessCount.get() > 0 ) {
                        continue;
                    }
                    if ( log.isDebugEnabled() )
                        log.debug ( sm.getString
                                    ( "persistentManager.swapTooManyActive",
                                      session.getIdInternal(),
                                      Integer.valueOf ( timeIdle ) ) );
                    try {
                        swapOut ( session );
                    } catch ( IOException e ) {
                    }
                    toswap--;
                }
            }
        }
    }
    protected void processMaxIdleBackups() {
        if ( !getState().isAvailable() || maxIdleBackup < 0 ) {
            return;
        }
        Session sessions[] = findSessions();
        if ( maxIdleBackup >= 0 ) {
            for ( int i = 0; i < sessions.length; i++ ) {
                StandardSession session = ( StandardSession ) sessions[i];
                synchronized ( session ) {
                    if ( !session.isValid() ) {
                        continue;
                    }
                    long lastAccessedTime = session.getLastAccessedTimeInternal();
                    Long persistedLastAccessedTime =
                        ( Long ) session.getNote ( PERSISTED_LAST_ACCESSED_TIME );
                    if ( persistedLastAccessedTime != null &&
                            lastAccessedTime == persistedLastAccessedTime.longValue() ) {
                        continue;
                    }
                    int timeIdle = ( int ) ( session.getIdleTimeInternal() / 1000L );
                    if ( timeIdle >= maxIdleBackup ) {
                        if ( log.isDebugEnabled() )
                            log.debug ( sm.getString
                                        ( "persistentManager.backupMaxIdle",
                                          session.getIdInternal(),
                                          Integer.valueOf ( timeIdle ) ) );
                        try {
                            writeSession ( session );
                        } catch ( IOException e ) {
                        }
                        session.setNote ( PERSISTED_LAST_ACCESSED_TIME,
                                          Long.valueOf ( lastAccessedTime ) );
                    }
                }
            }
        }
    }
}
