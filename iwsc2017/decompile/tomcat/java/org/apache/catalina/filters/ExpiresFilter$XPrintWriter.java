package org.apache.catalina.filters;
import java.io.IOException;
import java.util.Locale;
import java.io.Writer;
import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
public class XPrintWriter extends PrintWriter {
    private final PrintWriter out;
    private final HttpServletRequest request;
    private final XHttpServletResponse response;
    public XPrintWriter ( final PrintWriter out, final HttpServletRequest request, final XHttpServletResponse response ) {
        super ( out );
        this.out = out;
        this.request = request;
        this.response = response;
    }
    @Override
    public PrintWriter append ( final char c ) {
        this.fireBeforeWriteResponseBodyEvent();
        return this.out.append ( c );
    }
    @Override
    public PrintWriter append ( final CharSequence csq ) {
        this.fireBeforeWriteResponseBodyEvent();
        return this.out.append ( csq );
    }
    @Override
    public PrintWriter append ( final CharSequence csq, final int start, final int end ) {
        this.fireBeforeWriteResponseBodyEvent();
        return this.out.append ( csq, start, end );
    }
    @Override
    public void close() {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.close();
    }
    private void fireBeforeWriteResponseBodyEvent() {
        if ( !this.response.isWriteResponseBodyStarted() ) {
            this.response.setWriteResponseBodyStarted ( true );
            ExpiresFilter.this.onBeforeWriteResponseBody ( this.request, this.response );
        }
    }
    @Override
    public void flush() {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.flush();
    }
    @Override
    public void print ( final boolean b ) {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.print ( b );
    }
    @Override
    public void print ( final char c ) {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.print ( c );
    }
    @Override
    public void print ( final char[] s ) {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.print ( s );
    }
    @Override
    public void print ( final double d ) {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.print ( d );
    }
    @Override
    public void print ( final float f ) {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.print ( f );
    }
    @Override
    public void print ( final int i ) {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.print ( i );
    }
    @Override
    public void print ( final long l ) {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.print ( l );
    }
    @Override
    public void print ( final Object obj ) {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.print ( obj );
    }
    @Override
    public void print ( final String s ) {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.print ( s );
    }
    @Override
    public PrintWriter printf ( final Locale l, final String format, final Object... args ) {
        this.fireBeforeWriteResponseBodyEvent();
        return this.out.printf ( l, format, args );
    }
    @Override
    public PrintWriter printf ( final String format, final Object... args ) {
        this.fireBeforeWriteResponseBodyEvent();
        return this.out.printf ( format, args );
    }
    @Override
    public void println() {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.println();
    }
    @Override
    public void println ( final boolean x ) {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.println ( x );
    }
    @Override
    public void println ( final char x ) {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.println ( x );
    }
    @Override
    public void println ( final char[] x ) {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.println ( x );
    }
    @Override
    public void println ( final double x ) {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.println ( x );
    }
    @Override
    public void println ( final float x ) {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.println ( x );
    }
    @Override
    public void println ( final int x ) {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.println ( x );
    }
    @Override
    public void println ( final long x ) {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.println ( x );
    }
    @Override
    public void println ( final Object x ) {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.println ( x );
    }
    @Override
    public void println ( final String x ) {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.println ( x );
    }
    @Override
    public void write ( final char[] buf ) {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.write ( buf );
    }
    @Override
    public void write ( final char[] buf, final int off, final int len ) {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.write ( buf, off, len );
    }
    @Override
    public void write ( final int c ) {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.write ( c );
    }
    @Override
    public void write ( final String s ) {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.write ( s );
    }
    @Override
    public void write ( final String s, final int off, final int len ) {
        this.fireBeforeWriteResponseBodyEvent();
        this.out.write ( s, off, len );
    }
}
