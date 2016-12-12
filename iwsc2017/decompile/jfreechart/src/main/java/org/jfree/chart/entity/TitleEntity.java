package org.jfree.chart.entity;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.chart.HashUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.util.ParamChecks;
import java.awt.Shape;
import org.jfree.chart.title.Title;
public class TitleEntity extends ChartEntity {
    private static final long serialVersionUID = -4445994133561919083L;
    private Title title;
    public TitleEntity ( final Shape area, final Title title ) {
        this ( area, title, null );
    }
    public TitleEntity ( final Shape area, final Title title, final String toolTipText ) {
        this ( area, title, toolTipText, null );
    }
    public TitleEntity ( final Shape area, final Title title, final String toolTipText, final String urlText ) {
        super ( area, toolTipText, urlText );
        ParamChecks.nullNotPermitted ( title, "title" );
        this.title = title;
    }
    public Title getTitle() {
        return this.title;
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder ( "TitleEntity: " );
        sb.append ( "tooltip = " );
        sb.append ( this.getToolTipText() );
        return sb.toString();
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof TitleEntity ) ) {
            return false;
        }
        final TitleEntity that = ( TitleEntity ) obj;
        return this.getArea().equals ( that.getArea() ) && ObjectUtilities.equal ( ( Object ) this.getToolTipText(), ( Object ) that.getToolTipText() ) && ObjectUtilities.equal ( ( Object ) this.getURLText(), ( Object ) that.getURLText() ) && this.title.equals ( that.title );
    }
    @Override
    public int hashCode() {
        int result = 41;
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
