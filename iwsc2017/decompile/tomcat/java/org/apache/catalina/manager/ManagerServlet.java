package org.apache.catalina.manager;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.Service;
import java.util.HashSet;
import java.util.HashMap;
import java.io.FileInputStream;
import javax.servlet.ServletInputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import org.apache.catalina.Session;
import org.apache.catalina.Manager;
import org.apache.catalina.util.ServerInfo;
import javax.naming.NamingEnumeration;
import javax.naming.Binding;
import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.startup.ExpandWar;
import javax.management.MalformedObjectNameException;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import org.apache.tomcat.util.Diagnostics;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.Container;
import org.apache.catalina.Server;
import org.apache.tomcat.util.ExceptionUtils;
import javax.servlet.UnavailableException;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.catalina.util.ContextName;
import java.util.Locale;
import java.util.Enumeration;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.catalina.Engine;
import org.apache.catalina.Wrapper;
import org.apache.tomcat.util.res.StringManager;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import org.apache.catalina.Host;
import org.apache.catalina.Context;
import java.io.File;
import org.apache.catalina.ContainerServlet;
import javax.servlet.http.HttpServlet;
public class ManagerServlet extends HttpServlet implements ContainerServlet {
    private static final long serialVersionUID = 1L;
    protected File configBase;
    protected transient Context context;
    protected int debug;
    protected File versioned;
    protected transient Host host;
    protected transient MBeanServer mBeanServer;
    protected ObjectName oname;
    protected transient javax.naming.Context global;
    protected static final StringManager sm;
    protected transient Wrapper wrapper;
    public ManagerServlet() {
        this.configBase = null;
        this.context = null;
        this.debug = 1;
        this.versioned = null;
        this.host = null;
        this.mBeanServer = null;
        this.oname = null;
        this.global = null;
        this.wrapper = null;
    }
    public Wrapper getWrapper() {
        return this.wrapper;
    }
    public void setWrapper ( final Wrapper wrapper ) {
        this.wrapper = wrapper;
        if ( wrapper == null ) {
            this.context = null;
            this.host = null;
            this.oname = null;
        } else {
            this.context = ( Context ) wrapper.getParent();
            this.host = ( Host ) this.context.getParent();
            final Engine engine = ( Engine ) this.host.getParent();
            final String name = engine.getName() + ":type=Deployer,host=" + this.host.getName();
            try {
                this.oname = new ObjectName ( name );
            } catch ( Exception e ) {
                this.log ( ManagerServlet.sm.getString ( "managerServlet.objectNameFail", name ), ( Throwable ) e );
            }
        }
        this.mBeanServer = Registry.getRegistry ( null, null ).getMBeanServer();
    }
    public void destroy() {
    }
    public void doGet ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException {
        final StringManager smClient = StringManager.getManager ( "org.apache.catalina.manager", request.getLocales() );
        String command = request.getPathInfo();
        if ( command == null ) {
            command = request.getServletPath();
        }
        final String config = request.getParameter ( "config" );
        final String path = request.getParameter ( "path" );
        ContextName cn = null;
        if ( path != null ) {
            cn = new ContextName ( path, request.getParameter ( "version" ) );
        }
        final String type = request.getParameter ( "type" );
        final String war = request.getParameter ( "war" );
        final String tag = request.getParameter ( "tag" );
        boolean update = false;
        if ( request.getParameter ( "update" ) != null && request.getParameter ( "update" ).equals ( "true" ) ) {
            update = true;
        }
        boolean statusLine = false;
        if ( "true".equals ( request.getParameter ( "statusLine" ) ) ) {
            statusLine = true;
        }
        response.setContentType ( "text/plain; charset=utf-8" );
        final PrintWriter writer = response.getWriter();
        if ( command == null ) {
            writer.println ( smClient.getString ( "managerServlet.noCommand" ) );
        } else if ( command.equals ( "/deploy" ) ) {
            if ( war != null || config != null ) {
                this.deploy ( writer, config, cn, war, update, smClient );
            } else if ( tag != null ) {
                this.deploy ( writer, cn, tag, smClient );
            } else {
                writer.println ( smClient.getString ( "managerServlet.invalidCommand", command ) );
            }
        } else if ( command.equals ( "/list" ) ) {
            this.list ( writer, smClient );
        } else if ( command.equals ( "/reload" ) ) {
            this.reload ( writer, cn, smClient );
        } else if ( command.equals ( "/resources" ) ) {
            this.resources ( writer, type, smClient );
        } else if ( command.equals ( "/save" ) ) {
            this.save ( writer, path, smClient );
        } else if ( command.equals ( "/serverinfo" ) ) {
            this.serverinfo ( writer, smClient );
        } else if ( command.equals ( "/sessions" ) ) {
            this.expireSessions ( writer, cn, request, smClient );
        } else if ( command.equals ( "/expire" ) ) {
            this.expireSessions ( writer, cn, request, smClient );
        } else if ( command.equals ( "/start" ) ) {
            this.start ( writer, cn, smClient );
        } else if ( command.equals ( "/stop" ) ) {
            this.stop ( writer, cn, smClient );
        } else if ( command.equals ( "/undeploy" ) ) {
            this.undeploy ( writer, cn, smClient );
        } else if ( command.equals ( "/findleaks" ) ) {
            this.findleaks ( statusLine, writer, smClient );
        } else if ( command.equals ( "/vminfo" ) ) {
            this.vmInfo ( writer, smClient, request.getLocales() );
        } else if ( command.equals ( "/threaddump" ) ) {
            this.threadDump ( writer, smClient, request.getLocales() );
        } else if ( command.equals ( "/sslConnectorCiphers" ) ) {
            this.sslConnectorCiphers ( writer, smClient );
        } else {
            writer.println ( smClient.getString ( "managerServlet.unknownCommand", command ) );
        }
        writer.flush();
        writer.close();
    }
    public void doPut ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException {
        final StringManager smClient = StringManager.getManager ( "org.apache.catalina.manager", request.getLocales() );
        String command = request.getPathInfo();
        if ( command == null ) {
            command = request.getServletPath();
        }
        final String path = request.getParameter ( "path" );
        ContextName cn = null;
        if ( path != null ) {
            cn = new ContextName ( path, request.getParameter ( "version" ) );
        }
        final String tag = request.getParameter ( "tag" );
        boolean update = false;
        if ( request.getParameter ( "update" ) != null && request.getParameter ( "update" ).equals ( "true" ) ) {
            update = true;
        }
        response.setContentType ( "text/plain;charset=utf-8" );
        final PrintWriter writer = response.getWriter();
        if ( command == null ) {
            writer.println ( smClient.getString ( "managerServlet.noCommand" ) );
        } else if ( command.equals ( "/deploy" ) ) {
            this.deploy ( writer, cn, tag, update, request, smClient );
        } else {
            writer.println ( smClient.getString ( "managerServlet.unknownCommand", command ) );
        }
        writer.flush();
        writer.close();
    }
    public void init() throws ServletException {
        if ( this.wrapper == null || this.context == null ) {
            throw new UnavailableException ( ManagerServlet.sm.getString ( "managerServlet.noWrapper" ) );
        }
        String value = null;
        try {
            value = this.getServletConfig().getInitParameter ( "debug" );
            this.debug = Integer.parseInt ( value );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
        }
        final Server server = ( ( Engine ) this.host.getParent() ).getService().getServer();
        if ( server != null ) {
            this.global = server.getGlobalNamingContext();
        }
        this.versioned = ( File ) this.getServletContext().getAttribute ( "javax.servlet.context.tempdir" );
        this.configBase = new File ( this.context.getCatalinaBase(), "conf" );
        Container container = this.context;
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
            this.configBase = new File ( this.configBase, engine.getName() );
        }
        if ( host != null ) {
            this.configBase = new File ( this.configBase, host.getName() );
        }
        if ( this.debug >= 1 ) {
            this.log ( "init: Associated with Deployer '" + this.oname + "'" );
            if ( this.global != null ) {
                this.log ( "init: Global resources are available" );
            }
        }
    }
    protected void findleaks ( final boolean statusLine, final PrintWriter writer, final StringManager smClient ) {
        if ( ! ( this.host instanceof StandardHost ) ) {
            writer.println ( smClient.getString ( "managerServlet.findleaksFail" ) );
            return;
        }
        final String[] results = ( ( StandardHost ) this.host ).findReloadedContextMemoryLeaks();
        if ( results.length > 0 ) {
            if ( statusLine ) {
                writer.println ( smClient.getString ( "managerServlet.findleaksList" ) );
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
    protected void vmInfo ( final PrintWriter writer, final StringManager smClient, final Enumeration<Locale> requestedLocales ) {
        writer.println ( smClient.getString ( "managerServlet.vminfo" ) );
        writer.print ( Diagnostics.getVMInfo ( requestedLocales ) );
    }
    protected void threadDump ( final PrintWriter writer, final StringManager smClient, final Enumeration<Locale> requestedLocales ) {
        writer.println ( smClient.getString ( "managerServlet.threaddump" ) );
        writer.print ( Diagnostics.getThreadDump ( requestedLocales ) );
    }
    protected void sslConnectorCiphers ( final PrintWriter writer, final StringManager smClient ) {
        writer.println ( smClient.getString ( "managerServlet.sslConnectorCiphers" ) );
        final Map<String, Set<String>> connectorCiphers = this.getConnectorCiphers();
        for ( final Map.Entry<String, Set<String>> entry : connectorCiphers.entrySet() ) {
            writer.println ( entry.getKey() );
            for ( final String cipher : entry.getValue() ) {
                writer.print ( "  " );
                writer.println ( cipher );
            }
        }
    }
    protected synchronized void save ( final PrintWriter writer, final String path, final StringManager smClient ) {
        ObjectName storeConfigOname;
        try {
            storeConfigOname = new ObjectName ( "Catalina:type=StoreConfig" );
        } catch ( MalformedObjectNameException e ) {
            this.log ( ManagerServlet.sm.getString ( "managerServlet.exception" ), ( Throwable ) e );
            writer.println ( smClient.getString ( "managerServlet.exception", e.toString() ) );
            return;
        }
        if ( !this.mBeanServer.isRegistered ( storeConfigOname ) ) {
            writer.println ( smClient.getString ( "managerServlet.storeConfig.noMBean", storeConfigOname ) );
            return;
        }
        Label_0169: {
            if ( path != null && path.length() != 0 ) {
                if ( path.startsWith ( "/" ) ) {
                    break Label_0169;
                }
            }
            try {
                this.mBeanServer.invoke ( storeConfigOname, "storeConfig", null, null );
                writer.println ( smClient.getString ( "managerServlet.saved" ) );
                return;
            } catch ( Exception e2 ) {
                this.log ( "managerServlet.storeConfig", ( Throwable ) e2 );
                writer.println ( smClient.getString ( "managerServlet.exception", e2.toString() ) );
                return;
            }
        }
        String contextPath = path;
        if ( path.equals ( "/" ) ) {
            contextPath = "";
        }
        final Context context = ( Context ) this.host.findChild ( contextPath );
        if ( context == null ) {
            writer.println ( smClient.getString ( "managerServlet.noContext", path ) );
            return;
        }
        try {
            this.mBeanServer.invoke ( storeConfigOname, "store", new Object[] { context }, new String[] { "java.lang.String" } );
            writer.println ( smClient.getString ( "managerServlet.savedContext", path ) );
        } catch ( Exception e3 ) {
            this.log ( "managerServlet.save[" + path + "]", ( Throwable ) e3 );
            writer.println ( smClient.getString ( "managerServlet.exception", e3.toString() ) );
        }
    }
    protected synchronized void deploy ( final PrintWriter writer, final ContextName cn, final String tag, final boolean update, final HttpServletRequest request, final StringManager smClient ) {
        if ( this.debug >= 1 ) {
            this.log ( "deploy: Deploying web application '" + cn + "'" );
        }
        if ( !validateContextName ( cn, writer, smClient ) ) {
            return;
        }
        final String name = cn.getName();
        final String baseName = cn.getBaseName();
        final String displayPath = cn.getDisplayName();
        final Context context = ( Context ) this.host.findChild ( name );
        if ( context != null && !update ) {
            writer.println ( smClient.getString ( "managerServlet.alreadyContext", displayPath ) );
            return;
        }
        final File deployedWar = new File ( this.host.getAppBaseFile(), baseName + ".war" );
        File uploadedWar;
        if ( tag == null ) {
            if ( update ) {
                uploadedWar = new File ( deployedWar.getAbsolutePath() + ".tmp" );
                if ( uploadedWar.exists() && !uploadedWar.delete() ) {
                    writer.println ( smClient.getString ( "managerServlet.deleteFail", uploadedWar ) );
                }
            } else {
                uploadedWar = deployedWar;
            }
        } else {
            final File uploadPath = new File ( this.versioned, tag );
            if ( !uploadPath.mkdirs() && !uploadPath.isDirectory() ) {
                writer.println ( smClient.getString ( "managerServlet.mkdirFail", uploadPath ) );
                return;
            }
            uploadedWar = new File ( uploadPath, baseName + ".war" );
        }
        if ( this.debug >= 2 ) {
            this.log ( "Uploading WAR file to " + uploadedWar );
        }
        try {
            if ( this.isServiced ( name ) ) {
                writer.println ( smClient.getString ( "managerServlet.inService", displayPath ) );
            } else {
                this.addServiced ( name );
                try {
                    this.uploadWar ( writer, request, uploadedWar, smClient );
                    if ( update && tag == null ) {
                        if ( deployedWar.exists() && !deployedWar.delete() ) {
                            writer.println ( smClient.getString ( "managerServlet.deleteFail", deployedWar ) );
                            return;
                        }
                        uploadedWar.renameTo ( deployedWar );
                    }
                    if ( tag != null ) {
                        copy ( uploadedWar, deployedWar );
                    }
                    this.check ( name );
                } finally {
                    this.removeServiced ( name );
                }
            }
        } catch ( Exception e ) {
            this.log ( "managerServlet.check[" + displayPath + "]", ( Throwable ) e );
            writer.println ( smClient.getString ( "managerServlet.exception", e.toString() ) );
            return;
        }
        this.writeDeployResult ( writer, smClient, name, displayPath );
    }
    protected void deploy ( final PrintWriter writer, final ContextName cn, final String tag, final StringManager smClient ) {
        if ( !validateContextName ( cn, writer, smClient ) ) {
            return;
        }
        final String baseName = cn.getBaseName();
        final String name = cn.getName();
        final String displayPath = cn.getDisplayName();
        final File localWar = new File ( new File ( this.versioned, tag ), baseName + ".war" );
        final File deployedWar = new File ( this.host.getAppBaseFile(), baseName + ".war" );
        try {
            if ( this.isServiced ( name ) ) {
                writer.println ( smClient.getString ( "managerServlet.inService", displayPath ) );
            } else {
                this.addServiced ( name );
                try {
                    if ( !deployedWar.delete() ) {
                        writer.println ( smClient.getString ( "managerServlet.deleteFail", deployedWar ) );
                        return;
                    }
                    copy ( localWar, deployedWar );
                    this.check ( name );
                } finally {
                    this.removeServiced ( name );
                }
            }
        } catch ( Exception e ) {
            this.log ( "managerServlet.check[" + displayPath + "]", ( Throwable ) e );
            writer.println ( smClient.getString ( "managerServlet.exception", e.toString() ) );
            return;
        }
        this.writeDeployResult ( writer, smClient, name, displayPath );
    }
    protected void deploy ( final PrintWriter writer, String config, final ContextName cn, String war, final boolean update, final StringManager smClient ) {
        if ( config != null && config.length() == 0 ) {
            config = null;
        }
        if ( war != null && war.length() == 0 ) {
            war = null;
        }
        if ( this.debug >= 1 ) {
            if ( config != null && config.length() > 0 ) {
                if ( war != null ) {
                    this.log ( "install: Installing context configuration at '" + config + "' from '" + war + "'" );
                } else {
                    this.log ( "install: Installing context configuration at '" + config + "'" );
                }
            } else if ( cn != null ) {
                this.log ( "install: Installing web application '" + cn + "' from '" + war + "'" );
            } else {
                this.log ( "install: Installing web application from '" + war + "'" );
            }
        }
        if ( !validateContextName ( cn, writer, smClient ) ) {
            return;
        }
        final String name = cn.getName();
        final String baseName = cn.getBaseName();
        final String displayPath = cn.getDisplayName();
        final Context context = ( Context ) this.host.findChild ( name );
        if ( context != null && !update ) {
            writer.println ( smClient.getString ( "managerServlet.alreadyContext", displayPath ) );
            return;
        }
        if ( config != null && config.startsWith ( "file:" ) ) {
            config = config.substring ( "file:".length() );
        }
        if ( war != null && war.startsWith ( "file:" ) ) {
            war = war.substring ( "file:".length() );
        }
        try {
            if ( this.isServiced ( name ) ) {
                writer.println ( smClient.getString ( "managerServlet.inService", displayPath ) );
            } else {
                this.addServiced ( name );
                try {
                    if ( config != null ) {
                        if ( !this.configBase.mkdirs() && !this.configBase.isDirectory() ) {
                            writer.println ( smClient.getString ( "managerServlet.mkdirFail", this.configBase ) );
                            return;
                        }
                        final File localConfig = new File ( this.configBase, baseName + ".xml" );
                        if ( localConfig.isFile() && !localConfig.delete() ) {
                            writer.println ( smClient.getString ( "managerServlet.deleteFail", localConfig ) );
                            return;
                        }
                        copy ( new File ( config ), localConfig );
                    }
                    if ( war != null ) {
                        File localWar;
                        if ( war.endsWith ( ".war" ) ) {
                            localWar = new File ( this.host.getAppBaseFile(), baseName + ".war" );
                        } else {
                            localWar = new File ( this.host.getAppBaseFile(), baseName );
                        }
                        if ( localWar.exists() && !ExpandWar.delete ( localWar ) ) {
                            writer.println ( smClient.getString ( "managerServlet.deleteFail", localWar ) );
                            return;
                        }
                        copy ( new File ( war ), localWar );
                    }
                    this.check ( name );
                } finally {
                    this.removeServiced ( name );
                }
            }
            this.writeDeployResult ( writer, smClient, name, displayPath );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            this.log ( "ManagerServlet.install[" + displayPath + "]", t );
            writer.println ( smClient.getString ( "managerServlet.exception", t.toString() ) );
        }
    }
    private void writeDeployResult ( final PrintWriter writer, final StringManager smClient, final String name, final String displayPath ) {
        final Context deployed = ( Context ) this.host.findChild ( name );
        if ( deployed != null && deployed.getConfigured() && deployed.getState().isAvailable() ) {
            writer.println ( smClient.getString ( "managerServlet.deployed", displayPath ) );
        } else if ( deployed != null && !deployed.getState().isAvailable() ) {
            writer.println ( smClient.getString ( "managerServlet.deployedButNotStarted", displayPath ) );
        } else {
            writer.println ( smClient.getString ( "managerServlet.deployFailed", displayPath ) );
        }
    }
    protected void list ( final PrintWriter writer, final StringManager smClient ) {
        if ( this.debug >= 1 ) {
            this.log ( "list: Listing contexts for virtual host '" + this.host.getName() + "'" );
        }
        writer.println ( smClient.getString ( "managerServlet.listed", this.host.getName() ) );
        final Container[] contexts = this.host.findChildren();
        for ( int i = 0; i < contexts.length; ++i ) {
            final Context context = ( Context ) contexts[i];
            if ( context != null ) {
                String displayPath = context.getPath();
                if ( displayPath.equals ( "" ) ) {
                    displayPath = "/";
                }
                if ( context.getState().isAvailable() ) {
                    writer.println ( smClient.getString ( "managerServlet.listitem", displayPath, "running", "" + context.getManager().findSessions().length, context.getDocBase() ) );
                } else {
                    writer.println ( smClient.getString ( "managerServlet.listitem", displayPath, "stopped", "0", context.getDocBase() ) );
                }
            }
        }
    }
    protected void reload ( final PrintWriter writer, final ContextName cn, final StringManager smClient ) {
        if ( this.debug >= 1 ) {
            this.log ( "restart: Reloading web application '" + cn + "'" );
        }
        if ( !validateContextName ( cn, writer, smClient ) ) {
            return;
        }
        try {
            final Context context = ( Context ) this.host.findChild ( cn.getName() );
            if ( context == null ) {
                writer.println ( smClient.getString ( "managerServlet.noContext", RequestUtil.filter ( cn.getDisplayName() ) ) );
                return;
            }
            if ( context.getName().equals ( this.context.getName() ) ) {
                writer.println ( smClient.getString ( "managerServlet.noSelf" ) );
                return;
            }
            context.reload();
            writer.println ( smClient.getString ( "managerServlet.reloaded", cn.getDisplayName() ) );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            this.log ( "ManagerServlet.reload[" + cn.getDisplayName() + "]", t );
            writer.println ( smClient.getString ( "managerServlet.exception", t.toString() ) );
        }
    }
    protected void resources ( final PrintWriter writer, final String type, final StringManager smClient ) {
        if ( this.debug >= 1 ) {
            if ( type != null ) {
                this.log ( "resources:  Listing resources of type " + type );
            } else {
                this.log ( "resources:  Listing resources of all types" );
            }
        }
        if ( this.global == null ) {
            writer.println ( smClient.getString ( "managerServlet.noGlobal" ) );
            return;
        }
        if ( type != null ) {
            writer.println ( smClient.getString ( "managerServlet.resourcesType", type ) );
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
            this.log ( "ManagerServlet.resources[" + type + "]", t );
            writer.println ( smClient.getString ( "managerServlet.exception", t.toString() ) );
            return;
        }
        this.printResources ( writer, "", this.global, type, clazz, smClient );
    }
    protected void printResources ( final PrintWriter writer, final String prefix, final javax.naming.Context namingContext, final String type, final Class<?> clazz, final StringManager smClient ) {
        try {
            final NamingEnumeration<Binding> items = namingContext.listBindings ( "" );
            while ( items.hasMore() ) {
                final Binding item = items.next();
                if ( item.getObject() instanceof javax.naming.Context ) {
                    this.printResources ( writer, prefix + item.getName() + "/", ( javax.naming.Context ) item.getObject(), type, clazz, smClient );
                } else {
                    if ( clazz != null && !clazz.isInstance ( item.getObject() ) ) {
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
            this.log ( "ManagerServlet.resources[" + type + "]", t );
            writer.println ( smClient.getString ( "managerServlet.exception", t.toString() ) );
        }
    }
    protected void serverinfo ( final PrintWriter writer, final StringManager smClient ) {
        if ( this.debug >= 1 ) {
            this.log ( "serverinfo" );
        }
        try {
            final StringBuilder props = new StringBuilder();
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
            this.getServletContext().log ( "ManagerServlet.serverinfo", t );
            writer.println ( smClient.getString ( "managerServlet.exception", t.toString() ) );
        }
    }
    protected void sessions ( final PrintWriter writer, final ContextName cn, final int idle, final StringManager smClient ) {
        if ( this.debug >= 1 ) {
            this.log ( "sessions: Session information for web application '" + cn + "'" );
            if ( idle >= 0 ) {
                this.log ( "sessions: Session expiration for " + idle + " minutes '" + cn + "'" );
            }
        }
        if ( !validateContextName ( cn, writer, smClient ) ) {
            return;
        }
        final String displayPath = cn.getDisplayName();
        try {
            final Context context = ( Context ) this.host.findChild ( cn.getName() );
            if ( context == null ) {
                writer.println ( smClient.getString ( "managerServlet.noContext", RequestUtil.filter ( displayPath ) ) );
                return;
            }
            final Manager manager = context.getManager();
            if ( manager == null ) {
                writer.println ( smClient.getString ( "managerServlet.noManager", RequestUtil.filter ( displayPath ) ) );
                return;
            }
            int maxCount = 60;
            int histoInterval = 1;
            final int maxInactiveInterval = context.getSessionTimeout();
            if ( maxInactiveInterval > 0 ) {
                histoInterval = maxInactiveInterval / maxCount;
                if ( histoInterval * maxCount < maxInactiveInterval ) {
                    ++histoInterval;
                }
                if ( 0 == histoInterval ) {
                    histoInterval = 1;
                }
                maxCount = maxInactiveInterval / histoInterval;
                if ( histoInterval * maxCount < maxInactiveInterval ) {
                    ++maxCount;
                }
            }
            writer.println ( smClient.getString ( "managerServlet.sessions", displayPath ) );
            writer.println ( smClient.getString ( "managerServlet.sessiondefaultmax", "" + maxInactiveInterval ) );
            final Session[] sessions = manager.findSessions();
            final int[] timeout = new int[maxCount + 1];
            int notimeout = 0;
            int expired = 0;
            for ( int i = 0; i < sessions.length; ++i ) {
                int time = ( int ) ( sessions[i].getIdleTimeInternal() / 1000L );
                if ( idle >= 0 && time >= idle * 60 ) {
                    sessions[i].expire();
                    ++expired;
                }
                time = time / 60 / histoInterval;
                if ( time < 0 ) {
                    ++notimeout;
                } else if ( time >= maxCount ) {
                    final int[] array = timeout;
                    final int n = maxCount;
                    ++array[n];
                } else {
                    final int[] array2 = timeout;
                    final int n2 = time;
                    ++array2[n2];
                }
            }
            if ( timeout[0] > 0 ) {
                writer.println ( smClient.getString ( "managerServlet.sessiontimeout", "<" + histoInterval, "" + timeout[0] ) );
            }
            for ( int i = 1; i < maxCount; ++i ) {
                if ( timeout[i] > 0 ) {
                    writer.println ( smClient.getString ( "managerServlet.sessiontimeout", "" + i * histoInterval + " - <" + ( i + 1 ) * histoInterval, "" + timeout[i] ) );
                }
            }
            if ( timeout[maxCount] > 0 ) {
                writer.println ( smClient.getString ( "managerServlet.sessiontimeout", ">=" + maxCount * histoInterval, "" + timeout[maxCount] ) );
            }
            if ( notimeout > 0 ) {
                writer.println ( smClient.getString ( "managerServlet.sessiontimeout.unlimited", "" + notimeout ) );
            }
            if ( idle >= 0 ) {
                writer.println ( smClient.getString ( "managerServlet.sessiontimeout.expired", ">" + idle, "" + expired ) );
            }
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            this.log ( "ManagerServlet.sessions[" + displayPath + "]", t );
            writer.println ( smClient.getString ( "managerServlet.exception", t.toString() ) );
        }
    }
    protected void expireSessions ( final PrintWriter writer, final ContextName cn, final HttpServletRequest req, final StringManager smClient ) {
        int idle = -1;
        final String idleParam = req.getParameter ( "idle" );
        if ( idleParam != null ) {
            try {
                idle = Integer.parseInt ( idleParam );
            } catch ( NumberFormatException e ) {
                this.log ( "Could not parse idle parameter to an int: " + idleParam );
            }
        }
        this.sessions ( writer, cn, idle, smClient );
    }
    protected void start ( final PrintWriter writer, final ContextName cn, final StringManager smClient ) {
        if ( this.debug >= 1 ) {
            this.log ( "start: Starting web application '" + cn + "'" );
        }
        if ( !validateContextName ( cn, writer, smClient ) ) {
            return;
        }
        final String displayPath = cn.getDisplayName();
        try {
            final Context context = ( Context ) this.host.findChild ( cn.getName() );
            if ( context == null ) {
                writer.println ( smClient.getString ( "managerServlet.noContext", RequestUtil.filter ( displayPath ) ) );
                return;
            }
            context.start();
            if ( context.getState().isAvailable() ) {
                writer.println ( smClient.getString ( "managerServlet.started", displayPath ) );
            } else {
                writer.println ( smClient.getString ( "managerServlet.startFailed", displayPath ) );
            }
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            this.getServletContext().log ( ManagerServlet.sm.getString ( "managerServlet.startFailed", displayPath ), t );
            writer.println ( smClient.getString ( "managerServlet.startFailed", displayPath ) );
            writer.println ( smClient.getString ( "managerServlet.exception", t.toString() ) );
        }
    }
    protected void stop ( final PrintWriter writer, final ContextName cn, final StringManager smClient ) {
        if ( this.debug >= 1 ) {
            this.log ( "stop: Stopping web application '" + cn + "'" );
        }
        if ( !validateContextName ( cn, writer, smClient ) ) {
            return;
        }
        final String displayPath = cn.getDisplayName();
        try {
            final Context context = ( Context ) this.host.findChild ( cn.getName() );
            if ( context == null ) {
                writer.println ( smClient.getString ( "managerServlet.noContext", RequestUtil.filter ( displayPath ) ) );
                return;
            }
            if ( context.getName().equals ( this.context.getName() ) ) {
                writer.println ( smClient.getString ( "managerServlet.noSelf" ) );
                return;
            }
            context.stop();
            writer.println ( smClient.getString ( "managerServlet.stopped", displayPath ) );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
            this.log ( "ManagerServlet.stop[" + displayPath + "]", t );
            writer.println ( smClient.getString ( "managerServlet.exception", t.toString() ) );
        }
    }
    protected void undeploy ( final PrintWriter writer, final ContextName cn, final StringManager smClient ) {
        if ( this.debug >= 1 ) {
            this.log ( "undeploy: Undeploying web application at '" + cn + "'" );
        }
        if ( !validateContextName ( cn, writer, smClient ) ) {
            return;
        }
        final String name = cn.getName();
        final String baseName = cn.getBaseName();
        final String displayPath = cn.getDisplayName();
        try {
            final Context context = ( Context ) this.host.findChild ( name );
            if ( context == null ) {
                writer.println ( smClient.getString ( "managerServlet.noContext", RequestUtil.filter ( displayPath ) ) );
                return;
            }
            if ( !this.isDeployed ( name ) ) {
                writer.println ( smClient.getString ( "managerServlet.notDeployed", RequestUtil.filter ( displayPath ) ) );
                return;
            }
            if ( this.isServiced ( name ) ) {
                writer.println ( smClient.getString ( "managerServlet.inService", displayPath ) );
            } else {
                this.addServiced ( name );
                try {
                    context.stop();
                } catch ( Throwable t ) {
                    ExceptionUtils.handleThrowable ( t );
                }
                try {
                    final File war = new File ( this.host.getAppBaseFile(), baseName + ".war" );
                    final File dir = new File ( this.host.getAppBaseFile(), baseName );
                    final File xml = new File ( this.configBase, baseName + ".xml" );
                    if ( war.exists() && !war.delete() ) {
                        writer.println ( smClient.getString ( "managerServlet.deleteFail", war ) );
                        return;
                    }
                    if ( dir.exists() && !this.undeployDir ( dir ) ) {
                        writer.println ( smClient.getString ( "managerServlet.deleteFail", dir ) );
                        return;
                    }
                    if ( xml.exists() && !xml.delete() ) {
                        writer.println ( smClient.getString ( "managerServlet.deleteFail", xml ) );
                        return;
                    }
                    this.check ( name );
                } finally {
                    this.removeServiced ( name );
                }
            }
            writer.println ( smClient.getString ( "managerServlet.undeployed", displayPath ) );
        } catch ( Throwable t2 ) {
            ExceptionUtils.handleThrowable ( t2 );
            this.log ( "ManagerServlet.undeploy[" + displayPath + "]", t2 );
            writer.println ( smClient.getString ( "managerServlet.exception", t2.toString() ) );
        }
    }
    protected boolean isDeployed ( final String name ) throws Exception {
        final String[] params = { name };
        final String[] signature = { "java.lang.String" };
        final Boolean result = ( Boolean ) this.mBeanServer.invoke ( this.oname, "isDeployed", params, signature );
        return result;
    }
    protected void check ( final String name ) throws Exception {
        final String[] params = { name };
        final String[] signature = { "java.lang.String" };
        this.mBeanServer.invoke ( this.oname, "check", params, signature );
    }
    protected boolean isServiced ( final String name ) throws Exception {
        final String[] params = { name };
        final String[] signature = { "java.lang.String" };
        final Boolean result = ( Boolean ) this.mBeanServer.invoke ( this.oname, "isServiced", params, signature );
        return result;
    }
    protected void addServiced ( final String name ) throws Exception {
        final String[] params = { name };
        final String[] signature = { "java.lang.String" };
        this.mBeanServer.invoke ( this.oname, "addServiced", params, signature );
    }
    protected void removeServiced ( final String name ) throws Exception {
        final String[] params = { name };
        final String[] signature = { "java.lang.String" };
        this.mBeanServer.invoke ( this.oname, "removeServiced", params, signature );
    }
    protected boolean undeployDir ( final File dir ) {
        String[] files = dir.list();
        if ( files == null ) {
            files = new String[0];
        }
        for ( int i = 0; i < files.length; ++i ) {
            final File file = new File ( dir, files[i] );
            if ( file.isDirectory() ) {
                if ( !this.undeployDir ( file ) ) {
                    return false;
                }
            } else if ( !file.delete() ) {
                return false;
            }
        }
        return dir.delete();
    }
    protected void uploadWar ( final PrintWriter writer, final HttpServletRequest request, final File war, final StringManager smClient ) throws IOException {
        if ( war.exists() && !war.delete() ) {
            final String msg = smClient.getString ( "managerServlet.deleteFail", war );
            throw new IOException ( msg );
        }
        try ( final ServletInputStream istream = request.getInputStream();
                    final BufferedOutputStream ostream = new BufferedOutputStream ( new FileOutputStream ( war ), 1024 ) ) {
            final byte[] buffer = new byte[1024];
            while ( true ) {
                final int n = istream.read ( buffer );
                if ( n < 0 ) {
                    break;
                }
                ostream.write ( buffer, 0, n );
            }
        } catch ( IOException e ) {
            if ( war.exists() && !war.delete() ) {
                writer.println ( smClient.getString ( "managerServlet.deleteFail", war ) );
            }
            throw e;
        }
    }
    protected static boolean validateContextName ( final ContextName cn, final PrintWriter writer, final StringManager sm ) {
        if ( cn != null && ( cn.getPath().startsWith ( "/" ) || cn.getPath().equals ( "" ) ) ) {
            return true;
        }
        String path = null;
        if ( cn != null ) {
            path = RequestUtil.filter ( cn.getPath() );
        }
        writer.println ( sm.getString ( "managerServlet.invalidPath", path ) );
        return false;
    }
    public static boolean copy ( final File src, final File dest ) {
        boolean result = false;
        try {
            if ( src != null && !src.getCanonicalPath().equals ( dest.getCanonicalPath() ) ) {
                result = copyInternal ( src, dest, new byte[4096] );
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        return result;
    }
    public static boolean copyInternal ( final File src, final File dest, final byte[] buf ) {
        boolean result = true;
        String[] files = null;
        if ( src.isDirectory() ) {
            files = src.list();
            result = dest.mkdir();
        } else {
            files = new String[] { "" };
        }
        if ( files == null ) {
            files = new String[0];
        }
        for ( int i = 0; i < files.length && result; ++i ) {
            final File fileSrc = new File ( src, files[i] );
            final File fileDest = new File ( dest, files[i] );
            if ( fileSrc.isDirectory() ) {
                result = copyInternal ( fileSrc, fileDest, buf );
            } else {
                try ( final FileInputStream is = new FileInputStream ( fileSrc );
                            final FileOutputStream os = new FileOutputStream ( fileDest ) ) {
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
        final Map<String, Set<String>> result = new HashMap<String, Set<String>>();
        final Engine e = ( Engine ) this.host.getParent();
        final Service s = e.getService();
        final Connector[] connectors2;
        final Connector[] connectors = connectors2 = s.findConnectors();
        for ( final Connector connector : connectors2 ) {
            if ( Boolean.TRUE.equals ( connector.getProperty ( "SSLEnabled" ) ) ) {
                final SSLHostConfig[] sslHostConfigs2;
                final SSLHostConfig[] sslHostConfigs = sslHostConfigs2 = connector.getProtocolHandler().findSslHostConfigs();
                for ( final SSLHostConfig sslHostConfig : sslHostConfigs2 ) {
                    final String name = connector.toString() + "-" + sslHostConfig.getHostName();
                    final Set<String> cipherList = new HashSet<String>();
                    final String[] enabledCiphers;
                    final String[] cipherNames = enabledCiphers = sslHostConfig.getEnabledCiphers();
                    for ( final String cipherName : enabledCiphers ) {
                        cipherList.add ( cipherName );
                    }
                    result.put ( name, cipherList );
                }
            } else {
                final Set<String> cipherList2 = new HashSet<String>();
                cipherList2.add ( ManagerServlet.sm.getString ( "managerServlet.notSslConnector" ) );
                result.put ( connector.toString(), cipherList2 );
            }
        }
        return result;
    }
    static {
        sm = StringManager.getManager ( "org.apache.catalina.manager" );
    }
}
