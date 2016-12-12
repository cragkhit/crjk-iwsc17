package org.apache.catalina.startup;
import org.apache.juli.logging.LogFactory;
import java.util.SortedMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.lang.management.ManagementFactory;
import org.apache.catalina.util.ServerInfo;
import org.apache.catalina.LifecycleEvent;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.LifecycleListener;
public class VersionLoggerListener implements LifecycleListener {
    private static final Log log;
    protected static final StringManager sm;
    private boolean logArgs;
    private boolean logEnv;
    private boolean logProps;
    public VersionLoggerListener() {
        this.logArgs = true;
        this.logEnv = false;
        this.logProps = false;
    }
    public boolean getLogArgs() {
        return this.logArgs;
    }
    public void setLogArgs ( final boolean logArgs ) {
        this.logArgs = logArgs;
    }
    public boolean getLogEnv() {
        return this.logEnv;
    }
    public void setLogEnv ( final boolean logEnv ) {
        this.logEnv = logEnv;
    }
    public boolean getLogProps() {
        return this.logProps;
    }
    public void setLogProps ( final boolean logProps ) {
        this.logProps = logProps;
    }
    @Override
    public void lifecycleEvent ( final LifecycleEvent event ) {
        if ( "before_init".equals ( event.getType() ) ) {
            this.log();
        }
    }
    private void log() {
        VersionLoggerListener.log.info ( VersionLoggerListener.sm.getString ( "versionLoggerListener.serverInfo.server.version", ServerInfo.getServerInfo() ) );
        VersionLoggerListener.log.info ( VersionLoggerListener.sm.getString ( "versionLoggerListener.serverInfo.server.built", ServerInfo.getServerBuilt() ) );
        VersionLoggerListener.log.info ( VersionLoggerListener.sm.getString ( "versionLoggerListener.serverInfo.server.number", ServerInfo.getServerNumber() ) );
        VersionLoggerListener.log.info ( VersionLoggerListener.sm.getString ( "versionLoggerListener.os.name", System.getProperty ( "os.name" ) ) );
        VersionLoggerListener.log.info ( VersionLoggerListener.sm.getString ( "versionLoggerListener.os.version", System.getProperty ( "os.version" ) ) );
        VersionLoggerListener.log.info ( VersionLoggerListener.sm.getString ( "versionLoggerListener.os.arch", System.getProperty ( "os.arch" ) ) );
        VersionLoggerListener.log.info ( VersionLoggerListener.sm.getString ( "versionLoggerListener.java.home", System.getProperty ( "java.home" ) ) );
        VersionLoggerListener.log.info ( VersionLoggerListener.sm.getString ( "versionLoggerListener.vm.version", System.getProperty ( "java.runtime.version" ) ) );
        VersionLoggerListener.log.info ( VersionLoggerListener.sm.getString ( "versionLoggerListener.vm.vendor", System.getProperty ( "java.vm.vendor" ) ) );
        VersionLoggerListener.log.info ( VersionLoggerListener.sm.getString ( "versionLoggerListener.catalina.base", System.getProperty ( "catalina.base" ) ) );
        VersionLoggerListener.log.info ( VersionLoggerListener.sm.getString ( "versionLoggerListener.catalina.home", System.getProperty ( "catalina.home" ) ) );
        if ( this.logArgs ) {
            final List<String> args = ManagementFactory.getRuntimeMXBean().getInputArguments();
            for ( final String arg : args ) {
                VersionLoggerListener.log.info ( VersionLoggerListener.sm.getString ( "versionLoggerListener.arg", arg ) );
            }
        }
        if ( this.logEnv ) {
            final SortedMap<String, String> sortedMap = new TreeMap<String, String> ( System.getenv() );
            for ( final Map.Entry<String, String> e : sortedMap.entrySet() ) {
                VersionLoggerListener.log.info ( VersionLoggerListener.sm.getString ( "versionLoggerListener.env", e.getKey(), e.getValue() ) );
            }
        }
        if ( this.logProps ) {
            final SortedMap<String, String> sortedMap = new TreeMap<String, String>();
            for ( final Map.Entry<Object, Object> e2 : System.getProperties().entrySet() ) {
                sortedMap.put ( String.valueOf ( e2.getKey() ), String.valueOf ( e2.getValue() ) );
            }
            for ( final Map.Entry<String, String> e : sortedMap.entrySet() ) {
                VersionLoggerListener.log.info ( VersionLoggerListener.sm.getString ( "versionLoggerListener.prop", e.getKey(), e.getValue() ) );
            }
        }
    }
    static {
        log = LogFactory.getLog ( VersionLoggerListener.class );
        sm = StringManager.getManager ( "org.apache.catalina.startup" );
    }
}
