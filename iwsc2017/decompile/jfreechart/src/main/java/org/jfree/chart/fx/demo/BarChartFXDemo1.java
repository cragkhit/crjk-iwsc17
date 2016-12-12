package org.jfree.chart.fx.demo;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.jfree.chart.fx.ChartViewer;
import javafx.stage.Stage;
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
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.CategoryDataset;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import javafx.application.Application;
public class BarChartFXDemo1 extends Application implements ChartMouseListenerFX {
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
    public void start ( final Stage stage ) throws Exception {
        final CategoryDataset dataset = createDataset();
        final JFreeChart chart = createChart ( dataset );
        final ChartViewer viewer = new ChartViewer ( chart );
        viewer.addChartMouseListener ( this );
        stage.setScene ( new Scene ( ( Parent ) viewer ) );
        stage.setTitle ( "JFreeChart: BarChartFXDemo1.java" );
        stage.setWidth ( 700.0 );
        stage.setHeight ( 390.0 );
        stage.show();
    }
    public static void main ( final String[] args ) {
        launch ( args );
    }
    public void chartMouseClicked ( final ChartMouseEventFX event ) {
        System.out.println ( event );
    }
    public void chartMouseMoved ( final ChartMouseEventFX event ) {
        System.out.println ( event );
    }
}
