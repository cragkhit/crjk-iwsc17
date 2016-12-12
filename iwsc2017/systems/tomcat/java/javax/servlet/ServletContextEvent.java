package javax.servlet;
public class ServletContextEvent extends java.util.EventObject {
    private static final long serialVersionUID = 1L;
    public ServletContextEvent ( ServletContext source ) {
        super ( source );
    }
    public ServletContext getServletContext() {
        return ( ServletContext ) super.getSource();
    }
}
