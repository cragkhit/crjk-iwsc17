package org.jfree.chart.axis;
import org.jfree.ui.TextAnchor;
public class NumberTick extends ValueTick {
    private Number number;
    public NumberTick ( final Number number, final String label, final TextAnchor textAnchor, final TextAnchor rotationAnchor, final double angle ) {
        super ( number.doubleValue(), label, textAnchor, rotationAnchor, angle );
        this.number = number;
    }
    public NumberTick ( final TickType tickType, final double value, final String label, final TextAnchor textAnchor, final TextAnchor rotationAnchor, final double angle ) {
        super ( tickType, value, label, textAnchor, rotationAnchor, angle );
        this.number = new Double ( value );
    }
    public Number getNumber() {
        return this.number;
    }
}
