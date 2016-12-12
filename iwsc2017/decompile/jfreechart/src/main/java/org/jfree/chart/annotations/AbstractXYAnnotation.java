package org.jfree.chart.annotations;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.XYAnnotationEntity;
import java.awt.Shape;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.axis.ValueAxis;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.plot.XYPlot;
import java.awt.Graphics2D;
public abstract class AbstractXYAnnotation extends AbstractAnnotation implements XYAnnotation {
    private String toolTipText;
    private String url;
    protected AbstractXYAnnotation() {
        this.toolTipText = null;
        this.url = null;
    }
    public String getToolTipText() {
        return this.toolTipText;
    }
    public void setToolTipText ( final String text ) {
        this.toolTipText = text;
    }
    public String getURL() {
        return this.url;
    }
    public void setURL ( final String url ) {
        this.url = url;
    }
    @Override
    public abstract void draw ( final Graphics2D p0, final XYPlot p1, final Rectangle2D p2, final ValueAxis p3, final ValueAxis p4, final int p5, final PlotRenderingInfo p6 );
    protected void addEntity ( final PlotRenderingInfo info, final Shape hotspot, final int rendererIndex, final String toolTipText, final String urlText ) {
        if ( info == null ) {
            return;
        }
        final EntityCollection entities = info.getOwner().getEntityCollection();
        if ( entities == null ) {
            return;
        }
        final XYAnnotationEntity entity = new XYAnnotationEntity ( hotspot, rendererIndex, toolTipText, urlText );
        entities.add ( entity );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof AbstractXYAnnotation ) ) {
            return false;
        }
        final AbstractXYAnnotation that = ( AbstractXYAnnotation ) obj;
        return ObjectUtilities.equal ( ( Object ) this.toolTipText, ( Object ) that.toolTipText ) && ObjectUtilities.equal ( ( Object ) this.url, ( Object ) that.url );
    }
    @Override
    public int hashCode() {
        int result = 193;
        if ( this.toolTipText != null ) {
            result = 37 * result + this.toolTipText.hashCode();
        }
        if ( this.url != null ) {
            result = 37 * result + this.url.hashCode();
        }
        return result;
    }
}
