package org.apache.coyote.http2;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.coyote.ActionCode;
import java.io.IOException;
import org.apache.tomcat.util.net.ApplicationBufferHandler;
import java.nio.ByteBuffer;
import org.apache.coyote.InputBuffer;
class StreamInputBuffer implements InputBuffer {
    private byte[] outBuffer;
    private volatile ByteBuffer inBuffer;
    private volatile boolean readInterest;
    private boolean reset;
    StreamInputBuffer() {
        this.reset = false;
    }
    @Override
    public final int doRead ( final ApplicationBufferHandler applicationBufferHandler ) throws IOException {
        this.ensureBuffersExist();
        int written = -1;
        synchronized ( this.inBuffer ) {
            while ( this.inBuffer.position() == 0 && !Stream.access$800 ( Stream.this ) ) {
                try {
                    if ( Stream.access$500().isDebugEnabled() ) {
                        Stream.access$500().debug ( Stream.access$300().getString ( "stream.inputBuffer.empty" ) );
                    }
                    this.inBuffer.wait();
                    if ( this.reset ) {
                        throw new IOException ( "HTTP/2 Stream reset" );
                    }
                    continue;
                } catch ( InterruptedException e ) {
                    throw new IOException ( e );
                }
                break;
            }
            if ( this.inBuffer.position() > 0 ) {
                this.inBuffer.flip();
                written = this.inBuffer.remaining();
                if ( Stream.access$500().isDebugEnabled() ) {
                    Stream.access$500().debug ( Stream.access$300().getString ( "stream.inputBuffer.copy", Integer.toString ( written ) ) );
                }
                this.inBuffer.get ( this.outBuffer, 0, written );
                this.inBuffer.clear();
            } else {
                if ( Stream.access$800 ( Stream.this ) ) {
                    return -1;
                }
                throw new IllegalStateException();
            }
        }
        applicationBufferHandler.setByteBuffer ( ByteBuffer.wrap ( this.outBuffer, 0, written ) );
        Stream.access$600 ( Stream.this ).writeWindowUpdate ( Stream.this, written, true );
        return written;
    }
    final void registerReadInterest() {
        synchronized ( this.inBuffer ) {
            this.readInterest = true;
        }
    }
    final synchronized boolean isRequestBodyFullyRead() {
        return ( this.inBuffer == null || this.inBuffer.position() == 0 ) && Stream.access$800 ( Stream.this );
    }
    final synchronized int available() {
        if ( this.inBuffer == null ) {
            return 0;
        }
        return this.inBuffer.position();
    }
    final synchronized boolean onDataAvailable() {
        if ( this.readInterest ) {
            if ( Stream.access$500().isDebugEnabled() ) {
                Stream.access$500().debug ( Stream.access$300().getString ( "stream.inputBuffer.dispatch" ) );
            }
            this.readInterest = false;
            Stream.access$900 ( Stream.this ).action ( ActionCode.DISPATCH_READ, null );
            Stream.access$900 ( Stream.this ).action ( ActionCode.DISPATCH_EXECUTE, null );
            return true;
        }
        if ( Stream.access$500().isDebugEnabled() ) {
            Stream.access$500().debug ( Stream.access$300().getString ( "stream.inputBuffer.signal" ) );
        }
        synchronized ( this.inBuffer ) {
            this.inBuffer.notifyAll();
        }
        return false;
    }
    private final ByteBuffer getInBuffer() {
        this.ensureBuffersExist();
        return this.inBuffer;
    }
    final synchronized void insertReplayedBody ( final ByteChunk body ) {
        this.inBuffer = ByteBuffer.wrap ( body.getBytes(), body.getOffset(), body.getLength() );
    }
    private final void ensureBuffersExist() {
        if ( this.inBuffer == null ) {
            final int size = Stream.access$600 ( Stream.this ).getLocalSettings().getInitialWindowSize();
            synchronized ( this ) {
                if ( this.inBuffer == null ) {
                    this.inBuffer = ByteBuffer.allocate ( size );
                    this.outBuffer = new byte[size];
                }
            }
        }
    }
    private final void receiveReset() {
        if ( this.inBuffer != null ) {
            synchronized ( this.inBuffer ) {
                this.reset = true;
                this.inBuffer.notifyAll();
            }
        }
    }
}
