package org.jfree.chart.fx;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Skinnable;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import org.jfree.chart.util.ExportUtils;
import org.jfree.chart.util.ParamChecks;
public class ChartViewer extends Control implements Skinnable,
    ChartMouseListenerFX {
    private JFreeChart chart;
    private ChartCanvas canvas;
    private boolean tooltipEnabled;
    private transient List<ChartMouseListenerFX> chartMouseListeners;
    public ChartViewer() {
        this ( null );
    }
    public ChartViewer ( JFreeChart chart ) {
        this ( chart, true );
    }
    public ChartViewer ( JFreeChart chart, boolean contextMenuEnabled ) {
        this.chart = chart;
        getStyleClass().add ( "chart-control" );
        setContextMenu ( createContextMenu() );
        getContextMenu().setOnShowing (
            e -> ChartViewer.this.setTooltipEnabled ( false ) );
        getContextMenu().setOnHiding (
            e -> ChartViewer.this.setTooltipEnabled ( true ) );
        this.tooltipEnabled = true;
        this.chartMouseListeners = new ArrayList<>();
    }
    @Override
    public String getUserAgentStylesheet() {
        return ChartViewer.class.getResource ( "chart-viewer.css" )
               .toExternalForm();
    }
    public JFreeChart getChart() {
        return this.chart;
    }
    public void setChart ( JFreeChart chart ) {
        ParamChecks.nullNotPermitted ( chart, "chart" );
        this.chart = chart;
        ChartViewerSkin skin = ( ChartViewerSkin ) getSkin();
        skin.setChart ( chart );
    }
    public ChartCanvas getCanvas() {
        ChartViewerSkin skin = ( ChartViewerSkin ) getSkin();
        return skin.getCanvas();
    }
    public boolean isTooltipEnabled() {
        return this.tooltipEnabled;
    }
    public void setTooltipEnabled ( boolean enabled ) {
        this.tooltipEnabled = enabled;
        ChartViewerSkin skin = ( ChartViewerSkin ) getSkin();
        if ( skin != null ) {
            skin.setTooltipEnabled ( enabled );
        }
    }
    public ChartRenderingInfo getRenderingInfo() {
        ChartViewerSkin skin = ( ChartViewerSkin ) getSkin();
        if ( skin != null ) {
            return skin.getRenderingInfo();
        }
        return null;
    }
    public void hideZoomRectangle() {
        ChartViewerSkin skin = ( ChartViewerSkin ) getSkin();
        skin.setZoomRectangleVisible ( false );
    }
    public void showZoomRectangle ( double x, double y, double w, double h ) {
        ChartViewerSkin skin = ( ChartViewerSkin ) getSkin();
        skin.showZoomRectangle ( x, y, w, h );
    }
    public void addChartMouseListener ( ChartMouseListenerFX listener ) {
        ParamChecks.nullNotPermitted ( listener, "listener" );
        this.chartMouseListeners.add ( listener );
    }
    public void removeChartMouseListener ( ChartMouseListenerFX listener ) {
        ParamChecks.nullNotPermitted ( listener, "listener" );
        this.chartMouseListeners.remove ( listener );
    }
    private ContextMenu createContextMenu() {
        final ContextMenu menu = new ContextMenu();
        Menu export = new Menu ( "Export As" );
        MenuItem pngItem = new MenuItem ( "PNG..." );
        pngItem.setOnAction ( e -> handleExportToPNG() );
        export.getItems().add ( pngItem );
        MenuItem jpegItem = new MenuItem ( "JPEG..." );
        jpegItem.setOnAction ( e -> handleExportToJPEG() );
        export.getItems().add ( jpegItem );
        if ( ExportUtils.isOrsonPDFAvailable() ) {
            MenuItem pdfItem = new MenuItem ( "PDF..." );
            pdfItem.setOnAction ( e -> handleExportToPDF() );
            export.getItems().add ( pdfItem );
        }
        if ( ExportUtils.isJFreeSVGAvailable() ) {
            MenuItem svgItem = new MenuItem ( "SVG..." );
            svgItem.setOnAction ( e -> handleExportToSVG() );
            export.getItems().add ( svgItem );
        }
        menu.getItems().add ( export );
        return menu;
    }
    private void handleExportToPDF() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle ( "Export to PDF" );
        ExtensionFilter filter = new FileChooser.ExtensionFilter (
            "Portable Document Format (PDF)", "pdf" );
        chooser.getExtensionFilters().add ( filter );
        File file = chooser.showSaveDialog ( getScene().getWindow() );
        if ( file != null ) {
            ExportUtils.writeAsPDF ( this.chart, ( int ) getWidth(),
                                     ( int ) getHeight(), file );
        }
    }
    private void handleExportToSVG() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle ( "Export to SVG" );
        ExtensionFilter filter = new FileChooser.ExtensionFilter (
            "Scalable Vector Graphics (SVG)", "svg" );
        chooser.getExtensionFilters().add ( filter );
        File file = chooser.showSaveDialog ( getScene().getWindow() );
        if ( file != null ) {
            ExportUtils.writeAsSVG ( this.chart, ( int ) getWidth(),
                                     ( int ) getHeight(), file );
        }
    }
    private void handleExportToPNG() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle ( "Export to PNG" );
        ExtensionFilter filter = new FileChooser.ExtensionFilter (
            "Portable Network Graphics (PNG)", "png" );
        chooser.getExtensionFilters().add ( filter );
        File file = chooser.showSaveDialog ( getScene().getWindow() );
        if ( file != null ) {
            try {
                ExportUtils.writeAsPNG ( this.chart, ( int ) getWidth(),
                                         ( int ) getHeight(), file );
            } catch ( IOException ex ) {
            }
        }
    }
    private void handleExportToJPEG() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle ( "Export to JPEG" );
        ExtensionFilter filter = new FileChooser.ExtensionFilter ( "JPEG", "jpg" );
        chooser.getExtensionFilters().add ( filter );
        File file = chooser.showSaveDialog ( getScene().getWindow() );
        if ( file != null ) {
            try {
                ExportUtils.writeAsJPEG ( this.chart, ( int ) getWidth(),
                                          ( int ) getHeight(), file );
            } catch ( IOException ex ) {
            }
        }
    }
    @Override
    public void chartMouseClicked ( ChartMouseEventFX event ) {
        for ( ChartMouseListenerFX listener : this.chartMouseListeners ) {
            listener.chartMouseClicked ( event );
        }
    }
    @Override
    public void chartMouseMoved ( ChartMouseEventFX event ) {
        for ( ChartMouseListenerFX listener : this.chartMouseListeners ) {
            listener.chartMouseMoved ( event );
        }
    }
}
