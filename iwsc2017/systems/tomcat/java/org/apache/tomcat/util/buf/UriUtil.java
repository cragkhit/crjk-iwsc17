package org.apache.tomcat.util.buf;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;
public final class UriUtil {
    private static Pattern PATTERN_EXCLAMATION_MARK = Pattern.compile ( "!/" );
    private static Pattern PATTERN_CARET = Pattern.compile ( "\\^/" );
    private static Pattern PATTERN_ASTERISK = Pattern.compile ( "\\*/" );
    private UriUtil() {
    }
    private static boolean isSchemeChar ( char c ) {
        return Character.isLetterOrDigit ( c ) || c == '+' || c == '-' || c == '.';
    }
    public static boolean hasScheme ( CharSequence uri ) {
        int len = uri.length();
        for ( int i = 0; i < len ; i++ ) {
            char c = uri.charAt ( i );
            if ( c == ':' ) {
                return i > 0;
            } else if ( !UriUtil.isSchemeChar ( c ) ) {
                return false;
            }
        }
        return false;
    }
    public static URL buildJarUrl ( File jarFile ) throws MalformedURLException {
        return buildJarUrl ( jarFile, null );
    }
    public static URL buildJarUrl ( File jarFile, String entryPath ) throws MalformedURLException {
        return buildJarUrl ( jarFile.toURI().toString(), entryPath );
    }
    public static URL buildJarUrl ( String fileUrlString ) throws MalformedURLException {
        return buildJarUrl ( fileUrlString, null );
    }
    public static URL buildJarUrl ( String fileUrlString, String entryPath ) throws MalformedURLException {
        String safeString = makeSafeForJarUrl ( fileUrlString );
        StringBuilder sb = new StringBuilder();
        sb.append ( "jar:" );
        sb.append ( safeString );
        sb.append ( "!/" );
        if ( entryPath != null ) {
            sb.append ( makeSafeForJarUrl ( entryPath ) );
        }
        return new URL ( sb.toString() );
    }
    public static URL buildJarSafeUrl ( File file ) throws MalformedURLException {
        String safe = makeSafeForJarUrl ( file.toURI().toString() );
        return new URL ( safe );
    }
    private static String makeSafeForJarUrl ( String input ) {
        String tmp = PATTERN_EXCLAMATION_MARK.matcher ( input ).replaceAll ( "%21/" );
        tmp = PATTERN_CARET.matcher ( tmp ).replaceAll ( "%5e/" );
        return PATTERN_ASTERISK.matcher ( tmp ).replaceAll ( "%2a/" );
    }
    public static URL warToJar ( URL warUrl ) throws MalformedURLException {
        String file = warUrl.getFile();
        if ( file.contains ( "*/" ) ) {
            file = file.replaceFirst ( "\\*/", "!/" );
        } else {
            file = file.replaceFirst ( "\\^/", "!/" );
        }
        return new URL ( "jar", warUrl.getHost(), warUrl.getPort(), file );
    }
}
