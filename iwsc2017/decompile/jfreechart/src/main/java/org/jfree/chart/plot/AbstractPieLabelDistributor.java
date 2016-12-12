package org.jfree.chart.plot;
import org.jfree.chart.util.ParamChecks;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
public abstract class AbstractPieLabelDistributor implements Serializable {
    protected List labels;
    public AbstractPieLabelDistributor() {
        this.labels = new ArrayList();
    }
    public PieLabelRecord getPieLabelRecord ( final int index ) {
        return this.labels.get ( index );
    }
    public void addPieLabelRecord ( final PieLabelRecord record ) {
        ParamChecks.nullNotPermitted ( record, "record" );
        this.labels.add ( record );
    }
    public int getItemCount() {
        return this.labels.size();
    }
    public void clear() {
        this.labels.clear();
    }
    public abstract void distributeLabels ( final double p0, final double p1 );
}
