package javax.servlet.jsp.tagext;
public class ValidationMessage {
    public ValidationMessage ( String id, String message ) {
        this.id = id;
        this.message = message;
    }
    public String getId() {
        return id;
    }
    public String getMessage() {
        return message;
    }
    private final String id;
    private final String message;
}
