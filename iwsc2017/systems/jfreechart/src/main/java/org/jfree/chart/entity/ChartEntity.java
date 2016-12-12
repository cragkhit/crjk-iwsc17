

package org.jfree.chart.entity;

import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.chart.HashUtilities;
import org.jfree.chart.imagemap.ToolTipTagFragmentGenerator;
import org.jfree.chart.imagemap.URLTagFragmentGenerator;
import org.jfree.chart.util.ParamChecks;
import org.jfree.io.SerialUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PublicCloneable;


public class ChartEntity implements Cloneable, PublicCloneable, Serializable {


    private static final long serialVersionUID = -4445994133561919083L;


    private transient Shape area;


    private String toolTipText;


    private String urlText;


    public ChartEntity ( Shape area ) {
        this ( area, null );
    }


    public ChartEntity ( Shape area, String toolTipText ) {
        this ( area, toolTipText, null );
    }


    public ChartEntity ( Shape area, String toolTipText, String urlText ) {
        ParamChecks.nullNotPermitted ( area, "area" );
        this.area = area;
        this.toolTipText = toolTipText;
        this.urlText = urlText;
    }


    public Shape getArea() {
        return this.area;
    }


    public void setArea ( Shape area ) {
        ParamChecks.nullNotPermitted ( area, "area" );
        this.area = area;
    }


    public String getToolTipText() {
        return this.toolTipText;
    }


    public void setToolTipText ( String text ) {
        this.toolTipText = text;
    }


    public String getURLText() {
        return this.urlText;
    }


    public void setURLText ( String text ) {
        this.urlText = text;
    }


    public String getShapeType() {
        if ( this.area instanceof Rectangle2D ) {
            return "rect";
        } else {
            return "poly";
        }
    }


    public String getShapeCoords() {
        if ( this.area instanceof Rectangle2D ) {
            return getRectCoords ( ( Rectangle2D ) this.area );
        } else {
            return getPolyCoords ( this.area );
        }
    }


    private String getRectCoords ( Rectangle2D rectangle ) {
        ParamChecks.nullNotPermitted ( rectangle, "rectangle" );
        int x1 = ( int ) rectangle.getX();
        int y1 = ( int ) rectangle.getY();
        int x2 = x1 + ( int ) rectangle.getWidth();
        int y2 = y1 + ( int ) rectangle.getHeight();
        if ( x2 == x1 ) {
            x2++;
        }
        if ( y2 == y1 ) {
            y2++;
        }
        return x1 + "," + y1 + "," + x2 + "," + y2;
    }


    private String getPolyCoords ( Shape shape ) {
        ParamChecks.nullNotPermitted ( shape, "shape" );
        StringBuilder result = new StringBuilder();
        boolean first = true;
        float[] coords = new float[6];
        PathIterator pi = shape.getPathIterator ( null, 1.0 );
        while ( !pi.isDone() ) {
            pi.currentSegment ( coords );
            if ( first ) {
                first = false;
                result.append ( ( int ) coords[0] );
                result.append ( "," ).append ( ( int ) coords[1] );
            } else {
                result.append ( "," );
                result.append ( ( int ) coords[0] );
                result.append ( "," );
                result.append ( ( int ) coords[1] );
            }
            pi.next();
        }
        return result.toString();
    }


    public String getImageMapAreaTag (
        ToolTipTagFragmentGenerator toolTipTagFragmentGenerator,
        URLTagFragmentGenerator urlTagFragmentGenerator ) {

        StringBuilder tag = new StringBuilder();
        boolean hasURL = ( this.urlText == null ? false
                           : !this.urlText.equals ( "" ) );
        boolean hasToolTip = ( this.toolTipText == null ? false
                               : !this.toolTipText.equals ( "" ) );
        if ( hasURL || hasToolTip ) {
            tag.append ( "<area shape=\"" ).append ( getShapeType() ).append ( "\"" )
            .append ( " coords=\"" ).append ( getShapeCoords() ).append ( "\"" );
            if ( hasToolTip ) {
                tag.append ( toolTipTagFragmentGenerator.generateToolTipFragment (
                                 this.toolTipText ) );
            }
            if ( hasURL ) {
                tag.append ( urlTagFragmentGenerator.generateURLFragment (
                                 this.urlText ) );
            } else {
                tag.append ( " nohref=\"nohref\"" );
            }
            if ( !hasToolTip ) {
                tag.append ( " alt=\"\"" );
            }
            tag.append ( "/>" );
        }
        return tag.toString();
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder ( "ChartEntity: " );
        sb.append ( "tooltip = " );
        sb.append ( this.toolTipText );
        return sb.toString();
    }


    @Override
    public boolean equals ( Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof ChartEntity ) ) {
            return false;
        }
        ChartEntity that = ( ChartEntity ) obj;
        if ( !this.area.equals ( that.area ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.toolTipText, that.toolTipText ) ) {
            return false;
        }
        if ( !ObjectUtilities.equal ( this.urlText, that.urlText ) ) {
            return false;
        }
        return true;
    }


    @Override
    public int hashCode() {
        int result = 37;
        result = HashUtilities.hashCode ( result, this.toolTipText );
        result = HashUtilities.hashCode ( result, this.urlText );
        return result;
    }


    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }


    private void writeObject ( ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeShape ( this.area, stream );
    }


    private void readObject ( ObjectInputStream stream )
    throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.area = SerialUtilities.readShape ( stream );
    }

}
