package org.apache.catalina;
import org.apache.tomcat.util.http.CookieProcessor;
import java.util.Map;
import javax.servlet.ServletSecurityElement;
import javax.servlet.ServletRegistration;
import java.util.Set;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.ServletRequest;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.ErrorPage;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.tomcat.util.descriptor.web.ApplicationParameter;
import org.apache.tomcat.InstanceManager;
import org.apache.tomcat.JarScanner;
import javax.servlet.ServletContext;
import org.apache.catalina.deploy.NamingResourcesImpl;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import java.net.URL;
import java.util.Locale;
import org.apache.tomcat.ContextBind;
public interface Context extends Container, ContextBind {
    public static final String ADD_WELCOME_FILE_EVENT = "addWelcomeFile";
    public static final String REMOVE_WELCOME_FILE_EVENT = "removeWelcomeFile";
    public static final String CLEAR_WELCOME_FILES_EVENT = "clearWelcomeFiles";
    public static final String CHANGE_SESSION_ID_EVENT = "changeSessionId";
    boolean getAllowCasualMultipartParsing();
    void setAllowCasualMultipartParsing ( boolean p0 );
    Object[] getApplicationEventListeners();
    void setApplicationEventListeners ( Object[] p0 );
    Object[] getApplicationLifecycleListeners();
    void setApplicationLifecycleListeners ( Object[] p0 );
    String getCharset ( Locale p0 );
    URL getConfigFile();
    void setConfigFile ( URL p0 );
    boolean getConfigured();
    void setConfigured ( boolean p0 );
    boolean getCookies();
    void setCookies ( boolean p0 );
    String getSessionCookieName();
    void setSessionCookieName ( String p0 );
    boolean getUseHttpOnly();
    void setUseHttpOnly ( boolean p0 );
    String getSessionCookieDomain();
    void setSessionCookieDomain ( String p0 );
    String getSessionCookiePath();
    void setSessionCookiePath ( String p0 );
    boolean getSessionCookiePathUsesTrailingSlash();
    void setSessionCookiePathUsesTrailingSlash ( boolean p0 );
    boolean getCrossContext();
    String getAltDDName();
    void setAltDDName ( String p0 );
    void setCrossContext ( boolean p0 );
    boolean getDenyUncoveredHttpMethods();
    void setDenyUncoveredHttpMethods ( boolean p0 );
    String getDisplayName();
    void setDisplayName ( String p0 );
    boolean getDistributable();
    void setDistributable ( boolean p0 );
    String getDocBase();
    void setDocBase ( String p0 );
    String getEncodedPath();
    boolean getIgnoreAnnotations();
    void setIgnoreAnnotations ( boolean p0 );
    LoginConfig getLoginConfig();
    void setLoginConfig ( LoginConfig p0 );
    NamingResourcesImpl getNamingResources();
    void setNamingResources ( NamingResourcesImpl p0 );
    String getPath();
    void setPath ( String p0 );
    String getPublicId();
    void setPublicId ( String p0 );
    boolean getReloadable();
    void setReloadable ( boolean p0 );
    boolean getOverride();
    void setOverride ( boolean p0 );
    boolean getPrivileged();
    void setPrivileged ( boolean p0 );
    ServletContext getServletContext();
    int getSessionTimeout();
    void setSessionTimeout ( int p0 );
    boolean getSwallowAbortedUploads();
    void setSwallowAbortedUploads ( boolean p0 );
    boolean getSwallowOutput();
    void setSwallowOutput ( boolean p0 );
    String getWrapperClass();
    void setWrapperClass ( String p0 );
    boolean getXmlNamespaceAware();
    void setXmlNamespaceAware ( boolean p0 );
    boolean getXmlValidation();
    void setXmlValidation ( boolean p0 );
    boolean getXmlBlockExternal();
    void setXmlBlockExternal ( boolean p0 );
    boolean getTldValidation();
    void setTldValidation ( boolean p0 );
    JarScanner getJarScanner();
    void setJarScanner ( JarScanner p0 );
    Authenticator getAuthenticator();
    void setLogEffectiveWebXml ( boolean p0 );
    boolean getLogEffectiveWebXml();
    InstanceManager getInstanceManager();
    void setInstanceManager ( InstanceManager p0 );
    void setContainerSciFilter ( String p0 );
    String getContainerSciFilter();
    void addApplicationListener ( String p0 );
    void addApplicationParameter ( ApplicationParameter p0 );
    void addConstraint ( SecurityConstraint p0 );
    void addErrorPage ( ErrorPage p0 );
    void addFilterDef ( FilterDef p0 );
    void addFilterMap ( FilterMap p0 );
    void addFilterMapBefore ( FilterMap p0 );
    void addLocaleEncodingMappingParameter ( String p0, String p1 );
    void addMimeMapping ( String p0, String p1 );
    void addParameter ( String p0, String p1 );
    void addRoleMapping ( String p0, String p1 );
    void addSecurityRole ( String p0 );
default void addServletMappingDecoded ( String pattern, String name ) {
        this.addServletMappingDecoded ( pattern, name, false );
    }
    void addServletMappingDecoded ( String p0, String p1, boolean p2 );
    void addWatchedResource ( String p0 );
    void addWelcomeFile ( String p0 );
    void addWrapperLifecycle ( String p0 );
    void addWrapperListener ( String p0 );
    Wrapper createWrapper();
    String[] findApplicationListeners();
    ApplicationParameter[] findApplicationParameters();
    SecurityConstraint[] findConstraints();
    ErrorPage findErrorPage ( int p0 );
    ErrorPage findErrorPage ( String p0 );
    ErrorPage[] findErrorPages();
    FilterDef findFilterDef ( String p0 );
    FilterDef[] findFilterDefs();
    FilterMap[] findFilterMaps();
    String findMimeMapping ( String p0 );
    String[] findMimeMappings();
    String findParameter ( String p0 );
    String[] findParameters();
    String findRoleMapping ( String p0 );
    boolean findSecurityRole ( String p0 );
    String[] findSecurityRoles();
    String findServletMapping ( String p0 );
    String[] findServletMappings();
    String findStatusPage ( int p0 );
    int[] findStatusPages();
    ThreadBindingListener getThreadBindingListener();
    void setThreadBindingListener ( ThreadBindingListener p0 );
    String[] findWatchedResources();
    boolean findWelcomeFile ( String p0 );
    String[] findWelcomeFiles();
    String[] findWrapperLifecycles();
    String[] findWrapperListeners();
    boolean fireRequestInitEvent ( ServletRequest p0 );
    boolean fireRequestDestroyEvent ( ServletRequest p0 );
    void reload();
    void removeApplicationListener ( String p0 );
    void removeApplicationParameter ( String p0 );
    void removeConstraint ( SecurityConstraint p0 );
    void removeErrorPage ( ErrorPage p0 );
    void removeFilterDef ( FilterDef p0 );
    void removeFilterMap ( FilterMap p0 );
    void removeMimeMapping ( String p0 );
    void removeParameter ( String p0 );
    void removeRoleMapping ( String p0 );
    void removeSecurityRole ( String p0 );
    void removeServletMapping ( String p0 );
    void removeWatchedResource ( String p0 );
    void removeWelcomeFile ( String p0 );
    void removeWrapperLifecycle ( String p0 );
    void removeWrapperListener ( String p0 );
    String getRealPath ( String p0 );
    int getEffectiveMajorVersion();
    void setEffectiveMajorVersion ( int p0 );
    int getEffectiveMinorVersion();
    void setEffectiveMinorVersion ( int p0 );
    JspConfigDescriptor getJspConfigDescriptor();
    void setJspConfigDescriptor ( JspConfigDescriptor p0 );
    void addServletContainerInitializer ( ServletContainerInitializer p0, Set<Class<?>> p1 );
    boolean getPaused();
    boolean isServlet22();
    Set<String> addServletSecurity ( ServletRegistration.Dynamic p0, ServletSecurityElement p1 );
    void setResourceOnlyServlets ( String p0 );
    String getResourceOnlyServlets();
    boolean isResourceOnlyServlet ( String p0 );
    String getBaseName();
    void setWebappVersion ( String p0 );
    String getWebappVersion();
    void setFireRequestListenersOnForwards ( boolean p0 );
    boolean getFireRequestListenersOnForwards();
    void setPreemptiveAuthentication ( boolean p0 );
    boolean getPreemptiveAuthentication();
    void setSendRedirectBody ( boolean p0 );
    boolean getSendRedirectBody();
    Loader getLoader();
    void setLoader ( Loader p0 );
    WebResourceRoot getResources();
    void setResources ( WebResourceRoot p0 );
    Manager getManager();
    void setManager ( Manager p0 );
    void setAddWebinfClassesResources ( boolean p0 );
    boolean getAddWebinfClassesResources();
    void addPostConstructMethod ( String p0, String p1 );
    void addPreDestroyMethod ( String p0, String p1 );
    void removePostConstructMethod ( String p0 );
    void removePreDestroyMethod ( String p0 );
    String findPostConstructMethod ( String p0 );
    String findPreDestroyMethod ( String p0 );
    Map<String, String> findPostConstructMethods();
    Map<String, String> findPreDestroyMethods();
    Object getNamingToken();
    void setCookieProcessor ( CookieProcessor p0 );
    CookieProcessor getCookieProcessor();
    void setValidateClientProvidedNewSessionId ( boolean p0 );
    boolean getValidateClientProvidedNewSessionId();
    void setMapperContextRootRedirectEnabled ( boolean p0 );
    boolean getMapperContextRootRedirectEnabled();
    void setMapperDirectoryRedirectEnabled ( boolean p0 );
    boolean getMapperDirectoryRedirectEnabled();
    void setUseRelativeRedirects ( boolean p0 );
    boolean getUseRelativeRedirects();
    void setDispatchersUseEncodedPaths ( boolean p0 );
    boolean getDispatchersUseEncodedPaths();
}
