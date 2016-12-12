package org.apache.tomcat.util.scan;
import java.util.Collections;
import org.apache.juli.logging.LogFactory;
import java.net.URI;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.apache.tomcat.Jar;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.buf.UriUtil;
import java.io.File;
import java.util.Iterator;
import java.util.Collection;
import java.util.Arrays;
import java.util.LinkedList;
import java.net.URLClassLoader;
import java.net.MalformedURLException;
import java.io.IOException;
import java.util.Deque;
import java.net.URL;
import java.util.HashSet;
import org.apache.tomcat.JarScannerCallback;
import javax.servlet.ServletContext;
import org.apache.tomcat.JarScanType;
import org.apache.tomcat.JarScanFilter;
import java.util.Set;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.tomcat.JarScanner;
public class StandardJarScanner implements JarScanner {
    private static final Log log;
    private static final StringManager sm;
    private static final Set<ClassLoader> CLASSLOADER_HIERARCHY;
    private boolean scanClassPath;
    private boolean scanManifest;
    private boolean scanAllFiles;
    private boolean scanAllDirectories;
    private boolean scanBootstrapClassPath;
    private JarScanFilter jarScanFilter;
    public StandardJarScanner() {
        this.scanClassPath = true;
        this.scanManifest = true;
        this.scanAllFiles = false;
        this.scanAllDirectories = true;
        this.scanBootstrapClassPath = false;
        this.jarScanFilter = new StandardJarScanFilter();
    }
    public boolean isScanClassPath() {
        return this.scanClassPath;
    }
    public void setScanClassPath ( final boolean scanClassPath ) {
        this.scanClassPath = scanClassPath;
    }
    public boolean isScanManifest() {
        return this.scanManifest;
    }
    public void setScanManifest ( final boolean scanManifest ) {
        this.scanManifest = scanManifest;
    }
    public boolean isScanAllFiles() {
        return this.scanAllFiles;
    }
    public void setScanAllFiles ( final boolean scanAllFiles ) {
        this.scanAllFiles = scanAllFiles;
    }
    public boolean isScanAllDirectories() {
        return this.scanAllDirectories;
    }
    public void setScanAllDirectories ( final boolean scanAllDirectories ) {
        this.scanAllDirectories = scanAllDirectories;
    }
    public boolean isScanBootstrapClassPath() {
        return this.scanBootstrapClassPath;
    }
    public void setScanBootstrapClassPath ( final boolean scanBootstrapClassPath ) {
        this.scanBootstrapClassPath = scanBootstrapClassPath;
    }
    @Override
    public JarScanFilter getJarScanFilter() {
        return this.jarScanFilter;
    }
    @Override
    public void setJarScanFilter ( final JarScanFilter jarScanFilter ) {
        this.jarScanFilter = jarScanFilter;
    }
    @Override
    public void scan ( final JarScanType scanType, final ServletContext context, final JarScannerCallback callback ) {
        if ( StandardJarScanner.log.isTraceEnabled() ) {
            StandardJarScanner.log.trace ( StandardJarScanner.sm.getString ( "jarScan.webinflibStart" ) );
        }
        final Set<URL> processedURLs = new HashSet<URL>();
        final Set<String> dirList = ( Set<String> ) context.getResourcePaths ( "/WEB-INF/lib/" );
        if ( dirList != null ) {
            for ( final String path : dirList ) {
                if ( path.endsWith ( ".jar" ) && this.getJarScanFilter().check ( scanType, path.substring ( path.lastIndexOf ( 47 ) + 1 ) ) ) {
                    if ( StandardJarScanner.log.isDebugEnabled() ) {
                        StandardJarScanner.log.debug ( StandardJarScanner.sm.getString ( "jarScan.webinflibJarScan", path ) );
                    }
                    URL url = null;
                    try {
                        url = context.getResource ( path );
                        processedURLs.add ( url );
                        this.process ( scanType, callback, url, path, true, null );
                    } catch ( IOException e ) {
                        StandardJarScanner.log.warn ( StandardJarScanner.sm.getString ( "jarScan.webinflibFail", url ), e );
                    }
                } else {
                    if ( !StandardJarScanner.log.isTraceEnabled() ) {
                        continue;
                    }
                    StandardJarScanner.log.trace ( StandardJarScanner.sm.getString ( "jarScan.webinflibJarNoScan", path ) );
                }
            }
        }
        try {
            final URL webInfURL = context.getResource ( "/WEB-INF/classes" );
            if ( webInfURL != null ) {
                processedURLs.add ( webInfURL );
                if ( this.isScanAllDirectories() ) {
                    final URL url2 = context.getResource ( "/WEB-INF/classes/META-INF" );
                    if ( url2 != null ) {
                        try {
                            callback.scanWebInfClasses();
                        } catch ( IOException e2 ) {
                            StandardJarScanner.log.warn ( StandardJarScanner.sm.getString ( "jarScan.webinfclassesFail" ), e2 );
                        }
                    }
                }
            }
        } catch ( MalformedURLException ex ) {}
        if ( this.isScanClassPath() ) {
            if ( StandardJarScanner.log.isTraceEnabled() ) {
                StandardJarScanner.log.trace ( StandardJarScanner.sm.getString ( "jarScan.classloaderStart" ) );
            }
            ClassLoader stopLoader = null;
            if ( !this.isScanBootstrapClassPath() ) {
                stopLoader = ClassLoader.getSystemClassLoader().getParent();
            }
            ClassLoader classLoader = context.getClassLoader();
            boolean isWebapp = true;
            while ( classLoader != null && classLoader != stopLoader ) {
                if ( classLoader instanceof URLClassLoader ) {
                    if ( isWebapp ) {
                        isWebapp = isWebappClassLoader ( classLoader );
                    }
                    final Deque<URL> classPathUrlsToProcess = new LinkedList<URL>();
                    classPathUrlsToProcess.addAll ( ( Collection<?> ) Arrays.asList ( ( ( URLClassLoader ) classLoader ).getURLs() ) );
                    while ( !classPathUrlsToProcess.isEmpty() ) {
                        final URL url3 = classPathUrlsToProcess.pop();
                        if ( processedURLs.contains ( url3 ) ) {
                            continue;
                        }
                        final ClassPathEntry cpe = new ClassPathEntry ( url3 );
                        if ( ( cpe.isJar() || scanType == JarScanType.PLUGGABILITY || this.isScanAllDirectories() ) && this.getJarScanFilter().check ( scanType, cpe.getName() ) ) {
                            if ( StandardJarScanner.log.isDebugEnabled() ) {
                                StandardJarScanner.log.debug ( StandardJarScanner.sm.getString ( "jarScan.classloaderJarScan", url3 ) );
                            }
                            try {
                                processedURLs.add ( url3 );
                                this.process ( scanType, callback, url3, null, isWebapp, classPathUrlsToProcess );
                            } catch ( IOException ioe ) {
                                StandardJarScanner.log.warn ( StandardJarScanner.sm.getString ( "jarScan.classloaderFail", url3 ), ioe );
                            }
                        } else {
                            if ( !StandardJarScanner.log.isTraceEnabled() ) {
                                continue;
                            }
                            StandardJarScanner.log.trace ( StandardJarScanner.sm.getString ( "jarScan.classloaderJarNoScan", url3 ) );
                        }
                    }
                }
                classLoader = classLoader.getParent();
            }
        }
    }
    private static boolean isWebappClassLoader ( final ClassLoader classLoader ) {
        return !StandardJarScanner.CLASSLOADER_HIERARCHY.contains ( classLoader );
    }
    private void process ( final JarScanType scanType, final JarScannerCallback callback, final URL url, final String webappPath, final boolean isWebapp, final Deque<URL> classPathUrlsToProcess ) throws IOException {
        if ( StandardJarScanner.log.isTraceEnabled() ) {
            StandardJarScanner.log.trace ( StandardJarScanner.sm.getString ( "jarScan.jarUrlStart", url ) );
        }
        if ( "jar".equals ( url.getProtocol() ) || url.getPath().endsWith ( ".jar" ) ) {
            try ( final Jar jar = JarFactory.newInstance ( url ) ) {
                if ( this.isScanManifest() ) {
                    processManifest ( jar, isWebapp, classPathUrlsToProcess );
                }
                callback.scan ( jar, webappPath, isWebapp );
            }
        } else if ( "file".equals ( url.getProtocol() ) ) {
            try {
                final File f = new File ( url.toURI() );
                if ( f.isFile() && this.isScanAllFiles() ) {
                    final URL jarURL = UriUtil.buildJarUrl ( f );
                    try ( final Jar jar2 = JarFactory.newInstance ( jarURL ) ) {
                        if ( this.isScanManifest() ) {
                            processManifest ( jar2, isWebapp, classPathUrlsToProcess );
                        }
                        callback.scan ( jar2, webappPath, isWebapp );
                    }
                } else if ( f.isDirectory() ) {
                    if ( scanType == JarScanType.PLUGGABILITY ) {
                        callback.scan ( f, webappPath, isWebapp );
                    } else {
                        final File metainf = new File ( f.getAbsoluteFile() + File.separator + "META-INF" );
                        if ( metainf.isDirectory() ) {
                            callback.scan ( f, webappPath, isWebapp );
                        }
                    }
                }
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                final IOException ioe = new IOException();
                ioe.initCause ( t );
                throw ioe;
            }
        }
    }
    private static void processManifest ( final Jar jar, final boolean isWebapp, final Deque<URL> classPathUrlsToProcess ) throws IOException {
        if ( isWebapp || classPathUrlsToProcess == null ) {
            return;
        }
        final Manifest manifest = jar.getManifest();
        if ( manifest != null ) {
            final Attributes attributes = manifest.getMainAttributes();
            final String classPathAttribute = attributes.getValue ( "Class-Path" );
            if ( classPathAttribute == null ) {
                return;
            }
            final String[] split;
            final String[] classPathEntries = split = classPathAttribute.split ( " " );
            for ( String classPathEntry : split ) {
                classPathEntry = classPathEntry.trim();
                Label_0180: {
                    if ( classPathEntry.length() != 0 ) {
                        final URL jarURL = jar.getJarFileURL();
                        URL classPathEntryURL;
                        try {
                            final URI jarURI = jarURL.toURI();
                            final URI classPathEntryURI = jarURI.resolve ( classPathEntry );
                            classPathEntryURL = classPathEntryURI.toURL();
                        } catch ( Exception e ) {
                            if ( StandardJarScanner.log.isDebugEnabled() ) {
                                StandardJarScanner.log.debug ( StandardJarScanner.sm.getString ( "jarScan.invalidUri", jarURL ), e );
                            }
                            break Label_0180;
                        }
                        classPathUrlsToProcess.add ( classPathEntryURL );
                    }
                }
            }
        }
    }
    static {
        log = LogFactory.getLog ( StandardJarScanner.class );
        sm = StringManager.getManager ( "org.apache.tomcat.util.scan" );
        final Set<ClassLoader> cls = new HashSet<ClassLoader>();
        for ( ClassLoader cl = StandardJarScanner.class.getClassLoader(); cl != null; cl = cl.getParent() ) {
            cls.add ( cl );
        }
        CLASSLOADER_HIERARCHY = Collections.unmodifiableSet ( ( Set<? extends ClassLoader> ) cls );
    }
    private static class ClassPathEntry {
        private final boolean jar;
        private final String name;
        public ClassPathEntry ( final URL url ) {
            String path = url.getPath();
            final int end = path.lastIndexOf ( ".jar" );
            if ( end != -1 ) {
                this.jar = true;
                final int start = path.lastIndexOf ( 47, end );
                this.name = path.substring ( start + 1, end + 4 );
            } else {
                this.jar = false;
                if ( path.endsWith ( "/" ) ) {
                    path = path.substring ( 0, path.length() - 1 );
                }
                final int start = path.lastIndexOf ( 47 );
                this.name = path.substring ( start + 1 );
            }
        }
        public boolean isJar() {
            return this.jar;
        }
        public String getName() {
            return this.name;
        }
    }
}
