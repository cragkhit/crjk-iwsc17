package org.apache.catalina.webresources;
import java.io.InputStream;
import org.apache.catalina.util.ResourceSet;
import java.util.Set;
import java.io.File;
import java.util.jar.Manifest;
import org.apache.catalina.WebResource;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
public class FileResourceSet extends AbstractFileResourceSet {
    public FileResourceSet() {
        super ( "/" );
    }
    public FileResourceSet ( final WebResourceRoot root, final String webAppMount, final String base, final String internalPath ) {
        super ( internalPath );
        this.setRoot ( root );
        this.setWebAppMount ( webAppMount );
        this.setBase ( base );
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
        if ( !path.equals ( webAppMount ) ) {
            if ( path.charAt ( path.length() - 1 ) != '/' ) {
                path += '/';
            }
            if ( webAppMount.startsWith ( path ) ) {
                String name = path.substring ( 0, path.length() - 1 );
                name = name.substring ( name.lastIndexOf ( 47 ) + 1 );
                if ( name.length() > 0 ) {
                    return new VirtualResource ( root, path, name );
                }
            }
            return new EmptyResource ( root, path );
        }
        final File f = this.file ( "", true );
        if ( f == null ) {
            return new EmptyResource ( root, path );
        }
        return new FileResource ( root, path, f, this.isReadOnly(), null );
    }
    @Override
    public String[] list ( String path ) {
        this.checkPath ( path );
        if ( path.charAt ( path.length() - 1 ) != '/' ) {
            path += '/';
        }
        String webAppMount = this.getWebAppMount();
        if ( webAppMount.startsWith ( path ) ) {
            webAppMount = webAppMount.substring ( path.length() );
            if ( webAppMount.equals ( this.getFileBase().getName() ) ) {
                return new String[] { this.getFileBase().getName() };
            }
            final int i = webAppMount.indexOf ( 47 );
            if ( i > 0 ) {
                return new String[] { webAppMount.substring ( 0, i ) };
            }
        }
        return FileResourceSet.EMPTY_STRING_ARRAY;
    }
    @Override
    public Set<String> listWebAppPaths ( String path ) {
        this.checkPath ( path );
        final ResourceSet<String> result = new ResourceSet<String>();
        if ( path.charAt ( path.length() - 1 ) != '/' ) {
            path += '/';
        }
        String webAppMount = this.getWebAppMount();
        if ( webAppMount.startsWith ( path ) ) {
            webAppMount = webAppMount.substring ( path.length() );
            if ( webAppMount.equals ( this.getFileBase().getName() ) ) {
                result.add ( path + this.getFileBase().getName() );
            } else {
                final int i = webAppMount.indexOf ( 47 );
                if ( i > 0 ) {
                    result.add ( path + webAppMount.substring ( 0, i + 1 ) );
                }
            }
        }
        result.setLocked ( true );
        return result;
    }
    @Override
    public boolean mkdir ( final String path ) {
        this.checkPath ( path );
        return false;
    }
    @Override
    public boolean write ( final String path, final InputStream is, final boolean overwrite ) {
        this.checkPath ( path );
        return false;
    }
    @Override
    protected void checkType ( final File file ) {
        if ( !file.isFile() ) {
            throw new IllegalArgumentException ( FileResourceSet.sm.getString ( "fileResourceSet.notFile", this.getBase(), File.separator, this.getInternalPath() ) );
        }
    }
}
