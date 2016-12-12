package org.jfree.chart.renderer.xy;
import java.util.List;
import java.awt.geom.GeneralPath;
import org.jfree.util.ObjectUtilities;
import org.jfree.ui.RectangleEdge;
import java.util.ArrayList;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.GradientPaint;
import java.awt.geom.Point2D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.plot.XYPlot;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jfree.chart.util.ParamChecks;
import org.jfree.ui.GradientPaintTransformer;
public class XYSplineRenderer extends XYLineAndShapeRenderer {
    private int precision;
    private FillType fillType;
    private GradientPaintTransformer gradientPaintTransformer;
    public XYSplineRenderer() {
        this ( 5, FillType.NONE );
    }
    public XYSplineRenderer ( final int precision ) {
        this ( precision, FillType.NONE );
    }
    public XYSplineRenderer ( final int precision, final FillType fillType ) {
        if ( precision <= 0 ) {
            throw new IllegalArgumentException ( "Requires precision > 0." );
        }
        ParamChecks.nullNotPermitted ( fillType, "fillType" );
        this.precision = precision;
        this.fillType = fillType;
        this.gradientPaintTransformer = ( GradientPaintTransformer ) new StandardGradientPaintTransformer();
    }
    public int getPrecision() {
        return this.precision;
    }
    public void setPrecision ( final int p ) {
        if ( p <= 0 ) {
            throw new IllegalArgumentException ( "Requires p > 0." );
        }
        this.precision = p;
        this.fireChangeEvent();
    }
    public FillType getFillType() {
        return this.fillType;
    }
    public void setFillType ( final FillType fillType ) {
        this.fillType = fillType;
        this.fireChangeEvent();
    }
    public GradientPaintTransformer getGradientPaintTransformer() {
        return this.gradientPaintTransformer;
    }
    public void setGradientPaintTransformer ( final GradientPaintTransformer gpt ) {
        this.gradientPaintTransformer = gpt;
        this.fireChangeEvent();
    }
    @Override
    public XYItemRendererState initialise ( final Graphics2D g2, final Rectangle2D dataArea, final XYPlot plot, final XYDataset data, final PlotRenderingInfo info ) {
        this.setDrawSeriesLineAsPath ( true );
        final XYSplineState state = new XYSplineState ( info );
        state.setProcessVisibleItemsOnly ( false );
        return state;
    }
    @Override
    protected void drawPrimaryLineAsPath ( final XYItemRendererState state, final Graphics2D g2, final XYPlot plot, final XYDataset dataset, final int pass, final int series, final int item, final ValueAxis xAxis, final ValueAxis yAxis, final Rectangle2D dataArea ) {
        final XYSplineState s = ( XYSplineState ) state;
        final RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        final RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
        final double x1 = dataset.getXValue ( series, item );
        final double y1 = dataset.getYValue ( series, item );
        final double transX1 = xAxis.valueToJava2D ( x1, dataArea, xAxisLocation );
        final double transY1 = yAxis.valueToJava2D ( y1, dataArea, yAxisLocation );
        if ( !Double.isNaN ( transX1 ) && !Double.isNaN ( transY1 ) ) {
            final Point2D p = ( plot.getOrientation() == PlotOrientation.HORIZONTAL ) ? new Point2D.Float ( ( float ) transY1, ( float ) transX1 ) : new Point2D.Float ( ( float ) transX1, ( float ) transY1 );
            if ( !s.points.contains ( p ) ) {
                s.points.add ( p );
            }
        }
        if ( item == dataset.getItemCount ( series ) - 1 ) {
            if ( s.points.size() > 1 ) {
                Point2D origin;
                if ( this.fillType == FillType.TO_ZERO ) {
                    final float xz = ( float ) xAxis.valueToJava2D ( 0.0, dataArea, yAxisLocation );
                    final float yz = ( float ) yAxis.valueToJava2D ( 0.0, dataArea, yAxisLocation );
                    origin = ( ( plot.getOrientation() == PlotOrientation.HORIZONTAL ) ? new Point2D.Float ( yz, xz ) : new Point2D.Float ( xz, yz ) );
                } else if ( this.fillType == FillType.TO_LOWER_BOUND ) {
                    final float xlb = ( float ) xAxis.valueToJava2D ( xAxis.getLowerBound(), dataArea, xAxisLocation );
                    final float ylb = ( float ) yAxis.valueToJava2D ( yAxis.getLowerBound(), dataArea, yAxisLocation );
                    origin = ( ( plot.getOrientation() == PlotOrientation.HORIZONTAL ) ? new Point2D.Float ( ylb, xlb ) : new Point2D.Float ( xlb, ylb ) );
                } else {
                    final float xub = ( float ) xAxis.valueToJava2D ( xAxis.getUpperBound(), dataArea, xAxisLocation );
                    final float yub = ( float ) yAxis.valueToJava2D ( yAxis.getUpperBound(), dataArea, yAxisLocation );
                    origin = ( ( plot.getOrientation() == PlotOrientation.HORIZONTAL ) ? new Point2D.Float ( yub, xub ) : new Point2D.Float ( xub, yub ) );
                }
                final Point2D cp0 = s.points.get ( 0 );
                s.seriesPath.moveTo ( cp0.getX(), cp0.getY() );
                if ( this.fillType != FillType.NONE ) {
                    if ( plot.getOrientation() == PlotOrientation.HORIZONTAL ) {
                        s.fillArea.moveTo ( origin.getX(), cp0.getY() );
                    } else {
                        s.fillArea.moveTo ( cp0.getX(), origin.getY() );
                    }
                    s.fillArea.lineTo ( cp0.getX(), cp0.getY() );
                }
                if ( s.points.size() == 2 ) {
                    final Point2D cp = s.points.get ( 1 );
                    if ( this.fillType != FillType.NONE ) {
                        s.fillArea.lineTo ( cp.getX(), cp.getY() );
                        s.fillArea.lineTo ( cp.getX(), origin.getY() );
                        s.fillArea.closePath();
                    }
                    s.seriesPath.lineTo ( cp.getX(), cp.getY() );
                } else {
                    final int np = s.points.size();
                    final float[] d = new float[np];
                    final float[] x2 = new float[np];
                    final float[] a = new float[np];
                    final float[] h = new float[np];
                    for ( int i = 0; i < np; ++i ) {
                        final Point2D.Float cpi = s.points.get ( i );
                        x2[i] = cpi.x;
                        d[i] = cpi.y;
                    }
                    for ( int i = 1; i <= np - 1; ++i ) {
                        h[i] = x2[i] - x2[i - 1];
                    }
                    final float[] sub = new float[np - 1];
                    final float[] diag = new float[np - 1];
                    final float[] sup = new float[np - 1];
                    for ( int j = 1; j <= np - 2; ++j ) {
                        diag[j] = ( h[j] + h[j + 1] ) / 3.0f;
                        sup[j] = h[j + 1] / 6.0f;
                        sub[j] = h[j] / 6.0f;
                        a[j] = ( d[j + 1] - d[j] ) / h[j + 1] - ( d[j] - d[j - 1] ) / h[j];
                    }
                    this.solveTridiag ( sub, diag, sup, a, np - 2 );
                    final float oldt = x2[0];
                    final float oldy = d[0];
                    for ( int j = 1; j <= np - 1; ++j ) {
                        for ( int k = 1; k <= this.precision; ++k ) {
                            final float t1 = h[j] * k / this.precision;
                            final float t2 = h[j] - t1;
                            final float y2 = ( ( -a[j - 1] / 6.0f * ( t2 + h[j] ) * t1 + d[j - 1] ) * t2 + ( -a[j] / 6.0f * ( t1 + h[j] ) * t2 + d[j] ) * t1 ) / h[j];
                            final float t3 = x2[j - 1] + t1;
                            s.seriesPath.lineTo ( t3, y2 );
                            if ( this.fillType != FillType.NONE ) {
                                s.fillArea.lineTo ( t3, y2 );
                            }
                        }
                    }
                }
                if ( this.fillType != FillType.NONE ) {
                    if ( plot.getOrientation() == PlotOrientation.HORIZONTAL ) {
                        s.fillArea.lineTo ( origin.getX(), s.points.get ( s.points.size() - 1 ).getY() );
                    } else {
                        s.fillArea.lineTo ( s.points.get ( s.points.size() - 1 ).getX(), origin.getY() );
                    }
                    s.fillArea.closePath();
                }
                if ( this.fillType != FillType.NONE ) {
                    final Paint fp = this.getSeriesFillPaint ( series );
                    if ( this.gradientPaintTransformer != null && fp instanceof GradientPaint ) {
                        final GradientPaint gp = this.gradientPaintTransformer.transform ( ( GradientPaint ) fp, ( Shape ) s.fillArea );
                        g2.setPaint ( gp );
                    } else {
                        g2.setPaint ( fp );
                    }
                    g2.fill ( s.fillArea );
                    s.fillArea.reset();
                }
                this.drawFirstPassShape ( g2, pass, series, item, s.seriesPath );
            }
            s.points = new ArrayList<Point2D>();
        }
    }
    private void solveTridiag ( final float[] sub, final float[] diag, final float[] sup, final float[] b, final int n ) {
        for ( int i = 2; i <= n; ++i ) {
            final int n2 = i;
            sub[n2] /= diag[i - 1];
            final int n3 = i;
            diag[n3] -= sub[i] * sup[i - 1];
            final int n4 = i;
            b[n4] -= sub[i] * b[i - 1];
        }
        b[n] /= diag[n];
        for ( int i = n - 1; i >= 1; --i ) {
            b[i] = ( b[i] - sup[i] * b[i + 1] ) / diag[i];
        }
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof XYSplineRenderer ) ) {
            return false;
        }
        final XYSplineRenderer that = ( XYSplineRenderer ) obj;
        return this.precision == that.precision && this.fillType == that.fillType && ObjectUtilities.equal ( ( Object ) this.gradientPaintTransformer, ( Object ) that.gradientPaintTransformer ) && super.equals ( obj );
    }
    public enum FillType {
        NONE,
        TO_ZERO,
        TO_LOWER_BOUND,
        TO_UPPER_BOUND;
    }
    public static class XYSplineState extends State {
        public GeneralPath fillArea;
        public List<Point2D> points;
        public XYSplineState ( final PlotRenderingInfo info ) {
            super ( info );
            this.fillArea = new GeneralPath();
            this.points = new ArrayList<Point2D>();
        }
    }
}
