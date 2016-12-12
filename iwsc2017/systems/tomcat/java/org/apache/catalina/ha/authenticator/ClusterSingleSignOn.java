package org.apache.catalina.ha.authenticator;
import java.security.Principal;
import org.apache.catalina.Container;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Session;
import org.apache.catalina.SessionListener;
import org.apache.catalina.authenticator.SingleSignOn;
import org.apache.catalina.authenticator.SingleSignOnEntry;
import org.apache.catalina.ha.CatalinaCluster;
import org.apache.catalina.ha.ClusterValve;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.tipis.AbstractReplicatedMap.MapOwner;
import org.apache.catalina.tribes.tipis.ReplicatedMap;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.res.StringManager;
public class ClusterSingleSignOn extends SingleSignOn implements ClusterValve, MapOwner {
    private static final StringManager sm = StringManager.getManager ( ClusterSingleSignOn.class );
    private CatalinaCluster cluster = null;
    @Override
    public CatalinaCluster getCluster() {
        return cluster;
    }
    @Override
    public void setCluster ( CatalinaCluster cluster ) {
        this.cluster = cluster;
    }
    private long rpcTimeout = 15000;
    public long getRpcTimeout() {
        return rpcTimeout;
    }
    public void setRpcTimeout ( long rpcTimeout ) {
        this.rpcTimeout = rpcTimeout;
    }
    private int mapSendOptions =
        Channel.SEND_OPTIONS_SYNCHRONIZED_ACK | Channel.SEND_OPTIONS_USE_ACK;
    public int getMapSendOptions() {
        return mapSendOptions;
    }
    public void setMapSendOptions ( int mapSendOptions ) {
        this.mapSendOptions = mapSendOptions;
    }
    private boolean terminateOnStartFailure = false;
    public boolean getTerminateOnStartFailure() {
        return terminateOnStartFailure;
    }
    public void setTerminateOnStartFailure ( boolean terminateOnStartFailure ) {
        this.terminateOnStartFailure = terminateOnStartFailure;
    }
    @Override
    protected boolean associate ( String ssoId, Session session ) {
        boolean result = super.associate ( ssoId, session );
        if ( result ) {
            ( ( ReplicatedMap<String, SingleSignOnEntry> ) cache ).replicate ( ssoId, true );
        }
        return result;
    }
    @Override
    protected boolean update ( String ssoId, Principal principal, String authType,
                               String username, String password ) {
        boolean result = super.update ( ssoId, principal, authType, username, password );
        if ( result ) {
            ( ( ReplicatedMap<String, SingleSignOnEntry> ) cache ).replicate ( ssoId, true );
        }
        return result;
    }
    @Override
    protected SessionListener getSessionListener ( String ssoId ) {
        return new ClusterSingleSignOnListener ( ssoId );
    }
    @Override
    public void objectMadePrimary ( Object key, Object value ) {
    }
    @Override
    protected synchronized void startInternal() throws LifecycleException {
        try {
            if ( cluster == null ) {
                Container host = getContainer();
                if ( host instanceof Host ) {
                    if ( host.getCluster() instanceof CatalinaCluster ) {
                        setCluster ( ( CatalinaCluster ) host.getCluster() );
                    }
                }
            }
            if ( cluster == null ) {
                throw new LifecycleException ( sm.getString ( "clusterSingleSignOn.nocluster" ) );
            }
            ClassLoader[] cls = new ClassLoader[] { this.getClass().getClassLoader() };
            ReplicatedMap<String, SingleSignOnEntry> cache = new ReplicatedMap<> (
                this, cluster.getChannel(), rpcTimeout, cluster.getClusterName() + "-SSO-cache",
                cls, terminateOnStartFailure );
            cache.setChannelSendOptions ( mapSendOptions );
            this.cache = cache;
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            throw new LifecycleException ( sm.getString ( "clusterSingleSignOn.clusterLoad.fail" ), t );
        }
        super.startInternal();
    }
    @Override
    protected synchronized void stopInternal() throws LifecycleException {
        super.stopInternal();
        if ( getCluster() != null ) {
            ( ( ReplicatedMap<?, ?> ) cache ).breakdown();
        }
    }
}
