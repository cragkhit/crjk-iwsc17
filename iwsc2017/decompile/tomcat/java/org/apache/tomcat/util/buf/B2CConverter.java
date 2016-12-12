package org.apache.tomcat.util.buf;
import java.util.Iterator;
import java.util.HashMap;
import java.io.IOException;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.nio.charset.CodingErrorAction;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.Charset;
import java.util.Map;
import org.apache.tomcat.util.res.StringManager;
public class B2CConverter {
    private static final StringManager sm;
    private static final Map<String, Charset> encodingToCharsetCache;
    protected static final int LEFTOVER_SIZE = 9;
    private final CharsetDecoder decoder;
    private ByteBuffer bb;
    private CharBuffer cb;
    private final ByteBuffer leftovers;
    public static Charset getCharset ( final String enc ) throws UnsupportedEncodingException {
        final String lowerCaseEnc = enc.toLowerCase ( Locale.ENGLISH );
        return getCharsetLower ( lowerCaseEnc );
    }
    public static Charset getCharsetLower ( final String lowerCaseEnc ) throws UnsupportedEncodingException {
        final Charset charset = B2CConverter.encodingToCharsetCache.get ( lowerCaseEnc );
        if ( charset == null ) {
            throw new UnsupportedEncodingException ( B2CConverter.sm.getString ( "b2cConverter.unknownEncoding", lowerCaseEnc ) );
        }
        return charset;
    }
    public B2CConverter ( final Charset charset ) {
        this ( charset, false );
    }
    public B2CConverter ( final Charset charset, final boolean replaceOnError ) {
        this.bb = null;
        this.cb = null;
        final byte[] left = new byte[9];
        this.leftovers = ByteBuffer.wrap ( left );
        CodingErrorAction action;
        if ( replaceOnError ) {
            action = CodingErrorAction.REPLACE;
        } else {
            action = CodingErrorAction.REPORT;
        }
        if ( charset.equals ( StandardCharsets.UTF_8 ) ) {
            this.decoder = new Utf8Decoder();
        } else {
            this.decoder = charset.newDecoder();
        }
        this.decoder.onMalformedInput ( action );
        this.decoder.onUnmappableCharacter ( action );
    }
    public void recycle() {
        this.decoder.reset();
        this.leftovers.position ( 0 );
    }
    public void convert ( final ByteChunk bc, final CharChunk cc, final boolean endOfInput ) throws IOException {
        if ( this.bb == null || this.bb.array() != bc.getBuffer() ) {
            this.bb = ByteBuffer.wrap ( bc.getBuffer(), bc.getStart(), bc.getLength() );
        } else {
            this.bb.limit ( bc.getEnd() );
            this.bb.position ( bc.getStart() );
        }
        if ( this.cb == null || this.cb.array() != cc.getBuffer() ) {
            this.cb = CharBuffer.wrap ( cc.getBuffer(), cc.getEnd(), cc.getBuffer().length - cc.getEnd() );
        } else {
            this.cb.limit ( cc.getBuffer().length );
            this.cb.position ( cc.getEnd() );
        }
        CoderResult result = null;
        if ( this.leftovers.position() > 0 ) {
            final int pos = this.cb.position();
            do {
                this.leftovers.put ( bc.substractB() );
                this.leftovers.flip();
                result = this.decoder.decode ( this.leftovers, this.cb, endOfInput );
                this.leftovers.position ( this.leftovers.limit() );
                this.leftovers.limit ( this.leftovers.array().length );
            } while ( result.isUnderflow() && this.cb.position() == pos );
            if ( result.isError() || result.isMalformed() ) {
                result.throwException();
            }
            this.bb.position ( bc.getStart() );
            this.leftovers.position ( 0 );
        }
        result = this.decoder.decode ( this.bb, this.cb, endOfInput );
        if ( result.isError() || result.isMalformed() ) {
            result.throwException();
        } else if ( result.isOverflow() ) {
            bc.setOffset ( this.bb.position() );
            cc.setEnd ( this.cb.position() );
        } else if ( result.isUnderflow() ) {
            bc.setOffset ( this.bb.position() );
            cc.setEnd ( this.cb.position() );
            if ( bc.getLength() > 0 ) {
                this.leftovers.limit ( this.leftovers.array().length );
                this.leftovers.position ( bc.getLength() );
                bc.substract ( this.leftovers.array(), 0, bc.getLength() );
            }
        }
    }
    public void convert ( final ByteBuffer bc, final CharBuffer cc, final ByteChunk.ByteInputChannel ic, final boolean endOfInput ) throws IOException {
        if ( this.bb == null || this.bb.array() != bc.array() ) {
            this.bb = ByteBuffer.wrap ( bc.array(), bc.arrayOffset() + bc.position(), bc.remaining() );
        } else {
            this.bb.limit ( bc.limit() );
            this.bb.position ( bc.position() );
        }
        if ( this.cb == null || this.cb.array() != cc.array() ) {
            this.cb = CharBuffer.wrap ( cc.array(), cc.limit(), cc.capacity() - cc.limit() );
        } else {
            this.cb.limit ( cc.capacity() );
            this.cb.position ( cc.limit() );
        }
        CoderResult result = null;
        if ( this.leftovers.position() > 0 ) {
            final int pos = this.cb.position();
            do {
                byte chr;
                if ( bc.remaining() == 0 ) {
                    final int n = ic.realReadBytes();
                    chr = ( byte ) ( ( n < 0 ) ? -1 : bc.get() );
                } else {
                    chr = bc.get();
                }
                this.leftovers.put ( chr );
                this.leftovers.flip();
                result = this.decoder.decode ( this.leftovers, this.cb, endOfInput );
                this.leftovers.position ( this.leftovers.limit() );
                this.leftovers.limit ( this.leftovers.array().length );
            } while ( result.isUnderflow() && this.cb.position() == pos );
            if ( result.isError() || result.isMalformed() ) {
                result.throwException();
            }
            this.bb.position ( bc.position() );
            this.leftovers.position ( 0 );
        }
        result = this.decoder.decode ( this.bb, this.cb, endOfInput );
        if ( result.isError() || result.isMalformed() ) {
            result.throwException();
        } else if ( result.isOverflow() ) {
            bc.position ( this.bb.position() );
            cc.limit ( this.cb.position() );
        } else if ( result.isUnderflow() ) {
            bc.position ( this.bb.position() );
            cc.limit ( this.cb.position() );
            if ( bc.remaining() > 0 ) {
                this.leftovers.limit ( this.leftovers.array().length );
                this.leftovers.position ( bc.remaining() );
                bc.get ( this.leftovers.array(), 0, bc.remaining() );
            }
        }
    }
    public Charset getCharset() {
        return this.decoder.charset();
    }
    static {
        sm = StringManager.getManager ( B2CConverter.class );
        encodingToCharsetCache = new HashMap<String, Charset>();
        for ( final Charset charset : Charset.availableCharsets().values() ) {
            B2CConverter.encodingToCharsetCache.put ( charset.name().toLowerCase ( Locale.ENGLISH ), charset );
            for ( final String alias : charset.aliases() ) {
                B2CConverter.encodingToCharsetCache.put ( alias.toLowerCase ( Locale.ENGLISH ), charset );
            }
        }
    }
}
