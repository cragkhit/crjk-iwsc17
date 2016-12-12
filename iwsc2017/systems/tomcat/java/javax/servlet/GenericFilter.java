package javax.servlet;
import java.io.Serializable;
import java.util.Enumeration;
public abstract class GenericFilter implements Filter, FilterConfig, Serializable {
    private static final long serialVersionUID = 1L;
    private volatile FilterConfig filterConfig;
    @Override
    public String getInitParameter ( String name ) {
        return getFilterConfig().getInitParameter ( name );
    }
    @Override
    public Enumeration<String> getInitParameterNames() {
        return getFilterConfig().getInitParameterNames();
    }
    public FilterConfig getFilterConfig() {
        return filterConfig;
    }
    @Override
    public ServletContext getServletContext() {
        return getFilterConfig().getServletContext();
    }
    @Override
    public void init ( FilterConfig filterConfig ) throws ServletException {
        this.filterConfig  = filterConfig;
        init();
    }
    public void init() throws ServletException {
    }
    @Override
    public String getFilterName() {
        return getFilterConfig().getFilterName();
    }
}
