package org.apache.coyote.http2;
import java.nio.ByteBuffer;
import org.apache.tomcat.util.res.StringManager;
final class Hpack {
    private static final StringManager sm;
    private static final byte LOWER_DIFF = 32;
    static final int DEFAULT_TABLE_SIZE = 4096;
    private static final int MAX_INTEGER_OCTETS = 8;
    private static final int[] PREFIX_TABLE;
    static final HeaderField[] STATIC_TABLE;
    static final int STATIC_TABLE_LENGTH;
    static int decodeInteger ( final ByteBuffer source, final int n ) throws HpackException {
        if ( source.remaining() == 0 ) {
            return -1;
        }
        int count = 1;
        final int sp = source.position();
        final int mask = Hpack.PREFIX_TABLE[n];
        int i = mask & source.get();
        if ( i < Hpack.PREFIX_TABLE[n] ) {
            return i;
        }
        int m = 0;
        while ( count++ <= 8 ) {
            if ( source.remaining() == 0 ) {
                source.position ( sp );
                return -1;
            }
            final int b = source.get();
            i += ( b & 0x7F ) * ( Hpack.PREFIX_TABLE[m] + 1 );
            m += 7;
            if ( ( b & 0x80 ) != 0x80 ) {
                return i;
            }
        }
        throw new HpackException ( Hpack.sm.getString ( "hpack.integerEncodedOverTooManyOctets", 8 ) );
    }
    static void encodeInteger ( final ByteBuffer source, int value, final int n ) {
        final int twoNminus1 = Hpack.PREFIX_TABLE[n];
        final int pos = source.position() - 1;
        if ( value < twoNminus1 ) {
            source.put ( pos, ( byte ) ( source.get ( pos ) | value ) );
        } else {
            source.put ( pos, ( byte ) ( source.get ( pos ) | twoNminus1 ) );
            for ( value -= twoNminus1; value >= 128; value /= 128 ) {
                source.put ( ( byte ) ( value % 128 + 128 ) );
            }
            source.put ( ( byte ) value );
        }
    }
    static byte toLower ( final byte b ) {
        if ( b >= 65 && b <= 90 ) {
            return ( byte ) ( b + 32 );
        }
        return b;
    }
    static {
        sm = StringManager.getManager ( Hpack.class );
        PREFIX_TABLE = new int[32];
        for ( int i = 0; i < 32; ++i ) {
            int n = 0;
            for ( int j = 0; j < i; ++j ) {
                n <<= 1;
                n |= 0x1;
            }
            Hpack.PREFIX_TABLE[i] = n;
        }
        final HeaderField[] fields = STATIC_TABLE = new HeaderField[] { null, new HeaderField ( ":authority", null ), new HeaderField ( ":method", "GET" ), new HeaderField ( ":method", "POST" ), new HeaderField ( ":path", "/" ), new HeaderField ( ":path", "/index.html" ), new HeaderField ( ":scheme", "http" ), new HeaderField ( ":scheme", "https" ), new HeaderField ( ":status", "200" ), new HeaderField ( ":status", "204" ), new HeaderField ( ":status", "206" ), new HeaderField ( ":status", "304" ), new HeaderField ( ":status", "400" ), new HeaderField ( ":status", "404" ), new HeaderField ( ":status", "500" ), new HeaderField ( "accept-charset", null ), new HeaderField ( "accept-encoding", "gzip, deflate" ), new HeaderField ( "accept-language", null ), new HeaderField ( "accept-ranges", null ), new HeaderField ( "accept", null ), new HeaderField ( "access-control-allow-origin", null ), new HeaderField ( "age", null ), new HeaderField ( "allow", null ), new HeaderField ( "authorization", null ), new HeaderField ( "cache-control", null ), new HeaderField ( "content-disposition", null ), new HeaderField ( "content-encoding", null ), new HeaderField ( "content-language", null ), new HeaderField ( "content-length", null ), new HeaderField ( "content-location", null ), new HeaderField ( "content-range", null ), new HeaderField ( "content-type", null ), new HeaderField ( "cookie", null ), new HeaderField ( "date", null ), new HeaderField ( "etag", null ), new HeaderField ( "expect", null ), new HeaderField ( "expires", null ), new HeaderField ( "from", null ), new HeaderField ( "host", null ), new HeaderField ( "if-match", null ), new HeaderField ( "if-modified-since", null ), new HeaderField ( "if-none-match", null ), new HeaderField ( "if-range", null ), new HeaderField ( "if-unmodified-since", null ), new HeaderField ( "last-modified", null ), new HeaderField ( "link", null ), new HeaderField ( "location", null ), new HeaderField ( "max-forwards", null ), new HeaderField ( "proxy-authenticate", null ), new HeaderField ( "proxy-authorization", null ), new HeaderField ( "range", null ), new HeaderField ( "referer", null ), new HeaderField ( "refresh", null ), new HeaderField ( "retry-after", null ), new HeaderField ( "server", null ), new HeaderField ( "set-cookie", null ), new HeaderField ( "strict-transport-security", null ), new HeaderField ( "transfer-encoding", null ), new HeaderField ( "user-agent", null ), new HeaderField ( "vary", null ), new HeaderField ( "via", null ), new HeaderField ( "www-authenticate", null ) };
        STATIC_TABLE_LENGTH = Hpack.STATIC_TABLE.length - 1;
    }
    static class HeaderField {
        final String name;
        final String value;
        final int size;
        HeaderField ( final String name, final String value ) {
            this.name = name;
            this.value = value;
            if ( value != null ) {
                this.size = 32 + name.length() + value.length();
            } else {
                this.size = -1;
            }
        }
    }
}
