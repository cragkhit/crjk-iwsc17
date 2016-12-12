package org.apache.catalina.ha.session;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import org.apache.catalina.ha.ClusterManager;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.tribes.tipis.LazyReplicatedMap;
import org.apache.catalina.ha.ClusterMessage;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.DistributedManager;
import org.apache.catalina.tribes.tipis.AbstractReplicatedMap;
public class BackupManager extends ClusterManagerBase implements AbstractReplicatedMap.MapOwner, DistributedManager {
    private final Log log;
    protected static final StringManager sm;
    protected static final long DEFAULT_REPL_TIMEOUT = 15000L;
    protected String name;
    private int mapSendOptions;
    private long rpcTimeout;
    private boolean terminateOnStartFailure;
    public BackupManager() {
        this.log = LogFactory.getLog ( BackupManager.class );
        this.mapSendOptions = 6;
        this.rpcTimeout = 15000L;
        this.terminateOnStartFailure = false;
    }
    @Override
    public void messageDataReceived ( final ClusterMessage msg ) {
    }
    @Override
    public ClusterMessage requestCompleted ( final String sessionId ) {
        if ( !this.getState().isAvailable() ) {
            return null;
        }
        final LazyReplicatedMap<String, Session> map = ( LazyReplicatedMap<String, Session> ) ( LazyReplicatedMap ) this.sessions;
        map.replicate ( sessionId, false );
        return null;
    }
    @Override
    public void objectMadePrimary ( final Object key, final Object value ) {
        if ( value instanceof DeltaSession ) {
            final DeltaSession session = ( DeltaSession ) value;
            synchronized ( session ) {
                session.access();
                session.setPrimarySession ( true );
                session.endAccess();
            }
        }
    }
    @Override
    public Session createEmptySession() {
        return new DeltaSession ( this );
    }
    @Override
    public String getName() {
        return this.name;
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        super.startInternal();
        try {
            if ( this.cluster == null ) {
                throw new LifecycleException ( BackupManager.sm.getString ( "backupManager.noCluster", this.getName() ) );
            }
            final LazyReplicatedMap<String, Session> map = new LazyReplicatedMap<String, Session> ( this, this.cluster.getChannel(), this.rpcTimeout, this.getMapName(), this.getClassLoaders(), this.terminateOnStartFailure );
            map.setChannelSendOptions ( this.mapSendOptions );
            this.sessions = map;
        } catch ( Exception x ) {
            this.log.error ( BackupManager.sm.getString ( "backupManager.startUnable", this.getName() ), x );
            throw new LifecycleException ( BackupManager.sm.getString ( "backupManager.startFailed", this.getName() ), x );
        }
        this.setState ( LifecycleState.STARTING );
    }
    public String getMapName() {
        final String name = this.cluster.getManagerName ( this.getName(), this ) + "-map";
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( "Backup manager, Setting map name to:" + name );
        }
        return name;
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        if ( this.log.isDebugEnabled() ) {
            this.log.debug ( BackupManager.sm.getString ( "backupManager.stopped", this.getName() ) );
        }
        this.setState ( LifecycleState.STOPPING );
        if ( this.sessions instanceof LazyReplicatedMap ) {
            final LazyReplicatedMap<String, Session> map = ( LazyReplicatedMap<String, Session> ) ( LazyReplicatedMap ) this.sessions;
            map.breakdown();
        }
        super.stopInternal();
    }
    @Override
    public void setName ( final String name ) {
        this.name = name;
    }
    public void setMapSendOptions ( final int mapSendOptions ) {
        this.mapSendOptions = mapSendOptions;
    }
    public int getMapSendOptions() {
        return this.mapSendOptions;
    }
    public void setRpcTimeout ( final long rpcTimeout ) {
        this.rpcTimeout = rpcTimeout;
    }
    public long getRpcTimeout() {
        return this.rpcTimeout;
    }
    public void setTerminateOnStartFailure ( final boolean terminateOnStartFailure ) {
        this.terminateOnStartFailure = terminateOnStartFailure;
    }
    public boolean isTerminateOnStartFailure() {
        return this.terminateOnStartFailure;
    }
    @Override
    public String[] getInvalidatedSessions() {
        return new String[0];
    }
    @Override
    public ClusterManager cloneFromTemplate() {
        final BackupManager result = new BackupManager();
        this.clone ( result );
        result.mapSendOptions = this.mapSendOptions;
        result.rpcTimeout = this.rpcTimeout;
        result.terminateOnStartFailure = this.terminateOnStartFailure;
        return result;
    }
    @Override
    public int getActiveSessionsFull() {
        final LazyReplicatedMap<String, Session> map = ( LazyReplicatedMap<String, Session> ) ( LazyReplicatedMap ) this.sessions;
        return map.sizeFull();
    }
    @Override
    public Set<String> getSessionIdsFull() {
        final Set<String> sessionIds = new HashSet<String>();
        final LazyReplicatedMap<String, Session> map = ( LazyReplicatedMap<String, Session> ) ( LazyReplicatedMap ) this.sessions;
        final Iterator<String> keys = map.keySetFull().iterator();
        while ( keys.hasNext() ) {
            sessionIds.add ( keys.next() );
        }
        return sessionIds;
    }
    static {
        sm = StringManager.getManager ( BackupManager.class );
    }
}
