package javax.servlet.jsp;
public class JspTagException extends JspException {
    private static final long serialVersionUID = 1L;
    public JspTagException ( String msg ) {
        super ( msg );
    }
    public JspTagException() {
        super();
    }
    public JspTagException ( String message, Throwable rootCause ) {
        super ( message, rootCause );
    }
    public JspTagException ( Throwable rootCause ) {
        super ( rootCause );
    }
}
