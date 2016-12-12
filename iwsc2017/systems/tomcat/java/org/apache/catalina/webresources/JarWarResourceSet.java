package org.apache.catalina.webresources;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.tomcat.util.buf.UriUtil;
public class JarWarResourceSet extends AbstractArchiveResourceSet {
    private final String archivePath;
    public JarWarResourceSet ( WebResourceRoot root, String webAppMount,
                               String base, String archivePath, String internalPath )
    throws IllegalArgumentException {
        setRoot ( root );
        setWebAppMount ( webAppMount );
        setBase ( base );
        this.archivePath = archivePath;
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
    protected WebResource createArchiveResource ( JarEntry jarEntry,
            String webAppPath, Manifest manifest ) {
        return new JarWarResource ( this, webAppPath, getBaseUrlString(), jarEntry, archivePath );
    }
    @Override
    protected HashMap<String, JarEntry> getArchiveEntries ( boolean single ) {
        synchronized ( archiveLock ) {
            if ( archiveEntries == null ) {
                JarFile warFile = null;
                InputStream jarFileIs = null;
                archiveEntries = new HashMap<>();
                try {
                    warFile = openJarFile();
                    JarEntry jarFileInWar = warFile.getJarEntry ( archivePath );
                    jarFileIs = warFile.getInputStream ( jarFileInWar );
                    try ( JarInputStream jarIs = new JarInputStream ( jarFileIs ) ) {
                        JarEntry entry = jarIs.getNextJarEntry();
                        while ( entry != null ) {
                            archiveEntries.put ( entry.getName(), entry );
                            entry = jarIs.getNextJarEntry();
                        }
                        setManifest ( jarIs.getManifest() );
                    }
                } catch ( IOException ioe ) {
                    archiveEntries = null;
                    throw new IllegalStateException ( ioe );
                } finally {
                    if ( warFile != null ) {
                        closeJarFile();
                    }
                    if ( jarFileIs != null ) {
                        try {
                            jarFileIs.close();
                        } catch ( IOException e ) {
                        }
                    }
                }
            }
            return archiveEntries;
        }
    }
    @Override
    protected JarEntry getArchiveEntry ( String pathInArchive ) {
        throw new IllegalStateException ( "Coding error" );
    }
    @Override
    protected void initInternal() throws LifecycleException {
        try ( JarFile warFile = new JarFile ( getBase() ) ) {
            JarEntry jarFileInWar = warFile.getJarEntry ( archivePath );
            InputStream jarFileIs = warFile.getInputStream ( jarFileInWar );
            try ( JarInputStream jarIs = new JarInputStream ( jarFileIs ) ) {
                setManifest ( jarIs.getManifest() );
            }
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
