package org.apache.catalina.filters;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletResponseWrapper;
public class XHttpServletResponse extends HttpServletResponseWrapper {
    private String cacheControlHeader;
    private long lastModifiedHeader;
    private boolean lastModifiedHeaderSet;
    private PrintWriter printWriter;
    private final HttpServletRequest request;
    private ServletOutputStream servletOutputStream;
    private boolean writeResponseBodyStarted;
    public XHttpServletResponse ( final HttpServletRequest request, final HttpServletResponse response ) {
        super ( response );
        this.request = request;
    }
    public void addDateHeader ( final String name, final long date ) {
        super.addDateHeader ( name, date );
        if ( !this.lastModifiedHeaderSet ) {
            this.lastModifiedHeader = date;
            this.lastModifiedHeaderSet = true;
        }
    }
    public void addHeader ( final String name, final String value ) {
        super.addHeader ( name, value );
        if ( "Cache-Control".equalsIgnoreCase ( name ) && this.cacheControlHeader == null ) {
            this.cacheControlHeader = value;
        }
    }
    public String getCacheControlHeader() {
        return this.cacheControlHeader;
    }
    public long getLastModifiedHeader() {
        return this.lastModifiedHeader;
    }
    public ServletOutputStream getOutputStream() throws IOException {
        if ( this.servletOutputStream == null ) {
            this.servletOutputStream = new XServletOutputStream ( super.getOutputStream(), this.request, this );
        }
        return this.servletOutputStream;
    }
    public PrintWriter getWriter() throws IOException {
        if ( this.printWriter == null ) {
            this.printWriter = new XPrintWriter ( super.getWriter(), this.request, this );
        }
        return this.printWriter;
    }
    public boolean isLastModifiedHeaderSet() {
        return this.lastModifiedHeaderSet;
    }
    public boolean isWriteResponseBodyStarted() {
        return this.writeResponseBodyStarted;
    }
    public void reset() {
        super.reset();
        this.lastModifiedHeader = 0L;
        this.lastModifiedHeaderSet = false;
        this.cacheControlHeader = null;
    }
    public void setDateHeader ( final String name, final long date ) {
        super.setDateHeader ( name, date );
        if ( "Last-Modified".equalsIgnoreCase ( name ) ) {
            this.lastModifiedHeader = date;
            this.lastModifiedHeaderSet = true;
        }
    }
    public void setHeader ( final String name, final String value ) {
        super.setHeader ( name, value );
        if ( "Cache-Control".equalsIgnoreCase ( name ) ) {
            this.cacheControlHeader = value;
        }
    }
    public void setWriteResponseBodyStarted ( final boolean writeResponseBodyStarted ) {
        this.writeResponseBodyStarted = writeResponseBodyStarted;
    }
}
