package org.apache.coyote.http11;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.coyote.http11.filters.GzipOutputFilter;
import java.io.IOException;
import org.apache.coyote.ActionCode;
import org.apache.tomcat.util.net.SocketWrapperBase;
import java.nio.ByteBuffer;
import org.apache.coyote.Response;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.res.StringManager;
import org.apache.coyote.OutputBuffer;
public class Http11OutputBuffer implements OutputBuffer {
    protected static final StringManager sm;
    private static final Log log;
    protected final Response response;
    protected boolean responseFinished;
    protected final ByteBuffer headerBuffer;
    protected OutputFilter[] filterLibrary;
    protected OutputFilter[] activeFilters;
    protected int lastActiveFilter;
    protected OutputBuffer outputStreamOutputBuffer;
    protected SocketWrapperBase<?> socketWrapper;
    protected long byteCount;
    protected Http11OutputBuffer ( final Response response, final int headerBufferSize ) {
        this.byteCount = 0L;
        this.response = response;
        this.headerBuffer = ByteBuffer.allocate ( headerBufferSize );
        this.filterLibrary = new OutputFilter[0];
        this.activeFilters = new OutputFilter[0];
        this.lastActiveFilter = -1;
        this.responseFinished = false;
        this.outputStreamOutputBuffer = new SocketOutputBuffer();
    }
    public void addFilter ( final OutputFilter filter ) {
        final OutputFilter[] newFilterLibrary = new OutputFilter[this.filterLibrary.length + 1];
        for ( int i = 0; i < this.filterLibrary.length; ++i ) {
            newFilterLibrary[i] = this.filterLibrary[i];
        }
        newFilterLibrary[this.filterLibrary.length] = filter;
        this.filterLibrary = newFilterLibrary;
        this.activeFilters = new OutputFilter[this.filterLibrary.length];
    }
    public OutputFilter[] getFilters() {
        return this.filterLibrary;
    }
    public void addActiveFilter ( final OutputFilter filter ) {
        if ( this.lastActiveFilter == -1 ) {
            filter.setBuffer ( this.outputStreamOutputBuffer );
        } else {
            for ( int i = 0; i <= this.lastActiveFilter; ++i ) {
                if ( this.activeFilters[i] == filter ) {
                    return;
                }
            }
            filter.setBuffer ( this.activeFilters[this.lastActiveFilter] );
        }
        ( this.activeFilters[++this.lastActiveFilter] = filter ).setResponse ( this.response );
    }
    @Override
    public int doWrite ( final ByteBuffer chunk ) throws IOException {
        if ( !this.response.isCommitted() ) {
            this.response.action ( ActionCode.COMMIT, null );
        }
        if ( this.lastActiveFilter == -1 ) {
            return this.outputStreamOutputBuffer.doWrite ( chunk );
        }
        return this.activeFilters[this.lastActiveFilter].doWrite ( chunk );
    }
    @Override
    public long getBytesWritten() {
        if ( this.lastActiveFilter == -1 ) {
            return this.outputStreamOutputBuffer.getBytesWritten();
        }
        return this.activeFilters[this.lastActiveFilter].getBytesWritten();
    }
    public void flush() throws IOException {
        for ( int i = 0; i <= this.lastActiveFilter; ++i ) {
            if ( this.activeFilters[i] instanceof GzipOutputFilter ) {
                if ( Http11OutputBuffer.log.isDebugEnabled() ) {
                    Http11OutputBuffer.log.debug ( "Flushing the gzip filter at position " + i + " of the filter chain..." );
                }
                ( ( GzipOutputFilter ) this.activeFilters[i] ).flush();
                break;
            }
        }
        this.flushBuffer ( this.isBlocking() );
    }
    void resetHeaderBuffer() {
        this.headerBuffer.position ( 0 ).limit ( this.headerBuffer.capacity() );
    }
    public void recycle() {
        this.nextRequest();
        this.socketWrapper = null;
    }
    public void nextRequest() {
        for ( int i = 0; i <= this.lastActiveFilter; ++i ) {
            this.activeFilters[i].recycle();
        }
        this.response.recycle();
        this.headerBuffer.position ( 0 ).limit ( this.headerBuffer.capacity() );
        this.lastActiveFilter = -1;
        this.responseFinished = false;
        this.byteCount = 0L;
    }
    public void finishResponse() throws IOException {
        if ( this.responseFinished ) {
            return;
        }
        if ( this.lastActiveFilter != -1 ) {
            this.activeFilters[this.lastActiveFilter].end();
        }
        this.flushBuffer ( true );
        this.responseFinished = true;
    }
    public void init ( final SocketWrapperBase<?> socketWrapper ) {
        this.socketWrapper = socketWrapper;
    }
    public void sendAck() throws IOException {
        if ( !this.response.isCommitted() ) {
            this.socketWrapper.write ( this.isBlocking(), Constants.ACK_BYTES, 0, Constants.ACK_BYTES.length );
            if ( this.flushBuffer ( true ) ) {
                throw new IOException ( Http11OutputBuffer.sm.getString ( "iob.failedwrite.ack" ) );
            }
        }
    }
    protected void commit() throws IOException {
        this.response.setCommitted ( true );
        if ( this.headerBuffer.position() > 0 ) {
            this.headerBuffer.flip();
            try {
                this.socketWrapper.write ( this.isBlocking(), this.headerBuffer );
            } finally {
                this.headerBuffer.position ( 0 ).limit ( this.headerBuffer.capacity() );
            }
        }
    }
    public void sendStatus() {
        this.write ( Constants.HTTP_11_BYTES );
        this.headerBuffer.put ( ( byte ) 32 );
        final int status = this.response.getStatus();
        switch ( status ) {
        case 200: {
            this.write ( Constants._200_BYTES );
            break;
        }
        case 400: {
            this.write ( Constants._400_BYTES );
            break;
        }
        case 404: {
            this.write ( Constants._404_BYTES );
            break;
        }
        default: {
            this.write ( status );
            break;
        }
        }
        this.headerBuffer.put ( ( byte ) 32 );
        this.headerBuffer.put ( ( byte ) 13 ).put ( ( byte ) 10 );
    }
    public void sendHeader ( final MessageBytes name, final MessageBytes value ) {
        this.write ( name );
        this.headerBuffer.put ( ( byte ) 58 ).put ( ( byte ) 32 );
        this.write ( value );
        this.headerBuffer.put ( ( byte ) 13 ).put ( ( byte ) 10 );
    }
    public void endHeaders() {
        this.headerBuffer.put ( ( byte ) 13 ).put ( ( byte ) 10 );
    }
    private void write ( final MessageBytes mb ) {
        if ( mb.getType() != 2 ) {
            mb.toBytes();
            final ByteChunk bc = mb.getByteChunk();
            final byte[] buffer = bc.getBuffer();
            for ( int i = bc.getOffset(); i < bc.getLength(); ++i ) {
                if ( ( buffer[i] > -1 && buffer[i] <= 31 && buffer[i] != 9 ) || buffer[i] == 127 ) {
                    buffer[i] = 32;
                }
            }
        }
        this.write ( mb.getByteChunk() );
    }
    private void write ( final ByteChunk bc ) {
        final int length = bc.getLength();
        this.checkLengthBeforeWrite ( length );
        this.headerBuffer.put ( bc.getBytes(), bc.getStart(), length );
    }
    public void write ( final byte[] b ) {
        this.checkLengthBeforeWrite ( b.length );
        this.headerBuffer.put ( b );
    }
    private void write ( final int value ) {
        final String s = Integer.toString ( value );
        final int len = s.length();
        this.checkLengthBeforeWrite ( len );
        for ( int i = 0; i < len; ++i ) {
            final char c = s.charAt ( i );
            this.headerBuffer.put ( ( byte ) c );
        }
    }
    private void checkLengthBeforeWrite ( final int length ) {
        if ( this.headerBuffer.position() + length + 4 > this.headerBuffer.capacity() ) {
            throw new HeadersTooLargeException ( Http11OutputBuffer.sm.getString ( "iob.responseheadertoolarge.error" ) );
        }
    }
    protected boolean flushBuffer ( final boolean block ) throws IOException {
        return this.socketWrapper.flush ( block );
    }
    protected final boolean isBlocking() {
        return this.response.getWriteListener() == null;
    }
    protected final boolean isReady() {
        final boolean result = !this.hasDataToWrite();
        if ( !result ) {
            this.socketWrapper.registerWriteInterest();
        }
        return result;
    }
    public boolean hasDataToWrite() {
        return this.socketWrapper.hasDataToWrite();
    }
    public void registerWriteInterest() {
        this.socketWrapper.registerWriteInterest();
    }
    static {
        sm = StringManager.getManager ( Http11OutputBuffer.class );
        log = LogFactory.getLog ( Http11OutputBuffer.class );
    }
    protected class SocketOutputBuffer implements OutputBuffer {
        @Override
        public int doWrite ( final ByteBuffer chunk ) throws IOException {
            try {
                int len = chunk.remaining();
                Http11OutputBuffer.this.socketWrapper.write ( Http11OutputBuffer.this.isBlocking(), chunk );
                len -= chunk.remaining();
                final Http11OutputBuffer this$0 = Http11OutputBuffer.this;
                this$0.byteCount += len;
                return len;
            } catch ( IOException ioe ) {
                Http11OutputBuffer.this.response.action ( ActionCode.CLOSE_NOW, ioe );
                throw ioe;
            }
        }
        @Override
        public long getBytesWritten() {
            return Http11OutputBuffer.this.byteCount;
        }
    }
}
