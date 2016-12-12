package org.apache.juli;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Properties;
import java.util.HashMap;
import java.security.Permission;
import java.net.URL;
import java.io.File;
import java.io.FileInputStream;
import java.security.AccessControlException;
import java.io.FilePermission;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.io.InputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Handler;
import java.util.StringTokenizer;
import java.security.AccessController;
import java.util.logging.Level;
import java.security.PrivilegedAction;
import java.util.logging.Logger;
import java.util.WeakHashMap;
import java.util.Map;
import java.util.logging.LogManager;
public class ClassLoaderLogManager extends LogManager {
    public static final String DEBUG_PROPERTY;
    protected final Map<ClassLoader, ClassLoaderLogInfo> classLoaderLoggers;
    protected final ThreadLocal<String> prefix;
    protected volatile boolean useShutdownHook;
    public ClassLoaderLogManager() {
        this.classLoaderLoggers = new WeakHashMap<ClassLoader, ClassLoaderLogInfo>();
        this.prefix = new ThreadLocal<String>();
        this.useShutdownHook = true;
        try {
            Runtime.getRuntime().addShutdownHook ( new Cleaner() );
        } catch ( IllegalStateException ex ) {}
    }
    public boolean isUseShutdownHook() {
        return this.useShutdownHook;
    }
    public void setUseShutdownHook ( final boolean useShutdownHook ) {
        this.useShutdownHook = useShutdownHook;
    }
    @Override
    public synchronized boolean addLogger ( final Logger logger ) {
        final String loggerName = logger.getName();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ClassLoaderLogInfo info = this.getClassLoaderInfo ( classLoader );
        if ( info.loggers.containsKey ( loggerName ) ) {
            return false;
        }
        info.loggers.put ( loggerName, logger );
        final String levelString = this.getProperty ( loggerName + ".level" );
        if ( levelString != null ) {
            try {
                AccessController.doPrivileged ( ( PrivilegedAction<Object> ) new PrivilegedAction<Void>() {
                    @Override
                    public Void run() {
                        logger.setLevel ( Level.parse ( levelString.trim() ) );
                        return null;
                    }
                } );
            } catch ( IllegalArgumentException ex ) {}
        }
        final int dotIndex = loggerName.lastIndexOf ( 46 );
        if ( dotIndex >= 0 ) {
            final String parentName = loggerName.substring ( 0, dotIndex );
            Logger.getLogger ( parentName );
        }
        final LogNode node = info.rootNode.findNode ( loggerName );
        node.logger = logger;
        final Logger parentLogger = node.findParentLogger();
        if ( parentLogger != null ) {
            doSetParentLogger ( logger, parentLogger );
        }
        node.setParentLogger ( logger );
        final String handlers = this.getProperty ( loggerName + ".handlers" );
        if ( handlers != null ) {
            logger.setUseParentHandlers ( false );
            final StringTokenizer tok = new StringTokenizer ( handlers, "," );
            while ( tok.hasMoreTokens() ) {
                final String handlerName = tok.nextToken().trim();
                Handler handler = null;
                for ( ClassLoader current = classLoader; current != null; current = current.getParent() ) {
                    info = this.classLoaderLoggers.get ( current );
                    if ( info != null ) {
                        handler = info.handlers.get ( handlerName );
                        if ( handler != null ) {
                            break;
                        }
                    }
                }
                if ( handler != null ) {
                    logger.addHandler ( handler );
                }
            }
        }
        final String useParentHandlersString = this.getProperty ( loggerName + ".useParentHandlers" );
        if ( Boolean.parseBoolean ( useParentHandlersString ) ) {
            logger.setUseParentHandlers ( true );
        }
        return true;
    }
    @Override
    public synchronized Logger getLogger ( final String name ) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return this.getClassLoaderInfo ( classLoader ).loggers.get ( name );
    }
    @Override
    public synchronized Enumeration<String> getLoggerNames() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return Collections.enumeration ( this.getClassLoaderInfo ( classLoader ).loggers.keySet() );
    }
    @Override
    public String getProperty ( final String name ) {
        final String prefix = this.prefix.get();
        String result = null;
        if ( prefix != null ) {
            result = this.findProperty ( prefix + name );
        }
        if ( result == null ) {
            result = this.findProperty ( name );
        }
        if ( result != null ) {
            result = this.replace ( result );
        }
        return result;
    }
    private synchronized String findProperty ( final String name ) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ClassLoaderLogInfo info = this.getClassLoaderInfo ( classLoader );
        String result = info.props.getProperty ( name );
        if ( result == null && info.props.isEmpty() ) {
            for ( ClassLoader current = classLoader.getParent(); current != null; current = current.getParent() ) {
                info = this.classLoaderLoggers.get ( current );
                if ( info != null ) {
                    result = info.props.getProperty ( name );
                    if ( result != null ) {
                        break;
                    }
                    if ( !info.props.isEmpty() ) {
                        break;
                    }
                }
            }
            if ( result == null ) {
                result = super.getProperty ( name );
            }
        }
        return result;
    }
    @Override
    public void readConfiguration() throws IOException, SecurityException {
        this.checkAccess();
        this.readConfiguration ( Thread.currentThread().getContextClassLoader() );
    }
    @Override
    public void readConfiguration ( final InputStream is ) throws IOException, SecurityException {
        this.checkAccess();
        this.reset();
        this.readConfiguration ( is, Thread.currentThread().getContextClassLoader() );
    }
    @Override
    public void reset() throws SecurityException {
        final Thread thread = Thread.currentThread();
        if ( thread.getClass().getName().startsWith ( "java.util.logging.LogManager$" ) ) {
            return;
        }
        final ClassLoader classLoader = thread.getContextClassLoader();
        final ClassLoaderLogInfo clLogInfo = this.getClassLoaderInfo ( classLoader );
        this.resetLoggers ( clLogInfo );
    }
    public synchronized void shutdown() {
        for ( final ClassLoaderLogInfo clLogInfo : this.classLoaderLoggers.values() ) {
            this.resetLoggers ( clLogInfo );
        }
    }
    private void resetLoggers ( final ClassLoaderLogInfo clLogInfo ) {
        synchronized ( clLogInfo ) {
            for ( final Logger logger : clLogInfo.loggers.values() ) {
                final Handler[] handlers2;
                final Handler[] handlers = handlers2 = logger.getHandlers();
                for ( final Handler handler : handlers2 ) {
                    logger.removeHandler ( handler );
                }
            }
            for ( final Handler handler2 : clLogInfo.handlers.values() ) {
                try {
                    handler2.close();
                } catch ( Exception ex ) {}
            }
            clLogInfo.handlers.clear();
        }
    }
    protected synchronized ClassLoaderLogInfo getClassLoaderInfo ( ClassLoader classLoader ) {
        if ( classLoader == null ) {
            classLoader = ClassLoader.getSystemClassLoader();
        }
        ClassLoaderLogInfo info = this.classLoaderLoggers.get ( classLoader );
        if ( info == null ) {
            final ClassLoader classLoaderParam = classLoader;
            AccessController.doPrivileged ( ( PrivilegedAction<Object> ) new PrivilegedAction<Void>() {
                @Override
                public Void run() {
                    try {
                        ClassLoaderLogManager.this.readConfiguration ( classLoaderParam );
                    } catch ( IOException ex ) {}
                    return null;
                }
            } );
            info = this.classLoaderLoggers.get ( classLoader );
        }
        return info;
    }
    protected synchronized void readConfiguration ( final ClassLoader classLoader ) throws IOException {
        InputStream is = null;
        try {
            if ( classLoader instanceof URLClassLoader ) {
                final URL logConfig = ( ( URLClassLoader ) classLoader ).findResource ( "logging.properties" );
                if ( null != logConfig ) {
                    if ( Boolean.getBoolean ( ClassLoaderLogManager.DEBUG_PROPERTY ) ) {
                        System.err.println ( this.getClass().getName() + ".readConfiguration(): Found logging.properties at " + logConfig );
                    }
                    is = classLoader.getResourceAsStream ( "logging.properties" );
                } else if ( Boolean.getBoolean ( ClassLoaderLogManager.DEBUG_PROPERTY ) ) {
                    System.err.println ( this.getClass().getName() + ".readConfiguration(): Found no logging.properties" );
                }
            }
        } catch ( AccessControlException ace ) {
            final ClassLoaderLogInfo info = this.classLoaderLoggers.get ( ClassLoader.getSystemClassLoader() );
            if ( info != null ) {
                final Logger log = info.loggers.get ( "" );
                if ( log != null ) {
                    final Permission perm = ace.getPermission();
                    if ( perm instanceof FilePermission && perm.getActions().equals ( "read" ) ) {
                        log.warning ( "Reading " + perm.getName() + " is not permitted. See \"per context logging\" in the default catalina.policy file." );
                    } else {
                        log.warning ( "Reading logging.properties is not permitted in some context. See \"per context logging\" in the default catalina.policy file." );
                        log.warning ( "Original error was: " + ace.getMessage() );
                    }
                }
            }
        }
        if ( is == null && classLoader == ClassLoader.getSystemClassLoader() ) {
            final String configFileStr = System.getProperty ( "java.util.logging.config.file" );
            if ( configFileStr != null ) {
                try {
                    is = new FileInputStream ( this.replace ( configFileStr ) );
                } catch ( IOException e ) {
                    System.err.println ( "Configuration error" );
                    e.printStackTrace();
                }
            }
            if ( is == null ) {
                final File defaultFile = new File ( new File ( System.getProperty ( "java.home" ), "lib" ), "logging.properties" );
                try {
                    is = new FileInputStream ( defaultFile );
                } catch ( IOException e2 ) {
                    System.err.println ( "Configuration error" );
                    e2.printStackTrace();
                }
            }
        }
        final Logger localRootLogger = new RootLogger();
        if ( is == null ) {
            ClassLoader current;
            ClassLoaderLogInfo info2;
            for ( current = classLoader.getParent(), info2 = null; current != null && info2 == null; info2 = this.getClassLoaderInfo ( current ), current = current.getParent() ) {}
            if ( info2 != null ) {
                localRootLogger.setParent ( info2.rootNode.logger );
            }
        }
        final ClassLoaderLogInfo info = new ClassLoaderLogInfo ( new LogNode ( null, localRootLogger ) );
        this.classLoaderLoggers.put ( classLoader, info );
        if ( is != null ) {
            this.readConfiguration ( is, classLoader );
        }
        this.addLogger ( localRootLogger );
    }
    protected synchronized void readConfiguration ( final InputStream is, final ClassLoader classLoader ) throws IOException {
        final ClassLoaderLogInfo info = this.classLoaderLoggers.get ( classLoader );
        try {
            info.props.load ( is );
        } catch ( IOException e ) {
            System.err.println ( "Configuration error" );
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch ( IOException ex ) {}
        }
        final String rootHandlers = info.props.getProperty ( ".handlers" );
        final String handlers = info.props.getProperty ( "handlers" );
        final Logger localRootLogger = info.rootNode.logger;
        if ( handlers != null ) {
            final StringTokenizer tok = new StringTokenizer ( handlers, "," );
            while ( tok.hasMoreTokens() ) {
                String handlerClassName;
                final String handlerName = handlerClassName = tok.nextToken().trim();
                String prefix = "";
                if ( handlerClassName.length() <= 0 ) {
                    continue;
                }
                if ( Character.isDigit ( handlerClassName.charAt ( 0 ) ) ) {
                    final int pos = handlerClassName.indexOf ( 46 );
                    if ( pos >= 0 ) {
                        prefix = handlerClassName.substring ( 0, pos + 1 );
                        handlerClassName = handlerClassName.substring ( pos + 1 );
                    }
                }
                try {
                    this.prefix.set ( prefix );
                    final Handler handler = ( Handler ) classLoader.loadClass ( handlerClassName ).newInstance();
                    this.prefix.set ( null );
                    info.handlers.put ( handlerName, handler );
                    if ( rootHandlers != null ) {
                        continue;
                    }
                    localRootLogger.addHandler ( handler );
                } catch ( Exception e2 ) {
                    System.err.println ( "Handler error" );
                    e2.printStackTrace();
                }
            }
        }
    }
    protected static void doSetParentLogger ( final Logger logger, final Logger parent ) {
        AccessController.doPrivileged ( ( PrivilegedAction<Object> ) new PrivilegedAction<Void>() {
            @Override
            public Void run() {
                logger.setParent ( parent );
                return null;
            }
        } );
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
                String replacement = this.replaceWebApplicationProperties ( propName );
                if ( replacement == null ) {
                    replacement = ( ( propName.length() > 0 ) ? System.getProperty ( propName ) : null );
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
    private String replaceWebApplicationProperties ( final String propName ) {
        final ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if ( ! ( cl instanceof WebappProperties ) ) {
            return null;
        }
        final WebappProperties wProps = ( WebappProperties ) cl;
        if ( "classloader.webappName".equals ( propName ) ) {
            return wProps.getWebappName();
        }
        if ( "classloader.hostName".equals ( propName ) ) {
            return wProps.getHostName();
        }
        if ( "classloader.serviceName".equals ( propName ) ) {
            return wProps.getServiceName();
        }
        return null;
    }
    static {
        DEBUG_PROPERTY = ClassLoaderLogManager.class.getName() + ".debug";
    }
    private final class Cleaner extends Thread {
        @Override
        public void run() {
            if ( ClassLoaderLogManager.this.useShutdownHook ) {
                ClassLoaderLogManager.this.shutdown();
            }
        }
    }
    protected static final class LogNode {
        Logger logger;
        final Map<String, LogNode> children;
        final LogNode parent;
        LogNode ( final LogNode parent, final Logger logger ) {
            this.children = new HashMap<String, LogNode>();
            this.parent = parent;
            this.logger = logger;
        }
        LogNode ( final LogNode parent ) {
            this ( parent, null );
        }
        LogNode findNode ( String name ) {
            LogNode currentNode = this;
            if ( this.logger.getName().equals ( name ) ) {
                return this;
            }
            while ( name != null ) {
                final int dotIndex = name.indexOf ( 46 );
                String nextName;
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
            for ( LogNode node = this.parent; node != null && logger == null; logger = node.logger, node = node.parent ) {}
            return logger;
        }
        void setParentLogger ( final Logger parent ) {
            for ( final LogNode childNode : this.children.values() ) {
                if ( childNode.logger == null ) {
                    childNode.setParentLogger ( parent );
                } else {
                    ClassLoaderLogManager.doSetParentLogger ( childNode.logger, parent );
                }
            }
        }
    }
    protected static final class ClassLoaderLogInfo {
        final LogNode rootNode;
        final Map<String, Logger> loggers;
        final Map<String, Handler> handlers;
        final Properties props;
        ClassLoaderLogInfo ( final LogNode rootNode ) {
            this.loggers = new ConcurrentHashMap<String, Logger>();
            this.handlers = new HashMap<String, Handler>();
            this.props = new Properties();
            this.rootNode = rootNode;
        }
    }
    protected static class RootLogger extends Logger {
        public RootLogger() {
            super ( "", null );
        }
    }
}
