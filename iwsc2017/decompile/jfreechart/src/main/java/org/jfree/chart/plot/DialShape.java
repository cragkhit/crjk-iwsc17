package org.jfree.chart.plot;
import java.io.ObjectStreamException;
import java.io.Serializable;
public final class DialShape implements Serializable {
    private static final long serialVersionUID = -3471933055190251131L;
    public static final DialShape CIRCLE;
    public static final DialShape CHORD;
    public static final DialShape PIE;
    private String name;
    private DialShape ( final String name ) {
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
        if ( ! ( obj instanceof DialShape ) ) {
            return false;
        }
        final DialShape shape = ( DialShape ) obj;
        return this.name.equals ( shape.toString() );
    }
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    private Object readResolve() throws ObjectStreamException {
        if ( this.equals ( DialShape.CIRCLE ) ) {
            return DialShape.CIRCLE;
        }
        if ( this.equals ( DialShape.CHORD ) ) {
            return DialShape.CHORD;
        }
        if ( this.equals ( DialShape.PIE ) ) {
            return DialShape.PIE;
        }
        return null;
    }
    static {
        CIRCLE = new DialShape ( "DialShape.CIRCLE" );
        CHORD = new DialShape ( "DialShape.CHORD" );
        PIE = new DialShape ( "DialShape.PIE" );
    }
}
