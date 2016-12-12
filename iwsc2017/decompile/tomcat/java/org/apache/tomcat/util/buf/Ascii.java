package org.apache.tomcat.util.buf;
public final class Ascii {
    private static final byte[] toLower;
    private static final boolean[] isDigit;
    private static final long OVERFLOW_LIMIT = 922337203685477580L;
    public static int toLower ( final int c ) {
        return Ascii.toLower[c & 0xFF] & 0xFF;
    }
    private static boolean isDigit ( final int c ) {
        return Ascii.isDigit[c & 0xFF];
    }
    public static long parseLong ( final byte[] b, int off, int len ) throws NumberFormatException {
        int c;
        if ( b == null || len <= 0 || !isDigit ( c = b[off++] ) ) {
            throw new NumberFormatException();
        }
        long n = c - 48;
        while ( --len > 0 ) {
            if ( !isDigit ( c = b[off++] ) || ( n >= 922337203685477580L && ( n != 922337203685477580L || c - 48 >= 8 ) ) ) {
                throw new NumberFormatException();
            }
            n = n * 10L + c - 48L;
        }
        return n;
    }
    static {
        toLower = new byte[256];
        isDigit = new boolean[256];
        for ( int i = 0; i < 256; ++i ) {
            Ascii.toLower[i] = ( byte ) i;
        }
        for ( int lc = 97; lc <= 122; ++lc ) {
            final int uc = lc + 65 - 97;
            Ascii.toLower[uc] = ( byte ) lc;
        }
        for ( int d = 48; d <= 57; ++d ) {
            Ascii.isDigit[d] = true;
        }
    }
}
