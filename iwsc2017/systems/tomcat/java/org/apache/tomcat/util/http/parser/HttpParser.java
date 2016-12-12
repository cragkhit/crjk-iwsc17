package org.apache.tomcat.util.http.parser;
import java.io.IOException;
import java.io.StringReader;
public class HttpParser {
    private static final int ARRAY_SIZE = 128;
    private static final boolean[] IS_CONTROL = new boolean[ARRAY_SIZE];
    private static final boolean[] IS_SEPARATOR = new boolean[ARRAY_SIZE];
    private static final boolean[] IS_TOKEN = new boolean[ARRAY_SIZE];
    private static final boolean[] IS_HEX = new boolean[ARRAY_SIZE];
    private static final boolean[] IS_NOT_REQUEST_TARGET = new boolean[ARRAY_SIZE];
    private static final boolean[] IS_HTTP_PROTOCOL = new boolean[ARRAY_SIZE];
    static {
        for ( int i = 0; i < ARRAY_SIZE; i++ ) {
            if ( i < 32 || i == 127 ) {
                IS_CONTROL[i] = true;
            }
            if ( i == '(' || i == ')' || i == '<' || i == '>'  || i == '@'  ||
                    i == ',' || i == ';' || i == ':' || i == '\\' || i == '\"' ||
                    i == '/' || i == '[' || i == ']' || i == '?'  || i == '='  ||
                    i == '{' || i == '}' || i == ' ' || i == '\t' ) {
                IS_SEPARATOR[i] = true;
            }
            if ( !IS_CONTROL[i] && !IS_SEPARATOR[i] && i < 128 ) {
                IS_TOKEN[i] = true;
            }
            if ( ( i >= '0' && i <= '9' ) || ( i >= 'a' && i <= 'f' ) || ( i >= 'A' && i <= 'F' ) ) {
                IS_HEX[i] = true;
            }
            if ( IS_CONTROL[i] || i > 127 ||
                    i == ' ' || i == '\"' || i == '#' || i == '<' || i == '>' || i == '\\' ||
                    i == '^' || i == '`'  || i == '{' || i == '|' || i == '}' ) {
                IS_NOT_REQUEST_TARGET[i] = true;
            }
            if ( i == 'H' || i == 'T' || i == 'P' || i == '/' || i == '.' || ( i >= '0' && i <= '9' ) ) {
                IS_HTTP_PROTOCOL[i] = true;
            }
        }
    }
    public static String unquote ( String input ) {
        if ( input == null || input.length() < 2 ) {
            return input;
        }
        int start;
        int end;
        if ( input.charAt ( 0 ) == '"' ) {
            start = 1;
            end = input.length() - 1;
        } else {
            start = 0;
            end = input.length();
        }
        StringBuilder result = new StringBuilder();
        for ( int i = start ; i < end; i++ ) {
            char c = input.charAt ( i );
            if ( input.charAt ( i ) == '\\' ) {
                i++;
                result.append ( input.charAt ( i ) );
            } else {
                result.append ( c );
            }
        }
        return result.toString();
    }
    public static boolean isToken ( int c ) {
        try {
            return IS_TOKEN[c];
        } catch ( ArrayIndexOutOfBoundsException ex ) {
            return false;
        }
    }
    public static boolean isHex ( int c ) {
        try {
            return IS_HEX[c];
        } catch ( ArrayIndexOutOfBoundsException ex ) {
            return false;
        }
    }
    public static boolean isNotRequestTarget ( int c ) {
        try {
            return IS_NOT_REQUEST_TARGET[c];
        } catch ( ArrayIndexOutOfBoundsException ex ) {
            return true;
        }
    }
    public static boolean isHttpProtocol ( int c ) {
        try {
            return IS_HTTP_PROTOCOL[c];
        } catch ( ArrayIndexOutOfBoundsException ex ) {
            return false;
        }
    }
    static int skipLws ( StringReader input, boolean withReset ) throws IOException {
        if ( withReset ) {
            input.mark ( 1 );
        }
        int c = input.read();
        while ( c == 32 || c == 9 || c == 10 || c == 13 ) {
            if ( withReset ) {
                input.mark ( 1 );
            }
            c = input.read();
        }
        if ( withReset ) {
            input.reset();
        }
        return c;
    }
    static SkipResult skipConstant ( StringReader input, String constant ) throws IOException {
        int len = constant.length();
        int c = skipLws ( input, false );
        for ( int i = 0; i < len; i++ ) {
            if ( i == 0 && c == -1 ) {
                return SkipResult.EOF;
            }
            if ( c != constant.charAt ( i ) ) {
                input.skip ( - ( i + 1 ) );
                return SkipResult.NOT_FOUND;
            }
            if ( i != ( len - 1 ) ) {
                c = input.read();
            }
        }
        return SkipResult.FOUND;
    }
    static String readToken ( StringReader input ) throws IOException {
        StringBuilder result = new StringBuilder();
        int c = skipLws ( input, false );
        while ( c != -1 && isToken ( c ) ) {
            result.append ( ( char ) c );
            c = input.read();
        }
        input.skip ( -1 );
        if ( c != -1 && result.length() == 0 ) {
            return null;
        } else {
            return result.toString();
        }
    }
    static String readQuotedString ( StringReader input, boolean returnQuoted ) throws IOException {
        int c = skipLws ( input, false );
        if ( c != '"' ) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        if ( returnQuoted ) {
            result.append ( '\"' );
        }
        c = input.read();
        while ( c != '"' ) {
            if ( c == -1 ) {
                return null;
            } else if ( c == '\\' ) {
                c = input.read();
                if ( returnQuoted ) {
                    result.append ( '\\' );
                }
                result.append ( c );
            } else {
                result.append ( ( char ) c );
            }
            c = input.read();
        }
        if ( returnQuoted ) {
            result.append ( '\"' );
        }
        return result.toString();
    }
    static String readTokenOrQuotedString ( StringReader input, boolean returnQuoted )
    throws IOException {
        int c = skipLws ( input, true );
        if ( c == '"' ) {
            return readQuotedString ( input, returnQuoted );
        } else {
            return readToken ( input );
        }
    }
    static String readQuotedToken ( StringReader input ) throws IOException {
        StringBuilder result = new StringBuilder();
        boolean quoted = false;
        int c = skipLws ( input, false );
        if ( c == '"' ) {
            quoted = true;
        } else if ( c == -1 || !isToken ( c ) ) {
            return null;
        } else {
            result.append ( ( char ) c );
        }
        c = input.read();
        while ( c != -1 && isToken ( c ) ) {
            result.append ( ( char ) c );
            c = input.read();
        }
        if ( quoted ) {
            if ( c != '"' ) {
                return null;
            }
        } else {
            input.skip ( -1 );
        }
        if ( c != -1 && result.length() == 0 ) {
            return null;
        } else {
            return result.toString();
        }
    }
    static String readLhex ( StringReader input ) throws IOException {
        StringBuilder result = new StringBuilder();
        boolean quoted = false;
        int c = skipLws ( input, false );
        if ( c == '"' ) {
            quoted = true;
        } else if ( c == -1 || !isHex ( c ) ) {
            return null;
        } else {
            if ( 'A' <= c && c <= 'F' ) {
                c -= ( 'A' - 'a' );
            }
            result.append ( ( char ) c );
        }
        c = input.read();
        while ( c != -1 && isHex ( c ) ) {
            if ( 'A' <= c && c <= 'F' ) {
                c -= ( 'A' - 'a' );
            }
            result.append ( ( char ) c );
            c = input.read();
        }
        if ( quoted ) {
            if ( c != '"' ) {
                return null;
            }
        } else {
            input.skip ( -1 );
        }
        if ( c != -1 && result.length() == 0 ) {
            return null;
        } else {
            return result.toString();
        }
    }
    static double readWeight ( StringReader input, char delimiter ) throws IOException {
        int c = skipLws ( input, false );
        if ( c == -1 || c == delimiter ) {
            return 1;
        } else if ( c != 'q' ) {
            skipUntil ( input, c, delimiter );
            return 0;
        }
        c = skipLws ( input, false );
        if ( c != '=' ) {
            skipUntil ( input, c, delimiter );
            return 0;
        }
        c = skipLws ( input, false );
        StringBuilder value = new StringBuilder ( 5 );
        int decimalPlacesRead = 0;
        if ( c == '0' || c == '1' ) {
            value.append ( ( char ) c );
            c = input.read();
            if ( c == '.' ) {
                value.append ( '.' );
            } else if ( c < '0' || c > '9' ) {
                decimalPlacesRead = 3;
            }
            while ( true ) {
                c = input.read();
                if ( c >= '0' && c <= '9' ) {
                    if ( decimalPlacesRead < 3 ) {
                        value.append ( ( char ) c );
                        decimalPlacesRead++;
                    }
                } else if ( c == delimiter || c == 9 || c == 32 || c == -1 ) {
                    break;
                } else {
                    skipUntil ( input, c, delimiter );
                    return 0;
                }
            }
        } else {
            skipUntil ( input, c, delimiter );
            return 0;
        }
        double result = Double.parseDouble ( value.toString() );
        if ( result > 1 ) {
            return 0;
        }
        return result;
    }
    static SkipResult skipUntil ( StringReader input, int c, char target ) throws IOException {
        while ( c != -1 && c != target ) {
            c = input.read();
        }
        if ( c == -1 ) {
            return SkipResult.EOF;
        } else {
            return SkipResult.FOUND;
        }
    }
}
