package org.apache.catalina.webresources;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.jar.Manifest;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceRoot.ResourceSetType;
import org.apache.catalina.util.ResourceSet;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class DirResourceSet extends AbstractFileResourceSet {
    private static final Log log = LogFactory.getLog ( DirResourceSet.class );
    public DirResourceSet() {
        super ( "/" );
    }
    public DirResourceSet ( WebResourceRoot root, String webAppMount, String base,
                            String internalPath ) {
        super ( internalPath );
        setRoot ( root );
        setWebAppMount ( webAppMount );
        setBase ( base );
        if ( root.getContext().getAddWebinfClassesResources() ) {
            File f = new File ( base, internalPath );
            f = new File ( f, "/WEB-INF/classes/META-INF/resources" );
            if ( f.isDirectory() ) {
                root.createWebResourceSet ( ResourceSetType.RESOURCE_JAR, "/",
                                            f.getAbsolutePath(), null, "/" );
            }
        }
        if ( getRoot().getState().isAvailable() ) {
            try {
                start();
            } catch ( LifecycleException e ) {
                throw new IllegalStateException ( e );
            }
        }
    }
    @Override
    public WebResource getResource ( String path ) {
        checkPath ( path );
        String webAppMount = getWebAppMount();
        WebResourceRoot root = getRoot();
        if ( path.startsWith ( webAppMount ) ) {
            File f = file ( path.substring ( webAppMount.length() ), false );
            if ( f == null ) {
                return new EmptyResource ( root, path );
            }
            if ( !f.exists() ) {
                return new EmptyResource ( root, path, f );
            }
            if ( f.isDirectory() && path.charAt ( path.length() - 1 ) != '/' ) {
                path = path + '/';
            }
            return new FileResource ( root, path, f, isReadOnly(), getManifest() );
        } else {
            return new EmptyResource ( root, path );
        }
    }
    @Override
    public String[] list ( String path ) {
        checkPath ( path );
        String webAppMount = getWebAppMount();
        if ( path.startsWith ( webAppMount ) ) {
            File f = file ( path.substring ( webAppMount.length() ), true );
            if ( f == null ) {
                return EMPTY_STRING_ARRAY;
            }
            String[] result = f.list();
            if ( result == null ) {
                return EMPTY_STRING_ARRAY;
            } else {
                return result;
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
            return EMPTY_STRING_ARRAY;
        }
    }
    @Override
    public Set<String> listWebAppPaths ( String path ) {
        checkPath ( path );
        String webAppMount = getWebAppMount();
        ResourceSet<String> result = new ResourceSet<>();
        if ( path.startsWith ( webAppMount ) ) {
            File f = file ( path.substring ( webAppMount.length() ), true );
            if ( f != null ) {
                File[] list = f.listFiles();
                if ( list != null ) {
                    for ( File entry : list ) {
                        StringBuilder sb = new StringBuilder ( path );
                        if ( path.charAt ( path.length() - 1 ) != '/' ) {
                            sb.append ( '/' );
                        }
                        sb.append ( entry.getName() );
                        if ( entry.isDirectory() ) {
                            sb.append ( '/' );
                        }
                        result.add ( sb.toString() );
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
    @Override
    public boolean mkdir ( String path ) {
        checkPath ( path );
        if ( isReadOnly() ) {
            return false;
        }
        String webAppMount = getWebAppMount();
        if ( path.startsWith ( webAppMount ) ) {
            File f = file ( path.substring ( webAppMount.length() ), false );
            if ( f == null ) {
                return false;
            }
            return f.mkdir();
        } else {
            return false;
        }
    }
    @Override
    public boolean write ( String path, InputStream is, boolean overwrite ) {
        checkPath ( path );
        if ( is == null ) {
            throw new NullPointerException (
                sm.getString ( "dirResourceSet.writeNpe" ) );
        }
        if ( isReadOnly() ) {
            return false;
        }
        File dest = null;
        String webAppMount = getWebAppMount();
        if ( path.startsWith ( webAppMount ) ) {
            dest = file ( path.substring ( webAppMount.length() ), false );
            if ( dest == null ) {
                return false;
            }
        } else {
            return false;
        }
        if ( dest.exists() && !overwrite ) {
            return false;
        }
        try {
            if ( overwrite ) {
                Files.copy ( is, dest.toPath(), StandardCopyOption.REPLACE_EXISTING );
            } else {
                Files.copy ( is, dest.toPath() );
            }
        } catch ( IOException ioe ) {
            return false;
        }
        return true;
    }
    @Override
    protected void checkType ( File file ) {
        if ( file.isDirectory() == false ) {
            throw new IllegalArgumentException ( sm.getString ( "dirResourceSet.notDirectory",
                                                 getBase(), File.separator, getInternalPath() ) );
        }
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        if ( getWebAppMount().equals ( "" ) ) {
            File mf = file ( "META-INF/MANIFEST.MF", true );
            if ( mf != null && mf.isFile() ) {
                try ( FileInputStream fis = new FileInputStream ( mf ) ) {
                    setManifest ( new Manifest ( fis ) );
                } catch ( IOException e ) {
                    log.warn ( sm.getString ( "dirResourceSet.manifestFail", mf.getAbsolutePath() ), e );
                }
            }
        }
    }
}
