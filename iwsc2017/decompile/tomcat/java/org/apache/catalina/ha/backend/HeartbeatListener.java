package org.apache.catalina.ha.backend;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.ContainerEvent;
import org.apache.juli.logging.Log;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.LifecycleListener;
public class HeartbeatListener implements LifecycleListener, ContainerListener {
    private static final Log log;
    private int port;
    private String host;
    private final String ip = "224.0.1.105";
    private final int multiport = 23364;
    private final int ttl = 16;
    private final String proxyList;
    private final String proxyURL = "/HeartbeatListener";
    private CollectedInfo coll;
    private Sender sender;
    public HeartbeatListener() {
        this.port = 0;
        this.host = null;
        this.proxyList = null;
        this.coll = null;
        this.sender = null;
    }
    public String getHost() {
        return this.host;
    }
    public String getGroup() {
        return "224.0.1.105";
    }
    public int getMultiport() {
        return 23364;
    }
    public int getTtl() {
        return 16;
    }
    public String getProxyList() {
        return this.proxyList;
    }
    public String getProxyURL() {
        return "/HeartbeatListener";
    }
    @Override
    public void containerEvent ( final ContainerEvent event ) {
    }
    @Override
    public void lifecycleEvent ( final LifecycleEvent event ) {
        if ( "periodic".equals ( event.getType() ) ) {
            if ( this.sender == null ) {
                if ( this.proxyList == null ) {
                    this.sender = new MultiCastSender();
                } else {
                    this.sender = new TcpSender();
                }
            }
            if ( this.coll == null ) {
                try {
                    this.coll = new CollectedInfo ( this.host, this.port );
                    this.port = this.coll.port;
                    this.host = this.coll.host;
                } catch ( Exception ex ) {
                    HeartbeatListener.log.error ( "Unable to initialize info collection: " + ex );
                    this.coll = null;
                    return;
                }
            }
            try {
                this.sender.init ( this );
            } catch ( Exception ex ) {
                HeartbeatListener.log.error ( "Unable to initialize Sender: " + ex );
                this.sender = null;
                return;
            }
            try {
                this.coll.refresh();
            } catch ( Exception ex ) {
                HeartbeatListener.log.error ( "Unable to collect load information: " + ex );
                this.coll = null;
                return;
            }
            final String output = "v=1&ready=" + this.coll.ready + "&busy=" + this.coll.busy + "&port=" + this.port;
            try {
                this.sender.send ( output );
            } catch ( Exception ex2 ) {
                HeartbeatListener.log.error ( "Unable to send colllected load information: " + ex2 );
            }
        }
    }
    static {
        log = LogFactory.getLog ( HeartbeatListener.class );
    }
}
