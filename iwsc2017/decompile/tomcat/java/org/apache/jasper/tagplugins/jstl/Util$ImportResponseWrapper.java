package org.apache.jasper.tagplugins.jstl;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.io.Writer;
import java.io.PrintWriter;
import javax.servlet.WriteListener;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import javax.servlet.http.HttpServletResponseWrapper;
public static class ImportResponseWrapper extends HttpServletResponseWrapper {
    private final StringWriter sw;
    private final ByteArrayOutputStream bos;
    private final ServletOutputStream sos;
    private boolean isWriterUsed;
    private boolean isStreamUsed;
    private int status;
    private String charEncoding;
    public ImportResponseWrapper ( final HttpServletResponse arg0 ) {
        super ( arg0 );
        this.sw = new StringWriter();
        this.bos = new ByteArrayOutputStream();
        this.sos = new ServletOutputStream() {
            public void write ( final int b ) throws IOException {
                ImportResponseWrapper.this.bos.write ( b );
            }
            public boolean isReady() {
                return false;
            }
            public void setWriteListener ( final WriteListener listener ) {
                throw new UnsupportedOperationException();
            }
        };
        this.status = 200;
    }
    public PrintWriter getWriter() {
        if ( this.isStreamUsed ) {
            throw new IllegalStateException ( "Unexpected internal error during &lt;import&gt: Target servlet called getWriter(), then getOutputStream()" );
        }
        this.isWriterUsed = true;
        return new PrintWriter ( this.sw );
    }
    public ServletOutputStream getOutputStream() {
        if ( this.isWriterUsed ) {
            throw new IllegalStateException ( "Unexpected internal error during &lt;import&gt: Target servlet called getOutputStream(), then getWriter()" );
        }
        this.isStreamUsed = true;
        return this.sos;
    }
    public void setContentType ( final String x ) {
    }
    public void setLocale ( final Locale x ) {
    }
    public void setStatus ( final int status ) {
        this.status = status;
    }
    public int getStatus() {
        return this.status;
    }
    public String getCharEncoding() {
        return this.charEncoding;
    }
    public void setCharEncoding ( final String ce ) {
        this.charEncoding = ce;
    }
    public String getString() throws UnsupportedEncodingException {
        if ( this.isWriterUsed ) {
            return this.sw.toString();
        }
        if ( !this.isStreamUsed ) {
            return "";
        }
        if ( this.charEncoding != null && !this.charEncoding.equals ( "" ) ) {
            return this.bos.toString ( this.charEncoding );
        }
        return this.bos.toString ( "ISO-8859-1" );
    }
}
