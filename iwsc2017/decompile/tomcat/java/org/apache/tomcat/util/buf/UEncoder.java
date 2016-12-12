package org.apache.tomcat.util.buf;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
public final class UEncoder {
    private BitSet safeChars;
    private C2BConverter c2b;
    private ByteChunk bb;
    private CharChunk cb;
    private CharChunk output;
    public UEncoder ( final SafeCharsSet safeCharsSet ) {
        this.safeChars = null;
        this.c2b = null;
        this.bb = null;
        this.cb = null;
        this.output = null;
        this.safeChars = safeCharsSet.getSafeChars();
    }
    public CharChunk encodeURL ( final String s, final int start, final int end ) throws IOException {
        if ( this.c2b == null ) {
            this.bb = new ByteChunk ( 8 );
            this.cb = new CharChunk ( 2 );
            this.output = new CharChunk ( 64 );
            this.c2b = new C2BConverter ( StandardCharsets.UTF_8 );
        } else {
            this.bb.recycle();
            this.cb.recycle();
            this.output.recycle();
        }
        for ( int i = start; i < end; ++i ) {
            final char c = s.charAt ( i );
            if ( this.safeChars.get ( c ) ) {
                this.output.append ( c );
            } else {
                this.cb.append ( c );
                this.c2b.convert ( this.cb, this.bb );
                if ( c >= '\ud800' && c <= '\udbff' && i + 1 < end ) {
                    final char d = s.charAt ( i + 1 );
                    if ( d >= '\udc00' && d <= '\udfff' ) {
                        this.cb.append ( d );
                        this.c2b.convert ( this.cb, this.bb );
                        ++i;
                    }
                }
                this.urlEncode ( this.output, this.bb );
                this.cb.recycle();
                this.bb.recycle();
            }
        }
        return this.output;
    }
    protected void urlEncode ( final CharChunk out, final ByteChunk bb ) throws IOException {
        final byte[] bytes = bb.getBuffer();
        for ( int j = bb.getStart(); j < bb.getEnd(); ++j ) {
            out.append ( '%' );
            char ch = Character.forDigit ( bytes[j] >> 4 & 0xF, 16 );
            out.append ( ch );
            ch = Character.forDigit ( bytes[j] & 0xF, 16 );
            out.append ( ch );
        }
    }
    private static BitSet initialSafeChars() {
        final BitSet initialSafeChars = new BitSet ( 128 );
        for ( int i = 97; i <= 122; ++i ) {
            initialSafeChars.set ( i );
        }
        for ( int i = 65; i <= 90; ++i ) {
            initialSafeChars.set ( i );
        }
        for ( int i = 48; i <= 57; ++i ) {
            initialSafeChars.set ( i );
        }
        initialSafeChars.set ( 36 );
        initialSafeChars.set ( 45 );
        initialSafeChars.set ( 95 );
        initialSafeChars.set ( 46 );
        initialSafeChars.set ( 33 );
        initialSafeChars.set ( 42 );
        initialSafeChars.set ( 39 );
        initialSafeChars.set ( 40 );
        initialSafeChars.set ( 41 );
        initialSafeChars.set ( 44 );
        return initialSafeChars;
    }
    public enum SafeCharsSet {
        WITH_SLASH ( "/" ),
        DEFAULT ( "" );
        private final BitSet safeChars;
        private BitSet getSafeChars() {
            return this.safeChars;
        }
        private SafeCharsSet ( final String additionalSafeChars ) {
            this.safeChars = initialSafeChars();
            for ( final char c : additionalSafeChars.toCharArray() ) {
                this.safeChars.set ( c );
            }
        }
    }
}
