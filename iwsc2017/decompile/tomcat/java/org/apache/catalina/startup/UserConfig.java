package org.apache.catalina.startup;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import java.io.File;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.ArrayList;
import org.apache.catalina.LifecycleEvent;
import java.util.regex.Pattern;
import org.apache.tomcat.util.res.StringManager;
import org.apache.catalina.Host;
import org.apache.juli.logging.Log;
import org.apache.catalina.LifecycleListener;
public final class UserConfig implements LifecycleListener {
    private static final Log log;
    private String configClass;
    private String contextClass;
    private String directoryName;
    private String homeBase;
    private Host host;
    private static final StringManager sm;
    private String userClass;
    Pattern allow;
    Pattern deny;
    public UserConfig() {
        this.configClass = "org.apache.catalina.startup.ContextConfig";
        this.contextClass = "org.apache.catalina.core.StandardContext";
        this.directoryName = "public_html";
        this.homeBase = null;
        this.host = null;
        this.userClass = "org.apache.catalina.startup.PasswdUserDatabase";
        this.allow = null;
        this.deny = null;
    }
    public String getConfigClass() {
        return this.configClass;
    }
    public void setConfigClass ( final String configClass ) {
        this.configClass = configClass;
    }
    public String getContextClass() {
        return this.contextClass;
    }
    public void setContextClass ( final String contextClass ) {
        this.contextClass = contextClass;
    }
    public String getDirectoryName() {
        return this.directoryName;
    }
    public void setDirectoryName ( final String directoryName ) {
        this.directoryName = directoryName;
    }
    public String getHomeBase() {
        return this.homeBase;
    }
    public void setHomeBase ( final String homeBase ) {
        this.homeBase = homeBase;
    }
    public String getUserClass() {
        return this.userClass;
    }
    public void setUserClass ( final String userClass ) {
        this.userClass = userClass;
    }
    public String getAllow() {
        if ( this.allow == null ) {
            return null;
        }
        return this.allow.toString();
    }
    public void setAllow ( final String allow ) {
        if ( allow == null || allow.length() == 0 ) {
            this.allow = null;
        } else {
            this.allow = Pattern.compile ( allow );
        }
    }
    public String getDeny() {
        if ( this.deny == null ) {
            return null;
        }
        return this.deny.toString();
    }
    public void setDeny ( final String deny ) {
        if ( deny == null || deny.length() == 0 ) {
            this.deny = null;
        } else {
            this.deny = Pattern.compile ( deny );
        }
    }
    @Override
    public void lifecycleEvent ( final LifecycleEvent event ) {
        try {
            this.host = ( Host ) event.getLifecycle();
        } catch ( ClassCastException e ) {
            UserConfig.log.error ( UserConfig.sm.getString ( "hostConfig.cce", event.getLifecycle() ), e );
            return;
        }
        if ( event.getType().equals ( "start" ) ) {
            this.start();
        } else if ( event.getType().equals ( "stop" ) ) {
            this.stop();
        }
    }
    private void deploy() {
        if ( this.host.getLogger().isDebugEnabled() ) {
            this.host.getLogger().debug ( UserConfig.sm.getString ( "userConfig.deploying" ) );
        }
        UserDatabase database = null;
        try {
            final Class<?> clazz = Class.forName ( this.userClass );
            database = ( UserDatabase ) clazz.newInstance();
            database.setUserConfig ( this );
        } catch ( Exception e ) {
            this.host.getLogger().error ( UserConfig.sm.getString ( "userConfig.database" ), e );
            return;
        }
        final ExecutorService executor = this.host.getStartStopExecutor();
        final List<Future<?>> results = new ArrayList<Future<?>>();
        final Enumeration<String> users = database.getUsers();
        while ( users.hasMoreElements() ) {
            final String user = users.nextElement();
            if ( !this.isDeployAllowed ( user ) ) {
                continue;
            }
            final String home = database.getHome ( user );
            results.add ( executor.submit ( new DeployUserDirectory ( this, user, home ) ) );
        }
        for ( final Future<?> result : results ) {
            try {
                result.get();
            } catch ( Exception e2 ) {
                this.host.getLogger().error ( UserConfig.sm.getString ( "userConfig.deploy.threaded.error" ), e2 );
            }
        }
    }
    private void deploy ( final String user, final String home ) {
        final String contextPath = "/~" + user;
        if ( this.host.findChild ( contextPath ) != null ) {
            return;
        }
        final File app = new File ( home, this.directoryName );
        if ( !app.exists() || !app.isDirectory() ) {
            return;
        }
        this.host.getLogger().info ( UserConfig.sm.getString ( "userConfig.deploy", user ) );
        try {
            Class<?> clazz = Class.forName ( this.contextClass );
            final Context context = ( Context ) clazz.newInstance();
            context.setPath ( contextPath );
            context.setDocBase ( app.toString() );
            clazz = Class.forName ( this.configClass );
            final LifecycleListener listener = ( LifecycleListener ) clazz.newInstance();
            context.addLifecycleListener ( listener );
            this.host.addChild ( context );
        } catch ( Exception e ) {
            this.host.getLogger().error ( UserConfig.sm.getString ( "userConfig.error", user ), e );
        }
    }
    private void start() {
        if ( this.host.getLogger().isDebugEnabled() ) {
            this.host.getLogger().debug ( UserConfig.sm.getString ( "userConfig.start" ) );
        }
        this.deploy();
    }
    private void stop() {
        if ( this.host.getLogger().isDebugEnabled() ) {
            this.host.getLogger().debug ( UserConfig.sm.getString ( "userConfig.stop" ) );
        }
    }
    private boolean isDeployAllowed ( final String user ) {
        return ( this.deny == null || !this.deny.matcher ( user ).matches() ) && ( this.allow == null || this.allow.matcher ( user ).matches() );
    }
    static {
        log = LogFactory.getLog ( UserConfig.class );
        sm = StringManager.getManager ( "org.apache.catalina.startup" );
    }
    private static class DeployUserDirectory implements Runnable {
        private UserConfig config;
        private String user;
        private String home;
        public DeployUserDirectory ( final UserConfig config, final String user, final String home ) {
            this.config = config;
            this.user = user;
            this.home = home;
        }
        @Override
        public void run() {
            this.config.deploy ( this.user, this.home );
        }
    }
}
