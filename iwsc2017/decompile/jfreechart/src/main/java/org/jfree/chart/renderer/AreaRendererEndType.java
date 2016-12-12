package org.jfree.chart.renderer;
import java.io.ObjectStreamException;
import java.io.Serializable;
public final class AreaRendererEndType implements Serializable {
    private static final long serialVersionUID = -1774146392916359839L;
    public static final AreaRendererEndType TAPER;
    public static final AreaRendererEndType TRUNCATE;
    public static final AreaRendererEndType LEVEL;
    private String name;
    private AreaRendererEndType ( final String name ) {
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
        if ( ! ( obj instanceof AreaRendererEndType ) ) {
            return false;
        }
        final AreaRendererEndType that = ( AreaRendererEndType ) obj;
        return this.name.equals ( that.toString() );
    }
    private Object readResolve() throws ObjectStreamException {
        Object result = null;
        if ( this.equals ( AreaRendererEndType.LEVEL ) ) {
            result = AreaRendererEndType.LEVEL;
        } else if ( this.equals ( AreaRendererEndType.TAPER ) ) {
            result = AreaRendererEndType.TAPER;
        } else if ( this.equals ( AreaRendererEndType.TRUNCATE ) ) {
            result = AreaRendererEndType.TRUNCATE;
        }
        return result;
    }
    static {
        TAPER = new AreaRendererEndType ( "AreaRendererEndType.TAPER" );
        TRUNCATE = new AreaRendererEndType ( "AreaRendererEndType.TRUNCATE" );
        LEVEL = new AreaRendererEndType ( "AreaRendererEndType.LEVEL" );
    }
}
