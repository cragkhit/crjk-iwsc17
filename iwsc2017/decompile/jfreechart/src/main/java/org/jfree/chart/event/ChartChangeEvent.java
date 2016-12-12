package org.jfree.chart.event;
import org.jfree.chart.JFreeChart;
import java.util.EventObject;
public class ChartChangeEvent extends EventObject {
    private ChartChangeEventType type;
    private JFreeChart chart;
    public ChartChangeEvent ( final Object source ) {
        this ( source, null, ChartChangeEventType.GENERAL );
    }
    public ChartChangeEvent ( final Object source, final JFreeChart chart ) {
        this ( source, chart, ChartChangeEventType.GENERAL );
    }
    public ChartChangeEvent ( final Object source, final JFreeChart chart, final ChartChangeEventType type ) {
        super ( source );
        this.chart = chart;
        this.type = type;
    }
    public JFreeChart getChart() {
        return this.chart;
    }
    public void setChart ( final JFreeChart chart ) {
        this.chart = chart;
    }
    public ChartChangeEventType getType() {
        return this.type;
    }
    public void setType ( final ChartChangeEventType type ) {
        this.type = type;
    }
}
