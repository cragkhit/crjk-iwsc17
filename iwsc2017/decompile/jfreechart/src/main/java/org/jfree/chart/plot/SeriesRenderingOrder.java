package org.jfree.chart.plot;
import java.io.ObjectStreamException;
import java.io.Serializable;
public final class SeriesRenderingOrder implements Serializable {
    private static final long serialVersionUID = 209336477448807735L;
    public static final SeriesRenderingOrder FORWARD;
    public static final SeriesRenderingOrder REVERSE;
    private String name;
    private SeriesRenderingOrder ( final String name ) {
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
        if ( ! ( obj instanceof SeriesRenderingOrder ) ) {
            return false;
        }
        final SeriesRenderingOrder order = ( SeriesRenderingOrder ) obj;
        return this.name.equals ( order.toString() );
    }
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    private Object readResolve() throws ObjectStreamException {
        if ( this.equals ( SeriesRenderingOrder.FORWARD ) ) {
            return SeriesRenderingOrder.FORWARD;
        }
        if ( this.equals ( SeriesRenderingOrder.REVERSE ) ) {
            return SeriesRenderingOrder.REVERSE;
        }
        return null;
    }
    static {
        FORWARD = new SeriesRenderingOrder ( "SeriesRenderingOrder.FORWARD" );
        REVERSE = new SeriesRenderingOrder ( "SeriesRenderingOrder.REVERSE" );
    }
}
