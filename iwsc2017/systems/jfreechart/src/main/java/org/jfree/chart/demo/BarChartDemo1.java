package org.jfree.chart.demo;
import java.awt.Color;
import java.awt.Dimension;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
public class BarChartDemo1 extends ApplicationFrame {
    private static final long serialVersionUID = 1L;
    static {
        ChartFactory.setChartTheme ( new StandardChartTheme ( "JFree/Shadow",
                                     true ) );
    }
    public BarChartDemo1 ( String title ) {
        super ( title );
        CategoryDataset dataset = createDataset();
        JFreeChart chart = createChart ( dataset );
        ChartPanel chartPanel = new ChartPanel ( chart, false );
        chartPanel.setFillZoomRectangle ( true );
        chartPanel.setMouseWheelEnabled ( true );
        chartPanel.setPreferredSize ( new Dimension ( 500, 270 ) );
        setContentPane ( chartPanel );
    }
    private static CategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue ( 7445, "JFreeSVG", "Warm-up" );
        dataset.addValue ( 24448, "Batik", "Warm-up" );
        dataset.addValue ( 4297, "JFreeSVG", "Test" );
        dataset.addValue ( 21022, "Batik", "Test" );
        return dataset;
    }
    private static JFreeChart createChart ( CategoryDataset dataset ) {
        JFreeChart chart = ChartFactory.createBarChart (
                               "Performance: JFreeSVG vs Batik", null  ,
                               "Milliseconds"  , dataset );
        chart.addSubtitle ( new TextTitle ( "Time to generate 1000 charts in SVG "
                                            + "format (lower bars = better performance)" ) );
        chart.setBackgroundPaint ( Color.white );
        CategoryPlot plot = ( CategoryPlot ) chart.getPlot();
        NumberAxis rangeAxis = ( NumberAxis ) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits ( NumberAxis.createIntegerTickUnits() );
        BarRenderer renderer = ( BarRenderer ) plot.getRenderer();
        renderer.setDrawBarOutline ( false );
        chart.getLegend().setFrame ( BlockBorder.NONE );
        return chart;
    }
    public static void main ( String[] args ) {
        BarChartDemo1 demo = new BarChartDemo1 ( "JFreeChart: BarChartDemo1.java" );
        demo.pack();
        RefineryUtilities.centerFrameOnScreen ( demo );
        demo.setVisible ( true );
    }
}
