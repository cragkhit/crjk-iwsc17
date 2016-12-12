package org.apache.catalina.loader;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import javax.management.ObjectName;
import javax.servlet.ServletContext;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Loader;
import org.apache.catalina.util.LifecycleMBeanBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.res.StringManager;
public class WebappLoader extends LifecycleMBeanBase
    implements Loader, PropertyChangeListener {
    public WebappLoader() {
        this ( null );
    }
    public WebappLoader ( ClassLoader parent ) {
        super();
        this.parentClassLoader = parent;
    }
    private WebappClassLoaderBase classLoader = null;
    private Context context = null;
    private boolean delegate = false;
    private String loaderClass = ParallelWebappClassLoader.class.getName();
    private ClassLoader parentClassLoader = null;
    private boolean reloadable = false;
    protected static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    protected final PropertyChangeSupport support = new PropertyChangeSupport ( this );
    private String classpath = null;
    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }
    @Override
    public Context getContext() {
        return context;
    }
    @Override
    public void setContext ( Context context ) {
        if ( this.context == context ) {
            return;
        }
        if ( getState().isAvailable() ) {
            throw new IllegalStateException (
                sm.getString ( "webappLoader.setContext.ise" ) );
        }
        if ( this.context != null ) {
            this.context.removePropertyChangeListener ( this );
        }
        Context oldContext = this.context;
        this.context = context;
        support.firePropertyChange ( "context", oldContext, this.context );
        if ( this.context != null ) {
            setReloadable ( this.context.getReloadable() );
            this.context.addPropertyChangeListener ( this );
        }
    }
    @Override
    public boolean getDelegate() {
        return this.delegate;
    }
    @Override
    public void setDelegate ( boolean delegate ) {
        boolean oldDelegate = this.delegate;
        this.delegate = delegate;
        support.firePropertyChange ( "delegate", Boolean.valueOf ( oldDelegate ),
                                     Boolean.valueOf ( this.delegate ) );
    }
    public String getLoaderClass() {
        return ( this.loaderClass );
    }
    public void setLoaderClass ( String loaderClass ) {
        this.loaderClass = loaderClass;
    }
    @Override
    public boolean getReloadable() {
        return this.reloadable;
    }
    @Override
    public void setReloadable ( boolean reloadable ) {
        boolean oldReloadable = this.reloadable;
        this.reloadable = reloadable;
        support.firePropertyChange ( "reloadable",
                                     Boolean.valueOf ( oldReloadable ),
                                     Boolean.valueOf ( this.reloadable ) );
    }
    @Override
    public void addPropertyChangeListener ( PropertyChangeListener listener ) {
        support.addPropertyChangeListener ( listener );
    }
    @Override
    public void backgroundProcess() {
        if ( reloadable && modified() ) {
            try {
                Thread.currentThread().setContextClassLoader
                ( WebappLoader.class.getClassLoader() );
                if ( context != null ) {
                    context.reload();
                }
            } finally {
                if ( context != null && context.getLoader() != null ) {
                    Thread.currentThread().setContextClassLoader
                    ( context.getLoader().getClassLoader() );
                }
            }
        }
    }
    public String[] getLoaderRepositories() {
        if ( classLoader == null ) {
            return new String[0];
        }
        URL[] urls = classLoader.getURLs();
        String[] result = new String[urls.length];
        for ( int i = 0; i < urls.length; i++ ) {
            result[i] = urls[i].toExternalForm();
        }
        return result;
    }
    public String getLoaderRepositoriesString() {
        String repositories[] = getLoaderRepositories();
        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < repositories.length ; i++ ) {
            sb.append ( repositories[i] ).append ( ":" );
        }
        return sb.toString();
    }
    public String getClasspath() {
        return classpath;
    }
    @Override
    public boolean modified() {
        return classLoader != null ? classLoader.modified() : false ;
    }
    @Override
    public void removePropertyChangeListener ( PropertyChangeListener listener ) {
        support.removePropertyChangeListener ( listener );
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "WebappLoader[" );
        if ( context != null ) {
            sb.append ( context.getName() );
        }
        sb.append ( "]" );
        return ( sb.toString() );
    }
    @Override
    protected void startInternal() throws LifecycleException {
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "webappLoader.starting" ) );
        }
        if ( context.getResources() == null ) {
            log.info ( "No resources for " + context );
            setState ( LifecycleState.STARTING );
            return;
        }
        try {
            classLoader = createClassLoader();
            classLoader.setResources ( context.getResources() );
            classLoader.setDelegate ( this.delegate );
            setClassPath();
            setPermissions();
            ( ( Lifecycle ) classLoader ).start();
            String contextName = context.getName();
            if ( !contextName.startsWith ( "/" ) ) {
                contextName = "/" + contextName;
            }
            ObjectName cloname = new ObjectName ( context.getDomain() + ":type=" +
                                                  classLoader.getClass().getSimpleName() + ",host=" +
                                                  context.getParent().getName() + ",context=" + contextName );
            Registry.getRegistry ( null, null )
            .registerComponent ( classLoader, cloname, null );
        } catch ( Throwable t ) {
            t = ExceptionUtils.unwrapInvocationTargetException ( t );
            ExceptionUtils.handleThrowable ( t );
            log.error ( "LifecycleException ", t );
            throw new LifecycleException ( "start: ", t );
        }
        setState ( LifecycleState.STARTING );
    }
    @Override
    protected void stopInternal() throws LifecycleException {
        if ( log.isDebugEnabled() ) {
            log.debug ( sm.getString ( "webappLoader.stopping" ) );
        }
        setState ( LifecycleState.STOPPING );
        ServletContext servletContext = context.getServletContext();
        servletContext.removeAttribute ( Globals.CLASS_PATH_ATTR );
        if ( classLoader != null ) {
            try {
                classLoader.stop();
            } finally {
                classLoader.destroy();
            }
            try {
                String contextName = context.getName();
                if ( !contextName.startsWith ( "/" ) ) {
                    contextName = "/" + contextName;
                }
                ObjectName cloname = new ObjectName ( context.getDomain() + ":type=" +
                                                      classLoader.getClass().getSimpleName() + ",host=" +
                                                      context.getParent().getName() + ",context=" + contextName );
                Registry.getRegistry ( null, null ).unregisterComponent ( cloname );
            } catch ( Exception e ) {
                log.error ( "LifecycleException ", e );
            }
        }
        classLoader = null;
    }
    @Override
    public void propertyChange ( PropertyChangeEvent event ) {
        if ( ! ( event.getSource() instanceof Context ) ) {
            return;
        }
        if ( event.getPropertyName().equals ( "reloadable" ) ) {
            try {
                setReloadable
                ( ( ( Boolean ) event.getNewValue() ).booleanValue() );
            } catch ( NumberFormatException e ) {
                log.error ( sm.getString ( "webappLoader.reloadable",
                                           event.getNewValue().toString() ) );
            }
        }
    }
    private WebappClassLoaderBase createClassLoader()
    throws Exception {
        Class<?> clazz = Class.forName ( loaderClass );
        WebappClassLoaderBase classLoader = null;
        if ( parentClassLoader == null ) {
            parentClassLoader = context.getParentClassLoader();
        }
        Class<?>[] argTypes = { ClassLoader.class };
        Object[] args = { parentClassLoader };
        Constructor<?> constr = clazz.getConstructor ( argTypes );
        classLoader = ( WebappClassLoaderBase ) constr.newInstance ( args );
        return classLoader;
    }
    private void setPermissions() {
        if ( !Globals.IS_SECURITY_ENABLED ) {
            return;
        }
        if ( context == null ) {
            return;
        }
        ServletContext servletContext = context.getServletContext();
        File workDir =
            ( File ) servletContext.getAttribute ( ServletContext.TEMPDIR );
        if ( workDir != null ) {
            try {
                String workDirPath = workDir.getCanonicalPath();
                classLoader.addPermission
                ( new FilePermission ( workDirPath, "read,write" ) );
                classLoader.addPermission
                ( new FilePermission ( workDirPath + File.separator + "-",
                                       "read,write,delete" ) );
            } catch ( IOException e ) {
            }
        }
        for ( URL url : context.getResources().getBaseUrls() ) {
            classLoader.addPermission ( url );
        }
    }
    private void setClassPath() {
        if ( context == null ) {
            return;
        }
        ServletContext servletContext = context.getServletContext();
        if ( servletContext == null ) {
            return;
        }
        StringBuilder classpath = new StringBuilder();
        ClassLoader loader = getClassLoader();
        if ( delegate && loader != null ) {
            loader = loader.getParent();
        }
        while ( loader != null ) {
            if ( !buildClassPath ( classpath, loader ) ) {
                break;
            }
            loader = loader.getParent();
        }
        if ( delegate ) {
            loader = getClassLoader();
            if ( loader != null ) {
                buildClassPath ( classpath, loader );
            }
        }
        this.classpath = classpath.toString();
        servletContext.setAttribute ( Globals.CLASS_PATH_ATTR, this.classpath );
    }
    private boolean buildClassPath ( StringBuilder classpath, ClassLoader loader ) {
        if ( loader instanceof URLClassLoader ) {
            URL repositories[] = ( ( URLClassLoader ) loader ).getURLs();
            for ( int i = 0; i < repositories.length; i++ ) {
                String repository = repositories[i].toString();
                if ( repository.startsWith ( "file://" ) ) {
                    repository = utf8Decode ( repository.substring ( 7 ) );
                } else if ( repository.startsWith ( "file:" ) ) {
                    repository = utf8Decode ( repository.substring ( 5 ) );
                } else {
                    continue;
                }
                if ( repository == null ) {
                    continue;
                }
                if ( classpath.length() > 0 ) {
                    classpath.append ( File.pathSeparator );
                }
                classpath.append ( repository );
            }
        } else if ( loader == ClassLoader.getSystemClassLoader() ) {
            String cp = System.getProperty ( "java.class.path" );
            if ( cp != null && cp.length() > 0 ) {
                if ( classpath.length() > 0 ) {
                    classpath.append ( File.pathSeparator );
                }
                classpath.append ( cp );
            }
            return false;
        } else {
            log.info ( "Unknown loader " + loader + " " + loader.getClass() );
            return false;
        }
        return true;
    }
    private String utf8Decode ( String input ) {
        String result = null;
        try {
            result = URLDecoder.decode ( input, "UTF-8" );
        } catch ( UnsupportedEncodingException uee ) {
        }
        return result;
    }
    private static final Log log = LogFactory.getLog ( WebappLoader.class );
    @Override
    protected String getDomainInternal() {
        return context.getDomain();
    }
    @Override
    protected String getObjectNameKeyProperties() {
        StringBuilder name = new StringBuilder ( "type=Loader" );
        name.append ( ",host=" );
        name.append ( context.getParent().getName() );
        name.append ( ",context=" );
        String contextName = context.getName();
        if ( !contextName.startsWith ( "/" ) ) {
            name.append ( "/" );
        }
        name.append ( contextName );
        return name.toString();
    }
}
