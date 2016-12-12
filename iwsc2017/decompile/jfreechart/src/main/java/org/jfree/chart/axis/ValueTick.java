package org.jfree.chart.axis;
import org.jfree.ui.TextAnchor;
public abstract class ValueTick extends Tick {
    private double value;
    private TickType tickType;
    public ValueTick ( final double value, final String label, final TextAnchor textAnchor, final TextAnchor rotationAnchor, final double angle ) {
        this ( TickType.MAJOR, value, label, textAnchor, rotationAnchor, angle );
        this.value = value;
    }
    public ValueTick ( final TickType tickType, final double value, final String label, final TextAnchor textAnchor, final TextAnchor rotationAnchor, final double angle ) {
        super ( label, textAnchor, rotationAnchor, angle );
        this.value = value;
        this.tickType = tickType;
    }
    public double getValue() {
        return this.value;
    }
    public TickType getTickType() {
        return this.tickType;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof ValueTick ) ) {
            return false;
        }
        final ValueTick that = ( ValueTick ) obj;
        return this.value == that.value && this.tickType.equals ( that.tickType ) && super.equals ( obj );
    }
}
