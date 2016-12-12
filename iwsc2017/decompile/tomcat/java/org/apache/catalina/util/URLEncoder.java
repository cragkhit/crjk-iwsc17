package org.apache.catalina.util;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.ByteArrayOutputStream;
import java.util.BitSet;
public class URLEncoder {
    private static final char[] hexadecimal;
    public static final URLEncoder DEFAULT;
    protected final BitSet safeCharacters;
    public URLEncoder() {
        this.safeCharacters = new BitSet ( 256 );
        for ( char i = 'a'; i <= 'z'; ++i ) {
            this.addSafeCharacter ( i );
        }
        for ( char i = 'A'; i <= 'Z'; ++i ) {
            this.addSafeCharacter ( i );
        }
        for ( char i = '0'; i <= '9'; ++i ) {
            this.addSafeCharacter ( i );
        }
    }
    public void addSafeCharacter ( final char c ) {
        this.safeCharacters.set ( c );
    }
    public String encode ( final String path, final String encoding ) {
        final int maxBytesPerChar = 10;
        final StringBuilder rewrittenPath = new StringBuilder ( path.length() );
        final ByteArrayOutputStream buf = new ByteArrayOutputStream ( maxBytesPerChar );
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter ( buf, encoding );
        } catch ( Exception e ) {
            e.printStackTrace();
            writer = new OutputStreamWriter ( buf );
        }
        for ( int i = 0; i < path.length(); ++i ) {
            final int c = path.charAt ( i );
            if ( this.safeCharacters.get ( c ) ) {
                rewrittenPath.append ( ( char ) c );
            } else {
                try {
                    writer.write ( ( char ) c );
                    writer.flush();
                } catch ( IOException e2 ) {
                    buf.reset();
                    continue;
                }
                final byte[] ba = buf.toByteArray();
                for ( int j = 0; j < ba.length; ++j ) {
                    final byte toEncode = ba[j];
                    rewrittenPath.append ( '%' );
                    final int low = toEncode & 0xF;
                    final int high = ( toEncode & 0xF0 ) >> 4;
                    rewrittenPath.append ( URLEncoder.hexadecimal[high] );
                    rewrittenPath.append ( URLEncoder.hexadecimal[low] );
                }
                buf.reset();
            }
        }
        return rewrittenPath.toString();
    }
    static {
        hexadecimal = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        ( DEFAULT = new URLEncoder() ).addSafeCharacter ( '~' );
        URLEncoder.DEFAULT.addSafeCharacter ( '-' );
        URLEncoder.DEFAULT.addSafeCharacter ( '_' );
        URLEncoder.DEFAULT.addSafeCharacter ( '.' );
        URLEncoder.DEFAULT.addSafeCharacter ( '*' );
        URLEncoder.DEFAULT.addSafeCharacter ( '/' );
    }
}
