package org.apache.catalina.loader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.apache.juli.logging.LogFactory;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.io.IOException;
import java.security.Permission;
import java.io.FilePermission;
import java.io.File;
import org.apache.catalina.Globals;
import java.lang.reflect.Constructor;
import java.beans.PropertyChangeEvent;
import javax.servlet.ServletContext;
import org.apache.catalina.LifecycleException;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.modeler.Registry;
import javax.management.ObjectName;
import org.apache.catalina.LifecycleState;
import java.net.URL;
import org.apache.juli.logging.Log;
import java.beans.PropertyChangeSupport;
import org.apache.tomcat.util.res.StringManager;
import org.apache.catalina.Context;
import java.beans.PropertyChangeListener;
import org.apache.catalina.Loader;
import org.apache.catalina.util.LifecycleMBeanBase;
public class WebappLoader extends LifecycleMBeanBase implements Loader, PropertyChangeListener {
    private WebappClassLoaderBase classLoader;
    private Context context;
    private boolean delegate;
    private String loaderClass;
    private ClassLoader parentClassLoader;
    private boolean reloadable;
    protected static final StringManager sm;
    protected final PropertyChangeSupport support;
    private String classpath;
    private static final Log log;
    public WebappLoader() {
        this ( null );
    }
    public WebappLoader ( final ClassLoader parent ) {
        this.classLoader = null;
        this.context = null;
        this.delegate = false;
        this.loaderClass = ParallelWebappClassLoader.class.getName();
        this.parentClassLoader = null;
        this.reloadable = false;
        this.support = new PropertyChangeSupport ( this );
        this.classpath = null;
        this.parentClassLoader = parent;
    }
    @Override
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }
    @Override
    public Context getContext() {
        return this.context;
    }
    @Override
    public void setContext ( final Context context ) {
        if ( this.context == context ) {
            return;
        }
        if ( this.getState().isAvailable() ) {
            throw new IllegalStateException ( WebappLoader.sm.getString ( "webappLoader.setContext.ise" ) );
        }
        if ( this.context != null ) {
            this.context.removePropertyChangeListener ( this );
        }
        final Context oldContext = this.context;
        this.context = context;
        this.support.firePropertyChange ( "context", oldContext, this.context );
        if ( this.context != null ) {
            this.setReloadable ( this.context.getReloadable() );
            this.context.addPropertyChangeListener ( this );
        }
    }
    @Override
    public boolean getDelegate() {
        return this.delegate;
    }
    @Override
    public void setDelegate ( final boolean delegate ) {
        final boolean oldDelegate = this.delegate;
        this.delegate = delegate;
        this.support.firePropertyChange ( "delegate", oldDelegate, ( Object ) this.delegate );
    }
    public String getLoaderClass() {
        return this.loaderClass;
    }
    public void setLoaderClass ( final String loaderClass ) {
        this.loaderClass = loaderClass;
    }
    @Override
    public boolean getReloadable() {
        return this.reloadable;
    }
    @Override
    public void setReloadable ( final boolean reloadable ) {
        final boolean oldReloadable = this.reloadable;
        this.reloadable = reloadable;
        this.support.firePropertyChange ( "reloadable", oldReloadable, ( Object ) this.reloadable );
    }
    @Override
    public void addPropertyChangeListener ( final PropertyChangeListener listener ) {
        this.support.addPropertyChangeListener ( listener );
    }
    @Override
    public void backgroundProcess() {
        if ( this.reloadable && this.modified() ) {
            try {
                Thread.currentThread().setContextClassLoader ( WebappLoader.class.getClassLoader() );
                if ( this.context != null ) {
                    this.context.reload();
                }
            } finally {
                if ( this.context != null && this.context.getLoader() != null ) {
                    Thread.currentThread().setContextClassLoader ( this.context.getLoader().getClassLoader() );
                }
            }
        }
    }
    public String[] getLoaderRepositories() {
        if ( this.classLoader == null ) {
            return new String[0];
        }
        final URL[] urls = this.classLoader.getURLs();
        final String[] result = new String[urls.length];
        for ( int i = 0; i < urls.length; ++i ) {
            result[i] = urls[i].toExternalForm();
        }
        return result;
    }
    public String getLoaderRepositoriesString() {
        final String[] repositories = this.getLoaderRepositories();
        final StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < repositories.length; ++i ) {
            sb.append ( repositories[i] ).append ( ":" );
        }
        return sb.toString();
    }
    public String getClasspath() {
        return this.classpath;
    }
    @Override
    public boolean modified() {
        return this.classLoader != null && this.classLoader.modified();
    }
    @Override
    public void removePropertyChangeListener ( final PropertyChangeListener listener ) {
        this.support.removePropertyChangeListener ( listener );
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "WebappLoader[" );
        if ( this.context != null ) {
            sb.append ( this.context.getName() );
        }
        sb.append ( "]" );
        return sb.toString();
    }
    @Override
    protected void startInternal() throws LifecycleException {
        if ( WebappLoader.log.isDebugEnabled() ) {
            WebappLoader.log.debug ( WebappLoader.sm.getString ( "webappLoader.starting" ) );
        }
        if ( this.context.getResources() == null ) {
            WebappLoader.log.info ( "No resources for " + this.context );
            this.setState ( LifecycleState.STARTING );
            return;
        }
        try {
            ( this.classLoader = this.createClassLoader() ).setResources ( this.context.getResources() );
            this.classLoader.setDelegate ( this.delegate );
            this.setClassPath();
            this.setPermissions();
            this.classLoader.start();
            String contextName = this.context.getName();
            if ( !contextName.startsWith ( "/" ) ) {
                contextName = "/" + contextName;
            }
            final ObjectName cloname = new ObjectName ( this.context.getDomain() + ":type=" + this.classLoader.getClass().getSimpleName() + ",host=" + this.context.getParent().getName() + ",context=" + contextName );
            Registry.getRegistry ( null, null ).registerComponent ( this.classLoader, cloname, null );
        } catch ( Throwable t ) {
            t = ExceptionUtils.unwrapInvocationTargetException ( t );
            ExceptionUtils.handleThrowable ( t );
            WebappLoader.log.error ( "LifecycleException ", t );
            throw new LifecycleException ( "start: ", t );
        }
        this.setState ( LifecycleState.STARTING );
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        if ( WebappLoader.log.isDebugEnabled() ) {
            WebappLoader.log.debug ( WebappLoader.sm.getString ( "webappLoader.stopping" ) );
        }
        this.setState ( LifecycleState.STOPPING );
        final ServletContext servletContext = this.context.getServletContext();
        servletContext.removeAttribute ( "org.apache.catalina.jsp_classpath" );
        if ( this.classLoader != null ) {
            try {
                this.classLoader.stop();
            } finally {
                this.classLoader.destroy();
            }
            try {
                String contextName = this.context.getName();
                if ( !contextName.startsWith ( "/" ) ) {
                    contextName = "/" + contextName;
                }
                final ObjectName cloname = new ObjectName ( this.context.getDomain() + ":type=" + this.classLoader.getClass().getSimpleName() + ",host=" + this.context.getParent().getName() + ",context=" + contextName );
                Registry.getRegistry ( null, null ).unregisterComponent ( cloname );
            } catch ( Exception e ) {
                WebappLoader.log.error ( "LifecycleException ", e );
            }
        }
        this.classLoader = null;
    }
    @Override
    public void propertyChange ( final PropertyChangeEvent event ) {
        if ( ! ( event.getSource() instanceof Context ) ) {
            return;
        }
        if ( event.getPropertyName().equals ( "reloadable" ) ) {
            try {
                this.setReloadable ( ( boolean ) event.getNewValue() );
            } catch ( NumberFormatException e ) {
                WebappLoader.log.error ( WebappLoader.sm.getString ( "webappLoader.reloadable", event.getNewValue().toString() ) );
            }
        }
    }
    private WebappClassLoaderBase createClassLoader() throws Exception {
        final Class<?> clazz = Class.forName ( this.loaderClass );
        WebappClassLoaderBase classLoader = null;
        if ( this.parentClassLoader == null ) {
            this.parentClassLoader = this.context.getParentClassLoader();
        }
        final Class<?>[] argTypes = ( Class<?>[] ) new Class[] { ClassLoader.class };
        final Object[] args = { this.parentClassLoader };
        final Constructor<?> constr = clazz.getConstructor ( argTypes );
        classLoader = ( WebappClassLoaderBase ) constr.newInstance ( args );
        return classLoader;
    }
    private void setPermissions() {
        if ( !Globals.IS_SECURITY_ENABLED ) {
            return;
        }
        if ( this.context == null ) {
            return;
        }
        final ServletContext servletContext = this.context.getServletContext();
        final File workDir = ( File ) servletContext.getAttribute ( "javax.servlet.context.tempdir" );
        if ( workDir != null ) {
            try {
                final String workDirPath = workDir.getCanonicalPath();
                this.classLoader.addPermission ( new FilePermission ( workDirPath, "read,write" ) );
                this.classLoader.addPermission ( new FilePermission ( workDirPath + File.separator + "-", "read,write,delete" ) );
            } catch ( IOException ex ) {}
        }
        for ( final URL url : this.context.getResources().getBaseUrls() ) {
            this.classLoader.addPermission ( url );
        }
    }
    private void setClassPath() {
        if ( this.context == null ) {
            return;
        }
        final ServletContext servletContext = this.context.getServletContext();
        if ( servletContext == null ) {
            return;
        }
        final StringBuilder classpath = new StringBuilder();
        ClassLoader loader = this.getClassLoader();
        if ( this.delegate && loader != null ) {
            loader = loader.getParent();
        }
        while ( loader != null && this.buildClassPath ( classpath, loader ) ) {
            loader = loader.getParent();
        }
        if ( this.delegate ) {
            loader = this.getClassLoader();
            if ( loader != null ) {
                this.buildClassPath ( classpath, loader );
            }
        }
        servletContext.setAttribute ( "org.apache.catalina.jsp_classpath", ( Object ) ( this.classpath = classpath.toString() ) );
    }
    private boolean buildClassPath ( final StringBuilder classpath, final ClassLoader loader ) {
        if ( loader instanceof URLClassLoader ) {
            final URL[] repositories = ( ( URLClassLoader ) loader ).getURLs();
            for ( int i = 0; i < repositories.length; ++i ) {
                String repository = repositories[i].toString();
                if ( repository.startsWith ( "file://" ) ) {
                    repository = this.utf8Decode ( repository.substring ( 7 ) );
                } else {
                    if ( !repository.startsWith ( "file:" ) ) {
                        continue;
                    }
                    repository = this.utf8Decode ( repository.substring ( 5 ) );
                }
                if ( repository != null ) {
                    if ( classpath.length() > 0 ) {
                        classpath.append ( File.pathSeparator );
                    }
                    classpath.append ( repository );
                }
            }
            return true;
        }
        if ( loader == ClassLoader.getSystemClassLoader() ) {
            final String cp = System.getProperty ( "java.class.path" );
            if ( cp != null && cp.length() > 0 ) {
                if ( classpath.length() > 0 ) {
                    classpath.append ( File.pathSeparator );
                }
                classpath.append ( cp );
            }
            return false;
        }
        WebappLoader.log.info ( "Unknown loader " + loader + " " + loader.getClass() );
        return false;
    }
    private String utf8Decode ( final String input ) {
        String result = null;
        try {
            result = URLDecoder.decode ( input, "UTF-8" );
        } catch ( UnsupportedEncodingException ex ) {}
        return result;
    }
    @Override
    protected String getDomainInternal() {
        return this.context.getDomain();
    }
    @Override
    protected String getObjectNameKeyProperties() {
        final StringBuilder name = new StringBuilder ( "type=Loader" );
        name.append ( ",host=" );
        name.append ( this.context.getParent().getName() );
        name.append ( ",context=" );
        final String contextName = this.context.getName();
        if ( !contextName.startsWith ( "/" ) ) {
            name.append ( "/" );
        }
        name.append ( contextName );
        return name.toString();
    }
    static {
        sm = StringManager.getManager ( "org.apache.catalina.loader" );
        log = LogFactory.getLog ( WebappLoader.class );
    }
}
