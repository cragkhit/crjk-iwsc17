package org.jfree.chart;
import org.jfree.chart.util.ResourceBundleWrapper;
import java.io.ObjectInputStream;
import org.jfree.io.SerialUtilities;
import java.io.ObjectOutputStream;
import javax.swing.SwingUtilities;
import javax.swing.JMenu;
import java.util.EventListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.io.Writer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFileChooser;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.Toolkit;
import org.jfree.chart.editor.ChartEditor;
import org.jfree.chart.editor.ChartEditorManager;
import org.jfree.chart.plot.PlotRenderingInfo;
import java.awt.Cursor;
import org.jfree.chart.plot.Pannable;
import java.io.IOException;
import java.awt.Component;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartChangeEvent;
import java.util.Iterator;
import java.awt.GraphicsConfiguration;
import java.awt.image.ImageObserver;
import java.awt.geom.AffineTransform;
import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.Composite;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Graphics;
import org.jfree.chart.entity.ChartEntity;
import java.awt.Insets;
import org.jfree.chart.entity.EntityCollection;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import org.jfree.chart.event.OverlayChangeEvent;
import org.jfree.chart.panel.Overlay;
import java.awt.event.MouseWheelListener;
import org.jfree.chart.util.ParamChecks;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.Zoomable;
import java.util.ArrayList;
import java.awt.Color;
import javax.swing.ToolTipManager;
import java.awt.Dimension;
import java.util.List;
import java.awt.Point;
import java.util.ResourceBundle;
import java.awt.Paint;
import java.io.File;
import javax.swing.JMenuItem;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.plot.PlotOrientation;
import java.awt.geom.Point2D;
import javax.swing.JPopupMenu;
import java.awt.Image;
import javax.swing.event.EventListenerList;
import java.io.Serializable;
import java.awt.print.Printable;
import org.jfree.chart.event.OverlayChangeListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import java.awt.event.ActionListener;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.event.ChartChangeListener;
import javax.swing.JPanel;
public class ChartPanel extends JPanel implements ChartChangeListener, ChartProgressListener, ActionListener, MouseListener, MouseMotionListener, OverlayChangeListener, Printable, Serializable {
    private static final long serialVersionUID = 6046366297214274674L;
    public static final boolean DEFAULT_BUFFER_USED = true;
    public static final int DEFAULT_WIDTH = 680;
    public static final int DEFAULT_HEIGHT = 420;
    public static final int DEFAULT_MINIMUM_DRAW_WIDTH = 300;
    public static final int DEFAULT_MINIMUM_DRAW_HEIGHT = 200;
    public static final int DEFAULT_MAXIMUM_DRAW_WIDTH = 1024;
    public static final int DEFAULT_MAXIMUM_DRAW_HEIGHT = 768;
    public static final int DEFAULT_ZOOM_TRIGGER_DISTANCE = 10;
    public static final String PROPERTIES_COMMAND = "PROPERTIES";
    public static final String COPY_COMMAND = "COPY";
    public static final String SAVE_COMMAND = "SAVE";
    private static final String SAVE_AS_PNG_COMMAND = "SAVE_AS_PNG";
    private static final String SAVE_AS_SVG_COMMAND = "SAVE_AS_SVG";
    private static final String SAVE_AS_PDF_COMMAND = "SAVE_AS_PDF";
    public static final String PRINT_COMMAND = "PRINT";
    public static final String ZOOM_IN_BOTH_COMMAND = "ZOOM_IN_BOTH";
    public static final String ZOOM_IN_DOMAIN_COMMAND = "ZOOM_IN_DOMAIN";
    public static final String ZOOM_IN_RANGE_COMMAND = "ZOOM_IN_RANGE";
    public static final String ZOOM_OUT_BOTH_COMMAND = "ZOOM_OUT_BOTH";
    public static final String ZOOM_OUT_DOMAIN_COMMAND = "ZOOM_DOMAIN_BOTH";
    public static final String ZOOM_OUT_RANGE_COMMAND = "ZOOM_RANGE_BOTH";
    public static final String ZOOM_RESET_BOTH_COMMAND = "ZOOM_RESET_BOTH";
    public static final String ZOOM_RESET_DOMAIN_COMMAND = "ZOOM_RESET_DOMAIN";
    public static final String ZOOM_RESET_RANGE_COMMAND = "ZOOM_RESET_RANGE";
    private JFreeChart chart;
    private transient EventListenerList chartMouseListeners;
    private boolean useBuffer;
    private boolean refreshBuffer;
    private transient Image chartBuffer;
    private int chartBufferHeight;
    private int chartBufferWidth;
    private int minimumDrawWidth;
    private int minimumDrawHeight;
    private int maximumDrawWidth;
    private int maximumDrawHeight;
    private JPopupMenu popup;
    private ChartRenderingInfo info;
    private Point2D anchor;
    private double scaleX;
    private double scaleY;
    private PlotOrientation orientation;
    private boolean domainZoomable;
    private boolean rangeZoomable;
    private Point2D zoomPoint;
    private transient Rectangle2D zoomRectangle;
    private boolean fillZoomRectangle;
    private int zoomTriggerDistance;
    private boolean horizontalAxisTrace;
    private boolean verticalAxisTrace;
    private transient Line2D verticalTraceLine;
    private transient Line2D horizontalTraceLine;
    private JMenuItem zoomInBothMenuItem;
    private JMenuItem zoomInDomainMenuItem;
    private JMenuItem zoomInRangeMenuItem;
    private JMenuItem zoomOutBothMenuItem;
    private JMenuItem zoomOutDomainMenuItem;
    private JMenuItem zoomOutRangeMenuItem;
    private JMenuItem zoomResetBothMenuItem;
    private JMenuItem zoomResetDomainMenuItem;
    private JMenuItem zoomResetRangeMenuItem;
    private File defaultDirectoryForSaveAs;
    private boolean enforceFileExtensions;
    private boolean ownToolTipDelaysActive;
    private int originalToolTipInitialDelay;
    private int originalToolTipReshowDelay;
    private int originalToolTipDismissDelay;
    private int ownToolTipInitialDelay;
    private int ownToolTipReshowDelay;
    private int ownToolTipDismissDelay;
    private double zoomInFactor;
    private double zoomOutFactor;
    private boolean zoomAroundAnchor;
    private transient Paint zoomOutlinePaint;
    private transient Paint zoomFillPaint;
    protected static ResourceBundle localizationResources;
    private double panW;
    private double panH;
    private Point panLast;
    private int panMask;
    private List overlays;
    private MouseWheelHandler mouseWheelHandler;
    public ChartPanel ( final JFreeChart chart ) {
        this ( chart, 680, 420, 300, 200, 1024, 768, true, true, true, true, true, true );
    }
    public ChartPanel ( final JFreeChart chart, final boolean useBuffer ) {
        this ( chart, 680, 420, 300, 200, 1024, 768, useBuffer, true, true, true, true, true );
    }
    public ChartPanel ( final JFreeChart chart, final boolean properties, final boolean save, final boolean print, final boolean zoom, final boolean tooltips ) {
        this ( chart, 680, 420, 300, 200, 1024, 768, true, properties, save, print, zoom, tooltips );
    }
    public ChartPanel ( final JFreeChart chart, final int width, final int height, final int minimumDrawWidth, final int minimumDrawHeight, final int maximumDrawWidth, final int maximumDrawHeight, final boolean useBuffer, final boolean properties, final boolean save, final boolean print, final boolean zoom, final boolean tooltips ) {
        this ( chart, width, height, minimumDrawWidth, minimumDrawHeight, maximumDrawWidth, maximumDrawHeight, useBuffer, properties, true, save, print, zoom, tooltips );
    }
    public ChartPanel ( final JFreeChart chart, final int width, final int height, final int minimumDrawWidth, final int minimumDrawHeight, final int maximumDrawWidth, final int maximumDrawHeight, final boolean useBuffer, final boolean properties, final boolean copy, final boolean save, final boolean print, final boolean zoom, final boolean tooltips ) {
        this.orientation = PlotOrientation.VERTICAL;
        this.domainZoomable = false;
        this.rangeZoomable = false;
        this.zoomPoint = null;
        this.zoomRectangle = null;
        this.fillZoomRectangle = true;
        this.horizontalAxisTrace = false;
        this.verticalAxisTrace = false;
        this.zoomInFactor = 0.5;
        this.zoomOutFactor = 2.0;
        this.panMask = 2;
        this.setChart ( chart );
        this.chartMouseListeners = new EventListenerList();
        this.info = new ChartRenderingInfo();
        this.setPreferredSize ( new Dimension ( width, height ) );
        this.useBuffer = useBuffer;
        this.refreshBuffer = false;
        this.minimumDrawWidth = minimumDrawWidth;
        this.minimumDrawHeight = minimumDrawHeight;
        this.maximumDrawWidth = maximumDrawWidth;
        this.maximumDrawHeight = maximumDrawHeight;
        this.zoomTriggerDistance = 10;
        this.popup = null;
        if ( properties || copy || save || print || zoom ) {
            this.popup = this.createPopupMenu ( properties, copy, save, print, zoom );
        }
        this.enableEvents ( 16L );
        this.enableEvents ( 32L );
        this.setDisplayToolTips ( tooltips );
        this.addMouseListener ( this );
        this.addMouseMotionListener ( this );
        this.defaultDirectoryForSaveAs = null;
        this.enforceFileExtensions = true;
        final ToolTipManager ttm = ToolTipManager.sharedInstance();
        this.ownToolTipInitialDelay = ttm.getInitialDelay();
        this.ownToolTipDismissDelay = ttm.getDismissDelay();
        this.ownToolTipReshowDelay = ttm.getReshowDelay();
        this.zoomAroundAnchor = false;
        this.zoomOutlinePaint = Color.blue;
        this.zoomFillPaint = new Color ( 0, 0, 255, 63 );
        this.panMask = 2;
        final String osName = System.getProperty ( "os.name" ).toLowerCase();
        if ( osName.startsWith ( "mac os x" ) ) {
            this.panMask = 8;
        }
        this.overlays = new ArrayList();
    }
    public JFreeChart getChart() {
        return this.chart;
    }
    public void setChart ( final JFreeChart chart ) {
        if ( this.chart != null ) {
            this.chart.removeChangeListener ( this );
            this.chart.removeProgressListener ( this );
        }
        if ( ( this.chart = chart ) != null ) {
            this.chart.addChangeListener ( this );
            this.chart.addProgressListener ( this );
            final Plot plot = chart.getPlot();
            this.domainZoomable = false;
            this.rangeZoomable = false;
            if ( plot instanceof Zoomable ) {
                final Zoomable z = ( Zoomable ) plot;
                this.domainZoomable = z.isDomainZoomable();
                this.rangeZoomable = z.isRangeZoomable();
                this.orientation = z.getOrientation();
            }
        } else {
            this.domainZoomable = false;
            this.rangeZoomable = false;
        }
        if ( this.useBuffer ) {
            this.refreshBuffer = true;
        }
        this.repaint();
    }
    public int getMinimumDrawWidth() {
        return this.minimumDrawWidth;
    }
    public void setMinimumDrawWidth ( final int width ) {
        this.minimumDrawWidth = width;
    }
    public int getMaximumDrawWidth() {
        return this.maximumDrawWidth;
    }
    public void setMaximumDrawWidth ( final int width ) {
        this.maximumDrawWidth = width;
    }
    public int getMinimumDrawHeight() {
        return this.minimumDrawHeight;
    }
    public void setMinimumDrawHeight ( final int height ) {
        this.minimumDrawHeight = height;
    }
    public int getMaximumDrawHeight() {
        return this.maximumDrawHeight;
    }
    public void setMaximumDrawHeight ( final int height ) {
        this.maximumDrawHeight = height;
    }
    public double getScaleX() {
        return this.scaleX;
    }
    public double getScaleY() {
        return this.scaleY;
    }
    public Point2D getAnchor() {
        return this.anchor;
    }
    protected void setAnchor ( final Point2D anchor ) {
        this.anchor = anchor;
    }
    public JPopupMenu getPopupMenu() {
        return this.popup;
    }
    public void setPopupMenu ( final JPopupMenu popup ) {
        this.popup = popup;
    }
    public ChartRenderingInfo getChartRenderingInfo() {
        return this.info;
    }
    public void setMouseZoomable ( final boolean flag ) {
        this.setMouseZoomable ( flag, true );
    }
    public void setMouseZoomable ( final boolean flag, final boolean fillRectangle ) {
        this.setDomainZoomable ( flag );
        this.setRangeZoomable ( flag );
        this.setFillZoomRectangle ( fillRectangle );
    }
    public boolean isDomainZoomable() {
        return this.domainZoomable;
    }
    public void setDomainZoomable ( final boolean flag ) {
        if ( flag ) {
            final Plot plot = this.chart.getPlot();
            if ( plot instanceof Zoomable ) {
                final Zoomable z = ( Zoomable ) plot;
                this.domainZoomable = ( flag && z.isDomainZoomable() );
            }
        } else {
            this.domainZoomable = false;
        }
    }
    public boolean isRangeZoomable() {
        return this.rangeZoomable;
    }
    public void setRangeZoomable ( final boolean flag ) {
        if ( flag ) {
            final Plot plot = this.chart.getPlot();
            if ( plot instanceof Zoomable ) {
                final Zoomable z = ( Zoomable ) plot;
                this.rangeZoomable = ( flag && z.isRangeZoomable() );
            }
        } else {
            this.rangeZoomable = false;
        }
    }
    public boolean getFillZoomRectangle() {
        return this.fillZoomRectangle;
    }
    public void setFillZoomRectangle ( final boolean flag ) {
        this.fillZoomRectangle = flag;
    }
    public int getZoomTriggerDistance() {
        return this.zoomTriggerDistance;
    }
    public void setZoomTriggerDistance ( final int distance ) {
        this.zoomTriggerDistance = distance;
    }
    public boolean getHorizontalAxisTrace() {
        return this.horizontalAxisTrace;
    }
    public void setHorizontalAxisTrace ( final boolean flag ) {
        this.horizontalAxisTrace = flag;
    }
    protected Line2D getHorizontalTraceLine() {
        return this.horizontalTraceLine;
    }
    protected void setHorizontalTraceLine ( final Line2D line ) {
        this.horizontalTraceLine = line;
    }
    public boolean getVerticalAxisTrace() {
        return this.verticalAxisTrace;
    }
    public void setVerticalAxisTrace ( final boolean flag ) {
        this.verticalAxisTrace = flag;
    }
    protected Line2D getVerticalTraceLine() {
        return this.verticalTraceLine;
    }
    protected void setVerticalTraceLine ( final Line2D line ) {
        this.verticalTraceLine = line;
    }
    public File getDefaultDirectoryForSaveAs() {
        return this.defaultDirectoryForSaveAs;
    }
    public void setDefaultDirectoryForSaveAs ( final File directory ) {
        if ( directory != null && !directory.isDirectory() ) {
            throw new IllegalArgumentException ( "The 'directory' argument is not a directory." );
        }
        this.defaultDirectoryForSaveAs = directory;
    }
    public boolean isEnforceFileExtensions() {
        return this.enforceFileExtensions;
    }
    public void setEnforceFileExtensions ( final boolean enforce ) {
        this.enforceFileExtensions = enforce;
    }
    public boolean getZoomAroundAnchor() {
        return this.zoomAroundAnchor;
    }
    public void setZoomAroundAnchor ( final boolean zoomAroundAnchor ) {
        this.zoomAroundAnchor = zoomAroundAnchor;
    }
    public Paint getZoomFillPaint() {
        return this.zoomFillPaint;
    }
    public void setZoomFillPaint ( final Paint paint ) {
        ParamChecks.nullNotPermitted ( paint, "paint" );
        this.zoomFillPaint = paint;
    }
    public Paint getZoomOutlinePaint() {
        return this.zoomOutlinePaint;
    }
    public void setZoomOutlinePaint ( final Paint paint ) {
        this.zoomOutlinePaint = paint;
    }
    public boolean isMouseWheelEnabled() {
        return this.mouseWheelHandler != null;
    }
    public void setMouseWheelEnabled ( final boolean flag ) {
        if ( flag && this.mouseWheelHandler == null ) {
            this.mouseWheelHandler = new MouseWheelHandler ( this );
        } else if ( !flag && this.mouseWheelHandler != null ) {
            this.removeMouseWheelListener ( this.mouseWheelHandler );
            this.mouseWheelHandler = null;
        }
    }
    public void addOverlay ( final Overlay overlay ) {
        ParamChecks.nullNotPermitted ( overlay, "overlay" );
        this.overlays.add ( overlay );
        overlay.addChangeListener ( this );
        this.repaint();
    }
    public void removeOverlay ( final Overlay overlay ) {
        ParamChecks.nullNotPermitted ( overlay, "overlay" );
        final boolean removed = this.overlays.remove ( overlay );
        if ( removed ) {
            overlay.removeChangeListener ( this );
            this.repaint();
        }
    }
    @Override
    public void overlayChanged ( final OverlayChangeEvent event ) {
        this.repaint();
    }
    public void setDisplayToolTips ( final boolean flag ) {
        if ( flag ) {
            ToolTipManager.sharedInstance().registerComponent ( this );
        } else {
            ToolTipManager.sharedInstance().unregisterComponent ( this );
        }
    }
    @Override
    public String getToolTipText ( final MouseEvent e ) {
        String result = null;
        if ( this.info != null ) {
            final EntityCollection entities = this.info.getEntityCollection();
            if ( entities != null ) {
                final Insets insets = this.getInsets();
                final ChartEntity entity = entities.getEntity ( ( int ) ( ( e.getX() - insets.left ) / this.scaleX ), ( int ) ( ( e.getY() - insets.top ) / this.scaleY ) );
                if ( entity != null ) {
                    result = entity.getToolTipText();
                }
            }
        }
        return result;
    }
    public Point translateJava2DToScreen ( final Point2D java2DPoint ) {
        final Insets insets = this.getInsets();
        final int x = ( int ) ( java2DPoint.getX() * this.scaleX + insets.left );
        final int y = ( int ) ( java2DPoint.getY() * this.scaleY + insets.top );
        return new Point ( x, y );
    }
    public Point2D translateScreenToJava2D ( final Point screenPoint ) {
        final Insets insets = this.getInsets();
        final double x = ( screenPoint.getX() - insets.left ) / this.scaleX;
        final double y = ( screenPoint.getY() - insets.top ) / this.scaleY;
        return new Point2D.Double ( x, y );
    }
    public Rectangle2D scale ( final Rectangle2D rect ) {
        final Insets insets = this.getInsets();
        final double x = rect.getX() * this.getScaleX() + insets.left;
        final double y = rect.getY() * this.getScaleY() + insets.top;
        final double w = rect.getWidth() * this.getScaleX();
        final double h = rect.getHeight() * this.getScaleY();
        return new Rectangle2D.Double ( x, y, w, h );
    }
    public ChartEntity getEntityForPoint ( final int viewX, final int viewY ) {
        ChartEntity result = null;
        if ( this.info != null ) {
            final Insets insets = this.getInsets();
            final double x = ( viewX - insets.left ) / this.scaleX;
            final double y = ( viewY - insets.top ) / this.scaleY;
            final EntityCollection entities = this.info.getEntityCollection();
            result = ( ( entities != null ) ? entities.getEntity ( x, y ) : null );
        }
        return result;
    }
    public boolean getRefreshBuffer() {
        return this.refreshBuffer;
    }
    public void setRefreshBuffer ( final boolean flag ) {
        this.refreshBuffer = flag;
    }
    public void paintComponent ( final Graphics g ) {
        super.paintComponent ( g );
        if ( this.chart == null ) {
            return;
        }
        final Graphics2D g2 = ( Graphics2D ) g.create();
        final Dimension size = this.getSize();
        final Insets insets = this.getInsets();
        final Rectangle2D available = new Rectangle2D.Double ( insets.left, insets.top, size.getWidth() - insets.left - insets.right, size.getHeight() - insets.top - insets.bottom );
        boolean scale = false;
        double drawWidth = available.getWidth();
        double drawHeight = available.getHeight();
        this.scaleX = 1.0;
        this.scaleY = 1.0;
        if ( drawWidth < this.minimumDrawWidth ) {
            this.scaleX = drawWidth / this.minimumDrawWidth;
            drawWidth = this.minimumDrawWidth;
            scale = true;
        } else if ( drawWidth > this.maximumDrawWidth ) {
            this.scaleX = drawWidth / this.maximumDrawWidth;
            drawWidth = this.maximumDrawWidth;
            scale = true;
        }
        if ( drawHeight < this.minimumDrawHeight ) {
            this.scaleY = drawHeight / this.minimumDrawHeight;
            drawHeight = this.minimumDrawHeight;
            scale = true;
        } else if ( drawHeight > this.maximumDrawHeight ) {
            this.scaleY = drawHeight / this.maximumDrawHeight;
            drawHeight = this.maximumDrawHeight;
            scale = true;
        }
        final Rectangle2D chartArea = new Rectangle2D.Double ( 0.0, 0.0, drawWidth, drawHeight );
        if ( this.useBuffer ) {
            if ( this.chartBuffer == null || this.chartBufferWidth != available.getWidth() || this.chartBufferHeight != available.getHeight() ) {
                this.chartBufferWidth = ( int ) available.getWidth();
                this.chartBufferHeight = ( int ) available.getHeight();
                final GraphicsConfiguration gc = g2.getDeviceConfiguration();
                this.chartBuffer = gc.createCompatibleImage ( this.chartBufferWidth, this.chartBufferHeight, 3 );
                this.refreshBuffer = true;
            }
            if ( this.refreshBuffer ) {
                this.refreshBuffer = false;
                final Rectangle2D bufferArea = new Rectangle2D.Double ( 0.0, 0.0, this.chartBufferWidth, this.chartBufferHeight );
                final Graphics2D bufferG2 = ( Graphics2D ) this.chartBuffer.getGraphics();
                final Composite savedComposite = bufferG2.getComposite();
                bufferG2.setComposite ( AlphaComposite.getInstance ( 1, 0.0f ) );
                final Rectangle r = new Rectangle ( 0, 0, this.chartBufferWidth, this.chartBufferHeight );
                bufferG2.fill ( r );
                bufferG2.setComposite ( savedComposite );
                if ( scale ) {
                    final AffineTransform saved = bufferG2.getTransform();
                    final AffineTransform st = AffineTransform.getScaleInstance ( this.scaleX, this.scaleY );
                    bufferG2.transform ( st );
                    this.chart.draw ( bufferG2, chartArea, this.anchor, this.info );
                    bufferG2.setTransform ( saved );
                } else {
                    this.chart.draw ( bufferG2, bufferArea, this.anchor, this.info );
                }
            }
            g2.drawImage ( this.chartBuffer, insets.left, insets.top, this );
        } else {
            final AffineTransform saved2 = g2.getTransform();
            g2.translate ( insets.left, insets.top );
            if ( scale ) {
                final AffineTransform st2 = AffineTransform.getScaleInstance ( this.scaleX, this.scaleY );
                g2.transform ( st2 );
            }
            this.chart.draw ( g2, chartArea, this.anchor, this.info );
            g2.setTransform ( saved2 );
        }
        for ( final Overlay overlay : this.overlays ) {
            overlay.paintOverlay ( g2, this );
        }
        this.drawZoomRectangle ( g2, !this.useBuffer );
        g2.dispose();
        this.anchor = null;
        this.verticalTraceLine = null;
        this.horizontalTraceLine = null;
    }
    @Override
    public void chartChanged ( final ChartChangeEvent event ) {
        this.refreshBuffer = true;
        final Plot plot = this.chart.getPlot();
        if ( plot instanceof Zoomable ) {
            final Zoomable z = ( Zoomable ) plot;
            this.orientation = z.getOrientation();
        }
        this.repaint();
    }
    @Override
    public void chartProgress ( final ChartProgressEvent event ) {
    }
    @Override
    public void actionPerformed ( final ActionEvent event ) {
        final String command = event.getActionCommand();
        double screenX = -1.0;
        double screenY = -1.0;
        if ( this.zoomPoint != null ) {
            screenX = this.zoomPoint.getX();
            screenY = this.zoomPoint.getY();
        }
        if ( command.equals ( "PROPERTIES" ) ) {
            this.doEditChartProperties();
        } else if ( command.equals ( "COPY" ) ) {
            this.doCopy();
        } else if ( command.equals ( "SAVE_AS_PNG" ) ) {
            try {
                this.doSaveAs();
            } catch ( IOException e ) {
                JOptionPane.showMessageDialog ( this, "I/O error occurred.", ChartPanel.localizationResources.getString ( "Save_as_PNG" ), 2 );
            }
        } else if ( command.equals ( "SAVE_AS_SVG" ) ) {
            try {
                this.saveAsSVG ( null );
            } catch ( IOException e ) {
                JOptionPane.showMessageDialog ( this, "I/O error occurred.", ChartPanel.localizationResources.getString ( "Save_as_SVG" ), 2 );
            }
        } else if ( command.equals ( "SAVE_AS_PDF" ) ) {
            this.saveAsPDF ( null );
        } else if ( command.equals ( "PRINT" ) ) {
            this.createChartPrintJob();
        } else if ( command.equals ( "ZOOM_IN_BOTH" ) ) {
            this.zoomInBoth ( screenX, screenY );
        } else if ( command.equals ( "ZOOM_IN_DOMAIN" ) ) {
            this.zoomInDomain ( screenX, screenY );
        } else if ( command.equals ( "ZOOM_IN_RANGE" ) ) {
            this.zoomInRange ( screenX, screenY );
        } else if ( command.equals ( "ZOOM_OUT_BOTH" ) ) {
            this.zoomOutBoth ( screenX, screenY );
        } else if ( command.equals ( "ZOOM_DOMAIN_BOTH" ) ) {
            this.zoomOutDomain ( screenX, screenY );
        } else if ( command.equals ( "ZOOM_RANGE_BOTH" ) ) {
            this.zoomOutRange ( screenX, screenY );
        } else if ( command.equals ( "ZOOM_RESET_BOTH" ) ) {
            this.restoreAutoBounds();
        } else if ( command.equals ( "ZOOM_RESET_DOMAIN" ) ) {
            this.restoreAutoDomainBounds();
        } else if ( command.equals ( "ZOOM_RESET_RANGE" ) ) {
            this.restoreAutoRangeBounds();
        }
    }
    @Override
    public void mouseEntered ( final MouseEvent e ) {
        if ( !this.ownToolTipDelaysActive ) {
            final ToolTipManager ttm = ToolTipManager.sharedInstance();
            this.originalToolTipInitialDelay = ttm.getInitialDelay();
            ttm.setInitialDelay ( this.ownToolTipInitialDelay );
            this.originalToolTipReshowDelay = ttm.getReshowDelay();
            ttm.setReshowDelay ( this.ownToolTipReshowDelay );
            this.originalToolTipDismissDelay = ttm.getDismissDelay();
            ttm.setDismissDelay ( this.ownToolTipDismissDelay );
            this.ownToolTipDelaysActive = true;
        }
    }
    @Override
    public void mouseExited ( final MouseEvent e ) {
        if ( this.ownToolTipDelaysActive ) {
            final ToolTipManager ttm = ToolTipManager.sharedInstance();
            ttm.setInitialDelay ( this.originalToolTipInitialDelay );
            ttm.setReshowDelay ( this.originalToolTipReshowDelay );
            ttm.setDismissDelay ( this.originalToolTipDismissDelay );
            this.ownToolTipDelaysActive = false;
        }
    }
    @Override
    public void mousePressed ( final MouseEvent e ) {
        if ( this.chart == null ) {
            return;
        }
        final Plot plot = this.chart.getPlot();
        final int mods = e.getModifiers();
        if ( ( mods & this.panMask ) == this.panMask ) {
            if ( plot instanceof Pannable ) {
                final Pannable pannable = ( Pannable ) plot;
                if ( pannable.isDomainPannable() || pannable.isRangePannable() ) {
                    final Rectangle2D screenDataArea = this.getScreenDataArea ( e.getX(), e.getY() );
                    if ( screenDataArea != null && screenDataArea.contains ( e.getPoint() ) ) {
                        this.panW = screenDataArea.getWidth();
                        this.panH = screenDataArea.getHeight();
                        this.panLast = e.getPoint();
                        this.setCursor ( Cursor.getPredefinedCursor ( 13 ) );
                    }
                }
            }
        } else if ( this.zoomRectangle == null ) {
            final Rectangle2D screenDataArea2 = this.getScreenDataArea ( e.getX(), e.getY() );
            if ( screenDataArea2 != null ) {
                this.zoomPoint = this.getPointInRectangle ( e.getX(), e.getY(), screenDataArea2 );
            } else {
                this.zoomPoint = null;
            }
            if ( e.isPopupTrigger() && this.popup != null ) {
                this.displayPopupMenu ( e.getX(), e.getY() );
            }
        }
    }
    private Point2D getPointInRectangle ( final int x, final int y, final Rectangle2D area ) {
        final double xx = Math.max ( area.getMinX(), Math.min ( x, area.getMaxX() ) );
        final double yy = Math.max ( area.getMinY(), Math.min ( y, area.getMaxY() ) );
        return new Point2D.Double ( xx, yy );
    }
    @Override
    public void mouseDragged ( final MouseEvent e ) {
        if ( this.popup != null && this.popup.isShowing() ) {
            return;
        }
        if ( this.panLast != null ) {
            final double dx = e.getX() - this.panLast.getX();
            final double dy = e.getY() - this.panLast.getY();
            if ( dx == 0.0 && dy == 0.0 ) {
                return;
            }
            final double wPercent = -dx / this.panW;
            final double hPercent = dy / this.panH;
            final boolean old = this.chart.getPlot().isNotify();
            this.chart.getPlot().setNotify ( false );
            final Pannable p = ( Pannable ) this.chart.getPlot();
            if ( p.getOrientation() == PlotOrientation.VERTICAL ) {
                p.panDomainAxes ( wPercent, this.info.getPlotInfo(), this.panLast );
                p.panRangeAxes ( hPercent, this.info.getPlotInfo(), this.panLast );
            } else {
                p.panDomainAxes ( hPercent, this.info.getPlotInfo(), this.panLast );
                p.panRangeAxes ( wPercent, this.info.getPlotInfo(), this.panLast );
            }
            this.panLast = e.getPoint();
            this.chart.getPlot().setNotify ( old );
        } else {
            if ( this.zoomPoint == null ) {
                return;
            }
            final Graphics2D g2 = ( Graphics2D ) this.getGraphics();
            if ( !this.useBuffer ) {
                this.drawZoomRectangle ( g2, true );
            }
            boolean hZoom;
            boolean vZoom;
            if ( this.orientation == PlotOrientation.HORIZONTAL ) {
                hZoom = this.rangeZoomable;
                vZoom = this.domainZoomable;
            } else {
                hZoom = this.domainZoomable;
                vZoom = this.rangeZoomable;
            }
            final Rectangle2D scaledDataArea = this.getScreenDataArea ( ( int ) this.zoomPoint.getX(), ( int ) this.zoomPoint.getY() );
            if ( hZoom && vZoom ) {
                final double xmax = Math.min ( e.getX(), scaledDataArea.getMaxX() );
                final double ymax = Math.min ( e.getY(), scaledDataArea.getMaxY() );
                this.zoomRectangle = new Rectangle2D.Double ( this.zoomPoint.getX(), this.zoomPoint.getY(), xmax - this.zoomPoint.getX(), ymax - this.zoomPoint.getY() );
            } else if ( hZoom ) {
                final double xmax = Math.min ( e.getX(), scaledDataArea.getMaxX() );
                this.zoomRectangle = new Rectangle2D.Double ( this.zoomPoint.getX(), scaledDataArea.getMinY(), xmax - this.zoomPoint.getX(), scaledDataArea.getHeight() );
            } else if ( vZoom ) {
                final double ymax2 = Math.min ( e.getY(), scaledDataArea.getMaxY() );
                this.zoomRectangle = new Rectangle2D.Double ( scaledDataArea.getMinX(), this.zoomPoint.getY(), scaledDataArea.getWidth(), ymax2 - this.zoomPoint.getY() );
            }
            if ( this.useBuffer ) {
                this.repaint();
            } else {
                this.drawZoomRectangle ( g2, true );
            }
            g2.dispose();
        }
    }
    @Override
    public void mouseReleased ( final MouseEvent e ) {
        if ( this.panLast != null ) {
            this.panLast = null;
            this.setCursor ( Cursor.getDefaultCursor() );
        } else if ( this.zoomRectangle != null ) {
            boolean hZoom;
            boolean vZoom;
            if ( this.orientation == PlotOrientation.HORIZONTAL ) {
                hZoom = this.rangeZoomable;
                vZoom = this.domainZoomable;
            } else {
                hZoom = this.domainZoomable;
                vZoom = this.rangeZoomable;
            }
            final boolean zoomTrigger1 = hZoom && Math.abs ( e.getX() - this.zoomPoint.getX() ) >= this.zoomTriggerDistance;
            final boolean zoomTrigger2 = vZoom && Math.abs ( e.getY() - this.zoomPoint.getY() ) >= this.zoomTriggerDistance;
            if ( zoomTrigger1 || zoomTrigger2 ) {
                if ( ( hZoom && e.getX() < this.zoomPoint.getX() ) || ( vZoom && e.getY() < this.zoomPoint.getY() ) ) {
                    this.restoreAutoBounds();
                } else {
                    final Rectangle2D screenDataArea = this.getScreenDataArea ( ( int ) this.zoomPoint.getX(), ( int ) this.zoomPoint.getY() );
                    final double maxX = screenDataArea.getMaxX();
                    final double maxY = screenDataArea.getMaxY();
                    double x;
                    double y;
                    double w;
                    double h;
                    if ( !vZoom ) {
                        x = this.zoomPoint.getX();
                        y = screenDataArea.getMinY();
                        w = Math.min ( this.zoomRectangle.getWidth(), maxX - this.zoomPoint.getX() );
                        h = screenDataArea.getHeight();
                    } else if ( !hZoom ) {
                        x = screenDataArea.getMinX();
                        y = this.zoomPoint.getY();
                        w = screenDataArea.getWidth();
                        h = Math.min ( this.zoomRectangle.getHeight(), maxY - this.zoomPoint.getY() );
                    } else {
                        x = this.zoomPoint.getX();
                        y = this.zoomPoint.getY();
                        w = Math.min ( this.zoomRectangle.getWidth(), maxX - this.zoomPoint.getX() );
                        h = Math.min ( this.zoomRectangle.getHeight(), maxY - this.zoomPoint.getY() );
                    }
                    final Rectangle2D zoomArea = new Rectangle2D.Double ( x, y, w, h );
                    this.zoom ( zoomArea );
                }
                this.zoomPoint = null;
                this.zoomRectangle = null;
            } else {
                final Graphics2D g2 = ( Graphics2D ) this.getGraphics();
                if ( this.useBuffer ) {
                    this.repaint();
                } else {
                    this.drawZoomRectangle ( g2, true );
                }
                g2.dispose();
                this.zoomPoint = null;
                this.zoomRectangle = null;
            }
        } else if ( e.isPopupTrigger() && this.popup != null ) {
            this.displayPopupMenu ( e.getX(), e.getY() );
        }
    }
    @Override
    public void mouseClicked ( final MouseEvent event ) {
        final Insets insets = this.getInsets();
        final int x = ( int ) ( ( event.getX() - insets.left ) / this.scaleX );
        final int y = ( int ) ( ( event.getY() - insets.top ) / this.scaleY );
        this.anchor = new Point2D.Double ( x, y );
        if ( this.chart == null ) {
            return;
        }
        this.chart.setNotify ( true );
        final Object[] listeners = this.chartMouseListeners.getListeners ( ChartMouseListener.class );
        if ( listeners.length == 0 ) {
            return;
        }
        ChartEntity entity = null;
        if ( this.info != null ) {
            final EntityCollection entities = this.info.getEntityCollection();
            if ( entities != null ) {
                entity = entities.getEntity ( x, y );
            }
        }
        final ChartMouseEvent chartEvent = new ChartMouseEvent ( this.getChart(), event, entity );
        for ( int i = listeners.length - 1; i >= 0; --i ) {
            ( ( ChartMouseListener ) listeners[i] ).chartMouseClicked ( chartEvent );
        }
    }
    @Override
    public void mouseMoved ( final MouseEvent e ) {
        final Graphics2D g2 = ( Graphics2D ) this.getGraphics();
        if ( this.horizontalAxisTrace ) {
            this.drawHorizontalAxisTrace ( g2, e.getX() );
        }
        if ( this.verticalAxisTrace ) {
            this.drawVerticalAxisTrace ( g2, e.getY() );
        }
        g2.dispose();
        final Object[] listeners = this.chartMouseListeners.getListeners ( ChartMouseListener.class );
        if ( listeners.length == 0 ) {
            return;
        }
        final Insets insets = this.getInsets();
        final int x = ( int ) ( ( e.getX() - insets.left ) / this.scaleX );
        final int y = ( int ) ( ( e.getY() - insets.top ) / this.scaleY );
        ChartEntity entity = null;
        if ( this.info != null ) {
            final EntityCollection entities = this.info.getEntityCollection();
            if ( entities != null ) {
                entity = entities.getEntity ( x, y );
            }
        }
        if ( this.chart != null ) {
            final ChartMouseEvent event = new ChartMouseEvent ( this.getChart(), e, entity );
            for ( int i = listeners.length - 1; i >= 0; --i ) {
                ( ( ChartMouseListener ) listeners[i] ).chartMouseMoved ( event );
            }
        }
    }
    public void zoomInBoth ( final double x, final double y ) {
        final Plot plot = this.chart.getPlot();
        if ( plot == null ) {
            return;
        }
        final boolean savedNotify = plot.isNotify();
        plot.setNotify ( false );
        this.zoomInDomain ( x, y );
        this.zoomInRange ( x, y );
        plot.setNotify ( savedNotify );
    }
    public void zoomInDomain ( final double x, final double y ) {
        final Plot plot = this.chart.getPlot();
        if ( plot instanceof Zoomable ) {
            final boolean savedNotify = plot.isNotify();
            plot.setNotify ( false );
            final Zoomable z = ( Zoomable ) plot;
            z.zoomDomainAxes ( this.zoomInFactor, this.info.getPlotInfo(), this.translateScreenToJava2D ( new Point ( ( int ) x, ( int ) y ) ), this.zoomAroundAnchor );
            plot.setNotify ( savedNotify );
        }
    }
    public void zoomInRange ( final double x, final double y ) {
        final Plot plot = this.chart.getPlot();
        if ( plot instanceof Zoomable ) {
            final boolean savedNotify = plot.isNotify();
            plot.setNotify ( false );
            final Zoomable z = ( Zoomable ) plot;
            z.zoomRangeAxes ( this.zoomInFactor, this.info.getPlotInfo(), this.translateScreenToJava2D ( new Point ( ( int ) x, ( int ) y ) ), this.zoomAroundAnchor );
            plot.setNotify ( savedNotify );
        }
    }
    public void zoomOutBoth ( final double x, final double y ) {
        final Plot plot = this.chart.getPlot();
        if ( plot == null ) {
            return;
        }
        final boolean savedNotify = plot.isNotify();
        plot.setNotify ( false );
        this.zoomOutDomain ( x, y );
        this.zoomOutRange ( x, y );
        plot.setNotify ( savedNotify );
    }
    public void zoomOutDomain ( final double x, final double y ) {
        final Plot plot = this.chart.getPlot();
        if ( plot instanceof Zoomable ) {
            final boolean savedNotify = plot.isNotify();
            plot.setNotify ( false );
            final Zoomable z = ( Zoomable ) plot;
            z.zoomDomainAxes ( this.zoomOutFactor, this.info.getPlotInfo(), this.translateScreenToJava2D ( new Point ( ( int ) x, ( int ) y ) ), this.zoomAroundAnchor );
            plot.setNotify ( savedNotify );
        }
    }
    public void zoomOutRange ( final double x, final double y ) {
        final Plot plot = this.chart.getPlot();
        if ( plot instanceof Zoomable ) {
            final boolean savedNotify = plot.isNotify();
            plot.setNotify ( false );
            final Zoomable z = ( Zoomable ) plot;
            z.zoomRangeAxes ( this.zoomOutFactor, this.info.getPlotInfo(), this.translateScreenToJava2D ( new Point ( ( int ) x, ( int ) y ) ), this.zoomAroundAnchor );
            plot.setNotify ( savedNotify );
        }
    }
    public void zoom ( final Rectangle2D selection ) {
        final Point2D selectOrigin = this.translateScreenToJava2D ( new Point ( ( int ) Math.ceil ( selection.getX() ), ( int ) Math.ceil ( selection.getY() ) ) );
        final PlotRenderingInfo plotInfo = this.info.getPlotInfo();
        final Rectangle2D scaledDataArea = this.getScreenDataArea ( ( int ) selection.getCenterX(), ( int ) selection.getCenterY() );
        if ( selection.getHeight() > 0.0 && selection.getWidth() > 0.0 ) {
            final double hLower = ( selection.getMinX() - scaledDataArea.getMinX() ) / scaledDataArea.getWidth();
            final double hUpper = ( selection.getMaxX() - scaledDataArea.getMinX() ) / scaledDataArea.getWidth();
            final double vLower = ( scaledDataArea.getMaxY() - selection.getMaxY() ) / scaledDataArea.getHeight();
            final double vUpper = ( scaledDataArea.getMaxY() - selection.getMinY() ) / scaledDataArea.getHeight();
            final Plot p = this.chart.getPlot();
            if ( p instanceof Zoomable ) {
                final boolean savedNotify = p.isNotify();
                p.setNotify ( false );
                final Zoomable z = ( Zoomable ) p;
                if ( z.getOrientation() == PlotOrientation.HORIZONTAL ) {
                    z.zoomDomainAxes ( vLower, vUpper, plotInfo, selectOrigin );
                    z.zoomRangeAxes ( hLower, hUpper, plotInfo, selectOrigin );
                } else {
                    z.zoomDomainAxes ( hLower, hUpper, plotInfo, selectOrigin );
                    z.zoomRangeAxes ( vLower, vUpper, plotInfo, selectOrigin );
                }
                p.setNotify ( savedNotify );
            }
        }
    }
    public void restoreAutoBounds() {
        final Plot plot = this.chart.getPlot();
        if ( plot == null ) {
            return;
        }
        final boolean savedNotify = plot.isNotify();
        plot.setNotify ( false );
        this.restoreAutoDomainBounds();
        this.restoreAutoRangeBounds();
        plot.setNotify ( savedNotify );
    }
    public void restoreAutoDomainBounds() {
        final Plot plot = this.chart.getPlot();
        if ( plot instanceof Zoomable ) {
            final Zoomable z = ( Zoomable ) plot;
            final boolean savedNotify = plot.isNotify();
            plot.setNotify ( false );
            final Point2D zp = ( this.zoomPoint != null ) ? this.zoomPoint : new Point();
            z.zoomDomainAxes ( 0.0, this.info.getPlotInfo(), zp );
            plot.setNotify ( savedNotify );
        }
    }
    public void restoreAutoRangeBounds() {
        final Plot plot = this.chart.getPlot();
        if ( plot instanceof Zoomable ) {
            final Zoomable z = ( Zoomable ) plot;
            final boolean savedNotify = plot.isNotify();
            plot.setNotify ( false );
            final Point2D zp = ( this.zoomPoint != null ) ? this.zoomPoint : new Point();
            z.zoomRangeAxes ( 0.0, this.info.getPlotInfo(), zp );
            plot.setNotify ( savedNotify );
        }
    }
    public Rectangle2D getScreenDataArea() {
        final Rectangle2D dataArea = this.info.getPlotInfo().getDataArea();
        final Insets insets = this.getInsets();
        final double x = dataArea.getX() * this.scaleX + insets.left;
        final double y = dataArea.getY() * this.scaleY + insets.top;
        final double w = dataArea.getWidth() * this.scaleX;
        final double h = dataArea.getHeight() * this.scaleY;
        return new Rectangle2D.Double ( x, y, w, h );
    }
    public Rectangle2D getScreenDataArea ( final int x, final int y ) {
        final PlotRenderingInfo plotInfo = this.info.getPlotInfo();
        Rectangle2D result;
        if ( plotInfo.getSubplotCount() == 0 ) {
            result = this.getScreenDataArea();
        } else {
            final Point2D selectOrigin = this.translateScreenToJava2D ( new Point ( x, y ) );
            final int subplotIndex = plotInfo.getSubplotIndex ( selectOrigin );
            if ( subplotIndex == -1 ) {
                return null;
            }
            result = this.scale ( plotInfo.getSubplotInfo ( subplotIndex ).getDataArea() );
        }
        return result;
    }
    public int getInitialDelay() {
        return this.ownToolTipInitialDelay;
    }
    public int getReshowDelay() {
        return this.ownToolTipReshowDelay;
    }
    public int getDismissDelay() {
        return this.ownToolTipDismissDelay;
    }
    public void setInitialDelay ( final int delay ) {
        this.ownToolTipInitialDelay = delay;
    }
    public void setReshowDelay ( final int delay ) {
        this.ownToolTipReshowDelay = delay;
    }
    public void setDismissDelay ( final int delay ) {
        this.ownToolTipDismissDelay = delay;
    }
    public double getZoomInFactor() {
        return this.zoomInFactor;
    }
    public void setZoomInFactor ( final double factor ) {
        this.zoomInFactor = factor;
    }
    public double getZoomOutFactor() {
        return this.zoomOutFactor;
    }
    public void setZoomOutFactor ( final double factor ) {
        this.zoomOutFactor = factor;
    }
    private void drawZoomRectangle ( final Graphics2D g2, final boolean xor ) {
        if ( this.zoomRectangle != null ) {
            if ( xor ) {
                g2.setXORMode ( Color.gray );
            }
            if ( this.fillZoomRectangle ) {
                g2.setPaint ( this.zoomFillPaint );
                g2.fill ( this.zoomRectangle );
            } else {
                g2.setPaint ( this.zoomOutlinePaint );
                g2.draw ( this.zoomRectangle );
            }
            if ( xor ) {
                g2.setPaintMode();
            }
        }
    }
    private void drawHorizontalAxisTrace ( final Graphics2D g2, final int x ) {
        final Rectangle2D dataArea = this.getScreenDataArea();
        g2.setXORMode ( Color.orange );
        if ( ( int ) dataArea.getMinX() < x && x < ( int ) dataArea.getMaxX() ) {
            if ( this.verticalTraceLine != null ) {
                g2.draw ( this.verticalTraceLine );
                this.verticalTraceLine.setLine ( x, ( int ) dataArea.getMinY(), x, ( int ) dataArea.getMaxY() );
            } else {
                this.verticalTraceLine = new Line2D.Float ( x, ( int ) dataArea.getMinY(), x, ( int ) dataArea.getMaxY() );
            }
            g2.draw ( this.verticalTraceLine );
        }
        g2.setPaintMode();
    }
    private void drawVerticalAxisTrace ( final Graphics2D g2, final int y ) {
        final Rectangle2D dataArea = this.getScreenDataArea();
        g2.setXORMode ( Color.orange );
        if ( ( int ) dataArea.getMinY() < y && y < ( int ) dataArea.getMaxY() ) {
            if ( this.horizontalTraceLine != null ) {
                g2.draw ( this.horizontalTraceLine );
                this.horizontalTraceLine.setLine ( ( int ) dataArea.getMinX(), y, ( int ) dataArea.getMaxX(), y );
            } else {
                this.horizontalTraceLine = new Line2D.Float ( ( int ) dataArea.getMinX(), y, ( int ) dataArea.getMaxX(), y );
            }
            g2.draw ( this.horizontalTraceLine );
        }
        g2.setPaintMode();
    }
    public void doEditChartProperties() {
        final ChartEditor editor = ChartEditorManager.getChartEditor ( this.chart );
        final int result = JOptionPane.showConfirmDialog ( this, editor, ChartPanel.localizationResources.getString ( "Chart_Properties" ), 2, -1 );
        if ( result == 0 ) {
            editor.updateChart ( this.chart );
        }
    }
    public void doCopy() {
        final Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        final Insets insets = this.getInsets();
        final int w = this.getWidth() - insets.left - insets.right;
        final int h = this.getHeight() - insets.top - insets.bottom;
        final ChartTransferable selection = new ChartTransferable ( this.chart, w, h, this.getMinimumDrawWidth(), this.getMinimumDrawHeight(), this.getMaximumDrawWidth(), this.getMaximumDrawHeight(), true );
        systemClipboard.setContents ( selection, null );
    }
    public void doSaveAs() throws IOException {
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory ( this.defaultDirectoryForSaveAs );
        final FileNameExtensionFilter filter = new FileNameExtensionFilter ( ChartPanel.localizationResources.getString ( "PNG_Image_Files" ), new String[] { "png" } );
        fileChooser.addChoosableFileFilter ( filter );
        fileChooser.setFileFilter ( filter );
        final int option = fileChooser.showSaveDialog ( this );
        if ( option == 0 ) {
            String filename = fileChooser.getSelectedFile().getPath();
            if ( this.isEnforceFileExtensions() && !filename.endsWith ( ".png" ) ) {
                filename += ".png";
            }
            ChartUtilities.saveChartAsPNG ( new File ( filename ), this.chart, this.getWidth(), this.getHeight() );
        }
    }
    private void saveAsSVG ( final File f ) throws IOException {
        File file = f;
        if ( file == null ) {
            final JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory ( this.defaultDirectoryForSaveAs );
            final FileNameExtensionFilter filter = new FileNameExtensionFilter ( ChartPanel.localizationResources.getString ( "SVG_Files" ), new String[] { "svg" } );
            fileChooser.addChoosableFileFilter ( filter );
            fileChooser.setFileFilter ( filter );
            final int option = fileChooser.showSaveDialog ( this );
            if ( option == 0 ) {
                String filename = fileChooser.getSelectedFile().getPath();
                if ( this.isEnforceFileExtensions() && !filename.endsWith ( ".svg" ) ) {
                    filename += ".svg";
                }
                file = new File ( filename );
                if ( file.exists() ) {
                    final String fileExists = ChartPanel.localizationResources.getString ( "FILE_EXISTS_CONFIRM_OVERWRITE" );
                    final int response = JOptionPane.showConfirmDialog ( this, fileExists, ChartPanel.localizationResources.getString ( "Save_as_SVG" ), 2 );
                    if ( response == 2 ) {
                        file = null;
                    }
                }
            }
        }
        if ( file != null ) {
            final String svg = this.generateSVG ( this.getWidth(), this.getHeight() );
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter ( new FileWriter ( file ) );
                writer.write ( "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" );
                writer.write ( svg + "\n" );
                writer.flush();
            } finally {
                try {
                    if ( writer != null ) {
                        writer.close();
                    }
                } catch ( IOException ex ) {
                    throw new RuntimeException ( ex );
                }
            }
        }
    }
    private String generateSVG ( final int width, final int height ) {
        final Graphics2D g2 = this.createSVGGraphics2D ( width, height );
        if ( g2 == null ) {
            throw new IllegalStateException ( "JFreeSVG library is not present." );
        }
        g2.setRenderingHint ( JFreeChart.KEY_SUPPRESS_SHADOW_GENERATION, true );
        String svg = null;
        final Rectangle2D drawArea = new Rectangle2D.Double ( 0.0, 0.0, width, height );
        this.chart.draw ( g2, drawArea );
        try {
            final Method m = g2.getClass().getMethod ( "getSVGElement", ( Class<?>[] ) new Class[0] );
            svg = ( String ) m.invoke ( g2, new Object[0] );
        } catch ( NoSuchMethodException ex ) {}
        catch ( SecurityException ex2 ) {}
        catch ( IllegalAccessException ex3 ) {}
        catch ( IllegalArgumentException ex4 ) {}
        catch ( InvocationTargetException ex5 ) {}
        return svg;
    }
    private Graphics2D createSVGGraphics2D ( final int w, final int h ) {
        try {
            final Class svgGraphics2d = Class.forName ( "org.jfree.graphics2d.svg.SVGGraphics2D" );
            final Constructor ctor = svgGraphics2d.getConstructor ( Integer.TYPE, Integer.TYPE );
            return ctor.newInstance ( w, h );
        } catch ( ClassNotFoundException ex ) {
            return null;
        } catch ( NoSuchMethodException ex2 ) {
            return null;
        } catch ( SecurityException ex3 ) {
            return null;
        } catch ( InstantiationException ex4 ) {
            return null;
        } catch ( IllegalAccessException ex5 ) {
            return null;
        } catch ( IllegalArgumentException ex6 ) {
            return null;
        } catch ( InvocationTargetException ex7 ) {
            return null;
        }
    }
    private void saveAsPDF ( final File f ) {
        File file = f;
        if ( file == null ) {
            final JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory ( this.defaultDirectoryForSaveAs );
            final FileNameExtensionFilter filter = new FileNameExtensionFilter ( ChartPanel.localizationResources.getString ( "PDF_Files" ), new String[] { "pdf" } );
            fileChooser.addChoosableFileFilter ( filter );
            fileChooser.setFileFilter ( filter );
            final int option = fileChooser.showSaveDialog ( this );
            if ( option == 0 ) {
                String filename = fileChooser.getSelectedFile().getPath();
                if ( this.isEnforceFileExtensions() && !filename.endsWith ( ".pdf" ) ) {
                    filename += ".pdf";
                }
                file = new File ( filename );
                if ( file.exists() ) {
                    final String fileExists = ChartPanel.localizationResources.getString ( "FILE_EXISTS_CONFIRM_OVERWRITE" );
                    final int response = JOptionPane.showConfirmDialog ( this, fileExists, ChartPanel.localizationResources.getString ( "Save_as_PDF" ), 2 );
                    if ( response == 2 ) {
                        file = null;
                    }
                }
            }
        }
        if ( file != null ) {
            this.writeAsPDF ( file, this.getWidth(), this.getHeight() );
        }
    }
    private boolean isOrsonPDFAvailable() {
        Class pdfDocumentClass = null;
        try {
            pdfDocumentClass = Class.forName ( "com.orsonpdf.PDFDocument" );
        } catch ( ClassNotFoundException ex ) {}
        return pdfDocumentClass != null;
    }
    private void writeAsPDF ( final File file, final int w, final int h ) {
        if ( !this.isOrsonPDFAvailable() ) {
            throw new IllegalStateException ( "OrsonPDF is not present on the classpath." );
        }
        ParamChecks.nullNotPermitted ( file, "file" );
        try {
            final Class pdfDocClass = Class.forName ( "com.orsonpdf.PDFDocument" );
            final Object pdfDoc = pdfDocClass.newInstance();
            final Method m = pdfDocClass.getMethod ( "createPage", Rectangle2D.class );
            final Rectangle2D rect = new Rectangle ( w, h );
            final Object page = m.invoke ( pdfDoc, rect );
            final Method m2 = page.getClass().getMethod ( "getGraphics2D", ( Class<?>[] ) new Class[0] );
            final Graphics2D g2 = ( Graphics2D ) m2.invoke ( page, new Object[0] );
            g2.setRenderingHint ( JFreeChart.KEY_SUPPRESS_SHADOW_GENERATION, true );
            final Rectangle2D drawArea = new Rectangle2D.Double ( 0.0, 0.0, w, h );
            this.chart.draw ( g2, drawArea );
            final Method m3 = pdfDocClass.getMethod ( "writeToFile", File.class );
            m3.invoke ( pdfDoc, file );
        } catch ( ClassNotFoundException ex ) {
            throw new RuntimeException ( ex );
        } catch ( InstantiationException ex2 ) {
            throw new RuntimeException ( ex2 );
        } catch ( IllegalAccessException ex3 ) {
            throw new RuntimeException ( ex3 );
        } catch ( NoSuchMethodException ex4 ) {
            throw new RuntimeException ( ex4 );
        } catch ( SecurityException ex5 ) {
            throw new RuntimeException ( ex5 );
        } catch ( IllegalArgumentException ex6 ) {
            throw new RuntimeException ( ex6 );
        } catch ( InvocationTargetException ex7 ) {
            throw new RuntimeException ( ex7 );
        }
    }
    public void createChartPrintJob() {
        final PrinterJob job = PrinterJob.getPrinterJob();
        final PageFormat pf = job.defaultPage();
        final PageFormat pf2 = job.pageDialog ( pf );
        if ( pf2 != pf ) {
            job.setPrintable ( this, pf2 );
            if ( job.printDialog() ) {
                try {
                    job.print();
                } catch ( PrinterException e ) {
                    JOptionPane.showMessageDialog ( this, e );
                }
            }
        }
    }
    @Override
    public int print ( final Graphics g, final PageFormat pf, final int pageIndex ) {
        if ( pageIndex != 0 ) {
            return 1;
        }
        final Graphics2D g2 = ( Graphics2D ) g;
        final double x = pf.getImageableX();
        final double y = pf.getImageableY();
        final double w = pf.getImageableWidth();
        final double h = pf.getImageableHeight();
        this.chart.draw ( g2, new Rectangle2D.Double ( x, y, w, h ), this.anchor, null );
        return 0;
    }
    public void addChartMouseListener ( final ChartMouseListener listener ) {
        ParamChecks.nullNotPermitted ( listener, "listener" );
        this.chartMouseListeners.add ( ChartMouseListener.class, listener );
    }
    public void removeChartMouseListener ( final ChartMouseListener listener ) {
        this.chartMouseListeners.remove ( ChartMouseListener.class, listener );
    }
    @Override
    public EventListener[] getListeners ( final Class listenerType ) {
        if ( listenerType == ChartMouseListener.class ) {
            return this.chartMouseListeners.getListeners ( ( Class<T> ) listenerType );
        }
        return super.getListeners ( ( Class<T> ) listenerType );
    }
    protected JPopupMenu createPopupMenu ( final boolean properties, final boolean save, final boolean print, final boolean zoom ) {
        return this.createPopupMenu ( properties, false, save, print, zoom );
    }
    protected JPopupMenu createPopupMenu ( final boolean properties, final boolean copy, final boolean save, final boolean print, final boolean zoom ) {
        final JPopupMenu result = new JPopupMenu ( ChartPanel.localizationResources.getString ( "Chart" ) + ":" );
        boolean separator = false;
        if ( properties ) {
            final JMenuItem propertiesItem = new JMenuItem ( ChartPanel.localizationResources.getString ( "Properties..." ) );
            propertiesItem.setActionCommand ( "PROPERTIES" );
            propertiesItem.addActionListener ( this );
            result.add ( propertiesItem );
            separator = true;
        }
        if ( copy ) {
            if ( separator ) {
                result.addSeparator();
            }
            final JMenuItem copyItem = new JMenuItem ( ChartPanel.localizationResources.getString ( "Copy" ) );
            copyItem.setActionCommand ( "COPY" );
            copyItem.addActionListener ( this );
            result.add ( copyItem );
            separator = !save;
        }
        if ( save ) {
            if ( separator ) {
                result.addSeparator();
            }
            final JMenu saveSubMenu = new JMenu ( ChartPanel.localizationResources.getString ( "Save_as" ) );
            final JMenuItem pngItem = new JMenuItem ( ChartPanel.localizationResources.getString ( "PNG..." ) );
            pngItem.setActionCommand ( "SAVE_AS_PNG" );
            pngItem.addActionListener ( this );
            saveSubMenu.add ( pngItem );
            if ( this.createSVGGraphics2D ( 10, 10 ) != null ) {
                final JMenuItem svgItem = new JMenuItem ( ChartPanel.localizationResources.getString ( "SVG..." ) );
                svgItem.setActionCommand ( "SAVE_AS_SVG" );
                svgItem.addActionListener ( this );
                saveSubMenu.add ( svgItem );
            }
            if ( this.isOrsonPDFAvailable() ) {
                final JMenuItem pdfItem = new JMenuItem ( ChartPanel.localizationResources.getString ( "PDF..." ) );
                pdfItem.setActionCommand ( "SAVE_AS_PDF" );
                pdfItem.addActionListener ( this );
                saveSubMenu.add ( pdfItem );
            }
            result.add ( saveSubMenu );
            separator = true;
        }
        if ( print ) {
            if ( separator ) {
                result.addSeparator();
            }
            final JMenuItem printItem = new JMenuItem ( ChartPanel.localizationResources.getString ( "Print..." ) );
            printItem.setActionCommand ( "PRINT" );
            printItem.addActionListener ( this );
            result.add ( printItem );
            separator = true;
        }
        if ( zoom ) {
            if ( separator ) {
                result.addSeparator();
            }
            final JMenu zoomInMenu = new JMenu ( ChartPanel.localizationResources.getString ( "Zoom_In" ) );
            ( this.zoomInBothMenuItem = new JMenuItem ( ChartPanel.localizationResources.getString ( "All_Axes" ) ) ).setActionCommand ( "ZOOM_IN_BOTH" );
            this.zoomInBothMenuItem.addActionListener ( this );
            zoomInMenu.add ( this.zoomInBothMenuItem );
            zoomInMenu.addSeparator();
            ( this.zoomInDomainMenuItem = new JMenuItem ( ChartPanel.localizationResources.getString ( "Domain_Axis" ) ) ).setActionCommand ( "ZOOM_IN_DOMAIN" );
            this.zoomInDomainMenuItem.addActionListener ( this );
            zoomInMenu.add ( this.zoomInDomainMenuItem );
            ( this.zoomInRangeMenuItem = new JMenuItem ( ChartPanel.localizationResources.getString ( "Range_Axis" ) ) ).setActionCommand ( "ZOOM_IN_RANGE" );
            this.zoomInRangeMenuItem.addActionListener ( this );
            zoomInMenu.add ( this.zoomInRangeMenuItem );
            result.add ( zoomInMenu );
            final JMenu zoomOutMenu = new JMenu ( ChartPanel.localizationResources.getString ( "Zoom_Out" ) );
            ( this.zoomOutBothMenuItem = new JMenuItem ( ChartPanel.localizationResources.getString ( "All_Axes" ) ) ).setActionCommand ( "ZOOM_OUT_BOTH" );
            this.zoomOutBothMenuItem.addActionListener ( this );
            zoomOutMenu.add ( this.zoomOutBothMenuItem );
            zoomOutMenu.addSeparator();
            ( this.zoomOutDomainMenuItem = new JMenuItem ( ChartPanel.localizationResources.getString ( "Domain_Axis" ) ) ).setActionCommand ( "ZOOM_DOMAIN_BOTH" );
            this.zoomOutDomainMenuItem.addActionListener ( this );
            zoomOutMenu.add ( this.zoomOutDomainMenuItem );
            ( this.zoomOutRangeMenuItem = new JMenuItem ( ChartPanel.localizationResources.getString ( "Range_Axis" ) ) ).setActionCommand ( "ZOOM_RANGE_BOTH" );
            this.zoomOutRangeMenuItem.addActionListener ( this );
            zoomOutMenu.add ( this.zoomOutRangeMenuItem );
            result.add ( zoomOutMenu );
            final JMenu autoRangeMenu = new JMenu ( ChartPanel.localizationResources.getString ( "Auto_Range" ) );
            ( this.zoomResetBothMenuItem = new JMenuItem ( ChartPanel.localizationResources.getString ( "All_Axes" ) ) ).setActionCommand ( "ZOOM_RESET_BOTH" );
            this.zoomResetBothMenuItem.addActionListener ( this );
            autoRangeMenu.add ( this.zoomResetBothMenuItem );
            autoRangeMenu.addSeparator();
            ( this.zoomResetDomainMenuItem = new JMenuItem ( ChartPanel.localizationResources.getString ( "Domain_Axis" ) ) ).setActionCommand ( "ZOOM_RESET_DOMAIN" );
            this.zoomResetDomainMenuItem.addActionListener ( this );
            autoRangeMenu.add ( this.zoomResetDomainMenuItem );
            ( this.zoomResetRangeMenuItem = new JMenuItem ( ChartPanel.localizationResources.getString ( "Range_Axis" ) ) ).setActionCommand ( "ZOOM_RESET_RANGE" );
            this.zoomResetRangeMenuItem.addActionListener ( this );
            autoRangeMenu.add ( this.zoomResetRangeMenuItem );
            result.addSeparator();
            result.add ( autoRangeMenu );
        }
        return result;
    }
    protected void displayPopupMenu ( final int x, final int y ) {
        if ( this.popup == null ) {
            return;
        }
        boolean isDomainZoomable = false;
        boolean isRangeZoomable = false;
        final Plot plot = ( this.chart != null ) ? this.chart.getPlot() : null;
        if ( plot instanceof Zoomable ) {
            final Zoomable z = ( Zoomable ) plot;
            isDomainZoomable = z.isDomainZoomable();
            isRangeZoomable = z.isRangeZoomable();
        }
        if ( this.zoomInDomainMenuItem != null ) {
            this.zoomInDomainMenuItem.setEnabled ( isDomainZoomable );
        }
        if ( this.zoomOutDomainMenuItem != null ) {
            this.zoomOutDomainMenuItem.setEnabled ( isDomainZoomable );
        }
        if ( this.zoomResetDomainMenuItem != null ) {
            this.zoomResetDomainMenuItem.setEnabled ( isDomainZoomable );
        }
        if ( this.zoomInRangeMenuItem != null ) {
            this.zoomInRangeMenuItem.setEnabled ( isRangeZoomable );
        }
        if ( this.zoomOutRangeMenuItem != null ) {
            this.zoomOutRangeMenuItem.setEnabled ( isRangeZoomable );
        }
        if ( this.zoomResetRangeMenuItem != null ) {
            this.zoomResetRangeMenuItem.setEnabled ( isRangeZoomable );
        }
        if ( this.zoomInBothMenuItem != null ) {
            this.zoomInBothMenuItem.setEnabled ( isDomainZoomable && isRangeZoomable );
        }
        if ( this.zoomOutBothMenuItem != null ) {
            this.zoomOutBothMenuItem.setEnabled ( isDomainZoomable && isRangeZoomable );
        }
        if ( this.zoomResetBothMenuItem != null ) {
            this.zoomResetBothMenuItem.setEnabled ( isDomainZoomable && isRangeZoomable );
        }
        this.popup.show ( this, x, y );
    }
    @Override
    public void updateUI() {
        if ( this.popup != null ) {
            SwingUtilities.updateComponentTreeUI ( this.popup );
        }
        super.updateUI();
    }
    private void writeObject ( final ObjectOutputStream stream ) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint ( this.zoomFillPaint, stream );
        SerialUtilities.writePaint ( this.zoomOutlinePaint, stream );
    }
    private void readObject ( final ObjectInputStream stream ) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.zoomFillPaint = SerialUtilities.readPaint ( stream );
        this.zoomOutlinePaint = SerialUtilities.readPaint ( stream );
        this.chartMouseListeners = new EventListenerList();
        if ( this.chart != null ) {
            this.chart.addChangeListener ( this );
        }
    }
    static {
        ChartPanel.localizationResources = ResourceBundleWrapper.getBundle ( "org.jfree.chart.LocalizationBundle" );
    }
}
