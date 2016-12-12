package javax.el;
public class PropertyNotWritableException extends ELException {
    private static final long serialVersionUID = 827987155471214717L;
    public PropertyNotWritableException() {
    }
    public PropertyNotWritableException ( final String message ) {
        super ( message );
    }
    public PropertyNotWritableException ( final Throwable cause ) {
        super ( cause );
    }
    public PropertyNotWritableException ( final String message, final Throwable cause ) {
        super ( message, cause );
    }
}
