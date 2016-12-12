package org.apache.catalina.ha;
import java.io.Serializable;
import org.apache.catalina.tribes.ChannelListener;
import org.apache.catalina.tribes.Member;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public abstract class ClusterListener implements ChannelListener {
    private static final Log log = LogFactory.getLog ( ClusterListener.class );
    protected CatalinaCluster cluster = null;
    public ClusterListener() {
    }
    public CatalinaCluster getCluster() {
        return cluster;
    }
    public void setCluster ( CatalinaCluster cluster ) {
        if ( log.isDebugEnabled() ) {
            if ( cluster != null )
                log.debug ( "add ClusterListener " + this.toString() +
                            " to cluster" + cluster );
            else
                log.debug ( "remove ClusterListener " + this.toString() +
                            " from cluster" );
        }
        this.cluster = cluster;
    }
    @Override
    public boolean equals ( Object listener ) {
        return super.equals ( listener );
    }
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    @Override
    public final void messageReceived ( Serializable msg, Member member ) {
        if ( msg instanceof ClusterMessage ) {
            messageReceived ( ( ClusterMessage ) msg );
        }
    }
    @Override
    public final boolean accept ( Serializable msg, Member member ) {
        if ( msg instanceof ClusterMessage ) {
            return true;
        }
        return false;
    }
    public abstract void messageReceived ( ClusterMessage msg ) ;
    public abstract boolean accept ( ClusterMessage msg ) ;
}
