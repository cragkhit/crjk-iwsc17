package org.apache.catalina.manager;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import java.io.IOException;
import java.util.Enumeration;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.net.InetAddress;
import org.apache.catalina.util.ServerInfo;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import java.util.Iterator;
import java.util.Set;
import javax.management.NotificationFilter;
import javax.management.ObjectInstance;
import javax.management.QueryExp;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.res.StringManager;
import javax.management.ObjectName;
import java.util.Vector;
import javax.management.MBeanServer;
import javax.management.NotificationListener;
import javax.servlet.http.HttpServlet;
public class StatusManagerServlet extends HttpServlet implements NotificationListener {
    private static final long serialVersionUID = 1L;
    protected MBeanServer mBeanServer;
    protected final Vector<ObjectName> protocolHandlers;
    protected final Vector<ObjectName> threadPools;
    protected final Vector<ObjectName> requestProcessors;
    protected final Vector<ObjectName> globalRequestProcessors;
    protected static final StringManager sm;
    public StatusManagerServlet() {
        this.mBeanServer = null;
        this.protocolHandlers = new Vector<ObjectName>();
        this.threadPools = new Vector<ObjectName>();
        this.requestProcessors = new Vector<ObjectName>();
        this.globalRequestProcessors = new Vector<ObjectName>();
    }
    public void init() throws ServletException {
        this.mBeanServer = Registry.getRegistry ( null, null ).getMBeanServer();
        try {
            String onStr = "*:type=ProtocolHandler,*";
            ObjectName objectName = new ObjectName ( onStr );
            Set<ObjectInstance> set = this.mBeanServer.queryMBeans ( objectName, null );
            for ( final ObjectInstance oi : set ) {
                this.protocolHandlers.addElement ( oi.getObjectName() );
            }
            onStr = "*:type=ThreadPool,*";
            objectName = new ObjectName ( onStr );
            set = this.mBeanServer.queryMBeans ( objectName, null );
            for ( final ObjectInstance oi : set ) {
                this.threadPools.addElement ( oi.getObjectName() );
            }
            onStr = "*:type=GlobalRequestProcessor,*";
            objectName = new ObjectName ( onStr );
            set = this.mBeanServer.queryMBeans ( objectName, null );
            for ( final ObjectInstance oi : set ) {
                this.globalRequestProcessors.addElement ( oi.getObjectName() );
            }
            onStr = "*:type=RequestProcessor,*";
            objectName = new ObjectName ( onStr );
            set = this.mBeanServer.queryMBeans ( objectName, null );
            for ( final ObjectInstance oi : set ) {
                this.requestProcessors.addElement ( oi.getObjectName() );
            }
            onStr = "JMImplementation:type=MBeanServerDelegate";
            objectName = new ObjectName ( onStr );
            this.mBeanServer.addNotificationListener ( objectName, this, null, null );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
    public void destroy() {
        final String onStr = "JMImplementation:type=MBeanServerDelegate";
        try {
            final ObjectName objectName = new ObjectName ( onStr );
            this.mBeanServer.removeNotificationListener ( objectName, this, null, null );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
    public void doGet ( final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException {
        int mode = 0;
        if ( request.getParameter ( "XML" ) != null && request.getParameter ( "XML" ).equals ( "true" ) ) {
            mode = 1;
        }
        StatusTransformer.setContentType ( response, mode );
        final PrintWriter writer = response.getWriter();
        boolean completeStatus = false;
        if ( request.getPathInfo() != null && request.getPathInfo().equals ( "/all" ) ) {
            completeStatus = true;
        }
        Object[] args = { request.getContextPath() };
        StatusTransformer.writeHeader ( writer, args, mode );
        args = new Object[] { request.getContextPath(), null };
        if ( completeStatus ) {
            args[1] = StatusManagerServlet.sm.getString ( "statusServlet.complete" );
        } else {
            args[1] = StatusManagerServlet.sm.getString ( "statusServlet.title" );
        }
        StatusTransformer.writeBody ( writer, args, mode );
        args = new Object[] { StatusManagerServlet.sm.getString ( "htmlManagerServlet.manager" ), response.encodeURL ( request.getContextPath() + "/html/list" ), StatusManagerServlet.sm.getString ( "htmlManagerServlet.list" ), response.encodeURL ( request.getContextPath() + "/" + StatusManagerServlet.sm.getString ( "htmlManagerServlet.helpHtmlManagerFile" ) ), StatusManagerServlet.sm.getString ( "htmlManagerServlet.helpHtmlManager" ), response.encodeURL ( request.getContextPath() + "/" + StatusManagerServlet.sm.getString ( "htmlManagerServlet.helpManagerFile" ) ), StatusManagerServlet.sm.getString ( "htmlManagerServlet.helpManager" ), null, null };
        if ( completeStatus ) {
            args[7] = response.encodeURL ( request.getContextPath() + "/status" );
            args[8] = StatusManagerServlet.sm.getString ( "statusServlet.title" );
        } else {
            args[7] = response.encodeURL ( request.getContextPath() + "/status/all" );
            args[8] = StatusManagerServlet.sm.getString ( "statusServlet.complete" );
        }
        StatusTransformer.writeManager ( writer, args, mode );
        args = new Object[] { StatusManagerServlet.sm.getString ( "htmlManagerServlet.serverTitle" ), StatusManagerServlet.sm.getString ( "htmlManagerServlet.serverVersion" ), StatusManagerServlet.sm.getString ( "htmlManagerServlet.serverJVMVersion" ), StatusManagerServlet.sm.getString ( "htmlManagerServlet.serverJVMVendor" ), StatusManagerServlet.sm.getString ( "htmlManagerServlet.serverOSName" ), StatusManagerServlet.sm.getString ( "htmlManagerServlet.serverOSVersion" ), StatusManagerServlet.sm.getString ( "htmlManagerServlet.serverOSArch" ), StatusManagerServlet.sm.getString ( "htmlManagerServlet.serverHostname" ), StatusManagerServlet.sm.getString ( "htmlManagerServlet.serverIPAddress" ) };
        StatusTransformer.writePageHeading ( writer, args, mode );
        args = new Object[] { ServerInfo.getServerInfo(), System.getProperty ( "java.runtime.version" ), System.getProperty ( "java.vm.vendor" ), System.getProperty ( "os.name" ), System.getProperty ( "os.version" ), System.getProperty ( "os.arch" ), null, null };
        try {
            final InetAddress address = InetAddress.getLocalHost();
            args[6] = address.getHostName();
            args[7] = address.getHostAddress();
        } catch ( UnknownHostException e2 ) {
            args[7] = ( args[6] = "-" );
        }
        StatusTransformer.writeServerInfo ( writer, args, mode );
        try {
            StatusTransformer.writeOSState ( writer, mode );
            StatusTransformer.writeVMState ( writer, mode );
            final Enumeration<ObjectName> enumeration = this.threadPools.elements();
            while ( enumeration.hasMoreElements() ) {
                final ObjectName objectName = enumeration.nextElement();
                final String name = objectName.getKeyProperty ( "name" );
                StatusTransformer.writeConnectorState ( writer, objectName, name, this.mBeanServer, this.globalRequestProcessors, this.requestProcessors, mode );
            }
            if ( request.getPathInfo() != null && request.getPathInfo().equals ( "/all" ) ) {
                StatusTransformer.writeDetailedState ( writer, this.mBeanServer, mode );
            }
        } catch ( Exception e ) {
            throw new ServletException ( ( Throwable ) e );
        }
        StatusTransformer.writeFooter ( writer, mode );
    }
    public void handleNotification ( final Notification notification, final Object handback ) {
        if ( notification instanceof MBeanServerNotification ) {
            final ObjectName objectName = ( ( MBeanServerNotification ) notification ).getMBeanName();
            if ( notification.getType().equals ( "JMX.mbean.registered" ) ) {
                final String type = objectName.getKeyProperty ( "type" );
                if ( type != null ) {
                    if ( type.equals ( "ProtocolHandler" ) ) {
                        this.protocolHandlers.addElement ( objectName );
                    } else if ( type.equals ( "ThreadPool" ) ) {
                        this.threadPools.addElement ( objectName );
                    } else if ( type.equals ( "GlobalRequestProcessor" ) ) {
                        this.globalRequestProcessors.addElement ( objectName );
                    } else if ( type.equals ( "RequestProcessor" ) ) {
                        this.requestProcessors.addElement ( objectName );
                    }
                }
            } else if ( notification.getType().equals ( "JMX.mbean.unregistered" ) ) {
                final String type = objectName.getKeyProperty ( "type" );
                if ( type != null ) {
                    if ( type.equals ( "ProtocolHandler" ) ) {
                        this.protocolHandlers.removeElement ( objectName );
                    } else if ( type.equals ( "ThreadPool" ) ) {
                        this.threadPools.removeElement ( objectName );
                    } else if ( type.equals ( "GlobalRequestProcessor" ) ) {
                        this.globalRequestProcessors.removeElement ( objectName );
                    } else if ( type.equals ( "RequestProcessor" ) ) {
                        this.requestProcessors.removeElement ( objectName );
                    }
                }
                final String j2eeType = objectName.getKeyProperty ( "j2eeType" );
                if ( j2eeType != null ) {}
            }
        }
    }
    static {
        sm = StringManager.getManager ( "org.apache.catalina.manager" );
    }
}
