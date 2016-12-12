package org.apache.catalina.tribes.group.interceptors;
import org.apache.juli.logging.LogFactory;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.group.InterceptorPayload;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.tribes.group.ChannelInterceptorBase;
public class GzipInterceptor extends ChannelInterceptorBase {
    private static final Log log;
    protected static final StringManager sm;
    public static final int DEFAULT_BUFFER_SIZE = 2048;
    @Override
    public void sendMessage ( final Member[] destination, final ChannelMessage msg, final InterceptorPayload payload ) throws ChannelException {
        try {
            final byte[] data = compress ( msg.getMessage().getBytes() );
            msg.getMessage().trim ( msg.getMessage().getLength() );
            msg.getMessage().append ( data, 0, data.length );
            super.sendMessage ( destination, msg, payload );
        } catch ( IOException x ) {
            GzipInterceptor.log.error ( GzipInterceptor.sm.getString ( "gzipInterceptor.compress.failed" ) );
            throw new ChannelException ( x );
        }
    }
    @Override
    public void messageReceived ( final ChannelMessage msg ) {
        try {
            final byte[] data = decompress ( msg.getMessage().getBytes() );
            msg.getMessage().trim ( msg.getMessage().getLength() );
            msg.getMessage().append ( data, 0, data.length );
            super.messageReceived ( msg );
        } catch ( IOException x ) {
            GzipInterceptor.log.error ( GzipInterceptor.sm.getString ( "gzipInterceptor.decompress.failed" ), x );
        }
    }
    public static byte[] compress ( final byte[] data ) throws IOException {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        final GZIPOutputStream gout = new GZIPOutputStream ( bout );
        gout.write ( data );
        gout.flush();
        gout.close();
        return bout.toByteArray();
    }
    public static byte[] decompress ( final byte[] data ) throws IOException {
        final ByteArrayOutputStream bout = new ByteArrayOutputStream ( 2048 );
        final ByteArrayInputStream bin = new ByteArrayInputStream ( data );
        final GZIPInputStream gin = new GZIPInputStream ( bin );
        final byte[] tmp = new byte[2048];
        for ( int length = gin.read ( tmp ); length > -1; length = gin.read ( tmp ) ) {
            bout.write ( tmp, 0, length );
        }
        return bout.toByteArray();
    }
    static {
        log = LogFactory.getLog ( GzipInterceptor.class );
        sm = StringManager.getManager ( GzipInterceptor.class );
    }
}
