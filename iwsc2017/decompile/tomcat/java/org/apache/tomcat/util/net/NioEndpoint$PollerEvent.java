package org.apache.tomcat.util.net;
import java.nio.channels.SelectionKey;
import java.nio.channels.CancelledKeyException;
public static class PollerEvent implements Runnable {
    private NioChannel socket;
    private int interestOps;
    private NioSocketWrapper socketWrapper;
    public PollerEvent ( final NioChannel ch, final NioSocketWrapper w, final int intOps ) {
        this.reset ( ch, w, intOps );
    }
    public void reset ( final NioChannel ch, final NioSocketWrapper w, final int intOps ) {
        this.socket = ch;
        this.interestOps = intOps;
        this.socketWrapper = w;
    }
    public void reset() {
        this.reset ( null, null, 0 );
    }
    @Override
    public void run() {
        if ( this.interestOps == 256 ) {
            try {
                this.socket.getIOChannel().register ( this.socket.getPoller().getSelector(), 1, this.socketWrapper );
            } catch ( Exception x ) {
                NioEndpoint.access$200().error ( AbstractEndpoint.sm.getString ( "endpoint.nio.registerFail" ), x );
            }
        } else {
            final SelectionKey key = this.socket.getIOChannel().keyFor ( this.socket.getPoller().getSelector() );
            try {
                if ( key == null ) {
                    this.socket.socketWrapper.getEndpoint().countDownConnection();
                } else {
                    final NioSocketWrapper socketWrapper = ( NioSocketWrapper ) key.attachment();
                    if ( socketWrapper != null ) {
                        final int ops = key.interestOps() | this.interestOps;
                        socketWrapper.interestOps ( ops );
                        key.interestOps ( ops );
                    } else {
                        this.socket.getPoller().cancelledKey ( key );
                    }
                }
            } catch ( CancelledKeyException ckx ) {
                try {
                    this.socket.getPoller().cancelledKey ( key );
                } catch ( Exception ex ) {}
            }
        }
    }
    @Override
    public String toString() {
        return "Poller event: socket [" + this.socket + "], socketWrapper [" + this.socketWrapper + "], interstOps [" + this.interestOps + "]";
    }
}
