package org.jfree.chart.fx.demo;
import static javafx.application.Application.launch;
import java.awt.Color;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
public class BarChartFXDemo1 extends Application implements ChartMouseListenerFX {
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
    @Override
    public void start ( Stage stage ) throws Exception {
        CategoryDataset dataset = createDataset();
        JFreeChart chart = createChart ( dataset );
        ChartViewer viewer = new ChartViewer ( chart );
        viewer.addChartMouseListener ( this );
        stage.setScene ( new Scene ( viewer ) );
        stage.setTitle ( "JFreeChart: BarChartFXDemo1.java" );
        stage.setWidth ( 700 );
        stage.setHeight ( 390 );
        stage.show();
    }
    public static void main ( String[] args ) {
        launch ( args );
    }
    @Override
    public void chartMouseClicked ( ChartMouseEventFX event ) {
        System.out.println ( event );
    }
    @Override
    public void chartMouseMoved ( ChartMouseEventFX event ) {
        System.out.println ( event );
    }
}
