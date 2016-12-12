package org.apache.catalina.webresources;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.tomcat.util.buf.UriUtil;
public abstract class AbstractSingleArchiveResourceSet extends AbstractArchiveResourceSet {
    public AbstractSingleArchiveResourceSet() {
    }
    public AbstractSingleArchiveResourceSet ( WebResourceRoot root, String webAppMount, String base,
            String internalPath ) throws IllegalArgumentException {
        setRoot ( root );
        setWebAppMount ( webAppMount );
        setBase ( base );
        setInternalPath ( internalPath );
        if ( getRoot().getState().isAvailable() ) {
            try {
                start();
            } catch ( LifecycleException e ) {
                throw new IllegalStateException ( e );
            }
        }
    }
    @Override
    protected HashMap<String, JarEntry> getArchiveEntries ( boolean single ) {
        synchronized ( archiveLock ) {
            if ( archiveEntries == null && !single ) {
                JarFile jarFile = null;
                archiveEntries = new HashMap<>();
                try {
                    jarFile = openJarFile();
                    Enumeration<JarEntry> entries = jarFile.entries();
                    while ( entries.hasMoreElements() ) {
                        JarEntry entry = entries.nextElement();
                        archiveEntries.put ( entry.getName(), entry );
                    }
                } catch ( IOException ioe ) {
                    archiveEntries = null;
                    throw new IllegalStateException ( ioe );
                } finally {
                    if ( jarFile != null ) {
                        closeJarFile();
                    }
                }
            }
            return archiveEntries;
        }
    }
    @Override
    protected JarEntry getArchiveEntry ( String pathInArchive ) {
        JarFile jarFile = null;
        try {
            jarFile = openJarFile();
            return jarFile.getJarEntry ( pathInArchive );
        } catch ( IOException ioe ) {
            throw new IllegalStateException ( ioe );
        } finally {
            if ( jarFile != null ) {
                closeJarFile();
            }
        }
    }
    @Override
    protected void initInternal() throws LifecycleException {
        try ( JarFile jarFile = new JarFile ( getBase() ) ) {
            setManifest ( jarFile.getManifest() );
        } catch ( IOException ioe ) {
            throw new IllegalArgumentException ( ioe );
        }
        try {
            setBaseUrl ( UriUtil.buildJarSafeUrl ( new File ( getBase() ) ) );
        } catch ( MalformedURLException e ) {
            throw new IllegalArgumentException ( e );
        }
    }
}
