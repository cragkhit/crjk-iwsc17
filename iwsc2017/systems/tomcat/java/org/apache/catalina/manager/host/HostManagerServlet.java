package org.apache.catalina.manager.host;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringTokenizer;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerServlet;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.HostConfig;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.res.StringManager;
public class HostManagerServlet
    extends HttpServlet implements ContainerServlet {
    private static final long serialVersionUID = 1L;
    protected transient Context context = null;
    protected int debug = 1;
    protected transient Host installedHost = null;
    protected transient Engine engine = null;
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
            installedHost = null;
            engine = null;
        } else {
            context = ( Context ) wrapper.getParent();
            installedHost = ( Host ) context.getParent();
            engine = ( Engine ) installedHost.getParent();
        }
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
        String name = request.getParameter ( "name" );
        response.setContentType ( "text/plain; charset=" + Constants.CHARSET );
        PrintWriter writer = response.getWriter();
        if ( command == null ) {
            writer.println ( sm.getString ( "hostManagerServlet.noCommand" ) );
        } else if ( command.equals ( "/add" ) ) {
            add ( request, writer, name, false, smClient );
        } else if ( command.equals ( "/remove" ) ) {
            remove ( writer, name, smClient );
        } else if ( command.equals ( "/list" ) ) {
            list ( writer, smClient );
        } else if ( command.equals ( "/start" ) ) {
            start ( writer, name, smClient );
        } else if ( command.equals ( "/stop" ) ) {
            stop ( writer, name, smClient );
        } else if ( command.equals ( "/persist" ) ) {
            persist ( writer, smClient );
        } else {
            writer.println ( sm.getString ( "hostManagerServlet.unknownCommand",
                                            command ) );
        }
        writer.flush();
        writer.close();
    }
    protected void add ( HttpServletRequest request, PrintWriter writer,
                         String name, boolean htmlMode, StringManager smClient ) {
        String aliases = request.getParameter ( "aliases" );
        String appBase = request.getParameter ( "appBase" );
        boolean manager = booleanParameter ( request, "manager", false, htmlMode );
        boolean autoDeploy = booleanParameter ( request, "autoDeploy", true, htmlMode );
        boolean deployOnStartup = booleanParameter ( request, "deployOnStartup", true, htmlMode );
        boolean deployXML = booleanParameter ( request, "deployXML", true, htmlMode );
        boolean unpackWARs = booleanParameter ( request, "unpackWARs", true, htmlMode );
        boolean copyXML = booleanParameter ( request, "copyXML", false, htmlMode );
        add ( writer, name, aliases, appBase, manager,
              autoDeploy,
              deployOnStartup,
              deployXML,
              unpackWARs,
              copyXML,
              smClient );
    }
    protected boolean booleanParameter ( HttpServletRequest request,
                                         String parameter, boolean theDefault, boolean htmlMode ) {
        String value = request.getParameter ( parameter );
        boolean booleanValue = theDefault;
        if ( value != null ) {
            if ( htmlMode ) {
                if ( value.equals ( "on" ) ) {
                    booleanValue = true;
                }
            } else if ( theDefault ) {
                if ( value.equals ( "false" ) ) {
                    booleanValue = false;
                }
            } else if ( value.equals ( "true" ) ) {
                booleanValue = true;
            }
        } else if ( htmlMode ) {
            booleanValue = false;
        }
        return booleanValue;
    }
    @Override
    public void init() throws ServletException {
        if ( ( wrapper == null ) || ( context == null ) )
            throw new UnavailableException
            ( sm.getString ( "hostManagerServlet.noWrapper" ) );
        String value = null;
        try {
            value = getServletConfig().getInitParameter ( "debug" );
            debug = Integer.parseInt ( value );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
        }
    }
    protected synchronized void add
    ( PrintWriter writer, String name, String aliases, String appBase,
      boolean manager,
      boolean autoDeploy,
      boolean deployOnStartup,
      boolean deployXML,
      boolean unpackWARs,
      boolean copyXML,
      StringManager smClient ) {
        if ( debug >= 1 ) {
            log ( sm.getString ( "hostManagerServlet.add", name ) );
        }
        if ( ( name == null ) || name.length() == 0 ) {
            writer.println ( smClient.getString (
                                 "hostManagerServlet.invalidHostName", name ) );
            return;
        }
        if ( engine.findChild ( name ) != null ) {
            writer.println ( smClient.getString (
                                 "hostManagerServlet.alreadyHost", name ) );
            return;
        }
        File appBaseFile = null;
        File file = null;
        String applicationBase = appBase;
        if ( applicationBase == null || applicationBase.length() == 0 ) {
            applicationBase = name;
        }
        file = new File ( applicationBase );
        if ( !file.isAbsolute() ) {
            file = new File ( engine.getCatalinaBase(), file.getPath() );
        }
        try {
            appBaseFile = file.getCanonicalFile();
        } catch ( IOException e ) {
            appBaseFile = file;
        }
        if ( !appBaseFile.mkdirs() && !appBaseFile.isDirectory() ) {
            writer.println ( smClient.getString (
                                 "hostManagerServlet.appBaseCreateFail",
                                 appBaseFile.toString(), name ) );
            return;
        }
        File configBaseFile = getConfigBase ( name );
        if ( manager ) {
            if ( configBaseFile == null ) {
                writer.println ( smClient.getString (
                                     "hostManagerServlet.configBaseCreateFail", name ) );
                return;
            }
            try ( InputStream is = getServletContext().getResourceAsStream ( "/manager.xml" ) ) {
                Path dest = ( new File ( configBaseFile, "manager.xml" ) ).toPath();
                Files.copy ( is, dest );
            } catch ( IOException e ) {
                writer.println ( smClient.getString ( "hostManagerServlet.managerXml" ) );
                return;
            }
        }
        StandardHost host = new StandardHost();
        host.setAppBase ( applicationBase );
        host.setName ( name );
        host.addLifecycleListener ( new HostConfig() );
        if ( ( aliases != null ) && ! ( "".equals ( aliases ) ) ) {
            StringTokenizer tok = new StringTokenizer ( aliases, ", " );
            while ( tok.hasMoreTokens() ) {
                host.addAlias ( tok.nextToken() );
            }
        }
        host.setAutoDeploy ( autoDeploy );
        host.setDeployOnStartup ( deployOnStartup );
        host.setDeployXML ( deployXML );
        host.setUnpackWARs ( unpackWARs );
        host.setCopyXML ( copyXML );
        try {
            engine.addChild ( host );
        } catch ( Exception e ) {
            writer.println ( smClient.getString ( "hostManagerServlet.exception",
                                                  e.toString() ) );
            return;
        }
        host = ( StandardHost ) engine.findChild ( name );
        if ( host != null ) {
            writer.println ( smClient.getString ( "hostManagerServlet.add", name ) );
        } else {
            writer.println ( smClient.getString (
                                 "hostManagerServlet.addFailed", name ) );
        }
    }
    protected synchronized void remove ( PrintWriter writer, String name,
                                         StringManager smClient ) {
        if ( debug >= 1 ) {
            log ( sm.getString ( "hostManagerServlet.remove", name ) );
        }
        if ( ( name == null ) || name.length() == 0 ) {
            writer.println ( smClient.getString (
                                 "hostManagerServlet.invalidHostName", name ) );
            return;
        }
        if ( engine.findChild ( name ) == null ) {
            writer.println ( smClient.getString (
                                 "hostManagerServlet.noHost", name ) );
            return;
        }
        if ( engine.findChild ( name ) == installedHost ) {
            writer.println ( smClient.getString (
                                 "hostManagerServlet.cannotRemoveOwnHost", name ) );
            return;
        }
        try {
            Container child = engine.findChild ( name );
            engine.removeChild ( child );
            if ( child instanceof ContainerBase ) {
                ( ( ContainerBase ) child ).destroy();
            }
        } catch ( Exception e ) {
            writer.println ( smClient.getString ( "hostManagerServlet.exception",
                                                  e.toString() ) );
            return;
        }
        Host host = ( StandardHost ) engine.findChild ( name );
        if ( host == null ) {
            writer.println ( smClient.getString (
                                 "hostManagerServlet.remove", name ) );
        } else {
            writer.println ( smClient.getString (
                                 "hostManagerServlet.removeFailed", name ) );
        }
    }
    protected void list ( PrintWriter writer, StringManager smClient ) {
        if ( debug >= 1 ) {
            log ( sm.getString ( "hostManagerServlet.list", engine.getName() ) );
        }
        writer.println ( smClient.getString ( "hostManagerServlet.listed",
                                              engine.getName() ) );
        Container[] hosts = engine.findChildren();
        for ( int i = 0; i < hosts.length; i++ ) {
            Host host = ( Host ) hosts[i];
            String name = host.getName();
            String[] aliases = host.findAliases();
            StringBuilder buf = new StringBuilder();
            if ( aliases.length > 0 ) {
                buf.append ( aliases[0] );
                for ( int j = 1; j < aliases.length; j++ ) {
                    buf.append ( ',' ).append ( aliases[j] );
                }
            }
            writer.println ( smClient.getString ( "hostManagerServlet.listitem",
                                                  name, buf.toString() ) );
        }
    }
    protected void start ( PrintWriter writer, String name,
                           StringManager smClient ) {
        if ( debug >= 1 ) {
            log ( sm.getString ( "hostManagerServlet.start", name ) );
        }
        if ( ( name == null ) || name.length() == 0 ) {
            writer.println ( smClient.getString (
                                 "hostManagerServlet.invalidHostName", name ) );
            return;
        }
        Container host = engine.findChild ( name );
        if ( host == null ) {
            writer.println ( smClient.getString (
                                 "hostManagerServlet.noHost", name ) );
            return;
        }
        if ( host == installedHost ) {
            writer.println ( smClient.getString (
                                 "hostManagerServlet.cannotStartOwnHost", name ) );
            return;
        }
        if ( host.getState().isAvailable() ) {
            writer.println ( smClient.getString (
                                 "hostManagerServlet.alreadyStarted", name ) );
            return;
        }
        try {
            host.start();
            writer.println ( smClient.getString (
                                 "hostManagerServlet.started", name ) );
        } catch ( Exception e ) {
            getServletContext().log
            ( sm.getString ( "hostManagerServlet.startFailed", name ), e );
            writer.println ( smClient.getString (
                                 "hostManagerServlet.startFailed", name ) );
            writer.println ( smClient.getString (
                                 "hostManagerServlet.exception", e.toString() ) );
            return;
        }
    }
    protected void stop ( PrintWriter writer, String name,
                          StringManager smClient ) {
        if ( debug >= 1 ) {
            log ( sm.getString ( "hostManagerServlet.stop", name ) );
        }
        if ( ( name == null ) || name.length() == 0 ) {
            writer.println ( smClient.getString (
                                 "hostManagerServlet.invalidHostName", name ) );
            return;
        }
        Container host = engine.findChild ( name );
        if ( host == null ) {
            writer.println ( smClient.getString ( "hostManagerServlet.noHost",
                                                  name ) );
            return;
        }
        if ( host == installedHost ) {
            writer.println ( smClient.getString (
                                 "hostManagerServlet.cannotStopOwnHost", name ) );
            return;
        }
        if ( !host.getState().isAvailable() ) {
            writer.println ( smClient.getString (
                                 "hostManagerServlet.alreadyStopped", name ) );
            return;
        }
        try {
            host.stop();
            writer.println ( smClient.getString ( "hostManagerServlet.stopped",
                                                  name ) );
        } catch ( Exception e ) {
            getServletContext().log ( sm.getString (
                                          "hostManagerServlet.stopFailed", name ), e );
            writer.println ( smClient.getString ( "hostManagerServlet.stopFailed",
                                                  name ) );
            writer.println ( smClient.getString ( "hostManagerServlet.exception",
                                                  e.toString() ) );
            return;
        }
    }
    protected void persist ( PrintWriter writer, StringManager smClient ) {
        if ( debug >= 1 ) {
            log ( sm.getString ( "hostManagerServlet.persist" ) );
        }
        try {
            MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName oname = new ObjectName ( engine.getDomain() + ":type=StoreConfig" );
            platformMBeanServer.invoke ( oname, "storeConfig", null, null );
            writer.println ( smClient.getString ( "hostManagerServlet.persisted" ) );
        } catch ( Exception e ) {
            getServletContext().log ( sm.getString ( "hostManagerServlet.persistFailed" ), e );
            writer.println ( smClient.getString ( "hostManagerServlet.persistFailed" ) );
            if ( e instanceof InstanceNotFoundException ) {
                writer.println ( "Please enable StoreConfig to use this feature." );
            } else {
                writer.println ( smClient.getString ( "hostManagerServlet.exception", e.toString() ) );
            }
            return;
        }
    }
    protected File getConfigBase ( String hostName ) {
        File configBase = new File ( context.getCatalinaBase(), "conf" );
        if ( !configBase.exists() ) {
            return null;
        }
        if ( engine != null ) {
            configBase = new File ( configBase, engine.getName() );
        }
        if ( installedHost != null ) {
            configBase = new File ( configBase, hostName );
        }
        if ( !configBase.mkdirs() && !configBase.isDirectory() ) {
            return null;
        }
        return configBase;
    }
}
