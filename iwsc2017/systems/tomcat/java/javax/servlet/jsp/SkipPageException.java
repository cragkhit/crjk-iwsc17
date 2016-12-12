package javax.servlet.jsp;
public class SkipPageException extends JspException {
    private static final long serialVersionUID = 1L;
    public SkipPageException() {
        super();
    }
    public SkipPageException ( String message ) {
        super ( message );
    }
    public SkipPageException ( String message, Throwable rootCause ) {
        super ( message, rootCause );
    }
    public SkipPageException ( Throwable rootCause ) {
        super ( rootCause );
    }
}
