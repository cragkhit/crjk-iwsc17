package org.apache.catalina.ha.session;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.catalina.Cluster;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Loader;
import org.apache.catalina.SessionIdGenerator;
import org.apache.catalina.Valve;
import org.apache.catalina.ha.CatalinaCluster;
import org.apache.catalina.ha.ClusterManager;
import org.apache.catalina.ha.tcp.ReplicationValve;
import org.apache.catalina.session.ManagerBase;
import org.apache.catalina.tribes.io.ReplicationStream;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public abstract class ClusterManagerBase extends ManagerBase implements ClusterManager {
    private final Log log = LogFactory.getLog ( ClusterManagerBase.class );
    protected CatalinaCluster cluster = null;
    private boolean notifyListenersOnReplication = true;
    private volatile ReplicationValve replicationValve = null ;
    private boolean recordAllActions = false;
    @Override
    public CatalinaCluster getCluster() {
        return cluster;
    }
    @Override
    public void setCluster ( CatalinaCluster cluster ) {
        this.cluster = cluster;
    }
    @Override
    public boolean isNotifyListenersOnReplication() {
        return notifyListenersOnReplication;
    }
    public void setNotifyListenersOnReplication ( boolean notifyListenersOnReplication ) {
        this.notifyListenersOnReplication = notifyListenersOnReplication;
    }
    public boolean isRecordAllActions() {
        return recordAllActions;
    }
    public void setRecordAllActions ( boolean recordAllActions ) {
        this.recordAllActions = recordAllActions;
    }
    public static ClassLoader[] getClassLoaders ( Context context ) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        Loader loader = context.getLoader();
        ClassLoader classLoader = null;
        if ( loader != null ) {
            classLoader = loader.getClassLoader();
        }
        if ( classLoader == null ) {
            classLoader = tccl;
        }
        if ( classLoader == tccl ) {
            return new ClassLoader[] {classLoader};
        } else {
            return new ClassLoader[] {classLoader, tccl};
        }
    }
    public ClassLoader[] getClassLoaders() {
        return getClassLoaders ( getContext() );
    }
    @Override
    public ReplicationStream getReplicationStream ( byte[] data ) throws IOException {
        return getReplicationStream ( data, 0, data.length );
    }
    @Override
    public ReplicationStream getReplicationStream ( byte[] data, int offset, int length ) throws IOException {
        ByteArrayInputStream fis = new ByteArrayInputStream ( data, offset, length );
        return new ReplicationStream ( fis, getClassLoaders() );
    }
    @Override
    public void load() {
    }
    @Override
    public void unload() {
    }
    protected void clone ( ClusterManagerBase copy ) {
        copy.setName ( "Clone-from-" + getName() );
        copy.setMaxActiveSessions ( getMaxActiveSessions() );
        copy.setProcessExpiresFrequency ( getProcessExpiresFrequency() );
        copy.setNotifyListenersOnReplication ( isNotifyListenersOnReplication() );
        copy.setSessionAttributeNameFilter ( getSessionAttributeNameFilter() );
        copy.setSessionAttributeValueClassNameFilter ( getSessionAttributeValueClassNameFilter() );
        copy.setWarnOnSessionAttributeFilterFailure ( getWarnOnSessionAttributeFilterFailure() );
        copy.setSecureRandomClass ( getSecureRandomClass() );
        copy.setSecureRandomProvider ( getSecureRandomProvider() );
        copy.setSecureRandomAlgorithm ( getSecureRandomAlgorithm() );
        if ( getSessionIdGenerator() != null ) {
            try {
                SessionIdGenerator copyIdGenerator = sessionIdGeneratorClass.newInstance();
                copyIdGenerator.setSessionIdLength ( getSessionIdGenerator().getSessionIdLength() );
                copyIdGenerator.setJvmRoute ( getSessionIdGenerator().getJvmRoute() );
                copy.setSessionIdGenerator ( copyIdGenerator );
            } catch ( InstantiationException | IllegalAccessException e ) {
            }
        }
        copy.setRecordAllActions ( isRecordAllActions() );
    }
    protected void registerSessionAtReplicationValve ( DeltaSession session ) {
        if ( replicationValve == null ) {
            CatalinaCluster cluster = getCluster() ;
            if ( cluster != null ) {
                Valve[] valves = cluster.getValves();
                if ( valves != null && valves.length > 0 ) {
                    for ( int i = 0; replicationValve == null && i < valves.length ; i++ ) {
                        if ( valves[i] instanceof ReplicationValve ) replicationValve =
                                ( ReplicationValve ) valves[i] ;
                    }
                    if ( replicationValve == null && log.isDebugEnabled() ) {
                        log.debug ( "no ReplicationValve found for CrossContext Support" );
                    }
                }
            }
        }
        if ( replicationValve != null ) {
            replicationValve.registerReplicationSession ( session );
        }
    }
    @Override
    protected void startInternal() throws LifecycleException {
        super.startInternal();
        if ( getCluster() == null ) {
            Cluster cluster = getContext().getCluster();
            if ( cluster instanceof CatalinaCluster ) {
                setCluster ( ( CatalinaCluster ) cluster );
            }
        }
        if ( cluster != null ) {
            cluster.registerManager ( this );
        }
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        if ( cluster != null ) {
            cluster.removeManager ( this );
        }
        replicationValve = null;
        super.stopInternal();
    }
}
