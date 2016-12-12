package org.apache.catalina.webresources;
import java.net.MalformedURLException;
import org.apache.tomcat.util.buf.UriUtil;
import java.io.File;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.HashMap;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
public abstract class AbstractSingleArchiveResourceSet extends AbstractArchiveResourceSet {
    public AbstractSingleArchiveResourceSet() {
    }
    public AbstractSingleArchiveResourceSet ( final WebResourceRoot root, final String webAppMount, final String base, final String internalPath ) throws IllegalArgumentException {
        this.setRoot ( root );
        this.setWebAppMount ( webAppMount );
        this.setBase ( base );
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
    protected HashMap<String, JarEntry> getArchiveEntries ( final boolean single ) {
        synchronized ( this.archiveLock ) {
            if ( this.archiveEntries == null && !single ) {
                JarFile jarFile = null;
                this.archiveEntries = new HashMap<String, JarEntry>();
                try {
                    jarFile = this.openJarFile();
                    final Enumeration<JarEntry> entries = jarFile.entries();
                    while ( entries.hasMoreElements() ) {
                        final JarEntry entry = entries.nextElement();
                        this.archiveEntries.put ( entry.getName(), entry );
                    }
                } catch ( IOException ioe ) {
                    this.archiveEntries = null;
                    throw new IllegalStateException ( ioe );
                } finally {
                    if ( jarFile != null ) {
                        this.closeJarFile();
                    }
                }
            }
            return this.archiveEntries;
        }
    }
    @Override
    protected JarEntry getArchiveEntry ( final String pathInArchive ) {
        JarFile jarFile = null;
        try {
            jarFile = this.openJarFile();
            return jarFile.getJarEntry ( pathInArchive );
        } catch ( IOException ioe ) {
            throw new IllegalStateException ( ioe );
        } finally {
            if ( jarFile != null ) {
                this.closeJarFile();
            }
        }
    }
    @Override
    protected void initInternal() throws LifecycleException {
        try ( final JarFile jarFile = new JarFile ( this.getBase() ) ) {
            this.setManifest ( jarFile.getManifest() );
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
