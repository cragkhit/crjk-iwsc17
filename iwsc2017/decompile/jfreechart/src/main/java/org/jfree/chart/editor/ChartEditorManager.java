package org.jfree.chart.editor;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.util.ParamChecks;
public class ChartEditorManager {
    static ChartEditorFactory factory;
    public static ChartEditorFactory getChartEditorFactory() {
        return ChartEditorManager.factory;
    }
    public static void setChartEditorFactory ( final ChartEditorFactory f ) {
        ParamChecks.nullNotPermitted ( f, "f" );
        ChartEditorManager.factory = f;
    }
    public static ChartEditor getChartEditor ( final JFreeChart chart ) {
        return ChartEditorManager.factory.createEditor ( chart );
    }
    static {
        ChartEditorManager.factory = new DefaultChartEditorFactory();
    }
}
