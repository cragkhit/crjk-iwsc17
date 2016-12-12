package org.jfree.data.time;
import java.io.ObjectStreamException;
import java.io.Serializable;
public final class TimePeriodAnchor implements Serializable {
    private static final long serialVersionUID = 2011955697457548862L;
    public static final TimePeriodAnchor START;
    public static final TimePeriodAnchor MIDDLE;
    public static final TimePeriodAnchor END;
    private String name;
    private TimePeriodAnchor ( final String name ) {
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
        if ( ! ( obj instanceof TimePeriodAnchor ) ) {
            return false;
        }
        final TimePeriodAnchor position = ( TimePeriodAnchor ) obj;
        return this.name.equals ( position.name );
    }
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    private Object readResolve() throws ObjectStreamException {
        if ( this.equals ( TimePeriodAnchor.START ) ) {
            return TimePeriodAnchor.START;
        }
        if ( this.equals ( TimePeriodAnchor.MIDDLE ) ) {
            return TimePeriodAnchor.MIDDLE;
        }
        if ( this.equals ( TimePeriodAnchor.END ) ) {
            return TimePeriodAnchor.END;
        }
        return null;
    }
    static {
        START = new TimePeriodAnchor ( "TimePeriodAnchor.START" );
        MIDDLE = new TimePeriodAnchor ( "TimePeriodAnchor.MIDDLE" );
        END = new TimePeriodAnchor ( "TimePeriodAnchor.END" );
    }
}
