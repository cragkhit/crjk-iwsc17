package org.jfree.data.statistics;
import java.io.ObjectStreamException;
import java.io.Serializable;
public class HistogramType implements Serializable {
    private static final long serialVersionUID = 2618927186251997727L;
    public static final HistogramType FREQUENCY;
    public static final HistogramType RELATIVE_FREQUENCY;
    public static final HistogramType SCALE_AREA_TO_1;
    private String name;
    private HistogramType ( final String name ) {
        this.name = name;
    }
    @Override
    public String toString() {
        return this.name;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == null ) {
            return false;
        }
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof HistogramType ) ) {
            return false;
        }
        final HistogramType t = ( HistogramType ) obj;
        return this.name.equals ( t.name );
    }
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    private Object readResolve() throws ObjectStreamException {
        if ( this.equals ( HistogramType.FREQUENCY ) ) {
            return HistogramType.FREQUENCY;
        }
        if ( this.equals ( HistogramType.RELATIVE_FREQUENCY ) ) {
            return HistogramType.RELATIVE_FREQUENCY;
        }
        if ( this.equals ( HistogramType.SCALE_AREA_TO_1 ) ) {
            return HistogramType.SCALE_AREA_TO_1;
        }
        return null;
    }
    static {
        FREQUENCY = new HistogramType ( "FREQUENCY" );
        RELATIVE_FREQUENCY = new HistogramType ( "RELATIVE_FREQUENCY" );
        SCALE_AREA_TO_1 = new HistogramType ( "SCALE_AREA_TO_1" );
    }
}
