package org.jfree.chart.fx;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.fx.interaction.ZoomHandlerFX;
import org.jfree.chart.util.ParamChecks;
public class ChartViewerSkin extends SkinBase<ChartViewer>  {
    private ChartCanvas canvas;
    private Rectangle zoomRectangle;
    public ChartViewerSkin ( ChartViewer control ) {
        super ( control );
        getChildren().add ( createNode ( control ) );
        this.zoomRectangle = new Rectangle ( 0, 0, new Color ( 0, 0, 1, 0.25 ) );
        this.zoomRectangle.setManaged ( false );
        this.zoomRectangle.setVisible ( false );
        getChildren().add ( this.zoomRectangle );
    }
    public ChartCanvas getCanvas() {
        return this.canvas;
    }
    public ChartRenderingInfo getRenderingInfo() {
        return this.canvas.getRenderingInfo();
    }
    public void setChart ( JFreeChart chart ) {
        this.canvas.setChart ( chart );
    }
    public void setTooltipEnabled ( boolean enabled ) {
        this.canvas.setTooltipEnabled ( enabled );
    }
    public Paint getZoomFillPaint() {
        return this.zoomRectangle.getFill();
    }
    public void setZoomFillPaint ( Paint paint ) {
        this.zoomRectangle.setFill ( paint );
    }
    public void addChartMouseListener ( ChartMouseListenerFX listener ) {
        ParamChecks.nullNotPermitted ( listener, "listener" );
        this.canvas.addChartMouseListener ( listener );
    }
    public void removeChartMouseListener ( ChartMouseListenerFX listener ) {
        this.canvas.removeChartMouseListener ( listener );
    }
    public void setZoomRectangleVisible ( boolean visible ) {
        this.zoomRectangle.setVisible ( visible );
    }
    public void showZoomRectangle ( double x, double y, double w, double h ) {
        this.zoomRectangle.setX ( x );
        this.zoomRectangle.setY ( y );
        this.zoomRectangle.setWidth ( w );
        this.zoomRectangle.setHeight ( h );
        this.zoomRectangle.setVisible ( true );
    }
    private BorderPane createNode ( ChartViewer control ) {
        BorderPane borderPane = new BorderPane();
        borderPane.setPrefSize ( 800, 500 );
        StackPane sp = new StackPane();
        sp.setMinSize ( 10, 10 );
        sp.setPrefSize ( 600, 400 );
        this.canvas = new ChartCanvas ( getSkinnable().getChart() );
        this.canvas.setTooltipEnabled ( control.isTooltipEnabled() );
        this.canvas.addChartMouseListener ( control );
        this.canvas.widthProperty().bind ( sp.widthProperty() );
        this.canvas.heightProperty().bind ( sp.heightProperty() );
        this.canvas.addMouseHandler ( new ZoomHandlerFX ( "zoom", control ) );
        sp.getChildren().add ( this.canvas );
        borderPane.setCenter ( sp );
        return borderPane;
    }
}
