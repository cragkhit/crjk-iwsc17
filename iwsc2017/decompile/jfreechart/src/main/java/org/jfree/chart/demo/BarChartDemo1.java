package org.jfree.chart.demo;
import org.jfree.chart.ChartTheme;
import org.jfree.chart.StandardChartTheme;
import java.awt.Window;
import org.jfree.ui.RefineryUtilities;
import org.jfree.chart.block.BlockFrame;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import java.awt.Paint;
import java.awt.Color;
import org.jfree.chart.title.Title;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ChartFactory;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.CategoryDataset;
import java.awt.Container;
import java.awt.Dimension;
import org.jfree.chart.ChartPanel;
import org.jfree.ui.ApplicationFrame;
public class BarChartDemo1 extends ApplicationFrame {
    private static final long serialVersionUID = 1L;
    public BarChartDemo1 ( final String title ) {
        super ( title );
        final CategoryDataset dataset = createDataset();
        final JFreeChart chart = createChart ( dataset );
        final ChartPanel chartPanel = new ChartPanel ( chart, false );
        chartPanel.setFillZoomRectangle ( true );
        chartPanel.setMouseWheelEnabled ( true );
        chartPanel.setPreferredSize ( new Dimension ( 500, 270 ) );
        this.setContentPane ( ( Container ) chartPanel );
    }
    private static CategoryDataset createDataset() {
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue ( 7445.0, "JFreeSVG", "Warm-up" );
        dataset.addValue ( 24448.0, "Batik", "Warm-up" );
        dataset.addValue ( 4297.0, "JFreeSVG", "Test" );
        dataset.addValue ( 21022.0, "Batik", "Test" );
        return dataset;
    }
    private static JFreeChart createChart ( final CategoryDataset dataset ) {
        final JFreeChart chart = ChartFactory.createBarChart ( "Performance: JFreeSVG vs Batik", null, "Milliseconds", dataset );
        chart.addSubtitle ( new TextTitle ( "Time to generate 1000 charts in SVG format (lower bars = better performance)" ) );
        chart.setBackgroundPaint ( Color.white );
        final CategoryPlot plot = ( CategoryPlot ) chart.getPlot();
        final NumberAxis rangeAxis = ( NumberAxis ) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits ( NumberAxis.createIntegerTickUnits() );
        final BarRenderer renderer = ( BarRenderer ) plot.getRenderer();
        renderer.setDrawBarOutline ( false );
        chart.getLegend().setFrame ( BlockBorder.NONE );
        return chart;
    }
    public static void main ( final String[] args ) {
        final BarChartDemo1 demo = new BarChartDemo1 ( "JFreeChart: BarChartDemo1.java" );
        demo.pack();
        RefineryUtilities.centerFrameOnScreen ( ( Window ) demo );
        demo.setVisible ( true );
    }
    static {
        ChartFactory.setChartTheme ( new StandardChartTheme ( "JFree/Shadow", true ) );
    }
}
