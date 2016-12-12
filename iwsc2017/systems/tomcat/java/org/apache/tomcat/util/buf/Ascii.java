package org.apache.tomcat.util.buf;
public final class Ascii {
    private static final byte[] toLower = new byte[256];
    private static final boolean[] isDigit = new boolean[256];
    private static final long OVERFLOW_LIMIT = Long.MAX_VALUE / 10;
    static {
        for ( int i = 0; i < 256; i++ ) {
            toLower[i] = ( byte ) i;
        }
        for ( int lc = 'a'; lc <= 'z'; lc++ ) {
            int uc = lc + 'A' - 'a';
            toLower[uc] = ( byte ) lc;
        }
        for ( int d = '0'; d <= '9'; d++ ) {
            isDigit[d] = true;
        }
    }
    public static int toLower ( int c ) {
        return toLower[c & 0xff] & 0xff;
    }
    private static boolean isDigit ( int c ) {
        return isDigit[c & 0xff];
    }
    public static long parseLong ( byte[] b, int off, int len )
    throws NumberFormatException {
        int c;
        if ( b == null || len <= 0 || !isDigit ( c = b[off++] ) ) {
            throw new NumberFormatException();
        }
        long n = c - '0';
        while ( --len > 0 ) {
            if ( isDigit ( c = b[off++] ) &&
                    ( n < OVERFLOW_LIMIT || ( n == OVERFLOW_LIMIT && ( c - '0' ) < 8 ) ) ) {
                n = n * 10 + c - '0';
            } else {
                throw new NumberFormatException();
            }
        }
        return n;
    }
}
