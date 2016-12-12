package org.apache.tomcat.util.http.fileupload;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
public class IOUtils {
    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    public IOUtils() {
        super();
    }
    public static void closeQuietly ( final Closeable closeable ) {
        try {
            if ( closeable != null ) {
                closeable.close();
            }
        } catch ( final IOException ioe ) {
        }
    }
    public static int copy ( InputStream input, OutputStream output ) throws IOException {
        long count = copyLarge ( input, output );
        if ( count > Integer.MAX_VALUE ) {
            return -1;
        }
        return ( int ) count;
    }
    public static long copyLarge ( InputStream input, OutputStream output )
    throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while ( EOF != ( n = input.read ( buffer ) ) ) {
            output.write ( buffer, 0, n );
            count += n;
        }
        return count;
    }
    public static int read ( final InputStream input, final byte[] buffer, final int offset, final int length ) throws IOException {
        if ( length < 0 ) {
            throw new IllegalArgumentException ( "Length must not be negative: " + length );
        }
        int remaining = length;
        while ( remaining > 0 ) {
            final int location = length - remaining;
            final int count = input.read ( buffer, offset + location, remaining );
            if ( EOF == count ) {
                break;
            }
            remaining -= count;
        }
        return length - remaining;
    }
    public static void readFully ( final InputStream input, final byte[] buffer, final int offset, final int length ) throws IOException {
        final int actual = read ( input, buffer, offset, length );
        if ( actual != length ) {
            throw new EOFException ( "Length to read: " + length + " actual: " + actual );
        }
    }
    public static void readFully ( final InputStream input, final byte[] buffer ) throws IOException {
        readFully ( input, buffer, 0, buffer.length );
    }
}
