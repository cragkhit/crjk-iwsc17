package javax.el;
public class MethodNotFoundException extends ELException {
    private static final long serialVersionUID = -3631968116081480328L;
    public MethodNotFoundException() {
    }
    public MethodNotFoundException ( final String message ) {
        super ( message );
    }
    public MethodNotFoundException ( final Throwable cause ) {
        super ( cause );
    }
    public MethodNotFoundException ( final String message, final Throwable cause ) {
        super ( message, cause );
    }
}
