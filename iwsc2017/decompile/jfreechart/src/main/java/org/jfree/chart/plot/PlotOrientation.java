package org.jfree.chart.plot;
import java.io.ObjectStreamException;
import java.io.Serializable;
public final class PlotOrientation implements Serializable {
    private static final long serialVersionUID = -2508771828190337782L;
    public static final PlotOrientation HORIZONTAL;
    public static final PlotOrientation VERTICAL;
    private String name;
    private PlotOrientation ( final String name ) {
        this.name = name;
    }
    public boolean isHorizontal() {
        return this.equals ( PlotOrientation.HORIZONTAL );
    }
    public boolean isVertical() {
        return this.equals ( PlotOrientation.VERTICAL );
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
        if ( ! ( obj instanceof PlotOrientation ) ) {
            return false;
        }
        final PlotOrientation orientation = ( PlotOrientation ) obj;
        return this.name.equals ( orientation.toString() );
    }
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    private Object readResolve() throws ObjectStreamException {
        Object result = null;
        if ( this.equals ( PlotOrientation.HORIZONTAL ) ) {
            result = PlotOrientation.HORIZONTAL;
        } else if ( this.equals ( PlotOrientation.VERTICAL ) ) {
            result = PlotOrientation.VERTICAL;
        }
        return result;
    }
    static {
        HORIZONTAL = new PlotOrientation ( "PlotOrientation.HORIZONTAL" );
        VERTICAL = new PlotOrientation ( "PlotOrientation.VERTICAL" );
    }
}
