package org.apache.jasper.servlet;
import java.util.EventListener;
import javax.servlet.Filter;
import javax.servlet.SessionCookieConfig;
import java.util.EnumSet;
import javax.servlet.SessionTrackingMode;
import javax.servlet.ServletRegistration;
import javax.servlet.FilterRegistration;
import java.util.Vector;
import javax.servlet.ServletException;
import javax.servlet.Servlet;
import java.util.HashSet;
import java.io.InputStream;
import java.net.MalformedURLException;
import org.apache.jasper.runtime.ExceptionUtils;
import java.io.File;
import javax.servlet.RequestDispatcher;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.JarScanType;
import org.apache.tomcat.util.descriptor.web.FragmentJarScannerCallback;
import org.apache.tomcat.JarScanFilter;
import org.apache.tomcat.util.scan.StandardJarScanFilter;
import org.apache.tomcat.util.scan.StandardJarScanner;
import java.util.Set;
import java.io.IOException;
import org.apache.jasper.compiler.Localizer;
import org.apache.tomcat.util.descriptor.web.WebXmlParser;
import org.apache.jasper.JasperException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.descriptor.JspConfigDescriptor;
import org.apache.tomcat.util.descriptor.web.WebXml;
import java.net.URL;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.ServletContext;
public class JspCServletContext implements ServletContext {
    private final Map<String, Object> myAttributes;
    private final Map<String, String> myParameters;
    private final PrintWriter myLogWriter;
    private final URL myResourceBaseURL;
    private WebXml webXml;
    private JspConfigDescriptor jspConfigDescriptor;
    private final ClassLoader loader;
    public JspCServletContext ( final PrintWriter aLogWriter, final URL aResourceBaseURL, final ClassLoader classLoader, final boolean validate, final boolean blockExternal ) throws JasperException {
        this.myParameters = new ConcurrentHashMap<String, String>();
        this.myAttributes = new HashMap<String, Object>();
        this.myParameters.put ( "org.apache.jasper.XML_BLOCK_EXTERNAL", String.valueOf ( blockExternal ) );
        this.myLogWriter = aLogWriter;
        this.myResourceBaseURL = aResourceBaseURL;
        this.loader = classLoader;
        this.webXml = this.buildMergedWebXml ( validate, blockExternal );
        this.jspConfigDescriptor = this.webXml.getJspConfigDescriptor();
    }
    private WebXml buildMergedWebXml ( final boolean validate, final boolean blockExternal ) throws JasperException {
        final WebXml webXml = new WebXml();
        final WebXmlParser webXmlParser = new WebXmlParser ( validate, validate, blockExternal );
        webXmlParser.setClassLoader ( this.getClass().getClassLoader() );
        try {
            final URL url = this.getResource ( "/WEB-INF/web.xml" );
            if ( !webXmlParser.parseWebXml ( url, webXml, false ) ) {
                throw new JasperException ( Localizer.getMessage ( "jspc.error.invalidWebXml" ) );
            }
        } catch ( IOException e ) {
            throw new JasperException ( e );
        }
        if ( webXml.isMetadataComplete() ) {
            return webXml;
        }
        final Set<String> absoluteOrdering = webXml.getAbsoluteOrdering();
        if ( absoluteOrdering != null && absoluteOrdering.isEmpty() ) {
            return webXml;
        }
        final Map<String, WebXml> fragments = this.scanForFragments ( webXmlParser );
        final Set<WebXml> orderedFragments = WebXml.orderWebFragments ( webXml, fragments, ( ServletContext ) this );
        webXml.merge ( orderedFragments );
        return webXml;
    }
    private Map<String, WebXml> scanForFragments ( final WebXmlParser webXmlParser ) throws JasperException {
        final StandardJarScanner scanner = new StandardJarScanner();
        scanner.setScanClassPath ( false );
        scanner.setJarScanFilter ( new StandardJarScanFilter() );
        final FragmentJarScannerCallback callback = new FragmentJarScannerCallback ( webXmlParser, false, true );
        scanner.scan ( JarScanType.PLUGGABILITY, ( ServletContext ) this, callback );
        if ( !callback.isOk() ) {
            throw new JasperException ( Localizer.getMessage ( "jspc.error.invalidFragment" ) );
        }
        return callback.getFragments();
    }
    public Object getAttribute ( final String name ) {
        return this.myAttributes.get ( name );
    }
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration ( this.myAttributes.keySet() );
    }
    public ServletContext getContext ( final String uripath ) {
        return null;
    }
    public String getContextPath() {
        return null;
    }
    public String getInitParameter ( final String name ) {
        return this.myParameters.get ( name );
    }
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration ( this.myParameters.keySet() );
    }
    public int getMajorVersion() {
        return 3;
    }
    public String getMimeType ( final String file ) {
        return null;
    }
    public int getMinorVersion() {
        return 1;
    }
    public RequestDispatcher getNamedDispatcher ( final String name ) {
        return null;
    }
    public String getRealPath ( final String path ) {
        if ( !this.myResourceBaseURL.getProtocol().equals ( "file" ) ) {
            return null;
        }
        if ( !path.startsWith ( "/" ) ) {
            return null;
        }
        try {
            final File f = new File ( this.getResource ( path ).toURI() );
            return f.getAbsolutePath();
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            return null;
        }
    }
    public RequestDispatcher getRequestDispatcher ( final String path ) {
        return null;
    }
    public URL getResource ( final String path ) throws MalformedURLException {
        if ( !path.startsWith ( "/" ) ) {
            throw new MalformedURLException ( "Path '" + path + "' does not start with '/'" );
        }
        URL url = new URL ( this.myResourceBaseURL, path.substring ( 1 ) );
        try {
            final InputStream is = url.openStream();
            final Throwable t2 = null;
            if ( is != null ) {
                if ( t2 != null ) {
                    try {
                        is.close();
                    } catch ( Throwable t3 ) {
                        t2.addSuppressed ( t3 );
                    }
                } else {
                    is.close();
                }
            }
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            url = null;
        }
        return url;
    }
    public InputStream getResourceAsStream ( final String path ) {
        try {
            return this.getResource ( path ).openStream();
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            return null;
        }
    }
    public Set<String> getResourcePaths ( String path ) {
        final Set<String> thePaths = new HashSet<String>();
        if ( !path.endsWith ( "/" ) ) {
            path += "/";
        }
        final String basePath = this.getRealPath ( path );
        if ( basePath == null ) {
            return thePaths;
        }
        final File theBaseDir = new File ( basePath );
        if ( !theBaseDir.exists() || !theBaseDir.isDirectory() ) {
            return thePaths;
        }
        final String[] theFiles = theBaseDir.list();
        if ( theFiles == null ) {
            return thePaths;
        }
        for ( int i = 0; i < theFiles.length; ++i ) {
            final File testFile = new File ( basePath + File.separator + theFiles[i] );
            if ( testFile.isFile() ) {
                thePaths.add ( path + theFiles[i] );
            } else if ( testFile.isDirectory() ) {
                thePaths.add ( path + theFiles[i] + "/" );
            }
        }
        return thePaths;
    }
    public String getServerInfo() {
        return "JspC/ApacheTomcat8";
    }
    @Deprecated
    public Servlet getServlet ( final String name ) throws ServletException {
        return null;
    }
    public String getServletContextName() {
        return this.getServerInfo();
    }
    @Deprecated
    public Enumeration<String> getServletNames() {
        return new Vector<String>().elements();
    }
    @Deprecated
    public Enumeration<Servlet> getServlets() {
        return new Vector<Servlet>().elements();
    }
    public void log ( final String message ) {
        this.myLogWriter.println ( message );
    }
    @Deprecated
    public void log ( final Exception exception, final String message ) {
        this.log ( message, exception );
    }
    public void log ( final String message, final Throwable exception ) {
        this.myLogWriter.println ( message );
        exception.printStackTrace ( this.myLogWriter );
    }
    public void removeAttribute ( final String name ) {
        this.myAttributes.remove ( name );
    }
    public void setAttribute ( final String name, final Object value ) {
        this.myAttributes.put ( name, value );
    }
    public FilterRegistration.Dynamic addFilter ( final String filterName, final String className ) {
        return null;
    }
    public ServletRegistration.Dynamic addServlet ( final String servletName, final String className ) {
        return null;
    }
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return EnumSet.noneOf ( SessionTrackingMode.class );
    }
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return EnumSet.noneOf ( SessionTrackingMode.class );
    }
    public SessionCookieConfig getSessionCookieConfig() {
        return null;
    }
    public void setSessionTrackingModes ( final Set<SessionTrackingMode> sessionTrackingModes ) {
    }
    public FilterRegistration.Dynamic addFilter ( final String filterName, final Filter filter ) {
        return null;
    }
    public FilterRegistration.Dynamic addFilter ( final String filterName, final Class<? extends Filter> filterClass ) {
        return null;
    }
    public ServletRegistration.Dynamic addServlet ( final String servletName, final Servlet servlet ) {
        return null;
    }
    public ServletRegistration.Dynamic addServlet ( final String servletName, final Class<? extends Servlet> servletClass ) {
        return null;
    }
    public <T extends Filter> T createFilter ( final Class<T> c ) throws ServletException {
        return null;
    }
    public <T extends Servlet> T createServlet ( final Class<T> c ) throws ServletException {
        return null;
    }
    public FilterRegistration getFilterRegistration ( final String filterName ) {
        return null;
    }
    public ServletRegistration getServletRegistration ( final String servletName ) {
        return null;
    }
    public boolean setInitParameter ( final String name, final String value ) {
        return this.myParameters.putIfAbsent ( name, value ) == null;
    }
    public void addListener ( final Class<? extends EventListener> listenerClass ) {
    }
    public void addListener ( final String className ) {
    }
    public <T extends EventListener> void addListener ( final T t ) {
    }
    public <T extends EventListener> T createListener ( final Class<T> c ) throws ServletException {
        return null;
    }
    public void declareRoles ( final String... roleNames ) {
    }
    public ClassLoader getClassLoader() {
        return this.loader;
    }
    public int getEffectiveMajorVersion() {
        return this.webXml.getMajorVersion();
    }
    public int getEffectiveMinorVersion() {
        return this.webXml.getMinorVersion();
    }
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return null;
    }
    public JspConfigDescriptor getJspConfigDescriptor() {
        return this.jspConfigDescriptor;
    }
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return null;
    }
    public String getVirtualServerName() {
        return null;
    }
}
