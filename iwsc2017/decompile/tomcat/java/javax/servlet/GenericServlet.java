package javax.servlet;
import java.io.IOException;
import java.util.Enumeration;
import java.io.Serializable;
public abstract class GenericServlet implements Servlet, ServletConfig, Serializable {
    private static final long serialVersionUID = 1L;
    private transient ServletConfig config;
    @Override
    public void destroy() {
    }
    @Override
    public String getInitParameter ( final String name ) {
        return this.getServletConfig().getInitParameter ( name );
    }
    @Override
    public Enumeration<String> getInitParameterNames() {
        return this.getServletConfig().getInitParameterNames();
    }
    @Override
    public ServletConfig getServletConfig() {
        return this.config;
    }
    @Override
    public ServletContext getServletContext() {
        return this.getServletConfig().getServletContext();
    }
    @Override
    public String getServletInfo() {
        return "";
    }
    @Override
    public void init ( final ServletConfig config ) throws ServletException {
        this.config = config;
        this.init();
    }
    public void init() throws ServletException {
    }
    public void log ( final String msg ) {
        this.getServletContext().log ( this.getServletName() + ": " + msg );
    }
    public void log ( final String message, final Throwable t ) {
        this.getServletContext().log ( this.getServletName() + ": " + message, t );
    }
    @Override
    public abstract void service ( final ServletRequest p0, final ServletResponse p1 ) throws ServletException, IOException;
    @Override
    public String getServletName() {
        return this.config.getServletName();
    }
}
