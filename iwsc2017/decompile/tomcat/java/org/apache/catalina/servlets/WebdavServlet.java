package org.apache.catalina.servlets;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.Reader;
import java.io.StringReader;
import javax.servlet.ServletContext;
import java.util.TimeZone;
import java.util.Date;
import java.text.DateFormat;
import org.apache.tomcat.util.http.FastHttpDateFormat;
import org.apache.tomcat.util.http.RequestUtil;
import org.apache.tomcat.util.buf.UDecoder;
import org.apache.tomcat.util.security.MD5Encoder;
import org.apache.tomcat.util.security.ConcurrentMessageDigest;
import java.nio.charset.StandardCharsets;
import org.apache.catalina.util.DOMWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import java.util.Stack;
import java.io.Writer;
import org.apache.catalina.util.XMLWriter;
import org.xml.sax.SAXException;
import java.io.InputStream;
import org.xml.sax.InputSource;
import org.apache.catalina.WebResource;
import java.util.Locale;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.EntityResolver;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.servlet.ServletException;
import java.util.Vector;
import java.util.Hashtable;
import org.apache.catalina.util.ConcurrentDateFormat;
public class WebdavServlet extends DefaultServlet {
    private static final long serialVersionUID = 1L;
    private static final String METHOD_PROPFIND = "PROPFIND";
    private static final String METHOD_PROPPATCH = "PROPPATCH";
    private static final String METHOD_MKCOL = "MKCOL";
    private static final String METHOD_COPY = "COPY";
    private static final String METHOD_MOVE = "MOVE";
    private static final String METHOD_LOCK = "LOCK";
    private static final String METHOD_UNLOCK = "UNLOCK";
    private static final int FIND_BY_PROPERTY = 0;
    private static final int FIND_ALL_PROP = 1;
    private static final int FIND_PROPERTY_NAMES = 2;
    private static final int LOCK_CREATION = 0;
    private static final int LOCK_REFRESH = 1;
    private static final int DEFAULT_TIMEOUT = 3600;
    private static final int MAX_TIMEOUT = 604800;
    protected static final String DEFAULT_NAMESPACE = "DAV:";
    protected static final ConcurrentDateFormat creationDateFormat;
    private final Hashtable<String, LockInfo> resourceLocks;
    private final Hashtable<String, Vector<String>> lockNullResources;
    private final Vector<LockInfo> collectionLocks;
    private String secret;
    private int maxDepth;
    private boolean allowSpecialPaths;
    public WebdavServlet() {
        this.resourceLocks = new Hashtable<String, LockInfo>();
        this.lockNullResources = new Hashtable<String, Vector<String>>();
        this.collectionLocks = new Vector<LockInfo>();
        this.secret = "catalina";
        this.maxDepth = 3;
        this.allowSpecialPaths = false;
    }
    @Override
    public void init() throws ServletException {
        super.init();
        if ( this.getServletConfig().getInitParameter ( "secret" ) != null ) {
            this.secret = this.getServletConfig().getInitParameter ( "secret" );
        }
        if ( this.getServletConfig().getInitParameter ( "maxDepth" ) != null ) {
            this.maxDepth = Integer.parseInt ( this.getServletConfig().getInitParameter ( "maxDepth" ) );
        }
        if ( this.getServletConfig().getInitParameter ( "allowSpecialPaths" ) != null ) {
            this.allowSpecialPaths = Boolean.parseBoolean ( this.getServletConfig().getInitParameter ( "allowSpecialPaths" ) );
        }
    }
    protected DocumentBuilder getDocumentBuilder() throws ServletException {
        DocumentBuilder documentBuilder = null;
        DocumentBuilderFactory documentBuilderFactory = null;
        try {
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware ( true );
            documentBuilderFactory.setExpandEntityReferences ( false );
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setEntityResolver ( new WebdavResolver ( this.getServletContext() ) );
        } catch ( ParserConfigurationException e ) {
            throw new ServletException ( WebdavServlet.sm.getString ( "webdavservlet.jaxpfailed" ) );
        }
        return documentBuilder;
    }
    protected void service ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException {
        final String path = this.getRelativePath ( req );
        if ( this.isSpecialPath ( path ) ) {
            resp.sendError ( 404 );
            return;
        }
        final String method = req.getMethod();
        if ( this.debug > 0 ) {
            this.log ( "[" + method + "] " + path );
        }
        if ( method.equals ( "PROPFIND" ) ) {
            this.doPropfind ( req, resp );
        } else if ( method.equals ( "PROPPATCH" ) ) {
            this.doProppatch ( req, resp );
        } else if ( method.equals ( "MKCOL" ) ) {
            this.doMkcol ( req, resp );
        } else if ( method.equals ( "COPY" ) ) {
            this.doCopy ( req, resp );
        } else if ( method.equals ( "MOVE" ) ) {
            this.doMove ( req, resp );
        } else if ( method.equals ( "LOCK" ) ) {
            this.doLock ( req, resp );
        } else if ( method.equals ( "UNLOCK" ) ) {
            this.doUnlock ( req, resp );
        } else {
            super.service ( req, resp );
        }
    }
    private final boolean isSpecialPath ( final String path ) {
        return !this.allowSpecialPaths && ( path.toUpperCase ( Locale.ENGLISH ).startsWith ( "/WEB-INF" ) || path.toUpperCase ( Locale.ENGLISH ).startsWith ( "/META-INF" ) );
    }
    @Override
    protected boolean checkIfHeaders ( final HttpServletRequest request, final HttpServletResponse response, final WebResource resource ) throws IOException {
        return super.checkIfHeaders ( request, response, resource );
    }
    @Override
    protected String getRelativePath ( final HttpServletRequest request ) {
        return this.getRelativePath ( request, false );
    }
    @Override
    protected String getRelativePath ( final HttpServletRequest request, final boolean allowEmptyPath ) {
        String pathInfo;
        if ( request.getAttribute ( "javax.servlet.include.request_uri" ) != null ) {
            pathInfo = ( String ) request.getAttribute ( "javax.servlet.include.path_info" );
        } else {
            pathInfo = request.getPathInfo();
        }
        final StringBuilder result = new StringBuilder();
        if ( pathInfo != null ) {
            result.append ( pathInfo );
        }
        if ( result.length() == 0 ) {
            result.append ( '/' );
        }
        return result.toString();
    }
    @Override
    protected String getPathPrefix ( final HttpServletRequest request ) {
        String contextPath = request.getContextPath();
        if ( request.getServletPath() != null ) {
            contextPath += request.getServletPath();
        }
        return contextPath;
    }
    @Override
    protected void doOptions ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException {
        resp.addHeader ( "DAV", "1,2" );
        final StringBuilder methodsAllowed = this.determineMethodsAllowed ( req );
        resp.addHeader ( "Allow", methodsAllowed.toString() );
        resp.addHeader ( "MS-Author-Via", "DAV" );
    }
    protected void doPropfind ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException {
        if ( !this.listings ) {
            final StringBuilder methodsAllowed = this.determineMethodsAllowed ( req );
            resp.addHeader ( "Allow", methodsAllowed.toString() );
            resp.sendError ( 405 );
            return;
        }
        String path = this.getRelativePath ( req );
        if ( path.length() > 1 && path.endsWith ( "/" ) ) {
            path = path.substring ( 0, path.length() - 1 );
        }
        Vector<String> properties = null;
        int depth = this.maxDepth;
        int type = 1;
        final String depthStr = req.getHeader ( "Depth" );
        if ( depthStr == null ) {
            depth = this.maxDepth;
        } else if ( depthStr.equals ( "0" ) ) {
            depth = 0;
        } else if ( depthStr.equals ( "1" ) ) {
            depth = 1;
        } else if ( depthStr.equals ( "infinity" ) ) {
            depth = this.maxDepth;
        }
        Node propNode = null;
        if ( req.getContentLengthLong() > 0L ) {
            final DocumentBuilder documentBuilder = this.getDocumentBuilder();
            try {
                final Document document = documentBuilder.parse ( new InputSource ( ( InputStream ) req.getInputStream() ) );
                final Element rootElement = document.getDocumentElement();
                final NodeList childList = rootElement.getChildNodes();
                for ( int i = 0; i < childList.getLength(); ++i ) {
                    final Node currentNode = childList.item ( i );
                    switch ( currentNode.getNodeType() ) {
                    case 1: {
                        if ( currentNode.getNodeName().endsWith ( "prop" ) ) {
                            type = 0;
                            propNode = currentNode;
                        }
                        if ( currentNode.getNodeName().endsWith ( "propname" ) ) {
                            type = 2;
                        }
                        if ( currentNode.getNodeName().endsWith ( "allprop" ) ) {
                            type = 1;
                            break;
                        }
                        break;
                    }
                    }
                }
            } catch ( SAXException e ) {
                resp.sendError ( 400 );
                return;
            } catch ( IOException e2 ) {
                resp.sendError ( 400 );
                return;
            }
        }
        if ( type == 0 ) {
            properties = new Vector<String>();
            final NodeList childList2 = propNode.getChildNodes();
            for ( int j = 0; j < childList2.getLength(); ++j ) {
                final Node currentNode2 = childList2.item ( j );
                switch ( currentNode2.getNodeType() ) {
                case 1: {
                    final String nodeName = currentNode2.getNodeName();
                    String propertyName = null;
                    if ( nodeName.indexOf ( 58 ) != -1 ) {
                        propertyName = nodeName.substring ( nodeName.indexOf ( 58 ) + 1 );
                    } else {
                        propertyName = nodeName;
                    }
                    properties.addElement ( propertyName );
                    break;
                }
                }
            }
        }
        WebResource resource = this.resources.getResource ( path );
        if ( !resource.exists() ) {
            final int slash = path.lastIndexOf ( 47 );
            if ( slash != -1 ) {
                final String parentPath = path.substring ( 0, slash );
                final Vector<String> currentLockNullResources = this.lockNullResources.get ( parentPath );
                if ( currentLockNullResources != null ) {
                    final Enumeration<String> lockNullResourcesList = currentLockNullResources.elements();
                    while ( lockNullResourcesList.hasMoreElements() ) {
                        final String lockNullPath = lockNullResourcesList.nextElement();
                        if ( lockNullPath.equals ( path ) ) {
                            resp.setStatus ( 207 );
                            resp.setContentType ( "text/xml; charset=UTF-8" );
                            final XMLWriter generatedXML = new XMLWriter ( resp.getWriter() );
                            generatedXML.writeXMLHeader();
                            generatedXML.writeElement ( "D", "DAV:", "multistatus", 0 );
                            this.parseLockNullProperties ( req, generatedXML, lockNullPath, type, properties );
                            generatedXML.writeElement ( "D", "multistatus", 1 );
                            generatedXML.sendData();
                            return;
                        }
                    }
                }
            }
        }
        if ( !resource.exists() ) {
            resp.sendError ( 404, path );
            return;
        }
        resp.setStatus ( 207 );
        resp.setContentType ( "text/xml; charset=UTF-8" );
        final XMLWriter generatedXML2 = new XMLWriter ( resp.getWriter() );
        generatedXML2.writeXMLHeader();
        generatedXML2.writeElement ( "D", "DAV:", "multistatus", 0 );
        if ( depth == 0 ) {
            this.parseProperties ( req, generatedXML2, path, type, properties );
        } else {
            Stack<String> stack = new Stack<String>();
            stack.push ( path );
            Stack<String> stackBelow = new Stack<String>();
            while ( !stack.isEmpty() && depth >= 0 ) {
                final String currentPath = stack.pop();
                this.parseProperties ( req, generatedXML2, currentPath, type, properties );
                resource = this.resources.getResource ( currentPath );
                if ( resource.isDirectory() && depth > 0 ) {
                    final String[] list;
                    final String[] entries = list = this.resources.list ( currentPath );
                    for ( final String entry : list ) {
                        String newPath = currentPath;
                        if ( !newPath.endsWith ( "/" ) ) {
                            newPath += "/";
                        }
                        newPath += entry;
                        stackBelow.push ( newPath );
                    }
                    String lockPath = currentPath;
                    if ( lockPath.endsWith ( "/" ) ) {
                        lockPath = lockPath.substring ( 0, lockPath.length() - 1 );
                    }
                    final Vector<String> currentLockNullResources2 = this.lockNullResources.get ( lockPath );
                    if ( currentLockNullResources2 != null ) {
                        final Enumeration<String> lockNullResourcesList2 = currentLockNullResources2.elements();
                        while ( lockNullResourcesList2.hasMoreElements() ) {
                            final String lockNullPath2 = lockNullResourcesList2.nextElement();
                            this.parseLockNullProperties ( req, generatedXML2, lockNullPath2, type, properties );
                        }
                    }
                }
                if ( stack.isEmpty() ) {
                    --depth;
                    stack = stackBelow;
                    stackBelow = new Stack<String>();
                }
                generatedXML2.sendData();
            }
        }
        generatedXML2.writeElement ( "D", "multistatus", 1 );
        generatedXML2.sendData();
    }
    protected void doProppatch ( final HttpServletRequest req, final HttpServletResponse resp ) throws IOException {
        if ( this.readOnly ) {
            resp.sendError ( 403 );
            return;
        }
        if ( this.isLocked ( req ) ) {
            resp.sendError ( 423 );
            return;
        }
        resp.sendError ( 501 );
    }
    protected void doMkcol ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException {
        if ( this.readOnly ) {
            resp.sendError ( 403 );
            return;
        }
        if ( this.isLocked ( req ) ) {
            resp.sendError ( 423 );
            return;
        }
        final String path = this.getRelativePath ( req );
        final WebResource resource = this.resources.getResource ( path );
        if ( resource.exists() ) {
            final StringBuilder methodsAllowed = this.determineMethodsAllowed ( req );
            resp.addHeader ( "Allow", methodsAllowed.toString() );
            resp.sendError ( 405 );
            return;
        }
        if ( req.getContentLengthLong() > 0L ) {
            final DocumentBuilder documentBuilder = this.getDocumentBuilder();
            try {
                documentBuilder.parse ( new InputSource ( ( InputStream ) req.getInputStream() ) );
                resp.sendError ( 501 );
                return;
            } catch ( SAXException saxe ) {
                resp.sendError ( 415 );
                return;
            }
        }
        if ( this.resources.mkdir ( path ) ) {
            resp.setStatus ( 201 );
            this.lockNullResources.remove ( path );
        } else {
            resp.sendError ( 409, WebdavStatus.getStatusText ( 409 ) );
        }
    }
    @Override
    protected void doDelete ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException {
        if ( this.readOnly ) {
            resp.sendError ( 403 );
            return;
        }
        if ( this.isLocked ( req ) ) {
            resp.sendError ( 423 );
            return;
        }
        this.deleteResource ( req, resp );
    }
    @Override
    protected void doPut ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException {
        if ( this.isLocked ( req ) ) {
            resp.sendError ( 423 );
            return;
        }
        super.doPut ( req, resp );
        final String path = this.getRelativePath ( req );
        this.lockNullResources.remove ( path );
    }
    protected void doCopy ( final HttpServletRequest req, final HttpServletResponse resp ) throws IOException {
        if ( this.readOnly ) {
            resp.sendError ( 403 );
            return;
        }
        this.copyResource ( req, resp );
    }
    protected void doMove ( final HttpServletRequest req, final HttpServletResponse resp ) throws IOException {
        if ( this.readOnly ) {
            resp.sendError ( 403 );
            return;
        }
        if ( this.isLocked ( req ) ) {
            resp.sendError ( 423 );
            return;
        }
        final String path = this.getRelativePath ( req );
        if ( this.copyResource ( req, resp ) ) {
            this.deleteResource ( path, req, resp, false );
        }
    }
    protected void doLock ( final HttpServletRequest req, final HttpServletResponse resp ) throws ServletException, IOException {
        if ( this.readOnly ) {
            resp.sendError ( 403 );
            return;
        }
        if ( this.isLocked ( req ) ) {
            resp.sendError ( 423 );
            return;
        }
        LockInfo lock = new LockInfo();
        final String depthStr = req.getHeader ( "Depth" );
        if ( depthStr == null ) {
            lock.depth = this.maxDepth;
        } else if ( depthStr.equals ( "0" ) ) {
            lock.depth = 0;
        } else {
            lock.depth = this.maxDepth;
        }
        int lockDuration = 3600;
        String lockDurationStr = req.getHeader ( "Timeout" );
        if ( lockDurationStr == null ) {
            lockDuration = 3600;
        } else {
            final int commaPos = lockDurationStr.indexOf ( 44 );
            if ( commaPos != -1 ) {
                lockDurationStr = lockDurationStr.substring ( 0, commaPos );
            }
            if ( lockDurationStr.startsWith ( "Second-" ) ) {
                lockDuration = Integer.parseInt ( lockDurationStr.substring ( 7 ) );
            } else if ( lockDurationStr.equalsIgnoreCase ( "infinity" ) ) {
                lockDuration = 604800;
            } else {
                try {
                    lockDuration = Integer.parseInt ( lockDurationStr );
                } catch ( NumberFormatException e ) {
                    lockDuration = 604800;
                }
            }
            if ( lockDuration == 0 ) {
                lockDuration = 3600;
            }
            if ( lockDuration > 604800 ) {
                lockDuration = 604800;
            }
        }
        lock.expiresAt = System.currentTimeMillis() + lockDuration * 1000;
        int lockRequestType = 0;
        Node lockInfoNode = null;
        final DocumentBuilder documentBuilder = this.getDocumentBuilder();
        try {
            final Document document = documentBuilder.parse ( new InputSource ( ( InputStream ) req.getInputStream() ) );
            final Element rootElement = ( Element ) ( lockInfoNode = document.getDocumentElement() );
        } catch ( IOException e2 ) {
            lockRequestType = 1;
        } catch ( SAXException e3 ) {
            lockRequestType = 1;
        }
        if ( lockInfoNode != null ) {
            NodeList childList = lockInfoNode.getChildNodes();
            StringWriter strWriter = null;
            DOMWriter domWriter = null;
            Node lockScopeNode = null;
            Node lockTypeNode = null;
            Node lockOwnerNode = null;
            for ( int i = 0; i < childList.getLength(); ++i ) {
                final Node currentNode = childList.item ( i );
                switch ( currentNode.getNodeType() ) {
                case 1: {
                    final String nodeName = currentNode.getNodeName();
                    if ( nodeName.endsWith ( "lockscope" ) ) {
                        lockScopeNode = currentNode;
                    }
                    if ( nodeName.endsWith ( "locktype" ) ) {
                        lockTypeNode = currentNode;
                    }
                    if ( nodeName.endsWith ( "owner" ) ) {
                        lockOwnerNode = currentNode;
                        break;
                    }
                    break;
                }
                }
            }
            if ( lockScopeNode != null ) {
                childList = lockScopeNode.getChildNodes();
                for ( int i = 0; i < childList.getLength(); ++i ) {
                    final Node currentNode = childList.item ( i );
                    switch ( currentNode.getNodeType() ) {
                    case 1: {
                        final String tempScope = currentNode.getNodeName();
                        if ( tempScope.indexOf ( 58 ) != -1 ) {
                            lock.scope = tempScope.substring ( tempScope.indexOf ( 58 ) + 1 );
                            break;
                        }
                        lock.scope = tempScope;
                        break;
                    }
                    }
                }
                if ( lock.scope == null ) {
                    resp.setStatus ( 400 );
                }
            } else {
                resp.setStatus ( 400 );
            }
            if ( lockTypeNode != null ) {
                childList = lockTypeNode.getChildNodes();
                for ( int i = 0; i < childList.getLength(); ++i ) {
                    final Node currentNode = childList.item ( i );
                    switch ( currentNode.getNodeType() ) {
                    case 1: {
                        final String tempType = currentNode.getNodeName();
                        if ( tempType.indexOf ( 58 ) != -1 ) {
                            lock.type = tempType.substring ( tempType.indexOf ( 58 ) + 1 );
                            break;
                        }
                        lock.type = tempType;
                        break;
                    }
                    }
                }
                if ( lock.type == null ) {
                    resp.setStatus ( 400 );
                }
            } else {
                resp.setStatus ( 400 );
            }
            if ( lockOwnerNode != null ) {
                childList = lockOwnerNode.getChildNodes();
                for ( int i = 0; i < childList.getLength(); ++i ) {
                    final Node currentNode = childList.item ( i );
                    switch ( currentNode.getNodeType() ) {
                    case 3: {
                        final StringBuilder sb = new StringBuilder();
                        final LockInfo lockInfo = lock;
                        lockInfo.owner = sb.append ( lockInfo.owner ).append ( currentNode.getNodeValue() ).toString();
                        break;
                    }
                    case 1: {
                        strWriter = new StringWriter();
                        domWriter = new DOMWriter ( strWriter, true );
                        domWriter.print ( currentNode );
                        final StringBuilder sb2 = new StringBuilder();
                        final LockInfo lockInfo2 = lock;
                        lockInfo2.owner = sb2.append ( lockInfo2.owner ).append ( strWriter.toString() ).toString();
                        break;
                    }
                    }
                }
                if ( lock.owner == null ) {
                    resp.setStatus ( 400 );
                }
            } else {
                lock.owner = "";
            }
        }
        final String path = this.getRelativePath ( req );
        lock.path = path;
        final WebResource resource = this.resources.getResource ( path );
        Enumeration<LockInfo> locksList = null;
        if ( lockRequestType == 0 ) {
            final String lockTokenStr = req.getServletPath() + "-" + lock.type + "-" + lock.scope + "-" + req.getUserPrincipal() + "-" + lock.depth + "-" + lock.owner + "-" + lock.tokens + "-" + lock.expiresAt + "-" + System.currentTimeMillis() + "-" + this.secret;
            final String lockToken = MD5Encoder.encode ( ConcurrentMessageDigest.digestMD5 ( new byte[][] { lockTokenStr.getBytes ( StandardCharsets.ISO_8859_1 ) } ) );
            if ( resource.isDirectory() && lock.depth == this.maxDepth ) {
                final Vector<String> lockPaths = new Vector<String>();
                locksList = this.collectionLocks.elements();
                while ( locksList.hasMoreElements() ) {
                    final LockInfo currentLock = locksList.nextElement();
                    if ( currentLock.hasExpired() ) {
                        this.resourceLocks.remove ( currentLock.path );
                    } else {
                        if ( !currentLock.path.startsWith ( lock.path ) || ( !currentLock.isExclusive() && !lock.isExclusive() ) ) {
                            continue;
                        }
                        lockPaths.addElement ( currentLock.path );
                    }
                }
                locksList = this.resourceLocks.elements();
                while ( locksList.hasMoreElements() ) {
                    final LockInfo currentLock = locksList.nextElement();
                    if ( currentLock.hasExpired() ) {
                        this.resourceLocks.remove ( currentLock.path );
                    } else {
                        if ( !currentLock.path.startsWith ( lock.path ) || ( !currentLock.isExclusive() && !lock.isExclusive() ) ) {
                            continue;
                        }
                        lockPaths.addElement ( currentLock.path );
                    }
                }
                if ( !lockPaths.isEmpty() ) {
                    final Enumeration<String> lockPathsList = lockPaths.elements();
                    resp.setStatus ( 409 );
                    final XMLWriter generatedXML = new XMLWriter();
                    generatedXML.writeXMLHeader();
                    generatedXML.writeElement ( "D", "DAV:", "multistatus", 0 );
                    while ( lockPathsList.hasMoreElements() ) {
                        generatedXML.writeElement ( "D", "response", 0 );
                        generatedXML.writeElement ( "D", "href", 0 );
                        generatedXML.writeText ( lockPathsList.nextElement() );
                        generatedXML.writeElement ( "D", "href", 1 );
                        generatedXML.writeElement ( "D", "status", 0 );
                        generatedXML.writeText ( "HTTP/1.1 423 " + WebdavStatus.getStatusText ( 423 ) );
                        generatedXML.writeElement ( "D", "status", 1 );
                        generatedXML.writeElement ( "D", "response", 1 );
                    }
                    generatedXML.writeElement ( "D", "multistatus", 1 );
                    final Writer writer = resp.getWriter();
                    writer.write ( generatedXML.toString() );
                    writer.close();
                    return;
                }
                boolean addLock = true;
                locksList = this.collectionLocks.elements();
                while ( locksList.hasMoreElements() ) {
                    final LockInfo currentLock2 = locksList.nextElement();
                    if ( currentLock2.path.equals ( lock.path ) ) {
                        if ( currentLock2.isExclusive() ) {
                            resp.sendError ( 423 );
                            return;
                        }
                        if ( lock.isExclusive() ) {
                            resp.sendError ( 423 );
                            return;
                        }
                        currentLock2.tokens.addElement ( lockToken );
                        lock = currentLock2;
                        addLock = false;
                    }
                }
                if ( addLock ) {
                    lock.tokens.addElement ( lockToken );
                    this.collectionLocks.addElement ( lock );
                }
            } else {
                final LockInfo presentLock = this.resourceLocks.get ( lock.path );
                if ( presentLock != null ) {
                    if ( presentLock.isExclusive() || lock.isExclusive() ) {
                        resp.sendError ( 412 );
                        return;
                    }
                    presentLock.tokens.addElement ( lockToken );
                    lock = presentLock;
                } else {
                    lock.tokens.addElement ( lockToken );
                    this.resourceLocks.put ( lock.path, lock );
                    if ( !resource.exists() ) {
                        final int slash = lock.path.lastIndexOf ( 47 );
                        final String parentPath = lock.path.substring ( 0, slash );
                        Vector<String> lockNulls = this.lockNullResources.get ( parentPath );
                        if ( lockNulls == null ) {
                            lockNulls = new Vector<String>();
                            this.lockNullResources.put ( parentPath, lockNulls );
                        }
                        lockNulls.addElement ( lock.path );
                    }
                    resp.addHeader ( "Lock-Token", "<opaquelocktoken:" + lockToken + ">" );
                }
            }
        }
        if ( lockRequestType == 1 ) {
            String ifHeader = req.getHeader ( "If" );
            if ( ifHeader == null ) {
                ifHeader = "";
            }
            LockInfo toRenew = this.resourceLocks.get ( path );
            Enumeration<String> tokenList = null;
            if ( toRenew != null ) {
                tokenList = toRenew.tokens.elements();
                while ( tokenList.hasMoreElements() ) {
                    final String token = tokenList.nextElement();
                    if ( ifHeader.indexOf ( token ) != -1 ) {
                        toRenew.expiresAt = lock.expiresAt;
                        lock = toRenew;
                    }
                }
            }
            final Enumeration<LockInfo> collectionLocksList = this.collectionLocks.elements();
            while ( collectionLocksList.hasMoreElements() ) {
                toRenew = collectionLocksList.nextElement();
                if ( path.equals ( toRenew.path ) ) {
                    tokenList = toRenew.tokens.elements();
                    while ( tokenList.hasMoreElements() ) {
                        final String token2 = tokenList.nextElement();
                        if ( ifHeader.indexOf ( token2 ) != -1 ) {
                            toRenew.expiresAt = lock.expiresAt;
                            lock = toRenew;
                        }
                    }
                }
            }
        }
        final XMLWriter generatedXML2 = new XMLWriter();
        generatedXML2.writeXMLHeader();
        generatedXML2.writeElement ( "D", "DAV:", "prop", 0 );
        generatedXML2.writeElement ( "D", "lockdiscovery", 0 );
        lock.toXML ( generatedXML2 );
        generatedXML2.writeElement ( "D", "lockdiscovery", 1 );
        generatedXML2.writeElement ( "D", "prop", 1 );
        resp.setStatus ( 200 );
        resp.setContentType ( "text/xml; charset=UTF-8" );
        final Writer writer2 = resp.getWriter();
        writer2.write ( generatedXML2.toString() );
        writer2.close();
    }
    protected void doUnlock ( final HttpServletRequest req, final HttpServletResponse resp ) throws IOException {
        if ( this.readOnly ) {
            resp.sendError ( 403 );
            return;
        }
        if ( this.isLocked ( req ) ) {
            resp.sendError ( 423 );
            return;
        }
        final String path = this.getRelativePath ( req );
        String lockTokenHeader = req.getHeader ( "Lock-Token" );
        if ( lockTokenHeader == null ) {
            lockTokenHeader = "";
        }
        LockInfo lock = this.resourceLocks.get ( path );
        Enumeration<String> tokenList = null;
        if ( lock != null ) {
            tokenList = lock.tokens.elements();
            while ( tokenList.hasMoreElements() ) {
                final String token = tokenList.nextElement();
                if ( lockTokenHeader.indexOf ( token ) != -1 ) {
                    lock.tokens.removeElement ( token );
                }
            }
            if ( lock.tokens.isEmpty() ) {
                this.resourceLocks.remove ( path );
                this.lockNullResources.remove ( path );
            }
        }
        final Enumeration<LockInfo> collectionLocksList = this.collectionLocks.elements();
        while ( collectionLocksList.hasMoreElements() ) {
            lock = collectionLocksList.nextElement();
            if ( path.equals ( lock.path ) ) {
                tokenList = lock.tokens.elements();
                while ( tokenList.hasMoreElements() ) {
                    final String token2 = tokenList.nextElement();
                    if ( lockTokenHeader.indexOf ( token2 ) != -1 ) {
                        lock.tokens.removeElement ( token2 );
                        break;
                    }
                }
                if ( !lock.tokens.isEmpty() ) {
                    continue;
                }
                this.collectionLocks.removeElement ( lock );
                this.lockNullResources.remove ( path );
            }
        }
        resp.setStatus ( 204 );
    }
    private boolean isLocked ( final HttpServletRequest req ) {
        final String path = this.getRelativePath ( req );
        String ifHeader = req.getHeader ( "If" );
        if ( ifHeader == null ) {
            ifHeader = "";
        }
        String lockTokenHeader = req.getHeader ( "Lock-Token" );
        if ( lockTokenHeader == null ) {
            lockTokenHeader = "";
        }
        return this.isLocked ( path, ifHeader + lockTokenHeader );
    }
    private boolean isLocked ( final String path, final String ifHeader ) {
        LockInfo lock = this.resourceLocks.get ( path );
        Enumeration<String> tokenList = null;
        if ( lock != null && lock.hasExpired() ) {
            this.resourceLocks.remove ( path );
        } else if ( lock != null ) {
            tokenList = lock.tokens.elements();
            boolean tokenMatch = false;
            while ( tokenList.hasMoreElements() ) {
                final String token = tokenList.nextElement();
                if ( ifHeader.indexOf ( token ) != -1 ) {
                    tokenMatch = true;
                    break;
                }
            }
            if ( !tokenMatch ) {
                return true;
            }
        }
        final Enumeration<LockInfo> collectionLocksList = this.collectionLocks.elements();
        while ( collectionLocksList.hasMoreElements() ) {
            lock = collectionLocksList.nextElement();
            if ( lock.hasExpired() ) {
                this.collectionLocks.removeElement ( lock );
            } else {
                if ( !path.startsWith ( lock.path ) ) {
                    continue;
                }
                tokenList = lock.tokens.elements();
                boolean tokenMatch2 = false;
                while ( tokenList.hasMoreElements() ) {
                    final String token2 = tokenList.nextElement();
                    if ( ifHeader.indexOf ( token2 ) != -1 ) {
                        tokenMatch2 = true;
                        break;
                    }
                }
                if ( !tokenMatch2 ) {
                    return true;
                }
                continue;
            }
        }
        return false;
    }
    private boolean copyResource ( final HttpServletRequest req, final HttpServletResponse resp ) throws IOException {
        String destinationPath = req.getHeader ( "Destination" );
        if ( destinationPath == null ) {
            resp.sendError ( 400 );
            return false;
        }
        destinationPath = UDecoder.URLDecode ( destinationPath, "UTF8" );
        final int protocolIndex = destinationPath.indexOf ( "://" );
        if ( protocolIndex >= 0 ) {
            final int firstSeparator = destinationPath.indexOf ( 47, protocolIndex + 4 );
            if ( firstSeparator < 0 ) {
                destinationPath = "/";
            } else {
                destinationPath = destinationPath.substring ( firstSeparator );
            }
        } else {
            final String hostName = req.getServerName();
            if ( hostName != null && destinationPath.startsWith ( hostName ) ) {
                destinationPath = destinationPath.substring ( hostName.length() );
            }
            final int portIndex = destinationPath.indexOf ( 58 );
            if ( portIndex >= 0 ) {
                destinationPath = destinationPath.substring ( portIndex );
            }
            if ( destinationPath.startsWith ( ":" ) ) {
                final int firstSeparator2 = destinationPath.indexOf ( 47 );
                if ( firstSeparator2 < 0 ) {
                    destinationPath = "/";
                } else {
                    destinationPath = destinationPath.substring ( firstSeparator2 );
                }
            }
        }
        destinationPath = RequestUtil.normalize ( destinationPath );
        final String contextPath = req.getContextPath();
        if ( contextPath != null && destinationPath.startsWith ( contextPath ) ) {
            destinationPath = destinationPath.substring ( contextPath.length() );
        }
        final String pathInfo = req.getPathInfo();
        if ( pathInfo != null ) {
            final String servletPath = req.getServletPath();
            if ( servletPath != null && destinationPath.startsWith ( servletPath ) ) {
                destinationPath = destinationPath.substring ( servletPath.length() );
            }
        }
        if ( this.debug > 0 ) {
            this.log ( "Dest path :" + destinationPath );
        }
        if ( this.isSpecialPath ( destinationPath ) ) {
            resp.sendError ( 403 );
            return false;
        }
        final String path = this.getRelativePath ( req );
        if ( destinationPath.equals ( path ) ) {
            resp.sendError ( 403 );
            return false;
        }
        boolean overwrite = true;
        final String overwriteHeader = req.getHeader ( "Overwrite" );
        if ( overwriteHeader != null ) {
            overwrite = overwriteHeader.equalsIgnoreCase ( "T" );
        }
        final WebResource destination = this.resources.getResource ( destinationPath );
        if ( overwrite ) {
            if ( destination.exists() ) {
                if ( !this.deleteResource ( destinationPath, req, resp, true ) ) {
                    return false;
                }
            } else {
                resp.setStatus ( 201 );
            }
        } else if ( destination.exists() ) {
            resp.sendError ( 412 );
            return false;
        }
        final Hashtable<String, Integer> errorList = new Hashtable<String, Integer>();
        final boolean result = this.copyResource ( errorList, path, destinationPath );
        if ( !result || !errorList.isEmpty() ) {
            if ( errorList.size() == 1 ) {
                resp.sendError ( ( int ) errorList.elements().nextElement() );
            } else {
                this.sendReport ( req, resp, errorList );
            }
            return false;
        }
        if ( destination.exists() ) {
            resp.setStatus ( 204 );
        } else {
            resp.setStatus ( 201 );
        }
        this.lockNullResources.remove ( destinationPath );
        return true;
    }
    private boolean copyResource ( final Hashtable<String, Integer> errorList, final String source, final String dest ) {
        if ( this.debug > 1 ) {
            this.log ( "Copy: " + source + " To: " + dest );
        }
        final WebResource sourceResource = this.resources.getResource ( source );
        if ( sourceResource.isDirectory() ) {
            if ( !this.resources.mkdir ( dest ) ) {
                final WebResource destResource = this.resources.getResource ( dest );
                if ( !destResource.isDirectory() ) {
                    errorList.put ( dest, 409 );
                    return false;
                }
            }
            final String[] list;
            final String[] entries = list = this.resources.list ( source );
            for ( final String entry : list ) {
                String childDest = dest;
                if ( !childDest.equals ( "/" ) ) {
                    childDest += "/";
                }
                childDest += entry;
                String childSrc = source;
                if ( !childSrc.equals ( "/" ) ) {
                    childSrc += "/";
                }
                childSrc += entry;
                this.copyResource ( errorList, childSrc, childDest );
            }
        } else {
            if ( !sourceResource.isFile() ) {
                errorList.put ( source, 500 );
                return false;
            }
            final WebResource destResource = this.resources.getResource ( dest );
            if ( !destResource.exists() && !destResource.getWebappPath().endsWith ( "/" ) ) {
                final int lastSlash = destResource.getWebappPath().lastIndexOf ( 47 );
                if ( lastSlash > 0 ) {
                    final String parent = destResource.getWebappPath().substring ( 0, lastSlash );
                    final WebResource parentResource = this.resources.getResource ( parent );
                    if ( !parentResource.isDirectory() ) {
                        errorList.put ( source, 409 );
                        return false;
                    }
                }
            }
            try ( final InputStream is = sourceResource.getInputStream() ) {
                if ( !this.resources.write ( dest, is, false ) ) {
                    errorList.put ( source, 500 );
                    return false;
                }
            } catch ( IOException e ) {
                this.log ( WebdavServlet.sm.getString ( "webdavservlet.inputstreamclosefail", source ), ( Throwable ) e );
            }
        }
        return true;
    }
    private boolean deleteResource ( final HttpServletRequest req, final HttpServletResponse resp ) throws IOException {
        final String path = this.getRelativePath ( req );
        return this.deleteResource ( path, req, resp, true );
    }
    private boolean deleteResource ( final String path, final HttpServletRequest req, final HttpServletResponse resp, final boolean setStatus ) throws IOException {
        String ifHeader = req.getHeader ( "If" );
        if ( ifHeader == null ) {
            ifHeader = "";
        }
        String lockTokenHeader = req.getHeader ( "Lock-Token" );
        if ( lockTokenHeader == null ) {
            lockTokenHeader = "";
        }
        if ( this.isLocked ( path, ifHeader + lockTokenHeader ) ) {
            resp.sendError ( 423 );
            return false;
        }
        final WebResource resource = this.resources.getResource ( path );
        if ( !resource.exists() ) {
            resp.sendError ( 404 );
            return false;
        }
        if ( !resource.isDirectory() ) {
            if ( !resource.delete() ) {
                resp.sendError ( 500 );
                return false;
            }
        } else {
            final Hashtable<String, Integer> errorList = new Hashtable<String, Integer>();
            this.deleteCollection ( req, path, errorList );
            if ( !resource.delete() ) {
                errorList.put ( path, 500 );
            }
            if ( !errorList.isEmpty() ) {
                this.sendReport ( req, resp, errorList );
                return false;
            }
        }
        if ( setStatus ) {
            resp.setStatus ( 204 );
        }
        return true;
    }
    private void deleteCollection ( final HttpServletRequest req, final String path, final Hashtable<String, Integer> errorList ) {
        if ( this.debug > 1 ) {
            this.log ( "Delete:" + path );
        }
        if ( this.isSpecialPath ( path ) ) {
            errorList.put ( path, 403 );
            return;
        }
        String ifHeader = req.getHeader ( "If" );
        if ( ifHeader == null ) {
            ifHeader = "";
        }
        String lockTokenHeader = req.getHeader ( "Lock-Token" );
        if ( lockTokenHeader == null ) {
            lockTokenHeader = "";
        }
        final String[] list;
        final String[] entries = list = this.resources.list ( path );
        for ( final String entry : list ) {
            String childName = path;
            if ( !childName.equals ( "/" ) ) {
                childName += "/";
            }
            childName += entry;
            if ( this.isLocked ( childName, ifHeader + lockTokenHeader ) ) {
                errorList.put ( childName, 423 );
            } else {
                final WebResource childResource = this.resources.getResource ( childName );
                if ( childResource.isDirectory() ) {
                    this.deleteCollection ( req, childName, errorList );
                }
                if ( !childResource.delete() && !childResource.isDirectory() ) {
                    errorList.put ( childName, 500 );
                }
            }
        }
    }
    private void sendReport ( final HttpServletRequest req, final HttpServletResponse resp, final Hashtable<String, Integer> errorList ) throws IOException {
        resp.setStatus ( 207 );
        final String absoluteUri = req.getRequestURI();
        final String relativePath = this.getRelativePath ( req );
        final XMLWriter generatedXML = new XMLWriter();
        generatedXML.writeXMLHeader();
        generatedXML.writeElement ( "D", "DAV:", "multistatus", 0 );
        final Enumeration<String> pathList = errorList.keys();
        while ( pathList.hasMoreElements() ) {
            final String errorPath = pathList.nextElement();
            final int errorCode = errorList.get ( errorPath );
            generatedXML.writeElement ( "D", "response", 0 );
            generatedXML.writeElement ( "D", "href", 0 );
            String toAppend = errorPath.substring ( relativePath.length() );
            if ( !toAppend.startsWith ( "/" ) ) {
                toAppend = "/" + toAppend;
            }
            generatedXML.writeText ( absoluteUri + toAppend );
            generatedXML.writeElement ( "D", "href", 1 );
            generatedXML.writeElement ( "D", "status", 0 );
            generatedXML.writeText ( "HTTP/1.1 " + errorCode + " " + WebdavStatus.getStatusText ( errorCode ) );
            generatedXML.writeElement ( "D", "status", 1 );
            generatedXML.writeElement ( "D", "response", 1 );
        }
        generatedXML.writeElement ( "D", "multistatus", 1 );
        final Writer writer = resp.getWriter();
        writer.write ( generatedXML.toString() );
        writer.close();
    }
    private void parseProperties ( final HttpServletRequest req, final XMLWriter generatedXML, final String path, final int type, final Vector<String> propertiesVector ) {
        if ( this.isSpecialPath ( path ) ) {
            return;
        }
        final WebResource resource = this.resources.getResource ( path );
        if ( !resource.exists() ) {
            return;
        }
        String href = req.getContextPath() + req.getServletPath();
        if ( href.endsWith ( "/" ) && path.startsWith ( "/" ) ) {
            href += path.substring ( 1 );
        } else {
            href += path;
        }
        if ( resource.isDirectory() && !href.endsWith ( "/" ) ) {
            href += "/";
        }
        final String rewrittenUrl = this.rewriteUrl ( href );
        this.generatePropFindResponse ( generatedXML, rewrittenUrl, path, type, propertiesVector, resource.isFile(), false, resource.getCreation(), resource.getLastModified(), resource.getContentLength(), this.getServletContext().getMimeType ( resource.getName() ), resource.getETag() );
    }
    private void parseLockNullProperties ( final HttpServletRequest req, final XMLWriter generatedXML, final String path, final int type, final Vector<String> propertiesVector ) {
        if ( this.isSpecialPath ( path ) ) {
            return;
        }
        final LockInfo lock = this.resourceLocks.get ( path );
        if ( lock == null ) {
            return;
        }
        final String absoluteUri = req.getRequestURI();
        final String relativePath = this.getRelativePath ( req );
        String toAppend = path.substring ( relativePath.length() );
        if ( !toAppend.startsWith ( "/" ) ) {
            toAppend = "/" + toAppend;
        }
        final String rewrittenUrl = this.rewriteUrl ( RequestUtil.normalize ( absoluteUri + toAppend ) );
        this.generatePropFindResponse ( generatedXML, rewrittenUrl, path, type, propertiesVector, true, true, lock.creationDate.getTime(), lock.creationDate.getTime(), 0L, "", "" );
    }
    private void generatePropFindResponse ( final XMLWriter generatedXML, final String rewrittenUrl, final String path, final int propFindType, final Vector<String> propertiesVector, final boolean isFile, final boolean isLockNull, final long created, final long lastModified, final long contentLength, final String contentType, final String eTag ) {
        generatedXML.writeElement ( "D", "response", 0 );
        String status = "HTTP/1.1 200 " + WebdavStatus.getStatusText ( 200 );
        generatedXML.writeElement ( "D", "href", 0 );
        generatedXML.writeText ( rewrittenUrl );
        generatedXML.writeElement ( "D", "href", 1 );
        String resourceName = path;
        final int lastSlash = path.lastIndexOf ( 47 );
        if ( lastSlash != -1 ) {
            resourceName = resourceName.substring ( lastSlash + 1 );
        }
        switch ( propFindType ) {
        case 1: {
            generatedXML.writeElement ( "D", "propstat", 0 );
            generatedXML.writeElement ( "D", "prop", 0 );
            generatedXML.writeProperty ( "D", "creationdate", this.getISOCreationDate ( created ) );
            generatedXML.writeElement ( "D", "displayname", 0 );
            generatedXML.writeData ( resourceName );
            generatedXML.writeElement ( "D", "displayname", 1 );
            if ( isFile ) {
                generatedXML.writeProperty ( "D", "getlastmodified", FastHttpDateFormat.formatDate ( lastModified, null ) );
                generatedXML.writeProperty ( "D", "getcontentlength", Long.toString ( contentLength ) );
                if ( contentType != null ) {
                    generatedXML.writeProperty ( "D", "getcontenttype", contentType );
                }
                generatedXML.writeProperty ( "D", "getetag", eTag );
                if ( isLockNull ) {
                    generatedXML.writeElement ( "D", "resourcetype", 0 );
                    generatedXML.writeElement ( "D", "lock-null", 2 );
                    generatedXML.writeElement ( "D", "resourcetype", 1 );
                } else {
                    generatedXML.writeElement ( "D", "resourcetype", 2 );
                }
            } else {
                generatedXML.writeElement ( "D", "resourcetype", 0 );
                generatedXML.writeElement ( "D", "collection", 2 );
                generatedXML.writeElement ( "D", "resourcetype", 1 );
            }
            generatedXML.writeProperty ( "D", "source", "" );
            final String supportedLocks = "<D:lockentry><D:lockscope><D:exclusive/></D:lockscope><D:locktype><D:write/></D:locktype></D:lockentry><D:lockentry><D:lockscope><D:shared/></D:lockscope><D:locktype><D:write/></D:locktype></D:lockentry>";
            generatedXML.writeElement ( "D", "supportedlock", 0 );
            generatedXML.writeText ( supportedLocks );
            generatedXML.writeElement ( "D", "supportedlock", 1 );
            this.generateLockDiscovery ( path, generatedXML );
            generatedXML.writeElement ( "D", "prop", 1 );
            generatedXML.writeElement ( "D", "status", 0 );
            generatedXML.writeText ( status );
            generatedXML.writeElement ( "D", "status", 1 );
            generatedXML.writeElement ( "D", "propstat", 1 );
            break;
        }
        case 2: {
            generatedXML.writeElement ( "D", "propstat", 0 );
            generatedXML.writeElement ( "D", "prop", 0 );
            generatedXML.writeElement ( "D", "creationdate", 2 );
            generatedXML.writeElement ( "D", "displayname", 2 );
            if ( isFile ) {
                generatedXML.writeElement ( "D", "getcontentlanguage", 2 );
                generatedXML.writeElement ( "D", "getcontentlength", 2 );
                generatedXML.writeElement ( "D", "getcontenttype", 2 );
                generatedXML.writeElement ( "D", "getetag", 2 );
                generatedXML.writeElement ( "D", "getlastmodified", 2 );
            }
            generatedXML.writeElement ( "D", "resourcetype", 2 );
            generatedXML.writeElement ( "D", "source", 2 );
            generatedXML.writeElement ( "D", "lockdiscovery", 2 );
            generatedXML.writeElement ( "D", "prop", 1 );
            generatedXML.writeElement ( "D", "status", 0 );
            generatedXML.writeText ( status );
            generatedXML.writeElement ( "D", "status", 1 );
            generatedXML.writeElement ( "D", "propstat", 1 );
            break;
        }
        case 0: {
            final Vector<String> propertiesNotFound = new Vector<String>();
            generatedXML.writeElement ( "D", "propstat", 0 );
            generatedXML.writeElement ( "D", "prop", 0 );
            final Enumeration<String> properties = propertiesVector.elements();
            while ( properties.hasMoreElements() ) {
                final String property = properties.nextElement();
                if ( property.equals ( "creationdate" ) ) {
                    generatedXML.writeProperty ( "D", "creationdate", this.getISOCreationDate ( created ) );
                } else if ( property.equals ( "displayname" ) ) {
                    generatedXML.writeElement ( "D", "displayname", 0 );
                    generatedXML.writeData ( resourceName );
                    generatedXML.writeElement ( "D", "displayname", 1 );
                } else if ( property.equals ( "getcontentlanguage" ) ) {
                    if ( isFile ) {
                        generatedXML.writeElement ( "D", "getcontentlanguage", 2 );
                    } else {
                        propertiesNotFound.addElement ( property );
                    }
                } else if ( property.equals ( "getcontentlength" ) ) {
                    if ( isFile ) {
                        generatedXML.writeProperty ( "D", "getcontentlength", Long.toString ( contentLength ) );
                    } else {
                        propertiesNotFound.addElement ( property );
                    }
                } else if ( property.equals ( "getcontenttype" ) ) {
                    if ( isFile ) {
                        generatedXML.writeProperty ( "D", "getcontenttype", contentType );
                    } else {
                        propertiesNotFound.addElement ( property );
                    }
                } else if ( property.equals ( "getetag" ) ) {
                    if ( isFile ) {
                        generatedXML.writeProperty ( "D", "getetag", eTag );
                    } else {
                        propertiesNotFound.addElement ( property );
                    }
                } else if ( property.equals ( "getlastmodified" ) ) {
                    if ( isFile ) {
                        generatedXML.writeProperty ( "D", "getlastmodified", FastHttpDateFormat.formatDate ( lastModified, null ) );
                    } else {
                        propertiesNotFound.addElement ( property );
                    }
                } else if ( property.equals ( "resourcetype" ) ) {
                    if ( isFile ) {
                        if ( isLockNull ) {
                            generatedXML.writeElement ( "D", "resourcetype", 0 );
                            generatedXML.writeElement ( "D", "lock-null", 2 );
                            generatedXML.writeElement ( "D", "resourcetype", 1 );
                        } else {
                            generatedXML.writeElement ( "D", "resourcetype", 2 );
                        }
                    } else {
                        generatedXML.writeElement ( "D", "resourcetype", 0 );
                        generatedXML.writeElement ( "D", "collection", 2 );
                        generatedXML.writeElement ( "D", "resourcetype", 1 );
                    }
                } else if ( property.equals ( "source" ) ) {
                    generatedXML.writeProperty ( "D", "source", "" );
                } else if ( property.equals ( "supportedlock" ) ) {
                    final String supportedLocks = "<D:lockentry><D:lockscope><D:exclusive/></D:lockscope><D:locktype><D:write/></D:locktype></D:lockentry><D:lockentry><D:lockscope><D:shared/></D:lockscope><D:locktype><D:write/></D:locktype></D:lockentry>";
                    generatedXML.writeElement ( "D", "supportedlock", 0 );
                    generatedXML.writeText ( supportedLocks );
                    generatedXML.writeElement ( "D", "supportedlock", 1 );
                } else if ( property.equals ( "lockdiscovery" ) ) {
                    if ( this.generateLockDiscovery ( path, generatedXML ) ) {
                        continue;
                    }
                    propertiesNotFound.addElement ( property );
                } else {
                    propertiesNotFound.addElement ( property );
                }
            }
            generatedXML.writeElement ( "D", "prop", 1 );
            generatedXML.writeElement ( "D", "status", 0 );
            generatedXML.writeText ( status );
            generatedXML.writeElement ( "D", "status", 1 );
            generatedXML.writeElement ( "D", "propstat", 1 );
            final Enumeration<String> propertiesNotFoundList = propertiesNotFound.elements();
            if ( propertiesNotFoundList.hasMoreElements() ) {
                status = "HTTP/1.1 404 " + WebdavStatus.getStatusText ( 404 );
                generatedXML.writeElement ( "D", "propstat", 0 );
                generatedXML.writeElement ( "D", "prop", 0 );
                while ( propertiesNotFoundList.hasMoreElements() ) {
                    generatedXML.writeElement ( "D", propertiesNotFoundList.nextElement(), 2 );
                }
                generatedXML.writeElement ( "D", "prop", 1 );
                generatedXML.writeElement ( "D", "status", 0 );
                generatedXML.writeText ( status );
                generatedXML.writeElement ( "D", "status", 1 );
                generatedXML.writeElement ( "D", "propstat", 1 );
                break;
            }
            break;
        }
        }
        generatedXML.writeElement ( "D", "response", 1 );
    }
    private boolean generateLockDiscovery ( final String path, final XMLWriter generatedXML ) {
        final LockInfo resourceLock = this.resourceLocks.get ( path );
        final Enumeration<LockInfo> collectionLocksList = this.collectionLocks.elements();
        boolean wroteStart = false;
        if ( resourceLock != null ) {
            wroteStart = true;
            generatedXML.writeElement ( "D", "lockdiscovery", 0 );
            resourceLock.toXML ( generatedXML );
        }
        while ( collectionLocksList.hasMoreElements() ) {
            final LockInfo currentLock = collectionLocksList.nextElement();
            if ( path.startsWith ( currentLock.path ) ) {
                if ( !wroteStart ) {
                    wroteStart = true;
                    generatedXML.writeElement ( "D", "lockdiscovery", 0 );
                }
                currentLock.toXML ( generatedXML );
            }
        }
        if ( wroteStart ) {
            generatedXML.writeElement ( "D", "lockdiscovery", 1 );
            return true;
        }
        return false;
    }
    private String getISOCreationDate ( final long creationDate ) {
        return WebdavServlet.creationDateFormat.format ( new Date ( creationDate ) );
    }
    private StringBuilder determineMethodsAllowed ( final HttpServletRequest req ) {
        final StringBuilder methodsAllowed = new StringBuilder();
        final WebResource resource = this.resources.getResource ( this.getRelativePath ( req ) );
        if ( !resource.exists() ) {
            methodsAllowed.append ( "OPTIONS, MKCOL, PUT, LOCK" );
            return methodsAllowed;
        }
        methodsAllowed.append ( "OPTIONS, GET, HEAD, POST, DELETE, TRACE" );
        methodsAllowed.append ( ", PROPPATCH, COPY, MOVE, LOCK, UNLOCK" );
        if ( this.listings ) {
            methodsAllowed.append ( ", PROPFIND" );
        }
        if ( resource.isFile() ) {
            methodsAllowed.append ( ", PUT" );
        }
        return methodsAllowed;
    }
    static {
        creationDateFormat = new ConcurrentDateFormat ( "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US, TimeZone.getTimeZone ( "GMT" ) );
    }
    private class LockInfo {
        String path;
        String type;
        String scope;
        int depth;
        String owner;
        Vector<String> tokens;
        long expiresAt;
        Date creationDate;
        private LockInfo() {
            this.path = "/";
            this.type = "write";
            this.scope = "exclusive";
            this.depth = 0;
            this.owner = "";
            this.tokens = new Vector<String>();
            this.expiresAt = 0L;
            this.creationDate = new Date();
        }
        @Override
        public String toString() {
            final StringBuilder result = new StringBuilder ( "Type:" );
            result.append ( this.type );
            result.append ( "\nScope:" );
            result.append ( this.scope );
            result.append ( "\nDepth:" );
            result.append ( this.depth );
            result.append ( "\nOwner:" );
            result.append ( this.owner );
            result.append ( "\nExpiration:" );
            result.append ( FastHttpDateFormat.formatDate ( this.expiresAt, null ) );
            final Enumeration<String> tokensList = this.tokens.elements();
            while ( tokensList.hasMoreElements() ) {
                result.append ( "\nToken:" );
                result.append ( tokensList.nextElement() );
            }
            result.append ( "\n" );
            return result.toString();
        }
        public boolean hasExpired() {
            return System.currentTimeMillis() > this.expiresAt;
        }
        public boolean isExclusive() {
            return this.scope.equals ( "exclusive" );
        }
        public void toXML ( final XMLWriter generatedXML ) {
            generatedXML.writeElement ( "D", "activelock", 0 );
            generatedXML.writeElement ( "D", "locktype", 0 );
            generatedXML.writeElement ( "D", this.type, 2 );
            generatedXML.writeElement ( "D", "locktype", 1 );
            generatedXML.writeElement ( "D", "lockscope", 0 );
            generatedXML.writeElement ( "D", this.scope, 2 );
            generatedXML.writeElement ( "D", "lockscope", 1 );
            generatedXML.writeElement ( "D", "depth", 0 );
            if ( this.depth == WebdavServlet.this.maxDepth ) {
                generatedXML.writeText ( "Infinity" );
            } else {
                generatedXML.writeText ( "0" );
            }
            generatedXML.writeElement ( "D", "depth", 1 );
            generatedXML.writeElement ( "D", "owner", 0 );
            generatedXML.writeText ( this.owner );
            generatedXML.writeElement ( "D", "owner", 1 );
            generatedXML.writeElement ( "D", "timeout", 0 );
            final long timeout = ( this.expiresAt - System.currentTimeMillis() ) / 1000L;
            generatedXML.writeText ( "Second-" + timeout );
            generatedXML.writeElement ( "D", "timeout", 1 );
            generatedXML.writeElement ( "D", "locktoken", 0 );
            final Enumeration<String> tokensList = this.tokens.elements();
            while ( tokensList.hasMoreElements() ) {
                generatedXML.writeElement ( "D", "href", 0 );
                generatedXML.writeText ( "opaquelocktoken:" + tokensList.nextElement() );
                generatedXML.writeElement ( "D", "href", 1 );
            }
            generatedXML.writeElement ( "D", "locktoken", 1 );
            generatedXML.writeElement ( "D", "activelock", 1 );
        }
    }
    private static class WebdavResolver implements EntityResolver {
        private ServletContext context;
        public WebdavResolver ( final ServletContext theContext ) {
            this.context = theContext;
        }
        @Override
        public InputSource resolveEntity ( final String publicId, final String systemId ) {
            this.context.log ( DefaultServlet.sm.getString ( "webdavservlet.enternalEntityIgnored", publicId, systemId ) );
            return new InputSource ( new StringReader ( "Ignored external entity" ) );
        }
    }
}
