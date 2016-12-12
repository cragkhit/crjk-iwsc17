package org.apache.tomcat.util.descriptor.web;
import org.apache.juli.logging.LogFactory;
import javax.servlet.ServletContext;
import java.util.EnumSet;
import java.util.List;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.servlet.SessionTrackingMode;
import javax.servlet.DispatcherType;
import java.util.Iterator;
import java.util.Collection;
import javax.servlet.descriptor.TaglibDescriptor;
import javax.servlet.descriptor.JspPropertyGroupDescriptor;
import java.util.ArrayList;
import javax.servlet.descriptor.JspConfigDescriptor;
import org.apache.tomcat.util.buf.UDecoder;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import org.apache.juli.logging.Log;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.digester.DocumentProperties;
public class WebXml extends XmlEncodingBase implements DocumentProperties.Encoding {
    protected static final String ORDER_OTHERS = "org.apache.catalina.order.others";
    private static final StringManager sm;
    private static final Log log;
    private boolean overridable;
    private Set<String> absoluteOrdering;
    private final Set<String> after;
    private final Set<String> before;
    private String publicId;
    private boolean metadataComplete;
    private String name;
    private int majorVersion;
    private int minorVersion;
    private String displayName;
    private boolean distributable;
    private boolean denyUncoveredHttpMethods;
    private final Map<String, String> contextParams;
    private final Map<String, FilterDef> filters;
    private final Set<FilterMap> filterMaps;
    private final Set<String> filterMappingNames;
    private final Set<String> listeners;
    private final Map<String, ServletDef> servlets;
    private final Map<String, String> servletMappings;
    private final Set<String> servletMappingNames;
    private SessionConfig sessionConfig;
    private final Map<String, String> mimeMappings;
    private boolean replaceWelcomeFiles;
    private boolean alwaysAddWelcomeFiles;
    private final Set<String> welcomeFiles;
    private final Map<String, ErrorPage> errorPages;
    private final Map<String, String> taglibs;
    private final Set<JspPropertyGroup> jspPropertyGroups;
    private final Set<SecurityConstraint> securityConstraints;
    private LoginConfig loginConfig;
    private final Set<String> securityRoles;
    private final Map<String, ContextEnvironment> envEntries;
    private final Map<String, ContextEjb> ejbRefs;
    private final Map<String, ContextLocalEjb> ejbLocalRefs;
    private final Map<String, ContextService> serviceRefs;
    private final Map<String, ContextResource> resourceRefs;
    private final Map<String, ContextResourceEnvRef> resourceEnvRefs;
    private final Map<String, MessageDestinationRef> messageDestinationRefs;
    private final Map<String, MessageDestination> messageDestinations;
    private final Map<String, String> localeEncodingMappings;
    private Map<String, String> postConstructMethods;
    private Map<String, String> preDestroyMethods;
    private URL uRL;
    private String jarName;
    private boolean webappJar;
    private boolean delegate;
    private static final String INDENT2 = "  ";
    private static final String INDENT4 = "    ";
    private static final String INDENT6 = "      ";
    public WebXml() {
        this.overridable = false;
        this.absoluteOrdering = null;
        this.after = new LinkedHashSet<String>();
        this.before = new LinkedHashSet<String>();
        this.publicId = null;
        this.metadataComplete = false;
        this.name = null;
        this.majorVersion = 4;
        this.minorVersion = 0;
        this.displayName = null;
        this.distributable = false;
        this.denyUncoveredHttpMethods = false;
        this.contextParams = new HashMap<String, String>();
        this.filters = new LinkedHashMap<String, FilterDef>();
        this.filterMaps = new LinkedHashSet<FilterMap>();
        this.filterMappingNames = new HashSet<String>();
        this.listeners = new LinkedHashSet<String>();
        this.servlets = new HashMap<String, ServletDef>();
        this.servletMappings = new HashMap<String, String>();
        this.servletMappingNames = new HashSet<String>();
        this.sessionConfig = new SessionConfig();
        this.mimeMappings = new HashMap<String, String>();
        this.replaceWelcomeFiles = false;
        this.alwaysAddWelcomeFiles = true;
        this.welcomeFiles = new LinkedHashSet<String>();
        this.errorPages = new HashMap<String, ErrorPage>();
        this.taglibs = new HashMap<String, String>();
        this.jspPropertyGroups = new LinkedHashSet<JspPropertyGroup>();
        this.securityConstraints = new HashSet<SecurityConstraint>();
        this.loginConfig = null;
        this.securityRoles = new HashSet<String>();
        this.envEntries = new HashMap<String, ContextEnvironment>();
        this.ejbRefs = new HashMap<String, ContextEjb>();
        this.ejbLocalRefs = new HashMap<String, ContextLocalEjb>();
        this.serviceRefs = new HashMap<String, ContextService>();
        this.resourceRefs = new HashMap<String, ContextResource>();
        this.resourceEnvRefs = new HashMap<String, ContextResourceEnvRef>();
        this.messageDestinationRefs = new HashMap<String, MessageDestinationRef>();
        this.messageDestinations = new HashMap<String, MessageDestination>();
        this.localeEncodingMappings = new HashMap<String, String>();
        this.postConstructMethods = new HashMap<String, String>();
        this.preDestroyMethods = new HashMap<String, String>();
        this.uRL = null;
        this.jarName = null;
        this.webappJar = true;
        this.delegate = false;
    }
    public boolean isOverridable() {
        return this.overridable;
    }
    public void setOverridable ( final boolean overridable ) {
        this.overridable = overridable;
    }
    public void createAbsoluteOrdering() {
        if ( this.absoluteOrdering == null ) {
            this.absoluteOrdering = new LinkedHashSet<String>();
        }
    }
    public void addAbsoluteOrdering ( final String fragmentName ) {
        this.createAbsoluteOrdering();
        this.absoluteOrdering.add ( fragmentName );
    }
    public void addAbsoluteOrderingOthers() {
        this.createAbsoluteOrdering();
        this.absoluteOrdering.add ( "org.apache.catalina.order.others" );
    }
    public Set<String> getAbsoluteOrdering() {
        return this.absoluteOrdering;
    }
    public void addAfterOrdering ( final String fragmentName ) {
        this.after.add ( fragmentName );
    }
    public void addAfterOrderingOthers() {
        if ( this.before.contains ( "org.apache.catalina.order.others" ) ) {
            throw new IllegalArgumentException ( WebXml.sm.getString ( "webXml.multipleOther" ) );
        }
        this.after.add ( "org.apache.catalina.order.others" );
    }
    public Set<String> getAfterOrdering() {
        return this.after;
    }
    public void addBeforeOrdering ( final String fragmentName ) {
        this.before.add ( fragmentName );
    }
    public void addBeforeOrderingOthers() {
        if ( this.after.contains ( "org.apache.catalina.order.others" ) ) {
            throw new IllegalArgumentException ( WebXml.sm.getString ( "webXml.multipleOther" ) );
        }
        this.before.add ( "org.apache.catalina.order.others" );
    }
    public Set<String> getBeforeOrdering() {
        return this.before;
    }
    public String getVersion() {
        final StringBuilder sb = new StringBuilder ( 3 );
        sb.append ( this.majorVersion );
        sb.append ( '.' );
        sb.append ( this.minorVersion );
        return sb.toString();
    }
    public void setVersion ( final String version ) {
        if ( version == null ) {
            return;
        }
        switch ( version ) {
        case "2.4": {
            this.majorVersion = 2;
            this.minorVersion = 4;
            break;
        }
        case "2.5": {
            this.majorVersion = 2;
            this.minorVersion = 5;
            break;
        }
        case "3.0": {
            this.majorVersion = 3;
            this.minorVersion = 0;
            break;
        }
        case "3.1": {
            this.majorVersion = 3;
            this.minorVersion = 1;
            break;
        }
        case "4.0": {
            this.majorVersion = 4;
            this.minorVersion = 0;
            break;
        }
        default: {
            WebXml.log.warn ( WebXml.sm.getString ( "webXml.version.unknown", version ) );
            break;
        }
        }
    }
    public String getPublicId() {
        return this.publicId;
    }
    public void setPublicId ( final String publicId ) {
        if ( publicId == null ) {
            return;
        }
        switch ( publicId ) {
        case "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN": {
            this.majorVersion = 2;
            this.minorVersion = 2;
            this.publicId = publicId;
            break;
        }
        case "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN": {
            this.majorVersion = 2;
            this.minorVersion = 3;
            this.publicId = publicId;
            break;
        }
        default: {
            WebXml.log.warn ( WebXml.sm.getString ( "webXml.unrecognisedPublicId", publicId ) );
            break;
        }
        }
    }
    public boolean isMetadataComplete() {
        return this.metadataComplete;
    }
    public void setMetadataComplete ( final boolean metadataComplete ) {
        this.metadataComplete = metadataComplete;
    }
    public String getName() {
        return this.name;
    }
    public void setName ( final String name ) {
        if ( "org.apache.catalina.order.others".equalsIgnoreCase ( name ) ) {
            WebXml.log.warn ( WebXml.sm.getString ( "webXml.reservedName", name ) );
        } else {
            this.name = name;
        }
    }
    public int getMajorVersion() {
        return this.majorVersion;
    }
    public int getMinorVersion() {
        return this.minorVersion;
    }
    public String getDisplayName() {
        return this.displayName;
    }
    public void setDisplayName ( final String displayName ) {
        this.displayName = displayName;
    }
    public boolean isDistributable() {
        return this.distributable;
    }
    public void setDistributable ( final boolean distributable ) {
        this.distributable = distributable;
    }
    public boolean getDenyUncoveredHttpMethods() {
        return this.denyUncoveredHttpMethods;
    }
    public void setDenyUncoveredHttpMethods ( final boolean denyUncoveredHttpMethods ) {
        this.denyUncoveredHttpMethods = denyUncoveredHttpMethods;
    }
    public void addContextParam ( final String param, final String value ) {
        this.contextParams.put ( param, value );
    }
    public Map<String, String> getContextParams() {
        return this.contextParams;
    }
    public void addFilter ( final FilterDef filter ) {
        if ( this.filters.containsKey ( filter.getFilterName() ) ) {
            throw new IllegalArgumentException ( WebXml.sm.getString ( "webXml.duplicateFilter", filter.getFilterName() ) );
        }
        this.filters.put ( filter.getFilterName(), filter );
    }
    public Map<String, FilterDef> getFilters() {
        return this.filters;
    }
    public void addFilterMapping ( final FilterMap filterMap ) {
        this.filterMaps.add ( filterMap );
        this.filterMappingNames.add ( filterMap.getFilterName() );
    }
    public Set<FilterMap> getFilterMappings() {
        return this.filterMaps;
    }
    public void addListener ( final String className ) {
        this.listeners.add ( className );
    }
    public Set<String> getListeners() {
        return this.listeners;
    }
    public void addServlet ( final ServletDef servletDef ) {
        this.servlets.put ( servletDef.getServletName(), servletDef );
        if ( this.overridable ) {
            servletDef.setOverridable ( this.overridable );
        }
    }
    public Map<String, ServletDef> getServlets() {
        return this.servlets;
    }
    public void addServletMapping ( final String urlPattern, final String servletName ) {
        this.addServletMappingDecoded ( UDecoder.URLDecode ( urlPattern, this.getEncoding() ), servletName );
    }
    public void addServletMappingDecoded ( final String urlPattern, final String servletName ) {
        final String oldServletName = this.servletMappings.put ( urlPattern, servletName );
        if ( oldServletName != null ) {
            throw new IllegalArgumentException ( WebXml.sm.getString ( "webXml.duplicateServletMapping", oldServletName, servletName, urlPattern ) );
        }
        this.servletMappingNames.add ( servletName );
    }
    public Map<String, String> getServletMappings() {
        return this.servletMappings;
    }
    public void setSessionConfig ( final SessionConfig sessionConfig ) {
        this.sessionConfig = sessionConfig;
    }
    public SessionConfig getSessionConfig() {
        return this.sessionConfig;
    }
    public void addMimeMapping ( final String extension, final String mimeType ) {
        this.mimeMappings.put ( extension, mimeType );
    }
    public Map<String, String> getMimeMappings() {
        return this.mimeMappings;
    }
    public void setReplaceWelcomeFiles ( final boolean replaceWelcomeFiles ) {
        this.replaceWelcomeFiles = replaceWelcomeFiles;
    }
    public void setAlwaysAddWelcomeFiles ( final boolean alwaysAddWelcomeFiles ) {
        this.alwaysAddWelcomeFiles = alwaysAddWelcomeFiles;
    }
    public void addWelcomeFile ( final String welcomeFile ) {
        if ( this.replaceWelcomeFiles ) {
            this.welcomeFiles.clear();
            this.replaceWelcomeFiles = false;
        }
        this.welcomeFiles.add ( welcomeFile );
    }
    public Set<String> getWelcomeFiles() {
        return this.welcomeFiles;
    }
    public void addErrorPage ( final ErrorPage errorPage ) {
        this.errorPages.put ( errorPage.getName(), errorPage );
    }
    public Map<String, ErrorPage> getErrorPages() {
        return this.errorPages;
    }
    public void addTaglib ( final String uri, final String location ) {
        if ( this.taglibs.containsKey ( uri ) ) {
            throw new IllegalArgumentException ( WebXml.sm.getString ( "webXml.duplicateTaglibUri", uri ) );
        }
        this.taglibs.put ( uri, location );
    }
    public Map<String, String> getTaglibs() {
        return this.taglibs;
    }
    public void addJspPropertyGroup ( final JspPropertyGroup propertyGroup ) {
        propertyGroup.setEncoding ( this.getEncoding() );
        this.jspPropertyGroups.add ( propertyGroup );
    }
    public Set<JspPropertyGroup> getJspPropertyGroups() {
        return this.jspPropertyGroups;
    }
    public void addSecurityConstraint ( final SecurityConstraint securityConstraint ) {
        securityConstraint.setEncoding ( this.getEncoding() );
        this.securityConstraints.add ( securityConstraint );
    }
    public Set<SecurityConstraint> getSecurityConstraints() {
        return this.securityConstraints;
    }
    public void setLoginConfig ( final LoginConfig loginConfig ) {
        this.loginConfig = loginConfig;
    }
    public LoginConfig getLoginConfig() {
        return this.loginConfig;
    }
    public void addSecurityRole ( final String securityRole ) {
        this.securityRoles.add ( securityRole );
    }
    public Set<String> getSecurityRoles() {
        return this.securityRoles;
    }
    public void addEnvEntry ( final ContextEnvironment envEntry ) {
        if ( this.envEntries.containsKey ( envEntry.getName() ) ) {
            throw new IllegalArgumentException ( WebXml.sm.getString ( "webXml.duplicateEnvEntry", envEntry.getName() ) );
        }
        this.envEntries.put ( envEntry.getName(), envEntry );
    }
    public Map<String, ContextEnvironment> getEnvEntries() {
        return this.envEntries;
    }
    public void addEjbRef ( final ContextEjb ejbRef ) {
        this.ejbRefs.put ( ejbRef.getName(), ejbRef );
    }
    public Map<String, ContextEjb> getEjbRefs() {
        return this.ejbRefs;
    }
    public void addEjbLocalRef ( final ContextLocalEjb ejbLocalRef ) {
        this.ejbLocalRefs.put ( ejbLocalRef.getName(), ejbLocalRef );
    }
    public Map<String, ContextLocalEjb> getEjbLocalRefs() {
        return this.ejbLocalRefs;
    }
    public void addServiceRef ( final ContextService serviceRef ) {
        this.serviceRefs.put ( serviceRef.getName(), serviceRef );
    }
    public Map<String, ContextService> getServiceRefs() {
        return this.serviceRefs;
    }
    public void addResourceRef ( final ContextResource resourceRef ) {
        if ( this.resourceRefs.containsKey ( resourceRef.getName() ) ) {
            throw new IllegalArgumentException ( WebXml.sm.getString ( "webXml.duplicateResourceRef", resourceRef.getName() ) );
        }
        this.resourceRefs.put ( resourceRef.getName(), resourceRef );
    }
    public Map<String, ContextResource> getResourceRefs() {
        return this.resourceRefs;
    }
    public void addResourceEnvRef ( final ContextResourceEnvRef resourceEnvRef ) {
        if ( this.resourceEnvRefs.containsKey ( resourceEnvRef.getName() ) ) {
            throw new IllegalArgumentException ( WebXml.sm.getString ( "webXml.duplicateResourceEnvRef", resourceEnvRef.getName() ) );
        }
        this.resourceEnvRefs.put ( resourceEnvRef.getName(), resourceEnvRef );
    }
    public Map<String, ContextResourceEnvRef> getResourceEnvRefs() {
        return this.resourceEnvRefs;
    }
    public void addMessageDestinationRef ( final MessageDestinationRef messageDestinationRef ) {
        if ( this.messageDestinationRefs.containsKey ( messageDestinationRef.getName() ) ) {
            throw new IllegalArgumentException ( WebXml.sm.getString ( "webXml.duplicateMessageDestinationRef", messageDestinationRef.getName() ) );
        }
        this.messageDestinationRefs.put ( messageDestinationRef.getName(), messageDestinationRef );
    }
    public Map<String, MessageDestinationRef> getMessageDestinationRefs() {
        return this.messageDestinationRefs;
    }
    public void addMessageDestination ( final MessageDestination messageDestination ) {
        if ( this.messageDestinations.containsKey ( messageDestination.getName() ) ) {
            throw new IllegalArgumentException ( WebXml.sm.getString ( "webXml.duplicateMessageDestination", messageDestination.getName() ) );
        }
        this.messageDestinations.put ( messageDestination.getName(), messageDestination );
    }
    public Map<String, MessageDestination> getMessageDestinations() {
        return this.messageDestinations;
    }
    public void addLocaleEncodingMapping ( final String locale, final String encoding ) {
        this.localeEncodingMappings.put ( locale, encoding );
    }
    public Map<String, String> getLocaleEncodingMappings() {
        return this.localeEncodingMappings;
    }
    public void addPostConstructMethods ( final String clazz, final String method ) {
        if ( !this.postConstructMethods.containsKey ( clazz ) ) {
            this.postConstructMethods.put ( clazz, method );
        }
    }
    public Map<String, String> getPostConstructMethods() {
        return this.postConstructMethods;
    }
    public void addPreDestroyMethods ( final String clazz, final String method ) {
        if ( !this.preDestroyMethods.containsKey ( clazz ) ) {
            this.preDestroyMethods.put ( clazz, method );
        }
    }
    public Map<String, String> getPreDestroyMethods() {
        return this.preDestroyMethods;
    }
    public JspConfigDescriptor getJspConfigDescriptor() {
        if ( this.jspPropertyGroups.isEmpty() && this.taglibs.isEmpty() ) {
            return null;
        }
        final Collection<JspPropertyGroupDescriptor> descriptors = new ArrayList<JspPropertyGroupDescriptor> ( this.jspPropertyGroups.size() );
        for ( final JspPropertyGroup jspPropertyGroup : this.jspPropertyGroups ) {
            final JspPropertyGroupDescriptor descriptor = ( JspPropertyGroupDescriptor ) new JspPropertyGroupDescriptorImpl ( jspPropertyGroup );
            descriptors.add ( descriptor );
        }
        final Collection<TaglibDescriptor> tlds = new HashSet<TaglibDescriptor> ( this.taglibs.size() );
        for ( final Map.Entry<String, String> entry : this.taglibs.entrySet() ) {
            final TaglibDescriptor descriptor2 = ( TaglibDescriptor ) new TaglibDescriptorImpl ( entry.getValue(), entry.getKey() );
            tlds.add ( descriptor2 );
        }
        return ( JspConfigDescriptor ) new JspConfigDescriptorImpl ( descriptors, tlds );
    }
    public void setURL ( final URL url ) {
        this.uRL = url;
    }
    public URL getURL() {
        return this.uRL;
    }
    public void setJarName ( final String jarName ) {
        this.jarName = jarName;
    }
    public String getJarName() {
        return this.jarName;
    }
    public void setWebappJar ( final boolean webappJar ) {
        this.webappJar = webappJar;
    }
    public boolean getWebappJar() {
        return this.webappJar;
    }
    public boolean getDelegate() {
        return this.delegate;
    }
    public void setDelegate ( final boolean delegate ) {
        this.delegate = delegate;
    }
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder ( 32 );
        buf.append ( "Name: " );
        buf.append ( this.getName() );
        buf.append ( ", URL: " );
        buf.append ( this.getURL() );
        return buf.toString();
    }
    public String toXml() {
        final StringBuilder sb = new StringBuilder ( 2048 );
        sb.append ( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );
        if ( this.publicId != null ) {
            sb.append ( "<!DOCTYPE web-app PUBLIC\n" );
            sb.append ( "  \"" );
            sb.append ( this.publicId );
            sb.append ( "\"\n" );
            sb.append ( "  \"" );
            if ( "-//Sun Microsystems, Inc.//DTD Web Application 2.2//EN".equals ( this.publicId ) ) {
                sb.append ( "http://java.sun.com/dtd/web-app_2_2.dtd" );
            } else {
                sb.append ( "http://java.sun.com/dtd/web-app_2_3.dtd" );
            }
            sb.append ( "\">\n" );
            sb.append ( "<web-app>" );
        } else {
            String javaeeNamespace = null;
            String webXmlSchemaLocation = null;
            final String version = this.getVersion();
            if ( "2.4".equals ( version ) ) {
                javaeeNamespace = "http://java.sun.com/xml/ns/j2ee";
                webXmlSchemaLocation = "http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd";
            } else if ( "2.5".equals ( version ) ) {
                javaeeNamespace = "http://java.sun.com/xml/ns/javaee";
                webXmlSchemaLocation = "http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd";
            } else if ( "3.0".equals ( version ) ) {
                javaeeNamespace = "http://java.sun.com/xml/ns/javaee";
                webXmlSchemaLocation = "http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd";
            } else if ( "3.1".equals ( version ) ) {
                javaeeNamespace = "http://xmlns.jcp.org/xml/ns/javaee";
                webXmlSchemaLocation = "http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd";
            } else if ( "4.0".equals ( version ) ) {
                javaeeNamespace = "http://xmlns.jcp.org/xml/ns/javaee";
                webXmlSchemaLocation = "http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd";
            }
            sb.append ( "<web-app xmlns=\"" );
            sb.append ( javaeeNamespace );
            sb.append ( "\"\n" );
            sb.append ( "         xmlns:xsi=" );
            sb.append ( "\"http://www.w3.org/2001/XMLSchema-instance\"\n" );
            sb.append ( "         xsi:schemaLocation=\"" );
            sb.append ( javaeeNamespace );
            sb.append ( " " );
            sb.append ( webXmlSchemaLocation );
            sb.append ( "\"\n" );
            sb.append ( "         version=\"" );
            sb.append ( this.getVersion() );
            sb.append ( "\"" );
            if ( "2.4".equals ( version ) ) {
                sb.append ( ">\n\n" );
            } else {
                sb.append ( "\n         metadata-complete=\"true\">\n\n" );
            }
        }
        appendElement ( sb, "  ", "display-name", this.displayName );
        if ( this.isDistributable() ) {
            sb.append ( "  <distributable/>\n\n" );
        }
        for ( final Map.Entry<String, String> entry : this.contextParams.entrySet() ) {
            sb.append ( "  <context-param>\n" );
            appendElement ( sb, "    ", "param-name", entry.getKey() );
            appendElement ( sb, "    ", "param-value", entry.getValue() );
            sb.append ( "  </context-param>\n" );
        }
        sb.append ( '\n' );
        if ( this.getMajorVersion() > 2 || this.getMinorVersion() > 2 ) {
            for ( final Map.Entry<String, FilterDef> entry2 : this.filters.entrySet() ) {
                final FilterDef filterDef = entry2.getValue();
                sb.append ( "  <filter>\n" );
                appendElement ( sb, "    ", "description", filterDef.getDescription() );
                appendElement ( sb, "    ", "display-name", filterDef.getDisplayName() );
                appendElement ( sb, "    ", "filter-name", filterDef.getFilterName() );
                appendElement ( sb, "    ", "filter-class", filterDef.getFilterClass() );
                if ( this.getMajorVersion() != 2 ) {
                    appendElement ( sb, "    ", "async-supported", filterDef.getAsyncSupported() );
                }
                for ( final Map.Entry<String, String> param : filterDef.getParameterMap().entrySet() ) {
                    sb.append ( "    <init-param>\n" );
                    appendElement ( sb, "      ", "param-name", param.getKey() );
                    appendElement ( sb, "      ", "param-value", param.getValue() );
                    sb.append ( "    </init-param>\n" );
                }
                sb.append ( "  </filter>\n" );
            }
            sb.append ( '\n' );
            for ( final FilterMap filterMap : this.filterMaps ) {
                sb.append ( "  <filter-mapping>\n" );
                appendElement ( sb, "    ", "filter-name", filterMap.getFilterName() );
                if ( filterMap.getMatchAllServletNames() ) {
                    sb.append ( "    <servlet-name>*</servlet-name>\n" );
                } else {
                    for ( final String servletName : filterMap.getServletNames() ) {
                        appendElement ( sb, "    ", "servlet-name", servletName );
                    }
                }
                if ( filterMap.getMatchAllUrlPatterns() ) {
                    sb.append ( "    <url-pattern>*</url-pattern>\n" );
                } else {
                    for ( final String urlPattern : filterMap.getURLPatterns() ) {
                        appendElement ( sb, "    ", "url-pattern", this.encodeUrl ( urlPattern ) );
                    }
                }
                if ( this.getMajorVersion() > 2 || this.getMinorVersion() > 3 ) {
                    for ( final String dispatcher : filterMap.getDispatcherNames() ) {
                        if ( this.getMajorVersion() != 2 || !DispatcherType.ASYNC.name().equals ( dispatcher ) ) {
                            appendElement ( sb, "    ", "dispatcher", dispatcher );
                        }
                    }
                }
                sb.append ( "  </filter-mapping>\n" );
            }
            sb.append ( '\n' );
        }
        if ( this.getMajorVersion() > 2 || this.getMinorVersion() > 2 ) {
            for ( final String listener : this.listeners ) {
                sb.append ( "  <listener>\n" );
                appendElement ( sb, "    ", "listener-class", listener );
                sb.append ( "  </listener>\n" );
            }
            sb.append ( '\n' );
        }
        for ( final Map.Entry<String, ServletDef> entry3 : this.servlets.entrySet() ) {
            final ServletDef servletDef = entry3.getValue();
            sb.append ( "  <servlet>\n" );
            appendElement ( sb, "    ", "description", servletDef.getDescription() );
            appendElement ( sb, "    ", "display-name", servletDef.getDisplayName() );
            appendElement ( sb, "    ", "servlet-name", entry3.getKey() );
            appendElement ( sb, "    ", "servlet-class", servletDef.getServletClass() );
            appendElement ( sb, "    ", "jsp-file", servletDef.getJspFile() );
            for ( final Map.Entry<String, String> param : servletDef.getParameterMap().entrySet() ) {
                sb.append ( "    <init-param>\n" );
                appendElement ( sb, "      ", "param-name", param.getKey() );
                appendElement ( sb, "      ", "param-value", param.getValue() );
                sb.append ( "    </init-param>\n" );
            }
            appendElement ( sb, "    ", "load-on-startup", servletDef.getLoadOnStartup() );
            appendElement ( sb, "    ", "enabled", servletDef.getEnabled() );
            if ( this.getMajorVersion() != 2 ) {
                appendElement ( sb, "    ", "async-supported", servletDef.getAsyncSupported() );
            }
            if ( ( this.getMajorVersion() > 2 || this.getMinorVersion() > 2 ) && servletDef.getRunAs() != null ) {
                sb.append ( "    <run-as>\n" );
                appendElement ( sb, "      ", "role-name", servletDef.getRunAs() );
                sb.append ( "    </run-as>\n" );
            }
            for ( final SecurityRoleRef roleRef : servletDef.getSecurityRoleRefs() ) {
                sb.append ( "    <security-role-ref>\n" );
                appendElement ( sb, "      ", "role-name", roleRef.getName() );
                appendElement ( sb, "      ", "role-link", roleRef.getLink() );
                sb.append ( "    </security-role-ref>\n" );
            }
            if ( this.getMajorVersion() != 2 ) {
                final MultipartDef multipartDef = servletDef.getMultipartDef();
                if ( multipartDef != null ) {
                    sb.append ( "    <multipart-config>\n" );
                    appendElement ( sb, "      ", "location", multipartDef.getLocation() );
                    appendElement ( sb, "      ", "max-file-size", multipartDef.getMaxFileSize() );
                    appendElement ( sb, "      ", "max-request-size", multipartDef.getMaxRequestSize() );
                    appendElement ( sb, "      ", "file-size-threshold", multipartDef.getFileSizeThreshold() );
                    sb.append ( "    </multipart-config>\n" );
                }
            }
            sb.append ( "  </servlet>\n" );
        }
        sb.append ( '\n' );
        for ( final Map.Entry<String, String> entry : this.servletMappings.entrySet() ) {
            sb.append ( "  <servlet-mapping>\n" );
            appendElement ( sb, "    ", "servlet-name", entry.getValue() );
            appendElement ( sb, "    ", "url-pattern", this.encodeUrl ( entry.getKey() ) );
            sb.append ( "  </servlet-mapping>\n" );
        }
        sb.append ( '\n' );
        if ( this.sessionConfig != null ) {
            sb.append ( "  <session-config>\n" );
            appendElement ( sb, "    ", "session-timeout", this.sessionConfig.getSessionTimeout() );
            if ( this.majorVersion >= 3 ) {
                sb.append ( "    <cookie-config>\n" );
                appendElement ( sb, "      ", "name", this.sessionConfig.getCookieName() );
                appendElement ( sb, "      ", "domain", this.sessionConfig.getCookieDomain() );
                appendElement ( sb, "      ", "path", this.sessionConfig.getCookiePath() );
                appendElement ( sb, "      ", "comment", this.sessionConfig.getCookieComment() );
                appendElement ( sb, "      ", "http-only", this.sessionConfig.getCookieHttpOnly() );
                appendElement ( sb, "      ", "secure", this.sessionConfig.getCookieSecure() );
                appendElement ( sb, "      ", "max-age", this.sessionConfig.getCookieMaxAge() );
                sb.append ( "    </cookie-config>\n" );
                for ( final SessionTrackingMode stm : this.sessionConfig.getSessionTrackingModes() ) {
                    appendElement ( sb, "    ", "tracking-mode", stm.name() );
                }
            }
            sb.append ( "  </session-config>\n\n" );
        }
        for ( final Map.Entry<String, String> entry : this.mimeMappings.entrySet() ) {
            sb.append ( "  <mime-mapping>\n" );
            appendElement ( sb, "    ", "extension", entry.getKey() );
            appendElement ( sb, "    ", "mime-type", entry.getValue() );
            sb.append ( "  </mime-mapping>\n" );
        }
        sb.append ( '\n' );
        if ( this.welcomeFiles.size() > 0 ) {
            sb.append ( "  <welcome-file-list>\n" );
            for ( final String welcomeFile : this.welcomeFiles ) {
                appendElement ( sb, "    ", "welcome-file", welcomeFile );
            }
            sb.append ( "  </welcome-file-list>\n\n" );
        }
        for ( final ErrorPage errorPage : this.errorPages.values() ) {
            final String exeptionType = errorPage.getExceptionType();
            final int errorCode = errorPage.getErrorCode();
            if ( exeptionType == null && errorCode == 0 && this.getMajorVersion() == 2 ) {
                continue;
            }
            sb.append ( "  <error-page>\n" );
            if ( errorPage.getExceptionType() != null ) {
                appendElement ( sb, "    ", "exception-type", exeptionType );
            } else if ( errorPage.getErrorCode() > 0 ) {
                appendElement ( sb, "    ", "error-code", Integer.toString ( errorCode ) );
            }
            appendElement ( sb, "    ", "location", errorPage.getLocation() );
            sb.append ( "  </error-page>\n" );
        }
        sb.append ( '\n' );
        if ( this.taglibs.size() > 0 || this.jspPropertyGroups.size() > 0 ) {
            if ( this.getMajorVersion() > 2 || this.getMinorVersion() > 3 ) {
                sb.append ( "  <jsp-config>\n" );
            }
            for ( final Map.Entry<String, String> entry : this.taglibs.entrySet() ) {
                sb.append ( "    <taglib>\n" );
                appendElement ( sb, "      ", "taglib-uri", entry.getKey() );
                appendElement ( sb, "      ", "taglib-location", entry.getValue() );
                sb.append ( "    </taglib>\n" );
            }
            if ( this.getMajorVersion() > 2 || this.getMinorVersion() > 3 ) {
                for ( final JspPropertyGroup jpg : this.jspPropertyGroups ) {
                    sb.append ( "    <jsp-property-group>\n" );
                    for ( final String urlPattern2 : jpg.getUrlPatterns() ) {
                        appendElement ( sb, "      ", "url-pattern", this.encodeUrl ( urlPattern2 ) );
                    }
                    appendElement ( sb, "      ", "el-ignored", jpg.getElIgnored() );
                    appendElement ( sb, "      ", "page-encoding", jpg.getPageEncoding() );
                    appendElement ( sb, "      ", "scripting-invalid", jpg.getScriptingInvalid() );
                    appendElement ( sb, "      ", "is-xml", jpg.getIsXml() );
                    for ( final String prelude : jpg.getIncludePreludes() ) {
                        appendElement ( sb, "      ", "include-prelude", prelude );
                    }
                    for ( final String coda : jpg.getIncludeCodas() ) {
                        appendElement ( sb, "      ", "include-coda", coda );
                    }
                    appendElement ( sb, "      ", "deferred-syntax-allowed-as-literal", jpg.getDeferredSyntax() );
                    appendElement ( sb, "      ", "trim-directive-whitespaces", jpg.getTrimWhitespace() );
                    appendElement ( sb, "      ", "default-content-type", jpg.getDefaultContentType() );
                    appendElement ( sb, "      ", "buffer", jpg.getBuffer() );
                    appendElement ( sb, "      ", "error-on-undeclared-namespace", jpg.getErrorOnUndeclaredNamespace() );
                    sb.append ( "    </jsp-property-group>\n" );
                }
                sb.append ( "  </jsp-config>\n\n" );
            }
        }
        if ( this.getMajorVersion() > 2 || this.getMinorVersion() > 2 ) {
            for ( final ContextResourceEnvRef resourceEnvRef : this.resourceEnvRefs.values() ) {
                sb.append ( "  <resource-env-ref>\n" );
                appendElement ( sb, "    ", "description", resourceEnvRef.getDescription() );
                appendElement ( sb, "    ", "resource-env-ref-name", resourceEnvRef.getName() );
                appendElement ( sb, "    ", "resource-env-ref-type", resourceEnvRef.getType() );
                for ( final InjectionTarget target : resourceEnvRef.getInjectionTargets() ) {
                    sb.append ( "    <injection-target>\n" );
                    appendElement ( sb, "      ", "injection-target-class", target.getTargetClass() );
                    appendElement ( sb, "      ", "injection-target-name", target.getTargetName() );
                    sb.append ( "    </injection-target>\n" );
                }
                sb.append ( "  </resource-env-ref>\n" );
            }
            sb.append ( '\n' );
        }
        for ( final ContextResource resourceRef : this.resourceRefs.values() ) {
            sb.append ( "  <resource-ref>\n" );
            appendElement ( sb, "    ", "description", resourceRef.getDescription() );
            appendElement ( sb, "    ", "res-ref-name", resourceRef.getName() );
            appendElement ( sb, "    ", "res-type", resourceRef.getType() );
            appendElement ( sb, "    ", "res-auth", resourceRef.getAuth() );
            if ( this.getMajorVersion() > 2 || this.getMinorVersion() > 2 ) {
                appendElement ( sb, "    ", "res-sharing-scope", resourceRef.getScope() );
            }
            for ( final InjectionTarget target : resourceRef.getInjectionTargets() ) {
                sb.append ( "    <injection-target>\n" );
                appendElement ( sb, "      ", "injection-target-class", target.getTargetClass() );
                appendElement ( sb, "      ", "injection-target-name", target.getTargetName() );
                sb.append ( "    </injection-target>\n" );
            }
            sb.append ( "  </resource-ref>\n" );
        }
        sb.append ( '\n' );
        for ( final SecurityConstraint constraint : this.securityConstraints ) {
            sb.append ( "  <security-constraint>\n" );
            if ( this.getMajorVersion() > 2 || this.getMinorVersion() > 2 ) {
                appendElement ( sb, "    ", "display-name", constraint.getDisplayName() );
            }
            for ( final SecurityCollection collection : constraint.findCollections() ) {
                sb.append ( "    <web-resource-collection>\n" );
                appendElement ( sb, "      ", "web-resource-name", collection.getName() );
                appendElement ( sb, "      ", "description", collection.getDescription() );
                for ( final String urlPattern3 : collection.findPatterns() ) {
                    appendElement ( sb, "      ", "url-pattern", this.encodeUrl ( urlPattern3 ) );
                }
                for ( final String method : collection.findMethods() ) {
                    appendElement ( sb, "      ", "http-method", method );
                }
                for ( final String method : collection.findOmittedMethods() ) {
                    appendElement ( sb, "      ", "http-method-omission", method );
                }
                sb.append ( "    </web-resource-collection>\n" );
            }
            if ( constraint.findAuthRoles().length > 0 ) {
                sb.append ( "    <auth-constraint>\n" );
                for ( final String role : constraint.findAuthRoles() ) {
                    appendElement ( sb, "      ", "role-name", role );
                }
                sb.append ( "    </auth-constraint>\n" );
            }
            if ( constraint.getUserConstraint() != null ) {
                sb.append ( "    <user-data-constraint>\n" );
                appendElement ( sb, "      ", "transport-guarantee", constraint.getUserConstraint() );
                sb.append ( "    </user-data-constraint>\n" );
            }
            sb.append ( "  </security-constraint>\n" );
        }
        sb.append ( '\n' );
        if ( this.loginConfig != null ) {
            sb.append ( "  <login-config>\n" );
            appendElement ( sb, "    ", "auth-method", this.loginConfig.getAuthMethod() );
            appendElement ( sb, "    ", "realm-name", this.loginConfig.getRealmName() );
            if ( this.loginConfig.getErrorPage() != null || this.loginConfig.getLoginPage() != null ) {
                sb.append ( "    <form-login-config>\n" );
                appendElement ( sb, "      ", "form-login-page", this.loginConfig.getLoginPage() );
                appendElement ( sb, "      ", "form-error-page", this.loginConfig.getErrorPage() );
                sb.append ( "    </form-login-config>\n" );
            }
            sb.append ( "  </login-config>\n\n" );
        }
        for ( final String roleName : this.securityRoles ) {
            sb.append ( "  <security-role>\n" );
            appendElement ( sb, "    ", "role-name", roleName );
            sb.append ( "  </security-role>\n" );
        }
        for ( final ContextEnvironment envEntry : this.envEntries.values() ) {
            sb.append ( "  <env-entry>\n" );
            appendElement ( sb, "    ", "description", envEntry.getDescription() );
            appendElement ( sb, "    ", "env-entry-name", envEntry.getName() );
            appendElement ( sb, "    ", "env-entry-type", envEntry.getType() );
            appendElement ( sb, "    ", "env-entry-value", envEntry.getValue() );
            for ( final InjectionTarget target : envEntry.getInjectionTargets() ) {
                sb.append ( "    <injection-target>\n" );
                appendElement ( sb, "      ", "injection-target-class", target.getTargetClass() );
                appendElement ( sb, "      ", "injection-target-name", target.getTargetName() );
                sb.append ( "    </injection-target>\n" );
            }
            sb.append ( "  </env-entry>\n" );
        }
        sb.append ( '\n' );
        for ( final ContextEjb ejbRef : this.ejbRefs.values() ) {
            sb.append ( "  <ejb-ref>\n" );
            appendElement ( sb, "    ", "description", ejbRef.getDescription() );
            appendElement ( sb, "    ", "ejb-ref-name", ejbRef.getName() );
            appendElement ( sb, "    ", "ejb-ref-type", ejbRef.getType() );
            appendElement ( sb, "    ", "home", ejbRef.getHome() );
            appendElement ( sb, "    ", "remote", ejbRef.getRemote() );
            appendElement ( sb, "    ", "ejb-link", ejbRef.getLink() );
            for ( final InjectionTarget target : ejbRef.getInjectionTargets() ) {
                sb.append ( "    <injection-target>\n" );
                appendElement ( sb, "      ", "injection-target-class", target.getTargetClass() );
                appendElement ( sb, "      ", "injection-target-name", target.getTargetName() );
                sb.append ( "    </injection-target>\n" );
            }
            sb.append ( "  </ejb-ref>\n" );
        }
        sb.append ( '\n' );
        if ( this.getMajorVersion() > 2 || this.getMinorVersion() > 2 ) {
            for ( final ContextLocalEjb ejbLocalRef : this.ejbLocalRefs.values() ) {
                sb.append ( "  <ejb-local-ref>\n" );
                appendElement ( sb, "    ", "description", ejbLocalRef.getDescription() );
                appendElement ( sb, "    ", "ejb-ref-name", ejbLocalRef.getName() );
                appendElement ( sb, "    ", "ejb-ref-type", ejbLocalRef.getType() );
                appendElement ( sb, "    ", "local-home", ejbLocalRef.getHome() );
                appendElement ( sb, "    ", "local", ejbLocalRef.getLocal() );
                appendElement ( sb, "    ", "ejb-link", ejbLocalRef.getLink() );
                for ( final InjectionTarget target : ejbLocalRef.getInjectionTargets() ) {
                    sb.append ( "    <injection-target>\n" );
                    appendElement ( sb, "      ", "injection-target-class", target.getTargetClass() );
                    appendElement ( sb, "      ", "injection-target-name", target.getTargetName() );
                    sb.append ( "    </injection-target>\n" );
                }
                sb.append ( "  </ejb-local-ref>\n" );
            }
            sb.append ( '\n' );
        }
        if ( this.getMajorVersion() > 2 || this.getMinorVersion() > 3 ) {
            for ( final ContextService serviceRef : this.serviceRefs.values() ) {
                sb.append ( "  <service-ref>\n" );
                appendElement ( sb, "    ", "description", serviceRef.getDescription() );
                appendElement ( sb, "    ", "display-name", serviceRef.getDisplayname() );
                appendElement ( sb, "    ", "service-ref-name", serviceRef.getName() );
                appendElement ( sb, "    ", "service-interface", serviceRef.getInterface() );
                appendElement ( sb, "    ", "service-ref-type", serviceRef.getType() );
                appendElement ( sb, "    ", "wsdl-file", serviceRef.getWsdlfile() );
                appendElement ( sb, "    ", "jaxrpc-mapping-file", serviceRef.getJaxrpcmappingfile() );
                String qname = serviceRef.getServiceqnameNamespaceURI();
                if ( qname != null ) {
                    qname += ":";
                }
                qname += serviceRef.getServiceqnameLocalpart();
                appendElement ( sb, "    ", "service-qname", qname );
                final Iterator<String> endpointIter = serviceRef.getServiceendpoints();
                while ( endpointIter.hasNext() ) {
                    final String endpoint = endpointIter.next();
                    sb.append ( "    <port-component-ref>\n" );
                    appendElement ( sb, "      ", "service-endpoint-interface", endpoint );
                    appendElement ( sb, "      ", "port-component-link", serviceRef.getProperty ( endpoint ) );
                    sb.append ( "    </port-component-ref>\n" );
                }
                final Iterator<String> handlerIter = serviceRef.getHandlers();
                while ( handlerIter.hasNext() ) {
                    final String handler = handlerIter.next();
                    sb.append ( "    <handler>\n" );
                    final ContextHandler ch = serviceRef.getHandler ( handler );
                    appendElement ( sb, "      ", "handler-name", ch.getName() );
                    appendElement ( sb, "      ", "handler-class", ch.getHandlerclass() );
                    sb.append ( "    </handler>\n" );
                }
                for ( final InjectionTarget target2 : serviceRef.getInjectionTargets() ) {
                    sb.append ( "    <injection-target>\n" );
                    appendElement ( sb, "      ", "injection-target-class", target2.getTargetClass() );
                    appendElement ( sb, "      ", "injection-target-name", target2.getTargetName() );
                    sb.append ( "    </injection-target>\n" );
                }
                sb.append ( "  </service-ref>\n" );
            }
            sb.append ( '\n' );
        }
        if ( !this.postConstructMethods.isEmpty() ) {
            for ( final Map.Entry<String, String> entry : this.postConstructMethods.entrySet() ) {
                sb.append ( "  <post-construct>\n" );
                appendElement ( sb, "    ", "lifecycle-callback-class", entry.getKey() );
                appendElement ( sb, "    ", "lifecycle-callback-method", entry.getValue() );
                sb.append ( "  </post-construct>\n" );
            }
            sb.append ( '\n' );
        }
        if ( !this.preDestroyMethods.isEmpty() ) {
            for ( final Map.Entry<String, String> entry : this.preDestroyMethods.entrySet() ) {
                sb.append ( "  <pre-destroy>\n" );
                appendElement ( sb, "    ", "lifecycle-callback-class", entry.getKey() );
                appendElement ( sb, "    ", "lifecycle-callback-method", entry.getValue() );
                sb.append ( "  </pre-destroy>\n" );
            }
            sb.append ( '\n' );
        }
        if ( this.getMajorVersion() > 2 || this.getMinorVersion() > 3 ) {
            for ( final MessageDestinationRef mdr : this.messageDestinationRefs.values() ) {
                sb.append ( "  <message-destination-ref>\n" );
                appendElement ( sb, "    ", "description", mdr.getDescription() );
                appendElement ( sb, "    ", "message-destination-ref-name", mdr.getName() );
                appendElement ( sb, "    ", "message-destination-type", mdr.getType() );
                appendElement ( sb, "    ", "message-destination-usage", mdr.getUsage() );
                appendElement ( sb, "    ", "message-destination-link", mdr.getLink() );
                for ( final InjectionTarget target : mdr.getInjectionTargets() ) {
                    sb.append ( "    <injection-target>\n" );
                    appendElement ( sb, "      ", "injection-target-class", target.getTargetClass() );
                    appendElement ( sb, "      ", "injection-target-name", target.getTargetName() );
                    sb.append ( "    </injection-target>\n" );
                }
                sb.append ( "  </message-destination-ref>\n" );
            }
            sb.append ( '\n' );
            for ( final MessageDestination md : this.messageDestinations.values() ) {
                sb.append ( "  <message-destination>\n" );
                appendElement ( sb, "    ", "description", md.getDescription() );
                appendElement ( sb, "    ", "display-name", md.getDisplayName() );
                appendElement ( sb, "    ", "message-destination-name", md.getName() );
                sb.append ( "  </message-destination>\n" );
            }
            sb.append ( '\n' );
        }
        if ( ( this.getMajorVersion() > 2 || this.getMinorVersion() > 3 ) && this.localeEncodingMappings.size() > 0 ) {
            sb.append ( "  <locale-encoding-mapping-list>\n" );
            for ( final Map.Entry<String, String> entry : this.localeEncodingMappings.entrySet() ) {
                sb.append ( "    <locale-encoding-mapping>\n" );
                appendElement ( sb, "      ", "locale", entry.getKey() );
                appendElement ( sb, "      ", "encoding", entry.getValue() );
                sb.append ( "    </locale-encoding-mapping>\n" );
            }
            sb.append ( "  </locale-encoding-mapping-list>\n" );
        }
        if ( ( this.getMajorVersion() > 3 || ( this.getMajorVersion() == 3 && this.getMinorVersion() > 0 ) ) && this.denyUncoveredHttpMethods ) {
            sb.append ( "\n" );
            sb.append ( "  <deny-uncovered-http-methods/>" );
        }
        sb.append ( "</web-app>" );
        return sb.toString();
    }
    private String encodeUrl ( final String input ) {
        try {
            return URLEncoder.encode ( input, "UTF-8" );
        } catch ( UnsupportedEncodingException e ) {
            return null;
        }
    }
    private static void appendElement ( final StringBuilder sb, final String indent, final String elementName, final String value ) {
        if ( value == null ) {
            return;
        }
        if ( value.length() == 0 ) {
            sb.append ( indent );
            sb.append ( '<' );
            sb.append ( elementName );
            sb.append ( "/>\n" );
        } else {
            sb.append ( indent );
            sb.append ( '<' );
            sb.append ( elementName );
            sb.append ( '>' );
            sb.append ( escapeXml ( value ) );
            sb.append ( "</" );
            sb.append ( elementName );
            sb.append ( ">\n" );
        }
    }
    private static void appendElement ( final StringBuilder sb, final String indent, final String elementName, final Object value ) {
        if ( value == null ) {
            return;
        }
        appendElement ( sb, indent, elementName, value.toString() );
    }
    private static String escapeXml ( final String s ) {
        if ( s == null ) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < s.length(); ++i ) {
            final char c = s.charAt ( i );
            if ( c == '<' ) {
                sb.append ( "&lt;" );
            } else if ( c == '>' ) {
                sb.append ( "&gt;" );
            } else if ( c == '\'' ) {
                sb.append ( "&apos;" );
            } else if ( c == '&' ) {
                sb.append ( "&amp;" );
            } else if ( c == '\"' ) {
                sb.append ( "&quot;" );
            } else {
                sb.append ( c );
            }
        }
        return sb.toString();
    }
    public boolean merge ( final Set<WebXml> fragments ) {
        final WebXml temp = new WebXml();
        for ( final WebXml fragment : fragments ) {
            if ( !mergeMap ( fragment.getContextParams(), this.contextParams, temp.getContextParams(), fragment, "Context Parameter" ) ) {
                return false;
            }
        }
        this.contextParams.putAll ( temp.getContextParams() );
        if ( this.displayName == null ) {
            for ( final WebXml fragment : fragments ) {
                final String value = fragment.getDisplayName();
                if ( value != null ) {
                    if ( temp.getDisplayName() != null ) {
                        WebXml.log.error ( WebXml.sm.getString ( "webXml.mergeConflictDisplayName", fragment.getName(), fragment.getURL() ) );
                        return false;
                    }
                    temp.setDisplayName ( value );
                }
            }
            this.displayName = temp.getDisplayName();
        }
        if ( !this.denyUncoveredHttpMethods ) {
            for ( final WebXml fragment : fragments ) {
                if ( fragment.getDenyUncoveredHttpMethods() ) {
                    this.denyUncoveredHttpMethods = true;
                    break;
                }
            }
        }
        if ( this.distributable ) {
            for ( final WebXml fragment : fragments ) {
                if ( !fragment.isDistributable() ) {
                    this.distributable = false;
                    break;
                }
            }
        }
        for ( final WebXml fragment : fragments ) {
            if ( !mergeResourceMap ( fragment.getEjbLocalRefs(), this.ejbLocalRefs, temp.getEjbLocalRefs(), fragment ) ) {
                return false;
            }
        }
        this.ejbLocalRefs.putAll ( temp.getEjbLocalRefs() );
        for ( final WebXml fragment : fragments ) {
            if ( !mergeResourceMap ( fragment.getEjbRefs(), this.ejbRefs, temp.getEjbRefs(), fragment ) ) {
                return false;
            }
        }
        this.ejbRefs.putAll ( temp.getEjbRefs() );
        for ( final WebXml fragment : fragments ) {
            if ( !mergeResourceMap ( fragment.getEnvEntries(), this.envEntries, temp.getEnvEntries(), fragment ) ) {
                return false;
            }
        }
        this.envEntries.putAll ( temp.getEnvEntries() );
        for ( final WebXml fragment : fragments ) {
            if ( !mergeMap ( fragment.getErrorPages(), this.errorPages, temp.getErrorPages(), fragment, "Error Page" ) ) {
                return false;
            }
        }
        this.errorPages.putAll ( temp.getErrorPages() );
        final List<FilterMap> filterMapsToAdd = new ArrayList<FilterMap>();
        for ( final WebXml fragment2 : fragments ) {
            for ( final FilterMap filterMap : fragment2.getFilterMappings() ) {
                if ( !this.filterMappingNames.contains ( filterMap.getFilterName() ) ) {
                    filterMapsToAdd.add ( filterMap );
                }
            }
        }
        for ( final FilterMap filterMap2 : filterMapsToAdd ) {
            this.addFilterMapping ( filterMap2 );
        }
        for ( final WebXml fragment2 : fragments ) {
            for ( final Map.Entry<String, FilterDef> entry : fragment2.getFilters().entrySet() ) {
                if ( this.filters.containsKey ( entry.getKey() ) ) {
                    mergeFilter ( entry.getValue(), this.filters.get ( entry.getKey() ), false );
                } else if ( temp.getFilters().containsKey ( entry.getKey() ) ) {
                    if ( !mergeFilter ( entry.getValue(), temp.getFilters().get ( entry.getKey() ), true ) ) {
                        WebXml.log.error ( WebXml.sm.getString ( "webXml.mergeConflictFilter", entry.getKey(), fragment2.getName(), fragment2.getURL() ) );
                        return false;
                    }
                    continue;
                } else {
                    temp.getFilters().put ( entry.getKey(), entry.getValue() );
                }
            }
        }
        this.filters.putAll ( temp.getFilters() );
        for ( final WebXml fragment2 : fragments ) {
            for ( final JspPropertyGroup jspPropertyGroup : fragment2.getJspPropertyGroups() ) {
                this.addJspPropertyGroup ( jspPropertyGroup );
            }
        }
        for ( final WebXml fragment2 : fragments ) {
            for ( final String listener : fragment2.getListeners() ) {
                this.addListener ( listener );
            }
        }
        for ( final WebXml fragment2 : fragments ) {
            if ( !mergeMap ( fragment2.getLocaleEncodingMappings(), this.localeEncodingMappings, temp.getLocaleEncodingMappings(), fragment2, "Locale Encoding Mapping" ) ) {
                return false;
            }
        }
        this.localeEncodingMappings.putAll ( temp.getLocaleEncodingMappings() );
        if ( this.getLoginConfig() == null ) {
            LoginConfig tempLoginConfig = null;
            for ( final WebXml fragment3 : fragments ) {
                final LoginConfig fragmentLoginConfig = fragment3.loginConfig;
                if ( fragmentLoginConfig != null ) {
                    if ( tempLoginConfig == null || fragmentLoginConfig.equals ( tempLoginConfig ) ) {
                        tempLoginConfig = fragmentLoginConfig;
                    } else {
                        WebXml.log.error ( WebXml.sm.getString ( "webXml.mergeConflictLoginConfig", fragment3.getName(), fragment3.getURL() ) );
                    }
                }
            }
            this.loginConfig = tempLoginConfig;
        }
        for ( final WebXml fragment2 : fragments ) {
            if ( !mergeResourceMap ( fragment2.getMessageDestinationRefs(), this.messageDestinationRefs, temp.getMessageDestinationRefs(), fragment2 ) ) {
                return false;
            }
        }
        this.messageDestinationRefs.putAll ( temp.getMessageDestinationRefs() );
        for ( final WebXml fragment2 : fragments ) {
            if ( !mergeResourceMap ( fragment2.getMessageDestinations(), this.messageDestinations, temp.getMessageDestinations(), fragment2 ) ) {
                return false;
            }
        }
        this.messageDestinations.putAll ( temp.getMessageDestinations() );
        for ( final WebXml fragment2 : fragments ) {
            if ( !mergeMap ( fragment2.getMimeMappings(), this.mimeMappings, temp.getMimeMappings(), fragment2, "Mime Mapping" ) ) {
                return false;
            }
        }
        this.mimeMappings.putAll ( temp.getMimeMappings() );
        for ( final WebXml fragment2 : fragments ) {
            if ( !mergeResourceMap ( fragment2.getResourceEnvRefs(), this.resourceEnvRefs, temp.getResourceEnvRefs(), fragment2 ) ) {
                return false;
            }
        }
        this.resourceEnvRefs.putAll ( temp.getResourceEnvRefs() );
        for ( final WebXml fragment2 : fragments ) {
            if ( !mergeResourceMap ( fragment2.getResourceRefs(), this.resourceRefs, temp.getResourceRefs(), fragment2 ) ) {
                return false;
            }
        }
        this.resourceRefs.putAll ( temp.getResourceRefs() );
        for ( final WebXml fragment2 : fragments ) {
            for ( final SecurityConstraint constraint : fragment2.getSecurityConstraints() ) {
                this.addSecurityConstraint ( constraint );
            }
        }
        for ( final WebXml fragment2 : fragments ) {
            for ( final String role : fragment2.getSecurityRoles() ) {
                this.addSecurityRole ( role );
            }
        }
        for ( final WebXml fragment2 : fragments ) {
            if ( !mergeResourceMap ( fragment2.getServiceRefs(), this.serviceRefs, temp.getServiceRefs(), fragment2 ) ) {
                return false;
            }
        }
        this.serviceRefs.putAll ( temp.getServiceRefs() );
        final List<Map.Entry<String, String>> servletMappingsToAdd = new ArrayList<Map.Entry<String, String>>();
        for ( final WebXml fragment3 : fragments ) {
            for ( final Map.Entry<String, String> servletMap : fragment3.getServletMappings().entrySet() ) {
                if ( !this.servletMappingNames.contains ( servletMap.getValue() ) && !this.servletMappings.containsKey ( servletMap.getKey() ) ) {
                    servletMappingsToAdd.add ( servletMap );
                }
            }
        }
        for ( final Map.Entry<String, String> mapping : servletMappingsToAdd ) {
            this.addServletMappingDecoded ( mapping.getKey(), mapping.getValue() );
        }
        for ( final WebXml fragment3 : fragments ) {
            for ( final Map.Entry<String, ServletDef> entry2 : fragment3.getServlets().entrySet() ) {
                if ( this.servlets.containsKey ( entry2.getKey() ) ) {
                    mergeServlet ( entry2.getValue(), this.servlets.get ( entry2.getKey() ), false );
                } else if ( temp.getServlets().containsKey ( entry2.getKey() ) ) {
                    if ( !mergeServlet ( entry2.getValue(), temp.getServlets().get ( entry2.getKey() ), true ) ) {
                        WebXml.log.error ( WebXml.sm.getString ( "webXml.mergeConflictServlet", entry2.getKey(), fragment3.getName(), fragment3.getURL() ) );
                        return false;
                    }
                    continue;
                } else {
                    temp.getServlets().put ( entry2.getKey(), entry2.getValue() );
                }
            }
        }
        this.servlets.putAll ( temp.getServlets() );
        if ( this.sessionConfig.getSessionTimeout() == null ) {
            for ( final WebXml fragment3 : fragments ) {
                final Integer value2 = fragment3.getSessionConfig().getSessionTimeout();
                if ( value2 != null ) {
                    if ( temp.getSessionConfig().getSessionTimeout() == null ) {
                        temp.getSessionConfig().setSessionTimeout ( value2.toString() );
                    } else {
                        if ( value2.equals ( temp.getSessionConfig().getSessionTimeout() ) ) {
                            continue;
                        }
                        WebXml.log.error ( WebXml.sm.getString ( "webXml.mergeConflictSessionTimeout", fragment3.getName(), fragment3.getURL() ) );
                        return false;
                    }
                }
            }
            if ( temp.getSessionConfig().getSessionTimeout() != null ) {
                this.sessionConfig.setSessionTimeout ( temp.getSessionConfig().getSessionTimeout().toString() );
            }
        }
        if ( this.sessionConfig.getCookieName() == null ) {
            for ( final WebXml fragment3 : fragments ) {
                final String value3 = fragment3.getSessionConfig().getCookieName();
                if ( value3 != null ) {
                    if ( temp.getSessionConfig().getCookieName() == null ) {
                        temp.getSessionConfig().setCookieName ( value3 );
                    } else {
                        if ( value3.equals ( temp.getSessionConfig().getCookieName() ) ) {
                            continue;
                        }
                        WebXml.log.error ( WebXml.sm.getString ( "webXml.mergeConflictSessionCookieName", fragment3.getName(), fragment3.getURL() ) );
                        return false;
                    }
                }
            }
            this.sessionConfig.setCookieName ( temp.getSessionConfig().getCookieName() );
        }
        if ( this.sessionConfig.getCookieDomain() == null ) {
            for ( final WebXml fragment3 : fragments ) {
                final String value3 = fragment3.getSessionConfig().getCookieDomain();
                if ( value3 != null ) {
                    if ( temp.getSessionConfig().getCookieDomain() == null ) {
                        temp.getSessionConfig().setCookieDomain ( value3 );
                    } else {
                        if ( value3.equals ( temp.getSessionConfig().getCookieDomain() ) ) {
                            continue;
                        }
                        WebXml.log.error ( WebXml.sm.getString ( "webXml.mergeConflictSessionCookieDomain", fragment3.getName(), fragment3.getURL() ) );
                        return false;
                    }
                }
            }
            this.sessionConfig.setCookieDomain ( temp.getSessionConfig().getCookieDomain() );
        }
        if ( this.sessionConfig.getCookiePath() == null ) {
            for ( final WebXml fragment3 : fragments ) {
                final String value3 = fragment3.getSessionConfig().getCookiePath();
                if ( value3 != null ) {
                    if ( temp.getSessionConfig().getCookiePath() == null ) {
                        temp.getSessionConfig().setCookiePath ( value3 );
                    } else {
                        if ( value3.equals ( temp.getSessionConfig().getCookiePath() ) ) {
                            continue;
                        }
                        WebXml.log.error ( WebXml.sm.getString ( "webXml.mergeConflictSessionCookiePath", fragment3.getName(), fragment3.getURL() ) );
                        return false;
                    }
                }
            }
            this.sessionConfig.setCookiePath ( temp.getSessionConfig().getCookiePath() );
        }
        if ( this.sessionConfig.getCookieComment() == null ) {
            for ( final WebXml fragment3 : fragments ) {
                final String value3 = fragment3.getSessionConfig().getCookieComment();
                if ( value3 != null ) {
                    if ( temp.getSessionConfig().getCookieComment() == null ) {
                        temp.getSessionConfig().setCookieComment ( value3 );
                    } else {
                        if ( value3.equals ( temp.getSessionConfig().getCookieComment() ) ) {
                            continue;
                        }
                        WebXml.log.error ( WebXml.sm.getString ( "webXml.mergeConflictSessionCookieComment", fragment3.getName(), fragment3.getURL() ) );
                        return false;
                    }
                }
            }
            this.sessionConfig.setCookieComment ( temp.getSessionConfig().getCookieComment() );
        }
        if ( this.sessionConfig.getCookieHttpOnly() == null ) {
            for ( final WebXml fragment3 : fragments ) {
                final Boolean value4 = fragment3.getSessionConfig().getCookieHttpOnly();
                if ( value4 != null ) {
                    if ( temp.getSessionConfig().getCookieHttpOnly() == null ) {
                        temp.getSessionConfig().setCookieHttpOnly ( value4.toString() );
                    } else {
                        if ( value4.equals ( temp.getSessionConfig().getCookieHttpOnly() ) ) {
                            continue;
                        }
                        WebXml.log.error ( WebXml.sm.getString ( "webXml.mergeConflictSessionCookieHttpOnly", fragment3.getName(), fragment3.getURL() ) );
                        return false;
                    }
                }
            }
            if ( temp.getSessionConfig().getCookieHttpOnly() != null ) {
                this.sessionConfig.setCookieHttpOnly ( temp.getSessionConfig().getCookieHttpOnly().toString() );
            }
        }
        if ( this.sessionConfig.getCookieSecure() == null ) {
            for ( final WebXml fragment3 : fragments ) {
                final Boolean value4 = fragment3.getSessionConfig().getCookieSecure();
                if ( value4 != null ) {
                    if ( temp.getSessionConfig().getCookieSecure() == null ) {
                        temp.getSessionConfig().setCookieSecure ( value4.toString() );
                    } else {
                        if ( value4.equals ( temp.getSessionConfig().getCookieSecure() ) ) {
                            continue;
                        }
                        WebXml.log.error ( WebXml.sm.getString ( "webXml.mergeConflictSessionCookieSecure", fragment3.getName(), fragment3.getURL() ) );
                        return false;
                    }
                }
            }
            if ( temp.getSessionConfig().getCookieSecure() != null ) {
                this.sessionConfig.setCookieSecure ( temp.getSessionConfig().getCookieSecure().toString() );
            }
        }
        if ( this.sessionConfig.getCookieMaxAge() == null ) {
            for ( final WebXml fragment3 : fragments ) {
                final Integer value2 = fragment3.getSessionConfig().getCookieMaxAge();
                if ( value2 != null ) {
                    if ( temp.getSessionConfig().getCookieMaxAge() == null ) {
                        temp.getSessionConfig().setCookieMaxAge ( value2.toString() );
                    } else {
                        if ( value2.equals ( temp.getSessionConfig().getCookieMaxAge() ) ) {
                            continue;
                        }
                        WebXml.log.error ( WebXml.sm.getString ( "webXml.mergeConflictSessionCookieMaxAge", fragment3.getName(), fragment3.getURL() ) );
                        return false;
                    }
                }
            }
            if ( temp.getSessionConfig().getCookieMaxAge() != null ) {
                this.sessionConfig.setCookieMaxAge ( temp.getSessionConfig().getCookieMaxAge().toString() );
            }
        }
        if ( this.sessionConfig.getSessionTrackingModes().size() == 0 ) {
            for ( final WebXml fragment3 : fragments ) {
                final EnumSet<SessionTrackingMode> value5 = fragment3.getSessionConfig().getSessionTrackingModes();
                if ( value5.size() > 0 ) {
                    if ( temp.getSessionConfig().getSessionTrackingModes().size() == 0 ) {
                        temp.getSessionConfig().getSessionTrackingModes().addAll ( ( Collection<?> ) value5 );
                    } else {
                        if ( value5.equals ( temp.getSessionConfig().getSessionTrackingModes() ) ) {
                            continue;
                        }
                        WebXml.log.error ( WebXml.sm.getString ( "webXml.mergeConflictSessionTrackingMode", fragment3.getName(), fragment3.getURL() ) );
                        return false;
                    }
                }
            }
            this.sessionConfig.getSessionTrackingModes().addAll ( ( Collection<?> ) temp.getSessionConfig().getSessionTrackingModes() );
        }
        for ( final WebXml fragment3 : fragments ) {
            if ( !mergeMap ( fragment3.getTaglibs(), this.taglibs, temp.getTaglibs(), fragment3, "Taglibs" ) ) {
                return false;
            }
        }
        this.taglibs.putAll ( temp.getTaglibs() );
        for ( final WebXml fragment3 : fragments ) {
            if ( fragment3.alwaysAddWelcomeFiles || this.welcomeFiles.size() == 0 ) {
                for ( final String welcomeFile : fragment3.getWelcomeFiles() ) {
                    this.addWelcomeFile ( welcomeFile );
                }
            }
        }
        if ( this.postConstructMethods.isEmpty() ) {
            for ( final WebXml fragment3 : fragments ) {
                if ( !mergeLifecycleCallback ( fragment3.getPostConstructMethods(), temp.getPostConstructMethods(), fragment3, "Post Construct Methods" ) ) {
                    return false;
                }
            }
            this.postConstructMethods.putAll ( temp.getPostConstructMethods() );
        }
        if ( this.preDestroyMethods.isEmpty() ) {
            for ( final WebXml fragment3 : fragments ) {
                if ( !mergeLifecycleCallback ( fragment3.getPreDestroyMethods(), temp.getPreDestroyMethods(), fragment3, "Pre Destroy Methods" ) ) {
                    return false;
                }
            }
            this.preDestroyMethods.putAll ( temp.getPreDestroyMethods() );
        }
        return true;
    }
    private static <T extends ResourceBase> boolean mergeResourceMap ( final Map<String, T> fragmentResources, final Map<String, T> mainResources, final Map<String, T> tempResources, final WebXml fragment ) {
        for ( final T resource : fragmentResources.values() ) {
            final String resourceName = resource.getName();
            if ( mainResources.containsKey ( resourceName ) ) {
                mainResources.get ( resourceName ).getInjectionTargets().addAll ( resource.getInjectionTargets() );
            } else {
                final T existingResource = tempResources.get ( resourceName );
                if ( existingResource != null ) {
                    if ( !existingResource.equals ( resource ) ) {
                        WebXml.log.error ( WebXml.sm.getString ( "webXml.mergeConflictResource", resourceName, fragment.getName(), fragment.getURL() ) );
                        return false;
                    }
                    continue;
                } else {
                    tempResources.put ( resourceName, resource );
                }
            }
        }
        return true;
    }
    private static <T> boolean mergeMap ( final Map<String, T> fragmentMap, final Map<String, T> mainMap, final Map<String, T> tempMap, final WebXml fragment, final String mapName ) {
        for ( final Map.Entry<String, T> entry : fragmentMap.entrySet() ) {
            final String key = entry.getKey();
            if ( !mainMap.containsKey ( key ) ) {
                final T value = entry.getValue();
                if ( tempMap.containsKey ( key ) ) {
                    if ( value != null && !value.equals ( tempMap.get ( key ) ) ) {
                        WebXml.log.error ( WebXml.sm.getString ( "webXml.mergeConflictString", mapName, key, fragment.getName(), fragment.getURL() ) );
                        return false;
                    }
                    continue;
                } else {
                    tempMap.put ( key, value );
                }
            }
        }
        return true;
    }
    private static boolean mergeFilter ( final FilterDef src, final FilterDef dest, final boolean failOnConflict ) {
        if ( dest.getAsyncSupported() == null ) {
            dest.setAsyncSupported ( src.getAsyncSupported() );
        } else if ( src.getAsyncSupported() != null && failOnConflict && !src.getAsyncSupported().equals ( dest.getAsyncSupported() ) ) {
            return false;
        }
        if ( dest.getFilterClass() == null ) {
            dest.setFilterClass ( src.getFilterClass() );
        } else if ( src.getFilterClass() != null && failOnConflict && !src.getFilterClass().equals ( dest.getFilterClass() ) ) {
            return false;
        }
        for ( final Map.Entry<String, String> srcEntry : src.getParameterMap().entrySet() ) {
            if ( dest.getParameterMap().containsKey ( srcEntry.getKey() ) ) {
                if ( failOnConflict && !dest.getParameterMap().get ( srcEntry.getKey() ).equals ( srcEntry.getValue() ) ) {
                    return false;
                }
                continue;
            } else {
                dest.addInitParameter ( srcEntry.getKey(), srcEntry.getValue() );
            }
        }
        return true;
    }
    private static boolean mergeServlet ( final ServletDef src, final ServletDef dest, final boolean failOnConflict ) {
        if ( dest.getServletClass() != null && dest.getJspFile() != null ) {
            return false;
        }
        if ( src.getServletClass() != null && src.getJspFile() != null ) {
            return false;
        }
        if ( dest.getServletClass() == null && dest.getJspFile() == null ) {
            dest.setServletClass ( src.getServletClass() );
            dest.setJspFile ( src.getJspFile() );
        } else if ( failOnConflict ) {
            if ( src.getServletClass() != null && ( dest.getJspFile() != null || !src.getServletClass().equals ( dest.getServletClass() ) ) ) {
                return false;
            }
            if ( src.getJspFile() != null && ( dest.getServletClass() != null || !src.getJspFile().equals ( dest.getJspFile() ) ) ) {
                return false;
            }
        }
        for ( final SecurityRoleRef securityRoleRef : src.getSecurityRoleRefs() ) {
            dest.addSecurityRoleRef ( securityRoleRef );
        }
        if ( dest.getLoadOnStartup() == null ) {
            if ( src.getLoadOnStartup() != null ) {
                dest.setLoadOnStartup ( src.getLoadOnStartup().toString() );
            }
        } else if ( src.getLoadOnStartup() != null && failOnConflict && !src.getLoadOnStartup().equals ( dest.getLoadOnStartup() ) ) {
            return false;
        }
        if ( dest.getEnabled() == null ) {
            if ( src.getEnabled() != null ) {
                dest.setEnabled ( src.getEnabled().toString() );
            }
        } else if ( src.getEnabled() != null && failOnConflict && !src.getEnabled().equals ( dest.getEnabled() ) ) {
            return false;
        }
        for ( final Map.Entry<String, String> srcEntry : src.getParameterMap().entrySet() ) {
            if ( dest.getParameterMap().containsKey ( srcEntry.getKey() ) ) {
                if ( failOnConflict && !dest.getParameterMap().get ( srcEntry.getKey() ).equals ( srcEntry.getValue() ) ) {
                    return false;
                }
                continue;
            } else {
                dest.addInitParameter ( srcEntry.getKey(), srcEntry.getValue() );
            }
        }
        if ( dest.getMultipartDef() == null ) {
            dest.setMultipartDef ( src.getMultipartDef() );
        } else if ( src.getMultipartDef() != null ) {
            return mergeMultipartDef ( src.getMultipartDef(), dest.getMultipartDef(), failOnConflict );
        }
        if ( dest.getAsyncSupported() == null ) {
            if ( src.getAsyncSupported() != null ) {
                dest.setAsyncSupported ( src.getAsyncSupported().toString() );
            }
        } else if ( src.getAsyncSupported() != null && failOnConflict && !src.getAsyncSupported().equals ( dest.getAsyncSupported() ) ) {
            return false;
        }
        return true;
    }
    private static boolean mergeMultipartDef ( final MultipartDef src, final MultipartDef dest, final boolean failOnConflict ) {
        if ( dest.getLocation() == null ) {
            dest.setLocation ( src.getLocation() );
        } else if ( src.getLocation() != null && failOnConflict && !src.getLocation().equals ( dest.getLocation() ) ) {
            return false;
        }
        if ( dest.getFileSizeThreshold() == null ) {
            dest.setFileSizeThreshold ( src.getFileSizeThreshold() );
        } else if ( src.getFileSizeThreshold() != null && failOnConflict && !src.getFileSizeThreshold().equals ( dest.getFileSizeThreshold() ) ) {
            return false;
        }
        if ( dest.getMaxFileSize() == null ) {
            dest.setMaxFileSize ( src.getMaxFileSize() );
        } else if ( src.getMaxFileSize() != null && failOnConflict && !src.getMaxFileSize().equals ( dest.getMaxFileSize() ) ) {
            return false;
        }
        if ( dest.getMaxRequestSize() == null ) {
            dest.setMaxRequestSize ( src.getMaxRequestSize() );
        } else if ( src.getMaxRequestSize() != null && failOnConflict && !src.getMaxRequestSize().equals ( dest.getMaxRequestSize() ) ) {
            return false;
        }
        return true;
    }
    private static boolean mergeLifecycleCallback ( final Map<String, String> fragmentMap, final Map<String, String> tempMap, final WebXml fragment, final String mapName ) {
        for ( final Map.Entry<String, String> entry : fragmentMap.entrySet() ) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            if ( tempMap.containsKey ( key ) ) {
                if ( value != null && !value.equals ( tempMap.get ( key ) ) ) {
                    WebXml.log.error ( WebXml.sm.getString ( "webXml.mergeConflictString", mapName, key, fragment.getName(), fragment.getURL() ) );
                    return false;
                }
                continue;
            } else {
                tempMap.put ( key, value );
            }
        }
        return true;
    }
    public static Set<WebXml> orderWebFragments ( final WebXml application, final Map<String, WebXml> fragments, final ServletContext servletContext ) {
        final Set<WebXml> orderedFragments = new LinkedHashSet<WebXml>();
        final boolean absoluteOrdering = application.getAbsoluteOrdering() != null;
        boolean orderingPresent = false;
        if ( absoluteOrdering ) {
            orderingPresent = true;
            final Set<String> requestedOrder = application.getAbsoluteOrdering();
            for ( final String requestedName : requestedOrder ) {
                if ( "org.apache.catalina.order.others".equals ( requestedName ) ) {
                    for ( final Map.Entry<String, WebXml> entry : fragments.entrySet() ) {
                        if ( !requestedOrder.contains ( entry.getKey() ) ) {
                            final WebXml fragment = entry.getValue();
                            if ( fragment == null ) {
                                continue;
                            }
                            orderedFragments.add ( fragment );
                        }
                    }
                } else {
                    final WebXml fragment2 = fragments.get ( requestedName );
                    if ( fragment2 != null ) {
                        orderedFragments.add ( fragment2 );
                    } else {
                        WebXml.log.warn ( WebXml.sm.getString ( "webXml.wrongFragmentName", requestedName ) );
                    }
                }
            }
        } else {
            for ( final WebXml fragment3 : fragments.values() ) {
                final Iterator<String> before = fragment3.getBeforeOrdering().iterator();
                while ( before.hasNext() ) {
                    orderingPresent = true;
                    final String beforeEntry = before.next();
                    if ( !beforeEntry.equals ( "org.apache.catalina.order.others" ) ) {
                        final WebXml beforeFragment = fragments.get ( beforeEntry );
                        if ( beforeFragment == null ) {
                            before.remove();
                        } else {
                            beforeFragment.addAfterOrdering ( fragment3.getName() );
                        }
                    }
                }
                final Iterator<String> after = fragment3.getAfterOrdering().iterator();
                while ( after.hasNext() ) {
                    orderingPresent = true;
                    final String afterEntry = after.next();
                    if ( !afterEntry.equals ( "org.apache.catalina.order.others" ) ) {
                        final WebXml afterFragment = fragments.get ( afterEntry );
                        if ( afterFragment == null ) {
                            after.remove();
                        } else {
                            afterFragment.addBeforeOrdering ( fragment3.getName() );
                        }
                    }
                }
            }
            for ( final WebXml fragment3 : fragments.values() ) {
                if ( fragment3.getBeforeOrdering().contains ( "org.apache.catalina.order.others" ) ) {
                    makeBeforeOthersExplicit ( fragment3.getAfterOrdering(), fragments );
                }
                if ( fragment3.getAfterOrdering().contains ( "org.apache.catalina.order.others" ) ) {
                    makeAfterOthersExplicit ( fragment3.getBeforeOrdering(), fragments );
                }
            }
            final Set<WebXml> beforeSet = new HashSet<WebXml>();
            final Set<WebXml> othersSet = new HashSet<WebXml>();
            final Set<WebXml> afterSet = new HashSet<WebXml>();
            for ( final WebXml fragment4 : fragments.values() ) {
                if ( fragment4.getBeforeOrdering().contains ( "org.apache.catalina.order.others" ) ) {
                    beforeSet.add ( fragment4 );
                    fragment4.getBeforeOrdering().remove ( "org.apache.catalina.order.others" );
                } else if ( fragment4.getAfterOrdering().contains ( "org.apache.catalina.order.others" ) ) {
                    afterSet.add ( fragment4 );
                    fragment4.getAfterOrdering().remove ( "org.apache.catalina.order.others" );
                } else {
                    othersSet.add ( fragment4 );
                }
            }
            decoupleOtherGroups ( beforeSet );
            decoupleOtherGroups ( othersSet );
            decoupleOtherGroups ( afterSet );
            orderFragments ( orderedFragments, beforeSet );
            orderFragments ( orderedFragments, othersSet );
            orderFragments ( orderedFragments, afterSet );
        }
        final Set<WebXml> containerFragments = new LinkedHashSet<WebXml>();
        for ( final WebXml fragment5 : fragments.values() ) {
            if ( !fragment5.getWebappJar() ) {
                containerFragments.add ( fragment5 );
                orderedFragments.remove ( fragment5 );
            }
        }
        if ( servletContext != null ) {
            List<String> orderedJarFileNames = null;
            if ( orderingPresent ) {
                orderedJarFileNames = new ArrayList<String>();
                for ( final WebXml fragment2 : orderedFragments ) {
                    orderedJarFileNames.add ( fragment2.getJarName() );
                }
            }
            servletContext.setAttribute ( "javax.servlet.context.orderedLibs", ( Object ) orderedJarFileNames );
        }
        if ( containerFragments.size() > 0 ) {
            final Set<WebXml> result = new LinkedHashSet<WebXml>();
            if ( containerFragments.iterator().next().getDelegate() ) {
                result.addAll ( containerFragments );
                result.addAll ( orderedFragments );
            } else {
                result.addAll ( orderedFragments );
                result.addAll ( containerFragments );
            }
            return result;
        }
        return orderedFragments;
    }
    private static void decoupleOtherGroups ( final Set<WebXml> group ) {
        final Set<String> names = new HashSet<String>();
        for ( final WebXml fragment : group ) {
            names.add ( fragment.getName() );
        }
        for ( final WebXml fragment : group ) {
            final Iterator<String> after = fragment.getAfterOrdering().iterator();
            while ( after.hasNext() ) {
                final String entry = after.next();
                if ( !names.contains ( entry ) ) {
                    after.remove();
                }
            }
        }
    }
    private static void orderFragments ( final Set<WebXml> orderedFragments, final Set<WebXml> unordered ) {
        final Set<WebXml> addedThisRound = new HashSet<WebXml>();
        final Set<WebXml> addedLastRound = new HashSet<WebXml>();
        while ( unordered.size() > 0 ) {
            final Iterator<WebXml> source = unordered.iterator();
            while ( source.hasNext() ) {
                final WebXml fragment = source.next();
                for ( final WebXml toRemove : addedLastRound ) {
                    fragment.getAfterOrdering().remove ( toRemove.getName() );
                }
                if ( fragment.getAfterOrdering().isEmpty() ) {
                    addedThisRound.add ( fragment );
                    orderedFragments.add ( fragment );
                    source.remove();
                }
            }
            if ( addedThisRound.size() == 0 ) {
                throw new IllegalArgumentException ( WebXml.sm.getString ( "webXml.mergeConflictOrder" ) );
            }
            addedLastRound.clear();
            addedLastRound.addAll ( addedThisRound );
            addedThisRound.clear();
        }
    }
    private static void makeBeforeOthersExplicit ( final Set<String> beforeOrdering, final Map<String, WebXml> fragments ) {
        for ( final String before : beforeOrdering ) {
            if ( !before.equals ( "org.apache.catalina.order.others" ) ) {
                final WebXml webXml = fragments.get ( before );
                if ( webXml.getBeforeOrdering().contains ( "org.apache.catalina.order.others" ) ) {
                    continue;
                }
                webXml.addBeforeOrderingOthers();
                makeBeforeOthersExplicit ( webXml.getAfterOrdering(), fragments );
            }
        }
    }
    private static void makeAfterOthersExplicit ( final Set<String> afterOrdering, final Map<String, WebXml> fragments ) {
        for ( final String after : afterOrdering ) {
            if ( !after.equals ( "org.apache.catalina.order.others" ) ) {
                final WebXml webXml = fragments.get ( after );
                if ( webXml.getAfterOrdering().contains ( "org.apache.catalina.order.others" ) ) {
                    continue;
                }
                webXml.addAfterOrderingOthers();
                makeAfterOthersExplicit ( webXml.getBeforeOrdering(), fragments );
            }
        }
    }
    static {
        sm = StringManager.getManager ( Constants.PACKAGE_NAME );
        log = LogFactory.getLog ( WebXml.class );
    }
}
