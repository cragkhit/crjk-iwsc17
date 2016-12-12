// 
// Decompiled by Procyon v0.5.29
// 

package listeners;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.ServletContextListener;

public final class SessionListener implements ServletContextListener, HttpSessionAttributeListener, HttpSessionListener
{
    private ServletContext context;
    
    public SessionListener() {
        this.context = null;
    }
    
    public void attributeAdded(final HttpSessionBindingEvent event) {
        this.log("attributeAdded('" + event.getSession().getId() + "', '" + event.getName() + "', '" + event.getValue() + "')");
    }
    
    public void attributeRemoved(final HttpSessionBindingEvent event) {
        this.log("attributeRemoved('" + event.getSession().getId() + "', '" + event.getName() + "', '" + event.getValue() + "')");
    }
    
    public void attributeReplaced(final HttpSessionBindingEvent event) {
        this.log("attributeReplaced('" + event.getSession().getId() + "', '" + event.getName() + "', '" + event.getValue() + "')");
    }
    
    public void contextDestroyed(final ServletContextEvent event) {
        this.log("contextDestroyed()");
        this.context = null;
    }
    
    public void contextInitialized(final ServletContextEvent event) {
        this.context = event.getServletContext();
        this.log("contextInitialized()");
    }
    
    public void sessionCreated(final HttpSessionEvent event) {
        this.log("sessionCreated('" + event.getSession().getId() + "')");
    }
    
    public void sessionDestroyed(final HttpSessionEvent event) {
        this.log("sessionDestroyed('" + event.getSession().getId() + "')");
    }
    
    private void log(final String message) {
        if (this.context != null) {
            this.context.log("SessionListener: " + message);
        }
        else {
            System.out.println("SessionListener: " + message);
        }
    }
}
