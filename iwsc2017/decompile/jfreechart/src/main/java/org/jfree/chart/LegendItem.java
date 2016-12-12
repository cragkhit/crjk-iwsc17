package org.jfree.chart;
import java.awt.BasicStroke;
import java.awt.geom.Line2D;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PublicCloneable;
import org.jfree.util.PaintUtilities;
import org.jfree.util.ShapeUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.AttributedStringUtilities;
import java.text.CharacterIterator;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jfree.chart.util.ParamChecks;
import java.awt.geom.Rectangle2D;
import java.awt.Color;
import java.awt.Stroke;
import org.jfree.ui.GradientPaintTransformer;
import java.awt.Shape;
import java.text.AttributedString;
import java.awt.Paint;
import java.awt.Font;
import org.jfree.data.general.Dataset;
import java.io.Serializable;
public class LegendItem implements Cloneable, Serializable {
    private static final long serialVersionUID = -797214582948827144L;
    private Dataset dataset;
    private Comparable seriesKey;
    private int datasetIndex;
    private int series;
    private String label;
    private Font labelFont;
    private transient Paint labelPaint;
    private transient AttributedString attributedLabel;
    private String description;
    private String toolTipText;
    private String urlText;
    private boolean shapeVisible;
    private transient Shape shape;
    private boolean shapeFilled;
    private transient Paint fillPaint;
    private GradientPaintTransformer fillPaintTransformer;
    private boolean shapeOutlineVisible;
    private transient Paint outlinePaint;
    private transient Stroke outlineStroke;
    private boolean lineVisible;
    private transient Shape line;
    private transient Stroke lineStroke;
    private transient Paint linePaint;
    private static final Shape UNUSED_SHAPE;
    private static final Stroke UNUSED_STROKE;
    public LegendItem ( final String label ) {
        this ( label, Color.black );
    }
    public LegendItem ( final String label, final Paint paint ) {
        this ( label, null, null, null, new Rectangle2D.Double ( -4.0, -4.0, 8.0, 8.0 ), paint );
    }
    public LegendItem ( final String label, final String description, final String toolTipText, final String urlText, final Shape shape, final Paint fillPaint ) {
        this ( label, description, toolTipText, urlText, true, shape, true, fillPaint, false, Color.black, LegendItem.UNUSED_STROKE, false, LegendItem.UNUSED_SHAPE, LegendItem.UNUSED_STROKE, Color.black );
    }
    public LegendItem ( final String label, final String description, final String toolTipText, final String urlText, final Shape shape, final Paint fillPaint, final Stroke outlineStroke, final Paint outlinePaint ) {
        this ( label, description, toolTipText, urlText, true, shape, true, fillPaint, true, outlinePaint, outlineStroke, false, LegendItem.UNUSED_SHAPE, LegendItem.UNUSED_STROKE, Color.black );
    }
    public LegendItem ( final String label, final String description, final String toolTipText, final String urlText, final Shape line, final Stroke lineStroke, final Paint linePaint ) {
        this ( label, description, toolTipText, urlText, false, LegendItem.UNUSED_SHAPE, false, Color.black, false, Color.black, LegendItem.UNUSED_STROKE, true, line, lineStroke, linePaint );
    }
    public LegendItem ( final String label, final String description, final String toolTipText, final String urlText, final boolean shapeVisible, final Shape shape, final boolean shapeFilled, final Paint fillPaint, final boolean shapeOutlineVisible, final Paint outlinePaint, final Stroke outlineStroke, final boolean lineVisible, final Shape line, final Stroke lineStroke, final Paint linePaint ) {
        ParamChecks.nullNotPermitted ( label, "label" );
        ParamChecks.nullNotPermitted ( fillPaint, "fillPaint" );
        ParamChecks.nullNotPermitted ( lineStroke, "lineStroke" );
        ParamChecks.nullNotPermitted ( outlinePaint, "outlinePaint" );
        ParamChecks.nullNotPermitted ( outlineStroke, "outlineStroke" );
        this.label = label;
        this.labelPaint = null;
        this.attributedLabel = null;
        this.description = description;
        this.shapeVisible = shapeVisible;
        this.shape = shape;
        this.shapeFilled = shapeFilled;
        this.fillPaint = fillPaint;
        this.fillPaintTransformer = ( GradientPaintTransformer ) new StandardGradientPaintTransformer();
        this.shapeOutlineVisible = shapeOutlineVisible;
        this.outlinePaint = outlinePaint;
        this.outlineStroke = outlineStroke;
        this.lineVisible = lineVisible;
        this.line = line;
        this.lineStroke = lineStroke;
        this.linePaint = linePaint;
        this.toolTipText = toolTipText;
        this.urlText = urlText;
    }
    public LegendItem ( final AttributedString label, final String description, final String toolTipText, final String urlText, final Shape shape, final Paint fillPaint ) {
        this ( label, description, toolTipText, urlText, true, shape, true, fillPaint, false, Color.black, LegendItem.UNUSED_STROKE, false, LegendItem.UNUSED_SHAPE, LegendItem.UNUSED_STROKE, Color.black );
    }
    public LegendItem ( final AttributedString label, final String description, final String toolTipText, final String urlText, final Shape shape, final Paint fillPaint, final Stroke outlineStroke, final Paint outlinePaint ) {
        this ( label, description, toolTipText, urlText, true, shape, true, fillPaint, true, outlinePaint, outlineStroke, false, LegendItem.UNUSED_SHAPE, LegendItem.UNUSED_STROKE, Color.black );
    }
    public LegendItem ( final AttributedString label, final String description, final String toolTipText, final String urlText, final Shape line, final Stroke lineStroke, final Paint linePaint ) {
        this ( label, description, toolTipText, urlText, false, LegendItem.UNUSED_SHAPE, false, Color.black, false, Color.black, LegendItem.UNUSED_STROKE, true, line, lineStroke, linePaint );
    }
    public LegendItem ( final AttributedString label, final String description, final String toolTipText, final String urlText, final boolean shapeVisible, final Shape shape, final boolean shapeFilled, final Paint fillPaint, final boolean shapeOutlineVisible, final Paint outlinePaint, final Stroke outlineStroke, final boolean lineVisible, final Shape line, final Stroke lineStroke, final Paint linePaint ) {
        ParamChecks.nullNotPermitted ( label, "label" );
        ParamChecks.nullNotPermitted ( fillPaint, "fillPaint" );
        ParamChecks.nullNotPermitted ( lineStroke, "lineStroke" );
        ParamChecks.nullNotPermitted ( line, "line" );
        ParamChecks.nullNotPermitted ( linePaint, "linePaint" );
        ParamChecks.nullNotPermitted ( outlinePaint, "outlinePaint" );
        ParamChecks.nullNotPermitted ( outlineStroke, "outlineStroke" );
        this.label = this.characterIteratorToString ( label.getIterator() );
        this.attributedLabel = label;
        this.description = description;
        this.shapeVisible = shapeVisible;
        this.shape = shape;
        this.shapeFilled = shapeFilled;
        this.fillPaint = fillPaint;
        this.fillPaintTransformer = ( GradientPaintTransformer ) new StandardGradientPaintTransformer();
        this.shapeOutlineVisible = shapeOutlineVisible;
        this.outlinePaint = outlinePaint;
        this.outlineStroke = outlineStroke;
        this.lineVisible = lineVisible;
        this.line = line;
        this.lineStroke = lineStroke;
        this.linePaint = linePaint;
        this.toolTipText = toolTipText;
        this.urlText = urlText;
    }
    private String characterIteratorToString ( final CharacterIterator iterator ) {
        final int endIndex = iterator.getEndIndex();
        final int beginIndex = iterator.getBeginIndex();
        final int count = endIndex - beginIndex;
        if ( count <= 0 ) {
            return "";
        }
        final char[] chars = new char[count];
        int i = 0;
        for ( char c = iterator.first(); c != '\uffff'; c = iterator.next() ) {
            chars[i] = c;
            ++i;
        }
        return new String ( chars );
    }
    public Dataset getDataset() {
        return this.dataset;
    }
    public void setDataset ( final Dataset dataset ) {
        this.dataset = dataset;
    }
    public int getDatasetIndex() {
        return this.datasetIndex;
    }
    public void setDatasetIndex ( final int index ) {
        this.datasetIndex = index;
    }
    public Comparable getSeriesKey() {
        return this.seriesKey;
    }
    public void setSeriesKey ( final Comparable key ) {
        this.seriesKey = key;
    }
    public int getSeriesIndex() {
        return this.series;
    }
    public void setSeriesIndex ( final int index ) {
        this.series = index;
    }
    public String getLabel() {
        return this.label;
    }
    public Font getLabelFont() {
        return this.labelFont;
    }
    public void setLabelFont ( final Font font ) {
        this.labelFont = font;
    }
    public Paint getLabelPaint() {
        return this.labelPaint;
    }
    public void setLabelPaint ( final Paint paint ) {
        this.labelPaint = paint;
    }
    public AttributedString getAttributedLabel() {
        return this.attributedLabel;
    }
    public String getDescription() {
        return this.description;
    }
    public void setDescription ( final String text ) {
        this.description = text;
    }
    public String getToolTipText() {
        return this.toolTipText;
    }
    public void setToolTipText ( final String text ) {
        this.toolTipText = text;
    }
    public String getURLText() {
        return this.urlText;
    }
    public void setURLText ( final String text ) {
        this.urlText = text;
    }
    public boolean isShapeVisible() {
        return this.shapeVisible;
    }
    public void setShapeVisible ( final boolean visible ) {
        this.shapeVisible = visible;
    }
    public Shape getShape() {
        return this.shape;
    }
    public void setShape ( final Shape shape ) {
        ParamChecks.nullNotPermitted ( shape, "shape" );
        this.shape = shape;
    }
    public boolean isShapeFilled() {
        return this.shapeFilled;
    }
    public Paint getFillPaint() {
        return this.fillPaint;
    }
    public void setFillPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.fillPaint = paint;
    }
    public boolean isShapeOutlineVisible() {
        return this.shapeOutlineVisible;
    }
    public Stroke getLineStroke() {
        return this.lineStroke;
    }
    public void setLineStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.lineStroke = stroke;
    }
    public Paint getLinePaint() {
        return this.linePaint;
    }
    public void setLinePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.linePaint = paint;
    }
    public Paint getOutlinePaint() {
        return this.outlinePaint;
    }
    public void setOutlinePaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.outlinePaint = paint;
    }
    public Stroke getOutlineStroke() {
        return this.outlineStroke;
    }
    public void setOutlineStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.outlineStroke = stroke;
    }
    public boolean isLineVisible() {
        return this.lineVisible;
    }
    public void setLineVisible ( final boolean visible ) {
        this.lineVisible = visible;
    }
    public Shape getLine() {
        return this.line;
    }
    public void setLine ( final Shape line ) {
        ParamChecks.nullNotPermitted ( line, "line" );
        this.line = line;
    }
    public GradientPaintTransformer getFillPaintTransformer() {
        return this.fillPaintTransformer;
    }
    public void setFillPaintTransformer ( final GradientPaintTransformer transformer ) {
        ParamChecks.nullNotPermitted ( transformer, "transformer" );
        this.fillPaintTransformer = transformer;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof LegendItem ) ) {
            return false;
        }
        final LegendItem that = ( LegendItem ) obj;
        return this.datasetIndex == that.datasetIndex && this.series == that.series && this.label.equals ( that.label ) && AttributedStringUtilities.equal ( this.attributedLabel, that.attributedLabel ) && ObjectUtilities.equal ( ( Object ) this.description, ( Object ) that.description ) && this.shapeVisible == that.shapeVisible && ShapeUtilities.equal ( this.shape, that.shape ) && this.shapeFilled == that.shapeFilled && PaintUtilities.equal ( this.fillPaint, that.fillPaint ) && ObjectUtilities.equal ( ( Object ) this.fillPaintTransformer, ( Object ) that.fillPaintTransformer ) && this.shapeOutlineVisible == that.shapeOutlineVisible && this.outlineStroke.equals ( that.outlineStroke ) && PaintUtilities.equal ( this.outlinePaint, that.outlinePaint ) && !this.lineVisible != that.lineVisible && ShapeUtilities.equal ( this.line, that.line ) && this.lineStroke.equals ( that.lineStroke ) && PaintUtilities.equal ( this.linePaint, that.linePaint ) && ObjectUtilities.equal ( ( Object ) this.labelFont, ( Object ) that.labelFont ) && PaintUtilities.equal ( this.labelPaint, that.labelPaint );
    }
    public Object clone() throws CloneNotSupportedException {
        final LegendItem clone = ( LegendItem ) super.clone();
        if ( this.seriesKey instanceof PublicCloneable ) {
            final PublicCloneable pc = ( PublicCloneable ) this.seriesKey;
            clone.seriesKey = ( Comparable ) pc.clone();
        }
        clone.shape = ShapeUtilities.clone ( this.shape );
        if ( this.fillPaintTransformer instanceof PublicCloneable ) {
            final PublicCloneable pc = ( PublicCloneable ) this.fillPaintTransformer;
            clone.fillPaintTransformer = ( GradientPaintTransformer ) pc.clone();
        }
        clone.line = ShapeUtilities.clone ( this.line );
        return clone;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeAttributedString ( this.attributedLabel, stream );
        SerialUtilities.writeShape ( this.shape, stream );
        SerialUtilities.writePaint ( this.fillPaint, stream );
        SerialUtilities.writeStroke ( this.outlineStroke, stream );
        SerialUtilities.writePaint ( this.outlinePaint, stream );
        SerialUtilities.writeShape ( this.line, stream );
        SerialUtilities.writeStroke ( this.lineStroke, stream );
        SerialUtilities.writePaint ( this.linePaint, stream );
        SerialUtilities.writePaint ( this.labelPaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.attributedLabel = SerialUtilities.readAttributedString ( stream );
        this.shape = SerialUtilities.readShape ( stream );
        this.fillPaint = SerialUtilities.readPaint ( stream );
        this.outlineStroke = SerialUtilities.readStroke ( stream );
        this.outlinePaint = SerialUtilities.readPaint ( stream );
        this.line = SerialUtilities.readShape ( stream );
        this.lineStroke = SerialUtilities.readStroke ( stream );
        this.linePaint = SerialUtilities.readPaint ( stream );
        this.labelPaint = SerialUtilities.readPaint ( stream );
    }
    static {
        UNUSED_SHAPE = new Line2D.Float();
        UNUSED_STROKE = new BasicStroke ( 0.0f );
    }
}
