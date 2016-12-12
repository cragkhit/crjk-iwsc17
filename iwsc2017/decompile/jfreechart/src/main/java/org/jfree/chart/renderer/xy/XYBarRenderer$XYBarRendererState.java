package org.jfree.chart.renderer.xy;
import org.jfree.chart.plot.PlotRenderingInfo;
protected class XYBarRendererState extends XYItemRendererState {
    private double g2Base;
    public XYBarRendererState ( final PlotRenderingInfo info ) {
        super ( info );
    }
    public double getG2Base() {
        return this.g2Base;
    }
    public void setG2Base ( final double value ) {
        this.g2Base = value;
    }
}
