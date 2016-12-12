package org.apache.tomcat.util.http.fileupload;
import java.io.EOFException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.Closeable;
public class IOUtils {
    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 4096;
    public static void closeQuietly ( final Closeable closeable ) {
        try {
            if ( closeable != null ) {
                closeable.close();
            }
        } catch ( IOException ex ) {}
    }
    public static int copy ( final InputStream input, final OutputStream output ) throws IOException {
        final long count = copyLarge ( input, output );
        if ( count > 2147483647L ) {
            return -1;
        }
        return ( int ) count;
    }
    public static long copyLarge ( final InputStream input, final OutputStream output ) throws IOException {
        final byte[] buffer = new byte[4096];
        long count = 0L;
        int n = 0;
        while ( -1 != ( n = input.read ( buffer ) ) ) {
            output.write ( buffer, 0, n );
            count += n;
        }
        return count;
    }
    public static int read ( final InputStream input, final byte[] buffer, final int offset, final int length ) throws IOException {
        if ( length < 0 ) {
            throw new IllegalArgumentException ( "Length must not be negative: " + length );
        }
        int remaining;
        int count;
        for ( remaining = length; remaining > 0; remaining -= count ) {
            final int location = length - remaining;
            count = input.read ( buffer, offset + location, remaining );
            if ( -1 == count ) {
                break;
            }
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
