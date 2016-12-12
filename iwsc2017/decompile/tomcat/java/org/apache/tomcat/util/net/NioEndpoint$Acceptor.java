package org.apache.tomcat.util.net;
import java.nio.channels.SocketChannel;
import org.apache.tomcat.util.ExceptionUtils;
import java.net.SocketTimeoutException;
import java.io.IOException;
protected class Acceptor extends AbstractEndpoint.Acceptor {
    @Override
    public void run() {
        int errorDelay = 0;
        while ( NioEndpoint.this.running ) {
            while ( NioEndpoint.this.paused && NioEndpoint.this.running ) {
                this.state = AcceptorState.PAUSED;
                try {
                    Thread.sleep ( 50L );
                } catch ( InterruptedException ex ) {}
            }
            if ( !NioEndpoint.this.running ) {
                break;
            }
            this.state = AcceptorState.RUNNING;
            try {
                NioEndpoint.this.countUpOrAwaitConnection();
                SocketChannel socket = null;
                try {
                    socket = NioEndpoint.access$000 ( NioEndpoint.this ).accept();
                } catch ( IOException ioe ) {
                    NioEndpoint.this.countDownConnection();
                    errorDelay = NioEndpoint.this.handleExceptionWithDelay ( errorDelay );
                    throw ioe;
                }
                errorDelay = 0;
                if ( NioEndpoint.this.running && !NioEndpoint.this.paused ) {
                    if ( NioEndpoint.this.setSocketOptions ( socket ) ) {
                        continue;
                    }
                    NioEndpoint.this.countDownConnection();
                    NioEndpoint.access$100 ( NioEndpoint.this, socket );
                } else {
                    NioEndpoint.this.countDownConnection();
                    NioEndpoint.access$100 ( NioEndpoint.this, socket );
                }
            } catch ( SocketTimeoutException ex2 ) {}
            catch ( IOException x ) {
                if ( !NioEndpoint.this.running ) {
                    continue;
                }
                NioEndpoint.access$200().error ( AbstractEndpoint.sm.getString ( "endpoint.accept.fail" ), x );
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                NioEndpoint.access$200().error ( AbstractEndpoint.sm.getString ( "endpoint.accept.fail" ), t );
            }
        }
        this.state = AcceptorState.ENDED;
    }
}
