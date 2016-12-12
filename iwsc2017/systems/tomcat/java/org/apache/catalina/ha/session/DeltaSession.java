package org.apache.catalina.ha.session;
import java.io.Externalizable;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.WriteAbortedException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.catalina.Manager;
import org.apache.catalina.SessionListener;
import org.apache.catalina.ha.CatalinaCluster;
import org.apache.catalina.ha.ClusterManager;
import org.apache.catalina.ha.ClusterMessage;
import org.apache.catalina.ha.ClusterSession;
import org.apache.catalina.session.ManagerBase;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.tribes.tipis.ReplicatedMapEntry;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
public class DeltaSession extends StandardSession implements Externalizable, ClusterSession, ReplicatedMapEntry {
    public static final Log log = LogFactory.getLog ( DeltaSession.class );
    protected static final StringManager sm = StringManager.getManager ( DeltaSession.class );
    private transient boolean isPrimarySession = true;
    private transient DeltaRequest deltaRequest = null;
    private transient long lastTimeReplicated = System.currentTimeMillis();
    protected final Lock diffLock = new ReentrantReadWriteLock().writeLock();
    private long version;
    public DeltaSession() {
        this ( null );
    }
    public DeltaSession ( Manager manager ) {
        super ( manager );
        this.resetDeltaRequest();
    }
    @Override
    public boolean isDirty() {
        return getDeltaRequest().getSize() > 0;
    }
    @Override
    public boolean isDiffable() {
        return true;
    }
    @Override
    public byte[] getDiff() throws IOException {
        lock();
        try {
            return getDeltaRequest().serialize();
        } finally {
            unlock();
        }
    }
    public ClassLoader[] getClassLoaders() {
        if ( manager instanceof ClusterManagerBase ) {
            return ( ( ClusterManagerBase ) manager ).getClassLoaders();
        } else if ( manager instanceof ManagerBase ) {
            ManagerBase mb = ( ManagerBase ) manager;
            return ClusterManagerBase.getClassLoaders ( mb.getContext() );
        }
        return null;
    }
    @Override
    public void applyDiff ( byte[] diff, int offset, int length ) throws IOException, ClassNotFoundException {
        lock();
        try ( ObjectInputStream stream = ( ( ClusterManager ) getManager() ).getReplicationStream ( diff, offset, length ) ) {
            ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
            try {
                ClassLoader[] loaders = getClassLoaders();
                if ( loaders != null && loaders.length > 0 ) {
                    Thread.currentThread().setContextClassLoader ( loaders[0] );
                }
                getDeltaRequest().readExternal ( stream );
                getDeltaRequest().execute ( this, ( ( ClusterManager ) getManager() ).isNotifyListenersOnReplication() );
            } finally {
                Thread.currentThread().setContextClassLoader ( contextLoader );
            }
        } finally {
            unlock();
        }
    }
    @Override
    public void resetDiff() {
        resetDeltaRequest();
    }
    @Override
    public void lock() {
        diffLock.lock();
    }
    @Override
    public void unlock() {
        diffLock.unlock();
    }
    @Override
    public void setOwner ( Object owner ) {
        if ( owner instanceof ClusterManager && getManager() == null ) {
            ClusterManager cm = ( ClusterManager ) owner;
            this.setManager ( cm );
            this.setValid ( true );
            this.setPrimarySession ( false );
            this.access();
            this.resetDeltaRequest();
            this.endAccess();
        }
    }
    @Override
    public boolean isAccessReplicate() {
        long replDelta = System.currentTimeMillis() - getLastTimeReplicated();
        if ( maxInactiveInterval >= 0 && replDelta > ( maxInactiveInterval * 1000L ) ) {
            return true;
        }
        return false;
    }
    @Override
    public void accessEntry() {
        this.access();
        this.setPrimarySession ( false );
        this.endAccess();
    }
    @Override
    public boolean isPrimarySession() {
        return isPrimarySession;
    }
    @Override
    public void setPrimarySession ( boolean primarySession ) {
        this.isPrimarySession = primarySession;
    }
    @Override
    public void setId ( String id, boolean notify ) {
        super.setId ( id, notify );
        resetDeltaRequest();
    }
    @Override
    public void setId ( String id ) {
        super.setId ( id, true );
        resetDeltaRequest();
    }
    @Override
    public void setMaxInactiveInterval ( int interval ) {
        this.setMaxInactiveInterval ( interval, true );
    }
    public void setMaxInactiveInterval ( int interval, boolean addDeltaRequest ) {
        super.maxInactiveInterval = interval;
        if ( addDeltaRequest && ( deltaRequest != null ) ) {
            lock();
            try {
                deltaRequest.setMaxInactiveInterval ( interval );
            } finally {
                unlock();
            }
        }
    }
    @Override
    public void setNew ( boolean isNew ) {
        setNew ( isNew, true );
    }
    public void setNew ( boolean isNew, boolean addDeltaRequest ) {
        super.setNew ( isNew );
        if ( addDeltaRequest && ( deltaRequest != null ) ) {
            lock();
            try {
                deltaRequest.setNew ( isNew );
            } finally {
                unlock();
            }
        }
    }
    @Override
    public void setPrincipal ( Principal principal ) {
        setPrincipal ( principal, true );
    }
    public void setPrincipal ( Principal principal, boolean addDeltaRequest ) {
        lock();
        try {
            super.setPrincipal ( principal );
            if ( addDeltaRequest && ( deltaRequest != null ) ) {
                deltaRequest.setPrincipal ( principal );
            }
        } finally {
            unlock();
        }
    }
    @Override
    public void setAuthType ( String authType ) {
        setAuthType ( authType, true );
    }
    public void setAuthType ( String authType, boolean addDeltaRequest ) {
        lock();
        try {
            super.setAuthType ( authType );
            if ( addDeltaRequest && ( deltaRequest != null ) ) {
                deltaRequest.setAuthType ( authType );
            }
        } finally {
            unlock();
        }
    }
    @Override
    public boolean isValid() {
        if ( !this.isValid ) {
            return false;
        }
        if ( this.expiring ) {
            return true;
        }
        if ( ACTIVITY_CHECK && accessCount.get() > 0 ) {
            return true;
        }
        if ( maxInactiveInterval > 0 ) {
            int timeIdle = ( int ) ( getIdleTimeInternal() / 1000L );
            if ( isPrimarySession() ) {
                if ( timeIdle >= maxInactiveInterval ) {
                    expire ( true );
                }
            } else {
                if ( timeIdle >= ( 2 * maxInactiveInterval ) ) {
                    expire ( true, false );
                }
            }
        }
        return ( this.isValid );
    }
    @Override
    public void endAccess() {
        super.endAccess() ;
        if ( manager instanceof ClusterManagerBase ) {
            ( ( ClusterManagerBase ) manager ).registerSessionAtReplicationValve ( this );
        }
    }
    @Override
    public void expire ( boolean notify ) {
        expire ( notify, true );
    }
    public void expire ( boolean notify, boolean notifyCluster ) {
        if ( !isValid ) {
            return;
        }
        synchronized ( this ) {
            if ( !isValid ) {
                return;
            }
            if ( manager == null ) {
                return;
            }
            String expiredId = getIdInternal();
            if ( notifyCluster && expiredId != null &&
                    manager instanceof DeltaManager ) {
                DeltaManager dmanager = ( DeltaManager ) manager;
                CatalinaCluster cluster = dmanager.getCluster();
                ClusterMessage msg = dmanager.requestCompleted ( expiredId, true );
                if ( msg != null ) {
                    cluster.send ( msg );
                }
            }
            super.expire ( notify );
            if ( notifyCluster ) {
                if ( log.isDebugEnabled() )
                    log.debug ( sm.getString ( "deltaSession.notifying",
                                               ( ( ClusterManager ) manager ).getName(),
                                               Boolean.valueOf ( isPrimarySession() ),
                                               expiredId ) );
                if ( manager instanceof DeltaManager ) {
                    ( ( DeltaManager ) manager ).sessionExpired ( expiredId );
                }
            }
        }
    }
    @Override
    public void recycle() {
        lock();
        try {
            super.recycle();
            deltaRequest.clear();
        } finally {
            unlock();
        }
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append ( "DeltaSession[" );
        sb.append ( id );
        sb.append ( "]" );
        return ( sb.toString() );
    }
    @Override
    public void addSessionListener ( SessionListener listener ) {
        addSessionListener ( listener, true );
    }
    public void addSessionListener ( SessionListener listener, boolean addDeltaRequest ) {
        lock();
        try {
            super.addSessionListener ( listener );
            if ( addDeltaRequest && deltaRequest != null && listener instanceof ReplicatedSessionListener ) {
                deltaRequest.addSessionListener ( listener );
            }
        } finally {
            unlock();
        }
    }
    @Override
    public void removeSessionListener ( SessionListener listener ) {
        removeSessionListener ( listener, true );
    }
    public void removeSessionListener ( SessionListener listener, boolean addDeltaRequest ) {
        lock();
        try {
            super.removeSessionListener ( listener );
            if ( addDeltaRequest && deltaRequest != null && listener instanceof ReplicatedSessionListener ) {
                deltaRequest.removeSessionListener ( listener );
            }
        } finally {
            unlock();
        }
    }
    @Override
    public void readExternal ( ObjectInput in ) throws IOException, ClassNotFoundException {
        lock();
        try {
            readObjectData ( in );
        } finally {
            unlock();
        }
    }
    @Override
    public void readObjectData ( ObjectInputStream stream ) throws ClassNotFoundException, IOException {
        doReadObject ( ( ObjectInput ) stream );
    }
    public void readObjectData ( ObjectInput stream ) throws ClassNotFoundException, IOException {
        doReadObject ( stream );
    }
    @Override
    public void writeObjectData ( ObjectOutputStream stream ) throws IOException {
        writeObjectData ( ( ObjectOutput ) stream );
    }
    public void writeObjectData ( ObjectOutput stream ) throws IOException {
        doWriteObject ( stream );
    }
    public void resetDeltaRequest() {
        lock();
        try {
            if ( deltaRequest == null ) {
                boolean recordAllActions = manager instanceof ClusterManagerBase &&
                                           ( ( ClusterManagerBase ) manager ).isRecordAllActions();
                deltaRequest = new DeltaRequest ( getIdInternal(), recordAllActions );
            } else {
                deltaRequest.reset();
                deltaRequest.setSessionId ( getIdInternal() );
            }
        } finally {
            unlock();
        }
    }
    public DeltaRequest getDeltaRequest() {
        if ( deltaRequest == null ) {
            resetDeltaRequest();
        }
        return deltaRequest;
    }
    @Override
    public void removeAttribute ( String name, boolean notify ) {
        removeAttribute ( name, notify, true );
    }
    public void removeAttribute ( String name, boolean notify, boolean addDeltaRequest ) {
        if ( !isValid() ) {
            throw new IllegalStateException ( sm.getString ( "standardSession.removeAttribute.ise" ) );
        }
        removeAttributeInternal ( name, notify, addDeltaRequest );
    }
    @Override
    public void setAttribute ( String name, Object value ) {
        setAttribute ( name, value, true, true );
    }
    public void setAttribute ( String name, Object value, boolean notify, boolean addDeltaRequest ) {
        if ( name == null ) {
            throw new IllegalArgumentException ( sm.getString ( "standardSession.setAttribute.namenull" ) );
        }
        if ( value == null ) {
            removeAttribute ( name );
            return;
        }
        lock();
        try {
            super.setAttribute ( name, value, notify );
            if ( addDeltaRequest && deltaRequest != null && !exclude ( name, value ) ) {
                deltaRequest.setAttribute ( name, value );
            }
        } finally {
            unlock();
        }
    }
    @Override
    protected void doReadObject ( ObjectInputStream stream ) throws ClassNotFoundException, IOException {
        doReadObject ( ( ObjectInput ) stream );
    }
    private void doReadObject ( ObjectInput stream ) throws ClassNotFoundException, IOException {
        authType = null;
        creationTime = ( ( Long ) stream.readObject() ).longValue();
        lastAccessedTime = ( ( Long ) stream.readObject() ).longValue();
        maxInactiveInterval = ( ( Integer ) stream.readObject() ).intValue();
        isNew = ( ( Boolean ) stream.readObject() ).booleanValue();
        isValid = ( ( Boolean ) stream.readObject() ).booleanValue();
        thisAccessedTime = ( ( Long ) stream.readObject() ).longValue();
        version = ( ( Long ) stream.readObject() ).longValue();
        boolean hasPrincipal = stream.readBoolean();
        principal = null;
        if ( hasPrincipal ) {
            principal = ( Principal ) stream.readObject();
        }
        id = ( String ) stream.readObject();
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "deltaSession.readSession", id ) );
        }
        if ( attributes == null ) {
            attributes = new ConcurrentHashMap<>();
        }
        int n = ( ( Integer ) stream.readObject() ).intValue();
        boolean isValidSave = isValid;
        isValid = true;
        for ( int i = 0; i < n; i++ ) {
            String name = ( String ) stream.readObject();
            final Object value;
            try {
                value = stream.readObject();
            } catch ( WriteAbortedException wae ) {
                if ( wae.getCause() instanceof NotSerializableException ) {
                    continue;
                }
                throw wae;
            }
            if ( exclude ( name, value ) ) {
                continue;
            }
            attributes.put ( name, value );
        }
        isValid = isValidSave;
        n = ( ( Integer ) stream.readObject() ).intValue();
        if ( listeners == null || n > 0 ) {
            listeners = new ArrayList<>();
        }
        for ( int i = 0; i < n; i++ ) {
            SessionListener listener = ( SessionListener ) stream.readObject();
            listeners.add ( listener );
        }
        if ( notes == null ) {
            notes = new Hashtable<>();
        }
        activate();
    }
    @Override
    public void writeExternal ( ObjectOutput out ) throws java.io.IOException {
        lock();
        try {
            doWriteObject ( out );
        } finally {
            unlock();
        }
    }
    @Override
    protected void doWriteObject ( ObjectOutputStream stream ) throws IOException {
        doWriteObject ( ( ObjectOutput ) stream );
    }
    private void doWriteObject ( ObjectOutput stream ) throws IOException {
        stream.writeObject ( Long.valueOf ( creationTime ) );
        stream.writeObject ( Long.valueOf ( lastAccessedTime ) );
        stream.writeObject ( Integer.valueOf ( maxInactiveInterval ) );
        stream.writeObject ( Boolean.valueOf ( isNew ) );
        stream.writeObject ( Boolean.valueOf ( isValid ) );
        stream.writeObject ( Long.valueOf ( thisAccessedTime ) );
        stream.writeObject ( Long.valueOf ( version ) );
        stream.writeBoolean ( getPrincipal() instanceof Serializable );
        if ( getPrincipal() instanceof Serializable ) {
            stream.writeObject ( getPrincipal() );
        }
        stream.writeObject ( id );
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "deltaSession.writeSession", id ) );
        }
        String keys[] = keys();
        ArrayList<String> saveNames = new ArrayList<>();
        ArrayList<Object> saveValues = new ArrayList<>();
        for ( int i = 0; i < keys.length; i++ ) {
            Object value = null;
            value = attributes.get ( keys[i] );
            if ( value != null && !exclude ( keys[i], value ) &&
                    isAttributeDistributable ( keys[i], value ) ) {
                saveNames.add ( keys[i] );
                saveValues.add ( value );
            }
        }
        int n = saveNames.size();
        stream.writeObject ( Integer.valueOf ( n ) );
        for ( int i = 0; i < n; i++ ) {
            stream.writeObject ( saveNames.get ( i ) );
            try {
                stream.writeObject ( saveValues.get ( i ) );
            } catch ( NotSerializableException e ) {
                log.error ( sm.getString ( "standardSession.notSerializable", saveNames.get ( i ), id ), e );
            }
        }
        ArrayList<SessionListener> saveListeners = new ArrayList<>();
        for ( SessionListener listener : listeners ) {
            if ( listener instanceof ReplicatedSessionListener ) {
                saveListeners.add ( listener );
            }
        }
        stream.writeObject ( Integer.valueOf ( saveListeners.size() ) );
        for ( SessionListener listener : saveListeners ) {
            stream.writeObject ( listener );
        }
    }
    protected void removeAttributeInternal ( String name, boolean notify,
            boolean addDeltaRequest ) {
        lock();
        try {
            Object value = attributes.get ( name );
            if ( value == null ) {
                return;
            }
            super.removeAttributeInternal ( name, notify );
            if ( addDeltaRequest && deltaRequest != null && !exclude ( name, null ) ) {
                deltaRequest.removeAttribute ( name );
            }
        } finally {
            unlock();
        }
    }
    @Override
    public long getLastTimeReplicated() {
        return lastTimeReplicated;
    }
    @Override
    public long getVersion() {
        return version;
    }
    @Override
    public void setLastTimeReplicated ( long lastTimeReplicated ) {
        this.lastTimeReplicated = lastTimeReplicated;
    }
    @Override
    public void setVersion ( long version ) {
        this.version = version;
    }
    protected void setAccessCount ( int count ) {
        if ( accessCount == null && ACTIVITY_CHECK ) {
            accessCount = new AtomicInteger();
        }
        if ( accessCount != null ) {
            super.accessCount.set ( count );
        }
    }
}
