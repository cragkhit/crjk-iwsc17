package org.apache.coyote.http2;
class ByteUtil {
    static boolean isBit7Set ( final byte input ) {
        return ( input & 0x80 ) > 0;
    }
    static int get31Bits ( final byte[] input, final int firstByte ) {
        return ( ( input[firstByte] & 0x7F ) << 24 ) + ( ( input[firstByte + 1] & 0xFF ) << 16 ) + ( ( input[firstByte + 2] & 0xFF ) << 8 ) + ( input[firstByte + 3] & 0xFF );
    }
    static void set31Bits ( final byte[] output, final int firstByte, final int value ) {
        output[firstByte] = ( byte ) ( ( value & 0x7F000000 ) >> 24 );
        output[firstByte + 1] = ( byte ) ( ( value & 0xFF0000 ) >> 16 );
        output[firstByte + 2] = ( byte ) ( ( value & 0xFF00 ) >> 8 );
        output[firstByte + 3] = ( byte ) ( value & 0xFF );
    }
    static int getOneByte ( final byte[] input, final int pos ) {
        return input[pos] & 0xFF;
    }
    static int getTwoBytes ( final byte[] input, final int firstByte ) {
        return ( ( input[firstByte] & 0xFF ) << 8 ) + ( input[firstByte + 1] & 0xFF );
    }
    static int getThreeBytes ( final byte[] input, final int firstByte ) {
        return ( ( input[firstByte] & 0xFF ) << 16 ) + ( ( input[firstByte + 1] & 0xFF ) << 8 ) + ( input[firstByte + 2] & 0xFF );
    }
    static void setTwoBytes ( final byte[] output, final int firstByte, final int value ) {
        output[firstByte] = ( byte ) ( ( value & 0xFF00 ) >> 8 );
        output[firstByte + 1] = ( byte ) ( value & 0xFF );
    }
    static void setThreeBytes ( final byte[] output, final int firstByte, final int value ) {
        output[firstByte] = ( byte ) ( ( value & 0xFF0000 ) >> 16 );
        output[firstByte + 1] = ( byte ) ( ( value & 0xFF00 ) >> 8 );
        output[firstByte + 2] = ( byte ) ( value & 0xFF );
    }
    static long getFourBytes ( final byte[] input, final int firstByte ) {
        return ( ( input[firstByte] & 0xFF ) << 24 ) + ( ( input[firstByte + 1] & 0xFF ) << 16 ) + ( ( input[firstByte + 2] & 0xFF ) << 8 ) + ( input[firstByte + 3] & 0xFF );
    }
    static void setFourBytes ( final byte[] output, final int firstByte, final long value ) {
        output[firstByte] = ( byte ) ( ( value & 0xFFFFFFFFFF000000L ) >> 24 );
        output[firstByte + 1] = ( byte ) ( ( value & 0xFF0000L ) >> 16 );
        output[firstByte + 2] = ( byte ) ( ( value & 0xFF00L ) >> 8 );
        output[firstByte + 3] = ( byte ) ( value & 0xFFL );
    }
}
