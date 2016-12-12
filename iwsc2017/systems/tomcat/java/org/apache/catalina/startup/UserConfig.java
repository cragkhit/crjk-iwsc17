package org.apache.catalina.startup;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
public final class UserConfig
    implements LifecycleListener {
    private static final Log log = LogFactory.getLog ( UserConfig.class );
    private String configClass = "org.apache.catalina.startup.ContextConfig";
    private String contextClass = "org.apache.catalina.core.StandardContext";
    private String directoryName = "public_html";
    private String homeBase = null;
    private Host host = null;
    private static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    private String userClass =
        "org.apache.catalina.startup.PasswdUserDatabase";
    Pattern allow = null;
    Pattern deny = null;
    public String getConfigClass() {
        return ( this.configClass );
    }
    public void setConfigClass ( String configClass ) {
        this.configClass = configClass;
    }
    public String getContextClass() {
        return ( this.contextClass );
    }
    public void setContextClass ( String contextClass ) {
        this.contextClass = contextClass;
    }
    public String getDirectoryName() {
        return ( this.directoryName );
    }
    public void setDirectoryName ( String directoryName ) {
        this.directoryName = directoryName;
    }
    public String getHomeBase() {
        return ( this.homeBase );
    }
    public void setHomeBase ( String homeBase ) {
        this.homeBase = homeBase;
    }
    public String getUserClass() {
        return ( this.userClass );
    }
    public void setUserClass ( String userClass ) {
        this.userClass = userClass;
    }
    public String getAllow() {
        if ( allow == null ) {
            return null;
        }
        return allow.toString();
    }
    public void setAllow ( String allow ) {
        if ( allow == null || allow.length() == 0 ) {
            this.allow = null;
        } else {
            this.allow = Pattern.compile ( allow );
        }
    }
    public String getDeny() {
        if ( deny == null ) {
            return null;
        }
        return deny.toString();
    }
    public void setDeny ( String deny ) {
        if ( deny == null || deny.length() == 0 ) {
            this.deny = null;
        } else {
            this.deny = Pattern.compile ( deny );
        }
    }
    @Override
    public void lifecycleEvent ( LifecycleEvent event ) {
        try {
            host = ( Host ) event.getLifecycle();
        } catch ( ClassCastException e ) {
            log.error ( sm.getString ( "hostConfig.cce", event.getLifecycle() ), e );
            return;
        }
        if ( event.getType().equals ( Lifecycle.START_EVENT ) ) {
            start();
        } else if ( event.getType().equals ( Lifecycle.STOP_EVENT ) ) {
            stop();
        }
    }
    private void deploy() {
        if ( host.getLogger().isDebugEnabled() ) {
            host.getLogger().debug ( sm.getString ( "userConfig.deploying" ) );
        }
        UserDatabase database = null;
        try {
            Class<?> clazz = Class.forName ( userClass );
            database = ( UserDatabase ) clazz.newInstance();
            database.setUserConfig ( this );
        } catch ( Exception e ) {
            host.getLogger().error ( sm.getString ( "userConfig.database" ), e );
            return;
        }
        ExecutorService executor = host.getStartStopExecutor();
        List<Future<?>> results = new ArrayList<>();
        Enumeration<String> users = database.getUsers();
        while ( users.hasMoreElements() ) {
            String user = users.nextElement();
            if ( !isDeployAllowed ( user ) ) {
                continue;
            }
            String home = database.getHome ( user );
            results.add ( executor.submit ( new DeployUserDirectory ( this, user, home ) ) );
        }
        for ( Future<?> result : results ) {
            try {
                result.get();
            } catch ( Exception e ) {
                host.getLogger().error ( sm.getString ( "userConfig.deploy.threaded.error" ), e );
            }
        }
    }
    private void deploy ( String user, String home ) {
        String contextPath = "/~" + user;
        if ( host.findChild ( contextPath ) != null ) {
            return;
        }
        File app = new File ( home, directoryName );
        if ( !app.exists() || !app.isDirectory() ) {
            return;
        }
        host.getLogger().info ( sm.getString ( "userConfig.deploy", user ) );
        try {
            Class<?> clazz = Class.forName ( contextClass );
            Context context =
                ( Context ) clazz.newInstance();
            context.setPath ( contextPath );
            context.setDocBase ( app.toString() );
            clazz = Class.forName ( configClass );
            LifecycleListener listener =
                ( LifecycleListener ) clazz.newInstance();
            context.addLifecycleListener ( listener );
            host.addChild ( context );
        } catch ( Exception e ) {
            host.getLogger().error ( sm.getString ( "userConfig.error", user ), e );
        }
    }
    private void start() {
        if ( host.getLogger().isDebugEnabled() ) {
            host.getLogger().debug ( sm.getString ( "userConfig.start" ) );
        }
        deploy();
    }
    private void stop() {
        if ( host.getLogger().isDebugEnabled() ) {
            host.getLogger().debug ( sm.getString ( "userConfig.stop" ) );
        }
    }
    private boolean isDeployAllowed ( String user ) {
        if ( deny != null && deny.matcher ( user ).matches() ) {
            return false;
        }
        if ( allow != null ) {
            if ( allow.matcher ( user ).matches() ) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }
    private static class DeployUserDirectory implements Runnable {
        private UserConfig config;
        private String user;
        private String home;
        public DeployUserDirectory ( UserConfig config, String user, String home ) {
            this.config = config;
            this.user = user;
            this.home = home;
        }
        @Override
        public void run() {
            config.deploy ( user, home );
        }
    }
}
