package org.jfree.chart.plot.dial;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.jfree.util.ObjectUtilities;
import java.awt.Shape;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import java.util.Iterator;
import org.jfree.chart.util.ParamChecks;
import java.util.ArrayList;
import org.jfree.data.general.ValueDataset;
import java.util.List;
import org.jfree.util.ObjectList;
import org.jfree.chart.plot.Plot;
public class DialPlot extends Plot implements DialLayerChangeListener {
    private DialLayer background;
    private DialLayer cap;
    private DialFrame dialFrame;
    private ObjectList datasets;
    private ObjectList scales;
    private ObjectList datasetToScaleMap;
    private List layers;
    private List pointers;
    private double viewX;
    private double viewY;
    private double viewW;
    private double viewH;
    public DialPlot() {
        this ( null );
    }
    public DialPlot ( final ValueDataset dataset ) {
        this.background = null;
        this.cap = null;
        this.dialFrame = new ArcDialFrame();
        this.datasets = new ObjectList();
        if ( dataset != null ) {
            this.setDataset ( dataset );
        }
        this.scales = new ObjectList();
        this.datasetToScaleMap = new ObjectList();
        this.layers = new ArrayList();
        this.pointers = new ArrayList();
        this.viewX = 0.0;
        this.viewY = 0.0;
        this.viewW = 1.0;
        this.viewH = 1.0;
    }
    public DialLayer getBackground() {
        return this.background;
    }
    public void setBackground ( final DialLayer background ) {
        if ( this.background != null ) {
            this.background.removeChangeListener ( this );
        }
        if ( ( this.background = background ) != null ) {
            background.addChangeListener ( this );
        }
        this.fireChangeEvent();
    }
    public DialLayer getCap() {
        return this.cap;
    }
    public void setCap ( final DialLayer cap ) {
        if ( this.cap != null ) {
            this.cap.removeChangeListener ( this );
        }
        if ( ( this.cap = cap ) != null ) {
            cap.addChangeListener ( this );
        }
        this.fireChangeEvent();
    }
    public DialFrame getDialFrame() {
        return this.dialFrame;
    }
    public void setDialFrame ( final DialFrame frame ) {
        ParamChecks.nullNotPermitted ( frame, "frame" );
        this.dialFrame.removeChangeListener ( this );
        ( this.dialFrame = frame ).addChangeListener ( this );
        this.fireChangeEvent();
    }
    public double getViewX() {
        return this.viewX;
    }
    public double getViewY() {
        return this.viewY;
    }
    public double getViewWidth() {
        return this.viewW;
    }
    public double getViewHeight() {
        return this.viewH;
    }
    public void setView ( final double x, final double y, final double w, final double h ) {
        this.viewX = x;
        this.viewY = y;
        this.viewW = w;
        this.viewH = h;
        this.fireChangeEvent();
    }
    public void addLayer ( final DialLayer layer ) {
        ParamChecks.nullNotPermitted ( layer, "layer" );
        this.layers.add ( layer );
        layer.addChangeListener ( this );
        this.fireChangeEvent();
    }
    public int getLayerIndex ( final DialLayer layer ) {
        ParamChecks.nullNotPermitted ( layer, "layer" );
        return this.layers.indexOf ( layer );
    }
    public void removeLayer ( final int index ) {
        final DialLayer layer = this.layers.get ( index );
        if ( layer != null ) {
            layer.removeChangeListener ( this );
        }
        this.layers.remove ( index );
        this.fireChangeEvent();
    }
    public void removeLayer ( final DialLayer layer ) {
        this.removeLayer ( this.getLayerIndex ( layer ) );
    }
    public void addPointer ( final DialPointer pointer ) {
        ParamChecks.nullNotPermitted ( pointer, "pointer" );
        this.pointers.add ( pointer );
        pointer.addChangeListener ( this );
        this.fireChangeEvent();
    }
    public int getPointerIndex ( final DialPointer pointer ) {
        ParamChecks.nullNotPermitted ( pointer, "pointer" );
        return this.pointers.indexOf ( pointer );
    }
    public void removePointer ( final int index ) {
        final DialPointer pointer = this.pointers.get ( index );
        if ( pointer != null ) {
            pointer.removeChangeListener ( this );
        }
        this.pointers.remove ( index );
        this.fireChangeEvent();
    }
    public void removePointer ( final DialPointer pointer ) {
        this.removeLayer ( this.getPointerIndex ( pointer ) );
    }
    public DialPointer getPointerForDataset ( final int datasetIndex ) {
        final DialPointer result = null;
        for ( final DialPointer p : this.pointers ) {
            if ( p.getDatasetIndex() == datasetIndex ) {
                return p;
            }
        }
        return result;
    }
    public ValueDataset getDataset() {
        return this.getDataset ( 0 );
    }
    public ValueDataset getDataset ( final int index ) {
        ValueDataset result = null;
        if ( this.datasets.size() > index ) {
            result = ( ValueDataset ) this.datasets.get ( index );
        }
        return result;
    }
    public void setDataset ( final ValueDataset dataset ) {
        this.setDataset ( 0, dataset );
    }
    public void setDataset ( final int index, final ValueDataset dataset ) {
        final ValueDataset existing = ( ValueDataset ) this.datasets.get ( index );
        if ( existing != null ) {
            existing.removeChangeListener ( this );
        }
        this.datasets.set ( index, ( Object ) dataset );
        if ( dataset != null ) {
            dataset.addChangeListener ( this );
        }
        final DatasetChangeEvent event = new DatasetChangeEvent ( this, dataset );
        this.datasetChanged ( event );
    }
    public int getDatasetCount() {
        return this.datasets.size();
    }
    @Override
    public void draw ( final Graphics2D g2, final Rectangle2D area, final Point2D anchor, final PlotState parentState, final PlotRenderingInfo info ) {
        final Shape origClip = g2.getClip();
        g2.setClip ( area );
        final Rectangle2D frame = this.viewToFrame ( area );
        if ( this.background != null && this.background.isVisible() ) {
            if ( this.background.isClippedToWindow() ) {
                final Shape savedClip = g2.getClip();
                g2.clip ( this.dialFrame.getWindow ( frame ) );
                this.background.draw ( g2, this, frame, area );
                g2.setClip ( savedClip );
            } else {
                this.background.draw ( g2, this, frame, area );
            }
        }
        for ( final DialLayer current : this.layers ) {
            if ( current.isVisible() ) {
                if ( current.isClippedToWindow() ) {
                    final Shape savedClip2 = g2.getClip();
                    g2.clip ( this.dialFrame.getWindow ( frame ) );
                    current.draw ( g2, this, frame, area );
                    g2.setClip ( savedClip2 );
                } else {
                    current.draw ( g2, this, frame, area );
                }
            }
        }
        for ( final DialPointer current2 : this.pointers ) {
            if ( current2.isVisible() ) {
                if ( current2.isClippedToWindow() ) {
                    final Shape savedClip2 = g2.getClip();
                    g2.clip ( this.dialFrame.getWindow ( frame ) );
                    current2.draw ( g2, this, frame, area );
                    g2.setClip ( savedClip2 );
                } else {
                    current2.draw ( g2, this, frame, area );
                }
            }
        }
        if ( this.cap != null && this.cap.isVisible() ) {
            if ( this.cap.isClippedToWindow() ) {
                final Shape savedClip3 = g2.getClip();
                g2.clip ( this.dialFrame.getWindow ( frame ) );
                this.cap.draw ( g2, this, frame, area );
                g2.setClip ( savedClip3 );
            } else {
                this.cap.draw ( g2, this, frame, area );
            }
        }
        if ( this.dialFrame.isVisible() ) {
            this.dialFrame.draw ( g2, this, frame, area );
        }
        g2.setClip ( origClip );
    }
    private Rectangle2D viewToFrame ( final Rectangle2D view ) {
        final double width = view.getWidth() / this.viewW;
        final double height = view.getHeight() / this.viewH;
        final double x = view.getX() - width * this.viewX;
        final double y = view.getY() - height * this.viewY;
        return new Rectangle2D.Double ( x, y, width, height );
    }
    public double getValue ( final int datasetIndex ) {
        double result = Double.NaN;
        final ValueDataset dataset = this.getDataset ( datasetIndex );
        if ( dataset != null ) {
            final Number n = dataset.getValue();
            if ( n != null ) {
                result = n.doubleValue();
            }
        }
        return result;
    }
    public void addScale ( final int index, final DialScale scale ) {
        ParamChecks.nullNotPermitted ( scale, "scale" );
        final DialScale existing = ( DialScale ) this.scales.get ( index );
        if ( existing != null ) {
            this.removeLayer ( existing );
        }
        this.layers.add ( scale );
        this.scales.set ( index, ( Object ) scale );
        scale.addChangeListener ( this );
        this.fireChangeEvent();
    }
    public DialScale getScale ( final int index ) {
        DialScale result = null;
        if ( this.scales.size() > index ) {
            result = ( DialScale ) this.scales.get ( index );
        }
        return result;
    }
    public void mapDatasetToScale ( final int index, final int scaleIndex ) {
        this.datasetToScaleMap.set ( index, ( Object ) new Integer ( scaleIndex ) );
        this.fireChangeEvent();
    }
    public DialScale getScaleForDataset ( final int datasetIndex ) {
        DialScale result = ( DialScale ) this.scales.get ( 0 );
        final Integer scaleIndex = ( Integer ) this.datasetToScaleMap.get ( datasetIndex );
        if ( scaleIndex != null ) {
            result = this.getScale ( scaleIndex );
        }
        return result;
    }
    public static Rectangle2D rectangleByRadius ( final Rectangle2D rect, final double radiusW, final double radiusH ) {
        ParamChecks.nullNotPermitted ( rect, "rect" );
        final double x = rect.getCenterX();
        final double y = rect.getCenterY();
        final double w = rect.getWidth() * radiusW;
        final double h = rect.getHeight() * radiusH;
        return new Rectangle2D.Double ( x - w / 2.0, y - h / 2.0, w, h );
    }
    @Override
    public void dialLayerChanged ( final DialLayerChangeEvent event ) {
        this.fireChangeEvent();
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof DialPlot ) ) {
            return false;
        }
        final DialPlot that = ( DialPlot ) obj;
        return ObjectUtilities.equal ( ( Object ) this.background, ( Object ) that.background ) && ObjectUtilities.equal ( ( Object ) this.cap, ( Object ) that.cap ) && this.dialFrame.equals ( that.dialFrame ) && this.viewX == that.viewX && this.viewY == that.viewY && this.viewW == that.viewW && this.viewH == that.viewH && this.layers.equals ( that.layers ) && this.pointers.equals ( that.pointers ) && super.equals ( obj );
    }
    @Override
    public int hashCode() {
        int result = 193;
        result = 37 * result + ObjectUtilities.hashCode ( ( Object ) this.background );
        result = 37 * result + ObjectUtilities.hashCode ( ( Object ) this.cap );
        result = 37 * result + this.dialFrame.hashCode();
        long temp = Double.doubleToLongBits ( this.viewX );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.viewY );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.viewW );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        temp = Double.doubleToLongBits ( this.viewH );
        result = 37 * result + ( int ) ( temp ^ temp >>> 32 );
        return result;
    }
    @Override
    public String getPlotType() {
        return "DialPlot";
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
    }
}
