package org.apache.catalina.startup;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.management.ObjectName;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.DistributedManager;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Manager;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.util.ContextName;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.buf.UriUtil;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.res.StringManager;
public class HostConfig implements LifecycleListener {
    private static final Log log = LogFactory.getLog ( HostConfig.class );
    protected static final StringManager sm = StringManager.getManager ( HostConfig.class );
    protected static final long FILE_MODIFICATION_RESOLUTION_MS = 1000;
    protected String contextClass = "org.apache.catalina.core.StandardContext";
    protected Host host = null;
    protected ObjectName oname = null;
    protected boolean deployXML = false;
    protected boolean copyXML = false;
    protected boolean unpackWARs = false;
    protected final Map<String, DeployedApplication> deployed =
        new ConcurrentHashMap<>();
    protected final ArrayList<String> serviced = new ArrayList<>();
    protected Digester digester = createDigester ( contextClass );
    private final Object digesterLock = new Object();
    protected final Set<String> invalidWars = new HashSet<>();
    public String getContextClass() {
        return ( this.contextClass );
    }
    public void setContextClass ( String contextClass ) {
        String oldContextClass = this.contextClass;
        this.contextClass = contextClass;
        if ( !oldContextClass.equals ( contextClass ) ) {
            synchronized ( digesterLock ) {
                digester = createDigester ( getContextClass() );
            }
        }
    }
    public boolean isDeployXML() {
        return ( this.deployXML );
    }
    public void setDeployXML ( boolean deployXML ) {
        this.deployXML = deployXML;
    }
    public boolean isCopyXML() {
        return ( this.copyXML );
    }
    public void setCopyXML ( boolean copyXML ) {
        this.copyXML = copyXML;
    }
    public boolean isUnpackWARs() {
        return ( this.unpackWARs );
    }
    public void setUnpackWARs ( boolean unpackWARs ) {
        this.unpackWARs = unpackWARs;
    }
    @Override
    public void lifecycleEvent ( LifecycleEvent event ) {
        try {
            host = ( Host ) event.getLifecycle();
            if ( host instanceof StandardHost ) {
                setCopyXML ( ( ( StandardHost ) host ).isCopyXML() );
                setDeployXML ( ( ( StandardHost ) host ).isDeployXML() );
                setUnpackWARs ( ( ( StandardHost ) host ).isUnpackWARs() );
                setContextClass ( ( ( StandardHost ) host ).getContextClass() );
            }
        } catch ( ClassCastException e ) {
            log.error ( sm.getString ( "hostConfig.cce", event.getLifecycle() ), e );
            return;
        }
        if ( event.getType().equals ( Lifecycle.PERIODIC_EVENT ) ) {
            check();
        } else if ( event.getType().equals ( Lifecycle.BEFORE_START_EVENT ) ) {
            beforeStart();
        } else if ( event.getType().equals ( Lifecycle.START_EVENT ) ) {
            start();
        } else if ( event.getType().equals ( Lifecycle.STOP_EVENT ) ) {
            stop();
        }
    }
    public synchronized void addServiced ( String name ) {
        serviced.add ( name );
    }
    public synchronized boolean isServiced ( String name ) {
        return ( serviced.contains ( name ) );
    }
    public synchronized void removeServiced ( String name ) {
        serviced.remove ( name );
    }
    public long getDeploymentTime ( String name ) {
        DeployedApplication app = deployed.get ( name );
        if ( app == null ) {
            return 0L;
        }
        return app.timestamp;
    }
    public boolean isDeployed ( String name ) {
        DeployedApplication app = deployed.get ( name );
        if ( app == null ) {
            return false;
        }
        return true;
    }
    protected static Digester createDigester ( String contextClassName ) {
        Digester digester = new Digester();
        digester.setValidating ( false );
        digester.addObjectCreate ( "Context", contextClassName, "className" );
        digester.addSetProperties ( "Context" );
        return ( digester );
    }
    protected File returnCanonicalPath ( String path ) {
        File file = new File ( path );
        if ( !file.isAbsolute() ) {
            file = new File ( host.getCatalinaBase(), path );
        }
        try {
            return file.getCanonicalFile();
        } catch ( IOException e ) {
            return file;
        }
    }
    public String getConfigBaseName() {
        return host.getConfigBaseFile().getAbsolutePath();
    }
    protected void deployApps() {
        File appBase = host.getAppBaseFile();
        File configBase = host.getConfigBaseFile();
        String[] filteredAppPaths = filterAppPaths ( appBase.list() );
        deployDescriptors ( configBase, configBase.list() );
        deployWARs ( appBase, filteredAppPaths );
        deployDirectories ( appBase, filteredAppPaths );
    }
    protected String[] filterAppPaths ( String[] unfilteredAppPaths ) {
        Pattern filter = host.getDeployIgnorePattern();
        if ( filter == null || unfilteredAppPaths == null ) {
            return unfilteredAppPaths;
        }
        List<String> filteredList = new ArrayList<>();
        Matcher matcher = null;
        for ( String appPath : unfilteredAppPaths ) {
            if ( matcher == null ) {
                matcher = filter.matcher ( appPath );
            } else {
                matcher.reset ( appPath );
            }
            if ( matcher.matches() ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "hostConfig.ignorePath", appPath ) );
                }
            } else {
                filteredList.add ( appPath );
            }
        }
        return filteredList.toArray ( new String[filteredList.size()] );
    }
    protected void deployApps ( String name ) {
        File appBase = host.getAppBaseFile();
        File configBase = host.getConfigBaseFile();
        ContextName cn = new ContextName ( name, false );
        String baseName = cn.getBaseName();
        if ( deploymentExists ( cn.getName() ) ) {
            return;
        }
        File xml = new File ( configBase, baseName + ".xml" );
        if ( xml.exists() ) {
            deployDescriptor ( cn, xml );
            return;
        }
        File war = new File ( appBase, baseName + ".war" );
        if ( war.exists() ) {
            deployWAR ( cn, war );
            return;
        }
        File dir = new File ( appBase, baseName );
        if ( dir.exists() ) {
            deployDirectory ( cn, dir );
        }
    }
    protected void deployDescriptors ( File configBase, String[] files ) {
        if ( files == null ) {
            return;
        }
        ExecutorService es = host.getStartStopExecutor();
        List<Future<?>> results = new ArrayList<>();
        for ( int i = 0; i < files.length; i++ ) {
            File contextXml = new File ( configBase, files[i] );
            if ( files[i].toLowerCase ( Locale.ENGLISH ).endsWith ( ".xml" ) ) {
                ContextName cn = new ContextName ( files[i], true );
                if ( isServiced ( cn.getName() ) || deploymentExists ( cn.getName() ) ) {
                    continue;
                }
                results.add (
                    es.submit ( new DeployDescriptor ( this, cn, contextXml ) ) );
            }
        }
        for ( Future<?> result : results ) {
            try {
                result.get();
            } catch ( Exception e ) {
                log.error ( sm.getString (
                                "hostConfig.deployDescriptor.threaded.error" ), e );
            }
        }
    }
    @SuppressWarnings ( "null" )
    protected void deployDescriptor ( ContextName cn, File contextXml ) {
        DeployedApplication deployedApp =
            new DeployedApplication ( cn.getName(), true );
        long startTime = 0;
        if ( log.isInfoEnabled() ) {
            startTime = System.currentTimeMillis();
            log.info ( sm.getString ( "hostConfig.deployDescriptor",
                                      contextXml.getAbsolutePath() ) );
        }
        Context context = null;
        boolean isExternalWar = false;
        boolean isExternal = false;
        File expandedDocBase = null;
        try ( FileInputStream fis = new FileInputStream ( contextXml ) ) {
            synchronized ( digesterLock ) {
                try {
                    context = ( Context ) digester.parse ( fis );
                } catch ( Exception e ) {
                    log.error ( sm.getString (
                                    "hostConfig.deployDescriptor.error",
                                    contextXml.getAbsolutePath() ), e );
                } finally {
                    digester.reset();
                    if ( context == null ) {
                        context = new FailedContext();
                    }
                }
            }
            Class<?> clazz = Class.forName ( host.getConfigClass() );
            LifecycleListener listener =
                ( LifecycleListener ) clazz.newInstance();
            context.addLifecycleListener ( listener );
            context.setConfigFile ( contextXml.toURI().toURL() );
            context.setName ( cn.getName() );
            context.setPath ( cn.getPath() );
            context.setWebappVersion ( cn.getVersion() );
            if ( context.getDocBase() != null ) {
                File docBase = new File ( context.getDocBase() );
                if ( !docBase.isAbsolute() ) {
                    docBase = new File ( host.getAppBaseFile(), context.getDocBase() );
                }
                if ( !docBase.getCanonicalPath().startsWith (
                            host.getAppBaseFile().getAbsolutePath() + File.separator ) ) {
                    isExternal = true;
                    deployedApp.redeployResources.put (
                        contextXml.getAbsolutePath(),
                        Long.valueOf ( contextXml.lastModified() ) );
                    deployedApp.redeployResources.put ( docBase.getAbsolutePath(),
                                                        Long.valueOf ( docBase.lastModified() ) );
                    if ( docBase.getAbsolutePath().toLowerCase ( Locale.ENGLISH ).endsWith ( ".war" ) ) {
                        isExternalWar = true;
                    }
                } else {
                    log.warn ( sm.getString ( "hostConfig.deployDescriptor.localDocBaseSpecified",
                                              docBase ) );
                    context.setDocBase ( null );
                }
            }
            host.addChild ( context );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            log.error ( sm.getString ( "hostConfig.deployDescriptor.error",
                                       contextXml.getAbsolutePath() ), t );
        } finally {
            expandedDocBase = new File ( host.getAppBaseFile(), cn.getBaseName() );
            if ( context.getDocBase() != null
                    && !context.getDocBase().toLowerCase ( Locale.ENGLISH ).endsWith ( ".war" ) ) {
                expandedDocBase = new File ( context.getDocBase() );
                if ( !expandedDocBase.isAbsolute() ) {
                    expandedDocBase = new File ( host.getAppBaseFile(), context.getDocBase() );
                }
            }
            boolean unpackWAR = unpackWARs;
            if ( unpackWAR && context instanceof StandardContext ) {
                unpackWAR = ( ( StandardContext ) context ).getUnpackWAR();
            }
            if ( isExternalWar ) {
                if ( unpackWAR ) {
                    deployedApp.redeployResources.put ( expandedDocBase.getAbsolutePath(),
                                                        Long.valueOf ( expandedDocBase.lastModified() ) );
                    addWatchedResources ( deployedApp, expandedDocBase.getAbsolutePath(), context );
                } else {
                    addWatchedResources ( deployedApp, null, context );
                }
            } else {
                if ( !isExternal ) {
                    File warDocBase = new File ( expandedDocBase.getAbsolutePath() + ".war" );
                    if ( warDocBase.exists() ) {
                        deployedApp.redeployResources.put ( warDocBase.getAbsolutePath(),
                                                            Long.valueOf ( warDocBase.lastModified() ) );
                    } else {
                        deployedApp.redeployResources.put (
                            warDocBase.getAbsolutePath(),
                            Long.valueOf ( 0 ) );
                    }
                }
                if ( unpackWAR ) {
                    deployedApp.redeployResources.put ( expandedDocBase.getAbsolutePath(),
                                                        Long.valueOf ( expandedDocBase.lastModified() ) );
                    addWatchedResources ( deployedApp,
                                          expandedDocBase.getAbsolutePath(), context );
                } else {
                    addWatchedResources ( deployedApp, null, context );
                }
                if ( !isExternal ) {
                    deployedApp.redeployResources.put (
                        contextXml.getAbsolutePath(),
                        Long.valueOf ( contextXml.lastModified() ) );
                }
            }
            addGlobalRedeployResources ( deployedApp );
        }
        if ( host.findChild ( context.getName() ) != null ) {
            deployed.put ( context.getName(), deployedApp );
        }
        if ( log.isInfoEnabled() ) {
            log.info ( sm.getString ( "hostConfig.deployDescriptor.finished",
                                      contextXml.getAbsolutePath(), Long.valueOf ( System.currentTimeMillis() - startTime ) ) );
        }
    }
    protected void deployWARs ( File appBase, String[] files ) {
        if ( files == null ) {
            return;
        }
        ExecutorService es = host.getStartStopExecutor();
        List<Future<?>> results = new ArrayList<>();
        for ( int i = 0; i < files.length; i++ ) {
            if ( files[i].equalsIgnoreCase ( "META-INF" ) ) {
                continue;
            }
            if ( files[i].equalsIgnoreCase ( "WEB-INF" ) ) {
                continue;
            }
            File war = new File ( appBase, files[i] );
            if ( files[i].toLowerCase ( Locale.ENGLISH ).endsWith ( ".war" ) &&
                    war.isFile() && !invalidWars.contains ( files[i] ) ) {
                ContextName cn = new ContextName ( files[i], true );
                if ( isServiced ( cn.getName() ) ) {
                    continue;
                }
                if ( deploymentExists ( cn.getName() ) ) {
                    DeployedApplication app = deployed.get ( cn.getName() );
                    boolean unpackWAR = unpackWARs;
                    if ( unpackWAR && host.findChild ( cn.getName() ) instanceof StandardContext ) {
                        unpackWAR = ( ( StandardContext ) host.findChild ( cn.getName() ) ).getUnpackWAR();
                    }
                    if ( !unpackWAR && app != null ) {
                        File dir = new File ( appBase, cn.getBaseName() );
                        if ( dir.exists() ) {
                            if ( !app.loggedDirWarning ) {
                                log.warn ( sm.getString (
                                               "hostConfig.deployWar.hiddenDir",
                                               dir.getAbsoluteFile(),
                                               war.getAbsoluteFile() ) );
                                app.loggedDirWarning = true;
                            }
                        } else {
                            app.loggedDirWarning = false;
                        }
                    }
                    continue;
                }
                if ( !validateContextPath ( appBase, cn.getBaseName() ) ) {
                    log.error ( sm.getString (
                                    "hostConfig.illegalWarName", files[i] ) );
                    invalidWars.add ( files[i] );
                    continue;
                }
                results.add ( es.submit ( new DeployWar ( this, cn, war ) ) );
            }
        }
        for ( Future<?> result : results ) {
            try {
                result.get();
            } catch ( Exception e ) {
                log.error ( sm.getString (
                                "hostConfig.deployWar.threaded.error" ), e );
            }
        }
    }
    private boolean validateContextPath ( File appBase, String contextPath ) {
        StringBuilder docBase;
        String canonicalDocBase = null;
        try {
            String canonicalAppBase = appBase.getCanonicalPath();
            docBase = new StringBuilder ( canonicalAppBase );
            if ( canonicalAppBase.endsWith ( File.separator ) ) {
                docBase.append ( contextPath.substring ( 1 ).replace (
                                     '/', File.separatorChar ) );
            } else {
                docBase.append ( contextPath.replace ( '/', File.separatorChar ) );
            }
            canonicalDocBase =
                ( new File ( docBase.toString() ) ).getCanonicalPath();
            if ( canonicalDocBase.endsWith ( File.separator ) ) {
                docBase.append ( File.separator );
            }
        } catch ( IOException ioe ) {
            return false;
        }
        return canonicalDocBase.equals ( docBase.toString() );
    }
    protected void deployWAR ( ContextName cn, File war ) {
        File xml = new File ( host.getAppBaseFile(),
                              cn.getBaseName() + "/" + Constants.ApplicationContextXml );
        File warTracker = new File ( host.getAppBaseFile(),
                                     cn.getBaseName() + "/" + Constants.WarTracker );
        boolean xmlInWar = false;
        try ( JarFile jar = new JarFile ( war ) ) {
            JarEntry entry = jar.getJarEntry ( Constants.ApplicationContextXml );
            if ( entry != null ) {
                xmlInWar = true;
            }
        } catch ( IOException e ) {
        }
        boolean useXml = false;
        if ( xml.exists() && unpackWARs &&
                ( !warTracker.exists() || warTracker.lastModified() == war.lastModified() ) ) {
            useXml = true;
        }
        Context context = null;
        try {
            if ( deployXML && useXml && !copyXML ) {
                synchronized ( digesterLock ) {
                    try {
                        context = ( Context ) digester.parse ( xml );
                    } catch ( Exception e ) {
                        log.error ( sm.getString (
                                        "hostConfig.deployDescriptor.error",
                                        war.getAbsolutePath() ), e );
                    } finally {
                        digester.reset();
                        if ( context == null ) {
                            context = new FailedContext();
                        }
                    }
                }
                context.setConfigFile ( xml.toURI().toURL() );
            } else if ( deployXML && xmlInWar ) {
                synchronized ( digesterLock ) {
                    try ( JarFile jar = new JarFile ( war ) ) {
                        JarEntry entry = jar.getJarEntry ( Constants.ApplicationContextXml );
                        try ( InputStream istream = jar.getInputStream ( entry ) ) {
                            context = ( Context ) digester.parse ( istream );
                        }
                    } catch ( Exception e ) {
                        log.error ( sm.getString (
                                        "hostConfig.deployDescriptor.error",
                                        war.getAbsolutePath() ), e );
                    } finally {
                        digester.reset();
                        if ( context == null ) {
                            context = new FailedContext();
                        }
                        context.setConfigFile (
                            UriUtil.buildJarUrl ( war, Constants.ApplicationContextXml ) );
                    }
                }
            } else if ( !deployXML && xmlInWar ) {
                log.error ( sm.getString ( "hostConfig.deployDescriptor.blocked",
                                           cn.getPath(), Constants.ApplicationContextXml,
                                           new File ( host.getConfigBaseFile(), cn.getBaseName() + ".xml" ) ) );
            } else {
                context = ( Context ) Class.forName ( contextClass ).newInstance();
            }
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            log.error ( sm.getString ( "hostConfig.deployWar.error",
                                       war.getAbsolutePath() ), t );
        } finally {
            if ( context == null ) {
                context = new FailedContext();
            }
        }
        boolean copyThisXml = false;
        if ( deployXML ) {
            if ( host instanceof StandardHost ) {
                copyThisXml = ( ( StandardHost ) host ).isCopyXML();
            }
            if ( !copyThisXml && context instanceof StandardContext ) {
                copyThisXml = ( ( StandardContext ) context ).getCopyXML();
            }
            if ( xmlInWar && copyThisXml ) {
                xml = new File ( host.getConfigBaseFile(),
                                 cn.getBaseName() + ".xml" );
                try ( JarFile jar = new JarFile ( war ) ) {
                    JarEntry entry = jar.getJarEntry ( Constants.ApplicationContextXml );
                    try ( InputStream istream = jar.getInputStream ( entry );
                                FileOutputStream fos = new FileOutputStream ( xml );
                                BufferedOutputStream ostream = new BufferedOutputStream ( fos, 1024 ) ) {
                        byte buffer[] = new byte[1024];
                        while ( true ) {
                            int n = istream.read ( buffer );
                            if ( n < 0 ) {
                                break;
                            }
                            ostream.write ( buffer, 0, n );
                        }
                        ostream.flush();
                    }
                } catch ( IOException e ) {
                }
            }
        }
        DeployedApplication deployedApp = new DeployedApplication ( cn.getName(),
                xml.exists() && deployXML && copyThisXml );
        long startTime = 0;
        if ( log.isInfoEnabled() ) {
            startTime = System.currentTimeMillis();
            log.info ( sm.getString ( "hostConfig.deployWar",
                                      war.getAbsolutePath() ) );
        }
        try {
            deployedApp.redeployResources.put
            ( war.getAbsolutePath(), Long.valueOf ( war.lastModified() ) );
            if ( deployXML && xml.exists() && copyThisXml ) {
                deployedApp.redeployResources.put ( xml.getAbsolutePath(),
                                                    Long.valueOf ( xml.lastModified() ) );
            } else {
                deployedApp.redeployResources.put (
                    ( new File ( host.getConfigBaseFile(),
                                 cn.getBaseName() + ".xml" ) ).getAbsolutePath(),
                    Long.valueOf ( 0 ) );
            }
            Class<?> clazz = Class.forName ( host.getConfigClass() );
            LifecycleListener listener =
                ( LifecycleListener ) clazz.newInstance();
            context.addLifecycleListener ( listener );
            context.setName ( cn.getName() );
            context.setPath ( cn.getPath() );
            context.setWebappVersion ( cn.getVersion() );
            context.setDocBase ( cn.getBaseName() + ".war" );
            host.addChild ( context );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            log.error ( sm.getString ( "hostConfig.deployWar.error",
                                       war.getAbsolutePath() ), t );
        } finally {
            boolean unpackWAR = unpackWARs;
            if ( unpackWAR && context instanceof StandardContext ) {
                unpackWAR = ( ( StandardContext ) context ).getUnpackWAR();
            }
            if ( unpackWAR && context.getDocBase() != null ) {
                File docBase = new File ( host.getAppBaseFile(), cn.getBaseName() );
                deployedApp.redeployResources.put ( docBase.getAbsolutePath(),
                                                    Long.valueOf ( docBase.lastModified() ) );
                addWatchedResources ( deployedApp, docBase.getAbsolutePath(),
                                      context );
                if ( deployXML && !copyThisXml && ( xmlInWar || xml.exists() ) ) {
                    deployedApp.redeployResources.put ( xml.getAbsolutePath(),
                                                        Long.valueOf ( xml.lastModified() ) );
                }
            } else {
                addWatchedResources ( deployedApp, null, context );
            }
            addGlobalRedeployResources ( deployedApp );
        }
        deployed.put ( cn.getName(), deployedApp );
        if ( log.isInfoEnabled() ) {
            log.info ( sm.getString ( "hostConfig.deployWar.finished",
                                      war.getAbsolutePath(), Long.valueOf ( System.currentTimeMillis() - startTime ) ) );
        }
    }
    protected void deployDirectories ( File appBase, String[] files ) {
        if ( files == null ) {
            return;
        }
        ExecutorService es = host.getStartStopExecutor();
        List<Future<?>> results = new ArrayList<>();
        for ( int i = 0; i < files.length; i++ ) {
            if ( files[i].equalsIgnoreCase ( "META-INF" ) ) {
                continue;
            }
            if ( files[i].equalsIgnoreCase ( "WEB-INF" ) ) {
                continue;
            }
            File dir = new File ( appBase, files[i] );
            if ( dir.isDirectory() ) {
                ContextName cn = new ContextName ( files[i], false );
                if ( isServiced ( cn.getName() ) || deploymentExists ( cn.getName() ) ) {
                    continue;
                }
                results.add ( es.submit ( new DeployDirectory ( this, cn, dir ) ) );
            }
        }
        for ( Future<?> result : results ) {
            try {
                result.get();
            } catch ( Exception e ) {
                log.error ( sm.getString (
                                "hostConfig.deployDir.threaded.error" ), e );
            }
        }
    }
    protected void deployDirectory ( ContextName cn, File dir ) {
        long startTime = 0;
        if ( log.isInfoEnabled() ) {
            startTime = System.currentTimeMillis();
            log.info ( sm.getString ( "hostConfig.deployDir",
                                      dir.getAbsolutePath() ) );
        }
        Context context = null;
        File xml = new File ( dir, Constants.ApplicationContextXml );
        File xmlCopy =
            new File ( host.getConfigBaseFile(), cn.getBaseName() + ".xml" );
        DeployedApplication deployedApp;
        boolean copyThisXml = copyXML;
        try {
            if ( deployXML && xml.exists() ) {
                synchronized ( digesterLock ) {
                    try {
                        context = ( Context ) digester.parse ( xml );
                    } catch ( Exception e ) {
                        log.error ( sm.getString (
                                        "hostConfig.deployDescriptor.error",
                                        xml ), e );
                        context = new FailedContext();
                    } finally {
                        digester.reset();
                        if ( context == null ) {
                            context = new FailedContext();
                        }
                    }
                }
                if ( copyThisXml == false && context instanceof StandardContext ) {
                    copyThisXml = ( ( StandardContext ) context ).getCopyXML();
                }
                if ( copyThisXml ) {
                    Files.copy ( xml.toPath(), xmlCopy.toPath() );
                    context.setConfigFile ( xmlCopy.toURI().toURL() );
                } else {
                    context.setConfigFile ( xml.toURI().toURL() );
                }
            } else if ( !deployXML && xml.exists() ) {
                log.error ( sm.getString ( "hostConfig.deployDescriptor.blocked",
                                           cn.getPath(), xml, xmlCopy ) );
                context = new FailedContext();
            } else {
                context = ( Context ) Class.forName ( contextClass ).newInstance();
            }
            Class<?> clazz = Class.forName ( host.getConfigClass() );
            LifecycleListener listener =
                ( LifecycleListener ) clazz.newInstance();
            context.addLifecycleListener ( listener );
            context.setName ( cn.getName() );
            context.setPath ( cn.getPath() );
            context.setWebappVersion ( cn.getVersion() );
            context.setDocBase ( cn.getBaseName() );
            host.addChild ( context );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            log.error ( sm.getString ( "hostConfig.deployDir.error",
                                       dir.getAbsolutePath() ), t );
        } finally {
            deployedApp = new DeployedApplication ( cn.getName(),
                                                    xml.exists() && deployXML && copyThisXml );
            deployedApp.redeployResources.put ( dir.getAbsolutePath() + ".war",
                                                Long.valueOf ( 0 ) );
            deployedApp.redeployResources.put ( dir.getAbsolutePath(),
                                                Long.valueOf ( dir.lastModified() ) );
            if ( deployXML && xml.exists() ) {
                if ( copyThisXml ) {
                    deployedApp.redeployResources.put (
                        xmlCopy.getAbsolutePath(),
                        Long.valueOf ( xmlCopy.lastModified() ) );
                } else {
                    deployedApp.redeployResources.put (
                        xml.getAbsolutePath(),
                        Long.valueOf ( xml.lastModified() ) );
                    deployedApp.redeployResources.put (
                        xmlCopy.getAbsolutePath(),
                        Long.valueOf ( 0 ) );
                }
            } else {
                deployedApp.redeployResources.put (
                    xmlCopy.getAbsolutePath(),
                    Long.valueOf ( 0 ) );
                if ( !xml.exists() ) {
                    deployedApp.redeployResources.put (
                        xml.getAbsolutePath(),
                        Long.valueOf ( 0 ) );
                }
            }
            addWatchedResources ( deployedApp, dir.getAbsolutePath(), context );
            addGlobalRedeployResources ( deployedApp );
        }
        deployed.put ( cn.getName(), deployedApp );
        if ( log.isInfoEnabled() ) {
            log.info ( sm.getString ( "hostConfig.deployDir.finished",
                                      dir.getAbsolutePath(), Long.valueOf ( System.currentTimeMillis() - startTime ) ) );
        }
    }
    protected boolean deploymentExists ( String contextName ) {
        return ( deployed.containsKey ( contextName ) ||
                 ( host.findChild ( contextName ) != null ) );
    }
    protected void addWatchedResources ( DeployedApplication app, String docBase,
                                         Context context ) {
        File docBaseFile = null;
        if ( docBase != null ) {
            docBaseFile = new File ( docBase );
            if ( !docBaseFile.isAbsolute() ) {
                docBaseFile = new File ( host.getAppBaseFile(), docBase );
            }
        }
        String[] watchedResources = context.findWatchedResources();
        for ( int i = 0; i < watchedResources.length; i++ ) {
            File resource = new File ( watchedResources[i] );
            if ( !resource.isAbsolute() ) {
                if ( docBase != null ) {
                    resource = new File ( docBaseFile, watchedResources[i] );
                } else {
                    if ( log.isDebugEnabled() )
                        log.debug ( "Ignoring non-existent WatchedResource '" +
                                    resource.getAbsolutePath() + "'" );
                    continue;
                }
            }
            if ( log.isDebugEnabled() )
                log.debug ( "Watching WatchedResource '" +
                            resource.getAbsolutePath() + "'" );
            app.reloadResources.put ( resource.getAbsolutePath(),
                                      Long.valueOf ( resource.lastModified() ) );
        }
    }
    protected void addGlobalRedeployResources ( DeployedApplication app ) {
        File hostContextXml =
            new File ( getConfigBaseName(), Constants.HostContextXml );
        if ( hostContextXml.isFile() ) {
            app.redeployResources.put ( hostContextXml.getAbsolutePath(),
                                        Long.valueOf ( hostContextXml.lastModified() ) );
        }
        File globalContextXml =
            returnCanonicalPath ( Constants.DefaultContextXml );
        if ( globalContextXml.isFile() ) {
            app.redeployResources.put ( globalContextXml.getAbsolutePath(),
                                        Long.valueOf ( globalContextXml.lastModified() ) );
        }
    }
    protected synchronized void checkResources ( DeployedApplication app,
            boolean skipFileModificationResolutionCheck ) {
        String[] resources =
            app.redeployResources.keySet().toArray ( new String[0] );
        long currentTimeWithResolutionOffset =
            System.currentTimeMillis() - FILE_MODIFICATION_RESOLUTION_MS;
        for ( int i = 0; i < resources.length; i++ ) {
            File resource = new File ( resources[i] );
            if ( log.isDebugEnabled() )
                log.debug ( "Checking context[" + app.name +
                            "] redeploy resource " + resource );
            long lastModified =
                app.redeployResources.get ( resources[i] ).longValue();
            if ( resource.exists() || lastModified == 0 ) {
                if ( resource.lastModified() != lastModified && ( !host.getAutoDeploy() ||
                        resource.lastModified() < currentTimeWithResolutionOffset ||
                        skipFileModificationResolutionCheck ) ) {
                    if ( resource.isDirectory() ) {
                        app.redeployResources.put ( resources[i],
                                                    Long.valueOf ( resource.lastModified() ) );
                    } else if ( app.hasDescriptor &&
                                resource.getName().toLowerCase (
                                    Locale.ENGLISH ).endsWith ( ".war" ) ) {
                        Context context = ( Context ) host.findChild ( app.name );
                        String docBase = context.getDocBase();
                        if ( !docBase.toLowerCase ( Locale.ENGLISH ).endsWith ( ".war" ) ) {
                            File docBaseFile = new File ( docBase );
                            if ( !docBaseFile.isAbsolute() ) {
                                docBaseFile = new File ( host.getAppBaseFile(),
                                                         docBase );
                            }
                            reload ( app, docBaseFile, resource.getAbsolutePath() );
                        } else {
                            reload ( app, null, null );
                        }
                        app.redeployResources.put ( resources[i],
                                                    Long.valueOf ( resource.lastModified() ) );
                        app.timestamp = System.currentTimeMillis();
                        boolean unpackWAR = unpackWARs;
                        if ( unpackWAR && context instanceof StandardContext ) {
                            unpackWAR = ( ( StandardContext ) context ).getUnpackWAR();
                        }
                        if ( unpackWAR ) {
                            addWatchedResources ( app, context.getDocBase(), context );
                        } else {
                            addWatchedResources ( app, null, context );
                        }
                        return;
                    } else {
                        undeploy ( app );
                        deleteRedeployResources ( app, resources, i, false );
                        return;
                    }
                }
            } else {
                try {
                    Thread.sleep ( 500 );
                } catch ( InterruptedException e1 ) {
                }
                if ( resource.exists() ) {
                    continue;
                }
                undeploy ( app );
                deleteRedeployResources ( app, resources, i, true );
                return;
            }
        }
        resources = app.reloadResources.keySet().toArray ( new String[0] );
        boolean update = false;
        for ( int i = 0; i < resources.length; i++ ) {
            File resource = new File ( resources[i] );
            if ( log.isDebugEnabled() ) {
                log.debug ( "Checking context[" + app.name + "] reload resource " + resource );
            }
            long lastModified = app.reloadResources.get ( resources[i] ).longValue();
            if ( ( resource.lastModified() != lastModified &&
                    ( !host.getAutoDeploy() ||
                      resource.lastModified() < currentTimeWithResolutionOffset ||
                      skipFileModificationResolutionCheck ) ) ||
                    update ) {
                if ( !update ) {
                    reload ( app, null, null );
                    update = true;
                }
                app.reloadResources.put ( resources[i],
                                          Long.valueOf ( resource.lastModified() ) );
            }
            app.timestamp = System.currentTimeMillis();
        }
    }
    private void reload ( DeployedApplication app, File fileToRemove, String newDocBase ) {
        if ( log.isInfoEnabled() ) {
            log.info ( sm.getString ( "hostConfig.reload", app.name ) );
        }
        Context context = ( Context ) host.findChild ( app.name );
        if ( context.getState().isAvailable() ) {
            if ( fileToRemove != null && newDocBase != null ) {
                context.addLifecycleListener (
                    new ExpandedDirectoryRemovalListener ( fileToRemove, newDocBase ) );
            }
            context.reload();
        } else {
            if ( fileToRemove != null && newDocBase != null ) {
                ExpandWar.delete ( fileToRemove );
                context.setDocBase ( newDocBase );
            }
            try {
                context.start();
            } catch ( Exception e ) {
                log.warn ( sm.getString
                           ( "hostConfig.context.restart", app.name ), e );
            }
        }
    }
    private void undeploy ( DeployedApplication app ) {
        if ( log.isInfoEnabled() ) {
            log.info ( sm.getString ( "hostConfig.undeploy", app.name ) );
        }
        Container context = host.findChild ( app.name );
        try {
            host.removeChild ( context );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            log.warn ( sm.getString
                       ( "hostConfig.context.remove", app.name ), t );
        }
        deployed.remove ( app.name );
    }
    private void deleteRedeployResources ( DeployedApplication app, String[] resources, int i,
                                           boolean deleteReloadResources ) {
        for ( int j = i + 1; j < resources.length; j++ ) {
            File current = new File ( resources[j] );
            if ( Constants.HostContextXml.equals ( current.getName() ) ) {
                continue;
            }
            if ( isDeletableResource ( app, current ) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Delete " + current );
                }
                ExpandWar.delete ( current );
            }
        }
        if ( deleteReloadResources ) {
            String[] resources2 = app.reloadResources.keySet().toArray ( new String[0] );
            for ( int j = 0; j < resources2.length; j++ ) {
                File current = new File ( resources2[j] );
                if ( Constants.HostContextXml.equals ( current.getName() ) ) {
                    continue;
                }
                if ( isDeletableResource ( app, current ) ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( "Delete " + current );
                    }
                    ExpandWar.delete ( current );
                }
            }
        }
    }
    private boolean isDeletableResource ( DeployedApplication app, File resource ) {
        if ( !resource.isAbsolute() ) {
            log.warn ( sm.getString ( "hostConfig.resourceNotAbsolute", app.name, resource ) );
            return false;
        }
        String canonicalLocation;
        try {
            canonicalLocation = resource.getParentFile().getCanonicalPath();
        } catch ( IOException e ) {
            log.warn ( sm.getString (
                           "hostConfig.canonicalizing", resource.getParentFile(), app.name ), e );
            return false;
        }
        String canonicalAppBase;
        try {
            canonicalAppBase = host.getAppBaseFile().getCanonicalPath();
        } catch ( IOException e ) {
            log.warn ( sm.getString (
                           "hostConfig.canonicalizing", host.getAppBaseFile(), app.name ), e );
            return false;
        }
        if ( canonicalLocation.equals ( canonicalAppBase ) ) {
            return true;
        }
        String canonicalConfigBase;
        try {
            canonicalConfigBase = host.getConfigBaseFile().getCanonicalPath();
        } catch ( IOException e ) {
            log.warn ( sm.getString (
                           "hostConfig.canonicalizing", host.getConfigBaseFile(), app.name ), e );
            return false;
        }
        if ( canonicalLocation.equals ( canonicalConfigBase ) &&
                resource.getName().endsWith ( ".xml" ) ) {
            return true;
        }
        return false;
    }
    public void beforeStart() {
        if ( host.getCreateDirs() ) {
            File[] dirs = new File[] {host.getAppBaseFile(), host.getConfigBaseFile() };
            for ( int i = 0; i < dirs.length; i++ ) {
                if ( !dirs[i].mkdirs() && !dirs[i].isDirectory() ) {
                    log.error ( sm.getString ( "hostConfig.createDirs", dirs[i] ) );
                }
            }
        }
    }
    public void start() {
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "hostConfig.start" ) );
        }
        try {
            ObjectName hostON = host.getObjectName();
            oname = new ObjectName
            ( hostON.getDomain() + ":type=Deployer,host=" + host.getName() );
            Registry.getRegistry ( null, null ).registerComponent
            ( this, oname, this.getClass().getName() );
        } catch ( Exception e ) {
            log.error ( sm.getString ( "hostConfig.jmx.register", oname ), e );
        }
        if ( !host.getAppBaseFile().isDirectory() ) {
            log.error ( sm.getString ( "hostConfig.appBase", host.getName(),
                                       host.getAppBaseFile().getPath() ) );
            host.setDeployOnStartup ( false );
            host.setAutoDeploy ( false );
        }
        if ( host.getDeployOnStartup() ) {
            deployApps();
        }
    }
    public void stop() {
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "hostConfig.stop" ) );
        }
        if ( oname != null ) {
            try {
                Registry.getRegistry ( null, null ).unregisterComponent ( oname );
            } catch ( Exception e ) {
                log.error ( sm.getString ( "hostConfig.jmx.unregister", oname ), e );
            }
        }
        oname = null;
    }
    protected void check() {
        if ( host.getAutoDeploy() ) {
            DeployedApplication[] apps =
                deployed.values().toArray ( new DeployedApplication[0] );
            for ( int i = 0; i < apps.length; i++ ) {
                if ( !isServiced ( apps[i].name ) ) {
                    checkResources ( apps[i], false );
                }
            }
            if ( host.getUndeployOldVersions() ) {
                checkUndeploy();
            }
            deployApps();
        }
    }
    public void check ( String name ) {
        DeployedApplication app = deployed.get ( name );
        if ( app != null ) {
            checkResources ( app, true );
        }
        deployApps ( name );
    }
    public synchronized void checkUndeploy() {
        SortedSet<String> sortedAppNames = new TreeSet<>();
        sortedAppNames.addAll ( deployed.keySet() );
        if ( sortedAppNames.size() < 2 ) {
            return;
        }
        Iterator<String> iter = sortedAppNames.iterator();
        ContextName previous = new ContextName ( iter.next(), false );
        do {
            ContextName current = new ContextName ( iter.next(), false );
            if ( current.getPath().equals ( previous.getPath() ) ) {
                Context previousContext = ( Context ) host.findChild ( previous.getName() );
                Context currentContext = ( Context ) host.findChild ( current.getName() );
                if ( previousContext != null && currentContext != null &&
                        currentContext.getState().isAvailable() &&
                        !isServiced ( previous.getName() ) ) {
                    Manager manager = previousContext.getManager();
                    if ( manager != null ) {
                        int sessionCount;
                        if ( manager instanceof DistributedManager ) {
                            sessionCount = ( ( DistributedManager ) manager ).getActiveSessionsFull();
                        } else {
                            sessionCount = manager.getActiveSessions();
                        }
                        if ( sessionCount == 0 ) {
                            if ( log.isInfoEnabled() ) {
                                log.info ( sm.getString (
                                               "hostConfig.undeployVersion", previous.getName() ) );
                            }
                            DeployedApplication app = deployed.get ( previous.getName() );
                            String[] resources = app.redeployResources.keySet().toArray ( new String[0] );
                            undeploy ( app );
                            deleteRedeployResources ( app, resources, -1, true );
                        }
                    }
                }
            }
            previous = current;
        } while ( iter.hasNext() );
    }
    public void manageApp ( Context context )  {
        String contextName = context.getName();
        if ( deployed.containsKey ( contextName ) ) {
            return;
        }
        DeployedApplication deployedApp =
            new DeployedApplication ( contextName, false );
        boolean isWar = false;
        if ( context.getDocBase() != null ) {
            File docBase = new File ( context.getDocBase() );
            if ( !docBase.isAbsolute() ) {
                docBase = new File ( host.getAppBaseFile(), context.getDocBase() );
            }
            deployedApp.redeployResources.put ( docBase.getAbsolutePath(),
                                                Long.valueOf ( docBase.lastModified() ) );
            if ( docBase.getAbsolutePath().toLowerCase ( Locale.ENGLISH ).endsWith ( ".war" ) ) {
                isWar = true;
            }
        }
        host.addChild ( context );
        boolean unpackWAR = unpackWARs;
        if ( unpackWAR && context instanceof StandardContext ) {
            unpackWAR = ( ( StandardContext ) context ).getUnpackWAR();
        }
        if ( isWar && unpackWAR ) {
            File docBase = new File ( host.getAppBaseFile(), context.getBaseName() );
            deployedApp.redeployResources.put ( docBase.getAbsolutePath(),
                                                Long.valueOf ( docBase.lastModified() ) );
            addWatchedResources ( deployedApp, docBase.getAbsolutePath(), context );
        } else {
            addWatchedResources ( deployedApp, null, context );
        }
        deployed.put ( contextName, deployedApp );
    }
    public void unmanageApp ( String contextName ) {
        if ( isServiced ( contextName ) ) {
            deployed.remove ( contextName );
            host.removeChild ( host.findChild ( contextName ) );
        }
    }
    protected static class DeployedApplication {
        public DeployedApplication ( String name, boolean hasDescriptor ) {
            this.name = name;
            this.hasDescriptor = hasDescriptor;
        }
        public final String name;
        public final boolean hasDescriptor;
        public final LinkedHashMap<String, Long> redeployResources =
            new LinkedHashMap<>();
        public final HashMap<String, Long> reloadResources = new HashMap<>();
        public long timestamp = System.currentTimeMillis();
        public boolean loggedDirWarning = false;
    }
    private static class DeployDescriptor implements Runnable {
        private HostConfig config;
        private ContextName cn;
        private File descriptor;
        public DeployDescriptor ( HostConfig config, ContextName cn,
                                  File descriptor ) {
            this.config = config;
            this.cn = cn;
            this.descriptor = descriptor;
        }
        @Override
        public void run() {
            config.deployDescriptor ( cn, descriptor );
        }
    }
    private static class DeployWar implements Runnable {
        private HostConfig config;
        private ContextName cn;
        private File war;
        public DeployWar ( HostConfig config, ContextName cn, File war ) {
            this.config = config;
            this.cn = cn;
            this.war = war;
        }
        @Override
        public void run() {
            config.deployWAR ( cn, war );
        }
    }
    private static class DeployDirectory implements Runnable {
        private HostConfig config;
        private ContextName cn;
        private File dir;
        public DeployDirectory ( HostConfig config, ContextName cn, File dir ) {
            this.config = config;
            this.cn = cn;
            this.dir = dir;
        }
        @Override
        public void run() {
            config.deployDirectory ( cn, dir );
        }
    }
    private static class ExpandedDirectoryRemovalListener implements LifecycleListener {
        private final File toDelete;
        private final String newDocBase;
        public ExpandedDirectoryRemovalListener ( File toDelete, String newDocBase ) {
            this.toDelete = toDelete;
            this.newDocBase = newDocBase;
        }
        @Override
        public void lifecycleEvent ( LifecycleEvent event ) {
            if ( Lifecycle.AFTER_STOP_EVENT.equals ( event.getType() ) ) {
                Context context = ( Context ) event.getLifecycle();
                ExpandWar.delete ( toDelete );
                context.setDocBase ( newDocBase );
                context.removeLifecycleListener ( this );
            }
        }
    }
}
