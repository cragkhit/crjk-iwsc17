package org.apache.tomcat.util.buf;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.tomcat.util.res.StringManager;
public class B2CConverter {
    private static final StringManager sm = StringManager.getManager ( B2CConverter.class );
    private static final Map<String, Charset> encodingToCharsetCache =
        new HashMap<>();
    protected static final int LEFTOVER_SIZE = 9;
    static {
        for ( Charset charset : Charset.availableCharsets().values() ) {
            encodingToCharsetCache.put (
                charset.name().toLowerCase ( Locale.ENGLISH ), charset );
            for ( String alias : charset.aliases() ) {
                encodingToCharsetCache.put (
                    alias.toLowerCase ( Locale.ENGLISH ), charset );
            }
        }
    }
    public static Charset getCharset ( String enc )
    throws UnsupportedEncodingException {
        String lowerCaseEnc = enc.toLowerCase ( Locale.ENGLISH );
        return getCharsetLower ( lowerCaseEnc );
    }
    public static Charset getCharsetLower ( String lowerCaseEnc )
    throws UnsupportedEncodingException {
        Charset charset = encodingToCharsetCache.get ( lowerCaseEnc );
        if ( charset == null ) {
            throw new UnsupportedEncodingException (
                sm.getString ( "b2cConverter.unknownEncoding", lowerCaseEnc ) );
        }
        return charset;
    }
    private final CharsetDecoder decoder;
    private ByteBuffer bb = null;
    private CharBuffer cb = null;
    private final ByteBuffer leftovers;
    public B2CConverter ( Charset charset ) {
        this ( charset, false );
    }
    public B2CConverter ( Charset charset, boolean replaceOnError ) {
        byte[] left = new byte[LEFTOVER_SIZE];
        leftovers = ByteBuffer.wrap ( left );
        CodingErrorAction action;
        if ( replaceOnError ) {
            action = CodingErrorAction.REPLACE;
        } else {
            action = CodingErrorAction.REPORT;
        }
        if ( charset.equals ( StandardCharsets.UTF_8 ) ) {
            decoder = new Utf8Decoder();
        } else {
            decoder = charset.newDecoder();
        }
        decoder.onMalformedInput ( action );
        decoder.onUnmappableCharacter ( action );
    }
    public void recycle() {
        decoder.reset();
        leftovers.position ( 0 );
    }
    public void convert ( ByteChunk bc, CharChunk cc, boolean endOfInput )
    throws IOException {
        if ( ( bb == null ) || ( bb.array() != bc.getBuffer() ) ) {
            bb = ByteBuffer.wrap ( bc.getBuffer(), bc.getStart(), bc.getLength() );
        } else {
            bb.limit ( bc.getEnd() );
            bb.position ( bc.getStart() );
        }
        if ( ( cb == null ) || ( cb.array() != cc.getBuffer() ) ) {
            cb = CharBuffer.wrap ( cc.getBuffer(), cc.getEnd(),
                                   cc.getBuffer().length - cc.getEnd() );
        } else {
            cb.limit ( cc.getBuffer().length );
            cb.position ( cc.getEnd() );
        }
        CoderResult result = null;
        if ( leftovers.position() > 0 ) {
            int pos = cb.position();
            do {
                leftovers.put ( bc.substractB() );
                leftovers.flip();
                result = decoder.decode ( leftovers, cb, endOfInput );
                leftovers.position ( leftovers.limit() );
                leftovers.limit ( leftovers.array().length );
            } while ( result.isUnderflow() && ( cb.position() == pos ) );
            if ( result.isError() || result.isMalformed() ) {
                result.throwException();
            }
            bb.position ( bc.getStart() );
            leftovers.position ( 0 );
        }
        result = decoder.decode ( bb, cb, endOfInput );
        if ( result.isError() || result.isMalformed() ) {
            result.throwException();
        } else if ( result.isOverflow() ) {
            bc.setOffset ( bb.position() );
            cc.setEnd ( cb.position() );
        } else if ( result.isUnderflow() ) {
            bc.setOffset ( bb.position() );
            cc.setEnd ( cb.position() );
            if ( bc.getLength() > 0 ) {
                leftovers.limit ( leftovers.array().length );
                leftovers.position ( bc.getLength() );
                bc.substract ( leftovers.array(), 0, bc.getLength() );
            }
        }
    }
    public void convert ( ByteBuffer bc, CharBuffer cc, ByteChunk.ByteInputChannel ic, boolean endOfInput )
    throws IOException {
        if ( ( bb == null ) || ( bb.array() != bc.array() ) ) {
            bb = ByteBuffer.wrap ( bc.array(), bc.arrayOffset() + bc.position(), bc.remaining() );
        } else {
            bb.limit ( bc.limit() );
            bb.position ( bc.position() );
        }
        if ( ( cb == null ) || ( cb.array() != cc.array() ) ) {
            cb = CharBuffer.wrap ( cc.array(), cc.limit(), cc.capacity() - cc.limit() );
        } else {
            cb.limit ( cc.capacity() );
            cb.position ( cc.limit() );
        }
        CoderResult result = null;
        if ( leftovers.position() > 0 ) {
            int pos = cb.position();
            do {
                byte chr;
                if ( bc.remaining() == 0 ) {
                    int n = ic.realReadBytes();
                    chr = n < 0 ? -1 : bc.get();
                } else {
                    chr = bc.get();
                }
                leftovers.put ( chr );
                leftovers.flip();
                result = decoder.decode ( leftovers, cb, endOfInput );
                leftovers.position ( leftovers.limit() );
                leftovers.limit ( leftovers.array().length );
            } while ( result.isUnderflow() && ( cb.position() == pos ) );
            if ( result.isError() || result.isMalformed() ) {
                result.throwException();
            }
            bb.position ( bc.position() );
            leftovers.position ( 0 );
        }
        result = decoder.decode ( bb, cb, endOfInput );
        if ( result.isError() || result.isMalformed() ) {
            result.throwException();
        } else if ( result.isOverflow() ) {
            bc.position ( bb.position() );
            cc.limit ( cb.position() );
        } else if ( result.isUnderflow() ) {
            bc.position ( bb.position() );
            cc.limit ( cb.position() );
            if ( bc.remaining() > 0 ) {
                leftovers.limit ( leftovers.array().length );
                leftovers.position ( bc.remaining() );
                bc.get ( leftovers.array(), 0, bc.remaining() );
            }
        }
    }
    public Charset getCharset() {
        return decoder.charset();
    }
}
