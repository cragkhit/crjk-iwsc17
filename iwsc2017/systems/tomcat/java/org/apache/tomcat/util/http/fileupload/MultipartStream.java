package org.apache.tomcat.util.http.fileupload;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import org.apache.tomcat.util.http.fileupload.FileUploadBase.FileUploadIOException;
import org.apache.tomcat.util.http.fileupload.util.Closeable;
import org.apache.tomcat.util.http.fileupload.util.Streams;
public class MultipartStream {
    public static class ProgressNotifier {
        private final ProgressListener listener;
        private final long contentLength;
        private long bytesRead;
        private int items;
        ProgressNotifier ( ProgressListener pListener, long pContentLength ) {
            listener = pListener;
            contentLength = pContentLength;
        }
        void noteBytesRead ( int pBytes ) {
            bytesRead += pBytes;
            notifyListener();
        }
        void noteItem() {
            ++items;
            notifyListener();
        }
        private void notifyListener() {
            if ( listener != null ) {
                listener.update ( bytesRead, contentLength, items );
            }
        }
    }
    public static final byte CR = 0x0D;
    public static final byte LF = 0x0A;
    public static final byte DASH = 0x2D;
    public static final int HEADER_PART_SIZE_MAX = 10240;
    protected static final int DEFAULT_BUFSIZE = 4096;
    protected static final byte[] HEADER_SEPARATOR = {CR, LF, CR, LF};
    protected static final byte[] FIELD_SEPARATOR = {CR, LF};
    protected static final byte[] STREAM_TERMINATOR = {DASH, DASH};
    protected static final byte[] BOUNDARY_PREFIX = {CR, LF, DASH, DASH};
    private final InputStream input;
    private int boundaryLength;
    private final int keepRegion;
    private final byte[] boundary;
    private int[] boundaryTable;
    private final int bufSize;
    private final byte[] buffer;
    private int head;
    private int tail;
    private String headerEncoding;
    private final ProgressNotifier notifier;
    public MultipartStream ( InputStream input,
                             byte[] boundary,
                             int bufSize,
                             ProgressNotifier pNotifier ) {
        if ( boundary == null ) {
            throw new IllegalArgumentException ( "boundary may not be null" );
        }
        this.boundaryLength = boundary.length + BOUNDARY_PREFIX.length;
        if ( bufSize < this.boundaryLength + 1 ) {
            throw new IllegalArgumentException (
                "The buffer size specified for the MultipartStream is too small" );
        }
        this.input = input;
        this.bufSize = Math.max ( bufSize, boundaryLength * 2 );
        this.buffer = new byte[this.bufSize];
        this.notifier = pNotifier;
        this.boundary = new byte[this.boundaryLength];
        this.boundaryTable = new int[this.boundaryLength + 1];
        this.keepRegion = this.boundary.length;
        System.arraycopy ( BOUNDARY_PREFIX, 0, this.boundary, 0,
                           BOUNDARY_PREFIX.length );
        System.arraycopy ( boundary, 0, this.boundary, BOUNDARY_PREFIX.length,
                           boundary.length );
        computeBoundaryTable();
        head = 0;
        tail = 0;
    }
    MultipartStream ( InputStream input,
                      byte[] boundary,
                      ProgressNotifier pNotifier ) {
        this ( input, boundary, DEFAULT_BUFSIZE, pNotifier );
    }
    public String getHeaderEncoding() {
        return headerEncoding;
    }
    public void setHeaderEncoding ( String encoding ) {
        headerEncoding = encoding;
    }
    public byte readByte() throws IOException {
        if ( head == tail ) {
            head = 0;
            tail = input.read ( buffer, head, bufSize );
            if ( tail == -1 ) {
                throw new IOException ( "No more data is available" );
            }
            if ( notifier != null ) {
                notifier.noteBytesRead ( tail );
            }
        }
        return buffer[head++];
    }
    public boolean readBoundary()
    throws FileUploadIOException, MalformedStreamException {
        byte[] marker = new byte[2];
        boolean nextChunk = false;
        head += boundaryLength;
        try {
            marker[0] = readByte();
            if ( marker[0] == LF ) {
                return true;
            }
            marker[1] = readByte();
            if ( arrayequals ( marker, STREAM_TERMINATOR, 2 ) ) {
                nextChunk = false;
            } else if ( arrayequals ( marker, FIELD_SEPARATOR, 2 ) ) {
                nextChunk = true;
            } else {
                throw new MalformedStreamException (
                    "Unexpected characters follow a boundary" );
            }
        } catch ( FileUploadIOException e ) {
            throw e;
        } catch ( IOException e ) {
            throw new MalformedStreamException ( "Stream ended unexpectedly" );
        }
        return nextChunk;
    }
    public void setBoundary ( byte[] boundary )
    throws IllegalBoundaryException {
        if ( boundary.length != boundaryLength - BOUNDARY_PREFIX.length ) {
            throw new IllegalBoundaryException (
                "The length of a boundary token cannot be changed" );
        }
        System.arraycopy ( boundary, 0, this.boundary, BOUNDARY_PREFIX.length,
                           boundary.length );
        computeBoundaryTable();
    }
    private void computeBoundaryTable() {
        int position = 2;
        int candidate = 0;
        boundaryTable[0] = -1;
        boundaryTable[1] = 0;
        while ( position <= boundaryLength ) {
            if ( boundary[position - 1] == boundary[candidate] ) {
                boundaryTable[position] = candidate + 1;
                candidate++;
                position++;
            } else if ( candidate > 0 ) {
                candidate = boundaryTable[candidate];
            } else {
                boundaryTable[position] = 0;
                position++;
            }
        }
    }
    public String readHeaders() throws FileUploadIOException, MalformedStreamException {
        int i = 0;
        byte b;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int size = 0;
        while ( i < HEADER_SEPARATOR.length ) {
            try {
                b = readByte();
            } catch ( FileUploadIOException e ) {
                throw e;
            } catch ( IOException e ) {
                throw new MalformedStreamException ( "Stream ended unexpectedly" );
            }
            if ( ++size > HEADER_PART_SIZE_MAX ) {
                throw new MalformedStreamException ( String.format (
                        "Header section has more than %s bytes (maybe it is not properly terminated)",
                        Integer.valueOf ( HEADER_PART_SIZE_MAX ) ) );
            }
            if ( b == HEADER_SEPARATOR[i] ) {
                i++;
            } else {
                i = 0;
            }
            baos.write ( b );
        }
        String headers = null;
        if ( headerEncoding != null ) {
            try {
                headers = baos.toString ( headerEncoding );
            } catch ( UnsupportedEncodingException e ) {
                headers = baos.toString();
            }
        } else {
            headers = baos.toString();
        }
        return headers;
    }
    public int readBodyData ( OutputStream output )
    throws MalformedStreamException, IOException {
        return ( int ) Streams.copy ( newInputStream(), output, false );
    }
    ItemInputStream newInputStream() {
        return new ItemInputStream();
    }
    public int discardBodyData() throws MalformedStreamException, IOException {
        return readBodyData ( null );
    }
    public boolean skipPreamble() throws IOException {
        System.arraycopy ( boundary, 2, boundary, 0, boundary.length - 2 );
        boundaryLength = boundary.length - 2;
        computeBoundaryTable();
        try {
            discardBodyData();
            return readBoundary();
        } catch ( MalformedStreamException e ) {
            return false;
        } finally {
            System.arraycopy ( boundary, 0, boundary, 2, boundary.length - 2 );
            boundaryLength = boundary.length;
            boundary[0] = CR;
            boundary[1] = LF;
            computeBoundaryTable();
        }
    }
    public static boolean arrayequals ( byte[] a,
                                        byte[] b,
                                        int count ) {
        for ( int i = 0; i < count; i++ ) {
            if ( a[i] != b[i] ) {
                return false;
            }
        }
        return true;
    }
    protected int findByte ( byte value,
                             int pos ) {
        for ( int i = pos; i < tail; i++ ) {
            if ( buffer[i] == value ) {
                return i;
            }
        }
        return -1;
    }
    protected int findSeparator() {
        int bufferPos = this.head;
        int tablePos = 0;
        while ( bufferPos < this.tail ) {
            while ( tablePos >= 0 && buffer[bufferPos] != boundary[tablePos] ) {
                tablePos = boundaryTable[tablePos];
            }
            bufferPos++;
            tablePos++;
            if ( tablePos == boundaryLength ) {
                return bufferPos - boundaryLength;
            }
        }
        return -1;
    }
    public static class MalformedStreamException extends IOException {
        private static final long serialVersionUID = 6466926458059796677L;
        public MalformedStreamException() {
            super();
        }
        public MalformedStreamException ( String message ) {
            super ( message );
        }
    }
    public static class IllegalBoundaryException extends IOException {
        private static final long serialVersionUID = -161533165102632918L;
        public IllegalBoundaryException() {
            super();
        }
        public IllegalBoundaryException ( String message ) {
            super ( message );
        }
    }
    public class ItemInputStream extends InputStream implements Closeable {
        private long total;
        private int pad;
        private int pos;
        private boolean closed;
        ItemInputStream() {
            findSeparator();
        }
        private void findSeparator() {
            pos = MultipartStream.this.findSeparator();
            if ( pos == -1 ) {
                if ( tail - head > keepRegion ) {
                    pad = keepRegion;
                } else {
                    pad = tail - head;
                }
            }
        }
        public long getBytesRead() {
            return total;
        }
        @Override
        public int available() throws IOException {
            if ( pos == -1 ) {
                return tail - head - pad;
            }
            return pos - head;
        }
        private static final int BYTE_POSITIVE_OFFSET = 256;
        @Override
        public int read() throws IOException {
            if ( closed ) {
                throw new FileItemStream.ItemSkippedException();
            }
            if ( available() == 0 && makeAvailable() == 0 ) {
                return -1;
            }
            ++total;
            int b = buffer[head++];
            if ( b >= 0 ) {
                return b;
            }
            return b + BYTE_POSITIVE_OFFSET;
        }
        @Override
        public int read ( byte[] b, int off, int len ) throws IOException {
            if ( closed ) {
                throw new FileItemStream.ItemSkippedException();
            }
            if ( len == 0 ) {
                return 0;
            }
            int res = available();
            if ( res == 0 ) {
                res = makeAvailable();
                if ( res == 0 ) {
                    return -1;
                }
            }
            res = Math.min ( res, len );
            System.arraycopy ( buffer, head, b, off, res );
            head += res;
            total += res;
            return res;
        }
        @Override
        public void close() throws IOException {
            close ( false );
        }
        public void close ( boolean pCloseUnderlying ) throws IOException {
            if ( closed ) {
                return;
            }
            if ( pCloseUnderlying ) {
                closed = true;
                input.close();
            } else {
                for ( ;; ) {
                    int av = available();
                    if ( av == 0 ) {
                        av = makeAvailable();
                        if ( av == 0 ) {
                            break;
                        }
                    }
                    skip ( av );
                }
            }
            closed = true;
        }
        @Override
        public long skip ( long bytes ) throws IOException {
            if ( closed ) {
                throw new FileItemStream.ItemSkippedException();
            }
            int av = available();
            if ( av == 0 ) {
                av = makeAvailable();
                if ( av == 0 ) {
                    return 0;
                }
            }
            long res = Math.min ( av, bytes );
            head += res;
            return res;
        }
        private int makeAvailable() throws IOException {
            if ( pos != -1 ) {
                return 0;
            }
            total += tail - head - pad;
            System.arraycopy ( buffer, tail - pad, buffer, 0, pad );
            head = 0;
            tail = pad;
            for ( ;; ) {
                int bytesRead = input.read ( buffer, tail, bufSize - tail );
                if ( bytesRead == -1 ) {
                    final String msg = "Stream ended unexpectedly";
                    throw new MalformedStreamException ( msg );
                }
                if ( notifier != null ) {
                    notifier.noteBytesRead ( bytesRead );
                }
                tail += bytesRead;
                findSeparator();
                int av = available();
                if ( av > 0 || pos != -1 ) {
                    return av;
                }
            }
        }
        @Override
        public boolean isClosed() {
            return closed;
        }
    }
}
