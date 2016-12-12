package org.jfree.chart.plot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.general.DatasetUtilities;
public class PlotUtilities {
    public static boolean isEmptyOrNull ( final XYPlot plot ) {
        if ( plot != null ) {
            for ( int i = 0, n = plot.getDatasetCount(); i < n; ++i ) {
                final XYDataset dataset = plot.getDataset ( i );
                if ( !DatasetUtilities.isEmptyOrNull ( dataset ) ) {
                    return false;
                }
            }
        }
        return true;
    }
}
