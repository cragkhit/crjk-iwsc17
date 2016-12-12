

package org.jfree.data.general;

import java.io.Serializable;
import org.jfree.chart.util.ParamChecks;


public class DatasetGroup implements Cloneable, Serializable {


    private static final long serialVersionUID = -3640642179674185688L;


    private String id;


    public DatasetGroup() {
        super();
        this.id = "NOID";
    }


    public DatasetGroup ( String id ) {
        ParamChecks.nullNotPermitted ( id, "id" );
        this.id = id;
    }


    public String getID() {
        return this.id;
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }


    @Override
    public boolean equals ( Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DatasetGroup ) ) {
            return false;
        }
        DatasetGroup that = ( DatasetGroup ) obj;
        if ( !this.id.equals ( that.id ) ) {
            return false;
        }
        return true;
    }

}
