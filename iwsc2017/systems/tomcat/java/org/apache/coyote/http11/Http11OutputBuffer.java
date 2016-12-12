package org.apache.coyote.http11;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.coyote.ActionCode;
import org.apache.coyote.OutputBuffer;
import org.apache.coyote.Response;
import org.apache.coyote.http11.filters.GzipOutputFilter;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.buf.ByteChunk;
import org.apache.tomcat.util.buf.MessageBytes;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.tomcat.util.res.StringManager;
public class Http11OutputBuffer implements OutputBuffer {
    protected static final StringManager sm = StringManager.getManager ( Http11OutputBuffer.class );
    private static final Log log = LogFactory.getLog ( Http11OutputBuffer.class );
    protected final Response response;
    protected boolean responseFinished;
    protected final ByteBuffer headerBuffer;
    protected OutputFilter[] filterLibrary;
    protected OutputFilter[] activeFilters;
    protected int lastActiveFilter;
    protected OutputBuffer outputStreamOutputBuffer;
    protected SocketWrapperBase<?> socketWrapper;
    protected long byteCount = 0;
    protected Http11OutputBuffer ( Response response, int headerBufferSize ) {
        this.response = response;
        headerBuffer = ByteBuffer.allocate ( headerBufferSize );
        filterLibrary = new OutputFilter[0];
        activeFilters = new OutputFilter[0];
        lastActiveFilter = -1;
        responseFinished = false;
        outputStreamOutputBuffer = new SocketOutputBuffer();
    }
    public void addFilter ( OutputFilter filter ) {
        OutputFilter[] newFilterLibrary = new OutputFilter[filterLibrary.length + 1];
        for ( int i = 0; i < filterLibrary.length; i++ ) {
            newFilterLibrary[i] = filterLibrary[i];
        }
        newFilterLibrary[filterLibrary.length] = filter;
        filterLibrary = newFilterLibrary;
        activeFilters = new OutputFilter[filterLibrary.length];
    }
    public OutputFilter[] getFilters() {
        return filterLibrary;
    }
    public void addActiveFilter ( OutputFilter filter ) {
        if ( lastActiveFilter == -1 ) {
            filter.setBuffer ( outputStreamOutputBuffer );
        } else {
            for ( int i = 0; i <= lastActiveFilter; i++ ) {
                if ( activeFilters[i] == filter ) {
                    return;
                }
            }
            filter.setBuffer ( activeFilters[lastActiveFilter] );
        }
        activeFilters[++lastActiveFilter] = filter;
        filter.setResponse ( response );
    }
    @Override
    public int doWrite ( ByteBuffer chunk ) throws IOException {
        if ( !response.isCommitted() ) {
            response.action ( ActionCode.COMMIT, null );
        }
        if ( lastActiveFilter == -1 ) {
            return outputStreamOutputBuffer.doWrite ( chunk );
        } else {
            return activeFilters[lastActiveFilter].doWrite ( chunk );
        }
    }
    @Override
    public long getBytesWritten() {
        if ( lastActiveFilter == -1 ) {
            return outputStreamOutputBuffer.getBytesWritten();
        } else {
            return activeFilters[lastActiveFilter].getBytesWritten();
        }
    }
    public void flush() throws IOException {
        for ( int i = 0; i <= lastActiveFilter; i++ ) {
            if ( activeFilters[i] instanceof GzipOutputFilter ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Flushing the gzip filter at position " + i +
                                " of the filter chain..." );
                }
                ( ( GzipOutputFilter ) activeFilters[i] ).flush();
                break;
            }
        }
        flushBuffer ( isBlocking() );
    }
    void resetHeaderBuffer() {
        headerBuffer.position ( 0 ).limit ( headerBuffer.capacity() );
    }
    public void recycle() {
        nextRequest();
        socketWrapper = null;
    }
    public void nextRequest() {
        for ( int i = 0; i <= lastActiveFilter; i++ ) {
            activeFilters[i].recycle();
        }
        response.recycle();
        headerBuffer.position ( 0 ).limit ( headerBuffer.capacity() );
        lastActiveFilter = -1;
        responseFinished = false;
        byteCount = 0;
    }
    public void finishResponse() throws IOException {
        if ( responseFinished ) {
            return;
        }
        if ( lastActiveFilter != -1 ) {
            activeFilters[lastActiveFilter].end();
        }
        flushBuffer ( true );
        responseFinished = true;
    }
    public void init ( SocketWrapperBase<?> socketWrapper ) {
        this.socketWrapper = socketWrapper;
    }
    public void sendAck() throws IOException {
        if ( !response.isCommitted() ) {
            socketWrapper.write ( isBlocking(), Constants.ACK_BYTES, 0, Constants.ACK_BYTES.length );
            if ( flushBuffer ( true ) ) {
                throw new IOException ( sm.getString ( "iob.failedwrite.ack" ) );
            }
        }
    }
    protected void commit() throws IOException {
        response.setCommitted ( true );
        if ( headerBuffer.position() > 0 ) {
            headerBuffer.flip();
            try {
                socketWrapper.write ( isBlocking(), headerBuffer );
            } finally {
                headerBuffer.position ( 0 ).limit ( headerBuffer.capacity() );
            }
        }
    }
    public void sendStatus() {
        write ( Constants.HTTP_11_BYTES );
        headerBuffer.put ( Constants.SP );
        int status = response.getStatus();
        switch ( status ) {
        case 200:
            write ( Constants._200_BYTES );
            break;
        case 400:
            write ( Constants._400_BYTES );
            break;
        case 404:
            write ( Constants._404_BYTES );
            break;
        default:
            write ( status );
        }
        headerBuffer.put ( Constants.SP );
        headerBuffer.put ( Constants.CR ).put ( Constants.LF );
    }
    public void sendHeader ( MessageBytes name, MessageBytes value ) {
        write ( name );
        headerBuffer.put ( Constants.COLON ).put ( Constants.SP );
        write ( value );
        headerBuffer.put ( Constants.CR ).put ( Constants.LF );
    }
    public void endHeaders() {
        headerBuffer.put ( Constants.CR ).put ( Constants.LF );
    }
    private void write ( MessageBytes mb ) {
        if ( mb.getType() != MessageBytes.T_BYTES ) {
            mb.toBytes();
            ByteChunk bc = mb.getByteChunk();
            byte[] buffer = bc.getBuffer();
            for ( int i = bc.getOffset(); i < bc.getLength(); i++ ) {
                if ( ( buffer[i] > -1 && buffer[i] <= 31 && buffer[i] != 9 ) ||
                        buffer[i] == 127 ) {
                    buffer[i] = ' ';
                }
            }
        }
        write ( mb.getByteChunk() );
    }
    private void write ( ByteChunk bc ) {
        int length = bc.getLength();
        checkLengthBeforeWrite ( length );
        headerBuffer.put ( bc.getBytes(), bc.getStart(), length );
    }
    public void write ( byte[] b ) {
        checkLengthBeforeWrite ( b.length );
        headerBuffer.put ( b );
    }
    private void write ( int value ) {
        String s = Integer.toString ( value );
        int len = s.length();
        checkLengthBeforeWrite ( len );
        for ( int i = 0; i < len; i++ ) {
            char c = s.charAt ( i );
            headerBuffer.put ( ( byte ) c );
        }
    }
    private void checkLengthBeforeWrite ( int length ) {
        if ( headerBuffer.position() + length + 4 > headerBuffer.capacity() ) {
            throw new HeadersTooLargeException (
                sm.getString ( "iob.responseheadertoolarge.error" ) );
        }
    }
    protected boolean flushBuffer ( boolean block ) throws IOException  {
        return socketWrapper.flush ( block );
    }
    protected final boolean isBlocking() {
        return response.getWriteListener() == null;
    }
    protected final boolean isReady() {
        boolean result = !hasDataToWrite();
        if ( !result ) {
            socketWrapper.registerWriteInterest();
        }
        return result;
    }
    public boolean hasDataToWrite() {
        return socketWrapper.hasDataToWrite();
    }
    public void registerWriteInterest() {
        socketWrapper.registerWriteInterest();
    }
    protected class SocketOutputBuffer implements OutputBuffer {
        @Override
        public int doWrite ( ByteBuffer chunk ) throws IOException {
            try {
                int len = chunk.remaining();
                socketWrapper.write ( isBlocking(), chunk );
                len -= chunk.remaining();
                byteCount += len;
                return len;
            } catch ( IOException ioe ) {
                response.action ( ActionCode.CLOSE_NOW, ioe );
                throw ioe;
            }
        }
        @Override
        public long getBytesWritten() {
            return byteCount;
        }
    }
}
