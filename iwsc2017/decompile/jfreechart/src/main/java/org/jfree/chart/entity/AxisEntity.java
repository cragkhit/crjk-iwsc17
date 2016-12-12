package org.jfree.chart.entity;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.chart.HashUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.util.ParamChecks;
import java.awt.Shape;
import org.jfree.chart.axis.Axis;
public class AxisEntity extends ChartEntity {
    private static final long serialVersionUID = -4445994133561919083L;
    private Axis axis;
    public AxisEntity ( final Shape area, final Axis axis ) {
        this ( area, axis, null );
    }
    public AxisEntity ( final Shape area, final Axis axis, final String toolTipText ) {
        this ( area, axis, toolTipText, null );
    }
    public AxisEntity ( final Shape area, final Axis axis, final String toolTipText, final String urlText ) {
        super ( area, toolTipText, urlText );
        ParamChecks.nullNotPermitted ( axis, "axis" );
        this.axis = axis;
    }
    public Axis getAxis() {
        return this.axis;
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "AxisEntity: " );
        sb.append ( "tooltip = " );
        sb.append ( this.getToolTipText() );
        return sb.toString();
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof AxisEntity ) ) {
            return false;
        }
        final AxisEntity that = ( AxisEntity ) obj;
        return this.getArea().equals ( that.getArea() ) && ObjectUtilities.equal ( ( Object ) this.getToolTipText(), ( Object ) that.getToolTipText() ) && ObjectUtilities.equal ( ( Object ) this.getURLText(), ( Object ) that.getURLText() ) && this.axis.equals ( that.axis );
    }
    @Override
    public int hashCode() {
        int result = 39;
        result = HashUtilities.hashCode ( result, this.getToolTipText() );
        result = HashUtilities.hashCode ( result, this.getURLText() );
        return result;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeShape ( this.getArea(), stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.setArea ( SerialUtilities.readShape ( stream ) );
    }
}
