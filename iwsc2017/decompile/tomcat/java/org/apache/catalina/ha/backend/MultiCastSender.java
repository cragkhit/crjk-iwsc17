package org.apache.catalina.ha.backend;
import org.apache.juli.logging.LogFactory;
import java.net.DatagramPacket;
import java.nio.charset.StandardCharsets;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.MulticastSocket;
import org.apache.juli.logging.Log;
public class MultiCastSender implements Sender {
    private static final Log log;
    HeartbeatListener config;
    MulticastSocket s;
    InetAddress group;
    public MultiCastSender() {
        this.config = null;
        this.s = null;
        this.group = null;
    }
    @Override
    public void init ( final HeartbeatListener config ) throws Exception {
        this.config = config;
    }
    @Override
    public int send ( final String mess ) throws Exception {
        if ( this.s == null ) {
            try {
                this.group = InetAddress.getByName ( this.config.getGroup() );
                if ( this.config.getHost() != null ) {
                    final InetAddress addr = InetAddress.getByName ( this.config.getHost() );
                    final InetSocketAddress addrs = new InetSocketAddress ( addr, this.config.getMultiport() );
                    this.s = new MulticastSocket ( addrs );
                } else {
                    this.s = new MulticastSocket ( this.config.getMultiport() );
                }
                this.s.setTimeToLive ( this.config.getTtl() );
                this.s.joinGroup ( this.group );
            } catch ( Exception ex ) {
                MultiCastSender.log.error ( "Unable to use multicast: " + ex );
                this.s = null;
                return -1;
            }
        }
        final byte[] buf = mess.getBytes ( StandardCharsets.US_ASCII );
        final DatagramPacket data = new DatagramPacket ( buf, buf.length, this.group, this.config.getMultiport() );
        try {
            this.s.send ( data );
        } catch ( Exception ex2 ) {
            MultiCastSender.log.error ( "Unable to send colllected load information: " + ex2 );
            this.s.close();
            this.s = null;
            return -1;
        }
        return 0;
    }
    static {
        log = LogFactory.getLog ( HeartbeatListener.class );
    }
}
