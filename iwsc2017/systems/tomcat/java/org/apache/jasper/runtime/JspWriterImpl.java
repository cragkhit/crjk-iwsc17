package org.apache.jasper.runtime;
import java.io.IOException;
import java.io.Writer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.servlet.ServletResponse;
import javax.servlet.jsp.JspWriter;
import org.apache.jasper.Constants;
import org.apache.jasper.compiler.Localizer;
import org.apache.jasper.security.SecurityUtil;
public class JspWriterImpl extends JspWriter {
    private Writer out;
    private ServletResponse response;
    private char cb[];
    private int nextChar;
    private boolean flushed = false;
    private boolean closed = false;
    public JspWriterImpl() {
        super ( Constants.DEFAULT_BUFFER_SIZE, true );
    }
    public JspWriterImpl ( ServletResponse response, int sz,
                           boolean autoFlush ) {
        super ( sz, autoFlush );
        if ( sz < 0 ) {
            throw new IllegalArgumentException ( "Buffer size <= 0" );
        }
        this.response = response;
        cb = sz == 0 ? null : new char[sz];
        nextChar = 0;
    }
    void init ( ServletResponse response, int sz, boolean autoFlush ) {
        this.response = response;
        if ( sz > 0 && ( cb == null || sz > cb.length ) ) {
            cb = new char[sz];
        }
        nextChar = 0;
        this.autoFlush = autoFlush;
        this.bufferSize = sz;
    }
    void recycle() {
        flushed = false;
        closed = false;
        out = null;
        nextChar = 0;
        response = null;
    }
    protected final void flushBuffer() throws IOException {
        if ( bufferSize == 0 ) {
            return;
        }
        flushed = true;
        ensureOpen();
        if ( nextChar == 0 ) {
            return;
        }
        initOut();
        out.write ( cb, 0, nextChar );
        nextChar = 0;
    }
    private void initOut() throws IOException {
        if ( out == null ) {
            out = response.getWriter();
        }
    }
    private String getLocalizeMessage ( final String message ) {
        if ( SecurityUtil.isPackageProtectionEnabled() ) {
            return AccessController.doPrivileged ( new PrivilegedAction<String>() {
                @Override
                public String run() {
                    return Localizer.getMessage ( message );
                }
            } );
        } else {
            return Localizer.getMessage ( message );
        }
    }
    @Override
    public final void clear() throws IOException {
        if ( ( bufferSize == 0 ) && ( out != null ) )
            throw new IllegalStateException (
                getLocalizeMessage ( "jsp.error.ise_on_clear" ) );
        if ( flushed )
            throw new IOException (
                getLocalizeMessage ( "jsp.error.attempt_to_clear_flushed_buffer" ) );
        ensureOpen();
        nextChar = 0;
    }
    @Override
    public void clearBuffer() throws IOException {
        if ( bufferSize == 0 )
            throw new IllegalStateException (
                getLocalizeMessage ( "jsp.error.ise_on_clear" ) );
        ensureOpen();
        nextChar = 0;
    }
    private final void bufferOverflow() throws IOException {
        throw new IOException ( getLocalizeMessage ( "jsp.error.overflow" ) );
    }
    @Override
    public void flush()  throws IOException {
        flushBuffer();
        if ( out != null ) {
            out.flush();
        }
    }
    @Override
    public void close() throws IOException {
        if ( response == null || closed ) {
            return;
        }
        flush();
        if ( out != null ) {
            out.close();
        }
        out = null;
        closed = true;
    }
    @Override
    public int getRemaining() {
        return bufferSize - nextChar;
    }
    private void ensureOpen() throws IOException {
        if ( response == null || closed ) {
            throw new IOException ( "Stream closed" );
        }
    }
    @Override
    public void write ( int c ) throws IOException {
        ensureOpen();
        if ( bufferSize == 0 ) {
            initOut();
            out.write ( c );
        } else {
            if ( nextChar >= bufferSize )
                if ( autoFlush ) {
                    flushBuffer();
                } else {
                    bufferOverflow();
                }
            cb[nextChar++] = ( char ) c;
        }
    }
    private static int min ( int a, int b ) {
        if ( a < b ) {
            return a;
        }
        return b;
    }
    @Override
    public void write ( char cbuf[], int off, int len )
    throws IOException {
        ensureOpen();
        if ( bufferSize == 0 ) {
            initOut();
            out.write ( cbuf, off, len );
            return;
        }
        if ( ( off < 0 ) || ( off > cbuf.length ) || ( len < 0 ) ||
                ( ( off + len ) > cbuf.length ) || ( ( off + len ) < 0 ) ) {
            throw new IndexOutOfBoundsException();
        } else if ( len == 0 ) {
            return;
        }
        if ( len >= bufferSize ) {
            if ( autoFlush ) {
                flushBuffer();
            } else {
                bufferOverflow();
            }
            initOut();
            out.write ( cbuf, off, len );
            return;
        }
        int b = off, t = off + len;
        while ( b < t ) {
            int d = min ( bufferSize - nextChar, t - b );
            System.arraycopy ( cbuf, b, cb, nextChar, d );
            b += d;
            nextChar += d;
            if ( nextChar >= bufferSize )
                if ( autoFlush ) {
                    flushBuffer();
                } else {
                    bufferOverflow();
                }
        }
    }
    @Override
    public void write ( char buf[] ) throws IOException {
        write ( buf, 0, buf.length );
    }
    @Override
    public void write ( String s, int off, int len ) throws IOException {
        ensureOpen();
        if ( bufferSize == 0 ) {
            initOut();
            out.write ( s, off, len );
            return;
        }
        int b = off, t = off + len;
        while ( b < t ) {
            int d = min ( bufferSize - nextChar, t - b );
            s.getChars ( b, b + d, cb, nextChar );
            b += d;
            nextChar += d;
            if ( nextChar >= bufferSize )
                if ( autoFlush ) {
                    flushBuffer();
                } else {
                    bufferOverflow();
                }
        }
    }
    @Override
    public void newLine() throws IOException {
        write ( System.lineSeparator() );
    }
    @Override
    public void print ( boolean b ) throws IOException {
        write ( b ? "true" : "false" );
    }
    @Override
    public void print ( char c ) throws IOException {
        write ( String.valueOf ( c ) );
    }
    @Override
    public void print ( int i ) throws IOException {
        write ( String.valueOf ( i ) );
    }
    @Override
    public void print ( long l ) throws IOException {
        write ( String.valueOf ( l ) );
    }
    @Override
    public void print ( float f ) throws IOException {
        write ( String.valueOf ( f ) );
    }
    @Override
    public void print ( double d ) throws IOException {
        write ( String.valueOf ( d ) );
    }
    @Override
    public void print ( char s[] ) throws IOException {
        write ( s );
    }
    @Override
    public void print ( String s ) throws IOException {
        if ( s == null ) {
            s = "null";
        }
        write ( s );
    }
    @Override
    public void print ( Object obj ) throws IOException {
        write ( String.valueOf ( obj ) );
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
}
