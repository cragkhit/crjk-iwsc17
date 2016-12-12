package org.apache.tomcat.util.buf;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.File;
import java.util.regex.Pattern;
public final class UriUtil {
    private static Pattern PATTERN_EXCLAMATION_MARK;
    private static Pattern PATTERN_CARET;
    private static Pattern PATTERN_ASTERISK;
    private static boolean isSchemeChar ( final char c ) {
        return Character.isLetterOrDigit ( c ) || c == '+' || c == '-' || c == '.';
    }
    public static boolean hasScheme ( final CharSequence uri ) {
        for ( int len = uri.length(), i = 0; i < len; ++i ) {
            final char c = uri.charAt ( i );
            if ( c == ':' ) {
                return i > 0;
            }
            if ( !isSchemeChar ( c ) ) {
                return false;
            }
        }
        return false;
    }
    public static URL buildJarUrl ( final File jarFile ) throws MalformedURLException {
        return buildJarUrl ( jarFile, null );
    }
    public static URL buildJarUrl ( final File jarFile, final String entryPath ) throws MalformedURLException {
        return buildJarUrl ( jarFile.toURI().toString(), entryPath );
    }
    public static URL buildJarUrl ( final String fileUrlString ) throws MalformedURLException {
        return buildJarUrl ( fileUrlString, null );
    }
    public static URL buildJarUrl ( final String fileUrlString, final String entryPath ) throws MalformedURLException {
        final String safeString = makeSafeForJarUrl ( fileUrlString );
        final StringBuilder sb = new StringBuilder();
        sb.append ( "jar:" );
        sb.append ( safeString );
        sb.append ( "!/" );
        if ( entryPath != null ) {
            sb.append ( makeSafeForJarUrl ( entryPath ) );
        }
        return new URL ( sb.toString() );
    }
    public static URL buildJarSafeUrl ( final File file ) throws MalformedURLException {
        final String safe = makeSafeForJarUrl ( file.toURI().toString() );
        return new URL ( safe );
    }
    private static String makeSafeForJarUrl ( final String input ) {
        String tmp = UriUtil.PATTERN_EXCLAMATION_MARK.matcher ( input ).replaceAll ( "%21/" );
        tmp = UriUtil.PATTERN_CARET.matcher ( tmp ).replaceAll ( "%5e/" );
        return UriUtil.PATTERN_ASTERISK.matcher ( tmp ).replaceAll ( "%2a/" );
    }
    public static URL warToJar ( final URL warUrl ) throws MalformedURLException {
        String file = warUrl.getFile();
        if ( file.contains ( "*/" ) ) {
            file = file.replaceFirst ( "\\*/", "!/" );
        } else {
            file = file.replaceFirst ( "\\^/", "!/" );
        }
        return new URL ( "jar", warUrl.getHost(), warUrl.getPort(), file );
    }
    static {
        UriUtil.PATTERN_EXCLAMATION_MARK = Pattern.compile ( "!/" );
        UriUtil.PATTERN_CARET = Pattern.compile ( "\\^/" );
        UriUtil.PATTERN_ASTERISK = Pattern.compile ( "\\*/" );
    }
}
