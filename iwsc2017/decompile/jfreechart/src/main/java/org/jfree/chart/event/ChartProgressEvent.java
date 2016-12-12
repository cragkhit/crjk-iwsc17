package org.jfree.chart.event;
import org.jfree.chart.JFreeChart;
import java.util.EventObject;
public class ChartProgressEvent extends EventObject {
    public static final int DRAWING_STARTED = 1;
    public static final int DRAWING_FINISHED = 2;
    private int type;
    private int percent;
    private JFreeChart chart;
    public ChartProgressEvent ( final Object source, final JFreeChart chart, final int type, final int percent ) {
        super ( source );
        this.chart = chart;
        this.type = type;
        this.percent = percent;
    }
    public JFreeChart getChart() {
        return this.chart;
    }
    public void setChart ( final JFreeChart chart ) {
        this.chart = chart;
    }
    public int getType() {
        return this.type;
    }
    public void setType ( final int type ) {
        this.type = type;
    }
    public int getPercent() {
        return this.percent;
    }
    public void setPercent ( final int percent ) {
        this.percent = percent;
    }
}
