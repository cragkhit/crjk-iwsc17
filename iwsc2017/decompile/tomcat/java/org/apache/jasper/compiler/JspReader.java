package org.apache.jasper.compiler;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.apache.jasper.runtime.ExceptionUtils;
import java.io.CharArrayWriter;
import org.apache.juli.logging.LogFactory;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import org.apache.jasper.JasperException;
import org.apache.tomcat.Jar;
import org.apache.jasper.JspCompilationContext;
import org.apache.juli.logging.Log;
class JspReader {
    private final Log log;
    private Mark current;
    private final JspCompilationContext context;
    private final ErrorDispatcher err;
    public JspReader ( final JspCompilationContext ctxt, final String fname, final String encoding, final Jar jar, final ErrorDispatcher err ) throws JasperException, FileNotFoundException, IOException {
        this ( ctxt, fname, JspUtil.getReader ( fname, encoding, jar, ctxt, err ), err );
    }
    public JspReader ( final JspCompilationContext ctxt, final String fname, final InputStreamReader reader, final ErrorDispatcher err ) throws JasperException {
        this.log = LogFactory.getLog ( JspReader.class );
        this.context = ctxt;
        this.err = err;
        try {
            final CharArrayWriter caw = new CharArrayWriter();
            final char[] buf = new char[1024];
            int i = 0;
            while ( ( i = reader.read ( buf ) ) != -1 ) {
                caw.write ( buf, 0, i );
            }
            caw.close();
            this.current = new Mark ( this, caw.toCharArray(), fname );
        } catch ( Throwable ex ) {
            ExceptionUtils.handleThrowable ( ex );
            this.log.error ( "Exception parsing file ", ex );
            err.jspError ( "jsp.error.file.cannot.read", fname );
            if ( reader != null ) {
                try {
                    reader.close();
                } catch ( Exception any ) {
                    if ( this.log.isDebugEnabled() ) {
                        this.log.debug ( "Exception closing reader: ", any );
                    }
                }
            }
        } finally {
            if ( reader != null ) {
                try {
                    reader.close();
                } catch ( Exception any2 ) {
                    if ( this.log.isDebugEnabled() ) {
                        this.log.debug ( "Exception closing reader: ", any2 );
                    }
                }
            }
        }
    }
    JspCompilationContext getJspCompilationContext() {
        return this.context;
    }
    boolean hasMoreInput() {
        return this.current.cursor < this.current.stream.length;
    }
    int nextChar() {
        if ( !this.hasMoreInput() ) {
            return -1;
        }
        final int ch = this.current.stream[this.current.cursor];
        final Mark current = this.current;
        ++current.cursor;
        if ( ch == 10 ) {
            final Mark current2 = this.current;
            ++current2.line;
            this.current.col = 0;
        } else {
            final Mark current3 = this.current;
            ++current3.col;
        }
        return ch;
    }
    private int nextChar ( final Mark mark ) {
        if ( !this.hasMoreInput() ) {
            return -1;
        }
        final int ch = this.current.stream[this.current.cursor];
        mark.init ( this.current, true );
        final Mark current = this.current;
        ++current.cursor;
        if ( ch == 10 ) {
            final Mark current2 = this.current;
            ++current2.line;
            this.current.col = 0;
        } else {
            final Mark current3 = this.current;
            ++current3.col;
        }
        return ch;
    }
    private Boolean indexOf ( final char c, final Mark mark ) {
        if ( !this.hasMoreInput() ) {
            return null;
        }
        final int end = this.current.stream.length;
        int line = this.current.line;
        int col = this.current.col;
        int i;
        for ( i = this.current.cursor; i < end; ++i ) {
            final int ch = this.current.stream[i];
            if ( ch == c ) {
                mark.update ( i, line, col );
            }
            if ( ch == 10 ) {
                ++line;
                col = 0;
            } else {
                ++col;
            }
            if ( ch == c ) {
                this.current.update ( i + 1, line, col );
                return Boolean.TRUE;
            }
        }
        this.current.update ( i, line, col );
        return Boolean.FALSE;
    }
    void pushChar() {
        final Mark current = this.current;
        --current.cursor;
        final Mark current2 = this.current;
        --current2.col;
    }
    String getText ( final Mark start, final Mark stop ) {
        final Mark oldstart = this.mark();
        this.reset ( start );
        final CharArrayWriter caw = new CharArrayWriter();
        while ( !this.markEquals ( stop ) ) {
            caw.write ( this.nextChar() );
        }
        caw.close();
        this.setCurrent ( oldstart );
        return caw.toString();
    }
    int peekChar() {
        return this.peekChar ( 0 );
    }
    int peekChar ( final int readAhead ) {
        final int target = this.current.cursor + readAhead;
        if ( target < this.current.stream.length ) {
            return this.current.stream[target];
        }
        return -1;
    }
    Mark mark() {
        return new Mark ( this.current );
    }
    private boolean markEquals ( final Mark another ) {
        return another.equals ( this.current );
    }
    void reset ( final Mark mark ) {
        this.current = new Mark ( mark );
    }
    private void setCurrent ( final Mark mark ) {
        this.current = mark;
    }
    boolean matches ( final String string ) {
        final int len = string.length();
        final int cursor = this.current.cursor;
        final int streamSize = this.current.stream.length;
        if ( cursor + len < streamSize ) {
            int line = this.current.line;
            int col = this.current.col;
            int i;
            for ( i = 0; i < len; ++i ) {
                final int ch = this.current.stream[i + cursor];
                if ( string.charAt ( i ) != ch ) {
                    return false;
                }
                if ( ch == 10 ) {
                    ++line;
                    col = 0;
                } else {
                    ++col;
                }
            }
            this.current.update ( i + cursor, line, col );
        } else {
            final Mark mark = this.mark();
            int ch2 = 0;
            int j = 0;
            do {
                ch2 = this.nextChar();
                if ( ( char ) ch2 != string.charAt ( j++ ) ) {
                    this.setCurrent ( mark );
                    return false;
                }
            } while ( j < len );
        }
        return true;
    }
    boolean matchesETag ( final String tagName ) {
        final Mark mark = this.mark();
        if ( !this.matches ( "</" + tagName ) ) {
            return false;
        }
        this.skipSpaces();
        if ( this.nextChar() == 62 ) {
            return true;
        }
        this.setCurrent ( mark );
        return false;
    }
    boolean matchesETagWithoutLessThan ( final String tagName ) {
        final Mark mark = this.mark();
        if ( !this.matches ( "/" + tagName ) ) {
            return false;
        }
        this.skipSpaces();
        if ( this.nextChar() == 62 ) {
            return true;
        }
        this.setCurrent ( mark );
        return false;
    }
    boolean matchesOptionalSpacesFollowedBy ( final String s ) {
        final Mark mark = this.mark();
        this.skipSpaces();
        final boolean result = this.matches ( s );
        if ( !result ) {
            this.setCurrent ( mark );
        }
        return result;
    }
    int skipSpaces() {
        int i = 0;
        while ( this.hasMoreInput() && this.isSpace() ) {
            ++i;
            this.nextChar();
        }
        return i;
    }
    Mark skipUntil ( final String limit ) {
        final Mark ret = this.mark();
        final int limlen = limit.length();
        final char firstChar = limit.charAt ( 0 );
        Boolean result = null;
        Mark restart = null;
        Label_0023:
        while ( ( result = this.indexOf ( firstChar, ret ) ) != null ) {
            if ( result ) {
                if ( restart != null ) {
                    restart.init ( this.current, true );
                } else {
                    restart = this.mark();
                }
                for ( int i = 1; i < limlen; ++i ) {
                    if ( this.peekChar() != limit.charAt ( i ) ) {
                        this.current.init ( restart, true );
                        continue Label_0023;
                    }
                    this.nextChar();
                }
                return ret;
            }
        }
        return null;
    }
    Mark skipUntilIgnoreEsc ( final String limit, final boolean ignoreEL ) {
        final Mark ret = this.mark();
        final int limlen = limit.length();
        int prev = 120;
        final char firstChar = limit.charAt ( 0 );
        for ( int ch = this.nextChar ( ret ); ch != -1; ch = this.nextChar ( ret ) ) {
            Label_0148: {
                if ( ch == 92 && prev == 92 ) {
                    ch = 0;
                } else if ( prev != 92 ) {
                    if ( !ignoreEL && ( ch == 36 || ch == 35 ) && this.peekChar() == 123 ) {
                        this.nextChar();
                        this.skipELExpression();
                    } else if ( ch == firstChar ) {
                        for ( int i = 1; i < limlen; ++i ) {
                            if ( this.peekChar() != limit.charAt ( i ) ) {
                                break Label_0148;
                            }
                            this.nextChar();
                        }
                        return ret;
                    }
                }
            }
            prev = ch;
        }
        return null;
    }
    Mark skipUntilETag ( final String tag ) {
        Mark ret = this.skipUntil ( "</" + tag );
        if ( ret != null ) {
            this.skipSpaces();
            if ( this.nextChar() != 62 ) {
                ret = null;
            }
        }
        return ret;
    }
    Mark skipELExpression() {
        final Mark last = this.mark();
        boolean singleQuoted = false;
        boolean doubleQuoted = false;
        int nesting = 0;
        int currentChar;
        do {
            for ( currentChar = this.nextChar ( last ); currentChar == 92 && ( singleQuoted || doubleQuoted ); currentChar = this.nextChar() ) {
                this.nextChar();
            }
            if ( currentChar == -1 ) {
                return null;
            }
            if ( currentChar == 34 && !singleQuoted ) {
                doubleQuoted = !doubleQuoted;
            } else if ( currentChar == 39 && !doubleQuoted ) {
                singleQuoted = !singleQuoted;
            } else if ( currentChar == 123 && !doubleQuoted && !singleQuoted ) {
                ++nesting;
            } else {
                if ( currentChar != 125 || doubleQuoted || singleQuoted ) {
                    continue;
                }
                --nesting;
            }
        } while ( currentChar != 125 || singleQuoted || doubleQuoted || nesting > -1 );
        return last;
    }
    final boolean isSpace() {
        return this.peekChar() <= 32;
    }
    String parseToken ( final boolean quoted ) throws JasperException {
        final StringBuilder StringBuilder = new StringBuilder();
        this.skipSpaces();
        StringBuilder.setLength ( 0 );
        if ( !this.hasMoreInput() ) {
            return "";
        }
        int ch = this.peekChar();
        if ( quoted ) {
            if ( ch == 34 || ch == 39 ) {
                char endQuote;
                for ( endQuote = ( ( ch == 34 ) ? '\"' : '\'' ), ch = this.nextChar(), ch = this.nextChar(); ch != -1 && ch != endQuote; ch = this.nextChar() ) {
                    if ( ch == 92 ) {
                        ch = this.nextChar();
                    }
                    StringBuilder.append ( ( char ) ch );
                }
                if ( ch == -1 ) {
                    this.err.jspError ( this.mark(), "jsp.error.quotes.unterminated", new String[0] );
                }
            } else {
                this.err.jspError ( this.mark(), "jsp.error.attr.quoted", new String[0] );
            }
        } else if ( !this.isDelimiter() ) {
            do {
                ch = this.nextChar();
                if ( ch == 92 && ( this.peekChar() == 34 || this.peekChar() == 39 || this.peekChar() == 62 || this.peekChar() == 37 ) ) {
                    ch = this.nextChar();
                }
                StringBuilder.append ( ( char ) ch );
            } while ( !this.isDelimiter() );
        }
        return StringBuilder.toString();
    }
    private boolean isDelimiter() {
        if ( this.isSpace() ) {
            return true;
        }
        int ch = this.peekChar();
        if ( ch == 61 || ch == 62 || ch == 34 || ch == 39 || ch == 47 ) {
            return true;
        }
        if ( ch != 45 ) {
            return false;
        }
        final Mark mark = this.mark();
        if ( ( ch = this.nextChar() ) == 62 || ( ch == 45 && this.nextChar() == 62 ) ) {
            this.setCurrent ( mark );
            return true;
        }
        this.setCurrent ( mark );
        return false;
    }
}
