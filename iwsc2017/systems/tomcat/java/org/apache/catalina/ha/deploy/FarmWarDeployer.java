package org.apache.catalina.ha.deploy;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.ha.ClusterDeployer;
import org.apache.catalina.ha.ClusterListener;
import org.apache.catalina.ha.ClusterMessage;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.util.ContextName;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.res.StringManager;
public class FarmWarDeployer extends ClusterListener
    implements ClusterDeployer, FileChangeListener {
    private static final Log log = LogFactory.getLog ( FarmWarDeployer.class );
    private static final StringManager sm = StringManager.getManager ( FarmWarDeployer.class );
    protected boolean started = false;
    protected final HashMap<String, FileMessageFactory> fileFactories =
        new HashMap<>();
    protected String deployDir;
    private File deployDirFile = null;
    protected String tempDir;
    private File tempDirFile = null;
    protected String watchDir;
    private File watchDirFile = null;
    protected boolean watchEnabled = false;
    protected WarWatcher watcher = null;
    private int count = 0;
    protected int processDeployFrequency = 2;
    protected File configBase = null;
    protected Host host = null;
    protected MBeanServer mBeanServer = null;
    protected ObjectName oname = null;
    protected int maxValidTime = 5 * 60;
    public FarmWarDeployer() {
    }
    @Override
    public void start() throws Exception {
        if ( started ) {
            return;
        }
        Container hcontainer = getCluster().getContainer();
        if ( ! ( hcontainer instanceof Host ) ) {
            log.error ( sm.getString ( "farmWarDeployer.hostOnly" ) );
            return ;
        }
        host = ( Host ) hcontainer;
        Container econtainer = host.getParent();
        if ( ! ( econtainer instanceof Engine ) ) {
            log.error ( sm.getString ( "farmWarDeployer.hostParentEngine",
                                       host.getName() ) );
            return ;
        }
        Engine engine = ( Engine ) econtainer;
        String hostname = null;
        hostname = host.getName();
        try {
            oname = new ObjectName ( engine.getName() + ":type=Deployer,host="
                                     + hostname );
        } catch ( Exception e ) {
            log.error ( sm.getString ( "farmWarDeployer.mbeanNameFail",
                                       engine.getName(), hostname ), e );
            return;
        }
        if ( watchEnabled ) {
            watcher = new WarWatcher ( this, getWatchDirFile() );
            if ( log.isInfoEnabled() ) {
                log.info ( sm.getString (
                               "farmWarDeployer.watchDir", getWatchDir() ) );
            }
        }
        configBase = host.getConfigBaseFile();
        mBeanServer = Registry.getRegistry ( null, null ).getMBeanServer();
        started = true;
        count = 0;
        getCluster().addClusterListener ( this );
        if ( log.isInfoEnabled() ) {
            log.info ( sm.getString ( "farmWarDeployer.started" ) );
        }
    }
    @Override
    public void stop() throws LifecycleException {
        started = false;
        getCluster().removeClusterListener ( this );
        count = 0;
        if ( watcher != null ) {
            watcher.clear();
            watcher = null;
        }
        if ( log.isInfoEnabled() ) {
            log.info ( sm.getString ( "farmWarDeployer.stopped" ) );
        }
    }
    @Override
    public void messageReceived ( ClusterMessage msg ) {
        try {
            if ( msg instanceof FileMessage ) {
                FileMessage fmsg = ( FileMessage ) msg;
                if ( log.isDebugEnabled() )
                    log.debug ( sm.getString ( "farmWarDeployer.msgRxDeploy",
                                               fmsg.getContextName(), fmsg.getFileName() ) );
                FileMessageFactory factory = getFactory ( fmsg );
                if ( factory.writeMessage ( fmsg ) ) {
                    String name = factory.getFile().getName();
                    if ( !name.endsWith ( ".war" ) ) {
                        name = name + ".war";
                    }
                    File deployable = new File ( getDeployDirFile(), name );
                    try {
                        String contextName = fmsg.getContextName();
                        if ( !isServiced ( contextName ) ) {
                            addServiced ( contextName );
                            try {
                                remove ( contextName );
                                if ( !factory.getFile().renameTo ( deployable ) ) {
                                    log.error ( sm.getString (
                                                    "farmWarDeployer.renameFail",
                                                    factory.getFile(), deployable ) );
                                }
                                check ( contextName );
                            } finally {
                                removeServiced ( contextName );
                            }
                            if ( log.isDebugEnabled() )
                                log.debug ( sm.getString (
                                                "farmWarDeployer.deployEnd",
                                                contextName ) );
                        } else
                            log.error ( sm.getString (
                                            "farmWarDeployer.servicingDeploy",
                                            contextName, name ) );
                    } catch ( Exception ex ) {
                        log.error ( ex );
                    } finally {
                        removeFactory ( fmsg );
                    }
                }
            } else if ( msg instanceof UndeployMessage ) {
                try {
                    UndeployMessage umsg = ( UndeployMessage ) msg;
                    String contextName = umsg.getContextName();
                    if ( log.isDebugEnabled() )
                        log.debug ( sm.getString ( "farmWarDeployer.msgRxUndeploy",
                                                   contextName ) );
                    if ( !isServiced ( contextName ) ) {
                        addServiced ( contextName );
                        try {
                            remove ( contextName );
                        } finally {
                            removeServiced ( contextName );
                        }
                        if ( log.isDebugEnabled() )
                            log.debug ( sm.getString (
                                            "farmWarDeployer.undeployEnd",
                                            contextName ) );
                    } else
                        log.error ( sm.getString (
                                        "farmWarDeployer.servicingUneploy",
                                        contextName ) );
                } catch ( Exception ex ) {
                    log.error ( ex );
                }
            }
        } catch ( java.io.IOException x ) {
            log.error ( sm.getString ( "farmWarDeployer.msgIoe" ), x );
        }
    }
    public synchronized FileMessageFactory getFactory ( FileMessage msg )
    throws java.io.FileNotFoundException, java.io.IOException {
        File writeToFile = new File ( getTempDirFile(), msg.getFileName() );
        FileMessageFactory factory = fileFactories.get ( msg.getFileName() );
        if ( factory == null ) {
            factory = FileMessageFactory.getInstance ( writeToFile, true );
            factory.setMaxValidTime ( maxValidTime );
            fileFactories.put ( msg.getFileName(), factory );
        }
        return factory;
    }
    public void removeFactory ( FileMessage msg ) {
        fileFactories.remove ( msg.getFileName() );
    }
    @Override
    public boolean accept ( ClusterMessage msg ) {
        return ( msg instanceof FileMessage ) || ( msg instanceof UndeployMessage );
    }
    @Override
    public void install ( String contextName, File webapp ) throws IOException {
        Member[] members = getCluster().getMembers();
        if ( members.length == 0 ) {
            return;
        }
        Member localMember = getCluster().getLocalMember();
        FileMessageFactory factory =
            FileMessageFactory.getInstance ( webapp, false );
        FileMessage msg = new FileMessage ( localMember, webapp.getName(),
                                            contextName );
        if ( log.isDebugEnabled() )
            log.debug ( sm.getString ( "farmWarDeployer.sendStart", contextName,
                                       webapp ) );
        msg = factory.readMessage ( msg );
        while ( msg != null ) {
            for ( int i = 0; i < members.length; i++ ) {
                if ( log.isDebugEnabled() )
                    log.debug ( sm.getString ( "farmWarDeployer.sendFragment",
                                               contextName, webapp, members[i] ) );
                getCluster().send ( msg, members[i] );
            }
            msg = factory.readMessage ( msg );
        }
        if ( log.isDebugEnabled() )
            log.debug ( sm.getString (
                            "farmWarDeployer.sendEnd", contextName, webapp ) );
    }
    @Override
    public void remove ( String contextName, boolean undeploy )
    throws IOException {
        if ( getCluster().getMembers().length > 0 ) {
            if ( log.isInfoEnabled() ) {
                log.info ( sm.getString ( "farmWarDeployer.removeStart", contextName ) );
            }
            Member localMember = getCluster().getLocalMember();
            UndeployMessage msg = new UndeployMessage ( localMember, System
                    .currentTimeMillis(), "Undeploy:" + contextName + ":"
                    + System.currentTimeMillis(), contextName );
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "farmWarDeployer.removeTxMsg", contextName ) );
            }
            cluster.send ( msg );
        }
        if ( undeploy ) {
            try {
                if ( !isServiced ( contextName ) ) {
                    addServiced ( contextName );
                    try {
                        remove ( contextName );
                    } finally {
                        removeServiced ( contextName );
                    }
                } else
                    log.error ( sm.getString ( "farmWarDeployer.removeFailRemote",
                                               contextName ) );
            } catch ( Exception ex ) {
                log.error ( sm.getString ( "farmWarDeployer.removeFailLocal",
                                           contextName ), ex );
            }
        }
    }
    @Override
    public void fileModified ( File newWar ) {
        try {
            File deployWar = new File ( getDeployDirFile(), newWar.getName() );
            ContextName cn = new ContextName ( deployWar.getName(), true );
            if ( deployWar.exists() && deployWar.lastModified() > newWar.lastModified() ) {
                if ( log.isInfoEnabled() ) {
                    log.info ( sm.getString ( "farmWarDeployer.alreadyDeployed", cn.getName() ) );
                }
                return;
            }
            if ( log.isInfoEnabled() )
                log.info ( sm.getString ( "farmWarDeployer.modInstall",
                                          cn.getName(), deployWar.getAbsolutePath() ) );
            if ( !isServiced ( cn.getName() ) ) {
                addServiced ( cn.getName() );
                try {
                    copy ( newWar, deployWar );
                    check ( cn.getName() );
                } finally {
                    removeServiced ( cn.getName() );
                }
            } else {
                log.error ( sm.getString ( "farmWarDeployer.servicingDeploy",
                                           cn.getName(), deployWar.getName() ) );
            }
            install ( cn.getName(), deployWar );
        } catch ( Exception x ) {
            log.error ( sm.getString ( "farmWarDeployer.modInstallFail" ), x );
        }
    }
    @Override
    public void fileRemoved ( File removeWar ) {
        try {
            ContextName cn = new ContextName ( removeWar.getName(), true );
            if ( log.isInfoEnabled() )
                log.info ( sm.getString ( "farmWarDeployer.removeLocal",
                                          cn.getName() ) );
            remove ( cn.getName(), true );
        } catch ( Exception x ) {
            log.error ( sm.getString ( "farmWarDeployer.removeLocalFail" ), x );
        }
    }
    protected void remove ( String contextName ) throws Exception {
        Context context = ( Context ) host.findChild ( contextName );
        if ( context != null ) {
            if ( log.isDebugEnabled() )
                log.debug ( sm.getString ( "farmWarDeployer.undeployLocal",
                                           contextName ) );
            context.stop();
            String baseName = context.getBaseName();
            File war = new File ( host.getAppBaseFile(), baseName + ".war" );
            File dir = new File ( host.getAppBaseFile(), baseName );
            File xml = new File ( configBase, baseName + ".xml" );
            if ( war.exists() ) {
                if ( !war.delete() ) {
                    log.error ( sm.getString ( "farmWarDeployer.deleteFail", war ) );
                }
            } else if ( dir.exists() ) {
                undeployDir ( dir );
            } else {
                if ( !xml.delete() ) {
                    log.error ( sm.getString ( "farmWarDeployer.deleteFail", xml ) );
                }
            }
            check ( contextName );
        }
    }
    protected void undeployDir ( File dir ) {
        String files[] = dir.list();
        if ( files == null ) {
            files = new String[0];
        }
        for ( int i = 0; i < files.length; i++ ) {
            File file = new File ( dir, files[i] );
            if ( file.isDirectory() ) {
                undeployDir ( file );
            } else {
                if ( !file.delete() ) {
                    log.error ( sm.getString ( "farmWarDeployer.deleteFail", file ) );
                }
            }
        }
        if ( !dir.delete() ) {
            log.error ( sm.getString ( "farmWarDeployer.deleteFail", dir ) );
        }
    }
    @Override
    public void backgroundProcess() {
        if ( started ) {
            if ( watchEnabled ) {
                count = ( count + 1 ) % processDeployFrequency;
                if ( count == 0 ) {
                    watcher.check();
                }
            }
            removeInvalidFileFactories();
        }
    }
    protected void check ( String name ) throws Exception {
        String[] params = { name };
        String[] signature = { "java.lang.String" };
        mBeanServer.invoke ( oname, "check", params, signature );
    }
    protected boolean isServiced ( String name ) throws Exception {
        String[] params = { name };
        String[] signature = { "java.lang.String" };
        Boolean result = ( Boolean ) mBeanServer.invoke ( oname, "isServiced",
                         params, signature );
        return result.booleanValue();
    }
    protected void addServiced ( String name ) throws Exception {
        String[] params = { name };
        String[] signature = { "java.lang.String" };
        mBeanServer.invoke ( oname, "addServiced", params, signature );
    }
    protected void removeServiced ( String name ) throws Exception {
        String[] params = { name };
        String[] signature = { "java.lang.String" };
        mBeanServer.invoke ( oname, "removeServiced", params, signature );
    }
    @Override
    public boolean equals ( Object listener ) {
        return super.equals ( listener );
    }
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    public String getDeployDir() {
        return deployDir;
    }
    public File getDeployDirFile() {
        if ( deployDirFile != null ) {
            return deployDirFile;
        }
        File dir = getAbsolutePath ( getDeployDir() );
        this.deployDirFile = dir;
        return dir;
    }
    public void setDeployDir ( String deployDir ) {
        this.deployDir = deployDir;
    }
    public String getTempDir() {
        return tempDir;
    }
    public File getTempDirFile() {
        if ( tempDirFile != null ) {
            return tempDirFile;
        }
        File dir = getAbsolutePath ( getTempDir() );
        this.tempDirFile = dir;
        return dir;
    }
    public void setTempDir ( String tempDir ) {
        this.tempDir = tempDir;
    }
    public String getWatchDir() {
        return watchDir;
    }
    public File getWatchDirFile() {
        if ( watchDirFile != null ) {
            return watchDirFile;
        }
        File dir = getAbsolutePath ( getWatchDir() );
        this.watchDirFile = dir;
        return dir;
    }
    public void setWatchDir ( String watchDir ) {
        this.watchDir = watchDir;
    }
    public boolean isWatchEnabled() {
        return watchEnabled;
    }
    public boolean getWatchEnabled() {
        return watchEnabled;
    }
    public void setWatchEnabled ( boolean watchEnabled ) {
        this.watchEnabled = watchEnabled;
    }
    public int getProcessDeployFrequency() {
        return ( this.processDeployFrequency );
    }
    public void setProcessDeployFrequency ( int processExpiresFrequency ) {
        if ( processExpiresFrequency <= 0 ) {
            return;
        }
        this.processDeployFrequency = processExpiresFrequency;
    }
    public int getMaxValidTime() {
        return maxValidTime;
    }
    public void setMaxValidTime ( int maxValidTime ) {
        this.maxValidTime = maxValidTime;
    }
    protected boolean copy ( File from, File to ) {
        try {
            if ( !to.exists() ) {
                if ( !to.createNewFile() ) {
                    log.error ( sm.getString ( "fileNewFail", to ) );
                    return false;
                }
            }
        } catch ( IOException e ) {
            log.error ( sm.getString ( "farmWarDeployer.fileCopyFail",
                                       from, to ), e );
            return false;
        }
        try ( java.io.FileInputStream is = new java.io.FileInputStream ( from );
                    java.io.FileOutputStream os = new java.io.FileOutputStream ( to, false ); ) {
            byte[] buf = new byte[4096];
            while ( true ) {
                int len = is.read ( buf );
                if ( len < 0 ) {
                    break;
                }
                os.write ( buf, 0, len );
            }
        } catch ( IOException e ) {
            log.error ( sm.getString ( "farmWarDeployer.fileCopyFail",
                                       from, to ), e );
            return false;
        }
        return true;
    }
    protected void removeInvalidFileFactories() {
        String[] fileNames = fileFactories.keySet().toArray ( new String[0] );
        for ( String fileName : fileNames ) {
            FileMessageFactory factory = fileFactories.get ( fileName );
            if ( !factory.isValid() ) {
                fileFactories.remove ( fileName );
            }
        }
    }
    private File getAbsolutePath ( String path ) {
        File dir = new File ( path );
        if ( !dir.isAbsolute() ) {
            dir = new File ( getCluster().getContainer().getCatalinaBase(),
                             dir.getPath() );
        }
        try {
            dir = dir.getCanonicalFile();
        } catch ( IOException e ) {
        }
        return dir;
    }
}
