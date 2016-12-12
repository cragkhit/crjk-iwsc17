package org.jfree.chart.plot;
import java.io.ObjectStreamException;
import java.io.Serializable;
public final class PolarAxisLocation implements Serializable {
    private static final long serialVersionUID = -3276922179323563410L;
    public static final PolarAxisLocation NORTH_LEFT;
    public static final PolarAxisLocation NORTH_RIGHT;
    public static final PolarAxisLocation SOUTH_LEFT;
    public static final PolarAxisLocation SOUTH_RIGHT;
    public static final PolarAxisLocation EAST_ABOVE;
    public static final PolarAxisLocation EAST_BELOW;
    public static final PolarAxisLocation WEST_ABOVE;
    public static final PolarAxisLocation WEST_BELOW;
    private String name;
    private PolarAxisLocation ( final String name ) {
        this.name = name;
    }
    @Override
    public String toString() {
        return this.name;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof PolarAxisLocation ) ) {
            return false;
        }
        final PolarAxisLocation location = ( PolarAxisLocation ) obj;
        return this.name.equals ( location.toString() );
    }
    private Object readResolve() throws ObjectStreamException {
        if ( this.equals ( PolarAxisLocation.NORTH_RIGHT ) ) {
            return PolarAxisLocation.NORTH_RIGHT;
        }
        if ( this.equals ( PolarAxisLocation.NORTH_LEFT ) ) {
            return PolarAxisLocation.NORTH_LEFT;
        }
        if ( this.equals ( PolarAxisLocation.SOUTH_RIGHT ) ) {
            return PolarAxisLocation.SOUTH_RIGHT;
        }
        if ( this.equals ( PolarAxisLocation.SOUTH_LEFT ) ) {
            return PolarAxisLocation.SOUTH_LEFT;
        }
        if ( this.equals ( PolarAxisLocation.EAST_ABOVE ) ) {
            return PolarAxisLocation.EAST_ABOVE;
        }
        if ( this.equals ( PolarAxisLocation.EAST_BELOW ) ) {
            return PolarAxisLocation.EAST_BELOW;
        }
        if ( this.equals ( PolarAxisLocation.WEST_ABOVE ) ) {
            return PolarAxisLocation.WEST_ABOVE;
        }
        if ( this.equals ( PolarAxisLocation.WEST_BELOW ) ) {
            return PolarAxisLocation.WEST_BELOW;
        }
        return null;
    }
    static {
        NORTH_LEFT = new PolarAxisLocation ( "PolarAxisLocation.NORTH_LEFT" );
        NORTH_RIGHT = new PolarAxisLocation ( "PolarAxisLocation.NORTH_RIGHT" );
        SOUTH_LEFT = new PolarAxisLocation ( "PolarAxisLocation.SOUTH_LEFT" );
        SOUTH_RIGHT = new PolarAxisLocation ( "PolarAxisLocation.SOUTH_RIGHT" );
        EAST_ABOVE = new PolarAxisLocation ( "PolarAxisLocation.EAST_ABOVE" );
        EAST_BELOW = new PolarAxisLocation ( "PolarAxisLocation.EAST_BELOW" );
        WEST_ABOVE = new PolarAxisLocation ( "PolarAxisLocation.WEST_ABOVE" );
        WEST_BELOW = new PolarAxisLocation ( "PolarAxisLocation.WEST_BELOW" );
    }
}
