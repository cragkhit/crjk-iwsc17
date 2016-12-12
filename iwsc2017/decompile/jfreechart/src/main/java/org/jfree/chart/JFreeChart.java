package org.jfree.chart;
import javax.swing.UIManager;
import java.io.ObjectInputStream;
import java.io.IOException;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import org.jfree.util.PaintUtilities;
import org.jfree.util.ObjectUtilities;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.event.ChartChangeListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import org.jfree.chart.block.EntityBlockResult;
import org.jfree.chart.block.BlockParams;
import org.jfree.chart.block.RectangleConstraint;
import org.jfree.chart.block.LengthConstraintType;
import org.jfree.data.Range;
import org.jfree.ui.VerticalAlignment;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.Size2D;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.PlotState;
import org.jfree.ui.Align;
import java.awt.image.ImageObserver;
import java.awt.Composite;
import java.awt.AlphaComposite;
import java.util.Map;
import org.jfree.chart.entity.ChartEntity;
import java.awt.Shape;
import org.jfree.chart.entity.JFreeChartEntity;
import java.util.HashMap;
import org.jfree.chart.event.ChartProgressEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.CategoryPlot;
import java.util.Collection;
import java.util.Iterator;
import org.jfree.chart.title.Title;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.block.BlockFrame;
import org.jfree.chart.block.LineBorder;
import org.jfree.chart.title.LegendTitle;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.BasicStroke;
import org.jfree.chart.util.ParamChecks;
import javax.swing.event.EventListenerList;
import org.jfree.chart.plot.Plot;
import java.util.List;
import org.jfree.chart.title.TextTitle;
import org.jfree.ui.RectangleInsets;
import java.awt.Stroke;
import java.awt.RenderingHints;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Font;
import org.jfree.ui.about.ProjectInfo;
import java.io.Serializable;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.event.TitleChangeListener;
import org.jfree.ui.Drawable;
public class JFreeChart implements Drawable, TitleChangeListener, PlotChangeListener, Serializable, Cloneable {
    private static final long serialVersionUID = -3470703747817429120L;
    public static final ProjectInfo INFO;
    public static final Font DEFAULT_TITLE_FONT;
    public static final Paint DEFAULT_BACKGROUND_PAINT;
    public static final Image DEFAULT_BACKGROUND_IMAGE;
    public static final int DEFAULT_BACKGROUND_IMAGE_ALIGNMENT = 15;
    public static final float DEFAULT_BACKGROUND_IMAGE_ALPHA = 0.5f;
    public static final RenderingHints.Key KEY_SUPPRESS_SHADOW_GENERATION;
    private transient RenderingHints renderingHints;
    private String id;
    private boolean borderVisible;
    private transient Stroke borderStroke;
    private transient Paint borderPaint;
    private RectangleInsets padding;
    private TextTitle title;
    private List subtitles;
    private Plot plot;
    private transient Paint backgroundPaint;
    private transient Image backgroundImage;
    private int backgroundImageAlignment;
    private float backgroundImageAlpha;
    private transient EventListenerList changeListeners;
    private transient EventListenerList progressListeners;
    private boolean notify;
    private boolean elementHinting;
    public JFreeChart ( final Plot plot ) {
        this ( null, null, plot, true );
    }
    public JFreeChart ( final String title, final Plot plot ) {
        this ( title, JFreeChart.DEFAULT_TITLE_FONT, plot, true );
    }
    public JFreeChart ( final String title, Font titleFont, final Plot plot, final boolean createLegend ) {
        this.backgroundImageAlignment = 15;
        this.backgroundImageAlpha = 0.5f;
        ParamChecks.nullNotPermitted ( plot, "plot" );
        this.id = null;
        plot.setChart ( this );
        this.progressListeners = new EventListenerList();
        this.changeListeners = new EventListenerList();
        this.notify = true;
        ( this.renderingHints = new RenderingHints ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON ) ).put ( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE );
        this.borderVisible = false;
        this.borderStroke = new BasicStroke ( 1.0f );
        this.borderPaint = Color.black;
        this.padding = RectangleInsets.ZERO_INSETS;
        ( this.plot = plot ).addChangeListener ( this );
        this.subtitles = new ArrayList();
        if ( createLegend ) {
            final LegendTitle legend = new LegendTitle ( this.plot );
            legend.setMargin ( new RectangleInsets ( 1.0, 1.0, 1.0, 1.0 ) );
            legend.setFrame ( new LineBorder() );
            legend.setBackgroundPaint ( Color.white );
            legend.setPosition ( RectangleEdge.BOTTOM );
            this.subtitles.add ( legend );
            legend.addChangeListener ( this );
        }
        if ( title != null ) {
            if ( titleFont == null ) {
                titleFont = JFreeChart.DEFAULT_TITLE_FONT;
            }
            ( this.title = new TextTitle ( title, titleFont ) ).addChangeListener ( this );
        }
        this.backgroundPaint = JFreeChart.DEFAULT_BACKGROUND_PAINT;
        this.backgroundImage = JFreeChart.DEFAULT_BACKGROUND_IMAGE;
        this.backgroundImageAlignment = 15;
        this.backgroundImageAlpha = 0.5f;
    }
    public String getID() {
        return this.id;
    }
    public void setID ( final String id ) {
        this.id = id;
    }
    public boolean getElementHinting() {
        return this.elementHinting;
    }
    public void setElementHinting ( final boolean hinting ) {
        this.elementHinting = hinting;
    }
    public RenderingHints getRenderingHints() {
        return this.renderingHints;
    }
    public void setRenderingHints ( final RenderingHints renderingHints ) {
        ParamChecks.nullNotPermitted ( renderingHints, "renderingHints" );
        this.renderingHints = renderingHints;
        this.fireChartChanged();
    }
    public boolean isBorderVisible() {
        return this.borderVisible;
    }
    public void setBorderVisible ( final boolean visible ) {
        this.borderVisible = visible;
        this.fireChartChanged();
    }
    public Stroke getBorderStroke() {
        return this.borderStroke;
    }
    public void setBorderStroke ( final Stroke stroke ) {
        this.borderStroke = stroke;
        this.fireChartChanged();
    }
    public Paint getBorderPaint() {
        return this.borderPaint;
    }
    public void setBorderPaint ( final Paint paint ) {
        this.borderPaint = paint;
        this.fireChartChanged();
    }
    public RectangleInsets getPadding() {
        return this.padding;
    }
    public void setPadding ( final RectangleInsets padding ) {
        ParamChecks.nullNotPermitted ( padding, "padding" );
        this.padding = padding;
        this.notifyListeners ( new ChartChangeEvent ( this ) );
    }
    public TextTitle getTitle() {
        return this.title;
    }
    public void setTitle ( final TextTitle title ) {
        if ( this.title != null ) {
            this.title.removeChangeListener ( this );
        }
        if ( ( this.title = title ) != null ) {
            title.addChangeListener ( this );
        }
        this.fireChartChanged();
    }
    public void setTitle ( final String text ) {
        if ( text != null ) {
            if ( this.title == null ) {
                this.setTitle ( new TextTitle ( text, JFreeChart.DEFAULT_TITLE_FONT ) );
            } else {
                this.title.setText ( text );
            }
        } else {
            this.setTitle ( ( TextTitle ) null );
        }
    }
    public void addLegend ( final LegendTitle legend ) {
        this.addSubtitle ( legend );
    }
    public LegendTitle getLegend() {
        return this.getLegend ( 0 );
    }
    public LegendTitle getLegend ( final int index ) {
        int seen = 0;
        for ( final Title subtitle : this.subtitles ) {
            if ( subtitle instanceof LegendTitle ) {
                if ( seen == index ) {
                    return ( LegendTitle ) subtitle;
                }
                ++seen;
            }
        }
        return null;
    }
    public void removeLegend() {
        this.removeSubtitle ( this.getLegend() );
    }
    public List getSubtitles() {
        return new ArrayList ( this.subtitles );
    }
    public void setSubtitles ( final List subtitles ) {
        if ( subtitles == null ) {
            throw new NullPointerException ( "Null 'subtitles' argument." );
        }
        this.setNotify ( false );
        this.clearSubtitles();
        for ( final Title t : subtitles ) {
            if ( t != null ) {
                this.addSubtitle ( t );
            }
        }
        this.setNotify ( true );
    }
    public int getSubtitleCount() {
        return this.subtitles.size();
    }
    public Title getSubtitle ( final int index ) {
        if ( index < 0 || index >= this.getSubtitleCount() ) {
            throw new IllegalArgumentException ( "Index out of range." );
        }
        return this.subtitles.get ( index );
    }
    public void addSubtitle ( final Title subtitle ) {
        ParamChecks.nullNotPermitted ( subtitle, "subtitle" );
        this.subtitles.add ( subtitle );
        subtitle.addChangeListener ( this );
        this.fireChartChanged();
    }
    public void addSubtitle ( final int index, final Title subtitle ) {
        if ( index < 0 || index > this.getSubtitleCount() ) {
            throw new IllegalArgumentException ( "The 'index' argument is out of range." );
        }
        ParamChecks.nullNotPermitted ( subtitle, "subtitle" );
        this.subtitles.add ( index, subtitle );
        subtitle.addChangeListener ( this );
        this.fireChartChanged();
    }
    public void clearSubtitles() {
        for ( final Title t : this.subtitles ) {
            t.removeChangeListener ( this );
        }
        this.subtitles.clear();
        this.fireChartChanged();
    }
    public void removeSubtitle ( final Title title ) {
        this.subtitles.remove ( title );
        this.fireChartChanged();
    }
    public Plot getPlot() {
        return this.plot;
    }
    public CategoryPlot getCategoryPlot() {
        return ( CategoryPlot ) this.plot;
    }
    public XYPlot getXYPlot() {
        return ( XYPlot ) this.plot;
    }
    public boolean getAntiAlias() {
        final Object val = this.renderingHints.get ( RenderingHints.KEY_ANTIALIASING );
        return RenderingHints.VALUE_ANTIALIAS_ON.equals ( val );
    }
    public void setAntiAlias ( final boolean flag ) {
        final Object hint = flag ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF;
        this.renderingHints.put ( RenderingHints.KEY_ANTIALIASING, hint );
        this.fireChartChanged();
    }
    public Object getTextAntiAlias() {
        return this.renderingHints.get ( RenderingHints.KEY_TEXT_ANTIALIASING );
    }
    public void setTextAntiAlias ( final boolean flag ) {
        if ( flag ) {
            this.setTextAntiAlias ( RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
        } else {
            this.setTextAntiAlias ( RenderingHints.VALUE_TEXT_ANTIALIAS_OFF );
        }
    }
    public void setTextAntiAlias ( final Object val ) {
        this.renderingHints.put ( RenderingHints.KEY_TEXT_ANTIALIASING, val );
        this.notifyListeners ( new ChartChangeEvent ( this ) );
    }
    public Paint getBackgroundPaint() {
        return this.backgroundPaint;
    }
    public void setBackgroundPaint ( final Paint paint ) {
        if ( this.backgroundPaint != null ) {
            if ( !this.backgroundPaint.equals ( paint ) ) {
                this.backgroundPaint = paint;
                this.fireChartChanged();
            }
        } else if ( paint != null ) {
            this.backgroundPaint = paint;
            this.fireChartChanged();
        }
    }
    public Image getBackgroundImage() {
        return this.backgroundImage;
    }
    public void setBackgroundImage ( final Image image ) {
        if ( this.backgroundImage != null ) {
            if ( !this.backgroundImage.equals ( image ) ) {
                this.backgroundImage = image;
                this.fireChartChanged();
            }
        } else if ( image != null ) {
            this.backgroundImage = image;
            this.fireChartChanged();
        }
    }
    public int getBackgroundImageAlignment() {
        return this.backgroundImageAlignment;
    }
    public void setBackgroundImageAlignment ( final int alignment ) {
        if ( this.backgroundImageAlignment != alignment ) {
            this.backgroundImageAlignment = alignment;
            this.fireChartChanged();
        }
    }
    public float getBackgroundImageAlpha() {
        return this.backgroundImageAlpha;
    }
    public void setBackgroundImageAlpha ( final float alpha ) {
        if ( this.backgroundImageAlpha != alpha ) {
            this.backgroundImageAlpha = alpha;
            this.fireChartChanged();
        }
    }
    public boolean isNotify() {
        return this.notify;
    }
    public void setNotify ( final boolean notify ) {
        this.notify = notify;
        if ( notify ) {
            this.notifyListeners ( new ChartChangeEvent ( this ) );
        }
    }
    public void draw ( final Graphics2D g2, final Rectangle2D area ) {
        this.draw ( g2, area, null, null );
    }
    public void draw ( final Graphics2D g2, final Rectangle2D area, final ChartRenderingInfo info ) {
        this.draw ( g2, area, null, info );
    }
    public void draw ( final Graphics2D g2, final Rectangle2D chartArea, final Point2D anchor, final ChartRenderingInfo info ) {
        this.notifyListeners ( new ChartProgressEvent ( this, this, 1, 0 ) );
        if ( this.elementHinting ) {
            final Map m = new HashMap();
            if ( this.id != null ) {
                m.put ( "id", this.id );
            }
            m.put ( "ref", "JFREECHART_TOP_LEVEL" );
            g2.setRenderingHint ( ChartHints.KEY_BEGIN_ELEMENT, m );
        }
        EntityCollection entities = null;
        if ( info != null ) {
            info.clear();
            info.setChartArea ( chartArea );
            entities = info.getEntityCollection();
        }
        if ( entities != null ) {
            entities.add ( new JFreeChartEntity ( ( Shape ) chartArea.clone(), this ) );
        }
        final Shape savedClip = g2.getClip();
        g2.clip ( chartArea );
        g2.addRenderingHints ( this.renderingHints );
        if ( this.backgroundPaint != null ) {
            g2.setPaint ( this.backgroundPaint );
            g2.fill ( chartArea );
        }
        if ( this.backgroundImage != null ) {
            final Composite originalComposite = g2.getComposite();
            g2.setComposite ( AlphaComposite.getInstance ( 3, this.backgroundImageAlpha ) );
            final Rectangle2D dest = new Rectangle2D.Double ( 0.0, 0.0, this.backgroundImage.getWidth ( null ), this.backgroundImage.getHeight ( null ) );
            Align.align ( dest, chartArea, this.backgroundImageAlignment );
            g2.drawImage ( this.backgroundImage, ( int ) dest.getX(), ( int ) dest.getY(), ( int ) dest.getWidth(), ( int ) dest.getHeight(), null );
            g2.setComposite ( originalComposite );
        }
        if ( this.isBorderVisible() ) {
            final Paint paint = this.getBorderPaint();
            final Stroke stroke = this.getBorderStroke();
            if ( paint != null && stroke != null ) {
                final Rectangle2D borderArea = new Rectangle2D.Double ( chartArea.getX(), chartArea.getY(), chartArea.getWidth() - 1.0, chartArea.getHeight() - 1.0 );
                g2.setPaint ( paint );
                g2.setStroke ( stroke );
                g2.draw ( borderArea );
            }
        }
        final Rectangle2D nonTitleArea = new Rectangle2D.Double();
        nonTitleArea.setRect ( chartArea );
        this.padding.trim ( nonTitleArea );
        if ( this.title != null && this.title.isVisible() ) {
            final EntityCollection e = this.drawTitle ( this.title, g2, nonTitleArea, entities != null );
            if ( e != null && entities != null ) {
                entities.addAll ( e );
            }
        }
        for ( final Title currentTitle : this.subtitles ) {
            if ( currentTitle.isVisible() ) {
                final EntityCollection e2 = this.drawTitle ( currentTitle, g2, nonTitleArea, entities != null );
                if ( e2 == null || entities == null ) {
                    continue;
                }
                entities.addAll ( e2 );
            }
        }
        final Rectangle2D plotArea = nonTitleArea;
        PlotRenderingInfo plotInfo = null;
        if ( info != null ) {
            plotInfo = info.getPlotInfo();
        }
        this.plot.draw ( g2, plotArea, anchor, null, plotInfo );
        g2.setClip ( savedClip );
        if ( this.elementHinting ) {
            g2.setRenderingHint ( ChartHints.KEY_END_ELEMENT, Boolean.TRUE );
        }
        this.notifyListeners ( new ChartProgressEvent ( this, this, 2, 100 ) );
    }
    private Rectangle2D createAlignedRectangle2D ( final Size2D dimensions, final Rectangle2D frame, final HorizontalAlignment hAlign, final VerticalAlignment vAlign ) {
        double x = Double.NaN;
        double y = Double.NaN;
        if ( hAlign == HorizontalAlignment.LEFT ) {
            x = frame.getX();
        } else if ( hAlign == HorizontalAlignment.CENTER ) {
            x = frame.getCenterX() - dimensions.width / 2.0;
        } else if ( hAlign == HorizontalAlignment.RIGHT ) {
            x = frame.getMaxX() - dimensions.width;
        }
        if ( vAlign == VerticalAlignment.TOP ) {
            y = frame.getY();
        } else if ( vAlign == VerticalAlignment.CENTER ) {
            y = frame.getCenterY() - dimensions.height / 2.0;
        } else if ( vAlign == VerticalAlignment.BOTTOM ) {
            y = frame.getMaxY() - dimensions.height;
        }
        return new Rectangle2D.Double ( x, y, dimensions.width, dimensions.height );
    }
    protected EntityCollection drawTitle ( final Title t, final Graphics2D g2, final Rectangle2D area, final boolean entities ) {
        ParamChecks.nullNotPermitted ( t, "t" );
        ParamChecks.nullNotPermitted ( area, "area" );
        final RectangleEdge position = t.getPosition();
        final double ww = area.getWidth();
        if ( ww <= 0.0 ) {
            return null;
        }
        final double hh = area.getHeight();
        if ( hh <= 0.0 ) {
            return null;
        }
        final RectangleConstraint constraint = new RectangleConstraint ( ww, new Range ( 0.0, ww ), LengthConstraintType.RANGE, hh, new Range ( 0.0, hh ), LengthConstraintType.RANGE );
        Object retValue = null;
        final BlockParams p = new BlockParams();
        p.setGenerateEntities ( entities );
        if ( position == RectangleEdge.TOP ) {
            final Size2D size = t.arrange ( g2, constraint );
            final Rectangle2D titleArea = this.createAlignedRectangle2D ( size, area, t.getHorizontalAlignment(), VerticalAlignment.TOP );
            retValue = t.draw ( g2, titleArea, p );
            area.setRect ( area.getX(), Math.min ( area.getY() + size.height, area.getMaxY() ), area.getWidth(), Math.max ( area.getHeight() - size.height, 0.0 ) );
        } else if ( position == RectangleEdge.BOTTOM ) {
            final Size2D size = t.arrange ( g2, constraint );
            final Rectangle2D titleArea = this.createAlignedRectangle2D ( size, area, t.getHorizontalAlignment(), VerticalAlignment.BOTTOM );
            retValue = t.draw ( g2, titleArea, p );
            area.setRect ( area.getX(), area.getY(), area.getWidth(), area.getHeight() - size.height );
        } else if ( position == RectangleEdge.RIGHT ) {
            final Size2D size = t.arrange ( g2, constraint );
            final Rectangle2D titleArea = this.createAlignedRectangle2D ( size, area, HorizontalAlignment.RIGHT, t.getVerticalAlignment() );
            retValue = t.draw ( g2, titleArea, p );
            area.setRect ( area.getX(), area.getY(), area.getWidth() - size.width, area.getHeight() );
        } else {
            if ( position != RectangleEdge.LEFT ) {
                throw new RuntimeException ( "Unrecognised title position." );
            }
            final Size2D size = t.arrange ( g2, constraint );
            final Rectangle2D titleArea = this.createAlignedRectangle2D ( size, area, HorizontalAlignment.LEFT, t.getVerticalAlignment() );
            retValue = t.draw ( g2, titleArea, p );
            area.setRect ( area.getX() + size.width, area.getY(), area.getWidth() - size.width, area.getHeight() );
        }
        EntityCollection result = null;
        if ( retValue instanceof EntityBlockResult ) {
            final EntityBlockResult ebr = ( EntityBlockResult ) retValue;
            result = ebr.getEntityCollection();
        }
        return result;
    }
    public BufferedImage createBufferedImage ( final int width, final int height ) {
        return this.createBufferedImage ( width, height, null );
    }
    public BufferedImage createBufferedImage ( final int width, final int height, final ChartRenderingInfo info ) {
        return this.createBufferedImage ( width, height, 2, info );
    }
    public BufferedImage createBufferedImage ( final int width, final int height, final int imageType, final ChartRenderingInfo info ) {
        final BufferedImage image = new BufferedImage ( width, height, imageType );
        final Graphics2D g2 = image.createGraphics();
        this.draw ( g2, new Rectangle2D.Double ( 0.0, 0.0, width, height ), null, info );
        g2.dispose();
        return image;
    }
    public BufferedImage createBufferedImage ( final int imageWidth, final int imageHeight, final double drawWidth, final double drawHeight, final ChartRenderingInfo info ) {
        final BufferedImage image = new BufferedImage ( imageWidth, imageHeight, 2 );
        final Graphics2D g2 = image.createGraphics();
        final double scaleX = imageWidth / drawWidth;
        final double scaleY = imageHeight / drawHeight;
        final AffineTransform st = AffineTransform.getScaleInstance ( scaleX, scaleY );
        g2.transform ( st );
        this.draw ( g2, new Rectangle2D.Double ( 0.0, 0.0, drawWidth, drawHeight ), null, info );
        g2.dispose();
        return image;
    }
    public void handleClick ( final int x, final int y, final ChartRenderingInfo info ) {
        this.plot.handleClick ( x, y, info.getPlotInfo() );
    }
    public void addChangeListener ( final ChartChangeListener listener ) {
        ParamChecks.nullNotPermitted ( listener, "listener" );
        this.changeListeners.add ( ChartChangeListener.class, listener );
    }
    public void removeChangeListener ( final ChartChangeListener listener ) {
        ParamChecks.nullNotPermitted ( listener, "listener" );
        this.changeListeners.remove ( ChartChangeListener.class, listener );
    }
    public void fireChartChanged() {
        final ChartChangeEvent event = new ChartChangeEvent ( this );
        this.notifyListeners ( event );
    }
    protected void notifyListeners ( final ChartChangeEvent event ) {
        if ( this.notify ) {
            final Object[] listeners = this.changeListeners.getListenerList();
            for ( int i = listeners.length - 2; i >= 0; i -= 2 ) {
                if ( listeners[i] == ChartChangeListener.class ) {
                    ( ( ChartChangeListener ) listeners[i + 1] ).chartChanged ( event );
                }
            }
        }
    }
    public void addProgressListener ( final ChartProgressListener listener ) {
        this.progressListeners.add ( ChartProgressListener.class, listener );
    }
    public void removeProgressListener ( final ChartProgressListener listener ) {
        this.progressListeners.remove ( ChartProgressListener.class, listener );
    }
    protected void notifyListeners ( final ChartProgressEvent event ) {
        final Object[] listeners = this.progressListeners.getListenerList();
        for ( int i = listeners.length - 2; i >= 0; i -= 2 ) {
            if ( listeners[i] == ChartProgressListener.class ) {
                ( ( ChartProgressListener ) listeners[i + 1] ).chartProgress ( event );
            }
        }
    }
    public void titleChanged ( final TitleChangeEvent event ) {
        event.setChart ( this );
        this.notifyListeners ( event );
    }
    public void plotChanged ( final PlotChangeEvent event ) {
        event.setChart ( this );
        this.notifyListeners ( event );
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof JFreeChart ) ) {
            return false;
        }
        final JFreeChart that = ( JFreeChart ) obj;
        return this.renderingHints.equals ( that.renderingHints ) && this.borderVisible == that.borderVisible && ObjectUtilities.equal ( ( Object ) this.borderStroke, ( Object ) that.borderStroke ) && PaintUtilities.equal ( this.borderPaint, that.borderPaint ) && this.padding.equals ( ( Object ) that.padding ) && ObjectUtilities.equal ( ( Object ) this.title, ( Object ) that.title ) && ObjectUtilities.equal ( ( Object ) this.subtitles, ( Object ) that.subtitles ) && ObjectUtilities.equal ( ( Object ) this.plot, ( Object ) that.plot ) && PaintUtilities.equal ( this.backgroundPaint, that.backgroundPaint ) && ObjectUtilities.equal ( ( Object ) this.backgroundImage, ( Object ) that.backgroundImage ) && this.backgroundImageAlignment == that.backgroundImageAlignment && this.backgroundImageAlpha == that.backgroundImageAlpha && this.notify == that.notify;
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeStroke ( this.borderStroke, stream );
        SerialUtilities.writePaint ( this.borderPaint, stream );
        SerialUtilities.writePaint ( this.backgroundPaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.borderStroke = SerialUtilities.readStroke ( stream );
        this.borderPaint = SerialUtilities.readPaint ( stream );
        this.backgroundPaint = SerialUtilities.readPaint ( stream );
        this.progressListeners = new EventListenerList();
        this.changeListeners = new EventListenerList();
        ( this.renderingHints = new RenderingHints ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON ) ).put ( RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE );
        if ( this.title != null ) {
            this.title.addChangeListener ( this );
        }
        for ( int i = 0; i < this.getSubtitleCount(); ++i ) {
            this.getSubtitle ( i ).addChangeListener ( this );
        }
        this.plot.addChangeListener ( this );
    }
    public static void main ( final String[] args ) {
        System.out.println ( JFreeChart.INFO.toString() );
    }
    public Object clone() throws CloneNotSupportedException {
        final JFreeChart chart = ( JFreeChart ) super.clone();
        chart.renderingHints = ( RenderingHints ) this.renderingHints.clone();
        if ( this.title != null ) {
            ( chart.title = ( TextTitle ) this.title.clone() ).addChangeListener ( chart );
        }
        chart.subtitles = new ArrayList();
        for ( int i = 0; i < this.getSubtitleCount(); ++i ) {
            final Title subtitle = ( Title ) this.getSubtitle ( i ).clone();
            chart.subtitles.add ( subtitle );
            subtitle.addChangeListener ( chart );
        }
        if ( this.plot != null ) {
            ( chart.plot = ( Plot ) this.plot.clone() ).addChangeListener ( chart );
        }
        chart.progressListeners = new EventListenerList();
        chart.changeListeners = new EventListenerList();
        return chart;
    }
    static {
        INFO = new JFreeChartInfo();
        DEFAULT_TITLE_FONT = new Font ( "SansSerif", 1, 18 );
        DEFAULT_BACKGROUND_PAINT = UIManager.getColor ( "Panel.background" );
        DEFAULT_BACKGROUND_IMAGE = null;
        KEY_SUPPRESS_SHADOW_GENERATION = new RenderingHints.Key ( 0 ) {
            @Override
            public boolean isCompatibleValue ( final Object val ) {
                return val instanceof Boolean;
            }
        };
    }
}
