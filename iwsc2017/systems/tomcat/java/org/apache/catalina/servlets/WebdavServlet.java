package org.apache.catalina.servlets;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Stack;
import java.util.TimeZone;
import java.util.Vector;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.catalina.WebResource;
import org.apache.catalina.util.ConcurrentDateFormat;
import org.apache.catalina.util.DOMWriter;
import org.apache.catalina.util.XMLWriter;
import org.apache.tomcat.util.buf.UDecoder;
import org.apache.tomcat.util.http.FastHttpDateFormat;
import org.apache.tomcat.util.http.RequestUtil;
import org.apache.tomcat.util.security.ConcurrentMessageDigest;
import org.apache.tomcat.util.security.MD5Encoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
public class WebdavServlet
    extends DefaultServlet {
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
    protected static final ConcurrentDateFormat creationDateFormat =
        new ConcurrentDateFormat ( "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US,
                                   TimeZone.getTimeZone ( "GMT" ) );
    private final Hashtable<String, LockInfo> resourceLocks = new Hashtable<>();
    private final Hashtable<String, Vector<String>> lockNullResources =
        new Hashtable<>();
    private final Vector<LockInfo> collectionLocks = new Vector<>();
    private String secret = "catalina";
    private int maxDepth = 3;
    private boolean allowSpecialPaths = false;
    @Override
    public void init()
    throws ServletException {
        super.init();
        if ( getServletConfig().getInitParameter ( "secret" ) != null ) {
            secret = getServletConfig().getInitParameter ( "secret" );
        }
        if ( getServletConfig().getInitParameter ( "maxDepth" ) != null )
            maxDepth = Integer.parseInt (
                           getServletConfig().getInitParameter ( "maxDepth" ) );
        if ( getServletConfig().getInitParameter ( "allowSpecialPaths" ) != null )
            allowSpecialPaths = Boolean.parseBoolean (
                                    getServletConfig().getInitParameter ( "allowSpecialPaths" ) );
    }
    protected DocumentBuilder getDocumentBuilder()
    throws ServletException {
        DocumentBuilder documentBuilder = null;
        DocumentBuilderFactory documentBuilderFactory = null;
        try {
            documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware ( true );
            documentBuilderFactory.setExpandEntityReferences ( false );
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setEntityResolver (
                new WebdavResolver ( this.getServletContext() ) );
        } catch ( ParserConfigurationException e ) {
            throw new ServletException
            ( sm.getString ( "webdavservlet.jaxpfailed" ) );
        }
        return documentBuilder;
    }
    @Override
    protected void service ( HttpServletRequest req, HttpServletResponse resp )
    throws ServletException, IOException {
        final String path = getRelativePath ( req );
        if ( isSpecialPath ( path ) ) {
            resp.sendError ( WebdavStatus.SC_NOT_FOUND );
            return;
        }
        final String method = req.getMethod();
        if ( debug > 0 ) {
            log ( "[" + method + "] " + path );
        }
        if ( method.equals ( METHOD_PROPFIND ) ) {
            doPropfind ( req, resp );
        } else if ( method.equals ( METHOD_PROPPATCH ) ) {
            doProppatch ( req, resp );
        } else if ( method.equals ( METHOD_MKCOL ) ) {
            doMkcol ( req, resp );
        } else if ( method.equals ( METHOD_COPY ) ) {
            doCopy ( req, resp );
        } else if ( method.equals ( METHOD_MOVE ) ) {
            doMove ( req, resp );
        } else if ( method.equals ( METHOD_LOCK ) ) {
            doLock ( req, resp );
        } else if ( method.equals ( METHOD_UNLOCK ) ) {
            doUnlock ( req, resp );
        } else {
            super.service ( req, resp );
        }
    }
    private final boolean isSpecialPath ( final String path ) {
        return !allowSpecialPaths && (
                   path.toUpperCase ( Locale.ENGLISH ).startsWith ( "/WEB-INF" ) ||
                   path.toUpperCase ( Locale.ENGLISH ).startsWith ( "/META-INF" ) );
    }
    @Override
    protected boolean checkIfHeaders ( HttpServletRequest request,
                                       HttpServletResponse response,
                                       WebResource resource )
    throws IOException {
        if ( !super.checkIfHeaders ( request, response, resource ) ) {
            return false;
        }
        return true;
    }
    @Override
    protected String getRelativePath ( HttpServletRequest request ) {
        return getRelativePath ( request, false );
    }
    @Override
    protected String getRelativePath ( HttpServletRequest request, boolean allowEmptyPath ) {
        String pathInfo;
        if ( request.getAttribute ( RequestDispatcher.INCLUDE_REQUEST_URI ) != null ) {
            pathInfo = ( String ) request.getAttribute ( RequestDispatcher.INCLUDE_PATH_INFO );
        } else {
            pathInfo = request.getPathInfo();
        }
        StringBuilder result = new StringBuilder();
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
        if ( request.getServletPath() !=  null ) {
            contextPath = contextPath + request.getServletPath();
        }
        return contextPath;
    }
    @Override
    protected void doOptions ( HttpServletRequest req, HttpServletResponse resp )
    throws ServletException, IOException {
        resp.addHeader ( "DAV", "1,2" );
        StringBuilder methodsAllowed = determineMethodsAllowed ( req );
        resp.addHeader ( "Allow", methodsAllowed.toString() );
        resp.addHeader ( "MS-Author-Via", "DAV" );
    }
    protected void doPropfind ( HttpServletRequest req, HttpServletResponse resp )
    throws ServletException, IOException {
        if ( !listings ) {
            StringBuilder methodsAllowed = determineMethodsAllowed ( req );
            resp.addHeader ( "Allow", methodsAllowed.toString() );
            resp.sendError ( WebdavStatus.SC_METHOD_NOT_ALLOWED );
            return;
        }
        String path = getRelativePath ( req );
        if ( path.length() > 1 && path.endsWith ( "/" ) ) {
            path = path.substring ( 0, path.length() - 1 );
        }
        Vector<String> properties = null;
        int depth = maxDepth;
        int type = FIND_ALL_PROP;
        String depthStr = req.getHeader ( "Depth" );
        if ( depthStr == null ) {
            depth = maxDepth;
        } else {
            if ( depthStr.equals ( "0" ) ) {
                depth = 0;
            } else if ( depthStr.equals ( "1" ) ) {
                depth = 1;
            } else if ( depthStr.equals ( "infinity" ) ) {
                depth = maxDepth;
            }
        }
        Node propNode = null;
        if ( req.getContentLengthLong() > 0 ) {
            DocumentBuilder documentBuilder = getDocumentBuilder();
            try {
                Document document = documentBuilder.parse
                                    ( new InputSource ( req.getInputStream() ) );
                Element rootElement = document.getDocumentElement();
                NodeList childList = rootElement.getChildNodes();
                for ( int i = 0; i < childList.getLength(); i++ ) {
                    Node currentNode = childList.item ( i );
                    switch ( currentNode.getNodeType() ) {
                    case Node.TEXT_NODE:
                        break;
                    case Node.ELEMENT_NODE:
                        if ( currentNode.getNodeName().endsWith ( "prop" ) ) {
                            type = FIND_BY_PROPERTY;
                            propNode = currentNode;
                        }
                        if ( currentNode.getNodeName().endsWith ( "propname" ) ) {
                            type = FIND_PROPERTY_NAMES;
                        }
                        if ( currentNode.getNodeName().endsWith ( "allprop" ) ) {
                            type = FIND_ALL_PROP;
                        }
                        break;
                    }
                }
            } catch ( SAXException e ) {
                resp.sendError ( WebdavStatus.SC_BAD_REQUEST );
                return;
            } catch ( IOException e ) {
                resp.sendError ( WebdavStatus.SC_BAD_REQUEST );
                return;
            }
        }
        if ( type == FIND_BY_PROPERTY ) {
            properties = new Vector<>();
            @SuppressWarnings ( "null" )
            NodeList childList = propNode.getChildNodes();
            for ( int i = 0; i < childList.getLength(); i++ ) {
                Node currentNode = childList.item ( i );
                switch ( currentNode.getNodeType() ) {
                case Node.TEXT_NODE:
                    break;
                case Node.ELEMENT_NODE:
                    String nodeName = currentNode.getNodeName();
                    String propertyName = null;
                    if ( nodeName.indexOf ( ':' ) != -1 ) {
                        propertyName = nodeName.substring
                                       ( nodeName.indexOf ( ':' ) + 1 );
                    } else {
                        propertyName = nodeName;
                    }
                    properties.addElement ( propertyName );
                    break;
                }
            }
        }
        WebResource resource = resources.getResource ( path );
        if ( !resource.exists() ) {
            int slash = path.lastIndexOf ( '/' );
            if ( slash != -1 ) {
                String parentPath = path.substring ( 0, slash );
                Vector<String> currentLockNullResources =
                    lockNullResources.get ( parentPath );
                if ( currentLockNullResources != null ) {
                    Enumeration<String> lockNullResourcesList =
                        currentLockNullResources.elements();
                    while ( lockNullResourcesList.hasMoreElements() ) {
                        String lockNullPath =
                            lockNullResourcesList.nextElement();
                        if ( lockNullPath.equals ( path ) ) {
                            resp.setStatus ( WebdavStatus.SC_MULTI_STATUS );
                            resp.setContentType ( "text/xml; charset=UTF-8" );
                            XMLWriter generatedXML =
                                new XMLWriter ( resp.getWriter() );
                            generatedXML.writeXMLHeader();
                            generatedXML.writeElement ( "D", DEFAULT_NAMESPACE,
                                                        "multistatus", XMLWriter.OPENING );
                            parseLockNullProperties
                            ( req, generatedXML, lockNullPath, type,
                              properties );
                            generatedXML.writeElement ( "D", "multistatus",
                                                        XMLWriter.CLOSING );
                            generatedXML.sendData();
                            return;
                        }
                    }
                }
            }
        }
        if ( !resource.exists() ) {
            resp.sendError ( HttpServletResponse.SC_NOT_FOUND, path );
            return;
        }
        resp.setStatus ( WebdavStatus.SC_MULTI_STATUS );
        resp.setContentType ( "text/xml; charset=UTF-8" );
        XMLWriter generatedXML = new XMLWriter ( resp.getWriter() );
        generatedXML.writeXMLHeader();
        generatedXML.writeElement ( "D", DEFAULT_NAMESPACE, "multistatus",
                                    XMLWriter.OPENING );
        if ( depth == 0 ) {
            parseProperties ( req, generatedXML, path, type,
                              properties );
        } else {
            Stack<String> stack = new Stack<>();
            stack.push ( path );
            Stack<String> stackBelow = new Stack<>();
            while ( ( !stack.isEmpty() ) && ( depth >= 0 ) ) {
                String currentPath = stack.pop();
                parseProperties ( req, generatedXML, currentPath,
                                  type, properties );
                resource = resources.getResource ( currentPath );
                if ( resource.isDirectory() && ( depth > 0 ) ) {
                    String[] entries = resources.list ( currentPath );
                    for ( String entry : entries ) {
                        String newPath = currentPath;
                        if ( ! ( newPath.endsWith ( "/" ) ) ) {
                            newPath += "/";
                        }
                        newPath += entry;
                        stackBelow.push ( newPath );
                    }
                    String lockPath = currentPath;
                    if ( lockPath.endsWith ( "/" ) )
                        lockPath =
                            lockPath.substring ( 0, lockPath.length() - 1 );
                    Vector<String> currentLockNullResources =
                        lockNullResources.get ( lockPath );
                    if ( currentLockNullResources != null ) {
                        Enumeration<String> lockNullResourcesList =
                            currentLockNullResources.elements();
                        while ( lockNullResourcesList.hasMoreElements() ) {
                            String lockNullPath =
                                lockNullResourcesList.nextElement();
                            parseLockNullProperties
                            ( req, generatedXML, lockNullPath, type,
                              properties );
                        }
                    }
                }
                if ( stack.isEmpty() ) {
                    depth--;
                    stack = stackBelow;
                    stackBelow = new Stack<>();
                }
                generatedXML.sendData();
            }
        }
        generatedXML.writeElement ( "D", "multistatus", XMLWriter.CLOSING );
        generatedXML.sendData();
    }
    protected void doProppatch ( HttpServletRequest req, HttpServletResponse resp )
    throws IOException {
        if ( readOnly ) {
            resp.sendError ( WebdavStatus.SC_FORBIDDEN );
            return;
        }
        if ( isLocked ( req ) ) {
            resp.sendError ( WebdavStatus.SC_LOCKED );
            return;
        }
        resp.sendError ( HttpServletResponse.SC_NOT_IMPLEMENTED );
    }
    protected void doMkcol ( HttpServletRequest req, HttpServletResponse resp )
    throws ServletException, IOException {
        if ( readOnly ) {
            resp.sendError ( WebdavStatus.SC_FORBIDDEN );
            return;
        }
        if ( isLocked ( req ) ) {
            resp.sendError ( WebdavStatus.SC_LOCKED );
            return;
        }
        String path = getRelativePath ( req );
        WebResource resource = resources.getResource ( path );
        if ( resource.exists() ) {
            StringBuilder methodsAllowed = determineMethodsAllowed ( req );
            resp.addHeader ( "Allow", methodsAllowed.toString() );
            resp.sendError ( WebdavStatus.SC_METHOD_NOT_ALLOWED );
            return;
        }
        if ( req.getContentLengthLong() > 0 ) {
            DocumentBuilder documentBuilder = getDocumentBuilder();
            try {
                documentBuilder.parse ( new InputSource ( req.getInputStream() ) );
                resp.sendError ( WebdavStatus.SC_NOT_IMPLEMENTED );
                return;
            } catch ( SAXException saxe ) {
                resp.sendError ( WebdavStatus.SC_UNSUPPORTED_MEDIA_TYPE );
                return;
            }
        }
        if ( resources.mkdir ( path ) ) {
            resp.setStatus ( WebdavStatus.SC_CREATED );
            lockNullResources.remove ( path );
        } else {
            resp.sendError ( WebdavStatus.SC_CONFLICT,
                             WebdavStatus.getStatusText
                             ( WebdavStatus.SC_CONFLICT ) );
        }
    }
    @Override
    protected void doDelete ( HttpServletRequest req, HttpServletResponse resp )
    throws ServletException, IOException {
        if ( readOnly ) {
            resp.sendError ( WebdavStatus.SC_FORBIDDEN );
            return;
        }
        if ( isLocked ( req ) ) {
            resp.sendError ( WebdavStatus.SC_LOCKED );
            return;
        }
        deleteResource ( req, resp );
    }
    @Override
    protected void doPut ( HttpServletRequest req, HttpServletResponse resp )
    throws ServletException, IOException {
        if ( isLocked ( req ) ) {
            resp.sendError ( WebdavStatus.SC_LOCKED );
            return;
        }
        super.doPut ( req, resp );
        String path = getRelativePath ( req );
        lockNullResources.remove ( path );
    }
    protected void doCopy ( HttpServletRequest req, HttpServletResponse resp )
    throws IOException {
        if ( readOnly ) {
            resp.sendError ( WebdavStatus.SC_FORBIDDEN );
            return;
        }
        copyResource ( req, resp );
    }
    protected void doMove ( HttpServletRequest req, HttpServletResponse resp )
    throws IOException {
        if ( readOnly ) {
            resp.sendError ( WebdavStatus.SC_FORBIDDEN );
            return;
        }
        if ( isLocked ( req ) ) {
            resp.sendError ( WebdavStatus.SC_LOCKED );
            return;
        }
        String path = getRelativePath ( req );
        if ( copyResource ( req, resp ) ) {
            deleteResource ( path, req, resp, false );
        }
    }
    protected void doLock ( HttpServletRequest req, HttpServletResponse resp )
    throws ServletException, IOException {
        if ( readOnly ) {
            resp.sendError ( WebdavStatus.SC_FORBIDDEN );
            return;
        }
        if ( isLocked ( req ) ) {
            resp.sendError ( WebdavStatus.SC_LOCKED );
            return;
        }
        LockInfo lock = new LockInfo();
        String depthStr = req.getHeader ( "Depth" );
        if ( depthStr == null ) {
            lock.depth = maxDepth;
        } else {
            if ( depthStr.equals ( "0" ) ) {
                lock.depth = 0;
            } else {
                lock.depth = maxDepth;
            }
        }
        int lockDuration = DEFAULT_TIMEOUT;
        String lockDurationStr = req.getHeader ( "Timeout" );
        if ( lockDurationStr == null ) {
            lockDuration = DEFAULT_TIMEOUT;
        } else {
            int commaPos = lockDurationStr.indexOf ( ',' );
            if ( commaPos != -1 ) {
                lockDurationStr = lockDurationStr.substring ( 0, commaPos );
            }
            if ( lockDurationStr.startsWith ( "Second-" ) ) {
                lockDuration = Integer.parseInt ( lockDurationStr.substring ( 7 ) );
            } else {
                if ( lockDurationStr.equalsIgnoreCase ( "infinity" ) ) {
                    lockDuration = MAX_TIMEOUT;
                } else {
                    try {
                        lockDuration = Integer.parseInt ( lockDurationStr );
                    } catch ( NumberFormatException e ) {
                        lockDuration = MAX_TIMEOUT;
                    }
                }
            }
            if ( lockDuration == 0 ) {
                lockDuration = DEFAULT_TIMEOUT;
            }
            if ( lockDuration > MAX_TIMEOUT ) {
                lockDuration = MAX_TIMEOUT;
            }
        }
        lock.expiresAt = System.currentTimeMillis() + ( lockDuration * 1000 );
        int lockRequestType = LOCK_CREATION;
        Node lockInfoNode = null;
        DocumentBuilder documentBuilder = getDocumentBuilder();
        try {
            Document document = documentBuilder.parse ( new InputSource
                                ( req.getInputStream() ) );
            Element rootElement = document.getDocumentElement();
            lockInfoNode = rootElement;
        } catch ( IOException e ) {
            lockRequestType = LOCK_REFRESH;
        } catch ( SAXException e ) {
            lockRequestType = LOCK_REFRESH;
        }
        if ( lockInfoNode != null ) {
            NodeList childList = lockInfoNode.getChildNodes();
            StringWriter strWriter = null;
            DOMWriter domWriter = null;
            Node lockScopeNode = null;
            Node lockTypeNode = null;
            Node lockOwnerNode = null;
            for ( int i = 0; i < childList.getLength(); i++ ) {
                Node currentNode = childList.item ( i );
                switch ( currentNode.getNodeType() ) {
                case Node.TEXT_NODE:
                    break;
                case Node.ELEMENT_NODE:
                    String nodeName = currentNode.getNodeName();
                    if ( nodeName.endsWith ( "lockscope" ) ) {
                        lockScopeNode = currentNode;
                    }
                    if ( nodeName.endsWith ( "locktype" ) ) {
                        lockTypeNode = currentNode;
                    }
                    if ( nodeName.endsWith ( "owner" ) ) {
                        lockOwnerNode = currentNode;
                    }
                    break;
                }
            }
            if ( lockScopeNode != null ) {
                childList = lockScopeNode.getChildNodes();
                for ( int i = 0; i < childList.getLength(); i++ ) {
                    Node currentNode = childList.item ( i );
                    switch ( currentNode.getNodeType() ) {
                    case Node.TEXT_NODE:
                        break;
                    case Node.ELEMENT_NODE:
                        String tempScope = currentNode.getNodeName();
                        if ( tempScope.indexOf ( ':' ) != -1 ) {
                            lock.scope = tempScope.substring
                                         ( tempScope.indexOf ( ':' ) + 1 );
                        } else {
                            lock.scope = tempScope;
                        }
                        break;
                    }
                }
                if ( lock.scope == null ) {
                    resp.setStatus ( WebdavStatus.SC_BAD_REQUEST );
                }
            } else {
                resp.setStatus ( WebdavStatus.SC_BAD_REQUEST );
            }
            if ( lockTypeNode != null ) {
                childList = lockTypeNode.getChildNodes();
                for ( int i = 0; i < childList.getLength(); i++ ) {
                    Node currentNode = childList.item ( i );
                    switch ( currentNode.getNodeType() ) {
                    case Node.TEXT_NODE:
                        break;
                    case Node.ELEMENT_NODE:
                        String tempType = currentNode.getNodeName();
                        if ( tempType.indexOf ( ':' ) != -1 ) {
                            lock.type =
                                tempType.substring ( tempType.indexOf ( ':' ) + 1 );
                        } else {
                            lock.type = tempType;
                        }
                        break;
                    }
                }
                if ( lock.type == null ) {
                    resp.setStatus ( WebdavStatus.SC_BAD_REQUEST );
                }
            } else {
                resp.setStatus ( WebdavStatus.SC_BAD_REQUEST );
            }
            if ( lockOwnerNode != null ) {
                childList = lockOwnerNode.getChildNodes();
                for ( int i = 0; i < childList.getLength(); i++ ) {
                    Node currentNode = childList.item ( i );
                    switch ( currentNode.getNodeType() ) {
                    case Node.TEXT_NODE:
                        lock.owner += currentNode.getNodeValue();
                        break;
                    case Node.ELEMENT_NODE:
                        strWriter = new StringWriter();
                        domWriter = new DOMWriter ( strWriter, true );
                        domWriter.print ( currentNode );
                        lock.owner += strWriter.toString();
                        break;
                    }
                }
                if ( lock.owner == null ) {
                    resp.setStatus ( WebdavStatus.SC_BAD_REQUEST );
                }
            } else {
                lock.owner = "";
            }
        }
        String path = getRelativePath ( req );
        lock.path = path;
        WebResource resource = resources.getResource ( path );
        Enumeration<LockInfo> locksList = null;
        if ( lockRequestType == LOCK_CREATION ) {
            String lockTokenStr = req.getServletPath() + "-" + lock.type + "-"
                                  + lock.scope + "-" + req.getUserPrincipal() + "-"
                                  + lock.depth + "-" + lock.owner + "-" + lock.tokens + "-"
                                  + lock.expiresAt + "-" + System.currentTimeMillis() + "-"
                                  + secret;
            String lockToken = MD5Encoder.encode ( ConcurrentMessageDigest.digestMD5 (
                    lockTokenStr.getBytes ( StandardCharsets.ISO_8859_1 ) ) );
            if ( resource.isDirectory() && lock.depth == maxDepth ) {
                Vector<String> lockPaths = new Vector<>();
                locksList = collectionLocks.elements();
                while ( locksList.hasMoreElements() ) {
                    LockInfo currentLock = locksList.nextElement();
                    if ( currentLock.hasExpired() ) {
                        resourceLocks.remove ( currentLock.path );
                        continue;
                    }
                    if ( ( currentLock.path.startsWith ( lock.path ) ) &&
                            ( ( currentLock.isExclusive() ) ||
                              ( lock.isExclusive() ) ) ) {
                        lockPaths.addElement ( currentLock.path );
                    }
                }
                locksList = resourceLocks.elements();
                while ( locksList.hasMoreElements() ) {
                    LockInfo currentLock = locksList.nextElement();
                    if ( currentLock.hasExpired() ) {
                        resourceLocks.remove ( currentLock.path );
                        continue;
                    }
                    if ( ( currentLock.path.startsWith ( lock.path ) ) &&
                            ( ( currentLock.isExclusive() ) ||
                              ( lock.isExclusive() ) ) ) {
                        lockPaths.addElement ( currentLock.path );
                    }
                }
                if ( !lockPaths.isEmpty() ) {
                    Enumeration<String> lockPathsList = lockPaths.elements();
                    resp.setStatus ( WebdavStatus.SC_CONFLICT );
                    XMLWriter generatedXML = new XMLWriter();
                    generatedXML.writeXMLHeader();
                    generatedXML.writeElement ( "D", DEFAULT_NAMESPACE,
                                                "multistatus", XMLWriter.OPENING );
                    while ( lockPathsList.hasMoreElements() ) {
                        generatedXML.writeElement ( "D", "response",
                                                    XMLWriter.OPENING );
                        generatedXML.writeElement ( "D", "href",
                                                    XMLWriter.OPENING );
                        generatedXML.writeText ( lockPathsList.nextElement() );
                        generatedXML.writeElement ( "D", "href",
                                                    XMLWriter.CLOSING );
                        generatedXML.writeElement ( "D", "status",
                                                    XMLWriter.OPENING );
                        generatedXML
                        .writeText ( "HTTP/1.1 " + WebdavStatus.SC_LOCKED
                                     + " " + WebdavStatus
                                     .getStatusText ( WebdavStatus.SC_LOCKED ) );
                        generatedXML.writeElement ( "D", "status",
                                                    XMLWriter.CLOSING );
                        generatedXML.writeElement ( "D", "response",
                                                    XMLWriter.CLOSING );
                    }
                    generatedXML.writeElement ( "D", "multistatus",
                                                XMLWriter.CLOSING );
                    Writer writer = resp.getWriter();
                    writer.write ( generatedXML.toString() );
                    writer.close();
                    return;
                }
                boolean addLock = true;
                locksList = collectionLocks.elements();
                while ( locksList.hasMoreElements() ) {
                    LockInfo currentLock = locksList.nextElement();
                    if ( currentLock.path.equals ( lock.path ) ) {
                        if ( currentLock.isExclusive() ) {
                            resp.sendError ( WebdavStatus.SC_LOCKED );
                            return;
                        } else {
                            if ( lock.isExclusive() ) {
                                resp.sendError ( WebdavStatus.SC_LOCKED );
                                return;
                            }
                        }
                        currentLock.tokens.addElement ( lockToken );
                        lock = currentLock;
                        addLock = false;
                    }
                }
                if ( addLock ) {
                    lock.tokens.addElement ( lockToken );
                    collectionLocks.addElement ( lock );
                }
            } else {
                LockInfo presentLock = resourceLocks.get ( lock.path );
                if ( presentLock != null ) {
                    if ( ( presentLock.isExclusive() ) || ( lock.isExclusive() ) ) {
                        resp.sendError ( WebdavStatus.SC_PRECONDITION_FAILED );
                        return;
                    } else {
                        presentLock.tokens.addElement ( lockToken );
                        lock = presentLock;
                    }
                } else {
                    lock.tokens.addElement ( lockToken );
                    resourceLocks.put ( lock.path, lock );
                    if ( !resource.exists() ) {
                        int slash = lock.path.lastIndexOf ( '/' );
                        String parentPath = lock.path.substring ( 0, slash );
                        Vector<String> lockNulls =
                            lockNullResources.get ( parentPath );
                        if ( lockNulls == null ) {
                            lockNulls = new Vector<>();
                            lockNullResources.put ( parentPath, lockNulls );
                        }
                        lockNulls.addElement ( lock.path );
                    }
                    resp.addHeader ( "Lock-Token", "<opaquelocktoken:"
                                     + lockToken + ">" );
                }
            }
        }
        if ( lockRequestType == LOCK_REFRESH ) {
            String ifHeader = req.getHeader ( "If" );
            if ( ifHeader == null ) {
                ifHeader = "";
            }
            LockInfo toRenew = resourceLocks.get ( path );
            Enumeration<String> tokenList = null;
            if ( toRenew != null ) {
                tokenList = toRenew.tokens.elements();
                while ( tokenList.hasMoreElements() ) {
                    String token = tokenList.nextElement();
                    if ( ifHeader.indexOf ( token ) != -1 ) {
                        toRenew.expiresAt = lock.expiresAt;
                        lock = toRenew;
                    }
                }
            }
            Enumeration<LockInfo> collectionLocksList =
                collectionLocks.elements();
            while ( collectionLocksList.hasMoreElements() ) {
                toRenew = collectionLocksList.nextElement();
                if ( path.equals ( toRenew.path ) ) {
                    tokenList = toRenew.tokens.elements();
                    while ( tokenList.hasMoreElements() ) {
                        String token = tokenList.nextElement();
                        if ( ifHeader.indexOf ( token ) != -1 ) {
                            toRenew.expiresAt = lock.expiresAt;
                            lock = toRenew;
                        }
                    }
                }
            }
        }
        XMLWriter generatedXML = new XMLWriter();
        generatedXML.writeXMLHeader();
        generatedXML.writeElement ( "D", DEFAULT_NAMESPACE, "prop",
                                    XMLWriter.OPENING );
        generatedXML.writeElement ( "D", "lockdiscovery", XMLWriter.OPENING );
        lock.toXML ( generatedXML );
        generatedXML.writeElement ( "D", "lockdiscovery", XMLWriter.CLOSING );
        generatedXML.writeElement ( "D", "prop", XMLWriter.CLOSING );
        resp.setStatus ( WebdavStatus.SC_OK );
        resp.setContentType ( "text/xml; charset=UTF-8" );
        Writer writer = resp.getWriter();
        writer.write ( generatedXML.toString() );
        writer.close();
    }
    protected void doUnlock ( HttpServletRequest req, HttpServletResponse resp )
    throws IOException {
        if ( readOnly ) {
            resp.sendError ( WebdavStatus.SC_FORBIDDEN );
            return;
        }
        if ( isLocked ( req ) ) {
            resp.sendError ( WebdavStatus.SC_LOCKED );
            return;
        }
        String path = getRelativePath ( req );
        String lockTokenHeader = req.getHeader ( "Lock-Token" );
        if ( lockTokenHeader == null ) {
            lockTokenHeader = "";
        }
        LockInfo lock = resourceLocks.get ( path );
        Enumeration<String> tokenList = null;
        if ( lock != null ) {
            tokenList = lock.tokens.elements();
            while ( tokenList.hasMoreElements() ) {
                String token = tokenList.nextElement();
                if ( lockTokenHeader.indexOf ( token ) != -1 ) {
                    lock.tokens.removeElement ( token );
                }
            }
            if ( lock.tokens.isEmpty() ) {
                resourceLocks.remove ( path );
                lockNullResources.remove ( path );
            }
        }
        Enumeration<LockInfo> collectionLocksList = collectionLocks.elements();
        while ( collectionLocksList.hasMoreElements() ) {
            lock = collectionLocksList.nextElement();
            if ( path.equals ( lock.path ) ) {
                tokenList = lock.tokens.elements();
                while ( tokenList.hasMoreElements() ) {
                    String token = tokenList.nextElement();
                    if ( lockTokenHeader.indexOf ( token ) != -1 ) {
                        lock.tokens.removeElement ( token );
                        break;
                    }
                }
                if ( lock.tokens.isEmpty() ) {
                    collectionLocks.removeElement ( lock );
                    lockNullResources.remove ( path );
                }
            }
        }
        resp.setStatus ( WebdavStatus.SC_NO_CONTENT );
    }
    private boolean isLocked ( HttpServletRequest req ) {
        String path = getRelativePath ( req );
        String ifHeader = req.getHeader ( "If" );
        if ( ifHeader == null ) {
            ifHeader = "";
        }
        String lockTokenHeader = req.getHeader ( "Lock-Token" );
        if ( lockTokenHeader == null ) {
            lockTokenHeader = "";
        }
        return isLocked ( path, ifHeader + lockTokenHeader );
    }
    private boolean isLocked ( String path, String ifHeader ) {
        LockInfo lock = resourceLocks.get ( path );
        Enumeration<String> tokenList = null;
        if ( ( lock != null ) && ( lock.hasExpired() ) ) {
            resourceLocks.remove ( path );
        } else if ( lock != null ) {
            tokenList = lock.tokens.elements();
            boolean tokenMatch = false;
            while ( tokenList.hasMoreElements() ) {
                String token = tokenList.nextElement();
                if ( ifHeader.indexOf ( token ) != -1 ) {
                    tokenMatch = true;
                    break;
                }
            }
            if ( !tokenMatch ) {
                return true;
            }
        }
        Enumeration<LockInfo> collectionLocksList = collectionLocks.elements();
        while ( collectionLocksList.hasMoreElements() ) {
            lock = collectionLocksList.nextElement();
            if ( lock.hasExpired() ) {
                collectionLocks.removeElement ( lock );
            } else if ( path.startsWith ( lock.path ) ) {
                tokenList = lock.tokens.elements();
                boolean tokenMatch = false;
                while ( tokenList.hasMoreElements() ) {
                    String token = tokenList.nextElement();
                    if ( ifHeader.indexOf ( token ) != -1 ) {
                        tokenMatch = true;
                        break;
                    }
                }
                if ( !tokenMatch ) {
                    return true;
                }
            }
        }
        return false;
    }
    private boolean copyResource ( HttpServletRequest req,
                                   HttpServletResponse resp )
    throws IOException {
        String destinationPath = req.getHeader ( "Destination" );
        if ( destinationPath == null ) {
            resp.sendError ( WebdavStatus.SC_BAD_REQUEST );
            return false;
        }
        destinationPath = UDecoder.URLDecode ( destinationPath, "UTF8" );
        int protocolIndex = destinationPath.indexOf ( "://" );
        if ( protocolIndex >= 0 ) {
            int firstSeparator =
                destinationPath.indexOf ( '/', protocolIndex + 4 );
            if ( firstSeparator < 0 ) {
                destinationPath = "/";
            } else {
                destinationPath = destinationPath.substring ( firstSeparator );
            }
        } else {
            String hostName = req.getServerName();
            if ( ( hostName != null ) && ( destinationPath.startsWith ( hostName ) ) ) {
                destinationPath = destinationPath.substring ( hostName.length() );
            }
            int portIndex = destinationPath.indexOf ( ':' );
            if ( portIndex >= 0 ) {
                destinationPath = destinationPath.substring ( portIndex );
            }
            if ( destinationPath.startsWith ( ":" ) ) {
                int firstSeparator = destinationPath.indexOf ( '/' );
                if ( firstSeparator < 0 ) {
                    destinationPath = "/";
                } else {
                    destinationPath =
                        destinationPath.substring ( firstSeparator );
                }
            }
        }
        destinationPath = RequestUtil.normalize ( destinationPath );
        String contextPath = req.getContextPath();
        if ( ( contextPath != null ) &&
                ( destinationPath.startsWith ( contextPath ) ) ) {
            destinationPath = destinationPath.substring ( contextPath.length() );
        }
        String pathInfo = req.getPathInfo();
        if ( pathInfo != null ) {
            String servletPath = req.getServletPath();
            if ( ( servletPath != null ) &&
                    ( destinationPath.startsWith ( servletPath ) ) ) {
                destinationPath = destinationPath
                                  .substring ( servletPath.length() );
            }
        }
        if ( debug > 0 ) {
            log ( "Dest path :" + destinationPath );
        }
        if ( isSpecialPath ( destinationPath ) ) {
            resp.sendError ( WebdavStatus.SC_FORBIDDEN );
            return false;
        }
        String path = getRelativePath ( req );
        if ( destinationPath.equals ( path ) ) {
            resp.sendError ( WebdavStatus.SC_FORBIDDEN );
            return false;
        }
        boolean overwrite = true;
        String overwriteHeader = req.getHeader ( "Overwrite" );
        if ( overwriteHeader != null ) {
            if ( overwriteHeader.equalsIgnoreCase ( "T" ) ) {
                overwrite = true;
            } else {
                overwrite = false;
            }
        }
        WebResource destination = resources.getResource ( destinationPath );
        if ( overwrite ) {
            if ( destination.exists() ) {
                if ( !deleteResource ( destinationPath, req, resp, true ) ) {
                    return false;
                }
            } else {
                resp.setStatus ( WebdavStatus.SC_CREATED );
            }
        } else {
            if ( destination.exists() ) {
                resp.sendError ( WebdavStatus.SC_PRECONDITION_FAILED );
                return false;
            }
        }
        Hashtable<String, Integer> errorList = new Hashtable<>();
        boolean result = copyResource ( errorList, path, destinationPath );
        if ( ( !result ) || ( !errorList.isEmpty() ) ) {
            if ( errorList.size() == 1 ) {
                resp.sendError ( errorList.elements().nextElement().intValue() );
            } else {
                sendReport ( req, resp, errorList );
            }
            return false;
        }
        if ( destination.exists() ) {
            resp.setStatus ( WebdavStatus.SC_NO_CONTENT );
        } else {
            resp.setStatus ( WebdavStatus.SC_CREATED );
        }
        lockNullResources.remove ( destinationPath );
        return true;
    }
    private boolean copyResource ( Hashtable<String, Integer> errorList,
                                   String source, String dest ) {
        if ( debug > 1 ) {
            log ( "Copy: " + source + " To: " + dest );
        }
        WebResource sourceResource = resources.getResource ( source );
        if ( sourceResource.isDirectory() ) {
            if ( !resources.mkdir ( dest ) ) {
                WebResource destResource = resources.getResource ( dest );
                if ( !destResource.isDirectory() ) {
                    errorList.put ( dest, Integer.valueOf ( WebdavStatus.SC_CONFLICT ) );
                    return false;
                }
            }
            String[] entries = resources.list ( source );
            for ( String entry : entries ) {
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
                copyResource ( errorList, childSrc, childDest );
            }
        } else if ( sourceResource.isFile() ) {
            WebResource destResource = resources.getResource ( dest );
            if ( !destResource.exists() && !destResource.getWebappPath().endsWith ( "/" ) ) {
                int lastSlash = destResource.getWebappPath().lastIndexOf ( '/' );
                if ( lastSlash > 0 ) {
                    String parent = destResource.getWebappPath().substring ( 0, lastSlash );
                    WebResource parentResource = resources.getResource ( parent );
                    if ( !parentResource.isDirectory() ) {
                        errorList.put ( source, Integer.valueOf ( WebdavStatus.SC_CONFLICT ) );
                        return false;
                    }
                }
            }
            try ( InputStream is = sourceResource.getInputStream() ) {
                if ( !resources.write ( dest, is,
                                        false ) ) {
                    errorList.put ( source,
                                    Integer.valueOf ( WebdavStatus.SC_INTERNAL_SERVER_ERROR ) );
                    return false;
                }
            } catch ( IOException e ) {
                log ( sm.getString ( "webdavservlet.inputstreamclosefail", source ), e );
            }
        } else {
            errorList.put ( source,
                            Integer.valueOf ( WebdavStatus.SC_INTERNAL_SERVER_ERROR ) );
            return false;
        }
        return true;
    }
    private boolean deleteResource ( HttpServletRequest req,
                                     HttpServletResponse resp )
    throws IOException {
        String path = getRelativePath ( req );
        return deleteResource ( path, req, resp, true );
    }
    private boolean deleteResource ( String path, HttpServletRequest req,
                                     HttpServletResponse resp, boolean setStatus )
    throws IOException {
        String ifHeader = req.getHeader ( "If" );
        if ( ifHeader == null ) {
            ifHeader = "";
        }
        String lockTokenHeader = req.getHeader ( "Lock-Token" );
        if ( lockTokenHeader == null ) {
            lockTokenHeader = "";
        }
        if ( isLocked ( path, ifHeader + lockTokenHeader ) ) {
            resp.sendError ( WebdavStatus.SC_LOCKED );
            return false;
        }
        WebResource resource = resources.getResource ( path );
        if ( !resource.exists() ) {
            resp.sendError ( WebdavStatus.SC_NOT_FOUND );
            return false;
        }
        if ( !resource.isDirectory() ) {
            if ( !resource.delete() ) {
                resp.sendError ( WebdavStatus.SC_INTERNAL_SERVER_ERROR );
                return false;
            }
        } else {
            Hashtable<String, Integer> errorList = new Hashtable<>();
            deleteCollection ( req, path, errorList );
            if ( !resource.delete() ) {
                errorList.put ( path, Integer.valueOf
                                ( WebdavStatus.SC_INTERNAL_SERVER_ERROR ) );
            }
            if ( !errorList.isEmpty() ) {
                sendReport ( req, resp, errorList );
                return false;
            }
        }
        if ( setStatus ) {
            resp.setStatus ( WebdavStatus.SC_NO_CONTENT );
        }
        return true;
    }
    private void deleteCollection ( HttpServletRequest req,
                                    String path,
                                    Hashtable<String, Integer> errorList ) {
        if ( debug > 1 ) {
            log ( "Delete:" + path );
        }
        if ( isSpecialPath ( path ) ) {
            errorList.put ( path, Integer.valueOf ( WebdavStatus.SC_FORBIDDEN ) );
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
        String[] entries = resources.list ( path );
        for ( String entry : entries ) {
            String childName = path;
            if ( !childName.equals ( "/" ) ) {
                childName += "/";
            }
            childName += entry;
            if ( isLocked ( childName, ifHeader + lockTokenHeader ) ) {
                errorList.put ( childName, Integer.valueOf ( WebdavStatus.SC_LOCKED ) );
            } else {
                WebResource childResource = resources.getResource ( childName );
                if ( childResource.isDirectory() ) {
                    deleteCollection ( req, childName, errorList );
                }
                if ( !childResource.delete() ) {
                    if ( !childResource.isDirectory() ) {
                        errorList.put ( childName, Integer.valueOf (
                                            WebdavStatus.SC_INTERNAL_SERVER_ERROR ) );
                    }
                }
            }
        }
    }
    private void sendReport ( HttpServletRequest req, HttpServletResponse resp,
                              Hashtable<String, Integer> errorList )
    throws IOException {
        resp.setStatus ( WebdavStatus.SC_MULTI_STATUS );
        String absoluteUri = req.getRequestURI();
        String relativePath = getRelativePath ( req );
        XMLWriter generatedXML = new XMLWriter();
        generatedXML.writeXMLHeader();
        generatedXML.writeElement ( "D", DEFAULT_NAMESPACE, "multistatus",
                                    XMLWriter.OPENING );
        Enumeration<String> pathList = errorList.keys();
        while ( pathList.hasMoreElements() ) {
            String errorPath = pathList.nextElement();
            int errorCode = errorList.get ( errorPath ).intValue();
            generatedXML.writeElement ( "D", "response", XMLWriter.OPENING );
            generatedXML.writeElement ( "D", "href", XMLWriter.OPENING );
            String toAppend = errorPath.substring ( relativePath.length() );
            if ( !toAppend.startsWith ( "/" ) ) {
                toAppend = "/" + toAppend;
            }
            generatedXML.writeText ( absoluteUri + toAppend );
            generatedXML.writeElement ( "D", "href", XMLWriter.CLOSING );
            generatedXML.writeElement ( "D", "status", XMLWriter.OPENING );
            generatedXML.writeText ( "HTTP/1.1 " + errorCode + " "
                                     + WebdavStatus.getStatusText ( errorCode ) );
            generatedXML.writeElement ( "D", "status", XMLWriter.CLOSING );
            generatedXML.writeElement ( "D", "response", XMLWriter.CLOSING );
        }
        generatedXML.writeElement ( "D", "multistatus", XMLWriter.CLOSING );
        Writer writer = resp.getWriter();
        writer.write ( generatedXML.toString() );
        writer.close();
    }
    private void parseProperties ( HttpServletRequest req,
                                   XMLWriter generatedXML,
                                   String path, int type,
                                   Vector<String> propertiesVector ) {
        if ( isSpecialPath ( path ) ) {
            return;
        }
        WebResource resource = resources.getResource ( path );
        if ( !resource.exists() ) {
            return;
        }
        String href = req.getContextPath() + req.getServletPath();
        if ( ( href.endsWith ( "/" ) ) && ( path.startsWith ( "/" ) ) ) {
            href += path.substring ( 1 );
        } else {
            href += path;
        }
        if ( resource.isDirectory() && ( !href.endsWith ( "/" ) ) ) {
            href += "/";
        }
        String rewrittenUrl = rewriteUrl ( href );
        generatePropFindResponse ( generatedXML, rewrittenUrl, path, type, propertiesVector,
                                   resource.isFile(), false, resource.getCreation(), resource.getLastModified(),
                                   resource.getContentLength(), getServletContext().getMimeType ( resource.getName() ),
                                   resource.getETag() );
    }
    private void parseLockNullProperties ( HttpServletRequest req,
                                           XMLWriter generatedXML,
                                           String path, int type,
                                           Vector<String> propertiesVector ) {
        if ( isSpecialPath ( path ) ) {
            return;
        }
        LockInfo lock = resourceLocks.get ( path );
        if ( lock == null ) {
            return;
        }
        String absoluteUri = req.getRequestURI();
        String relativePath = getRelativePath ( req );
        String toAppend = path.substring ( relativePath.length() );
        if ( !toAppend.startsWith ( "/" ) ) {
            toAppend = "/" + toAppend;
        }
        String rewrittenUrl = rewriteUrl ( RequestUtil.normalize (
                                               absoluteUri + toAppend ) );
        generatePropFindResponse ( generatedXML, rewrittenUrl, path, type, propertiesVector,
                                   true, true, lock.creationDate.getTime(), lock.creationDate.getTime(),
                                   0, "", "" );
    }
    private void generatePropFindResponse ( XMLWriter generatedXML, String rewrittenUrl,
                                            String path, int propFindType, Vector<String> propertiesVector, boolean isFile,
                                            boolean isLockNull, long created, long lastModified, long contentLength,
                                            String contentType, String eTag ) {
        generatedXML.writeElement ( "D", "response", XMLWriter.OPENING );
        String status = "HTTP/1.1 " + WebdavStatus.SC_OK + " " +
                        WebdavStatus.getStatusText ( WebdavStatus.SC_OK );
        generatedXML.writeElement ( "D", "href", XMLWriter.OPENING );
        generatedXML.writeText ( rewrittenUrl );
        generatedXML.writeElement ( "D", "href", XMLWriter.CLOSING );
        String resourceName = path;
        int lastSlash = path.lastIndexOf ( '/' );
        if ( lastSlash != -1 ) {
            resourceName = resourceName.substring ( lastSlash + 1 );
        }
        switch ( propFindType ) {
        case FIND_ALL_PROP :
            generatedXML.writeElement ( "D", "propstat", XMLWriter.OPENING );
            generatedXML.writeElement ( "D", "prop", XMLWriter.OPENING );
            generatedXML.writeProperty ( "D", "creationdate", getISOCreationDate ( created ) );
            generatedXML.writeElement ( "D", "displayname", XMLWriter.OPENING );
            generatedXML.writeData ( resourceName );
            generatedXML.writeElement ( "D", "displayname", XMLWriter.CLOSING );
            if ( isFile ) {
                generatedXML.writeProperty ( "D", "getlastmodified",
                                             FastHttpDateFormat.formatDate ( lastModified, null ) );
                generatedXML.writeProperty ( "D", "getcontentlength", Long.toString ( contentLength ) );
                if ( contentType != null ) {
                    generatedXML.writeProperty ( "D", "getcontenttype", contentType );
                }
                generatedXML.writeProperty ( "D", "getetag", eTag );
                if ( isLockNull ) {
                    generatedXML.writeElement ( "D", "resourcetype", XMLWriter.OPENING );
                    generatedXML.writeElement ( "D", "lock-null", XMLWriter.NO_CONTENT );
                    generatedXML.writeElement ( "D", "resourcetype", XMLWriter.CLOSING );
                } else {
                    generatedXML.writeElement ( "D", "resourcetype", XMLWriter.NO_CONTENT );
                }
            } else {
                generatedXML.writeElement ( "D", "resourcetype", XMLWriter.OPENING );
                generatedXML.writeElement ( "D", "collection", XMLWriter.NO_CONTENT );
                generatedXML.writeElement ( "D", "resourcetype", XMLWriter.CLOSING );
            }
            generatedXML.writeProperty ( "D", "source", "" );
            String supportedLocks = "<D:lockentry>"
                                    + "<D:lockscope><D:exclusive/></D:lockscope>"
                                    + "<D:locktype><D:write/></D:locktype>"
                                    + "</D:lockentry>" + "<D:lockentry>"
                                    + "<D:lockscope><D:shared/></D:lockscope>"
                                    + "<D:locktype><D:write/></D:locktype>"
                                    + "</D:lockentry>";
            generatedXML.writeElement ( "D", "supportedlock", XMLWriter.OPENING );
            generatedXML.writeText ( supportedLocks );
            generatedXML.writeElement ( "D", "supportedlock", XMLWriter.CLOSING );
            generateLockDiscovery ( path, generatedXML );
            generatedXML.writeElement ( "D", "prop", XMLWriter.CLOSING );
            generatedXML.writeElement ( "D", "status", XMLWriter.OPENING );
            generatedXML.writeText ( status );
            generatedXML.writeElement ( "D", "status", XMLWriter.CLOSING );
            generatedXML.writeElement ( "D", "propstat", XMLWriter.CLOSING );
            break;
        case FIND_PROPERTY_NAMES :
            generatedXML.writeElement ( "D", "propstat", XMLWriter.OPENING );
            generatedXML.writeElement ( "D", "prop", XMLWriter.OPENING );
            generatedXML.writeElement ( "D", "creationdate", XMLWriter.NO_CONTENT );
            generatedXML.writeElement ( "D", "displayname", XMLWriter.NO_CONTENT );
            if ( isFile ) {
                generatedXML.writeElement ( "D", "getcontentlanguage", XMLWriter.NO_CONTENT );
                generatedXML.writeElement ( "D", "getcontentlength", XMLWriter.NO_CONTENT );
                generatedXML.writeElement ( "D", "getcontenttype", XMLWriter.NO_CONTENT );
                generatedXML.writeElement ( "D", "getetag", XMLWriter.NO_CONTENT );
                generatedXML.writeElement ( "D", "getlastmodified", XMLWriter.NO_CONTENT );
            }
            generatedXML.writeElement ( "D", "resourcetype", XMLWriter.NO_CONTENT );
            generatedXML.writeElement ( "D", "source", XMLWriter.NO_CONTENT );
            generatedXML.writeElement ( "D", "lockdiscovery", XMLWriter.NO_CONTENT );
            generatedXML.writeElement ( "D", "prop", XMLWriter.CLOSING );
            generatedXML.writeElement ( "D", "status", XMLWriter.OPENING );
            generatedXML.writeText ( status );
            generatedXML.writeElement ( "D", "status", XMLWriter.CLOSING );
            generatedXML.writeElement ( "D", "propstat", XMLWriter.CLOSING );
            break;
        case FIND_BY_PROPERTY :
            Vector<String> propertiesNotFound = new Vector<>();
            generatedXML.writeElement ( "D", "propstat", XMLWriter.OPENING );
            generatedXML.writeElement ( "D", "prop", XMLWriter.OPENING );
            Enumeration<String> properties = propertiesVector.elements();
            while ( properties.hasMoreElements() ) {
                String property = properties.nextElement();
                if ( property.equals ( "creationdate" ) ) {
                    generatedXML.writeProperty ( "D", "creationdate", getISOCreationDate ( created ) );
                } else if ( property.equals ( "displayname" ) ) {
                    generatedXML.writeElement ( "D", "displayname", XMLWriter.OPENING );
                    generatedXML.writeData ( resourceName );
                    generatedXML.writeElement ( "D", "displayname", XMLWriter.CLOSING );
                } else if ( property.equals ( "getcontentlanguage" ) ) {
                    if ( isFile ) {
                        generatedXML.writeElement ( "D", "getcontentlanguage",
                                                    XMLWriter.NO_CONTENT );
                    } else {
                        propertiesNotFound.addElement ( property );
                    }
                } else if ( property.equals ( "getcontentlength" ) ) {
                    if ( isFile ) {
                        generatedXML.writeProperty ( "D", "getcontentlength",
                                                     Long.toString ( contentLength ) );
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
                        generatedXML.writeProperty ( "D", "getlastmodified",
                                                     FastHttpDateFormat.formatDate ( lastModified, null ) );
                    } else {
                        propertiesNotFound.addElement ( property );
                    }
                } else if ( property.equals ( "resourcetype" ) ) {
                    if ( isFile ) {
                        if ( isLockNull ) {
                            generatedXML.writeElement ( "D", "resourcetype", XMLWriter.OPENING );
                            generatedXML.writeElement ( "D", "lock-null", XMLWriter.NO_CONTENT );
                            generatedXML.writeElement ( "D", "resourcetype", XMLWriter.CLOSING );
                        } else {
                            generatedXML.writeElement ( "D", "resourcetype", XMLWriter.NO_CONTENT );
                        }
                    } else {
                        generatedXML.writeElement ( "D", "resourcetype", XMLWriter.OPENING );
                        generatedXML.writeElement ( "D", "collection", XMLWriter.NO_CONTENT );
                        generatedXML.writeElement ( "D", "resourcetype", XMLWriter.CLOSING );
                    }
                } else if ( property.equals ( "source" ) ) {
                    generatedXML.writeProperty ( "D", "source", "" );
                } else if ( property.equals ( "supportedlock" ) ) {
                    supportedLocks = "<D:lockentry>"
                                     + "<D:lockscope><D:exclusive/></D:lockscope>"
                                     + "<D:locktype><D:write/></D:locktype>"
                                     + "</D:lockentry>" + "<D:lockentry>"
                                     + "<D:lockscope><D:shared/></D:lockscope>"
                                     + "<D:locktype><D:write/></D:locktype>"
                                     + "</D:lockentry>";
                    generatedXML.writeElement ( "D", "supportedlock", XMLWriter.OPENING );
                    generatedXML.writeText ( supportedLocks );
                    generatedXML.writeElement ( "D", "supportedlock", XMLWriter.CLOSING );
                } else if ( property.equals ( "lockdiscovery" ) ) {
                    if ( !generateLockDiscovery ( path, generatedXML ) ) {
                        propertiesNotFound.addElement ( property );
                    }
                } else {
                    propertiesNotFound.addElement ( property );
                }
            }
            generatedXML.writeElement ( "D", "prop", XMLWriter.CLOSING );
            generatedXML.writeElement ( "D", "status", XMLWriter.OPENING );
            generatedXML.writeText ( status );
            generatedXML.writeElement ( "D", "status", XMLWriter.CLOSING );
            generatedXML.writeElement ( "D", "propstat", XMLWriter.CLOSING );
            Enumeration<String> propertiesNotFoundList = propertiesNotFound.elements();
            if ( propertiesNotFoundList.hasMoreElements() ) {
                status = "HTTP/1.1 " + WebdavStatus.SC_NOT_FOUND + " " +
                         WebdavStatus.getStatusText ( WebdavStatus.SC_NOT_FOUND );
                generatedXML.writeElement ( "D", "propstat", XMLWriter.OPENING );
                generatedXML.writeElement ( "D", "prop", XMLWriter.OPENING );
                while ( propertiesNotFoundList.hasMoreElements() ) {
                    generatedXML.writeElement ( "D", propertiesNotFoundList.nextElement(),
                                                XMLWriter.NO_CONTENT );
                }
                generatedXML.writeElement ( "D", "prop", XMLWriter.CLOSING );
                generatedXML.writeElement ( "D", "status", XMLWriter.OPENING );
                generatedXML.writeText ( status );
                generatedXML.writeElement ( "D", "status", XMLWriter.CLOSING );
                generatedXML.writeElement ( "D", "propstat", XMLWriter.CLOSING );
            }
            break;
        }
        generatedXML.writeElement ( "D", "response", XMLWriter.CLOSING );
    }
    private boolean generateLockDiscovery
    ( String path, XMLWriter generatedXML ) {
        LockInfo resourceLock = resourceLocks.get ( path );
        Enumeration<LockInfo> collectionLocksList = collectionLocks.elements();
        boolean wroteStart = false;
        if ( resourceLock != null ) {
            wroteStart = true;
            generatedXML.writeElement ( "D", "lockdiscovery", XMLWriter.OPENING );
            resourceLock.toXML ( generatedXML );
        }
        while ( collectionLocksList.hasMoreElements() ) {
            LockInfo currentLock = collectionLocksList.nextElement();
            if ( path.startsWith ( currentLock.path ) ) {
                if ( !wroteStart ) {
                    wroteStart = true;
                    generatedXML.writeElement ( "D", "lockdiscovery",
                                                XMLWriter.OPENING );
                }
                currentLock.toXML ( generatedXML );
            }
        }
        if ( wroteStart ) {
            generatedXML.writeElement ( "D", "lockdiscovery", XMLWriter.CLOSING );
        } else {
            return false;
        }
        return true;
    }
    private String getISOCreationDate ( long creationDate ) {
        return creationDateFormat.format ( new Date ( creationDate ) );
    }
    private StringBuilder determineMethodsAllowed ( HttpServletRequest req ) {
        StringBuilder methodsAllowed = new StringBuilder();
        WebResource resource = resources.getResource ( getRelativePath ( req ) );
        if ( !resource.exists() ) {
            methodsAllowed.append ( "OPTIONS, MKCOL, PUT, LOCK" );
            return methodsAllowed;
        }
        methodsAllowed.append ( "OPTIONS, GET, HEAD, POST, DELETE, TRACE" );
        methodsAllowed.append ( ", PROPPATCH, COPY, MOVE, LOCK, UNLOCK" );
        if ( listings ) {
            methodsAllowed.append ( ", PROPFIND" );
        }
        if ( resource.isFile() ) {
            methodsAllowed.append ( ", PUT" );
        }
        return methodsAllowed;
    }
    private class LockInfo {
        String path = "/";
        String type = "write";
        String scope = "exclusive";
        int depth = 0;
        String owner = "";
        Vector<String> tokens = new Vector<>();
        long expiresAt = 0;
        Date creationDate = new Date();
        @Override
        public String toString() {
            StringBuilder result =  new StringBuilder ( "Type:" );
            result.append ( type );
            result.append ( "\nScope:" );
            result.append ( scope );
            result.append ( "\nDepth:" );
            result.append ( depth );
            result.append ( "\nOwner:" );
            result.append ( owner );
            result.append ( "\nExpiration:" );
            result.append ( FastHttpDateFormat.formatDate ( expiresAt, null ) );
            Enumeration<String> tokensList = tokens.elements();
            while ( tokensList.hasMoreElements() ) {
                result.append ( "\nToken:" );
                result.append ( tokensList.nextElement() );
            }
            result.append ( "\n" );
            return result.toString();
        }
        public boolean hasExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
        public boolean isExclusive() {
            return scope.equals ( "exclusive" );
        }
        public void toXML ( XMLWriter generatedXML ) {
            generatedXML.writeElement ( "D", "activelock", XMLWriter.OPENING );
            generatedXML.writeElement ( "D", "locktype", XMLWriter.OPENING );
            generatedXML.writeElement ( "D", type, XMLWriter.NO_CONTENT );
            generatedXML.writeElement ( "D", "locktype", XMLWriter.CLOSING );
            generatedXML.writeElement ( "D", "lockscope", XMLWriter.OPENING );
            generatedXML.writeElement ( "D", scope, XMLWriter.NO_CONTENT );
            generatedXML.writeElement ( "D", "lockscope", XMLWriter.CLOSING );
            generatedXML.writeElement ( "D", "depth", XMLWriter.OPENING );
            if ( depth == maxDepth ) {
                generatedXML.writeText ( "Infinity" );
            } else {
                generatedXML.writeText ( "0" );
            }
            generatedXML.writeElement ( "D", "depth", XMLWriter.CLOSING );
            generatedXML.writeElement ( "D", "owner", XMLWriter.OPENING );
            generatedXML.writeText ( owner );
            generatedXML.writeElement ( "D", "owner", XMLWriter.CLOSING );
            generatedXML.writeElement ( "D", "timeout", XMLWriter.OPENING );
            long timeout = ( expiresAt - System.currentTimeMillis() ) / 1000;
            generatedXML.writeText ( "Second-" + timeout );
            generatedXML.writeElement ( "D", "timeout", XMLWriter.CLOSING );
            generatedXML.writeElement ( "D", "locktoken", XMLWriter.OPENING );
            Enumeration<String> tokensList = tokens.elements();
            while ( tokensList.hasMoreElements() ) {
                generatedXML.writeElement ( "D", "href", XMLWriter.OPENING );
                generatedXML.writeText ( "opaquelocktoken:"
                                         + tokensList.nextElement() );
                generatedXML.writeElement ( "D", "href", XMLWriter.CLOSING );
            }
            generatedXML.writeElement ( "D", "locktoken", XMLWriter.CLOSING );
            generatedXML.writeElement ( "D", "activelock", XMLWriter.CLOSING );
        }
    }
    private static class WebdavResolver implements EntityResolver {
        private ServletContext context;
        public WebdavResolver ( ServletContext theContext ) {
            context = theContext;
        }
        @Override
        public InputSource resolveEntity ( String publicId, String systemId ) {
            context.log ( sm.getString ( "webdavservlet.enternalEntityIgnored",
                                         publicId, systemId ) );
            return new InputSource (
                       new StringReader ( "Ignored external entity" ) );
        }
    }
}
class WebdavStatus {
    private static final Hashtable<Integer, String> mapStatusCodes =
        new Hashtable<>();
    public static final int SC_OK = HttpServletResponse.SC_OK;
    public static final int SC_CREATED = HttpServletResponse.SC_CREATED;
    public static final int SC_ACCEPTED = HttpServletResponse.SC_ACCEPTED;
    public static final int SC_NO_CONTENT = HttpServletResponse.SC_NO_CONTENT;
    public static final int SC_MOVED_PERMANENTLY =
        HttpServletResponse.SC_MOVED_PERMANENTLY;
    public static final int SC_MOVED_TEMPORARILY =
        HttpServletResponse.SC_MOVED_TEMPORARILY;
    public static final int SC_NOT_MODIFIED =
        HttpServletResponse.SC_NOT_MODIFIED;
    public static final int SC_BAD_REQUEST =
        HttpServletResponse.SC_BAD_REQUEST;
    public static final int SC_UNAUTHORIZED =
        HttpServletResponse.SC_UNAUTHORIZED;
    public static final int SC_FORBIDDEN = HttpServletResponse.SC_FORBIDDEN;
    public static final int SC_NOT_FOUND = HttpServletResponse.SC_NOT_FOUND;
    public static final int SC_INTERNAL_SERVER_ERROR =
        HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
    public static final int SC_NOT_IMPLEMENTED =
        HttpServletResponse.SC_NOT_IMPLEMENTED;
    public static final int SC_BAD_GATEWAY =
        HttpServletResponse.SC_BAD_GATEWAY;
    public static final int SC_SERVICE_UNAVAILABLE =
        HttpServletResponse.SC_SERVICE_UNAVAILABLE;
    public static final int SC_CONTINUE = 100;
    public static final int SC_METHOD_NOT_ALLOWED = 405;
    public static final int SC_CONFLICT = 409;
    public static final int SC_PRECONDITION_FAILED = 412;
    public static final int SC_REQUEST_TOO_LONG = 413;
    public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;
    public static final int SC_MULTI_STATUS = 207;
    public static final int SC_UNPROCESSABLE_ENTITY = 418;
    public static final int SC_INSUFFICIENT_SPACE_ON_RESOURCE = 419;
    public static final int SC_METHOD_FAILURE = 420;
    public static final int SC_LOCKED = 423;
    static {
        addStatusCodeMap ( SC_OK, "OK" );
        addStatusCodeMap ( SC_CREATED, "Created" );
        addStatusCodeMap ( SC_ACCEPTED, "Accepted" );
        addStatusCodeMap ( SC_NO_CONTENT, "No Content" );
        addStatusCodeMap ( SC_MOVED_PERMANENTLY, "Moved Permanently" );
        addStatusCodeMap ( SC_MOVED_TEMPORARILY, "Moved Temporarily" );
        addStatusCodeMap ( SC_NOT_MODIFIED, "Not Modified" );
        addStatusCodeMap ( SC_BAD_REQUEST, "Bad Request" );
        addStatusCodeMap ( SC_UNAUTHORIZED, "Unauthorized" );
        addStatusCodeMap ( SC_FORBIDDEN, "Forbidden" );
        addStatusCodeMap ( SC_NOT_FOUND, "Not Found" );
        addStatusCodeMap ( SC_INTERNAL_SERVER_ERROR, "Internal Server Error" );
        addStatusCodeMap ( SC_NOT_IMPLEMENTED, "Not Implemented" );
        addStatusCodeMap ( SC_BAD_GATEWAY, "Bad Gateway" );
        addStatusCodeMap ( SC_SERVICE_UNAVAILABLE, "Service Unavailable" );
        addStatusCodeMap ( SC_CONTINUE, "Continue" );
        addStatusCodeMap ( SC_METHOD_NOT_ALLOWED, "Method Not Allowed" );
        addStatusCodeMap ( SC_CONFLICT, "Conflict" );
        addStatusCodeMap ( SC_PRECONDITION_FAILED, "Precondition Failed" );
        addStatusCodeMap ( SC_REQUEST_TOO_LONG, "Request Too Long" );
        addStatusCodeMap ( SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type" );
        addStatusCodeMap ( SC_MULTI_STATUS, "Multi-Status" );
        addStatusCodeMap ( SC_UNPROCESSABLE_ENTITY, "Unprocessable Entity" );
        addStatusCodeMap ( SC_INSUFFICIENT_SPACE_ON_RESOURCE,
                           "Insufficient Space On Resource" );
        addStatusCodeMap ( SC_METHOD_FAILURE, "Method Failure" );
        addStatusCodeMap ( SC_LOCKED, "Locked" );
    }
    public static String getStatusText ( int nHttpStatusCode ) {
        Integer intKey = Integer.valueOf ( nHttpStatusCode );
        if ( !mapStatusCodes.containsKey ( intKey ) ) {
            return "";
        } else {
            return mapStatusCodes.get ( intKey );
        }
    }
    private static void addStatusCodeMap ( int nKey, String strVal ) {
        mapStatusCodes.put ( Integer.valueOf ( nKey ), strVal );
    }
}
