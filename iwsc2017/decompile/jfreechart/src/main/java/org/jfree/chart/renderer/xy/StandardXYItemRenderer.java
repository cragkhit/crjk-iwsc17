package org.jfree.chart.renderer.xy;
import java.io.ObjectOutputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectInputStream;
import org.jfree.util.ObjectUtilities;
import java.awt.Point;
import java.awt.Image;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.entity.EntityCollection;
import java.awt.image.ImageObserver;
import org.jfree.chart.plot.Plot;
import org.jfree.util.ShapeUtilities;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.axis.ValueAxis;
import java.awt.geom.GeneralPath;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Paint;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.general.Dataset;
import org.jfree.chart.LegendItem;
import org.jfree.chart.util.ParamChecks;
import org.jfree.util.BooleanUtilities;
import java.awt.geom.Line2D;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import java.awt.Shape;
import org.jfree.util.BooleanList;
import org.jfree.util.UnitType;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class StandardXYItemRenderer extends AbstractXYItemRenderer implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = -3271351259436865995L;
    public static final int SHAPES = 1;
    public static final int LINES = 2;
    public static final int SHAPES_AND_LINES = 3;
    public static final int IMAGES = 4;
    public static final int DISCONTINUOUS = 8;
    public static final int DISCONTINUOUS_LINES = 10;
    private boolean baseShapesVisible;
    private boolean plotLines;
    private boolean plotImages;
    private boolean plotDiscontinuous;
    private UnitType gapThresholdType;
    private double gapThreshold;
    private Boolean shapesFilled;
    private BooleanList seriesShapesFilled;
    private boolean baseShapesFilled;
    private boolean drawSeriesLineAsPath;
    private transient Shape legendLine;
    public StandardXYItemRenderer() {
        this ( 2, null );
    }
    public StandardXYItemRenderer ( final int type ) {
        this ( type, null );
    }
    public StandardXYItemRenderer ( final int type, final XYToolTipGenerator toolTipGenerator ) {
        this ( type, toolTipGenerator, null );
    }
    public StandardXYItemRenderer ( final int type, final XYToolTipGenerator toolTipGenerator, final XYURLGenerator urlGenerator ) {
        this.gapThresholdType = UnitType.RELATIVE;
        this.gapThreshold = 1.0;
        this.setBaseToolTipGenerator ( toolTipGenerator );
        this.setURLGenerator ( urlGenerator );
        if ( ( type & 0x1 ) != 0x0 ) {
            this.baseShapesVisible = true;
        }
        if ( ( type & 0x2 ) != 0x0 ) {
            this.plotLines = true;
        }
        if ( ( type & 0x4 ) != 0x0 ) {
            this.plotImages = true;
        }
        if ( ( type & 0x8 ) != 0x0 ) {
            this.plotDiscontinuous = true;
        }
        this.shapesFilled = null;
        this.seriesShapesFilled = new BooleanList();
        this.baseShapesFilled = true;
        this.legendLine = new Line2D.Double ( -7.0, 0.0, 7.0, 0.0 );
        this.drawSeriesLineAsPath = false;
    }
    public boolean getBaseShapesVisible() {
        return this.baseShapesVisible;
    }
    public void setBaseShapesVisible ( final boolean flag ) {
        if ( this.baseShapesVisible != flag ) {
            this.baseShapesVisible = flag;
            this.fireChangeEvent();
        }
    }
    public boolean getItemShapeFilled ( final int series, final int item ) {
        if ( this.shapesFilled != null ) {
            return this.shapesFilled;
        }
        final Boolean flag = this.seriesShapesFilled.getBoolean ( series );
        if ( flag != null ) {
            return flag;
        }
        return this.baseShapesFilled;
    }
    public Boolean getShapesFilled() {
        return this.shapesFilled;
    }
    public void setShapesFilled ( final boolean filled ) {
        this.setShapesFilled ( BooleanUtilities.valueOf ( filled ) );
    }
    public void setShapesFilled ( final Boolean filled ) {
        this.shapesFilled = filled;
        this.fireChangeEvent();
    }
    public Boolean getSeriesShapesFilled ( final int series ) {
        return this.seriesShapesFilled.getBoolean ( series );
    }
    public void setSeriesShapesFilled ( final int series, final Boolean flag ) {
        this.seriesShapesFilled.setBoolean ( series, flag );
        this.fireChangeEvent();
    }
    public boolean getBaseShapesFilled() {
        return this.baseShapesFilled;
    }
    public void setBaseShapesFilled ( final boolean flag ) {
        this.baseShapesFilled = flag;
    }
    public boolean getPlotLines() {
        return this.plotLines;
    }
    public void setPlotLines ( final boolean flag ) {
        if ( this.plotLines != flag ) {
            this.plotLines = flag;
            this.fireChangeEvent();
        }
    }
    public UnitType getGapThresholdType() {
        return this.gapThresholdType;
    }
    public void setGapThresholdType ( final UnitType thresholdType ) {
        ParamChecks.nullNotPermitted ( thresholdType, "thresholdType" );
        this.gapThresholdType = thresholdType;
        this.fireChangeEvent();
    }
    public double getGapThreshold() {
        return this.gapThreshold;
    }
    public void setGapThreshold ( final double t ) {
        this.gapThreshold = t;
        this.fireChangeEvent();
    }
    public boolean getPlotImages() {
        return this.plotImages;
    }
    public void setPlotImages ( final boolean flag ) {
        if ( this.plotImages != flag ) {
            this.plotImages = flag;
            this.fireChangeEvent();
        }
    }
    public boolean getPlotDiscontinuous() {
        return this.plotDiscontinuous;
    }
    public void setPlotDiscontinuous ( final boolean flag ) {
        if ( this.plotDiscontinuous != flag ) {
            this.plotDiscontinuous = flag;
            this.fireChangeEvent();
        }
    }
    public boolean getDrawSeriesLineAsPath() {
        return this.drawSeriesLineAsPath;
    }
    public void setDrawSeriesLineAsPath ( final boolean flag ) {
        this.drawSeriesLineAsPath = flag;
    }
    public Shape getLegendLine() {
        return this.legendLine;
    }
    public void setLegendLine ( final Shape line ) {
        ParamChecks.nullNotPermitted ( line, "line" );
        this.legendLine = line;
        this.fireChangeEvent();
    }
    @Override
    public LegendItem getLegendItem ( final int datasetIndex, final int series ) {
        final XYPlot plot = this.getPlot();
        if ( plot == null ) {
            return null;
        }
        LegendItem result = null;
        final XYDataset dataset = plot.getDataset ( datasetIndex );
        if ( dataset != null && this.getItemVisible ( series, 0 ) ) {
            final String description;
            final String label = description = this.getLegendItemLabelGenerator().generateLabel ( dataset, series );
            String toolTipText = null;
            if ( this.getLegendItemToolTipGenerator() != null ) {
                toolTipText = this.getLegendItemToolTipGenerator().generateLabel ( dataset, series );
            }
            String urlText = null;
            if ( this.getLegendItemURLGenerator() != null ) {
                urlText = this.getLegendItemURLGenerator().generateLabel ( dataset, series );
            }
            final Shape shape = this.lookupLegendShape ( series );
            final boolean shapeFilled = this.getItemShapeFilled ( series, 0 );
            final Paint linePaint;
            final Paint paint = linePaint = this.lookupSeriesPaint ( series );
            final Stroke lineStroke = this.lookupSeriesStroke ( series );
            result = new LegendItem ( label, description, toolTipText, urlText, this.baseShapesVisible, shape, shapeFilled, paint, !shapeFilled, paint, lineStroke, this.plotLines, this.legendLine, lineStroke, linePaint );
            result.setLabelFont ( this.lookupLegendTextFont ( series ) );
            final Paint labelPaint = this.lookupLegendTextPaint ( series );
            if ( labelPaint != null ) {
                result.setLabelPaint ( labelPaint );
            }
            result.setDataset ( dataset );
            result.setDatasetIndex ( datasetIndex );
            result.setSeriesKey ( dataset.getSeriesKey ( series ) );
            result.setSeriesIndex ( series );
        }
        return result;
    }
    @Override
    public XYItemRendererState initialise ( final Graphics2D g2, final Rectangle2D dataArea, final XYPlot plot, final XYDataset data, final PlotRenderingInfo info ) {
        final State state = new State ( info );
        state.seriesPath = new GeneralPath();
        state.seriesIndex = -1;
        return state;
    }
    @Override
    public void drawItem ( final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea, final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass ) {
        boolean itemVisible = this.getItemVisible ( series, item );
        Shape entityArea = null;
        EntityCollection entities = null;
        if ( info != null ) {
            entities = info.getOwner().getEntityCollection();
        }
        final PlotOrientation orientation = plot.getOrientation();
        final Paint paint = this.getItemPaint ( series, item );
        final Stroke seriesStroke = this.getItemStroke ( series, item );
        g2.setPaint ( paint );
        g2.setStroke ( seriesStroke );
        final double x1 = dataset.getXValue ( series, item );
        final double y1 = dataset.getYValue ( series, item );
        if ( Double.isNaN ( x1 ) || Double.isNaN ( y1 ) ) {
            itemVisible = false;
        }
        final RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        final RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
        final double transX1 = domainAxis.valueToJava2D ( x1, dataArea, xAxisLocation );
        final double transY1 = rangeAxis.valueToJava2D ( y1, dataArea, yAxisLocation );
        if ( this.getPlotLines() ) {
            if ( this.drawSeriesLineAsPath ) {
                final State s = ( State ) state;
                if ( s.getSeriesIndex() != series ) {
                    s.seriesPath.reset();
                    s.lastPointGood = false;
                    s.setSeriesIndex ( series );
                }
                if ( itemVisible && !Double.isNaN ( transX1 ) && !Double.isNaN ( transY1 ) ) {
                    float x2 = ( float ) transX1;
                    float y2 = ( float ) transY1;
                    if ( orientation == PlotOrientation.HORIZONTAL ) {
                        x2 = ( float ) transY1;
                        y2 = ( float ) transX1;
                    }
                    if ( s.isLastPointGood() ) {
                        s.seriesPath.lineTo ( x2, y2 );
                    } else {
                        s.seriesPath.moveTo ( x2, y2 );
                    }
                    s.setLastPointGood ( true );
                } else {
                    s.setLastPointGood ( false );
                }
                if ( item == dataset.getItemCount ( series ) - 1 && s.seriesIndex == series ) {
                    g2.setStroke ( this.lookupSeriesStroke ( series ) );
                    g2.setPaint ( this.lookupSeriesPaint ( series ) );
                    g2.draw ( s.seriesPath );
                }
            } else if ( item != 0 && itemVisible ) {
                final double x3 = dataset.getXValue ( series, item - 1 );
                final double y3 = dataset.getYValue ( series, item - 1 );
                if ( !Double.isNaN ( x3 ) && !Double.isNaN ( y3 ) ) {
                    boolean drawLine = true;
                    if ( this.getPlotDiscontinuous() ) {
                        final int numX = dataset.getItemCount ( series );
                        final double minX = dataset.getXValue ( series, 0 );
                        final double maxX = dataset.getXValue ( series, numX - 1 );
                        if ( this.gapThresholdType == UnitType.ABSOLUTE ) {
                            drawLine = ( Math.abs ( x1 - x3 ) <= this.gapThreshold );
                        } else {
                            drawLine = ( Math.abs ( x1 - x3 ) <= ( maxX - minX ) / numX * this.getGapThreshold() );
                        }
                    }
                    if ( drawLine ) {
                        final double transX2 = domainAxis.valueToJava2D ( x3, dataArea, xAxisLocation );
                        final double transY2 = rangeAxis.valueToJava2D ( y3, dataArea, yAxisLocation );
                        if ( Double.isNaN ( transX2 ) || Double.isNaN ( transY2 ) || Double.isNaN ( transX1 ) || Double.isNaN ( transY1 ) ) {
                            return;
                        }
                        if ( orientation == PlotOrientation.HORIZONTAL ) {
                            state.workingLine.setLine ( transY2, transX2, transY1, transX1 );
                        } else if ( orientation == PlotOrientation.VERTICAL ) {
                            state.workingLine.setLine ( transX2, transY2, transX1, transY1 );
                        }
                        if ( state.workingLine.intersects ( dataArea ) ) {
                            g2.draw ( state.workingLine );
                        }
                    }
                }
            }
        }
        if ( !itemVisible ) {
            return;
        }
        if ( this.getBaseShapesVisible() ) {
            Shape shape = this.getItemShape ( series, item );
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                shape = ShapeUtilities.createTranslatedShape ( shape, transY1, transX1 );
            } else if ( orientation == PlotOrientation.VERTICAL ) {
                shape = ShapeUtilities.createTranslatedShape ( shape, transX1, transY1 );
            }
            if ( shape.intersects ( dataArea ) ) {
                if ( this.getItemShapeFilled ( series, item ) ) {
                    g2.fill ( shape );
                } else {
                    g2.draw ( shape );
                }
            }
            entityArea = shape;
        }
        if ( this.getPlotImages() ) {
            final Image image = this.getImage ( plot, series, item, transX1, transY1 );
            if ( image != null ) {
                final Point hotspot = this.getImageHotspot ( plot, series, item, transX1, transY1, image );
                g2.drawImage ( image, ( int ) ( transX1 - hotspot.getX() ), ( int ) ( transY1 - hotspot.getY() ), null );
                entityArea = new Rectangle2D.Double ( transX1 - hotspot.getX(), transY1 - hotspot.getY(), image.getWidth ( null ), image.getHeight ( null ) );
            }
        }
        double xx = transX1;
        double yy = transY1;
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            xx = transY1;
            yy = transX1;
        }
        if ( this.isItemLabelVisible ( series, item ) ) {
            this.drawItemLabel ( g2, orientation, dataset, series, item, xx, yy, y1 < 0.0 );
        }
        final int domainAxisIndex = plot.getDomainAxisIndex ( domainAxis );
        final int rangeAxisIndex = plot.getRangeAxisIndex ( rangeAxis );
        this.updateCrosshairValues ( crosshairState, x1, y1, domainAxisIndex, rangeAxisIndex, transX1, transY1, orientation );
        if ( entities != null && AbstractXYItemRenderer.isPointInRect ( dataArea, xx, yy ) ) {
            this.addEntity ( entities, entityArea, dataset, series, item, xx, yy );
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof StandardXYItemRenderer ) ) {
            return false;
        }
        final StandardXYItemRenderer that = ( StandardXYItemRenderer ) obj;
        return this.baseShapesVisible == that.baseShapesVisible && this.plotLines == that.plotLines && this.plotImages == that.plotImages && this.plotDiscontinuous == that.plotDiscontinuous && this.gapThresholdType == that.gapThresholdType && this.gapThreshold == that.gapThreshold && ObjectUtilities.equal ( ( Object ) this.shapesFilled, ( Object ) that.shapesFilled ) && this.seriesShapesFilled.equals ( ( Object ) that.seriesShapesFilled ) && this.baseShapesFilled == that.baseShapesFilled && this.drawSeriesLineAsPath == that.drawSeriesLineAsPath && ShapeUtilities.equal ( this.legendLine, that.legendLine ) && super.equals ( obj );
    }
    public Object clone() throws CloneNotSupportedException {
        final StandardXYItemRenderer clone = ( StandardXYItemRenderer ) super.clone();
        clone.seriesShapesFilled = ( BooleanList ) this.seriesShapesFilled.clone();
        clone.legendLine = ShapeUtilities.clone ( this.legendLine );
        return clone;
    }
    protected Image getImage ( final Plot plot, final int series, final int item, final double x, final double y ) {
        return null;
    }
    protected Point getImageHotspot ( final Plot plot, final int series, final int item, final double x, final double y, final Image image ) {
        final int height = image.getHeight ( null );
        final int width = image.getWidth ( null );
        return new Point ( width / 2, height / 2 );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.legendLine = SerialUtilities.readShape ( stream );
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeShape ( this.legendLine, stream );
    }
    public static class State extends XYItemRendererState {
        public GeneralPath seriesPath;
        private int seriesIndex;
        private boolean lastPointGood;
        public State ( final PlotRenderingInfo info ) {
            super ( info );
        }
        public boolean isLastPointGood() {
            return this.lastPointGood;
        }
        public void setLastPointGood ( final boolean good ) {
            this.lastPointGood = good;
        }
        public int getSeriesIndex() {
            return this.seriesIndex;
        }
        public void setSeriesIndex ( final int index ) {
            this.seriesIndex = index;
        }
    }
}
