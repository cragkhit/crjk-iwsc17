package org.apache.jasper.security;
import org.apache.jasper.Constants;
public final class SecurityUtil {
    private static final boolean packageDefinitionEnabled =
        System.getProperty ( "package.definition" ) == null ? false : true;
    public static boolean isPackageProtectionEnabled() {
        if ( packageDefinitionEnabled && Constants.IS_SECURITY_ENABLED ) {
            return true;
        }
        return false;
    }
    public static String filter ( String message ) {
        if ( message == null ) {
            return ( null );
        }
        char content[] = new char[message.length()];
        message.getChars ( 0, message.length(), content, 0 );
        StringBuilder result = new StringBuilder ( content.length + 50 );
        for ( int i = 0; i < content.length; i++ ) {
            switch ( content[i] ) {
            case '<':
                result.append ( "&lt;" );
                break;
            case '>':
                result.append ( "&gt;" );
                break;
            case '&':
                result.append ( "&amp;" );
                break;
            case '"':
                result.append ( "&quot;" );
                break;
            default:
                result.append ( content[i] );
            }
        }
        return ( result.toString() );
    }
}
