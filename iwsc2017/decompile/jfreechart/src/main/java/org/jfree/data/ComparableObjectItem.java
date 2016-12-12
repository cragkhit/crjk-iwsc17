package org.jfree.data;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.util.ParamChecks;
import java.io.Serializable;
public class ComparableObjectItem implements Cloneable, Comparable, Serializable {
    private static final long serialVersionUID = 2751513470325494890L;
    private Comparable x;
    private Object obj;
    public ComparableObjectItem ( final Comparable x, final Object y ) {
        ParamChecks.nullNotPermitted ( x, "x" );
        this.x = x;
        this.obj = y;
    }
    protected Comparable getComparable() {
        return this.x;
    }
    protected Object getObject() {
        return this.obj;
    }
    protected void setObject ( final Object y ) {
        this.obj = y;
    }
    @Override
    public int compareTo ( final Object o1 ) {
        if ( o1 instanceof ComparableObjectItem ) {
            final ComparableObjectItem that = ( ComparableObjectItem ) o1;
            return this.x.compareTo ( that.x );
        }
        final int result = 1;
        return result;
    }
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof ComparableObjectItem ) ) {
            return false;
        }
        final ComparableObjectItem that = ( ComparableObjectItem ) obj;
        return this.x.equals ( that.x ) && ObjectUtilities.equal ( this.obj, that.obj );
    }
    @Override
    public int hashCode() {
        int result = this.x.hashCode();
        result = 29 * result + ( ( this.obj != null ) ? this.obj.hashCode() : 0 );
        return result;
    }
}
