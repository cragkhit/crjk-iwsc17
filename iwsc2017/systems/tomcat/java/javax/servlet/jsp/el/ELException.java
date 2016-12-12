package javax.servlet.jsp.el;
@SuppressWarnings ( "dep-ann" )
public class ELException extends Exception {
    private static final long serialVersionUID = 1L;
    public ELException() {
        super();
    }
    public ELException ( String pMessage ) {
        super ( pMessage );
    }
    public ELException ( Throwable pRootCause ) {
        super ( pRootCause );
    }
    public ELException ( String pMessage, Throwable pRootCause ) {
        super ( pMessage, pRootCause );
    }
    public Throwable getRootCause() {
        return getCause();
    }
}
