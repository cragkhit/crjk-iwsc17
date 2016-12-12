package org.apache.catalina.connector;
import java.security.PrivilegedAction;
private final class DateHeaderPrivilegedAction implements PrivilegedAction<Void> {
    private final String name;
    private final long value;
    private final boolean add;
    DateHeaderPrivilegedAction ( final String name, final long value, final boolean add ) {
        this.name = name;
        this.value = value;
        this.add = add;
    }
    @Override
    public Void run() {
        if ( this.add ) {
            ResponseFacade.this.response.addDateHeader ( this.name, this.value );
        } else {
            ResponseFacade.this.response.setDateHeader ( this.name, this.value );
        }
        return null;
    }
}
