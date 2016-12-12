package org.apache.jasper.runtime;
import java.io.CharArrayReader;
import java.io.Reader;
import java.io.IOException;
import javax.servlet.jsp.JspWriter;
import java.io.Writer;
import javax.servlet.jsp.tagext.BodyContent;
public class BodyContentImpl extends BodyContent {
    private static final boolean LIMIT_BUFFER;
    private char[] cb;
    private int nextChar;
    private boolean closed;
    private Writer writer;
    public BodyContentImpl ( final JspWriter enclosingWriter ) {
        super ( enclosingWriter );
        this.cb = new char[512];
        this.bufferSize = this.cb.length;
        this.nextChar = 0;
        this.closed = false;
    }
    public void write ( final int c ) throws IOException {
        if ( this.writer != null ) {
            this.writer.write ( c );
        } else {
            this.ensureOpen();
            if ( this.nextChar >= this.bufferSize ) {
                this.reAllocBuff ( 1 );
            }
            this.cb[this.nextChar++] = ( char ) c;
        }
    }
    public void write ( final char[] cbuf, final int off, final int len ) throws IOException {
        if ( this.writer != null ) {
            this.writer.write ( cbuf, off, len );
        } else {
            this.ensureOpen();
            if ( off < 0 || off > cbuf.length || len < 0 || off + len > cbuf.length || off + len < 0 ) {
                throw new IndexOutOfBoundsException();
            }
            if ( len == 0 ) {
                return;
            }
            if ( len >= this.bufferSize - this.nextChar ) {
                this.reAllocBuff ( len );
            }
            System.arraycopy ( cbuf, off, this.cb, this.nextChar, len );
            this.nextChar += len;
        }
    }
    public void write ( final char[] buf ) throws IOException {
        if ( this.writer != null ) {
            this.writer.write ( buf );
        } else {
            this.write ( buf, 0, buf.length );
        }
    }
    public void write ( final String s, final int off, final int len ) throws IOException {
        if ( this.writer != null ) {
            this.writer.write ( s, off, len );
        } else {
            this.ensureOpen();
            if ( len >= this.bufferSize - this.nextChar ) {
                this.reAllocBuff ( len );
            }
            s.getChars ( off, off + len, this.cb, this.nextChar );
            this.nextChar += len;
        }
    }
    public void write ( final String s ) throws IOException {
        if ( this.writer != null ) {
            this.writer.write ( s );
        } else {
            this.write ( s, 0, s.length() );
        }
    }
    public void newLine() throws IOException {
        if ( this.writer != null ) {
            this.writer.write ( System.lineSeparator() );
        } else {
            this.write ( System.lineSeparator() );
        }
    }
    public void print ( final boolean b ) throws IOException {
        if ( this.writer != null ) {
            this.writer.write ( b ? "true" : "false" );
        } else {
            this.write ( b ? "true" : "false" );
        }
    }
    public void print ( final char c ) throws IOException {
        if ( this.writer != null ) {
            this.writer.write ( String.valueOf ( c ) );
        } else {
            this.write ( String.valueOf ( c ) );
        }
    }
    public void print ( final int i ) throws IOException {
        if ( this.writer != null ) {
            this.writer.write ( String.valueOf ( i ) );
        } else {
            this.write ( String.valueOf ( i ) );
        }
    }
    public void print ( final long l ) throws IOException {
        if ( this.writer != null ) {
            this.writer.write ( String.valueOf ( l ) );
        } else {
            this.write ( String.valueOf ( l ) );
        }
    }
    public void print ( final float f ) throws IOException {
        if ( this.writer != null ) {
            this.writer.write ( String.valueOf ( f ) );
        } else {
            this.write ( String.valueOf ( f ) );
        }
    }
    public void print ( final double d ) throws IOException {
        if ( this.writer != null ) {
            this.writer.write ( String.valueOf ( d ) );
        } else {
            this.write ( String.valueOf ( d ) );
        }
    }
    public void print ( final char[] s ) throws IOException {
        if ( this.writer != null ) {
            this.writer.write ( s );
        } else {
            this.write ( s );
        }
    }
    public void print ( String s ) throws IOException {
        if ( s == null ) {
            s = "null";
        }
        if ( this.writer != null ) {
            this.writer.write ( s );
        } else {
            this.write ( s );
        }
    }
    public void print ( final Object obj ) throws IOException {
        if ( this.writer != null ) {
            this.writer.write ( String.valueOf ( obj ) );
        } else {
            this.write ( String.valueOf ( obj ) );
        }
    }
    public void println() throws IOException {
        this.newLine();
    }
    public void println ( final boolean x ) throws IOException {
        this.print ( x );
        this.println();
    }
    public void println ( final char x ) throws IOException {
        this.print ( x );
        this.println();
    }
    public void println ( final int x ) throws IOException {
        this.print ( x );
        this.println();
    }
    public void println ( final long x ) throws IOException {
        this.print ( x );
        this.println();
    }
    public void println ( final float x ) throws IOException {
        this.print ( x );
        this.println();
    }
    public void println ( final double x ) throws IOException {
        this.print ( x );
        this.println();
    }
    public void println ( final char[] x ) throws IOException {
        this.print ( x );
        this.println();
    }
    public void println ( final String x ) throws IOException {
        this.print ( x );
        this.println();
    }
    public void println ( final Object x ) throws IOException {
        this.print ( x );
        this.println();
    }
    public void clear() throws IOException {
        if ( this.writer != null ) {
            throw new IOException();
        }
        this.nextChar = 0;
        if ( BodyContentImpl.LIMIT_BUFFER && this.cb.length > 512 ) {
            this.cb = new char[512];
            this.bufferSize = this.cb.length;
        }
    }
    public void clearBuffer() throws IOException {
        if ( this.writer == null ) {
            this.clear();
        }
    }
    public void close() throws IOException {
        if ( this.writer != null ) {
            this.writer.close();
        } else {
            this.closed = true;
        }
    }
    public int getBufferSize() {
        return ( this.writer == null ) ? this.bufferSize : 0;
    }
    public int getRemaining() {
        return ( this.writer == null ) ? ( this.bufferSize - this.nextChar ) : 0;
    }
    public Reader getReader() {
        return ( this.writer == null ) ? new CharArrayReader ( this.cb, 0, this.nextChar ) : null;
    }
    public String getString() {
        return ( this.writer == null ) ? new String ( this.cb, 0, this.nextChar ) : null;
    }
    public void writeOut ( final Writer out ) throws IOException {
        if ( this.writer == null ) {
            out.write ( this.cb, 0, this.nextChar );
        }
    }
    void setWriter ( final Writer writer ) {
        this.writer = writer;
        this.closed = false;
        if ( writer == null ) {
            this.clearBody();
        }
    }
    protected void recycle() {
        this.writer = null;
        try {
            this.clear();
        } catch ( IOException ex ) {}
    }
    private void ensureOpen() throws IOException {
        if ( this.closed ) {
            throw new IOException ( "Stream closed" );
        }
    }
    private void reAllocBuff ( int len ) {
        if ( this.bufferSize + len <= this.cb.length ) {
            this.bufferSize = this.cb.length;
            return;
        }
        if ( len < this.cb.length ) {
            len = this.cb.length;
        }
        final char[] tmp = new char[this.cb.length + len];
        System.arraycopy ( this.cb, 0, tmp, 0, this.cb.length );
        this.cb = tmp;
        this.bufferSize = this.cb.length;
    }
    static {
        LIMIT_BUFFER = Boolean.parseBoolean ( System.getProperty ( "org.apache.jasper.runtime.BodyContentImpl.LIMIT_BUFFER", "false" ) );
    }
}
