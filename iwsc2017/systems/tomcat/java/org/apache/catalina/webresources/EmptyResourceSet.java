package org.apache.catalina.webresources;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.WebResource;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceSet;
import org.apache.catalina.util.LifecycleBase;
public class EmptyResourceSet extends LifecycleBase implements WebResourceSet {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private WebResourceRoot root;
    private boolean classLoaderOnly;
    private boolean staticOnly;
    public EmptyResourceSet ( WebResourceRoot root ) {
        this.root = root;
    }
    @Override
    public WebResource getResource ( String path ) {
        return new EmptyResource ( root, path );
    }
    @Override
    public String[] list ( String path ) {
        return EMPTY_STRING_ARRAY;
    }
    @Override
    public Set<String> listWebAppPaths ( String path ) {
        return Collections.emptySet();
    }
    @Override
    public boolean mkdir ( String path ) {
        return false;
    }
    @Override
    public boolean write ( String path, InputStream is, boolean overwrite ) {
        return false;
    }
    @Override
    public void setRoot ( WebResourceRoot root ) {
        this.root = root;
    }
    @Override
    public boolean getClassLoaderOnly() {
        return classLoaderOnly;
    }
    @Override
    public void setClassLoaderOnly ( boolean classLoaderOnly ) {
        this.classLoaderOnly = classLoaderOnly;
    }
    @Override
    public boolean getStaticOnly() {
        return staticOnly;
    }
    @Override
    public void setStaticOnly ( boolean staticOnly ) {
        this.staticOnly = staticOnly;
    }
    @Override
    public URL getBaseUrl() {
        return null;
    }
    @Override
    public void setReadOnly ( boolean readOnly ) {
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
        setState ( LifecycleState.STARTING );
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        setState ( LifecycleState.STOPPING );
    }
    @Override
    protected void destroyInternal() throws LifecycleException {
    }
}
