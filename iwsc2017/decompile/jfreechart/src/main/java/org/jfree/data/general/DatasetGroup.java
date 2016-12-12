package org.jfree.data.general;
import org.jfree.chart.util.ParamChecks;
import java.io.Serializable;
public class DatasetGroup implements Cloneable, Serializable {
    private static final long serialVersionUID = -3640642179674185688L;
    private String id;
    public DatasetGroup() {
        this.id = "NOID";
    }
    public DatasetGroup ( final String id ) {
        ParamChecks.nullNotPermitted ( id, "id" );
        this.id = id;
    }
    public String getID() {
        return this.id;
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DatasetGroup ) ) {
            return false;
        }
        final DatasetGroup that = ( DatasetGroup ) obj;
        return this.id.equals ( that.id );
    }
}
