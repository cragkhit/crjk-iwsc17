package org.apache.jasper.compiler;
import java.io.CharArrayWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.jasper.JasperException;
import org.apache.jasper.JspCompilationContext;
import org.apache.jasper.runtime.ExceptionUtils;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.Jar;
class JspReader {
    private final Log log = LogFactory.getLog ( JspReader.class );
    private Mark current;
    private final JspCompilationContext context;
    private final ErrorDispatcher err;
    public JspReader ( JspCompilationContext ctxt,
                       String fname,
                       String encoding,
                       Jar jar,
                       ErrorDispatcher err )
    throws JasperException, FileNotFoundException, IOException {
        this ( ctxt, fname, JspUtil.getReader ( fname, encoding, jar, ctxt, err ),
               err );
    }
    public JspReader ( JspCompilationContext ctxt,
                       String fname,
                       InputStreamReader reader,
                       ErrorDispatcher err )
    throws JasperException {
        this.context = ctxt;
        this.err = err;
        try {
            CharArrayWriter caw = new CharArrayWriter();
            char buf[] = new char[1024];
            for ( int i = 0 ; ( i = reader.read ( buf ) ) != -1 ; ) {
                caw.write ( buf, 0, i );
            }
            caw.close();
            current = new Mark ( this, caw.toCharArray(), fname );
        } catch ( Throwable ex ) {
            ExceptionUtils.handleThrowable ( ex );
            log.error ( "Exception parsing file ", ex );
            err.jspError ( "jsp.error.file.cannot.read", fname );
        } finally {
            if ( reader != null ) {
                try {
                    reader.close();
                } catch ( Exception any ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( "Exception closing reader: ", any );
                    }
                }
            }
        }
    }
    JspCompilationContext getJspCompilationContext() {
        return context;
    }
    boolean hasMoreInput() {
        return current.cursor < current.stream.length;
    }
    int nextChar() {
        if ( !hasMoreInput() ) {
            return -1;
        }
        int ch = current.stream[current.cursor];
        current.cursor++;
        if ( ch == '\n' ) {
            current.line++;
            current.col = 0;
        } else {
            current.col++;
        }
        return ch;
    }
    private int nextChar ( Mark mark ) {
        if ( !hasMoreInput() ) {
            return -1;
        }
        int ch = current.stream[current.cursor];
        mark.init ( current, true );
        current.cursor++;
        if ( ch == '\n' ) {
            current.line++;
            current.col = 0;
        } else {
            current.col++;
        }
        return ch;
    }
    private Boolean indexOf ( char c, Mark mark ) {
        if ( !hasMoreInput() ) {
            return null;
        }
        int end = current.stream.length;
        int ch;
        int line = current.line;
        int col = current.col;
        int i = current.cursor;
        for ( ; i < end; i ++ ) {
            ch = current.stream[i];
            if ( ch == c ) {
                mark.update ( i, line, col );
            }
            if ( ch == '\n' ) {
                line++;
                col = 0;
            } else {
                col++;
            }
            if ( ch == c ) {
                current.update ( i + 1, line, col );
                return Boolean.TRUE;
            }
        }
        current.update ( i, line, col );
        return Boolean.FALSE;
    }
    void pushChar() {
        current.cursor--;
        current.col--;
    }
    String getText ( Mark start, Mark stop ) {
        Mark oldstart = mark();
        reset ( start );
        CharArrayWriter caw = new CharArrayWriter();
        while ( !markEquals ( stop ) ) {
            caw.write ( nextChar() );
        }
        caw.close();
        setCurrent ( oldstart );
        return caw.toString();
    }
    int peekChar() {
        return peekChar ( 0 );
    }
    int peekChar ( int readAhead ) {
        int target = current.cursor + readAhead;
        if ( target < current.stream.length ) {
            return current.stream[target];
        }
        return -1;
    }
    Mark mark() {
        return new Mark ( current );
    }
    private boolean markEquals ( Mark another ) {
        return another.equals ( current );
    }
    void reset ( Mark mark ) {
        current = new Mark ( mark );
    }
    private void setCurrent ( Mark mark ) {
        current = mark;
    }
    boolean matches ( String string ) {
        int len = string.length();
        int cursor = current.cursor;
        int streamSize = current.stream.length;
        if ( cursor + len < streamSize ) {
            int line = current.line;
            int col = current.col;
            int ch;
            int i = 0;
            for ( ; i < len; i ++ ) {
                ch = current.stream[i + cursor];
                if ( string.charAt ( i ) != ch ) {
                    return false;
                }
                if ( ch == '\n' ) {
                    line ++;
                    col = 0;
                } else {
                    col++;
                }
            }
            current.update ( i + cursor, line, col );
        } else {
            Mark mark = mark();
            int ch = 0;
            int i = 0;
            do {
                ch = nextChar();
                if ( ( ( char ) ch ) != string.charAt ( i++ ) ) {
                    setCurrent ( mark );
                    return false;
                }
            } while ( i < len );
        }
        return true;
    }
    boolean matchesETag ( String tagName ) {
        Mark mark = mark();
        if ( !matches ( "</" + tagName ) ) {
            return false;
        }
        skipSpaces();
        if ( nextChar() == '>' ) {
            return true;
        }
        setCurrent ( mark );
        return false;
    }
    boolean matchesETagWithoutLessThan ( String tagName ) {
        Mark mark = mark();
        if ( !matches ( "/" + tagName ) ) {
            return false;
        }
        skipSpaces();
        if ( nextChar() == '>' ) {
            return true;
        }
        setCurrent ( mark );
        return false;
    }
    boolean matchesOptionalSpacesFollowedBy ( String s ) {
        Mark mark = mark();
        skipSpaces();
        boolean result = matches ( s );
        if ( !result ) {
            setCurrent ( mark );
        }
        return result;
    }
    int skipSpaces() {
        int i = 0;
        while ( hasMoreInput() && isSpace() ) {
            i++;
            nextChar();
        }
        return i;
    }
    Mark skipUntil ( String limit ) {
        Mark ret = mark();
        int limlen = limit.length();
        char firstChar = limit.charAt ( 0 );
        Boolean result = null;
        Mark restart = null;
        skip:
        while ( ( result = indexOf ( firstChar, ret ) ) != null ) {
            if ( result.booleanValue() ) {
                if ( restart != null ) {
                    restart.init ( current, true );
                } else {
                    restart = mark();
                }
                for ( int i = 1 ; i < limlen ; i++ ) {
                    if ( peekChar() == limit.charAt ( i ) ) {
                        nextChar();
                    } else {
                        current.init ( restart, true );
                        continue skip;
                    }
                }
                return ret;
            }
        }
        return null;
    }
    Mark skipUntilIgnoreEsc ( String limit, boolean ignoreEL ) {
        Mark ret = mark();
        int limlen = limit.length();
        int ch;
        int prev = 'x';
        char firstChar = limit.charAt ( 0 );
        skip:
        for ( ch = nextChar ( ret ) ; ch != -1 ; prev = ch, ch = nextChar ( ret ) ) {
            if ( ch == '\\' && prev == '\\' ) {
                ch = 0;
            } else if ( prev == '\\' ) {
                continue;
            } else if ( !ignoreEL && ( ch == '$' || ch == '#' ) && peekChar() == '{' ) {
                nextChar();
                skipELExpression();
            } else if ( ch == firstChar ) {
                for ( int i = 1 ; i < limlen ; i++ ) {
                    if ( peekChar() == limit.charAt ( i ) ) {
                        nextChar();
                    } else {
                        continue skip;
                    }
                }
                return ret;
            }
        }
        return null;
    }
    Mark skipUntilETag ( String tag ) {
        Mark ret = skipUntil ( "</" + tag );
        if ( ret != null ) {
            skipSpaces();
            if ( nextChar() != '>' ) {
                ret = null;
            }
        }
        return ret;
    }
    Mark skipELExpression() {
        Mark last = mark();
        boolean singleQuoted = false;
        boolean doubleQuoted = false;
        int nesting = 0;
        int currentChar;
        do {
            currentChar = nextChar ( last );
            while ( currentChar == '\\' && ( singleQuoted || doubleQuoted ) ) {
                nextChar();
                currentChar = nextChar();
            }
            if ( currentChar == -1 ) {
                return null;
            }
            if ( currentChar == '"' && !singleQuoted ) {
                doubleQuoted = !doubleQuoted;
            } else if ( currentChar == '\'' && !doubleQuoted ) {
                singleQuoted = !singleQuoted;
            } else if ( currentChar == '{' && !doubleQuoted && !singleQuoted ) {
                nesting++;
            } else if ( currentChar == '}' && !doubleQuoted && !singleQuoted ) {
                nesting--;
            }
        } while ( currentChar != '}' || singleQuoted || doubleQuoted || nesting > -1 );
        return last;
    }
    final boolean isSpace() {
        return peekChar() <= ' ';
    }
    String parseToken ( boolean quoted ) throws JasperException {
        StringBuilder StringBuilder = new StringBuilder();
        skipSpaces();
        StringBuilder.setLength ( 0 );
        if ( !hasMoreInput() ) {
            return "";
        }
        int ch = peekChar();
        if ( quoted ) {
            if ( ch == '"' || ch == '\'' ) {
                char endQuote = ch == '"' ? '"' : '\'';
                ch = nextChar();
                for ( ch = nextChar(); ch != -1 && ch != endQuote;
                        ch = nextChar() ) {
                    if ( ch == '\\' ) {
                        ch = nextChar();
                    }
                    StringBuilder.append ( ( char ) ch );
                }
                if ( ch == -1 ) {
                    err.jspError ( mark(), "jsp.error.quotes.unterminated" );
                }
            } else {
                err.jspError ( mark(), "jsp.error.attr.quoted" );
            }
        } else {
            if ( !isDelimiter() ) {
                do {
                    ch = nextChar();
                    if ( ch == '\\' ) {
                        if ( peekChar() == '"' || peekChar() == '\'' ||
                                peekChar() == '>' || peekChar() == '%' ) {
                            ch = nextChar();
                        }
                    }
                    StringBuilder.append ( ( char ) ch );
                } while ( !isDelimiter() );
            }
        }
        return StringBuilder.toString();
    }
    private boolean isDelimiter() {
        if ( ! isSpace() ) {
            int ch = peekChar();
            if ( ch == '=' || ch == '>' || ch == '"' || ch == '\''
                    || ch == '/' ) {
                return true;
            }
            if ( ch == '-' ) {
                Mark mark = mark();
                if ( ( ( ch = nextChar() ) == '>' )
                        || ( ( ch == '-' ) && ( nextChar() == '>' ) ) ) {
                    setCurrent ( mark );
                    return true;
                } else {
                    setCurrent ( mark );
                    return false;
                }
            }
            return false;
        } else {
            return true;
        }
    }
}
