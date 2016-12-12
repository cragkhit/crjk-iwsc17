package org.apache.catalina.webresources;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.tomcat.util.res.StringManager;
import java.util.jar.Manifest;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceSet;
import org.apache.catalina.util.LifecycleBase;
public abstract class AbstractResourceSet extends LifecycleBase implements WebResourceSet {
    private WebResourceRoot root;
    private String base;
    private String internalPath;
    private String webAppMount;
    private boolean classLoaderOnly;
    private boolean staticOnly;
    private Manifest manifest;
    protected static final StringManager sm;
    public AbstractResourceSet() {
        this.internalPath = "";
    }
    protected final void checkPath ( final String path ) {
        if ( path == null || path.length() == 0 || path.charAt ( 0 ) != '/' ) {
            throw new IllegalArgumentException ( AbstractResourceSet.sm.getString ( "abstractResourceSet.checkPath", path ) );
        }
    }
    @Override
    public final void setRoot ( final WebResourceRoot root ) {
        this.root = root;
    }
    protected final WebResourceRoot getRoot() {
        return this.root;
    }
    protected final String getInternalPath() {
        return this.internalPath;
    }
    public final void setInternalPath ( final String internalPath ) {
        this.checkPath ( internalPath );
        if ( internalPath.equals ( "/" ) ) {
            this.internalPath = "";
        } else {
            this.internalPath = internalPath;
        }
    }
    public final void setWebAppMount ( final String webAppMount ) {
        this.checkPath ( webAppMount );
        if ( webAppMount.equals ( "/" ) ) {
            this.webAppMount = "";
        } else {
            this.webAppMount = webAppMount;
        }
    }
    protected final String getWebAppMount() {
        return this.webAppMount;
    }
    public final void setBase ( final String base ) {
        this.base = base;
    }
    protected final String getBase() {
        return this.base;
    }
    @Override
    public boolean getClassLoaderOnly() {
        return this.classLoaderOnly;
    }
    @Override
    public void setClassLoaderOnly ( final boolean classLoaderOnly ) {
        this.classLoaderOnly = classLoaderOnly;
    }
    @Override
    public boolean getStaticOnly() {
        return this.staticOnly;
    }
    @Override
    public void setStaticOnly ( final boolean staticOnly ) {
        this.staticOnly = staticOnly;
    }
    protected final void setManifest ( final Manifest manifest ) {
        this.manifest = manifest;
    }
    protected final Manifest getManifest() {
        return this.manifest;
    }
    @Override
    protected final void startInternal() throws LifecycleException {
        this.setState ( LifecycleState.STARTING );
    }
    @Override
    protected final void stopInternal() throws LifecycleException {
        this.setState ( LifecycleState.STOPPING );
    }
    @Override
    protected final void destroyInternal() throws LifecycleException {
        this.gc();
    }
    static {
        sm = StringManager.getManager ( AbstractResourceSet.class );
    }
}
