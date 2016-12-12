package org.jfree.chart.axis;
import java.io.ObjectStreamException;
import java.io.Serializable;
public final class DateTickMarkPosition implements Serializable {
    private static final long serialVersionUID = 2540750672764537240L;
    public static final DateTickMarkPosition START;
    public static final DateTickMarkPosition MIDDLE;
    public static final DateTickMarkPosition END;
    private String name;
    private DateTickMarkPosition ( final String name ) {
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
        if ( ! ( obj instanceof DateTickMarkPosition ) ) {
            return false;
        }
        final DateTickMarkPosition position = ( DateTickMarkPosition ) obj;
        return this.name.equals ( position.toString() );
    }
    private Object readResolve() throws ObjectStreamException {
        if ( this.equals ( DateTickMarkPosition.START ) ) {
            return DateTickMarkPosition.START;
        }
        if ( this.equals ( DateTickMarkPosition.MIDDLE ) ) {
            return DateTickMarkPosition.MIDDLE;
        }
        if ( this.equals ( DateTickMarkPosition.END ) ) {
            return DateTickMarkPosition.END;
        }
        return null;
    }
    static {
        START = new DateTickMarkPosition ( "DateTickMarkPosition.START" );
        MIDDLE = new DateTickMarkPosition ( "DateTickMarkPosition.MIDDLE" );
        END = new DateTickMarkPosition ( "DateTickMarkPosition.END" );
    }
}
