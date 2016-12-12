package org.apache.catalina.ha.backend;
import org.apache.catalina.ContainerEvent;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class HeartbeatListener implements LifecycleListener, ContainerListener {
    private static final Log log = LogFactory.getLog ( HeartbeatListener.class );
    private int port = 0;
    private String host = null;
    private final String ip = "224.0.1.105";
    private final int multiport = 23364;
    private final int ttl = 16;
    public String getHost() {
        return host;
    }
    public String getGroup() {
        return ip;
    }
    public int getMultiport() {
        return multiport;
    }
    public int getTtl() {
        return ttl;
    }
    private final String proxyList = null;
    public String getProxyList() {
        return proxyList;
    }
    private final String proxyURL = "/HeartbeatListener";
    public String getProxyURL() {
        return proxyURL;
    }
    private CollectedInfo coll = null;
    private Sender sender = null;
    @Override
    public void containerEvent ( ContainerEvent event ) {
    }
    @Override
    public void lifecycleEvent ( LifecycleEvent event ) {
        if ( Lifecycle.PERIODIC_EVENT.equals ( event.getType() ) ) {
            if ( sender == null ) {
                if ( proxyList == null ) {
                    sender = new MultiCastSender();
                } else {
                    sender = new TcpSender();
                }
            }
            if ( coll == null ) {
                try {
                    coll = new CollectedInfo ( host, port );
                    this.port = coll.port;
                    this.host = coll.host;
                } catch ( Exception ex ) {
                    log.error ( "Unable to initialize info collection: " + ex );
                    coll = null;
                    return;
                }
            }
            try {
                sender.init ( this );
            } catch ( Exception ex ) {
                log.error ( "Unable to initialize Sender: " + ex );
                sender = null;
                return;
            }
            try {
                coll.refresh();
            } catch ( Exception ex ) {
                log.error ( "Unable to collect load information: " + ex );
                coll = null;
                return;
            }
            String output = "v=1&ready=" + coll.ready + "&busy=" + coll.busy +
                            "&port=" + port;
            try {
                sender.send ( output );
            } catch ( Exception ex ) {
                log.error ( "Unable to send colllected load information: " + ex );
            }
        }
    }
}
