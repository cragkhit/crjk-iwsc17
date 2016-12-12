package org.apache.catalina;
import java.util.EventObject;
public final class LifecycleEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    private final Object data;
    private final String type;
    public LifecycleEvent ( final Lifecycle lifecycle, final String type, final Object data ) {
        super ( lifecycle );
        this.type = type;
        this.data = data;
    }
    public Object getData() {
        return this.data;
    }
    public Lifecycle getLifecycle() {
        return ( Lifecycle ) this.getSource();
    }
    public String getType() {
        return this.type;
    }
}
