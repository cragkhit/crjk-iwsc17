package javax.el;
public class PropertyNotFoundException extends ELException {
    private static final long serialVersionUID = -3799200961303506745L;
    public PropertyNotFoundException() {
    }
    public PropertyNotFoundException ( final String message ) {
        super ( message );
    }
    public PropertyNotFoundException ( final Throwable cause ) {
        super ( cause );
    }
    public PropertyNotFoundException ( final String message, final Throwable cause ) {
        super ( message, cause );
    }
}
