package javax.servlet;
public class ServletException extends Exception {
    private static final long serialVersionUID = 1L;
    public ServletException() {
    }
    public ServletException ( final String message ) {
        super ( message );
    }
    public ServletException ( final String message, final Throwable rootCause ) {
        super ( message, rootCause );
    }
    public ServletException ( final Throwable rootCause ) {
        super ( rootCause );
    }
    public Throwable getRootCause() {
        return this.getCause();
    }
}
