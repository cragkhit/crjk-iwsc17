package org.apache.catalina.manager;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Binding;
import javax.naming.NamingEnumeration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerServlet;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Manager;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.Session;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.ExpandWar;
import org.apache.catalina.util.ContextName;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.ServerInfo;
import org.apache.tomcat.util.Diagnostics;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.res.StringManager;
public class ManagerServlet extends HttpServlet implements ContainerServlet {
    private static final long serialVersionUID = 1L;
    protected File configBase = null;
    protected transient Context context = null;
    protected int debug = 1;
    protected File versioned = null;
    protected transient Host host = null;
    protected transient MBeanServer mBeanServer = null;
    protected ObjectName oname = null;
    protected transient javax.naming.Context global = null;
    protected static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    protected transient Wrapper wrapper = null;
    @Override
    public Wrapper getWrapper() {
        return ( this.wrapper );
    }
    @Override
    public void setWrapper ( Wrapper wrapper ) {
        this.wrapper = wrapper;
        if ( wrapper == null ) {
            context = null;
            host = null;
            oname = null;
        } else {
            context = ( Context ) wrapper.getParent();
            host = ( Host ) context.getParent();
            Engine engine = ( Engine ) host.getParent();
            String name = engine.getName() + ":type=Deployer,host=" +
                          host.getName();
            try {
                oname = new ObjectName ( name );
            } catch ( Exception e ) {
                log ( sm.getString ( "managerServlet.objectNameFail", name ), e );
            }
        }
        mBeanServer = Registry.getRegistry ( null, null ).getMBeanServer();
    }
    @Override
    public void destroy() {
    }
    @Override
    public void doGet ( HttpServletRequest request,
                        HttpServletResponse response )
    throws IOException, ServletException {
        StringManager smClient = StringManager.getManager (
                                     Constants.Package, request.getLocales() );
        String command = request.getPathInfo();
        if ( command == null ) {
            command = request.getServletPath();
        }
        String config = request.getParameter ( "config" );
        String path = request.getParameter ( "path" );
        ContextName cn = null;
        if ( path != null ) {
            cn = new ContextName ( path, request.getParameter ( "version" ) );
        }
        String type = request.getParameter ( "type" );
        String war = request.getParameter ( "war" );
        String tag = request.getParameter ( "tag" );
        boolean update = false;
        if ( ( request.getParameter ( "update" ) != null )
                && ( request.getParameter ( "update" ).equals ( "true" ) ) ) {
            update = true;
        }
        boolean statusLine = false;
        if ( "true".equals ( request.getParameter ( "statusLine" ) ) ) {
            statusLine = true;
        }
        response.setContentType ( "text/plain; charset=" + Constants.CHARSET );
        PrintWriter writer = response.getWriter();
        if ( command == null ) {
            writer.println ( smClient.getString ( "managerServlet.noCommand" ) );
        } else if ( command.equals ( "/deploy" ) ) {
            if ( war != null || config != null ) {
                deploy ( writer, config, cn, war, update, smClient );
            } else if ( tag != null ) {
                deploy ( writer, cn, tag, smClient );
            } else {
                writer.println ( smClient.getString (
                                     "managerServlet.invalidCommand", command ) );
            }
        } else if ( command.equals ( "/list" ) ) {
            list ( writer, smClient );
        } else if ( command.equals ( "/reload" ) ) {
            reload ( writer, cn, smClient );
        } else if ( command.equals ( "/resources" ) ) {
            resources ( writer, type, smClient );
        } else if ( command.equals ( "/save" ) ) {
            save ( writer, path, smClient );
        } else if ( command.equals ( "/serverinfo" ) ) {
            serverinfo ( writer, smClient );
        } else if ( command.equals ( "/sessions" ) ) {
            expireSessions ( writer, cn, request, smClient );
        } else if ( command.equals ( "/expire" ) ) {
            expireSessions ( writer, cn, request, smClient );
        } else if ( command.equals ( "/start" ) ) {
            start ( writer, cn, smClient );
        } else if ( command.equals ( "/stop" ) ) {
            stop ( writer, cn, smClient );
        } else if ( command.equals ( "/undeploy" ) ) {
            undeploy ( writer, cn, smClient );
        } else if ( command.equals ( "/findleaks" ) ) {
            findleaks ( statusLine, writer, smClient );
        } else if ( command.equals ( "/vminfo" ) ) {
            vmInfo ( writer, smClient, request.getLocales() );
        } else if ( command.equals ( "/threaddump" ) ) {
            threadDump ( writer, smClient, request.getLocales() );
        } else if ( command.equals ( "/sslConnectorCiphers" ) ) {
            sslConnectorCiphers ( writer, smClient );
        } else {
            writer.println ( smClient.getString ( "managerServlet.unknownCommand",
                                                  command ) );
        }
        writer.flush();
        writer.close();
    }
    @Override
    public void doPut ( HttpServletRequest request,
                        HttpServletResponse response )
    throws IOException, ServletException {
        StringManager smClient = StringManager.getManager (
                                     Constants.Package, request.getLocales() );
        String command = request.getPathInfo();
        if ( command == null ) {
            command = request.getServletPath();
        }
        String path = request.getParameter ( "path" );
        ContextName cn = null;
        if ( path != null ) {
            cn = new ContextName ( path, request.getParameter ( "version" ) );
        }
        String tag = request.getParameter ( "tag" );
        boolean update = false;
        if ( ( request.getParameter ( "update" ) != null )
                && ( request.getParameter ( "update" ).equals ( "true" ) ) ) {
            update = true;
        }
        response.setContentType ( "text/plain;charset=" + Constants.CHARSET );
        PrintWriter writer = response.getWriter();
        if ( command == null ) {
            writer.println ( smClient.getString ( "managerServlet.noCommand" ) );
        } else if ( command.equals ( "/deploy" ) ) {
            deploy ( writer, cn, tag, update, request, smClient );
        } else {
            writer.println ( smClient.getString ( "managerServlet.unknownCommand",
                                                  command ) );
        }
        writer.flush();
        writer.close();
    }
    @Override
    public void init() throws ServletException {
        if ( ( wrapper == null ) || ( context == null ) )
            throw new UnavailableException (
                sm.getString ( "managerServlet.noWrapper" ) );
        String value = null;
        try {
            value = getServletConfig().getInitParameter ( "debug" );
            debug = Integer.parseInt ( value );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
        }
        Server server = ( ( Engine ) host.getParent() ).getService().getServer();
        if ( server != null ) {
            global = server.getGlobalNamingContext();
        }
        versioned = ( File ) getServletContext().getAttribute
                    ( ServletContext.TEMPDIR );
        configBase = new File ( context.getCatalinaBase(), "conf" );
        Container container = context;
        Container host = null;
        Container engine = null;
        while ( container != null ) {
            if ( container instanceof Host ) {
                host = container;
            }
            if ( container instanceof Engine ) {
                engine = container;
            }
            container = container.getParent();
        }
        if ( engine != null ) {
            configBase = new File ( configBase, engine.getName() );
        }
        if ( host != null ) {
            configBase = new File ( configBase, host.getName() );
        }
        if ( debug >= 1 ) {
            log ( "init: Associated with Deployer '" +
                  oname + "'" );
            if ( global != null ) {
                log ( "init: Global resources are available" );
            }
        }
    }
    protected void findleaks ( boolean statusLine, PrintWriter writer,
                               StringManager smClient ) {
        if ( ! ( host instanceof StandardHost ) ) {
            writer.println ( smClient.getString ( "managerServlet.findleaksFail" ) );
            return;
        }
        String[] results =
            ( ( StandardHost ) host ).findReloadedContextMemoryLeaks();
        if ( results.length > 0 ) {
            if ( statusLine ) {
                writer.println (
                    smClient.getString ( "managerServlet.findleaksList" ) );
            }
            for ( String result : results ) {
                if ( "".equals ( result ) ) {
                    result = "/";
                }
                writer.println ( result );
            }
        } else if ( statusLine ) {
            writer.println ( smClient.getString ( "managerServlet.findleaksNone" ) );
        }
    }
    protected void vmInfo ( PrintWriter writer, StringManager smClient,
                            Enumeration<Locale> requestedLocales ) {
        writer.println ( smClient.getString ( "managerServlet.vminfo" ) );
        writer.print ( Diagnostics.getVMInfo ( requestedLocales ) );
    }
    protected void threadDump ( PrintWriter writer, StringManager smClient,
                                Enumeration<Locale> requestedLocales ) {
        writer.println ( smClient.getString ( "managerServlet.threaddump" ) );
        writer.print ( Diagnostics.getThreadDump ( requestedLocales ) );
    }
    protected void sslConnectorCiphers ( PrintWriter writer,
                                         StringManager smClient ) {
        writer.println ( smClient.getString (
                             "managerServlet.sslConnectorCiphers" ) );
        Map<String, Set<String>> connectorCiphers = getConnectorCiphers();
        for ( Map.Entry<String, Set<String>> entry : connectorCiphers.entrySet() ) {
            writer.println ( entry.getKey() );
            for ( String cipher : entry.getValue() ) {
                writer.print ( "  " );
                writer.println ( cipher );
            }
        }
    }
    protected synchronized void save ( PrintWriter writer, String path, StringManager smClient ) {
        ObjectName storeConfigOname;
        try {
            storeConfigOname = new ObjectName ( "Catalina:type=StoreConfig" );
        } catch ( MalformedObjectNameException e ) {
            log ( sm.getString ( "managerServlet.exception" ), e );
            writer.println ( smClient.getString ( "managerServlet.exception", e.toString() ) );
            return;
        }
        if ( !mBeanServer.isRegistered ( storeConfigOname ) ) {
            writer.println ( smClient.getString (
                                 "managerServlet.storeConfig.noMBean", storeConfigOname ) );
            return;
        }
        if ( ( path == null ) || path.length() == 0 || !path.startsWith ( "/" ) ) {
            try {
                mBeanServer.invoke ( storeConfigOname, "storeConfig", null, null );
                writer.println ( smClient.getString ( "managerServlet.saved" ) );
            } catch ( Exception e ) {
                log ( "managerServlet.storeConfig", e );
                writer.println ( smClient.getString ( "managerServlet.exception",
                                                      e.toString() ) );
                return;
            }
        } else {
            String contextPath = path;
            if ( path.equals ( "/" ) ) {
                contextPath = "";
            }
            Context context = ( Context ) host.findChild ( contextPath );
            if ( context == null ) {
                writer.println ( smClient.getString ( "managerServlet.noContext",
                                                      path ) );
                return;
            }
            try {
                mBeanServer.invoke ( storeConfigOname, "store",
                                     new Object[] {context},
                                     new String [] { "java.lang.String"} );
                writer.println ( smClient.getString ( "managerServlet.savedContext",
                                                      path ) );
            } catch ( Exception e ) {
                log ( "managerServlet.save[" + path + "]", e );
                writer.println ( smClient.getString ( "managerServlet.exception",
                                                      e.toString() ) );
                return;
            }
        }
    }
    protected synchronized void deploy
    ( PrintWriter writer, ContextName cn,
      String tag, boolean update, HttpServletRequest request,
      StringManager smClient ) {
        if ( debug >= 1 ) {
            log ( "deploy: Deploying web application '" + cn + "'" );
        }
        if ( !validateContextName ( cn, writer, smClient ) ) {
            return;
        }
        String name = cn.getName();
        String baseName = cn.getBaseName();
        String displayPath = cn.getDisplayName();
        Context context = ( Context ) host.findChild ( name );
        if ( context != null && !update ) {
            writer.println ( smClient.getString ( "managerServlet.alreadyContext",
                                                  displayPath ) );
            return;
        }
        File deployedWar = new File ( host.getAppBaseFile(), baseName + ".war" );
        File uploadedWar;
        if ( tag == null ) {
            if ( update ) {
                uploadedWar = new File ( deployedWar.getAbsolutePath() + ".tmp" );
                if ( uploadedWar.exists() && !uploadedWar.delete() ) {
                    writer.println ( smClient.getString ( "managerServlet.deleteFail",
                                                          uploadedWar ) );
                }
            } else {
                uploadedWar = deployedWar;
            }
        } else {
            File uploadPath = new File ( versioned, tag );
            if ( !uploadPath.mkdirs() && !uploadPath.isDirectory() ) {
                writer.println ( smClient.getString ( "managerServlet.mkdirFail",
                                                      uploadPath ) );
                return;
            }
            uploadedWar = new File ( uploadPath, baseName + ".war" );
        }
        if ( debug >= 2 ) {
            log ( "Uploading WAR file to " + uploadedWar );
        }
        try {
            if ( isServiced ( name ) ) {
                writer.println ( smClient.getString ( "managerServlet.inService", displayPath ) );
            } else {
                addServiced ( name );
                try {
                    uploadWar ( writer, request, uploadedWar, smClient );
                    if ( update && tag == null ) {
                        if ( deployedWar.exists() && !deployedWar.delete() ) {
                            writer.println ( smClient.getString ( "managerServlet.deleteFail",
                                                                  deployedWar ) );
                            return;
                        }
                        uploadedWar.renameTo ( deployedWar );
                    }
                    if ( tag != null ) {
                        copy ( uploadedWar, deployedWar );
                    }
                    check ( name );
                } finally {
                    removeServiced ( name );
                }
            }
        } catch ( Exception e ) {
            log ( "managerServlet.check[" + displayPath + "]", e );
            writer.println ( smClient.getString ( "managerServlet.exception",
                                                  e.toString() ) );
            return;
        }
        writeDeployResult ( writer, smClient, name, displayPath );
    }
    protected void deploy ( PrintWriter writer, ContextName cn, String tag,
                            StringManager smClient ) {
        if ( !validateContextName ( cn, writer, smClient ) ) {
            return;
        }
        String baseName = cn.getBaseName();
        String name = cn.getName();
        String displayPath = cn.getDisplayName();
        File localWar = new File ( new File ( versioned, tag ), baseName + ".war" );
        File deployedWar = new File ( host.getAppBaseFile(), baseName + ".war" );
        try {
            if ( isServiced ( name ) ) {
                writer.println ( smClient.getString ( "managerServlet.inService", displayPath ) );
            } else {
                addServiced ( name );
                try {
                    if ( !deployedWar.delete() ) {
                        writer.println ( smClient.getString ( "managerServlet.deleteFail",
                                                              deployedWar ) );
                        return;
                    }
                    copy ( localWar, deployedWar );
                    check ( name );
                } finally {
                    removeServiced ( name );
                }
            }
        } catch ( Exception e ) {
            log ( "managerServlet.check[" + displayPath + "]", e );
            writer.println ( smClient.getString ( "managerServlet.exception",
                                                  e.toString() ) );
            return;
        }
        writeDeployResult ( writer, smClient, name, displayPath );
    }
    protected void deploy ( PrintWriter writer, String config, ContextName cn,
                            String war, boolean update, StringManager smClient ) {
        if ( config != null && config.length() == 0 ) {
            config = null;
        }
        if ( war != null && war.length() == 0 ) {
            war = null;
        }
        if ( debug >= 1 ) {
            if ( config != null && config.length() > 0 ) {
                if ( war != null ) {
                    log ( "install: Installing context configuration at '" +
                          config + "' from '" + war + "'" );
                } else {
                    log ( "install: Installing context configuration at '" +
                          config + "'" );
                }
            } else {
                if ( cn != null ) {
                    log ( "install: Installing web application '" + cn +
                          "' from '" + war + "'" );
                } else {
                    log ( "install: Installing web application from '" + war + "'" );
                }
            }
        }
        if ( !validateContextName ( cn, writer, smClient ) ) {
            return;
        }
        @SuppressWarnings ( "null" )
        String name = cn.getName();
        String baseName = cn.getBaseName();
        String displayPath = cn.getDisplayName();
        Context context = ( Context ) host.findChild ( name );
        if ( context != null && !update ) {
            writer.println ( smClient.getString ( "managerServlet.alreadyContext",
                                                  displayPath ) );
            return;
        }
        if ( config != null && ( config.startsWith ( "file:" ) ) ) {
            config = config.substring ( "file:".length() );
        }
        if ( war != null && ( war.startsWith ( "file:" ) ) ) {
            war = war.substring ( "file:".length() );
        }
        try {
            if ( isServiced ( name ) ) {
                writer.println ( smClient.getString ( "managerServlet.inService", displayPath ) );
            } else {
                addServiced ( name );
                try {
                    if ( config != null ) {
                        if ( !configBase.mkdirs() && !configBase.isDirectory() ) {
                            writer.println ( smClient.getString (
                                                 "managerServlet.mkdirFail", configBase ) );
                            return;
                        }
                        File localConfig = new File ( configBase, baseName + ".xml" );
                        if ( localConfig.isFile() && !localConfig.delete() ) {
                            writer.println ( smClient.getString (
                                                 "managerServlet.deleteFail", localConfig ) );
                            return;
                        }
                        copy ( new File ( config ), localConfig );
                    }
                    if ( war != null ) {
                        File localWar;
                        if ( war.endsWith ( ".war" ) ) {
                            localWar = new File ( host.getAppBaseFile(), baseName + ".war" );
                        } else {
                            localWar = new File ( host.getAppBaseFile(), baseName );
                        }
                        if ( localWar.exists() && !ExpandWar.delete ( localWar ) ) {
                            writer.println ( smClient.getString (
                                                 "managerServlet.deleteFail", localWar ) );
                            return;
                        }
                        copy ( new File ( war ), localWar );
                    }
                    check ( name );
                } finally {
                    removeServiced ( name );
                }
            }
            writeDeployResult ( writer, smClient, name, displayPath );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            log ( "ManagerServlet.install[" + displayPath + "]", t );
            writer.println ( smClient.getString ( "managerServlet.exception",
                                                  t.toString() ) );
        }
    }
    private void writeDeployResult ( PrintWriter writer, StringManager smClient,
                                     String name, String displayPath ) {
        Context deployed = ( Context ) host.findChild ( name );
        if ( deployed != null && deployed.getConfigured() &&
                deployed.getState().isAvailable() ) {
            writer.println ( smClient.getString (
                                 "managerServlet.deployed", displayPath ) );
        } else if ( deployed != null && !deployed.getState().isAvailable() ) {
            writer.println ( smClient.getString (
                                 "managerServlet.deployedButNotStarted", displayPath ) );
        } else {
            writer.println ( smClient.getString (
                                 "managerServlet.deployFailed", displayPath ) );
        }
    }
    protected void list ( PrintWriter writer, StringManager smClient ) {
        if ( debug >= 1 )
            log ( "list: Listing contexts for virtual host '" +
                  host.getName() + "'" );
        writer.println ( smClient.getString ( "managerServlet.listed",
                                              host.getName() ) );
        Container[] contexts = host.findChildren();
        for ( int i = 0; i < contexts.length; i++ ) {
            Context context = ( Context ) contexts[i];
            if ( context != null ) {
                String displayPath = context.getPath();
                if ( displayPath.equals ( "" ) ) {
                    displayPath = "/";
                }
                if ( context.getState().isAvailable() ) {
                    writer.println ( smClient.getString ( "managerServlet.listitem",
                                                          displayPath,
                                                          "running",
                                                          "" + context.getManager().findSessions().length,
                                                          context.getDocBase() ) );
                } else {
                    writer.println ( smClient.getString ( "managerServlet.listitem",
                                                          displayPath,
                                                          "stopped",
                                                          "0",
                                                          context.getDocBase() ) );
                }
            }
        }
    }
    protected void reload ( PrintWriter writer, ContextName cn,
                            StringManager smClient ) {
        if ( debug >= 1 ) {
            log ( "restart: Reloading web application '" + cn + "'" );
        }
        if ( !validateContextName ( cn, writer, smClient ) ) {
            return;
        }
        try {
            Context context = ( Context ) host.findChild ( cn.getName() );
            if ( context == null ) {
                writer.println ( smClient.getString ( "managerServlet.noContext",
                                                      RequestUtil.filter ( cn.getDisplayName() ) ) );
                return;
            }
            if ( context.getName().equals ( this.context.getName() ) ) {
                writer.println ( smClient.getString ( "managerServlet.noSelf" ) );
                return;
            }
            context.reload();
            writer.println ( smClient.getString ( "managerServlet.reloaded",
                                                  cn.getDisplayName() ) );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            log ( "ManagerServlet.reload[" + cn.getDisplayName() + "]", t );
            writer.println ( smClient.getString ( "managerServlet.exception",
                                                  t.toString() ) );
        }
    }
    protected void resources ( PrintWriter writer, String type,
                               StringManager smClient ) {
        if ( debug >= 1 ) {
            if ( type != null ) {
                log ( "resources:  Listing resources of type " + type );
            } else {
                log ( "resources:  Listing resources of all types" );
            }
        }
        if ( global == null ) {
            writer.println ( smClient.getString ( "managerServlet.noGlobal" ) );
            return;
        }
        if ( type != null ) {
            writer.println ( smClient.getString ( "managerServlet.resourcesType",
                                                  type ) );
        } else {
            writer.println ( smClient.getString ( "managerServlet.resourcesAll" ) );
        }
        Class<?> clazz = null;
        try {
            if ( type != null ) {
                clazz = Class.forName ( type );
            }
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            log ( "ManagerServlet.resources[" + type + "]", t );
            writer.println ( smClient.getString ( "managerServlet.exception",
                                                  t.toString() ) );
            return;
        }
        printResources ( writer, "", global, type, clazz, smClient );
    }
    protected void printResources ( PrintWriter writer, String prefix,
                                    javax.naming.Context namingContext,
                                    String type, Class<?> clazz,
                                    StringManager smClient ) {
        try {
            NamingEnumeration<Binding> items = namingContext.listBindings ( "" );
            while ( items.hasMore() ) {
                Binding item = items.next();
                if ( item.getObject() instanceof javax.naming.Context ) {
                    printResources
                    ( writer, prefix + item.getName() + "/",
                      ( javax.naming.Context ) item.getObject(), type, clazz,
                      smClient );
                } else {
                    if ( ( clazz != null ) &&
                            ( ! ( clazz.isInstance ( item.getObject() ) ) ) ) {
                        continue;
                    }
                    writer.print ( prefix + item.getName() );
                    writer.print ( ':' );
                    writer.print ( item.getClassName() );
                    writer.println();
                }
            }
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            log ( "ManagerServlet.resources[" + type + "]", t );
            writer.println ( smClient.getString ( "managerServlet.exception",
                                                  t.toString() ) );
        }
    }
    protected void serverinfo ( PrintWriter writer,  StringManager smClient ) {
        if ( debug >= 1 ) {
            log ( "serverinfo" );
        }
        try {
            StringBuilder props = new StringBuilder();
            props.append ( "OK - Server info" );
            props.append ( "\nTomcat Version: " );
            props.append ( ServerInfo.getServerInfo() );
            props.append ( "\nOS Name: " );
            props.append ( System.getProperty ( "os.name" ) );
            props.append ( "\nOS Version: " );
            props.append ( System.getProperty ( "os.version" ) );
            props.append ( "\nOS Architecture: " );
            props.append ( System.getProperty ( "os.arch" ) );
            props.append ( "\nJVM Version: " );
            props.append ( System.getProperty ( "java.runtime.version" ) );
            props.append ( "\nJVM Vendor: " );
            props.append ( System.getProperty ( "java.vm.vendor" ) );
            writer.println ( props.toString() );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            getServletContext().log ( "ManagerServlet.serverinfo", t );
            writer.println ( smClient.getString ( "managerServlet.exception",
                                                  t.toString() ) );
        }
    }
    protected void sessions ( PrintWriter writer, ContextName cn, int idle,
                              StringManager smClient ) {
        if ( debug >= 1 ) {
            log ( "sessions: Session information for web application '" + cn + "'" );
            if ( idle >= 0 ) {
                log ( "sessions: Session expiration for " + idle + " minutes '" + cn + "'" );
            }
        }
        if ( !validateContextName ( cn, writer, smClient ) ) {
            return;
        }
        String displayPath = cn.getDisplayName();
        try {
            Context context = ( Context ) host.findChild ( cn.getName() );
            if ( context == null ) {
                writer.println ( smClient.getString ( "managerServlet.noContext",
                                                      RequestUtil.filter ( displayPath ) ) );
                return;
            }
            Manager manager = context.getManager() ;
            if ( manager == null ) {
                writer.println ( smClient.getString ( "managerServlet.noManager",
                                                      RequestUtil.filter ( displayPath ) ) );
                return;
            }
            int maxCount = 60;
            int histoInterval = 1;
            int maxInactiveInterval = context.getSessionTimeout();
            if ( maxInactiveInterval > 0 ) {
                histoInterval = maxInactiveInterval / maxCount;
                if ( histoInterval * maxCount < maxInactiveInterval ) {
                    histoInterval++;
                }
                if ( 0 == histoInterval ) {
                    histoInterval = 1;
                }
                maxCount = maxInactiveInterval / histoInterval;
                if ( histoInterval * maxCount < maxInactiveInterval ) {
                    maxCount++;
                }
            }
            writer.println ( smClient.getString ( "managerServlet.sessions",
                                                  displayPath ) );
            writer.println ( smClient.getString (
                                 "managerServlet.sessiondefaultmax",
                                 "" + maxInactiveInterval ) );
            Session [] sessions = manager.findSessions();
            int[] timeout = new int[maxCount + 1];
            int notimeout = 0;
            int expired = 0;
            for ( int i = 0; i < sessions.length; i++ ) {
                int time = ( int ) ( sessions[i].getIdleTimeInternal() / 1000L );
                if ( idle >= 0 && time >= idle * 60 ) {
                    sessions[i].expire();
                    expired++;
                }
                time = time / 60 / histoInterval;
                if ( time < 0 ) {
                    notimeout++;
                } else if ( time >= maxCount ) {
                    timeout[maxCount]++;
                } else {
                    timeout[time]++;
                }
            }
            if ( timeout[0] > 0 )
                writer.println ( smClient.getString (
                                     "managerServlet.sessiontimeout",
                                     "<" + histoInterval, "" + timeout[0] ) );
            for ( int i = 1; i < maxCount; i++ ) {
                if ( timeout[i] > 0 )
                    writer.println ( smClient.getString (
                                         "managerServlet.sessiontimeout",
                                         "" + ( i ) * histoInterval + " - <" + ( i + 1 ) * histoInterval,
                                         "" + timeout[i] ) );
            }
            if ( timeout[maxCount] > 0 ) {
                writer.println ( smClient.getString (
                                     "managerServlet.sessiontimeout",
                                     ">=" + maxCount * histoInterval,
                                     "" + timeout[maxCount] ) );
            }
            if ( notimeout > 0 )
                writer.println ( smClient.getString (
                                     "managerServlet.sessiontimeout.unlimited",
                                     "" + notimeout ) );
            if ( idle >= 0 )
                writer.println ( smClient.getString (
                                     "managerServlet.sessiontimeout.expired",
                                     ">" + idle, "" + expired ) );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            log ( "ManagerServlet.sessions[" + displayPath + "]", t );
            writer.println ( smClient.getString ( "managerServlet.exception",
                                                  t.toString() ) );
        }
    }
    protected void expireSessions ( PrintWriter writer, ContextName cn,
                                    HttpServletRequest req, StringManager smClient ) {
        int idle = -1;
        String idleParam = req.getParameter ( "idle" );
        if ( idleParam != null ) {
            try {
                idle = Integer.parseInt ( idleParam );
            } catch ( NumberFormatException e ) {
                log ( "Could not parse idle parameter to an int: " + idleParam );
            }
        }
        sessions ( writer, cn, idle, smClient );
    }
    protected void start ( PrintWriter writer, ContextName cn,
                           StringManager smClient ) {
        if ( debug >= 1 ) {
            log ( "start: Starting web application '" + cn + "'" );
        }
        if ( !validateContextName ( cn, writer, smClient ) ) {
            return;
        }
        String displayPath = cn.getDisplayName();
        try {
            Context context = ( Context ) host.findChild ( cn.getName() );
            if ( context == null ) {
                writer.println ( smClient.getString ( "managerServlet.noContext",
                                                      RequestUtil.filter ( displayPath ) ) );
                return;
            }
            context.start();
            if ( context.getState().isAvailable() )
                writer.println ( smClient.getString ( "managerServlet.started",
                                                      displayPath ) );
            else
                writer.println ( smClient.getString ( "managerServlet.startFailed",
                                                      displayPath ) );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            getServletContext().log ( sm.getString ( "managerServlet.startFailed",
                                      displayPath ), t );
            writer.println ( smClient.getString ( "managerServlet.startFailed",
                                                  displayPath ) );
            writer.println ( smClient.getString ( "managerServlet.exception",
                                                  t.toString() ) );
        }
    }
    protected void stop ( PrintWriter writer, ContextName cn,
                          StringManager smClient ) {
        if ( debug >= 1 ) {
            log ( "stop: Stopping web application '" + cn + "'" );
        }
        if ( !validateContextName ( cn, writer, smClient ) ) {
            return;
        }
        String displayPath = cn.getDisplayName();
        try {
            Context context = ( Context ) host.findChild ( cn.getName() );
            if ( context == null ) {
                writer.println ( smClient.getString ( "managerServlet.noContext",
                                                      RequestUtil.filter ( displayPath ) ) );
                return;
            }
            if ( context.getName().equals ( this.context.getName() ) ) {
                writer.println ( smClient.getString ( "managerServlet.noSelf" ) );
                return;
            }
            context.stop();
            writer.println ( smClient.getString (
                                 "managerServlet.stopped", displayPath ) );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            log ( "ManagerServlet.stop[" + displayPath + "]", t );
            writer.println ( smClient.getString ( "managerServlet.exception",
                                                  t.toString() ) );
        }
    }
    protected void undeploy ( PrintWriter writer, ContextName cn,
                              StringManager smClient ) {
        if ( debug >= 1 ) {
            log ( "undeploy: Undeploying web application at '" + cn + "'" );
        }
        if ( !validateContextName ( cn, writer, smClient ) ) {
            return;
        }
        String name = cn.getName();
        String baseName = cn.getBaseName();
        String displayPath = cn.getDisplayName();
        try {
            Context context = ( Context ) host.findChild ( name );
            if ( context == null ) {
                writer.println ( smClient.getString ( "managerServlet.noContext",
                                                      RequestUtil.filter ( displayPath ) ) );
                return;
            }
            if ( !isDeployed ( name ) ) {
                writer.println ( smClient.getString ( "managerServlet.notDeployed",
                                                      RequestUtil.filter ( displayPath ) ) );
                return;
            }
            if ( isServiced ( name ) ) {
                writer.println ( smClient.getString ( "managerServlet.inService", displayPath ) );
            } else {
                addServiced ( name );
                try {
                    context.stop();
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                }
                try {
                    File war = new File ( host.getAppBaseFile(), baseName + ".war" );
                    File dir = new File ( host.getAppBaseFile(), baseName );
                    File xml = new File ( configBase, baseName + ".xml" );
                    if ( war.exists() && !war.delete() ) {
                        writer.println ( smClient.getString (
                                             "managerServlet.deleteFail", war ) );
                        return;
                    } else if ( dir.exists() && !undeployDir ( dir ) ) {
                        writer.println ( smClient.getString (
                                             "managerServlet.deleteFail", dir ) );
                        return;
                    } else if ( xml.exists() && !xml.delete() ) {
                        writer.println ( smClient.getString (
                                             "managerServlet.deleteFail", xml ) );
                        return;
                    }
                    check ( name );
                } finally {
                    removeServiced ( name );
                }
            }
            writer.println ( smClient.getString ( "managerServlet.undeployed",
                                                  displayPath ) );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            log ( "ManagerServlet.undeploy[" + displayPath + "]", t );
            writer.println ( smClient.getString ( "managerServlet.exception",
                                                  t.toString() ) );
        }
    }
    protected boolean isDeployed ( String name )
    throws Exception {
        String[] params = { name };
        String[] signature = { "java.lang.String" };
        Boolean result =
            ( Boolean ) mBeanServer.invoke ( oname, "isDeployed", params, signature );
        return result.booleanValue();
    }
    protected void check ( String name )
    throws Exception {
        String[] params = { name };
        String[] signature = { "java.lang.String" };
        mBeanServer.invoke ( oname, "check", params, signature );
    }
    protected boolean isServiced ( String name )
    throws Exception {
        String[] params = { name };
        String[] signature = { "java.lang.String" };
        Boolean result =
            ( Boolean ) mBeanServer.invoke ( oname, "isServiced", params, signature );
        return result.booleanValue();
    }
    protected void addServiced ( String name )
    throws Exception {
        String[] params = { name };
        String[] signature = { "java.lang.String" };
        mBeanServer.invoke ( oname, "addServiced", params, signature );
    }
    protected void removeServiced ( String name )
    throws Exception {
        String[] params = { name };
        String[] signature = { "java.lang.String" };
        mBeanServer.invoke ( oname, "removeServiced", params, signature );
    }
    protected boolean undeployDir ( File dir ) {
        String files[] = dir.list();
        if ( files == null ) {
            files = new String[0];
        }
        for ( int i = 0; i < files.length; i++ ) {
            File file = new File ( dir, files[i] );
            if ( file.isDirectory() ) {
                if ( !undeployDir ( file ) ) {
                    return false;
                }
            } else {
                if ( !file.delete() ) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
    protected void uploadWar ( PrintWriter writer, HttpServletRequest request,
                               File war, StringManager smClient ) throws IOException {
        if ( war.exists() && !war.delete() ) {
            String msg = smClient.getString ( "managerServlet.deleteFail", war );
            throw new IOException ( msg );
        }
        try ( ServletInputStream istream = request.getInputStream();
                    BufferedOutputStream ostream =
                        new BufferedOutputStream ( new FileOutputStream ( war ), 1024 ) ) {
            byte buffer[] = new byte[1024];
            while ( true ) {
                int n = istream.read ( buffer );
                if ( n < 0 ) {
                    break;
                }
                ostream.write ( buffer, 0, n );
            }
        } catch ( IOException e ) {
            if ( war.exists() && !war.delete() ) {
                writer.println (
                    smClient.getString ( "managerServlet.deleteFail", war ) );
            }
            throw e;
        }
    }
    protected static boolean validateContextName ( ContextName cn,
            PrintWriter writer, StringManager sm ) {
        if ( cn != null &&
                ( cn.getPath().startsWith ( "/" ) || cn.getPath().equals ( "" ) ) ) {
            return true;
        }
        String path = null;
        if ( cn != null ) {
            path = RequestUtil.filter ( cn.getPath() );
        }
        writer.println ( sm.getString ( "managerServlet.invalidPath", path ) );
        return false;
    }
    public static boolean copy ( File src, File dest ) {
        boolean result = false;
        try {
            if ( src != null &&
                    !src.getCanonicalPath().equals ( dest.getCanonicalPath() ) ) {
                result = copyInternal ( src, dest, new byte[4096] );
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return result;
    }
    public static boolean copyInternal ( File src, File dest, byte[] buf ) {
        boolean result = true;
        String files[] = null;
        if ( src.isDirectory() ) {
            files = src.list();
            result = dest.mkdir();
        } else {
            files = new String[1];
            files[0] = "";
        }
        if ( files == null ) {
            files = new String[0];
        }
        for ( int i = 0; ( i < files.length ) && result; i++ ) {
            File fileSrc = new File ( src, files[i] );
            File fileDest = new File ( dest, files[i] );
            if ( fileSrc.isDirectory() ) {
                result = copyInternal ( fileSrc, fileDest, buf );
            } else {
                try ( FileInputStream is = new FileInputStream ( fileSrc );
                            FileOutputStream os = new FileOutputStream ( fileDest ) ) {
                    int len = 0;
                    while ( true ) {
                        len = is.read ( buf );
                        if ( len == -1 ) {
                            break;
                        }
                        os.write ( buf, 0, len );
                    }
                } catch ( IOException e ) {
                    e.printStackTrace();
                    result = false;
                }
            }
        }
        return result;
    }
    protected Map<String, Set<String>> getConnectorCiphers() {
        Map<String, Set<String>> result = new HashMap<>();
        Engine e = ( Engine ) host.getParent();
        Service s = e.getService();
        Connector connectors[] = s.findConnectors();
        for ( Connector connector : connectors ) {
            if ( Boolean.TRUE.equals ( connector.getProperty ( "SSLEnabled" ) ) ) {
                SSLHostConfig[] sslHostConfigs = connector.getProtocolHandler().findSslHostConfigs();
                for ( SSLHostConfig sslHostConfig : sslHostConfigs ) {
                    String name = connector.toString() + "-" + sslHostConfig.getHostName();
                    Set<String> cipherList = new HashSet<>();
                    String[] cipherNames = sslHostConfig.getEnabledCiphers();
                    for ( String cipherName : cipherNames ) {
                        cipherList.add ( cipherName );
                    }
                    result.put ( name, cipherList );
                }
            } else {
                Set<String> cipherList = new HashSet<>();
                cipherList.add ( sm.getString ( "managerServlet.notSslConnector" ) );
                result.put ( connector.toString(), cipherList );
            }
        }
        return result;
    }
}
