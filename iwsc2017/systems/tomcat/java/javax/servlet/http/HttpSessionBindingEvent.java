package javax.servlet.http;
public class HttpSessionBindingEvent extends HttpSessionEvent {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final Object value;
    public HttpSessionBindingEvent ( HttpSession session, String name ) {
        super ( session );
        this.name = name;
        this.value = null;
    }
    public HttpSessionBindingEvent ( HttpSession session, String name,
                                     Object value ) {
        super ( session );
        this.name = name;
        this.value = value;
    }
    @Override
    public HttpSession getSession() {
        return super.getSession();
    }
    public String getName() {
        return name;
    }
    public Object getValue() {
        return this.value;
    }
}
