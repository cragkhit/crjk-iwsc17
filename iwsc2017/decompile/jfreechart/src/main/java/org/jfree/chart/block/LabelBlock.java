package org.jfree.chart.block;
import java.awt.Color;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;
import java.awt.geom.Point2D;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.ChartEntity;
import java.awt.Shape;
import org.jfree.chart.entity.StandardEntityCollection;
import java.awt.geom.Rectangle2D;
import org.jfree.ui.Size2D;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleAnchor;
import org.jfree.text.TextBlockAnchor;
import java.awt.Paint;
import java.awt.Font;
import org.jfree.text.TextBlock;
import org.jfree.util.PublicCloneable;
public class LabelBlock extends AbstractBlock implements Block, PublicCloneable {
    static final long serialVersionUID = 249626098864178017L;
    private String text;
    private TextBlock label;
    private Font font;
    private String toolTipText;
    private String urlText;
    public static final Paint DEFAULT_PAINT;
    private transient Paint paint;
    private TextBlockAnchor contentAlignmentPoint;
    private RectangleAnchor textAnchor;
    public LabelBlock ( final String label ) {
        this ( label, new Font ( "SansSerif", 0, 10 ), LabelBlock.DEFAULT_PAINT );
    }
    public LabelBlock ( final String text, final Font font ) {
        this ( text, font, LabelBlock.DEFAULT_PAINT );
    }
    public LabelBlock ( final String text, final Font font, final Paint paint ) {
        this.text = text;
        this.paint = paint;
        this.label = TextUtilities.createTextBlock ( text, font, this.paint );
        this.font = font;
        this.toolTipText = null;
        this.urlText = null;
        this.contentAlignmentPoint = TextBlockAnchor.CENTER;
        this.textAnchor = RectangleAnchor.CENTER;
    }
    public Font getFont() {
        return this.font;
    }
    public void setFont ( final Font font ) {
        ParamChecks.nullNotPermitted ( font, "font" );
        this.font = font;
        this.label = TextUtilities.createTextBlock ( this.text, font, this.paint );
    }
    public Paint getPaint() {
        return this.paint;
    }
    public void setPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.paint = paint;
        this.label = TextUtilities.createTextBlock ( this.text, this.font, this.paint );
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
    public TextBlockAnchor getContentAlignmentPoint() {
        return this.contentAlignmentPoint;
    }
    public void setContentAlignmentPoint ( final TextBlockAnchor anchor ) {
        ParamChecks.nullNotPermitted ( anchor, "anchor" );
        this.contentAlignmentPoint = anchor;
    }
    public RectangleAnchor getTextAnchor() {
        return this.textAnchor;
    }
    public void setTextAnchor ( final RectangleAnchor anchor ) {
        this.textAnchor = anchor;
    }
    @Override
    public Size2D arrange ( final Graphics2D g2, final RectangleConstraint constraint ) {
        g2.setFont ( this.font );
        final Size2D s = this.label.calculateDimensions ( g2 );
        return new Size2D ( this.calculateTotalWidth ( s.getWidth() ), this.calculateTotalHeight ( s.getHeight() ) );
    }
    public void draw ( final Graphics2D g2, final Rectangle2D area ) {
        this.draw ( g2, area, null );
    }
    @Override
    public Object draw ( final Graphics2D g2, Rectangle2D area, final Object params ) {
        area = this.trimMargin ( area );
        this.drawBorder ( g2, area );
        area = this.trimBorder ( area );
        area = this.trimPadding ( area );
        EntityBlockParams ebp = null;
        StandardEntityCollection sec = null;
        Shape entityArea = null;
        if ( params instanceof EntityBlockParams ) {
            ebp = ( EntityBlockParams ) params;
            if ( ebp.getGenerateEntities() ) {
                sec = new StandardEntityCollection();
                entityArea = ( Shape ) area.clone();
            }
        }
        g2.setPaint ( this.paint );
        g2.setFont ( this.font );
        final Point2D pt = RectangleAnchor.coordinates ( area, this.textAnchor );
        this.label.draw ( g2, ( float ) pt.getX(), ( float ) pt.getY(), this.contentAlignmentPoint );
        BlockResult result = null;
        if ( ebp != null && sec != null && ( this.toolTipText != null || this.urlText != null ) ) {
            final ChartEntity entity = new ChartEntity ( entityArea, this.toolTipText, this.urlText );
            sec.add ( entity );
            result = new BlockResult();
            result.setEntityCollection ( sec );
        }
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( ! ( obj instanceof LabelBlock ) ) {
            return false;
        }
        final LabelBlock that = ( LabelBlock ) obj;
        return this.text.equals ( that.text ) && this.font.equals ( that.font ) && PaintUtilities.equal ( this.paint, that.paint ) && ObjectUtilities.equal ( ( Object ) this.toolTipText, ( Object ) that.toolTipText ) && ObjectUtilities.equal ( ( Object ) this.urlText, ( Object ) that.urlText ) && this.contentAlignmentPoint.equals ( ( Object ) that.contentAlignmentPoint ) && this.textAnchor.equals ( ( Object ) that.textAnchor ) && super.equals ( obj );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.paint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.paint = SerialUtilities.readPaint ( stream );
    }
    static {
        DEFAULT_PAINT = Color.black;
    }
}
