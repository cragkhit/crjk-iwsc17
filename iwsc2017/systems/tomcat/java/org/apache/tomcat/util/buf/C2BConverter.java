package org.apache.tomcat.util.buf;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
public final class C2BConverter {
    private final CharsetEncoder encoder;
    private ByteBuffer bb = null;
    private CharBuffer cb = null;
    private final CharBuffer leftovers;
    public C2BConverter ( Charset charset ) {
        encoder = charset.newEncoder();
        encoder.onUnmappableCharacter ( CodingErrorAction.REPLACE )
        .onMalformedInput ( CodingErrorAction.REPLACE );
        char[] left = new char[4];
        leftovers = CharBuffer.wrap ( left );
    }
    public void recycle() {
        encoder.reset();
        leftovers.position ( 0 );
    }
    public boolean isUndeflow() {
        return ( leftovers.position() > 0 );
    }
    public void convert ( CharChunk cc, ByteChunk bc ) throws IOException {
        if ( ( bb == null ) || ( bb.array() != bc.getBuffer() ) ) {
            bb = ByteBuffer.wrap ( bc.getBuffer(), bc.getEnd(), bc.getBuffer().length - bc.getEnd() );
        } else {
            bb.limit ( bc.getBuffer().length );
            bb.position ( bc.getEnd() );
        }
        if ( ( cb == null ) || ( cb.array() != cc.getBuffer() ) ) {
            cb = CharBuffer.wrap ( cc.getBuffer(), cc.getStart(), cc.getLength() );
        } else {
            cb.limit ( cc.getEnd() );
            cb.position ( cc.getStart() );
        }
        CoderResult result = null;
        if ( leftovers.position() > 0 ) {
            int pos = bb.position();
            do {
                leftovers.put ( ( char ) cc.substract() );
                leftovers.flip();
                result = encoder.encode ( leftovers, bb, false );
                leftovers.position ( leftovers.limit() );
                leftovers.limit ( leftovers.array().length );
            } while ( result.isUnderflow() && ( bb.position() == pos ) );
            if ( result.isError() || result.isMalformed() ) {
                result.throwException();
            }
            cb.position ( cc.getStart() );
            leftovers.position ( 0 );
        }
        result = encoder.encode ( cb, bb, false );
        if ( result.isError() || result.isMalformed() ) {
            result.throwException();
        } else if ( result.isOverflow() ) {
            bc.setEnd ( bb.position() );
            cc.setOffset ( cb.position() );
        } else if ( result.isUnderflow() ) {
            bc.setEnd ( bb.position() );
            cc.setOffset ( cb.position() );
            if ( cc.getLength() > 0 ) {
                leftovers.limit ( leftovers.array().length );
                leftovers.position ( cc.getLength() );
                cc.substract ( leftovers.array(), 0, cc.getLength() );
            }
        }
    }
    public void convert ( CharBuffer cc, ByteBuffer bc ) throws IOException {
        if ( ( bb == null ) || ( bb.array() != bc.array() ) ) {
            bb = ByteBuffer.wrap ( bc.array(), bc.limit(), bc.capacity() - bc.limit() );
        } else {
            bb.limit ( bc.capacity() );
            bb.position ( bc.limit() );
        }
        if ( ( cb == null ) || ( cb.array() != cc.array() ) ) {
            cb = CharBuffer.wrap ( cc.array(), cc.arrayOffset() + cc.position(), cc.remaining() );
        } else {
            cb.limit ( cc.limit() );
            cb.position ( cc.position() );
        }
        CoderResult result = null;
        if ( leftovers.position() > 0 ) {
            int pos = bb.position();
            do {
                leftovers.put ( cc.get() );
                leftovers.flip();
                result = encoder.encode ( leftovers, bb, false );
                leftovers.position ( leftovers.limit() );
                leftovers.limit ( leftovers.array().length );
            } while ( result.isUnderflow() && ( bb.position() == pos ) );
            if ( result.isError() || result.isMalformed() ) {
                result.throwException();
            }
            cb.position ( cc.position() );
            leftovers.position ( 0 );
        }
        result = encoder.encode ( cb, bb, false );
        if ( result.isError() || result.isMalformed() ) {
            result.throwException();
        } else if ( result.isOverflow() ) {
            bc.limit ( bb.position() );
            cc.position ( cb.position() );
        } else if ( result.isUnderflow() ) {
            bc.limit ( bb.position() );
            cc.position ( cb.position() );
            if ( cc.remaining() > 0 ) {
                leftovers.limit ( leftovers.array().length );
                leftovers.position ( cc.remaining() );
                cc.get ( leftovers.array(), 0, cc.remaining() );
            }
        }
    }
    public Charset getCharset() {
        return encoder.charset();
    }
}
