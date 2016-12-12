package org.apache.el.util;
import java.security.AccessController;
import java.security.PrivilegedAction;
public class Validation {
    private static final String[] invalidIdentifiers;
    private static final boolean IS_SECURITY_ENABLED;
    private static final boolean SKIP_IDENTIFIER_CHECK;
    public static boolean isIdentifier ( final String key ) {
        if ( Validation.SKIP_IDENTIFIER_CHECK ) {
            return true;
        }
        if ( key == null || key.length() == 0 ) {
            return false;
        }
        int i = 0;
        int j = Validation.invalidIdentifiers.length;
        while ( i < j ) {
            final int k = i + j >>> 1;
            final int result = Validation.invalidIdentifiers[k].compareTo ( key );
            if ( result == 0 ) {
                return false;
            }
            if ( result < 0 ) {
                i = k + 1;
            } else {
                j = k;
            }
        }
        if ( !Character.isJavaIdentifierStart ( key.charAt ( 0 ) ) ) {
            return false;
        }
        for ( int idx = 1; idx < key.length(); ++idx ) {
            if ( !Character.isJavaIdentifierPart ( key.charAt ( idx ) ) ) {
                return false;
            }
        }
        return true;
    }
    static {
        invalidIdentifiers = new String[] { "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "false", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "null", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "void", "volatile", "while" };
        IS_SECURITY_ENABLED = ( System.getSecurityManager() != null );
        String skipIdentifierCheckStr;
        if ( Validation.IS_SECURITY_ENABLED ) {
            skipIdentifierCheckStr = AccessController.doPrivileged ( ( PrivilegedAction<String> ) new PrivilegedAction<String>() {
                @Override
                public String run() {
                    return System.getProperty ( "org.apache.el.parser.SKIP_IDENTIFIER_CHECK", "false" );
                }
            } );
        } else {
            skipIdentifierCheckStr = System.getProperty ( "org.apache.el.parser.SKIP_IDENTIFIER_CHECK", "false" );
        }
        SKIP_IDENTIFIER_CHECK = Boolean.parseBoolean ( skipIdentifierCheckStr );
    }
}
