package org.apache.catalina.servlets;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.xml.sax.InputSource;
import org.xml.sax.ext.EntityResolver2;
import java.io.Serializable;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Node;
import javax.xml.transform.dom.DOMSource;
import org.xml.sax.EntityResolver;
import java.util.Locale;
import org.apache.catalina.Context;
import java.io.InputStreamReader;
import java.io.StringWriter;
import org.apache.catalina.util.ServerInfo;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import javax.xml.transform.Result;
import java.io.Writer;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.Reader;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import javax.xml.transform.TransformerFactory;
import org.apache.tomcat.util.security.PrivilegedSetTccl;
import java.security.PrivilegedAction;
import java.security.AccessController;
import org.apache.tomcat.util.security.PrivilegedGetTccl;
import org.apache.catalina.Globals;
import org.apache.catalina.util.RequestUtil;
import javax.xml.transform.Source;
import java.util.StringTokenizer;
import java.util.Enumeration;
import javax.servlet.ServletResponse;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import java.util.Iterator;
import java.util.Collection;
import org.apache.catalina.connector.ResponseFacade;
import javax.servlet.ServletResponseWrapper;
import java.io.FileNotFoundException;
import org.apache.catalina.util.URLEncoder;
import java.io.BufferedInputStream;
import java.io.RandomAccessFile;
import java.io.File;
import java.io.InputStream;
import org.apache.catalina.WebResource;
import java.io.FileInputStream;
import org.apache.catalina.connector.RequestFacade;
import javax.servlet.DispatcherType;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import org.apache.catalina.WebResourceRoot;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.tomcat.util.res.StringManager;
import javax.servlet.http.HttpServlet;
public class DefaultServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    protected static final StringManager sm;
    private static final DocumentBuilderFactory factory;
    private static final SecureEntityResolver secureEntityResolver;
    protected static final ArrayList<Range> FULL;
    protected static final String mimeSeparation = "CATALINA_MIME_BOUNDARY";
    protected static final int BUFFER_SIZE = 4096;
    protected int debug;
    protected int input;
    protected boolean listings;
    protected boolean readOnly;
    protected CompressionFormat[] compressionFormats;
    protected int output;
    protected String localXsltFile;
    protected String contextXsltFile;
    protected String globalXsltFile;
    protected String readmeFile;
    protected transient WebResourceRoot resources;
    protected String fileEncoding;
    protected int sendfileSize;
    protected boolean useAcceptRanges;
    protected boolean showServerInfo;
    public DefaultServlet() {
        this.debug = 0;
        this.input = 2048;
        this.listings = false;
        this.readOnly = true;
        this.output = 2048;
        this.localXsltFile = null;
        this.contextXsltFile = null;
        this.globalXsltFile = null;
        this.readmeFile = null;
        this.resources = null;
        this.fileEncoding = null;
        this.sendfileSize = 49152;
        this.useAcceptRanges = true;
        this.showServerInfo = true;
    }
    public void destroy() {
    }
    public void init() throws ServletException {
        if ( this.getServletConfig().getInitParameter ( "debug" ) != null ) {
            this.debug = Integer.parseInt ( this.getServletConfig().getInitParameter ( "debug" ) );
        }
        if ( this.getServletConfig().getInitParameter ( "input" ) != null ) {
            this.input = Integer.parseInt ( this.getServletConfig().getInitParameter ( "input" ) );
        }
        if ( this.getServletConfig().getInitParameter ( "output" ) != null ) {
            this.output = Integer.parseInt ( this.getServletConfig().getInitParameter ( "output" ) );
        }
        this.listings = Boolean.parseBoolean ( this.getServletConfig().getInitParameter ( "listings" ) );
        if ( this.getServletConfig().getInitParameter ( "readonly" ) != null ) {
            this.readOnly = Boolean.parseBoolean ( this.getServletConfig().getInitParameter ( "readonly" ) );
        }
        this.compressionFormats = this.parseCompressionFormats ( this.getServletConfig().getInitParameter ( "precompressed" ), this.getServletConfig().getInitParameter ( "gzip" ) );
        if ( this.getServletConfig().getInitParameter ( "sendfileSize" ) != null ) {
            this.sendfileSize = Integer.parseInt ( this.getServletConfig().getInitParameter ( "sendfileSize" ) ) * 1024;
        }
        this.fileEncoding = this.getServletConfig().getInitParameter ( "fileEncoding" );
        this.globalXsltFile = this.getServletConfig().getInitParameter ( "globalXsltFile" );
        this.contextXsltFile = this.getServletConfig().getInitParameter ( "contextXsltFile" );
        this.localXsltFile = this.getServletConfig().getInitParameter ( "localXsltFile" );
        this.readmeFile = this.getServletConfig().getInitParameter ( "readmeFile" );
        if ( this.getServletConfig().getInitParameter ( "useAcceptRanges" ) != null ) {
            this.useAcceptRanges = Boolean.parseBoolean ( this.getServletConfig().getInitParameter ( "useAcceptRanges" ) );
        }
        if ( this.input < 256 ) {
            this.input = 256;
        }
        if ( this.output < 256 ) {
            this.output = 256;
        }
        if ( this.debug > 0 ) {
            this.log ( "DefaultServlet.init:  input buffer size=" + this.input + ", output buffer size=" + this.output );
        }
        this.resources = ( WebResourceRoot ) this.getServletContext().getAttribute ( "org.apache.catalina.resources" );
        if ( this.resources == null ) {
            throw new UnavailableException ( "No resources" );
        }
        if ( this.getServletConfig().getInitParameter ( "showServerInfo" ) != null ) {
            this.showServerInfo = Boolean.parseBoolean ( this.getServletConfig().getInitParameter ( "showServerInfo" ) );
        }
    }
    private CompressionFormat[] parseCompressionFormats ( final String precompressed, final String gzip ) {
        final List<CompressionFormat> ret = new ArrayList<CompressionFormat>();
        if ( precompressed != null && precompressed.indexOf ( 61 ) > 0 ) {
            for ( final String pair : precompressed.split ( "," ) ) {
                final String[] setting = pair.split ( "=" );
                final String encoding = setting[0];
                final String extension = setting[1];
                ret.add ( new CompressionFormat ( extension, encoding ) );
            }
        } else if ( precompressed != null ) {
            if ( Boolean.parseBoolean ( precompressed ) ) {
                ret.add ( new CompressionFormat ( ".br", "br" ) );
                ret.add ( new CompressionFormat ( ".gz", "gzip" ) );
            }
        } else if ( Boolean.parseBoolean ( gzip ) ) {
            ret.add ( new CompressionFormat ( ".gz", "gzip" ) );
        }
        return ret.toArray ( new CompressionFormat[ret.size()] );
    }
    protected String getRelativePath ( final HttpServletRequest request ) {
        return this.getRelativePath ( request, false );
    }
    protected String getRelativePath ( final HttpServletRequest request, final boolean allowEmptyPath ) {
        String pathInfo;
        String servletPath;
        if ( request.getAttribute ( "javax.servlet.include.request_uri" ) != null ) {
            pathInfo = ( String ) request.getAttribute ( "javax.servlet.include.path_info" );
            servletPath = ( String ) request.getAttribute ( "javax.servlet.include.servlet_path" );
        } else {
            pathInfo = request.getPathInfo();
            servletPath = request.getServletPath();
        }
        final StringBuilder result = new StringBuilder();
        if ( servletPath.length() > 0 ) {
            result.append ( servletPath );
        }
        if ( pathInfo != null ) {
            result.append ( pathInfo );
        }
        if ( result.length() == 0 && !allowEmptyPath ) {
            result.append ( '/' );
        }
        return result.toString();
    }
    protected String getPathPrefix ( final HttpServletRequest request ) {
        return request.getContextPath();
    }
    protected void doGet ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException {
        this.serveResource ( request, response, true, this.fileEncoding );
    }
    protected void doHead ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException {
        final boolean serveContent = DispatcherType.INCLUDE.equals ( ( Object ) request.getDispatcherType() );
        this.serveResource ( request, response, serveContent, this.fileEncoding );
    }
    protected void doOptions ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException {
        final StringBuilder allow = new StringBuilder();
        allow.append ( "GET, HEAD" );
        allow.append ( ", POST" );
        allow.append ( ", PUT" );
        allow.append ( ", DELETE" );
        if ( req instanceof RequestFacade && ( ( RequestFacade ) req ).getAllowTrace() ) {
            allow.append ( ", TRACE" );
        }
        allow.append ( ", OPTIONS" );
        resp.setHeader ( "Allow", allow.toString() );
    }
    protected void doPost ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException {
        this.doGet ( request, response );
    }
    protected void doPut ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException {
        if ( this.readOnly ) {
            resp.sendError ( 403 );
            return;
        }
        final String path = this.getRelativePath ( req );
        final WebResource resource = this.resources.getResource ( path );
        final Range range = this.parseContentRange ( req, resp );
        InputStream resourceInputStream = null;
        try {
            if ( range != null ) {
                final File contentFile = this.executePartialPut ( req, range, path );
                resourceInputStream = new FileInputStream ( contentFile );
            } else {
                resourceInputStream = ( InputStream ) req.getInputStream();
            }
            if ( this.resources.write ( path, resourceInputStream, true ) ) {
                if ( resource.exists() ) {
                    resp.setStatus ( 204 );
                } else {
                    resp.setStatus ( 201 );
                }
            } else {
                resp.sendError ( 409 );
            }
        } finally {
            if ( resourceInputStream != null ) {
                try {
                    resourceInputStream.close();
                } catch ( IOException ex ) {}
            }
        }
    }
    protected File executePartialPut ( final HttpServletRequest req, final Range range, final String path ) throws IOException {
        final File tempDir = ( File ) this.getServletContext().getAttribute ( "javax.servlet.context.tempdir" );
        final String convertedResourcePath = path.replace ( '/', '.' );
        final File contentFile = new File ( tempDir, convertedResourcePath );
        if ( contentFile.createNewFile() ) {
            contentFile.deleteOnExit();
        }
        try ( final RandomAccessFile randAccessContentFile = new RandomAccessFile ( contentFile, "rw" ) ) {
            final WebResource oldResource = this.resources.getResource ( path );
            if ( oldResource.isFile() ) {
                try ( final BufferedInputStream bufOldRevStream = new BufferedInputStream ( oldResource.getInputStream(), 4096 ) ) {
                    final byte[] copyBuffer = new byte[4096];
                    int numBytesRead;
                    while ( ( numBytesRead = bufOldRevStream.read ( copyBuffer ) ) != -1 ) {
                        randAccessContentFile.write ( copyBuffer, 0, numBytesRead );
                    }
                }
            }
            randAccessContentFile.setLength ( range.length );
            randAccessContentFile.seek ( range.start );
            final byte[] transferBuffer = new byte[4096];
            try ( final BufferedInputStream requestBufInStream = new BufferedInputStream ( ( InputStream ) req.getInputStream(), 4096 ) ) {
                int numBytesRead2;
                while ( ( numBytesRead2 = requestBufInStream.read ( transferBuffer ) ) != -1 ) {
                    randAccessContentFile.write ( transferBuffer, 0, numBytesRead2 );
                }
            }
        }
        return contentFile;
    }
    protected void doDelete ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException {
        if ( this.readOnly ) {
            resp.sendError ( 403 );
            return;
        }
        final String path = this.getRelativePath ( req );
        final WebResource resource = this.resources.getResource ( path );
        if ( resource.exists() ) {
            if ( resource.delete() ) {
                resp.setStatus ( 204 );
            } else {
                resp.sendError ( 405 );
            }
        } else {
            resp.sendError ( 404 );
        }
    }
    protected boolean checkIfHeaders ( final HttpServletRequest request, final HttpServletResponse response, final WebResource resource ) throws IOException {
        return this.checkIfMatch ( request, response, resource ) && this.checkIfModifiedSince ( request, response, resource ) && this.checkIfNoneMatch ( request, response, resource ) && this.checkIfUnmodifiedSince ( request, response, resource );
    }
    protected String rewriteUrl ( final String path ) {
        return URLEncoder.DEFAULT.encode ( path, "UTF-8" );
    }
    protected void serveResource ( final HttpServletRequest request, final HttpServletResponse response, final boolean content, final String encoding ) throws IOException, ServletException {
        boolean serveContent = content;
        final String path = this.getRelativePath ( request, true );
        if ( this.debug > 0 ) {
            if ( serveContent ) {
                this.log ( "DefaultServlet.serveResource:  Serving resource '" + path + "' headers and data" );
            } else {
                this.log ( "DefaultServlet.serveResource:  Serving resource '" + path + "' headers only" );
            }
        }
        if ( path.length() == 0 ) {
            this.doDirectoryRedirect ( request, response );
            return;
        }
        WebResource resource = this.resources.getResource ( path );
        if ( !resource.exists() ) {
            String requestUri = ( String ) request.getAttribute ( "javax.servlet.include.request_uri" );
            if ( requestUri == null ) {
                requestUri = request.getRequestURI();
                response.sendError ( 404, requestUri );
                return;
            }
            throw new FileNotFoundException ( DefaultServlet.sm.getString ( "defaultServlet.missingResource", requestUri ) );
        } else if ( !resource.canRead() ) {
            String requestUri = ( String ) request.getAttribute ( "javax.servlet.include.request_uri" );
            if ( requestUri == null ) {
                requestUri = request.getRequestURI();
                response.sendError ( 403, requestUri );
                return;
            }
            throw new FileNotFoundException ( DefaultServlet.sm.getString ( "defaultServlet.missingResource", requestUri ) );
        } else {
            if ( resource.isFile() && ( path.endsWith ( "/" ) || path.endsWith ( "\\" ) ) ) {
                String requestUri = ( String ) request.getAttribute ( "javax.servlet.include.request_uri" );
                if ( requestUri == null ) {
                    requestUri = request.getRequestURI();
                }
                response.sendError ( 404, requestUri );
                return;
            }
            final boolean isError = response.getStatus() >= 400;
            boolean included = false;
            if ( resource.isFile() ) {
                included = ( request.getAttribute ( "javax.servlet.include.context_path" ) != null );
                if ( !included && !isError && !this.checkIfHeaders ( request, response, resource ) ) {
                    return;
                }
            }
            String contentType = resource.getMimeType();
            if ( contentType == null ) {
                contentType = this.getServletContext().getMimeType ( resource.getName() );
                resource.setMimeType ( contentType );
            }
            String eTag = null;
            String lastModifiedHttp = null;
            if ( resource.isFile() && !isError ) {
                eTag = resource.getETag();
                lastModifiedHttp = resource.getLastModifiedHttp();
            }
            boolean usingPrecompressedVersion = false;
            if ( this.compressionFormats.length > 0 && !included && resource.isFile() && !this.pathEndsWithCompressedExtension ( path ) ) {
                final List<PrecompressedResource> precompressedResources = this.getAvailablePrecompressedResources ( path );
                if ( !precompressedResources.isEmpty() ) {
                    final Collection<String> varyHeaders = ( Collection<String> ) response.getHeaders ( "Vary" );
                    boolean addRequired = true;
                    for ( final String varyHeader : varyHeaders ) {
                        if ( "*".equals ( varyHeader ) || "accept-encoding".equalsIgnoreCase ( varyHeader ) ) {
                            addRequired = false;
                            break;
                        }
                    }
                    if ( addRequired ) {
                        response.addHeader ( "Vary", "accept-encoding" );
                    }
                    final PrecompressedResource bestResource = this.getBestPrecompressedResource ( request, precompressedResources );
                    if ( bestResource != null ) {
                        response.addHeader ( "Content-Encoding", bestResource.format.encoding );
                        resource = bestResource.resource;
                        usingPrecompressedVersion = true;
                    }
                }
            }
            ArrayList<Range> ranges = null;
            long contentLength = -1L;
            if ( resource.isDirectory() ) {
                if ( !path.endsWith ( "/" ) ) {
                    this.doDirectoryRedirect ( request, response );
                    return;
                }
                if ( !this.listings ) {
                    response.sendError ( 404, request.getRequestURI() );
                    return;
                }
                contentType = "text/html;charset=UTF-8";
            } else {
                if ( !isError ) {
                    if ( this.useAcceptRanges ) {
                        response.setHeader ( "Accept-Ranges", "bytes" );
                    }
                    ranges = this.parseRange ( request, response, resource );
                    response.setHeader ( "ETag", eTag );
                    response.setHeader ( "Last-Modified", lastModifiedHttp );
                }
                contentLength = resource.getContentLength();
                if ( contentLength == 0L ) {
                    serveContent = false;
                }
            }
            ServletOutputStream ostream = null;
            PrintWriter writer = null;
            if ( serveContent ) {
                try {
                    ostream = response.getOutputStream();
                } catch ( IllegalStateException e ) {
                    if ( usingPrecompressedVersion || ( contentType != null && !contentType.startsWith ( "text" ) && !contentType.endsWith ( "xml" ) && !contentType.contains ( "/javascript" ) ) ) {
                        throw e;
                    }
                    writer = response.getWriter();
                    ranges = DefaultServlet.FULL;
                }
            }
            ServletResponse r = ( ServletResponse ) response;
            long contentWritten = 0L;
            while ( r instanceof ServletResponseWrapper ) {
                r = ( ( ServletResponseWrapper ) r ).getResponse();
            }
            if ( r instanceof ResponseFacade ) {
                contentWritten = ( ( ResponseFacade ) r ).getContentWritten();
            }
            if ( contentWritten > 0L ) {
                ranges = DefaultServlet.FULL;
            }
            if ( resource.isDirectory() || isError || ( ( ranges == null || ranges.isEmpty() ) && request.getHeader ( "Range" ) == null ) || ranges == DefaultServlet.FULL ) {
                if ( contentType != null ) {
                    if ( this.debug > 0 ) {
                        this.log ( "DefaultServlet.serveFile:  contentType='" + contentType + "'" );
                    }
                    response.setContentType ( contentType );
                }
                if ( resource.isFile() && contentLength >= 0L && ( !serveContent || ostream != null ) ) {
                    if ( this.debug > 0 ) {
                        this.log ( "DefaultServlet.serveFile:  contentLength=" + contentLength );
                    }
                    if ( contentWritten == 0L ) {
                        response.setContentLengthLong ( contentLength );
                    }
                }
                if ( serveContent ) {
                    try {
                        response.setBufferSize ( this.output );
                    } catch ( IllegalStateException ex ) {}
                    InputStream renderResult = null;
                    if ( ostream == null ) {
                        if ( resource.isDirectory() ) {
                            renderResult = this.render ( this.getPathPrefix ( request ), resource, encoding );
                        } else {
                            renderResult = resource.getInputStream();
                        }
                        this.copy ( resource, renderResult, writer, encoding );
                    } else {
                        if ( resource.isDirectory() ) {
                            renderResult = this.render ( this.getPathPrefix ( request ), resource, encoding );
                        } else if ( !this.checkSendfile ( request, response, resource, contentLength, null ) ) {
                            final byte[] resourceBody = resource.getContent();
                            if ( resourceBody == null ) {
                                renderResult = resource.getInputStream();
                            } else {
                                ostream.write ( resourceBody );
                            }
                        }
                        if ( renderResult != null ) {
                            this.copy ( resource, renderResult, ostream );
                        }
                    }
                }
            } else {
                if ( ranges == null || ranges.isEmpty() ) {
                    return;
                }
                response.setStatus ( 206 );
                if ( ranges.size() == 1 ) {
                    final Range range = ranges.get ( 0 );
                    response.addHeader ( "Content-Range", "bytes " + range.start + "-" + range.end + "/" + range.length );
                    final long length = range.end - range.start + 1L;
                    response.setContentLengthLong ( length );
                    if ( contentType != null ) {
                        if ( this.debug > 0 ) {
                            this.log ( "DefaultServlet.serveFile:  contentType='" + contentType + "'" );
                        }
                        response.setContentType ( contentType );
                    }
                    if ( serveContent ) {
                        try {
                            response.setBufferSize ( this.output );
                        } catch ( IllegalStateException ex2 ) {}
                        if ( ostream == null ) {
                            throw new IllegalStateException();
                        }
                        if ( !this.checkSendfile ( request, response, resource, range.end - range.start + 1L, range ) ) {
                            this.copy ( resource, ostream, range );
                        }
                    }
                } else {
                    response.setContentType ( "multipart/byteranges; boundary=CATALINA_MIME_BOUNDARY" );
                    if ( serveContent ) {
                        try {
                            response.setBufferSize ( this.output );
                        } catch ( IllegalStateException ex3 ) {}
                        if ( ostream == null ) {
                            throw new IllegalStateException();
                        }
                        this.copy ( resource, ostream, ranges.iterator(), contentType );
                    }
                }
            }
        }
    }
    private boolean pathEndsWithCompressedExtension ( final String path ) {
        for ( final CompressionFormat format : this.compressionFormats ) {
            if ( path.endsWith ( format.extension ) ) {
                return true;
            }
        }
        return false;
    }
    private List<PrecompressedResource> getAvailablePrecompressedResources ( final String path ) {
        final List<PrecompressedResource> ret = new ArrayList<PrecompressedResource> ( this.compressionFormats.length );
        for ( final CompressionFormat format : this.compressionFormats ) {
            final WebResource precompressedResource = this.resources.getResource ( path + format.extension );
            if ( precompressedResource.exists() && precompressedResource.isFile() ) {
                ret.add ( new PrecompressedResource ( precompressedResource, format ) );
            }
        }
        return ret;
    }
    private PrecompressedResource getBestPrecompressedResource ( final HttpServletRequest request, final List<PrecompressedResource> precompressedResources ) {
        final Enumeration<String> headers = ( Enumeration<String> ) request.getHeaders ( "Accept-Encoding" );
        PrecompressedResource bestResource = null;
        double bestResourceQuality = 0.0;
        int bestResourcePreference = Integer.MAX_VALUE;
        while ( headers.hasMoreElements() ) {
            final String header = headers.nextElement();
            for ( final String preference : header.split ( "," ) ) {
                double quality = 1.0;
                final int qualityIdx = preference.indexOf ( 59 );
                Label_0295: {
                    if ( qualityIdx > 0 ) {
                        final int equalsIdx = preference.indexOf ( 61, qualityIdx + 1 );
                        if ( equalsIdx == -1 ) {
                            break Label_0295;
                        }
                        quality = Double.parseDouble ( preference.substring ( equalsIdx + 1 ).trim() );
                    }
                    if ( quality >= bestResourceQuality ) {
                        String encoding = preference;
                        if ( qualityIdx > 0 ) {
                            encoding = encoding.substring ( 0, qualityIdx );
                        }
                        encoding = encoding.trim();
                        if ( "identity".equals ( encoding ) ) {
                            bestResource = null;
                            bestResourceQuality = quality;
                            bestResourcePreference = Integer.MAX_VALUE;
                        } else if ( "*".equals ( encoding ) ) {
                            bestResource = precompressedResources.get ( 0 );
                            bestResourceQuality = quality;
                            bestResourcePreference = 0;
                        } else {
                            int i = 0;
                            while ( i < precompressedResources.size() ) {
                                final PrecompressedResource resource = precompressedResources.get ( i );
                                if ( encoding.equals ( resource.format.encoding ) ) {
                                    if ( quality > bestResourceQuality || i < bestResourcePreference ) {
                                        bestResource = resource;
                                        bestResourceQuality = quality;
                                        bestResourcePreference = i;
                                        break;
                                    }
                                    break;
                                } else {
                                    ++i;
                                }
                            }
                        }
                    }
                }
            }
        }
        return bestResource;
    }
    private void doDirectoryRedirect ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException {
        final StringBuilder location = new StringBuilder ( request.getRequestURI() );
        location.append ( '/' );
        if ( request.getQueryString() != null ) {
            location.append ( '?' );
            location.append ( request.getQueryString() );
        }
        response.sendRedirect ( response.encodeRedirectURL ( location.toString() ) );
    }
    protected Range parseContentRange ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException {
        String rangeHeader = request.getHeader ( "Content-Range" );
        if ( rangeHeader == null ) {
            return null;
        }
        if ( !rangeHeader.startsWith ( "bytes" ) ) {
            response.sendError ( 400 );
            return null;
        }
        rangeHeader = rangeHeader.substring ( 6 ).trim();
        final int dashPos = rangeHeader.indexOf ( 45 );
        final int slashPos = rangeHeader.indexOf ( 47 );
        if ( dashPos == -1 ) {
            response.sendError ( 400 );
            return null;
        }
        if ( slashPos == -1 ) {
            response.sendError ( 400 );
            return null;
        }
        final Range range = new Range();
        try {
            range.start = Long.parseLong ( rangeHeader.substring ( 0, dashPos ) );
            range.end = Long.parseLong ( rangeHeader.substring ( dashPos + 1, slashPos ) );
            range.length = Long.parseLong ( rangeHeader.substring ( slashPos + 1, rangeHeader.length() ) );
        } catch ( NumberFormatException e ) {
            response.sendError ( 400 );
            return null;
        }
        if ( !range.validate() ) {
            response.sendError ( 400 );
            return null;
        }
        return range;
    }
    protected ArrayList<Range> parseRange ( final HttpServletRequest request, final HttpServletResponse response, final WebResource resource ) throws IOException {
        final String headerValue = request.getHeader ( "If-Range" );
        if ( headerValue != null ) {
            long headerValueTime = -1L;
            try {
                headerValueTime = request.getDateHeader ( "If-Range" );
            } catch ( IllegalArgumentException ex ) {}
            final String eTag = resource.getETag();
            final long lastModified = resource.getLastModified();
            if ( headerValueTime == -1L ) {
                if ( !eTag.equals ( headerValue.trim() ) ) {
                    return DefaultServlet.FULL;
                }
            } else if ( lastModified > headerValueTime + 1000L ) {
                return DefaultServlet.FULL;
            }
        }
        final long fileLength = resource.getContentLength();
        if ( fileLength == 0L ) {
            return null;
        }
        String rangeHeader = request.getHeader ( "Range" );
        if ( rangeHeader == null ) {
            return null;
        }
        if ( !rangeHeader.startsWith ( "bytes" ) ) {
            response.addHeader ( "Content-Range", "bytes */" + fileLength );
            response.sendError ( 416 );
            return null;
        }
        rangeHeader = rangeHeader.substring ( 6 );
        final ArrayList<Range> result = new ArrayList<Range>();
        final StringTokenizer commaTokenizer = new StringTokenizer ( rangeHeader, "," );
        while ( commaTokenizer.hasMoreTokens() ) {
            final String rangeDefinition = commaTokenizer.nextToken().trim();
            final Range currentRange = new Range();
            currentRange.length = fileLength;
            final int dashPos = rangeDefinition.indexOf ( 45 );
            if ( dashPos == -1 ) {
                response.addHeader ( "Content-Range", "bytes */" + fileLength );
                response.sendError ( 416 );
                return null;
            }
            Label_0482: {
                if ( dashPos == 0 ) {
                    try {
                        final long offset = Long.parseLong ( rangeDefinition );
                        currentRange.start = fileLength + offset;
                        currentRange.end = fileLength - 1L;
                        break Label_0482;
                    } catch ( NumberFormatException e ) {
                        response.addHeader ( "Content-Range", "bytes */" + fileLength );
                        response.sendError ( 416 );
                        return null;
                    }
                }
                try {
                    currentRange.start = Long.parseLong ( rangeDefinition.substring ( 0, dashPos ) );
                    if ( dashPos < rangeDefinition.length() - 1 ) {
                        currentRange.end = Long.parseLong ( rangeDefinition.substring ( dashPos + 1, rangeDefinition.length() ) );
                    } else {
                        currentRange.end = fileLength - 1L;
                    }
                } catch ( NumberFormatException e ) {
                    response.addHeader ( "Content-Range", "bytes */" + fileLength );
                    response.sendError ( 416 );
                    return null;
                }
            }
            if ( !currentRange.validate() ) {
                response.addHeader ( "Content-Range", "bytes */" + fileLength );
                response.sendError ( 416 );
                return null;
            }
            result.add ( currentRange );
        }
        return result;
    }
    protected InputStream render ( final String contextPath, final WebResource resource, final String encoding ) throws IOException, ServletException {
        final Source xsltSource = this.findXsltSource ( resource );
        if ( xsltSource == null ) {
            return this.renderHtml ( contextPath, resource, encoding );
        }
        return this.renderXml ( contextPath, resource, xsltSource, encoding );
    }
    protected InputStream renderXml ( final String contextPath, final WebResource resource, final Source xsltSource, final String encoding ) throws IOException, ServletException {
        final StringBuilder sb = new StringBuilder();
        sb.append ( "<?xml version=\"1.0\"?>" );
        sb.append ( "<listing " );
        sb.append ( " contextPath='" );
        sb.append ( contextPath );
        sb.append ( "'" );
        sb.append ( " directory='" );
        sb.append ( resource.getName() );
        sb.append ( "' " );
        sb.append ( " hasParent='" ).append ( !resource.getName().equals ( "/" ) );
        sb.append ( "'>" );
        sb.append ( "<entries>" );
        final String[] entries = this.resources.list ( resource.getWebappPath() );
        final String rewrittenContextPath = this.rewriteUrl ( contextPath );
        final String directoryWebappPath = resource.getWebappPath();
        for ( final String entry : entries ) {
            if ( !entry.equalsIgnoreCase ( "WEB-INF" ) && !entry.equalsIgnoreCase ( "META-INF" ) ) {
                if ( !entry.equalsIgnoreCase ( this.localXsltFile ) ) {
                    if ( ! ( directoryWebappPath + entry ).equals ( this.contextXsltFile ) ) {
                        final WebResource childResource = this.resources.getResource ( directoryWebappPath + entry );
                        if ( childResource.exists() ) {
                            sb.append ( "<entry" );
                            sb.append ( " type='" ).append ( childResource.isDirectory() ? "dir" : "file" ).append ( "'" );
                            sb.append ( " urlPath='" ).append ( rewrittenContextPath ).append ( this.rewriteUrl ( directoryWebappPath + entry ) ).append ( childResource.isDirectory() ? "/" : "" ).append ( "'" );
                            if ( childResource.isFile() ) {
                                sb.append ( " size='" ).append ( this.renderSize ( childResource.getContentLength() ) ).append ( "'" );
                            }
                            sb.append ( " date='" ).append ( childResource.getLastModifiedHttp() ).append ( "'" );
                            sb.append ( ">" );
                            sb.append ( RequestUtil.filter ( entry ) );
                            if ( childResource.isDirectory() ) {
                                sb.append ( "/" );
                            }
                            sb.append ( "</entry>" );
                        }
                    }
                }
            }
        }
        sb.append ( "</entries>" );
        final String readme = this.getReadme ( resource, encoding );
        if ( readme != null ) {
            sb.append ( "<readme><![CDATA[" );
            sb.append ( readme );
            sb.append ( "]]></readme>" );
        }
        sb.append ( "</listing>" );
        ClassLoader original;
        if ( Globals.IS_SECURITY_ENABLED ) {
            final PrivilegedGetTccl pa = new PrivilegedGetTccl();
            original = AccessController.doPrivileged ( ( PrivilegedAction<ClassLoader> ) pa );
        } else {
            original = Thread.currentThread().getContextClassLoader();
        }
        try {
            if ( Globals.IS_SECURITY_ENABLED ) {
                final PrivilegedSetTccl pa2 = new PrivilegedSetTccl ( DefaultServlet.class.getClassLoader() );
                AccessController.doPrivileged ( ( PrivilegedAction<Object> ) pa2 );
            } else {
                Thread.currentThread().setContextClassLoader ( DefaultServlet.class.getClassLoader() );
            }
            final TransformerFactory tFactory = TransformerFactory.newInstance();
            final Source xmlSource = new StreamSource ( new StringReader ( sb.toString() ) );
            final Transformer transformer = tFactory.newTransformer ( xsltSource );
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            final OutputStreamWriter osWriter = new OutputStreamWriter ( stream, "UTF8" );
            final StreamResult out = new StreamResult ( osWriter );
            transformer.transform ( xmlSource, out );
            osWriter.flush();
            return new ByteArrayInputStream ( stream.toByteArray() );
        } catch ( TransformerException e ) {
            throw new ServletException ( "XSL transformer error", ( Throwable ) e );
        } finally {
            if ( Globals.IS_SECURITY_ENABLED ) {
                final PrivilegedSetTccl pa3 = new PrivilegedSetTccl ( original );
                AccessController.doPrivileged ( ( PrivilegedAction<Object> ) pa3 );
            } else {
                Thread.currentThread().setContextClassLoader ( original );
            }
        }
    }
    protected InputStream renderHtml ( final String contextPath, final WebResource resource, final String encoding ) throws IOException {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final OutputStreamWriter osWriter = new OutputStreamWriter ( stream, "UTF8" );
        final PrintWriter writer = new PrintWriter ( osWriter );
        final StringBuilder sb = new StringBuilder();
        final String[] entries = this.resources.list ( resource.getWebappPath() );
        final String rewrittenContextPath = this.rewriteUrl ( contextPath );
        final String directoryWebappPath = resource.getWebappPath();
        sb.append ( "<html>\r\n" );
        sb.append ( "<head>\r\n" );
        sb.append ( "<title>" );
        sb.append ( DefaultServlet.sm.getString ( "directory.title", directoryWebappPath ) );
        sb.append ( "</title>\r\n" );
        sb.append ( "<STYLE><!--" );
        sb.append ( "h1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} h2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} h3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} body {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} b {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;} p {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;} a {color:black;} a.name {color:black;} .line {height:1px;background-color:#525D76;border:none;}" );
        sb.append ( "--></STYLE> " );
        sb.append ( "</head>\r\n" );
        sb.append ( "<body>" );
        sb.append ( "<h1>" );
        sb.append ( DefaultServlet.sm.getString ( "directory.title", directoryWebappPath ) );
        String parentDirectory = directoryWebappPath;
        if ( parentDirectory.endsWith ( "/" ) ) {
            parentDirectory = parentDirectory.substring ( 0, parentDirectory.length() - 1 );
        }
        final int slash = parentDirectory.lastIndexOf ( 47 );
        if ( slash >= 0 ) {
            String parent = directoryWebappPath.substring ( 0, slash );
            sb.append ( " - <a href=\"" );
            sb.append ( rewrittenContextPath );
            if ( parent.equals ( "" ) ) {
                parent = "/";
            }
            sb.append ( this.rewriteUrl ( parent ) );
            if ( !parent.endsWith ( "/" ) ) {
                sb.append ( "/" );
            }
            sb.append ( "\">" );
            sb.append ( "<b>" );
            sb.append ( DefaultServlet.sm.getString ( "directory.parent", parent ) );
            sb.append ( "</b>" );
            sb.append ( "</a>" );
        }
        sb.append ( "</h1>" );
        sb.append ( "<HR size=\"1\" noshade=\"noshade\">" );
        sb.append ( "<table width=\"100%\" cellspacing=\"0\" cellpadding=\"5\" align=\"center\">\r\n" );
        sb.append ( "<tr>\r\n" );
        sb.append ( "<td align=\"left\"><font size=\"+1\"><strong>" );
        sb.append ( DefaultServlet.sm.getString ( "directory.filename" ) );
        sb.append ( "</strong></font></td>\r\n" );
        sb.append ( "<td align=\"center\"><font size=\"+1\"><strong>" );
        sb.append ( DefaultServlet.sm.getString ( "directory.size" ) );
        sb.append ( "</strong></font></td>\r\n" );
        sb.append ( "<td align=\"right\"><font size=\"+1\"><strong>" );
        sb.append ( DefaultServlet.sm.getString ( "directory.lastModified" ) );
        sb.append ( "</strong></font></td>\r\n" );
        sb.append ( "</tr>" );
        boolean shade = false;
        for ( final String entry : entries ) {
            if ( !entry.equalsIgnoreCase ( "WEB-INF" ) ) {
                if ( !entry.equalsIgnoreCase ( "META-INF" ) ) {
                    final WebResource childResource = this.resources.getResource ( directoryWebappPath + entry );
                    if ( childResource.exists() ) {
                        sb.append ( "<tr" );
                        if ( shade ) {
                            sb.append ( " bgcolor=\"#eeeeee\"" );
                        }
                        sb.append ( ">\r\n" );
                        shade = !shade;
                        sb.append ( "<td align=\"left\">&nbsp;&nbsp;\r\n" );
                        sb.append ( "<a href=\"" );
                        sb.append ( rewrittenContextPath );
                        sb.append ( this.rewriteUrl ( directoryWebappPath + entry ) );
                        if ( childResource.isDirectory() ) {
                            sb.append ( "/" );
                        }
                        sb.append ( "\"><tt>" );
                        sb.append ( RequestUtil.filter ( entry ) );
                        if ( childResource.isDirectory() ) {
                            sb.append ( "/" );
                        }
                        sb.append ( "</tt></a></td>\r\n" );
                        sb.append ( "<td align=\"right\"><tt>" );
                        if ( childResource.isDirectory() ) {
                            sb.append ( "&nbsp;" );
                        } else {
                            sb.append ( this.renderSize ( childResource.getContentLength() ) );
                        }
                        sb.append ( "</tt></td>\r\n" );
                        sb.append ( "<td align=\"right\"><tt>" );
                        sb.append ( childResource.getLastModifiedHttp() );
                        sb.append ( "</tt></td>\r\n" );
                        sb.append ( "</tr>\r\n" );
                    }
                }
            }
        }
        sb.append ( "</table>\r\n" );
        sb.append ( "<HR size=\"1\" noshade=\"noshade\">" );
        final String readme = this.getReadme ( resource, encoding );
        if ( readme != null ) {
            sb.append ( readme );
            sb.append ( "<HR size=\"1\" noshade=\"noshade\">" );
        }
        if ( this.showServerInfo ) {
            sb.append ( "<h3>" ).append ( ServerInfo.getServerInfo() ).append ( "</h3>" );
        }
        sb.append ( "</body>\r\n" );
        sb.append ( "</html>\r\n" );
        writer.write ( sb.toString() );
        writer.flush();
        return new ByteArrayInputStream ( stream.toByteArray() );
    }
    protected String renderSize ( final long size ) {
        final long leftSide = size / 1024L;
        long rightSide = size % 1024L / 103L;
        if ( leftSide == 0L && rightSide == 0L && size > 0L ) {
            rightSide = 1L;
        }
        return "" + leftSide + "." + rightSide + " kb";
    }
    protected String getReadme ( final WebResource directory, final String encoding ) {
        if ( this.readmeFile == null ) {
            return null;
        }
        final WebResource resource = this.resources.getResource ( directory.getWebappPath() + this.readmeFile );
        if ( resource.isFile() ) {
            final StringWriter buffer = new StringWriter();
            InputStreamReader reader = null;
            try ( final InputStream is = resource.getInputStream() ) {
                if ( encoding != null ) {
                    reader = new InputStreamReader ( is, encoding );
                } else {
                    reader = new InputStreamReader ( is );
                }
                this.copyRange ( reader, new PrintWriter ( buffer ) );
            } catch ( IOException e ) {
                this.log ( "Failure to close reader", ( Throwable ) e );
            } finally {
                if ( reader != null ) {
                    try {
                        reader.close();
                    } catch ( IOException ex ) {}
                }
            }
            return buffer.toString();
        }
        if ( this.debug > 10 ) {
            this.log ( "readme '" + this.readmeFile + "' not found" );
        }
        return null;
    }
    protected Source findXsltSource ( final WebResource directory ) throws IOException {
        if ( this.localXsltFile != null ) {
            final WebResource resource = this.resources.getResource ( directory.getWebappPath() + this.localXsltFile );
            if ( resource.isFile() ) {
                final InputStream is = resource.getInputStream();
                if ( is != null ) {
                    if ( Globals.IS_SECURITY_ENABLED ) {
                        return this.secureXslt ( is );
                    }
                    return new StreamSource ( is );
                }
            }
            if ( this.debug > 10 ) {
                this.log ( "localXsltFile '" + this.localXsltFile + "' not found" );
            }
        }
        if ( this.contextXsltFile != null ) {
            final InputStream is2 = this.getServletContext().getResourceAsStream ( this.contextXsltFile );
            if ( is2 != null ) {
                if ( Globals.IS_SECURITY_ENABLED ) {
                    return this.secureXslt ( is2 );
                }
                return new StreamSource ( is2 );
            } else if ( this.debug > 10 ) {
                this.log ( "contextXsltFile '" + this.contextXsltFile + "' not found" );
            }
        }
        if ( this.globalXsltFile != null ) {
            final File f = this.validateGlobalXsltFile();
            if ( f != null ) {
                try ( final FileInputStream fis = new FileInputStream ( f ) ) {
                    final byte[] b = new byte[ ( int ) f.length()];
                    fis.read ( b );
                    return new StreamSource ( new ByteArrayInputStream ( b ) );
                }
            }
        }
        return null;
    }
    private File validateGlobalXsltFile() {
        final Context context = this.resources.getContext();
        final File baseConf = new File ( context.getCatalinaBase(), "conf" );
        File result = this.validateGlobalXsltFile ( baseConf );
        if ( result == null ) {
            final File homeConf = new File ( context.getCatalinaHome(), "conf" );
            if ( !baseConf.equals ( homeConf ) ) {
                result = this.validateGlobalXsltFile ( homeConf );
            }
        }
        return result;
    }
    private File validateGlobalXsltFile ( final File base ) {
        File candidate = new File ( this.globalXsltFile );
        if ( !candidate.isAbsolute() ) {
            candidate = new File ( base, this.globalXsltFile );
        }
        if ( !candidate.isFile() ) {
            return null;
        }
        try {
            if ( !candidate.getCanonicalPath().startsWith ( base.getCanonicalPath() ) ) {
                return null;
            }
        } catch ( IOException ioe ) {
            return null;
        }
        final String nameLower = candidate.getName().toLowerCase ( Locale.ENGLISH );
        if ( !nameLower.endsWith ( ".xslt" ) && !nameLower.endsWith ( ".xsl" ) ) {
            return null;
        }
        return candidate;
    }
    private Source secureXslt ( final InputStream is ) {
        Source result = null;
        try {
            final DocumentBuilder builder = DefaultServlet.factory.newDocumentBuilder();
            builder.setEntityResolver ( DefaultServlet.secureEntityResolver );
            final Document document = builder.parse ( is );
            result = new DOMSource ( document );
        } catch ( ParserConfigurationException ) {}
        catch ( SAXException ) {}
        catch ( IOException e ) {
            if ( this.debug > 0 ) {
                this.log ( e.getMessage(), ( Throwable ) e );
            }
        } finally {
            if ( is != null ) {
                try {
                    is.close();
                } catch ( IOException ex ) {}
            }
        }
        return result;
    }
    protected boolean checkSendfile ( final HttpServletRequest request, final HttpServletResponse response, final WebResource resource, final long length, final Range range ) {
        if ( this.sendfileSize > 0 && resource.isFile() && length > this.sendfileSize && resource.getCanonicalPath() != null && Boolean.TRUE.equals ( request.getAttribute ( "org.apache.tomcat.sendfile.support" ) ) && request.getClass().getName().equals ( "org.apache.catalina.connector.RequestFacade" ) && response.getClass().getName().equals ( "org.apache.catalina.connector.ResponseFacade" ) ) {
            request.setAttribute ( "org.apache.tomcat.sendfile.filename", ( Object ) resource.getCanonicalPath() );
            if ( range == null ) {
                request.setAttribute ( "org.apache.tomcat.sendfile.start", ( Object ) 0L );
                request.setAttribute ( "org.apache.tomcat.sendfile.end", ( Object ) length );
            } else {
                request.setAttribute ( "org.apache.tomcat.sendfile.start", ( Object ) range.start );
                request.setAttribute ( "org.apache.tomcat.sendfile.end", ( Object ) ( range.end + 1L ) );
            }
            return true;
        }
        return false;
    }
    protected boolean checkIfMatch ( final HttpServletRequest request, final HttpServletResponse response, final WebResource resource ) throws IOException {
        final String eTag = resource.getETag();
        final String headerValue = request.getHeader ( "If-Match" );
        if ( headerValue != null && headerValue.indexOf ( 42 ) == -1 ) {
            StringTokenizer commaTokenizer;
            boolean conditionSatisfied;
            String currentToken;
            for ( commaTokenizer = new StringTokenizer ( headerValue, "," ), conditionSatisfied = false; !conditionSatisfied && commaTokenizer.hasMoreTokens(); conditionSatisfied = true ) {
                currentToken = commaTokenizer.nextToken();
                if ( currentToken.trim().equals ( eTag ) ) {}
            }
            if ( !conditionSatisfied ) {
                response.sendError ( 412 );
                return false;
            }
        }
        return true;
    }
    protected boolean checkIfModifiedSince ( final HttpServletRequest request, final HttpServletResponse response, final WebResource resource ) {
        try {
            final long headerValue = request.getDateHeader ( "If-Modified-Since" );
            final long lastModified = resource.getLastModified();
            if ( headerValue != -1L && request.getHeader ( "If-None-Match" ) == null && lastModified < headerValue + 1000L ) {
                response.setStatus ( 304 );
                response.setHeader ( "ETag", resource.getETag() );
                return false;
            }
        } catch ( IllegalArgumentException illegalArgument ) {
            return true;
        }
        return true;
    }
    protected boolean checkIfNoneMatch ( final HttpServletRequest request, final HttpServletResponse response, final WebResource resource ) throws IOException {
        final String eTag = resource.getETag();
        final String headerValue = request.getHeader ( "If-None-Match" );
        if ( headerValue != null ) {
            boolean conditionSatisfied = false;
            if ( !headerValue.equals ( "*" ) ) {
                for ( StringTokenizer commaTokenizer = new StringTokenizer ( headerValue, "," ); !conditionSatisfied && commaTokenizer.hasMoreTokens(); conditionSatisfied = true ) {
                    final String currentToken = commaTokenizer.nextToken();
                    if ( currentToken.trim().equals ( eTag ) ) {}
                }
            } else {
                conditionSatisfied = true;
            }
            if ( conditionSatisfied ) {
                if ( "GET".equals ( request.getMethod() ) || "HEAD".equals ( request.getMethod() ) ) {
                    response.setStatus ( 304 );
                    response.setHeader ( "ETag", eTag );
                    return false;
                }
                response.sendError ( 412 );
                return false;
            }
        }
        return true;
    }
    protected boolean checkIfUnmodifiedSince ( final HttpServletRequest request, final HttpServletResponse response, final WebResource resource ) throws IOException {
        try {
            final long lastModified = resource.getLastModified();
            final long headerValue = request.getDateHeader ( "If-Unmodified-Since" );
            if ( headerValue != -1L && lastModified >= headerValue + 1000L ) {
                response.sendError ( 412 );
                return false;
            }
        } catch ( IllegalArgumentException illegalArgument ) {
            return true;
        }
        return true;
    }
    protected void copy ( final WebResource resource, final InputStream is, final ServletOutputStream ostream ) throws IOException {
        IOException exception = null;
        final InputStream istream = new BufferedInputStream ( is, this.input );
        exception = this.copyRange ( istream, ostream );
        istream.close();
        if ( exception != null ) {
            throw exception;
        }
    }
    protected void copy ( final WebResource resource, final InputStream is, final PrintWriter writer, final String encoding ) throws IOException {
        IOException exception = null;
        InputStream resourceInputStream = null;
        if ( resource.isFile() ) {
            resourceInputStream = resource.getInputStream();
        } else {
            resourceInputStream = is;
        }
        Reader reader;
        if ( encoding == null ) {
            reader = new InputStreamReader ( resourceInputStream );
        } else {
            reader = new InputStreamReader ( resourceInputStream, encoding );
        }
        exception = this.copyRange ( reader, writer );
        reader.close();
        if ( exception != null ) {
            throw exception;
        }
    }
    protected void copy ( final WebResource resource, final ServletOutputStream ostream, final Range range ) throws IOException {
        IOException exception = null;
        final InputStream resourceInputStream = resource.getInputStream();
        final InputStream istream = new BufferedInputStream ( resourceInputStream, this.input );
        exception = this.copyRange ( istream, ostream, range.start, range.end );
        istream.close();
        if ( exception != null ) {
            throw exception;
        }
    }
    protected void copy ( final WebResource resource, final ServletOutputStream ostream, final Iterator<Range> ranges, final String contentType ) throws IOException {
        IOException exception = null;
        while ( exception == null && ranges.hasNext() ) {
            final InputStream resourceInputStream = resource.getInputStream();
            try ( final InputStream istream = new BufferedInputStream ( resourceInputStream, this.input ) ) {
                final Range currentRange = ranges.next();
                ostream.println();
                ostream.println ( "--CATALINA_MIME_BOUNDARY" );
                if ( contentType != null ) {
                    ostream.println ( "Content-Type: " + contentType );
                }
                ostream.println ( "Content-Range: bytes " + currentRange.start + "-" + currentRange.end + "/" + currentRange.length );
                ostream.println();
                exception = this.copyRange ( istream, ostream, currentRange.start, currentRange.end );
            }
        }
        ostream.println();
        ostream.print ( "--CATALINA_MIME_BOUNDARY--" );
        if ( exception != null ) {
            throw exception;
        }
    }
    protected IOException copyRange ( final InputStream istream, final ServletOutputStream ostream ) {
        IOException exception = null;
        final byte[] buffer = new byte[this.input];
        int len = buffer.length;
        try {
            while ( true ) {
                len = istream.read ( buffer );
                if ( len == -1 ) {
                    break;
                }
                ostream.write ( buffer, 0, len );
            }
        } catch ( IOException e ) {
            exception = e;
            len = -1;
        }
        return exception;
    }
    protected IOException copyRange ( final Reader reader, final PrintWriter writer ) {
        IOException exception = null;
        final char[] buffer = new char[this.input];
        int len = buffer.length;
        try {
            while ( true ) {
                len = reader.read ( buffer );
                if ( len == -1 ) {
                    break;
                }
                writer.write ( buffer, 0, len );
            }
        } catch ( IOException e ) {
            exception = e;
            len = -1;
        }
        return exception;
    }
    protected IOException copyRange ( final InputStream istream, final ServletOutputStream ostream, final long start, final long end ) {
        if ( this.debug > 10 ) {
            this.log ( "Serving bytes:" + start + "-" + end );
        }
        long skipped = 0L;
        try {
            skipped = istream.skip ( start );
        } catch ( IOException e ) {
            return e;
        }
        if ( skipped < start ) {
            return new IOException ( DefaultServlet.sm.getString ( "defaultservlet.skipfail", skipped, start ) );
        }
        IOException exception = null;
        long bytesToRead = end - start + 1L;
        final byte[] buffer = new byte[this.input];
        int len = buffer.length;
        while ( bytesToRead > 0L && len >= buffer.length ) {
            try {
                len = istream.read ( buffer );
                if ( bytesToRead >= len ) {
                    ostream.write ( buffer, 0, len );
                    bytesToRead -= len;
                } else {
                    ostream.write ( buffer, 0, ( int ) bytesToRead );
                    bytesToRead = 0L;
                }
            } catch ( IOException e2 ) {
                exception = e2;
                len = -1;
            }
            if ( len < buffer.length ) {
                break;
            }
        }
        return exception;
    }
    static {
        sm = StringManager.getManager ( "org.apache.catalina.servlets" );
        FULL = new ArrayList<Range>();
        if ( Globals.IS_SECURITY_ENABLED ) {
            ( factory = DocumentBuilderFactory.newInstance() ).setNamespaceAware ( true );
            DefaultServlet.factory.setValidating ( false );
            secureEntityResolver = new SecureEntityResolver();
        } else {
            factory = null;
            secureEntityResolver = null;
        }
    }
    protected static class Range {
        public long start;
        public long end;
        public long length;
        public boolean validate() {
            if ( this.end >= this.length ) {
                this.end = this.length - 1L;
            }
            return this.start >= 0L && this.end >= 0L && this.start <= this.end && this.length > 0L;
        }
    }
    protected static class CompressionFormat implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String extension;
        public final String encoding;
        public CompressionFormat ( final String extension, final String encoding ) {
            this.extension = extension;
            this.encoding = encoding;
        }
    }
    private static class PrecompressedResource {
        public final WebResource resource;
        public final CompressionFormat format;
        private PrecompressedResource ( final WebResource resource, final CompressionFormat format ) {
            this.resource = resource;
            this.format = format;
        }
    }
    private static class SecureEntityResolver implements EntityResolver2 {
        @Override
        public InputSource resolveEntity ( final String publicId, final String systemId ) throws SAXException, IOException {
            throw new SAXException ( DefaultServlet.sm.getString ( "defaultServlet.blockExternalEntity", publicId, systemId ) );
        }
        @Override
        public InputSource getExternalSubset ( final String name, final String baseURI ) throws SAXException, IOException {
            throw new SAXException ( DefaultServlet.sm.getString ( "defaultServlet.blockExternalSubset", name, baseURI ) );
        }
        @Override
        public InputSource resolveEntity ( final String name, final String publicId, final String baseURI, final String systemId ) throws SAXException, IOException {
            throw new SAXException ( DefaultServlet.sm.getString ( "defaultServlet.blockExternalEntity2", name, publicId, baseURI, systemId ) );
        }
    }
}
