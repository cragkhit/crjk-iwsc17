package org.apache.catalina.startup;
import org.apache.tomcat.util.http.CookieProcessor;
import org.apache.catalina.ThreadBindingListener;
import org.apache.tomcat.InstanceManager;
import java.util.Map;
import java.io.File;
import org.apache.catalina.Valve;
import javax.servlet.ServletSecurityElement;
import javax.servlet.ServletRegistration;
import java.util.Set;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.ServletRequest;
import org.apache.catalina.Wrapper;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.ErrorPage;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.tomcat.util.descriptor.web.ApplicationParameter;
import org.apache.catalina.Authenticator;
import org.apache.tomcat.JarScanner;
import javax.servlet.ServletContext;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import java.util.Locale;
import org.apache.catalina.AccessLog;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.Request;
import java.beans.PropertyChangeListener;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.Realm;
import org.apache.catalina.Cluster;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Manager;
import org.apache.juli.logging.Log;
import org.apache.catalina.Loader;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Host;
import org.apache.catalina.util.ContextName;
import org.apache.catalina.Engine;
import org.apache.catalina.Container;
import java.net.URL;
import org.apache.tomcat.util.res.StringManager;
import org.apache.catalina.Context;
import org.apache.catalina.util.LifecycleMBeanBase;
public class FailedContext extends LifecycleMBeanBase implements Context {
    protected static final StringManager sm;
    private URL configFile;
    private String docBase;
    private String name;
    private Container parent;
    private String path;
    private String webappVersion;
    public FailedContext() {
        this.name = null;
        this.path = null;
        this.webappVersion = null;
    }
    @Override
    public URL getConfigFile() {
        return this.configFile;
    }
    @Override
    public void setConfigFile ( final URL configFile ) {
        this.configFile = configFile;
    }
    @Override
    public String getDocBase() {
        return this.docBase;
    }
    @Override
    public void setDocBase ( final String docBase ) {
        this.docBase = docBase;
    }
    @Override
    public String getName() {
        return this.name;
    }
    @Override
    public void setName ( final String name ) {
        this.name = name;
    }
    @Override
    public Container getParent() {
        return this.parent;
    }
    @Override
    public void setParent ( final Container parent ) {
        this.parent = parent;
    }
    @Override
    public String getPath() {
        return this.path;
    }
    @Override
    public void setPath ( final String path ) {
        this.path = path;
    }
    @Override
    public String getWebappVersion() {
        return this.webappVersion;
    }
    @Override
    public void setWebappVersion ( final String webappVersion ) {
        this.webappVersion = webappVersion;
    }
    @Override
    protected String getDomainInternal() {
        final Container p = this.getParent();
        if ( p == null ) {
            return null;
        }
        return p.getDomain();
    }
    @Override
    public String getMBeanKeyProperties() {
        Container c = this;
        final StringBuilder keyProperties = new StringBuilder();
        int containerCount = 0;
        while ( ! ( c instanceof Engine ) ) {
            if ( c instanceof Context ) {
                keyProperties.append ( ",context=" );
                final ContextName cn = new ContextName ( c.getName(), false );
                keyProperties.append ( cn.getDisplayName() );
            } else if ( c instanceof Host ) {
                keyProperties.append ( ",host=" );
                keyProperties.append ( c.getName() );
            } else {
                if ( c == null ) {
                    keyProperties.append ( ",container" );
                    keyProperties.append ( containerCount++ );
                    keyProperties.append ( "=null" );
                    break;
                }
                keyProperties.append ( ",container" );
                keyProperties.append ( containerCount++ );
                keyProperties.append ( '=' );
                keyProperties.append ( c.getName() );
            }
            c = c.getParent();
        }
        return keyProperties.toString();
    }
    @Override
    protected String getObjectNameKeyProperties() {
        final StringBuilder keyProperties = new StringBuilder ( "j2eeType=WebModule,name=//" );
        final String hostname = this.getParent().getName();
        if ( hostname == null ) {
            keyProperties.append ( "DEFAULT" );
        } else {
            keyProperties.append ( hostname );
        }
        final String contextName = this.getName();
        if ( !contextName.startsWith ( "/" ) ) {
            keyProperties.append ( '/' );
        }
        keyProperties.append ( contextName );
        keyProperties.append ( ",J2EEApplication=none,J2EEServer=none" );
        return keyProperties.toString();
    }
    @Override
    protected void startInternal() throws LifecycleException {
        throw new LifecycleException ( FailedContext.sm.getString ( "failedContext.start", this.getName() ) );
    }
    @Override
    protected void stopInternal() throws LifecycleException {
    }
    @Override
    public void addWatchedResource ( final String name ) {
    }
    @Override
    public String[] findWatchedResources() {
        return new String[0];
    }
    @Override
    public void removeWatchedResource ( final String name ) {
    }
    @Override
    public void addChild ( final Container child ) {
    }
    @Override
    public Container findChild ( final String name ) {
        return null;
    }
    @Override
    public Container[] findChildren() {
        return new Container[0];
    }
    @Override
    public void removeChild ( final Container child ) {
    }
    @Override
    public String toString() {
        return this.getName();
    }
    @Override
    public Loader getLoader() {
        return null;
    }
    @Override
    public void setLoader ( final Loader loader ) {
    }
    @Override
    public Log getLogger() {
        return null;
    }
    @Override
    public String getLogName() {
        return null;
    }
    @Override
    public Manager getManager() {
        return null;
    }
    @Override
    public void setManager ( final Manager manager ) {
    }
    @Override
    public Pipeline getPipeline() {
        return null;
    }
    @Override
    public Cluster getCluster() {
        return null;
    }
    @Override
    public void setCluster ( final Cluster cluster ) {
    }
    @Override
    public int getBackgroundProcessorDelay() {
        return -1;
    }
    @Override
    public void setBackgroundProcessorDelay ( final int delay ) {
    }
    @Override
    public ClassLoader getParentClassLoader() {
        return null;
    }
    @Override
    public void setParentClassLoader ( final ClassLoader parent ) {
    }
    @Override
    public Realm getRealm() {
        return null;
    }
    @Override
    public void setRealm ( final Realm realm ) {
    }
    @Override
    public WebResourceRoot getResources() {
        return null;
    }
    @Override
    public void setResources ( final WebResourceRoot resources ) {
    }
    @Override
    public void backgroundProcess() {
    }
    @Override
    public void addContainerListener ( final ContainerListener listener ) {
    }
    @Override
    public ContainerListener[] findContainerListeners() {
        return null;
    }
    @Override
    public void removeContainerListener ( final ContainerListener listener ) {
    }
    @Override
    public void addPropertyChangeListener ( final PropertyChangeListener listener ) {
    }
    @Override
    public void removePropertyChangeListener ( final PropertyChangeListener listener ) {
    }
    @Override
    public void fireContainerEvent ( final String type, final Object data ) {
    }
    @Override
    public void logAccess ( final Request request, final Response response, final long time, final boolean useDefault ) {
    }
    @Override
    public AccessLog getAccessLog() {
        return null;
    }
    @Override
    public int getStartStopThreads() {
        return 0;
    }
    @Override
    public void setStartStopThreads ( final int startStopThreads ) {
    }
    @Override
    public boolean getAllowCasualMultipartParsing() {
        return false;
    }
    @Override
    public void setAllowCasualMultipartParsing ( final boolean allowCasualMultipartParsing ) {
    }
    @Override
    public Object[] getApplicationEventListeners() {
        return null;
    }
    @Override
    public void setApplicationEventListeners ( final Object[] listeners ) {
    }
    @Override
    public Object[] getApplicationLifecycleListeners() {
        return null;
    }
    @Override
    public void setApplicationLifecycleListeners ( final Object[] listeners ) {
    }
    @Override
    public String getCharset ( final Locale locale ) {
        return null;
    }
    @Override
    public boolean getConfigured() {
        return false;
    }
    @Override
    public void setConfigured ( final boolean configured ) {
    }
    @Override
    public boolean getCookies() {
        return false;
    }
    @Override
    public void setCookies ( final boolean cookies ) {
    }
    @Override
    public String getSessionCookieName() {
        return null;
    }
    @Override
    public void setSessionCookieName ( final String sessionCookieName ) {
    }
    @Override
    public boolean getUseHttpOnly() {
        return false;
    }
    @Override
    public void setUseHttpOnly ( final boolean useHttpOnly ) {
    }
    @Override
    public String getSessionCookieDomain() {
        return null;
    }
    @Override
    public void setSessionCookieDomain ( final String sessionCookieDomain ) {
    }
    @Override
    public String getSessionCookiePath() {
        return null;
    }
    @Override
    public void setSessionCookiePath ( final String sessionCookiePath ) {
    }
    @Override
    public boolean getSessionCookiePathUsesTrailingSlash() {
        return false;
    }
    @Override
    public void setSessionCookiePathUsesTrailingSlash ( final boolean sessionCookiePathUsesTrailingSlash ) {
    }
    @Override
    public boolean getCrossContext() {
        return false;
    }
    @Override
    public void setCrossContext ( final boolean crossContext ) {
    }
    @Override
    public String getAltDDName() {
        return null;
    }
    @Override
    public void setAltDDName ( final String altDDName ) {
    }
    @Override
    public boolean getDenyUncoveredHttpMethods() {
        return false;
    }
    @Override
    public void setDenyUncoveredHttpMethods ( final boolean denyUncoveredHttpMethods ) {
    }
    @Override
    public String getDisplayName() {
        return null;
    }
    @Override
    public void setDisplayName ( final String displayName ) {
    }
    @Override
    public boolean getDistributable() {
        return false;
    }
    @Override
    public void setDistributable ( final boolean distributable ) {
    }
    @Override
    public String getEncodedPath() {
        return null;
    }
    @Override
    public boolean getIgnoreAnnotations() {
        return false;
    }
    @Override
    public void setIgnoreAnnotations ( final boolean ignoreAnnotations ) {
    }
    @Override
    public LoginConfig getLoginConfig() {
        return null;
    }
    @Override
    public void setLoginConfig ( final LoginConfig config ) {
    }
    @Override
    public NamingResourcesImpl getNamingResources() {
        return null;
    }
    @Override
    public void setNamingResources ( final NamingResourcesImpl namingResources ) {
    }
    @Override
    public String getPublicId() {
        return null;
    }
    @Override
    public void setPublicId ( final String publicId ) {
    }
    @Override
    public boolean getReloadable() {
        return false;
    }
    @Override
    public void setReloadable ( final boolean reloadable ) {
    }
    @Override
    public boolean getOverride() {
        return false;
    }
    @Override
    public void setOverride ( final boolean override ) {
    }
    @Override
    public boolean getPrivileged() {
        return false;
    }
    @Override
    public void setPrivileged ( final boolean privileged ) {
    }
    @Override
    public ServletContext getServletContext() {
        return null;
    }
    @Override
    public int getSessionTimeout() {
        return 0;
    }
    @Override
    public void setSessionTimeout ( final int timeout ) {
    }
    @Override
    public boolean getSwallowAbortedUploads() {
        return false;
    }
    @Override
    public void setSwallowAbortedUploads ( final boolean swallowAbortedUploads ) {
    }
    @Override
    public boolean getSwallowOutput() {
        return false;
    }
    @Override
    public void setSwallowOutput ( final boolean swallowOutput ) {
    }
    @Override
    public String getWrapperClass() {
        return null;
    }
    @Override
    public void setWrapperClass ( final String wrapperClass ) {
    }
    @Override
    public boolean getXmlNamespaceAware() {
        return false;
    }
    @Override
    public void setXmlNamespaceAware ( final boolean xmlNamespaceAware ) {
    }
    @Override
    public boolean getXmlValidation() {
        return false;
    }
    @Override
    public void setXmlValidation ( final boolean xmlValidation ) {
    }
    @Override
    public boolean getXmlBlockExternal() {
        return true;
    }
    @Override
    public void setXmlBlockExternal ( final boolean xmlBlockExternal ) {
    }
    @Override
    public boolean getTldValidation() {
        return false;
    }
    @Override
    public void setTldValidation ( final boolean tldValidation ) {
    }
    @Override
    public JarScanner getJarScanner() {
        return null;
    }
    @Override
    public void setJarScanner ( final JarScanner jarScanner ) {
    }
    @Override
    public Authenticator getAuthenticator() {
        return null;
    }
    @Override
    public void setLogEffectiveWebXml ( final boolean logEffectiveWebXml ) {
    }
    @Override
    public boolean getLogEffectiveWebXml() {
        return false;
    }
    @Override
    public void addApplicationListener ( final String listener ) {
    }
    @Override
    public String[] findApplicationListeners() {
        return null;
    }
    @Override
    public void removeApplicationListener ( final String listener ) {
    }
    @Override
    public void addApplicationParameter ( final ApplicationParameter parameter ) {
    }
    @Override
    public ApplicationParameter[] findApplicationParameters() {
        return null;
    }
    @Override
    public void removeApplicationParameter ( final String name ) {
    }
    @Override
    public void addConstraint ( final SecurityConstraint constraint ) {
    }
    @Override
    public SecurityConstraint[] findConstraints() {
        return null;
    }
    @Override
    public void removeConstraint ( final SecurityConstraint constraint ) {
    }
    @Override
    public void addErrorPage ( final ErrorPage errorPage ) {
    }
    @Override
    public ErrorPage findErrorPage ( final int errorCode ) {
        return null;
    }
    @Override
    public ErrorPage findErrorPage ( final String exceptionType ) {
        return null;
    }
    @Override
    public ErrorPage[] findErrorPages() {
        return null;
    }
    @Override
    public void removeErrorPage ( final ErrorPage errorPage ) {
    }
    @Override
    public void addFilterDef ( final FilterDef filterDef ) {
    }
    @Override
    public FilterDef findFilterDef ( final String filterName ) {
        return null;
    }
    @Override
    public FilterDef[] findFilterDefs() {
        return null;
    }
    @Override
    public void removeFilterDef ( final FilterDef filterDef ) {
    }
    @Override
    public void addFilterMap ( final FilterMap filterMap ) {
    }
    @Override
    public void addFilterMapBefore ( final FilterMap filterMap ) {
    }
    @Override
    public FilterMap[] findFilterMaps() {
        return null;
    }
    @Override
    public void removeFilterMap ( final FilterMap filterMap ) {
    }
    @Override
    public void addLocaleEncodingMappingParameter ( final String locale, final String encoding ) {
    }
    @Override
    public void addMimeMapping ( final String extension, final String mimeType ) {
    }
    @Override
    public String findMimeMapping ( final String extension ) {
        return null;
    }
    @Override
    public String[] findMimeMappings() {
        return null;
    }
    @Override
    public void removeMimeMapping ( final String extension ) {
    }
    @Override
    public void addParameter ( final String name, final String value ) {
    }
    @Override
    public String findParameter ( final String name ) {
        return null;
    }
    @Override
    public String[] findParameters() {
        return null;
    }
    @Override
    public void removeParameter ( final String name ) {
    }
    @Override
    public void addRoleMapping ( final String role, final String link ) {
    }
    @Override
    public String findRoleMapping ( final String role ) {
        return null;
    }
    @Override
    public void removeRoleMapping ( final String role ) {
    }
    @Override
    public void addSecurityRole ( final String role ) {
    }
    @Override
    public boolean findSecurityRole ( final String role ) {
        return false;
    }
    @Override
    public String[] findSecurityRoles() {
        return null;
    }
    @Override
    public void removeSecurityRole ( final String role ) {
    }
    @Override
    public void addServletMappingDecoded ( final String pattern, final String name, final boolean jspWildcard ) {
    }
    @Override
    public String findServletMapping ( final String pattern ) {
        return null;
    }
    @Override
    public String[] findServletMappings() {
        return null;
    }
    @Override
    public void removeServletMapping ( final String pattern ) {
    }
    @Override
    public void addWelcomeFile ( final String name ) {
    }
    @Override
    public boolean findWelcomeFile ( final String name ) {
        return false;
    }
    @Override
    public String[] findWelcomeFiles() {
        return null;
    }
    @Override
    public void removeWelcomeFile ( final String name ) {
    }
    @Override
    public void addWrapperLifecycle ( final String listener ) {
    }
    @Override
    public String[] findWrapperLifecycles() {
        return null;
    }
    @Override
    public void removeWrapperLifecycle ( final String listener ) {
    }
    @Override
    public void addWrapperListener ( final String listener ) {
    }
    @Override
    public String[] findWrapperListeners() {
        return null;
    }
    @Override
    public void removeWrapperListener ( final String listener ) {
    }
    @Override
    public Wrapper createWrapper() {
        return null;
    }
    @Override
    public String findStatusPage ( final int status ) {
        return null;
    }
    @Override
    public int[] findStatusPages() {
        return null;
    }
    @Override
    public boolean fireRequestInitEvent ( final ServletRequest request ) {
        return false;
    }
    @Override
    public boolean fireRequestDestroyEvent ( final ServletRequest request ) {
        return false;
    }
    @Override
    public void reload() {
    }
    @Override
    public String getRealPath ( final String path ) {
        return null;
    }
    @Override
    public int getEffectiveMajorVersion() {
        return 0;
    }
    @Override
    public void setEffectiveMajorVersion ( final int major ) {
    }
    @Override
    public int getEffectiveMinorVersion() {
        return 0;
    }
    @Override
    public void setEffectiveMinorVersion ( final int minor ) {
    }
    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return null;
    }
    @Override
    public void setJspConfigDescriptor ( final JspConfigDescriptor descriptor ) {
    }
    @Override
    public void addServletContainerInitializer ( final ServletContainerInitializer sci, final Set<Class<?>> classes ) {
    }
    @Override
    public boolean getPaused() {
        return false;
    }
    @Override
    public boolean isServlet22() {
        return false;
    }
    @Override
    public Set<String> addServletSecurity ( final ServletRegistration.Dynamic registration, final ServletSecurityElement servletSecurityElement ) {
        return null;
    }
    @Override
    public void setResourceOnlyServlets ( final String resourceOnlyServlets ) {
    }
    @Override
    public String getResourceOnlyServlets() {
        return null;
    }
    @Override
    public boolean isResourceOnlyServlet ( final String servletName ) {
        return false;
    }
    @Override
    public String getBaseName() {
        return null;
    }
    @Override
    public void setFireRequestListenersOnForwards ( final boolean enable ) {
    }
    @Override
    public boolean getFireRequestListenersOnForwards() {
        return false;
    }
    @Override
    public void setPreemptiveAuthentication ( final boolean enable ) {
    }
    @Override
    public boolean getPreemptiveAuthentication() {
        return false;
    }
    @Override
    public void setSendRedirectBody ( final boolean enable ) {
    }
    @Override
    public boolean getSendRedirectBody() {
        return false;
    }
    public synchronized void addValve ( final Valve valve ) {
    }
    @Override
    public File getCatalinaBase() {
        return null;
    }
    @Override
    public File getCatalinaHome() {
        return null;
    }
    @Override
    public void setAddWebinfClassesResources ( final boolean addWebinfClassesResources ) {
    }
    @Override
    public boolean getAddWebinfClassesResources() {
        return false;
    }
    @Override
    public void addPostConstructMethod ( final String clazz, final String method ) {
    }
    @Override
    public void addPreDestroyMethod ( final String clazz, final String method ) {
    }
    @Override
    public void removePostConstructMethod ( final String clazz ) {
    }
    @Override
    public void removePreDestroyMethod ( final String clazz ) {
    }
    @Override
    public String findPostConstructMethod ( final String clazz ) {
        return null;
    }
    @Override
    public String findPreDestroyMethod ( final String clazz ) {
        return null;
    }
    @Override
    public Map<String, String> findPostConstructMethods() {
        return null;
    }
    @Override
    public Map<String, String> findPreDestroyMethods() {
        return null;
    }
    @Override
    public InstanceManager getInstanceManager() {
        return null;
    }
    @Override
    public void setInstanceManager ( final InstanceManager instanceManager ) {
    }
    @Override
    public void setContainerSciFilter ( final String containerSciFilter ) {
    }
    @Override
    public String getContainerSciFilter() {
        return null;
    }
    @Override
    public ThreadBindingListener getThreadBindingListener() {
        return null;
    }
    @Override
    public void setThreadBindingListener ( final ThreadBindingListener threadBindingListener ) {
    }
    @Override
    public ClassLoader bind ( final boolean usePrivilegedAction, final ClassLoader originalClassLoader ) {
        return null;
    }
    @Override
    public void unbind ( final boolean usePrivilegedAction, final ClassLoader originalClassLoader ) {
    }
    @Override
    public Object getNamingToken() {
        return null;
    }
    @Override
    public void setCookieProcessor ( final CookieProcessor cookieProcessor ) {
    }
    @Override
    public CookieProcessor getCookieProcessor() {
        return null;
    }
    @Override
    public void setValidateClientProvidedNewSessionId ( final boolean validateClientProvidedNewSessionId ) {
    }
    @Override
    public boolean getValidateClientProvidedNewSessionId() {
        return false;
    }
    @Override
    public void setMapperContextRootRedirectEnabled ( final boolean mapperContextRootRedirectEnabled ) {
    }
    @Override
    public boolean getMapperContextRootRedirectEnabled() {
        return false;
    }
    @Override
    public void setMapperDirectoryRedirectEnabled ( final boolean mapperDirectoryRedirectEnabled ) {
    }
    @Override
    public boolean getMapperDirectoryRedirectEnabled() {
        return false;
    }
    @Override
    public void setUseRelativeRedirects ( final boolean useRelativeRedirects ) {
    }
    @Override
    public boolean getUseRelativeRedirects() {
        return true;
    }
    @Override
    public void setDispatchersUseEncodedPaths ( final boolean dispatchersUseEncodedPaths ) {
    }
    @Override
    public boolean getDispatchersUseEncodedPaths() {
        return true;
    }
    static {
        sm = StringManager.getManager ( "org.apache.catalina.startup" );
    }
}
