package org.apache.tomcat.websocket.server;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
public class WsContextListener implements ServletContextListener {
    @Override
    public void contextInitialized ( ServletContextEvent sce ) {
        ServletContext sc = sce.getServletContext();
        if ( sc.getAttribute ( Constants.SERVER_CONTAINER_SERVLET_CONTEXT_ATTRIBUTE ) == null ) {
            WsSci.init ( sce.getServletContext(), false );
        }
    }
    @Override
    public void contextDestroyed ( ServletContextEvent sce ) {
        ServletContext sc = sce.getServletContext();
        Object obj = sc.getAttribute ( Constants.SERVER_CONTAINER_SERVLET_CONTEXT_ATTRIBUTE );
        if ( obj instanceof WsServerContainer ) {
            ( ( WsServerContainer ) obj ).destroy();
        }
    }
}
