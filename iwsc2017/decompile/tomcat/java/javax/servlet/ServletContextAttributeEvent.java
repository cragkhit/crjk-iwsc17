package javax.servlet;
public class ServletContextAttributeEvent extends ServletContextEvent {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final Object value;
    public ServletContextAttributeEvent ( final ServletContext source, final String name, final Object value ) {
        super ( source );
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
