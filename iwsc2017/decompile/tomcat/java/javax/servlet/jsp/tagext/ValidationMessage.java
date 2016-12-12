package javax.servlet.jsp.tagext;
public class ValidationMessage {
    private final String id;
    private final String message;
    public ValidationMessage ( final String id, final String message ) {
        this.id = id;
        this.message = message;
    }
    public String getId() {
        return this.id;
    }
    public String getMessage() {
        return this.message;
    }
}
