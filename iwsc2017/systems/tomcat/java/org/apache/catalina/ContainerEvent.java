package org.apache.catalina;
import java.util.EventObject;
public final class ContainerEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    private final Object data;
    private final String type;
    public ContainerEvent ( Container container, String type, Object data ) {
        super ( container );
        this.type = type;
        this.data = data;
    }
    public Object getData() {
        return this.data;
    }
    public Container getContainer() {
        return ( Container ) getSource();
    }
    public String getType() {
        return this.type;
    }
    @Override
    public String toString() {
        return ( "ContainerEvent['" + getContainer() + "','" +
                 getType() + "','" + getData() + "']" );
    }
}
