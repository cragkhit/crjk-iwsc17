package javax.servlet.jsp.el;
public class ELException extends Exception {
    private static final long serialVersionUID = 1L;
    public ELException() {
    }
    public ELException ( final String pMessage ) {
        super ( pMessage );
    }
    public ELException ( final Throwable pRootCause ) {
        super ( pRootCause );
    }
    public ELException ( final String pMessage, final Throwable pRootCause ) {
        super ( pMessage, pRootCause );
    }
    public Throwable getRootCause() {
        return this.getCause();
    }
}
