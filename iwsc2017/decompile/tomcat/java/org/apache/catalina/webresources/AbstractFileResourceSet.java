package org.apache.catalina.webresources;
import org.apache.catalina.LifecycleException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.tomcat.util.http.RequestUtil;
import java.io.IOException;
import java.io.File;
public abstract class AbstractFileResourceSet extends AbstractResourceSet {
    protected static final String[] EMPTY_STRING_ARRAY;
    private File fileBase;
    private String absoluteBase;
    private String canonicalBase;
    private boolean readOnly;
    protected AbstractFileResourceSet ( final String internalPath ) {
        this.readOnly = false;
        this.setInternalPath ( internalPath );
    }
    protected final File getFileBase() {
        return this.fileBase;
    }
    @Override
    public void setReadOnly ( final boolean readOnly ) {
        this.readOnly = readOnly;
    }
    @Override
    public boolean isReadOnly() {
        return this.readOnly;
    }
    protected final File file ( String name, final boolean mustExist ) {
        if ( name.equals ( "/" ) ) {
            name = "";
        }
        final File file = new File ( this.fileBase, name );
        if ( mustExist && !file.canRead() ) {
            return null;
        }
        if ( this.getRoot().getAllowLinking() ) {
            return file;
        }
        String canPath = null;
        try {
            canPath = file.getCanonicalPath();
        } catch ( IOException ex ) {}
        if ( canPath == null ) {
            return null;
        }
        if ( !canPath.startsWith ( this.canonicalBase ) ) {
            return null;
        }
        String fileAbsPath = file.getAbsolutePath();
        if ( fileAbsPath.endsWith ( "." ) ) {
            fileAbsPath += '/';
        }
        String absPath = this.normalize ( fileAbsPath );
        if ( this.absoluteBase.length() < absPath.length() && this.canonicalBase.length() < canPath.length() ) {
            absPath = absPath.substring ( this.absoluteBase.length() + 1 );
            if ( absPath.equals ( "" ) ) {
                absPath = "/";
            }
            canPath = canPath.substring ( this.canonicalBase.length() + 1 );
            if ( canPath.equals ( "" ) ) {
                canPath = "/";
            }
            if ( !canPath.equals ( absPath ) ) {
                return null;
            }
        }
        return file;
    }
    private String normalize ( final String path ) {
        return RequestUtil.normalize ( path, File.separatorChar == '/' );
    }
    @Override
    public URL getBaseUrl() {
        try {
            return this.getFileBase().toURI().toURL();
        } catch ( MalformedURLException e ) {
            return null;
        }
    }
    @Override
    public void gc() {
    }
    @Override
    protected void initInternal() throws LifecycleException {
        this.checkType ( this.fileBase = new File ( this.getBase(), this.getInternalPath() ) );
        String absolutePath = this.fileBase.getAbsolutePath();
        if ( absolutePath.endsWith ( "." ) ) {
            absolutePath += '/';
        }
        this.absoluteBase = this.normalize ( absolutePath );
        try {
            this.canonicalBase = this.fileBase.getCanonicalPath();
        } catch ( IOException e ) {
            throw new IllegalArgumentException ( e );
        }
    }
    protected abstract void checkType ( final File p0 );
    static {
        EMPTY_STRING_ARRAY = new String[0];
    }
}
