package org.jfree.chart.annotations;
import org.jfree.ui.RectangleEdge;
import org.jfree.data.category.CategoryDataset;
import org.jfree.text.TextUtilities;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.CategoryAxis;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.plot.CategoryPlot;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import org.jfree.chart.axis.CategoryAnchor;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class CategoryTextAnnotation extends TextAnnotation implements CategoryAnnotation, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 3333360090781320147L;
    private Comparable category;
    private CategoryAnchor categoryAnchor;
    private double value;
    public CategoryTextAnnotation ( final String text, final Comparable category, final double value ) {
        super ( text );
        ParamChecks.nullNotPermitted ( category, "category" );
        this.category = category;
        this.value = value;
        this.categoryAnchor = CategoryAnchor.MIDDLE;
    }
    public Comparable getCategory() {
        return this.category;
    }
    public void setCategory ( final Comparable category ) {
        ParamChecks.nullNotPermitted ( category, "category" );
        this.category = category;
        this.fireAnnotationChanged();
    }
    public CategoryAnchor getCategoryAnchor() {
        return this.categoryAnchor;
    }
    public void setCategoryAnchor ( final CategoryAnchor anchor ) {
        ParamChecks.nullNotPermitted ( anchor, "anchor" );
        this.categoryAnchor = anchor;
        this.fireAnnotationChanged();
    }
    public double getValue() {
        return this.value;
    }
    public void setValue ( final double value ) {
        this.value = value;
        this.fireAnnotationChanged();
    }
    @Override
    public void draw ( final Graphics2D g2, final CategoryPlot plot, final Rectangle2D dataArea, final CategoryAxis domainAxis, final ValueAxis rangeAxis ) {
        final CategoryDataset dataset = plot.getDataset();
        final int catIndex = dataset.getColumnIndex ( this.category );
        final int catCount = dataset.getColumnCount();
        float anchorX = 0.0f;
        float anchorY = 0.0f;
        final PlotOrientation orientation = plot.getOrientation();
        final RectangleEdge domainEdge = Plot.resolveDomainAxisLocation ( plot.getDomainAxisLocation(), orientation );
        final RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation ( plot.getRangeAxisLocation(), orientation );
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            anchorY = ( float ) domainAxis.getCategoryJava2DCoordinate ( this.categoryAnchor, catIndex, catCount, dataArea, domainEdge );
            anchorX = ( float ) rangeAxis.valueToJava2D ( this.value, dataArea, rangeEdge );
        } else if ( orientation == PlotOrientation.VERTICAL ) {
            anchorX = ( float ) domainAxis.getCategoryJava2DCoordinate ( this.categoryAnchor, catIndex, catCount, dataArea, domainEdge );
            anchorY = ( float ) rangeAxis.valueToJava2D ( this.value, dataArea, rangeEdge );
        }
        g2.setFont ( this.getFont() );
        g2.setPaint ( this.getPaint() );
        TextUtilities.drawRotatedString ( this.getText(), g2, anchorX, anchorY, this.getTextAnchor(), this.getRotationAngle(), this.getRotationAnchor() );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof CategoryTextAnnotation ) ) {
            return false;
        }
        final CategoryTextAnnotation that = ( CategoryTextAnnotation ) obj;
        return super.equals ( obj ) && this.category.equals ( that.getCategory() ) && this.categoryAnchor.equals ( that.getCategoryAnchor() ) && this.value == that.getValue();
    }
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 37 * result + this.category.hashCode();
        result = 37 * result + this.categoryAnchor.hashCode();
        final long temp = Double.doubleToLongBits ( this.value );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        return result;
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
