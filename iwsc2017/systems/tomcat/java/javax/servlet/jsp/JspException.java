package javax.servlet.jsp;
public class JspException extends Exception {
    private static final long serialVersionUID = 1L;
    public JspException() {
    }
    public JspException ( String msg ) {
        super ( msg );
    }
    public JspException ( String message, Throwable cause ) {
        super ( message, cause );
    }
    public JspException ( Throwable cause ) {
        super ( cause );
    }
    @SuppressWarnings ( "dep-ann" )
    public Throwable getRootCause() {
        return getCause();
    }
}
