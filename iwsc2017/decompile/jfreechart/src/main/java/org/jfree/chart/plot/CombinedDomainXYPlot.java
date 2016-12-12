package org.jfree.chart.plot;
import java.util.Collection;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.AxisState;
import java.awt.geom.Point2D;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.axis.AxisSpace;
import java.awt.Graphics2D;
import java.util.Collections;
import org.jfree.ui.RectangleInsets;
import org.jfree.chart.util.ParamChecks;
import org.jfree.data.Range;
import org.jfree.chart.util.ShadowGenerator;
import java.util.Iterator;
import java.util.ArrayList;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.NumberAxis;
import java.awt.geom.Rectangle2D;
import java.util.List;
import org.jfree.chart.event.PlotChangeListener;
public class CombinedDomainXYPlot extends XYPlot implements PlotChangeListener {
    private static final long serialVersionUID = -7765545541261907383L;
    private List<XYPlot> subplots;
    private double gap;
    private transient Rectangle2D[] subplotAreas;
    public CombinedDomainXYPlot() {
        this ( new NumberAxis() );
    }
    public CombinedDomainXYPlot ( final ValueAxis domainAxis ) {
        super ( null, domainAxis, null, null );
        this.gap = 5.0;
        this.subplots = new ArrayList<XYPlot>();
    }
    @Override
    public String getPlotType() {
        return "Combined_Domain_XYPlot";
    }
    public double getGap() {
        return this.gap;
    }
    public void setGap ( final double gap ) {
        this.gap = gap;
        this.fireChangeEvent();
    }
    @Override
    public boolean isRangePannable() {
        for ( final XYPlot subplot : this.subplots ) {
            if ( subplot.isRangePannable() ) {
                return true;
            }
        }
        return false;
    }
    @Override
    public void setRangePannable ( final boolean pannable ) {
        for ( final XYPlot subplot : this.subplots ) {
            subplot.setRangePannable ( pannable );
        }
    }
    @Override
    public void setOrientation ( final PlotOrientation orientation ) {
        super.setOrientation ( orientation );
        for ( final XYPlot p : this.subplots ) {
            p.setOrientation ( orientation );
        }
    }
    @Override
    public void setShadowGenerator ( final ShadowGenerator generator ) {
        this.setNotify ( false );
        super.setShadowGenerator ( generator );
        for ( final XYPlot p : this.subplots ) {
            p.setShadowGenerator ( generator );
        }
        this.setNotify ( true );
    }
    @Override
    public Range getDataRange ( final ValueAxis axis ) {
        if ( this.subplots == null ) {
            return null;
        }
        Range result = null;
        for ( final XYPlot p : this.subplots ) {
            result = Range.combine ( result, p.getDataRange ( axis ) );
        }
        return result;
    }
    public void add ( final XYPlot subplot ) {
        this.add ( subplot, 1 );
    }
    public void add ( final XYPlot subplot, final int weight ) {
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
        final ValueAxis axis = this.getDomainAxis();
        if ( axis != null ) {
            axis.configure();
        }
        this.fireChangeEvent();
    }
    public void remove ( final XYPlot subplot ) {
        ParamChecks.nullNotPermitted ( subplot, "subplot" );
        int position = -1;
        for ( int size = this.subplots.size(), i = 0; position == -1 && i < size; ++i ) {
            if ( this.subplots.get ( i ) == subplot ) {
                position = i;
            }
        }
        if ( position != -1 ) {
            this.subplots.remove ( position );
            subplot.setParent ( null );
            subplot.removeChangeListener ( this );
            final ValueAxis domain = this.getDomainAxis();
            if ( domain != null ) {
                domain.configure();
            }
            this.fireChangeEvent();
        }
    }
    public List getSubplots() {
        return Collections.unmodifiableList ( ( List<?> ) this.subplots );
    }
    @Override
    protected AxisSpace calculateAxisSpace ( final Graphics2D g2, final Rectangle2D plotArea ) {
        AxisSpace space = new AxisSpace();
        final PlotOrientation orientation = this.getOrientation();
        final AxisSpace fixed = this.getFixedDomainAxisSpace();
        if ( fixed != null ) {
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                space.setLeft ( fixed.getLeft() );
                space.setRight ( fixed.getRight() );
            } else if ( orientation == PlotOrientation.VERTICAL ) {
                space.setTop ( fixed.getTop() );
                space.setBottom ( fixed.getBottom() );
            }
        } else {
            final ValueAxis xAxis = this.getDomainAxis();
            final RectangleEdge xEdge = Plot.resolveDomainAxisLocation ( this.getDomainAxisLocation(), orientation );
            if ( xAxis != null ) {
                space = xAxis.reserveSpace ( g2, this, plotArea, xEdge, space );
            }
        }
        final Rectangle2D adjustedPlotArea = space.shrink ( plotArea, null );
        final int n = this.subplots.size();
        int totalWeight = 0;
        for ( int i = 0; i < n; ++i ) {
            final XYPlot sub = this.subplots.get ( i );
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
        for ( int j = 0; j < n; ++j ) {
            final XYPlot plot = this.subplots.get ( j );
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                final double w = usableSize * plot.getWeight() / totalWeight;
                this.subplotAreas[j] = new Rectangle2D.Double ( x, y, w, adjustedPlotArea.getHeight() );
                x = x + w + this.gap;
            } else if ( orientation == PlotOrientation.VERTICAL ) {
                final double h = usableSize * plot.getWeight() / totalWeight;
                this.subplotAreas[j] = new Rectangle2D.Double ( x, y, adjustedPlotArea.getWidth(), h );
                y = y + h + this.gap;
            }
            final AxisSpace subSpace = plot.calculateRangeAxisSpace ( g2, this.subplotAreas[j], null );
            space.ensureAtLeast ( subSpace );
        }
        return space;
    }
    @Override
    public void draw ( final Graphics2D g2, final Rectangle2D area, final Point2D anchor, PlotState parentState, final PlotRenderingInfo info ) {
        if ( info != null ) {
            info.setPlotArea ( area );
        }
        final RectangleInsets insets = this.getInsets();
        insets.trim ( area );
        this.setFixedRangeAxisSpaceForSubplots ( null );
        final AxisSpace space = this.calculateAxisSpace ( g2, area );
        final Rectangle2D dataArea = space.shrink ( area, null );
        this.setFixedRangeAxisSpaceForSubplots ( space );
        final ValueAxis axis = this.getDomainAxis();
        final RectangleEdge edge = this.getDomainAxisEdge();
        final double cursor = RectangleEdge.coordinate ( dataArea, edge );
        final AxisState axisState = axis.draw ( g2, cursor, area, dataArea, edge, info );
        if ( parentState == null ) {
            parentState = new PlotState();
        }
        parentState.getSharedAxisStates().put ( axis, axisState );
        for ( int i = 0; i < this.subplots.size(); ++i ) {
            final XYPlot plot = this.subplots.get ( i );
            PlotRenderingInfo subplotInfo = null;
            if ( info != null ) {
                subplotInfo = new PlotRenderingInfo ( info.getOwner() );
                info.addSubplotInfo ( subplotInfo );
            }
            plot.draw ( g2, this.subplotAreas[i], anchor, parentState, subplotInfo );
        }
        if ( info != null ) {
            info.setDataArea ( dataArea );
        }
    }
    @Override
    public LegendItemCollection getLegendItems() {
        LegendItemCollection result = this.getFixedLegendItems();
        if ( result == null ) {
            result = new LegendItemCollection();
            if ( this.subplots != null ) {
                for ( final XYPlot plot : this.subplots ) {
                    final LegendItemCollection more = plot.getLegendItems();
                    result.addAll ( more );
                }
            }
        }
        return result;
    }
    @Override
    public void zoomRangeAxes ( final double factor, final PlotRenderingInfo info, final Point2D source ) {
        this.zoomRangeAxes ( factor, info, source, false );
    }
    @Override
    public void zoomRangeAxes ( final double factor, final PlotRenderingInfo state, final Point2D source, final boolean useAnchor ) {
        final XYPlot subplot = this.findSubplot ( state, source );
        if ( subplot != null ) {
            subplot.zoomRangeAxes ( factor, state, source, useAnchor );
        } else {
            for ( final XYPlot p : this.subplots ) {
                p.zoomRangeAxes ( factor, state, source, useAnchor );
            }
        }
    }
    @Override
    public void zoomRangeAxes ( final double lowerPercent, final double upperPercent, final PlotRenderingInfo info, final Point2D source ) {
        final XYPlot subplot = this.findSubplot ( info, source );
        if ( subplot != null ) {
            subplot.zoomRangeAxes ( lowerPercent, upperPercent, info, source );
        } else {
            for ( final XYPlot p : this.subplots ) {
                p.zoomRangeAxes ( lowerPercent, upperPercent, info, source );
            }
        }
    }
    @Override
    public void panRangeAxes ( final double panRange, final PlotRenderingInfo info, final Point2D source ) {
        final XYPlot subplot = this.findSubplot ( info, source );
        if ( subplot == null ) {
            return;
        }
        if ( !subplot.isRangePannable() ) {
            return;
        }
        final PlotRenderingInfo subplotInfo = info.getSubplotInfo ( info.getSubplotIndex ( source ) );
        if ( subplotInfo == null ) {
            return;
        }
        for ( int i = 0; i < subplot.getRangeAxisCount(); ++i ) {
            final ValueAxis rangeAxis = subplot.getRangeAxis ( i );
            if ( rangeAxis != null ) {
                rangeAxis.pan ( panRange );
            }
        }
    }
    public XYPlot findSubplot ( final PlotRenderingInfo info, final Point2D source ) {
        ParamChecks.nullNotPermitted ( info, "info" );
        ParamChecks.nullNotPermitted ( source, "source" );
        XYPlot result = null;
        final int subplotIndex = info.getSubplotIndex ( source );
        if ( subplotIndex >= 0 ) {
            result = this.subplots.get ( subplotIndex );
        }
        return result;
    }
    @Override
    public void setRenderer ( final XYItemRenderer renderer ) {
        super.setRenderer ( renderer );
        for ( final XYPlot p : this.subplots ) {
            p.setRenderer ( renderer );
        }
    }
    @Override
    public void setFixedRangeAxisSpace ( final AxisSpace space ) {
        super.setFixedRangeAxisSpace ( space );
        this.setFixedRangeAxisSpaceForSubplots ( space );
        this.fireChangeEvent();
    }
    protected void setFixedRangeAxisSpaceForSubplots ( final AxisSpace space ) {
        for ( final XYPlot p : this.subplots ) {
            p.setFixedRangeAxisSpace ( space, false );
        }
    }
    @Override
    public void handleClick ( final int x, final int y, final PlotRenderingInfo info ) {
        final Rectangle2D dataArea = info.getDataArea();
        if ( dataArea.contains ( x, y ) ) {
            for ( int i = 0; i < this.subplots.size(); ++i ) {
                final XYPlot subplot = this.subplots.get ( i );
                final PlotRenderingInfo subplotInfo = info.getSubplotInfo ( i );
                subplot.handleClick ( x, y, subplotInfo );
            }
        }
    }
    @Override
    public void datasetChanged ( final DatasetChangeEvent event ) {
        super.datasetChanged ( event );
        if ( this.subplots == null ) {
            return;
        }
        XYDataset dataset = null;
        if ( event.getDataset() instanceof XYDataset ) {
            dataset = ( XYDataset ) event.getDataset();
        }
        for ( final XYPlot subplot : this.subplots ) {
            if ( subplot.indexOf ( dataset ) >= 0 ) {
                subplot.configureRangeAxes();
            }
        }
    }
    @Override
    public void plotChanged ( final PlotChangeEvent event ) {
        this.notifyListeners ( event );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof CombinedDomainXYPlot ) ) {
            return false;
        }
        final CombinedDomainXYPlot that = ( CombinedDomainXYPlot ) obj;
        return this.gap == that.gap && ObjectUtilities.equal ( ( Object ) this.subplots, ( Object ) that.subplots ) && super.equals ( obj );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final CombinedDomainXYPlot result = ( CombinedDomainXYPlot ) super.clone();
        result.subplots = ( List<XYPlot> ) ObjectUtilities.deepClone ( ( Collection ) this.subplots );
        for ( final Plot child : result.subplots ) {
            child.setParent ( result );
        }
        final ValueAxis domainAxis = result.getDomainAxis();
        if ( domainAxis != null ) {
            domainAxis.configure();
        }
        return result;
    }
}
