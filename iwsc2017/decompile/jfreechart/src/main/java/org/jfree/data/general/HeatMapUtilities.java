package org.jfree.data.general;
import java.awt.Paint;
import java.awt.Graphics2D;
import org.jfree.chart.util.ParamChecks;
import java.awt.image.BufferedImage;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYDataset;
public abstract class HeatMapUtilities {
    public static XYDataset extractRowFromHeatMapDataset ( final HeatMapDataset dataset, final int row, final Comparable seriesName ) {
        final XYSeries series = new XYSeries ( seriesName );
        for ( int cols = dataset.getXSampleCount(), c = 0; c < cols; ++c ) {
            series.add ( dataset.getXValue ( c ), dataset.getZValue ( c, row ) );
        }
        final XYSeriesCollection result = new XYSeriesCollection ( series );
        return result;
    }
    public static XYDataset extractColumnFromHeatMapDataset ( final HeatMapDataset dataset, final int column, final Comparable seriesName ) {
        final XYSeries series = new XYSeries ( seriesName );
        for ( int rows = dataset.getYSampleCount(), r = 0; r < rows; ++r ) {
            series.add ( dataset.getYValue ( r ), dataset.getZValue ( column, r ) );
        }
        final XYSeriesCollection result = new XYSeriesCollection ( series );
        return result;
    }
    public static BufferedImage createHeatMapImage ( final HeatMapDataset dataset, final PaintScale paintScale ) {
        ParamChecks.nullNotPermitted ( dataset, "dataset" );
        ParamChecks.nullNotPermitted ( paintScale, "paintScale" );
        final int xCount = dataset.getXSampleCount();
        final int yCount = dataset.getYSampleCount();
        final BufferedImage image = new BufferedImage ( xCount, yCount, 2 );
        final Graphics2D g2 = image.createGraphics();
        for ( int xIndex = 0; xIndex < xCount; ++xIndex ) {
            for ( int yIndex = 0; yIndex < yCount; ++yIndex ) {
                final double z = dataset.getZValue ( xIndex, yIndex );
                final Paint p = paintScale.getPaint ( z );
                g2.setPaint ( p );
                g2.fillRect ( xIndex, yCount - yIndex - 1, 1, 1 );
            }
        }
        return image;
    }
}
