package org.apache.catalina.webresources;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.LifecycleException;
import java.net.URL;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceSet;
import org.apache.catalina.util.LifecycleBase;
public class EmptyResourceSet extends LifecycleBase implements WebResourceSet {
    private static final String[] EMPTY_STRING_ARRAY;
    private WebResourceRoot root;
    private boolean classLoaderOnly;
    private boolean staticOnly;
    public EmptyResourceSet ( final WebResourceRoot root ) {
        this.root = root;
    }
    @Override
    public WebResource getResource ( final String path ) {
        return new EmptyResource ( this.root, path );
    }
    @Override
    public String[] list ( final String path ) {
        return EmptyResourceSet.EMPTY_STRING_ARRAY;
    }
    @Override
    public Set<String> listWebAppPaths ( final String path ) {
        return Collections.emptySet();
    }
    @Override
    public boolean mkdir ( final String path ) {
        return false;
    }
    @Override
    public boolean write ( final String path, final InputStream is, final boolean overwrite ) {
        return false;
    }
    @Override
    public void setRoot ( final WebResourceRoot root ) {
        this.root = root;
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
    @Override
    public URL getBaseUrl() {
        return null;
    }
    @Override
    public void setReadOnly ( final boolean readOnly ) {
    }
    @Override
    public boolean isReadOnly() {
        return true;
    }
    @Override
    public void gc() {
    }
    @Override
    protected void initInternal() throws LifecycleException {
    }
    @Override
    protected void startInternal() throws LifecycleException {
        this.setState ( LifecycleState.STARTING );
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        this.setState ( LifecycleState.STOPPING );
    }
    @Override
    protected void destroyInternal() throws LifecycleException {
    }
    static {
        EMPTY_STRING_ARRAY = new String[0];
    }
}
