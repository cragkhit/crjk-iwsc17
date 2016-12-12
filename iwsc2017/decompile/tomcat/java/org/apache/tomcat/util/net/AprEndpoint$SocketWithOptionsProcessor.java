package org.apache.tomcat.util.net;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import org.apache.tomcat.jni.Error;
import org.apache.tomcat.util.ExceptionUtils;
import java.io.IOException;
import java.net.SocketTimeoutException;
import org.apache.tomcat.jni.Poll;
import org.apache.tomcat.jni.OS;
import org.apache.tomcat.jni.Pool;
import java.util.concurrent.atomic.AtomicInteger;
protected class SocketWithOptionsProcessor implements Runnable {
    protected SocketWrapperBase<Long> socket;
    public SocketWithOptionsProcessor ( final SocketWrapperBase<Long> socket ) {
        this.socket = null;
        this.socket = socket;
    }
    @Override
    public void run() {
        synchronized ( this.socket ) {
            if ( !AprEndpoint.this.deferAccept ) {
                if ( AprEndpoint.this.setSocketOptions ( this.socket ) ) {
                    AprEndpoint.this.getPoller().add ( this.socket.getSocket(), AprEndpoint.this.getConnectionTimeout(), 1 );
                } else {
                    AprEndpoint.access$000 ( AprEndpoint.this, this.socket.getSocket() );
                    this.socket = null;
                }
            } else {
                if ( !AprEndpoint.this.setSocketOptions ( this.socket ) ) {
                    AprEndpoint.access$000 ( AprEndpoint.this, this.socket.getSocket() );
                    this.socket = null;
                    return;
                }
                final Handler.SocketState state = AprEndpoint.this.getHandler().process ( this.socket, SocketEvent.OPEN_READ );
                if ( state == Handler.SocketState.CLOSED ) {
                    AprEndpoint.access$000 ( AprEndpoint.this, this.socket.getSocket() );
                    this.socket = null;
                }
            }
        }
    }
}
