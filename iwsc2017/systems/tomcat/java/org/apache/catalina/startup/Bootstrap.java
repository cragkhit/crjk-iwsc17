package org.apache.catalina.startup;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.catalina.Globals;
import org.apache.catalina.security.SecurityClassLoad;
import org.apache.catalina.startup.ClassLoaderFactory.Repository;
import org.apache.catalina.startup.ClassLoaderFactory.RepositoryType;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public final class Bootstrap {
    private static final Log log = LogFactory.getLog ( Bootstrap.class );
    private static Bootstrap daemon = null;
    private static final File catalinaBaseFile;
    private static final File catalinaHomeFile;
    private static final Pattern PATH_PATTERN = Pattern.compile ( "(\".*?\")|(([^,])*)" );
    static {
        String userDir = System.getProperty ( "user.dir" );
        String home = System.getProperty ( Globals.CATALINA_HOME_PROP );
        File homeFile = null;
        if ( home != null ) {
            File f = new File ( home );
            try {
                homeFile = f.getCanonicalFile();
            } catch ( IOException ioe ) {
                homeFile = f.getAbsoluteFile();
            }
        }
        if ( homeFile == null ) {
            File bootstrapJar = new File ( userDir, "bootstrap.jar" );
            if ( bootstrapJar.exists() ) {
                File f = new File ( userDir, ".." );
                try {
                    homeFile = f.getCanonicalFile();
                } catch ( IOException ioe ) {
                    homeFile = f.getAbsoluteFile();
                }
            }
        }
        if ( homeFile == null ) {
            File f = new File ( userDir );
            try {
                homeFile = f.getCanonicalFile();
            } catch ( IOException ioe ) {
                homeFile = f.getAbsoluteFile();
            }
        }
        catalinaHomeFile = homeFile;
        System.setProperty (
            Globals.CATALINA_HOME_PROP, catalinaHomeFile.getPath() );
        String base = System.getProperty ( Globals.CATALINA_BASE_PROP );
        if ( base == null ) {
            catalinaBaseFile = catalinaHomeFile;
        } else {
            File baseFile = new File ( base );
            try {
                baseFile = baseFile.getCanonicalFile();
            } catch ( IOException ioe ) {
                baseFile = baseFile.getAbsoluteFile();
            }
            catalinaBaseFile = baseFile;
        }
        System.setProperty (
            Globals.CATALINA_BASE_PROP, catalinaBaseFile.getPath() );
    }
    private Object catalinaDaemon = null;
    ClassLoader commonLoader = null;
    ClassLoader catalinaLoader = null;
    ClassLoader sharedLoader = null;
    private void initClassLoaders() {
        try {
            commonLoader = createClassLoader ( "common", null );
            if ( commonLoader == null ) {
                commonLoader = this.getClass().getClassLoader();
            }
            catalinaLoader = createClassLoader ( "server", commonLoader );
            sharedLoader = createClassLoader ( "shared", commonLoader );
        } catch ( Throwable t ) {
            handleThrowable ( t );
            log.error ( "Class loader creation threw exception", t );
            System.exit ( 1 );
        }
    }
    private ClassLoader createClassLoader ( String name, ClassLoader parent )
    throws Exception {
        String value = CatalinaProperties.getProperty ( name + ".loader" );
        if ( ( value == null ) || ( value.equals ( "" ) ) ) {
            return parent;
        }
        value = replace ( value );
        List<Repository> repositories = new ArrayList<>();
        String[] repositoryPaths = getPaths ( value );
        for ( String repository : repositoryPaths ) {
            try {
                @SuppressWarnings ( "unused" )
                URL url = new URL ( repository );
                repositories.add (
                    new Repository ( repository, RepositoryType.URL ) );
                continue;
            } catch ( MalformedURLException e ) {
            }
            if ( repository.endsWith ( "*.jar" ) ) {
                repository = repository.substring
                             ( 0, repository.length() - "*.jar".length() );
                repositories.add (
                    new Repository ( repository, RepositoryType.GLOB ) );
            } else if ( repository.endsWith ( ".jar" ) ) {
                repositories.add (
                    new Repository ( repository, RepositoryType.JAR ) );
            } else {
                repositories.add (
                    new Repository ( repository, RepositoryType.DIR ) );
            }
        }
        return ClassLoaderFactory.createClassLoader ( repositories, parent );
    }
    protected String replace ( String str ) {
        String result = str;
        int pos_start = str.indexOf ( "${" );
        if ( pos_start >= 0 ) {
            StringBuilder builder = new StringBuilder();
            int pos_end = -1;
            while ( pos_start >= 0 ) {
                builder.append ( str, pos_end + 1, pos_start );
                pos_end = str.indexOf ( '}', pos_start + 2 );
                if ( pos_end < 0 ) {
                    pos_end = pos_start - 1;
                    break;
                }
                String propName = str.substring ( pos_start + 2, pos_end );
                String replacement;
                if ( propName.length() == 0 ) {
                    replacement = null;
                } else if ( Globals.CATALINA_HOME_PROP.equals ( propName ) ) {
                    replacement = getCatalinaHome();
                } else if ( Globals.CATALINA_BASE_PROP.equals ( propName ) ) {
                    replacement = getCatalinaBase();
                } else {
                    replacement = System.getProperty ( propName );
                }
                if ( replacement != null ) {
                    builder.append ( replacement );
                } else {
                    builder.append ( str, pos_start, pos_end + 1 );
                }
                pos_start = str.indexOf ( "${", pos_end + 1 );
            }
            builder.append ( str, pos_end + 1, str.length() );
            result = builder.toString();
        }
        return result;
    }
    public void init() throws Exception {
        initClassLoaders();
        Thread.currentThread().setContextClassLoader ( catalinaLoader );
        SecurityClassLoad.securityClassLoad ( catalinaLoader );
        if ( log.isDebugEnabled() ) {
            log.debug ( "Loading startup class" );
        }
        Class<?> startupClass =
            catalinaLoader.loadClass
            ( "org.apache.catalina.startup.Catalina" );
        Object startupInstance = startupClass.newInstance();
        if ( log.isDebugEnabled() ) {
            log.debug ( "Setting startup class properties" );
        }
        String methodName = "setParentClassLoader";
        Class<?> paramTypes[] = new Class[1];
        paramTypes[0] = Class.forName ( "java.lang.ClassLoader" );
        Object paramValues[] = new Object[1];
        paramValues[0] = sharedLoader;
        Method method =
            startupInstance.getClass().getMethod ( methodName, paramTypes );
        method.invoke ( startupInstance, paramValues );
        catalinaDaemon = startupInstance;
    }
    private void load ( String[] arguments )
    throws Exception {
        String methodName = "load";
        Object param[];
        Class<?> paramTypes[];
        if ( arguments == null || arguments.length == 0 ) {
            paramTypes = null;
            param = null;
        } else {
            paramTypes = new Class[1];
            paramTypes[0] = arguments.getClass();
            param = new Object[1];
            param[0] = arguments;
        }
        Method method =
            catalinaDaemon.getClass().getMethod ( methodName, paramTypes );
        if ( log.isDebugEnabled() ) {
            log.debug ( "Calling startup class " + method );
        }
        method.invoke ( catalinaDaemon, param );
    }
    private Object getServer() throws Exception {
        String methodName = "getServer";
        Method method =
            catalinaDaemon.getClass().getMethod ( methodName );
        return method.invoke ( catalinaDaemon );
    }
    public void init ( String[] arguments )
    throws Exception {
        init();
        load ( arguments );
    }
    public void start()
    throws Exception {
        if ( catalinaDaemon == null ) {
            init();
        }
        Method method = catalinaDaemon.getClass().getMethod ( "start", ( Class [] ) null );
        method.invoke ( catalinaDaemon, ( Object [] ) null );
    }
    public void stop()
    throws Exception {
        Method method = catalinaDaemon.getClass().getMethod ( "stop", ( Class [] ) null );
        method.invoke ( catalinaDaemon, ( Object [] ) null );
    }
    public void stopServer()
    throws Exception {
        Method method =
            catalinaDaemon.getClass().getMethod ( "stopServer", ( Class [] ) null );
        method.invoke ( catalinaDaemon, ( Object [] ) null );
    }
    public void stopServer ( String[] arguments )
    throws Exception {
        Object param[];
        Class<?> paramTypes[];
        if ( arguments == null || arguments.length == 0 ) {
            paramTypes = null;
            param = null;
        } else {
            paramTypes = new Class[1];
            paramTypes[0] = arguments.getClass();
            param = new Object[1];
            param[0] = arguments;
        }
        Method method =
            catalinaDaemon.getClass().getMethod ( "stopServer", paramTypes );
        method.invoke ( catalinaDaemon, param );
    }
    public void setAwait ( boolean await )
    throws Exception {
        Class<?> paramTypes[] = new Class[1];
        paramTypes[0] = Boolean.TYPE;
        Object paramValues[] = new Object[1];
        paramValues[0] = Boolean.valueOf ( await );
        Method method =
            catalinaDaemon.getClass().getMethod ( "setAwait", paramTypes );
        method.invoke ( catalinaDaemon, paramValues );
    }
    public boolean getAwait()
    throws Exception {
        Class<?> paramTypes[] = new Class[0];
        Object paramValues[] = new Object[0];
        Method method =
            catalinaDaemon.getClass().getMethod ( "getAwait", paramTypes );
        Boolean b = ( Boolean ) method.invoke ( catalinaDaemon, paramValues );
        return b.booleanValue();
    }
    public void destroy() {
    }
    public static void main ( String args[] ) {
        if ( daemon == null ) {
            Bootstrap bootstrap = new Bootstrap();
            try {
                bootstrap.init();
            } catch ( Throwable t ) {
                handleThrowable ( t );
                t.printStackTrace();
                return;
            }
            daemon = bootstrap;
        } else {
            Thread.currentThread().setContextClassLoader ( daemon.catalinaLoader );
        }
        try {
            String command = "start";
            if ( args.length > 0 ) {
                command = args[args.length - 1];
            }
            if ( command.equals ( "startd" ) ) {
                args[args.length - 1] = "start";
                daemon.load ( args );
                daemon.start();
            } else if ( command.equals ( "stopd" ) ) {
                args[args.length - 1] = "stop";
                daemon.stop();
            } else if ( command.equals ( "start" ) ) {
                daemon.setAwait ( true );
                daemon.load ( args );
                daemon.start();
            } else if ( command.equals ( "stop" ) ) {
                daemon.stopServer ( args );
            } else if ( command.equals ( "configtest" ) ) {
                daemon.load ( args );
                if ( null == daemon.getServer() ) {
                    System.exit ( 1 );
                }
                System.exit ( 0 );
            } else {
                log.warn ( "Bootstrap: command \"" + command + "\" does not exist." );
            }
        } catch ( Throwable t ) {
            if ( t instanceof InvocationTargetException &&
                    t.getCause() != null ) {
                t = t.getCause();
            }
            handleThrowable ( t );
            t.printStackTrace();
            System.exit ( 1 );
        }
    }
    public static String getCatalinaHome() {
        return catalinaHomeFile.getPath();
    }
    public static String getCatalinaBase() {
        return catalinaBaseFile.getPath();
    }
    public static File getCatalinaHomeFile() {
        return catalinaHomeFile;
    }
    public static File getCatalinaBaseFile() {
        return catalinaBaseFile;
    }
    private static void handleThrowable ( Throwable t ) {
        if ( t instanceof ThreadDeath ) {
            throw ( ThreadDeath ) t;
        }
        if ( t instanceof VirtualMachineError ) {
            throw ( VirtualMachineError ) t;
        }
    }
    protected static String[] getPaths ( String value ) {
        List<String> result = new ArrayList<>();
        Matcher matcher = PATH_PATTERN.matcher ( value );
        while ( matcher.find() ) {
            String path = value.substring ( matcher.start(), matcher.end() );
            path = path.trim();
            if ( path.length() == 0 ) {
                continue;
            }
            char first = path.charAt ( 0 );
            char last = path.charAt ( path.length() - 1 );
            if ( first == '"' && last == '"' && path.length() > 1 ) {
                path = path.substring ( 1, path.length() - 1 );
                path = path.trim();
                if ( path.length() == 0 ) {
                    continue;
                }
            } else if ( path.contains ( "\"" ) ) {
                throw new IllegalArgumentException (
                    "The double quote [\"] character only be used to quote paths. It must " +
                    "not appear in a path. This loader path is not valid: [" + value + "]" );
            } else {
            }
            result.add ( path );
        }
        return result.toArray ( new String[result.size()] );
    }
}
