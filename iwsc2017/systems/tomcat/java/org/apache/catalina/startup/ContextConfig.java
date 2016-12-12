package org.apache.catalina.startup;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.SessionCookieConfig;
import javax.servlet.annotation.HandlesTypes;
import org.apache.catalina.Authenticator;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Globals;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.Valve;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.util.ContextName;
import org.apache.catalina.util.Introspection;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.Jar;
import org.apache.tomcat.JarScanType;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.bcel.classfile.AnnotationElementValue;
import org.apache.tomcat.util.bcel.classfile.AnnotationEntry;
import org.apache.tomcat.util.bcel.classfile.ArrayElementValue;
import org.apache.tomcat.util.bcel.classfile.ClassFormatException;
import org.apache.tomcat.util.bcel.classfile.ClassParser;
import org.apache.tomcat.util.bcel.classfile.ElementValue;
import org.apache.tomcat.util.bcel.classfile.ElementValuePair;
import org.apache.tomcat.util.bcel.classfile.JavaClass;
import org.apache.tomcat.util.buf.UriUtil;
import org.apache.tomcat.util.descriptor.InputSourceUtil;
import org.apache.tomcat.util.descriptor.XmlErrorHandler;
import org.apache.tomcat.util.descriptor.web.ContextEjb;
import org.apache.tomcat.util.descriptor.web.ContextEnvironment;
import org.apache.tomcat.util.descriptor.web.ContextLocalEjb;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.ContextResourceEnvRef;
import org.apache.tomcat.util.descriptor.web.ContextService;
import org.apache.tomcat.util.descriptor.web.ErrorPage;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.tomcat.util.descriptor.web.FragmentJarScannerCallback;
import org.apache.tomcat.util.descriptor.web.JspPropertyGroup;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.descriptor.web.MessageDestinationRef;
import org.apache.tomcat.util.descriptor.web.MultipartDef;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.apache.tomcat.util.descriptor.web.SecurityRoleRef;
import org.apache.tomcat.util.descriptor.web.ServletDef;
import org.apache.tomcat.util.descriptor.web.SessionConfig;
import org.apache.tomcat.util.descriptor.web.WebXml;
import org.apache.tomcat.util.descriptor.web.WebXmlParser;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.RuleSet;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.scan.JarFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
public class ContextConfig implements LifecycleListener {
    private static final Log log = LogFactory.getLog ( ContextConfig.class );
    protected static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    protected static final LoginConfig DUMMY_LOGIN_CONFIG =
        new LoginConfig ( "NONE", null, null, null );
    protected static final Properties authenticators;
    static {
        Properties props = new Properties();
        try ( InputStream is = ContextConfig.class.getClassLoader().getResourceAsStream (
                                       "org/apache/catalina/startup/Authenticators.properties" ); ) {
            if ( is != null ) {
                props.load ( is );
            }
        } catch ( IOException ioe ) {
            props = null;
        }
        authenticators = props;
    }
    protected static long deploymentCount = 0L;
    protected static final Map<Host, DefaultWebXmlCacheEntry> hostWebXmlCache =
        new ConcurrentHashMap<>();
    private static final Set<ServletContainerInitializer> EMPTY_SCI_SET = Collections.emptySet();
    protected Map<String, Authenticator> customAuthenticators;
    protected Context context = null;
    protected String defaultWebXml = null;
    protected boolean ok = false;
    protected String originalDocBase = null;
    private File antiLockingDocBase = null;
    protected final Map<ServletContainerInitializer, Set<Class<?>>> initializerClassMap =
        new LinkedHashMap<>();
    protected final Map<Class<?>, Set<ServletContainerInitializer>> typeInitializerMap =
        new HashMap<>();
    protected boolean handlesTypesAnnotations = false;
    protected boolean handlesTypesNonAnnotations = false;
    public String getDefaultWebXml() {
        if ( defaultWebXml == null ) {
            defaultWebXml = Constants.DefaultWebXml;
        }
        return defaultWebXml;
    }
    public void setDefaultWebXml ( String path ) {
        this.defaultWebXml = path;
    }
    public void setCustomAuthenticators (
        Map<String, Authenticator> customAuthenticators ) {
        this.customAuthenticators = customAuthenticators;
    }
    @Override
    public void lifecycleEvent ( LifecycleEvent event ) {
        try {
            context = ( Context ) event.getLifecycle();
        } catch ( ClassCastException e ) {
            log.error ( sm.getString ( "contextConfig.cce", event.getLifecycle() ), e );
            return;
        }
        if ( event.getType().equals ( Lifecycle.CONFIGURE_START_EVENT ) ) {
            configureStart();
        } else if ( event.getType().equals ( Lifecycle.BEFORE_START_EVENT ) ) {
            beforeStart();
        } else if ( event.getType().equals ( Lifecycle.AFTER_START_EVENT ) ) {
            if ( originalDocBase != null ) {
                context.setDocBase ( originalDocBase );
            }
        } else if ( event.getType().equals ( Lifecycle.CONFIGURE_STOP_EVENT ) ) {
            configureStop();
        } else if ( event.getType().equals ( Lifecycle.AFTER_INIT_EVENT ) ) {
            init();
        } else if ( event.getType().equals ( Lifecycle.AFTER_DESTROY_EVENT ) ) {
            destroy();
        }
    }
    protected void applicationAnnotationsConfig() {
        long t1 = System.currentTimeMillis();
        WebAnnotationSet.loadApplicationAnnotations ( context );
        long t2 = System.currentTimeMillis();
        if ( context instanceof StandardContext ) {
            ( ( StandardContext ) context ).setStartupTime ( t2 - t1 +
                    ( ( StandardContext ) context ).getStartupTime() );
        }
    }
    protected void authenticatorConfig() {
        LoginConfig loginConfig = context.getLoginConfig();
        SecurityConstraint constraints[] = context.findConstraints();
        if ( context.getIgnoreAnnotations() &&
                ( constraints == null || constraints.length == 0 ) &&
                !context.getPreemptiveAuthentication() )  {
            return;
        } else {
            if ( loginConfig == null ) {
                loginConfig = DUMMY_LOGIN_CONFIG;
                context.setLoginConfig ( loginConfig );
            }
        }
        if ( context.getAuthenticator() != null ) {
            return;
        }
        if ( context.getRealm() == null ) {
            log.error ( sm.getString ( "contextConfig.missingRealm" ) );
            ok = false;
            return;
        }
        Valve authenticator = null;
        if ( customAuthenticators != null ) {
            authenticator = ( Valve ) customAuthenticators.get ( loginConfig.getAuthMethod() );
        }
        if ( authenticator == null ) {
            if ( authenticators == null ) {
                log.error ( sm.getString ( "contextConfig.authenticatorResources" ) );
                ok = false;
                return;
            }
            String authenticatorName = authenticators.getProperty ( loginConfig.getAuthMethod() );
            if ( authenticatorName == null ) {
                log.error ( sm.getString ( "contextConfig.authenticatorMissing",
                                           loginConfig.getAuthMethod() ) );
                ok = false;
                return;
            }
            try {
                Class<?> authenticatorClass = Class.forName ( authenticatorName );
                authenticator = ( Valve ) authenticatorClass.newInstance();
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                log.error ( sm.getString (
                                "contextConfig.authenticatorInstantiate",
                                authenticatorName ),
                            t );
                ok = false;
            }
        }
        if ( authenticator != null ) {
            Pipeline pipeline = context.getPipeline();
            if ( pipeline != null ) {
                pipeline.addValve ( authenticator );
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString (
                                    "contextConfig.authenticatorConfigured",
                                    loginConfig.getAuthMethod() ) );
                }
            }
        }
    }
    protected Digester createContextDigester() {
        Digester digester = new Digester();
        digester.setValidating ( false );
        digester.setRulesValidation ( true );
        HashMap<Class<?>, List<String>> fakeAttributes = new HashMap<>();
        ArrayList<String> attrs = new ArrayList<>();
        attrs.add ( "className" );
        fakeAttributes.put ( Object.class, attrs );
        digester.setFakeAttributes ( fakeAttributes );
        RuleSet contextRuleSet = new ContextRuleSet ( "", false );
        digester.addRuleSet ( contextRuleSet );
        RuleSet namingRuleSet = new NamingRuleSet ( "Context/" );
        digester.addRuleSet ( namingRuleSet );
        return digester;
    }
    protected void contextConfig ( Digester digester ) {
        String defaultContextXml = null;
        if ( context instanceof StandardContext ) {
            defaultContextXml = ( ( StandardContext ) context ).getDefaultContextXml();
        }
        if ( defaultContextXml == null ) {
            defaultContextXml = Constants.DefaultContextXml;
        }
        if ( !context.getOverride() ) {
            File defaultContextFile = new File ( defaultContextXml );
            if ( !defaultContextFile.isAbsolute() ) {
                defaultContextFile =
                    new File ( context.getCatalinaBase(), defaultContextXml );
            }
            if ( defaultContextFile.exists() ) {
                try {
                    URL defaultContextUrl = defaultContextFile.toURI().toURL();
                    processContextConfig ( digester, defaultContextUrl );
                } catch ( MalformedURLException e ) {
                    log.error ( sm.getString (
                                    "contextConfig.badUrl", defaultContextFile ), e );
                }
            }
            File hostContextFile = new File ( getHostConfigBase(), Constants.HostContextXml );
            if ( hostContextFile.exists() ) {
                try {
                    URL hostContextUrl = hostContextFile.toURI().toURL();
                    processContextConfig ( digester, hostContextUrl );
                } catch ( MalformedURLException e ) {
                    log.error ( sm.getString (
                                    "contextConfig.badUrl", hostContextFile ), e );
                }
            }
        }
        if ( context.getConfigFile() != null ) {
            processContextConfig ( digester, context.getConfigFile() );
        }
    }
    protected void processContextConfig ( Digester digester, URL contextXml ) {
        if ( log.isDebugEnabled() ) {
            log.debug ( "Processing context [" + context.getName()
                        + "] configuration file [" + contextXml + "]" );
        }
        InputSource source = null;
        InputStream stream = null;
        try {
            source = new InputSource ( contextXml.toString() );
            URLConnection xmlConn = contextXml.openConnection();
            xmlConn.setUseCaches ( false );
            stream = xmlConn.getInputStream();
        } catch ( Exception e ) {
            log.error ( sm.getString ( "contextConfig.contextMissing",
                                       contextXml ) , e );
        }
        if ( source == null ) {
            return;
        }
        try {
            source.setByteStream ( stream );
            digester.setClassLoader ( this.getClass().getClassLoader() );
            digester.setUseContextClassLoader ( false );
            digester.push ( context.getParent() );
            digester.push ( context );
            XmlErrorHandler errorHandler = new XmlErrorHandler();
            digester.setErrorHandler ( errorHandler );
            digester.parse ( source );
            if ( errorHandler.getWarnings().size() > 0 ||
                    errorHandler.getErrors().size() > 0 ) {
                errorHandler.logFindings ( log, contextXml.toString() );
                ok = false;
            }
            if ( log.isDebugEnabled() ) {
                log.debug ( "Successfully processed context [" + context.getName()
                            + "] configuration file [" + contextXml + "]" );
            }
        } catch ( SAXParseException e ) {
            log.error ( sm.getString ( "contextConfig.contextParse",
                                       context.getName() ), e );
            log.error ( sm.getString ( "contextConfig.defaultPosition",
                                       "" + e.getLineNumber(),
                                       "" + e.getColumnNumber() ) );
            ok = false;
        } catch ( Exception e ) {
            log.error ( sm.getString ( "contextConfig.contextParse",
                                       context.getName() ), e );
            ok = false;
        } finally {
            try {
                if ( stream != null ) {
                    stream.close();
                }
            } catch ( IOException e ) {
                log.error ( sm.getString ( "contextConfig.contextClose" ), e );
            }
        }
    }
    protected void fixDocBase() throws IOException {
        Host host = ( Host ) context.getParent();
        File appBase = host.getAppBaseFile();
        String docBase = context.getDocBase();
        if ( docBase == null ) {
            String path = context.getPath();
            if ( path == null ) {
                return;
            }
            ContextName cn = new ContextName ( path, context.getWebappVersion() );
            docBase = cn.getBaseName();
        }
        File file = new File ( docBase );
        if ( !file.isAbsolute() ) {
            docBase = ( new File ( appBase, docBase ) ).getPath();
        } else {
            docBase = file.getCanonicalPath();
        }
        file = new File ( docBase );
        String origDocBase = docBase;
        ContextName cn = new ContextName ( context.getPath(), context.getWebappVersion() );
        String pathName = cn.getBaseName();
        boolean unpackWARs = true;
        if ( host instanceof StandardHost ) {
            unpackWARs = ( ( StandardHost ) host ).isUnpackWARs();
            if ( unpackWARs && context instanceof StandardContext ) {
                unpackWARs = ( ( StandardContext ) context ).getUnpackWAR();
            }
        }
        boolean docBaseInAppBase = docBase.startsWith ( appBase.getPath() + File.separatorChar );
        if ( docBase.toLowerCase ( Locale.ENGLISH ).endsWith ( ".war" ) && !file.isDirectory() ) {
            URL war = UriUtil.buildJarUrl ( new File ( docBase ) );
            if ( unpackWARs ) {
                docBase = ExpandWar.expand ( host, war, pathName );
                file = new File ( docBase );
                docBase = file.getCanonicalPath();
                if ( context instanceof StandardContext ) {
                    ( ( StandardContext ) context ).setOriginalDocBase ( origDocBase );
                }
            } else {
                ExpandWar.validate ( host, war, pathName );
            }
        } else {
            File docDir = new File ( docBase );
            File warFile = new File ( docBase + ".war" );
            URL war = null;
            if ( warFile.exists() && docBaseInAppBase ) {
                war = UriUtil.buildJarUrl ( warFile );
            }
            if ( docDir.exists() ) {
                if ( war != null && unpackWARs ) {
                    ExpandWar.expand ( host, war, pathName );
                }
            } else {
                if ( war != null ) {
                    if ( unpackWARs ) {
                        docBase = ExpandWar.expand ( host, war, pathName );
                        file = new File ( docBase );
                        docBase = file.getCanonicalPath();
                    } else {
                        docBase = warFile.getCanonicalPath();
                        ExpandWar.validate ( host, war, pathName );
                    }
                }
                if ( context instanceof StandardContext ) {
                    ( ( StandardContext ) context ).setOriginalDocBase ( origDocBase );
                }
            }
        }
        docBaseInAppBase = docBase.startsWith ( appBase.getPath() + File.separatorChar );
        if ( docBaseInAppBase ) {
            docBase = docBase.substring ( appBase.getPath().length() );
            docBase = docBase.replace ( File.separatorChar, '/' );
            if ( docBase.startsWith ( "/" ) ) {
                docBase = docBase.substring ( 1 );
            }
        } else {
            docBase = docBase.replace ( File.separatorChar, '/' );
        }
        context.setDocBase ( docBase );
    }
    protected void antiLocking() {
        if ( ( context instanceof StandardContext )
                && ( ( StandardContext ) context ).getAntiResourceLocking() ) {
            Host host = ( Host ) context.getParent();
            String docBase = context.getDocBase();
            if ( docBase == null ) {
                return;
            }
            originalDocBase = docBase;
            File docBaseFile = new File ( docBase );
            if ( !docBaseFile.isAbsolute() ) {
                docBaseFile = new File ( host.getAppBaseFile(), docBase );
            }
            String path = context.getPath();
            if ( path == null ) {
                return;
            }
            ContextName cn = new ContextName ( path, context.getWebappVersion() );
            docBase = cn.getBaseName();
            if ( originalDocBase.toLowerCase ( Locale.ENGLISH ).endsWith ( ".war" ) ) {
                antiLockingDocBase = new File (
                    System.getProperty ( "java.io.tmpdir" ),
                    deploymentCount++ + "-" + docBase + ".war" );
            } else {
                antiLockingDocBase = new File (
                    System.getProperty ( "java.io.tmpdir" ),
                    deploymentCount++ + "-" + docBase );
            }
            antiLockingDocBase = antiLockingDocBase.getAbsoluteFile();
            if ( log.isDebugEnabled() ) {
                log.debug ( "Anti locking context[" + context.getName()
                            + "] setting docBase to " +
                            antiLockingDocBase.getPath() );
            }
            ExpandWar.delete ( antiLockingDocBase );
            if ( ExpandWar.copy ( docBaseFile, antiLockingDocBase ) ) {
                context.setDocBase ( antiLockingDocBase.getPath() );
            }
        }
    }
    protected void init() {
        Digester contextDigester = createContextDigester();
        contextDigester.getParser();
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "contextConfig.init" ) );
        }
        context.setConfigured ( false );
        ok = true;
        contextConfig ( contextDigester );
    }
    protected synchronized void beforeStart() {
        try {
            fixDocBase();
        } catch ( IOException e ) {
            log.error ( sm.getString (
                            "contextConfig.fixDocBase", context.getName() ), e );
        }
        antiLocking();
    }
    protected synchronized void configureStart() {
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "contextConfig.start" ) );
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "contextConfig.xmlSettings",
                                       context.getName(),
                                       Boolean.valueOf ( context.getXmlValidation() ),
                                       Boolean.valueOf ( context.getXmlNamespaceAware() ) ) );
        }
        webConfig();
        if ( !context.getIgnoreAnnotations() ) {
            applicationAnnotationsConfig();
        }
        if ( ok ) {
            validateSecurityRoles();
        }
        if ( ok ) {
            authenticatorConfig();
        }
        if ( log.isDebugEnabled() ) {
            log.debug ( "Pipeline Configuration:" );
            Pipeline pipeline = context.getPipeline();
            Valve valves[] = null;
            if ( pipeline != null ) {
                valves = pipeline.getValves();
            }
            if ( valves != null ) {
                for ( int i = 0; i < valves.length; i++ ) {
                    log.debug ( "  " + valves[i].getClass().getName() );
                }
            }
            log.debug ( "======================" );
        }
        if ( ok ) {
            context.setConfigured ( true );
        } else {
            log.error ( sm.getString ( "contextConfig.unavailable" ) );
            context.setConfigured ( false );
        }
    }
    protected synchronized void configureStop() {
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "contextConfig.stop" ) );
        }
        int i;
        Container[] children = context.findChildren();
        for ( i = 0; i < children.length; i++ ) {
            context.removeChild ( children[i] );
        }
        SecurityConstraint[] securityConstraints = context.findConstraints();
        for ( i = 0; i < securityConstraints.length; i++ ) {
            context.removeConstraint ( securityConstraints[i] );
        }
        ErrorPage[] errorPages = context.findErrorPages();
        for ( i = 0; i < errorPages.length; i++ ) {
            context.removeErrorPage ( errorPages[i] );
        }
        FilterDef[] filterDefs = context.findFilterDefs();
        for ( i = 0; i < filterDefs.length; i++ ) {
            context.removeFilterDef ( filterDefs[i] );
        }
        FilterMap[] filterMaps = context.findFilterMaps();
        for ( i = 0; i < filterMaps.length; i++ ) {
            context.removeFilterMap ( filterMaps[i] );
        }
        String[] mimeMappings = context.findMimeMappings();
        for ( i = 0; i < mimeMappings.length; i++ ) {
            context.removeMimeMapping ( mimeMappings[i] );
        }
        String[] parameters = context.findParameters();
        for ( i = 0; i < parameters.length; i++ ) {
            context.removeParameter ( parameters[i] );
        }
        String[] securityRoles = context.findSecurityRoles();
        for ( i = 0; i < securityRoles.length; i++ ) {
            context.removeSecurityRole ( securityRoles[i] );
        }
        String[] servletMappings = context.findServletMappings();
        for ( i = 0; i < servletMappings.length; i++ ) {
            context.removeServletMapping ( servletMappings[i] );
        }
        String[] welcomeFiles = context.findWelcomeFiles();
        for ( i = 0; i < welcomeFiles.length; i++ ) {
            context.removeWelcomeFile ( welcomeFiles[i] );
        }
        String[] wrapperLifecycles = context.findWrapperLifecycles();
        for ( i = 0; i < wrapperLifecycles.length; i++ ) {
            context.removeWrapperLifecycle ( wrapperLifecycles[i] );
        }
        String[] wrapperListeners = context.findWrapperListeners();
        for ( i = 0; i < wrapperListeners.length; i++ ) {
            context.removeWrapperListener ( wrapperListeners[i] );
        }
        if ( antiLockingDocBase != null ) {
            ExpandWar.delete ( antiLockingDocBase, false );
        }
        initializerClassMap.clear();
        typeInitializerMap.clear();
        ok = true;
    }
    protected synchronized void destroy() {
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "contextConfig.destroy" ) );
        }
        Server s = getServer();
        if ( s != null && !s.getState().isAvailable() ) {
            return;
        }
        if ( context instanceof StandardContext ) {
            String workDir = ( ( StandardContext ) context ).getWorkPath();
            if ( workDir != null ) {
                ExpandWar.delete ( new File ( workDir ) );
            }
        }
    }
    private Server getServer() {
        Container c = context;
        while ( c != null && ! ( c instanceof Engine ) ) {
            c = c.getParent();
        }
        if ( c == null ) {
            return null;
        }
        Service s = ( ( Engine ) c ).getService();
        if ( s == null ) {
            return null;
        }
        return s.getServer();
    }
    protected void validateSecurityRoles() {
        SecurityConstraint constraints[] = context.findConstraints();
        for ( int i = 0; i < constraints.length; i++ ) {
            String roles[] = constraints[i].findAuthRoles();
            for ( int j = 0; j < roles.length; j++ ) {
                if ( !"*".equals ( roles[j] ) &&
                        !context.findSecurityRole ( roles[j] ) ) {
                    log.warn ( sm.getString ( "contextConfig.role.auth", roles[j] ) );
                    context.addSecurityRole ( roles[j] );
                }
            }
        }
        Container wrappers[] = context.findChildren();
        for ( int i = 0; i < wrappers.length; i++ ) {
            Wrapper wrapper = ( Wrapper ) wrappers[i];
            String runAs = wrapper.getRunAs();
            if ( ( runAs != null ) && !context.findSecurityRole ( runAs ) ) {
                log.warn ( sm.getString ( "contextConfig.role.runas", runAs ) );
                context.addSecurityRole ( runAs );
            }
            String names[] = wrapper.findSecurityReferences();
            for ( int j = 0; j < names.length; j++ ) {
                String link = wrapper.findSecurityReference ( names[j] );
                if ( ( link != null ) && !context.findSecurityRole ( link ) ) {
                    log.warn ( sm.getString ( "contextConfig.role.link", link ) );
                    context.addSecurityRole ( link );
                }
            }
        }
    }
    protected File getHostConfigBase() {
        File file = null;
        if ( context.getParent() instanceof Host ) {
            file = ( ( Host ) context.getParent() ).getConfigBaseFile();
        }
        return file;
    }
    protected void webConfig() {
        WebXmlParser webXmlParser = new WebXmlParser ( context.getXmlNamespaceAware(),
                context.getXmlValidation(), context.getXmlBlockExternal() );
        Set<WebXml> defaults = new HashSet<>();
        defaults.add ( getDefaultWebXmlFragment ( webXmlParser ) );
        WebXml webXml = createWebXml();
        InputSource contextWebXml = getContextWebXmlSource();
        if ( !webXmlParser.parseWebXml ( contextWebXml, webXml, false ) ) {
            ok = false;
        }
        ServletContext sContext = context.getServletContext();
        Map<String, WebXml> fragments = processJarsForWebFragments ( webXml, webXmlParser );
        Set<WebXml> orderedFragments = null;
        orderedFragments =
            WebXml.orderWebFragments ( webXml, fragments, sContext );
        if ( ok ) {
            processServletContainerInitializers();
        }
        if ( !webXml.isMetadataComplete() || typeInitializerMap.size() > 0 ) {
            Map<String, JavaClassCacheEntry> javaClassCache = new HashMap<>();
            if ( ok ) {
                WebResource[] webResources =
                    context.getResources().listResources ( "/WEB-INF/classes" );
                for ( WebResource webResource : webResources ) {
                    if ( "META-INF".equals ( webResource.getName() ) ) {
                        continue;
                    }
                    processAnnotationsWebResource ( webResource, webXml,
                                                    webXml.isMetadataComplete(), javaClassCache );
                }
            }
            if ( ok ) {
                processAnnotations (
                    orderedFragments, webXml.isMetadataComplete(), javaClassCache );
            }
            javaClassCache.clear();
        }
        if ( !webXml.isMetadataComplete() ) {
            if ( ok ) {
                ok = webXml.merge ( orderedFragments );
            }
            webXml.merge ( defaults );
            if ( ok ) {
                convertJsps ( webXml );
            }
            if ( ok ) {
                configureContext ( webXml );
            }
        } else {
            webXml.merge ( defaults );
            convertJsps ( webXml );
            configureContext ( webXml );
        }
        if ( context.getLogEffectiveWebXml() ) {
            log.info ( "web.xml:\n" + webXml.toXml() );
        }
        if ( ok ) {
            Set<WebXml> resourceJars = new LinkedHashSet<>();
            for ( WebXml fragment : orderedFragments ) {
                resourceJars.add ( fragment );
            }
            for ( WebXml fragment : fragments.values() ) {
                if ( !resourceJars.contains ( fragment ) ) {
                    resourceJars.add ( fragment );
                }
            }
            processResourceJARs ( resourceJars );
        }
        if ( ok ) {
            for ( Map.Entry<ServletContainerInitializer,
                    Set<Class<?>>> entry :
                    initializerClassMap.entrySet() ) {
                if ( entry.getValue().isEmpty() ) {
                    context.addServletContainerInitializer (
                        entry.getKey(), null );
                } else {
                    context.addServletContainerInitializer (
                        entry.getKey(), entry.getValue() );
                }
            }
        }
    }
    private void configureContext ( WebXml webxml ) {
        context.setPublicId ( webxml.getPublicId() );
        context.setEffectiveMajorVersion ( webxml.getMajorVersion() );
        context.setEffectiveMinorVersion ( webxml.getMinorVersion() );
        for ( Entry<String, String> entry : webxml.getContextParams().entrySet() ) {
            context.addParameter ( entry.getKey(), entry.getValue() );
        }
        context.setDenyUncoveredHttpMethods (
            webxml.getDenyUncoveredHttpMethods() );
        context.setDisplayName ( webxml.getDisplayName() );
        context.setDistributable ( webxml.isDistributable() );
        for ( ContextLocalEjb ejbLocalRef : webxml.getEjbLocalRefs().values() ) {
            context.getNamingResources().addLocalEjb ( ejbLocalRef );
        }
        for ( ContextEjb ejbRef : webxml.getEjbRefs().values() ) {
            context.getNamingResources().addEjb ( ejbRef );
        }
        for ( ContextEnvironment environment : webxml.getEnvEntries().values() ) {
            context.getNamingResources().addEnvironment ( environment );
        }
        for ( ErrorPage errorPage : webxml.getErrorPages().values() ) {
            context.addErrorPage ( errorPage );
        }
        for ( FilterDef filter : webxml.getFilters().values() ) {
            if ( filter.getAsyncSupported() == null ) {
                filter.setAsyncSupported ( "false" );
            }
            context.addFilterDef ( filter );
        }
        for ( FilterMap filterMap : webxml.getFilterMappings() ) {
            context.addFilterMap ( filterMap );
        }
        context.setJspConfigDescriptor ( webxml.getJspConfigDescriptor() );
        for ( String listener : webxml.getListeners() ) {
            context.addApplicationListener ( listener );
        }
        for ( Entry<String, String> entry :
                webxml.getLocaleEncodingMappings().entrySet() ) {
            context.addLocaleEncodingMappingParameter ( entry.getKey(),
                    entry.getValue() );
        }
        if ( webxml.getLoginConfig() != null ) {
            context.setLoginConfig ( webxml.getLoginConfig() );
        }
        for ( MessageDestinationRef mdr :
                webxml.getMessageDestinationRefs().values() ) {
            context.getNamingResources().addMessageDestinationRef ( mdr );
        }
        context.setIgnoreAnnotations ( webxml.isMetadataComplete() );
        for ( Entry<String, String> entry :
                webxml.getMimeMappings().entrySet() ) {
            context.addMimeMapping ( entry.getKey(), entry.getValue() );
        }
        for ( ContextResourceEnvRef resource :
                webxml.getResourceEnvRefs().values() ) {
            context.getNamingResources().addResourceEnvRef ( resource );
        }
        for ( ContextResource resource : webxml.getResourceRefs().values() ) {
            context.getNamingResources().addResource ( resource );
        }
        boolean allAuthenticatedUsersIsAppRole =
            webxml.getSecurityRoles().contains (
                SecurityConstraint.ROLE_ALL_AUTHENTICATED_USERS );
        for ( SecurityConstraint constraint : webxml.getSecurityConstraints() ) {
            if ( allAuthenticatedUsersIsAppRole ) {
                constraint.treatAllAuthenticatedUsersAsApplicationRole();
            }
            context.addConstraint ( constraint );
        }
        for ( String role : webxml.getSecurityRoles() ) {
            context.addSecurityRole ( role );
        }
        for ( ContextService service : webxml.getServiceRefs().values() ) {
            context.getNamingResources().addService ( service );
        }
        for ( ServletDef servlet : webxml.getServlets().values() ) {
            Wrapper wrapper = context.createWrapper();
            if ( servlet.getLoadOnStartup() != null ) {
                wrapper.setLoadOnStartup ( servlet.getLoadOnStartup().intValue() );
            }
            if ( servlet.getEnabled() != null ) {
                wrapper.setEnabled ( servlet.getEnabled().booleanValue() );
            }
            wrapper.setName ( servlet.getServletName() );
            Map<String, String> params = servlet.getParameterMap();
            for ( Entry<String, String> entry : params.entrySet() ) {
                wrapper.addInitParameter ( entry.getKey(), entry.getValue() );
            }
            wrapper.setRunAs ( servlet.getRunAs() );
            Set<SecurityRoleRef> roleRefs = servlet.getSecurityRoleRefs();
            for ( SecurityRoleRef roleRef : roleRefs ) {
                wrapper.addSecurityReference (
                    roleRef.getName(), roleRef.getLink() );
            }
            wrapper.setServletClass ( servlet.getServletClass() );
            MultipartDef multipartdef = servlet.getMultipartDef();
            if ( multipartdef != null ) {
                if ( multipartdef.getMaxFileSize() != null &&
                        multipartdef.getMaxRequestSize() != null &&
                        multipartdef.getFileSizeThreshold() != null ) {
                    wrapper.setMultipartConfigElement ( new MultipartConfigElement (
                                                            multipartdef.getLocation(),
                                                            Long.parseLong ( multipartdef.getMaxFileSize() ),
                                                            Long.parseLong ( multipartdef.getMaxRequestSize() ),
                                                            Integer.parseInt (
                                                                    multipartdef.getFileSizeThreshold() ) ) );
                } else {
                    wrapper.setMultipartConfigElement ( new MultipartConfigElement (
                                                            multipartdef.getLocation() ) );
                }
            }
            if ( servlet.getAsyncSupported() != null ) {
                wrapper.setAsyncSupported (
                    servlet.getAsyncSupported().booleanValue() );
            }
            wrapper.setOverridable ( servlet.isOverridable() );
            context.addChild ( wrapper );
        }
        for ( Entry<String, String> entry :
                webxml.getServletMappings().entrySet() ) {
            context.addServletMappingDecoded ( entry.getKey(), entry.getValue() );
        }
        SessionConfig sessionConfig = webxml.getSessionConfig();
        if ( sessionConfig != null ) {
            if ( sessionConfig.getSessionTimeout() != null ) {
                context.setSessionTimeout (
                    sessionConfig.getSessionTimeout().intValue() );
            }
            SessionCookieConfig scc =
                context.getServletContext().getSessionCookieConfig();
            scc.setName ( sessionConfig.getCookieName() );
            scc.setDomain ( sessionConfig.getCookieDomain() );
            scc.setPath ( sessionConfig.getCookiePath() );
            scc.setComment ( sessionConfig.getCookieComment() );
            if ( sessionConfig.getCookieHttpOnly() != null ) {
                scc.setHttpOnly ( sessionConfig.getCookieHttpOnly().booleanValue() );
            }
            if ( sessionConfig.getCookieSecure() != null ) {
                scc.setSecure ( sessionConfig.getCookieSecure().booleanValue() );
            }
            if ( sessionConfig.getCookieMaxAge() != null ) {
                scc.setMaxAge ( sessionConfig.getCookieMaxAge().intValue() );
            }
            if ( sessionConfig.getSessionTrackingModes().size() > 0 ) {
                context.getServletContext().setSessionTrackingModes (
                    sessionConfig.getSessionTrackingModes() );
            }
        }
        for ( String welcomeFile : webxml.getWelcomeFiles() ) {
            if ( welcomeFile != null && welcomeFile.length() > 0 ) {
                context.addWelcomeFile ( welcomeFile );
            }
        }
        for ( JspPropertyGroup jspPropertyGroup :
                webxml.getJspPropertyGroups() ) {
            String jspServletName = context.findServletMapping ( "*.jsp" );
            if ( jspServletName == null ) {
                jspServletName = "jsp";
            }
            if ( context.findChild ( jspServletName ) != null ) {
                for ( String urlPattern : jspPropertyGroup.getUrlPatterns() ) {
                    context.addServletMappingDecoded ( urlPattern, jspServletName, true );
                }
            } else {
                if ( log.isDebugEnabled() ) {
                    for ( String urlPattern : jspPropertyGroup.getUrlPatterns() ) {
                        log.debug ( "Skiping " + urlPattern + " , no servlet " +
                                    jspServletName );
                    }
                }
            }
        }
        for ( Entry<String, String> entry :
                webxml.getPostConstructMethods().entrySet() ) {
            context.addPostConstructMethod ( entry.getKey(), entry.getValue() );
        }
        for ( Entry<String, String> entry :
                webxml.getPreDestroyMethods().entrySet() ) {
            context.addPreDestroyMethod ( entry.getKey(), entry.getValue() );
        }
    }
    private WebXml getDefaultWebXmlFragment ( WebXmlParser webXmlParser ) {
        Host host = ( Host ) context.getParent();
        DefaultWebXmlCacheEntry entry = hostWebXmlCache.get ( host );
        InputSource globalWebXml = getGlobalWebXmlSource();
        InputSource hostWebXml = getHostWebXmlSource();
        long globalTimeStamp = 0;
        long hostTimeStamp = 0;
        if ( globalWebXml != null ) {
            URLConnection uc = null;
            try {
                URL url = new URL ( globalWebXml.getSystemId() );
                uc = url.openConnection();
                globalTimeStamp = uc.getLastModified();
            } catch ( IOException e ) {
                globalTimeStamp = -1;
            } finally {
                if ( uc != null ) {
                    try {
                        uc.getInputStream().close();
                    } catch ( IOException e ) {
                        ExceptionUtils.handleThrowable ( e );
                        globalTimeStamp = -1;
                    }
                }
            }
        }
        if ( hostWebXml != null ) {
            URLConnection uc = null;
            try {
                URL url = new URL ( hostWebXml.getSystemId() );
                uc = url.openConnection();
                hostTimeStamp = uc.getLastModified();
            } catch ( IOException e ) {
                hostTimeStamp = -1;
            } finally {
                if ( uc != null ) {
                    try {
                        uc.getInputStream().close();
                    } catch ( IOException e ) {
                        ExceptionUtils.handleThrowable ( e );
                        hostTimeStamp = -1;
                    }
                }
            }
        }
        if ( entry != null && entry.getGlobalTimeStamp() == globalTimeStamp &&
                entry.getHostTimeStamp() == hostTimeStamp ) {
            InputSourceUtil.close ( globalWebXml );
            InputSourceUtil.close ( hostWebXml );
            return entry.getWebXml();
        }
        synchronized ( host.getPipeline() ) {
            entry = hostWebXmlCache.get ( host );
            if ( entry != null && entry.getGlobalTimeStamp() == globalTimeStamp &&
                    entry.getHostTimeStamp() == hostTimeStamp ) {
                return entry.getWebXml();
            }
            WebXml webXmlDefaultFragment = createWebXml();
            webXmlDefaultFragment.setOverridable ( true );
            webXmlDefaultFragment.setDistributable ( true );
            webXmlDefaultFragment.setAlwaysAddWelcomeFiles ( false );
            if ( globalWebXml == null ) {
                log.info ( sm.getString ( "contextConfig.defaultMissing" ) );
            } else {
                if ( !webXmlParser.parseWebXml (
                            globalWebXml, webXmlDefaultFragment, false ) ) {
                    ok = false;
                }
            }
            webXmlDefaultFragment.setReplaceWelcomeFiles ( true );
            if ( !webXmlParser.parseWebXml (
                        hostWebXml, webXmlDefaultFragment, false ) ) {
                ok = false;
            }
            if ( globalTimeStamp != -1 && hostTimeStamp != -1 ) {
                entry = new DefaultWebXmlCacheEntry ( webXmlDefaultFragment,
                                                      globalTimeStamp, hostTimeStamp );
                hostWebXmlCache.put ( host, entry );
            }
            return webXmlDefaultFragment;
        }
    }
    private void convertJsps ( WebXml webXml ) {
        Map<String, String> jspInitParams;
        ServletDef jspServlet = webXml.getServlets().get ( "jsp" );
        if ( jspServlet == null ) {
            jspInitParams = new HashMap<>();
            Wrapper w = ( Wrapper ) context.findChild ( "jsp" );
            if ( w != null ) {
                String[] params = w.findInitParameters();
                for ( String param : params ) {
                    jspInitParams.put ( param, w.findInitParameter ( param ) );
                }
            }
        } else {
            jspInitParams = jspServlet.getParameterMap();
        }
        for ( ServletDef servletDef : webXml.getServlets().values() ) {
            if ( servletDef.getJspFile() != null ) {
                convertJsp ( servletDef, jspInitParams );
            }
        }
    }
    private void convertJsp ( ServletDef servletDef,
                              Map<String, String> jspInitParams ) {
        servletDef.setServletClass ( org.apache.catalina.core.Constants.JSP_SERVLET_CLASS );
        String jspFile = servletDef.getJspFile();
        if ( ( jspFile != null ) && !jspFile.startsWith ( "/" ) ) {
            if ( context.isServlet22() ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "contextConfig.jspFile.warning",
                                               jspFile ) );
                }
                jspFile = "/" + jspFile;
            } else {
                throw new IllegalArgumentException
                ( sm.getString ( "contextConfig.jspFile.error", jspFile ) );
            }
        }
        servletDef.getParameterMap().put ( "jspFile", jspFile );
        servletDef.setJspFile ( null );
        for ( Map.Entry<String, String> initParam : jspInitParams.entrySet() ) {
            servletDef.addInitParameter ( initParam.getKey(), initParam.getValue() );
        }
    }
    protected WebXml createWebXml() {
        return new WebXml();
    }
    protected void processServletContainerInitializers() {
        List<ServletContainerInitializer> detectedScis;
        try {
            WebappServiceLoader<ServletContainerInitializer> loader = new WebappServiceLoader<> ( context );
            detectedScis = loader.load ( ServletContainerInitializer.class );
        } catch ( IOException e ) {
            log.error ( sm.getString (
                            "contextConfig.servletContainerInitializerFail",
                            context.getName() ),
                        e );
            ok = false;
            return;
        }
        for ( ServletContainerInitializer sci : detectedScis ) {
            initializerClassMap.put ( sci, new HashSet<Class<?>>() );
            HandlesTypes ht;
            try {
                ht = sci.getClass().getAnnotation ( HandlesTypes.class );
            } catch ( Exception e ) {
                if ( log.isDebugEnabled() ) {
                    log.info ( sm.getString ( "contextConfig.sci.debug",
                                              sci.getClass().getName() ),
                               e );
                } else {
                    log.info ( sm.getString ( "contextConfig.sci.info",
                                              sci.getClass().getName() ) );
                }
                continue;
            }
            if ( ht == null ) {
                continue;
            }
            Class<?>[] types = ht.value();
            if ( types == null ) {
                continue;
            }
            for ( Class<?> type : types ) {
                if ( type.isAnnotation() ) {
                    handlesTypesAnnotations = true;
                } else {
                    handlesTypesNonAnnotations = true;
                }
                Set<ServletContainerInitializer> scis =
                    typeInitializerMap.get ( type );
                if ( scis == null ) {
                    scis = new HashSet<>();
                    typeInitializerMap.put ( type, scis );
                }
                scis.add ( sci );
            }
        }
    }
    protected void processResourceJARs ( Set<WebXml> fragments ) {
        for ( WebXml fragment : fragments ) {
            URL url = fragment.getURL();
            try {
                if ( "jar".equals ( url.getProtocol() ) || url.toString().endsWith ( ".jar" ) ) {
                    try ( Jar jar = JarFactory.newInstance ( url ) ) {
                        jar.nextEntry();
                        String entryName = jar.getEntryName();
                        while ( entryName != null ) {
                            if ( entryName.startsWith ( "META-INF/resources/" ) ) {
                                context.getResources().createWebResourceSet (
                                    WebResourceRoot.ResourceSetType.RESOURCE_JAR,
                                    "/", url, "/META-INF/resources" );
                                break;
                            }
                            jar.nextEntry();
                            entryName = jar.getEntryName();
                        }
                    }
                } else if ( "file".equals ( url.getProtocol() ) ) {
                    File file = new File ( url.toURI() );
                    File resources = new File ( file, "META-INF/resources/" );
                    if ( resources.isDirectory() ) {
                        context.getResources().createWebResourceSet (
                            WebResourceRoot.ResourceSetType.RESOURCE_JAR,
                            "/", resources.getAbsolutePath(), null, "/" );
                    }
                }
            } catch ( IOException ioe ) {
                log.error ( sm.getString ( "contextConfig.resourceJarFail", url,
                                           context.getName() ) );
            } catch ( URISyntaxException e ) {
                log.error ( sm.getString ( "contextConfig.resourceJarFail", url,
                                           context.getName() ) );
            }
        }
    }
    protected InputSource getGlobalWebXmlSource() {
        if ( defaultWebXml == null && context instanceof StandardContext ) {
            defaultWebXml = ( ( StandardContext ) context ).getDefaultWebXml();
        }
        if ( defaultWebXml == null ) {
            getDefaultWebXml();
        }
        if ( Constants.NoDefaultWebXml.equals ( defaultWebXml ) ) {
            return null;
        }
        return getWebXmlSource ( defaultWebXml,
                                 context.getCatalinaBase().getPath() );
    }
    protected InputSource getHostWebXmlSource() {
        File hostConfigBase = getHostConfigBase();
        if ( hostConfigBase == null ) {
            return null;
        }
        return getWebXmlSource ( Constants.HostWebXml, hostConfigBase.getPath() );
    }
    protected InputSource getContextWebXmlSource() {
        InputStream stream = null;
        InputSource source = null;
        URL url = null;
        String altDDName = null;
        ServletContext servletContext = context.getServletContext();
        try {
            if ( servletContext != null ) {
                altDDName = ( String ) servletContext.getAttribute ( Globals.ALT_DD_ATTR );
                if ( altDDName != null ) {
                    try {
                        stream = new FileInputStream ( altDDName );
                        url = new File ( altDDName ).toURI().toURL();
                    } catch ( FileNotFoundException e ) {
                        log.error ( sm.getString ( "contextConfig.altDDNotFound",
                                                   altDDName ) );
                    } catch ( MalformedURLException e ) {
                        log.error ( sm.getString ( "contextConfig.applicationUrl" ) );
                    }
                } else {
                    stream = servletContext.getResourceAsStream
                             ( Constants.ApplicationWebXml );
                    try {
                        url = servletContext.getResource (
                                  Constants.ApplicationWebXml );
                    } catch ( MalformedURLException e ) {
                        log.error ( sm.getString ( "contextConfig.applicationUrl" ) );
                    }
                }
            }
            if ( stream == null || url == null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "contextConfig.applicationMissing" ) + " " + context );
                }
            } else {
                source = new InputSource ( url.toExternalForm() );
                source.setByteStream ( stream );
            }
        } finally {
            if ( source == null && stream != null ) {
                try {
                    stream.close();
                } catch ( IOException e ) {
                }
            }
        }
        return source;
    }
    protected InputSource getWebXmlSource ( String filename, String path ) {
        File file = new File ( filename );
        if ( !file.isAbsolute() ) {
            file = new File ( path, filename );
        }
        InputStream stream = null;
        InputSource source = null;
        try {
            if ( !file.exists() ) {
                stream =
                    getClass().getClassLoader().getResourceAsStream ( filename );
                if ( stream != null ) {
                    source =
                        new InputSource ( getClass().getClassLoader().getResource (
                                              filename ).toURI().toString() );
                }
            } else {
                source = new InputSource ( file.getAbsoluteFile().toURI().toString() );
                stream = new FileInputStream ( file );
            }
            if ( stream != null && source != null ) {
                source.setByteStream ( stream );
            }
        } catch ( Exception e ) {
            log.error ( sm.getString (
                            "contextConfig.defaultError", filename, file ), e );
        } finally {
            if ( source == null && stream != null ) {
                try {
                    stream.close();
                } catch ( IOException e ) {
                }
            }
        }
        return source;
    }
    protected Map<String, WebXml> processJarsForWebFragments ( WebXml application,
            WebXmlParser webXmlParser ) {
        JarScanner jarScanner = context.getJarScanner();
        boolean delegate = false;
        if ( context instanceof StandardContext ) {
            delegate = ( ( StandardContext ) context ).getDelegate();
        }
        boolean parseRequired = true;
        Set<String> absoluteOrder = application.getAbsoluteOrdering();
        if ( absoluteOrder != null && absoluteOrder.isEmpty() &&
                !context.getXmlValidation() ) {
            parseRequired = false;
        }
        FragmentJarScannerCallback callback =
            new FragmentJarScannerCallback ( webXmlParser, delegate, parseRequired );
        jarScanner.scan ( JarScanType.PLUGGABILITY,
                          context.getServletContext(), callback );
        if ( !callback.isOk() ) {
            ok = false;
        }
        return callback.getFragments();
    }
    protected void processAnnotations ( Set<WebXml> fragments,
                                        boolean handlesTypesOnly, Map<String, JavaClassCacheEntry> javaClassCache ) {
        for ( WebXml fragment : fragments ) {
            boolean htOnly = handlesTypesOnly || !fragment.getWebappJar() ||
                             fragment.isMetadataComplete();
            WebXml annotations = new WebXml();
            annotations.setDistributable ( true );
            URL url = fragment.getURL();
            processAnnotationsUrl ( url, annotations, htOnly, javaClassCache );
            Set<WebXml> set = new HashSet<>();
            set.add ( annotations );
            fragment.merge ( set );
        }
    }
    protected void processAnnotationsWebResource ( WebResource webResource,
            WebXml fragment, boolean handlesTypesOnly,
            Map<String, JavaClassCacheEntry> javaClassCache ) {
        if ( webResource.isDirectory() ) {
            WebResource[] webResources =
                webResource.getWebResourceRoot().listResources (
                    webResource.getWebappPath() );
            if ( webResources.length > 0 ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString (
                                    "contextConfig.processAnnotationsWebDir.debug",
                                    webResource.getURL() ) );
                }
                for ( WebResource r : webResources ) {
                    processAnnotationsWebResource ( r, fragment, handlesTypesOnly, javaClassCache );
                }
            }
        } else if ( webResource.isFile() &&
                    webResource.getName().endsWith ( ".class" ) ) {
            try ( InputStream is = webResource.getInputStream() ) {
                processAnnotationsStream ( is, fragment, handlesTypesOnly, javaClassCache );
            } catch ( IOException e ) {
                log.error ( sm.getString ( "contextConfig.inputStreamWebResource",
                                           webResource.getWebappPath() ), e );
            } catch ( ClassFormatException e ) {
                log.error ( sm.getString ( "contextConfig.inputStreamWebResource",
                                           webResource.getWebappPath() ), e );
            }
        }
    }
    protected void processAnnotationsUrl ( URL url, WebXml fragment,
                                           boolean handlesTypesOnly, Map<String, JavaClassCacheEntry> javaClassCache ) {
        if ( url == null ) {
            return;
        } else if ( "jar".equals ( url.getProtocol() ) || url.toString().endsWith ( ".jar" ) ) {
            processAnnotationsJar ( url, fragment, handlesTypesOnly, javaClassCache );
        } else if ( "file".equals ( url.getProtocol() ) ) {
            try {
                processAnnotationsFile (
                    new File ( url.toURI() ), fragment, handlesTypesOnly, javaClassCache );
            } catch ( URISyntaxException e ) {
                log.error ( sm.getString ( "contextConfig.fileUrl", url ), e );
            }
        } else {
            log.error ( sm.getString ( "contextConfig.unknownUrlProtocol",
                                       url.getProtocol(), url ) );
        }
    }
    protected void processAnnotationsJar ( URL url, WebXml fragment,
                                           boolean handlesTypesOnly, Map<String, JavaClassCacheEntry> javaClassCache ) {
        try ( Jar jar = JarFactory.newInstance ( url ) ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString (
                                "contextConfig.processAnnotationsJar.debug", url ) );
            }
            jar.nextEntry();
            String entryName = jar.getEntryName();
            while ( entryName != null ) {
                if ( entryName.endsWith ( ".class" ) ) {
                    try ( InputStream is = jar.getEntryInputStream() ) {
                        processAnnotationsStream ( is, fragment, handlesTypesOnly, javaClassCache );
                    } catch ( IOException e ) {
                        log.error ( sm.getString ( "contextConfig.inputStreamJar",
                                                   entryName, url ), e );
                    } catch ( ClassFormatException e ) {
                        log.error ( sm.getString ( "contextConfig.inputStreamJar",
                                                   entryName, url ), e );
                    }
                }
                jar.nextEntry();
                entryName = jar.getEntryName();
            }
        } catch ( IOException e ) {
            log.error ( sm.getString ( "contextConfig.jarFile", url ), e );
        }
    }
    protected void processAnnotationsFile ( File file, WebXml fragment,
                                            boolean handlesTypesOnly, Map<String, JavaClassCacheEntry> javaClassCache ) {
        if ( file.isDirectory() ) {
            String[] dirs = file.list();
            if ( dirs != null ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString (
                                    "contextConfig.processAnnotationsDir.debug", file ) );
                }
                for ( String dir : dirs ) {
                    processAnnotationsFile (
                        new File ( file, dir ), fragment, handlesTypesOnly, javaClassCache );
                }
            }
        } else if ( file.getName().endsWith ( ".class" ) && file.canRead() ) {
            try ( FileInputStream fis = new FileInputStream ( file ) ) {
                processAnnotationsStream ( fis, fragment, handlesTypesOnly, javaClassCache );
            } catch ( IOException e ) {
                log.error ( sm.getString ( "contextConfig.inputStreamFile",
                                           file.getAbsolutePath() ), e );
            } catch ( ClassFormatException e ) {
                log.error ( sm.getString ( "contextConfig.inputStreamFile",
                                           file.getAbsolutePath() ), e );
            }
        }
    }
    protected void processAnnotationsStream ( InputStream is, WebXml fragment,
            boolean handlesTypesOnly, Map<String, JavaClassCacheEntry> javaClassCache )
    throws ClassFormatException, IOException {
        ClassParser parser = new ClassParser ( is );
        JavaClass clazz = parser.parse();
        checkHandlesTypes ( clazz, javaClassCache );
        if ( handlesTypesOnly ) {
            return;
        }
        AnnotationEntry[] annotationsEntries = clazz.getAnnotationEntries();
        if ( annotationsEntries != null ) {
            String className = clazz.getClassName();
            for ( AnnotationEntry ae : annotationsEntries ) {
                String type = ae.getAnnotationType();
                if ( "Ljavax/servlet/annotation/WebServlet;".equals ( type ) ) {
                    processAnnotationWebServlet ( className, ae, fragment );
                } else if ( "Ljavax/servlet/annotation/WebFilter;".equals ( type ) ) {
                    processAnnotationWebFilter ( className, ae, fragment );
                } else if ( "Ljavax/servlet/annotation/WebListener;".equals ( type ) ) {
                    fragment.addListener ( className );
                } else {
                }
            }
        }
    }
    protected void checkHandlesTypes ( JavaClass javaClass,
                                       Map<String, JavaClassCacheEntry> javaClassCache ) {
        if ( typeInitializerMap.size() == 0 ) {
            return;
        }
        if ( ( javaClass.getAccessFlags() &
                org.apache.tomcat.util.bcel.Const.ACC_ANNOTATION ) > 0 ) {
            return;
        }
        String className = javaClass.getClassName();
        Class<?> clazz = null;
        if ( handlesTypesNonAnnotations ) {
            populateJavaClassCache ( className, javaClass, javaClassCache );
            JavaClassCacheEntry entry = javaClassCache.get ( className );
            if ( entry.getSciSet() == null ) {
                try {
                    populateSCIsForCacheEntry ( entry, javaClassCache );
                } catch ( StackOverflowError soe ) {
                    throw new IllegalStateException ( sm.getString (
                                                          "contextConfig.annotationsStackOverflow",
                                                          context.getName(),
                                                          classHierarchyToString ( className, entry, javaClassCache ) ) );
                }
            }
            if ( !entry.getSciSet().isEmpty() ) {
                clazz = Introspection.loadClass ( context, className );
                if ( clazz == null ) {
                    return;
                }
                for ( ServletContainerInitializer sci : entry.getSciSet() ) {
                    Set<Class<?>> classes = initializerClassMap.get ( sci );
                    if ( classes == null ) {
                        classes = new HashSet<>();
                        initializerClassMap.put ( sci, classes );
                    }
                    classes.add ( clazz );
                }
            }
        }
        if ( handlesTypesAnnotations ) {
            AnnotationEntry[] annotationEntries = javaClass.getAnnotationEntries();
            if ( annotationEntries != null ) {
                for ( Map.Entry<Class<?>, Set<ServletContainerInitializer>> entry :
                        typeInitializerMap.entrySet() ) {
                    if ( entry.getKey().isAnnotation() ) {
                        String entryClassName = entry.getKey().getName();
                        for ( AnnotationEntry annotationEntry : annotationEntries ) {
                            if ( entryClassName.equals (
                                        getClassName ( annotationEntry.getAnnotationType() ) ) ) {
                                if ( clazz == null ) {
                                    clazz = Introspection.loadClass (
                                                context, className );
                                    if ( clazz == null ) {
                                        return;
                                    }
                                }
                                for ( ServletContainerInitializer sci : entry.getValue() ) {
                                    initializerClassMap.get ( sci ).add ( clazz );
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
    private String classHierarchyToString ( String className,
                                            JavaClassCacheEntry entry, Map<String, JavaClassCacheEntry> javaClassCache ) {
        JavaClassCacheEntry start = entry;
        StringBuilder msg = new StringBuilder ( className );
        msg.append ( "->" );
        String parentName = entry.getSuperclassName();
        JavaClassCacheEntry parent = javaClassCache.get ( parentName );
        int count = 0;
        while ( count < 100 && parent != null && parent != start ) {
            msg.append ( parentName );
            msg.append ( "->" );
            count ++;
            parentName = parent.getSuperclassName();
            parent = javaClassCache.get ( parentName );
        }
        msg.append ( parentName );
        return msg.toString();
    }
    private void populateJavaClassCache ( String className, JavaClass javaClass,
                                          Map<String, JavaClassCacheEntry> javaClassCache ) {
        if ( javaClassCache.containsKey ( className ) ) {
            return;
        }
        javaClassCache.put ( className, new JavaClassCacheEntry ( javaClass ) );
        populateJavaClassCache ( javaClass.getSuperclassName(), javaClassCache );
        for ( String iterface : javaClass.getInterfaceNames() ) {
            populateJavaClassCache ( iterface, javaClassCache );
        }
    }
    private void populateJavaClassCache ( String className,
                                          Map<String, JavaClassCacheEntry> javaClassCache ) {
        if ( !javaClassCache.containsKey ( className ) ) {
            String name = className.replace ( '.', '/' ) + ".class";
            try ( InputStream is = context.getLoader().getClassLoader().getResourceAsStream ( name ) ) {
                if ( is == null ) {
                    return;
                }
                ClassParser parser = new ClassParser ( is );
                JavaClass clazz = parser.parse();
                populateJavaClassCache ( clazz.getClassName(), clazz, javaClassCache );
            } catch ( ClassFormatException e ) {
                log.debug ( sm.getString ( "contextConfig.invalidSciHandlesTypes",
                                           className ), e );
            } catch ( IOException e ) {
                log.debug ( sm.getString ( "contextConfig.invalidSciHandlesTypes",
                                           className ), e );
            }
        }
    }
    private void populateSCIsForCacheEntry ( JavaClassCacheEntry cacheEntry,
            Map<String, JavaClassCacheEntry> javaClassCache ) {
        Set<ServletContainerInitializer> result = new HashSet<>();
        String superClassName = cacheEntry.getSuperclassName();
        JavaClassCacheEntry superClassCacheEntry =
            javaClassCache.get ( superClassName );
        if ( cacheEntry.equals ( superClassCacheEntry ) ) {
            cacheEntry.setSciSet ( EMPTY_SCI_SET );
            return;
        }
        if ( superClassCacheEntry != null ) {
            if ( superClassCacheEntry.getSciSet() == null ) {
                populateSCIsForCacheEntry ( superClassCacheEntry, javaClassCache );
            }
            result.addAll ( superClassCacheEntry.getSciSet() );
        }
        result.addAll ( getSCIsForClass ( superClassName ) );
        for ( String interfaceName : cacheEntry.getInterfaceNames() ) {
            JavaClassCacheEntry interfaceEntry =
                javaClassCache.get ( interfaceName );
            if ( interfaceEntry != null ) {
                if ( interfaceEntry.getSciSet() == null ) {
                    populateSCIsForCacheEntry ( interfaceEntry, javaClassCache );
                }
                result.addAll ( interfaceEntry.getSciSet() );
            }
            result.addAll ( getSCIsForClass ( interfaceName ) );
        }
        cacheEntry.setSciSet ( result.isEmpty() ? EMPTY_SCI_SET : result );
    }
    private Set<ServletContainerInitializer> getSCIsForClass ( String className ) {
        for ( Map.Entry<Class<?>, Set<ServletContainerInitializer>> entry :
                typeInitializerMap.entrySet() ) {
            Class<?> clazz = entry.getKey();
            if ( !clazz.isAnnotation() ) {
                if ( clazz.getName().equals ( className ) ) {
                    return entry.getValue();
                }
            }
        }
        return EMPTY_SCI_SET;
    }
    private static final String getClassName ( String internalForm ) {
        if ( !internalForm.startsWith ( "L" ) ) {
            return internalForm;
        }
        return internalForm.substring ( 1,
                                        internalForm.length() - 1 ).replace ( '/', '.' );
    }
    protected void processAnnotationWebServlet ( String className,
            AnnotationEntry ae, WebXml fragment ) {
        String servletName = null;
        List<ElementValuePair> evps = ae.getElementValuePairs();
        for ( ElementValuePair evp : evps ) {
            String name = evp.getNameString();
            if ( "name".equals ( name ) ) {
                servletName = evp.getValue().stringifyValue();
                break;
            }
        }
        if ( servletName == null ) {
            servletName = className;
        }
        ServletDef servletDef = fragment.getServlets().get ( servletName );
        boolean isWebXMLservletDef;
        if ( servletDef == null ) {
            servletDef = new ServletDef();
            servletDef.setServletName ( servletName );
            servletDef.setServletClass ( className );
            isWebXMLservletDef = false;
        } else {
            isWebXMLservletDef = true;
        }
        boolean urlPatternsSet = false;
        String[] urlPatterns = null;
        for ( ElementValuePair evp : evps ) {
            String name = evp.getNameString();
            if ( "value".equals ( name ) || "urlPatterns".equals ( name ) ) {
                if ( urlPatternsSet ) {
                    throw new IllegalArgumentException ( sm.getString (
                            "contextConfig.urlPatternValue", "WebServlet", className ) );
                }
                urlPatternsSet = true;
                urlPatterns = processAnnotationsStringArray ( evp.getValue() );
            } else if ( "description".equals ( name ) ) {
                if ( servletDef.getDescription() == null ) {
                    servletDef.setDescription ( evp.getValue().stringifyValue() );
                }
            } else if ( "displayName".equals ( name ) ) {
                if ( servletDef.getDisplayName() == null ) {
                    servletDef.setDisplayName ( evp.getValue().stringifyValue() );
                }
            } else if ( "largeIcon".equals ( name ) ) {
                if ( servletDef.getLargeIcon() == null ) {
                    servletDef.setLargeIcon ( evp.getValue().stringifyValue() );
                }
            } else if ( "smallIcon".equals ( name ) ) {
                if ( servletDef.getSmallIcon() == null ) {
                    servletDef.setSmallIcon ( evp.getValue().stringifyValue() );
                }
            } else if ( "asyncSupported".equals ( name ) ) {
                if ( servletDef.getAsyncSupported() == null ) {
                    servletDef.setAsyncSupported ( evp.getValue()
                                                   .stringifyValue() );
                }
            } else if ( "loadOnStartup".equals ( name ) ) {
                if ( servletDef.getLoadOnStartup() == null ) {
                    servletDef
                    .setLoadOnStartup ( evp.getValue().stringifyValue() );
                }
            } else if ( "initParams".equals ( name ) ) {
                Map<String, String> initParams = processAnnotationWebInitParams ( evp
                                                 .getValue() );
                if ( isWebXMLservletDef ) {
                    Map<String, String> webXMLInitParams = servletDef
                                                           .getParameterMap();
                    for ( Map.Entry<String, String> entry : initParams
                            .entrySet() ) {
                        if ( webXMLInitParams.get ( entry.getKey() ) == null ) {
                            servletDef.addInitParameter ( entry.getKey(), entry
                                                          .getValue() );
                        }
                    }
                } else {
                    for ( Map.Entry<String, String> entry : initParams
                            .entrySet() ) {
                        servletDef.addInitParameter ( entry.getKey(), entry
                                                      .getValue() );
                    }
                }
            }
        }
        if ( !isWebXMLservletDef && urlPatterns != null ) {
            fragment.addServlet ( servletDef );
        }
        if ( urlPatterns != null ) {
            if ( !fragment.getServletMappings().containsValue ( servletName ) ) {
                for ( String urlPattern : urlPatterns ) {
                    fragment.addServletMapping ( urlPattern, servletName );
                }
            }
        }
    }
    protected void processAnnotationWebFilter ( String className,
            AnnotationEntry ae, WebXml fragment ) {
        String filterName = null;
        List<ElementValuePair> evps = ae.getElementValuePairs();
        for ( ElementValuePair evp : evps ) {
            String name = evp.getNameString();
            if ( "filterName".equals ( name ) ) {
                filterName = evp.getValue().stringifyValue();
                break;
            }
        }
        if ( filterName == null ) {
            filterName = className;
        }
        FilterDef filterDef = fragment.getFilters().get ( filterName );
        FilterMap filterMap = new FilterMap();
        boolean isWebXMLfilterDef;
        if ( filterDef == null ) {
            filterDef = new FilterDef();
            filterDef.setFilterName ( filterName );
            filterDef.setFilterClass ( className );
            isWebXMLfilterDef = false;
        } else {
            isWebXMLfilterDef = true;
        }
        boolean urlPatternsSet = false;
        boolean servletNamesSet = false;
        boolean dispatchTypesSet = false;
        String[] urlPatterns = null;
        for ( ElementValuePair evp : evps ) {
            String name = evp.getNameString();
            if ( "value".equals ( name ) || "urlPatterns".equals ( name ) ) {
                if ( urlPatternsSet ) {
                    throw new IllegalArgumentException ( sm.getString (
                            "contextConfig.urlPatternValue", "WebFilter", className ) );
                }
                urlPatterns = processAnnotationsStringArray ( evp.getValue() );
                urlPatternsSet = urlPatterns.length > 0;
                for ( String urlPattern : urlPatterns ) {
                    filterMap.addURLPattern ( urlPattern );
                }
            } else if ( "servletNames".equals ( name ) ) {
                String[] servletNames = processAnnotationsStringArray ( evp
                                        .getValue() );
                servletNamesSet = servletNames.length > 0;
                for ( String servletName : servletNames ) {
                    filterMap.addServletName ( servletName );
                }
            } else if ( "dispatcherTypes".equals ( name ) ) {
                String[] dispatcherTypes = processAnnotationsStringArray ( evp
                                           .getValue() );
                dispatchTypesSet = dispatcherTypes.length > 0;
                for ( String dispatcherType : dispatcherTypes ) {
                    filterMap.setDispatcher ( dispatcherType );
                }
            } else if ( "description".equals ( name ) ) {
                if ( filterDef.getDescription() == null ) {
                    filterDef.setDescription ( evp.getValue().stringifyValue() );
                }
            } else if ( "displayName".equals ( name ) ) {
                if ( filterDef.getDisplayName() == null ) {
                    filterDef.setDisplayName ( evp.getValue().stringifyValue() );
                }
            } else if ( "largeIcon".equals ( name ) ) {
                if ( filterDef.getLargeIcon() == null ) {
                    filterDef.setLargeIcon ( evp.getValue().stringifyValue() );
                }
            } else if ( "smallIcon".equals ( name ) ) {
                if ( filterDef.getSmallIcon() == null ) {
                    filterDef.setSmallIcon ( evp.getValue().stringifyValue() );
                }
            } else if ( "asyncSupported".equals ( name ) ) {
                if ( filterDef.getAsyncSupported() == null ) {
                    filterDef
                    .setAsyncSupported ( evp.getValue().stringifyValue() );
                }
            } else if ( "initParams".equals ( name ) ) {
                Map<String, String> initParams = processAnnotationWebInitParams ( evp
                                                 .getValue() );
                if ( isWebXMLfilterDef ) {
                    Map<String, String> webXMLInitParams = filterDef
                                                           .getParameterMap();
                    for ( Map.Entry<String, String> entry : initParams
                            .entrySet() ) {
                        if ( webXMLInitParams.get ( entry.getKey() ) == null ) {
                            filterDef.addInitParameter ( entry.getKey(), entry
                                                         .getValue() );
                        }
                    }
                } else {
                    for ( Map.Entry<String, String> entry : initParams
                            .entrySet() ) {
                        filterDef.addInitParameter ( entry.getKey(), entry
                                                     .getValue() );
                    }
                }
            }
        }
        if ( !isWebXMLfilterDef ) {
            fragment.addFilter ( filterDef );
            if ( urlPatternsSet || servletNamesSet ) {
                filterMap.setFilterName ( filterName );
                fragment.addFilterMapping ( filterMap );
            }
        }
        if ( urlPatternsSet || dispatchTypesSet ) {
            Set<FilterMap> fmap = fragment.getFilterMappings();
            FilterMap descMap = null;
            for ( FilterMap map : fmap ) {
                if ( filterName.equals ( map.getFilterName() ) ) {
                    descMap = map;
                    break;
                }
            }
            if ( descMap != null ) {
                String[] urlsPatterns = descMap.getURLPatterns();
                if ( urlPatternsSet
                        && ( urlsPatterns == null || urlsPatterns.length == 0 ) ) {
                    for ( String urlPattern : filterMap.getURLPatterns() ) {
                        descMap.addURLPattern ( urlPattern );
                    }
                }
                String[] dispatcherNames = descMap.getDispatcherNames();
                if ( dispatchTypesSet
                        && ( dispatcherNames == null || dispatcherNames.length == 0 ) ) {
                    for ( String dis : filterMap.getDispatcherNames() ) {
                        descMap.setDispatcher ( dis );
                    }
                }
            }
        }
    }
    protected String[] processAnnotationsStringArray ( ElementValue ev ) {
        ArrayList<String> values = new ArrayList<>();
        if ( ev instanceof ArrayElementValue ) {
            ElementValue[] arrayValues =
                ( ( ArrayElementValue ) ev ).getElementValuesArray();
            for ( ElementValue value : arrayValues ) {
                values.add ( value.stringifyValue() );
            }
        } else {
            values.add ( ev.stringifyValue() );
        }
        String[] result = new String[values.size()];
        return values.toArray ( result );
    }
    protected Map<String, String> processAnnotationWebInitParams (
        ElementValue ev ) {
        Map<String, String> result = new HashMap<>();
        if ( ev instanceof ArrayElementValue ) {
            ElementValue[] arrayValues =
                ( ( ArrayElementValue ) ev ).getElementValuesArray();
            for ( ElementValue value : arrayValues ) {
                if ( value instanceof AnnotationElementValue ) {
                    List<ElementValuePair> evps = ( ( AnnotationElementValue ) value )
                                                  .getAnnotationEntry().getElementValuePairs();
                    String initParamName = null;
                    String initParamValue = null;
                    for ( ElementValuePair evp : evps ) {
                        if ( "name".equals ( evp.getNameString() ) ) {
                            initParamName = evp.getValue().stringifyValue();
                        } else if ( "value".equals ( evp.getNameString() ) ) {
                            initParamValue = evp.getValue().stringifyValue();
                        } else {
                        }
                    }
                    result.put ( initParamName, initParamValue );
                }
            }
        }
        return result;
    }
    private static class DefaultWebXmlCacheEntry {
        private final WebXml webXml;
        private final long globalTimeStamp;
        private final long hostTimeStamp;
        public DefaultWebXmlCacheEntry ( WebXml webXml, long globalTimeStamp,
                                         long hostTimeStamp ) {
            this.webXml = webXml;
            this.globalTimeStamp = globalTimeStamp;
            this.hostTimeStamp = hostTimeStamp;
        }
        public WebXml getWebXml() {
            return webXml;
        }
        public long getGlobalTimeStamp() {
            return globalTimeStamp;
        }
        public long getHostTimeStamp() {
            return hostTimeStamp;
        }
    }
    static class JavaClassCacheEntry {
        public final String superclassName;
        public final String[] interfaceNames;
        private Set<ServletContainerInitializer> sciSet = null;
        public JavaClassCacheEntry ( JavaClass javaClass ) {
            superclassName = javaClass.getSuperclassName();
            interfaceNames = javaClass.getInterfaceNames();
        }
        public String getSuperclassName() {
            return superclassName;
        }
        public String[] getInterfaceNames() {
            return interfaceNames;
        }
        public Set<ServletContainerInitializer> getSciSet() {
            return sciSet;
        }
        public void setSciSet ( Set<ServletContainerInitializer> sciSet ) {
            this.sciSet = sciSet;
        }
    }
}
