package org.apache.tomcat.util.net;
import org.apache.juli.logging.LogFactory;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import org.apache.tomcat.util.net.openssl.ciphers.Cipher;
import java.util.List;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
public class TLSClientHelloExtractor {
    private static final Log log;
    private static final StringManager sm;
    private final ExtractorResult result;
    private final List<Cipher> clientRequestedCiphers;
    private final String sniValue;
    private static final int TLS_RECORD_HEADER_LEN = 5;
    public TLSClientHelloExtractor ( final ByteBuffer netInBuffer ) {
        final int pos = netInBuffer.position();
        final int limit = netInBuffer.limit();
        ExtractorResult result = ExtractorResult.NOT_PRESENT;
        final List<Cipher> clientRequestedCiphers = new ArrayList<Cipher>();
        String sniValue = null;
        try {
            netInBuffer.flip();
            if ( !isAvailable ( netInBuffer, 5 ) ) {
                result = handleIncompleteRead ( netInBuffer );
                return;
            }
            if ( !isTLSHandshake ( netInBuffer ) ) {
                return;
            }
            if ( !isAllRecordAvailable ( netInBuffer ) ) {
                result = handleIncompleteRead ( netInBuffer );
                return;
            }
            if ( !isClientHello ( netInBuffer ) ) {
                return;
            }
            if ( !isAllClientHelloAvailable ( netInBuffer ) ) {
                TLSClientHelloExtractor.log.warn ( TLSClientHelloExtractor.sm.getString ( "sniExtractor.clientHelloTooBig" ) );
                return;
            }
            skipBytes ( netInBuffer, 2 );
            skipBytes ( netInBuffer, 32 );
            skipBytes ( netInBuffer, netInBuffer.get() & 0xFF );
            for ( int cipherCount = netInBuffer.getChar() / '\u0002', i = 0; i < cipherCount; ++i ) {
                final int cipherId = netInBuffer.getChar();
                clientRequestedCiphers.add ( Cipher.valueOf ( cipherId ) );
            }
            skipBytes ( netInBuffer, netInBuffer.get() & 0xFF );
            if ( !netInBuffer.hasRemaining() ) {
                return;
            }
            skipBytes ( netInBuffer, 2 );
            while ( netInBuffer.hasRemaining() && sniValue == null ) {
                sniValue = readSniExtension ( netInBuffer );
            }
            if ( sniValue != null ) {
                result = ExtractorResult.COMPLETE;
            }
        } finally {
            this.result = result;
            this.clientRequestedCiphers = clientRequestedCiphers;
            this.sniValue = sniValue;
            netInBuffer.limit ( limit );
            netInBuffer.position ( pos );
        }
    }
    public ExtractorResult getResult() {
        return this.result;
    }
    public String getSNIValue() {
        if ( this.result == ExtractorResult.COMPLETE ) {
            return this.sniValue;
        }
        throw new IllegalStateException();
    }
    public List<Cipher> getClientRequestedCiphers() {
        if ( this.result == ExtractorResult.COMPLETE || this.result == ExtractorResult.NOT_PRESENT ) {
            return this.clientRequestedCiphers;
        }
        throw new IllegalStateException();
    }
    private static ExtractorResult handleIncompleteRead ( final ByteBuffer bb ) {
        if ( bb.limit() == bb.capacity() ) {
            return ExtractorResult.UNDERFLOW;
        }
        return ExtractorResult.NEED_READ;
    }
    private static boolean isAvailable ( final ByteBuffer bb, final int size ) {
        if ( bb.remaining() < size ) {
            bb.position ( bb.limit() );
            return false;
        }
        return true;
    }
    private static boolean isTLSHandshake ( final ByteBuffer bb ) {
        if ( bb.get() != 22 ) {
            return false;
        }
        byte b2 = bb.get();
        b2 = bb.get();
        return b2 >= 3 && ( b2 != 3 || b2 != 0 );
    }
    private static boolean isAllRecordAvailable ( final ByteBuffer bb ) {
        final int size = bb.getChar();
        return isAvailable ( bb, size );
    }
    private static boolean isClientHello ( final ByteBuffer bb ) {
        return bb.get() == 1;
    }
    private static boolean isAllClientHelloAvailable ( final ByteBuffer bb ) {
        final int size = ( ( bb.get() & 0xFF ) << 16 ) + ( ( bb.get() & 0xFF ) << 8 ) + ( bb.get() & 0xFF );
        return isAvailable ( bb, size );
    }
    private static void skipBytes ( final ByteBuffer bb, final int size ) {
        bb.position ( bb.position() + size );
    }
    private static String readSniExtension ( final ByteBuffer bb ) {
        final char extensionType = bb.getChar();
        final char extensionDataSize = bb.getChar();
        if ( extensionType == '\0' ) {
            skipBytes ( bb, 3 );
            final char serverNameSize = bb.getChar();
            final byte[] serverNameBytes = new byte[serverNameSize];
            bb.get ( serverNameBytes );
            return new String ( serverNameBytes, StandardCharsets.UTF_8 );
        }
        skipBytes ( bb, extensionDataSize );
        return null;
    }
    static {
        log = LogFactory.getLog ( TLSClientHelloExtractor.class );
        sm = StringManager.getManager ( TLSClientHelloExtractor.class );
    }
    public enum ExtractorResult {
        COMPLETE,
        NOT_PRESENT,
        UNDERFLOW,
        NEED_READ;
    }
}
