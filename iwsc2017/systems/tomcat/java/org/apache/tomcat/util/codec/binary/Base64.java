package org.apache.tomcat.util.codec.binary;
import java.math.BigInteger;
public class Base64 extends BaseNCodec {
    private static final int BITS_PER_ENCODED_BYTE = 6;
    private static final int BYTES_PER_UNENCODED_BLOCK = 3;
    private static final int BYTES_PER_ENCODED_BLOCK = 4;
    static final byte[] CHUNK_SEPARATOR = {'\r', '\n'};
    private static final byte[] STANDARD_ENCODE_TABLE = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };
    private static final byte[] URL_SAFE_ENCODE_TABLE = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'
    };
    private static final byte[] DECODE_TABLE = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, 62, -1, 63,
        52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1,
        -1,  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14,
        15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63,
        -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
        41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
    };
    private static final int MASK_6BITS = 0x3f;
    private final byte[] encodeTable;
    private final byte[] decodeTable = DECODE_TABLE;
    private final byte[] lineSeparator;
    private final int decodeSize;
    private final int encodeSize;
    public Base64() {
        this ( 0 );
    }
    public Base64 ( final boolean urlSafe ) {
        this ( MIME_CHUNK_SIZE, CHUNK_SEPARATOR, urlSafe );
    }
    public Base64 ( final int lineLength ) {
        this ( lineLength, CHUNK_SEPARATOR );
    }
    public Base64 ( final int lineLength, final byte[] lineSeparator ) {
        this ( lineLength, lineSeparator, false );
    }
    public Base64 ( final int lineLength, final byte[] lineSeparator, final boolean urlSafe ) {
        super ( BYTES_PER_UNENCODED_BLOCK, BYTES_PER_ENCODED_BLOCK,
                lineLength,
                lineSeparator == null ? 0 : lineSeparator.length );
        if ( lineSeparator != null ) {
            if ( containsAlphabetOrPad ( lineSeparator ) ) {
                final String sep = StringUtils.newStringUtf8 ( lineSeparator );
                throw new IllegalArgumentException ( "lineSeparator must not contain base64 characters: [" + sep + "]" );
            }
            if ( lineLength > 0 ) {
                this.encodeSize = BYTES_PER_ENCODED_BLOCK + lineSeparator.length;
                this.lineSeparator = new byte[lineSeparator.length];
                System.arraycopy ( lineSeparator, 0, this.lineSeparator, 0, lineSeparator.length );
            } else {
                this.encodeSize = BYTES_PER_ENCODED_BLOCK;
                this.lineSeparator = null;
            }
        } else {
            this.encodeSize = BYTES_PER_ENCODED_BLOCK;
            this.lineSeparator = null;
        }
        this.decodeSize = this.encodeSize - 1;
        this.encodeTable = urlSafe ? URL_SAFE_ENCODE_TABLE : STANDARD_ENCODE_TABLE;
    }
    public boolean isUrlSafe() {
        return this.encodeTable == URL_SAFE_ENCODE_TABLE;
    }
    @Override
    void encode ( final byte[] in, int inPos, final int inAvail, final Context context ) {
        if ( context.eof ) {
            return;
        }
        if ( inAvail < 0 ) {
            context.eof = true;
            if ( 0 == context.modulus && lineLength == 0 ) {
                return;
            }
            final byte[] buffer = ensureBufferSize ( encodeSize, context );
            final int savedPos = context.pos;
            switch ( context.modulus ) {
            case 0 :
                break;
            case 1 :
                buffer[context.pos++] = encodeTable[ ( context.ibitWorkArea >> 2 ) & MASK_6BITS];
                buffer[context.pos++] = encodeTable[ ( context.ibitWorkArea << 4 ) & MASK_6BITS];
                if ( encodeTable == STANDARD_ENCODE_TABLE ) {
                    buffer[context.pos++] = pad;
                    buffer[context.pos++] = pad;
                }
                break;
            case 2 :
                buffer[context.pos++] = encodeTable[ ( context.ibitWorkArea >> 10 ) & MASK_6BITS];
                buffer[context.pos++] = encodeTable[ ( context.ibitWorkArea >> 4 ) & MASK_6BITS];
                buffer[context.pos++] = encodeTable[ ( context.ibitWorkArea << 2 ) & MASK_6BITS];
                if ( encodeTable == STANDARD_ENCODE_TABLE ) {
                    buffer[context.pos++] = pad;
                }
                break;
            default:
                throw new IllegalStateException ( "Impossible modulus " + context.modulus );
            }
            context.currentLinePos += context.pos - savedPos;
            if ( lineLength > 0 && context.currentLinePos > 0 ) {
                System.arraycopy ( lineSeparator, 0, buffer, context.pos, lineSeparator.length );
                context.pos += lineSeparator.length;
            }
        } else {
            for ( int i = 0; i < inAvail; i++ ) {
                final byte[] buffer = ensureBufferSize ( encodeSize, context );
                context.modulus = ( context.modulus + 1 ) % BYTES_PER_UNENCODED_BLOCK;
                int b = in[inPos++];
                if ( b < 0 ) {
                    b += 256;
                }
                context.ibitWorkArea = ( context.ibitWorkArea << 8 ) + b;
                if ( 0 == context.modulus ) {
                    buffer[context.pos++] = encodeTable[ ( context.ibitWorkArea >> 18 ) & MASK_6BITS];
                    buffer[context.pos++] = encodeTable[ ( context.ibitWorkArea >> 12 ) & MASK_6BITS];
                    buffer[context.pos++] = encodeTable[ ( context.ibitWorkArea >> 6 ) & MASK_6BITS];
                    buffer[context.pos++] = encodeTable[context.ibitWorkArea & MASK_6BITS];
                    context.currentLinePos += BYTES_PER_ENCODED_BLOCK;
                    if ( lineLength > 0 && lineLength <= context.currentLinePos ) {
                        System.arraycopy ( lineSeparator, 0, buffer, context.pos, lineSeparator.length );
                        context.pos += lineSeparator.length;
                        context.currentLinePos = 0;
                    }
                }
            }
        }
    }
    @Override
    void decode ( final byte[] in, int inPos, final int inAvail, final Context context ) {
        if ( context.eof ) {
            return;
        }
        if ( inAvail < 0 ) {
            context.eof = true;
        }
        for ( int i = 0; i < inAvail; i++ ) {
            final byte[] buffer = ensureBufferSize ( decodeSize, context );
            final byte b = in[inPos++];
            if ( b == pad ) {
                context.eof = true;
                break;
            }
            if ( b >= 0 && b < DECODE_TABLE.length ) {
                final int result = DECODE_TABLE[b];
                if ( result >= 0 ) {
                    context.modulus = ( context.modulus + 1 ) % BYTES_PER_ENCODED_BLOCK;
                    context.ibitWorkArea = ( context.ibitWorkArea << BITS_PER_ENCODED_BYTE ) + result;
                    if ( context.modulus == 0 ) {
                        buffer[context.pos++] = ( byte ) ( ( context.ibitWorkArea >> 16 ) & MASK_8BITS );
                        buffer[context.pos++] = ( byte ) ( ( context.ibitWorkArea >> 8 ) & MASK_8BITS );
                        buffer[context.pos++] = ( byte ) ( context.ibitWorkArea & MASK_8BITS );
                    }
                }
            }
        }
        if ( context.eof && context.modulus != 0 ) {
            final byte[] buffer = ensureBufferSize ( decodeSize, context );
            switch ( context.modulus ) {
            case 1 :
                break;
            case 2 :
                context.ibitWorkArea = context.ibitWorkArea >> 4;
                buffer[context.pos++] = ( byte ) ( ( context.ibitWorkArea ) & MASK_8BITS );
                break;
            case 3 :
                context.ibitWorkArea = context.ibitWorkArea >> 2;
                buffer[context.pos++] = ( byte ) ( ( context.ibitWorkArea >> 8 ) & MASK_8BITS );
                buffer[context.pos++] = ( byte ) ( ( context.ibitWorkArea ) & MASK_8BITS );
                break;
            default:
                throw new IllegalStateException ( "Impossible modulus " + context.modulus );
            }
        }
    }
    public static boolean isBase64 ( final byte octet ) {
        return octet == PAD_DEFAULT || ( octet >= 0 && octet < DECODE_TABLE.length && DECODE_TABLE[octet] != -1 );
    }
    public static boolean isBase64 ( final String base64 ) {
        return isBase64 ( StringUtils.getBytesUtf8 ( base64 ) );
    }
    public static boolean isBase64 ( final byte[] arrayOctet ) {
        for ( int i = 0; i < arrayOctet.length; i++ ) {
            if ( !isBase64 ( arrayOctet[i] ) && !isWhiteSpace ( arrayOctet[i] ) ) {
                return false;
            }
        }
        return true;
    }
    public static byte[] encodeBase64 ( final byte[] binaryData ) {
        return encodeBase64 ( binaryData, false );
    }
    public static String encodeBase64String ( final byte[] binaryData ) {
        return StringUtils.newStringUtf8 ( encodeBase64 ( binaryData, false ) );
    }
    public static byte[] encodeBase64URLSafe ( final byte[] binaryData ) {
        return encodeBase64 ( binaryData, false, true );
    }
    public static String encodeBase64URLSafeString ( final byte[] binaryData ) {
        return StringUtils.newStringUtf8 ( encodeBase64 ( binaryData, false, true ) );
    }
    public static byte[] encodeBase64Chunked ( final byte[] binaryData ) {
        return encodeBase64 ( binaryData, true );
    }
    public static byte[] encodeBase64 ( final byte[] binaryData, final boolean isChunked ) {
        return encodeBase64 ( binaryData, isChunked, false );
    }
    public static byte[] encodeBase64 ( final byte[] binaryData, final boolean isChunked, final boolean urlSafe ) {
        return encodeBase64 ( binaryData, isChunked, urlSafe, Integer.MAX_VALUE );
    }
    public static byte[] encodeBase64 ( final byte[] binaryData, final boolean isChunked,
                                        final boolean urlSafe, final int maxResultSize ) {
        if ( binaryData == null || binaryData.length == 0 ) {
            return binaryData;
        }
        final Base64 b64 = isChunked ? new Base64 ( urlSafe ) : new Base64 ( 0, CHUNK_SEPARATOR, urlSafe );
        final long len = b64.getEncodedLength ( binaryData );
        if ( len > maxResultSize ) {
            throw new IllegalArgumentException ( "Input array too big, the output array would be bigger (" +
                                                 len +
                                                 ") than the specified maximum size of " +
                                                 maxResultSize );
        }
        return b64.encode ( binaryData );
    }
    public static byte[] decodeBase64 ( final String base64String ) {
        return new Base64().decode ( base64String );
    }
    public static byte[] decodeBase64 ( final byte[] base64Data ) {
        return decodeBase64 ( base64Data, 0, base64Data.length );
    }
    public  static byte[] decodeBase64 (
        final byte[] base64Data, final int off, final int len ) {
        return new Base64().decode ( base64Data, off, len );
    }
    public static BigInteger decodeInteger ( final byte[] pArray ) {
        return new BigInteger ( 1, decodeBase64 ( pArray ) );
    }
    public static byte[] encodeInteger ( final BigInteger bigInt ) {
        if ( bigInt == null ) {
            throw new NullPointerException ( "encodeInteger called with null parameter" );
        }
        return encodeBase64 ( toIntegerBytes ( bigInt ), false );
    }
    static byte[] toIntegerBytes ( final BigInteger bigInt ) {
        int bitlen = bigInt.bitLength();
        bitlen = ( ( bitlen + 7 ) >> 3 ) << 3;
        final byte[] bigBytes = bigInt.toByteArray();
        if ( ( ( bigInt.bitLength() % 8 ) != 0 ) && ( ( ( bigInt.bitLength() / 8 ) + 1 ) == ( bitlen / 8 ) ) ) {
            return bigBytes;
        }
        int startSrc = 0;
        int len = bigBytes.length;
        if ( ( bigInt.bitLength() % 8 ) == 0 ) {
            startSrc = 1;
            len--;
        }
        final int startDst = bitlen / 8 - len;
        final byte[] resizedBytes = new byte[bitlen / 8];
        System.arraycopy ( bigBytes, startSrc, resizedBytes, startDst, len );
        return resizedBytes;
    }
    @Override
    protected boolean isInAlphabet ( final byte octet ) {
        return octet >= 0 && octet < decodeTable.length && decodeTable[octet] != -1;
    }
}
