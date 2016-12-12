package org.apache.tomcat.util.net;
import org.apache.tomcat.util.ExceptionUtils;
import java.io.IOException;
protected class SocketProcessor extends SocketProcessorBase<Nio2Channel> {
    public SocketProcessor ( final SocketWrapperBase<Nio2Channel> socketWrapper, final SocketEvent event ) {
        super ( socketWrapper, event );
    }
    @Override
    protected void doRun() {
        if ( SocketEvent.OPEN_WRITE != this.event ) {
            ( ( Nio2SocketWrapper ) this.socketWrapper ).releaseReadPending();
        }
        boolean launch = false;
        try {
            int handshake = -1;
            try {
                if ( ( ( Nio2Channel ) this.socketWrapper.getSocket() ).isHandshakeComplete() ) {
                    handshake = 0;
                } else if ( this.event == SocketEvent.STOP || this.event == SocketEvent.DISCONNECT || this.event == SocketEvent.ERROR ) {
                    handshake = -1;
                } else {
                    handshake = ( ( Nio2Channel ) this.socketWrapper.getSocket() ).handshake();
                    this.event = SocketEvent.OPEN_READ;
                }
            } catch ( IOException x ) {
                handshake = -1;
                if ( Nio2Endpoint.access$300().isDebugEnabled() ) {
                    Nio2Endpoint.access$300().debug ( AbstractEndpoint.sm.getString ( "endpoint.err.handshake" ), x );
                }
            }
            if ( handshake == 0 ) {
                Handler.SocketState state = Handler.SocketState.OPEN;
                if ( this.event == null ) {
                    state = Nio2Endpoint.this.getHandler().process ( ( SocketWrapperBase<Nio2Channel> ) this.socketWrapper, SocketEvent.OPEN_READ );
                } else {
                    state = Nio2Endpoint.this.getHandler().process ( ( SocketWrapperBase<Nio2Channel> ) this.socketWrapper, this.event );
                }
                if ( state == Handler.SocketState.CLOSED ) {
                    Nio2Endpoint.this.closeSocket ( ( SocketWrapperBase<Nio2Channel> ) this.socketWrapper );
                    if ( Nio2Endpoint.this.running && !Nio2Endpoint.this.paused && !Nio2Endpoint.access$3000 ( Nio2Endpoint.this ).push ( this.socketWrapper.getSocket() ) ) {
                        ( ( Nio2Channel ) this.socketWrapper.getSocket() ).free();
                    }
                } else if ( state == Handler.SocketState.UPGRADING ) {
                    launch = true;
                }
            } else if ( handshake == -1 ) {
                Nio2Endpoint.this.closeSocket ( ( SocketWrapperBase<Nio2Channel> ) this.socketWrapper );
                if ( Nio2Endpoint.this.running && !Nio2Endpoint.this.paused && !Nio2Endpoint.access$3000 ( Nio2Endpoint.this ).push ( this.socketWrapper.getSocket() ) ) {
                    ( ( Nio2Channel ) this.socketWrapper.getSocket() ).free();
                }
            }
        } catch ( VirtualMachineError vme ) {
            ExceptionUtils.handleThrowable ( vme );
        } catch ( Throwable t ) {
            Nio2Endpoint.access$300().error ( AbstractEndpoint.sm.getString ( "endpoint.processing.fail" ), t );
            if ( this.socketWrapper != null ) {
                Nio2Endpoint.this.closeSocket ( ( SocketWrapperBase<Nio2Channel> ) this.socketWrapper );
            }
        } finally {
            if ( launch ) {
                try {
                    Nio2Endpoint.this.getExecutor().execute ( new SocketProcessor ( ( SocketWrapperBase<Nio2Channel> ) this.socketWrapper, SocketEvent.OPEN_READ ) );
                } catch ( NullPointerException npe ) {
                    if ( Nio2Endpoint.this.running ) {
                        Nio2Endpoint.access$300().error ( AbstractEndpoint.sm.getString ( "endpoint.launch.fail" ), npe );
                    }
                }
            }
            this.socketWrapper = null;
            this.event = null;
            if ( Nio2Endpoint.this.running && !Nio2Endpoint.this.paused ) {
                Nio2Endpoint.this.processorCache.push ( ( SocketProcessorBase<S> ) this );
            }
        }
    }
}
