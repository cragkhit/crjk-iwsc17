package org.apache.catalina.webresources;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.catalina.LifecycleException;
import org.apache.tomcat.util.http.RequestUtil;
public abstract class AbstractFileResourceSet extends AbstractResourceSet {
    protected static final String[] EMPTY_STRING_ARRAY = new String[0];
    private File fileBase;
    private String absoluteBase;
    private String canonicalBase;
    private boolean readOnly = false;
    protected AbstractFileResourceSet ( String internalPath ) {
        setInternalPath ( internalPath );
    }
    protected final File getFileBase() {
        return fileBase;
    }
    @Override
    public void setReadOnly ( boolean readOnly ) {
        this.readOnly = readOnly;
    }
    @Override
    public boolean isReadOnly() {
        return readOnly;
    }
    protected final File file ( String name, boolean mustExist ) {
        if ( name.equals ( "/" ) ) {
            name = "";
        }
        File file = new File ( fileBase, name );
        if ( !mustExist || file.canRead() ) {
            if ( getRoot().getAllowLinking() ) {
                return file;
            }
            String canPath = null;
            try {
                canPath = file.getCanonicalPath();
            } catch ( IOException e ) {
            }
            if ( canPath == null ) {
                return null;
            }
            if ( !canPath.startsWith ( canonicalBase ) ) {
                return null;
            }
            String fileAbsPath = file.getAbsolutePath();
            if ( fileAbsPath.endsWith ( "." ) ) {
                fileAbsPath = fileAbsPath + '/';
            }
            String absPath = normalize ( fileAbsPath );
            if ( ( absoluteBase.length() < absPath.length() )
                    && ( canonicalBase.length() < canPath.length() ) ) {
                absPath = absPath.substring ( absoluteBase.length() + 1 );
                if ( absPath.equals ( "" ) ) {
                    absPath = "/";
                }
                canPath = canPath.substring ( canonicalBase.length() + 1 );
                if ( canPath.equals ( "" ) ) {
                    canPath = "/";
                }
                if ( !canPath.equals ( absPath ) ) {
                    return null;
                }
            }
        } else {
            return null;
        }
        return file;
    }
    private String normalize ( String path ) {
        return RequestUtil.normalize ( path, File.separatorChar == '/' );
    }
    @Override
    public URL getBaseUrl() {
        try {
            return getFileBase().toURI().toURL();
        } catch ( MalformedURLException e ) {
            return null;
        }
    }
    @Override
    public void gc() {
    }
    @Override
    protected void initInternal() throws LifecycleException {
        fileBase = new File ( getBase(), getInternalPath() );
        checkType ( fileBase );
        String absolutePath = fileBase.getAbsolutePath();
        if ( absolutePath.endsWith ( "." ) ) {
            absolutePath = absolutePath + '/';
        }
        this.absoluteBase = normalize ( absolutePath );
        try {
            this.canonicalBase = fileBase.getCanonicalPath();
        } catch ( IOException e ) {
            throw new IllegalArgumentException ( e );
        }
    }
    protected abstract void checkType ( File file );
}
