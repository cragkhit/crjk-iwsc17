package org.apache.jasper.runtime;
import java.security.AccessController;
import org.apache.jasper.compiler.Localizer;
import java.security.PrivilegedAction;
import org.apache.jasper.security.SecurityUtil;
import java.io.IOException;
import javax.servlet.ServletResponse;
import java.io.Writer;
import javax.servlet.jsp.JspWriter;
public class JspWriterImpl extends JspWriter {
    private Writer out;
    private ServletResponse response;
    private char[] cb;
    private int nextChar;
    private boolean flushed;
    private boolean closed;
    public JspWriterImpl() {
        super ( 8192, true );
        this.flushed = false;
        this.closed = false;
    }
    public JspWriterImpl ( final ServletResponse response, final int sz, final boolean autoFlush ) {
        super ( sz, autoFlush );
        this.flushed = false;
        this.closed = false;
        if ( sz < 0 ) {
            throw new IllegalArgumentException ( "Buffer size <= 0" );
        }
        this.response = response;
        this.cb = ( char[] ) ( ( sz == 0 ) ? null : new char[sz] );
        this.nextChar = 0;
    }
    void init ( final ServletResponse response, final int sz, final boolean autoFlush ) {
        this.response = response;
        if ( sz > 0 && ( this.cb == null || sz > this.cb.length ) ) {
            this.cb = new char[sz];
        }
        this.nextChar = 0;
        this.autoFlush = autoFlush;
        this.bufferSize = sz;
    }
    void recycle() {
        this.flushed = false;
        this.closed = false;
        this.out = null;
        this.nextChar = 0;
        this.response = null;
    }
    protected final void flushBuffer() throws IOException {
        if ( this.bufferSize == 0 ) {
            return;
        }
        this.flushed = true;
        this.ensureOpen();
        if ( this.nextChar == 0 ) {
            return;
        }
        this.initOut();
        this.out.write ( this.cb, 0, this.nextChar );
        this.nextChar = 0;
    }
    private void initOut() throws IOException {
        if ( this.out == null ) {
            this.out = this.response.getWriter();
        }
    }
    private String getLocalizeMessage ( final String message ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return AccessController.doPrivileged ( ( PrivilegedAction<String> ) new PrivilegedAction<String>() {
                @Override
                public String run() {
                    return Localizer.getMessage ( message );
                }
            } );
        }
        return Localizer.getMessage ( message );
    }
    public final void clear() throws IOException {
        if ( this.bufferSize == 0 && this.out != null ) {
            throw new IllegalStateException ( this.getLocalizeMessage ( "jsp.error.ise_on_clear" ) );
        }
        if ( this.flushed ) {
            throw new IOException ( this.getLocalizeMessage ( "jsp.error.attempt_to_clear_flushed_buffer" ) );
        }
        this.ensureOpen();
        this.nextChar = 0;
    }
    public void clearBuffer() throws IOException {
        if ( this.bufferSize == 0 ) {
            throw new IllegalStateException ( this.getLocalizeMessage ( "jsp.error.ise_on_clear" ) );
        }
        this.ensureOpen();
        this.nextChar = 0;
    }
    private final void bufferOverflow() throws IOException {
        throw new IOException ( this.getLocalizeMessage ( "jsp.error.overflow" ) );
    }
    public void flush() throws IOException {
        this.flushBuffer();
        if ( this.out != null ) {
            this.out.flush();
        }
    }
    public void close() throws IOException {
        if ( this.response == null || this.closed ) {
            return;
        }
        this.flush();
        if ( this.out != null ) {
            this.out.close();
        }
        this.out = null;
        this.closed = true;
    }
    public int getRemaining() {
        return this.bufferSize - this.nextChar;
    }
    private void ensureOpen() throws IOException {
        if ( this.response == null || this.closed ) {
            throw new IOException ( "Stream closed" );
        }
    }
    public void write ( final int c ) throws IOException {
        this.ensureOpen();
        if ( this.bufferSize == 0 ) {
            this.initOut();
            this.out.write ( c );
        } else {
            if ( this.nextChar >= this.bufferSize ) {
                if ( this.autoFlush ) {
                    this.flushBuffer();
                } else {
                    this.bufferOverflow();
                }
            }
            this.cb[this.nextChar++] = ( char ) c;
        }
    }
    private static int min ( final int a, final int b ) {
        if ( a < b ) {
            return a;
        }
        return b;
    }
    public void write ( final char[] cbuf, final int off, final int len ) throws IOException {
        this.ensureOpen();
        if ( this.bufferSize == 0 ) {
            this.initOut();
            this.out.write ( cbuf, off, len );
            return;
        }
        if ( off < 0 || off > cbuf.length || len < 0 || off + len > cbuf.length || off + len < 0 ) {
            throw new IndexOutOfBoundsException();
        }
        if ( len == 0 ) {
            return;
        }
        if ( len >= this.bufferSize ) {
            if ( this.autoFlush ) {
                this.flushBuffer();
            } else {
                this.bufferOverflow();
            }
            this.initOut();
            this.out.write ( cbuf, off, len );
            return;
        }
        int b = off;
        final int t = off + len;
        while ( b < t ) {
            final int d = min ( this.bufferSize - this.nextChar, t - b );
            System.arraycopy ( cbuf, b, this.cb, this.nextChar, d );
            b += d;
            this.nextChar += d;
            if ( this.nextChar >= this.bufferSize ) {
                if ( this.autoFlush ) {
                    this.flushBuffer();
                } else {
                    this.bufferOverflow();
                }
            }
        }
    }
    public void write ( final char[] buf ) throws IOException {
        this.write ( buf, 0, buf.length );
    }
    public void write ( final String s, final int off, final int len ) throws IOException {
        this.ensureOpen();
        if ( this.bufferSize == 0 ) {
            this.initOut();
            this.out.write ( s, off, len );
            return;
        }
        int b = off;
        final int t = off + len;
        while ( b < t ) {
            final int d = min ( this.bufferSize - this.nextChar, t - b );
            s.getChars ( b, b + d, this.cb, this.nextChar );
            b += d;
            this.nextChar += d;
            if ( this.nextChar >= this.bufferSize ) {
                if ( this.autoFlush ) {
                    this.flushBuffer();
                } else {
                    this.bufferOverflow();
                }
            }
        }
    }
    public void newLine() throws IOException {
        this.write ( System.lineSeparator() );
    }
    public void print ( final boolean b ) throws IOException {
        this.write ( b ? "true" : "false" );
    }
    public void print ( final char c ) throws IOException {
        this.write ( String.valueOf ( c ) );
    }
    public void print ( final int i ) throws IOException {
        this.write ( String.valueOf ( i ) );
    }
    public void print ( final long l ) throws IOException {
        this.write ( String.valueOf ( l ) );
    }
    public void print ( final float f ) throws IOException {
        this.write ( String.valueOf ( f ) );
    }
    public void print ( final double d ) throws IOException {
        this.write ( String.valueOf ( d ) );
    }
    public void print ( final char[] s ) throws IOException {
        this.write ( s );
    }
    public void print ( String s ) throws IOException {
        if ( s == null ) {
            s = "null";
        }
        this.write ( s );
    }
    public void print ( final Object obj ) throws IOException {
        this.write ( String.valueOf ( obj ) );
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
}
