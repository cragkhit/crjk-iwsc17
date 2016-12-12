package org.apache.catalina.storeconfig;
import java.io.PrintWriter;
import java.net.URL;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.mbeans.MBeanUtils;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;
public class StoreConfig implements IStoreConfig {
    private static Log log = LogFactory.getLog ( StoreConfig.class );
    protected static final StringManager sm = StringManager
            .getManager ( Constants.Package );
    private String serverFilename = "conf/server.xml";
    private StoreRegistry registry;
    private Server server;
    public String getServerFilename() {
        return serverFilename;
    }
    public void setServerFilename ( String string ) {
        serverFilename = string;
    }
    @Override
    public StoreRegistry getRegistry() {
        return registry;
    }
    @Override
    public void setServer ( Server aServer ) {
        server = aServer;
    }
    @Override
    public Server getServer() {
        return server;
    }
    @Override
    public void setRegistry ( StoreRegistry aRegistry ) {
        registry = aRegistry;
    }
    @Override
    public void storeConfig() {
        store ( server );
    }
    public synchronized void storeServer ( String aServerName, boolean backup,
                                           boolean externalAllowed ) throws MalformedObjectNameException {
        if ( aServerName == null || aServerName.length() == 0 ) {
            if ( log.isErrorEnabled() ) {
                log.error ( "Please, call with a correct server ObjectName!" );
            }
            return;
        }
        MBeanServer mserver = MBeanUtils.createServer();
        ObjectName objectName = new ObjectName ( aServerName );
        if ( mserver.isRegistered ( objectName ) ) {
            try {
                Server aServer = ( Server ) mserver.getAttribute ( objectName,
                                 "managedResource" );
                StoreDescription desc = null;
                desc = getRegistry().findDescription ( StandardContext.class );
                if ( desc != null ) {
                    boolean oldSeparate = desc.isStoreSeparate();
                    boolean oldBackup = desc.isBackup();
                    boolean oldExternalAllowed = desc.isExternalAllowed();
                    try {
                        desc.setStoreSeparate ( true );
                        desc.setBackup ( backup );
                        desc.setExternalAllowed ( externalAllowed );
                        store ( aServer );
                    } finally {
                        desc.setStoreSeparate ( oldSeparate );
                        desc.setBackup ( oldBackup );
                        desc.setExternalAllowed ( oldExternalAllowed );
                    }
                } else {
                    store ( aServer );
                }
            } catch ( Exception e ) {
                if ( log.isInfoEnabled() )
                    log.info ( "Object " + aServerName
                               + " is no a Server instance or store exception", e );
            }
        } else if ( log.isInfoEnabled() ) {
            log.info ( "Server " + aServerName + " not found!" );
        }
    }
    public synchronized void storeContext ( String aContextName, boolean backup,
                                            boolean externalAllowed ) throws MalformedObjectNameException {
        if ( aContextName == null || aContextName.length() == 0 ) {
            if ( log.isErrorEnabled() ) {
                log.error ( "Please, call with a correct context ObjectName!" );
            }
            return;
        }
        MBeanServer mserver = MBeanUtils.createServer();
        ObjectName objectName = new ObjectName ( aContextName );
        if ( mserver.isRegistered ( objectName ) ) {
            try {
                Context aContext = ( Context ) mserver.getAttribute ( objectName,
                                   "managedResource" );
                URL configFile = aContext.getConfigFile();
                if ( configFile != null ) {
                    try {
                        StoreDescription desc = null;
                        desc = getRegistry().findDescription (
                                   aContext.getClass() );
                        if ( desc != null ) {
                            boolean oldSeparate = desc.isStoreSeparate();
                            boolean oldBackup = desc.isBackup();
                            boolean oldExternalAllowed = desc
                                                         .isExternalAllowed();
                            try {
                                desc.setStoreSeparate ( true );
                                desc.setBackup ( backup );
                                desc.setExternalAllowed ( externalAllowed );
                                desc.getStoreFactory()
                                .store ( null, -2, aContext );
                            } finally {
                                desc.setStoreSeparate ( oldSeparate );
                                desc.setBackup ( oldBackup );
                                desc.setBackup ( oldExternalAllowed );
                            }
                        }
                    } catch ( Exception e ) {
                        log.error ( e );
                    }
                } else
                    log.error ( "Missing configFile at Context "
                                + aContext.getPath() + " to store!" );
            } catch ( Exception e ) {
                if ( log.isInfoEnabled() )
                    log
                    .info (
                        "Object "
                        + aContextName
                        + " is no a context instance or store exception",
                        e );
            }
        } else if ( log.isInfoEnabled() ) {
            log.info ( "Context " + aContextName + " not found!" );
        }
    }
    @Override
    public synchronized boolean store ( Server aServer ) {
        StoreFileMover mover = new StoreFileMover ( System
                .getProperty ( "catalina.base" ), getServerFilename(),
                getRegistry().getEncoding() );
        try {
            try ( PrintWriter writer = mover.getWriter() ) {
                store ( writer, -2, aServer );
            }
            mover.move();
            return true;
        } catch ( Exception e ) {
            log.error ( sm.getString ( "config.storeServerError" ), e );
        }
        return false;
    }
    @Override
    public synchronized boolean store ( Context aContext ) {
        URL configFile = aContext.getConfigFile();
        if ( configFile != null ) {
            try {
                StoreDescription desc = null;
                desc = getRegistry().findDescription ( aContext.getClass() );
                if ( desc != null ) {
                    boolean old = desc.isStoreSeparate();
                    try {
                        desc.setStoreSeparate ( true );
                        desc.getStoreFactory().store ( null, -2, aContext );
                    } finally {
                        desc.setStoreSeparate ( old );
                    }
                }
                return true;
            } catch ( Exception e ) {
                log.error ( sm.getString ( "config.storeContextError", aContext.getName() ), e );
            }
        } else {
            log.error ( "Missing configFile at Context " + aContext.getPath() );
        }
        return false;
    }
    @Override
    public void store ( PrintWriter aWriter, int indent,
                        Context aContext ) throws Exception {
        boolean oldSeparate = true;
        StoreDescription desc = null;
        try {
            desc = getRegistry().findDescription ( aContext.getClass() );
            oldSeparate = desc.isStoreSeparate();
            desc.setStoreSeparate ( false );
            desc.getStoreFactory().store ( aWriter, indent, aContext );
        } finally {
            if ( desc != null ) {
                desc.setStoreSeparate ( oldSeparate );
            }
        }
    }
    @Override
    public void store ( PrintWriter aWriter, int indent, Host aHost )
    throws Exception {
        StoreDescription desc = getRegistry().findDescription (
                                    aHost.getClass() );
        desc.getStoreFactory().store ( aWriter, indent, aHost );
    }
    @Override
    public void store ( PrintWriter aWriter, int indent,
                        Service aService ) throws Exception {
        StoreDescription desc = getRegistry().findDescription (
                                    aService.getClass() );
        desc.getStoreFactory().store ( aWriter, indent, aService );
    }
    @Override
    public void store ( PrintWriter writer, int indent,
                        Server aServer ) throws Exception {
        StoreDescription desc = getRegistry().findDescription (
                                    aServer.getClass() );
        desc.getStoreFactory().store ( writer, indent, aServer );
    }
}
