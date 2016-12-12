package org.apache.tomcat.websocket.server;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
public class WsSessionListener implements HttpSessionListener {
    private final WsServerContainer wsServerContainer;
    public WsSessionListener ( final WsServerContainer wsServerContainer ) {
        this.wsServerContainer = wsServerContainer;
    }
    public void sessionDestroyed ( final HttpSessionEvent se ) {
        this.wsServerContainer.closeAuthenticatedSession ( se.getSession().getId() );
    }
}
