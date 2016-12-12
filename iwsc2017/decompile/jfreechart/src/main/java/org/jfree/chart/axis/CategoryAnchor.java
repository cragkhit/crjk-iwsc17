package org.jfree.chart.axis;
import java.io.ObjectStreamException;
import java.io.Serializable;
public final class CategoryAnchor implements Serializable {
    private static final long serialVersionUID = -2604142742210173810L;
    public static final CategoryAnchor START;
    public static final CategoryAnchor MIDDLE;
    public static final CategoryAnchor END;
    private String name;
    private CategoryAnchor ( final String name ) {
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
        if ( ! ( obj instanceof CategoryAnchor ) ) {
            return false;
        }
        final CategoryAnchor position = ( CategoryAnchor ) obj;
        return this.name.equals ( position.toString() );
    }
    private Object readResolve() throws ObjectStreamException {
        if ( this.equals ( CategoryAnchor.START ) ) {
            return CategoryAnchor.START;
        }
        if ( this.equals ( CategoryAnchor.MIDDLE ) ) {
            return CategoryAnchor.MIDDLE;
        }
        if ( this.equals ( CategoryAnchor.END ) ) {
            return CategoryAnchor.END;
        }
        return null;
    }
    static {
        START = new CategoryAnchor ( "CategoryAnchor.START" );
        MIDDLE = new CategoryAnchor ( "CategoryAnchor.MIDDLE" );
        END = new CategoryAnchor ( "CategoryAnchor.END" );
    }
}
