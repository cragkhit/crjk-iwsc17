package javax.servlet.http;
public class HttpSessionBindingEvent extends HttpSessionEvent {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final Object value;
    public HttpSessionBindingEvent ( final HttpSession session, final String name ) {
        super ( session );
        this.name = name;
        this.value = null;
    }
    public HttpSessionBindingEvent ( final HttpSession session, final String name, final Object value ) {
        super ( session );
        this.name = name;
        this.value = value;
    }
    @Override
    public HttpSession getSession() {
        return super.getSession();
    }
    public String getName() {
        return this.name;
    }
    public Object getValue() {
        return this.value;
    }
}
