package javax.servlet;
public class ServletRequestAttributeEvent extends ServletRequestEvent {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final Object value;
    public ServletRequestAttributeEvent ( final ServletContext sc, final ServletRequest request, final String name, final Object value ) {
        super ( sc, request );
        this.name = name;
        this.value = value;
    }
    public String getName() {
        return this.name;
    }
    public Object getValue() {
        return this.value;
    }
}
