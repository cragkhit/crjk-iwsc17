package org.apache.catalina.ha.session;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.catalina.DistributedManager;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Session;
import org.apache.catalina.ha.ClusterManager;
import org.apache.catalina.ha.ClusterMessage;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.tipis.AbstractReplicatedMap.MapOwner;
import org.apache.catalina.tribes.tipis.LazyReplicatedMap;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
public class BackupManager extends ClusterManagerBase
    implements MapOwner, DistributedManager {
    private final Log log = LogFactory.getLog ( BackupManager.class );
    protected static final StringManager sm = StringManager.getManager ( BackupManager.class );
    protected static final long DEFAULT_REPL_TIMEOUT = 15000;
    protected String name;
    private int mapSendOptions = Channel.SEND_OPTIONS_SYNCHRONIZED_ACK | Channel.SEND_OPTIONS_USE_ACK;
    private long rpcTimeout = DEFAULT_REPL_TIMEOUT;
    private boolean terminateOnStartFailure = false;
    public BackupManager() {
        super();
    }
    @Override
    public void messageDataReceived ( ClusterMessage msg ) {
    }
    @Override
    public ClusterMessage requestCompleted ( String sessionId ) {
        if ( !getState().isAvailable() ) {
            return null;
        }
        LazyReplicatedMap<String, Session> map =
            ( LazyReplicatedMap<String, Session> ) sessions;
        map.replicate ( sessionId, false );
        return null;
    }
    @Override
    public void objectMadePrimary ( Object key, Object value ) {
        if ( value instanceof DeltaSession ) {
            DeltaSession session = ( DeltaSession ) value;
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
            if ( cluster == null ) {
                throw new LifecycleException ( sm.getString ( "backupManager.noCluster", getName() ) );
            }
            LazyReplicatedMap<String, Session> map = new LazyReplicatedMap<> (
                this, cluster.getChannel(), rpcTimeout, getMapName(),
                getClassLoaders(), terminateOnStartFailure );
            map.setChannelSendOptions ( mapSendOptions );
            this.sessions = map;
        }  catch ( Exception x ) {
            log.error ( sm.getString ( "backupManager.startUnable", getName() ), x );
            throw new LifecycleException ( sm.getString ( "backupManager.startFailed", getName() ), x );
        }
        setState ( LifecycleState.STARTING );
    }
    public String getMapName() {
        String name = cluster.getManagerName ( getName(), this ) + "-" + "map";
        if ( log.isDebugEnabled() ) {
            log.debug ( "Backup manager, Setting map name to:" + name );
        }
        return name;
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "backupManager.stopped", getName() ) );
        }
        setState ( LifecycleState.STOPPING );
        if ( sessions instanceof LazyReplicatedMap ) {
            LazyReplicatedMap<String, Session> map =
                ( LazyReplicatedMap<String, Session> ) sessions;
            map.breakdown();
        }
        super.stopInternal();
    }
    @Override
    public void setName ( String name ) {
        this.name = name;
    }
    public void setMapSendOptions ( int mapSendOptions ) {
        this.mapSendOptions = mapSendOptions;
    }
    public int getMapSendOptions() {
        return mapSendOptions;
    }
    public void setRpcTimeout ( long rpcTimeout ) {
        this.rpcTimeout = rpcTimeout;
    }
    public long getRpcTimeout() {
        return rpcTimeout;
    }
    public void setTerminateOnStartFailure ( boolean terminateOnStartFailure ) {
        this.terminateOnStartFailure = terminateOnStartFailure;
    }
    public boolean isTerminateOnStartFailure() {
        return terminateOnStartFailure;
    }
    @Override
    public String[] getInvalidatedSessions() {
        return new String[0];
    }
    @Override
    public ClusterManager cloneFromTemplate() {
        BackupManager result = new BackupManager();
        clone ( result );
        result.mapSendOptions = mapSendOptions;
        result.rpcTimeout = rpcTimeout;
        result.terminateOnStartFailure = terminateOnStartFailure;
        return result;
    }
    @Override
    public int getActiveSessionsFull() {
        LazyReplicatedMap<String, Session> map =
            ( LazyReplicatedMap<String, Session> ) sessions;
        return map.sizeFull();
    }
    @Override
    public Set<String> getSessionIdsFull() {
        Set<String> sessionIds = new HashSet<>();
        LazyReplicatedMap<String, Session> map =
            ( LazyReplicatedMap<String, Session> ) sessions;
        Iterator<String> keys = map.keySetFull().iterator();
        while ( keys.hasNext() ) {
            sessionIds.add ( keys.next() );
        }
        return sessionIds;
    }
}
