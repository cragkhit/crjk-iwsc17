package org.apache.tomcat.util.scan;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.servlet.ServletContext;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.Jar;
import org.apache.tomcat.JarScanFilter;
import org.apache.tomcat.JarScanType;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.buf.UriUtil;
import org.apache.tomcat.util.res.StringManager;
public class StandardJarScanner implements JarScanner {
    private static final Log log = LogFactory.getLog ( StandardJarScanner.class );
    private static final StringManager sm = StringManager.getManager ( Constants.Package );
    private static final Set<ClassLoader> CLASSLOADER_HIERARCHY;
    static {
        Set<ClassLoader> cls = new HashSet<>();
        ClassLoader cl = StandardJarScanner.class.getClassLoader();
        while ( cl != null ) {
            cls.add ( cl );
            cl = cl.getParent();
        }
        CLASSLOADER_HIERARCHY = Collections.unmodifiableSet ( cls );
    }
    private boolean scanClassPath = true;
    public boolean isScanClassPath() {
        return scanClassPath;
    }
    public void setScanClassPath ( boolean scanClassPath ) {
        this.scanClassPath = scanClassPath;
    }
    private boolean scanManifest = true;
    public boolean isScanManifest() {
        return scanManifest;
    }
    public void setScanManifest ( boolean scanManifest ) {
        this.scanManifest = scanManifest;
    }
    private boolean scanAllFiles = false;
    public boolean isScanAllFiles() {
        return scanAllFiles;
    }
    public void setScanAllFiles ( boolean scanAllFiles ) {
        this.scanAllFiles = scanAllFiles;
    }
    private boolean scanAllDirectories = true;
    public boolean isScanAllDirectories() {
        return scanAllDirectories;
    }
    public void setScanAllDirectories ( boolean scanAllDirectories ) {
        this.scanAllDirectories = scanAllDirectories;
    }
    private boolean scanBootstrapClassPath = false;
    public boolean isScanBootstrapClassPath() {
        return scanBootstrapClassPath;
    }
    public void setScanBootstrapClassPath ( boolean scanBootstrapClassPath ) {
        this.scanBootstrapClassPath = scanBootstrapClassPath;
    }
    private JarScanFilter jarScanFilter = new StandardJarScanFilter();
    @Override
    public JarScanFilter getJarScanFilter() {
        return jarScanFilter;
    }
    @Override
    public void setJarScanFilter ( JarScanFilter jarScanFilter ) {
        this.jarScanFilter = jarScanFilter;
    }
    @Override
    public void scan ( JarScanType scanType, ServletContext context,
                       JarScannerCallback callback ) {
        if ( log.isTraceEnabled() ) {
            log.trace ( sm.getString ( "jarScan.webinflibStart" ) );
        }
        Set<URL> processedURLs = new HashSet<>();
        Set<String> dirList = context.getResourcePaths ( Constants.WEB_INF_LIB );
        if ( dirList != null ) {
            Iterator<String> it = dirList.iterator();
            while ( it.hasNext() ) {
                String path = it.next();
                if ( path.endsWith ( Constants.JAR_EXT ) &&
                        getJarScanFilter().check ( scanType,
                                                   path.substring ( path.lastIndexOf ( '/' ) + 1 ) ) ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( sm.getString ( "jarScan.webinflibJarScan", path ) );
                    }
                    URL url = null;
                    try {
                        url = context.getResource ( path );
                        processedURLs.add ( url );
                        process ( scanType, callback, url, path, true, null );
                    } catch ( IOException e ) {
                        log.warn ( sm.getString ( "jarScan.webinflibFail", url ), e );
                    }
                } else {
                    if ( log.isTraceEnabled() ) {
                        log.trace ( sm.getString ( "jarScan.webinflibJarNoScan", path ) );
                    }
                }
            }
        }
        try {
            URL webInfURL = context.getResource ( Constants.WEB_INF_CLASSES );
            if ( webInfURL != null ) {
                processedURLs.add ( webInfURL );
                if ( isScanAllDirectories() ) {
                    URL url = context.getResource ( Constants.WEB_INF_CLASSES + "/META-INF" );
                    if ( url != null ) {
                        try {
                            callback.scanWebInfClasses();
                        } catch ( IOException e ) {
                            log.warn ( sm.getString ( "jarScan.webinfclassesFail" ), e );
                        }
                    }
                }
            }
        } catch ( MalformedURLException e ) {
        }
        if ( isScanClassPath() ) {
            if ( log.isTraceEnabled() ) {
                log.trace ( sm.getString ( "jarScan.classloaderStart" ) );
            }
            ClassLoader stopLoader = null;
            if ( !isScanBootstrapClassPath() ) {
                stopLoader = ClassLoader.getSystemClassLoader().getParent();
            }
            ClassLoader classLoader = context.getClassLoader();
            boolean isWebapp = true;
            while ( classLoader != null && classLoader != stopLoader ) {
                if ( classLoader instanceof URLClassLoader ) {
                    if ( isWebapp ) {
                        isWebapp = isWebappClassLoader ( classLoader );
                    }
                    Deque<URL> classPathUrlsToProcess = new LinkedList<>();
                    classPathUrlsToProcess.addAll (
                        Arrays.asList ( ( ( URLClassLoader ) classLoader ).getURLs() ) );
                    while ( !classPathUrlsToProcess.isEmpty() ) {
                        URL url = classPathUrlsToProcess.pop();
                        if ( processedURLs.contains ( url ) ) {
                            continue;
                        }
                        ClassPathEntry cpe = new ClassPathEntry ( url );
                        if ( ( cpe.isJar() ||
                                scanType == JarScanType.PLUGGABILITY ||
                                isScanAllDirectories() ) &&
                                getJarScanFilter().check ( scanType,
                                                           cpe.getName() ) ) {
                            if ( log.isDebugEnabled() ) {
                                log.debug ( sm.getString ( "jarScan.classloaderJarScan", url ) );
                            }
                            try {
                                processedURLs.add ( url );
                                process ( scanType, callback, url, null, isWebapp, classPathUrlsToProcess );
                            } catch ( IOException ioe ) {
                                log.warn ( sm.getString ( "jarScan.classloaderFail", url ), ioe );
                            }
                        } else {
                            if ( log.isTraceEnabled() ) {
                                log.trace ( sm.getString ( "jarScan.classloaderJarNoScan", url ) );
                            }
                        }
                    }
                }
                classLoader = classLoader.getParent();
            }
        }
    }
    private static boolean isWebappClassLoader ( ClassLoader classLoader ) {
        return !CLASSLOADER_HIERARCHY.contains ( classLoader );
    }
    private void process ( JarScanType scanType, JarScannerCallback callback,
                           URL url, String webappPath, boolean isWebapp, Deque<URL> classPathUrlsToProcess )
    throws IOException {
        if ( log.isTraceEnabled() ) {
            log.trace ( sm.getString ( "jarScan.jarUrlStart", url ) );
        }
        if ( "jar".equals ( url.getProtocol() ) || url.getPath().endsWith ( Constants.JAR_EXT ) ) {
            try ( Jar jar = JarFactory.newInstance ( url ) ) {
                if ( isScanManifest() ) {
                    processManifest ( jar, isWebapp, classPathUrlsToProcess );
                }
                callback.scan ( jar, webappPath, isWebapp );
            }
        } else if ( "file".equals ( url.getProtocol() ) ) {
            File f;
            try {
                f = new File ( url.toURI() );
                if ( f.isFile() && isScanAllFiles() ) {
                    URL jarURL = UriUtil.buildJarUrl ( f );
                    try ( Jar jar = JarFactory.newInstance ( jarURL ) ) {
                        if ( isScanManifest() ) {
                            processManifest ( jar, isWebapp, classPathUrlsToProcess );
                        }
                        callback.scan ( jar, webappPath, isWebapp );
                    }
                } else if ( f.isDirectory() ) {
                    if ( scanType == JarScanType.PLUGGABILITY ) {
                        callback.scan ( f, webappPath, isWebapp );
                    } else {
                        File metainf = new File ( f.getAbsoluteFile() + File.separator + "META-INF" );
                        if ( metainf.isDirectory() ) {
                            callback.scan ( f, webappPath, isWebapp );
                        }
                    }
                }
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                IOException ioe = new IOException();
                ioe.initCause ( t );
                throw ioe;
            }
        }
    }
    private static void processManifest ( Jar jar, boolean isWebapp,
                                          Deque<URL> classPathUrlsToProcess ) throws IOException {
        if ( isWebapp || classPathUrlsToProcess == null ) {
            return;
        }
        Manifest manifest = jar.getManifest();
        if ( manifest != null ) {
            Attributes attributes = manifest.getMainAttributes();
            String classPathAttribute = attributes.getValue ( "Class-Path" );
            if ( classPathAttribute == null ) {
                return;
            }
            String[] classPathEntries = classPathAttribute.split ( " " );
            for ( String classPathEntry : classPathEntries ) {
                classPathEntry = classPathEntry.trim();
                if ( classPathEntry.length() == 0 ) {
                    continue;
                }
                URL jarURL = jar.getJarFileURL();
                URL classPathEntryURL;
                try {
                    URI jarURI = jarURL.toURI();
                    URI classPathEntryURI = jarURI.resolve ( classPathEntry );
                    classPathEntryURL = classPathEntryURI.toURL();
                } catch ( Exception e ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( sm.getString ( "jarScan.invalidUri", jarURL ), e );
                    }
                    continue;
                }
                classPathUrlsToProcess.add ( classPathEntryURL );
            }
        }
    }
    private static class ClassPathEntry {
        private final boolean jar;
        private final String name;
        public ClassPathEntry ( URL url ) {
            String path = url.getPath();
            int end = path.lastIndexOf ( Constants.JAR_EXT );
            if ( end != -1 ) {
                jar = true;
                int start = path.lastIndexOf ( '/', end );
                name = path.substring ( start + 1, end + 4 );
            } else {
                jar = false;
                if ( path.endsWith ( "/" ) ) {
                    path = path.substring ( 0, path.length() - 1 );
                }
                int start = path.lastIndexOf ( '/' );
                name = path.substring ( start + 1 );
            }
        }
        public boolean isJar() {
            return jar;
        }
        public String getName() {
            return name;
        }
    }
}
