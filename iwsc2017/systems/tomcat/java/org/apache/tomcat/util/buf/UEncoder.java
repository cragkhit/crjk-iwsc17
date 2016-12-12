package org.apache.tomcat.util.buf;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.BitSet;
public final class UEncoder {
    public enum SafeCharsSet {
        WITH_SLASH ( "/" ), DEFAULT ( "" );
        private final BitSet safeChars;
        private BitSet getSafeChars() {
            return this.safeChars;
        }
        private SafeCharsSet ( String additionalSafeChars ) {
            safeChars = initialSafeChars();
            for ( char c : additionalSafeChars.toCharArray() ) {
                safeChars.set ( c );
            }
        }
    }
    private BitSet safeChars = null;
    private C2BConverter c2b = null;
    private ByteChunk bb = null;
    private CharChunk cb = null;
    private CharChunk output = null;
    public UEncoder ( SafeCharsSet safeCharsSet ) {
        this.safeChars = safeCharsSet.getSafeChars();
    }
    public CharChunk encodeURL ( String s, int start, int end )
    throws IOException {
        if ( c2b == null ) {
            bb = new ByteChunk ( 8 );
            cb = new CharChunk ( 2 );
            output = new CharChunk ( 64 );
            c2b = new C2BConverter ( StandardCharsets.UTF_8 );
        } else {
            bb.recycle();
            cb.recycle();
            output.recycle();
        }
        for ( int i = start; i < end; i++ ) {
            char c = s.charAt ( i );
            if ( safeChars.get ( c ) ) {
                output.append ( c );
            } else {
                cb.append ( c );
                c2b.convert ( cb, bb );
                if ( c >= 0xD800 && c <= 0xDBFF ) {
                    if ( ( i + 1 ) < end ) {
                        char d = s.charAt ( i + 1 );
                        if ( d >= 0xDC00 && d <= 0xDFFF ) {
                            cb.append ( d );
                            c2b.convert ( cb, bb );
                            i++;
                        }
                    }
                }
                urlEncode ( output, bb );
                cb.recycle();
                bb.recycle();
            }
        }
        return output;
    }
    protected void urlEncode ( CharChunk out, ByteChunk bb )
    throws IOException {
        byte[] bytes = bb.getBuffer();
        for ( int j = bb.getStart(); j < bb.getEnd(); j++ ) {
            out.append ( '%' );
            char ch = Character.forDigit ( ( bytes[j] >> 4 ) & 0xF, 16 );
            out.append ( ch );
            ch = Character.forDigit ( bytes[j] & 0xF, 16 );
            out.append ( ch );
        }
    }
    private static BitSet initialSafeChars() {
        BitSet initialSafeChars = new BitSet ( 128 );
        int i;
        for ( i = 'a'; i <= 'z'; i++ ) {
            initialSafeChars.set ( i );
        }
        for ( i = 'A'; i <= 'Z'; i++ ) {
            initialSafeChars.set ( i );
        }
        for ( i = '0'; i <= '9'; i++ ) {
            initialSafeChars.set ( i );
        }
        initialSafeChars.set ( '$' );
        initialSafeChars.set ( '-' );
        initialSafeChars.set ( '_' );
        initialSafeChars.set ( '.' );
        initialSafeChars.set ( '!' );
        initialSafeChars.set ( '*' );
        initialSafeChars.set ( '\'' );
        initialSafeChars.set ( '(' );
        initialSafeChars.set ( ')' );
        initialSafeChars.set ( ',' );
        return initialSafeChars;
    }
}
