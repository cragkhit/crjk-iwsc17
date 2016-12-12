package org.jfree.chart.event;
public class RendererChangeEvent extends ChartChangeEvent {
    private Object renderer;
    private boolean seriesVisibilityChanged;
    public RendererChangeEvent ( final Object renderer ) {
        this ( renderer, false );
    }
    public RendererChangeEvent ( final Object renderer, final boolean seriesVisibilityChanged ) {
        super ( renderer );
        this.renderer = renderer;
        this.seriesVisibilityChanged = seriesVisibilityChanged;
    }
    public Object getRenderer() {
        return this.renderer;
    }
    public boolean getSeriesVisibilityChanged() {
        return this.seriesVisibilityChanged;
    }
}
