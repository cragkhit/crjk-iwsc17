package org.apache.catalina.ha.session;
import org.apache.juli.logging.LogFactory;
import java.util.Iterator;
import org.apache.catalina.ha.ClusterManager;
import java.util.Map;
import org.apache.catalina.ha.ClusterMessage;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.ha.ClusterListener;
public class ClusterSessionListener extends ClusterListener {
    private static final Log log;
    private static final StringManager sm;
    @Override
    public void messageReceived ( final ClusterMessage myobj ) {
        if ( myobj instanceof SessionMessage ) {
            final SessionMessage msg = ( SessionMessage ) myobj;
            final String ctxname = msg.getContextName();
            final Map<String, ClusterManager> managers = this.cluster.getManagers();
            if ( ctxname == null ) {
                for ( final Map.Entry<String, ClusterManager> entry : managers.entrySet() ) {
                    if ( entry.getValue() != null ) {
                        entry.getValue().messageDataReceived ( msg );
                    } else {
                        if ( !ClusterSessionListener.log.isDebugEnabled() ) {
                            continue;
                        }
                        ClusterSessionListener.log.debug ( ClusterSessionListener.sm.getString ( "clusterSessionListener.noManager", entry.getKey() ) );
                    }
                }
            } else {
                final ClusterManager mgr = managers.get ( ctxname );
                if ( mgr != null ) {
                    mgr.messageDataReceived ( msg );
                } else {
                    if ( ClusterSessionListener.log.isWarnEnabled() ) {
                        ClusterSessionListener.log.warn ( ClusterSessionListener.sm.getString ( "clusterSessionListener.noManager", ctxname ) );
                    }
                    if ( msg.getEventType() == 4 ) {
                        final SessionMessage replymsg = new SessionMessageImpl ( ctxname, 16, null, "NO-CONTEXT-MANAGER", "NO-CONTEXT-MANAGER-" + ctxname );
                        this.cluster.send ( replymsg, msg.getAddress() );
                    }
                }
            }
        }
    }
    @Override
    public boolean accept ( final ClusterMessage msg ) {
        return msg instanceof SessionMessage;
    }
    static {
        log = LogFactory.getLog ( ClusterSessionListener.class );
        sm = StringManager.getManager ( ClusterSessionListener.class );
    }
}
