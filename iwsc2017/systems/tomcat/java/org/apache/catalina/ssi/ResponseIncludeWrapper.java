package org.apache.catalina.ssi;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.apache.tomcat.util.ExceptionUtils;
public class ResponseIncludeWrapper extends HttpServletResponseWrapper {
    private static final String CONTENT_TYPE = "content-type";
    private static final String LAST_MODIFIED = "last-modified";
    private static final DateFormat RFC1123_FORMAT;
    private static final String RFC1123_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";
    protected long lastModified = -1;
    private String contentType = null;
    protected final ServletOutputStream captureServletOutputStream;
    protected ServletOutputStream servletOutputStream;
    protected PrintWriter printWriter;
    private final ServletContext context;
    private final HttpServletRequest request;
    static {
        RFC1123_FORMAT = new SimpleDateFormat ( RFC1123_PATTERN, Locale.US );
        RFC1123_FORMAT.setTimeZone ( TimeZone.getTimeZone ( "GMT" ) );
    }
    public ResponseIncludeWrapper ( ServletContext context,
                                    HttpServletRequest request, HttpServletResponse response,
                                    ServletOutputStream captureServletOutputStream ) {
        super ( response );
        this.context = context;
        this.request = request;
        this.captureServletOutputStream = captureServletOutputStream;
    }
    public void flushOutputStreamOrWriter() throws IOException {
        if ( servletOutputStream != null ) {
            servletOutputStream.flush();
        }
        if ( printWriter != null ) {
            printWriter.flush();
        }
    }
    @Override
    public PrintWriter getWriter() throws java.io.IOException {
        if ( servletOutputStream == null ) {
            if ( printWriter == null ) {
                setCharacterEncoding ( getCharacterEncoding() );
                printWriter = new PrintWriter (
                    new OutputStreamWriter ( captureServletOutputStream,
                                             getCharacterEncoding() ) );
            }
            return printWriter;
        }
        throw new IllegalStateException();
    }
    @Override
    public ServletOutputStream getOutputStream() throws java.io.IOException {
        if ( printWriter == null ) {
            if ( servletOutputStream == null ) {
                servletOutputStream = captureServletOutputStream;
            }
            return servletOutputStream;
        }
        throw new IllegalStateException();
    }
    public long getLastModified() {
        if ( lastModified == -1 ) {
            return -1;
        }
        return lastModified;
    }
    @Override
    public String getContentType() {
        if ( contentType == null ) {
            String url = request.getRequestURI();
            String mime = context.getMimeType ( url );
            if ( mime != null ) {
                setContentType ( mime );
            } else {
                setContentType ( "application/x-octet-stream" );
            }
        }
        return contentType;
    }
    @Override
    public void setContentType ( String mime ) {
        contentType = mime;
        if ( contentType != null ) {
            getResponse().setContentType ( contentType );
        }
    }
    @Override
    public void addDateHeader ( String name, long value ) {
        super.addDateHeader ( name, value );
        String lname = name.toLowerCase ( Locale.ENGLISH );
        if ( lname.equals ( LAST_MODIFIED ) ) {
            lastModified = value;
        }
    }
    @Override
    public void addHeader ( String name, String value ) {
        super.addHeader ( name, value );
        String lname = name.toLowerCase ( Locale.ENGLISH );
        if ( lname.equals ( LAST_MODIFIED ) ) {
            try {
                synchronized ( RFC1123_FORMAT ) {
                    lastModified = RFC1123_FORMAT.parse ( value ).getTime();
                }
            } catch ( Throwable ignore ) {
                ExceptionUtils.handleThrowable ( ignore );
            }
        } else if ( lname.equals ( CONTENT_TYPE ) ) {
            contentType = value;
        }
    }
    @Override
    public void setDateHeader ( String name, long value ) {
        super.setDateHeader ( name, value );
        String lname = name.toLowerCase ( Locale.ENGLISH );
        if ( lname.equals ( LAST_MODIFIED ) ) {
            lastModified = value;
        }
    }
    @Override
    public void setHeader ( String name, String value ) {
        super.setHeader ( name, value );
        String lname = name.toLowerCase ( Locale.ENGLISH );
        if ( lname.equals ( LAST_MODIFIED ) ) {
            try {
                synchronized ( RFC1123_FORMAT ) {
                    lastModified = RFC1123_FORMAT.parse ( value ).getTime();
                }
            } catch ( Throwable ignore ) {
                ExceptionUtils.handleThrowable ( ignore );
            }
        } else if ( lname.equals ( CONTENT_TYPE ) ) {
            contentType = value;
        }
    }
}
