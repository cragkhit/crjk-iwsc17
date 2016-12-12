package org.jfree.chart.fx.interaction;
import java.io.Serializable;
import java.util.EventObject;
import javafx.scene.input.MouseEvent;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.fx.ChartViewer;
public class ChartMouseEventFX extends EventObject implements Serializable {
    private static final long serialVersionUID = -682393837314562149L;
    private JFreeChart chart;
    private MouseEvent trigger;
    private ChartEntity entity;
    public ChartMouseEventFX ( JFreeChart chart, MouseEvent trigger,
                               ChartEntity entity ) {
        super ( chart );
        this.chart = chart;
        this.trigger = trigger;
        this.entity = entity;
    }
    public JFreeChart getChart() {
        return this.chart;
    }
    public MouseEvent getTrigger() {
        return this.trigger;
    }
    public ChartEntity getEntity() {
        return this.entity;
    }
}
