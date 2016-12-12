package org.apache.tomcat.util.http.parser;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.IOException;
import java.io.StringReader;
public class HttpParser {
    private static final int ARRAY_SIZE = 128;
    private static final boolean[] IS_CONTROL;
    private static final boolean[] IS_SEPARATOR;
    private static final boolean[] IS_TOKEN;
    private static final boolean[] IS_HEX;
    private static final boolean[] IS_NOT_REQUEST_TARGET;
    private static final boolean[] IS_HTTP_PROTOCOL;
    public static String unquote ( final String input ) {
        if ( input == null || input.length() < 2 ) {
            return input;
        }
        int start;
        int end;
        if ( input.charAt ( 0 ) == '\"' ) {
            start = 1;
            end = input.length() - 1;
        } else {
            start = 0;
            end = input.length();
        }
        final StringBuilder result = new StringBuilder();
        for ( int i = start; i < end; ++i ) {
            final char c = input.charAt ( i );
            if ( input.charAt ( i ) == '\\' ) {
                ++i;
                result.append ( input.charAt ( i ) );
            } else {
                result.append ( c );
            }
        }
        return result.toString();
    }
    public static boolean isToken ( final int c ) {
        try {
            return HttpParser.IS_TOKEN[c];
        } catch ( ArrayIndexOutOfBoundsException ex ) {
            return false;
        }
    }
    public static boolean isHex ( final int c ) {
        try {
            return HttpParser.IS_HEX[c];
        } catch ( ArrayIndexOutOfBoundsException ex ) {
            return false;
        }
    }
    public static boolean isNotRequestTarget ( final int c ) {
        try {
            return HttpParser.IS_NOT_REQUEST_TARGET[c];
        } catch ( ArrayIndexOutOfBoundsException ex ) {
            return true;
        }
    }
    public static boolean isHttpProtocol ( final int c ) {
        try {
            return HttpParser.IS_HTTP_PROTOCOL[c];
        } catch ( ArrayIndexOutOfBoundsException ex ) {
            return false;
        }
    }
    static int skipLws ( final StringReader input, final boolean withReset ) throws IOException {
        if ( withReset ) {
            input.mark ( 1 );
        }
        int c;
        for ( c = input.read(); c == 32 || c == 9 || c == 10 || c == 13; c = input.read() ) {
            if ( withReset ) {
                input.mark ( 1 );
            }
        }
        if ( withReset ) {
            input.reset();
        }
        return c;
    }
    static SkipResult skipConstant ( final StringReader input, final String constant ) throws IOException {
        final int len = constant.length();
        int c = skipLws ( input, false );
        for ( int i = 0; i < len; ++i ) {
            if ( i == 0 && c == -1 ) {
                return SkipResult.EOF;
            }
            if ( c != constant.charAt ( i ) ) {
                input.skip ( - ( i + 1 ) );
                return SkipResult.NOT_FOUND;
            }
            if ( i != len - 1 ) {
                c = input.read();
            }
        }
        return SkipResult.FOUND;
    }
    static String readToken ( final StringReader input ) throws IOException {
        final StringBuilder result = new StringBuilder();
        int c;
        for ( c = skipLws ( input, false ); c != -1 && isToken ( c ); c = input.read() ) {
            result.append ( ( char ) c );
        }
        input.skip ( -1L );
        if ( c != -1 && result.length() == 0 ) {
            return null;
        }
        return result.toString();
    }
    static String readQuotedString ( final StringReader input, final boolean returnQuoted ) throws IOException {
        int c = skipLws ( input, false );
        if ( c != 34 ) {
            return null;
        }
        final StringBuilder result = new StringBuilder();
        if ( returnQuoted ) {
            result.append ( '\"' );
        }
        for ( c = input.read(); c != 34; c = input.read() ) {
            if ( c == -1 ) {
                return null;
            }
            if ( c == 92 ) {
                c = input.read();
                if ( returnQuoted ) {
                    result.append ( '\\' );
                }
                result.append ( c );
            } else {
                result.append ( ( char ) c );
            }
        }
        if ( returnQuoted ) {
            result.append ( '\"' );
        }
        return result.toString();
    }
    static String readTokenOrQuotedString ( final StringReader input, final boolean returnQuoted ) throws IOException {
        final int c = skipLws ( input, true );
        if ( c == 34 ) {
            return readQuotedString ( input, returnQuoted );
        }
        return readToken ( input );
    }
    static String readQuotedToken ( final StringReader input ) throws IOException {
        final StringBuilder result = new StringBuilder();
        boolean quoted = false;
        int c = skipLws ( input, false );
        if ( c == 34 ) {
            quoted = true;
        } else {
            if ( c == -1 || !isToken ( c ) ) {
                return null;
            }
            result.append ( ( char ) c );
        }
        for ( c = input.read(); c != -1 && isToken ( c ); c = input.read() ) {
            result.append ( ( char ) c );
        }
        if ( quoted ) {
            if ( c != 34 ) {
                return null;
            }
        } else {
            input.skip ( -1L );
        }
        if ( c != -1 && result.length() == 0 ) {
            return null;
        }
        return result.toString();
    }
    static String readLhex ( final StringReader input ) throws IOException {
        final StringBuilder result = new StringBuilder();
        boolean quoted = false;
        int c = skipLws ( input, false );
        if ( c == 34 ) {
            quoted = true;
        } else {
            if ( c == -1 || !isHex ( c ) ) {
                return null;
            }
            if ( 65 <= c && c <= 70 ) {
                c += 32;
            }
            result.append ( ( char ) c );
        }
        for ( c = input.read(); c != -1 && isHex ( c ); c = input.read() ) {
            if ( 65 <= c && c <= 70 ) {
                c += 32;
            }
            result.append ( ( char ) c );
        }
        if ( quoted ) {
            if ( c != 34 ) {
                return null;
            }
        } else {
            input.skip ( -1L );
        }
        if ( c != -1 && result.length() == 0 ) {
            return null;
        }
        return result.toString();
    }
    static double readWeight ( final StringReader input, final char delimiter ) throws IOException {
        int c = skipLws ( input, false );
        if ( c == -1 || c == delimiter ) {
            return 1.0;
        }
        if ( c != 113 ) {
            skipUntil ( input, c, delimiter );
            return 0.0;
        }
        c = skipLws ( input, false );
        if ( c != 61 ) {
            skipUntil ( input, c, delimiter );
            return 0.0;
        }
        c = skipLws ( input, false );
        final StringBuilder value = new StringBuilder ( 5 );
        int decimalPlacesRead = 0;
        if ( c != 48 && c != 49 ) {
            skipUntil ( input, c, delimiter );
            return 0.0;
        }
        value.append ( ( char ) c );
        c = input.read();
        if ( c == 46 ) {
            value.append ( '.' );
        } else if ( c < 48 || c > 57 ) {
            decimalPlacesRead = 3;
        }
        while ( true ) {
            c = input.read();
            if ( c < 48 || c > 57 ) {
                break;
            }
            if ( decimalPlacesRead >= 3 ) {
                continue;
            }
            value.append ( ( char ) c );
            ++decimalPlacesRead;
        }
        if ( c != delimiter && c != 9 && c != 32 && c != -1 ) {
            skipUntil ( input, c, delimiter );
            return 0.0;
        }
        final double result = Double.parseDouble ( value.toString() );
        if ( result > 1.0 ) {
            return 0.0;
        }
        return result;
    }
    static SkipResult skipUntil ( final StringReader input, int c, final char target ) throws IOException {
        while ( c != -1 && c != target ) {
            c = input.read();
        }
        if ( c == -1 ) {
            return SkipResult.EOF;
        }
        return SkipResult.FOUND;
    }
    static {
        IS_CONTROL = new boolean[128];
        IS_SEPARATOR = new boolean[128];
        IS_TOKEN = new boolean[128];
        IS_HEX = new boolean[128];
        IS_NOT_REQUEST_TARGET = new boolean[128];
        IS_HTTP_PROTOCOL = new boolean[128];
        for ( int i = 0; i < 128; ++i ) {
            if ( i < 32 || i == 127 ) {
                HttpParser.IS_CONTROL[i] = true;
            }
            if ( i == 40 || i == 41 || i == 60 || i == 62 || i == 64 || i == 44 || i == 59 || i == 58 || i == 92 || i == 34 || i == 47 || i == 91 || i == 93 || i == 63 || i == 61 || i == 123 || i == 125 || i == 32 || i == 9 ) {
                HttpParser.IS_SEPARATOR[i] = true;
            }
            if ( !HttpParser.IS_CONTROL[i] && !HttpParser.IS_SEPARATOR[i] && i < 128 ) {
                HttpParser.IS_TOKEN[i] = true;
            }
            if ( ( i >= 48 && i <= 57 ) || ( i >= 97 && i <= 102 ) || ( i >= 65 && i <= 70 ) ) {
                HttpParser.IS_HEX[i] = true;
            }
            if ( HttpParser.IS_CONTROL[i] || i > 127 || i == 32 || i == 34 || i == 35 || i == 60 || i == 62 || i == 92 || i == 94 || i == 96 || i == 123 || i == 124 || i == 125 ) {
                HttpParser.IS_NOT_REQUEST_TARGET[i] = true;
            }
            if ( i == 72 || i == 84 || i == 80 || i == 47 || i == 46 || ( i >= 48 && i <= 57 ) ) {
                HttpParser.IS_HTTP_PROTOCOL[i] = true;
            }
        }
    }
}
