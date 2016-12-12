package org.jfree.chart.fx.demo;
import org.jfree.chart.axis.ValueAxis;
import java.awt.geom.Rectangle2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import javafx.application.Platform;
import org.jfree.chart.fx.overlay.OverlayFX;
import java.awt.Stroke;
import java.awt.Paint;
import java.awt.BasicStroke;
import java.awt.Color;
import org.jfree.chart.fx.overlay.CrosshairOverlayFX;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.fx.ChartViewer;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYDataset;
import javafx.application.Application;
public class CrosshairOverlayFXDemo1 extends Application {
    public static XYDataset createDataset() {
        final XYSeries series = new XYSeries ( "S1" );
        for ( int x = 0; x < 10; ++x ) {
            series.add ( x, x + Math.random() * 4.0 );
        }
        final XYSeriesCollection dataset = new XYSeriesCollection ( series );
        return dataset;
    }
    public static JFreeChart createChart ( final XYDataset dataset ) {
        final JFreeChart chart = ChartFactory.createXYLineChart ( "CrosshairOverlayDemo1", "X", "Y", dataset );
        return chart;
    }
    public void start ( final Stage stage ) throws Exception {
        stage.setScene ( new Scene ( ( Parent ) new MyDemoPane() ) );
        stage.setTitle ( "JFreeChart: CrosshairOverlayFXDemo1.java" );
        stage.setWidth ( 700.0 );
        stage.setHeight ( 390.0 );
        stage.show();
    }
    public static void main ( final String[] args ) {
        launch ( args );
    }
    static class MyDemoPane extends StackPane implements ChartMouseListenerFX {
        private ChartViewer chartViewer;
        private Crosshair xCrosshair;
        private Crosshair yCrosshair;
        public MyDemoPane() {
            final XYDataset dataset = CrosshairOverlayFXDemo1.createDataset();
            final JFreeChart chart = CrosshairOverlayFXDemo1.createChart ( dataset );
            ( this.chartViewer = new ChartViewer ( chart ) ).addChartMouseListener ( this );
            this.getChildren().add ( ( Object ) this.chartViewer );
            final CrosshairOverlayFX crosshairOverlay = new CrosshairOverlayFX();
            ( this.xCrosshair = new Crosshair ( Double.NaN, Color.GRAY, new BasicStroke ( 0.0f ) ) ).setStroke ( new BasicStroke ( 1.5f, 1, 1, 1.0f, new float[] { 2.0f, 2.0f }, 0.0f ) );
            this.xCrosshair.setLabelVisible ( true );
            ( this.yCrosshair = new Crosshair ( Double.NaN, Color.GRAY, new BasicStroke ( 0.0f ) ) ).setStroke ( new BasicStroke ( 1.5f, 1, 1, 1.0f, new float[] { 2.0f, 2.0f }, 0.0f ) );
            this.yCrosshair.setLabelVisible ( true );
            crosshairOverlay.addDomainCrosshair ( this.xCrosshair );
            crosshairOverlay.addRangeCrosshair ( this.yCrosshair );
            Platform.runLater ( () -> this.chartViewer.getCanvas().addOverlay ( crosshairOverlay ) );
        }
        public void chartMouseClicked ( final ChartMouseEventFX event ) {
        }
        public void chartMouseMoved ( final ChartMouseEventFX event ) {
            final Rectangle2D dataArea = this.chartViewer.getCanvas().getRenderingInfo().getPlotInfo().getDataArea();
            final JFreeChart chart = event.getChart();
            final XYPlot plot = ( XYPlot ) chart.getPlot();
            final ValueAxis xAxis = plot.getDomainAxis();
            double x = xAxis.java2DToValue ( event.getTrigger().getX(), dataArea, RectangleEdge.BOTTOM );
            if ( !xAxis.getRange().contains ( x ) ) {
                x = Double.NaN;
            }
            final double y = DatasetUtilities.findYValue ( plot.getDataset(), 0, x );
            this.xCrosshair.setValue ( x );
            this.yCrosshair.setValue ( y );
        }
    }
}
