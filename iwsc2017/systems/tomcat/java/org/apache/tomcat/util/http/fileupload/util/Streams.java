package org.apache.tomcat.util.http.fileupload.util;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.apache.tomcat.util.http.fileupload.InvalidFileNameException;
public final class Streams {
    private Streams() {
    }
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    public static long copy ( InputStream inputStream, OutputStream outputStream, boolean closeOutputStream )
    throws IOException {
        return copy ( inputStream, outputStream, closeOutputStream, new byte[DEFAULT_BUFFER_SIZE] );
    }
    public static long copy ( InputStream inputStream,
                              OutputStream outputStream, boolean closeOutputStream,
                              byte[] buffer )
    throws IOException {
        OutputStream out = outputStream;
        InputStream in = inputStream;
        try {
            long total = 0;
            for ( ;; ) {
                int res = in.read ( buffer );
                if ( res == -1 ) {
                    break;
                }
                if ( res > 0 ) {
                    total += res;
                    if ( out != null ) {
                        out.write ( buffer, 0, res );
                    }
                }
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
    public static String asString ( InputStream inputStream ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copy ( inputStream, baos, true );
        return baos.toString();
    }
    public static String asString ( InputStream inputStream, String encoding ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copy ( inputStream, baos, true );
        return baos.toString ( encoding );
    }
    public static String checkFileName ( String fileName ) {
        if ( fileName != null  &&  fileName.indexOf ( '\u0000' ) != -1 ) {
            final StringBuilder sb = new StringBuilder();
            for ( int i = 0;  i < fileName.length();  i++ ) {
                char c = fileName.charAt ( i );
                switch ( c ) {
                case 0:
                    sb.append ( "\\0" );
                    break;
                default:
                    sb.append ( c );
                    break;
                }
            }
            throw new InvalidFileNameException ( fileName,
                                                 "Invalid file name: " + sb );
        }
        return fileName;
    }
}
