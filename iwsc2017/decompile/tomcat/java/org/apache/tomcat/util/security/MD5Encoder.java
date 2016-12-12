package org.apache.tomcat.util.security;
public final class MD5Encoder {
    private static final char[] hexadecimal;
    public static String encode ( final byte[] binaryData ) {
        if ( binaryData.length != 16 ) {
            return null;
        }
        final char[] buffer = new char[32];
        for ( int i = 0; i < 16; ++i ) {
            final int low = binaryData[i] & 0xF;
            final int high = ( binaryData[i] & 0xF0 ) >> 4;
            buffer[i * 2] = MD5Encoder.hexadecimal[high];
            buffer[i * 2 + 1] = MD5Encoder.hexadecimal[low];
        }
        return new String ( buffer );
    }
    static {
        hexadecimal = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    }
}
