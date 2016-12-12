package org.apache.jasper.servlet;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.descriptor.TaglibDescriptor;
import org.apache.jasper.compiler.JarScannerFactory;
import org.apache.jasper.compiler.Localizer;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.Jar;
import org.apache.tomcat.JarScanType;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.util.descriptor.tld.TaglibXml;
import org.apache.tomcat.util.descriptor.tld.TldParser;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import org.xml.sax.SAXException;
public class TldScanner {
    private static final Log log = LogFactory.getLog ( TldScanner.class );
    private static final String MSG = "org.apache.jasper.servlet.TldScanner";
    private static final String TLD_EXT = ".tld";
    private static final String WEB_INF = "/WEB-INF/";
    private final ServletContext context;
    private final TldParser tldParser;
    private final Map<String, TldResourcePath> uriTldResourcePathMap = new HashMap<>();
    private final Map<TldResourcePath, TaglibXml> tldResourcePathTaglibXmlMap = new HashMap<>();
    private final List<String> listeners = new ArrayList<>();
    public TldScanner ( ServletContext context,
                        boolean namespaceAware,
                        boolean validation,
                        boolean blockExternal ) {
        this.context = context;
        this.tldParser = new TldParser ( namespaceAware, validation, blockExternal );
    }
    public void scan() throws IOException, SAXException {
        scanPlatform();
        scanJspConfig();
        scanResourcePaths ( WEB_INF );
        scanJars();
    }
    public Map<String, TldResourcePath> getUriTldResourcePathMap() {
        return uriTldResourcePathMap;
    }
    public Map<TldResourcePath, TaglibXml> getTldResourcePathTaglibXmlMap() {
        return tldResourcePathTaglibXmlMap;
    }
    public List<String> getListeners() {
        return listeners;
    }
    public void setClassLoader ( ClassLoader classLoader ) {
        tldParser.setClassLoader ( classLoader );
    }
    protected void scanPlatform() {
    }
    protected void scanJspConfig() throws IOException, SAXException {
        JspConfigDescriptor jspConfigDescriptor = context.getJspConfigDescriptor();
        if ( jspConfigDescriptor == null ) {
            return;
        }
        Collection<TaglibDescriptor> descriptors = jspConfigDescriptor.getTaglibs();
        for ( TaglibDescriptor descriptor : descriptors ) {
            String taglibURI = descriptor.getTaglibURI();
            String resourcePath = descriptor.getTaglibLocation();
            if ( !resourcePath.startsWith ( "/" ) ) {
                resourcePath = WEB_INF + resourcePath;
            }
            if ( uriTldResourcePathMap.containsKey ( taglibURI ) ) {
                log.warn ( Localizer.getMessage ( MSG + ".webxmlSkip",
                                                  resourcePath,
                                                  taglibURI ) );
                continue;
            }
            if ( log.isTraceEnabled() ) {
                log.trace ( Localizer.getMessage ( MSG + ".webxmlAdd",
                                                   resourcePath,
                                                   taglibURI ) );
            }
            URL url = context.getResource ( resourcePath );
            if ( url != null ) {
                TldResourcePath tldResourcePath;
                if ( resourcePath.endsWith ( ".jar" ) ) {
                    tldResourcePath = new TldResourcePath ( url, resourcePath, "META-INF/taglib.tld" );
                } else {
                    tldResourcePath = new TldResourcePath ( url, resourcePath );
                }
                TaglibXml tld = tldParser.parse ( tldResourcePath );
                uriTldResourcePathMap.put ( taglibURI, tldResourcePath );
                tldResourcePathTaglibXmlMap.put ( tldResourcePath, tld );
                if ( tld.getListeners() != null ) {
                    listeners.addAll ( tld.getListeners() );
                }
            } else {
                log.warn ( Localizer.getMessage ( MSG + ".webxmlFailPathDoesNotExist",
                                                  resourcePath,
                                                  taglibURI ) );
                continue;
            }
        }
    }
    protected void scanResourcePaths ( String startPath )
    throws IOException, SAXException {
        boolean found = false;
        Set<String> dirList = context.getResourcePaths ( startPath );
        if ( dirList != null ) {
            for ( String path : dirList ) {
                if ( path.startsWith ( "/WEB-INF/classes/" ) ) {
                } else if ( path.startsWith ( "/WEB-INF/lib/" ) ) {
                } else if ( path.endsWith ( "/" ) ) {
                    scanResourcePaths ( path );
                } else if ( path.startsWith ( "/WEB-INF/tags/" ) ) {
                    if ( path.endsWith ( "/implicit.tld" ) ) {
                        found = true;
                        parseTld ( path );
                    }
                } else if ( path.endsWith ( TLD_EXT ) ) {
                    found = true;
                    parseTld ( path );
                }
            }
        }
        if ( found ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( Localizer.getMessage ( "jsp.tldCache.tldInResourcePath", startPath ) );
            }
        } else {
            if ( log.isDebugEnabled() ) {
                log.debug ( Localizer.getMessage ( "jsp.tldCache.noTldInResourcePath", startPath ) );
            }
        }
    }
    public void scanJars() {
        JarScanner scanner = JarScannerFactory.getJarScanner ( context );
        TldScannerCallback callback = new TldScannerCallback();
        scanner.scan ( JarScanType.TLD, context, callback );
        if ( callback.scanFoundNoTLDs() ) {
            log.info ( Localizer.getMessage ( "jsp.tldCache.noTldSummary" ) );
        }
    }
    protected void parseTld ( String resourcePath ) throws IOException, SAXException {
        TldResourcePath tldResourcePath =
            new TldResourcePath ( context.getResource ( resourcePath ), resourcePath );
        parseTld ( tldResourcePath );
    }
    protected void parseTld ( TldResourcePath path ) throws IOException, SAXException {
        if ( tldResourcePathTaglibXmlMap.containsKey ( path ) ) {
            return;
        }
        TaglibXml tld = tldParser.parse ( path );
        String uri = tld.getUri();
        if ( uri != null ) {
            if ( !uriTldResourcePathMap.containsKey ( uri ) ) {
                uriTldResourcePathMap.put ( uri, path );
            }
        }
        tldResourcePathTaglibXmlMap.put ( path, tld );
        if ( tld.getListeners() != null ) {
            listeners.addAll ( tld.getListeners() );
        }
    }
    class TldScannerCallback implements JarScannerCallback {
        private boolean foundJarWithoutTld = false;
        private boolean foundFileWithoutTld = false;
        @Override
        public void scan ( Jar jar, String webappPath, boolean isWebapp ) throws IOException {
            boolean found = false;
            URL jarFileUrl = jar.getJarFileURL();
            jar.nextEntry();
            for ( String entryName = jar.getEntryName();
                    entryName != null;
                    jar.nextEntry(), entryName = jar.getEntryName() ) {
                if ( ! ( entryName.startsWith ( "META-INF/" ) &&
                         entryName.endsWith ( TLD_EXT ) ) ) {
                    continue;
                }
                found = true;
                TldResourcePath tldResourcePath =
                    new TldResourcePath ( jarFileUrl, webappPath, entryName );
                try {
                    parseTld ( tldResourcePath );
                } catch ( SAXException e ) {
                    throw new IOException ( e );
                }
            }
            if ( found ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( Localizer.getMessage ( "jsp.tldCache.tldInJar", jarFileUrl.toString() ) );
                }
            } else {
                foundJarWithoutTld = true;
                if ( log.isDebugEnabled() ) {
                    log.debug ( Localizer.getMessage (
                                    "jsp.tldCache.noTldInJar", jarFileUrl.toString() ) );
                }
            }
        }
        @Override
        public void scan ( File file, final String webappPath, boolean isWebapp )
        throws IOException {
            File metaInf = new File ( file, "META-INF" );
            if ( !metaInf.isDirectory() ) {
                return;
            }
            foundFileWithoutTld = false;
            final Path filePath = file.toPath();
            Files.walkFileTree ( metaInf.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile ( Path file,
                                                   BasicFileAttributes attrs )
                throws IOException {
                    Path fileName = file.getFileName();
                    if ( fileName == null || !fileName.toString().toLowerCase (
                                Locale.ENGLISH ).endsWith ( TLD_EXT ) ) {
                        return FileVisitResult.CONTINUE;
                    }
                    foundFileWithoutTld = true;
                    String resourcePath;
                    if ( webappPath == null ) {
                        resourcePath = null;
                    } else {
                        String subPath = file.subpath (
                                             filePath.getNameCount(), file.getNameCount() ).toString();
                        if ( '/' != File.separatorChar ) {
                            subPath = subPath.replace ( File.separatorChar, '/' );
                        }
                        resourcePath = webappPath + "/" + subPath;
                    }
                    try {
                        URL url = file.toUri().toURL();
                        TldResourcePath path = new TldResourcePath ( url, resourcePath );
                        parseTld ( path );
                    } catch ( SAXException e ) {
                        throw new IOException ( e );
                    }
                    return FileVisitResult.CONTINUE;
                }
            } );
            if ( foundFileWithoutTld ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( Localizer.getMessage ( "jsp.tldCache.tldInDir",
                                                       file.getAbsolutePath() ) );
                }
            } else {
                if ( log.isDebugEnabled() ) {
                    log.debug ( Localizer.getMessage ( "jsp.tldCache.noTldInDir",
                                                       file.getAbsolutePath() ) );
                }
            }
        }
        @Override
        public void scanWebInfClasses() throws IOException {
            Set<String> paths = context.getResourcePaths ( WEB_INF + "classes/META-INF" );
            if ( paths == null ) {
                return;
            }
            for ( String path : paths ) {
                if ( path.endsWith ( TLD_EXT ) ) {
                    try {
                        parseTld ( path );
                    } catch ( SAXException e ) {
                        throw new IOException ( e );
                    }
                }
            }
        }
        boolean scanFoundNoTLDs() {
            return foundJarWithoutTld;
        }
    }
}
