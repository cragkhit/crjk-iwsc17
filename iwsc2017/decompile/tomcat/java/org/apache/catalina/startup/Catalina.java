package org.apache.catalina.startup;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.security.SecurityConfig;
import java.io.PrintStream;
import org.apache.tomcat.util.log.SystemLogHandler;
import org.apache.catalina.LifecycleState;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.juli.ClassLoaderLogManager;
import java.util.logging.LogManager;
import org.xml.sax.SAXParseException;
import java.io.OutputStream;
import org.apache.catalina.LifecycleException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.io.InputStream;
import org.xml.sax.InputSource;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import org.apache.tomcat.util.digester.RuleSet;
import org.apache.tomcat.util.digester.Rule;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import org.apache.tomcat.util.digester.Digester;
import java.io.File;
import org.apache.juli.logging.Log;
import org.apache.catalina.Server;
import org.apache.tomcat.util.res.StringManager;
public class Catalina {
    protected static final StringManager sm;
    protected boolean await;
    protected String configFile;
    protected ClassLoader parentClassLoader;
    protected Server server;
    protected boolean useShutdownHook;
    protected Thread shutdownHook;
    protected boolean useNaming;
    private static final Log log;
    public Catalina() {
        this.await = false;
        this.configFile = "conf/server.xml";
        this.parentClassLoader = Catalina.class.getClassLoader();
        this.server = null;
        this.useShutdownHook = true;
        this.shutdownHook = null;
        this.useNaming = true;
        this.setSecurityProtection();
    }
    public void setConfigFile ( final String file ) {
        this.configFile = file;
    }
    public String getConfigFile() {
        return this.configFile;
    }
    public void setUseShutdownHook ( final boolean useShutdownHook ) {
        this.useShutdownHook = useShutdownHook;
    }
    public boolean getUseShutdownHook() {
        return this.useShutdownHook;
    }
    public void setParentClassLoader ( final ClassLoader parentClassLoader ) {
        this.parentClassLoader = parentClassLoader;
    }
    public ClassLoader getParentClassLoader() {
        if ( this.parentClassLoader != null ) {
            return this.parentClassLoader;
        }
        return ClassLoader.getSystemClassLoader();
    }
    public void setServer ( final Server server ) {
        this.server = server;
    }
    public Server getServer() {
        return this.server;
    }
    public boolean isUseNaming() {
        return this.useNaming;
    }
    public void setUseNaming ( final boolean useNaming ) {
        this.useNaming = useNaming;
    }
    public void setAwait ( final boolean b ) {
        this.await = b;
    }
    public boolean isAwait() {
        return this.await;
    }
    protected boolean arguments ( final String[] args ) {
        boolean isConfig = false;
        if ( args.length < 1 ) {
            this.usage();
            return false;
        }
        for ( int i = 0; i < args.length; ++i ) {
            if ( isConfig ) {
                this.configFile = args[i];
                isConfig = false;
            } else if ( args[i].equals ( "-config" ) ) {
                isConfig = true;
            } else if ( args[i].equals ( "-nonaming" ) ) {
                this.setUseNaming ( false );
            } else {
                if ( args[i].equals ( "-help" ) ) {
                    this.usage();
                    return false;
                }
                if ( !args[i].equals ( "start" ) ) {
                    if ( !args[i].equals ( "configtest" ) ) {
                        if ( !args[i].equals ( "stop" ) ) {
                            this.usage();
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    protected File configFile() {
        File file = new File ( this.configFile );
        if ( !file.isAbsolute() ) {
            file = new File ( Bootstrap.getCatalinaBase(), this.configFile );
        }
        return file;
    }
    protected Digester createStartDigester() {
        final long t1 = System.currentTimeMillis();
        final Digester digester = new Digester();
        digester.setValidating ( false );
        digester.setRulesValidation ( true );
        final HashMap<Class<?>, List<String>> fakeAttributes = new HashMap<Class<?>, List<String>>();
        final ArrayList<String> attrs = new ArrayList<String>();
        attrs.add ( "className" );
        fakeAttributes.put ( Object.class, attrs );
        digester.setFakeAttributes ( fakeAttributes );
        digester.setUseContextClassLoader ( true );
        digester.addObjectCreate ( "Server", "org.apache.catalina.core.StandardServer", "className" );
        digester.addSetProperties ( "Server" );
        digester.addSetNext ( "Server", "setServer", "org.apache.catalina.Server" );
        digester.addObjectCreate ( "Server/GlobalNamingResources", "org.apache.catalina.deploy.NamingResourcesImpl" );
        digester.addSetProperties ( "Server/GlobalNamingResources" );
        digester.addSetNext ( "Server/GlobalNamingResources", "setGlobalNamingResources", "org.apache.catalina.deploy.NamingResourcesImpl" );
        digester.addObjectCreate ( "Server/Listener", null, "className" );
        digester.addSetProperties ( "Server/Listener" );
        digester.addSetNext ( "Server/Listener", "addLifecycleListener", "org.apache.catalina.LifecycleListener" );
        digester.addObjectCreate ( "Server/Service", "org.apache.catalina.core.StandardService", "className" );
        digester.addSetProperties ( "Server/Service" );
        digester.addSetNext ( "Server/Service", "addService", "org.apache.catalina.Service" );
        digester.addObjectCreate ( "Server/Service/Listener", null, "className" );
        digester.addSetProperties ( "Server/Service/Listener" );
        digester.addSetNext ( "Server/Service/Listener", "addLifecycleListener", "org.apache.catalina.LifecycleListener" );
        digester.addObjectCreate ( "Server/Service/Executor", "org.apache.catalina.core.StandardThreadExecutor", "className" );
        digester.addSetProperties ( "Server/Service/Executor" );
        digester.addSetNext ( "Server/Service/Executor", "addExecutor", "org.apache.catalina.Executor" );
        digester.addRule ( "Server/Service/Connector", new ConnectorCreateRule() );
        digester.addRule ( "Server/Service/Connector", new SetAllPropertiesRule ( new String[] { "executor", "sslImplementationName", "protocol" } ) );
        digester.addSetNext ( "Server/Service/Connector", "addConnector", "org.apache.catalina.connector.Connector" );
        digester.addObjectCreate ( "Server/Service/Connector/SSLHostConfig", "org.apache.tomcat.util.net.SSLHostConfig" );
        digester.addSetProperties ( "Server/Service/Connector/SSLHostConfig" );
        digester.addSetNext ( "Server/Service/Connector/SSLHostConfig", "addSslHostConfig", "org.apache.tomcat.util.net.SSLHostConfig" );
        digester.addRule ( "Server/Service/Connector/SSLHostConfig/Certificate", new CertificateCreateRule() );
        digester.addRule ( "Server/Service/Connector/SSLHostConfig/Certificate", new SetAllPropertiesRule ( new String[] { "type" } ) );
        digester.addSetNext ( "Server/Service/Connector/SSLHostConfig/Certificate", "addCertificate", "org.apache.tomcat.util.net.SSLHostConfigCertificate" );
        digester.addObjectCreate ( "Server/Service/Connector/Listener", null, "className" );
        digester.addSetProperties ( "Server/Service/Connector/Listener" );
        digester.addSetNext ( "Server/Service/Connector/Listener", "addLifecycleListener", "org.apache.catalina.LifecycleListener" );
        digester.addObjectCreate ( "Server/Service/Connector/UpgradeProtocol", null, "className" );
        digester.addSetProperties ( "Server/Service/Connector/UpgradeProtocol" );
        digester.addSetNext ( "Server/Service/Connector/UpgradeProtocol", "addUpgradeProtocol", "org.apache.coyote.UpgradeProtocol" );
        digester.addRuleSet ( new NamingRuleSet ( "Server/GlobalNamingResources/" ) );
        digester.addRuleSet ( new EngineRuleSet ( "Server/Service/" ) );
        digester.addRuleSet ( new HostRuleSet ( "Server/Service/Engine/" ) );
        digester.addRuleSet ( new ContextRuleSet ( "Server/Service/Engine/Host/" ) );
        this.addClusterRuleSet ( digester, "Server/Service/Engine/Host/Cluster/" );
        digester.addRuleSet ( new NamingRuleSet ( "Server/Service/Engine/Host/Context/" ) );
        digester.addRule ( "Server/Service/Engine", new SetParentClassLoaderRule ( this.parentClassLoader ) );
        this.addClusterRuleSet ( digester, "Server/Service/Engine/Cluster/" );
        final long t2 = System.currentTimeMillis();
        if ( Catalina.log.isDebugEnabled() ) {
            Catalina.log.debug ( "Digester for server.xml created " + ( t2 - t1 ) );
        }
        return digester;
    }
    private void addClusterRuleSet ( final Digester digester, final String prefix ) {
        Class<?> clazz = null;
        Constructor<?> constructor = null;
        try {
            clazz = Class.forName ( "org.apache.catalina.ha.ClusterRuleSet" );
            constructor = clazz.getConstructor ( String.class );
            final RuleSet ruleSet = ( RuleSet ) constructor.newInstance ( prefix );
            digester.addRuleSet ( ruleSet );
        } catch ( Exception e ) {
            if ( Catalina.log.isDebugEnabled() ) {
                Catalina.log.debug ( Catalina.sm.getString ( "catalina.noCluster", e.getClass().getName() + ": " + e.getMessage() ), e );
            } else if ( Catalina.log.isInfoEnabled() ) {
                Catalina.log.info ( Catalina.sm.getString ( "catalina.noCluster", e.getClass().getName() + ": " + e.getMessage() ) );
            }
        }
    }
    protected Digester createStopDigester() {
        final Digester digester = new Digester();
        digester.setUseContextClassLoader ( true );
        digester.addObjectCreate ( "Server", "org.apache.catalina.core.StandardServer", "className" );
        digester.addSetProperties ( "Server" );
        digester.addSetNext ( "Server", "setServer", "org.apache.catalina.Server" );
        return digester;
    }
    public void stopServer() {
        this.stopServer ( null );
    }
    public void stopServer ( final String[] arguments ) {
        if ( arguments != null ) {
            this.arguments ( arguments );
        }
        Server s = this.getServer();
        if ( s == null ) {
            final Digester digester = this.createStopDigester();
            final File file = this.configFile();
            try ( final FileInputStream fis = new FileInputStream ( file ) ) {
                final InputSource is = new InputSource ( file.toURI().toURL().toString() );
                is.setByteStream ( fis );
                digester.push ( this );
                digester.parse ( is );
            } catch ( Exception e ) {
                Catalina.log.error ( "Catalina.stop: ", e );
                System.exit ( 1 );
            }
            s = this.getServer();
            if ( s.getPort() > 0 ) {
                try ( final Socket socket = new Socket ( s.getAddress(), s.getPort() );
                            final OutputStream stream = socket.getOutputStream() ) {
                    final String shutdown = s.getShutdown();
                    for ( int i = 0; i < shutdown.length(); ++i ) {
                        stream.write ( shutdown.charAt ( i ) );
                    }
                    stream.flush();
                } catch ( ConnectException ce ) {
                    Catalina.log.error ( Catalina.sm.getString ( "catalina.stopServer.connectException", s.getAddress(), String.valueOf ( s.getPort() ) ) );
                    Catalina.log.error ( "Catalina.stop: ", ce );
                    System.exit ( 1 );
                } catch ( IOException e2 ) {
                    Catalina.log.error ( "Catalina.stop: ", e2 );
                    System.exit ( 1 );
                }
            } else {
                Catalina.log.error ( Catalina.sm.getString ( "catalina.stopServer" ) );
                System.exit ( 1 );
            }
            return;
        }
        try {
            s.stop();
        } catch ( LifecycleException e3 ) {
            Catalina.log.error ( "Catalina.stop: ", e3 );
        }
    }
    public void load() {
        final long t1 = System.nanoTime();
        this.initDirs();
        this.initNaming();
        final Digester digester = this.createStartDigester();
        InputSource inputSource = null;
        InputStream inputStream = null;
        File file = null;
        try {
            try {
                file = this.configFile();
                inputStream = new FileInputStream ( file );
                inputSource = new InputSource ( file.toURI().toURL().toString() );
            } catch ( Exception e ) {
                if ( Catalina.log.isDebugEnabled() ) {
                    Catalina.log.debug ( Catalina.sm.getString ( "catalina.configFail", file ), e );
                }
            }
            if ( inputStream == null ) {
                try {
                    inputStream = this.getClass().getClassLoader().getResourceAsStream ( this.getConfigFile() );
                    inputSource = new InputSource ( this.getClass().getClassLoader().getResource ( this.getConfigFile() ).toString() );
                } catch ( Exception e ) {
                    if ( Catalina.log.isDebugEnabled() ) {
                        Catalina.log.debug ( Catalina.sm.getString ( "catalina.configFail", this.getConfigFile() ), e );
                    }
                }
            }
            if ( inputStream == null ) {
                try {
                    inputStream = this.getClass().getClassLoader().getResourceAsStream ( "server-embed.xml" );
                    inputSource = new InputSource ( this.getClass().getClassLoader().getResource ( "server-embed.xml" ).toString() );
                } catch ( Exception e ) {
                    if ( Catalina.log.isDebugEnabled() ) {
                        Catalina.log.debug ( Catalina.sm.getString ( "catalina.configFail", "server-embed.xml" ), e );
                    }
                }
            }
            if ( inputStream == null || inputSource == null ) {
                if ( file == null ) {
                    Catalina.log.warn ( Catalina.sm.getString ( "catalina.configFail", this.getConfigFile() + "] or [server-embed.xml]" ) );
                } else {
                    Catalina.log.warn ( Catalina.sm.getString ( "catalina.configFail", file.getAbsolutePath() ) );
                    if ( file.exists() && !file.canRead() ) {
                        Catalina.log.warn ( "Permissions incorrect, read permission is not allowed on the file." );
                    }
                }
                return;
            }
            try {
                inputSource.setByteStream ( inputStream );
                digester.push ( this );
                digester.parse ( inputSource );
            } catch ( SAXParseException spe ) {
                Catalina.log.warn ( "Catalina.start using " + this.getConfigFile() + ": " + spe.getMessage() );
                return;
            } catch ( Exception e ) {
                Catalina.log.warn ( "Catalina.start using " + this.getConfigFile() + ": ", e );
                return;
            }
        } finally {
            if ( inputStream != null ) {
                try {
                    inputStream.close();
                } catch ( IOException ex ) {}
            }
        }
        this.getServer().setCatalina ( this );
        this.getServer().setCatalinaHome ( Bootstrap.getCatalinaHomeFile() );
        this.getServer().setCatalinaBase ( Bootstrap.getCatalinaBaseFile() );
        this.initStreams();
        try {
            this.getServer().init();
        } catch ( LifecycleException e2 ) {
            if ( Boolean.getBoolean ( "org.apache.catalina.startup.EXIT_ON_INIT_FAILURE" ) ) {
                throw new Error ( e2 );
            }
            Catalina.log.error ( "Catalina.start", e2 );
        }
        final long t2 = System.nanoTime();
        if ( Catalina.log.isInfoEnabled() ) {
            Catalina.log.info ( "Initialization processed in " + ( t2 - t1 ) / 1000000L + " ms" );
        }
    }
    public void load ( final String[] args ) {
        try {
            if ( this.arguments ( args ) ) {
                this.load();
            }
        } catch ( Exception e ) {
            e.printStackTrace ( System.out );
        }
    }
    public void start() {
        if ( this.getServer() == null ) {
            this.load();
        }
        if ( this.getServer() == null ) {
            Catalina.log.fatal ( "Cannot start server. Server instance is not configured." );
            return;
        }
        final long t1 = System.nanoTime();
        try {
            this.getServer().start();
        } catch ( LifecycleException e ) {
            Catalina.log.fatal ( Catalina.sm.getString ( "catalina.serverStartFail" ), e );
            try {
                this.getServer().destroy();
            } catch ( LifecycleException e2 ) {
                Catalina.log.debug ( "destroy() failed for failed Server ", e2 );
            }
            return;
        }
        final long t2 = System.nanoTime();
        if ( Catalina.log.isInfoEnabled() ) {
            Catalina.log.info ( "Server startup in " + ( t2 - t1 ) / 1000000L + " ms" );
        }
        if ( this.useShutdownHook ) {
            if ( this.shutdownHook == null ) {
                this.shutdownHook = new CatalinaShutdownHook();
            }
            Runtime.getRuntime().addShutdownHook ( this.shutdownHook );
            final LogManager logManager = LogManager.getLogManager();
            if ( logManager instanceof ClassLoaderLogManager ) {
                ( ( ClassLoaderLogManager ) logManager ).setUseShutdownHook ( false );
            }
        }
        if ( this.await ) {
            this.await();
            this.stop();
        }
    }
    public void stop() {
        try {
            if ( this.useShutdownHook ) {
                Runtime.getRuntime().removeShutdownHook ( this.shutdownHook );
                final LogManager logManager = LogManager.getLogManager();
                if ( logManager instanceof ClassLoaderLogManager ) {
                    ( ( ClassLoaderLogManager ) logManager ).setUseShutdownHook ( true );
                }
            }
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
        }
        try {
            final Server s = this.getServer();
            final LifecycleState state = s.getState();
            if ( LifecycleState.STOPPING_PREP.compareTo ( state ) > 0 || LifecycleState.DESTROYED.compareTo ( state ) < 0 ) {
                s.stop();
                s.destroy();
            }
        } catch ( LifecycleException e ) {
            Catalina.log.error ( "Catalina.stop", e );
        }
    }
    public void await() {
        this.getServer().await();
    }
    protected void usage() {
        System.out.println ( "usage: java org.apache.catalina.startup.Catalina [ -config {pathname} ] [ -nonaming ]  { -help | start | stop }" );
    }
    protected void initDirs() {
        final String temp = System.getProperty ( "java.io.tmpdir" );
        if ( temp == null || !new File ( temp ).isDirectory() ) {
            Catalina.log.error ( Catalina.sm.getString ( "embedded.notmp", temp ) );
        }
    }
    protected void initStreams() {
        System.setOut ( new SystemLogHandler ( System.out ) );
        System.setErr ( new SystemLogHandler ( System.err ) );
    }
    protected void initNaming() {
        if ( !this.useNaming ) {
            Catalina.log.info ( "Catalina naming disabled" );
            System.setProperty ( "catalina.useNaming", "false" );
        } else {
            System.setProperty ( "catalina.useNaming", "true" );
            String value = "org.apache.naming";
            final String oldValue = System.getProperty ( "java.naming.factory.url.pkgs" );
            if ( oldValue != null ) {
                value = value + ":" + oldValue;
            }
            System.setProperty ( "java.naming.factory.url.pkgs", value );
            if ( Catalina.log.isDebugEnabled() ) {
                Catalina.log.debug ( "Setting naming prefix=" + value );
            }
            value = System.getProperty ( "java.naming.factory.initial" );
            if ( value == null ) {
                System.setProperty ( "java.naming.factory.initial", "org.apache.naming.java.javaURLContextFactory" );
            } else {
                Catalina.log.debug ( "INITIAL_CONTEXT_FACTORY already set " + value );
            }
        }
    }
    protected void setSecurityProtection() {
        final SecurityConfig securityConfig = SecurityConfig.newInstance();
        securityConfig.setPackageDefinition();
        securityConfig.setPackageAccess();
    }
    static {
        sm = StringManager.getManager ( "org.apache.catalina.startup" );
        log = LogFactory.getLog ( Catalina.class );
    }
    protected class CatalinaShutdownHook extends Thread {
        @Override
        public void run() {
            try {
                if ( Catalina.this.getServer() != null ) {
                    Catalina.this.stop();
                }
            } catch ( Throwable ex ) {
                ExceptionUtils.handleThrowable ( ex );
                Catalina.log.error ( Catalina.sm.getString ( "catalina.shutdownHookFail" ), ex );
            } finally {
                final LogManager logManager = LogManager.getLogManager();
                if ( logManager instanceof ClassLoaderLogManager ) {
                    ( ( ClassLoaderLogManager ) logManager ).shutdown();
                }
            }
        }
    }
}
