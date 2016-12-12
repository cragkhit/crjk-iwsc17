package javax.servlet;
import java.util.EventObject;
public class ServletRequestEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    private final transient ServletRequest request;
    public ServletRequestEvent ( final ServletContext sc, final ServletRequest request ) {
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
