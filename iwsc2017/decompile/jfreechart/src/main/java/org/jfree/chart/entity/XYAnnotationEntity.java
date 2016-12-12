package org.jfree.chart.entity;
import java.awt.Shape;
import java.io.Serializable;
public class XYAnnotationEntity extends ChartEntity implements Serializable {
    private static final long serialVersionUID = 2340334068383660799L;
    private int rendererIndex;
    public XYAnnotationEntity ( final Shape hotspot, final int rendererIndex, final String toolTipText, final String urlText ) {
        super ( hotspot, toolTipText, urlText );
        this.rendererIndex = rendererIndex;
    }
    public int getRendererIndex() {
        return this.rendererIndex;
    }
    public void setRendererIndex ( final int index ) {
        this.rendererIndex = index;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        if ( ! ( obj instanceof XYAnnotationEntity ) ) {
            return false;
        }
        final XYAnnotationEntity that = ( XYAnnotationEntity ) obj;
        return this.rendererIndex == that.rendererIndex;
    }
}
