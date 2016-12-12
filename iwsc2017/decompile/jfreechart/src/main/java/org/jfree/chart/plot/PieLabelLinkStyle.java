package org.jfree.chart.plot;
import java.io.ObjectStreamException;
import java.io.Serializable;
public final class PieLabelLinkStyle implements Serializable {
    public static final PieLabelLinkStyle STANDARD;
    public static final PieLabelLinkStyle QUAD_CURVE;
    public static final PieLabelLinkStyle CUBIC_CURVE;
    private String name;
    private PieLabelLinkStyle ( final String name ) {
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
        if ( ! ( obj instanceof PieLabelLinkStyle ) ) {
            return false;
        }
        final PieLabelLinkStyle style = ( PieLabelLinkStyle ) obj;
        return this.name.equals ( style.toString() );
    }
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    private Object readResolve() throws ObjectStreamException {
        Object result = null;
        if ( this.equals ( PieLabelLinkStyle.STANDARD ) ) {
            result = PieLabelLinkStyle.STANDARD;
        } else if ( this.equals ( PieLabelLinkStyle.QUAD_CURVE ) ) {
            result = PieLabelLinkStyle.QUAD_CURVE;
        } else if ( this.equals ( PieLabelLinkStyle.CUBIC_CURVE ) ) {
            result = PieLabelLinkStyle.CUBIC_CURVE;
        }
        return result;
    }
    static {
        STANDARD = new PieLabelLinkStyle ( "PieLabelLinkStyle.STANDARD" );
        QUAD_CURVE = new PieLabelLinkStyle ( "PieLabelLinkStyle.QUAD_CURVE" );
        CUBIC_CURVE = new PieLabelLinkStyle ( "PieLabelLinkStyle.CUBIC_CURVE" );
    }
}
