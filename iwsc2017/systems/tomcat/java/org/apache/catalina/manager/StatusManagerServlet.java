package org.apache.catalina.manager;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import javax.management.MBeanServer;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.catalina.util.ServerInfo;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.res.StringManager;
public class StatusManagerServlet
    extends HttpServlet implements NotificationListener {
    private static final long serialVersionUID = 1L;
    protected MBeanServer mBeanServer = null;
    protected final Vector<ObjectName> protocolHandlers = new Vector<>();
    protected final Vector<ObjectName> threadPools = new Vector<>();
    protected final Vector<ObjectName> requestProcessors = new Vector<>();
    protected final Vector<ObjectName> globalRequestProcessors = new Vector<>();
    protected static final StringManager sm =
        StringManager.getManager ( Constants.Package );
    @Override
    public void init() throws ServletException {
        mBeanServer = Registry.getRegistry ( null, null ).getMBeanServer();
        try {
            String onStr = "*:type=ProtocolHandler,*";
            ObjectName objectName = new ObjectName ( onStr );
            Set<ObjectInstance> set = mBeanServer.queryMBeans ( objectName, null );
            Iterator<ObjectInstance> iterator = set.iterator();
            while ( iterator.hasNext() ) {
                ObjectInstance oi = iterator.next();
                protocolHandlers.addElement ( oi.getObjectName() );
            }
            onStr = "*:type=ThreadPool,*";
            objectName = new ObjectName ( onStr );
            set = mBeanServer.queryMBeans ( objectName, null );
            iterator = set.iterator();
            while ( iterator.hasNext() ) {
                ObjectInstance oi = iterator.next();
                threadPools.addElement ( oi.getObjectName() );
            }
            onStr = "*:type=GlobalRequestProcessor,*";
            objectName = new ObjectName ( onStr );
            set = mBeanServer.queryMBeans ( objectName, null );
            iterator = set.iterator();
            while ( iterator.hasNext() ) {
                ObjectInstance oi = iterator.next();
                globalRequestProcessors.addElement ( oi.getObjectName() );
            }
            onStr = "*:type=RequestProcessor,*";
            objectName = new ObjectName ( onStr );
            set = mBeanServer.queryMBeans ( objectName, null );
            iterator = set.iterator();
            while ( iterator.hasNext() ) {
                ObjectInstance oi = iterator.next();
                requestProcessors.addElement ( oi.getObjectName() );
            }
            onStr = "JMImplementation:type=MBeanServerDelegate";
            objectName = new ObjectName ( onStr );
            mBeanServer.addNotificationListener ( objectName, this, null, null );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
    @Override
    public void destroy() {
        String onStr = "JMImplementation:type=MBeanServerDelegate";
        ObjectName objectName;
        try {
            objectName = new ObjectName ( onStr );
            mBeanServer.removeNotificationListener ( objectName, this, null, null );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
    @Override
    public void doGet ( HttpServletRequest request,
                        HttpServletResponse response )
    throws IOException, ServletException {
        int mode = 0;
        if ( request.getParameter ( "XML" ) != null
                && request.getParameter ( "XML" ).equals ( "true" ) ) {
            mode = 1;
        }
        StatusTransformer.setContentType ( response, mode );
        PrintWriter writer = response.getWriter();
        boolean completeStatus = false;
        if ( ( request.getPathInfo() != null )
                && ( request.getPathInfo().equals ( "/all" ) ) ) {
            completeStatus = true;
        }
        Object[] args = new Object[1];
        args[0] = request.getContextPath();
        StatusTransformer.writeHeader ( writer, args, mode );
        args = new Object[2];
        args[0] = request.getContextPath();
        if ( completeStatus ) {
            args[1] = sm.getString ( "statusServlet.complete" );
        } else {
            args[1] = sm.getString ( "statusServlet.title" );
        }
        StatusTransformer.writeBody ( writer, args, mode );
        args = new Object[9];
        args[0] = sm.getString ( "htmlManagerServlet.manager" );
        args[1] = response.encodeURL ( request.getContextPath() + "/html/list" );
        args[2] = sm.getString ( "htmlManagerServlet.list" );
        args[3] = response.encodeURL
                  ( request.getContextPath() + "/" +
                    sm.getString ( "htmlManagerServlet.helpHtmlManagerFile" ) );
        args[4] = sm.getString ( "htmlManagerServlet.helpHtmlManager" );
        args[5] = response.encodeURL
                  ( request.getContextPath() + "/" +
                    sm.getString ( "htmlManagerServlet.helpManagerFile" ) );
        args[6] = sm.getString ( "htmlManagerServlet.helpManager" );
        if ( completeStatus ) {
            args[7] = response.encodeURL
                      ( request.getContextPath() + "/status" );
            args[8] = sm.getString ( "statusServlet.title" );
        } else {
            args[7] = response.encodeURL
                      ( request.getContextPath() + "/status/all" );
            args[8] = sm.getString ( "statusServlet.complete" );
        }
        StatusTransformer.writeManager ( writer, args, mode );
        args = new Object[9];
        args[0] = sm.getString ( "htmlManagerServlet.serverTitle" );
        args[1] = sm.getString ( "htmlManagerServlet.serverVersion" );
        args[2] = sm.getString ( "htmlManagerServlet.serverJVMVersion" );
        args[3] = sm.getString ( "htmlManagerServlet.serverJVMVendor" );
        args[4] = sm.getString ( "htmlManagerServlet.serverOSName" );
        args[5] = sm.getString ( "htmlManagerServlet.serverOSVersion" );
        args[6] = sm.getString ( "htmlManagerServlet.serverOSArch" );
        args[7] = sm.getString ( "htmlManagerServlet.serverHostname" );
        args[8] = sm.getString ( "htmlManagerServlet.serverIPAddress" );
        StatusTransformer.writePageHeading ( writer, args, mode );
        args = new Object[8];
        args[0] = ServerInfo.getServerInfo();
        args[1] = System.getProperty ( "java.runtime.version" );
        args[2] = System.getProperty ( "java.vm.vendor" );
        args[3] = System.getProperty ( "os.name" );
        args[4] = System.getProperty ( "os.version" );
        args[5] = System.getProperty ( "os.arch" );
        try {
            InetAddress address = InetAddress.getLocalHost();
            args[6] = address.getHostName();
            args[7] = address.getHostAddress();
        } catch ( UnknownHostException e ) {
            args[6] = "-";
            args[7] = "-";
        }
        StatusTransformer.writeServerInfo ( writer, args, mode );
        try {
            StatusTransformer.writeOSState ( writer, mode );
            StatusTransformer.writeVMState ( writer, mode );
            Enumeration<ObjectName> enumeration = threadPools.elements();
            while ( enumeration.hasMoreElements() ) {
                ObjectName objectName = enumeration.nextElement();
                String name = objectName.getKeyProperty ( "name" );
                StatusTransformer.writeConnectorState
                ( writer, objectName,
                  name, mBeanServer, globalRequestProcessors,
                  requestProcessors, mode );
            }
            if ( ( request.getPathInfo() != null )
                    && ( request.getPathInfo().equals ( "/all" ) ) ) {
                StatusTransformer.writeDetailedState
                ( writer, mBeanServer, mode );
            }
        } catch ( Exception e ) {
            throw new ServletException ( e );
        }
        StatusTransformer.writeFooter ( writer, mode );
    }
    @Override
    public void handleNotification ( Notification notification,
                                     java.lang.Object handback ) {
        if ( notification instanceof MBeanServerNotification ) {
            ObjectName objectName =
                ( ( MBeanServerNotification ) notification ).getMBeanName();
            if ( notification.getType().equals
                    ( MBeanServerNotification.REGISTRATION_NOTIFICATION ) ) {
                String type = objectName.getKeyProperty ( "type" );
                if ( type != null ) {
                    if ( type.equals ( "ProtocolHandler" ) ) {
                        protocolHandlers.addElement ( objectName );
                    } else if ( type.equals ( "ThreadPool" ) ) {
                        threadPools.addElement ( objectName );
                    } else if ( type.equals ( "GlobalRequestProcessor" ) ) {
                        globalRequestProcessors.addElement ( objectName );
                    } else if ( type.equals ( "RequestProcessor" ) ) {
                        requestProcessors.addElement ( objectName );
                    }
                }
            } else if ( notification.getType().equals
                        ( MBeanServerNotification.UNREGISTRATION_NOTIFICATION ) ) {
                String type = objectName.getKeyProperty ( "type" );
                if ( type != null ) {
                    if ( type.equals ( "ProtocolHandler" ) ) {
                        protocolHandlers.removeElement ( objectName );
                    } else if ( type.equals ( "ThreadPool" ) ) {
                        threadPools.removeElement ( objectName );
                    } else if ( type.equals ( "GlobalRequestProcessor" ) ) {
                        globalRequestProcessors.removeElement ( objectName );
                    } else if ( type.equals ( "RequestProcessor" ) ) {
                        requestProcessors.removeElement ( objectName );
                    }
                }
                String j2eeType = objectName.getKeyProperty ( "j2eeType" );
                if ( j2eeType != null ) {
                }
            }
        }
    }
}
