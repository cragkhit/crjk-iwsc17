package org.apache.catalina.webresources;
import java.net.MalformedURLException;
import org.apache.tomcat.util.buf.UriUtil;
import java.io.File;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.io.IOException;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.HashMap;
import org.apache.catalina.WebResource;
import java.util.jar.Manifest;
import java.util.jar.JarEntry;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
public class JarWarResourceSet extends AbstractArchiveResourceSet {
    private final String archivePath;
    public JarWarResourceSet ( final WebResourceRoot root, final String webAppMount, final String base, final String archivePath, final String internalPath ) throws IllegalArgumentException {
        this.setRoot ( root );
        this.setWebAppMount ( webAppMount );
        this.setBase ( base );
        this.archivePath = archivePath;
        this.setInternalPath ( internalPath );
        if ( this.getRoot().getState().isAvailable() ) {
            try {
                this.start();
            } catch ( LifecycleException e ) {
                throw new IllegalStateException ( e );
            }
        }
    }
    @Override
    protected WebResource createArchiveResource ( final JarEntry jarEntry, final String webAppPath, final Manifest manifest ) {
        return new JarWarResource ( this, webAppPath, this.getBaseUrlString(), jarEntry, this.archivePath );
    }
    @Override
    protected HashMap<String, JarEntry> getArchiveEntries ( final boolean single ) {
        synchronized ( this.archiveLock ) {
            if ( this.archiveEntries == null ) {
                JarFile warFile = null;
                InputStream jarFileIs = null;
                this.archiveEntries = new HashMap<String, JarEntry>();
                try {
                    warFile = this.openJarFile();
                    final JarEntry jarFileInWar = warFile.getJarEntry ( this.archivePath );
                    jarFileIs = warFile.getInputStream ( jarFileInWar );
                    try ( final JarInputStream jarIs = new JarInputStream ( jarFileIs ) ) {
                        for ( JarEntry entry = jarIs.getNextJarEntry(); entry != null; entry = jarIs.getNextJarEntry() ) {
                            this.archiveEntries.put ( entry.getName(), entry );
                        }
                        this.setManifest ( jarIs.getManifest() );
                    }
                } catch ( IOException ioe ) {
                    this.archiveEntries = null;
                    throw new IllegalStateException ( ioe );
                } finally {
                    if ( warFile != null ) {
                        this.closeJarFile();
                    }
                    if ( jarFileIs != null ) {
                        try {
                            jarFileIs.close();
                        } catch ( IOException ex ) {}
                    }
                }
            }
            return this.archiveEntries;
        }
    }
    @Override
    protected JarEntry getArchiveEntry ( final String pathInArchive ) {
        throw new IllegalStateException ( "Coding error" );
    }
    @Override
    protected void initInternal() throws LifecycleException {
        try ( final JarFile warFile = new JarFile ( this.getBase() ) ) {
            final JarEntry jarFileInWar = warFile.getJarEntry ( this.archivePath );
            final InputStream jarFileIs = warFile.getInputStream ( jarFileInWar );
            try ( final JarInputStream jarIs = new JarInputStream ( jarFileIs ) ) {
                this.setManifest ( jarIs.getManifest() );
            }
        } catch ( IOException ioe ) {
            throw new IllegalArgumentException ( ioe );
        }
        try {
            this.setBaseUrl ( UriUtil.buildJarSafeUrl ( new File ( this.getBase() ) ) );
        } catch ( MalformedURLException e ) {
            throw new IllegalArgumentException ( e );
        }
    }
}
