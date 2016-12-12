// 
// Decompiled by Procyon v0.5.29
// 

package listeners;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextAttributeListener;

public final class ContextListener implements ServletContextAttributeListener, ServletContextListener
{
    private ServletContext context;
    
    public ContextListener() {
        this.context = null;
    }
    
    public void attributeAdded(final ServletContextAttributeEvent event) {
        this.log("attributeAdded('" + event.getName() + "', '" + event.getValue() + "')");
    }
    
    public void attributeRemoved(final ServletContextAttributeEvent event) {
        this.log("attributeRemoved('" + event.getName() + "', '" + event.getValue() + "')");
    }
    
    public void attributeReplaced(final ServletContextAttributeEvent event) {
        this.log("attributeReplaced('" + event.getName() + "', '" + event.getValue() + "')");
    }
    
    public void contextDestroyed(final ServletContextEvent event) {
        this.log("contextDestroyed()");
        this.context = null;
    }
    
    public void contextInitialized(final ServletContextEvent event) {
        this.context = event.getServletContext();
        this.log("contextInitialized()");
    }
    
    private void log(final String message) {
        if (this.context != null) {
            this.context.log("ContextListener: " + message);
        }
        else {
            System.out.println("ContextListener: " + message);
        }
    }
}
