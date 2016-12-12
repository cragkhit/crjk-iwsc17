package org.jfree.chart.util;
import java.io.ObjectStreamException;
import java.io.Serializable;
public final class XYCoordinateType implements Serializable {
    public static final XYCoordinateType DATA;
    public static final XYCoordinateType RELATIVE;
    public static final XYCoordinateType INDEX;
    private String name;
    private XYCoordinateType ( final String name ) {
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
        if ( ! ( obj instanceof XYCoordinateType ) ) {
            return false;
        }
        final XYCoordinateType order = ( XYCoordinateType ) obj;
        return this.name.equals ( order.toString() );
    }
    private Object readResolve() throws ObjectStreamException {
        if ( this.equals ( XYCoordinateType.DATA ) ) {
            return XYCoordinateType.DATA;
        }
        if ( this.equals ( XYCoordinateType.RELATIVE ) ) {
            return XYCoordinateType.RELATIVE;
        }
        if ( this.equals ( XYCoordinateType.INDEX ) ) {
            return XYCoordinateType.INDEX;
        }
        return null;
    }
    static {
        DATA = new XYCoordinateType ( "XYCoordinateType.DATA" );
        RELATIVE = new XYCoordinateType ( "XYCoordinateType.RELATIVE" );
        INDEX = new XYCoordinateType ( "XYCoordinateType.INDEX" );
    }
}
