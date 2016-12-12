package org.jfree.chart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PolarPlot;
import java.awt.event.ActionEvent;
import java.awt.Component;
import java.awt.event.ActionListener;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
public class PolarChartPanel extends ChartPanel {
    private static final String POLAR_ZOOM_IN_ACTION_COMMAND = "Polar Zoom In";
    private static final String POLAR_ZOOM_OUT_ACTION_COMMAND = "Polar Zoom Out";
    private static final String POLAR_AUTO_RANGE_ACTION_COMMAND = "Polar Auto Range";
    public PolarChartPanel ( final JFreeChart chart ) {
        this ( chart, true );
    }
    public PolarChartPanel ( final JFreeChart chart, final boolean useBuffer ) {
        super ( chart, useBuffer );
        this.checkChart ( chart );
        this.setMinimumDrawWidth ( 200 );
        this.setMinimumDrawHeight ( 200 );
        this.setMaximumDrawWidth ( 2000 );
        this.setMaximumDrawHeight ( 2000 );
    }
    @Override
    public void setChart ( final JFreeChart chart ) {
        this.checkChart ( chart );
        super.setChart ( chart );
    }
    @Override
    protected JPopupMenu createPopupMenu ( final boolean properties, final boolean save, final boolean print, final boolean zoom ) {
        final JPopupMenu result = super.createPopupMenu ( properties, save, print, zoom );
        int zoomInIndex = this.getPopupMenuItem ( result, PolarChartPanel.localizationResources.getString ( "Zoom_In" ) );
        int zoomOutIndex = this.getPopupMenuItem ( result, PolarChartPanel.localizationResources.getString ( "Zoom_Out" ) );
        int autoIndex = this.getPopupMenuItem ( result, PolarChartPanel.localizationResources.getString ( "Auto_Range" ) );
        if ( zoom ) {
            final JMenuItem zoomIn = new JMenuItem ( PolarChartPanel.localizationResources.getString ( "Zoom_In" ) );
            zoomIn.setActionCommand ( "Polar Zoom In" );
            zoomIn.addActionListener ( this );
            final JMenuItem zoomOut = new JMenuItem ( PolarChartPanel.localizationResources.getString ( "Zoom_Out" ) );
            zoomOut.setActionCommand ( "Polar Zoom Out" );
            zoomOut.addActionListener ( this );
            final JMenuItem auto = new JMenuItem ( PolarChartPanel.localizationResources.getString ( "Auto_Range" ) );
            auto.setActionCommand ( "Polar Auto Range" );
            auto.addActionListener ( this );
            if ( zoomInIndex != -1 ) {
                result.remove ( zoomInIndex );
            } else {
                zoomInIndex = result.getComponentCount() - 1;
            }
            result.add ( zoomIn, zoomInIndex );
            if ( zoomOutIndex != -1 ) {
                result.remove ( zoomOutIndex );
            } else {
                zoomOutIndex = zoomInIndex + 1;
            }
            result.add ( zoomOut, zoomOutIndex );
            if ( autoIndex != -1 ) {
                result.remove ( autoIndex );
            } else {
                autoIndex = zoomOutIndex + 1;
            }
            result.add ( auto, autoIndex );
        }
        return result;
    }
    @Override
    public void actionPerformed ( final ActionEvent event ) {
        final String command = event.getActionCommand();
        if ( command.equals ( "Polar Zoom In" ) ) {
            final PolarPlot plot = ( PolarPlot ) this.getChart().getPlot();
            plot.zoom ( 0.5 );
        } else if ( command.equals ( "Polar Zoom Out" ) ) {
            final PolarPlot plot = ( PolarPlot ) this.getChart().getPlot();
            plot.zoom ( 2.0 );
        } else if ( command.equals ( "Polar Auto Range" ) ) {
            final PolarPlot plot = ( PolarPlot ) this.getChart().getPlot();
            plot.getAxis().setAutoRange ( true );
        } else {
            super.actionPerformed ( event );
        }
    }
    private void checkChart ( final JFreeChart chart ) {
        final Plot plot = chart.getPlot();
        if ( ! ( plot instanceof PolarPlot ) ) {
            throw new IllegalArgumentException ( "plot is not a PolarPlot" );
        }
    }
    private int getPopupMenuItem ( final JPopupMenu menu, final String text ) {
        int index = -1;
        for ( int i = 0; index == -1 && i < menu.getComponentCount(); ++i ) {
            final Component comp = menu.getComponent ( i );
            if ( comp instanceof JMenuItem ) {
                final JMenuItem item = ( JMenuItem ) comp;
                if ( text.equals ( item.getText() ) ) {
                    index = i;
                }
            }
        }
        return index;
    }
}
