package org.apache.coyote.http2;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.coyote.OutputBuffer;
class StreamOutputBuffer implements OutputBuffer {
    private final ByteBuffer buffer;
    private volatile long written;
    private volatile boolean closed;
    private volatile boolean endOfStreamSent;
    StreamOutputBuffer() {
        this.buffer = ByteBuffer.allocate ( 8192 );
        this.written = 0L;
        this.closed = false;
        this.endOfStreamSent = false;
    }
    @Override
    public final synchronized int doWrite ( final ByteBuffer chunk ) throws IOException {
        if ( this.closed ) {
            throw new IllegalStateException ( Stream.access$300().getString ( "stream.closed", Stream.this.getConnectionId(), Stream.this.getIdentifier() ) );
        }
        if ( !Stream.access$400 ( Stream.this ).isCommitted() ) {
            Stream.access$400 ( Stream.this ).sendHeaders();
        }
        final int chunkLimit = chunk.limit();
        int offset = 0;
        while ( chunk.remaining() > 0 ) {
            final int thisTime = Math.min ( this.buffer.remaining(), chunk.remaining() );
            chunk.limit ( chunk.position() + thisTime );
            this.buffer.put ( chunk );
            chunk.limit ( chunkLimit );
            offset += thisTime;
            if ( chunk.remaining() > 0 && !this.buffer.hasRemaining() && this.flush ( true, Stream.access$400 ( Stream.this ).getWriteListener() == null ) ) {
                break;
            }
        }
        this.written += offset;
        return offset;
    }
    final synchronized boolean flush ( final boolean block ) throws IOException {
        return this.flush ( false, block );
    }
    private final synchronized boolean flush ( final boolean writeInProgress, final boolean block ) throws IOException {
        if ( Stream.access$500().isDebugEnabled() ) {
            Stream.access$500().debug ( Stream.access$300().getString ( "stream.outputBuffer.flush.debug", Stream.this.getConnectionId(), Stream.this.getIdentifier(), Integer.toString ( this.buffer.position() ), Boolean.toString ( writeInProgress ), Boolean.toString ( this.closed ) ) );
        }
        if ( this.buffer.position() == 0 ) {
            if ( this.closed && !this.endOfStreamSent ) {
                Stream.access$600 ( Stream.this ).writeBody ( Stream.this, this.buffer, 0, true );
            }
            return false;
        }
        this.buffer.flip();
        int left = this.buffer.remaining();
        while ( left > 0 ) {
            int streamReservation = Stream.access$700 ( Stream.this, left, block );
            if ( streamReservation == 0 ) {
                this.buffer.compact();
                return true;
            }
            while ( streamReservation > 0 ) {
                final int connectionReservation = Stream.access$600 ( Stream.this ).reserveWindowSize ( Stream.this, streamReservation );
                Stream.access$600 ( Stream.this ).writeBody ( Stream.this, this.buffer, connectionReservation, !writeInProgress && this.closed && left == connectionReservation );
                streamReservation -= connectionReservation;
                left -= connectionReservation;
            }
        }
        this.buffer.clear();
        return false;
    }
    final synchronized boolean isReady() {
        return Stream.this.getWindowSize() > 0L && Stream.access$600 ( Stream.this ).getWindowSize() > 0L;
    }
    @Override
    public final long getBytesWritten() {
        return this.written;
    }
    final void close() throws IOException {
        this.closed = true;
        Stream.this.flushData();
    }
    final boolean hasNoBody() {
        return this.written == 0L && this.closed;
    }
}
