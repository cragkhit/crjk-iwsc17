package org.jfree.chart.axis;
import java.io.ObjectStreamException;
import java.io.Serializable;
public final class AxisLabelLocation implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final AxisLabelLocation HIGH_END;
    public static final AxisLabelLocation MIDDLE;
    public static final AxisLabelLocation LOW_END;
    private String name;
    private AxisLabelLocation ( final String name ) {
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
        if ( ! ( obj instanceof AxisLabelLocation ) ) {
            return false;
        }
        final AxisLabelLocation location = ( AxisLabelLocation ) obj;
        return this.name.equals ( location.toString() );
    }
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + this.name.hashCode();
        return hash;
    }
    private Object readResolve() throws ObjectStreamException {
        if ( this.equals ( AxisLabelLocation.HIGH_END ) ) {
            return AxisLabelLocation.HIGH_END;
        }
        if ( this.equals ( AxisLabelLocation.MIDDLE ) ) {
            return AxisLabelLocation.MIDDLE;
        }
        if ( this.equals ( AxisLabelLocation.LOW_END ) ) {
            return AxisLabelLocation.LOW_END;
        }
        return null;
    }
    static {
        HIGH_END = new AxisLabelLocation ( "HIGH_END" );
        MIDDLE = new AxisLabelLocation ( "MIDDLE" );
        LOW_END = new AxisLabelLocation ( "LOW_END" );
    }
}
