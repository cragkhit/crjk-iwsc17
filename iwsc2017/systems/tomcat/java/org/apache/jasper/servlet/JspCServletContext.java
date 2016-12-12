package org.apache.jasper.servlet;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import org.apache.jasper.Constants;
import org.apache.jasper.JasperException;
import org.apache.jasper.compiler.Localizer;
import org.apache.jasper.runtime.ExceptionUtils;
import org.apache.tomcat.JarScanType;
import org.apache.tomcat.util.descriptor.web.FragmentJarScannerCallback;
import org.apache.tomcat.util.descriptor.web.WebXml;
import org.apache.tomcat.util.descriptor.web.WebXmlParser;
import org.apache.tomcat.util.scan.StandardJarScanFilter;
import org.apache.tomcat.util.scan.StandardJarScanner;
public class JspCServletContext implements ServletContext {
    private final Map<String, Object> myAttributes;
    private final Map<String, String> myParameters = new ConcurrentHashMap<>();
    private final PrintWriter myLogWriter;
    private final URL myResourceBaseURL;
    private WebXml webXml;
    private JspConfigDescriptor jspConfigDescriptor;
    private final ClassLoader loader;
    public JspCServletContext ( PrintWriter aLogWriter, URL aResourceBaseURL,
                                ClassLoader classLoader, boolean validate, boolean blockExternal )
    throws JasperException {
        myAttributes = new HashMap<>();
        myParameters.put ( Constants.XML_BLOCK_EXTERNAL_INIT_PARAM,
                           String.valueOf ( blockExternal ) );
        myLogWriter = aLogWriter;
        myResourceBaseURL = aResourceBaseURL;
        this.loader = classLoader;
        this.webXml = buildMergedWebXml ( validate, blockExternal );
        jspConfigDescriptor = webXml.getJspConfigDescriptor();
    }
    private WebXml buildMergedWebXml ( boolean validate, boolean blockExternal )
    throws JasperException {
        WebXml webXml = new WebXml();
        WebXmlParser webXmlParser = new WebXmlParser ( validate, validate, blockExternal );
        webXmlParser.setClassLoader ( getClass().getClassLoader() );
        try {
            URL url = getResource (
                          org.apache.tomcat.util.descriptor.web.Constants.WEB_XML_LOCATION );
            if ( !webXmlParser.parseWebXml ( url, webXml, false ) ) {
                throw new JasperException ( Localizer.getMessage ( "jspc.error.invalidWebXml" ) );
            }
        } catch ( IOException e ) {
            throw new JasperException ( e );
        }
        if ( webXml.isMetadataComplete() ) {
            return webXml;
        }
        Set<String> absoluteOrdering = webXml.getAbsoluteOrdering();
        if ( absoluteOrdering != null && absoluteOrdering.isEmpty() ) {
            return webXml;
        }
        Map<String, WebXml> fragments = scanForFragments ( webXmlParser );
        Set<WebXml> orderedFragments = WebXml.orderWebFragments ( webXml, fragments, this );
        webXml.merge ( orderedFragments );
        return webXml;
    }
    private Map<String, WebXml> scanForFragments ( WebXmlParser webXmlParser ) throws JasperException {
        StandardJarScanner scanner = new StandardJarScanner();
        scanner.setScanClassPath ( false );
        scanner.setJarScanFilter ( new StandardJarScanFilter() );
        FragmentJarScannerCallback callback =
            new FragmentJarScannerCallback ( webXmlParser, false, true );
        scanner.scan ( JarScanType.PLUGGABILITY, this, callback );
        if ( !callback.isOk() ) {
            throw new JasperException ( Localizer.getMessage ( "jspc.error.invalidFragment" ) );
        }
        return callback.getFragments();
    }
    @Override
    public Object getAttribute ( String name ) {
        return myAttributes.get ( name );
    }
    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration ( myAttributes.keySet() );
    }
    @Override
    public ServletContext getContext ( String uripath ) {
        return null;
    }
    @Override
    public String getContextPath() {
        return null;
    }
    @Override
    public String getInitParameter ( String name ) {
        return myParameters.get ( name );
    }
    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration ( myParameters.keySet() );
    }
    @Override
    public int getMajorVersion() {
        return 3;
    }
    @Override
    public String getMimeType ( String file ) {
        return null;
    }
    @Override
    public int getMinorVersion() {
        return 1;
    }
    @Override
    public RequestDispatcher getNamedDispatcher ( String name ) {
        return null;
    }
    @Override
    public String getRealPath ( String path ) {
        if ( !myResourceBaseURL.getProtocol().equals ( "file" ) ) {
            return null;
        }
        if ( !path.startsWith ( "/" ) ) {
            return null;
        }
        try {
            File f = new File ( getResource ( path ).toURI() );
            return f.getAbsolutePath();
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            return null;
        }
    }
    @Override
    public RequestDispatcher getRequestDispatcher ( String path ) {
        return null;
    }
    @Override
    public URL getResource ( String path ) throws MalformedURLException {
        if ( !path.startsWith ( "/" ) )
            throw new MalformedURLException ( "Path '" + path +
                                              "' does not start with '/'" );
        URL url = new URL ( myResourceBaseURL, path.substring ( 1 ) );
        try ( InputStream is = url.openStream() ) {
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            url = null;
        }
        return url;
    }
    @Override
    public InputStream getResourceAsStream ( String path ) {
        try {
            return ( getResource ( path ).openStream() );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            return ( null );
        }
    }
    @Override
    public Set<String> getResourcePaths ( String path ) {
        Set<String> thePaths = new HashSet<>();
        if ( !path.endsWith ( "/" ) ) {
            path += "/";
        }
        String basePath = getRealPath ( path );
        if ( basePath == null ) {
            return ( thePaths );
        }
        File theBaseDir = new File ( basePath );
        if ( !theBaseDir.exists() || !theBaseDir.isDirectory() ) {
            return ( thePaths );
        }
        String theFiles[] = theBaseDir.list();
        if ( theFiles == null ) {
            return thePaths;
        }
        for ( int i = 0; i < theFiles.length; i++ ) {
            File testFile = new File ( basePath + File.separator + theFiles[i] );
            if ( testFile.isFile() ) {
                thePaths.add ( path + theFiles[i] );
            } else if ( testFile.isDirectory() ) {
                thePaths.add ( path + theFiles[i] + "/" );
            }
        }
        return ( thePaths );
    }
    @Override
    public String getServerInfo() {
        return ( "JspC/ApacheTomcat8" );
    }
    @Override
    @Deprecated
    public Servlet getServlet ( String name ) throws ServletException {
        return null;
    }
    @Override
    public String getServletContextName() {
        return ( getServerInfo() );
    }
    @Override
    @Deprecated
    public Enumeration<String> getServletNames() {
        return ( new Vector<String>().elements() );
    }
    @Override
    @Deprecated
    public Enumeration<Servlet> getServlets() {
        return ( new Vector<Servlet>().elements() );
    }
    @Override
    public void log ( String message ) {
        myLogWriter.println ( message );
    }
    @Override
    @Deprecated
    public void log ( Exception exception, String message ) {
        log ( message, exception );
    }
    @Override
    public void log ( String message, Throwable exception ) {
        myLogWriter.println ( message );
        exception.printStackTrace ( myLogWriter );
    }
    @Override
    public void removeAttribute ( String name ) {
        myAttributes.remove ( name );
    }
    @Override
    public void setAttribute ( String name, Object value ) {
        myAttributes.put ( name, value );
    }
    @Override
    public FilterRegistration.Dynamic addFilter ( String filterName,
            String className ) {
        return null;
    }
    @Override
    public ServletRegistration.Dynamic addServlet ( String servletName,
            String className ) {
        return null;
    }
    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return EnumSet.noneOf ( SessionTrackingMode.class );
    }
    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return EnumSet.noneOf ( SessionTrackingMode.class );
    }
    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return null;
    }
    @Override
    public void setSessionTrackingModes (
        Set<SessionTrackingMode> sessionTrackingModes ) {
    }
    @Override
    public Dynamic addFilter ( String filterName, Filter filter ) {
        return null;
    }
    @Override
    public Dynamic addFilter ( String filterName,
                               Class<? extends Filter> filterClass ) {
        return null;
    }
    @Override
    public ServletRegistration.Dynamic addServlet ( String servletName,
            Servlet servlet ) {
        return null;
    }
    @Override
    public ServletRegistration.Dynamic addServlet ( String servletName,
            Class<? extends Servlet> servletClass ) {
        return null;
    }
    @Override
    public <T extends Filter> T createFilter ( Class<T> c )
    throws ServletException {
        return null;
    }
    @Override
    public <T extends Servlet> T createServlet ( Class<T> c )
    throws ServletException {
        return null;
    }
    @Override
    public FilterRegistration getFilterRegistration ( String filterName ) {
        return null;
    }
    @Override
    public ServletRegistration getServletRegistration ( String servletName ) {
        return null;
    }
    @Override
    public boolean setInitParameter ( String name, String value ) {
        return myParameters.putIfAbsent ( name, value ) == null;
    }
    @Override
    public void addListener ( Class<? extends EventListener> listenerClass ) {
    }
    @Override
    public void addListener ( String className ) {
    }
    @Override
    public <T extends EventListener> void addListener ( T t ) {
    }
    @Override
    public <T extends EventListener> T createListener ( Class<T> c )
    throws ServletException {
        return null;
    }
    @Override
    public void declareRoles ( String... roleNames ) {
    }
    @Override
    public ClassLoader getClassLoader() {
        return loader;
    }
    @Override
    public int getEffectiveMajorVersion() {
        return webXml.getMajorVersion();
    }
    @Override
    public int getEffectiveMinorVersion() {
        return webXml.getMinorVersion();
    }
    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return null;
    }
    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return jspConfigDescriptor;
    }
    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return null;
    }
    @Override
    public String getVirtualServerName() {
        return null;
    }
}
