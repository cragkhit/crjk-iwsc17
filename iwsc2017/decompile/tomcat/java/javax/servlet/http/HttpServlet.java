package javax.servlet.http;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import java.text.MessageFormat;
import javax.servlet.ServletOutputStream;
import java.util.Enumeration;
import java.lang.reflect.Method;
import javax.servlet.DispatcherType;
import java.io.IOException;
import javax.servlet.ServletException;
import java.util.ResourceBundle;
import javax.servlet.GenericServlet;
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
    private static final String LSTRING_FILE = "javax.servlet.http.LocalStrings";
    private static final ResourceBundle lStrings;
    protected void doGet ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException {
        final String protocol = req.getProtocol();
        final String msg = HttpServlet.lStrings.getString ( "http.method_get_not_supported" );
        if ( protocol.endsWith ( "1.1" ) ) {
            resp.sendError ( 405, msg );
        } else {
            resp.sendError ( 400, msg );
        }
    }
    protected long getLastModified ( final HttpServletRequest req ) {
        return -1L;
    }
    protected void doHead ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException {
        if ( DispatcherType.INCLUDE.equals ( req.getDispatcherType() ) ) {
            this.doGet ( req, resp );
        } else {
            final NoBodyResponse response = new NoBodyResponse ( resp );
            this.doGet ( req, response );
            response.setContentLength();
        }
    }
    protected void doPost ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException {
        final String protocol = req.getProtocol();
        final String msg = HttpServlet.lStrings.getString ( "http.method_post_not_supported" );
        if ( protocol.endsWith ( "1.1" ) ) {
            resp.sendError ( 405, msg );
        } else {
            resp.sendError ( 400, msg );
        }
    }
    protected void doPut ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException {
        final String protocol = req.getProtocol();
        final String msg = HttpServlet.lStrings.getString ( "http.method_put_not_supported" );
        if ( protocol.endsWith ( "1.1" ) ) {
            resp.sendError ( 405, msg );
        } else {
            resp.sendError ( 400, msg );
        }
    }
    protected void doDelete ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException {
        final String protocol = req.getProtocol();
        final String msg = HttpServlet.lStrings.getString ( "http.method_delete_not_supported" );
        if ( protocol.endsWith ( "1.1" ) ) {
            resp.sendError ( 405, msg );
        } else {
            resp.sendError ( 400, msg );
        }
    }
    private static Method[] getAllDeclaredMethods ( final Class<?> c ) {
        if ( c.equals ( HttpServlet.class ) ) {
            return null;
        }
        final Method[] parentMethods = getAllDeclaredMethods ( c.getSuperclass() );
        Method[] thisMethods = c.getDeclaredMethods();
        if ( parentMethods != null && parentMethods.length > 0 ) {
            final Method[] allMethods = new Method[parentMethods.length + thisMethods.length];
            System.arraycopy ( parentMethods, 0, allMethods, 0, parentMethods.length );
            System.arraycopy ( thisMethods, 0, allMethods, parentMethods.length, thisMethods.length );
            thisMethods = allMethods;
        }
        return thisMethods;
    }
    protected void doOptions ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException {
        final Method[] methods = getAllDeclaredMethods ( this.getClass() );
        boolean ALLOW_GET = false;
        boolean ALLOW_HEAD = false;
        boolean ALLOW_POST = false;
        boolean ALLOW_PUT = false;
        boolean ALLOW_DELETE = false;
        final boolean ALLOW_TRACE = true;
        final boolean ALLOW_OPTIONS = true;
        for ( int i = 0; i < methods.length; ++i ) {
            final Method m = methods[i];
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
            allow = "GET";
        }
        if ( ALLOW_HEAD ) {
            if ( allow == null ) {
                allow = "HEAD";
            } else {
                allow += ", HEAD";
            }
        }
        if ( ALLOW_POST ) {
            if ( allow == null ) {
                allow = "POST";
            } else {
                allow += ", POST";
            }
        }
        if ( ALLOW_PUT ) {
            if ( allow == null ) {
                allow = "PUT";
            } else {
                allow += ", PUT";
            }
        }
        if ( ALLOW_DELETE ) {
            if ( allow == null ) {
                allow = "DELETE";
            } else {
                allow += ", DELETE";
            }
        }
        if ( ALLOW_TRACE ) {
            if ( allow == null ) {
                allow = "TRACE";
            } else {
                allow += ", TRACE";
            }
        }
        if ( ALLOW_OPTIONS ) {
            if ( allow == null ) {
                allow = "OPTIONS";
            } else {
                allow += ", OPTIONS";
            }
        }
        resp.setHeader ( "Allow", allow );
    }
    protected void doTrace ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException {
        final String CRLF = "\r\n";
        final StringBuilder buffer = new StringBuilder ( "TRACE " ).append ( req.getRequestURI() ).append ( " " ).append ( req.getProtocol() );
        final Enumeration<String> reqHeaderEnum = req.getHeaderNames();
        while ( reqHeaderEnum.hasMoreElements() ) {
            final String headerName = reqHeaderEnum.nextElement();
            buffer.append ( CRLF ).append ( headerName ).append ( ": " ).append ( req.getHeader ( headerName ) );
        }
        buffer.append ( CRLF );
        final int responseLength = buffer.length();
        resp.setContentType ( "message/http" );
        resp.setContentLength ( responseLength );
        final ServletOutputStream out = resp.getOutputStream();
        out.print ( buffer.toString() );
        out.close();
    }
    protected void service ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException {
        final String method = req.getMethod();
        if ( method.equals ( "GET" ) ) {
            final long lastModified = this.getLastModified ( req );
            if ( lastModified == -1L ) {
                this.doGet ( req, resp );
            } else {
                long ifModifiedSince;
                try {
                    ifModifiedSince = req.getDateHeader ( "If-Modified-Since" );
                } catch ( IllegalArgumentException iae ) {
                    ifModifiedSince = -1L;
                }
                if ( ifModifiedSince < lastModified / 1000L * 1000L ) {
                    this.maybeSetLastModified ( resp, lastModified );
                    this.doGet ( req, resp );
                } else {
                    resp.setStatus ( 304 );
                }
            }
        } else if ( method.equals ( "HEAD" ) ) {
            final long lastModified = this.getLastModified ( req );
            this.maybeSetLastModified ( resp, lastModified );
            this.doHead ( req, resp );
        } else if ( method.equals ( "POST" ) ) {
            this.doPost ( req, resp );
        } else if ( method.equals ( "PUT" ) ) {
            this.doPut ( req, resp );
        } else if ( method.equals ( "DELETE" ) ) {
            this.doDelete ( req, resp );
        } else if ( method.equals ( "OPTIONS" ) ) {
            this.doOptions ( req, resp );
        } else if ( method.equals ( "TRACE" ) ) {
            this.doTrace ( req, resp );
        } else {
            String errMsg = HttpServlet.lStrings.getString ( "http.method_not_implemented" );
            final Object[] errArgs = { method };
            errMsg = MessageFormat.format ( errMsg, errArgs );
            resp.sendError ( 501, errMsg );
        }
    }
    private void maybeSetLastModified ( final HttpServletResponse resp, final long lastModified ) {
        if ( resp.containsHeader ( "Last-Modified" ) ) {
            return;
        }
        if ( lastModified >= 0L ) {
            resp.setDateHeader ( "Last-Modified", lastModified );
        }
    }
    @Override
    public void service ( final ServletRequest req, final ServletResponse res ) throws ServletException, IOException {
        HttpServletRequest request;
        HttpServletResponse response;
        try {
            request = ( HttpServletRequest ) req;
            response = ( HttpServletResponse ) res;
        } catch ( ClassCastException e ) {
            throw new ServletException ( "non-HTTP request or response" );
        }
        this.service ( request, response );
    }
    static {
        lStrings = ResourceBundle.getBundle ( "javax.servlet.http.LocalStrings" );
    }
}
