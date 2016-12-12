package org.jfree.chart.editor;
import org.jfree.chart.JFreeChart;
public class DefaultChartEditorFactory implements ChartEditorFactory {
    @Override
    public ChartEditor createEditor ( final JFreeChart chart ) {
        return new DefaultChartEditor ( chart );
    }
}
