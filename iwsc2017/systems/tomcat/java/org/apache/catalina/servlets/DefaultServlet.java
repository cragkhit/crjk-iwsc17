package org.apache.catalina.servlets;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.connector.ResponseFacade;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.ServerInfo;
import org.apache.catalina.util.URLEncoder;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.security.PrivilegedGetTccl;
import org.apache.tomcat.util.security.PrivilegedSetTccl;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;
public class DefaultServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    protected static final StringManager sm = StringManager.getManager ( Constants.Package );
    private static final DocumentBuilderFactory factory;
    private static final SecureEntityResolver secureEntityResolver;
    protected static final ArrayList<Range> FULL = new ArrayList<>();
    protected static final String mimeSeparation = "CATALINA_MIME_BOUNDARY";
    protected static final int BUFFER_SIZE = 4096;
    static {
        if ( Globals.IS_SECURITY_ENABLED ) {
            factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware ( true );
            factory.setValidating ( false );
            secureEntityResolver = new SecureEntityResolver();
        } else {
            factory = null;
            secureEntityResolver = null;
        }
    }
    protected int debug = 0;
    protected int input = 2048;
    protected boolean listings = false;
    protected boolean readOnly = true;
    protected CompressionFormat[] compressionFormats;
    protected int output = 2048;
    protected String localXsltFile = null;
    protected String contextXsltFile = null;
    protected String globalXsltFile = null;
    protected String readmeFile = null;
    protected transient WebResourceRoot resources = null;
    protected String fileEncoding = null;
    protected int sendfileSize = 48 * 1024;
    protected boolean useAcceptRanges = true;
    protected boolean showServerInfo = true;
    @Override
    public void destroy() {
    }
    @Override
    public void init() throws ServletException {
        if ( getServletConfig().getInitParameter ( "debug" ) != null ) {
            debug = Integer.parseInt ( getServletConfig().getInitParameter ( "debug" ) );
        }
        if ( getServletConfig().getInitParameter ( "input" ) != null ) {
            input = Integer.parseInt ( getServletConfig().getInitParameter ( "input" ) );
        }
        if ( getServletConfig().getInitParameter ( "output" ) != null ) {
            output = Integer.parseInt ( getServletConfig().getInitParameter ( "output" ) );
        }
        listings = Boolean.parseBoolean ( getServletConfig().getInitParameter ( "listings" ) );
        if ( getServletConfig().getInitParameter ( "readonly" ) != null ) {
            readOnly = Boolean.parseBoolean ( getServletConfig().getInitParameter ( "readonly" ) );
        }
        compressionFormats = parseCompressionFormats (
                                 getServletConfig().getInitParameter ( "precompressed" ),
                                 getServletConfig().getInitParameter ( "gzip" ) );
        if ( getServletConfig().getInitParameter ( "sendfileSize" ) != null )
            sendfileSize =
                Integer.parseInt ( getServletConfig().getInitParameter ( "sendfileSize" ) ) * 1024;
        fileEncoding = getServletConfig().getInitParameter ( "fileEncoding" );
        globalXsltFile = getServletConfig().getInitParameter ( "globalXsltFile" );
        contextXsltFile = getServletConfig().getInitParameter ( "contextXsltFile" );
        localXsltFile = getServletConfig().getInitParameter ( "localXsltFile" );
        readmeFile = getServletConfig().getInitParameter ( "readmeFile" );
        if ( getServletConfig().getInitParameter ( "useAcceptRanges" ) != null ) {
            useAcceptRanges = Boolean.parseBoolean ( getServletConfig().getInitParameter ( "useAcceptRanges" ) );
        }
        if ( input < 256 ) {
            input = 256;
        }
        if ( output < 256 ) {
            output = 256;
        }
        if ( debug > 0 ) {
            log ( "DefaultServlet.init:  input buffer size=" + input +
                  ", output buffer size=" + output );
        }
        resources = ( WebResourceRoot ) getServletContext().getAttribute (
                        Globals.RESOURCES_ATTR );
        if ( resources == null ) {
            throw new UnavailableException ( "No resources" );
        }
        if ( getServletConfig().getInitParameter ( "showServerInfo" ) != null ) {
            showServerInfo = Boolean.parseBoolean ( getServletConfig().getInitParameter ( "showServerInfo" ) );
        }
    }
    private CompressionFormat[] parseCompressionFormats ( String precompressed, String gzip ) {
        List<CompressionFormat> ret = new ArrayList<>();
        if ( precompressed != null && precompressed.indexOf ( '=' ) > 0 ) {
            for ( String pair : precompressed.split ( "," ) ) {
                String[] setting = pair.split ( "=" );
                String encoding = setting[0];
                String extension = setting[1];
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
    protected String getRelativePath ( HttpServletRequest request ) {
        return getRelativePath ( request, false );
    }
    protected String getRelativePath ( HttpServletRequest request, boolean allowEmptyPath ) {
        String servletPath;
        String pathInfo;
        if ( request.getAttribute ( RequestDispatcher.INCLUDE_REQUEST_URI ) != null ) {
            pathInfo = ( String ) request.getAttribute ( RequestDispatcher.INCLUDE_PATH_INFO );
            servletPath = ( String ) request.getAttribute ( RequestDispatcher.INCLUDE_SERVLET_PATH );
        } else {
            pathInfo = request.getPathInfo();
            servletPath = request.getServletPath();
        }
        StringBuilder result = new StringBuilder();
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
    @Override
    protected void doGet ( HttpServletRequest request,
                           HttpServletResponse response )
    throws IOException, ServletException {
        serveResource ( request, response, true, fileEncoding );
    }
    @Override
    protected void doHead ( HttpServletRequest request, HttpServletResponse response )
    throws IOException, ServletException {
        boolean serveContent = DispatcherType.INCLUDE.equals ( request.getDispatcherType() );
        serveResource ( request, response, serveContent, fileEncoding );
    }
    @Override
    protected void doOptions ( HttpServletRequest req, HttpServletResponse resp )
    throws ServletException, IOException {
        StringBuilder allow = new StringBuilder();
        allow.append ( "GET, HEAD" );
        allow.append ( ", POST" );
        allow.append ( ", PUT" );
        allow.append ( ", DELETE" );
        if ( req instanceof RequestFacade &&
                ( ( RequestFacade ) req ).getAllowTrace() ) {
            allow.append ( ", TRACE" );
        }
        allow.append ( ", OPTIONS" );
        resp.setHeader ( "Allow", allow.toString() );
    }
    @Override
    protected void doPost ( HttpServletRequest request,
                            HttpServletResponse response )
    throws IOException, ServletException {
        doGet ( request, response );
    }
    @Override
    protected void doPut ( HttpServletRequest req, HttpServletResponse resp )
    throws ServletException, IOException {
        if ( readOnly ) {
            resp.sendError ( HttpServletResponse.SC_FORBIDDEN );
            return;
        }
        String path = getRelativePath ( req );
        WebResource resource = resources.getResource ( path );
        Range range = parseContentRange ( req, resp );
        InputStream resourceInputStream = null;
        try {
            if ( range != null ) {
                File contentFile = executePartialPut ( req, range, path );
                resourceInputStream = new FileInputStream ( contentFile );
            } else {
                resourceInputStream = req.getInputStream();
            }
            if ( resources.write ( path, resourceInputStream, true ) ) {
                if ( resource.exists() ) {
                    resp.setStatus ( HttpServletResponse.SC_NO_CONTENT );
                } else {
                    resp.setStatus ( HttpServletResponse.SC_CREATED );
                }
            } else {
                resp.sendError ( HttpServletResponse.SC_CONFLICT );
            }
        } finally {
            if ( resourceInputStream != null ) {
                try {
                    resourceInputStream.close();
                } catch ( IOException ioe ) {
                }
            }
        }
    }
    protected File executePartialPut ( HttpServletRequest req, Range range,
                                       String path )
    throws IOException {
        File tempDir = ( File ) getServletContext().getAttribute
                       ( ServletContext.TEMPDIR );
        String convertedResourcePath = path.replace ( '/', '.' );
        File contentFile = new File ( tempDir, convertedResourcePath );
        if ( contentFile.createNewFile() ) {
            contentFile.deleteOnExit();
        }
        try ( RandomAccessFile randAccessContentFile =
                        new RandomAccessFile ( contentFile, "rw" ); ) {
            WebResource oldResource = resources.getResource ( path );
            if ( oldResource.isFile() ) {
                try ( BufferedInputStream bufOldRevStream =
                                new BufferedInputStream ( oldResource.getInputStream(),
                                                          BUFFER_SIZE ); ) {
                    int numBytesRead;
                    byte[] copyBuffer = new byte[BUFFER_SIZE];
                    while ( ( numBytesRead = bufOldRevStream.read ( copyBuffer ) ) != -1 ) {
                        randAccessContentFile.write ( copyBuffer, 0, numBytesRead );
                    }
                }
            }
            randAccessContentFile.setLength ( range.length );
            randAccessContentFile.seek ( range.start );
            int numBytesRead;
            byte[] transferBuffer = new byte[BUFFER_SIZE];
            try ( BufferedInputStream requestBufInStream =
                            new BufferedInputStream ( req.getInputStream(), BUFFER_SIZE ); ) {
                while ( ( numBytesRead = requestBufInStream.read ( transferBuffer ) ) != -1 ) {
                    randAccessContentFile.write ( transferBuffer, 0, numBytesRead );
                }
            }
        }
        return contentFile;
    }
    @Override
    protected void doDelete ( HttpServletRequest req, HttpServletResponse resp )
    throws ServletException, IOException {
        if ( readOnly ) {
            resp.sendError ( HttpServletResponse.SC_FORBIDDEN );
            return;
        }
        String path = getRelativePath ( req );
        WebResource resource = resources.getResource ( path );
        if ( resource.exists() ) {
            if ( resource.delete() ) {
                resp.setStatus ( HttpServletResponse.SC_NO_CONTENT );
            } else {
                resp.sendError ( HttpServletResponse.SC_METHOD_NOT_ALLOWED );
            }
        } else {
            resp.sendError ( HttpServletResponse.SC_NOT_FOUND );
        }
    }
    protected boolean checkIfHeaders ( HttpServletRequest request,
                                       HttpServletResponse response,
                                       WebResource resource )
    throws IOException {
        return checkIfMatch ( request, response, resource )
               && checkIfModifiedSince ( request, response, resource )
               && checkIfNoneMatch ( request, response, resource )
               && checkIfUnmodifiedSince ( request, response, resource );
    }
    protected String rewriteUrl ( String path ) {
        return URLEncoder.DEFAULT.encode ( path, "UTF-8" );
    }
    protected void serveResource ( HttpServletRequest request,
                                   HttpServletResponse response,
                                   boolean content,
                                   String encoding )
    throws IOException, ServletException {
        boolean serveContent = content;
        String path = getRelativePath ( request, true );
        if ( debug > 0 ) {
            if ( serveContent )
                log ( "DefaultServlet.serveResource:  Serving resource '" +
                      path + "' headers and data" );
            else
                log ( "DefaultServlet.serveResource:  Serving resource '" +
                      path + "' headers only" );
        }
        if ( path.length() == 0 ) {
            doDirectoryRedirect ( request, response );
            return;
        }
        WebResource resource = resources.getResource ( path );
        if ( !resource.exists() ) {
            String requestUri = ( String ) request.getAttribute (
                                    RequestDispatcher.INCLUDE_REQUEST_URI );
            if ( requestUri == null ) {
                requestUri = request.getRequestURI();
            } else {
                throw new FileNotFoundException ( sm.getString (
                                                      "defaultServlet.missingResource", requestUri ) );
            }
            response.sendError ( HttpServletResponse.SC_NOT_FOUND, requestUri );
            return;
        }
        if ( !resource.canRead() ) {
            String requestUri = ( String ) request.getAttribute (
                                    RequestDispatcher.INCLUDE_REQUEST_URI );
            if ( requestUri == null ) {
                requestUri = request.getRequestURI();
            } else {
                throw new FileNotFoundException ( sm.getString (
                                                      "defaultServlet.missingResource", requestUri ) );
            }
            response.sendError ( HttpServletResponse.SC_FORBIDDEN, requestUri );
            return;
        }
        if ( resource.isFile() && ( path.endsWith ( "/" ) || path.endsWith ( "\\" ) ) ) {
            String requestUri = ( String ) request.getAttribute (
                                    RequestDispatcher.INCLUDE_REQUEST_URI );
            if ( requestUri == null ) {
                requestUri = request.getRequestURI();
            }
            response.sendError ( HttpServletResponse.SC_NOT_FOUND, requestUri );
            return;
        }
        boolean isError = response.getStatus() >= HttpServletResponse.SC_BAD_REQUEST;
        boolean included = false;
        if ( resource.isFile() ) {
            included = ( request.getAttribute (
                             RequestDispatcher.INCLUDE_CONTEXT_PATH ) != null );
            if ( !included && !isError && !checkIfHeaders ( request, response, resource ) ) {
                return;
            }
        }
        String contentType = resource.getMimeType();
        if ( contentType == null ) {
            contentType = getServletContext().getMimeType ( resource.getName() );
            resource.setMimeType ( contentType );
        }
        String eTag = null;
        String lastModifiedHttp = null;
        if ( resource.isFile() && !isError ) {
            eTag = resource.getETag();
            lastModifiedHttp = resource.getLastModifiedHttp();
        }
        boolean usingPrecompressedVersion = false;
        if ( compressionFormats.length > 0 && !included && resource.isFile() &&
                !pathEndsWithCompressedExtension ( path ) ) {
            List<PrecompressedResource> precompressedResources =
                getAvailablePrecompressedResources ( path );
            if ( !precompressedResources.isEmpty() ) {
                Collection<String> varyHeaders = response.getHeaders ( "Vary" );
                boolean addRequired = true;
                for ( String varyHeader : varyHeaders ) {
                    if ( "*".equals ( varyHeader ) ||
                            "accept-encoding".equalsIgnoreCase ( varyHeader ) ) {
                        addRequired = false;
                        break;
                    }
                }
                if ( addRequired ) {
                    response.addHeader ( "Vary", "accept-encoding" );
                }
                PrecompressedResource bestResource =
                    getBestPrecompressedResource ( request, precompressedResources );
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
                doDirectoryRedirect ( request, response );
                return;
            }
            if ( !listings ) {
                response.sendError ( HttpServletResponse.SC_NOT_FOUND,
                                     request.getRequestURI() );
                return;
            }
            contentType = "text/html;charset=UTF-8";
        } else {
            if ( !isError ) {
                if ( useAcceptRanges ) {
                    response.setHeader ( "Accept-Ranges", "bytes" );
                }
                ranges = parseRange ( request, response, resource );
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
                if ( !usingPrecompressedVersion &&
                        ( ( contentType == null ) ||
                          ( contentType.startsWith ( "text" ) ) ||
                          ( contentType.endsWith ( "xml" ) ) ||
                          ( contentType.contains ( "/javascript" ) ) )
                   ) {
                    writer = response.getWriter();
                    ranges = FULL;
                } else {
                    throw e;
                }
            }
        }
        ServletResponse r = response;
        long contentWritten = 0;
        while ( r instanceof ServletResponseWrapper ) {
            r = ( ( ServletResponseWrapper ) r ).getResponse();
        }
        if ( r instanceof ResponseFacade ) {
            contentWritten = ( ( ResponseFacade ) r ).getContentWritten();
        }
        if ( contentWritten > 0 ) {
            ranges = FULL;
        }
        if ( resource.isDirectory() ||
                isError ||
                ( ( ranges == null || ranges.isEmpty() )
                  && request.getHeader ( "Range" ) == null ) ||
                ranges == FULL ) {
            if ( contentType != null ) {
                if ( debug > 0 )
                    log ( "DefaultServlet.serveFile:  contentType='" +
                          contentType + "'" );
                response.setContentType ( contentType );
            }
            if ( resource.isFile() && contentLength >= 0 &&
                    ( !serveContent || ostream != null ) ) {
                if ( debug > 0 )
                    log ( "DefaultServlet.serveFile:  contentLength=" +
                          contentLength );
                if ( contentWritten == 0 ) {
                    response.setContentLengthLong ( contentLength );
                }
            }
            if ( serveContent ) {
                try {
                    response.setBufferSize ( output );
                } catch ( IllegalStateException e ) {
                }
                InputStream renderResult = null;
                if ( ostream == null ) {
                    if ( resource.isDirectory() ) {
                        renderResult = render ( getPathPrefix ( request ), resource, encoding );
                    } else {
                        renderResult = resource.getInputStream();
                    }
                    copy ( resource, renderResult, writer, encoding );
                } else {
                    if ( resource.isDirectory() ) {
                        renderResult = render ( getPathPrefix ( request ), resource, encoding );
                    } else {
                        if ( !checkSendfile ( request, response, resource,
                                              contentLength, null ) ) {
                            byte[] resourceBody = resource.getContent();
                            if ( resourceBody == null ) {
                                renderResult = resource.getInputStream();
                            } else {
                                ostream.write ( resourceBody );
                            }
                        }
                    }
                    if ( renderResult != null ) {
                        copy ( resource, renderResult, ostream );
                    }
                }
            }
        } else {
            if ( ( ranges == null ) || ( ranges.isEmpty() ) ) {
                return;
            }
            response.setStatus ( HttpServletResponse.SC_PARTIAL_CONTENT );
            if ( ranges.size() == 1 ) {
                Range range = ranges.get ( 0 );
                response.addHeader ( "Content-Range", "bytes "
                                     + range.start
                                     + "-" + range.end + "/"
                                     + range.length );
                long length = range.end - range.start + 1;
                response.setContentLengthLong ( length );
                if ( contentType != null ) {
                    if ( debug > 0 )
                        log ( "DefaultServlet.serveFile:  contentType='" +
                              contentType + "'" );
                    response.setContentType ( contentType );
                }
                if ( serveContent ) {
                    try {
                        response.setBufferSize ( output );
                    } catch ( IllegalStateException e ) {
                    }
                    if ( ostream != null ) {
                        if ( !checkSendfile ( request, response, resource,
                                              range.end - range.start + 1, range ) ) {
                            copy ( resource, ostream, range );
                        }
                    } else {
                        throw new IllegalStateException();
                    }
                }
            } else {
                response.setContentType ( "multipart/byteranges; boundary="
                                          + mimeSeparation );
                if ( serveContent ) {
                    try {
                        response.setBufferSize ( output );
                    } catch ( IllegalStateException e ) {
                    }
                    if ( ostream != null ) {
                        copy ( resource, ostream, ranges.iterator(), contentType );
                    } else {
                        throw new IllegalStateException();
                    }
                }
            }
        }
    }
    private boolean pathEndsWithCompressedExtension ( String path ) {
        for ( CompressionFormat format : compressionFormats ) {
            if ( path.endsWith ( format.extension ) ) {
                return true;
            }
        }
        return false;
    }
    private List<PrecompressedResource> getAvailablePrecompressedResources ( String path ) {
        List<PrecompressedResource> ret = new ArrayList<> ( compressionFormats.length );
        for ( CompressionFormat format : compressionFormats ) {
            WebResource precompressedResource = resources.getResource ( path + format.extension );
            if ( precompressedResource.exists() && precompressedResource.isFile() ) {
                ret.add ( new PrecompressedResource ( precompressedResource, format ) );
            }
        }
        return ret;
    }
    private PrecompressedResource getBestPrecompressedResource ( HttpServletRequest request,
            List<PrecompressedResource> precompressedResources ) {
        Enumeration<String> headers = request.getHeaders ( "Accept-Encoding" );
        PrecompressedResource bestResource = null;
        double bestResourceQuality = 0;
        int bestResourcePreference = Integer.MAX_VALUE;
        while ( headers.hasMoreElements() ) {
            String header = headers.nextElement();
            for ( String preference : header.split ( "," ) ) {
                double quality = 1;
                int qualityIdx = preference.indexOf ( ';' );
                if ( qualityIdx > 0 ) {
                    int equalsIdx = preference.indexOf ( '=', qualityIdx + 1 );
                    if ( equalsIdx == -1 ) {
                        continue;
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
                        continue;
                    }
                    if ( "*".equals ( encoding ) ) {
                        bestResource = precompressedResources.get ( 0 );
                        bestResourceQuality = quality;
                        bestResourcePreference = 0;
                        continue;
                    }
                    for ( int i = 0; i < precompressedResources.size(); ++i ) {
                        PrecompressedResource resource = precompressedResources.get ( i );
                        if ( encoding.equals ( resource.format.encoding ) ) {
                            if ( quality > bestResourceQuality || i < bestResourcePreference ) {
                                bestResource = resource;
                                bestResourceQuality = quality;
                                bestResourcePreference = i;
                            }
                            break;
                        }
                    }
                }
            }
        }
        return bestResource;
    }
    private void doDirectoryRedirect ( HttpServletRequest request, HttpServletResponse response )
    throws IOException {
        StringBuilder location = new StringBuilder ( request.getRequestURI() );
        location.append ( '/' );
        if ( request.getQueryString() != null ) {
            location.append ( '?' );
            location.append ( request.getQueryString() );
        }
        response.sendRedirect ( response.encodeRedirectURL ( location.toString() ) );
    }
    protected Range parseContentRange ( HttpServletRequest request,
                                        HttpServletResponse response )
    throws IOException {
        String rangeHeader = request.getHeader ( "Content-Range" );
        if ( rangeHeader == null ) {
            return null;
        }
        if ( !rangeHeader.startsWith ( "bytes" ) ) {
            response.sendError ( HttpServletResponse.SC_BAD_REQUEST );
            return null;
        }
        rangeHeader = rangeHeader.substring ( 6 ).trim();
        int dashPos = rangeHeader.indexOf ( '-' );
        int slashPos = rangeHeader.indexOf ( '/' );
        if ( dashPos == -1 ) {
            response.sendError ( HttpServletResponse.SC_BAD_REQUEST );
            return null;
        }
        if ( slashPos == -1 ) {
            response.sendError ( HttpServletResponse.SC_BAD_REQUEST );
            return null;
        }
        Range range = new Range();
        try {
            range.start = Long.parseLong ( rangeHeader.substring ( 0, dashPos ) );
            range.end =
                Long.parseLong ( rangeHeader.substring ( dashPos + 1, slashPos ) );
            range.length = Long.parseLong
                           ( rangeHeader.substring ( slashPos + 1, rangeHeader.length() ) );
        } catch ( NumberFormatException e ) {
            response.sendError ( HttpServletResponse.SC_BAD_REQUEST );
            return null;
        }
        if ( !range.validate() ) {
            response.sendError ( HttpServletResponse.SC_BAD_REQUEST );
            return null;
        }
        return range;
    }
    protected ArrayList<Range> parseRange ( HttpServletRequest request,
                                            HttpServletResponse response,
                                            WebResource resource ) throws IOException {
        String headerValue = request.getHeader ( "If-Range" );
        if ( headerValue != null ) {
            long headerValueTime = ( -1L );
            try {
                headerValueTime = request.getDateHeader ( "If-Range" );
            } catch ( IllegalArgumentException e ) {
            }
            String eTag = resource.getETag();
            long lastModified = resource.getLastModified();
            if ( headerValueTime == ( -1L ) ) {
                if ( !eTag.equals ( headerValue.trim() ) ) {
                    return FULL;
                }
            } else {
                if ( lastModified > ( headerValueTime + 1000 ) ) {
                    return FULL;
                }
            }
        }
        long fileLength = resource.getContentLength();
        if ( fileLength == 0 ) {
            return null;
        }
        String rangeHeader = request.getHeader ( "Range" );
        if ( rangeHeader == null ) {
            return null;
        }
        if ( !rangeHeader.startsWith ( "bytes" ) ) {
            response.addHeader ( "Content-Range", "bytes */" + fileLength );
            response.sendError
            ( HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE );
            return null;
        }
        rangeHeader = rangeHeader.substring ( 6 );
        ArrayList<Range> result = new ArrayList<>();
        StringTokenizer commaTokenizer = new StringTokenizer ( rangeHeader, "," );
        while ( commaTokenizer.hasMoreTokens() ) {
            String rangeDefinition = commaTokenizer.nextToken().trim();
            Range currentRange = new Range();
            currentRange.length = fileLength;
            int dashPos = rangeDefinition.indexOf ( '-' );
            if ( dashPos == -1 ) {
                response.addHeader ( "Content-Range", "bytes */" + fileLength );
                response.sendError
                ( HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE );
                return null;
            }
            if ( dashPos == 0 ) {
                try {
                    long offset = Long.parseLong ( rangeDefinition );
                    currentRange.start = fileLength + offset;
                    currentRange.end = fileLength - 1;
                } catch ( NumberFormatException e ) {
                    response.addHeader ( "Content-Range",
                                         "bytes */" + fileLength );
                    response.sendError
                    ( HttpServletResponse
                      .SC_REQUESTED_RANGE_NOT_SATISFIABLE );
                    return null;
                }
            } else {
                try {
                    currentRange.start = Long.parseLong
                                         ( rangeDefinition.substring ( 0, dashPos ) );
                    if ( dashPos < rangeDefinition.length() - 1 )
                        currentRange.end = Long.parseLong
                                           ( rangeDefinition.substring
                                             ( dashPos + 1, rangeDefinition.length() ) );
                    else {
                        currentRange.end = fileLength - 1;
                    }
                } catch ( NumberFormatException e ) {
                    response.addHeader ( "Content-Range",
                                         "bytes */" + fileLength );
                    response.sendError
                    ( HttpServletResponse
                      .SC_REQUESTED_RANGE_NOT_SATISFIABLE );
                    return null;
                }
            }
            if ( !currentRange.validate() ) {
                response.addHeader ( "Content-Range", "bytes */" + fileLength );
                response.sendError
                ( HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE );
                return null;
            }
            result.add ( currentRange );
        }
        return result;
    }
    protected InputStream render ( String contextPath, WebResource resource, String encoding )
    throws IOException, ServletException {
        Source xsltSource = findXsltSource ( resource );
        if ( xsltSource == null ) {
            return renderHtml ( contextPath, resource, encoding );
        }
        return renderXml ( contextPath, resource, xsltSource, encoding );
    }
    protected InputStream renderXml ( String contextPath, WebResource resource, Source xsltSource,
                                      String encoding )
    throws IOException, ServletException {
        StringBuilder sb = new StringBuilder();
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
        String[] entries = resources.list ( resource.getWebappPath() );
        String rewrittenContextPath =  rewriteUrl ( contextPath );
        String directoryWebappPath = resource.getWebappPath();
        for ( String entry : entries ) {
            if ( entry.equalsIgnoreCase ( "WEB-INF" ) ||
                    entry.equalsIgnoreCase ( "META-INF" ) ||
                    entry.equalsIgnoreCase ( localXsltFile ) ) {
                continue;
            }
            if ( ( directoryWebappPath + entry ).equals ( contextXsltFile ) ) {
                continue;
            }
            WebResource childResource =
                resources.getResource ( directoryWebappPath + entry );
            if ( !childResource.exists() ) {
                continue;
            }
            sb.append ( "<entry" );
            sb.append ( " type='" )
            .append ( childResource.isDirectory() ? "dir" : "file" )
            .append ( "'" );
            sb.append ( " urlPath='" )
            .append ( rewrittenContextPath )
            .append ( rewriteUrl ( directoryWebappPath + entry ) )
            .append ( childResource.isDirectory() ? "/" : "" )
            .append ( "'" );
            if ( childResource.isFile() ) {
                sb.append ( " size='" )
                .append ( renderSize ( childResource.getContentLength() ) )
                .append ( "'" );
            }
            sb.append ( " date='" )
            .append ( childResource.getLastModifiedHttp() )
            .append ( "'" );
            sb.append ( ">" );
            sb.append ( RequestUtil.filter ( entry ) );
            if ( childResource.isDirectory() ) {
                sb.append ( "/" );
            }
            sb.append ( "</entry>" );
        }
        sb.append ( "</entries>" );
        String readme = getReadme ( resource, encoding );
        if ( readme != null ) {
            sb.append ( "<readme><![CDATA[" );
            sb.append ( readme );
            sb.append ( "]]></readme>" );
        }
        sb.append ( "</listing>" );
        ClassLoader original;
        if ( Globals.IS_SECURITY_ENABLED ) {
            PrivilegedGetTccl pa = new PrivilegedGetTccl();
            original = AccessController.doPrivileged ( pa );
        } else {
            original = Thread.currentThread().getContextClassLoader();
        }
        try {
            if ( Globals.IS_SECURITY_ENABLED ) {
                PrivilegedSetTccl pa =
                    new PrivilegedSetTccl ( DefaultServlet.class.getClassLoader() );
                AccessController.doPrivileged ( pa );
            } else {
                Thread.currentThread().setContextClassLoader (
                    DefaultServlet.class.getClassLoader() );
            }
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Source xmlSource = new StreamSource ( new StringReader ( sb.toString() ) );
            Transformer transformer = tFactory.newTransformer ( xsltSource );
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            OutputStreamWriter osWriter = new OutputStreamWriter ( stream, "UTF8" );
            StreamResult out = new StreamResult ( osWriter );
            transformer.transform ( xmlSource, out );
            osWriter.flush();
            return ( new ByteArrayInputStream ( stream.toByteArray() ) );
        } catch ( TransformerException e ) {
            throw new ServletException ( "XSL transformer error", e );
        } finally {
            if ( Globals.IS_SECURITY_ENABLED ) {
                PrivilegedSetTccl pa = new PrivilegedSetTccl ( original );
                AccessController.doPrivileged ( pa );
            } else {
                Thread.currentThread().setContextClassLoader ( original );
            }
        }
    }
    protected InputStream renderHtml ( String contextPath, WebResource resource, String encoding )
    throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        OutputStreamWriter osWriter = new OutputStreamWriter ( stream, "UTF8" );
        PrintWriter writer = new PrintWriter ( osWriter );
        StringBuilder sb = new StringBuilder();
        String[] entries = resources.list ( resource.getWebappPath() );
        String rewrittenContextPath =  rewriteUrl ( contextPath );
        String directoryWebappPath = resource.getWebappPath();
        sb.append ( "<html>\r\n" );
        sb.append ( "<head>\r\n" );
        sb.append ( "<title>" );
        sb.append ( sm.getString ( "directory.title", directoryWebappPath ) );
        sb.append ( "</title>\r\n" );
        sb.append ( "<STYLE><!--" );
        sb.append ( org.apache.catalina.util.TomcatCSS.TOMCAT_CSS );
        sb.append ( "--></STYLE> " );
        sb.append ( "</head>\r\n" );
        sb.append ( "<body>" );
        sb.append ( "<h1>" );
        sb.append ( sm.getString ( "directory.title", directoryWebappPath ) );
        String parentDirectory = directoryWebappPath;
        if ( parentDirectory.endsWith ( "/" ) ) {
            parentDirectory =
                parentDirectory.substring ( 0, parentDirectory.length() - 1 );
        }
        int slash = parentDirectory.lastIndexOf ( '/' );
        if ( slash >= 0 ) {
            String parent = directoryWebappPath.substring ( 0, slash );
            sb.append ( " - <a href=\"" );
            sb.append ( rewrittenContextPath );
            if ( parent.equals ( "" ) ) {
                parent = "/";
            }
            sb.append ( rewriteUrl ( parent ) );
            if ( !parent.endsWith ( "/" ) ) {
                sb.append ( "/" );
            }
            sb.append ( "\">" );
            sb.append ( "<b>" );
            sb.append ( sm.getString ( "directory.parent", parent ) );
            sb.append ( "</b>" );
            sb.append ( "</a>" );
        }
        sb.append ( "</h1>" );
        sb.append ( "<HR size=\"1\" noshade=\"noshade\">" );
        sb.append ( "<table width=\"100%\" cellspacing=\"0\"" +
                    " cellpadding=\"5\" align=\"center\">\r\n" );
        sb.append ( "<tr>\r\n" );
        sb.append ( "<td align=\"left\"><font size=\"+1\"><strong>" );
        sb.append ( sm.getString ( "directory.filename" ) );
        sb.append ( "</strong></font></td>\r\n" );
        sb.append ( "<td align=\"center\"><font size=\"+1\"><strong>" );
        sb.append ( sm.getString ( "directory.size" ) );
        sb.append ( "</strong></font></td>\r\n" );
        sb.append ( "<td align=\"right\"><font size=\"+1\"><strong>" );
        sb.append ( sm.getString ( "directory.lastModified" ) );
        sb.append ( "</strong></font></td>\r\n" );
        sb.append ( "</tr>" );
        boolean shade = false;
        for ( String entry : entries ) {
            if ( entry.equalsIgnoreCase ( "WEB-INF" ) ||
                    entry.equalsIgnoreCase ( "META-INF" ) ) {
                continue;
            }
            WebResource childResource =
                resources.getResource ( directoryWebappPath + entry );
            if ( !childResource.exists() ) {
                continue;
            }
            sb.append ( "<tr" );
            if ( shade ) {
                sb.append ( " bgcolor=\"#eeeeee\"" );
            }
            sb.append ( ">\r\n" );
            shade = !shade;
            sb.append ( "<td align=\"left\">&nbsp;&nbsp;\r\n" );
            sb.append ( "<a href=\"" );
            sb.append ( rewrittenContextPath );
            sb.append ( rewriteUrl ( directoryWebappPath + entry ) );
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
                sb.append ( renderSize ( childResource.getContentLength() ) );
            }
            sb.append ( "</tt></td>\r\n" );
            sb.append ( "<td align=\"right\"><tt>" );
            sb.append ( childResource.getLastModifiedHttp() );
            sb.append ( "</tt></td>\r\n" );
            sb.append ( "</tr>\r\n" );
        }
        sb.append ( "</table>\r\n" );
        sb.append ( "<HR size=\"1\" noshade=\"noshade\">" );
        String readme = getReadme ( resource, encoding );
        if ( readme != null ) {
            sb.append ( readme );
            sb.append ( "<HR size=\"1\" noshade=\"noshade\">" );
        }
        if ( showServerInfo ) {
            sb.append ( "<h3>" ).append ( ServerInfo.getServerInfo() ).append ( "</h3>" );
        }
        sb.append ( "</body>\r\n" );
        sb.append ( "</html>\r\n" );
        writer.write ( sb.toString() );
        writer.flush();
        return ( new ByteArrayInputStream ( stream.toByteArray() ) );
    }
    protected String renderSize ( long size ) {
        long leftSide = size / 1024;
        long rightSide = ( size % 1024 ) / 103;
        if ( ( leftSide == 0 ) && ( rightSide == 0 ) && ( size > 0 ) ) {
            rightSide = 1;
        }
        return ( "" + leftSide + "." + rightSide + " kb" );
    }
    protected String getReadme ( WebResource directory, String encoding ) {
        if ( readmeFile != null ) {
            WebResource resource = resources.getResource (
                                       directory.getWebappPath() + readmeFile );
            if ( resource.isFile() ) {
                StringWriter buffer = new StringWriter();
                InputStreamReader reader = null;
                try ( InputStream is = resource.getInputStream(); ) {
                    if ( encoding != null ) {
                        reader = new InputStreamReader ( is, encoding );
                    } else {
                        reader = new InputStreamReader ( is );
                    }
                    copyRange ( reader, new PrintWriter ( buffer ) );
                } catch ( IOException e ) {
                    log ( "Failure to close reader", e );
                } finally {
                    if ( reader != null ) {
                        try {
                            reader.close();
                        } catch ( IOException e ) {
                        }
                    }
                }
                return buffer.toString();
            } else {
                if ( debug > 10 ) {
                    log ( "readme '" + readmeFile + "' not found" );
                }
                return null;
            }
        }
        return null;
    }
    protected Source findXsltSource ( WebResource directory )
    throws IOException {
        if ( localXsltFile != null ) {
            WebResource resource = resources.getResource (
                                       directory.getWebappPath() + localXsltFile );
            if ( resource.isFile() ) {
                InputStream is = resource.getInputStream();
                if ( is != null ) {
                    if ( Globals.IS_SECURITY_ENABLED ) {
                        return secureXslt ( is );
                    } else {
                        return new StreamSource ( is );
                    }
                }
            }
            if ( debug > 10 ) {
                log ( "localXsltFile '" + localXsltFile + "' not found" );
            }
        }
        if ( contextXsltFile != null ) {
            InputStream is =
                getServletContext().getResourceAsStream ( contextXsltFile );
            if ( is != null ) {
                if ( Globals.IS_SECURITY_ENABLED ) {
                    return secureXslt ( is );
                } else {
                    return new StreamSource ( is );
                }
            }
            if ( debug > 10 ) {
                log ( "contextXsltFile '" + contextXsltFile + "' not found" );
            }
        }
        if ( globalXsltFile != null ) {
            File f = validateGlobalXsltFile();
            if ( f != null ) {
                try ( FileInputStream fis = new FileInputStream ( f ) ) {
                    byte b[] = new byte[ ( int ) f.length()];
                    fis.read ( b );
                    return new StreamSource ( new ByteArrayInputStream ( b ) );
                }
            }
        }
        return null;
    }
    private File validateGlobalXsltFile() {
        Context context = resources.getContext();
        File baseConf = new File ( context.getCatalinaBase(), "conf" );
        File result = validateGlobalXsltFile ( baseConf );
        if ( result == null ) {
            File homeConf = new File ( context.getCatalinaHome(), "conf" );
            if ( !baseConf.equals ( homeConf ) ) {
                result = validateGlobalXsltFile ( homeConf );
            }
        }
        return result;
    }
    private File validateGlobalXsltFile ( File base ) {
        File candidate = new File ( globalXsltFile );
        if ( !candidate.isAbsolute() ) {
            candidate = new File ( base, globalXsltFile );
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
        String nameLower = candidate.getName().toLowerCase ( Locale.ENGLISH );
        if ( !nameLower.endsWith ( ".xslt" ) && !nameLower.endsWith ( ".xsl" ) ) {
            return null;
        }
        return candidate;
    }
    private Source secureXslt ( InputStream is ) {
        Source result = null;
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver ( secureEntityResolver );
            Document document = builder.parse ( is );
            result = new DOMSource ( document );
        } catch ( ParserConfigurationException | SAXException | IOException e ) {
            if ( debug > 0 ) {
                log ( e.getMessage(), e );
            }
        } finally {
            if ( is != null ) {
                try {
                    is.close();
                } catch ( IOException e ) {
                }
            }
        }
        return result;
    }
    protected boolean checkSendfile ( HttpServletRequest request,
                                      HttpServletResponse response,
                                      WebResource resource,
                                      long length, Range range ) {
        if ( sendfileSize > 0
                && resource.isFile()
                && length > sendfileSize
                && ( resource.getCanonicalPath() != null )
                && ( Boolean.TRUE.equals ( request.getAttribute ( Globals.SENDFILE_SUPPORTED_ATTR ) ) )
                && ( request.getClass().getName().equals ( "org.apache.catalina.connector.RequestFacade" ) )
                && ( response.getClass().getName().equals ( "org.apache.catalina.connector.ResponseFacade" ) ) ) {
            request.setAttribute ( Globals.SENDFILE_FILENAME_ATTR, resource.getCanonicalPath() );
            if ( range == null ) {
                request.setAttribute ( Globals.SENDFILE_FILE_START_ATTR, Long.valueOf ( 0L ) );
                request.setAttribute ( Globals.SENDFILE_FILE_END_ATTR, Long.valueOf ( length ) );
            } else {
                request.setAttribute ( Globals.SENDFILE_FILE_START_ATTR, Long.valueOf ( range.start ) );
                request.setAttribute ( Globals.SENDFILE_FILE_END_ATTR, Long.valueOf ( range.end + 1 ) );
            }
            return true;
        }
        return false;
    }
    protected boolean checkIfMatch ( HttpServletRequest request,
                                     HttpServletResponse response, WebResource resource )
    throws IOException {
        String eTag = resource.getETag();
        String headerValue = request.getHeader ( "If-Match" );
        if ( headerValue != null ) {
            if ( headerValue.indexOf ( '*' ) == -1 ) {
                StringTokenizer commaTokenizer = new StringTokenizer
                ( headerValue, "," );
                boolean conditionSatisfied = false;
                while ( !conditionSatisfied && commaTokenizer.hasMoreTokens() ) {
                    String currentToken = commaTokenizer.nextToken();
                    if ( currentToken.trim().equals ( eTag ) ) {
                        conditionSatisfied = true;
                    }
                }
                if ( !conditionSatisfied ) {
                    response.sendError
                    ( HttpServletResponse.SC_PRECONDITION_FAILED );
                    return false;
                }
            }
        }
        return true;
    }
    protected boolean checkIfModifiedSince ( HttpServletRequest request,
            HttpServletResponse response, WebResource resource ) {
        try {
            long headerValue = request.getDateHeader ( "If-Modified-Since" );
            long lastModified = resource.getLastModified();
            if ( headerValue != -1 ) {
                if ( ( request.getHeader ( "If-None-Match" ) == null )
                        && ( lastModified < headerValue + 1000 ) ) {
                    response.setStatus ( HttpServletResponse.SC_NOT_MODIFIED );
                    response.setHeader ( "ETag", resource.getETag() );
                    return false;
                }
            }
        } catch ( IllegalArgumentException illegalArgument ) {
            return true;
        }
        return true;
    }
    protected boolean checkIfNoneMatch ( HttpServletRequest request,
                                         HttpServletResponse response, WebResource resource )
    throws IOException {
        String eTag = resource.getETag();
        String headerValue = request.getHeader ( "If-None-Match" );
        if ( headerValue != null ) {
            boolean conditionSatisfied = false;
            if ( !headerValue.equals ( "*" ) ) {
                StringTokenizer commaTokenizer =
                    new StringTokenizer ( headerValue, "," );
                while ( !conditionSatisfied && commaTokenizer.hasMoreTokens() ) {
                    String currentToken = commaTokenizer.nextToken();
                    if ( currentToken.trim().equals ( eTag ) ) {
                        conditionSatisfied = true;
                    }
                }
            } else {
                conditionSatisfied = true;
            }
            if ( conditionSatisfied ) {
                if ( ( "GET".equals ( request.getMethod() ) )
                        || ( "HEAD".equals ( request.getMethod() ) ) ) {
                    response.setStatus ( HttpServletResponse.SC_NOT_MODIFIED );
                    response.setHeader ( "ETag", eTag );
                    return false;
                }
                response.sendError ( HttpServletResponse.SC_PRECONDITION_FAILED );
                return false;
            }
        }
        return true;
    }
    protected boolean checkIfUnmodifiedSince ( HttpServletRequest request,
            HttpServletResponse response, WebResource resource )
    throws IOException {
        try {
            long lastModified = resource.getLastModified();
            long headerValue = request.getDateHeader ( "If-Unmodified-Since" );
            if ( headerValue != -1 ) {
                if ( lastModified >= ( headerValue + 1000 ) ) {
                    response.sendError ( HttpServletResponse.SC_PRECONDITION_FAILED );
                    return false;
                }
            }
        } catch ( IllegalArgumentException illegalArgument ) {
            return true;
        }
        return true;
    }
    protected void copy ( WebResource resource, InputStream is,
                          ServletOutputStream ostream )
    throws IOException {
        IOException exception = null;
        InputStream istream = new BufferedInputStream ( is, input );
        exception = copyRange ( istream, ostream );
        istream.close();
        if ( exception != null ) {
            throw exception;
        }
    }
    protected void copy ( WebResource resource, InputStream is, PrintWriter writer,
                          String encoding ) throws IOException {
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
        exception = copyRange ( reader, writer );
        reader.close();
        if ( exception != null ) {
            throw exception;
        }
    }
    protected void copy ( WebResource resource, ServletOutputStream ostream,
                          Range range )
    throws IOException {
        IOException exception = null;
        InputStream resourceInputStream = resource.getInputStream();
        InputStream istream =
            new BufferedInputStream ( resourceInputStream, input );
        exception = copyRange ( istream, ostream, range.start, range.end );
        istream.close();
        if ( exception != null ) {
            throw exception;
        }
    }
    protected void copy ( WebResource resource, ServletOutputStream ostream,
                          Iterator<Range> ranges, String contentType )
    throws IOException {
        IOException exception = null;
        while ( ( exception == null ) && ( ranges.hasNext() ) ) {
            InputStream resourceInputStream = resource.getInputStream();
            try ( InputStream istream = new BufferedInputStream ( resourceInputStream, input ) ) {
                Range currentRange = ranges.next();
                ostream.println();
                ostream.println ( "--" + mimeSeparation );
                if ( contentType != null ) {
                    ostream.println ( "Content-Type: " + contentType );
                }
                ostream.println ( "Content-Range: bytes " + currentRange.start
                                  + "-" + currentRange.end + "/"
                                  + currentRange.length );
                ostream.println();
                exception = copyRange ( istream, ostream, currentRange.start,
                                        currentRange.end );
            }
        }
        ostream.println();
        ostream.print ( "--" + mimeSeparation + "--" );
        if ( exception != null ) {
            throw exception;
        }
    }
    protected IOException copyRange ( InputStream istream,
                                      ServletOutputStream ostream ) {
        IOException exception = null;
        byte buffer[] = new byte[input];
        int len = buffer.length;
        while ( true ) {
            try {
                len = istream.read ( buffer );
                if ( len == -1 ) {
                    break;
                }
                ostream.write ( buffer, 0, len );
            } catch ( IOException e ) {
                exception = e;
                len = -1;
                break;
            }
        }
        return exception;
    }
    protected IOException copyRange ( Reader reader, PrintWriter writer ) {
        IOException exception = null;
        char buffer[] = new char[input];
        int len = buffer.length;
        while ( true ) {
            try {
                len = reader.read ( buffer );
                if ( len == -1 ) {
                    break;
                }
                writer.write ( buffer, 0, len );
            } catch ( IOException e ) {
                exception = e;
                len = -1;
                break;
            }
        }
        return exception;
    }
    protected IOException copyRange ( InputStream istream,
                                      ServletOutputStream ostream,
                                      long start, long end ) {
        if ( debug > 10 ) {
            log ( "Serving bytes:" + start + "-" + end );
        }
        long skipped = 0;
        try {
            skipped = istream.skip ( start );
        } catch ( IOException e ) {
            return e;
        }
        if ( skipped < start ) {
            return new IOException ( sm.getString ( "defaultservlet.skipfail",
                                                    Long.valueOf ( skipped ), Long.valueOf ( start ) ) );
        }
        IOException exception = null;
        long bytesToRead = end - start + 1;
        byte buffer[] = new byte[input];
        int len = buffer.length;
        while ( ( bytesToRead > 0 ) && ( len >= buffer.length ) ) {
            try {
                len = istream.read ( buffer );
                if ( bytesToRead >= len ) {
                    ostream.write ( buffer, 0, len );
                    bytesToRead -= len;
                } else {
                    ostream.write ( buffer, 0, ( int ) bytesToRead );
                    bytesToRead = 0;
                }
            } catch ( IOException e ) {
                exception = e;
                len = -1;
            }
            if ( len < buffer.length ) {
                break;
            }
        }
        return exception;
    }
    protected static class Range {
        public long start;
        public long end;
        public long length;
        public boolean validate() {
            if ( end >= length ) {
                end = length - 1;
            }
            return ( start >= 0 ) && ( end >= 0 ) && ( start <= end ) && ( length > 0 );
        }
    }
    protected static class CompressionFormat implements Serializable {
        private static final long serialVersionUID = 1L;
        public final String extension;
        public final String encoding;
        public CompressionFormat ( String extension, String encoding ) {
            this.extension = extension;
            this.encoding = encoding;
        }
    }
    private static class PrecompressedResource {
        public final WebResource resource;
        public final CompressionFormat format;
        private PrecompressedResource ( WebResource resource, CompressionFormat format ) {
            this.resource = resource;
            this.format = format;
        }
    }
    private static class SecureEntityResolver implements EntityResolver2  {
        @Override
        public InputSource resolveEntity ( String publicId, String systemId )
        throws SAXException, IOException {
            throw new SAXException ( sm.getString ( "defaultServlet.blockExternalEntity",
                                                    publicId, systemId ) );
        }
        @Override
        public InputSource getExternalSubset ( String name, String baseURI )
        throws SAXException, IOException {
            throw new SAXException ( sm.getString ( "defaultServlet.blockExternalSubset",
                                                    name, baseURI ) );
        }
        @Override
        public InputSource resolveEntity ( String name, String publicId,
                                           String baseURI, String systemId ) throws SAXException,
            IOException {
            throw new SAXException ( sm.getString ( "defaultServlet.blockExternalEntity2",
                                                    name, publicId, baseURI, systemId ) );
        }
    }
}
