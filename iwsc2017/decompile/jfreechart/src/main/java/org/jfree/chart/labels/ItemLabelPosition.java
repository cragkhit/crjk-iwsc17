package org.jfree.chart.labels;
import org.jfree.chart.util.ParamChecks;
import org.jfree.ui.TextAnchor;
import java.io.Serializable;
public class ItemLabelPosition implements Serializable {
    private static final long serialVersionUID = 5845390630157034499L;
    private ItemLabelAnchor itemLabelAnchor;
    private TextAnchor textAnchor;
    private TextAnchor rotationAnchor;
    private double angle;
    public ItemLabelPosition() {
        this ( ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER, TextAnchor.CENTER, 0.0 );
    }
    public ItemLabelPosition ( final ItemLabelAnchor itemLabelAnchor, final TextAnchor textAnchor ) {
        this ( itemLabelAnchor, textAnchor, TextAnchor.CENTER, 0.0 );
    }
    public ItemLabelPosition ( final ItemLabelAnchor itemLabelAnchor, final TextAnchor textAnchor, final TextAnchor rotationAnchor, final double angle ) {
        ParamChecks.nullNotPermitted ( itemLabelAnchor, "itemLabelAnchor" );
        ParamChecks.nullNotPermitted ( textAnchor, "textAnchor" );
        ParamChecks.nullNotPermitted ( rotationAnchor, "rotationAnchor" );
        this.itemLabelAnchor = itemLabelAnchor;
        this.textAnchor = textAnchor;
        this.rotationAnchor = rotationAnchor;
        this.angle = angle;
    }
    public ItemLabelAnchor getItemLabelAnchor() {
        return this.itemLabelAnchor;
    }
    public TextAnchor getTextAnchor() {
        return this.textAnchor;
    }
    public TextAnchor getRotationAnchor() {
        return this.rotationAnchor;
    }
    public double getAngle() {
        return this.angle;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof ItemLabelPosition ) ) {
            return false;
        }
        final ItemLabelPosition that = ( ItemLabelPosition ) obj;
        return this.itemLabelAnchor.equals ( that.itemLabelAnchor ) && this.textAnchor.equals ( ( Object ) that.textAnchor ) && this.rotationAnchor.equals ( ( Object ) that.rotationAnchor ) && this.angle == that.angle;
    }
}
