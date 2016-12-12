package org.apache.jasper.runtime;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import org.apache.jasper.Constants;
public class BodyContentImpl extends BodyContent {
    private static final boolean LIMIT_BUFFER =
        Boolean.parseBoolean ( System.getProperty ( "org.apache.jasper.runtime.BodyContentImpl.LIMIT_BUFFER", "false" ) );
    private char[] cb;
    private int nextChar;
    private boolean closed;
    private Writer writer;
    public BodyContentImpl ( JspWriter enclosingWriter ) {
        super ( enclosingWriter );
        cb = new char[Constants.DEFAULT_TAG_BUFFER_SIZE];
        bufferSize = cb.length;
        nextChar = 0;
        closed = false;
    }
    @Override
    public void write ( int c ) throws IOException {
        if ( writer != null ) {
            writer.write ( c );
        } else {
            ensureOpen();
            if ( nextChar >= bufferSize ) {
                reAllocBuff ( 1 );
            }
            cb[nextChar++] = ( char ) c;
        }
    }
    @Override
    public void write ( char[] cbuf, int off, int len ) throws IOException {
        if ( writer != null ) {
            writer.write ( cbuf, off, len );
        } else {
            ensureOpen();
            if ( ( off < 0 ) || ( off > cbuf.length ) || ( len < 0 ) ||
                    ( ( off + len ) > cbuf.length ) || ( ( off + len ) < 0 ) ) {
                throw new IndexOutOfBoundsException();
            } else if ( len == 0 ) {
                return;
            }
            if ( len >= bufferSize - nextChar ) {
                reAllocBuff ( len );
            }
            System.arraycopy ( cbuf, off, cb, nextChar, len );
            nextChar += len;
        }
    }
    @Override
    public void write ( char[] buf ) throws IOException {
        if ( writer != null ) {
            writer.write ( buf );
        } else {
            write ( buf, 0, buf.length );
        }
    }
    @Override
    public void write ( String s, int off, int len ) throws IOException {
        if ( writer != null ) {
            writer.write ( s, off, len );
        } else {
            ensureOpen();
            if ( len >= bufferSize - nextChar ) {
                reAllocBuff ( len );
            }
            s.getChars ( off, off + len, cb, nextChar );
            nextChar += len;
        }
    }
    @Override
    public void write ( String s ) throws IOException {
        if ( writer != null ) {
            writer.write ( s );
        } else {
            write ( s, 0, s.length() );
        }
    }
    @Override
    public void newLine() throws IOException {
        if ( writer != null ) {
            writer.write ( System.lineSeparator() );
        } else {
            write ( System.lineSeparator() );
        }
    }
    @Override
    public void print ( boolean b ) throws IOException {
        if ( writer != null ) {
            writer.write ( b ? "true" : "false" );
        } else {
            write ( b ? "true" : "false" );
        }
    }
    @Override
    public void print ( char c ) throws IOException {
        if ( writer != null ) {
            writer.write ( String.valueOf ( c ) );
        } else {
            write ( String.valueOf ( c ) );
        }
    }
    @Override
    public void print ( int i ) throws IOException {
        if ( writer != null ) {
            writer.write ( String.valueOf ( i ) );
        } else {
            write ( String.valueOf ( i ) );
        }
    }
    @Override
    public void print ( long l ) throws IOException {
        if ( writer != null ) {
            writer.write ( String.valueOf ( l ) );
        } else {
            write ( String.valueOf ( l ) );
        }
    }
    @Override
    public void print ( float f ) throws IOException {
        if ( writer != null ) {
            writer.write ( String.valueOf ( f ) );
        } else {
            write ( String.valueOf ( f ) );
        }
    }
    @Override
    public void print ( double d ) throws IOException {
        if ( writer != null ) {
            writer.write ( String.valueOf ( d ) );
        } else {
            write ( String.valueOf ( d ) );
        }
    }
    @Override
    public void print ( char[] s ) throws IOException {
        if ( writer != null ) {
            writer.write ( s );
        } else {
            write ( s );
        }
    }
    @Override
    public void print ( String s ) throws IOException {
        if ( s == null ) {
            s = "null";
        }
        if ( writer != null ) {
            writer.write ( s );
        } else {
            write ( s );
        }
    }
    @Override
    public void print ( Object obj ) throws IOException {
        if ( writer != null ) {
            writer.write ( String.valueOf ( obj ) );
        } else {
            write ( String.valueOf ( obj ) );
        }
    }
    @Override
    public void println() throws IOException {
        newLine();
    }
    @Override
    public void println ( boolean x ) throws IOException {
        print ( x );
        println();
    }
    @Override
    public void println ( char x ) throws IOException {
        print ( x );
        println();
    }
    @Override
    public void println ( int x ) throws IOException {
        print ( x );
        println();
    }
    @Override
    public void println ( long x ) throws IOException {
        print ( x );
        println();
    }
    @Override
    public void println ( float x ) throws IOException {
        print ( x );
        println();
    }
    @Override
    public void println ( double x ) throws IOException {
        print ( x );
        println();
    }
    @Override
    public void println ( char x[] ) throws IOException {
        print ( x );
        println();
    }
    @Override
    public void println ( String x ) throws IOException {
        print ( x );
        println();
    }
    @Override
    public void println ( Object x ) throws IOException {
        print ( x );
        println();
    }
    @Override
    public void clear() throws IOException {
        if ( writer != null ) {
            throw new IOException();
        } else {
            nextChar = 0;
            if ( LIMIT_BUFFER && ( cb.length > Constants.DEFAULT_TAG_BUFFER_SIZE ) ) {
                cb = new char[Constants.DEFAULT_TAG_BUFFER_SIZE];
                bufferSize = cb.length;
            }
        }
    }
    @Override
    public void clearBuffer() throws IOException {
        if ( writer == null ) {
            this.clear();
        }
    }
    @Override
    public void close() throws IOException {
        if ( writer != null ) {
            writer.close();
        } else {
            closed = true;
        }
    }
    @Override
    public int getBufferSize() {
        return ( writer == null ) ? bufferSize : 0;
    }
    @Override
    public int getRemaining() {
        return ( writer == null ) ? bufferSize - nextChar : 0;
    }
    @Override
    public Reader getReader() {
        return ( writer == null ) ? new CharArrayReader ( cb, 0, nextChar ) : null;
    }
    @Override
    public String getString() {
        return ( writer == null ) ? new String ( cb, 0, nextChar ) : null;
    }
    @Override
    public void writeOut ( Writer out ) throws IOException {
        if ( writer == null ) {
            out.write ( cb, 0, nextChar );
        }
    }
    void setWriter ( Writer writer ) {
        this.writer = writer;
        closed = false;
        if ( writer == null ) {
            clearBody();
        }
    }
    protected void recycle() {
        this.writer = null;
        try {
            this.clear();
        } catch ( IOException ex ) {
        }
    }
    private void ensureOpen() throws IOException {
        if ( closed ) {
            throw new IOException ( "Stream closed" );
        }
    }
    private void reAllocBuff ( int len ) {
        if ( bufferSize + len <= cb.length ) {
            bufferSize = cb.length;
            return;
        }
        if ( len < cb.length ) {
            len = cb.length;
        }
        char[] tmp = new char[cb.length + len];
        System.arraycopy ( cb, 0, tmp, 0, cb.length );
        cb = tmp;
        bufferSize = cb.length;
    }
}
