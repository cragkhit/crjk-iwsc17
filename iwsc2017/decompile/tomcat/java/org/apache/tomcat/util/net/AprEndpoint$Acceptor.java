package org.apache.tomcat.util.net;
import org.apache.tomcat.jni.Sockaddr;
import org.apache.tomcat.jni.Error;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.jni.Address;
import org.apache.tomcat.jni.Socket;
import org.apache.juli.logging.LogFactory;
import org.apache.juli.logging.Log;
protected class Acceptor extends AbstractEndpoint.Acceptor {
    private final Log log;
    protected Acceptor() {
        this.log = LogFactory.getLog ( Acceptor.class );
    }
    @Override
    public void run() {
        int errorDelay = 0;
        while ( AprEndpoint.this.running ) {
            while ( AprEndpoint.this.paused && AprEndpoint.this.running ) {
                this.state = AcceptorState.PAUSED;
                try {
                    Thread.sleep ( 50L );
                } catch ( InterruptedException ex ) {}
            }
            if ( !AprEndpoint.this.running ) {
                break;
            }
            this.state = AcceptorState.RUNNING;
            try {
                AprEndpoint.this.countUpOrAwaitConnection();
                long socket = 0L;
                try {
                    socket = Socket.accept ( AprEndpoint.this.serverSock );
                    if ( this.log.isDebugEnabled() ) {
                        final long sa = Address.get ( 1, socket );
                        final Sockaddr addr = Address.getInfo ( sa );
                        this.log.debug ( AbstractEndpoint.sm.getString ( "endpoint.apr.remoteport", socket, addr.port ) );
                    }
                } catch ( Exception e ) {
                    AprEndpoint.this.countDownConnection();
                    errorDelay = AprEndpoint.this.handleExceptionWithDelay ( errorDelay );
                    throw e;
                }
                errorDelay = 0;
                if ( AprEndpoint.this.running && !AprEndpoint.this.paused ) {
                    if ( AprEndpoint.this.processSocketWithOptions ( socket ) ) {
                        continue;
                    }
                    AprEndpoint.access$000 ( AprEndpoint.this, socket );
                } else {
                    AprEndpoint.access$100 ( AprEndpoint.this, socket );
                }
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                if ( !AprEndpoint.this.running ) {
                    continue;
                }
                final String msg = AbstractEndpoint.sm.getString ( "endpoint.accept.fail" );
                if ( t instanceof Error ) {
                    final Error e2 = ( Error ) t;
                    if ( e2.getError() == 233 ) {
                        this.log.warn ( msg, t );
                    } else {
                        this.log.error ( msg, t );
                    }
                } else {
                    this.log.error ( msg, t );
                }
            }
        }
        this.state = AcceptorState.ENDED;
    }
}
