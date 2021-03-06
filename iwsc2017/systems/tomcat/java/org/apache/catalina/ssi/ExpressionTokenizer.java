package org.apache.catalina.ssi;
public class ExpressionTokenizer {
    public static final int TOKEN_STRING = 0;
    public static final int TOKEN_AND = 1;
    public static final int TOKEN_OR = 2;
    public static final int TOKEN_NOT = 3;
    public static final int TOKEN_EQ = 4;
    public static final int TOKEN_NOT_EQ = 5;
    public static final int TOKEN_RBRACE = 6;
    public static final int TOKEN_LBRACE = 7;
    public static final int TOKEN_GE = 8;
    public static final int TOKEN_LE = 9;
    public static final int TOKEN_GT = 10;
    public static final int TOKEN_LT = 11;
    public static final int TOKEN_END = 12;
    private final char[] expr;
    private String tokenVal = null;
    private int index;
    private final int length;
    public ExpressionTokenizer ( String expr ) {
        this.expr = expr.trim().toCharArray();
        this.length = this.expr.length;
    }
    public boolean hasMoreTokens() {
        return index < length;
    }
    public int getIndex() {
        return index;
    }
    protected boolean isMetaChar ( char c ) {
        return Character.isWhitespace ( c ) || c == '(' || c == ')' || c == '!'
               || c == '<' || c == '>' || c == '|' || c == '&' || c == '=';
    }
    public int nextToken() {
        while ( index < length && Character.isWhitespace ( expr[index] ) ) {
            index++;
        }
        tokenVal = null;
        if ( index == length ) {
            return TOKEN_END;
        }
        int start = index;
        char currentChar = expr[index];
        char nextChar = ( char ) 0;
        index++;
        if ( index < length ) {
            nextChar = expr[index];
        }
        switch ( currentChar ) {
        case '(' :
            return TOKEN_LBRACE;
        case ')' :
            return TOKEN_RBRACE;
        case '=' :
            return TOKEN_EQ;
        case '!' :
            if ( nextChar == '=' ) {
                index++;
                return TOKEN_NOT_EQ;
            }
            return TOKEN_NOT;
        case '|' :
            if ( nextChar == '|' ) {
                index++;
                return TOKEN_OR;
            }
            break;
        case '&' :
            if ( nextChar == '&' ) {
                index++;
                return TOKEN_AND;
            }
            break;
        case '>' :
            if ( nextChar == '=' ) {
                index++;
                return TOKEN_GE;
            }
            return TOKEN_GT;
        case '<' :
            if ( nextChar == '=' ) {
                index++;
                return TOKEN_LE;
            }
            return TOKEN_LT;
        default :
            break;
        }
        int end = index;
        if ( currentChar == '"' || currentChar == '\'' ) {
            char endChar = currentChar;
            boolean escaped = false;
            start++;
            for ( ; index < length; index++ ) {
                if ( expr[index] == '\\' && !escaped ) {
                    escaped = true;
                    continue;
                }
                if ( expr[index] == endChar && !escaped ) {
                    break;
                }
                escaped = false;
            }
            end = index;
            index++;
        } else if ( currentChar == '/' ) {
            char endChar = currentChar;
            boolean escaped = false;
            for ( ; index < length; index++ ) {
                if ( expr[index] == '\\' && !escaped ) {
                    escaped = true;
                    continue;
                }
                if ( expr[index] == endChar && !escaped ) {
                    break;
                }
                escaped = false;
            }
            end = ++index;
        } else {
            for ( ; index < length; index++ ) {
                if ( isMetaChar ( expr[index] ) ) {
                    break;
                }
            }
            end = index;
        }
        this.tokenVal = new String ( expr, start, end - start );
        return TOKEN_STRING;
    }
    public String getTokenValue() {
        return tokenVal;
    }
}
