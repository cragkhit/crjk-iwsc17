

package org.jfree.chart.editor;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.util.ParamChecks;


public class ChartEditorManager {


    static ChartEditorFactory factory = new DefaultChartEditorFactory();


    private ChartEditorManager() {
    }


    public static ChartEditorFactory getChartEditorFactory() {
        return factory;
    }


    public static void setChartEditorFactory ( ChartEditorFactory f ) {
        ParamChecks.nullNotPermitted ( f, "f" );
        factory = f;
    }


    public static ChartEditor getChartEditor ( JFreeChart chart ) {
        return factory.createEditor ( chart );
    }
}
