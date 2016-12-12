package org.jfree.chart.plot;
import org.jfree.chart.util.ResourceBundleWrapper;
import java.util.Collection;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.event.AxisChangeEvent;
import java.beans.PropertyChangeEvent;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.Range;
import java.awt.geom.Line2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.Ellipse2D;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.ContourEntity;
import org.jfree.chart.axis.NumberAxis;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.util.Iterator;
import java.awt.Composite;
import java.awt.AlphaComposite;
import java.awt.Shape;
import org.jfree.chart.axis.AxisSpace;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.annotations.XYAnnotation;
import java.util.ArrayList;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.chart.labels.StandardContourToolTipGenerator;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.data.general.DatasetChangeListener;
import java.util.ResourceBundle;
import org.jfree.chart.ClipPath;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.labels.ContourToolTipGenerator;
import java.util.List;
import java.awt.Paint;
import java.awt.Stroke;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.axis.ColorBar;
import org.jfree.data.contour.ContourDataset;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.ui.RectangleInsets;
import java.io.Serializable;
import java.beans.PropertyChangeListener;
public class ContourPlot extends Plot implements ContourValuePlot, ValueAxisPlot, PropertyChangeListener, Serializable, Cloneable {
    private static final long serialVersionUID = 7861072556590502247L;
    protected static final RectangleInsets DEFAULT_INSETS;
    private ValueAxis domainAxis;
    private ValueAxis rangeAxis;
    private ContourDataset dataset;
    private ColorBar colorBar;
    private RectangleEdge colorBarLocation;
    private boolean domainCrosshairVisible;
    private double domainCrosshairValue;
    private transient Stroke domainCrosshairStroke;
    private transient Paint domainCrosshairPaint;
    private boolean domainCrosshairLockedOnData;
    private boolean rangeCrosshairVisible;
    private double rangeCrosshairValue;
    private transient Stroke rangeCrosshairStroke;
    private transient Paint rangeCrosshairPaint;
    private boolean rangeCrosshairLockedOnData;
    private double dataAreaRatio;
    private List domainMarkers;
    private List rangeMarkers;
    private List annotations;
    private ContourToolTipGenerator toolTipGenerator;
    private XYURLGenerator urlGenerator;
    private boolean renderAsPoints;
    private double ptSizePct;
    private transient ClipPath clipPath;
    private transient Paint missingPaint;
    protected static ResourceBundle localizationResources;
    public ContourPlot() {
        this ( null, null, null, null );
    }
    public ContourPlot ( final ContourDataset dataset, final ValueAxis domainAxis, final ValueAxis rangeAxis, final ColorBar colorBar ) {
        this.colorBar = null;
        this.domainCrosshairLockedOnData = true;
        this.rangeCrosshairLockedOnData = true;
        this.dataAreaRatio = 0.0;
        this.renderAsPoints = false;
        this.ptSizePct = 0.05;
        this.clipPath = null;
        this.missingPaint = null;
        this.dataset = dataset;
        if ( dataset != null ) {
            dataset.addChangeListener ( this );
        }
        if ( ( this.domainAxis = domainAxis ) != null ) {
            domainAxis.setPlot ( this );
            domainAxis.addChangeListener ( this );
        }
        if ( ( this.rangeAxis = rangeAxis ) != null ) {
            rangeAxis.setPlot ( this );
            rangeAxis.addChangeListener ( this );
        }
        if ( ( this.colorBar = colorBar ) != null ) {
            colorBar.getAxis().setPlot ( this );
            colorBar.getAxis().addChangeListener ( this );
            colorBar.configure ( this );
        }
        this.colorBarLocation = RectangleEdge.LEFT;
        this.toolTipGenerator = new StandardContourToolTipGenerator();
    }
    public RectangleEdge getColorBarLocation() {
        return this.colorBarLocation;
    }
    public void setColorBarLocation ( final RectangleEdge edge ) {
        this.colorBarLocation = edge;
        this.fireChangeEvent();
    }
    public ContourDataset getDataset() {
        return this.dataset;
    }
    public void setDataset ( final ContourDataset dataset ) {
        final ContourDataset existing = this.dataset;
        if ( existing != null ) {
            existing.removeChangeListener ( this );
        }
        if ( ( this.dataset = dataset ) != null ) {
            this.setDatasetGroup ( dataset.getGroup() );
            dataset.addChangeListener ( this );
        }
        final DatasetChangeEvent event = new DatasetChangeEvent ( this, dataset );
        this.datasetChanged ( event );
    }
    public ValueAxis getDomainAxis() {
        final ValueAxis result = this.domainAxis;
        return result;
    }
    public void setDomainAxis ( final ValueAxis axis ) {
        if ( this.isCompatibleDomainAxis ( axis ) ) {
            if ( axis != null ) {
                axis.setPlot ( this );
                axis.addChangeListener ( this );
            }
            if ( this.domainAxis != null ) {
                this.domainAxis.removeChangeListener ( this );
            }
            this.domainAxis = axis;
            this.fireChangeEvent();
        }
    }
    public ValueAxis getRangeAxis() {
        final ValueAxis result = this.rangeAxis;
        return result;
    }
    public void setRangeAxis ( final ValueAxis axis ) {
        if ( axis != null ) {
            axis.setPlot ( this );
            axis.addChangeListener ( this );
        }
        if ( this.rangeAxis != null ) {
            this.rangeAxis.removeChangeListener ( this );
        }
        this.rangeAxis = axis;
        this.fireChangeEvent();
    }
    public void setColorBarAxis ( final ColorBar axis ) {
        this.colorBar = axis;
        this.fireChangeEvent();
    }
    public double getDataAreaRatio() {
        return this.dataAreaRatio;
    }
    public void setDataAreaRatio ( final double ratio ) {
        this.dataAreaRatio = ratio;
    }
    public void addDomainMarker ( final Marker marker ) {
        if ( this.domainMarkers == null ) {
            this.domainMarkers = new ArrayList();
        }
        this.domainMarkers.add ( marker );
        this.fireChangeEvent();
    }
    public void clearDomainMarkers() {
        if ( this.domainMarkers != null ) {
            this.domainMarkers.clear();
            this.fireChangeEvent();
        }
    }
    public void addRangeMarker ( final Marker marker ) {
        if ( this.rangeMarkers == null ) {
            this.rangeMarkers = new ArrayList();
        }
        this.rangeMarkers.add ( marker );
        this.fireChangeEvent();
    }
    public void clearRangeMarkers() {
        if ( this.rangeMarkers != null ) {
            this.rangeMarkers.clear();
            this.fireChangeEvent();
        }
    }
    public void addAnnotation ( final XYAnnotation annotation ) {
        if ( this.annotations == null ) {
            this.annotations = new ArrayList();
        }
        this.annotations.add ( annotation );
        this.fireChangeEvent();
    }
    public void clearAnnotations() {
        if ( this.annotations != null ) {
            this.annotations.clear();
            this.fireChangeEvent();
        }
    }
    public boolean isCompatibleDomainAxis ( final ValueAxis axis ) {
        return true;
    }
    @Override
    public void draw ( final Graphics2D g2, final Rectangle2D area, final Point2D anchor, final PlotState parentState, final PlotRenderingInfo info ) {
        final boolean b1 = area.getWidth() <= 10.0;
        final boolean b2 = area.getHeight() <= 10.0;
        if ( b1 || b2 ) {
            return;
        }
        if ( info != null ) {
            info.setPlotArea ( area );
        }
        final RectangleInsets insets = this.getInsets();
        insets.trim ( area );
        AxisSpace space = new AxisSpace();
        space = this.domainAxis.reserveSpace ( g2, this, area, RectangleEdge.BOTTOM, space );
        space = this.rangeAxis.reserveSpace ( g2, this, area, RectangleEdge.LEFT, space );
        final Rectangle2D estimatedDataArea = space.shrink ( area, null );
        AxisSpace space2 = new AxisSpace();
        space2 = this.colorBar.reserveSpace ( g2, this, area, estimatedDataArea, this.colorBarLocation, space2 );
        final Rectangle2D adjustedPlotArea = space2.shrink ( area, null );
        final Rectangle2D dataArea = space.shrink ( adjustedPlotArea, null );
        final Rectangle2D colorBarArea = space2.reserved ( area, this.colorBarLocation );
        if ( this.getDataAreaRatio() != 0.0 ) {
            double ratio = this.getDataAreaRatio();
            final Rectangle2D tmpDataArea = ( Rectangle2D ) dataArea.clone();
            double h = tmpDataArea.getHeight();
            double w = tmpDataArea.getWidth();
            if ( ratio > 0.0 ) {
                if ( w * ratio <= h ) {
                    h = ratio * w;
                } else {
                    w = h / ratio;
                }
            } else {
                ratio *= -1.0;
                final double xLength = this.getDomainAxis().getRange().getLength();
                final double yLength = this.getRangeAxis().getRange().getLength();
                final double unitRatio = yLength / xLength;
                ratio *= unitRatio;
                if ( w * ratio <= h ) {
                    h = ratio * w;
                } else {
                    w = h / ratio;
                }
            }
            dataArea.setRect ( tmpDataArea.getX() + tmpDataArea.getWidth() / 2.0 - w / 2.0, tmpDataArea.getY(), w, h );
        }
        if ( info != null ) {
            info.setDataArea ( dataArea );
        }
        final CrosshairState crosshairState = new CrosshairState();
        crosshairState.setCrosshairDistance ( Double.POSITIVE_INFINITY );
        this.drawBackground ( g2, dataArea );
        double cursor = dataArea.getMaxY();
        if ( this.domainAxis != null ) {
            this.domainAxis.draw ( g2, cursor, adjustedPlotArea, dataArea, RectangleEdge.BOTTOM, info );
        }
        if ( this.rangeAxis != null ) {
            cursor = dataArea.getMinX();
            this.rangeAxis.draw ( g2, cursor, adjustedPlotArea, dataArea, RectangleEdge.LEFT, info );
        }
        if ( this.colorBar != null ) {
            cursor = 0.0;
            this.colorBar.draw ( g2, cursor, adjustedPlotArea, dataArea, colorBarArea, this.colorBarLocation );
        }
        final Shape originalClip = g2.getClip();
        final Composite originalComposite = g2.getComposite();
        g2.clip ( dataArea );
        g2.setComposite ( AlphaComposite.getInstance ( 3, this.getForegroundAlpha() ) );
        this.render ( g2, dataArea, info, crosshairState );
        if ( this.domainMarkers != null ) {
            for ( final Marker marker : this.domainMarkers ) {
                this.drawDomainMarker ( g2, this, this.getDomainAxis(), marker, dataArea );
            }
        }
        if ( this.rangeMarkers != null ) {
            for ( final Marker marker : this.rangeMarkers ) {
                this.drawRangeMarker ( g2, this, this.getRangeAxis(), marker, dataArea );
            }
        }
        g2.setClip ( originalClip );
        g2.setComposite ( originalComposite );
        this.drawOutline ( g2, dataArea );
    }
    public void render ( final Graphics2D g2, final Rectangle2D dataArea, final PlotRenderingInfo info, final CrosshairState crosshairState ) {
        final ContourDataset data = this.getDataset();
        if ( data != null ) {
            final ColorBar zAxis = this.getColorBar();
            if ( this.clipPath != null ) {
                final GeneralPath clipper = this.getClipPath().draw ( g2, dataArea, this.domainAxis, this.rangeAxis );
                if ( this.clipPath.isClip() ) {
                    g2.clip ( clipper );
                }
            }
            if ( this.renderAsPoints ) {
                this.pointRenderer ( g2, dataArea, info, this, this.domainAxis, this.rangeAxis, zAxis, data, crosshairState );
            } else {
                this.contourRenderer ( g2, dataArea, info, this, this.domainAxis, this.rangeAxis, zAxis, data, crosshairState );
            }
            this.setDomainCrosshairValue ( crosshairState.getCrosshairX(), false );
            if ( this.isDomainCrosshairVisible() ) {
                this.drawVerticalLine ( g2, dataArea, this.getDomainCrosshairValue(), this.getDomainCrosshairStroke(), this.getDomainCrosshairPaint() );
            }
            this.setRangeCrosshairValue ( crosshairState.getCrosshairY(), false );
            if ( this.isRangeCrosshairVisible() ) {
                this.drawHorizontalLine ( g2, dataArea, this.getRangeCrosshairValue(), this.getRangeCrosshairStroke(), this.getRangeCrosshairPaint() );
            }
        } else if ( this.clipPath != null ) {
            this.getClipPath().draw ( g2, dataArea, this.domainAxis, this.rangeAxis );
        }
    }
    public void contourRenderer ( final Graphics2D g2, final Rectangle2D dataArea, final PlotRenderingInfo info, final ContourPlot plot, final ValueAxis horizontalAxis, final ValueAxis verticalAxis, final ColorBar colorBar, final ContourDataset data, final CrosshairState crosshairState ) {
        EntityCollection entities = null;
        if ( info != null ) {
            entities = info.getOwner().getEntityCollection();
        }
        final Rectangle2D.Double rect = new Rectangle2D.Double();
        final Object antiAlias = g2.getRenderingHint ( RenderingHints.KEY_ANTIALIASING );
        g2.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
        final Number[] xNumber = data.getXValues();
        final Number[] yNumber = data.getYValues();
        final Number[] zNumber = data.getZValues();
        final double[] x = new double[xNumber.length];
        final double[] y = new double[yNumber.length];
        for ( int i = 0; i < x.length; ++i ) {
            x[i] = xNumber[i].doubleValue();
            y[i] = yNumber[i].doubleValue();
        }
        final int[] xIndex = data.indexX();
        final int[] indexX = data.getXIndices();
        final boolean vertInverted = ( ( NumberAxis ) verticalAxis ).isInverted();
        boolean horizInverted = false;
        if ( horizontalAxis instanceof NumberAxis ) {
            horizInverted = ( ( NumberAxis ) horizontalAxis ).isInverted();
        }
        double transX = 0.0;
        double transDXp1 = 0.0;
        double transDX = 0.0;
        double transDYp1 = 0.0;
        final int iMax = xIndex[xIndex.length - 1];
        for ( int k = 0; k < x.length; ++k ) {
            final int j = xIndex[k];
            double transY;
            double transDYm1;
            if ( indexX[j] == k ) {
                double transDXm1;
                if ( j == 0 ) {
                    final double transXm1;
                    transX = ( transXm1 = horizontalAxis.valueToJava2D ( x[k], dataArea, RectangleEdge.BOTTOM ) );
                    final double transXp1 = horizontalAxis.valueToJava2D ( x[indexX[j + 1]], dataArea, RectangleEdge.BOTTOM );
                    transDXm1 = Math.abs ( 0.5 * ( transX - transXm1 ) );
                    transDXp1 = Math.abs ( 0.5 * ( transX - transXp1 ) );
                } else if ( j == iMax ) {
                    transX = horizontalAxis.valueToJava2D ( x[k], dataArea, RectangleEdge.BOTTOM );
                    final double transXm1 = horizontalAxis.valueToJava2D ( x[indexX[j - 1]], dataArea, RectangleEdge.BOTTOM );
                    final double transXp1 = transX;
                    transDXm1 = Math.abs ( 0.5 * ( transX - transXm1 ) );
                    transDXp1 = Math.abs ( 0.5 * ( transX - transXp1 ) );
                } else {
                    transX = horizontalAxis.valueToJava2D ( x[k], dataArea, RectangleEdge.BOTTOM );
                    final double transXp1 = horizontalAxis.valueToJava2D ( x[indexX[j + 1]], dataArea, RectangleEdge.BOTTOM );
                    transDXm1 = transDXp1;
                    transDXp1 = Math.abs ( 0.5 * ( transX - transXp1 ) );
                }
                if ( horizInverted ) {
                    transX -= transDXp1;
                } else {
                    transX -= transDXm1;
                }
                transDX = transDXm1 + transDXp1;
                final double transYm1;
                transY = ( transYm1 = verticalAxis.valueToJava2D ( y[k], dataArea, RectangleEdge.LEFT ) );
                if ( k + 1 == y.length ) {
                    continue;
                }
                final double transYp1 = verticalAxis.valueToJava2D ( y[k + 1], dataArea, RectangleEdge.LEFT );
                transDYm1 = Math.abs ( 0.5 * ( transY - transYm1 ) );
                transDYp1 = Math.abs ( 0.5 * ( transY - transYp1 ) );
            } else if ( ( j < indexX.length - 1 && indexX[j + 1] - 1 == k ) || k == x.length - 1 ) {
                transY = verticalAxis.valueToJava2D ( y[k], dataArea, RectangleEdge.LEFT );
                final double transYm1 = verticalAxis.valueToJava2D ( y[k - 1], dataArea, RectangleEdge.LEFT );
                final double transYp1 = transY;
                transDYm1 = Math.abs ( 0.5 * ( transY - transYm1 ) );
                transDYp1 = Math.abs ( 0.5 * ( transY - transYp1 ) );
            } else {
                transY = verticalAxis.valueToJava2D ( y[k], dataArea, RectangleEdge.LEFT );
                final double transYp1 = verticalAxis.valueToJava2D ( y[k + 1], dataArea, RectangleEdge.LEFT );
                transDYm1 = transDYp1;
                transDYp1 = Math.abs ( 0.5 * ( transY - transYp1 ) );
            }
            if ( vertInverted ) {
                transY -= transDYm1;
            } else {
                transY -= transDYp1;
            }
            final double transDY = transDYm1 + transDYp1;
            rect.setRect ( transX, transY, transDX, transDY );
            if ( zNumber[k] != null ) {
                g2.setPaint ( colorBar.getPaint ( zNumber[k].doubleValue() ) );
                g2.fill ( rect );
            } else if ( this.missingPaint != null ) {
                g2.setPaint ( this.missingPaint );
                g2.fill ( rect );
            }
            final Rectangle2D.Double entityArea = rect;
            if ( entities != null ) {
                String tip = "";
                if ( this.getToolTipGenerator() != null ) {
                    tip = this.toolTipGenerator.generateToolTip ( data, k );
                }
                final String url = null;
                final ContourEntity entity = new ContourEntity ( ( Shape ) entityArea.clone(), tip, url );
                entity.setIndex ( k );
                entities.add ( entity );
            }
            if ( plot.isDomainCrosshairLockedOnData() ) {
                if ( plot.isRangeCrosshairLockedOnData() ) {
                    crosshairState.updateCrosshairPoint ( x[k], y[k], transX, transY, PlotOrientation.VERTICAL );
                } else {
                    crosshairState.updateCrosshairX ( transX );
                }
            } else if ( plot.isRangeCrosshairLockedOnData() ) {
                crosshairState.updateCrosshairY ( transY );
            }
        }
        g2.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, antiAlias );
    }
    public void pointRenderer ( final Graphics2D g2, final Rectangle2D dataArea, final PlotRenderingInfo info, final ContourPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final ColorBar colorBar, final ContourDataset data, final CrosshairState crosshairState ) {
        EntityCollection entities = null;
        if ( info != null ) {
            entities = info.getOwner().getEntityCollection();
        }
        final RectangularShape rect = new Ellipse2D.Double();
        final Object antiAlias = g2.getRenderingHint ( RenderingHints.KEY_ANTIALIASING );
        g2.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
        final Number[] xNumber = data.getXValues();
        final Number[] yNumber = data.getYValues();
        final Number[] zNumber = data.getZValues();
        final double[] x = new double[xNumber.length];
        final double[] y = new double[yNumber.length];
        for ( int i = 0; i < x.length; ++i ) {
            x[i] = xNumber[i].doubleValue();
            y[i] = yNumber[i].doubleValue();
        }
        final double size = dataArea.getWidth() * this.ptSizePct;
        for ( int k = 0; k < x.length; ++k ) {
            final double transX = domainAxis.valueToJava2D ( x[k], dataArea, RectangleEdge.BOTTOM ) - 0.5 * size;
            final double transY = rangeAxis.valueToJava2D ( y[k], dataArea, RectangleEdge.LEFT ) - 0.5 * size;
            final double transDX = size;
            final double transDY = size;
            rect.setFrame ( transX, transY, transDX, transDY );
            if ( zNumber[k] != null ) {
                g2.setPaint ( colorBar.getPaint ( zNumber[k].doubleValue() ) );
                g2.fill ( rect );
            } else if ( this.missingPaint != null ) {
                g2.setPaint ( this.missingPaint );
                g2.fill ( rect );
            }
            final RectangularShape entityArea = rect;
            if ( entities != null ) {
                String tip = null;
                if ( this.getToolTipGenerator() != null ) {
                    tip = this.toolTipGenerator.generateToolTip ( data, k );
                }
                final String url = null;
                final ContourEntity entity = new ContourEntity ( ( Shape ) entityArea.clone(), tip, url );
                entity.setIndex ( k );
                entities.add ( entity );
            }
            if ( plot.isDomainCrosshairLockedOnData() ) {
                if ( plot.isRangeCrosshairLockedOnData() ) {
                    crosshairState.updateCrosshairPoint ( x[k], y[k], transX, transY, PlotOrientation.VERTICAL );
                } else {
                    crosshairState.updateCrosshairX ( transX );
                }
            } else if ( plot.isRangeCrosshairLockedOnData() ) {
                crosshairState.updateCrosshairY ( transY );
            }
        }
        g2.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, antiAlias );
    }
    protected void drawVerticalLine ( final Graphics2D g2, final Rectangle2D dataArea, final double value, final Stroke stroke, final Paint paint ) {
        final double xx = this.getDomainAxis().valueToJava2D ( value, dataArea, RectangleEdge.BOTTOM );
        final Line2D line = new Line2D.Double ( xx, dataArea.getMinY(), xx, dataArea.getMaxY() );
        g2.setStroke ( stroke );
        g2.setPaint ( paint );
        g2.draw ( line );
    }
    protected void drawHorizontalLine ( final Graphics2D g2, final Rectangle2D dataArea, final double value, final Stroke stroke, final Paint paint ) {
        final double yy = this.getRangeAxis().valueToJava2D ( value, dataArea, RectangleEdge.LEFT );
        final Line2D line = new Line2D.Double ( dataArea.getMinX(), yy, dataArea.getMaxX(), yy );
        g2.setStroke ( stroke );
        g2.setPaint ( paint );
        g2.draw ( line );
    }
    @Override
    public void handleClick ( final int x, final int y, final PlotRenderingInfo info ) {
    }
    @Override
    public void zoom ( final double percent ) {
        if ( percent <= 0.0 ) {
            this.getRangeAxis().setAutoRange ( true );
            this.getDomainAxis().setAutoRange ( true );
        }
    }
    @Override
    public String getPlotType() {
        return ContourPlot.localizationResources.getString ( "Contour_Plot" );
    }
    @Override
    public Range getDataRange ( final ValueAxis axis ) {
        if ( this.dataset == null ) {
            return null;
        }
        Range result = null;
        if ( axis == this.getDomainAxis() ) {
            result = DatasetUtilities.findDomainBounds ( this.dataset );
        } else if ( axis == this.getRangeAxis() ) {
            result = DatasetUtilities.findRangeBounds ( this.dataset );
        }
        return result;
    }
    @Override
    public Range getContourDataRange() {
        Range result = null;
        final ContourDataset data = this.getDataset();
        if ( data != null ) {
            final Range h = this.getDomainAxis().getRange();
            final Range v = this.getRangeAxis().getRange();
            result = this.visibleRange ( data, h, v );
        }
        return result;
    }
    @Override
    public void propertyChange ( final PropertyChangeEvent event ) {
        this.fireChangeEvent();
    }
    @Override
    public void datasetChanged ( final DatasetChangeEvent event ) {
        if ( this.domainAxis != null ) {
            this.domainAxis.configure();
        }
        if ( this.rangeAxis != null ) {
            this.rangeAxis.configure();
        }
        if ( this.colorBar != null ) {
            this.colorBar.configure ( this );
        }
        super.datasetChanged ( event );
    }
    public ColorBar getColorBar() {
        return this.colorBar;
    }
    public boolean isDomainCrosshairVisible() {
        return this.domainCrosshairVisible;
    }
    public void setDomainCrosshairVisible ( final boolean flag ) {
        if ( this.domainCrosshairVisible != flag ) {
            this.domainCrosshairVisible = flag;
            this.fireChangeEvent();
        }
    }
    public boolean isDomainCrosshairLockedOnData() {
        return this.domainCrosshairLockedOnData;
    }
    public void setDomainCrosshairLockedOnData ( final boolean flag ) {
        if ( this.domainCrosshairLockedOnData != flag ) {
            this.domainCrosshairLockedOnData = flag;
            this.fireChangeEvent();
        }
    }
    public double getDomainCrosshairValue() {
        return this.domainCrosshairValue;
    }
    public void setDomainCrosshairValue ( final double value ) {
        this.setDomainCrosshairValue ( value, true );
    }
    public void setDomainCrosshairValue ( final double value, final boolean notify ) {
        this.domainCrosshairValue = value;
        if ( this.isDomainCrosshairVisible() && notify ) {
            this.fireChangeEvent();
        }
    }
    public Stroke getDomainCrosshairStroke() {
        return this.domainCrosshairStroke;
    }
    public void setDomainCrosshairStroke ( final Stroke stroke ) {
        this.domainCrosshairStroke = stroke;
        this.fireChangeEvent();
    }
    public Paint getDomainCrosshairPaint() {
        return this.domainCrosshairPaint;
    }
    public void setDomainCrosshairPaint ( final Paint paint ) {
        this.domainCrosshairPaint = paint;
        this.fireChangeEvent();
    }
    public boolean isRangeCrosshairVisible() {
        return this.rangeCrosshairVisible;
    }
    public void setRangeCrosshairVisible ( final boolean flag ) {
        if ( this.rangeCrosshairVisible != flag ) {
            this.rangeCrosshairVisible = flag;
            this.fireChangeEvent();
        }
    }
    public boolean isRangeCrosshairLockedOnData() {
        return this.rangeCrosshairLockedOnData;
    }
    public void setRangeCrosshairLockedOnData ( final boolean flag ) {
        if ( this.rangeCrosshairLockedOnData != flag ) {
            this.rangeCrosshairLockedOnData = flag;
            this.fireChangeEvent();
        }
    }
    public double getRangeCrosshairValue() {
        return this.rangeCrosshairValue;
    }
    public void setRangeCrosshairValue ( final double value ) {
        this.setRangeCrosshairValue ( value, true );
    }
    public void setRangeCrosshairValue ( final double value, final boolean notify ) {
        this.rangeCrosshairValue = value;
        if ( this.isRangeCrosshairVisible() && notify ) {
            this.fireChangeEvent();
        }
    }
    public Stroke getRangeCrosshairStroke() {
        return this.rangeCrosshairStroke;
    }
    public void setRangeCrosshairStroke ( final Stroke stroke ) {
        this.rangeCrosshairStroke = stroke;
        this.fireChangeEvent();
    }
    public Paint getRangeCrosshairPaint() {
        return this.rangeCrosshairPaint;
    }
    public void setRangeCrosshairPaint ( final Paint paint ) {
        this.rangeCrosshairPaint = paint;
        this.fireChangeEvent();
    }
    public ContourToolTipGenerator getToolTipGenerator() {
        return this.toolTipGenerator;
    }
    public void setToolTipGenerator ( final ContourToolTipGenerator generator ) {
        this.toolTipGenerator = generator;
    }
    public XYURLGenerator getURLGenerator() {
        return this.urlGenerator;
    }
    public void setURLGenerator ( final XYURLGenerator urlGenerator ) {
        this.urlGenerator = urlGenerator;
    }
    public void drawDomainMarker ( final Graphics2D g2, final ContourPlot plot, final ValueAxis domainAxis, final Marker marker, final Rectangle2D dataArea ) {
        if ( marker instanceof ValueMarker ) {
            final ValueMarker vm = ( ValueMarker ) marker;
            final double value = vm.getValue();
            final Range range = domainAxis.getRange();
            if ( !range.contains ( value ) ) {
                return;
            }
            final double x = domainAxis.valueToJava2D ( value, dataArea, RectangleEdge.BOTTOM );
            final Line2D line = new Line2D.Double ( x, dataArea.getMinY(), x, dataArea.getMaxY() );
            final Paint paint = marker.getOutlinePaint();
            final Stroke stroke = marker.getOutlineStroke();
            g2.setPaint ( ( paint != null ) ? paint : Plot.DEFAULT_OUTLINE_PAINT );
            g2.setStroke ( ( stroke != null ) ? stroke : Plot.DEFAULT_OUTLINE_STROKE );
            g2.draw ( line );
        }
    }
    public void drawRangeMarker ( final Graphics2D g2, final ContourPlot plot, final ValueAxis rangeAxis, final Marker marker, final Rectangle2D dataArea ) {
        if ( marker instanceof ValueMarker ) {
            final ValueMarker vm = ( ValueMarker ) marker;
            final double value = vm.getValue();
            final Range range = rangeAxis.getRange();
            if ( !range.contains ( value ) ) {
                return;
            }
            final double y = rangeAxis.valueToJava2D ( value, dataArea, RectangleEdge.LEFT );
            final Line2D line = new Line2D.Double ( dataArea.getMinX(), y, dataArea.getMaxX(), y );
            final Paint paint = marker.getOutlinePaint();
            final Stroke stroke = marker.getOutlineStroke();
            g2.setPaint ( ( paint != null ) ? paint : Plot.DEFAULT_OUTLINE_PAINT );
            g2.setStroke ( ( stroke != null ) ? stroke : Plot.DEFAULT_OUTLINE_STROKE );
            g2.draw ( line );
        }
    }
    public ClipPath getClipPath() {
        return this.clipPath;
    }
    public void setClipPath ( final ClipPath clipPath ) {
        this.clipPath = clipPath;
    }
    public double getPtSizePct() {
        return this.ptSizePct;
    }
    public boolean isRenderAsPoints() {
        return this.renderAsPoints;
    }
    public void setPtSizePct ( final double ptSizePct ) {
        this.ptSizePct = ptSizePct;
    }
    public void setRenderAsPoints ( final boolean renderAsPoints ) {
        this.renderAsPoints = renderAsPoints;
    }
    @Override
    public void axisChanged ( final AxisChangeEvent event ) {
        final Object source = event.getSource();
        if ( source.equals ( this.rangeAxis ) || source.equals ( this.domainAxis ) ) {
            final ColorBar cba = this.colorBar;
            if ( this.colorBar.getAxis().isAutoRange() ) {
                cba.getAxis().configure();
            }
        }
        super.axisChanged ( event );
    }
    public Range visibleRange ( final ContourDataset data, final Range x, final Range y ) {
        final Range range = data.getZValueRange ( x, y );
        return range;
    }
    public Paint getMissingPaint() {
        return this.missingPaint;
    }
    public void setMissingPaint ( final Paint paint ) {
        this.missingPaint = paint;
    }
    public void zoomDomainAxes ( final double x, final double y, final double factor ) {
    }
    public void zoomDomainAxes ( final double x, final double y, final double lowerPercent, final double upperPercent ) {
    }
    public void zoomRangeAxes ( final double x, final double y, final double factor ) {
    }
    public void zoomRangeAxes ( final double x, final double y, final double lowerPercent, final double upperPercent ) {
    }
    public boolean isDomainZoomable() {
        return false;
    }
    public boolean isRangeZoomable() {
        return false;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final ContourPlot clone = ( ContourPlot ) super.clone();
        if ( this.domainAxis != null ) {
            ( clone.domainAxis = ( ValueAxis ) this.domainAxis.clone() ).setPlot ( clone );
            clone.domainAxis.addChangeListener ( clone );
        }
        if ( this.rangeAxis != null ) {
            ( clone.rangeAxis = ( ValueAxis ) this.rangeAxis.clone() ).setPlot ( clone );
            clone.rangeAxis.addChangeListener ( clone );
        }
        if ( clone.dataset != null ) {
            clone.dataset.addChangeListener ( clone );
        }
        if ( this.colorBar != null ) {
            clone.colorBar = ( ColorBar ) this.colorBar.clone();
        }
        clone.domainMarkers = ( List ) ObjectUtilities.deepClone ( ( Collection ) this.domainMarkers );
        clone.rangeMarkers = ( List ) ObjectUtilities.deepClone ( ( Collection ) this.rangeMarkers );
        clone.annotations = ( List ) ObjectUtilities.deepClone ( ( Collection ) this.annotations );
        if ( this.clipPath != null ) {
            clone.clipPath = ( ClipPath ) this.clipPath.clone();
        }
        return clone;
    }
    static {
        DEFAULT_INSETS = new RectangleInsets ( 2.0, 2.0, 100.0, 10.0 );
        ContourPlot.localizationResources = ResourceBundleWrapper.getBundle ( "org.jfree.chart.plot.LocalizationBundle" );
    }
}
