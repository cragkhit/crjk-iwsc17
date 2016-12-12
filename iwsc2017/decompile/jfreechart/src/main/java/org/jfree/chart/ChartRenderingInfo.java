package org.jfree.chart;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.awt.Shape;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PublicCloneable;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
public class ChartRenderingInfo implements Cloneable, Serializable {
    private static final long serialVersionUID = 2751952018173406822L;
    private transient Rectangle2D chartArea;
    private PlotRenderingInfo plotInfo;
    private EntityCollection entities;
    public ChartRenderingInfo() {
        this ( new StandardEntityCollection() );
    }
    public ChartRenderingInfo ( final EntityCollection entities ) {
        this.chartArea = new Rectangle2D.Double();
        this.plotInfo = new PlotRenderingInfo ( this );
        this.entities = entities;
    }
    public Rectangle2D getChartArea() {
        return this.chartArea;
    }
    public void setChartArea ( final Rectangle2D area ) {
        this.chartArea.setRect ( area );
    }
    public EntityCollection getEntityCollection() {
        return this.entities;
    }
    public void setEntityCollection ( final EntityCollection entities ) {
        this.entities = entities;
    }
    public void clear() {
        this.chartArea.setRect ( 0.0, 0.0, 0.0, 0.0 );
        this.plotInfo = new PlotRenderingInfo ( this );
        if ( this.entities != null ) {
            this.entities.clear();
        }
    }
    public PlotRenderingInfo getPlotInfo() {
        return this.plotInfo;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof ChartRenderingInfo ) ) {
            return false;
        }
        final ChartRenderingInfo that = ( ChartRenderingInfo ) obj;
        return ObjectUtilities.equal ( ( Object ) this.chartArea, ( Object ) that.chartArea ) && ObjectUtilities.equal ( ( Object ) this.plotInfo, ( Object ) that.plotInfo ) && ObjectUtilities.equal ( ( Object ) this.entities, ( Object ) that.entities );
    }
    public Object clone() throws CloneNotSupportedException {
        final ChartRenderingInfo clone = ( ChartRenderingInfo ) super.clone();
        if ( this.chartArea != null ) {
            clone.chartArea = ( Rectangle2D ) this.chartArea.clone();
        }
        if ( this.entities instanceof PublicCloneable ) {
            final PublicCloneable pc = ( PublicCloneable ) this.entities;
            clone.entities = ( EntityCollection ) pc.clone();
        }
        return clone;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeShape ( ( Shape ) this.chartArea, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.chartArea = ( Rectangle2D ) SerialUtilities.readShape ( stream );
    }
}
