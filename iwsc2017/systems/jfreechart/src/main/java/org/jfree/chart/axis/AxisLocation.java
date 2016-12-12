

package org.jfree.chart.axis;

import java.io.ObjectStreamException;
import java.io.Serializable;
import org.jfree.chart.util.ParamChecks;


public final class AxisLocation implements Serializable {


    private static final long serialVersionUID = -3276922179323563410L;


    public static final AxisLocation TOP_OR_LEFT = new AxisLocation (
        "AxisLocation.TOP_OR_LEFT" );


    public static final AxisLocation TOP_OR_RIGHT = new AxisLocation (
        "AxisLocation.TOP_OR_RIGHT" );


    public static final AxisLocation BOTTOM_OR_LEFT = new AxisLocation (
        "AxisLocation.BOTTOM_OR_LEFT" );


    public static final AxisLocation BOTTOM_OR_RIGHT = new AxisLocation (
        "AxisLocation.BOTTOM_OR_RIGHT" );


    private String name;


    private AxisLocation ( String name ) {
        this.name = name;
    }


    public AxisLocation getOpposite() {
        return getOpposite ( this );
    }


    @Override
    public String toString() {
        return this.name;
    }


    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( ! ( obj instanceof AxisLocation ) ) {
            return false;
        }
        AxisLocation location = ( AxisLocation ) obj;
        if ( !this.name.equals ( location.toString() ) ) {
            return false;
        }
        return true;
    }


    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + this.name.hashCode();
        return hash;
    }


    public static AxisLocation getOpposite ( AxisLocation location ) {
        ParamChecks.nullNotPermitted ( location, "location" );
        AxisLocation result = null;
        if ( location == AxisLocation.TOP_OR_LEFT ) {
            result = AxisLocation.BOTTOM_OR_RIGHT;
        } else if ( location == AxisLocation.TOP_OR_RIGHT ) {
            result = AxisLocation.BOTTOM_OR_LEFT;
        } else if ( location == AxisLocation.BOTTOM_OR_LEFT ) {
            result = AxisLocation.TOP_OR_RIGHT;
        } else if ( location == AxisLocation.BOTTOM_OR_RIGHT ) {
            result = AxisLocation.TOP_OR_LEFT;
        } else {
            throw new IllegalStateException ( "AxisLocation not recognised." );
        }
        return result;
    }


    private Object readResolve() throws ObjectStreamException {
        if ( this.equals ( AxisLocation.TOP_OR_RIGHT ) ) {
            return AxisLocation.TOP_OR_RIGHT;
        } else if ( this.equals ( AxisLocation.BOTTOM_OR_RIGHT ) ) {
            return AxisLocation.BOTTOM_OR_RIGHT;
        } else if ( this.equals ( AxisLocation.TOP_OR_LEFT ) ) {
            return AxisLocation.TOP_OR_LEFT;
        } else if ( this.equals ( AxisLocation.BOTTOM_OR_LEFT ) ) {
            return AxisLocation.BOTTOM_OR_LEFT;
        }
        return null;
    }

}
