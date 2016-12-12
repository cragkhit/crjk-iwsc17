package org.apache.tomcat.util.net;
import org.apache.tomcat.util.ExceptionUtils;
import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;
protected class Acceptor extends AbstractEndpoint.Acceptor {
    @Override
    public void run() {
        int errorDelay = 0;
        while ( Nio2Endpoint.this.running ) {
            while ( Nio2Endpoint.this.paused && Nio2Endpoint.this.running ) {
                this.state = AcceptorState.PAUSED;
                try {
                    Thread.sleep ( 50L );
                } catch ( InterruptedException ex ) {}
            }
            if ( !Nio2Endpoint.this.running ) {
                break;
            }
            this.state = AcceptorState.RUNNING;
            try {
                Nio2Endpoint.this.countUpOrAwaitConnection();
                AsynchronousSocketChannel socket = null;
                try {
                    socket = Nio2Endpoint.access$200 ( Nio2Endpoint.this ).accept().get();
                } catch ( Exception e ) {
                    Nio2Endpoint.this.countDownConnection();
                    if ( Nio2Endpoint.this.running ) {
                        errorDelay = Nio2Endpoint.this.handleExceptionWithDelay ( errorDelay );
                        throw e;
                    }
                    break;
                }
                errorDelay = 0;
                if ( Nio2Endpoint.this.running && !Nio2Endpoint.this.paused ) {
                    if ( Nio2Endpoint.this.setSocketOptions ( socket ) ) {
                        continue;
                    }
                    Nio2Endpoint.this.countDownConnection();
                    try {
                        socket.close();
                    } catch ( IOException ioe ) {
                        if ( !Nio2Endpoint.access$300().isDebugEnabled() ) {
                            continue;
                        }
                        Nio2Endpoint.access$300().debug ( "", ioe );
                    }
                } else {
                    Nio2Endpoint.this.countDownConnection();
                    try {
                        socket.close();
                    } catch ( IOException ioe ) {
                        if ( !Nio2Endpoint.access$300().isDebugEnabled() ) {
                            continue;
                        }
                        Nio2Endpoint.access$300().debug ( "", ioe );
                    }
                }
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                Nio2Endpoint.access$300().error ( AbstractEndpoint.sm.getString ( "endpoint.accept.fail" ), t );
            }
        }
        this.state = AcceptorState.ENDED;
    }
}
