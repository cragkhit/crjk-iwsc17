package org.apache.tomcat.util.codec.binary;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
public class StringUtils {
    private static byte[] getBytes ( final String string, final Charset charset ) {
        if ( string == null ) {
            return null;
        }
        return string.getBytes ( charset );
    }
    public static byte[] getBytesUtf8 ( final String string ) {
        return getBytes ( string, StandardCharsets.UTF_8 );
    }
    private static String newString ( final byte[] bytes, final Charset charset ) {
        return ( bytes == null ) ? null : new String ( bytes, charset );
    }
    public static String newStringUtf8 ( final byte[] bytes ) {
        return newString ( bytes, StandardCharsets.UTF_8 );
    }
}
