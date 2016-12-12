package javax.servlet.http;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.ResourceBundle;
import javax.servlet.DispatcherType;
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
public abstract class HttpServlet extends GenericServlet {
    private static final long serialVersionUID = 1L;
    private static final String METHOD_DELETE = "DELETE";
    private static final String METHOD_HEAD = "HEAD";
    private static final String METHOD_GET = "GET";
    private static final String METHOD_OPTIONS = "OPTIONS";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_PUT = "PUT";
    private static final String METHOD_TRACE = "TRACE";
    private static final String HEADER_IFMODSINCE = "If-Modified-Since";
    private static final String HEADER_LASTMOD = "Last-Modified";
    private static final String LSTRING_FILE =
        "javax.servlet.http.LocalStrings";
    private static final ResourceBundle lStrings =
        ResourceBundle.getBundle ( LSTRING_FILE );
    public HttpServlet() {
    }
    protected void doGet ( HttpServletRequest req, HttpServletResponse resp )
    throws ServletException, IOException {
        String protocol = req.getProtocol();
        String msg = lStrings.getString ( "http.method_get_not_supported" );
        if ( protocol.endsWith ( "1.1" ) ) {
            resp.sendError ( HttpServletResponse.SC_METHOD_NOT_ALLOWED, msg );
        } else {
            resp.sendError ( HttpServletResponse.SC_BAD_REQUEST, msg );
        }
    }
    protected long getLastModified ( HttpServletRequest req ) {
        return -1;
    }
    protected void doHead ( HttpServletRequest req, HttpServletResponse resp )
    throws ServletException, IOException {
        if ( DispatcherType.INCLUDE.equals ( req.getDispatcherType() ) ) {
            doGet ( req, resp );
        } else {
            NoBodyResponse response = new NoBodyResponse ( resp );
            doGet ( req, response );
            response.setContentLength();
        }
    }
    protected void doPost ( HttpServletRequest req, HttpServletResponse resp )
    throws ServletException, IOException {
        String protocol = req.getProtocol();
        String msg = lStrings.getString ( "http.method_post_not_supported" );
        if ( protocol.endsWith ( "1.1" ) ) {
            resp.sendError ( HttpServletResponse.SC_METHOD_NOT_ALLOWED, msg );
        } else {
            resp.sendError ( HttpServletResponse.SC_BAD_REQUEST, msg );
        }
    }
    protected void doPut ( HttpServletRequest req, HttpServletResponse resp )
    throws ServletException, IOException {
        String protocol = req.getProtocol();
        String msg = lStrings.getString ( "http.method_put_not_supported" );
        if ( protocol.endsWith ( "1.1" ) ) {
            resp.sendError ( HttpServletResponse.SC_METHOD_NOT_ALLOWED, msg );
        } else {
            resp.sendError ( HttpServletResponse.SC_BAD_REQUEST, msg );
        }
    }
    protected void doDelete ( HttpServletRequest req,
                              HttpServletResponse resp )
    throws ServletException, IOException {
        String protocol = req.getProtocol();
        String msg = lStrings.getString ( "http.method_delete_not_supported" );
        if ( protocol.endsWith ( "1.1" ) ) {
            resp.sendError ( HttpServletResponse.SC_METHOD_NOT_ALLOWED, msg );
        } else {
            resp.sendError ( HttpServletResponse.SC_BAD_REQUEST, msg );
        }
    }
    private static Method[] getAllDeclaredMethods ( Class<?> c ) {
        if ( c.equals ( javax.servlet.http.HttpServlet.class ) ) {
            return null;
        }
        Method[] parentMethods = getAllDeclaredMethods ( c.getSuperclass() );
        Method[] thisMethods = c.getDeclaredMethods();
        if ( ( parentMethods != null ) && ( parentMethods.length > 0 ) ) {
            Method[] allMethods =
                new Method[parentMethods.length + thisMethods.length];
            System.arraycopy ( parentMethods, 0, allMethods, 0,
                               parentMethods.length );
            System.arraycopy ( thisMethods, 0, allMethods, parentMethods.length,
                               thisMethods.length );
            thisMethods = allMethods;
        }
        return thisMethods;
    }
    protected void doOptions ( HttpServletRequest req,
                               HttpServletResponse resp )
    throws ServletException, IOException {
        Method[] methods = getAllDeclaredMethods ( this.getClass() );
        boolean ALLOW_GET = false;
        boolean ALLOW_HEAD = false;
        boolean ALLOW_POST = false;
        boolean ALLOW_PUT = false;
        boolean ALLOW_DELETE = false;
        boolean ALLOW_TRACE = true;
        boolean ALLOW_OPTIONS = true;
        for ( int i = 0; i < methods.length; i++ ) {
            Method m = methods[i];
            if ( m.getName().equals ( "doGet" ) ) {
                ALLOW_GET = true;
                ALLOW_HEAD = true;
            }
            if ( m.getName().equals ( "doPost" ) ) {
                ALLOW_POST = true;
            }
            if ( m.getName().equals ( "doPut" ) ) {
                ALLOW_PUT = true;
            }
            if ( m.getName().equals ( "doDelete" ) ) {
                ALLOW_DELETE = true;
            }
        }
        String allow = null;
        if ( ALLOW_GET ) {
            allow = METHOD_GET;
        }
        if ( ALLOW_HEAD )
            if ( allow == null ) {
                allow = METHOD_HEAD;
            } else {
                allow += ", " + METHOD_HEAD;
            }
        if ( ALLOW_POST )
            if ( allow == null ) {
                allow = METHOD_POST;
            } else {
                allow += ", " + METHOD_POST;
            }
        if ( ALLOW_PUT )
            if ( allow == null ) {
                allow = METHOD_PUT;
            } else {
                allow += ", " + METHOD_PUT;
            }
        if ( ALLOW_DELETE )
            if ( allow == null ) {
                allow = METHOD_DELETE;
            } else {
                allow += ", " + METHOD_DELETE;
            }
        if ( ALLOW_TRACE )
            if ( allow == null ) {
                allow = METHOD_TRACE;
            } else {
                allow += ", " + METHOD_TRACE;
            }
        if ( ALLOW_OPTIONS )
            if ( allow == null ) {
                allow = METHOD_OPTIONS;
            } else {
                allow += ", " + METHOD_OPTIONS;
            }
        resp.setHeader ( "Allow", allow );
    }
    protected void doTrace ( HttpServletRequest req, HttpServletResponse resp )
    throws ServletException, IOException {
        int responseLength;
        String CRLF = "\r\n";
        StringBuilder buffer = new StringBuilder ( "TRACE " ).append ( req.getRequestURI() )
        .append ( " " ).append ( req.getProtocol() );
        Enumeration<String> reqHeaderEnum = req.getHeaderNames();
        while ( reqHeaderEnum.hasMoreElements() ) {
            String headerName = reqHeaderEnum.nextElement();
            buffer.append ( CRLF ).append ( headerName ).append ( ": " )
            .append ( req.getHeader ( headerName ) );
        }
        buffer.append ( CRLF );
        responseLength = buffer.length();
        resp.setContentType ( "message/http" );
        resp.setContentLength ( responseLength );
        ServletOutputStream out = resp.getOutputStream();
        out.print ( buffer.toString() );
        out.close();
        return;
    }
    protected void service ( HttpServletRequest req, HttpServletResponse resp )
    throws ServletException, IOException {
        String method = req.getMethod();
        if ( method.equals ( METHOD_GET ) ) {
            long lastModified = getLastModified ( req );
            if ( lastModified == -1 ) {
                doGet ( req, resp );
            } else {
                long ifModifiedSince;
                try {
                    ifModifiedSince = req.getDateHeader ( HEADER_IFMODSINCE );
                } catch ( IllegalArgumentException iae ) {
                    ifModifiedSince = -1;
                }
                if ( ifModifiedSince < ( lastModified / 1000 * 1000 ) ) {
                    maybeSetLastModified ( resp, lastModified );
                    doGet ( req, resp );
                } else {
                    resp.setStatus ( HttpServletResponse.SC_NOT_MODIFIED );
                }
            }
        } else if ( method.equals ( METHOD_HEAD ) ) {
            long lastModified = getLastModified ( req );
            maybeSetLastModified ( resp, lastModified );
            doHead ( req, resp );
        } else if ( method.equals ( METHOD_POST ) ) {
            doPost ( req, resp );
        } else if ( method.equals ( METHOD_PUT ) ) {
            doPut ( req, resp );
        } else if ( method.equals ( METHOD_DELETE ) ) {
            doDelete ( req, resp );
        } else if ( method.equals ( METHOD_OPTIONS ) ) {
            doOptions ( req, resp );
        } else if ( method.equals ( METHOD_TRACE ) ) {
            doTrace ( req, resp );
        } else {
            String errMsg = lStrings.getString ( "http.method_not_implemented" );
            Object[] errArgs = new Object[1];
            errArgs[0] = method;
            errMsg = MessageFormat.format ( errMsg, errArgs );
            resp.sendError ( HttpServletResponse.SC_NOT_IMPLEMENTED, errMsg );
        }
    }
    private void maybeSetLastModified ( HttpServletResponse resp,
                                        long lastModified ) {
        if ( resp.containsHeader ( HEADER_LASTMOD ) ) {
            return;
        }
        if ( lastModified >= 0 ) {
            resp.setDateHeader ( HEADER_LASTMOD, lastModified );
        }
    }
    @Override
    public void service ( ServletRequest req, ServletResponse res )
    throws ServletException, IOException {
        HttpServletRequest  request;
        HttpServletResponse response;
        try {
            request = ( HttpServletRequest ) req;
            response = ( HttpServletResponse ) res;
        } catch ( ClassCastException e ) {
            throw new ServletException ( "non-HTTP request or response" );
        }
        service ( request, response );
    }
}
class NoBodyResponse extends HttpServletResponseWrapper {
    private final NoBodyOutputStream noBody;
    private PrintWriter writer;
    private boolean didSetContentLength;
    NoBodyResponse ( HttpServletResponse r ) {
        super ( r );
        noBody = new NoBodyOutputStream();
    }
    void setContentLength() {
        if ( !didSetContentLength ) {
            if ( writer != null ) {
                writer.flush();
            }
            super.setContentLength ( noBody.getContentLength() );
        }
    }
    @Override
    public void setContentLength ( int len ) {
        super.setContentLength ( len );
        didSetContentLength = true;
    }
    @Override
    public void setContentLengthLong ( long len ) {
        super.setContentLengthLong ( len );
        didSetContentLength = true;
    }
    @Override
    public void setHeader ( String name, String value ) {
        super.setHeader ( name, value );
        checkHeader ( name );
    }
    @Override
    public void addHeader ( String name, String value ) {
        super.addHeader ( name, value );
        checkHeader ( name );
    }
    @Override
    public void setIntHeader ( String name, int value ) {
        super.setIntHeader ( name, value );
        checkHeader ( name );
    }
    @Override
    public void addIntHeader ( String name, int value ) {
        super.addIntHeader ( name, value );
        checkHeader ( name );
    }
    private void checkHeader ( String name ) {
        if ( "content-length".equalsIgnoreCase ( name ) ) {
            didSetContentLength = true;
        }
    }
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return noBody;
    }
    @Override
    public PrintWriter getWriter() throws UnsupportedEncodingException {
        if ( writer == null ) {
            OutputStreamWriter w;
            w = new OutputStreamWriter ( noBody, getCharacterEncoding() );
            writer = new PrintWriter ( w );
        }
        return writer;
    }
}
class NoBodyOutputStream extends ServletOutputStream {
    private static final String LSTRING_FILE =
        "javax.servlet.http.LocalStrings";
    private static final ResourceBundle lStrings =
        ResourceBundle.getBundle ( LSTRING_FILE );
    private int contentLength = 0;
    NoBodyOutputStream() {
    }
    int getContentLength() {
        return contentLength;
    }
    @Override
    public void write ( int b ) {
        contentLength++;
    }
    @Override
    public void write ( byte buf[], int offset, int len ) throws IOException {
        if ( buf == null ) {
            throw new NullPointerException (
                lStrings.getString ( "err.io.nullArray" ) );
        }
        if ( offset < 0 || len < 0 || offset + len > buf.length ) {
            String msg = lStrings.getString ( "err.io.indexOutOfBounds" );
            Object[] msgArgs = new Object[3];
            msgArgs[0] = Integer.valueOf ( offset );
            msgArgs[1] = Integer.valueOf ( len );
            msgArgs[2] = Integer.valueOf ( buf.length );
            msg = MessageFormat.format ( msg, msgArgs );
            throw new IndexOutOfBoundsException ( msg );
        }
        contentLength += len;
    }
    @Override
    public boolean isReady() {
        return false;
    }
    @Override
    public void setWriteListener ( javax.servlet.WriteListener listener ) {
    }
}
