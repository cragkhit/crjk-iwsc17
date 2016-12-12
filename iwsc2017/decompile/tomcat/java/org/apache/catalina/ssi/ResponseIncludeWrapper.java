package org.apache.catalina.ssi;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import org.apache.tomcat.util.ExceptionUtils;
import java.util.Locale;
import java.io.Writer;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import java.text.DateFormat;
import javax.servlet.http.HttpServletResponseWrapper;
public class ResponseIncludeWrapper extends HttpServletResponseWrapper {
    private static final String CONTENT_TYPE = "content-type";
    private static final String LAST_MODIFIED = "last-modified";
    private static final DateFormat RFC1123_FORMAT;
    private static final String RFC1123_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";
    protected long lastModified;
    private String contentType;
    protected final ServletOutputStream captureServletOutputStream;
    protected ServletOutputStream servletOutputStream;
    protected PrintWriter printWriter;
    private final ServletContext context;
    private final HttpServletRequest request;
    public ResponseIncludeWrapper ( final ServletContext context, final HttpServletRequest request, final HttpServletResponse response, final ServletOutputStream captureServletOutputStream ) {
        super ( response );
        this.lastModified = -1L;
        this.contentType = null;
        this.context = context;
        this.request = request;
        this.captureServletOutputStream = captureServletOutputStream;
    }
    public void flushOutputStreamOrWriter() throws IOException {
        if ( this.servletOutputStream != null ) {
            this.servletOutputStream.flush();
        }
        if ( this.printWriter != null ) {
            this.printWriter.flush();
        }
    }
    public PrintWriter getWriter() throws IOException {
        if ( this.servletOutputStream == null ) {
            if ( this.printWriter == null ) {
                this.setCharacterEncoding ( this.getCharacterEncoding() );
                this.printWriter = new PrintWriter ( new OutputStreamWriter ( ( OutputStream ) this.captureServletOutputStream, this.getCharacterEncoding() ) );
            }
            return this.printWriter;
        }
        throw new IllegalStateException();
    }
    public ServletOutputStream getOutputStream() throws IOException {
        if ( this.printWriter == null ) {
            if ( this.servletOutputStream == null ) {
                this.servletOutputStream = this.captureServletOutputStream;
            }
            return this.servletOutputStream;
        }
        throw new IllegalStateException();
    }
    public long getLastModified() {
        if ( this.lastModified == -1L ) {
            return -1L;
        }
        return this.lastModified;
    }
    public String getContentType() {
        if ( this.contentType == null ) {
            final String url = this.request.getRequestURI();
            final String mime = this.context.getMimeType ( url );
            if ( mime != null ) {
                this.setContentType ( mime );
            } else {
                this.setContentType ( "application/x-octet-stream" );
            }
        }
        return this.contentType;
    }
    public void setContentType ( final String mime ) {
        this.contentType = mime;
        if ( this.contentType != null ) {
            this.getResponse().setContentType ( this.contentType );
        }
    }
    public void addDateHeader ( final String name, final long value ) {
        super.addDateHeader ( name, value );
        final String lname = name.toLowerCase ( Locale.ENGLISH );
        if ( lname.equals ( "last-modified" ) ) {
            this.lastModified = value;
        }
    }
    public void addHeader ( final String name, final String value ) {
        super.addHeader ( name, value );
        final String lname = name.toLowerCase ( Locale.ENGLISH );
        if ( lname.equals ( "last-modified" ) ) {
            try {
                synchronized ( ResponseIncludeWrapper.RFC1123_FORMAT ) {
                    this.lastModified = ResponseIncludeWrapper.RFC1123_FORMAT.parse ( value ).getTime();
                }
            } catch ( Throwable ignore ) {
                ExceptionUtils.handleThrowable ( ignore );
            }
        } else if ( lname.equals ( "content-type" ) ) {
            this.contentType = value;
        }
    }
    public void setDateHeader ( final String name, final long value ) {
        super.setDateHeader ( name, value );
        final String lname = name.toLowerCase ( Locale.ENGLISH );
        if ( lname.equals ( "last-modified" ) ) {
            this.lastModified = value;
        }
    }
    public void setHeader ( final String name, final String value ) {
        super.setHeader ( name, value );
        final String lname = name.toLowerCase ( Locale.ENGLISH );
        if ( lname.equals ( "last-modified" ) ) {
            try {
                synchronized ( ResponseIncludeWrapper.RFC1123_FORMAT ) {
                    this.lastModified = ResponseIncludeWrapper.RFC1123_FORMAT.parse ( value ).getTime();
                }
            } catch ( Throwable ignore ) {
                ExceptionUtils.handleThrowable ( ignore );
            }
        } else if ( lname.equals ( "content-type" ) ) {
            this.contentType = value;
        }
    }
    static {
        ( RFC1123_FORMAT = new SimpleDateFormat ( "EEE, dd MMM yyyy HH:mm:ss z", Locale.US ) ).setTimeZone ( TimeZone.getTimeZone ( "GMT" ) );
    }
}
