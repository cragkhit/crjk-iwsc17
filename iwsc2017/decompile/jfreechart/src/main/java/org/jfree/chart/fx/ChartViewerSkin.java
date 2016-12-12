package org.jfree.chart.fx;
import javafx.scene.Node;
import org.jfree.chart.fx.interaction.MouseHandlerFX;
import org.jfree.chart.fx.interaction.ZoomHandlerFX;
import javafx.beans.value.ObservableValue;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import org.jfree.chart.util.ParamChecks;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartRenderingInfo;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.control.Control;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.SkinBase;
public class ChartViewerSkin extends SkinBase<ChartViewer> {
    private ChartCanvas canvas;
    private Rectangle zoomRectangle;
    public ChartViewerSkin ( final ChartViewer control ) {
        super ( ( Control ) control );
        this.getChildren().add ( ( Object ) this.createNode ( control ) );
        ( this.zoomRectangle = new Rectangle ( 0.0, 0.0, ( Paint ) new Color ( 0.0, 0.0, 1.0, 0.25 ) ) ).setManaged ( false );
        this.zoomRectangle.setVisible ( false );
        this.getChildren().add ( ( Object ) this.zoomRectangle );
    }
    public ChartCanvas getCanvas() {
        return this.canvas;
    }
    public ChartRenderingInfo getRenderingInfo() {
        return this.canvas.getRenderingInfo();
    }
    public void setChart ( final JFreeChart chart ) {
        this.canvas.setChart ( chart );
    }
    public void setTooltipEnabled ( final boolean enabled ) {
        this.canvas.setTooltipEnabled ( enabled );
    }
    public Paint getZoomFillPaint() {
        return this.zoomRectangle.getFill();
    }
    public void setZoomFillPaint ( final Paint paint ) {
        this.zoomRectangle.setFill ( paint );
    }
    public void addChartMouseListener ( final ChartMouseListenerFX listener ) {
        ParamChecks.nullNotPermitted ( listener, "listener" );
        this.canvas.addChartMouseListener ( listener );
    }
    public void removeChartMouseListener ( final ChartMouseListenerFX listener ) {
        this.canvas.removeChartMouseListener ( listener );
    }
    public void setZoomRectangleVisible ( final boolean visible ) {
        this.zoomRectangle.setVisible ( visible );
    }
    public void showZoomRectangle ( final double x, final double y, final double w, final double h ) {
        this.zoomRectangle.setX ( x );
        this.zoomRectangle.setY ( y );
        this.zoomRectangle.setWidth ( w );
        this.zoomRectangle.setHeight ( h );
        this.zoomRectangle.setVisible ( true );
    }
    private BorderPane createNode ( final ChartViewer control ) {
        final BorderPane borderPane = new BorderPane();
        borderPane.setPrefSize ( 800.0, 500.0 );
        final StackPane sp = new StackPane();
        sp.setMinSize ( 10.0, 10.0 );
        sp.setPrefSize ( 600.0, 400.0 );
        ( this.canvas = new ChartCanvas ( ( ( ChartViewer ) this.getSkinnable() ).getChart() ) ).setTooltipEnabled ( control.isTooltipEnabled() );
        this.canvas.addChartMouseListener ( control );
        this.canvas.widthProperty().bind ( ( ObservableValue ) sp.widthProperty() );
        this.canvas.heightProperty().bind ( ( ObservableValue ) sp.heightProperty() );
        this.canvas.addMouseHandler ( new ZoomHandlerFX ( "zoom", control ) );
        sp.getChildren().add ( ( Object ) this.canvas );
        borderPane.setCenter ( ( Node ) sp );
        return borderPane;
    }
}
