package org.apache.jasper.security;
import org.apache.jasper.Constants;
public final class SecurityUtil {
    private static final boolean packageDefinitionEnabled;
    public static boolean isPackageProtectionEnabled() {
        return SecurityUtil.packageDefinitionEnabled && Constants.IS_SECURITY_ENABLED;
    }
    public static String filter ( final String message ) {
        if ( message == null ) {
            return null;
        }
        final char[] content = new char[message.length()];
        message.getChars ( 0, message.length(), content, 0 );
        final StringBuilder result = new StringBuilder ( content.length + 50 );
        for ( int i = 0; i < content.length; ++i ) {
            switch ( content[i] ) {
            case '<': {
                result.append ( "&lt;" );
                break;
            }
            case '>': {
                result.append ( "&gt;" );
                break;
            }
            case '&': {
                result.append ( "&amp;" );
                break;
            }
            case '\"': {
                result.append ( "&quot;" );
                break;
            }
            default: {
                result.append ( content[i] );
                break;
            }
            }
        }
        return result.toString();
    }
    static {
        packageDefinitionEnabled = ( System.getProperty ( "package.definition" ) != null );
    }
}
