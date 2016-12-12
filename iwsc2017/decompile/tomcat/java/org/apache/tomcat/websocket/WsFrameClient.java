package org.apache.tomcat.websocket;
import java.io.EOFException;
import javax.websocket.CloseReason;
import java.io.IOException;
import org.apache.juli.logging.LogFactory;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
public class WsFrameClient extends WsFrameBase {
    private final Log log;
    private static final StringManager sm;
    private final AsyncChannelWrapper channel;
    private final CompletionHandler<Integer, Void> handler;
    private volatile ByteBuffer response;
    public WsFrameClient ( final ByteBuffer response, final AsyncChannelWrapper channel, final WsSession wsSession, final Transformation transformation ) {
        super ( wsSession, transformation );
        this.log = LogFactory.getLog ( WsFrameClient.class );
        this.response = response;
        this.channel = channel;
        this.handler = new WsFrameClientCompletionHandler();
    }
    void startInputProcessing() {
        try {
            this.processSocketRead();
        } catch ( IOException e ) {
            this.close ( e );
        }
    }
    private void processSocketRead() throws IOException {
        while ( this.response.hasRemaining() ) {
            this.inputBuffer.mark();
            this.inputBuffer.position ( this.inputBuffer.limit() ).limit ( this.inputBuffer.capacity() );
            final int toCopy = Math.min ( this.response.remaining(), this.inputBuffer.remaining() );
            final int orgLimit = this.response.limit();
            this.response.limit ( this.response.position() + toCopy );
            this.inputBuffer.put ( this.response );
            this.response.limit ( orgLimit );
            this.inputBuffer.limit ( this.inputBuffer.position() ).reset();
            this.processInputBuffer();
        }
        this.response.clear();
        if ( this.isOpen() ) {
            this.channel.read ( this.response, ( Object ) null, this.handler );
        }
    }
    private final void close ( final Throwable t ) {
        CloseReason cr;
        if ( t instanceof WsIOException ) {
            cr = ( ( WsIOException ) t ).getCloseReason();
        } else {
            cr = new CloseReason ( ( CloseReason.CloseCode ) CloseReason.CloseCodes.CLOSED_ABNORMALLY, t.getMessage() );
        }
        try {
            this.wsSession.close ( cr );
        } catch ( IOException ex ) {}
    }
    @Override
    protected boolean isMasked() {
        return false;
    }
    @Override
    protected Log getLog() {
        return this.log;
    }
    static {
        sm = StringManager.getManager ( WsFrameClient.class );
    }
    private class WsFrameClientCompletionHandler implements CompletionHandler<Integer, Void> {
        @Override
        public void completed ( final Integer result, final Void attachment ) {
            if ( result == -1 ) {
                if ( WsFrameClient.this.isOpen() ) {
                    WsFrameClient.this.close ( new EOFException() );
                }
                return;
            }
            WsFrameClient.this.response.flip();
            try {
                WsFrameClient.this.processSocketRead();
            } catch ( IOException e ) {
                if ( WsFrameClient.this.isOpen() ) {
                    WsFrameClient.this.log.debug ( WsFrameClient.sm.getString ( "wsFrameClient.ioe" ), e );
                    WsFrameClient.this.close ( e );
                }
            }
        }
        @Override
        public void failed ( final Throwable exc, final Void attachment ) {
            if ( exc instanceof ReadBufferOverflowException ) {
                WsFrameClient.this.response = ByteBuffer.allocate ( ( ( ReadBufferOverflowException ) exc ).getMinBufferSize() );
                WsFrameClient.this.response.flip();
                try {
                    WsFrameClient.this.processSocketRead();
                } catch ( IOException e ) {
                    WsFrameClient.this.close ( e );
                }
            } else {
                WsFrameClient.this.close ( exc );
            }
        }
    }
}
