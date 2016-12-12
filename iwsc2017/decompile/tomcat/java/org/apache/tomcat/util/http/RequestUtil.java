package org.apache.tomcat.util.http;
public class RequestUtil {
    public static String normalize ( final String path ) {
        return normalize ( path, true );
    }
    public static String normalize ( final String path, final boolean replaceBackSlash ) {
        if ( path == null ) {
            return null;
        }
        String normalized = path;
        if ( replaceBackSlash && normalized.indexOf ( 92 ) >= 0 ) {
            normalized = normalized.replace ( '\\', '/' );
        }
        if ( !normalized.startsWith ( "/" ) ) {
            normalized = "/" + normalized;
        }
        boolean addedTrailingSlash = false;
        if ( normalized.endsWith ( "/." ) || normalized.endsWith ( "/.." ) ) {
            normalized += "/";
            addedTrailingSlash = true;
        }
        while ( true ) {
            final int index = normalized.indexOf ( "//" );
            if ( index < 0 ) {
                break;
            }
            normalized = normalized.substring ( 0, index ) + normalized.substring ( index + 1 );
        }
        while ( true ) {
            final int index = normalized.indexOf ( "/./" );
            if ( index < 0 ) {
                break;
            }
            normalized = normalized.substring ( 0, index ) + normalized.substring ( index + 2 );
        }
        while ( true ) {
            final int index = normalized.indexOf ( "/../" );
            if ( index < 0 ) {
                if ( normalized.length() > 1 && addedTrailingSlash ) {
                    normalized = normalized.substring ( 0, normalized.length() - 1 );
                }
                return normalized;
            }
            if ( index == 0 ) {
                return null;
            }
            final int index2 = normalized.lastIndexOf ( 47, index - 1 );
            normalized = normalized.substring ( 0, index2 ) + normalized.substring ( index + 3 );
        }
    }
}
