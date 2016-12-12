package org.jfree.chart.plot;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.util.ParamChecks;
import org.jfree.chart.util.ShadowGenerator;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.ObjectUtilities;
public class CombinedDomainXYPlot extends XYPlot
    implements PlotChangeListener {
    private static final long serialVersionUID = -7765545541261907383L;
    private List<XYPlot> subplots;
    private double gap = 5.0;
    private transient Rectangle2D[] subplotAreas;
    public CombinedDomainXYPlot() {
        this ( new NumberAxis() );
    }
    public CombinedDomainXYPlot ( ValueAxis domainAxis ) {
        super ( null,
                domainAxis,
                null,
                null );
        this.subplots = new java.util.ArrayList<XYPlot>();
    }
    @Override
    public String getPlotType() {
        return "Combined_Domain_XYPlot";
    }
    public double getGap() {
        return this.gap;
    }
    public void setGap ( double gap ) {
        this.gap = gap;
        fireChangeEvent();
    }
    @Override
    public boolean isRangePannable() {
        for ( XYPlot subplot : this.subplots ) {
            if ( subplot.isRangePannable() ) {
                return true;
            }
        }
        return false;
    }
    @Override
    public void setRangePannable ( boolean pannable ) {
        for ( XYPlot subplot : this.subplots ) {
            subplot.setRangePannable ( pannable );
        }
    }
    @Override
    public void setOrientation ( PlotOrientation orientation ) {
        super.setOrientation ( orientation );
        for ( XYPlot p : this.subplots ) {
            p.setOrientation ( orientation );
        }
    }
    @Override
    public void setShadowGenerator ( ShadowGenerator generator ) {
        setNotify ( false );
        super.setShadowGenerator ( generator );
        for ( XYPlot p : this.subplots ) {
            p.setShadowGenerator ( generator );
        }
        setNotify ( true );
    }
    @Override
    public Range getDataRange ( ValueAxis axis ) {
        if ( this.subplots == null ) {
            return null;
        }
        Range result = null;
        for ( XYPlot p : this.subplots ) {
            result = Range.combine ( result, p.getDataRange ( axis ) );
        }
        return result;
    }
    public void add ( XYPlot subplot ) {
        add ( subplot, 1 );
    }
    public void add ( XYPlot subplot, int weight ) {
        ParamChecks.nullNotPermitted ( subplot, "subplot" );
        if ( weight <= 0 ) {
            throw new IllegalArgumentException ( "Require weight >= 1." );
        }
        subplot.setParent ( this );
        subplot.setWeight ( weight );
        subplot.setInsets ( RectangleInsets.ZERO_INSETS, false );
        subplot.setDomainAxis ( null );
        subplot.addChangeListener ( this );
        this.subplots.add ( subplot );
        ValueAxis axis = getDomainAxis();
        if ( axis != null ) {
            axis.configure();
        }
        fireChangeEvent();
    }
    public void remove ( XYPlot subplot ) {
        ParamChecks.nullNotPermitted ( subplot, "subplot" );
        int position = -1;
        int size = this.subplots.size();
        int i = 0;
        while ( position == -1 && i < size ) {
            if ( this.subplots.get ( i ) == subplot ) {
                position = i;
            }
            i++;
        }
        if ( position != -1 ) {
            this.subplots.remove ( position );
            subplot.setParent ( null );
            subplot.removeChangeListener ( this );
            ValueAxis domain = getDomainAxis();
            if ( domain != null ) {
                domain.configure();
            }
            fireChangeEvent();
        }
    }
    public List getSubplots() {
        return Collections.unmodifiableList ( this.subplots );
    }
    @Override
    protected AxisSpace calculateAxisSpace ( Graphics2D g2,
            Rectangle2D plotArea ) {
        AxisSpace space = new AxisSpace();
        PlotOrientation orientation = getOrientation();
        AxisSpace fixed = getFixedDomainAxisSpace();
        if ( fixed != null ) {
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                space.setLeft ( fixed.getLeft() );
                space.setRight ( fixed.getRight() );
            } else if ( orientation == PlotOrientation.VERTICAL ) {
                space.setTop ( fixed.getTop() );
                space.setBottom ( fixed.getBottom() );
            }
        } else {
            ValueAxis xAxis = getDomainAxis();
            RectangleEdge xEdge = Plot.resolveDomainAxisLocation (
                                      getDomainAxisLocation(), orientation );
            if ( xAxis != null ) {
                space = xAxis.reserveSpace ( g2, this, plotArea, xEdge, space );
            }
        }
        Rectangle2D adjustedPlotArea = space.shrink ( plotArea, null );
        int n = this.subplots.size();
        int totalWeight = 0;
        for ( int i = 0; i < n; i++ ) {
            XYPlot sub = ( XYPlot ) this.subplots.get ( i );
            totalWeight += sub.getWeight();
        }
        this.subplotAreas = new Rectangle2D[n];
        double x = adjustedPlotArea.getX();
        double y = adjustedPlotArea.getY();
        double usableSize = 0.0;
        if ( orientation == PlotOrientation.HORIZONTAL ) {
            usableSize = adjustedPlotArea.getWidth() - this.gap * ( n - 1 );
        } else if ( orientation == PlotOrientation.VERTICAL ) {
            usableSize = adjustedPlotArea.getHeight() - this.gap * ( n - 1 );
        }
        for ( int i = 0; i < n; i++ ) {
            XYPlot plot = ( XYPlot ) this.subplots.get ( i );
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                double w = usableSize * plot.getWeight() / totalWeight;
                this.subplotAreas[i] = new Rectangle2D.Double ( x, y, w,
                        adjustedPlotArea.getHeight() );
                x = x + w + this.gap;
            } else if ( orientation == PlotOrientation.VERTICAL ) {
                double h = usableSize * plot.getWeight() / totalWeight;
                this.subplotAreas[i] = new Rectangle2D.Double ( x, y,
                        adjustedPlotArea.getWidth(), h );
                y = y + h + this.gap;
            }
            AxisSpace subSpace = plot.calculateRangeAxisSpace ( g2,
                                 this.subplotAreas[i], null );
            space.ensureAtLeast ( subSpace );
        }
        return space;
    }
    @Override
    public void draw ( Graphics2D g2, Rectangle2D area, Point2D anchor,
                       PlotState parentState, PlotRenderingInfo info ) {
        if ( info != null ) {
            info.setPlotArea ( area );
        }
        RectangleInsets insets = getInsets();
        insets.trim ( area );
        setFixedRangeAxisSpaceForSubplots ( null );
        AxisSpace space = calculateAxisSpace ( g2, area );
        Rectangle2D dataArea = space.shrink ( area, null );
        setFixedRangeAxisSpaceForSubplots ( space );
        ValueAxis axis = getDomainAxis();
        RectangleEdge edge = getDomainAxisEdge();
        double cursor = RectangleEdge.coordinate ( dataArea, edge );
        AxisState axisState = axis.draw ( g2, cursor, area, dataArea, edge, info );
        if ( parentState == null ) {
            parentState = new PlotState();
        }
        parentState.getSharedAxisStates().put ( axis, axisState );
        for ( int i = 0; i < this.subplots.size(); i++ ) {
            XYPlot plot = ( XYPlot ) this.subplots.get ( i );
            PlotRenderingInfo subplotInfo = null;
            if ( info != null ) {
                subplotInfo = new PlotRenderingInfo ( info.getOwner() );
                info.addSubplotInfo ( subplotInfo );
            }
            plot.draw ( g2, this.subplotAreas[i], anchor, parentState,
                        subplotInfo );
        }
        if ( info != null ) {
            info.setDataArea ( dataArea );
        }
    }
    @Override
    public LegendItemCollection getLegendItems() {
        LegendItemCollection result = getFixedLegendItems();
        if ( result == null ) {
            result = new LegendItemCollection();
            if ( this.subplots != null ) {
                Iterator iterator = this.subplots.iterator();
                while ( iterator.hasNext() ) {
                    XYPlot plot = ( XYPlot ) iterator.next();
                    LegendItemCollection more = plot.getLegendItems();
                    result.addAll ( more );
                }
            }
        }
        return result;
    }
    @Override
    public void zoomRangeAxes ( double factor, PlotRenderingInfo info,
                                Point2D source ) {
        zoomRangeAxes ( factor, info, source, false );
    }
    @Override
    public void zoomRangeAxes ( double factor, PlotRenderingInfo state,
                                Point2D source, boolean useAnchor ) {
        XYPlot subplot = findSubplot ( state, source );
        if ( subplot != null ) {
            subplot.zoomRangeAxes ( factor, state, source, useAnchor );
        } else {
            for ( XYPlot p : this.subplots ) {
                p.zoomRangeAxes ( factor, state, source, useAnchor );
            }
        }
    }
    @Override
    public void zoomRangeAxes ( double lowerPercent, double upperPercent,
                                PlotRenderingInfo info, Point2D source ) {
        XYPlot subplot = findSubplot ( info, source );
        if ( subplot != null ) {
            subplot.zoomRangeAxes ( lowerPercent, upperPercent, info, source );
        } else {
            for ( XYPlot p : this.subplots ) {
                p.zoomRangeAxes ( lowerPercent, upperPercent, info, source );
            }
        }
    }
    @Override
    public void panRangeAxes ( double panRange, PlotRenderingInfo info,
                               Point2D source ) {
        XYPlot subplot = findSubplot ( info, source );
        if ( subplot == null ) {
            return;
        }
        if ( !subplot.isRangePannable() ) {
            return;
        }
        PlotRenderingInfo subplotInfo = info.getSubplotInfo (
                                            info.getSubplotIndex ( source ) );
        if ( subplotInfo == null ) {
            return;
        }
        for ( int i = 0; i < subplot.getRangeAxisCount(); i++ ) {
            ValueAxis rangeAxis = subplot.getRangeAxis ( i );
            if ( rangeAxis != null ) {
                rangeAxis.pan ( panRange );
            }
        }
    }
    public XYPlot findSubplot ( PlotRenderingInfo info, Point2D source ) {
        ParamChecks.nullNotPermitted ( info, "info" );
        ParamChecks.nullNotPermitted ( source, "source" );
        XYPlot result = null;
        int subplotIndex = info.getSubplotIndex ( source );
        if ( subplotIndex >= 0 ) {
            result = ( XYPlot ) this.subplots.get ( subplotIndex );
        }
        return result;
    }
    @Override
    public void setRenderer ( XYItemRenderer renderer ) {
        super.setRenderer ( renderer );
        for ( XYPlot p : this.subplots ) {
            p.setRenderer ( renderer );
        }
    }
    @Override
    public void setFixedRangeAxisSpace ( AxisSpace space ) {
        super.setFixedRangeAxisSpace ( space );
        setFixedRangeAxisSpaceForSubplots ( space );
        fireChangeEvent();
    }
    protected void setFixedRangeAxisSpaceForSubplots ( AxisSpace space ) {
        for ( XYPlot p : this.subplots ) {
            p.setFixedRangeAxisSpace ( space, false );
        }
    }
    @Override
    public void handleClick ( int x, int y, PlotRenderingInfo info ) {
        Rectangle2D dataArea = info.getDataArea();
        if ( dataArea.contains ( x, y ) ) {
            for ( int i = 0; i < this.subplots.size(); i++ ) {
                XYPlot subplot = ( XYPlot ) this.subplots.get ( i );
                PlotRenderingInfo subplotInfo = info.getSubplotInfo ( i );
                subplot.handleClick ( x, y, subplotInfo );
            }
        }
    }
    @Override
    public void datasetChanged ( DatasetChangeEvent event ) {
        super.datasetChanged ( event );
        if ( this.subplots == null ) {
            return;
        }
        XYDataset dataset = null;
        if ( event.getDataset() instanceof XYDataset ) {
            dataset = ( XYDataset ) event.getDataset();
        }
        for ( XYPlot subplot : this.subplots ) {
            if ( subplot.indexOf ( dataset ) >= 0 ) {
                subplot.configureRangeAxes();
            }
        }
    }
    @Override
    public void plotChanged ( PlotChangeEvent event ) {
        notifyListeners ( event );
    }
    @Override
    public boolean equals ( Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof CombinedDomainXYPlot ) ) {
            return false;
        }
        CombinedDomainXYPlot that = ( CombinedDomainXYPlot ) obj;
        if ( this.gap != that.gap ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.subplots, that.subplots ) ) {
            return false;
        }
        return super.equals ( obj );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        CombinedDomainXYPlot result = ( CombinedDomainXYPlot ) super.clone();
        result.subplots = ( List ) ObjectUtilities.deepClone ( this.subplots );
        for ( Iterator it = result.subplots.iterator(); it.hasNext(); ) {
            Plot child = ( Plot ) it.next();
            child.setParent ( result );
        }
        ValueAxis domainAxis = result.getDomainAxis();
        if ( domainAxis != null ) {
            domainAxis.configure();
        }
        return result;
    }
}
