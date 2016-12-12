package org.jfree.chart;
import java.io.ObjectStreamException;
import java.io.Serializable;
public final class LegendRenderingOrder implements Serializable {
    private static final long serialVersionUID = -3832486612685808616L;
    public static final LegendRenderingOrder STANDARD;
    public static final LegendRenderingOrder REVERSE;
    private String name;
    private LegendRenderingOrder ( final String name ) {
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
        if ( ! ( obj instanceof LegendRenderingOrder ) ) {
            return false;
        }
        final LegendRenderingOrder order = ( LegendRenderingOrder ) obj;
        return this.name.equals ( order.toString() );
    }
    private Object readResolve() throws ObjectStreamException {
        if ( this.equals ( LegendRenderingOrder.STANDARD ) ) {
            return LegendRenderingOrder.STANDARD;
        }
        if ( this.equals ( LegendRenderingOrder.REVERSE ) ) {
            return LegendRenderingOrder.REVERSE;
        }
        return null;
    }
    static {
        STANDARD = new LegendRenderingOrder ( "LegendRenderingOrder.STANDARD" );
        REVERSE = new LegendRenderingOrder ( "LegendRenderingOrder.REVERSE" );
    }
}
