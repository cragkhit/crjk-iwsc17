package org.apache.catalina.ha.session;
import java.util.Map;
import org.apache.catalina.ha.ClusterListener;
import org.apache.catalina.ha.ClusterManager;
import org.apache.catalina.ha.ClusterMessage;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
public class ClusterSessionListener extends ClusterListener {
    private static final Log log =
        LogFactory.getLog ( ClusterSessionListener.class );
    private static final StringManager sm = StringManager.getManager ( ClusterSessionListener.class );
    public ClusterSessionListener() {
    }
    @Override
    public void messageReceived ( ClusterMessage myobj ) {
        if ( myobj instanceof SessionMessage ) {
            SessionMessage msg = ( SessionMessage ) myobj;
            String ctxname = msg.getContextName();
            Map<String, ClusterManager> managers = cluster.getManagers() ;
            if ( ctxname == null ) {
                for ( Map.Entry<String, ClusterManager> entry :
                        managers.entrySet() ) {
                    if ( entry.getValue() != null ) {
                        entry.getValue().messageDataReceived ( msg );
                    } else {
                        if ( log.isDebugEnabled() ) {
                            log.debug ( sm.getString ( "clusterSessionListener.noManager", entry.getKey() ) );
                        }
                    }
                }
            } else {
                ClusterManager mgr = managers.get ( ctxname );
                if ( mgr != null ) {
                    mgr.messageDataReceived ( msg );
                } else {
                    if ( log.isWarnEnabled() ) {
                        log.warn ( sm.getString ( "clusterSessionListener.noManager", ctxname ) );
                    }
                    if ( msg.getEventType() == SessionMessage.EVT_GET_ALL_SESSIONS ) {
                        SessionMessage replymsg = new SessionMessageImpl ( ctxname,
                                SessionMessage.EVT_ALL_SESSION_NOCONTEXTMANAGER,
                                null, "NO-CONTEXT-MANAGER", "NO-CONTEXT-MANAGER-" + ctxname );
                        cluster.send ( replymsg, msg.getAddress() );
                    }
                }
            }
        }
        return;
    }
    @Override
    public boolean accept ( ClusterMessage msg ) {
        return ( msg instanceof SessionMessage );
    }
}
