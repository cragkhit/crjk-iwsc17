package org.apache.jasper.servlet;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.util.Locale;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.io.File;
import org.apache.tomcat.Jar;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.JarScanType;
import org.apache.jasper.compiler.JarScannerFactory;
import java.util.Set;
import java.net.URL;
import java.util.Iterator;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.util.Collection;
import org.apache.jasper.compiler.Localizer;
import javax.servlet.descriptor.TaglibDescriptor;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.tomcat.util.descriptor.tld.TaglibXml;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import java.util.Map;
import org.apache.tomcat.util.descriptor.tld.TldParser;
import javax.servlet.ServletContext;
import org.apache.juli.logging.Log;
public class TldScanner {
    private static final Log log;
    private static final String MSG = "org.apache.jasper.servlet.TldScanner";
    private static final String TLD_EXT = ".tld";
    private static final String WEB_INF = "/WEB-INF/";
    private final ServletContext context;
    private final TldParser tldParser;
    private final Map<String, TldResourcePath> uriTldResourcePathMap;
    private final Map<TldResourcePath, TaglibXml> tldResourcePathTaglibXmlMap;
    private final List<String> listeners;
    public TldScanner ( final ServletContext context, final boolean namespaceAware, final boolean validation, final boolean blockExternal ) {
        this.uriTldResourcePathMap = new HashMap<String, TldResourcePath>();
        this.tldResourcePathTaglibXmlMap = new HashMap<TldResourcePath, TaglibXml>();
        this.listeners = new ArrayList<String>();
        this.context = context;
        this.tldParser = new TldParser ( namespaceAware, validation, blockExternal );
    }
    public void scan() throws IOException, SAXException {
        this.scanPlatform();
        this.scanJspConfig();
        this.scanResourcePaths ( "/WEB-INF/" );
        this.scanJars();
    }
    public Map<String, TldResourcePath> getUriTldResourcePathMap() {
        return this.uriTldResourcePathMap;
    }
    public Map<TldResourcePath, TaglibXml> getTldResourcePathTaglibXmlMap() {
        return this.tldResourcePathTaglibXmlMap;
    }
    public List<String> getListeners() {
        return this.listeners;
    }
    public void setClassLoader ( final ClassLoader classLoader ) {
        this.tldParser.setClassLoader ( classLoader );
    }
    protected void scanPlatform() {
    }
    protected void scanJspConfig() throws IOException, SAXException {
        final JspConfigDescriptor jspConfigDescriptor = this.context.getJspConfigDescriptor();
        if ( jspConfigDescriptor == null ) {
            return;
        }
        final Collection<TaglibDescriptor> descriptors = ( Collection<TaglibDescriptor> ) jspConfigDescriptor.getTaglibs();
        for ( final TaglibDescriptor descriptor : descriptors ) {
            final String taglibURI = descriptor.getTaglibURI();
            String resourcePath = descriptor.getTaglibLocation();
            if ( !resourcePath.startsWith ( "/" ) ) {
                resourcePath = "/WEB-INF/" + resourcePath;
            }
            if ( this.uriTldResourcePathMap.containsKey ( taglibURI ) ) {
                TldScanner.log.warn ( Localizer.getMessage ( "org.apache.jasper.servlet.TldScanner.webxmlSkip", resourcePath, taglibURI ) );
            } else {
                if ( TldScanner.log.isTraceEnabled() ) {
                    TldScanner.log.trace ( Localizer.getMessage ( "org.apache.jasper.servlet.TldScanner.webxmlAdd", resourcePath, taglibURI ) );
                }
                final URL url = this.context.getResource ( resourcePath );
                if ( url != null ) {
                    TldResourcePath tldResourcePath;
                    if ( resourcePath.endsWith ( ".jar" ) ) {
                        tldResourcePath = new TldResourcePath ( url, resourcePath, "META-INF/taglib.tld" );
                    } else {
                        tldResourcePath = new TldResourcePath ( url, resourcePath );
                    }
                    final TaglibXml tld = this.tldParser.parse ( tldResourcePath );
                    this.uriTldResourcePathMap.put ( taglibURI, tldResourcePath );
                    this.tldResourcePathTaglibXmlMap.put ( tldResourcePath, tld );
                    if ( tld.getListeners() == null ) {
                        continue;
                    }
                    this.listeners.addAll ( tld.getListeners() );
                } else {
                    TldScanner.log.warn ( Localizer.getMessage ( "org.apache.jasper.servlet.TldScanner.webxmlFailPathDoesNotExist", resourcePath, taglibURI ) );
                }
            }
        }
    }
    protected void scanResourcePaths ( final String startPath ) throws IOException, SAXException {
        boolean found = false;
        final Set<String> dirList = ( Set<String> ) this.context.getResourcePaths ( startPath );
        if ( dirList != null ) {
            for ( final String path : dirList ) {
                if ( path.startsWith ( "/WEB-INF/classes/" ) ) {
                    continue;
                }
                if ( path.startsWith ( "/WEB-INF/lib/" ) ) {
                    continue;
                }
                if ( path.endsWith ( "/" ) ) {
                    this.scanResourcePaths ( path );
                } else if ( path.startsWith ( "/WEB-INF/tags/" ) ) {
                    if ( !path.endsWith ( "/implicit.tld" ) ) {
                        continue;
                    }
                    found = true;
                    this.parseTld ( path );
                } else {
                    if ( !path.endsWith ( ".tld" ) ) {
                        continue;
                    }
                    found = true;
                    this.parseTld ( path );
                }
            }
        }
        if ( found ) {
            if ( TldScanner.log.isDebugEnabled() ) {
                TldScanner.log.debug ( Localizer.getMessage ( "jsp.tldCache.tldInResourcePath", startPath ) );
            }
        } else if ( TldScanner.log.isDebugEnabled() ) {
            TldScanner.log.debug ( Localizer.getMessage ( "jsp.tldCache.noTldInResourcePath", startPath ) );
        }
    }
    public void scanJars() {
        final JarScanner scanner = JarScannerFactory.getJarScanner ( this.context );
        final TldScannerCallback callback = new TldScannerCallback();
        scanner.scan ( JarScanType.TLD, this.context, callback );
        if ( callback.scanFoundNoTLDs() ) {
            TldScanner.log.info ( Localizer.getMessage ( "jsp.tldCache.noTldSummary" ) );
        }
    }
    protected void parseTld ( final String resourcePath ) throws IOException, SAXException {
        final TldResourcePath tldResourcePath = new TldResourcePath ( this.context.getResource ( resourcePath ), resourcePath );
        this.parseTld ( tldResourcePath );
    }
    protected void parseTld ( final TldResourcePath path ) throws IOException, SAXException {
        if ( this.tldResourcePathTaglibXmlMap.containsKey ( path ) ) {
            return;
        }
        final TaglibXml tld = this.tldParser.parse ( path );
        final String uri = tld.getUri();
        if ( uri != null && !this.uriTldResourcePathMap.containsKey ( uri ) ) {
            this.uriTldResourcePathMap.put ( uri, path );
        }
        this.tldResourcePathTaglibXmlMap.put ( path, tld );
        if ( tld.getListeners() != null ) {
            this.listeners.addAll ( tld.getListeners() );
        }
    }
    static {
        log = LogFactory.getLog ( TldScanner.class );
    }
    class TldScannerCallback implements JarScannerCallback {
        private boolean foundJarWithoutTld;
        private boolean foundFileWithoutTld;
        TldScannerCallback() {
            this.foundJarWithoutTld = false;
            this.foundFileWithoutTld = false;
        }
        @Override
        public void scan ( final Jar jar, final String webappPath, final boolean isWebapp ) throws IOException {
            boolean found = false;
            final URL jarFileUrl = jar.getJarFileURL();
            jar.nextEntry();
            for ( String entryName = jar.getEntryName(); entryName != null; entryName = jar.getEntryName() ) {
                if ( entryName.startsWith ( "META-INF/" ) ) {
                    if ( entryName.endsWith ( ".tld" ) ) {
                        found = true;
                        final TldResourcePath tldResourcePath = new TldResourcePath ( jarFileUrl, webappPath, entryName );
                        try {
                            TldScanner.this.parseTld ( tldResourcePath );
                        } catch ( SAXException e ) {
                            throw new IOException ( e );
                        }
                    }
                }
                jar.nextEntry();
            }
            if ( found ) {
                if ( TldScanner.log.isDebugEnabled() ) {
                    TldScanner.log.debug ( Localizer.getMessage ( "jsp.tldCache.tldInJar", jarFileUrl.toString() ) );
                }
            } else {
                this.foundJarWithoutTld = true;
                if ( TldScanner.log.isDebugEnabled() ) {
                    TldScanner.log.debug ( Localizer.getMessage ( "jsp.tldCache.noTldInJar", jarFileUrl.toString() ) );
                }
            }
        }
        @Override
        public void scan ( final File file, final String webappPath, final boolean isWebapp ) throws IOException {
            final File metaInf = new File ( file, "META-INF" );
            if ( !metaInf.isDirectory() ) {
                return;
            }
            this.foundFileWithoutTld = false;
            final Path filePath = file.toPath();
            Files.walkFileTree ( metaInf.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile ( final Path file, final BasicFileAttributes attrs ) throws IOException {
                    final Path fileName = file.getFileName();
                    if ( fileName == null || !fileName.toString().toLowerCase ( Locale.ENGLISH ).endsWith ( ".tld" ) ) {
                        return FileVisitResult.CONTINUE;
                    }
                    TldScannerCallback.this.foundFileWithoutTld = true;
                    String resourcePath;
                    if ( webappPath == null ) {
                        resourcePath = null;
                    } else {
                        String subPath = file.subpath ( filePath.getNameCount(), file.getNameCount() ).toString();
                        if ( '/' != File.separatorChar ) {
                            subPath = subPath.replace ( File.separatorChar, '/' );
                        }
                        resourcePath = webappPath + "/" + subPath;
                    }
                    try {
                        final URL url = file.toUri().toURL();
                        final TldResourcePath path = new TldResourcePath ( url, resourcePath );
                        TldScanner.this.parseTld ( path );
                    } catch ( SAXException e ) {
                        throw new IOException ( e );
                    }
                    return FileVisitResult.CONTINUE;
                }
            } );
            if ( this.foundFileWithoutTld ) {
                if ( TldScanner.log.isDebugEnabled() ) {
                    TldScanner.log.debug ( Localizer.getMessage ( "jsp.tldCache.tldInDir", file.getAbsolutePath() ) );
                }
            } else if ( TldScanner.log.isDebugEnabled() ) {
                TldScanner.log.debug ( Localizer.getMessage ( "jsp.tldCache.noTldInDir", file.getAbsolutePath() ) );
            }
        }
        @Override
        public void scanWebInfClasses() throws IOException {
            final Set<String> paths = ( Set<String> ) TldScanner.this.context.getResourcePaths ( "/WEB-INF/classes/META-INF" );
            if ( paths == null ) {
                return;
            }
            for ( final String path : paths ) {
                if ( path.endsWith ( ".tld" ) ) {
                    try {
                        TldScanner.this.parseTld ( path );
                    } catch ( SAXException e ) {
                        throw new IOException ( e );
                    }
                }
            }
        }
        boolean scanFoundNoTLDs() {
            return this.foundJarWithoutTld;
        }
    }
}
