package org.apache.catalina.ha.session;
import org.apache.juli.logging.LogFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Iterator;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.ArrayList;
import java.io.WriteAbortedException;
import java.io.NotSerializableException;
import java.util.concurrent.ConcurrentHashMap;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import org.apache.catalina.SessionListener;
import org.apache.catalina.ha.ClusterMessage;
import org.apache.catalina.ha.CatalinaCluster;
import java.security.Principal;
import java.io.ObjectInputStream;
import java.io.ObjectInput;
import org.apache.catalina.ha.ClusterManager;
import org.apache.catalina.session.ManagerBase;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.apache.catalina.Manager;
import java.util.concurrent.locks.Lock;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.tribes.tipis.ReplicatedMapEntry;
import org.apache.catalina.ha.ClusterSession;
import java.io.Externalizable;
import org.apache.catalina.session.StandardSession;
public class DeltaSession extends StandardSession implements Externalizable, ClusterSession, ReplicatedMapEntry {
    public static final Log log;
    protected static final StringManager sm;
    private transient boolean isPrimarySession;
    private transient DeltaRequest deltaRequest;
    private transient long lastTimeReplicated;
    protected final Lock diffLock;
    private long version;
    public DeltaSession() {
        this ( null );
    }
    public DeltaSession ( final Manager manager ) {
        super ( manager );
        this.isPrimarySession = true;
        this.deltaRequest = null;
        this.lastTimeReplicated = System.currentTimeMillis();
        this.diffLock = new ReentrantReadWriteLock().writeLock();
        this.resetDeltaRequest();
    }
    @Override
    public boolean isDirty() {
        return this.getDeltaRequest().getSize() > 0;
    }
    @Override
    public boolean isDiffable() {
        return true;
    }
    @Override
    public byte[] getDiff() throws IOException {
        this.lock();
        try {
            return this.getDeltaRequest().serialize();
        } finally {
            this.unlock();
        }
    }
    public ClassLoader[] getClassLoaders() {
        if ( this.manager instanceof ClusterManagerBase ) {
            return ( ( ClusterManagerBase ) this.manager ).getClassLoaders();
        }
        if ( this.manager instanceof ManagerBase ) {
            final ManagerBase mb = ( ManagerBase ) this.manager;
            return ClusterManagerBase.getClassLoaders ( mb.getContext() );
        }
        return null;
    }
    @Override
    public void applyDiff ( final byte[] diff, final int offset, final int length ) throws IOException, ClassNotFoundException {
        this.lock();
        try ( final ObjectInputStream stream = ( ( ClusterManager ) this.getManager() ).getReplicationStream ( diff, offset, length ) ) {
            final ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
            try {
                final ClassLoader[] loaders = this.getClassLoaders();
                if ( loaders != null && loaders.length > 0 ) {
                    Thread.currentThread().setContextClassLoader ( loaders[0] );
                }
                this.getDeltaRequest().readExternal ( stream );
                this.getDeltaRequest().execute ( this, ( ( ClusterManager ) this.getManager() ).isNotifyListenersOnReplication() );
            } finally {
                Thread.currentThread().setContextClassLoader ( contextLoader );
            }
        } finally {
            this.unlock();
        }
    }
    @Override
    public void resetDiff() {
        this.resetDeltaRequest();
    }
    @Override
    public void lock() {
        this.diffLock.lock();
    }
    @Override
    public void unlock() {
        this.diffLock.unlock();
    }
    @Override
    public void setOwner ( final Object owner ) {
        if ( owner instanceof ClusterManager && this.getManager() == null ) {
            final ClusterManager cm = ( ClusterManager ) owner;
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
        final long replDelta = System.currentTimeMillis() - this.getLastTimeReplicated();
        return this.maxInactiveInterval >= 0 && replDelta > this.maxInactiveInterval * 1000L;
    }
    @Override
    public void accessEntry() {
        this.access();
        this.setPrimarySession ( false );
        this.endAccess();
    }
    @Override
    public boolean isPrimarySession() {
        return this.isPrimarySession;
    }
    @Override
    public void setPrimarySession ( final boolean primarySession ) {
        this.isPrimarySession = primarySession;
    }
    @Override
    public void setId ( final String id, final boolean notify ) {
        super.setId ( id, notify );
        this.resetDeltaRequest();
    }
    @Override
    public void setId ( final String id ) {
        super.setId ( id, true );
        this.resetDeltaRequest();
    }
    @Override
    public void setMaxInactiveInterval ( final int interval ) {
        this.setMaxInactiveInterval ( interval, true );
    }
    public void setMaxInactiveInterval ( final int interval, final boolean addDeltaRequest ) {
        super.maxInactiveInterval = interval;
        if ( addDeltaRequest && this.deltaRequest != null ) {
            this.lock();
            try {
                this.deltaRequest.setMaxInactiveInterval ( interval );
            } finally {
                this.unlock();
            }
        }
    }
    @Override
    public void setNew ( final boolean isNew ) {
        this.setNew ( isNew, true );
    }
    public void setNew ( final boolean isNew, final boolean addDeltaRequest ) {
        super.setNew ( isNew );
        if ( addDeltaRequest && this.deltaRequest != null ) {
            this.lock();
            try {
                this.deltaRequest.setNew ( isNew );
            } finally {
                this.unlock();
            }
        }
    }
    @Override
    public void setPrincipal ( final Principal principal ) {
        this.setPrincipal ( principal, true );
    }
    public void setPrincipal ( final Principal principal, final boolean addDeltaRequest ) {
        this.lock();
        try {
            super.setPrincipal ( principal );
            if ( addDeltaRequest && this.deltaRequest != null ) {
                this.deltaRequest.setPrincipal ( principal );
            }
        } finally {
            this.unlock();
        }
    }
    @Override
    public void setAuthType ( final String authType ) {
        this.setAuthType ( authType, true );
    }
    public void setAuthType ( final String authType, final boolean addDeltaRequest ) {
        this.lock();
        try {
            super.setAuthType ( authType );
            if ( addDeltaRequest && this.deltaRequest != null ) {
                this.deltaRequest.setAuthType ( authType );
            }
        } finally {
            this.unlock();
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
        if ( DeltaSession.ACTIVITY_CHECK && this.accessCount.get() > 0 ) {
            return true;
        }
        if ( this.maxInactiveInterval > 0 ) {
            final int timeIdle = ( int ) ( this.getIdleTimeInternal() / 1000L );
            if ( this.isPrimarySession() ) {
                if ( timeIdle >= this.maxInactiveInterval ) {
                    this.expire ( true );
                }
            } else if ( timeIdle >= 2 * this.maxInactiveInterval ) {
                this.expire ( true, false );
            }
        }
        return this.isValid;
    }
    @Override
    public void endAccess() {
        super.endAccess();
        if ( this.manager instanceof ClusterManagerBase ) {
            ( ( ClusterManagerBase ) this.manager ).registerSessionAtReplicationValve ( this );
        }
    }
    @Override
    public void expire ( final boolean notify ) {
        this.expire ( notify, true );
    }
    public void expire ( final boolean notify, final boolean notifyCluster ) {
        if ( !this.isValid ) {
            return;
        }
        synchronized ( this ) {
            if ( !this.isValid ) {
                return;
            }
            if ( this.manager == null ) {
                return;
            }
            final String expiredId = this.getIdInternal();
            if ( notifyCluster && expiredId != null && this.manager instanceof DeltaManager ) {
                final DeltaManager dmanager = ( DeltaManager ) this.manager;
                final CatalinaCluster cluster = dmanager.getCluster();
                final ClusterMessage msg = dmanager.requestCompleted ( expiredId, true );
                if ( msg != null ) {
                    cluster.send ( msg );
                }
            }
            super.expire ( notify );
            if ( notifyCluster ) {
                if ( DeltaSession.log.isDebugEnabled() ) {
                    DeltaSession.log.debug ( DeltaSession.sm.getString ( "deltaSession.notifying", ( ( ClusterManager ) this.manager ).getName(), this.isPrimarySession(), expiredId ) );
                }
                if ( this.manager instanceof DeltaManager ) {
                    ( ( DeltaManager ) this.manager ).sessionExpired ( expiredId );
                }
            }
        }
    }
    @Override
    public void recycle() {
        this.lock();
        try {
            super.recycle();
            this.deltaRequest.clear();
        } finally {
            this.unlock();
        }
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append ( "DeltaSession[" );
        sb.append ( this.id );
        sb.append ( "]" );
        return sb.toString();
    }
    @Override
    public void addSessionListener ( final SessionListener listener ) {
        this.addSessionListener ( listener, true );
    }
    public void addSessionListener ( final SessionListener listener, final boolean addDeltaRequest ) {
        this.lock();
        try {
            super.addSessionListener ( listener );
            if ( addDeltaRequest && this.deltaRequest != null && listener instanceof ReplicatedSessionListener ) {
                this.deltaRequest.addSessionListener ( listener );
            }
        } finally {
            this.unlock();
        }
    }
    @Override
    public void removeSessionListener ( final SessionListener listener ) {
        this.removeSessionListener ( listener, true );
    }
    public void removeSessionListener ( final SessionListener listener, final boolean addDeltaRequest ) {
        this.lock();
        try {
            super.removeSessionListener ( listener );
            if ( addDeltaRequest && this.deltaRequest != null && listener instanceof ReplicatedSessionListener ) {
                this.deltaRequest.removeSessionListener ( listener );
            }
        } finally {
            this.unlock();
        }
    }
    @Override
    public void readExternal ( final ObjectInput in ) throws IOException, ClassNotFoundException {
        this.lock();
        try {
            this.readObjectData ( in );
        } finally {
            this.unlock();
        }
    }
    @Override
    public void readObjectData ( final ObjectInputStream stream ) throws ClassNotFoundException, IOException {
        this.doReadObject ( ( ObjectInput ) stream );
    }
    public void readObjectData ( final ObjectInput stream ) throws ClassNotFoundException, IOException {
        this.doReadObject ( stream );
    }
    @Override
    public void writeObjectData ( final ObjectOutputStream stream ) throws IOException {
        this.writeObjectData ( ( ObjectOutput ) stream );
    }
    public void writeObjectData ( final ObjectOutput stream ) throws IOException {
        this.doWriteObject ( stream );
    }
    public void resetDeltaRequest() {
        this.lock();
        try {
            if ( this.deltaRequest == null ) {
                final boolean recordAllActions = this.manager instanceof ClusterManagerBase && ( ( ClusterManagerBase ) this.manager ).isRecordAllActions();
                this.deltaRequest = new DeltaRequest ( this.getIdInternal(), recordAllActions );
            } else {
                this.deltaRequest.reset();
                this.deltaRequest.setSessionId ( this.getIdInternal() );
            }
        } finally {
            this.unlock();
        }
    }
    public DeltaRequest getDeltaRequest() {
        if ( this.deltaRequest == null ) {
            this.resetDeltaRequest();
        }
        return this.deltaRequest;
    }
    @Override
    public void removeAttribute ( final String name, final boolean notify ) {
        this.removeAttribute ( name, notify, true );
    }
    public void removeAttribute ( final String name, final boolean notify, final boolean addDeltaRequest ) {
        if ( !this.isValid() ) {
            throw new IllegalStateException ( DeltaSession.sm.getString ( "standardSession.removeAttribute.ise" ) );
        }
        this.removeAttributeInternal ( name, notify, addDeltaRequest );
    }
    @Override
    public void setAttribute ( final String name, final Object value ) {
        this.setAttribute ( name, value, true, true );
    }
    public void setAttribute ( final String name, final Object value, final boolean notify, final boolean addDeltaRequest ) {
        if ( name == null ) {
            throw new IllegalArgumentException ( DeltaSession.sm.getString ( "standardSession.setAttribute.namenull" ) );
        }
        if ( value == null ) {
            this.removeAttribute ( name );
            return;
        }
        this.lock();
        try {
            super.setAttribute ( name, value, notify );
            if ( addDeltaRequest && this.deltaRequest != null && !this.exclude ( name, value ) ) {
                this.deltaRequest.setAttribute ( name, value );
            }
        } finally {
            this.unlock();
        }
    }
    @Override
    protected void doReadObject ( final ObjectInputStream stream ) throws ClassNotFoundException, IOException {
        this.doReadObject ( ( ObjectInput ) stream );
    }
    private void doReadObject ( final ObjectInput stream ) throws ClassNotFoundException, IOException {
        this.authType = null;
        this.creationTime = ( long ) stream.readObject();
        this.lastAccessedTime = ( long ) stream.readObject();
        this.maxInactiveInterval = ( int ) stream.readObject();
        this.isNew = ( boolean ) stream.readObject();
        this.isValid = ( boolean ) stream.readObject();
        this.thisAccessedTime = ( long ) stream.readObject();
        this.version = ( long ) stream.readObject();
        final boolean hasPrincipal = stream.readBoolean();
        this.principal = null;
        if ( hasPrincipal ) {
            this.principal = ( Principal ) stream.readObject();
        }
        this.id = ( String ) stream.readObject();
        if ( DeltaSession.log.isDebugEnabled() ) {
            DeltaSession.log.debug ( DeltaSession.sm.getString ( "deltaSession.readSession", this.id ) );
        }
        if ( this.attributes == null ) {
            this.attributes = new ConcurrentHashMap<String, Object>();
        }
        int n = ( int ) stream.readObject();
        final boolean isValidSave = this.isValid;
        this.isValid = true;
        for ( int i = 0; i < n; ++i ) {
            final String name = ( String ) stream.readObject();
            Object value;
            try {
                value = stream.readObject();
            } catch ( WriteAbortedException wae ) {
                if ( wae.getCause() instanceof NotSerializableException ) {
                    continue;
                }
                throw wae;
            }
            if ( !this.exclude ( name, value ) ) {
                this.attributes.put ( name, value );
            }
        }
        this.isValid = isValidSave;
        n = ( int ) stream.readObject();
        if ( this.listeners == null || n > 0 ) {
            this.listeners = new ArrayList<SessionListener>();
        }
        for ( int i = 0; i < n; ++i ) {
            final SessionListener listener = ( SessionListener ) stream.readObject();
            this.listeners.add ( listener );
        }
        if ( this.notes == null ) {
            this.notes = new Hashtable<String, Object>();
        }
        this.activate();
    }
    @Override
    public void writeExternal ( final ObjectOutput out ) throws IOException {
        this.lock();
        try {
            this.doWriteObject ( out );
        } finally {
            this.unlock();
        }
    }
    @Override
    protected void doWriteObject ( final ObjectOutputStream stream ) throws IOException {
        this.doWriteObject ( ( ObjectOutput ) stream );
    }
    private void doWriteObject ( final ObjectOutput stream ) throws IOException {
        stream.writeObject ( this.creationTime );
        stream.writeObject ( this.lastAccessedTime );
        stream.writeObject ( this.maxInactiveInterval );
        stream.writeObject ( this.isNew );
        stream.writeObject ( this.isValid );
        stream.writeObject ( this.thisAccessedTime );
        stream.writeObject ( this.version );
        stream.writeBoolean ( this.getPrincipal() instanceof Serializable );
        if ( this.getPrincipal() instanceof Serializable ) {
            stream.writeObject ( this.getPrincipal() );
        }
        stream.writeObject ( this.id );
        if ( DeltaSession.log.isDebugEnabled() ) {
            DeltaSession.log.debug ( DeltaSession.sm.getString ( "deltaSession.writeSession", this.id ) );
        }
        final String[] keys = this.keys();
        final ArrayList<String> saveNames = new ArrayList<String>();
        final ArrayList<Object> saveValues = new ArrayList<Object>();
        for ( int i = 0; i < keys.length; ++i ) {
            Object value = null;
            value = this.attributes.get ( keys[i] );
            if ( value != null && !this.exclude ( keys[i], value ) && this.isAttributeDistributable ( keys[i], value ) ) {
                saveNames.add ( keys[i] );
                saveValues.add ( value );
            }
        }
        final int n = saveNames.size();
        stream.writeObject ( n );
        for ( int j = 0; j < n; ++j ) {
            stream.writeObject ( saveNames.get ( j ) );
            try {
                stream.writeObject ( saveValues.get ( j ) );
            } catch ( NotSerializableException e ) {
                DeltaSession.log.error ( DeltaSession.sm.getString ( "standardSession.notSerializable", saveNames.get ( j ), this.id ), e );
            }
        }
        final ArrayList<SessionListener> saveListeners = new ArrayList<SessionListener>();
        for ( final SessionListener listener : this.listeners ) {
            if ( listener instanceof ReplicatedSessionListener ) {
                saveListeners.add ( listener );
            }
        }
        stream.writeObject ( saveListeners.size() );
        for ( final SessionListener listener : saveListeners ) {
            stream.writeObject ( listener );
        }
    }
    protected void removeAttributeInternal ( final String name, final boolean notify, final boolean addDeltaRequest ) {
        this.lock();
        try {
            final Object value = this.attributes.get ( name );
            if ( value == null ) {
                return;
            }
            super.removeAttributeInternal ( name, notify );
            if ( addDeltaRequest && this.deltaRequest != null && !this.exclude ( name, null ) ) {
                this.deltaRequest.removeAttribute ( name );
            }
        } finally {
            this.unlock();
        }
    }
    @Override
    public long getLastTimeReplicated() {
        return this.lastTimeReplicated;
    }
    @Override
    public long getVersion() {
        return this.version;
    }
    @Override
    public void setLastTimeReplicated ( final long lastTimeReplicated ) {
        this.lastTimeReplicated = lastTimeReplicated;
    }
    @Override
    public void setVersion ( final long version ) {
        this.version = version;
    }
    protected void setAccessCount ( final int count ) {
        if ( this.accessCount == null && DeltaSession.ACTIVITY_CHECK ) {
            this.accessCount = new AtomicInteger();
        }
        if ( this.accessCount != null ) {
            super.accessCount.set ( count );
        }
    }
    static {
        log = LogFactory.getLog ( DeltaSession.class );
        sm = StringManager.getManager ( DeltaSession.class );
    }
}
