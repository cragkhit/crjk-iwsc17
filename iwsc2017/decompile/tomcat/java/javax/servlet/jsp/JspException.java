package javax.servlet.jsp;
public class JspException extends Exception {
    private static final long serialVersionUID = 1L;
    public JspException() {
    }
    public JspException ( final String msg ) {
        super ( msg );
    }
    public JspException ( final String message, final Throwable cause ) {
        super ( message, cause );
    }
    public JspException ( final Throwable cause ) {
        super ( cause );
    }
    public Throwable getRootCause() {
        return this.getCause();
    }
}
