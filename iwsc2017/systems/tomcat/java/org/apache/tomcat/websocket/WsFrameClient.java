package org.apache.tomcat.websocket;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
public class WsFrameClient extends WsFrameBase {
    private final Log log = LogFactory.getLog ( WsFrameClient.class );
    private static final StringManager sm =
        StringManager.getManager ( WsFrameClient.class );
    private final AsyncChannelWrapper channel;
    private final CompletionHandler<Integer, Void> handler;
    private volatile ByteBuffer response;
    public WsFrameClient ( ByteBuffer response, AsyncChannelWrapper channel,
                           WsSession wsSession, Transformation transformation ) {
        super ( wsSession, transformation );
        this.response = response;
        this.channel = channel;
        this.handler = new WsFrameClientCompletionHandler();
    }
    void startInputProcessing() {
        try {
            processSocketRead();
        } catch ( IOException e ) {
            close ( e );
        }
    }
    private void processSocketRead() throws IOException {
        while ( response.hasRemaining() ) {
            inputBuffer.mark();
            inputBuffer.position ( inputBuffer.limit() ).limit ( inputBuffer.capacity() );
            int toCopy = Math.min ( response.remaining(), inputBuffer.remaining() );
            int orgLimit = response.limit();
            response.limit ( response.position() + toCopy );
            inputBuffer.put ( response );
            response.limit ( orgLimit );
            inputBuffer.limit ( inputBuffer.position() ).reset();
            processInputBuffer();
        }
        response.clear();
        if ( isOpen() ) {
            channel.read ( response, null, handler );
        }
    }
    private final void close ( Throwable t ) {
        CloseReason cr;
        if ( t instanceof WsIOException ) {
            cr = ( ( WsIOException ) t ).getCloseReason();
        } else {
            cr = new CloseReason (
                CloseCodes.CLOSED_ABNORMALLY, t.getMessage() );
        }
        try {
            wsSession.close ( cr );
        } catch ( IOException ignore ) {
        }
    }
    @Override
    protected boolean isMasked() {
        return false;
    }
    @Override
    protected Log getLog() {
        return log;
    }
    private class WsFrameClientCompletionHandler
        implements CompletionHandler<Integer, Void> {
        @Override
        public void completed ( Integer result, Void attachment ) {
            if ( result.intValue() == -1 ) {
                if ( isOpen() ) {
                    close ( new EOFException() );
                }
                return;
            }
            response.flip();
            try {
                processSocketRead();
            } catch ( IOException e ) {
                if ( isOpen() ) {
                    log.debug ( sm.getString ( "wsFrameClient.ioe" ), e );
                    close ( e );
                }
            }
        }
        @Override
        public void failed ( Throwable exc, Void attachment ) {
            if ( exc instanceof ReadBufferOverflowException ) {
                response = ByteBuffer.allocate (
                               ( ( ReadBufferOverflowException ) exc ).getMinBufferSize() );
                response.flip();
                try {
                    processSocketRead();
                } catch ( IOException e ) {
                    close ( e );
                }
            } else {
                close ( exc );
            }
        }
    }
}
