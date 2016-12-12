package org.apache.catalina.filters;
import javax.servlet.WriteListener;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletOutputStream;
public class XServletOutputStream extends ServletOutputStream {
    private final HttpServletRequest request;
    private final XHttpServletResponse response;
    private final ServletOutputStream servletOutputStream;
    public XServletOutputStream ( final ServletOutputStream servletOutputStream, final HttpServletRequest request, final XHttpServletResponse response ) {
        this.servletOutputStream = servletOutputStream;
        this.response = response;
        this.request = request;
    }
    public void close() throws IOException {
        this.fireOnBeforeWriteResponseBodyEvent();
        this.servletOutputStream.close();
    }
    private void fireOnBeforeWriteResponseBodyEvent() {
        if ( !this.response.isWriteResponseBodyStarted() ) {
            this.response.setWriteResponseBodyStarted ( true );
            ExpiresFilter.this.onBeforeWriteResponseBody ( this.request, this.response );
        }
    }
    public void flush() throws IOException {
        this.fireOnBeforeWriteResponseBodyEvent();
        this.servletOutputStream.flush();
    }
    public void print ( final boolean b ) throws IOException {
        this.fireOnBeforeWriteResponseBodyEvent();
        this.servletOutputStream.print ( b );
    }
    public void print ( final char c ) throws IOException {
        this.fireOnBeforeWriteResponseBodyEvent();
        this.servletOutputStream.print ( c );
    }
    public void print ( final double d ) throws IOException {
        this.fireOnBeforeWriteResponseBodyEvent();
        this.servletOutputStream.print ( d );
    }
    public void print ( final float f ) throws IOException {
        this.fireOnBeforeWriteResponseBodyEvent();
        this.servletOutputStream.print ( f );
    }
    public void print ( final int i ) throws IOException {
        this.fireOnBeforeWriteResponseBodyEvent();
        this.servletOutputStream.print ( i );
    }
    public void print ( final long l ) throws IOException {
        this.fireOnBeforeWriteResponseBodyEvent();
        this.servletOutputStream.print ( l );
    }
    public void print ( final String s ) throws IOException {
        this.fireOnBeforeWriteResponseBodyEvent();
        this.servletOutputStream.print ( s );
    }
    public void println() throws IOException {
        this.fireOnBeforeWriteResponseBodyEvent();
        this.servletOutputStream.println();
    }
    public void println ( final boolean b ) throws IOException {
        this.fireOnBeforeWriteResponseBodyEvent();
        this.servletOutputStream.println ( b );
    }
    public void println ( final char c ) throws IOException {
        this.fireOnBeforeWriteResponseBodyEvent();
        this.servletOutputStream.println ( c );
    }
    public void println ( final double d ) throws IOException {
        this.fireOnBeforeWriteResponseBodyEvent();
        this.servletOutputStream.println ( d );
    }
    public void println ( final float f ) throws IOException {
        this.fireOnBeforeWriteResponseBodyEvent();
        this.servletOutputStream.println ( f );
    }
    public void println ( final int i ) throws IOException {
        this.fireOnBeforeWriteResponseBodyEvent();
        this.servletOutputStream.println ( i );
    }
    public void println ( final long l ) throws IOException {
        this.fireOnBeforeWriteResponseBodyEvent();
        this.servletOutputStream.println ( l );
    }
    public void println ( final String s ) throws IOException {
        this.fireOnBeforeWriteResponseBodyEvent();
        this.servletOutputStream.println ( s );
    }
    public void write ( final byte[] b ) throws IOException {
        this.fireOnBeforeWriteResponseBodyEvent();
        this.servletOutputStream.write ( b );
    }
    public void write ( final byte[] b, final int off, final int len ) throws IOException {
        this.fireOnBeforeWriteResponseBodyEvent();
        this.servletOutputStream.write ( b, off, len );
    }
    public void write ( final int b ) throws IOException {
        this.fireOnBeforeWriteResponseBodyEvent();
        this.servletOutputStream.write ( b );
    }
    public boolean isReady() {
        return false;
    }
    public void setWriteListener ( final WriteListener listener ) {
    }
}
