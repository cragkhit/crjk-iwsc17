package org.apache.tomcat.websocket.server;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.websocket.Transformation;
import org.apache.tomcat.websocket.WsFrameBase;
import org.apache.tomcat.websocket.WsIOException;
import org.apache.tomcat.websocket.WsSession;
public class WsFrameServer extends WsFrameBase {
    private static final Log log = LogFactory.getLog ( WsFrameServer.class );
    private static final StringManager sm = StringManager.getManager ( WsFrameServer.class );
    private final SocketWrapperBase<?> socketWrapper;
    private final ClassLoader applicationClassLoader;
    public WsFrameServer ( SocketWrapperBase<?> socketWrapper, WsSession wsSession,
                           Transformation transformation, ClassLoader applicationClassLoader ) {
        super ( wsSession, transformation );
        this.socketWrapper = socketWrapper;
        this.applicationClassLoader = applicationClassLoader;
    }
    public void onDataAvailable() throws IOException {
        if ( log.isDebugEnabled() ) {
            log.debug ( "wsFrameServer.onDataAvailable" );
        }
        while ( isOpen() ) {
            inputBuffer.mark();
            inputBuffer.position ( inputBuffer.limit() ).limit ( inputBuffer.capacity() );
            int read = socketWrapper.read ( false, inputBuffer );
            inputBuffer.limit ( inputBuffer.position() ).reset();
            if ( read <= 0 ) {
                return;
            }
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "wsFrameServer.bytesRead", Integer.toString ( read ) ) );
            }
            processInputBuffer();
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
        return log;
    }
    @Override
    protected void sendMessageText ( boolean last ) throws WsIOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader ( applicationClassLoader );
            super.sendMessageText ( last );
        } finally {
            Thread.currentThread().setContextClassLoader ( cl );
        }
    }
    @Override
    protected void sendMessageBinary ( ByteBuffer msg, boolean last ) throws WsIOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader ( applicationClassLoader );
            super.sendMessageBinary ( msg, last );
        } finally {
            Thread.currentThread().setContextClassLoader ( cl );
        }
    }
}
