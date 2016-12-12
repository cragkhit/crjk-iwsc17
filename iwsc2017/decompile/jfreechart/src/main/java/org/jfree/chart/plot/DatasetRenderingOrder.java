package org.jfree.chart.plot;
import java.io.ObjectStreamException;
import java.io.Serializable;
public final class DatasetRenderingOrder implements Serializable {
    private static final long serialVersionUID = -600593412366385072L;
    public static final DatasetRenderingOrder FORWARD;
    public static final DatasetRenderingOrder REVERSE;
    private String name;
    private DatasetRenderingOrder ( final String name ) {
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
        if ( ! ( obj instanceof DatasetRenderingOrder ) ) {
            return false;
        }
        final DatasetRenderingOrder order = ( DatasetRenderingOrder ) obj;
        return this.name.equals ( order.toString() );
    }
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    private Object readResolve() throws ObjectStreamException {
        if ( this.equals ( DatasetRenderingOrder.FORWARD ) ) {
            return DatasetRenderingOrder.FORWARD;
        }
        if ( this.equals ( DatasetRenderingOrder.REVERSE ) ) {
            return DatasetRenderingOrder.REVERSE;
        }
        return null;
    }
    static {
        FORWARD = new DatasetRenderingOrder ( "DatasetRenderingOrder.FORWARD" );
        REVERSE = new DatasetRenderingOrder ( "DatasetRenderingOrder.REVERSE" );
    }
}
