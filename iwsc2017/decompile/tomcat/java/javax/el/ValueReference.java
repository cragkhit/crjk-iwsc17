package javax.el;
import java.io.Serializable;
public class ValueReference implements Serializable {
    private static final long serialVersionUID = 1L;
    private final Object base;
    private final Object property;
    public ValueReference ( final Object base, final Object property ) {
        this.base = base;
        this.property = property;
    }
    public Object getBase() {
        return this.base;
    }
    public Object getProperty() {
        return this.property;
    }
}
