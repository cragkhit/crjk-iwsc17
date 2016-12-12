package org.apache.catalina;
import java.util.EventObject;
public final class SessionEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    private final Object data;
    private final Session session;
    private final String type;
    public SessionEvent ( final Session session, final String type, final Object data ) {
        super ( session );
        this.session = session;
        this.type = type;
        this.data = data;
    }
    public Object getData() {
        return this.data;
    }
    public Session getSession() {
        return this.session;
    }
    public String getType() {
        return this.type;
    }
    @Override
    public String toString() {
        return "SessionEvent['" + this.getSession() + "','" + this.getType() + "']";
    }
}
