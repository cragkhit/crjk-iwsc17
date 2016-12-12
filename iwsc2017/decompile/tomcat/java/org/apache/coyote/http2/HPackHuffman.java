package org.apache.coyote.http2;
import java.util.Set;
import java.util.Collection;
import java.util.Arrays;
import java.util.HashSet;
import java.nio.ByteBuffer;
import org.apache.tomcat.util.res.StringManager;
public class HPackHuffman {
    protected static final StringManager sm;
    private static final HuffmanCode[] HUFFMAN_CODES;
    private static final int[] DECODING_TABLE;
    private static final int LOW_TERMINAL_BIT = 32768;
    private static final int HIGH_TERMINAL_BIT = Integer.MIN_VALUE;
    private static final int LOW_MASK = 32767;
    public static void decode ( final ByteBuffer data, final int length, final StringBuilder target ) throws HpackException {
        assert data.remaining() >= length;
        int treePos = 0;
        boolean eosBits = true;
        for ( int i = 0; i < length; ++i ) {
            final byte b = data.get();
            for ( int bitPos = 7; bitPos >= 0; --bitPos ) {
                final int val = HPackHuffman.DECODING_TABLE[treePos];
                if ( ( 1 << bitPos & b ) == 0x0 ) {
                    eosBits = false;
                    if ( ( val & 0x8000 ) == 0x0 ) {
                        treePos = ( val & 0x7FFF );
                    } else {
                        target.append ( ( char ) ( val & 0x7FFF ) );
                        treePos = 0;
                        eosBits = true;
                    }
                } else if ( ( val & Integer.MIN_VALUE ) == 0x0 ) {
                    treePos = ( val >> 16 & 0x7FFF );
                } else {
                    target.append ( ( char ) ( val >> 16 & 0x7FFF ) );
                    treePos = 0;
                    eosBits = true;
                }
            }
        }
        if ( !eosBits ) {
            throw new HpackException ( HPackHuffman.sm.getString ( "hpackhuffman.huffmanEncodedHpackValueDidNotEndWithEOS" ) );
        }
    }
    public static boolean encode ( final ByteBuffer buffer, final String toEncode, final boolean forceLowercase ) {
        if ( buffer.remaining() <= toEncode.length() ) {
            return false;
        }
        final int start = buffer.position();
        int length = 0;
        for ( int i = 0; i < toEncode.length(); ++i ) {
            byte c = ( byte ) toEncode.charAt ( i );
            if ( forceLowercase ) {
                c = Hpack.toLower ( c );
            }
            final HuffmanCode code = HPackHuffman.HUFFMAN_CODES[c];
            length += code.length;
        }
        final int byteLength = length / 8 + ( ( length % 8 != 0 ) ? 1 : 0 );
        buffer.put ( ( byte ) ( -128 ) );
        Hpack.encodeInteger ( buffer, byteLength, 7 );
        int bytePos = 0;
        byte currentBufferByte = 0;
        for ( int j = 0; j < toEncode.length(); ++j ) {
            byte c2 = ( byte ) toEncode.charAt ( j );
            if ( forceLowercase ) {
                c2 = Hpack.toLower ( c2 );
            }
            final HuffmanCode code2 = HPackHuffman.HUFFMAN_CODES[c2];
            if ( code2.length + bytePos <= 8 ) {
                currentBufferByte |= ( byte ) ( ( code2.value & 0xFF ) << 8 - ( code2.length + bytePos ) );
                bytePos += code2.length;
            } else {
                final int val = code2.value;
                int remainingInByte;
                for ( int rem = code2.length; rem > 0; rem -= remainingInByte ) {
                    if ( !buffer.hasRemaining() ) {
                        buffer.position ( start );
                        return false;
                    }
                    remainingInByte = 8 - bytePos;
                    if ( rem > remainingInByte ) {
                        currentBufferByte |= ( byte ) ( val >> rem - remainingInByte );
                    } else {
                        currentBufferByte |= ( byte ) ( val << remainingInByte - rem );
                    }
                    if ( rem > remainingInByte ) {
                        buffer.put ( currentBufferByte );
                        currentBufferByte = 0;
                        bytePos = 0;
                    } else {
                        bytePos = rem;
                    }
                }
            }
            if ( bytePos == 8 ) {
                if ( !buffer.hasRemaining() ) {
                    buffer.position ( start );
                    return false;
                }
                buffer.put ( currentBufferByte );
                currentBufferByte = 0;
                bytePos = 0;
            }
            if ( buffer.position() - start > toEncode.length() ) {
                buffer.position ( start );
                return false;
            }
        }
        if ( bytePos > 0 ) {
            if ( !buffer.hasRemaining() ) {
                buffer.position ( start );
                return false;
            }
            buffer.put ( ( byte ) ( currentBufferByte | 255 >> bytePos ) );
        }
        return true;
    }
    static {
        sm = StringManager.getManager ( HPackHuffman.class );
        final HuffmanCode[] codes = HUFFMAN_CODES = new HuffmanCode[] { new HuffmanCode ( 8184, 13 ), new HuffmanCode ( 8388568, 23 ), new HuffmanCode ( 268435426, 28 ), new HuffmanCode ( 268435427, 28 ), new HuffmanCode ( 268435428, 28 ), new HuffmanCode ( 268435429, 28 ), new HuffmanCode ( 268435430, 28 ), new HuffmanCode ( 268435431, 28 ), new HuffmanCode ( 268435432, 28 ), new HuffmanCode ( 16777194, 24 ), new HuffmanCode ( 1073741820, 30 ), new HuffmanCode ( 268435433, 28 ), new HuffmanCode ( 268435434, 28 ), new HuffmanCode ( 1073741821, 30 ), new HuffmanCode ( 268435435, 28 ), new HuffmanCode ( 268435436, 28 ), new HuffmanCode ( 268435437, 28 ), new HuffmanCode ( 268435438, 28 ), new HuffmanCode ( 268435439, 28 ), new HuffmanCode ( 268435440, 28 ), new HuffmanCode ( 268435441, 28 ), new HuffmanCode ( 268435442, 28 ), new HuffmanCode ( 1073741822, 30 ), new HuffmanCode ( 268435443, 28 ), new HuffmanCode ( 268435444, 28 ), new HuffmanCode ( 268435445, 28 ), new HuffmanCode ( 268435446, 28 ), new HuffmanCode ( 268435447, 28 ), new HuffmanCode ( 268435448, 28 ), new HuffmanCode ( 268435449, 28 ), new HuffmanCode ( 268435450, 28 ), new HuffmanCode ( 268435451, 28 ), new HuffmanCode ( 20, 6 ), new HuffmanCode ( 1016, 10 ), new HuffmanCode ( 1017, 10 ), new HuffmanCode ( 4090, 12 ), new HuffmanCode ( 8185, 13 ), new HuffmanCode ( 21, 6 ), new HuffmanCode ( 248, 8 ), new HuffmanCode ( 2042, 11 ), new HuffmanCode ( 1018, 10 ), new HuffmanCode ( 1019, 10 ), new HuffmanCode ( 249, 8 ), new HuffmanCode ( 2043, 11 ), new HuffmanCode ( 250, 8 ), new HuffmanCode ( 22, 6 ), new HuffmanCode ( 23, 6 ), new HuffmanCode ( 24, 6 ), new HuffmanCode ( 0, 5 ), new HuffmanCode ( 1, 5 ), new HuffmanCode ( 2, 5 ), new HuffmanCode ( 25, 6 ), new HuffmanCode ( 26, 6 ), new HuffmanCode ( 27, 6 ), new HuffmanCode ( 28, 6 ), new HuffmanCode ( 29, 6 ), new HuffmanCode ( 30, 6 ), new HuffmanCode ( 31, 6 ), new HuffmanCode ( 92, 7 ), new HuffmanCode ( 251, 8 ), new HuffmanCode ( 32764, 15 ), new HuffmanCode ( 32, 6 ), new HuffmanCode ( 4091, 12 ), new HuffmanCode ( 1020, 10 ), new HuffmanCode ( 8186, 13 ), new HuffmanCode ( 33, 6 ), new HuffmanCode ( 93, 7 ), new HuffmanCode ( 94, 7 ), new HuffmanCode ( 95, 7 ), new HuffmanCode ( 96, 7 ), new HuffmanCode ( 97, 7 ), new HuffmanCode ( 98, 7 ), new HuffmanCode ( 99, 7 ), new HuffmanCode ( 100, 7 ), new HuffmanCode ( 101, 7 ), new HuffmanCode ( 102, 7 ), new HuffmanCode ( 103, 7 ), new HuffmanCode ( 104, 7 ), new HuffmanCode ( 105, 7 ), new HuffmanCode ( 106, 7 ), new HuffmanCode ( 107, 7 ), new HuffmanCode ( 108, 7 ), new HuffmanCode ( 109, 7 ), new HuffmanCode ( 110, 7 ), new HuffmanCode ( 111, 7 ), new HuffmanCode ( 112, 7 ), new HuffmanCode ( 113, 7 ), new HuffmanCode ( 114, 7 ), new HuffmanCode ( 252, 8 ), new HuffmanCode ( 115, 7 ), new HuffmanCode ( 253, 8 ), new HuffmanCode ( 8187, 13 ), new HuffmanCode ( 524272, 19 ), new HuffmanCode ( 8188, 13 ), new HuffmanCode ( 16380, 14 ), new HuffmanCode ( 34, 6 ), new HuffmanCode ( 32765, 15 ), new HuffmanCode ( 3, 5 ), new HuffmanCode ( 35, 6 ), new HuffmanCode ( 4, 5 ), new HuffmanCode ( 36, 6 ), new HuffmanCode ( 5, 5 ), new HuffmanCode ( 37, 6 ), new HuffmanCode ( 38, 6 ), new HuffmanCode ( 39, 6 ), new HuffmanCode ( 6, 5 ), new HuffmanCode ( 116, 7 ), new HuffmanCode ( 117, 7 ), new HuffmanCode ( 40, 6 ), new HuffmanCode ( 41, 6 ), new HuffmanCode ( 42, 6 ), new HuffmanCode ( 7, 5 ), new HuffmanCode ( 43, 6 ), new HuffmanCode ( 118, 7 ), new HuffmanCode ( 44, 6 ), new HuffmanCode ( 8, 5 ), new HuffmanCode ( 9, 5 ), new HuffmanCode ( 45, 6 ), new HuffmanCode ( 119, 7 ), new HuffmanCode ( 120, 7 ), new HuffmanCode ( 121, 7 ), new HuffmanCode ( 122, 7 ), new HuffmanCode ( 123, 7 ), new HuffmanCode ( 32766, 15 ), new HuffmanCode ( 2044, 11 ), new HuffmanCode ( 16381, 14 ), new HuffmanCode ( 8189, 13 ), new HuffmanCode ( 268435452, 28 ), new HuffmanCode ( 1048550, 20 ), new HuffmanCode ( 4194258, 22 ), new HuffmanCode ( 1048551, 20 ), new HuffmanCode ( 1048552, 20 ), new HuffmanCode ( 4194259, 22 ), new HuffmanCode ( 4194260, 22 ), new HuffmanCode ( 4194261, 22 ), new HuffmanCode ( 8388569, 23 ), new HuffmanCode ( 4194262, 22 ), new HuffmanCode ( 8388570, 23 ), new HuffmanCode ( 8388571, 23 ), new HuffmanCode ( 8388572, 23 ), new HuffmanCode ( 8388573, 23 ), new HuffmanCode ( 8388574, 23 ), new HuffmanCode ( 16777195, 24 ), new HuffmanCode ( 8388575, 23 ), new HuffmanCode ( 16777196, 24 ), new HuffmanCode ( 16777197, 24 ), new HuffmanCode ( 4194263, 22 ), new HuffmanCode ( 8388576, 23 ), new HuffmanCode ( 16777198, 24 ), new HuffmanCode ( 8388577, 23 ), new HuffmanCode ( 8388578, 23 ), new HuffmanCode ( 8388579, 23 ), new HuffmanCode ( 8388580, 23 ), new HuffmanCode ( 2097116, 21 ), new HuffmanCode ( 4194264, 22 ), new HuffmanCode ( 8388581, 23 ), new HuffmanCode ( 4194265, 22 ), new HuffmanCode ( 8388582, 23 ), new HuffmanCode ( 8388583, 23 ), new HuffmanCode ( 16777199, 24 ), new HuffmanCode ( 4194266, 22 ), new HuffmanCode ( 2097117, 21 ), new HuffmanCode ( 1048553, 20 ), new HuffmanCode ( 4194267, 22 ), new HuffmanCode ( 4194268, 22 ), new HuffmanCode ( 8388584, 23 ), new HuffmanCode ( 8388585, 23 ), new HuffmanCode ( 2097118, 21 ), new HuffmanCode ( 8388586, 23 ), new HuffmanCode ( 4194269, 22 ), new HuffmanCode ( 4194270, 22 ), new HuffmanCode ( 16777200, 24 ), new HuffmanCode ( 2097119, 21 ), new HuffmanCode ( 4194271, 22 ), new HuffmanCode ( 8388587, 23 ), new HuffmanCode ( 8388588, 23 ), new HuffmanCode ( 2097120, 21 ), new HuffmanCode ( 2097121, 21 ), new HuffmanCode ( 4194272, 22 ), new HuffmanCode ( 2097122, 21 ), new HuffmanCode ( 8388589, 23 ), new HuffmanCode ( 4194273, 22 ), new HuffmanCode ( 8388590, 23 ), new HuffmanCode ( 8388591, 23 ), new HuffmanCode ( 1048554, 20 ), new HuffmanCode ( 4194274, 22 ), new HuffmanCode ( 4194275, 22 ), new HuffmanCode ( 4194276, 22 ), new HuffmanCode ( 8388592, 23 ), new HuffmanCode ( 4194277, 22 ), new HuffmanCode ( 4194278, 22 ), new HuffmanCode ( 8388593, 23 ), new HuffmanCode ( 67108832, 26 ), new HuffmanCode ( 67108833, 26 ), new HuffmanCode ( 1048555, 20 ), new HuffmanCode ( 524273, 19 ), new HuffmanCode ( 4194279, 22 ), new HuffmanCode ( 8388594, 23 ), new HuffmanCode ( 4194280, 22 ), new HuffmanCode ( 33554412, 25 ), new HuffmanCode ( 67108834, 26 ), new HuffmanCode ( 67108835, 26 ), new HuffmanCode ( 67108836, 26 ), new HuffmanCode ( 134217694, 27 ), new HuffmanCode ( 134217695, 27 ), new HuffmanCode ( 67108837, 26 ), new HuffmanCode ( 16777201, 24 ), new HuffmanCode ( 33554413, 25 ), new HuffmanCode ( 524274, 19 ), new HuffmanCode ( 2097123, 21 ), new HuffmanCode ( 67108838, 26 ), new HuffmanCode ( 134217696, 27 ), new HuffmanCode ( 134217697, 27 ), new HuffmanCode ( 67108839, 26 ), new HuffmanCode ( 134217698, 27 ), new HuffmanCode ( 16777202, 24 ), new HuffmanCode ( 2097124, 21 ), new HuffmanCode ( 2097125, 21 ), new HuffmanCode ( 67108840, 26 ), new HuffmanCode ( 67108841, 26 ), new HuffmanCode ( 268435453, 28 ), new HuffmanCode ( 134217699, 27 ), new HuffmanCode ( 134217700, 27 ), new HuffmanCode ( 134217701, 27 ), new HuffmanCode ( 1048556, 20 ), new HuffmanCode ( 16777203, 24 ), new HuffmanCode ( 1048557, 20 ), new HuffmanCode ( 2097126, 21 ), new HuffmanCode ( 4194281, 22 ), new HuffmanCode ( 2097127, 21 ), new HuffmanCode ( 2097128, 21 ), new HuffmanCode ( 8388595, 23 ), new HuffmanCode ( 4194282, 22 ), new HuffmanCode ( 4194283, 22 ), new HuffmanCode ( 33554414, 25 ), new HuffmanCode ( 33554415, 25 ), new HuffmanCode ( 16777204, 24 ), new HuffmanCode ( 16777205, 24 ), new HuffmanCode ( 67108842, 26 ), new HuffmanCode ( 8388596, 23 ), new HuffmanCode ( 67108843, 26 ), new HuffmanCode ( 134217702, 27 ), new HuffmanCode ( 67108844, 26 ), new HuffmanCode ( 67108845, 26 ), new HuffmanCode ( 134217703, 27 ), new HuffmanCode ( 134217704, 27 ), new HuffmanCode ( 134217705, 27 ), new HuffmanCode ( 134217706, 27 ), new HuffmanCode ( 134217707, 27 ), new HuffmanCode ( 268435454, 28 ), new HuffmanCode ( 134217708, 27 ), new HuffmanCode ( 134217709, 27 ), new HuffmanCode ( 134217710, 27 ), new HuffmanCode ( 134217711, 27 ), new HuffmanCode ( 134217712, 27 ), new HuffmanCode ( 67108846, 26 ), new HuffmanCode ( 1073741823, 30 ) };
        final int[] codingTree = new int[256];
        int pos = 0;
        int allocated = 1;
        final HuffmanCode[] currentCode = new HuffmanCode[256];
        currentCode[0] = new HuffmanCode ( 0, 0 );
        final Set<HuffmanCode> allCodes = new HashSet<HuffmanCode>();
        allCodes.addAll ( Arrays.asList ( HPackHuffman.HUFFMAN_CODES ) );
        while ( !allCodes.isEmpty() ) {
            final int length = currentCode[pos].length;
            final int code = currentCode[pos].value;
            final int newLength = length + 1;
            final HuffmanCode high = new HuffmanCode ( code << 1 | 0x1, newLength );
            final HuffmanCode low = new HuffmanCode ( code << 1, newLength );
            int newVal = 0;
            final boolean highTerminal = allCodes.remove ( high );
            if ( highTerminal ) {
                int i;
                for ( i = 0, i = 0; i < codes.length && !codes[i].equals ( high ); ++i ) {}
                newVal = ( 0x8000 | i );
            } else {
                final int highPos = allocated++;
                currentCode[highPos] = high;
                newVal = highPos;
            }
            newVal <<= 16;
            final boolean lowTerminal = allCodes.remove ( low );
            if ( lowTerminal ) {
                int j;
                for ( j = 0, j = 0; j < codes.length && !codes[j].equals ( low ); ++j ) {}
                newVal |= ( 0x8000 | j );
            } else {
                final int lowPos = allocated++;
                currentCode[lowPos] = low;
                newVal |= lowPos;
            }
            codingTree[pos] = newVal;
            ++pos;
        }
        DECODING_TABLE = codingTree;
    }
    protected static class HuffmanCode {
        int value;
        int length;
        public HuffmanCode ( final int value, final int length ) {
            this.value = value;
            this.length = length;
        }
        public int getValue() {
            return this.value;
        }
        public int getLength() {
            return this.length;
        }
        @Override
        public boolean equals ( final Object o ) {
            if ( this == o ) {
                return true;
            }
            if ( o == null || this.getClass() != o.getClass() ) {
                return false;
            }
            final HuffmanCode that = ( HuffmanCode ) o;
            return this.length == that.length && this.value == that.value;
        }
        @Override
        public int hashCode() {
            int result = this.value;
            result = 31 * result + this.length;
            return result;
        }
        @Override
        public String toString() {
            return "HuffmanCode{value=" + this.value + ", length=" + this.length + '}';
        }
    }
}
