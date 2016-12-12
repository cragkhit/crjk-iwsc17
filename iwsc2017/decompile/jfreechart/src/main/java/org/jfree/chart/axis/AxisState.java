package org.jfree.chart.axis;
import org.jfree.ui.RectangleEdge;
import java.util.ArrayList;
import java.util.List;
public class AxisState {
    private double cursor;
    private List ticks;
    private double max;
    public AxisState() {
        this ( 0.0 );
    }
    public AxisState ( final double cursor ) {
        this.cursor = cursor;
        this.ticks = new ArrayList();
    }
    public double getCursor() {
        return this.cursor;
    }
    public void setCursor ( final double cursor ) {
        this.cursor = cursor;
    }
    public void moveCursor ( final double units, final RectangleEdge edge ) {
        if ( edge == RectangleEdge.TOP ) {
            this.cursorUp ( units );
        } else if ( edge == RectangleEdge.BOTTOM ) {
            this.cursorDown ( units );
        } else if ( edge == RectangleEdge.LEFT ) {
            this.cursorLeft ( units );
        } else if ( edge == RectangleEdge.RIGHT ) {
            this.cursorRight ( units );
        }
    }
    public void cursorUp ( final double units ) {
        this.cursor -= units;
    }
    public void cursorDown ( final double units ) {
        this.cursor += units;
    }
    public void cursorLeft ( final double units ) {
        this.cursor -= units;
    }
    public void cursorRight ( final double units ) {
        this.cursor += units;
    }
    public List getTicks() {
        return this.ticks;
    }
    public void setTicks ( final List ticks ) {
        this.ticks = ticks;
    }
    public double getMax() {
        return this.max;
    }
    public void setMax ( final double max ) {
        this.max = max;
    }
}
