package org.apache.tomcat.util.net;
import java.nio.channels.SelectionKey;
import org.apache.tomcat.util.ExceptionUtils;
import java.nio.channels.CancelledKeyException;
import java.io.IOException;
protected class SocketProcessor extends SocketProcessorBase<NioChannel> {
    public SocketProcessor ( final SocketWrapperBase<NioChannel> socketWrapper, final SocketEvent event ) {
        super ( socketWrapper, event );
    }
    @Override
    protected void doRun() {
        final NioChannel socket = ( NioChannel ) this.socketWrapper.getSocket();
        final SelectionKey key = socket.getIOChannel().keyFor ( socket.getPoller().getSelector() );
        try {
            int handshake = -1;
            try {
                if ( key != null ) {
                    if ( socket.isHandshakeComplete() ) {
                        handshake = 0;
                    } else if ( this.event == SocketEvent.STOP || this.event == SocketEvent.DISCONNECT || this.event == SocketEvent.ERROR ) {
                        handshake = -1;
                    } else {
                        handshake = socket.handshake ( key.isReadable(), key.isWritable() );
                        this.event = SocketEvent.OPEN_READ;
                    }
                }
            } catch ( IOException x ) {
                handshake = -1;
                if ( NioEndpoint.access$200().isDebugEnabled() ) {
                    NioEndpoint.access$200().debug ( "Error during SSL handshake", x );
                }
            } catch ( CancelledKeyException ckx ) {
                handshake = -1;
            }
            if ( handshake == 0 ) {
                Handler.SocketState state = Handler.SocketState.OPEN;
                if ( this.event == null ) {
                    state = NioEndpoint.this.getHandler().process ( ( SocketWrapperBase<NioChannel> ) this.socketWrapper, SocketEvent.OPEN_READ );
                } else {
                    state = NioEndpoint.this.getHandler().process ( ( SocketWrapperBase<NioChannel> ) this.socketWrapper, this.event );
                }
                if ( state == Handler.SocketState.CLOSED ) {
                    NioEndpoint.access$600 ( NioEndpoint.this, socket, key );
                }
            } else if ( handshake == -1 ) {
                NioEndpoint.access$600 ( NioEndpoint.this, socket, key );
            } else if ( handshake == 1 ) {
                this.socketWrapper.registerReadInterest();
            } else if ( handshake == 4 ) {
                this.socketWrapper.registerWriteInterest();
            }
        } catch ( CancelledKeyException cx ) {
            socket.getPoller().cancelledKey ( key );
        } catch ( VirtualMachineError vme ) {
            ExceptionUtils.handleThrowable ( vme );
        } catch ( Throwable t ) {
            NioEndpoint.access$200().error ( "", t );
            socket.getPoller().cancelledKey ( key );
        } finally {
            this.socketWrapper = null;
            this.event = null;
            if ( NioEndpoint.this.running && !NioEndpoint.this.paused ) {
                NioEndpoint.this.processorCache.push ( ( SocketProcessorBase<S> ) this );
            }
        }
    }
}
