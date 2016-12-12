package javax.servlet.http;
public class HttpSessionEvent extends java.util.EventObject {
    private static final long serialVersionUID = 1L;
    public HttpSessionEvent ( HttpSession source ) {
        super ( source );
    }
    public HttpSession getSession() {
        return ( HttpSession ) super.getSource();
    }
}
