package org.apache.catalina.ssi;
import java.io.InputStream;
import java.net.URLConnection;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import javax.servlet.ServletContext;
import java.util.Date;
import java.util.Locale;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
public class SSIServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    protected int debug;
    protected boolean buffered;
    protected Long expires;
    protected boolean isVirtualWebappRelative;
    protected String inputEncoding;
    protected String outputEncoding;
    protected boolean allowExec;
    public SSIServlet() {
        this.debug = 0;
        this.buffered = false;
        this.expires = null;
        this.isVirtualWebappRelative = false;
        this.inputEncoding = null;
        this.outputEncoding = "UTF-8";
        this.allowExec = false;
    }
    public void init() throws ServletException {
        if ( this.getServletConfig().getInitParameter ( "debug" ) != null ) {
            this.debug = Integer.parseInt ( this.getServletConfig().getInitParameter ( "debug" ) );
        }
        this.isVirtualWebappRelative = Boolean.parseBoolean ( this.getServletConfig().getInitParameter ( "isVirtualWebappRelative" ) );
        if ( this.getServletConfig().getInitParameter ( "expires" ) != null ) {
            this.expires = Long.valueOf ( this.getServletConfig().getInitParameter ( "expires" ) );
        }
        this.buffered = Boolean.parseBoolean ( this.getServletConfig().getInitParameter ( "buffered" ) );
        this.inputEncoding = this.getServletConfig().getInitParameter ( "inputEncoding" );
        if ( this.getServletConfig().getInitParameter ( "outputEncoding" ) != null ) {
            this.outputEncoding = this.getServletConfig().getInitParameter ( "outputEncoding" );
        }
        this.allowExec = Boolean.parseBoolean ( this.getServletConfig().getInitParameter ( "allowExec" ) );
        if ( this.debug > 0 ) {
            this.log ( "SSIServlet.init() SSI invoker started with 'debug'=" + this.debug );
        }
    }
    public void doGet ( final HttpServletRequest req, final HttpServletResponse res ) throws IOException, ServletException {
        if ( this.debug > 0 ) {
            this.log ( "SSIServlet.doGet()" );
        }
        this.requestHandler ( req, res );
    }
    public void doPost ( final HttpServletRequest req, final HttpServletResponse res ) throws IOException, ServletException {
        if ( this.debug > 0 ) {
            this.log ( "SSIServlet.doPost()" );
        }
        this.requestHandler ( req, res );
    }
    protected void requestHandler ( final HttpServletRequest req, final HttpServletResponse res ) throws IOException {
        final ServletContext servletContext = this.getServletContext();
        final String path = SSIServletRequestUtil.getRelativePath ( req );
        if ( this.debug > 0 ) {
            this.log ( "SSIServlet.requestHandler()\nServing " + ( this.buffered ? "buffered " : "unbuffered " ) + "resource '" + path + "'" );
        }
        if ( path == null || path.toUpperCase ( Locale.ENGLISH ).startsWith ( "/WEB-INF" ) || path.toUpperCase ( Locale.ENGLISH ).startsWith ( "/META-INF" ) ) {
            res.sendError ( 404, path );
            this.log ( "Can't serve file: " + path );
            return;
        }
        final URL resource = servletContext.getResource ( path );
        if ( resource == null ) {
            res.sendError ( 404, path );
            this.log ( "Can't find file: " + path );
            return;
        }
        String resourceMimeType = servletContext.getMimeType ( path );
        if ( resourceMimeType == null ) {
            resourceMimeType = "text/html";
        }
        res.setContentType ( resourceMimeType + ";charset=" + this.outputEncoding );
        if ( this.expires != null ) {
            res.setDateHeader ( "Expires", new Date().getTime() + this.expires * 1000L );
        }
        req.setAttribute ( "org.apache.catalina.ssi.SSIServlet", ( Object ) "true" );
        this.processSSI ( req, res, resource );
    }
    protected void processSSI ( final HttpServletRequest req, final HttpServletResponse res, final URL resource ) throws IOException {
        final SSIExternalResolver ssiExternalResolver = new SSIServletExternalResolver ( this.getServletContext(), req, res, this.isVirtualWebappRelative, this.debug, this.inputEncoding );
        final SSIProcessor ssiProcessor = new SSIProcessor ( ssiExternalResolver, this.debug, this.allowExec );
        PrintWriter printWriter = null;
        StringWriter stringWriter = null;
        if ( this.buffered ) {
            stringWriter = new StringWriter();
            printWriter = new PrintWriter ( stringWriter );
        } else {
            printWriter = res.getWriter();
        }
        final URLConnection resourceInfo = resource.openConnection();
        final InputStream resourceInputStream = resourceInfo.getInputStream();
        String encoding = resourceInfo.getContentEncoding();
        if ( encoding == null ) {
            encoding = this.inputEncoding;
        }
        InputStreamReader isr;
        if ( encoding == null ) {
            isr = new InputStreamReader ( resourceInputStream );
        } else {
            isr = new InputStreamReader ( resourceInputStream, encoding );
        }
        final BufferedReader bufferedReader = new BufferedReader ( isr );
        final long lastModified = ssiProcessor.process ( bufferedReader, resourceInfo.getLastModified(), printWriter );
        if ( lastModified > 0L ) {
            res.setDateHeader ( "last-modified", lastModified );
        }
        if ( this.buffered ) {
            printWriter.flush();
            final String text = stringWriter.toString();
            res.getWriter().write ( text );
        }
        bufferedReader.close();
    }
}
