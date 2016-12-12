package org.apache.tomcat.util.http.fileupload.util.mime;
import java.io.IOException;
import java.io.OutputStream;
final class QuotedPrintableDecoder {
    private static final int UPPER_NIBBLE_SHIFT = Byte.SIZE / 2;
    private QuotedPrintableDecoder() {
    }
    public static int decode ( byte[] data, OutputStream out ) throws IOException {
        int off = 0;
        int length = data.length;
        int endOffset = off + length;
        int bytesWritten = 0;
        while ( off < endOffset ) {
            byte ch = data[off++];
            if ( ch == '_' ) {
                out.write ( ' ' );
            } else if ( ch == '=' ) {
                if ( off + 1 >= endOffset ) {
                    throw new IOException ( "Invalid quoted printable encoding; truncated escape sequence" );
                }
                byte b1 = data[off++];
                byte b2 = data[off++];
                if ( b1 == '\r' ) {
                    if ( b2 != '\n' ) {
                        throw new IOException ( "Invalid quoted printable encoding; CR must be followed by LF" );
                    }
                } else {
                    int c1 = hexToBinary ( b1 );
                    int c2 = hexToBinary ( b2 );
                    out.write ( ( c1 << UPPER_NIBBLE_SHIFT ) | c2 );
                    bytesWritten++;
                }
            } else {
                out.write ( ch );
                bytesWritten++;
            }
        }
        return bytesWritten;
    }
    private static int hexToBinary ( final byte b ) throws IOException {
        final int i = Character.digit ( ( char ) b, 16 );
        if ( i == -1 ) {
            throw new IOException ( "Invalid quoted printable encoding: not a valid hex digit: " + b );
        }
        return i;
    }
}
