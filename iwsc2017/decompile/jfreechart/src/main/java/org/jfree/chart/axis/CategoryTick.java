package org.jfree.chart.axis;
import org.jfree.util.ObjectUtilities;
import org.jfree.ui.TextAnchor;
import org.jfree.text.TextBlockAnchor;
import org.jfree.text.TextBlock;
public class CategoryTick extends Tick {
    private Comparable category;
    private TextBlock label;
    private TextBlockAnchor labelAnchor;
    public CategoryTick ( final Comparable category, final TextBlock label, final TextBlockAnchor labelAnchor, final TextAnchor rotationAnchor, final double angle ) {
        super ( "", TextAnchor.CENTER, rotationAnchor, angle );
        this.category = category;
        this.label = label;
        this.labelAnchor = labelAnchor;
    }
    public Comparable getCategory() {
        return this.category;
    }
    public TextBlock getLabel() {
        return this.label;
    }
    public TextBlockAnchor getLabelAnchor() {
        return this.labelAnchor;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( obj instanceof CategoryTick && super.equals ( obj ) ) {
            final CategoryTick that = ( CategoryTick ) obj;
            return ObjectUtilities.equal ( ( Object ) this.category, ( Object ) that.category ) && ObjectUtilities.equal ( ( Object ) this.label, ( Object ) that.label ) && ObjectUtilities.equal ( ( Object ) this.labelAnchor, ( Object ) that.labelAnchor );
        }
        return false;
    }
    @Override
    public int hashCode() {
        int result = 41;
        result = 37 * result + this.category.hashCode();
        result = 37 * result + this.label.hashCode();
        result = 37 * result + this.labelAnchor.hashCode();
        return result;
    }
}
