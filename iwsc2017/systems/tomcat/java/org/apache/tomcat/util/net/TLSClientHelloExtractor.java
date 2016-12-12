package org.apache.tomcat.util.net;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.net.openssl.ciphers.Cipher;
import org.apache.tomcat.util.res.StringManager;
public class TLSClientHelloExtractor {
    private static final Log log = LogFactory.getLog ( TLSClientHelloExtractor.class );
    private static final StringManager sm = StringManager.getManager ( TLSClientHelloExtractor.class );
    private final ExtractorResult result;
    private final List<Cipher> clientRequestedCiphers;
    private final String sniValue;
    private static final int TLS_RECORD_HEADER_LEN = 5;
    public TLSClientHelloExtractor ( ByteBuffer netInBuffer ) {
        int pos = netInBuffer.position();
        int limit = netInBuffer.limit();
        ExtractorResult result = ExtractorResult.NOT_PRESENT;
        List<Cipher> clientRequestedCiphers = new ArrayList<>();
        String sniValue = null;
        try {
            netInBuffer.flip();
            if ( !isAvailable ( netInBuffer, TLS_RECORD_HEADER_LEN ) ) {
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
                log.warn ( sm.getString ( "sniExtractor.clientHelloTooBig" ) );
                return;
            }
            skipBytes ( netInBuffer, 2 );
            skipBytes ( netInBuffer, 32 );
            skipBytes ( netInBuffer, ( netInBuffer.get() & 0xFF ) );
            int cipherCount = netInBuffer.getChar() / 2;
            for ( int i = 0; i < cipherCount; i++ ) {
                int cipherId = netInBuffer.getChar();
                clientRequestedCiphers.add ( Cipher.valueOf ( cipherId ) );
            }
            skipBytes ( netInBuffer, ( netInBuffer.get() & 0xFF ) );
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
        return result;
    }
    public String getSNIValue() {
        if ( result == ExtractorResult.COMPLETE ) {
            return sniValue;
        } else {
            throw new IllegalStateException();
        }
    }
    public List<Cipher> getClientRequestedCiphers() {
        if ( result == ExtractorResult.COMPLETE || result == ExtractorResult.NOT_PRESENT ) {
            return clientRequestedCiphers;
        } else {
            throw new IllegalStateException();
        }
    }
    private static ExtractorResult handleIncompleteRead ( ByteBuffer bb ) {
        if ( bb.limit() == bb.capacity() ) {
            return ExtractorResult.UNDERFLOW;
        } else {
            return ExtractorResult.NEED_READ;
        }
    }
    private static boolean isAvailable ( ByteBuffer bb, int size ) {
        if ( bb.remaining() < size ) {
            bb.position ( bb.limit() );
            return false;
        }
        return true;
    }
    private static boolean isTLSHandshake ( ByteBuffer bb ) {
        if ( bb.get() != 22 ) {
            return false;
        }
        byte b2 = bb.get();
        byte b3 = bb.get();
        if ( b2 < 3 || b2 == 3 && b3 == 0 ) {
            return false;
        }
        return true;
    }
    private static boolean isAllRecordAvailable ( ByteBuffer bb ) {
        int size = bb.getChar();
        return isAvailable ( bb, size );
    }
    private static boolean isClientHello ( ByteBuffer bb ) {
        if ( bb.get() == 1 ) {
            return true;
        }
        return false;
    }
    private static boolean isAllClientHelloAvailable ( ByteBuffer bb ) {
        int size = ( ( bb.get() & 0xFF ) << 16 ) + ( ( bb.get() & 0xFF ) << 8 ) + ( bb.get() & 0xFF );
        return isAvailable ( bb, size );
    }
    private static void skipBytes ( ByteBuffer bb, int size ) {
        bb.position ( bb.position() + size );
    }
    private static String readSniExtension ( ByteBuffer bb ) {
        char extensionType = bb.getChar();
        char extensionDataSize = bb.getChar();
        if ( extensionType == 0 ) {
            skipBytes ( bb, 3 );
            char serverNameSize = bb.getChar();
            byte[] serverNameBytes = new byte[serverNameSize];
            bb.get ( serverNameBytes );
            return new String ( serverNameBytes, StandardCharsets.UTF_8 );
        } else {
            skipBytes ( bb, extensionDataSize );
        }
        return null;
    }
    public static enum ExtractorResult {
        COMPLETE,
        NOT_PRESENT,
        UNDERFLOW,
        NEED_READ
    }
}
