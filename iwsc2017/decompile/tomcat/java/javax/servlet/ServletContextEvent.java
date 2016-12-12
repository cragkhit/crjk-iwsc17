package javax.servlet;
import java.util.EventObject;
public class ServletContextEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    public ServletContextEvent ( final ServletContext source ) {
        super ( source );
    }
    public ServletContext getServletContext() {
        return ( ServletContext ) super.getSource();
    }
}
