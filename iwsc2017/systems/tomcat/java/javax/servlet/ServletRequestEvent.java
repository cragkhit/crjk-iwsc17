package javax.servlet;
public class ServletRequestEvent extends java.util.EventObject {
    private static final long serialVersionUID = 1L;
    private final transient ServletRequest request;
    public ServletRequestEvent ( ServletContext sc, ServletRequest request ) {
        super ( sc );
        this.request = request;
    }
    public ServletRequest getServletRequest() {
        return this.request;
    }
    public ServletContext getServletContext() {
        return ( ServletContext ) super.getSource();
    }
}
