package org.jfree.chart.plot;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.awt.Shape;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.util.ParamChecks;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.ChartRenderingInfo;
import java.io.Serializable;
public class PlotRenderingInfo implements Cloneable, Serializable {
    private static final long serialVersionUID = 8446720134379617220L;
    private ChartRenderingInfo owner;
    private transient Rectangle2D plotArea;
    private transient Rectangle2D dataArea;
    private List subplotInfo;
    public PlotRenderingInfo ( final ChartRenderingInfo owner ) {
        this.owner = owner;
        this.dataArea = new Rectangle2D.Double();
        this.subplotInfo = new ArrayList();
    }
    public ChartRenderingInfo getOwner() {
        return this.owner;
    }
    public Rectangle2D getPlotArea() {
        return this.plotArea;
    }
    public void setPlotArea ( final Rectangle2D area ) {
        this.plotArea = area;
    }
    public Rectangle2D getDataArea() {
        return this.dataArea;
    }
    public void setDataArea ( final Rectangle2D area ) {
        this.dataArea = area;
    }
    public int getSubplotCount() {
        return this.subplotInfo.size();
    }
    public void addSubplotInfo ( final PlotRenderingInfo info ) {
        this.subplotInfo.add ( info );
    }
    public PlotRenderingInfo getSubplotInfo ( final int index ) {
        return this.subplotInfo.get ( index );
    }
    public int getSubplotIndex ( final Point2D source ) {
        ParamChecks.nullNotPermitted ( source, "source" );
        for ( int subplotCount = this.getSubplotCount(), i = 0; i < subplotCount; ++i ) {
            final PlotRenderingInfo info = this.getSubplotInfo ( i );
            final Rectangle2D area = info.getDataArea();
            if ( area.contains ( source ) ) {
                return i;
            }
        }
        return -1;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( this == obj ) {
            return true;
        }
        if ( ! ( obj instanceof PlotRenderingInfo ) ) {
            return false;
        }
        final PlotRenderingInfo that = ( PlotRenderingInfo ) obj;
        return ObjectUtilities.equal ( ( Object ) this.dataArea, ( Object ) that.dataArea ) && ObjectUtilities.equal ( ( Object ) this.plotArea, ( Object ) that.plotArea ) && ObjectUtilities.equal ( ( Object ) this.subplotInfo, ( Object ) that.subplotInfo );
    }
    public Object clone() throws CloneNotSupportedException {
        final PlotRenderingInfo clone = ( PlotRenderingInfo ) super.clone();
        if ( this.plotArea != null ) {
            clone.plotArea = ( Rectangle2D ) this.plotArea.clone();
        }
        if ( this.dataArea != null ) {
            clone.dataArea = ( Rectangle2D ) this.dataArea.clone();
        }
        clone.subplotInfo = new ArrayList ( this.subplotInfo.size() );
        for ( int i = 0; i < this.subplotInfo.size(); ++i ) {
            final PlotRenderingInfo info = this.subplotInfo.get ( i );
            clone.subplotInfo.add ( info.clone() );
        }
        return clone;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeShape ( ( Shape ) this.dataArea, stream );
        SerialUtilities.writeShape ( ( Shape ) this.plotArea, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.dataArea = ( Rectangle2D ) SerialUtilities.readShape ( stream );
        this.plotArea = ( Rectangle2D ) SerialUtilities.readShape ( stream );
    }
}
