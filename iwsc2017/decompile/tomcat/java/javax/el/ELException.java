package javax.el;
public class ELException extends RuntimeException {
    private static final long serialVersionUID = -6228042809457459161L;
    public ELException() {
    }
    public ELException ( final String message ) {
        super ( message );
    }
    public ELException ( final Throwable cause ) {
        super ( cause );
    }
    public ELException ( final String message, final Throwable cause ) {
        super ( message, cause );
    }
}
