package org.apache.tomcat.websocket.server;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
public class WsContextListener implements ServletContextListener {
    public void contextInitialized ( final ServletContextEvent sce ) {
        final ServletContext sc = sce.getServletContext();
        if ( sc.getAttribute ( "javax.websocket.server.ServerContainer" ) == null ) {
            WsSci.init ( sce.getServletContext(), false );
        }
    }
    public void contextDestroyed ( final ServletContextEvent sce ) {
        final ServletContext sc = sce.getServletContext();
        final Object obj = sc.getAttribute ( "javax.websocket.server.ServerContainer" );
        if ( obj instanceof WsServerContainer ) {
            ( ( WsServerContainer ) obj ).destroy();
        }
    }
}
