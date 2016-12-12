package org.apache.el.util;
import java.security.AccessController;
import java.security.PrivilegedAction;
public class Validation {
    private static final String invalidIdentifiers[] = { "abstract", "assert",
                                                         "boolean", "break", "byte", "case", "catch", "char", "class", "const",
                                                         "continue", "default", "do", "double", "else", "enum", "extends",
                                                         "false", "final", "finally", "float", "for", "goto", "if", "implements",
                                                         "import", "instanceof", "int", "interface", "long", "native", "new",
                                                         "null", "package", "private", "protected", "public", "return", "short",
                                                         "static", "strictfp", "super", "switch", "synchronized", "this",
                                                         "throw", "throws", "transient", "true", "try", "void", "volatile",
                                                         "while"
                                                       };
    private static final boolean IS_SECURITY_ENABLED =
        ( System.getSecurityManager() != null );
    private static final boolean SKIP_IDENTIFIER_CHECK;
    static {
        String skipIdentifierCheckStr;
        if ( IS_SECURITY_ENABLED ) {
            skipIdentifierCheckStr = AccessController.doPrivileged (
            new PrivilegedAction<String>() {
                @Override
                public String run() {
                    return System.getProperty (
                               "org.apache.el.parser.SKIP_IDENTIFIER_CHECK",
                               "false" );
                }
            }
                                     );
        } else {
            skipIdentifierCheckStr = System.getProperty (
                                         "org.apache.el.parser.SKIP_IDENTIFIER_CHECK", "false" );
        }
        SKIP_IDENTIFIER_CHECK = Boolean.parseBoolean ( skipIdentifierCheckStr );
    }
    private Validation() {
    }
    public static boolean isIdentifier ( String key ) {
        if ( SKIP_IDENTIFIER_CHECK ) {
            return true;
        }
        if ( key == null || key.length() == 0 ) {
            return false;
        }
        int i = 0;
        int j = invalidIdentifiers.length;
        while ( i < j ) {
            int k = ( i + j ) >>> 1;
            int result = invalidIdentifiers[k].compareTo ( key );
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
        for ( int idx = 1; idx < key.length(); idx++ ) {
            if ( !Character.isJavaIdentifierPart ( key.charAt ( idx ) ) ) {
                return false;
            }
        }
        return true;
    }
}
