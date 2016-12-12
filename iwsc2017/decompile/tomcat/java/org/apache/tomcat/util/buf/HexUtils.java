package org.apache.tomcat.util.buf;
import org.apache.tomcat.util.res.StringManager;
public final class HexUtils {
    private static final StringManager sm;
    private static final int[] DEC;
    private static final byte[] HEX;
    private static final char[] hex;
    public static int getDec ( final int index ) {
        try {
            return HexUtils.DEC[index - 48];
        } catch ( ArrayIndexOutOfBoundsException ex ) {
            return -1;
        }
    }
    public static byte getHex ( final int index ) {
        return HexUtils.HEX[index];
    }
    public static String toHexString ( final byte[] bytes ) {
        if ( null == bytes ) {
            return null;
        }
        final StringBuilder sb = new StringBuilder ( bytes.length << 1 );
        for ( int i = 0; i < bytes.length; ++i ) {
            sb.append ( HexUtils.hex[ ( bytes[i] & 0xF0 ) >> 4] ).append ( HexUtils.hex[bytes[i] & 0xF] );
        }
        return sb.toString();
    }
    public static byte[] fromHexString ( final String input ) {
        if ( input == null ) {
            return null;
        }
        if ( ( input.length() & 0x1 ) == 0x1 ) {
            throw new IllegalArgumentException ( HexUtils.sm.getString ( "hexUtils.fromHex.oddDigits" ) );
        }
        final char[] inputChars = input.toCharArray();
        final byte[] result = new byte[input.length() >> 1];
        for ( int i = 0; i < result.length; ++i ) {
            final int upperNibble = getDec ( inputChars[2 * i] );
            final int lowerNibble = getDec ( inputChars[2 * i + 1] );
            if ( upperNibble < 0 || lowerNibble < 0 ) {
                throw new IllegalArgumentException ( HexUtils.sm.getString ( "hexUtils.fromHex.nonHex" ) );
            }
            result[i] = ( byte ) ( ( upperNibble << 4 ) + lowerNibble );
        }
        return result;
    }
    static {
        sm = StringManager.getManager ( HexUtils.class );
        DEC = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15 };
        HEX = new byte[] { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102 };
        hex = "0123456789abcdef".toCharArray();
    }
}
