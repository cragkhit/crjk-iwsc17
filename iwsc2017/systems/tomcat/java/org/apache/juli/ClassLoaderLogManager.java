package org.apache.juli;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
public class ClassLoaderLogManager extends LogManager {
    public static final String DEBUG_PROPERTY =
        ClassLoaderLogManager.class.getName() + ".debug";
    private final class Cleaner extends Thread {
        @Override
        public void run() {
            if ( useShutdownHook ) {
                shutdown();
            }
        }
    }
    public ClassLoaderLogManager() {
        super();
        try {
            Runtime.getRuntime().addShutdownHook ( new Cleaner() );
        } catch ( IllegalStateException ise ) {
        }
    }
    protected final Map<ClassLoader, ClassLoaderLogInfo> classLoaderLoggers =
        new WeakHashMap<>();
    protected final ThreadLocal<String> prefix = new ThreadLocal<>();
    protected volatile boolean useShutdownHook = true;
    public boolean isUseShutdownHook() {
        return useShutdownHook;
    }
    public void setUseShutdownHook ( boolean useShutdownHook ) {
        this.useShutdownHook = useShutdownHook;
    }
    @Override
    public synchronized boolean addLogger ( final Logger logger ) {
        final String loggerName = logger.getName();
        ClassLoader classLoader =
            Thread.currentThread().getContextClassLoader();
        ClassLoaderLogInfo info = getClassLoaderInfo ( classLoader );
        if ( info.loggers.containsKey ( loggerName ) ) {
            return false;
        }
        info.loggers.put ( loggerName, logger );
        final String levelString = getProperty ( loggerName + ".level" );
        if ( levelString != null ) {
            try {
                AccessController.doPrivileged ( new PrivilegedAction<Void>() {
                    @Override
                    public Void run() {
                        logger.setLevel ( Level.parse ( levelString.trim() ) );
                        return null;
                    }
                } );
            } catch ( IllegalArgumentException e ) {
            }
        }
        int dotIndex = loggerName.lastIndexOf ( '.' );
        if ( dotIndex >= 0 ) {
            final String parentName = loggerName.substring ( 0, dotIndex );
            Logger.getLogger ( parentName );
        }
        LogNode node = info.rootNode.findNode ( loggerName );
        node.logger = logger;
        Logger parentLogger = node.findParentLogger();
        if ( parentLogger != null ) {
            doSetParentLogger ( logger, parentLogger );
        }
        node.setParentLogger ( logger );
        String handlers = getProperty ( loggerName + ".handlers" );
        if ( handlers != null ) {
            logger.setUseParentHandlers ( false );
            StringTokenizer tok = new StringTokenizer ( handlers, "," );
            while ( tok.hasMoreTokens() ) {
                String handlerName = ( tok.nextToken().trim() );
                Handler handler = null;
                ClassLoader current = classLoader;
                while ( current != null ) {
                    info = classLoaderLoggers.get ( current );
                    if ( info != null ) {
                        handler = info.handlers.get ( handlerName );
                        if ( handler != null ) {
                            break;
                        }
                    }
                    current = current.getParent();
                }
                if ( handler != null ) {
                    logger.addHandler ( handler );
                }
            }
        }
        String useParentHandlersString = getProperty ( loggerName + ".useParentHandlers" );
        if ( Boolean.parseBoolean ( useParentHandlersString ) ) {
            logger.setUseParentHandlers ( true );
        }
        return true;
    }
    @Override
    public synchronized Logger getLogger ( final String name ) {
        ClassLoader classLoader = Thread.currentThread()
                                  .getContextClassLoader();
        return getClassLoaderInfo ( classLoader ).loggers.get ( name );
    }
    @Override
    public synchronized Enumeration<String> getLoggerNames() {
        ClassLoader classLoader = Thread.currentThread()
                                  .getContextClassLoader();
        return Collections.enumeration ( getClassLoaderInfo ( classLoader ).loggers.keySet() );
    }
    @Override
    public String getProperty ( String name ) {
        String prefix = this.prefix.get();
        String result = null;
        if ( prefix != null ) {
            result = findProperty ( prefix + name );
        }
        if ( result == null ) {
            result = findProperty ( name );
        }
        if ( result != null ) {
            result = replace ( result );
        }
        return result;
    }
    private synchronized String findProperty ( String name ) {
        ClassLoader classLoader = Thread.currentThread()
                                  .getContextClassLoader();
        ClassLoaderLogInfo info = getClassLoaderInfo ( classLoader );
        String result = info.props.getProperty ( name );
        if ( ( result == null ) && ( info.props.isEmpty() ) ) {
            ClassLoader current = classLoader.getParent();
            while ( current != null ) {
                info = classLoaderLoggers.get ( current );
                if ( info != null ) {
                    result = info.props.getProperty ( name );
                    if ( ( result != null ) || ( !info.props.isEmpty() ) ) {
                        break;
                    }
                }
                current = current.getParent();
            }
            if ( result == null ) {
                result = super.getProperty ( name );
            }
        }
        return result;
    }
    @Override
    public void readConfiguration()
    throws IOException, SecurityException {
        checkAccess();
        readConfiguration ( Thread.currentThread().getContextClassLoader() );
    }
    @Override
    public void readConfiguration ( InputStream is )
    throws IOException, SecurityException {
        checkAccess();
        reset();
        readConfiguration ( is, Thread.currentThread().getContextClassLoader() );
    }
    @Override
    public void reset() throws SecurityException {
        Thread thread = Thread.currentThread();
        if ( thread.getClass().getName().startsWith (
                    "java.util.logging.LogManager$" ) ) {
            return;
        }
        ClassLoader classLoader = thread.getContextClassLoader();
        ClassLoaderLogInfo clLogInfo = getClassLoaderInfo ( classLoader );
        resetLoggers ( clLogInfo );
    }
    public synchronized void shutdown() {
        for ( ClassLoaderLogInfo clLogInfo : classLoaderLoggers.values() ) {
            resetLoggers ( clLogInfo );
        }
    }
    private void resetLoggers ( ClassLoaderLogInfo clLogInfo ) {
        synchronized ( clLogInfo ) {
            for ( Logger logger : clLogInfo.loggers.values() ) {
                Handler[] handlers = logger.getHandlers();
                for ( Handler handler : handlers ) {
                    logger.removeHandler ( handler );
                }
            }
            for ( Handler handler : clLogInfo.handlers.values() ) {
                try {
                    handler.close();
                } catch ( Exception e ) {
                }
            }
            clLogInfo.handlers.clear();
        }
    }
    protected synchronized ClassLoaderLogInfo getClassLoaderInfo ( ClassLoader classLoader ) {
        if ( classLoader == null ) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        ClassLoaderLogInfo info = classLoaderLoggers.get ( classLoader );
        if ( info == null ) {
            final ClassLoader classLoaderParam = classLoader;
            AccessController.doPrivileged ( new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    try {
                        readConfiguration ( classLoaderParam );
                    } catch ( IOException e ) {
                    }
                    return null;
                }
            } );
            info = classLoaderLoggers.get ( classLoader );
        }
        return info;
    }
    protected synchronized void readConfiguration ( ClassLoader classLoader )
    throws IOException {
        InputStream is = null;
        try {
            if ( classLoader instanceof URLClassLoader ) {
                URL logConfig = ( ( URLClassLoader ) classLoader ).findResource ( "logging.properties" );
                if ( null != logConfig ) {
                    if ( Boolean.getBoolean ( DEBUG_PROPERTY ) )
                        System.err.println ( getClass().getName()
                                             + ".readConfiguration(): "
                                             + "Found logging.properties at "
                                             + logConfig );
                    is = classLoader.getResourceAsStream ( "logging.properties" );
                } else {
                    if ( Boolean.getBoolean ( DEBUG_PROPERTY ) )
                        System.err.println ( getClass().getName()
                                             + ".readConfiguration(): "
                                             + "Found no logging.properties" );
                }
            }
        } catch ( AccessControlException ace ) {
            ClassLoaderLogInfo info = classLoaderLoggers.get ( ClassLoader.getSystemClassLoader() );
            if ( info != null ) {
                Logger log = info.loggers.get ( "" );
                if ( log != null ) {
                    Permission perm = ace.getPermission();
                    if ( perm instanceof FilePermission && perm.getActions().equals ( "read" ) ) {
                        log.warning ( "Reading " + perm.getName() + " is not permitted. See \"per context logging\" in the default catalina.policy file." );
                    } else {
                        log.warning ( "Reading logging.properties is not permitted in some context. See \"per context logging\" in the default catalina.policy file." );
                        log.warning ( "Original error was: " + ace.getMessage() );
                    }
                }
            }
        }
        if ( ( is == null ) && ( classLoader == ClassLoader.getSystemClassLoader() ) ) {
            String configFileStr = System.getProperty ( "java.util.logging.config.file" );
            if ( configFileStr != null ) {
                try {
                    is = new FileInputStream ( replace ( configFileStr ) );
                } catch ( IOException e ) {
                    System.err.println ( "Configuration error" );
                    e.printStackTrace();
                }
            }
            if ( is == null ) {
                File defaultFile = new File ( new File ( System.getProperty ( "java.home" ), "lib" ),
                                              "logging.properties" );
                try {
                    is = new FileInputStream ( defaultFile );
                } catch ( IOException e ) {
                    System.err.println ( "Configuration error" );
                    e.printStackTrace();
                }
            }
        }
        Logger localRootLogger = new RootLogger();
        if ( is == null ) {
            ClassLoader current = classLoader.getParent();
            ClassLoaderLogInfo info = null;
            while ( current != null && info == null ) {
                info = getClassLoaderInfo ( current );
                current = current.getParent();
            }
            if ( info != null ) {
                localRootLogger.setParent ( info.rootNode.logger );
            }
        }
        ClassLoaderLogInfo info =
            new ClassLoaderLogInfo ( new LogNode ( null, localRootLogger ) );
        classLoaderLoggers.put ( classLoader, info );
        if ( is != null ) {
            readConfiguration ( is, classLoader );
        }
        addLogger ( localRootLogger );
    }
    protected synchronized void readConfiguration ( InputStream is, ClassLoader classLoader )
    throws IOException {
        ClassLoaderLogInfo info = classLoaderLoggers.get ( classLoader );
        try {
            info.props.load ( is );
        } catch ( IOException e ) {
            System.err.println ( "Configuration error" );
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch ( IOException ioe ) {
            }
        }
        String rootHandlers = info.props.getProperty ( ".handlers" );
        String handlers = info.props.getProperty ( "handlers" );
        Logger localRootLogger = info.rootNode.logger;
        if ( handlers != null ) {
            StringTokenizer tok = new StringTokenizer ( handlers, "," );
            while ( tok.hasMoreTokens() ) {
                String handlerName = ( tok.nextToken().trim() );
                String handlerClassName = handlerName;
                String prefix = "";
                if ( handlerClassName.length() <= 0 ) {
                    continue;
                }
                if ( Character.isDigit ( handlerClassName.charAt ( 0 ) ) ) {
                    int pos = handlerClassName.indexOf ( '.' );
                    if ( pos >= 0 ) {
                        prefix = handlerClassName.substring ( 0, pos + 1 );
                        handlerClassName = handlerClassName.substring ( pos + 1 );
                    }
                }
                try {
                    this.prefix.set ( prefix );
                    Handler handler =
                        ( Handler ) classLoader.loadClass ( handlerClassName ).newInstance();
                    this.prefix.set ( null );
                    info.handlers.put ( handlerName, handler );
                    if ( rootHandlers == null ) {
                        localRootLogger.addHandler ( handler );
                    }
                } catch ( Exception e ) {
                    System.err.println ( "Handler error" );
                    e.printStackTrace();
                }
            }
        }
    }
    protected static void doSetParentLogger ( final Logger logger,
            final Logger parent ) {
        AccessController.doPrivileged ( new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                logger.setParent ( parent );
                return null;
            }
        } );
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
                String replacement = replaceWebApplicationProperties ( propName );
                if ( replacement == null ) {
                    replacement = propName.length() > 0 ? System.getProperty ( propName ) : null;
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
    private String replaceWebApplicationProperties ( String propName ) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if ( cl instanceof WebappProperties ) {
            WebappProperties wProps = ( WebappProperties ) cl;
            if ( "classloader.webappName".equals ( propName ) ) {
                return wProps.getWebappName();
            } else if ( "classloader.hostName".equals ( propName ) ) {
                return wProps.getHostName();
            } else if ( "classloader.serviceName".equals ( propName ) ) {
                return wProps.getServiceName();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
    protected static final class LogNode {
        Logger logger;
        final Map<String, LogNode> children = new HashMap<>();
        final LogNode parent;
        LogNode ( final LogNode parent, final Logger logger ) {
            this.parent = parent;
            this.logger = logger;
        }
        LogNode ( final LogNode parent ) {
            this ( parent, null );
        }
        LogNode findNode ( String name ) {
            LogNode currentNode = this;
            if ( logger.getName().equals ( name ) ) {
                return this;
            }
            while ( name != null ) {
                final int dotIndex = name.indexOf ( '.' );
                final String nextName;
                if ( dotIndex < 0 ) {
                    nextName = name;
                    name = null;
                } else {
                    nextName = name.substring ( 0, dotIndex );
                    name = name.substring ( dotIndex + 1 );
                }
                LogNode childNode = currentNode.children.get ( nextName );
                if ( childNode == null ) {
                    childNode = new LogNode ( currentNode );
                    currentNode.children.put ( nextName, childNode );
                }
                currentNode = childNode;
            }
            return currentNode;
        }
        Logger findParentLogger() {
            Logger logger = null;
            LogNode node = parent;
            while ( node != null && logger == null ) {
                logger = node.logger;
                node = node.parent;
            }
            return logger;
        }
        void setParentLogger ( final Logger parent ) {
            for ( final Iterator<LogNode> iter =
                        children.values().iterator(); iter.hasNext(); ) {
                final LogNode childNode = iter.next();
                if ( childNode.logger == null ) {
                    childNode.setParentLogger ( parent );
                } else {
                    doSetParentLogger ( childNode.logger, parent );
                }
            }
        }
    }
    protected static final class ClassLoaderLogInfo {
        final LogNode rootNode;
        final Map<String, Logger> loggers = new ConcurrentHashMap<>();
        final Map<String, Handler> handlers = new HashMap<>();
        final Properties props = new Properties();
        ClassLoaderLogInfo ( final LogNode rootNode ) {
            this.rootNode = rootNode;
        }
    }
    protected static class RootLogger extends Logger {
        public RootLogger() {
            super ( "", null );
        }
    }
}
