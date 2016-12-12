package org.apache.catalina.manager.host;
import javax.management.MBeanServer;
import javax.management.InstanceNotFoundException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import org.apache.catalina.core.ContainerBase;
import java.nio.file.Path;
import java.io.InputStream;
import org.apache.catalina.Container;
import java.util.StringTokenizer;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.startup.HostConfig;
import org.apache.catalina.core.StandardHost;
import java.nio.file.Files;
import java.nio.file.CopyOption;
import java.io.File;
import org.apache.tomcat.util.ExceptionUtils;
import javax.servlet.UnavailableException;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Enumeration;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.catalina.Wrapper;
import org.apache.tomcat.util.res.StringManager;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Context;
import org.apache.catalina.ContainerServlet;
import javax.servlet.http.HttpServlet;
public class HostManagerServlet extends HttpServlet implements ContainerServlet {
    private static final long serialVersionUID = 1L;
    protected transient Context context;
    protected int debug;
    protected transient Host installedHost;
    protected transient Engine engine;
    protected static final StringManager sm;
    protected transient Wrapper wrapper;
    public HostManagerServlet() {
        this.context = null;
        this.debug = 1;
        this.installedHost = null;
        this.engine = null;
        this.wrapper = null;
    }
    public Wrapper getWrapper() {
        return this.wrapper;
    }
    public void setWrapper ( final Wrapper wrapper ) {
        this.wrapper = wrapper;
        if ( wrapper == null ) {
            this.context = null;
            this.installedHost = null;
            this.engine = null;
        } else {
            this.context = ( Context ) wrapper.getParent();
            this.installedHost = ( Host ) this.context.getParent();
            this.engine = ( Engine ) this.installedHost.getParent();
        }
    }
    public void destroy() {
    }
    public void doGet ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException {
        final StringManager smClient = StringManager.getManager ( "org.apache.catalina.manager.host", request.getLocales() );
        String command = request.getPathInfo();
        if ( command == null ) {
            command = request.getServletPath();
        }
        final String name = request.getParameter ( "name" );
        response.setContentType ( "text/plain; charset=utf-8" );
        final PrintWriter writer = response.getWriter();
        if ( command == null ) {
            writer.println ( HostManagerServlet.sm.getString ( "hostManagerServlet.noCommand" ) );
        } else if ( command.equals ( "/add" ) ) {
            this.add ( request, writer, name, false, smClient );
        } else if ( command.equals ( "/remove" ) ) {
            this.remove ( writer, name, smClient );
        } else if ( command.equals ( "/list" ) ) {
            this.list ( writer, smClient );
        } else if ( command.equals ( "/start" ) ) {
            this.start ( writer, name, smClient );
        } else if ( command.equals ( "/stop" ) ) {
            this.stop ( writer, name, smClient );
        } else if ( command.equals ( "/persist" ) ) {
            this.persist ( writer, smClient );
        } else {
            writer.println ( HostManagerServlet.sm.getString ( "hostManagerServlet.unknownCommand", command ) );
        }
        writer.flush();
        writer.close();
    }
    protected void add ( final HttpServletRequest request, final PrintWriter writer, final String name, final boolean htmlMode, final StringManager smClient ) {
        final String aliases = request.getParameter ( "aliases" );
        final String appBase = request.getParameter ( "appBase" );
        final boolean manager = this.booleanParameter ( request, "manager", false, htmlMode );
        final boolean autoDeploy = this.booleanParameter ( request, "autoDeploy", true, htmlMode );
        final boolean deployOnStartup = this.booleanParameter ( request, "deployOnStartup", true, htmlMode );
        final boolean deployXML = this.booleanParameter ( request, "deployXML", true, htmlMode );
        final boolean unpackWARs = this.booleanParameter ( request, "unpackWARs", true, htmlMode );
        final boolean copyXML = this.booleanParameter ( request, "copyXML", false, htmlMode );
        this.add ( writer, name, aliases, appBase, manager, autoDeploy, deployOnStartup, deployXML, unpackWARs, copyXML, smClient );
    }
    protected boolean booleanParameter ( final HttpServletRequest request, final String parameter, final boolean theDefault, final boolean htmlMode ) {
        final String value = request.getParameter ( parameter );
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
    public void init() throws ServletException {
        if ( this.wrapper == null || this.context == null ) {
            throw new UnavailableException ( HostManagerServlet.sm.getString ( "hostManagerServlet.noWrapper" ) );
        }
        String value = null;
        try {
            value = this.getServletConfig().getInitParameter ( "debug" );
            this.debug = Integer.parseInt ( value );
        } catch ( Throwable t ) {
            ExceptionUtils.handleThrowable ( t );
        }
    }
    protected synchronized void add ( final PrintWriter writer, final String name, final String aliases, final String appBase, final boolean manager, final boolean autoDeploy, final boolean deployOnStartup, final boolean deployXML, final boolean unpackWARs, final boolean copyXML, final StringManager smClient ) {
        if ( this.debug >= 1 ) {
            this.log ( HostManagerServlet.sm.getString ( "hostManagerServlet.add", name ) );
        }
        if ( name == null || name.length() == 0 ) {
            writer.println ( smClient.getString ( "hostManagerServlet.invalidHostName", name ) );
            return;
        }
        if ( this.engine.findChild ( name ) != null ) {
            writer.println ( smClient.getString ( "hostManagerServlet.alreadyHost", name ) );
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
            file = new File ( this.engine.getCatalinaBase(), file.getPath() );
        }
        try {
            appBaseFile = file.getCanonicalFile();
        } catch ( IOException e2 ) {
            appBaseFile = file;
        }
        if ( !appBaseFile.mkdirs() && !appBaseFile.isDirectory() ) {
            writer.println ( smClient.getString ( "hostManagerServlet.appBaseCreateFail", appBaseFile.toString(), name ) );
            return;
        }
        final File configBaseFile = this.getConfigBase ( name );
        if ( manager ) {
            if ( configBaseFile == null ) {
                writer.println ( smClient.getString ( "hostManagerServlet.configBaseCreateFail", name ) );
                return;
            }
            try ( final InputStream is = this.getServletContext().getResourceAsStream ( "/manager.xml" ) ) {
                final Path dest = new File ( configBaseFile, "manager.xml" ).toPath();
                Files.copy ( is, dest, new CopyOption[0] );
            } catch ( IOException e3 ) {
                writer.println ( smClient.getString ( "hostManagerServlet.managerXml" ) );
                return;
            }
        }
        StandardHost host = new StandardHost();
        host.setAppBase ( applicationBase );
        host.setName ( name );
        host.addLifecycleListener ( new HostConfig() );
        if ( aliases != null && !"".equals ( aliases ) ) {
            final StringTokenizer tok = new StringTokenizer ( aliases, ", " );
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
            this.engine.addChild ( host );
        } catch ( Exception e ) {
            writer.println ( smClient.getString ( "hostManagerServlet.exception", e.toString() ) );
            return;
        }
        host = ( StandardHost ) this.engine.findChild ( name );
        if ( host != null ) {
            writer.println ( smClient.getString ( "hostManagerServlet.add", name ) );
        } else {
            writer.println ( smClient.getString ( "hostManagerServlet.addFailed", name ) );
        }
    }
    protected synchronized void remove ( final PrintWriter writer, final String name, final StringManager smClient ) {
        if ( this.debug >= 1 ) {
            this.log ( HostManagerServlet.sm.getString ( "hostManagerServlet.remove", name ) );
        }
        if ( name == null || name.length() == 0 ) {
            writer.println ( smClient.getString ( "hostManagerServlet.invalidHostName", name ) );
            return;
        }
        if ( this.engine.findChild ( name ) == null ) {
            writer.println ( smClient.getString ( "hostManagerServlet.noHost", name ) );
            return;
        }
        if ( this.engine.findChild ( name ) == this.installedHost ) {
            writer.println ( smClient.getString ( "hostManagerServlet.cannotRemoveOwnHost", name ) );
            return;
        }
        try {
            final Container child = this.engine.findChild ( name );
            this.engine.removeChild ( child );
            if ( child instanceof ContainerBase ) {
                ( ( ContainerBase ) child ).destroy();
            }
        } catch ( Exception e ) {
            writer.println ( smClient.getString ( "hostManagerServlet.exception", e.toString() ) );
            return;
        }
        final Host host = ( StandardHost ) this.engine.findChild ( name );
        if ( host == null ) {
            writer.println ( smClient.getString ( "hostManagerServlet.remove", name ) );
        } else {
            writer.println ( smClient.getString ( "hostManagerServlet.removeFailed", name ) );
        }
    }
    protected void list ( final PrintWriter writer, final StringManager smClient ) {
        if ( this.debug >= 1 ) {
            this.log ( HostManagerServlet.sm.getString ( "hostManagerServlet.list", this.engine.getName() ) );
        }
        writer.println ( smClient.getString ( "hostManagerServlet.listed", this.engine.getName() ) );
        final Container[] hosts = this.engine.findChildren();
        for ( int i = 0; i < hosts.length; ++i ) {
            final Host host = ( Host ) hosts[i];
            final String name = host.getName();
            final String[] aliases = host.findAliases();
            final StringBuilder buf = new StringBuilder();
            if ( aliases.length > 0 ) {
                buf.append ( aliases[0] );
                for ( int j = 1; j < aliases.length; ++j ) {
                    buf.append ( ',' ).append ( aliases[j] );
                }
            }
            writer.println ( smClient.getString ( "hostManagerServlet.listitem", name, buf.toString() ) );
        }
    }
    protected void start ( final PrintWriter writer, final String name, final StringManager smClient ) {
        if ( this.debug >= 1 ) {
            this.log ( HostManagerServlet.sm.getString ( "hostManagerServlet.start", name ) );
        }
        if ( name == null || name.length() == 0 ) {
            writer.println ( smClient.getString ( "hostManagerServlet.invalidHostName", name ) );
            return;
        }
        final Container host = this.engine.findChild ( name );
        if ( host == null ) {
            writer.println ( smClient.getString ( "hostManagerServlet.noHost", name ) );
            return;
        }
        if ( host == this.installedHost ) {
            writer.println ( smClient.getString ( "hostManagerServlet.cannotStartOwnHost", name ) );
            return;
        }
        if ( host.getState().isAvailable() ) {
            writer.println ( smClient.getString ( "hostManagerServlet.alreadyStarted", name ) );
            return;
        }
        try {
            host.start();
            writer.println ( smClient.getString ( "hostManagerServlet.started", name ) );
        } catch ( Exception e ) {
            this.getServletContext().log ( HostManagerServlet.sm.getString ( "hostManagerServlet.startFailed", name ), ( Throwable ) e );
            writer.println ( smClient.getString ( "hostManagerServlet.startFailed", name ) );
            writer.println ( smClient.getString ( "hostManagerServlet.exception", e.toString() ) );
        }
    }
    protected void stop ( final PrintWriter writer, final String name, final StringManager smClient ) {
        if ( this.debug >= 1 ) {
            this.log ( HostManagerServlet.sm.getString ( "hostManagerServlet.stop", name ) );
        }
        if ( name == null || name.length() == 0 ) {
            writer.println ( smClient.getString ( "hostManagerServlet.invalidHostName", name ) );
            return;
        }
        final Container host = this.engine.findChild ( name );
        if ( host == null ) {
            writer.println ( smClient.getString ( "hostManagerServlet.noHost", name ) );
            return;
        }
        if ( host == this.installedHost ) {
            writer.println ( smClient.getString ( "hostManagerServlet.cannotStopOwnHost", name ) );
            return;
        }
        if ( !host.getState().isAvailable() ) {
            writer.println ( smClient.getString ( "hostManagerServlet.alreadyStopped", name ) );
            return;
        }
        try {
            host.stop();
            writer.println ( smClient.getString ( "hostManagerServlet.stopped", name ) );
        } catch ( Exception e ) {
            this.getServletContext().log ( HostManagerServlet.sm.getString ( "hostManagerServlet.stopFailed", name ), ( Throwable ) e );
            writer.println ( smClient.getString ( "hostManagerServlet.stopFailed", name ) );
            writer.println ( smClient.getString ( "hostManagerServlet.exception", e.toString() ) );
        }
    }
    protected void persist ( final PrintWriter writer, final StringManager smClient ) {
        if ( this.debug >= 1 ) {
            this.log ( HostManagerServlet.sm.getString ( "hostManagerServlet.persist" ) );
        }
        try {
            final MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
            final ObjectName oname = new ObjectName ( this.engine.getDomain() + ":type=StoreConfig" );
            platformMBeanServer.invoke ( oname, "storeConfig", null, null );
            writer.println ( smClient.getString ( "hostManagerServlet.persisted" ) );
        } catch ( Exception e ) {
            this.getServletContext().log ( HostManagerServlet.sm.getString ( "hostManagerServlet.persistFailed" ), ( Throwable ) e );
            writer.println ( smClient.getString ( "hostManagerServlet.persistFailed" ) );
            if ( e instanceof InstanceNotFoundException ) {
                writer.println ( "Please enable StoreConfig to use this feature." );
            } else {
                writer.println ( smClient.getString ( "hostManagerServlet.exception", e.toString() ) );
            }
        }
    }
    protected File getConfigBase ( final String hostName ) {
        File configBase = new File ( this.context.getCatalinaBase(), "conf" );
        if ( !configBase.exists() ) {
            return null;
        }
        if ( this.engine != null ) {
            configBase = new File ( configBase, this.engine.getName() );
        }
        if ( this.installedHost != null ) {
            configBase = new File ( configBase, hostName );
        }
        if ( !configBase.mkdirs() && !configBase.isDirectory() ) {
            return null;
        }
        return configBase;
    }
    static {
        sm = StringManager.getManager ( "org.apache.catalina.manager.host" );
    }
}
