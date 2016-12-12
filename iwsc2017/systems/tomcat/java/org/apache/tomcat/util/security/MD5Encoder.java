package org.apache.tomcat.util.security;
public final class MD5Encoder {
    private MD5Encoder() {
    }
    private static final char[] hexadecimal = {'0', '1', '2', '3', '4', '5',
                                               '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
                                              };
    public static String encode ( byte[] binaryData ) {
        if ( binaryData.length != 16 ) {
            return null;
        }
        char[] buffer = new char[32];
        for ( int i = 0; i < 16; i++ ) {
            int low = binaryData[i] & 0x0f;
            int high = ( binaryData[i] & 0xf0 ) >> 4;
            buffer[i * 2] = hexadecimal[high];
            buffer[i * 2 + 1] = hexadecimal[low];
        }
        return new String ( buffer );
    }
}
