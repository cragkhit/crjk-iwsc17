package org.apache.catalina.ssi;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Globals;
public class SSIServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    protected int debug = 0;
    protected boolean buffered = false;
    protected Long expires = null;
    protected boolean isVirtualWebappRelative = false;
    protected String inputEncoding = null;
    protected String outputEncoding = "UTF-8";
    protected boolean allowExec = false;
    @Override
    public void init() throws ServletException {
        if ( getServletConfig().getInitParameter ( "debug" ) != null ) {
            debug = Integer.parseInt ( getServletConfig().getInitParameter ( "debug" ) );
        }
        isVirtualWebappRelative =
            Boolean.parseBoolean ( getServletConfig().getInitParameter ( "isVirtualWebappRelative" ) );
        if ( getServletConfig().getInitParameter ( "expires" ) != null ) {
            expires = Long.valueOf ( getServletConfig().getInitParameter ( "expires" ) );
        }
        buffered = Boolean.parseBoolean ( getServletConfig().getInitParameter ( "buffered" ) );
        inputEncoding = getServletConfig().getInitParameter ( "inputEncoding" );
        if ( getServletConfig().getInitParameter ( "outputEncoding" ) != null ) {
            outputEncoding = getServletConfig().getInitParameter ( "outputEncoding" );
        }
        allowExec = Boolean.parseBoolean (
                        getServletConfig().getInitParameter ( "allowExec" ) );
        if ( debug > 0 ) {
            log ( "SSIServlet.init() SSI invoker started with 'debug'=" + debug );
        }
    }
    @Override
    public void doGet ( HttpServletRequest req, HttpServletResponse res )
    throws IOException, ServletException {
        if ( debug > 0 ) {
            log ( "SSIServlet.doGet()" );
        }
        requestHandler ( req, res );
    }
    @Override
    public void doPost ( HttpServletRequest req, HttpServletResponse res )
    throws IOException, ServletException {
        if ( debug > 0 ) {
            log ( "SSIServlet.doPost()" );
        }
        requestHandler ( req, res );
    }
    protected void requestHandler ( HttpServletRequest req,
                                    HttpServletResponse res ) throws IOException {
        ServletContext servletContext = getServletContext();
        String path = SSIServletRequestUtil.getRelativePath ( req );
        if ( debug > 0 )
            log ( "SSIServlet.requestHandler()\n" + "Serving "
                  + ( buffered ? "buffered " : "unbuffered " ) + "resource '"
                  + path + "'" );
        if ( path == null || path.toUpperCase ( Locale.ENGLISH ).startsWith ( "/WEB-INF" )
                || path.toUpperCase ( Locale.ENGLISH ).startsWith ( "/META-INF" ) ) {
            res.sendError ( HttpServletResponse.SC_NOT_FOUND, path );
            log ( "Can't serve file: " + path );
            return;
        }
        URL resource = servletContext.getResource ( path );
        if ( resource == null ) {
            res.sendError ( HttpServletResponse.SC_NOT_FOUND, path );
            log ( "Can't find file: " + path );
            return;
        }
        String resourceMimeType = servletContext.getMimeType ( path );
        if ( resourceMimeType == null ) {
            resourceMimeType = "text/html";
        }
        res.setContentType ( resourceMimeType + ";charset=" + outputEncoding );
        if ( expires != null ) {
            res.setDateHeader ( "Expires", ( new java.util.Date() ).getTime()
                                + expires.longValue() * 1000 );
        }
        req.setAttribute ( Globals.SSI_FLAG_ATTR, "true" );
        processSSI ( req, res, resource );
    }
    protected void processSSI ( HttpServletRequest req, HttpServletResponse res,
                                URL resource ) throws IOException {
        SSIExternalResolver ssiExternalResolver =
            new SSIServletExternalResolver ( getServletContext(), req, res,
                                             isVirtualWebappRelative, debug, inputEncoding );
        SSIProcessor ssiProcessor = new SSIProcessor ( ssiExternalResolver,
                debug, allowExec );
        PrintWriter printWriter = null;
        StringWriter stringWriter = null;
        if ( buffered ) {
            stringWriter = new StringWriter();
            printWriter = new PrintWriter ( stringWriter );
        } else {
            printWriter = res.getWriter();
        }
        URLConnection resourceInfo = resource.openConnection();
        InputStream resourceInputStream = resourceInfo.getInputStream();
        String encoding = resourceInfo.getContentEncoding();
        if ( encoding == null ) {
            encoding = inputEncoding;
        }
        InputStreamReader isr;
        if ( encoding == null ) {
            isr = new InputStreamReader ( resourceInputStream );
        } else {
            isr = new InputStreamReader ( resourceInputStream, encoding );
        }
        BufferedReader bufferedReader = new BufferedReader ( isr );
        long lastModified = ssiProcessor.process ( bufferedReader,
                            resourceInfo.getLastModified(), printWriter );
        if ( lastModified > 0 ) {
            res.setDateHeader ( "last-modified", lastModified );
        }
        if ( buffered ) {
            printWriter.flush();
            @SuppressWarnings ( "null" )
            String text = stringWriter.toString();
            res.getWriter().write ( text );
        }
        bufferedReader.close();
    }
}
