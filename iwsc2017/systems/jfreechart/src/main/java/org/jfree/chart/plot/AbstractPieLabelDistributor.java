

package org.jfree.chart.plot;

import java.io.Serializable;
import java.util.List;
import org.jfree.chart.util.ParamChecks;


public abstract class AbstractPieLabelDistributor implements Serializable {


    protected List labels;


    public AbstractPieLabelDistributor() {
        this.labels = new java.util.ArrayList();
    }


    public PieLabelRecord getPieLabelRecord ( int index ) {
        return ( PieLabelRecord ) this.labels.get ( index );
    }


    public void addPieLabelRecord ( PieLabelRecord record ) {
        ParamChecks.nullNotPermitted ( record, "record" );
        this.labels.add ( record );
    }


    public int getItemCount() {
        return this.labels.size();
    }


    public void clear() {
        this.labels.clear();
    }


    public abstract void distributeLabels ( double minY, double height );

}
