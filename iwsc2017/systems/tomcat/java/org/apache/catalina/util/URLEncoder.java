package org.apache.catalina.util;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.BitSet;
public class URLEncoder {
    private static final char[] hexadecimal = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'A', 'B', 'C', 'D', 'E', 'F'
    };
    public static final URLEncoder DEFAULT = new URLEncoder();
    static {
        DEFAULT.addSafeCharacter ( '~' );
        DEFAULT.addSafeCharacter ( '-' );
        DEFAULT.addSafeCharacter ( '_' );
        DEFAULT.addSafeCharacter ( '.' );
        DEFAULT.addSafeCharacter ( '*' );
        DEFAULT.addSafeCharacter ( '/' );
    }
    protected final BitSet safeCharacters = new BitSet ( 256 );
    public URLEncoder() {
        for ( char i = 'a'; i <= 'z'; i++ ) {
            addSafeCharacter ( i );
        }
        for ( char i = 'A'; i <= 'Z'; i++ ) {
            addSafeCharacter ( i );
        }
        for ( char i = '0'; i <= '9'; i++ ) {
            addSafeCharacter ( i );
        }
    }
    public void addSafeCharacter ( char c ) {
        safeCharacters.set ( c );
    }
    public String encode ( String path, String encoding ) {
        int maxBytesPerChar = 10;
        StringBuilder rewrittenPath = new StringBuilder ( path.length() );
        ByteArrayOutputStream buf = new ByteArrayOutputStream ( maxBytesPerChar );
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter ( buf, encoding );
        } catch ( Exception e ) {
            e.printStackTrace();
            writer = new OutputStreamWriter ( buf );
        }
        for ( int i = 0; i < path.length(); i++ ) {
            int c = path.charAt ( i );
            if ( safeCharacters.get ( c ) ) {
                rewrittenPath.append ( ( char ) c );
            } else {
                try {
                    writer.write ( ( char ) c );
                    writer.flush();
                } catch ( IOException e ) {
                    buf.reset();
                    continue;
                }
                byte[] ba = buf.toByteArray();
                for ( int j = 0; j < ba.length; j++ ) {
                    byte toEncode = ba[j];
                    rewrittenPath.append ( '%' );
                    int low = toEncode & 0x0f;
                    int high = ( toEncode & 0xf0 ) >> 4;
                    rewrittenPath.append ( hexadecimal[high] );
                    rewrittenPath.append ( hexadecimal[low] );
                }
                buf.reset();
            }
        }
        return rewrittenPath.toString();
    }
}
