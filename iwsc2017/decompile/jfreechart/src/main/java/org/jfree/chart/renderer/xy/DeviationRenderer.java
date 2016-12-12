package org.jfree.chart.renderer.xy;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.ui.RectangleEdge;
import java.awt.Shape;
import java.awt.Composite;
import java.awt.AlphaComposite;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.axis.ValueAxis;
import java.awt.geom.GeneralPath;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
public class DeviationRenderer extends XYLineAndShapeRenderer {
    private float alpha;
    public DeviationRenderer() {
        this ( true, true );
    }
    public DeviationRenderer ( final boolean lines, final boolean shapes ) {
        super ( lines, shapes );
        super.setDrawSeriesLineAsPath ( true );
        this.alpha = 0.5f;
    }
    public float getAlpha() {
        return this.alpha;
    }
    public void setAlpha ( final float alpha ) {
        if ( alpha < 0.0f || alpha > 1.0f ) {
            throw new IllegalArgumentException ( "Requires 'alpha' in the range 0.0 to 1.0." );
        }
        this.alpha = alpha;
        this.fireChangeEvent();
    }
    @Override
    public void setDrawSeriesLineAsPath ( final boolean flag ) {
    }
    @Override
    public Range findRangeBounds ( final XYDataset dataset ) {
        return this.findRangeBounds ( dataset, true );
    }
    @Override
    public XYItemRendererState initialise ( final Graphics2D g2, final Rectangle2D dataArea, final XYPlot plot, final XYDataset dataset, final PlotRenderingInfo info ) {
        final State state = new State ( info );
        state.seriesPath = new GeneralPath();
        state.setProcessVisibleItemsOnly ( false );
        return state;
    }
    @Override
    public int getPassCount() {
        return 3;
    }
    @Override
    protected boolean isItemPass ( final int pass ) {
        return pass == 2;
    }
    @Override
    protected boolean isLinePass ( final int pass ) {
        return pass == 1;
    }
    @Override
    public void drawItem ( final Graphics2D g2, final XYItemRendererState state, final Rectangle2D dataArea, final PlotRenderingInfo info, final XYPlot plot, final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset, final int series, final int item, final CrosshairState crosshairState, final int pass ) {
        if ( !this.getItemVisible ( series, item ) ) {
            return;
        }
        if ( pass == 0 ) {
            final IntervalXYDataset intervalDataset = ( IntervalXYDataset ) dataset;
            final State drState = ( State ) state;
            final double x = intervalDataset.getXValue ( series, item );
            final double yLow = intervalDataset.getStartYValue ( series, item );
            final double yHigh = intervalDataset.getEndYValue ( series, item );
            final RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
            final RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
            final double xx = domainAxis.valueToJava2D ( x, dataArea, xAxisLocation );
            final double yyLow = rangeAxis.valueToJava2D ( yLow, dataArea, yAxisLocation );
            final double yyHigh = rangeAxis.valueToJava2D ( yHigh, dataArea, yAxisLocation );
            final PlotOrientation orientation = plot.getOrientation();
            if ( orientation == PlotOrientation.HORIZONTAL ) {
                drState.lowerCoordinates.add ( new double[] { yyLow, xx } );
                drState.upperCoordinates.add ( new double[] { yyHigh, xx } );
            } else if ( orientation == PlotOrientation.VERTICAL ) {
                drState.lowerCoordinates.add ( new double[] { xx, yyLow } );
                drState.upperCoordinates.add ( new double[] { xx, yyHigh } );
            }
            if ( item == dataset.getItemCount ( series ) - 1 ) {
                final Composite originalComposite = g2.getComposite();
                g2.setComposite ( AlphaComposite.getInstance ( 3, this.alpha ) );
                g2.setPaint ( this.getItemFillPaint ( series, item ) );
                final GeneralPath area = new GeneralPath ( 1, drState.lowerCoordinates.size() + drState.upperCoordinates.size() );
                double[] coords = drState.lowerCoordinates.get ( 0 );
                area.moveTo ( ( float ) coords[0], ( float ) coords[1] );
                for ( int i = 1; i < drState.lowerCoordinates.size(); ++i ) {
                    coords = drState.lowerCoordinates.get ( i );
                    area.lineTo ( ( float ) coords[0], ( float ) coords[1] );
                }
                final int count = drState.upperCoordinates.size();
                coords = drState.upperCoordinates.get ( count - 1 );
                area.lineTo ( ( float ) coords[0], ( float ) coords[1] );
                for ( int j = count - 2; j >= 0; --j ) {
                    coords = drState.upperCoordinates.get ( j );
                    area.lineTo ( ( float ) coords[0], ( float ) coords[1] );
                }
                area.closePath();
                g2.fill ( area );
                g2.setComposite ( originalComposite );
                drState.lowerCoordinates.clear();
                drState.upperCoordinates.clear();
            }
        }
        if ( this.isLinePass ( pass ) ) {
            if ( item == 0 ) {
                final State s = ( State ) state;
                s.seriesPath.reset();
                s.setLastPointGood ( false );
            }
            if ( this.getItemLineVisible ( series, item ) ) {
                this.drawPrimaryLineAsPath ( state, g2, plot, dataset, pass, series, item, domainAxis, rangeAxis, dataArea );
            }
        } else if ( this.isItemPass ( pass ) ) {
            EntityCollection entities = null;
            if ( info != null ) {
                entities = info.getOwner().getEntityCollection();
            }
            this.drawSecondaryPass ( g2, plot, dataset, pass, series, item, domainAxis, dataArea, rangeAxis, crosshairState, entities );
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DeviationRenderer ) ) {
            return false;
        }
        final DeviationRenderer that = ( DeviationRenderer ) obj;
        return this.alpha == that.alpha && super.equals ( obj );
    }
    public static class State extends XYLineAndShapeRenderer.State {
        public List upperCoordinates;
        public List lowerCoordinates;
        public State ( final PlotRenderingInfo info ) {
            super ( info );
            this.lowerCoordinates = new ArrayList();
            this.upperCoordinates = new ArrayList();
        }
    }
}
