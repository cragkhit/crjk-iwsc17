package org.apache.tomcat.util.codec.binary;
import org.apache.tomcat.util.buf.HexUtils;
import org.apache.tomcat.util.codec.BinaryDecoder;
import org.apache.tomcat.util.codec.BinaryEncoder;
import org.apache.tomcat.util.codec.DecoderException;
import org.apache.tomcat.util.codec.EncoderException;
public abstract class BaseNCodec implements BinaryEncoder, BinaryDecoder {
    static class Context {
        int ibitWorkArea;
        long lbitWorkArea;
        byte[] buffer;
        int pos;
        int readPos;
        boolean eof;
        int currentLinePos;
        int modulus;
        Context() {
        }
        @SuppressWarnings ( "boxing" )
        @Override
        public String toString() {
            return String.format ( "%s[buffer=%s, currentLinePos=%s, eof=%s, " +
                                   "ibitWorkArea=%s, lbitWorkArea=%s, modulus=%s, pos=%s, " +
                                   "readPos=%s]", this.getClass().getSimpleName(),
                                   HexUtils.toHexString ( buffer ), currentLinePos, eof,
                                   ibitWorkArea, lbitWorkArea, modulus, pos, readPos );
        }
    }
    static final int EOF = -1;
    public static final int MIME_CHUNK_SIZE = 76;
    public static final int PEM_CHUNK_SIZE = 64;
    private static final int DEFAULT_BUFFER_RESIZE_FACTOR = 2;
    private static final int DEFAULT_BUFFER_SIZE = 128;
    protected static final int MASK_8BITS = 0xff;
    protected static final byte PAD_DEFAULT = '=';
    protected final byte pad;
    private final int unencodedBlockSize;
    private final int encodedBlockSize;
    protected final int lineLength;
    private final int chunkSeparatorLength;
    protected BaseNCodec ( final int unencodedBlockSize, final int encodedBlockSize,
                           final int lineLength, final int chunkSeparatorLength ) {
        this ( unencodedBlockSize, encodedBlockSize, lineLength, chunkSeparatorLength, PAD_DEFAULT );
    }
    protected BaseNCodec ( final int unencodedBlockSize, final int encodedBlockSize,
                           final int lineLength, final int chunkSeparatorLength, final byte pad ) {
        this.unencodedBlockSize = unencodedBlockSize;
        this.encodedBlockSize = encodedBlockSize;
        final boolean useChunking = lineLength > 0 && chunkSeparatorLength > 0;
        this.lineLength = useChunking ? ( lineLength / encodedBlockSize ) * encodedBlockSize : 0;
        this.chunkSeparatorLength = chunkSeparatorLength;
        this.pad = pad;
    }
    boolean hasData ( final Context context ) {
        return context.buffer != null;
    }
    int available ( final Context context ) {
        return context.buffer != null ? context.pos - context.readPos : 0;
    }
    protected int getDefaultBufferSize() {
        return DEFAULT_BUFFER_SIZE;
    }
    private byte[] resizeBuffer ( final Context context ) {
        if ( context.buffer == null ) {
            context.buffer = new byte[getDefaultBufferSize()];
            context.pos = 0;
            context.readPos = 0;
        } else {
            final byte[] b = new byte[context.buffer.length * DEFAULT_BUFFER_RESIZE_FACTOR];
            System.arraycopy ( context.buffer, 0, b, 0, context.buffer.length );
            context.buffer = b;
        }
        return context.buffer;
    }
    protected byte[] ensureBufferSize ( final int size, final Context context ) {
        if ( ( context.buffer == null ) || ( context.buffer.length < context.pos + size ) ) {
            return resizeBuffer ( context );
        }
        return context.buffer;
    }
    int readResults ( final byte[] b, final int bPos, final int bAvail, final Context context ) {
        if ( context.buffer != null ) {
            final int len = Math.min ( available ( context ), bAvail );
            System.arraycopy ( context.buffer, context.readPos, b, bPos, len );
            context.readPos += len;
            if ( context.readPos >= context.pos ) {
                context.buffer = null;
            }
            return len;
        }
        return context.eof ? EOF : 0;
    }
    protected static boolean isWhiteSpace ( final byte byteToCheck ) {
        switch ( byteToCheck ) {
        case ' ' :
        case '\n' :
        case '\r' :
        case '\t' :
            return true;
        default :
            return false;
        }
    }
    @Override
    public Object encode ( final Object obj ) throws EncoderException {
        if ( ! ( obj instanceof byte[] ) ) {
            throw new EncoderException ( "Parameter supplied to Base-N encode is not a byte[]" );
        }
        return encode ( ( byte[] ) obj );
    }
    public String encodeToString ( final byte[] pArray ) {
        return StringUtils.newStringUtf8 ( encode ( pArray ) );
    }
    public String encodeAsString ( final byte[] pArray ) {
        return StringUtils.newStringUtf8 ( encode ( pArray ) );
    }
    @Override
    public Object decode ( final Object obj ) throws DecoderException {
        if ( obj instanceof byte[] ) {
            return decode ( ( byte[] ) obj );
        } else if ( obj instanceof String ) {
            return decode ( ( String ) obj );
        } else {
            throw new DecoderException ( "Parameter supplied to Base-N decode is not a byte[] or a String" );
        }
    }
    public byte[] decode ( final String pArray ) {
        return decode ( StringUtils.getBytesUtf8 ( pArray ) );
    }
    @Override
    public byte[] decode ( final byte[] pArray ) {
        return decode ( pArray, 0, pArray.length );
    }
    public byte[] decode ( final byte[] pArray, final int off, final int len ) {
        if ( pArray == null || len == 0 ) {
            return new byte[0];
        }
        final Context context = new Context();
        decode ( pArray, off, len, context );
        decode ( pArray, off, EOF, context );
        final byte[] result = new byte[context.pos];
        readResults ( result, 0, result.length, context );
        return result;
    }
    @Override
    public byte[] encode ( final byte[] pArray ) {
        if ( pArray == null || pArray.length == 0 ) {
            return pArray;
        }
        return encode ( pArray, 0, pArray.length );
    }
    public byte[] encode ( final byte[] pArray, int offset, int length ) {
        if ( pArray == null || pArray.length == 0 ) {
            return pArray;
        }
        final Context context = new Context();
        encode ( pArray, offset, length, context );
        encode ( pArray, offset, EOF, context );
        final byte[] buf = new byte[context.pos - context.readPos];
        readResults ( buf, 0, buf.length, context );
        return buf;
    }
    abstract void encode ( byte[] pArray, int i, int length, Context context );
    abstract void decode ( byte[] pArray, int i, int length, Context context );
    protected abstract boolean isInAlphabet ( byte value );
    public boolean isInAlphabet ( final byte[] arrayOctet, final boolean allowWSPad ) {
        for ( byte octet : arrayOctet ) {
            if ( !isInAlphabet ( octet ) &&
                    ( !allowWSPad || ( octet != pad ) && !isWhiteSpace ( octet ) ) ) {
                return false;
            }
        }
        return true;
    }
    public boolean isInAlphabet ( final String basen ) {
        return isInAlphabet ( StringUtils.getBytesUtf8 ( basen ), true );
    }
    protected boolean containsAlphabetOrPad ( final byte[] arrayOctet ) {
        if ( arrayOctet == null ) {
            return false;
        }
        for ( final byte element : arrayOctet ) {
            if ( pad == element || isInAlphabet ( element ) ) {
                return true;
            }
        }
        return false;
    }
    public long getEncodedLength ( final byte[] pArray ) {
        long len = ( ( pArray.length + unencodedBlockSize - 1 )  / unencodedBlockSize ) * ( long ) encodedBlockSize;
        if ( lineLength > 0 ) {
            len += ( ( len + lineLength - 1 ) / lineLength ) * chunkSeparatorLength;
        }
        return len;
    }
}
