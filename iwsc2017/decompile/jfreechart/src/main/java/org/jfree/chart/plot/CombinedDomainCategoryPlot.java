package org.jfree.chart.plot;
import java.util.Collection;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.LegendItemCollection;
import org.jfree.data.Range;
import org.jfree.chart.util.ShadowGenerator;
import org.jfree.chart.axis.AxisState;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.axis.AxisSpace;
import java.awt.Graphics2D;
import java.util.Iterator;
import java.awt.geom.Point2D;
import java.util.Collections;
import org.jfree.ui.RectangleInsets;
import org.jfree.chart.util.ParamChecks;
import java.util.ArrayList;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.category.CategoryDataset;
import org.jfree.chart.axis.CategoryAxis;
import java.awt.geom.Rectangle2D;
import java.util.List;
import org.jfree.chart.event.PlotChangeListener;
public class CombinedDomainCategoryPlot extends CategoryPlot implements PlotChangeListener {
    private static final long serialVersionUID = 8207194522653701572L;
    private List subplots;
    private double gap;
    private transient Rectangle2D[] subplotAreas;
    public CombinedDomainCategoryPlot() {
        this ( new CategoryAxis() );
    }
    public CombinedDomainCategoryPlot ( final CategoryAxis domainAxis ) {
        super ( null, domainAxis, null, null );
        this.subplots = new ArrayList();
        this.gap = 5.0;
    }
    public double getGap() {
        return this.gap;
    }
    public void setGap ( final double gap ) {
        this.gap = gap;
        this.fireChangeEvent();
    }
    public void add ( final CategoryPlot subplot ) {
        this.add ( subplot, 1 );
    }
    public void add ( final CategoryPlot subplot, final int weight ) {
        ParamChecks.nullNotPermitted ( subplot, "subplot" );
        if ( weight < 1 ) {
            throw new IllegalArgumentException ( "Require weight >= 1." );
        }
        subplot.setParent ( this );
        subplot.setWeight ( weight );
        subplot.setInsets ( new RectangleInsets ( 0.0, 0.0, 0.0, 0.0 ) );
        subplot.setDomainAxis ( null );
        subplot.setOrientation ( this.getOrientation() );
        subplot.addChangeListener ( this );
        this.subplots.add ( subplot );
        final CategoryAxis axis = this.getDomainAxis();
        if ( axis != null ) {
            axis.configure();
        }
        this.fireChangeEvent();
    }
    public void remove ( final CategoryPlot subplot ) {
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
            final CategoryAxis domain = this.getDomainAxis();
            if ( domain != null ) {
                domain.configure();
            }
            this.fireChangeEvent();
        }
    }
    public List getSubplots() {
        if ( this.subplots != null ) {
            return Collections.unmodifiableList ( ( List<?> ) this.subplots );
        }
        return Collections.EMPTY_LIST;
    }
    public CategoryPlot findSubplot ( final PlotRenderingInfo info, final Point2D source ) {
        ParamChecks.nullNotPermitted ( info, "info" );
        ParamChecks.nullNotPermitted ( source, "source" );
        CategoryPlot result = null;
        final int subplotIndex = info.getSubplotIndex ( source );
        if ( subplotIndex >= 0 ) {
            result = this.subplots.get ( subplotIndex );
        }
        return result;
    }
    @Override
    public void zoomRangeAxes ( final double factor, final PlotRenderingInfo info, final Point2D source ) {
        this.zoomRangeAxes ( factor, info, source, false );
    }
    @Override
    public void zoomRangeAxes ( final double factor, final PlotRenderingInfo info, final Point2D source, final boolean useAnchor ) {
        CategoryPlot subplot = this.findSubplot ( info, source );
        if ( subplot != null ) {
            subplot.zoomRangeAxes ( factor, info, source, useAnchor );
        } else {
            final Iterator iterator = this.getSubplots().iterator();
            while ( iterator.hasNext() ) {
                subplot = iterator.next();
                subplot.zoomRangeAxes ( factor, info, source, useAnchor );
            }
        }
    }
    @Override
    public void zoomRangeAxes ( final double lowerPercent, final double upperPercent, final PlotRenderingInfo info, final Point2D source ) {
        CategoryPlot subplot = this.findSubplot ( info, source );
        if ( subplot != null ) {
            subplot.zoomRangeAxes ( lowerPercent, upperPercent, info, source );
        } else {
            final Iterator iterator = this.getSubplots().iterator();
            while ( iterator.hasNext() ) {
                subplot = iterator.next();
                subplot.zoomRangeAxes ( lowerPercent, upperPercent, info, source );
            }
        }
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
            final CategoryAxis categoryAxis = this.getDomainAxis();
            final RectangleEdge categoryEdge = Plot.resolveDomainAxisLocation ( this.getDomainAxisLocation(), orientation );
            if ( categoryAxis != null ) {
                space = categoryAxis.reserveSpace ( g2, this, plotArea, categoryEdge, space );
            } else if ( this.getDrawSharedDomainAxis() ) {
                space = this.getDomainAxis().reserveSpace ( g2, this, plotArea, categoryEdge, space );
            }
        }
        final Rectangle2D adjustedPlotArea = space.shrink ( plotArea, null );
        final int n = this.subplots.size();
        int totalWeight = 0;
        for ( int i = 0; i < n; ++i ) {
            final CategoryPlot sub = this.subplots.get ( i );
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
            final CategoryPlot plot = this.subplots.get ( j );
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
        area.setRect ( area.getX() + insets.getLeft(), area.getY() + insets.getTop(), area.getWidth() - insets.getLeft() - insets.getRight(), area.getHeight() - insets.getTop() - insets.getBottom() );
        this.setFixedRangeAxisSpaceForSubplots ( null );
        final AxisSpace space = this.calculateAxisSpace ( g2, area );
        final Rectangle2D dataArea = space.shrink ( area, null );
        this.setFixedRangeAxisSpaceForSubplots ( space );
        final CategoryAxis axis = this.getDomainAxis();
        final RectangleEdge domainEdge = this.getDomainAxisEdge();
        final double cursor = RectangleEdge.coordinate ( dataArea, domainEdge );
        final AxisState axisState = axis.draw ( g2, cursor, area, dataArea, domainEdge, info );
        if ( parentState == null ) {
            parentState = new PlotState();
        }
        parentState.getSharedAxisStates().put ( axis, axisState );
        for ( int i = 0; i < this.subplots.size(); ++i ) {
            final CategoryPlot plot = this.subplots.get ( i );
            PlotRenderingInfo subplotInfo = null;
            if ( info != null ) {
                subplotInfo = new PlotRenderingInfo ( info.getOwner() );
                info.addSubplotInfo ( subplotInfo );
            }
            Point2D subAnchor = null;
            if ( anchor != null && this.subplotAreas[i].contains ( anchor ) ) {
                subAnchor = anchor;
            }
            plot.draw ( g2, this.subplotAreas[i], subAnchor, parentState, subplotInfo );
        }
        if ( info != null ) {
            info.setDataArea ( dataArea );
        }
    }
    protected void setFixedRangeAxisSpaceForSubplots ( final AxisSpace space ) {
        for ( final CategoryPlot plot : this.subplots ) {
            plot.setFixedRangeAxisSpace ( space, false );
        }
    }
    @Override
    public void setOrientation ( final PlotOrientation orientation ) {
        super.setOrientation ( orientation );
        for ( final CategoryPlot plot : this.subplots ) {
            plot.setOrientation ( orientation );
        }
    }
    @Override
    public void setShadowGenerator ( final ShadowGenerator generator ) {
        this.setNotify ( false );
        super.setShadowGenerator ( generator );
        for ( final CategoryPlot plot : this.subplots ) {
            plot.setShadowGenerator ( generator );
        }
        this.setNotify ( true );
    }
    @Override
    public Range getDataRange ( final ValueAxis axis ) {
        return super.getDataRange ( axis );
    }
    @Override
    public LegendItemCollection getLegendItems() {
        LegendItemCollection result = this.getFixedLegendItems();
        if ( result == null ) {
            result = new LegendItemCollection();
            if ( this.subplots != null ) {
                for ( final CategoryPlot plot : this.subplots ) {
                    final LegendItemCollection more = plot.getLegendItems();
                    result.addAll ( more );
                }
            }
        }
        return result;
    }
    @Override
    public List getCategories() {
        final List result = new ArrayList();
        if ( this.subplots != null ) {
            for ( final CategoryPlot plot : this.subplots ) {
                final List more = plot.getCategories();
                for ( final Comparable category : more ) {
                    if ( !result.contains ( category ) ) {
                        result.add ( category );
                    }
                }
            }
        }
        return Collections.unmodifiableList ( ( List<?> ) result );
    }
    @Override
    public List getCategoriesForAxis ( final CategoryAxis axis ) {
        return this.getCategories();
    }
    @Override
    public void handleClick ( final int x, final int y, final PlotRenderingInfo info ) {
        final Rectangle2D dataArea = info.getDataArea();
        if ( dataArea.contains ( x, y ) ) {
            for ( int i = 0; i < this.subplots.size(); ++i ) {
                final CategoryPlot subplot = this.subplots.get ( i );
                final PlotRenderingInfo subplotInfo = info.getSubplotInfo ( i );
                subplot.handleClick ( x, y, subplotInfo );
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
        if ( ! ( obj instanceof CombinedDomainCategoryPlot ) ) {
            return false;
        }
        final CombinedDomainCategoryPlot that = ( CombinedDomainCategoryPlot ) obj;
        return this.gap == that.gap && ObjectUtilities.equal ( ( Object ) this.subplots, ( Object ) that.subplots ) && super.equals ( obj );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final CombinedDomainCategoryPlot result = ( CombinedDomainCategoryPlot ) super.clone();
        result.subplots = ( List ) ObjectUtilities.deepClone ( ( Collection ) this.subplots );
        for ( final Plot child : result.subplots ) {
            child.setParent ( result );
        }
        return result;
    }
}
