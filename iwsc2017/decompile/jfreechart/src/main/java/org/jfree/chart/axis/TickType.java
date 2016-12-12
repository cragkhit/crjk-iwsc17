package org.jfree.chart.axis;
import java.io.ObjectStreamException;
import java.io.Serializable;
public final class TickType implements Serializable {
    public static final TickType MAJOR;
    public static final TickType MINOR;
    private String name;
    private TickType ( final String name ) {
        this.name = name;
    }
    @Override
    public String toString() {
        return this.name;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( ! ( obj instanceof TickType ) ) {
            return false;
        }
        final TickType that = ( TickType ) obj;
        return this.name.equals ( that.name );
    }
    private Object readResolve() throws ObjectStreamException {
        Object result = null;
        if ( this.equals ( TickType.MAJOR ) ) {
            result = TickType.MAJOR;
        } else if ( this.equals ( TickType.MINOR ) ) {
            result = TickType.MINOR;
        }
        return result;
    }
    static {
        MAJOR = new TickType ( "MAJOR" );
        MINOR = new TickType ( "MINOR" );
    }
}
