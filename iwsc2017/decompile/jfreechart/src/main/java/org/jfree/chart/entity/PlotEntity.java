package org.jfree.chart.entity;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.chart.HashUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.util.ParamChecks;
import java.awt.Shape;
import org.jfree.chart.plot.Plot;
public class PlotEntity extends ChartEntity {
    private static final long serialVersionUID = -4445994133561919083L;
    private Plot plot;
    public PlotEntity ( final Shape area, final Plot plot ) {
        this ( area, plot, null );
    }
    public PlotEntity ( final Shape area, final Plot plot, final String toolTipText ) {
        this ( area, plot, toolTipText, null );
    }
    public PlotEntity ( final Shape area, final Plot plot, final String toolTipText, final String urlText ) {
        super ( area, toolTipText, urlText );
        ParamChecks.nullNotPermitted ( plot, "plot" );
        this.plot = plot;
    }
    public Plot getPlot() {
        return this.plot;
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "PlotEntity: " );
        sb.append ( "tooltip = " );
        sb.append ( this.getToolTipText() );
        return sb.toString();
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof PlotEntity ) ) {
            return false;
        }
        final PlotEntity that = ( PlotEntity ) obj;
        return this.getArea().equals ( that.getArea() ) && ObjectUtilities.equal ( ( Object ) this.getToolTipText(), ( Object ) that.getToolTipText() ) && ObjectUtilities.equal ( ( Object ) this.getURLText(), ( Object ) that.getURLText() ) && this.plot.equals ( that.plot );
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
