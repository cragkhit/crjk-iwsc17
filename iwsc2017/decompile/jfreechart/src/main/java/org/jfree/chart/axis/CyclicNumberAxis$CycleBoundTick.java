package org.jfree.chart.axis;
import org.jfree.ui.TextAnchor;
protected static class CycleBoundTick extends NumberTick {
    public boolean mapToLastCycle;
    public CycleBoundTick ( final boolean mapToLastCycle, final Number number, final String label, final TextAnchor textAnchor, final TextAnchor rotationAnchor, final double angle ) {
        super ( number, label, textAnchor, rotationAnchor, angle );
        this.mapToLastCycle = mapToLastCycle;
    }
}
