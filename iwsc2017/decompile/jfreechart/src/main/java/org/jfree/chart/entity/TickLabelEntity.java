package org.jfree.chart.entity;
import java.awt.Shape;
import java.io.Serializable;
public class TickLabelEntity extends ChartEntity implements Cloneable, Serializable {
    private static final long serialVersionUID = 681583956588092095L;
    public TickLabelEntity ( final Shape area, final String toolTipText, final String urlText ) {
        super ( area, toolTipText, urlText );
    }
}
