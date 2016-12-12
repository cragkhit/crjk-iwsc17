package org.apache.catalina.startup;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import java.util.Stack;
import javax.servlet.SingleThreadModel;
import javax.servlet.Servlet;
import org.apache.catalina.core.StandardWrapper;
public static class ExistingStandardWrapper extends StandardWrapper {
    private final Servlet existing;
    public ExistingStandardWrapper ( final Servlet existing ) {
        this.existing = existing;
        if ( existing instanceof SingleThreadModel ) {
            this.singleThreadModel = true;
            this.instancePool = new Stack<Servlet>();
        }
        this.asyncSupported = hasAsync ( existing );
    }
    private static boolean hasAsync ( final Servlet existing ) {
        boolean result = false;
        final Class<?> clazz = existing.getClass();
        final WebServlet ws = clazz.getAnnotation ( WebServlet.class );
        if ( ws != null ) {
            result = ws.asyncSupported();
        }
        return result;
    }
    @Override
    public synchronized Servlet loadServlet() throws ServletException {
        if ( this.singleThreadModel ) {
            Servlet instance;
            try {
                instance = ( Servlet ) this.existing.getClass().newInstance();
            } catch ( InstantiationException e ) {
                throw new ServletException ( ( Throwable ) e );
            } catch ( IllegalAccessException e2 ) {
                throw new ServletException ( ( Throwable ) e2 );
            }
            instance.init ( ( ServletConfig ) this.facade );
            return instance;
        }
        if ( !this.instanceInitialized ) {
            this.existing.init ( ( ServletConfig ) this.facade );
            this.instanceInitialized = true;
        }
        return this.existing;
    }
    @Override
    public long getAvailable() {
        return 0L;
    }
    @Override
    public boolean isUnavailable() {
        return false;
    }
    @Override
    public Servlet getServlet() {
        return this.existing;
    }
    @Override
    public String getServletClass() {
        return this.existing.getClass().getName();
    }
}
