package org.jfree.chart.block;
import java.io.ObjectStreamException;
import java.io.Serializable;
public final class LengthConstraintType implements Serializable {
    private static final long serialVersionUID = -1156658804028142978L;
    public static final LengthConstraintType NONE;
    public static final LengthConstraintType RANGE;
    public static final LengthConstraintType FIXED;
    private String name;
    private LengthConstraintType ( final String name ) {
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
        if ( ! ( obj instanceof LengthConstraintType ) ) {
            return false;
        }
        final LengthConstraintType that = ( LengthConstraintType ) obj;
        return this.name.equals ( that.toString() );
    }
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    private Object readResolve() throws ObjectStreamException {
        if ( this.equals ( LengthConstraintType.NONE ) ) {
            return LengthConstraintType.NONE;
        }
        if ( this.equals ( LengthConstraintType.RANGE ) ) {
            return LengthConstraintType.RANGE;
        }
        if ( this.equals ( LengthConstraintType.FIXED ) ) {
            return LengthConstraintType.FIXED;
        }
        return null;
    }
    static {
        NONE = new LengthConstraintType ( "LengthConstraintType.NONE" );
        RANGE = new LengthConstraintType ( "RectangleConstraintType.RANGE" );
        FIXED = new LengthConstraintType ( "LengthConstraintType.FIXED" );
    }
}
