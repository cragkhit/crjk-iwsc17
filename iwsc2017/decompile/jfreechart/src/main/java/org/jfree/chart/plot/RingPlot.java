package org.jfree.chart.plot;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PaintUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.util.LineUtilities;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.text.TextUtilities;
import org.jfree.ui.TextAnchor;
import java.awt.Shape;
import org.jfree.util.ShapeUtilities;
import java.awt.geom.Line2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.UnitType;
import java.awt.geom.Arc2D;
import org.jfree.util.Rotation;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import java.awt.BasicStroke;
import java.text.DecimalFormat;
import org.jfree.data.general.PieDataset;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.Color;
import java.awt.Font;
import java.text.Format;
import java.io.Serializable;
public class RingPlot extends PiePlot implements Cloneable, Serializable {
    private static final long serialVersionUID = 1556064784129676620L;
    private CenterTextMode centerTextMode;
    private String centerText;
    private Format centerTextFormatter;
    private Font centerTextFont;
    private Color centerTextColor;
    private boolean separatorsVisible;
    private transient Stroke separatorStroke;
    private transient Paint separatorPaint;
    private double innerSeparatorExtension;
    private double outerSeparatorExtension;
    private double sectionDepth;
    public RingPlot() {
        this ( null );
    }
    public RingPlot ( final PieDataset dataset ) {
        super ( dataset );
        this.centerTextMode = CenterTextMode.NONE;
        this.centerTextFormatter = new DecimalFormat ( "0.00" );
        this.centerTextMode = CenterTextMode.NONE;
        this.centerText = null;
        this.centerTextFormatter = new DecimalFormat ( "0.00" );
        this.centerTextFont = RingPlot.DEFAULT_LABEL_FONT;
        this.centerTextColor = Color.BLACK;
        this.separatorsVisible = true;
        this.separatorStroke = new BasicStroke ( 0.5f );
        this.separatorPaint = Color.gray;
        this.innerSeparatorExtension = 0.2;
        this.outerSeparatorExtension = 0.2;
        this.sectionDepth = 0.2;
    }
    public CenterTextMode getCenterTextMode() {
        return this.centerTextMode;
    }
    public void setCenterTextMode ( final CenterTextMode mode ) {
        ParamChecks.nullNotPermitted ( mode, "mode" );
        this.centerTextMode = mode;
        this.fireChangeEvent();
    }
    public String getCenterText() {
        return this.centerText;
    }
    public void setCenterText ( final String text ) {
        this.centerText = text;
        this.fireChangeEvent();
    }
    public Format getCenterTextFormatter() {
        return this.centerTextFormatter;
    }
    public void setCenterTextFormatter ( final Format formatter ) {
        ParamChecks.nullNotPermitted ( formatter, "formatter" );
        this.centerTextFormatter = formatter;
    }
    public Font getCenterTextFont() {
        return this.centerTextFont;
    }
    public void setCenterTextFont ( final Font font ) {
        ParamChecks.nullNotPermitted ( font, "font" );
        this.centerTextFont = font;
        this.fireChangeEvent();
    }
    public Color getCenterTextColor() {
        return this.centerTextColor;
    }
    public void setCenterTextColor ( final Color color ) {
        ParamChecks.nullNotPermitted ( color, "color" );
        this.centerTextColor = color;
        this.fireChangeEvent();
    }
    public boolean getSeparatorsVisible() {
        return this.separatorsVisible;
    }
    public void setSeparatorsVisible ( final boolean visible ) {
        this.separatorsVisible = visible;
        this.fireChangeEvent();
    }
    public Stroke getSeparatorStroke() {
        return this.separatorStroke;
    }
    public void setSeparatorStroke ( final Stroke stroke ) {
        ParamChecks.nullNotPermitted ( stroke, "stroke" );
        this.separatorStroke = stroke;
        this.fireChangeEvent();
    }
    public Paint getSeparatorPaint() {
        return this.separatorPaint;
    }
    public void setSeparatorPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.separatorPaint = paint;
        this.fireChangeEvent();
    }
    public double getInnerSeparatorExtension() {
        return this.innerSeparatorExtension;
    }
    public void setInnerSeparatorExtension ( final double percent ) {
        this.innerSeparatorExtension = percent;
        this.fireChangeEvent();
    }
    public double getOuterSeparatorExtension() {
        return this.outerSeparatorExtension;
    }
    public void setOuterSeparatorExtension ( final double percent ) {
        this.outerSeparatorExtension = percent;
        this.fireChangeEvent();
    }
    public double getSectionDepth() {
        return this.sectionDepth;
    }
    public void setSectionDepth ( final double sectionDepth ) {
        this.sectionDepth = sectionDepth;
        this.fireChangeEvent();
    }
    @Override
    public PiePlotState initialise ( final Graphics2D g2, final Rectangle2D plotArea, final PiePlot plot, final Integer index, final PlotRenderingInfo info ) {
        final PiePlotState state = super.initialise ( g2, plotArea, plot, index, info );
        state.setPassesRequired ( 3 );
        return state;
    }
    @Override
    protected void drawItem ( final Graphics2D g2, final int section, final Rectangle2D dataArea, final PiePlotState state, final int currentPass ) {
        final PieDataset dataset = this.getDataset();
        final Number n = dataset.getValue ( section );
        if ( n == null ) {
            return;
        }
        final double value = n.doubleValue();
        double angle1 = 0.0;
        double angle2 = 0.0;
        final Rotation direction = this.getDirection();
        if ( direction == Rotation.CLOCKWISE ) {
            angle1 = state.getLatestAngle();
            angle2 = angle1 - value / state.getTotal() * 360.0;
        } else {
            if ( direction != Rotation.ANTICLOCKWISE ) {
                throw new IllegalStateException ( "Rotation type not recognised." );
            }
            angle1 = state.getLatestAngle();
            angle2 = angle1 + value / state.getTotal() * 360.0;
        }
        final double angle3 = angle2 - angle1;
        if ( Math.abs ( angle3 ) > this.getMinimumArcAngleToDraw() ) {
            final Comparable key = this.getSectionKey ( section );
            double ep = 0.0;
            final double mep = this.getMaximumExplodePercent();
            if ( mep > 0.0 ) {
                ep = this.getExplodePercent ( key ) / mep;
            }
            final Rectangle2D arcBounds = this.getArcBounds ( state.getPieArea(), state.getExplodedPieArea(), angle1, angle3, ep );
            final Arc2D.Double arc = new Arc2D.Double ( arcBounds, angle1, angle3, 0 );
            final double depth = this.sectionDepth / 2.0;
            final RectangleInsets s = new RectangleInsets ( UnitType.RELATIVE, depth, depth, depth, depth );
            final Rectangle2D innerArcBounds = new Rectangle2D.Double();
            innerArcBounds.setRect ( arcBounds );
            s.trim ( innerArcBounds );
            final Arc2D.Double arc2 = new Arc2D.Double ( innerArcBounds, angle1 + angle3, -angle3, 0 );
            final GeneralPath path = new GeneralPath();
            path.moveTo ( ( float ) arc.getStartPoint().getX(), ( float ) arc.getStartPoint().getY() );
            path.append ( arc.getPathIterator ( null ), false );
            path.append ( arc2.getPathIterator ( null ), true );
            path.closePath();
            final Line2D separator = new Line2D.Double ( arc2.getEndPoint(), arc.getStartPoint() );
            if ( currentPass == 0 ) {
                final Paint shadowPaint = this.getShadowPaint();
                final double shadowXOffset = this.getShadowXOffset();
                final double shadowYOffset = this.getShadowYOffset();
                if ( shadowPaint != null && this.getShadowGenerator() == null ) {
                    final Shape shadowArc = ShapeUtilities.createTranslatedShape ( ( Shape ) path, ( double ) ( float ) shadowXOffset, ( double ) ( float ) shadowYOffset );
                    g2.setPaint ( shadowPaint );
                    g2.fill ( shadowArc );
                }
            } else if ( currentPass == 1 ) {
                final Paint paint = this.lookupSectionPaint ( key );
                g2.setPaint ( paint );
                g2.fill ( path );
                final Paint outlinePaint = this.lookupSectionOutlinePaint ( key );
                final Stroke outlineStroke = this.lookupSectionOutlineStroke ( key );
                if ( this.getSectionOutlinesVisible() && outlinePaint != null && outlineStroke != null ) {
                    g2.setPaint ( outlinePaint );
                    g2.setStroke ( outlineStroke );
                    g2.draw ( path );
                }
                if ( section == 0 ) {
                    String nstr = null;
                    if ( this.centerTextMode.equals ( CenterTextMode.VALUE ) ) {
                        nstr = this.centerTextFormatter.format ( n );
                    } else if ( this.centerTextMode.equals ( CenterTextMode.FIXED ) ) {
                        nstr = this.centerText;
                    }
                    if ( nstr != null ) {
                        g2.setFont ( this.centerTextFont );
                        g2.setPaint ( this.centerTextColor );
                        TextUtilities.drawAlignedString ( nstr, g2, ( float ) dataArea.getCenterX(), ( float ) dataArea.getCenterY(), TextAnchor.CENTER );
                    }
                }
                if ( state.getInfo() != null ) {
                    final EntityCollection entities = state.getEntityCollection();
                    if ( entities != null ) {
                        String tip = null;
                        final PieToolTipGenerator toolTipGenerator = this.getToolTipGenerator();
                        if ( toolTipGenerator != null ) {
                            tip = toolTipGenerator.generateToolTip ( dataset, key );
                        }
                        String url = null;
                        final PieURLGenerator urlGenerator = this.getURLGenerator();
                        if ( urlGenerator != null ) {
                            url = urlGenerator.generateURL ( dataset, key, this.getPieIndex() );
                        }
                        final PieSectionEntity entity = new PieSectionEntity ( path, dataset, this.getPieIndex(), section, key, tip, url );
                        entities.add ( entity );
                    }
                }
            } else if ( currentPass == 2 && this.separatorsVisible ) {
                final Line2D extendedSeparator = LineUtilities.extendLine ( separator, this.innerSeparatorExtension, this.outerSeparatorExtension );
                g2.setStroke ( this.separatorStroke );
                g2.setPaint ( this.separatorPaint );
                g2.draw ( extendedSeparator );
            }
        }
        state.setLatestAngle ( angle2 );
    }
    @Override
    protected double getLabelLinkDepth() {
        return Math.min ( super.getLabelLinkDepth(), this.getSectionDepth() / 2.0 );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( ! ( obj instanceof RingPlot ) ) {
            return false;
        }
        final RingPlot that = ( RingPlot ) obj;
        return this.centerTextMode.equals ( that.centerTextMode ) && ObjectUtilities.equal ( ( Object ) this.centerText, ( Object ) that.centerText ) && this.centerTextFormatter.equals ( that.centerTextFormatter ) && this.centerTextFont.equals ( that.centerTextFont ) && this.centerTextColor.equals ( that.centerTextColor ) && this.separatorsVisible == that.separatorsVisible && ObjectUtilities.equal ( ( Object ) this.separatorStroke, ( Object ) that.separatorStroke ) && PaintUtilities.equal ( this.separatorPaint, that.separatorPaint ) && this.innerSeparatorExtension == that.innerSeparatorExtension && this.outerSeparatorExtension == that.outerSeparatorExtension && this.sectionDepth == that.sectionDepth && super.equals ( obj );
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeStroke ( this.separatorStroke, stream );
        SerialUtilities.writePaint ( this.separatorPaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.separatorStroke = SerialUtilities.readStroke ( stream );
        this.separatorPaint = SerialUtilities.readPaint ( stream );
    }
}
