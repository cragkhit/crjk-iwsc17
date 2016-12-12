package javax.websocket;
public class EncodeException extends Exception {
    private static final long serialVersionUID = 1L;
    private Object object;
    public EncodeException ( final Object object, final String message ) {
        super ( message );
        this.object = object;
    }
    public EncodeException ( final Object object, final String message, final Throwable cause ) {
        super ( message, cause );
        this.object = object;
    }
    public Object getObject() {
        return this.object;
    }
}
