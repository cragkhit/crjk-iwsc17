package org.apache.catalina.webresources;
import org.apache.juli.logging.LogFactory;
import java.util.jar.Manifest;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.CopyOption;
import java.io.InputStream;
import org.apache.catalina.util.ResourceSet;
import java.util.Set;
import org.apache.catalina.WebResource;
import org.apache.catalina.LifecycleException;
import java.io.File;
import org.apache.catalina.WebResourceRoot;
import org.apache.juli.logging.Log;
public class DirResourceSet extends AbstractFileResourceSet {
    private static final Log log;
    public DirResourceSet() {
        super ( "/" );
    }
    public DirResourceSet ( final WebResourceRoot root, final String webAppMount, final String base, final String internalPath ) {
        super ( internalPath );
        this.setRoot ( root );
        this.setWebAppMount ( webAppMount );
        this.setBase ( base );
        if ( root.getContext().getAddWebinfClassesResources() ) {
            File f = new File ( base, internalPath );
            f = new File ( f, "/WEB-INF/classes/META-INF/resources" );
            if ( f.isDirectory() ) {
                root.createWebResourceSet ( WebResourceRoot.ResourceSetType.RESOURCE_JAR, "/", f.getAbsolutePath(), null, "/" );
            }
        }
        if ( this.getRoot().getState().isAvailable() ) {
            try {
                this.start();
            } catch ( LifecycleException e ) {
                throw new IllegalStateException ( e );
            }
        }
    }
    @Override
    public WebResource getResource ( String path ) {
        this.checkPath ( path );
        final String webAppMount = this.getWebAppMount();
        final WebResourceRoot root = this.getRoot();
        if ( !path.startsWith ( webAppMount ) ) {
            return new EmptyResource ( root, path );
        }
        final File f = this.file ( path.substring ( webAppMount.length() ), false );
        if ( f == null ) {
            return new EmptyResource ( root, path );
        }
        if ( !f.exists() ) {
            return new EmptyResource ( root, path, f );
        }
        if ( f.isDirectory() && path.charAt ( path.length() - 1 ) != '/' ) {
            path += '/';
        }
        return new FileResource ( root, path, f, this.isReadOnly(), this.getManifest() );
    }
    @Override
    public String[] list ( String path ) {
        this.checkPath ( path );
        final String webAppMount = this.getWebAppMount();
        if ( path.startsWith ( webAppMount ) ) {
            final File f = this.file ( path.substring ( webAppMount.length() ), true );
            if ( f == null ) {
                return DirResourceSet.EMPTY_STRING_ARRAY;
            }
            final String[] result = f.list();
            if ( result == null ) {
                return DirResourceSet.EMPTY_STRING_ARRAY;
            }
            return result;
        } else {
            if ( !path.endsWith ( "/" ) ) {
                path += "/";
            }
            if ( !webAppMount.startsWith ( path ) ) {
                return DirResourceSet.EMPTY_STRING_ARRAY;
            }
            final int i = webAppMount.indexOf ( 47, path.length() );
            if ( i == -1 ) {
                return new String[] { webAppMount.substring ( path.length() ) };
            }
            return new String[] { webAppMount.substring ( path.length(), i ) };
        }
    }
    @Override
    public Set<String> listWebAppPaths ( String path ) {
        this.checkPath ( path );
        final String webAppMount = this.getWebAppMount();
        final ResourceSet<String> result = new ResourceSet<String>();
        if ( path.startsWith ( webAppMount ) ) {
            final File f = this.file ( path.substring ( webAppMount.length() ), true );
            if ( f != null ) {
                final File[] list = f.listFiles();
                if ( list != null ) {
                    for ( final File entry : list ) {
                        final StringBuilder sb = new StringBuilder ( path );
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
    @Override
    public boolean mkdir ( final String path ) {
        this.checkPath ( path );
        if ( this.isReadOnly() ) {
            return false;
        }
        final String webAppMount = this.getWebAppMount();
        if ( path.startsWith ( webAppMount ) ) {
            final File f = this.file ( path.substring ( webAppMount.length() ), false );
            return f != null && f.mkdir();
        }
        return false;
    }
    @Override
    public boolean write ( final String path, final InputStream is, final boolean overwrite ) {
        this.checkPath ( path );
        if ( is == null ) {
            throw new NullPointerException ( DirResourceSet.sm.getString ( "dirResourceSet.writeNpe" ) );
        }
        if ( this.isReadOnly() ) {
            return false;
        }
        File dest = null;
        final String webAppMount = this.getWebAppMount();
        if ( !path.startsWith ( webAppMount ) ) {
            return false;
        }
        dest = this.file ( path.substring ( webAppMount.length() ), false );
        if ( dest == null ) {
            return false;
        }
        if ( dest.exists() && !overwrite ) {
            return false;
        }
        try {
            if ( overwrite ) {
                Files.copy ( is, dest.toPath(), StandardCopyOption.REPLACE_EXISTING );
            } else {
                Files.copy ( is, dest.toPath(), new CopyOption[0] );
            }
        } catch ( IOException ioe ) {
            return false;
        }
        return true;
    }
    @Override
    protected void checkType ( final File file ) {
        if ( !file.isDirectory() ) {
            throw new IllegalArgumentException ( DirResourceSet.sm.getString ( "dirResourceSet.notDirectory", this.getBase(), File.separator, this.getInternalPath() ) );
        }
    }
    @Override
    protected void initInternal() throws LifecycleException {
        super.initInternal();
        if ( this.getWebAppMount().equals ( "" ) ) {
            final File mf = this.file ( "META-INF/MANIFEST.MF", true );
            if ( mf != null && mf.isFile() ) {
                try ( final FileInputStream fis = new FileInputStream ( mf ) ) {
                    this.setManifest ( new Manifest ( fis ) );
                } catch ( IOException e ) {
                    DirResourceSet.log.warn ( DirResourceSet.sm.getString ( "dirResourceSet.manifestFail", mf.getAbsolutePath() ), e );
                }
            }
        }
    }
    static {
        log = LogFactory.getLog ( DirResourceSet.class );
    }
}
