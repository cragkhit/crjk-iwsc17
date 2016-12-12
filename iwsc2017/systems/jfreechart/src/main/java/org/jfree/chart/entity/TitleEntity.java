package org.jfree.chart.entity;
import java.awt.Shape;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.jfree.chart.HashUtilities;
import org.jfree.chart.title.Title;
import org.jfree.chart.util.ParamChecks;
import org.jfree.io.SerialUtilities;
import org.jfree.util.ObjectUtilities;
public class TitleEntity extends ChartEntity {
    private static final long serialVersionUID = -4445994133561919083L;
    private Title title;
    public TitleEntity ( Shape area, Title title ) {
        this ( area, title, null );
    }
    public TitleEntity ( Shape area, Title title, String toolTipText ) {
        this ( area, title, toolTipText, null );
    }
    public TitleEntity ( Shape area, Title title, String toolTipText,
                         String urlText ) {
        super ( area, toolTipText, urlText );
        ParamChecks.nullNotPermitted ( title, "title" );
        this.title = title;
    }
    public Title getTitle() {
        return this.title;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "TitleEntity: " );
        sb.append ( "tooltip = " );
        sb.append ( getToolTipText() );
        return sb.toString();
    }
    @Override
    public boolean equals ( Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof TitleEntity ) ) {
            return false;
        }
        TitleEntity that = ( TitleEntity ) obj;
        if ( !getArea().equals ( that.getArea() ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( getToolTipText(), that.getToolTipText() ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( getURLText(), that.getURLText() ) ) {
            return false;
        }
        if ( ! ( this.title.equals ( that.title ) ) ) {
            return false;
        }
        return true;
    }
    @Override
    public int hashCode() {
        int result = 41;
        result = HashUtilities.hashCode ( result, getToolTipText() );
        result = HashUtilities.hashCode ( result, getURLText() );
        return result;
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    private void writeObject ( ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeShape ( getArea(), stream );
    }
    private void readObject ( ObjectInputStream stream )
    throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        setArea ( SerialUtilities.readShape ( stream ) );
    }
}
