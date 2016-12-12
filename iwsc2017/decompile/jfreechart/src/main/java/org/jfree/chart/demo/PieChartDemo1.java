package org.jfree.chart.demo;
import org.jfree.chart.ChartTheme;
import org.jfree.chart.StandardChartTheme;
import java.awt.Window;
import org.jfree.ui.RefineryUtilities;
import java.awt.Dimension;
import org.jfree.chart.ChartPanel;
import org.jfree.ui.RectangleInsets;
import javax.swing.JPanel;
import java.awt.RadialGradientPaint;
import org.jfree.chart.title.Title;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.title.TextTitle;
import java.awt.Stroke;
import java.awt.BasicStroke;
import org.jfree.chart.plot.PiePlot;
import java.awt.Font;
import org.jfree.ui.HorizontalAlignment;
import java.awt.Paint;
import java.awt.geom.Point2D;
import java.awt.GradientPaint;
import java.awt.Color;
import java.awt.Point;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import java.awt.Container;
import org.jfree.ui.ApplicationFrame;
public class PieChartDemo1 extends ApplicationFrame {
    private static final long serialVersionUID = 1L;
    public PieChartDemo1 ( final String title ) {
        super ( title );
        this.setContentPane ( ( Container ) createDemoPanel() );
    }
    private static PieDataset createDataset() {
        final DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue ( "Samsung", new Double ( 27.8 ) );
        dataset.setValue ( "Others", new Double ( 55.3 ) );
        dataset.setValue ( "Nokia", new Double ( 16.8 ) );
        dataset.setValue ( "Apple", new Double ( 17.1 ) );
        return dataset;
    }
    private static JFreeChart createChart ( final PieDataset dataset ) {
        final JFreeChart chart = ChartFactory.createPieChart ( "Smart Phones Manufactured / Q3 2011", dataset, false, true, false );
        chart.setBackgroundPaint ( new GradientPaint ( new Point ( 0, 0 ), new Color ( 20, 20, 20 ), new Point ( 400, 200 ), Color.DARK_GRAY ) );
        final TextTitle t = chart.getTitle();
        t.setHorizontalAlignment ( HorizontalAlignment.LEFT );
        t.setPaint ( new Color ( 240, 240, 240 ) );
        t.setFont ( new Font ( "Arial", 1, 26 ) );
        final PiePlot plot = ( PiePlot ) chart.getPlot();
        plot.setBackgroundPaint ( null );
        plot.setInteriorGap ( 0.04 );
        plot.setOutlineVisible ( false );
        plot.setSectionPaint ( "Others", createGradientPaint ( new Color ( 200, 200, 255 ), Color.BLUE ) );
        plot.setSectionPaint ( "Samsung", createGradientPaint ( new Color ( 255, 200, 200 ), Color.RED ) );
        plot.setSectionPaint ( "Apple", createGradientPaint ( new Color ( 200, 255, 200 ), Color.GREEN ) );
        plot.setSectionPaint ( "Nokia", createGradientPaint ( new Color ( 200, 255, 200 ), Color.YELLOW ) );
        plot.setBaseSectionOutlinePaint ( Color.WHITE );
        plot.setSectionOutlinesVisible ( true );
        plot.setBaseSectionOutlineStroke ( new BasicStroke ( 2.0f ) );
        plot.setLabelFont ( new Font ( "Courier New", 1, 20 ) );
        plot.setLabelLinkPaint ( Color.WHITE );
        plot.setLabelLinkStroke ( new BasicStroke ( 2.0f ) );
        plot.setLabelOutlineStroke ( null );
        plot.setLabelPaint ( Color.WHITE );
        plot.setLabelBackgroundPaint ( null );
        final TextTitle source = new TextTitle ( "Source: http://www.bbc.co.uk/news/business-15489523", new Font ( "Courier New", 0, 12 ) );
        source.setPaint ( Color.WHITE );
        source.setPosition ( RectangleEdge.BOTTOM );
        source.setHorizontalAlignment ( HorizontalAlignment.RIGHT );
        chart.addSubtitle ( source );
        return chart;
    }
    private static RadialGradientPaint createGradientPaint ( final Color c1, final Color c2 ) {
        final Point2D center = new Point2D.Float ( 0.0f, 0.0f );
        final float radius = 200.0f;
        final float[] dist = { 0.0f, 1.0f };
        return new RadialGradientPaint ( center, radius, dist, new Color[] { c1, c2 } );
    }
    public static JPanel createDemoPanel() {
        final JFreeChart chart = createChart ( createDataset() );
        chart.setPadding ( new RectangleInsets ( 4.0, 8.0, 2.0, 2.0 ) );
        final ChartPanel panel = new ChartPanel ( chart, false );
        panel.setMouseWheelEnabled ( true );
        panel.setPreferredSize ( new Dimension ( 600, 300 ) );
        return panel;
    }
    public static void main ( final String[] args ) {
        final PieChartDemo1 demo = new PieChartDemo1 ( "JFreeChart: Pie Chart Demo 1" );
        demo.pack();
        RefineryUtilities.centerFrameOnScreen ( ( Window ) demo );
        demo.setVisible ( true );
    }
    static {
        ChartFactory.setChartTheme ( new StandardChartTheme ( "JFree/Shadow", true ) );
    }
}
