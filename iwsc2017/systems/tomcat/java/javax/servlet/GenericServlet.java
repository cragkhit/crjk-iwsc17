package javax.servlet;
import java.io.IOException;
import java.util.Enumeration;
public abstract class GenericServlet implements Servlet, ServletConfig,
    java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private transient ServletConfig config;
    public GenericServlet() {
    }
    @Override
    public void destroy() {
    }
    @Override
    public String getInitParameter ( String name ) {
        return getServletConfig().getInitParameter ( name );
    }
    @Override
    public Enumeration<String> getInitParameterNames() {
        return getServletConfig().getInitParameterNames();
    }
    @Override
    public ServletConfig getServletConfig() {
        return config;
    }
    @Override
    public ServletContext getServletContext() {
        return getServletConfig().getServletContext();
    }
    @Override
    public String getServletInfo() {
        return "";
    }
    @Override
    public void init ( ServletConfig config ) throws ServletException {
        this.config = config;
        this.init();
    }
    public void init() throws ServletException {
    }
    public void log ( String msg ) {
        getServletContext().log ( getServletName() + ": " + msg );
    }
    public void log ( String message, Throwable t ) {
        getServletContext().log ( getServletName() + ": " + message, t );
    }
    @Override
    public abstract void service ( ServletRequest req, ServletResponse res )
    throws ServletException, IOException;
    @Override
    public String getServletName() {
        return config.getServletName();
    }
}
