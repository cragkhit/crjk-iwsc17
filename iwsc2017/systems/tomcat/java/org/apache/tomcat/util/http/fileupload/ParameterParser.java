package org.apache.tomcat.util.http.fileupload;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.tomcat.util.http.fileupload.util.mime.MimeUtility;
public class ParameterParser {
    private char[] chars = null;
    private int pos = 0;
    private int len = 0;
    private int i1 = 0;
    private int i2 = 0;
    private boolean lowerCaseNames = false;
    public ParameterParser() {
        super();
    }
    private boolean hasChar() {
        return this.pos < this.len;
    }
    private String getToken ( boolean quoted ) {
        while ( ( i1 < i2 ) && ( Character.isWhitespace ( chars[i1] ) ) ) {
            i1++;
        }
        while ( ( i2 > i1 ) && ( Character.isWhitespace ( chars[i2 - 1] ) ) ) {
            i2--;
        }
        if ( quoted
                && ( ( i2 - i1 ) >= 2 )
                && ( chars[i1] == '"' )
                && ( chars[i2 - 1] == '"' ) ) {
            i1++;
            i2--;
        }
        String result = null;
        if ( i2 > i1 ) {
            result = new String ( chars, i1, i2 - i1 );
        }
        return result;
    }
    private boolean isOneOf ( char ch, final char[] charray ) {
        boolean result = false;
        for ( char element : charray ) {
            if ( ch == element ) {
                result = true;
                break;
            }
        }
        return result;
    }
    private String parseToken ( final char[] terminators ) {
        char ch;
        i1 = pos;
        i2 = pos;
        while ( hasChar() ) {
            ch = chars[pos];
            if ( isOneOf ( ch, terminators ) ) {
                break;
            }
            i2++;
            pos++;
        }
        return getToken ( false );
    }
    private String parseQuotedToken ( final char[] terminators ) {
        char ch;
        i1 = pos;
        i2 = pos;
        boolean quoted = false;
        boolean charEscaped = false;
        while ( hasChar() ) {
            ch = chars[pos];
            if ( !quoted && isOneOf ( ch, terminators ) ) {
                break;
            }
            if ( !charEscaped && ch == '"' ) {
                quoted = !quoted;
            }
            charEscaped = ( !charEscaped && ch == '\\' );
            i2++;
            pos++;
        }
        return getToken ( true );
    }
    public boolean isLowerCaseNames() {
        return this.lowerCaseNames;
    }
    public void setLowerCaseNames ( boolean b ) {
        this.lowerCaseNames = b;
    }
    public Map<String, String> parse ( final String str, char[] separators ) {
        if ( separators == null || separators.length == 0 ) {
            return new HashMap<>();
        }
        char separator = separators[0];
        if ( str != null ) {
            int idx = str.length();
            for ( char separator2 : separators ) {
                int tmp = str.indexOf ( separator2 );
                if ( tmp != -1 && tmp < idx ) {
                    idx = tmp;
                    separator = separator2;
                }
            }
        }
        return parse ( str, separator );
    }
    public Map<String, String> parse ( final String str, char separator ) {
        if ( str == null ) {
            return new HashMap<>();
        }
        return parse ( str.toCharArray(), separator );
    }
    public Map<String, String> parse ( final char[] charArray, char separator ) {
        if ( charArray == null ) {
            return new HashMap<>();
        }
        return parse ( charArray, 0, charArray.length, separator );
    }
    public Map<String, String> parse (
        final char[] charArray,
        int offset,
        int length,
        char separator ) {
        if ( charArray == null ) {
            return new HashMap<>();
        }
        HashMap<String, String> params = new HashMap<>();
        this.chars = charArray;
        this.pos = offset;
        this.len = length;
        String paramName = null;
        String paramValue = null;
        while ( hasChar() ) {
            paramName = parseToken ( new char[] {
                                         '=', separator
                                     } );
            paramValue = null;
            if ( hasChar() && ( charArray[pos] == '=' ) ) {
                pos++;
                paramValue = parseQuotedToken ( new char[] {
                                                    separator
                                                } );
                if ( paramValue != null ) {
                    try {
                        paramValue = MimeUtility.decodeText ( paramValue );
                    } catch ( UnsupportedEncodingException e ) {
                    }
                }
            }
            if ( hasChar() && ( charArray[pos] == separator ) ) {
                pos++;
            }
            if ( ( paramName != null ) && ( paramName.length() > 0 ) ) {
                if ( this.lowerCaseNames ) {
                    paramName = paramName.toLowerCase ( Locale.ENGLISH );
                }
                params.put ( paramName, paramValue );
            }
        }
        return params;
    }
}
