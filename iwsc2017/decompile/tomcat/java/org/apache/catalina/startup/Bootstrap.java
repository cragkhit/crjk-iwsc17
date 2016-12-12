package org.apache.catalina.startup;
import java.io.IOException;
import org.apache.juli.logging.LogFactory;
import java.util.regex.Matcher;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.catalina.security.SecurityClassLoad;
import java.util.List;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.io.File;
import org.apache.juli.logging.Log;
public final class Bootstrap {
    private static final Log log;
    private static Bootstrap daemon;
    private static final File catalinaBaseFile;
    private static final File catalinaHomeFile;
    private static final Pattern PATH_PATTERN;
    private Object catalinaDaemon;
    ClassLoader commonLoader;
    ClassLoader catalinaLoader;
    ClassLoader sharedLoader;
    public Bootstrap() {
        this.catalinaDaemon = null;
        this.commonLoader = null;
        this.catalinaLoader = null;
        this.sharedLoader = null;
    }
    private void initClassLoaders() {
        try {
            this.commonLoader = this.createClassLoader ( "common", null );
            if ( this.commonLoader == null ) {
                this.commonLoader = this.getClass().getClassLoader();
            }
            this.catalinaLoader = this.createClassLoader ( "server", this.commonLoader );
            this.sharedLoader = this.createClassLoader ( "shared", this.commonLoader );
        } catch ( Throwable t ) {
            handleThrowable ( t );
            Bootstrap.log.error ( "Class loader creation threw exception", t );
            System.exit ( 1 );
        }
    }
    private ClassLoader createClassLoader ( final String name, final ClassLoader parent ) throws Exception {
        String value = CatalinaProperties.getProperty ( name + ".loader" );
        if ( value == null || value.equals ( "" ) ) {
            return parent;
        }
        value = this.replace ( value );
        final List<ClassLoaderFactory.Repository> repositories = new ArrayList<ClassLoaderFactory.Repository>();
        final String[] paths;
        final String[] repositoryPaths = paths = getPaths ( value );
        for ( String repository : paths ) {
            try {
                final URL url = new URL ( repository );
                repositories.add ( new ClassLoaderFactory.Repository ( repository, ClassLoaderFactory.RepositoryType.URL ) );
            } catch ( MalformedURLException ex ) {
                if ( repository.endsWith ( "*.jar" ) ) {
                    repository = repository.substring ( 0, repository.length() - "*.jar".length() );
                    repositories.add ( new ClassLoaderFactory.Repository ( repository, ClassLoaderFactory.RepositoryType.GLOB ) );
                } else if ( repository.endsWith ( ".jar" ) ) {
                    repositories.add ( new ClassLoaderFactory.Repository ( repository, ClassLoaderFactory.RepositoryType.JAR ) );
                } else {
                    repositories.add ( new ClassLoaderFactory.Repository ( repository, ClassLoaderFactory.RepositoryType.DIR ) );
                }
            }
        }
        return ClassLoaderFactory.createClassLoader ( repositories, parent );
    }
    protected String replace ( final String str ) {
        String result = str;
        int pos_start = str.indexOf ( "${" );
        if ( pos_start >= 0 ) {
            final StringBuilder builder = new StringBuilder();
            int pos_end;
            for ( pos_end = -1; pos_start >= 0; pos_start = str.indexOf ( "${", pos_end + 1 ) ) {
                builder.append ( str, pos_end + 1, pos_start );
                pos_end = str.indexOf ( 125, pos_start + 2 );
                if ( pos_end < 0 ) {
                    pos_end = pos_start - 1;
                    break;
                }
                final String propName = str.substring ( pos_start + 2, pos_end );
                String replacement;
                if ( propName.length() == 0 ) {
                    replacement = null;
                } else if ( "catalina.home".equals ( propName ) ) {
                    replacement = getCatalinaHome();
                } else if ( "catalina.base".equals ( propName ) ) {
                    replacement = getCatalinaBase();
                } else {
                    replacement = System.getProperty ( propName );
                }
                if ( replacement != null ) {
                    builder.append ( replacement );
                } else {
                    builder.append ( str, pos_start, pos_end + 1 );
                }
            }
            builder.append ( str, pos_end + 1, str.length() );
            result = builder.toString();
        }
        return result;
    }
    public void init() throws Exception {
        this.initClassLoaders();
        Thread.currentThread().setContextClassLoader ( this.catalinaLoader );
        SecurityClassLoad.securityClassLoad ( this.catalinaLoader );
        if ( Bootstrap.log.isDebugEnabled() ) {
            Bootstrap.log.debug ( "Loading startup class" );
        }
        final Class<?> startupClass = this.catalinaLoader.loadClass ( "org.apache.catalina.startup.Catalina" );
        final Object startupInstance = startupClass.newInstance();
        if ( Bootstrap.log.isDebugEnabled() ) {
            Bootstrap.log.debug ( "Setting startup class properties" );
        }
        final String methodName = "setParentClassLoader";
        final Class<?>[] paramTypes = ( Class<?>[] ) new Class[] { Class.forName ( "java.lang.ClassLoader" ) };
        final Object[] paramValues = { this.sharedLoader };
        final Method method = startupInstance.getClass().getMethod ( methodName, paramTypes );
        method.invoke ( startupInstance, paramValues );
        this.catalinaDaemon = startupInstance;
    }
    private void load ( final String[] arguments ) throws Exception {
        final String methodName = "load";
        Class<?>[] paramTypes;
        Object[] param;
        if ( arguments == null || arguments.length == 0 ) {
            paramTypes = null;
            param = null;
        } else {
            paramTypes = ( Class<?>[] ) new Class[] { arguments.getClass() };
            param = new Object[] { arguments };
        }
        final Method method = this.catalinaDaemon.getClass().getMethod ( methodName, paramTypes );
        if ( Bootstrap.log.isDebugEnabled() ) {
            Bootstrap.log.debug ( "Calling startup class " + method );
        }
        method.invoke ( this.catalinaDaemon, param );
    }
    private Object getServer() throws Exception {
        final String methodName = "getServer";
        final Method method = this.catalinaDaemon.getClass().getMethod ( methodName, ( Class<?>[] ) new Class[0] );
        return method.invoke ( this.catalinaDaemon, new Object[0] );
    }
    public void init ( final String[] arguments ) throws Exception {
        this.init();
        this.load ( arguments );
    }
    public void start() throws Exception {
        if ( this.catalinaDaemon == null ) {
            this.init();
        }
        final Method method = this.catalinaDaemon.getClass().getMethod ( "start", ( Class<?>[] ) null );
        method.invoke ( this.catalinaDaemon, ( Object[] ) null );
    }
    public void stop() throws Exception {
        final Method method = this.catalinaDaemon.getClass().getMethod ( "stop", ( Class<?>[] ) null );
        method.invoke ( this.catalinaDaemon, ( Object[] ) null );
    }
    public void stopServer() throws Exception {
        final Method method = this.catalinaDaemon.getClass().getMethod ( "stopServer", ( Class<?>[] ) null );
        method.invoke ( this.catalinaDaemon, ( Object[] ) null );
    }
    public void stopServer ( final String[] arguments ) throws Exception {
        Class<?>[] paramTypes;
        Object[] param;
        if ( arguments == null || arguments.length == 0 ) {
            paramTypes = null;
            param = null;
        } else {
            paramTypes = ( Class<?>[] ) new Class[] { arguments.getClass() };
            param = new Object[] { arguments };
        }
        final Method method = this.catalinaDaemon.getClass().getMethod ( "stopServer", paramTypes );
        method.invoke ( this.catalinaDaemon, param );
    }
    public void setAwait ( final boolean await ) throws Exception {
        final Class<?>[] paramTypes = ( Class<?>[] ) new Class[] { Boolean.TYPE };
        final Object[] paramValues = { await };
        final Method method = this.catalinaDaemon.getClass().getMethod ( "setAwait", paramTypes );
        method.invoke ( this.catalinaDaemon, paramValues );
    }
    public boolean getAwait() throws Exception {
        final Class<?>[] paramTypes = ( Class<?>[] ) new Class[0];
        final Object[] paramValues = new Object[0];
        final Method method = this.catalinaDaemon.getClass().getMethod ( "getAwait", paramTypes );
        final Boolean b = ( Boolean ) method.invoke ( this.catalinaDaemon, paramValues );
        return b;
    }
    public void destroy() {
    }
    public static void main ( final String[] args ) {
        if ( Bootstrap.daemon == null ) {
            final Bootstrap bootstrap = new Bootstrap();
            try {
                bootstrap.init();
            } catch ( Throwable t ) {
                handleThrowable ( t );
                t.printStackTrace();
                return;
            }
            Bootstrap.daemon = bootstrap;
        } else {
            Thread.currentThread().setContextClassLoader ( Bootstrap.daemon.catalinaLoader );
        }
        try {
            String command = "start";
            if ( args.length > 0 ) {
                command = args[args.length - 1];
            }
            if ( command.equals ( "startd" ) ) {
                args[args.length - 1] = "start";
                Bootstrap.daemon.load ( args );
                Bootstrap.daemon.start();
            } else if ( command.equals ( "stopd" ) ) {
                args[args.length - 1] = "stop";
                Bootstrap.daemon.stop();
            } else if ( command.equals ( "start" ) ) {
                Bootstrap.daemon.setAwait ( true );
                Bootstrap.daemon.load ( args );
                Bootstrap.daemon.start();
            } else if ( command.equals ( "stop" ) ) {
                Bootstrap.daemon.stopServer ( args );
            } else if ( command.equals ( "configtest" ) ) {
                Bootstrap.daemon.load ( args );
                if ( null == Bootstrap.daemon.getServer() ) {
                    System.exit ( 1 );
                }
                System.exit ( 0 );
            } else {
                Bootstrap.log.warn ( "Bootstrap: command \"" + command + "\" does not exist." );
            }
        } catch ( Throwable t2 ) {
            if ( t2 instanceof InvocationTargetException && t2.getCause() != null ) {
                t2 = t2.getCause();
            }
            handleThrowable ( t2 );
            t2.printStackTrace();
            System.exit ( 1 );
        }
    }
    public static String getCatalinaHome() {
        return Bootstrap.catalinaHomeFile.getPath();
    }
    public static String getCatalinaBase() {
        return Bootstrap.catalinaBaseFile.getPath();
    }
    public static File getCatalinaHomeFile() {
        return Bootstrap.catalinaHomeFile;
    }
    public static File getCatalinaBaseFile() {
        return Bootstrap.catalinaBaseFile;
    }
    private static void handleThrowable ( final Throwable t ) {
        if ( t instanceof ThreadDeath ) {
            throw ( ThreadDeath ) t;
        }
        if ( t instanceof VirtualMachineError ) {
            throw ( VirtualMachineError ) t;
        }
    }
    protected static String[] getPaths ( final String value ) {
        final List<String> result = new ArrayList<String>();
        final Matcher matcher = Bootstrap.PATH_PATTERN.matcher ( value );
        while ( matcher.find() ) {
            String path = value.substring ( matcher.start(), matcher.end() );
            path = path.trim();
            if ( path.length() == 0 ) {
                continue;
            }
            final char first = path.charAt ( 0 );
            final char last = path.charAt ( path.length() - 1 );
            if ( first == '\"' && last == '\"' && path.length() > 1 ) {
                path = path.substring ( 1, path.length() - 1 );
                path = path.trim();
                if ( path.length() == 0 ) {
                    continue;
                }
            } else if ( path.contains ( "\"" ) ) {
                throw new IllegalArgumentException ( "The double quote [\"] character only be used to quote paths. It must not appear in a path. This loader path is not valid: [" + value + "]" );
            }
            result.add ( path );
        }
        return result.toArray ( new String[result.size()] );
    }
    static {
        log = LogFactory.getLog ( Bootstrap.class );
        Bootstrap.daemon = null;
        PATH_PATTERN = Pattern.compile ( "(\".*?\")|(([^,])*)" );
        final String userDir = System.getProperty ( "user.dir" );
        final String home = System.getProperty ( "catalina.home" );
        File homeFile = null;
        if ( home != null ) {
            final File f = new File ( home );
            try {
                homeFile = f.getCanonicalFile();
            } catch ( IOException ioe ) {
                homeFile = f.getAbsoluteFile();
            }
        }
        if ( homeFile == null ) {
            final File bootstrapJar = new File ( userDir, "bootstrap.jar" );
            if ( bootstrapJar.exists() ) {
                final File f2 = new File ( userDir, ".." );
                try {
                    homeFile = f2.getCanonicalFile();
                } catch ( IOException ioe2 ) {
                    homeFile = f2.getAbsoluteFile();
                }
            }
        }
        if ( homeFile == null ) {
            final File f = new File ( userDir );
            try {
                homeFile = f.getCanonicalFile();
            } catch ( IOException ioe ) {
                homeFile = f.getAbsoluteFile();
            }
        }
        catalinaHomeFile = homeFile;
        System.setProperty ( "catalina.home", Bootstrap.catalinaHomeFile.getPath() );
        final String base = System.getProperty ( "catalina.base" );
        if ( base == null ) {
            catalinaBaseFile = Bootstrap.catalinaHomeFile;
        } else {
            File baseFile = new File ( base );
            try {
                baseFile = baseFile.getCanonicalFile();
            } catch ( IOException ioe2 ) {
                baseFile = baseFile.getAbsoluteFile();
            }
            catalinaBaseFile = baseFile;
        }
        System.setProperty ( "catalina.base", Bootstrap.catalinaBaseFile.getPath() );
    }
}
