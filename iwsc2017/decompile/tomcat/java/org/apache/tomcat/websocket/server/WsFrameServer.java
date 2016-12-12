package org.apache.tomcat.websocket.server;
import org.apache.juli.logging.LogFactory;
import java.nio.ByteBuffer;
import org.apache.tomcat.websocket.WsIOException;
import java.io.IOException;
import org.apache.tomcat.websocket.Transformation;
import org.apache.tomcat.websocket.WsSession;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.tomcat.websocket.WsFrameBase;
public class WsFrameServer extends WsFrameBase {
    private static final Log log;
    private static final StringManager sm;
    private final SocketWrapperBase<?> socketWrapper;
    private final ClassLoader applicationClassLoader;
    public WsFrameServer ( final SocketWrapperBase<?> socketWrapper, final WsSession wsSession, final Transformation transformation, final ClassLoader applicationClassLoader ) {
        super ( wsSession, transformation );
        this.socketWrapper = socketWrapper;
        this.applicationClassLoader = applicationClassLoader;
    }
    public void onDataAvailable() throws IOException {
        if ( WsFrameServer.log.isDebugEnabled() ) {
            WsFrameServer.log.debug ( "wsFrameServer.onDataAvailable" );
        }
        while ( this.isOpen() ) {
            this.inputBuffer.mark();
            this.inputBuffer.position ( this.inputBuffer.limit() ).limit ( this.inputBuffer.capacity() );
            final int read = this.socketWrapper.read ( false, this.inputBuffer );
            this.inputBuffer.limit ( this.inputBuffer.position() ).reset();
            if ( read <= 0 ) {
                return;
            }
            if ( WsFrameServer.log.isDebugEnabled() ) {
                WsFrameServer.log.debug ( WsFrameServer.sm.getString ( "wsFrameServer.bytesRead", Integer.toString ( read ) ) );
            }
            this.processInputBuffer();
        }
    }
    @Override
    protected boolean isMasked() {
        return true;
    }
    @Override
    protected Transformation getTransformation() {
        return super.getTransformation();
    }
    @Override
    protected boolean isOpen() {
        return super.isOpen();
    }
    @Override
    protected Log getLog() {
        return WsFrameServer.log;
    }
    @Override
    protected void sendMessageText ( final boolean last ) throws WsIOException {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader ( this.applicationClassLoader );
            super.sendMessageText ( last );
        } finally {
            Thread.currentThread().setContextClassLoader ( cl );
        }
    }
    @Override
    protected void sendMessageBinary ( final ByteBuffer msg, final boolean last ) throws WsIOException {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader ( this.applicationClassLoader );
            super.sendMessageBinary ( msg, last );
        } finally {
            Thread.currentThread().setContextClassLoader ( cl );
        }
    }
    static {
        log = LogFactory.getLog ( WsFrameServer.class );
        sm = StringManager.getManager ( WsFrameServer.class );
    }
}
