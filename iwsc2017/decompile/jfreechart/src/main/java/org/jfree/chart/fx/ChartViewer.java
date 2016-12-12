package org.jfree.chart.fx;
import javafx.stage.WindowEvent;
import javafx.event.ActionEvent;
import java.util.Iterator;
import org.jfree.chart.fx.interaction.ChartMouseEventFX;
import java.io.IOException;
import java.io.File;
import org.jfree.ui.Drawable;
import javafx.stage.FileChooser;
import org.jfree.chart.util.ExportUtils;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.ContextMenu;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.util.ParamChecks;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.fx.interaction.ChartMouseListenerFX;
import javafx.scene.control.Skinnable;
import javafx.scene.control.Control;
public class ChartViewer extends Control implements Skinnable, ChartMouseListenerFX {
    private JFreeChart chart;
    private ChartCanvas canvas;
    private boolean tooltipEnabled;
    private transient List<ChartMouseListenerFX> chartMouseListeners;
    public ChartViewer() {
        this ( null );
    }
    public ChartViewer ( final JFreeChart chart ) {
        this ( chart, true );
    }
    public ChartViewer ( final JFreeChart chart, final boolean contextMenuEnabled ) {
        this.chart = chart;
        this.getStyleClass().add ( ( Object ) "chart-control" );
        this.setContextMenu ( this.createContextMenu() );
        this.getContextMenu().setOnShowing ( e -> this.setTooltipEnabled ( false ) );
        this.getContextMenu().setOnHiding ( e -> this.setTooltipEnabled ( true ) );
        this.tooltipEnabled = true;
        this.chartMouseListeners = new ArrayList<ChartMouseListenerFX>();
    }
    public String getUserAgentStylesheet() {
        return ChartViewer.class.getResource ( "chart-viewer.css" ).toExternalForm();
    }
    public JFreeChart getChart() {
        return this.chart;
    }
    public void setChart ( final JFreeChart chart ) {
        ParamChecks.nullNotPermitted ( chart, "chart" );
        this.chart = chart;
        final ChartViewerSkin skin = ( ChartViewerSkin ) this.getSkin();
        skin.setChart ( chart );
    }
    public ChartCanvas getCanvas() {
        final ChartViewerSkin skin = ( ChartViewerSkin ) this.getSkin();
        return skin.getCanvas();
    }
    public boolean isTooltipEnabled() {
        return this.tooltipEnabled;
    }
    public void setTooltipEnabled ( final boolean enabled ) {
        this.tooltipEnabled = enabled;
        final ChartViewerSkin skin = ( ChartViewerSkin ) this.getSkin();
        if ( skin != null ) {
            skin.setTooltipEnabled ( enabled );
        }
    }
    public ChartRenderingInfo getRenderingInfo() {
        final ChartViewerSkin skin = ( ChartViewerSkin ) this.getSkin();
        if ( skin != null ) {
            return skin.getRenderingInfo();
        }
        return null;
    }
    public void hideZoomRectangle() {
        final ChartViewerSkin skin = ( ChartViewerSkin ) this.getSkin();
        skin.setZoomRectangleVisible ( false );
    }
    public void showZoomRectangle ( final double x, final double y, final double w, final double h ) {
        final ChartViewerSkin skin = ( ChartViewerSkin ) this.getSkin();
        skin.showZoomRectangle ( x, y, w, h );
    }
    public void addChartMouseListener ( final ChartMouseListenerFX listener ) {
        ParamChecks.nullNotPermitted ( listener, "listener" );
        this.chartMouseListeners.add ( listener );
    }
    public void removeChartMouseListener ( final ChartMouseListenerFX listener ) {
        ParamChecks.nullNotPermitted ( listener, "listener" );
        this.chartMouseListeners.remove ( listener );
    }
    private ContextMenu createContextMenu() {
        final ContextMenu menu = new ContextMenu();
        final Menu export = new Menu ( "Export As" );
        final MenuItem pngItem = new MenuItem ( "PNG..." );
        pngItem.setOnAction ( e -> this.handleExportToPNG() );
        export.getItems().add ( ( Object ) pngItem );
        final MenuItem jpegItem = new MenuItem ( "JPEG..." );
        jpegItem.setOnAction ( e -> this.handleExportToJPEG() );
        export.getItems().add ( ( Object ) jpegItem );
        if ( ExportUtils.isOrsonPDFAvailable() ) {
            final MenuItem pdfItem = new MenuItem ( "PDF..." );
            pdfItem.setOnAction ( e -> this.handleExportToPDF() );
            export.getItems().add ( ( Object ) pdfItem );
        }
        if ( ExportUtils.isJFreeSVGAvailable() ) {
            final MenuItem svgItem = new MenuItem ( "SVG..." );
            svgItem.setOnAction ( e -> this.handleExportToSVG() );
            export.getItems().add ( ( Object ) svgItem );
        }
        menu.getItems().add ( ( Object ) export );
        return menu;
    }
    private void handleExportToPDF() {
        final FileChooser chooser = new FileChooser();
        chooser.setTitle ( "Export to PDF" );
        final FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter ( "Portable Document Format (PDF)", new String[] { "pdf" } );
        chooser.getExtensionFilters().add ( ( Object ) filter );
        final File file = chooser.showSaveDialog ( this.getScene().getWindow() );
        if ( file != null ) {
            ExportUtils.writeAsPDF ( ( Drawable ) this.chart, ( int ) this.getWidth(), ( int ) this.getHeight(), file );
        }
    }
    private void handleExportToSVG() {
        final FileChooser chooser = new FileChooser();
        chooser.setTitle ( "Export to SVG" );
        final FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter ( "Scalable Vector Graphics (SVG)", new String[] { "svg" } );
        chooser.getExtensionFilters().add ( ( Object ) filter );
        final File file = chooser.showSaveDialog ( this.getScene().getWindow() );
        if ( file != null ) {
            ExportUtils.writeAsSVG ( ( Drawable ) this.chart, ( int ) this.getWidth(), ( int ) this.getHeight(), file );
        }
    }
    private void handleExportToPNG() {
        final FileChooser chooser = new FileChooser();
        chooser.setTitle ( "Export to PNG" );
        final FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter ( "Portable Network Graphics (PNG)", new String[] { "png" } );
        chooser.getExtensionFilters().add ( ( Object ) filter );
        final File file = chooser.showSaveDialog ( this.getScene().getWindow() );
        if ( file != null ) {
            try {
                ExportUtils.writeAsPNG ( ( Drawable ) this.chart, ( int ) this.getWidth(), ( int ) this.getHeight(), file );
            } catch ( IOException ex ) {}
        }
    }
    private void handleExportToJPEG() {
        final FileChooser chooser = new FileChooser();
        chooser.setTitle ( "Export to JPEG" );
        final FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter ( "JPEG", new String[] { "jpg" } );
        chooser.getExtensionFilters().add ( ( Object ) filter );
        final File file = chooser.showSaveDialog ( this.getScene().getWindow() );
        if ( file != null ) {
            try {
                ExportUtils.writeAsJPEG ( ( Drawable ) this.chart, ( int ) this.getWidth(), ( int ) this.getHeight(), file );
            } catch ( IOException ex ) {}
        }
    }
    public void chartMouseClicked ( final ChartMouseEventFX event ) {
        for ( final ChartMouseListenerFX listener : this.chartMouseListeners ) {
            listener.chartMouseClicked ( event );
        }
    }
    public void chartMouseMoved ( final ChartMouseEventFX event ) {
        for ( final ChartMouseListenerFX listener : this.chartMouseListeners ) {
            listener.chartMouseMoved ( event );
        }
    }
}
