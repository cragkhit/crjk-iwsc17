package org.apache.catalina.mbeans;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.Loader;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.JmxEnabled;
import org.apache.catalina.Valve;
import org.apache.catalina.realm.UserDatabaseRealm;
import org.apache.catalina.Manager;
import org.apache.catalina.Context;
import org.apache.catalina.session.StandardManager;
import org.apache.catalina.startup.HostConfig;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import java.io.File;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.realm.MemoryRealm;
import org.apache.catalina.realm.JNDIRealm;
import org.apache.catalina.realm.JDBCRealm;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.Realm;
import org.apache.catalina.realm.DataSourceRealm;
import org.apache.catalina.core.StandardService;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.Container;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
public class MBeanFactory {
    private static final Log log;
    protected static final StringManager sm;
    private static final MBeanServer mserver;
    private Object container;
    public void setContainer ( final Object container ) {
        this.container = container;
    }
    private final String getPathStr ( final String t ) {
        if ( t == null || t.equals ( "/" ) ) {
            return "";
        }
        return t;
    }
    private Container getParentContainerFromParent ( final ObjectName pname ) throws Exception {
        final String type = pname.getKeyProperty ( "type" );
        final String j2eeType = pname.getKeyProperty ( "j2eeType" );
        final Service service = this.getService ( pname );
        final StandardEngine engine = ( StandardEngine ) service.getContainer();
        if ( j2eeType != null && j2eeType.equals ( "WebModule" ) ) {
            String name = pname.getKeyProperty ( "name" );
            name = name.substring ( 2 );
            final int i = name.indexOf ( 47 );
            final String hostName = name.substring ( 0, i );
            final String path = name.substring ( i );
            final Container host = engine.findChild ( hostName );
            final String pathStr = this.getPathStr ( path );
            final Container context = host.findChild ( pathStr );
            return context;
        }
        if ( type != null ) {
            if ( type.equals ( "Engine" ) ) {
                return engine;
            }
            if ( type.equals ( "Host" ) ) {
                final String hostName2 = pname.getKeyProperty ( "host" );
                final Container host2 = engine.findChild ( hostName2 );
                return host2;
            }
        }
        return null;
    }
    private Container getParentContainerFromChild ( final ObjectName oname ) throws Exception {
        final String hostName = oname.getKeyProperty ( "host" );
        String path = oname.getKeyProperty ( "path" );
        final Service service = this.getService ( oname );
        final Container engine = service.getContainer();
        if ( hostName == null ) {
            return engine;
        }
        if ( path == null ) {
            final Container host = engine.findChild ( hostName );
            return host;
        }
        final Container host = engine.findChild ( hostName );
        path = this.getPathStr ( path );
        final Container context = host.findChild ( path );
        return context;
    }
    private Service getService ( final ObjectName oname ) throws Exception {
        if ( this.container instanceof Service ) {
            return ( Service ) this.container;
        }
        StandardService service = null;
        final String domain = oname.getDomain();
        if ( this.container instanceof Server ) {
            final Service[] services = ( ( Server ) this.container ).findServices();
            for ( int i = 0; i < services.length; ++i ) {
                service = ( StandardService ) services[i];
                if ( domain.equals ( service.getObjectName().getDomain() ) ) {
                    break;
                }
            }
        }
        if ( service == null || !service.getObjectName().getDomain().equals ( domain ) ) {
            throw new Exception ( "Service with the domain is not found" );
        }
        return service;
    }
    public String createAjpConnector ( final String parent, final String address, final int port ) throws Exception {
        return this.createConnector ( parent, address, port, true, false );
    }
    public String createDataSourceRealm ( final String parent, final String dataSourceName, final String roleNameCol, final String userCredCol, final String userNameCol, final String userRoleTable, final String userTable ) throws Exception {
        final DataSourceRealm realm = new DataSourceRealm();
        realm.setDataSourceName ( dataSourceName );
        realm.setRoleNameCol ( roleNameCol );
        realm.setUserCredCol ( userCredCol );
        realm.setUserNameCol ( userNameCol );
        realm.setUserRoleTable ( userRoleTable );
        realm.setUserTable ( userTable );
        final ObjectName pname = new ObjectName ( parent );
        final Container container = this.getParentContainerFromParent ( pname );
        container.setRealm ( realm );
        final ObjectName oname = realm.getObjectName();
        if ( oname != null ) {
            return oname.toString();
        }
        return null;
    }
    public String createHttpConnector ( final String parent, final String address, final int port ) throws Exception {
        return this.createConnector ( parent, address, port, false, false );
    }
    private String createConnector ( final String parent, final String address, final int port, final boolean isAjp, final boolean isSSL ) throws Exception {
        final String protocol = isAjp ? "AJP/1.3" : "HTTP/1.1";
        final Connector retobj = new Connector ( protocol );
        if ( address != null && address.length() > 0 ) {
            retobj.setProperty ( "address", address );
        }
        retobj.setPort ( port );
        retobj.setSecure ( isSSL );
        retobj.setScheme ( isSSL ? "https" : "http" );
        final ObjectName pname = new ObjectName ( parent );
        final Service service = this.getService ( pname );
        service.addConnector ( retobj );
        final ObjectName coname = retobj.getObjectName();
        return coname.toString();
    }
    public String createHttpsConnector ( final String parent, final String address, final int port ) throws Exception {
        return this.createConnector ( parent, address, port, false, true );
    }
    public String createJDBCRealm ( final String parent, final String driverName, final String connectionName, final String connectionPassword, final String connectionURL ) throws Exception {
        final JDBCRealm realm = new JDBCRealm();
        realm.setDriverName ( driverName );
        realm.setConnectionName ( connectionName );
        realm.setConnectionPassword ( connectionPassword );
        realm.setConnectionURL ( connectionURL );
        final ObjectName pname = new ObjectName ( parent );
        final Container container = this.getParentContainerFromParent ( pname );
        container.setRealm ( realm );
        final ObjectName oname = realm.getObjectName();
        if ( oname != null ) {
            return oname.toString();
        }
        return null;
    }
    public String createJNDIRealm ( final String parent ) throws Exception {
        final JNDIRealm realm = new JNDIRealm();
        final ObjectName pname = new ObjectName ( parent );
        final Container container = this.getParentContainerFromParent ( pname );
        container.setRealm ( realm );
        final ObjectName oname = realm.getObjectName();
        if ( oname != null ) {
            return oname.toString();
        }
        return null;
    }
    public String createMemoryRealm ( final String parent ) throws Exception {
        final MemoryRealm realm = new MemoryRealm();
        final ObjectName pname = new ObjectName ( parent );
        final Container container = this.getParentContainerFromParent ( pname );
        container.setRealm ( realm );
        final ObjectName oname = realm.getObjectName();
        if ( oname != null ) {
            return oname.toString();
        }
        return null;
    }
    public String createStandardContext ( final String parent, final String path, final String docBase ) throws Exception {
        return this.createStandardContext ( parent, path, docBase, false, false );
    }
    public String createStandardContext ( final String parent, String path, final String docBase, final boolean xmlValidation, final boolean xmlNamespaceAware ) throws Exception {
        final StandardContext context = new StandardContext();
        path = this.getPathStr ( path );
        context.setPath ( path );
        context.setDocBase ( docBase );
        context.setXmlValidation ( xmlValidation );
        context.setXmlNamespaceAware ( xmlNamespaceAware );
        final ContextConfig contextConfig = new ContextConfig();
        context.addLifecycleListener ( contextConfig );
        final ObjectName pname = new ObjectName ( parent );
        final ObjectName deployer = new ObjectName ( pname.getDomain() + ":type=Deployer,host=" + pname.getKeyProperty ( "host" ) );
        if ( MBeanFactory.mserver.isRegistered ( deployer ) ) {
            final String contextName = context.getName();
            MBeanFactory.mserver.invoke ( deployer, "addServiced", new Object[] { contextName }, new String[] { "java.lang.String" } );
            final String configPath = ( String ) MBeanFactory.mserver.getAttribute ( deployer, "configBaseName" );
            final String baseName = context.getBaseName();
            final File configFile = new File ( new File ( configPath ), baseName + ".xml" );
            if ( configFile.isFile() ) {
                context.setConfigFile ( configFile.toURI().toURL() );
            }
            MBeanFactory.mserver.invoke ( deployer, "manageApp", new Object[] { context }, new String[] { "org.apache.catalina.Context" } );
            MBeanFactory.mserver.invoke ( deployer, "removeServiced", new Object[] { contextName }, new String[] { "java.lang.String" } );
        } else {
            MBeanFactory.log.warn ( "Deployer not found for " + pname.getKeyProperty ( "host" ) );
            final Service service = this.getService ( pname );
            final Engine engine = service.getContainer();
            final Host host = ( Host ) engine.findChild ( pname.getKeyProperty ( "host" ) );
            host.addChild ( context );
        }
        return context.getObjectName().toString();
    }
    public String createStandardHost ( final String parent, final String name, final String appBase, final boolean autoDeploy, final boolean deployOnStartup, final boolean deployXML, final boolean unpackWARs ) throws Exception {
        final StandardHost host = new StandardHost();
        host.setName ( name );
        host.setAppBase ( appBase );
        host.setAutoDeploy ( autoDeploy );
        host.setDeployOnStartup ( deployOnStartup );
        host.setDeployXML ( deployXML );
        host.setUnpackWARs ( unpackWARs );
        final HostConfig hostConfig = new HostConfig();
        host.addLifecycleListener ( hostConfig );
        final ObjectName pname = new ObjectName ( parent );
        final Service service = this.getService ( pname );
        final Engine engine = service.getContainer();
        engine.addChild ( host );
        return host.getObjectName().toString();
    }
    public String createStandardServiceEngine ( final String domain, final String defaultHost, final String baseDir ) throws Exception {
        if ( ! ( this.container instanceof Server ) ) {
            throw new Exception ( "Container not Server" );
        }
        final StandardEngine engine = new StandardEngine();
        engine.setDomain ( domain );
        engine.setName ( domain );
        engine.setDefaultHost ( defaultHost );
        final Service service = new StandardService();
        service.setContainer ( engine );
        service.setName ( domain );
        ( ( Server ) this.container ).addService ( service );
        return engine.getObjectName().toString();
    }
    public String createStandardManager ( final String parent ) throws Exception {
        final StandardManager manager = new StandardManager();
        final ObjectName pname = new ObjectName ( parent );
        final Container container = this.getParentContainerFromParent ( pname );
        if ( ! ( container instanceof Context ) ) {
            throw new Exception ( MBeanFactory.sm.getString ( "mBeanFactory.managerContext" ) );
        }
        ( ( Context ) container ).setManager ( manager );
        final ObjectName oname = manager.getObjectName();
        if ( oname != null ) {
            return oname.toString();
        }
        return null;
    }
    public String createUserDatabaseRealm ( final String parent, final String resourceName ) throws Exception {
        final UserDatabaseRealm realm = new UserDatabaseRealm();
        realm.setResourceName ( resourceName );
        final ObjectName pname = new ObjectName ( parent );
        final Container container = this.getParentContainerFromParent ( pname );
        container.setRealm ( realm );
        final ObjectName oname = realm.getObjectName();
        if ( oname != null ) {
            return oname.toString();
        }
        return null;
    }
    public String createValve ( final String className, final String parent ) throws Exception {
        final ObjectName parentName = new ObjectName ( parent );
        final Container container = this.getParentContainerFromParent ( parentName );
        if ( container == null ) {
            throw new IllegalArgumentException();
        }
        final Valve valve = ( Valve ) Class.forName ( className ).newInstance();
        container.getPipeline().addValve ( valve );
        if ( valve instanceof JmxEnabled ) {
            return ( ( JmxEnabled ) valve ).getObjectName().toString();
        }
        return null;
    }
    public String createWebappLoader ( final String parent ) throws Exception {
        final WebappLoader loader = new WebappLoader();
        final ObjectName pname = new ObjectName ( parent );
        final Container container = this.getParentContainerFromParent ( pname );
        if ( container instanceof Context ) {
            ( ( Context ) container ).setLoader ( loader );
        }
        final ObjectName oname = MBeanUtils.createObjectName ( pname.getDomain(), loader );
        return oname.toString();
    }
    public void removeConnector ( final String name ) throws Exception {
        final ObjectName oname = new ObjectName ( name );
        final Service service = this.getService ( oname );
        final String port = oname.getKeyProperty ( "port" );
        final Connector[] conns = service.findConnectors();
        for ( int i = 0; i < conns.length; ++i ) {
            final String connAddress = String.valueOf ( conns[i].getProperty ( "address" ) );
            final String connPort = "" + conns[i].getPort();
            if ( connAddress == null && port.equals ( connPort ) ) {
                service.removeConnector ( conns[i] );
                conns[i].destroy();
                break;
            }
            if ( port.equals ( connPort ) ) {
                service.removeConnector ( conns[i] );
                conns[i].destroy();
                break;
            }
        }
    }
    public void removeContext ( final String contextName ) throws Exception {
        final ObjectName oname = new ObjectName ( contextName );
        final String domain = oname.getDomain();
        final StandardService service = ( StandardService ) this.getService ( oname );
        final Engine engine = service.getContainer();
        String name = oname.getKeyProperty ( "name" );
        name = name.substring ( 2 );
        final int i = name.indexOf ( 47 );
        final String hostName = name.substring ( 0, i );
        final String path = name.substring ( i );
        final ObjectName deployer = new ObjectName ( domain + ":type=Deployer,host=" + hostName );
        final String pathStr = this.getPathStr ( path );
        if ( MBeanFactory.mserver.isRegistered ( deployer ) ) {
            MBeanFactory.mserver.invoke ( deployer, "addServiced", new Object[] { pathStr }, new String[] { "java.lang.String" } );
            MBeanFactory.mserver.invoke ( deployer, "unmanageApp", new Object[] { pathStr }, new String[] { "java.lang.String" } );
            MBeanFactory.mserver.invoke ( deployer, "removeServiced", new Object[] { pathStr }, new String[] { "java.lang.String" } );
        } else {
            MBeanFactory.log.warn ( "Deployer not found for " + hostName );
            final Host host = ( Host ) engine.findChild ( hostName );
            final Context context = ( Context ) host.findChild ( pathStr );
            host.removeChild ( context );
            if ( context instanceof StandardContext ) {
                try {
                    ( ( StandardContext ) context ).destroy();
                } catch ( Exception e ) {
                    MBeanFactory.log.warn ( "Error during context [" + context.getName() + "] destroy ", e );
                }
            }
        }
    }
    public void removeHost ( final String name ) throws Exception {
        final ObjectName oname = new ObjectName ( name );
        final String hostName = oname.getKeyProperty ( "host" );
        final Service service = this.getService ( oname );
        final Engine engine = service.getContainer();
        final Host host = ( Host ) engine.findChild ( hostName );
        if ( host != null ) {
            engine.removeChild ( host );
        }
    }
    public void removeLoader ( final String name ) throws Exception {
        final ObjectName oname = new ObjectName ( name );
        final Container container = this.getParentContainerFromChild ( oname );
        if ( container instanceof Context ) {
            ( ( Context ) container ).setLoader ( null );
        }
    }
    public void removeManager ( final String name ) throws Exception {
        final ObjectName oname = new ObjectName ( name );
        final Container container = this.getParentContainerFromChild ( oname );
        if ( container instanceof Context ) {
            ( ( Context ) container ).setManager ( null );
        }
    }
    public void removeRealm ( final String name ) throws Exception {
        final ObjectName oname = new ObjectName ( name );
        final Container container = this.getParentContainerFromChild ( oname );
        container.setRealm ( null );
    }
    public void removeService ( final String name ) throws Exception {
        if ( ! ( this.container instanceof Server ) ) {
            throw new Exception();
        }
        final ObjectName oname = new ObjectName ( name );
        final Service service = this.getService ( oname );
        ( ( Server ) this.container ).removeService ( service );
    }
    public void removeValve ( final String name ) throws Exception {
        final ObjectName oname = new ObjectName ( name );
        final Container container = this.getParentContainerFromChild ( oname );
        final Valve[] valves = container.getPipeline().getValves();
        for ( int i = 0; i < valves.length; ++i ) {
            final ObjectName voname = ( ( JmxEnabled ) valves[i] ).getObjectName();
            if ( voname.equals ( oname ) ) {
                container.getPipeline().removeValve ( valves[i] );
            }
        }
    }
    static {
        log = LogFactory.getLog ( MBeanFactory.class );
        sm = StringManager.getManager ( MBeanFactory.class );
        mserver = MBeanUtils.createServer();
    }
}
