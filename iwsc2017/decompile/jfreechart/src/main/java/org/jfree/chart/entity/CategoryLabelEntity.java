package org.jfree.chart.entity;
import org.jfree.chart.HashUtilities;
import org.jfree.util.ObjectUtilities;
import java.awt.Shape;
public class CategoryLabelEntity extends TickLabelEntity {
    private Comparable key;
    public CategoryLabelEntity ( final Comparable key, final Shape area, final String toolTipText, final String urlText ) {
        super ( area, toolTipText, urlText );
        this.key = key;
    }
    public Comparable getKey() {
        return this.key;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof CategoryLabelEntity ) ) {
            return false;
        }
        final CategoryLabelEntity that = ( CategoryLabelEntity ) obj;
        return ObjectUtilities.equal ( ( Object ) this.key, ( Object ) that.key ) && super.equals ( obj );
    }
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = HashUtilities.hashCode ( result, this.key );
        return result;
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "CategoryLabelEntity: " );
        sb.append ( "category=" );
        sb.append ( this.key );
        sb.append ( ", tooltip=" ).append ( this.getToolTipText() );
        sb.append ( ", url=" ).append ( this.getURLText() );
        return sb.toString();
    }
}
