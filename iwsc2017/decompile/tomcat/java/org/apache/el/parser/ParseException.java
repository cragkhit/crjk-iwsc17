package org.apache.el.parser;
public class ParseException extends Exception {
    private static final long serialVersionUID = 1L;
    public Token currentToken;
    public int[][] expectedTokenSequences;
    public String[] tokenImage;
    public ParseException ( final Token currentTokenVal, final int[][] expectedTokenSequencesVal, final String[] tokenImageVal ) {
        super ( initialise ( currentTokenVal, expectedTokenSequencesVal, tokenImageVal ) );
        this.currentToken = currentTokenVal;
        this.expectedTokenSequences = expectedTokenSequencesVal;
        this.tokenImage = tokenImageVal;
    }
    public ParseException() {
    }
    public ParseException ( final String message ) {
        super ( message );
    }
    private static String initialise ( final Token currentToken, final int[][] expectedTokenSequences, final String[] tokenImage ) {
        final StringBuffer expected = new StringBuffer();
        int maxSize = 0;
        for ( int i = 0; i < expectedTokenSequences.length; ++i ) {
            if ( maxSize < expectedTokenSequences[i].length ) {
                maxSize = expectedTokenSequences[i].length;
            }
            for ( int j = 0; j < expectedTokenSequences[i].length; ++j ) {
                expected.append ( tokenImage[expectedTokenSequences[i][j]] ).append ( ' ' );
            }
            if ( expectedTokenSequences[i][expectedTokenSequences[i].length - 1] != 0 ) {
                expected.append ( "..." );
            }
            expected.append ( System.lineSeparator() ).append ( "    " );
        }
        String retval = "Encountered \"";
        Token tok = currentToken.next;
        for ( int k = 0; k < maxSize; ++k ) {
            if ( k != 0 ) {
                retval += " ";
            }
            if ( tok.kind == 0 ) {
                retval += tokenImage[0];
                break;
            }
            retval = retval + " " + tokenImage[tok.kind];
            retval += " \"";
            retval += add_escapes ( tok.image );
            retval += " \"";
            tok = tok.next;
        }
        retval = retval + "\" at line " + currentToken.next.beginLine + ", column " + currentToken.next.beginColumn;
        retval = retval + "." + System.lineSeparator();
        if ( expectedTokenSequences.length == 1 ) {
            retval = retval + "Was expecting:" + System.lineSeparator() + "    ";
        } else {
            retval = retval + "Was expecting one of:" + System.lineSeparator() + "    ";
        }
        retval += expected.toString();
        return retval;
    }
    static String add_escapes ( final String str ) {
        final StringBuffer retval = new StringBuffer();
        for ( int i = 0; i < str.length(); ++i ) {
            switch ( str.charAt ( i ) ) {
            case '\0': {
                break;
            }
            case '\b': {
                retval.append ( "\\b" );
                break;
            }
            case '\t': {
                retval.append ( "\\t" );
                break;
            }
            case '\n': {
                retval.append ( "\\n" );
                break;
            }
            case '\f': {
                retval.append ( "\\f" );
                break;
            }
            case '\r': {
                retval.append ( "\\r" );
                break;
            }
            case '\"': {
                retval.append ( "\\\"" );
                break;
            }
            case '\'': {
                retval.append ( "\\'" );
                break;
            }
            case '\\': {
                retval.append ( "\\\\" );
                break;
            }
            default: {
                final char ch;
                if ( ( ch = str.charAt ( i ) ) < ' ' || ch > '~' ) {
                    final String s = "0000" + Integer.toString ( ch, 16 );
                    retval.append ( "\\u" + s.substring ( s.length() - 4, s.length() ) );
                    break;
                }
                retval.append ( ch );
                break;
            }
            }
        }
        return retval.toString();
    }
}
