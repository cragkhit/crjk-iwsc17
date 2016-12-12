package org.jfree.data;
import java.io.ObjectStreamException;
import java.io.Serializable;
public final class RangeType implements Serializable {
    private static final long serialVersionUID = -9073319010650549239L;
    public static final RangeType FULL;
    public static final RangeType POSITIVE;
    public static final RangeType NEGATIVE;
    private String name;
    private RangeType ( final String name ) {
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
        if ( ! ( obj instanceof RangeType ) ) {
            return false;
        }
        final RangeType that = ( RangeType ) obj;
        return this.name.equals ( that.toString() );
    }
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    private Object readResolve() throws ObjectStreamException {
        if ( this.equals ( RangeType.FULL ) ) {
            return RangeType.FULL;
        }
        if ( this.equals ( RangeType.POSITIVE ) ) {
            return RangeType.POSITIVE;
        }
        if ( this.equals ( RangeType.NEGATIVE ) ) {
            return RangeType.NEGATIVE;
        }
        return null;
    }
    static {
        FULL = new RangeType ( "RangeType.FULL" );
        POSITIVE = new RangeType ( "RangeType.POSITIVE" );
        NEGATIVE = new RangeType ( "RangeType.NEGATIVE" );
    }
}
