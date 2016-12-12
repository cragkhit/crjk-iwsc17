package org.apache.tomcat.websocket.server;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import javax.websocket.SendHandler;
import javax.websocket.SendResult;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.websocket.Transformation;
import org.apache.tomcat.websocket.WsRemoteEndpointImplBase;
public class WsRemoteEndpointImplServer extends WsRemoteEndpointImplBase {
    private static final StringManager sm =
        StringManager.getManager ( WsRemoteEndpointImplServer.class );
    private static final Log log = LogFactory.getLog ( WsRemoteEndpointImplServer.class );
    private final SocketWrapperBase<?> socketWrapper;
    private final WsWriteTimeout wsWriteTimeout;
    private volatile SendHandler handler = null;
    private volatile ByteBuffer[] buffers = null;
    private volatile long timeoutExpiry = -1;
    private volatile boolean close;
    public WsRemoteEndpointImplServer ( SocketWrapperBase<?> socketWrapper,
                                        WsServerContainer serverContainer ) {
        this.socketWrapper = socketWrapper;
        this.wsWriteTimeout = serverContainer.getTimeout();
    }
    @Override
    protected final boolean isMasked() {
        return false;
    }
    @Override
    protected void doWrite ( SendHandler handler, long blockingWriteTimeoutExpiry,
                             ByteBuffer... buffers ) {
        if ( blockingWriteTimeoutExpiry == -1 ) {
            this.handler = handler;
            this.buffers = buffers;
            onWritePossible ( true );
        } else {
            for ( ByteBuffer buffer : buffers ) {
                long timeout = blockingWriteTimeoutExpiry - System.currentTimeMillis();
                if ( timeout < 0 ) {
                    SendResult sr = new SendResult ( new SocketTimeoutException() );
                    handler.onResult ( sr );
                    return;
                }
                socketWrapper.setWriteTimeout ( timeout );
                try {
                    socketWrapper.write ( true, buffer );
                    timeout = blockingWriteTimeoutExpiry - System.currentTimeMillis();
                    if ( timeout < 0 ) {
                        SendResult sr = new SendResult ( new SocketTimeoutException() );
                        handler.onResult ( sr );
                        return;
                    }
                    socketWrapper.setWriteTimeout ( timeout );
                    socketWrapper.flush ( true );
                    handler.onResult ( SENDRESULT_OK );
                } catch ( IOException e ) {
                    SendResult sr = new SendResult ( e );
                    handler.onResult ( sr );
                }
            }
        }
    }
    public void onWritePossible ( boolean useDispatch ) {
        ByteBuffer[] buffers = this.buffers;
        if ( buffers == null ) {
            return;
        }
        boolean complete = false;
        try {
            socketWrapper.flush ( false );
            while ( socketWrapper.isReadyForWrite() ) {
                complete = true;
                for ( ByteBuffer buffer : buffers ) {
                    if ( buffer.hasRemaining() ) {
                        complete = false;
                        socketWrapper.write ( false, buffer );
                        break;
                    }
                }
                if ( complete ) {
                    socketWrapper.flush ( false );
                    complete = socketWrapper.isReadyForWrite();
                    if ( complete ) {
                        wsWriteTimeout.unregister ( this );
                        clearHandler ( null, useDispatch );
                        if ( close ) {
                            close();
                        }
                    }
                    break;
                }
            }
        } catch ( IOException | IllegalStateException e ) {
            wsWriteTimeout.unregister ( this );
            clearHandler ( e, useDispatch );
            close();
        }
        if ( !complete ) {
            long timeout = getSendTimeout();
            if ( timeout > 0 ) {
                timeoutExpiry = timeout + System.currentTimeMillis();
                wsWriteTimeout.register ( this );
            }
        }
    }
    @Override
    protected void doClose() {
        if ( handler != null ) {
            clearHandler ( new EOFException(), true );
        }
        try {
            socketWrapper.close();
        } catch ( IOException e ) {
            if ( log.isInfoEnabled() ) {
                log.info ( sm.getString ( "wsRemoteEndpointServer.closeFailed" ), e );
            }
        }
        wsWriteTimeout.unregister ( this );
    }
    protected long getTimeoutExpiry() {
        return timeoutExpiry;
    }
    protected void onTimeout ( boolean useDispatch ) {
        if ( handler != null ) {
            clearHandler ( new SocketTimeoutException(), useDispatch );
        }
        close();
    }
    @Override
    protected void setTransformation ( Transformation transformation ) {
        super.setTransformation ( transformation );
    }
    private void clearHandler ( Throwable t, boolean useDispatch ) {
        SendHandler sh = handler;
        handler = null;
        buffers = null;
        if ( sh != null ) {
            if ( useDispatch ) {
                OnResultRunnable r = new OnResultRunnable ( sh, t );
                AbstractEndpoint<?> endpoint = socketWrapper.getEndpoint();
                Executor containerExecutor = endpoint.getExecutor();
                if ( endpoint.isRunning() && containerExecutor != null ) {
                    containerExecutor.execute ( r );
                } else {
                    r.run();
                }
            } else {
                if ( t == null ) {
                    sh.onResult ( new SendResult() );
                } else {
                    sh.onResult ( new SendResult ( t ) );
                }
            }
        }
    }
    private static class OnResultRunnable implements Runnable {
        private final SendHandler sh;
        private final Throwable t;
        private OnResultRunnable ( SendHandler sh, Throwable t ) {
            this.sh = sh;
            this.t = t;
        }
        @Override
        public void run() {
            if ( t == null ) {
                sh.onResult ( new SendResult() );
            } else {
                sh.onResult ( new SendResult ( t ) );
            }
        }
    }
}
