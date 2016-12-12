package org.apache.tomcat.util.buf;
import java.io.IOException;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.Charset;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetEncoder;
public final class C2BConverter {
    private final CharsetEncoder encoder;
    private ByteBuffer bb;
    private CharBuffer cb;
    private final CharBuffer leftovers;
    public C2BConverter ( final Charset charset ) {
        this.bb = null;
        this.cb = null;
        this.encoder = charset.newEncoder();
        this.encoder.onUnmappableCharacter ( CodingErrorAction.REPLACE ).onMalformedInput ( CodingErrorAction.REPLACE );
        final char[] left = new char[4];
        this.leftovers = CharBuffer.wrap ( left );
    }
    public void recycle() {
        this.encoder.reset();
        this.leftovers.position ( 0 );
    }
    public boolean isUndeflow() {
        return this.leftovers.position() > 0;
    }
    public void convert ( final CharChunk cc, final ByteChunk bc ) throws IOException {
        if ( this.bb == null || this.bb.array() != bc.getBuffer() ) {
            this.bb = ByteBuffer.wrap ( bc.getBuffer(), bc.getEnd(), bc.getBuffer().length - bc.getEnd() );
        } else {
            this.bb.limit ( bc.getBuffer().length );
            this.bb.position ( bc.getEnd() );
        }
        if ( this.cb == null || this.cb.array() != cc.getBuffer() ) {
            this.cb = CharBuffer.wrap ( cc.getBuffer(), cc.getStart(), cc.getLength() );
        } else {
            this.cb.limit ( cc.getEnd() );
            this.cb.position ( cc.getStart() );
        }
        CoderResult result = null;
        if ( this.leftovers.position() > 0 ) {
            final int pos = this.bb.position();
            do {
                this.leftovers.put ( ( char ) cc.substract() );
                this.leftovers.flip();
                result = this.encoder.encode ( this.leftovers, this.bb, false );
                this.leftovers.position ( this.leftovers.limit() );
                this.leftovers.limit ( this.leftovers.array().length );
            } while ( result.isUnderflow() && this.bb.position() == pos );
            if ( result.isError() || result.isMalformed() ) {
                result.throwException();
            }
            this.cb.position ( cc.getStart() );
            this.leftovers.position ( 0 );
        }
        result = this.encoder.encode ( this.cb, this.bb, false );
        if ( result.isError() || result.isMalformed() ) {
            result.throwException();
        } else if ( result.isOverflow() ) {
            bc.setEnd ( this.bb.position() );
            cc.setOffset ( this.cb.position() );
        } else if ( result.isUnderflow() ) {
            bc.setEnd ( this.bb.position() );
            cc.setOffset ( this.cb.position() );
            if ( cc.getLength() > 0 ) {
                this.leftovers.limit ( this.leftovers.array().length );
                this.leftovers.position ( cc.getLength() );
                cc.substract ( this.leftovers.array(), 0, cc.getLength() );
            }
        }
    }
    public void convert ( final CharBuffer cc, final ByteBuffer bc ) throws IOException {
        if ( this.bb == null || this.bb.array() != bc.array() ) {
            this.bb = ByteBuffer.wrap ( bc.array(), bc.limit(), bc.capacity() - bc.limit() );
        } else {
            this.bb.limit ( bc.capacity() );
            this.bb.position ( bc.limit() );
        }
        if ( this.cb == null || this.cb.array() != cc.array() ) {
            this.cb = CharBuffer.wrap ( cc.array(), cc.arrayOffset() + cc.position(), cc.remaining() );
        } else {
            this.cb.limit ( cc.limit() );
            this.cb.position ( cc.position() );
        }
        CoderResult result = null;
        if ( this.leftovers.position() > 0 ) {
            final int pos = this.bb.position();
            do {
                this.leftovers.put ( cc.get() );
                this.leftovers.flip();
                result = this.encoder.encode ( this.leftovers, this.bb, false );
                this.leftovers.position ( this.leftovers.limit() );
                this.leftovers.limit ( this.leftovers.array().length );
            } while ( result.isUnderflow() && this.bb.position() == pos );
            if ( result.isError() || result.isMalformed() ) {
                result.throwException();
            }
            this.cb.position ( cc.position() );
            this.leftovers.position ( 0 );
        }
        result = this.encoder.encode ( this.cb, this.bb, false );
        if ( result.isError() || result.isMalformed() ) {
            result.throwException();
        } else if ( result.isOverflow() ) {
            bc.limit ( this.bb.position() );
            cc.position ( this.cb.position() );
        } else if ( result.isUnderflow() ) {
            bc.limit ( this.bb.position() );
            cc.position ( this.cb.position() );
            if ( cc.remaining() > 0 ) {
                this.leftovers.limit ( this.leftovers.array().length );
                this.leftovers.position ( cc.remaining() );
                cc.get ( this.leftovers.array(), 0, cc.remaining() );
            }
        }
    }
    public Charset getCharset() {
        return this.encoder.charset();
    }
}
