package org.jfree.chart.plot;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.LegendItemCollection;
import org.jfree.data.Range;
import org.jfree.chart.util.ShadowGenerator;
import java.util.Iterator;
import org.jfree.chart.axis.AxisState;
import java.awt.geom.Point2D;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.axis.AxisSpace;
import java.awt.Graphics2D;
import java.util.Collections;
import org.jfree.ui.RectangleInsets;
import org.jfree.chart.util.ParamChecks;
import java.util.ArrayList;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.data.category.CategoryDataset;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.NumberAxis;
import java.awt.geom.Rectangle2D;
import java.util.List;
import org.jfree.chart.event.PlotChangeListener;
public class CombinedRangeCategoryPlot extends CategoryPlot implements PlotChangeListener {
    private static final long serialVersionUID = 7260210007554504515L;
    private List subplots;
    private double gap;
    private transient Rectangle2D[] subplotArea;
    public CombinedRangeCategoryPlot() {
        this ( new NumberAxis() );
    }
    public CombinedRangeCategoryPlot ( final ValueAxis rangeAxis ) {
        super ( null, null, rangeAxis, null );
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
        if ( weight <= 0 ) {
            throw new IllegalArgumentException ( "Require weight >= 1." );
        }
        subplot.setParent ( this );
        subplot.setWeight ( weight );
        subplot.setInsets ( new RectangleInsets ( 0.0, 0.0, 0.0, 0.0 ) );
        subplot.setRangeAxis ( null );
        subplot.setOrientation ( this.getOrientation() );
        subplot.addChangeListener ( this );
        this.subplots.add ( subplot );
        final ValueAxis axis = this.getRangeAxis();
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
            final ValueAxis range = this.getRangeAxis();
            if ( range != null ) {
                range.configure();
            }
            final ValueAxis range2 = this.getRangeAxis ( 1 );
            if ( range2 != null ) {
                range2.configure();
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
    @Override
    protected AxisSpace calculateAxisSpace ( final Graphics2D g2, final Rectangle2D plotArea ) {
        AxisSpace space = new AxisSpace();
        final PlotOrientation orientation = this.getOrientation();
        final AxisSpace fixed = this.getFixedRangeAxisSpace();
        if ( fixed != null ) {
            if ( orientation == PlotOrientation.VERTICAL ) {
                space.setLeft ( fixed.getLeft() );
                space.setRight ( fixed.getRight() );
            } else if ( orientation == PlotOrientation.HORIZONTAL ) {
                space.setTop ( fixed.getTop() );
                space.setBottom ( fixed.getBottom() );
            }
        } else {
            final ValueAxis valueAxis = this.getRangeAxis();
            final RectangleEdge valueEdge = Plot.resolveRangeAxisLocation ( this.getRangeAxisLocation(), orientation );
            if ( valueAxis != null ) {
                space = valueAxis.reserveSpace ( g2, this, plotArea, valueEdge, space );
            }
        }
        final Rectangle2D adjustedPlotArea = space.shrink ( plotArea, null );
        final int n = this.subplots.size();
        int totalWeight = 0;
        for ( int i = 0; i < n; ++i ) {
            final CategoryPlot sub = this.subplots.get ( i );
            totalWeight += sub.getWeight();
        }
        this.subplotArea = new Rectangle2D[n];
        double x = adjustedPlotArea.getX();
        double y = adjustedPlotArea.getY();
        double usableSize = 0.0;
        if ( orientation == PlotOrientation.VERTICAL ) {
            usableSize = adjustedPlotArea.getWidth() - this.gap * ( n - 1 );
        } else if ( orientation == PlotOrientation.HORIZONTAL ) {
            usableSize = adjustedPlotArea.getHeight() - this.gap * ( n - 1 );
        }
        for ( int j = 0; j < n; ++j ) {
            final CategoryPlot plot = this.subplots.get ( j );
            if ( orientation == PlotOrientation.VERTICAL ) {
                final double w = usableSize * plot.getWeight() / totalWeight;
                this.subplotArea[j] = new Rectangle2D.Double ( x, y, w, adjustedPlotArea.getHeight() );
                x = x + w + this.gap;
            } else if ( orientation == PlotOrientation.HORIZONTAL ) {
                final double h = usableSize * plot.getWeight() / totalWeight;
                this.subplotArea[j] = new Rectangle2D.Double ( x, y, adjustedPlotArea.getWidth(), h );
                y = y + h + this.gap;
            }
            final AxisSpace subSpace = plot.calculateDomainAxisSpace ( g2, this.subplotArea[j], null );
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
        final AxisSpace space = this.calculateAxisSpace ( g2, area );
        final Rectangle2D dataArea = space.shrink ( area, null );
        this.setFixedDomainAxisSpaceForSubplots ( space );
        final ValueAxis axis = this.getRangeAxis();
        final RectangleEdge rangeEdge = this.getRangeAxisEdge();
        final double cursor = RectangleEdge.coordinate ( dataArea, rangeEdge );
        final AxisState state = axis.draw ( g2, cursor, area, dataArea, rangeEdge, info );
        if ( parentState == null ) {
            parentState = new PlotState();
        }
        parentState.getSharedAxisStates().put ( axis, state );
        for ( int i = 0; i < this.subplots.size(); ++i ) {
            final CategoryPlot plot = this.subplots.get ( i );
            PlotRenderingInfo subplotInfo = null;
            if ( info != null ) {
                subplotInfo = new PlotRenderingInfo ( info.getOwner() );
                info.addSubplotInfo ( subplotInfo );
            }
            Point2D subAnchor = null;
            if ( anchor != null && this.subplotArea[i].contains ( anchor ) ) {
                subAnchor = anchor;
            }
            plot.draw ( g2, this.subplotArea[i], subAnchor, parentState, subplotInfo );
        }
        if ( info != null ) {
            info.setDataArea ( dataArea );
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
        Range result = null;
        if ( this.subplots != null ) {
            for ( final CategoryPlot subplot : this.subplots ) {
                result = Range.combine ( result, subplot.getDataRange ( axis ) );
            }
        }
        return result;
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
    protected void setFixedDomainAxisSpaceForSubplots ( final AxisSpace space ) {
        for ( final CategoryPlot plot : this.subplots ) {
            plot.setFixedDomainAxisSpace ( space, false );
        }
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
        if ( ! ( obj instanceof CombinedRangeCategoryPlot ) ) {
            return false;
        }
        final CombinedRangeCategoryPlot that = ( CombinedRangeCategoryPlot ) obj;
        return this.gap == that.gap && ObjectUtilities.equal ( ( Object ) this.subplots, ( Object ) that.subplots ) && super.equals ( obj );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final CombinedRangeCategoryPlot result = ( CombinedRangeCategoryPlot ) super.clone();
        result.subplots = ( List ) ObjectUtilities.deepClone ( ( Collection ) this.subplots );
        for ( final Plot child : result.subplots ) {
            child.setParent ( result );
        }
        final ValueAxis rangeAxis = result.getRangeAxis();
        if ( rangeAxis != null ) {
            rangeAxis.configure();
        }
        return result;
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        final ValueAxis rangeAxis = this.getRangeAxis();
        if ( rangeAxis != null ) {
            rangeAxis.configure();
        }
    }
}
