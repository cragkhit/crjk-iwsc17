package org.jfree.chart.axis;
import org.jfree.ui.TextAnchor;
import java.text.AttributedString;
public class LogTick extends ValueTick {
    AttributedString attributedLabel;
    public LogTick ( final TickType type, final double value, final AttributedString label, final TextAnchor textAnchor ) {
        super ( type, value, null, textAnchor, textAnchor, 0.0 );
        this.attributedLabel = label;
    }
    public AttributedString getAttributedLabel() {
        return this.attributedLabel;
    }
}
