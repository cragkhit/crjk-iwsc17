package javax.servlet.jsp;
public class JspTagException extends JspException {
    private static final long serialVersionUID = 1L;
    public JspTagException ( final String msg ) {
        super ( msg );
    }
    public JspTagException() {
    }
    public JspTagException ( final String message, final Throwable rootCause ) {
        super ( message, rootCause );
    }
    public JspTagException ( final Throwable rootCause ) {
        super ( rootCause );
    }
}
