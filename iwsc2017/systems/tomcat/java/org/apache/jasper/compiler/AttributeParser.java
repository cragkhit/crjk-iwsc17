package org.apache.jasper.compiler;
public class AttributeParser {
    public static String getUnquoted ( String input, char quote,
                                       boolean isELIgnored, boolean isDeferredSyntaxAllowedAsLiteral,
                                       boolean strict, boolean quoteAttributeEL ) {
        return ( new AttributeParser ( input, quote, isELIgnored,
                                       isDeferredSyntaxAllowedAsLiteral, strict, quoteAttributeEL ) ).getUnquoted();
    }
    private final String input;
    private final char quote;
    private final boolean isELIgnored;
    private final boolean isDeferredSyntaxAllowedAsLiteral;
    private final boolean strict;
    private final boolean quoteAttributeEL;
    private final char type;
    private final int size;
    private int i = 0;
    private boolean lastChEscaped = false;
    private final StringBuilder result;
    private AttributeParser ( String input, char quote,
                              boolean isELIgnored, boolean isDeferredSyntaxAllowedAsLiteral,
                              boolean strict, boolean quoteAttributeEL ) {
        this.input = input;
        this.quote = quote;
        this.isELIgnored = isELIgnored;
        this.isDeferredSyntaxAllowedAsLiteral =
            isDeferredSyntaxAllowedAsLiteral;
        this.strict = strict;
        this.quoteAttributeEL = quoteAttributeEL;
        this.type = getType ( input );
        this.size = input.length();
        result = new StringBuilder ( size );
    }
    private String getUnquoted() {
        while ( i < size ) {
            parseLiteral();
            parseEL();
        }
        return result.toString();
    }
    private void parseLiteral() {
        boolean foundEL = false;
        while ( i < size && !foundEL ) {
            char ch = nextChar();
            if ( !isELIgnored && ch == '\\' ) {
                if ( type == 0 ) {
                    result.append ( "\\" );
                } else {
                    result.append ( type );
                    result.append ( "{'\\\\'}" );
                }
            } else if ( !isELIgnored && ch == '$' && lastChEscaped ) {
                if ( type == 0 ) {
                    result.append ( "\\$" );
                } else {
                    result.append ( type );
                    result.append ( "{'$'}" );
                }
            } else if ( !isELIgnored && ch == '#' && lastChEscaped ) {
                if ( type == 0 ) {
                    result.append ( "\\#" );
                } else {
                    result.append ( type );
                    result.append ( "{'#'}" );
                }
            } else if ( ch == type ) {
                if ( i < size ) {
                    char next = input.charAt ( i );
                    if ( next == '{' ) {
                        foundEL = true;
                        i--;
                    } else {
                        result.append ( ch );
                    }
                } else {
                    result.append ( ch );
                }
            } else {
                result.append ( ch );
            }
        }
    }
    private void parseEL() {
        boolean endEL = false;
        boolean insideLiteral = false;
        char literalQuote = 0;
        while ( i < size && !endEL ) {
            char ch;
            if ( quoteAttributeEL ) {
                ch = nextChar();
            } else {
                ch = input.charAt ( i++ );
            }
            if ( ch == '\'' || ch == '\"' ) {
                if ( insideLiteral ) {
                    if ( literalQuote == ch ) {
                        insideLiteral = false;
                    }
                } else {
                    insideLiteral = true;
                    literalQuote = ch;
                }
                result.append ( ch );
            } else if ( ch == '\\' ) {
                result.append ( ch );
                if ( insideLiteral && size < i ) {
                    if ( quoteAttributeEL ) {
                        ch = nextChar();
                    } else {
                        ch = input.charAt ( i++ );
                    }
                    result.append ( ch );
                }
            } else if ( ch == '}' ) {
                if ( !insideLiteral ) {
                    endEL = true;
                }
                result.append ( ch );
            } else {
                result.append ( ch );
            }
        }
    }
    private char nextChar() {
        lastChEscaped = false;
        char ch = input.charAt ( i );
        if ( ch == '&' ) {
            if ( i + 5 < size && input.charAt ( i + 1 ) == 'a' &&
                    input.charAt ( i + 2 ) == 'p' && input.charAt ( i + 3 ) == 'o' &&
                    input.charAt ( i + 4 ) == 's' && input.charAt ( i + 5 ) == ';' ) {
                ch = '\'';
                i += 6;
            } else if ( i + 5 < size && input.charAt ( i + 1 ) == 'q' &&
                        input.charAt ( i + 2 ) == 'u' && input.charAt ( i + 3 ) == 'o' &&
                        input.charAt ( i + 4 ) == 't' && input.charAt ( i + 5 ) == ';' ) {
                ch = '\"';
                i += 6;
            } else {
                ++i;
            }
        } else if ( ch == '\\' && i + 1 < size ) {
            ch = input.charAt ( i + 1 );
            if ( ch == '\\' || ch == '\"' || ch == '\'' ||
                    ( !isELIgnored &&
                      ( ch == '$' ||
                        ( !isDeferredSyntaxAllowedAsLiteral &&
                          ch == '#' ) ) ) ) {
                i += 2;
                lastChEscaped = true;
            } else {
                ch = '\\';
                ++i;
            }
        } else if ( ch == '<' && ( i + 2 < size ) && input.charAt ( i + 1 ) == '\\' &&
                    input.charAt ( i + 2 ) == '%' ) {
            result.append ( '<' );
            i += 3;
            return '%';
        } else if ( ch == '%' && i + 2 < size && input.charAt ( i + 1 ) == '\\' &&
                    input.charAt ( i + 2 ) == '>' ) {
            result.append ( '%' );
            i += 3;
            return '>';
        } else if ( ch == quote && strict ) {
            String msg = Localizer.getMessage ( "jsp.error.attribute.noescape",
                                                input, "" + quote );
            throw new IllegalArgumentException ( msg );
        } else {
            ++i;
        }
        return ch;
    }
    private char getType ( String value ) {
        if ( value == null ) {
            return 0;
        }
        if ( isELIgnored ) {
            return 0;
        }
        int j = 0;
        int len = value.length();
        char current;
        while ( j < len ) {
            current = value.charAt ( j );
            if ( current == '\\' ) {
                j++;
            } else if ( current == '#' && !isDeferredSyntaxAllowedAsLiteral ) {
                if ( j < ( len - 1 ) && value.charAt ( j + 1 ) == '{' ) {
                    return '#';
                }
            } else if ( current == '$' ) {
                if ( j < ( len - 1 ) && value.charAt ( j + 1 ) == '{' ) {
                    return '$';
                }
            }
            j++;
        }
        return 0;
    }
}
