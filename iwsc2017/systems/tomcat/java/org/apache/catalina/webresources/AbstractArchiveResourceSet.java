package org.apache.catalina.webresources;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.util.ResourceSet;
public abstract class AbstractArchiveResourceSet extends AbstractResourceSet {
    private URL baseUrl;
    private String baseUrlString;
    private JarFile archive = null;
    protected HashMap<String, JarEntry> archiveEntries = null;
    protected final Object archiveLock = new Object();
    private long archiveUseCount = 0;
    protected final void setBaseUrl ( URL baseUrl ) {
        this.baseUrl = baseUrl;
        if ( baseUrl == null ) {
            this.baseUrlString = null;
        } else {
            this.baseUrlString = baseUrl.toString();
        }
    }
    @Override
    public final URL getBaseUrl() {
        return baseUrl;
    }
    protected final String getBaseUrlString() {
        return baseUrlString;
    }
    @Override
    public final String[] list ( String path ) {
        checkPath ( path );
        String webAppMount = getWebAppMount();
        ArrayList<String> result = new ArrayList<>();
        if ( path.startsWith ( webAppMount ) ) {
            String pathInJar =
                getInternalPath() + path.substring ( webAppMount.length() );
            if ( pathInJar.length() > 0 && pathInJar.charAt ( 0 ) == '/' ) {
                pathInJar = pathInJar.substring ( 1 );
            }
            Iterator<String> entries = getArchiveEntries ( false ).keySet().iterator();
            while ( entries.hasNext() ) {
                String name = entries.next();
                if ( name.length() > pathInJar.length() &&
                        name.startsWith ( pathInJar ) ) {
                    if ( name.charAt ( name.length() - 1 ) == '/' ) {
                        name = name.substring (
                                   pathInJar.length(), name.length() - 1 );
                    } else {
                        name = name.substring ( pathInJar.length() );
                    }
                    if ( name.length() == 0 ) {
                        continue;
                    }
                    if ( name.charAt ( 0 ) == '/' ) {
                        name = name.substring ( 1 );
                    }
                    if ( name.length() > 0 && name.lastIndexOf ( '/' ) == -1 ) {
                        result.add ( name );
                    }
                }
            }
        } else {
            if ( !path.endsWith ( "/" ) ) {
                path = path + "/";
            }
            if ( webAppMount.startsWith ( path ) ) {
                int i = webAppMount.indexOf ( '/', path.length() );
                if ( i == -1 ) {
                    return new String[] {webAppMount.substring ( path.length() ) };
                } else {
                    return new String[] {
                               webAppMount.substring ( path.length(), i )
                           };
                }
            }
        }
        return result.toArray ( new String[result.size()] );
    }
    @Override
    public final Set<String> listWebAppPaths ( String path ) {
        checkPath ( path );
        String webAppMount = getWebAppMount();
        ResourceSet<String> result = new ResourceSet<>();
        if ( path.startsWith ( webAppMount ) ) {
            String pathInJar =
                getInternalPath() + path.substring ( webAppMount.length() );
            if ( pathInJar.length() > 0 ) {
                if ( pathInJar.charAt ( pathInJar.length() - 1 ) != '/' ) {
                    pathInJar = pathInJar.substring ( 1 ) + '/';
                }
                if ( pathInJar.charAt ( 0 ) == '/' ) {
                    pathInJar = pathInJar.substring ( 1 );
                }
            }
            Iterator<String> entries = getArchiveEntries ( false ).keySet().iterator();
            while ( entries.hasNext() ) {
                String name = entries.next();
                if ( name.length() > pathInJar.length() &&
                        name.startsWith ( pathInJar ) ) {
                    int nextSlash = name.indexOf ( '/', pathInJar.length() );
                    if ( nextSlash == -1 || nextSlash == name.length() - 1 ) {
                        if ( name.startsWith ( pathInJar ) ) {
                            result.add ( webAppMount + '/' +
                                         name.substring ( getInternalPath().length() ) );
                        }
                    }
                }
            }
        } else {
            if ( !path.endsWith ( "/" ) ) {
                path = path + "/";
            }
            if ( webAppMount.startsWith ( path ) ) {
                int i = webAppMount.indexOf ( '/', path.length() );
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
    protected abstract HashMap<String, JarEntry> getArchiveEntries ( boolean single );
    protected abstract JarEntry getArchiveEntry ( String pathInArchive );
    @Override
    public final boolean mkdir ( String path ) {
        checkPath ( path );
        return false;
    }
    @Override
    public final boolean write ( String path, InputStream is, boolean overwrite ) {
        checkPath ( path );
        if ( is == null ) {
            throw new NullPointerException (
                sm.getString ( "dirResourceSet.writeNpe" ) );
        }
        return false;
    }
    @Override
    public final WebResource getResource ( String path ) {
        checkPath ( path );
        String webAppMount = getWebAppMount();
        WebResourceRoot root = getRoot();
        if ( path.startsWith ( webAppMount ) ) {
            String pathInJar = getInternalPath() + path.substring (
                                   webAppMount.length(), path.length() );
            if ( pathInJar.length() > 0 && pathInJar.charAt ( 0 ) == '/' ) {
                pathInJar = pathInJar.substring ( 1 );
            }
            if ( pathInJar.equals ( "" ) ) {
                if ( !path.endsWith ( "/" ) ) {
                    path = path + "/";
                }
                return new JarResourceRoot ( root, new File ( getBase() ),
                                             baseUrlString, path );
            } else {
                Map<String, JarEntry> jarEntries = getArchiveEntries ( true );
                JarEntry jarEntry = null;
                if ( ! ( pathInJar.charAt ( pathInJar.length() - 1 ) == '/' ) ) {
                    if ( jarEntries == null ) {
                        jarEntry = getArchiveEntry ( pathInJar + '/' );
                    } else {
                        jarEntry = jarEntries.get ( pathInJar + '/' );
                    }
                    if ( jarEntry != null ) {
                        path = path + '/';
                    }
                }
                if ( jarEntry == null ) {
                    if ( jarEntries == null ) {
                        jarEntry = getArchiveEntry ( pathInJar );
                    } else {
                        jarEntry = jarEntries.get ( pathInJar );
                    }
                }
                if ( jarEntry == null ) {
                    return new EmptyResource ( root, path );
                } else {
                    return createArchiveResource ( jarEntry, path, getManifest() );
                }
            }
        } else {
            return new EmptyResource ( root, path );
        }
    }
    protected abstract WebResource createArchiveResource ( JarEntry jarEntry,
            String webAppPath, Manifest manifest );
    @Override
    public final boolean isReadOnly() {
        return true;
    }
    @Override
    public void setReadOnly ( boolean readOnly ) {
        if ( readOnly ) {
            return;
        }
        throw new IllegalArgumentException (
            sm.getString ( "abstractArchiveResourceSet.setReadOnlyFalse" ) );
    }
    protected JarFile openJarFile() throws IOException {
        synchronized ( archiveLock ) {
            if ( archive == null ) {
                archive = new JarFile ( getBase() );
            }
            archiveUseCount++;
            return archive;
        }
    }
    protected void closeJarFile() {
        synchronized ( archiveLock ) {
            archiveUseCount--;
        }
    }
    @Override
    public void gc() {
        synchronized ( archiveLock ) {
            if ( archive != null && archiveUseCount == 0 ) {
                try {
                    archive.close();
                } catch ( IOException e ) {
                }
                archive = null;
                archiveEntries = null;
            }
        }
    }
}
