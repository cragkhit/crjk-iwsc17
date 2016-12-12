package org.apache.jasper.servlet;
import java.util.Iterator;
import java.util.Set;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.util.Locale;
import java.nio.file.FileVisitResult;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.io.File;
import java.net.URL;
import org.apache.jasper.compiler.Localizer;
import org.xml.sax.SAXException;
import java.io.IOException;
import org.apache.tomcat.util.descriptor.tld.TldResourcePath;
import org.apache.tomcat.Jar;
import org.apache.tomcat.JarScannerCallback;
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
            if ( TldScanner.access$000().isDebugEnabled() ) {
                TldScanner.access$000().debug ( Localizer.getMessage ( "jsp.tldCache.tldInJar", jarFileUrl.toString() ) );
            }
        } else {
            this.foundJarWithoutTld = true;
            if ( TldScanner.access$000().isDebugEnabled() ) {
                TldScanner.access$000().debug ( Localizer.getMessage ( "jsp.tldCache.noTldInJar", jarFileUrl.toString() ) );
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
            if ( TldScanner.access$000().isDebugEnabled() ) {
                TldScanner.access$000().debug ( Localizer.getMessage ( "jsp.tldCache.tldInDir", file.getAbsolutePath() ) );
            }
        } else if ( TldScanner.access$000().isDebugEnabled() ) {
            TldScanner.access$000().debug ( Localizer.getMessage ( "jsp.tldCache.noTldInDir", file.getAbsolutePath() ) );
        }
    }
    @Override
    public void scanWebInfClasses() throws IOException {
        final Set<String> paths = ( Set<String> ) TldScanner.access$200 ( TldScanner.this ).getResourcePaths ( "/WEB-INF/classes/META-INF" );
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
