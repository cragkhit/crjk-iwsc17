package org.apache.tomcat.util.http.fileupload.util;
import org.apache.tomcat.util.http.fileupload.InvalidFileNameException;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
public final class Streams {
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    public static long copy ( final InputStream inputStream, final OutputStream outputStream, final boolean closeOutputStream ) throws IOException {
        return copy ( inputStream, outputStream, closeOutputStream, new byte[8192] );
    }
    public static long copy ( final InputStream inputStream, final OutputStream outputStream, final boolean closeOutputStream, final byte[] buffer ) throws IOException {
        OutputStream out = outputStream;
        InputStream in = inputStream;
        try {
            long total = 0L;
            while ( true ) {
                final int res = in.read ( buffer );
                if ( res == -1 ) {
                    break;
                }
                if ( res <= 0 ) {
                    continue;
                }
                total += res;
                if ( out == null ) {
                    continue;
                }
                out.write ( buffer, 0, res );
            }
            if ( out != null ) {
                if ( closeOutputStream ) {
                    out.close();
                } else {
                    out.flush();
                }
                out = null;
            }
            in.close();
            in = null;
            return total;
        } finally {
            IOUtils.closeQuietly ( in );
            if ( closeOutputStream ) {
                IOUtils.closeQuietly ( out );
            }
        }
    }
    public static String asString ( final InputStream inputStream ) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copy ( inputStream, baos, true );
        return baos.toString();
    }
    public static String asString ( final InputStream inputStream, final String encoding ) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copy ( inputStream, baos, true );
        return baos.toString ( encoding );
    }
    public static String checkFileName ( final String fileName ) {
        if ( fileName != null && fileName.indexOf ( 0 ) != -1 ) {
            final StringBuilder sb = new StringBuilder();
            for ( int i = 0; i < fileName.length(); ++i ) {
                final char c = fileName.charAt ( i );
                switch ( c ) {
                case '\0': {
                    sb.append ( "\\0" );
                    break;
                }
                default: {
                    sb.append ( c );
                    break;
                }
                }
            }
            throw new InvalidFileNameException ( fileName, "Invalid file name: " + ( Object ) sb );
        }
        return fileName;
    }
}
