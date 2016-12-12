package org.apache.catalina.webresources;
import java.io.IOException;
import java.util.jar.Manifest;
import java.util.Map;
import org.apache.catalina.WebResourceRoot;
import java.io.File;
import org.apache.catalina.WebResource;
import java.io.InputStream;
import org.apache.catalina.util.ResourceSet;
import java.util.Set;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.HashMap;
import java.util.jar.JarFile;
import java.net.URL;
public abstract class AbstractArchiveResourceSet extends AbstractResourceSet {
    private URL baseUrl;
    private String baseUrlString;
    private JarFile archive;
    protected HashMap<String, JarEntry> archiveEntries;
    protected final Object archiveLock;
    private long archiveUseCount;
    public AbstractArchiveResourceSet() {
        this.archive = null;
        this.archiveEntries = null;
        this.archiveLock = new Object();
        this.archiveUseCount = 0L;
    }
    protected final void setBaseUrl ( final URL baseUrl ) {
        this.baseUrl = baseUrl;
        if ( baseUrl == null ) {
            this.baseUrlString = null;
        } else {
            this.baseUrlString = baseUrl.toString();
        }
    }
    @Override
    public final URL getBaseUrl() {
        return this.baseUrl;
    }
    protected final String getBaseUrlString() {
        return this.baseUrlString;
    }
    @Override
    public final String[] list ( String path ) {
        this.checkPath ( path );
        final String webAppMount = this.getWebAppMount();
        final ArrayList<String> result = new ArrayList<String>();
        if ( path.startsWith ( webAppMount ) ) {
            String pathInJar = this.getInternalPath() + path.substring ( webAppMount.length() );
            if ( pathInJar.length() > 0 && pathInJar.charAt ( 0 ) == '/' ) {
                pathInJar = pathInJar.substring ( 1 );
            }
            for ( String name : this.getArchiveEntries ( false ).keySet() ) {
                if ( name.length() > pathInJar.length() && name.startsWith ( pathInJar ) ) {
                    if ( name.charAt ( name.length() - 1 ) == '/' ) {
                        name = name.substring ( pathInJar.length(), name.length() - 1 );
                    } else {
                        name = name.substring ( pathInJar.length() );
                    }
                    if ( name.length() == 0 ) {
                        continue;
                    }
                    if ( name.charAt ( 0 ) == '/' ) {
                        name = name.substring ( 1 );
                    }
                    if ( name.length() <= 0 || name.lastIndexOf ( 47 ) != -1 ) {
                        continue;
                    }
                    result.add ( name );
                }
            }
        } else {
            if ( !path.endsWith ( "/" ) ) {
                path += "/";
            }
            if ( webAppMount.startsWith ( path ) ) {
                final int i = webAppMount.indexOf ( 47, path.length() );
                if ( i == -1 ) {
                    return new String[] { webAppMount.substring ( path.length() ) };
                }
                return new String[] { webAppMount.substring ( path.length(), i ) };
            }
        }
        return result.toArray ( new String[result.size()] );
    }
    @Override
    public final Set<String> listWebAppPaths ( String path ) {
        this.checkPath ( path );
        final String webAppMount = this.getWebAppMount();
        final ResourceSet<String> result = new ResourceSet<String>();
        if ( path.startsWith ( webAppMount ) ) {
            String pathInJar = this.getInternalPath() + path.substring ( webAppMount.length() );
            if ( pathInJar.length() > 0 ) {
                if ( pathInJar.charAt ( pathInJar.length() - 1 ) != '/' ) {
                    pathInJar = pathInJar.substring ( 1 ) + '/';
                }
                if ( pathInJar.charAt ( 0 ) == '/' ) {
                    pathInJar = pathInJar.substring ( 1 );
                }
            }
            for ( final String name : this.getArchiveEntries ( false ).keySet() ) {
                if ( name.length() > pathInJar.length() && name.startsWith ( pathInJar ) ) {
                    final int nextSlash = name.indexOf ( 47, pathInJar.length() );
                    if ( ( nextSlash != -1 && nextSlash != name.length() - 1 ) || !name.startsWith ( pathInJar ) ) {
                        continue;
                    }
                    result.add ( webAppMount + '/' + name.substring ( this.getInternalPath().length() ) );
                }
            }
        } else {
            if ( !path.endsWith ( "/" ) ) {
                path += "/";
            }
            if ( webAppMount.startsWith ( path ) ) {
                final int i = webAppMount.indexOf ( 47, path.length() );
                if ( i == -1 ) {
                    result.add ( webAppMount + "/" );
                } else {
                    result.add ( webAppMount.substring ( 0, i + 1 ) );
                }
            }
        }
        result.setLocked ( true );
        return result;
    }
    protected abstract HashMap<String, JarEntry> getArchiveEntries ( final boolean p0 );
    protected abstract JarEntry getArchiveEntry ( final String p0 );
    @Override
    public final boolean mkdir ( final String path ) {
        this.checkPath ( path );
        return false;
    }
    @Override
    public final boolean write ( final String path, final InputStream is, final boolean overwrite ) {
        this.checkPath ( path );
        if ( is == null ) {
            throw new NullPointerException ( AbstractArchiveResourceSet.sm.getString ( "dirResourceSet.writeNpe" ) );
        }
        return false;
    }
    @Override
    public final WebResource getResource ( String path ) {
        this.checkPath ( path );
        final String webAppMount = this.getWebAppMount();
        final WebResourceRoot root = this.getRoot();
        if ( !path.startsWith ( webAppMount ) ) {
            return new EmptyResource ( root, path );
        }
        String pathInJar = this.getInternalPath() + path.substring ( webAppMount.length(), path.length() );
        if ( pathInJar.length() > 0 && pathInJar.charAt ( 0 ) == '/' ) {
            pathInJar = pathInJar.substring ( 1 );
        }
        if ( pathInJar.equals ( "" ) ) {
            if ( !path.endsWith ( "/" ) ) {
                path += "/";
            }
            return new JarResourceRoot ( root, new File ( this.getBase() ), this.baseUrlString, path );
        }
        final Map<String, JarEntry> jarEntries = this.getArchiveEntries ( true );
        JarEntry jarEntry = null;
        if ( pathInJar.charAt ( pathInJar.length() - 1 ) != '/' ) {
            if ( jarEntries == null ) {
                jarEntry = this.getArchiveEntry ( pathInJar + '/' );
            } else {
                jarEntry = jarEntries.get ( pathInJar + '/' );
            }
            if ( jarEntry != null ) {
                path += '/';
            }
        }
        if ( jarEntry == null ) {
            if ( jarEntries == null ) {
                jarEntry = this.getArchiveEntry ( pathInJar );
            } else {
                jarEntry = jarEntries.get ( pathInJar );
            }
        }
        if ( jarEntry == null ) {
            return new EmptyResource ( root, path );
        }
        return this.createArchiveResource ( jarEntry, path, this.getManifest() );
    }
    protected abstract WebResource createArchiveResource ( final JarEntry p0, final String p1, final Manifest p2 );
    @Override
    public final boolean isReadOnly() {
        return true;
    }
    @Override
    public void setReadOnly ( final boolean readOnly ) {
        if ( readOnly ) {
            return;
        }
        throw new IllegalArgumentException ( AbstractArchiveResourceSet.sm.getString ( "abstractArchiveResourceSet.setReadOnlyFalse" ) );
    }
    protected JarFile openJarFile() throws IOException {
        synchronized ( this.archiveLock ) {
            if ( this.archive == null ) {
                this.archive = new JarFile ( this.getBase() );
            }
            ++this.archiveUseCount;
            return this.archive;
        }
    }
    protected void closeJarFile() {
        synchronized ( this.archiveLock ) {
            --this.archiveUseCount;
        }
    }
    @Override
    public void gc() {
        synchronized ( this.archiveLock ) {
            if ( this.archive != null && this.archiveUseCount == 0L ) {
                try {
                    this.archive.close();
                } catch ( IOException ex ) {}
                this.archive = null;
                this.archiveEntries = null;
            }
        }
    }
}
