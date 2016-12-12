package org.apache.tomcat.util.http;
public class RequestUtil {
    private RequestUtil() {
    }
    public static String normalize ( String path ) {
        return normalize ( path, true );
    }
    public static String normalize ( String path, boolean replaceBackSlash ) {
        if ( path == null ) {
            return null;
        }
        String normalized = path;
        if ( replaceBackSlash && normalized.indexOf ( '\\' ) >= 0 ) {
            normalized = normalized.replace ( '\\', '/' );
        }
        if ( !normalized.startsWith ( "/" ) ) {
            normalized = "/" + normalized;
        }
        boolean addedTrailingSlash = false;
        if ( normalized.endsWith ( "/." ) || normalized.endsWith ( "/.." ) ) {
            normalized = normalized + "/";
            addedTrailingSlash = true;
        }
        while ( true ) {
            int index = normalized.indexOf ( "//" );
            if ( index < 0 ) {
                break;
            }
            normalized = normalized.substring ( 0, index ) + normalized.substring ( index + 1 );
        }
        while ( true ) {
            int index = normalized.indexOf ( "/./" );
            if ( index < 0 ) {
                break;
            }
            normalized = normalized.substring ( 0, index ) + normalized.substring ( index + 2 );
        }
        while ( true ) {
            int index = normalized.indexOf ( "/../" );
            if ( index < 0 ) {
                break;
            }
            if ( index == 0 ) {
                return null;
            }
            int index2 = normalized.lastIndexOf ( '/', index - 1 );
            normalized = normalized.substring ( 0, index2 ) + normalized.substring ( index + 3 );
        }
        if ( normalized.length() > 1 && addedTrailingSlash ) {
            normalized = normalized.substring ( 0, normalized.length() - 1 );
        }
        return normalized;
    }
}
