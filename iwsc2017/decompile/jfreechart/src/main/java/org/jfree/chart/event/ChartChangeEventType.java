package org.jfree.chart.event;
import java.io.ObjectStreamException;
import java.io.Serializable;
public final class ChartChangeEventType implements Serializable {
    private static final long serialVersionUID = 5481917022435735602L;
    public static final ChartChangeEventType GENERAL;
    public static final ChartChangeEventType NEW_DATASET;
    public static final ChartChangeEventType DATASET_UPDATED;
    private String name;
    private ChartChangeEventType ( final String name ) {
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
        if ( ! ( obj instanceof ChartChangeEventType ) ) {
            return false;
        }
        final ChartChangeEventType that = ( ChartChangeEventType ) obj;
        return this.name.equals ( that.toString() );
    }
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    private Object readResolve() throws ObjectStreamException {
        if ( this.equals ( ChartChangeEventType.GENERAL ) ) {
            return ChartChangeEventType.GENERAL;
        }
        if ( this.equals ( ChartChangeEventType.NEW_DATASET ) ) {
            return ChartChangeEventType.NEW_DATASET;
        }
        if ( this.equals ( ChartChangeEventType.DATASET_UPDATED ) ) {
            return ChartChangeEventType.DATASET_UPDATED;
        }
        return null;
    }
    static {
        GENERAL = new ChartChangeEventType ( "ChartChangeEventType.GENERAL" );
        NEW_DATASET = new ChartChangeEventType ( "ChartChangeEventType.NEW_DATASET" );
        DATASET_UPDATED = new ChartChangeEventType ( "ChartChangeEventType.DATASET_UPDATED" );
    }
}
