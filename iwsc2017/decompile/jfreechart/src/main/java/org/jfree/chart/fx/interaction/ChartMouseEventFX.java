package org.jfree.chart.fx.interaction;
import org.jfree.chart.entity.ChartEntity;
import javafx.scene.input.MouseEvent;
import org.jfree.chart.JFreeChart;
import java.io.Serializable;
import java.util.EventObject;
public class ChartMouseEventFX extends EventObject implements Serializable {
    private static final long serialVersionUID = -682393837314562149L;
    private JFreeChart chart;
    private MouseEvent trigger;
    private ChartEntity entity;
    public ChartMouseEventFX ( final JFreeChart chart, final MouseEvent trigger, final ChartEntity entity ) {
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
