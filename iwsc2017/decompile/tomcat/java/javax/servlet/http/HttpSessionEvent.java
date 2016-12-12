package javax.servlet.http;
import java.util.EventObject;
public class HttpSessionEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    public HttpSessionEvent ( final HttpSession source ) {
        super ( source );
    }
    public HttpSession getSession() {
        return ( HttpSession ) super.getSource();
    }
}
