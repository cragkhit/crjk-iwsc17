package org.jfree.chart.panel;
import org.jfree.util.ObjectUtilities;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import org.jfree.ui.RectangleAnchor;
import java.awt.Stroke;
import java.awt.Paint;
import org.jfree.text.TextUtilities;
import org.jfree.ui.TextAnchor;
import java.awt.geom.Line2D;
import java.util.Iterator;
import org.jfree.ui.RectangleEdge;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.JFreeChart;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import java.awt.Shape;
import org.jfree.chart.ChartPanel;
import java.awt.Graphics2D;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import org.jfree.chart.util.ParamChecks;
import org.jfree.chart.plot.Crosshair;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
import java.beans.PropertyChangeListener;
public class CrosshairOverlay extends AbstractOverlay implements Overlay, PropertyChangeListener, PublicCloneable, Cloneable, Serializable {
    private List xCrosshairs;
    private List yCrosshairs;
    public CrosshairOverlay() {
        this.xCrosshairs = new ArrayList();
        this.yCrosshairs = new ArrayList();
    }
    public void addDomainCrosshair ( final Crosshair crosshair ) {
        ParamChecks.nullNotPermitted ( crosshair, "crosshair" );
        this.xCrosshairs.add ( crosshair );
        crosshair.addPropertyChangeListener ( this );
        this.fireOverlayChanged();
    }
    public void removeDomainCrosshair ( final Crosshair crosshair ) {
        ParamChecks.nullNotPermitted ( crosshair, "crosshair" );
        if ( this.xCrosshairs.remove ( crosshair ) ) {
            crosshair.removePropertyChangeListener ( this );
            this.fireOverlayChanged();
        }
    }
    public void clearDomainCrosshairs() {
        if ( this.xCrosshairs.isEmpty() ) {
            return;
        }
        final List crosshairs = this.getDomainCrosshairs();
        for ( int i = 0; i < crosshairs.size(); ++i ) {
            final Crosshair c = crosshairs.get ( i );
            this.xCrosshairs.remove ( c );
            c.removePropertyChangeListener ( this );
        }
        this.fireOverlayChanged();
    }
    public List getDomainCrosshairs() {
        return new ArrayList ( this.xCrosshairs );
    }
    public void addRangeCrosshair ( final Crosshair crosshair ) {
        ParamChecks.nullNotPermitted ( crosshair, "crosshair" );
        this.yCrosshairs.add ( crosshair );
        crosshair.addPropertyChangeListener ( this );
        this.fireOverlayChanged();
    }
    public void removeRangeCrosshair ( final Crosshair crosshair ) {
        ParamChecks.nullNotPermitted ( crosshair, "crosshair" );
        if ( this.yCrosshairs.remove ( crosshair ) ) {
            crosshair.removePropertyChangeListener ( this );
            this.fireOverlayChanged();
        }
    }
    public void clearRangeCrosshairs() {
        if ( this.yCrosshairs.isEmpty() ) {
            return;
        }
        final List crosshairs = this.getRangeCrosshairs();
        for ( int i = 0; i < crosshairs.size(); ++i ) {
            final Crosshair c = crosshairs.get ( i );
            this.yCrosshairs.remove ( c );
            c.removePropertyChangeListener ( this );
        }
        this.fireOverlayChanged();
    }
    public List getRangeCrosshairs() {
        return new ArrayList ( this.yCrosshairs );
    }
    @Override
    public void propertyChange ( final PropertyChangeEvent e ) {
        this.fireOverlayChanged();
    }
    @Override
    public void paintOverlay ( final Graphics2D g2, final ChartPanel chartPanel ) {
        final Shape savedClip = g2.getClip();
        final Rectangle2D dataArea = chartPanel.getScreenDataArea();
        g2.clip ( dataArea );
        final JFreeChart chart = chartPanel.getChart();
        final XYPlot plot = ( XYPlot ) chart.getPlot();
        final ValueAxis xAxis = plot.getDomainAxis();
        final RectangleEdge xAxisEdge = plot.getDomainAxisEdge();
        for ( final Crosshair ch : this.xCrosshairs ) {
            if ( ch.isVisible() ) {
                final double x = ch.getValue();
                final double xx = xAxis.valueToJava2D ( x, dataArea, xAxisEdge );
                if ( plot.getOrientation() == PlotOrientation.VERTICAL ) {
                    this.drawVerticalCrosshair ( g2, dataArea, xx, ch );
                } else {
                    this.drawHorizontalCrosshair ( g2, dataArea, xx, ch );
                }
            }
        }
        final ValueAxis yAxis = plot.getRangeAxis();
        final RectangleEdge yAxisEdge = plot.getRangeAxisEdge();
        for ( final Crosshair ch2 : this.yCrosshairs ) {
            if ( ch2.isVisible() ) {
                final double y = ch2.getValue();
                final double yy = yAxis.valueToJava2D ( y, dataArea, yAxisEdge );
                if ( plot.getOrientation() == PlotOrientation.VERTICAL ) {
                    this.drawHorizontalCrosshair ( g2, dataArea, yy, ch2 );
                } else {
                    this.drawVerticalCrosshair ( g2, dataArea, yy, ch2 );
                }
            }
        }
        g2.setClip ( savedClip );
    }
    protected void drawHorizontalCrosshair ( final Graphics2D g2, final Rectangle2D dataArea, final double y, final Crosshair crosshair ) {
        if ( y >= dataArea.getMinY() && y <= dataArea.getMaxY() ) {
            final Line2D line = new Line2D.Double ( dataArea.getMinX(), y, dataArea.getMaxX(), y );
            final Paint savedPaint = g2.getPaint();
            final Stroke savedStroke = g2.getStroke();
            g2.setPaint ( crosshair.getPaint() );
            g2.setStroke ( crosshair.getStroke() );
            g2.draw ( line );
            if ( crosshair.isLabelVisible() ) {
                final String label = crosshair.getLabelGenerator().generateLabel ( crosshair );
                RectangleAnchor anchor = crosshair.getLabelAnchor();
                Point2D pt = this.calculateLabelPoint ( line, anchor, 5.0, 5.0 );
                float xx = ( float ) pt.getX();
                float yy = ( float ) pt.getY();
                TextAnchor alignPt = this.textAlignPtForLabelAnchorH ( anchor );
                Shape hotspot = TextUtilities.calculateRotatedStringBounds ( label, g2, xx, yy, alignPt, 0.0, TextAnchor.CENTER );
                if ( !dataArea.contains ( hotspot.getBounds2D() ) ) {
                    anchor = this.flipAnchorV ( anchor );
                    pt = this.calculateLabelPoint ( line, anchor, 5.0, 5.0 );
                    xx = ( float ) pt.getX();
                    yy = ( float ) pt.getY();
                    alignPt = this.textAlignPtForLabelAnchorH ( anchor );
                    hotspot = TextUtilities.calculateRotatedStringBounds ( label, g2, xx, yy, alignPt, 0.0, TextAnchor.CENTER );
                }
                g2.setPaint ( crosshair.getLabelBackgroundPaint() );
                g2.fill ( hotspot );
                g2.setPaint ( crosshair.getLabelOutlinePaint() );
                g2.setStroke ( crosshair.getLabelOutlineStroke() );
                g2.draw ( hotspot );
                TextUtilities.drawAlignedString ( label, g2, xx, yy, alignPt );
            }
            g2.setPaint ( savedPaint );
            g2.setStroke ( savedStroke );
        }
    }
    protected void drawVerticalCrosshair ( final Graphics2D g2, final Rectangle2D dataArea, final double x, final Crosshair crosshair ) {
        if ( x >= dataArea.getMinX() && x <= dataArea.getMaxX() ) {
            final Line2D line = new Line2D.Double ( x, dataArea.getMinY(), x, dataArea.getMaxY() );
            final Paint savedPaint = g2.getPaint();
            final Stroke savedStroke = g2.getStroke();
            g2.setPaint ( crosshair.getPaint() );
            g2.setStroke ( crosshair.getStroke() );
            g2.draw ( line );
            if ( crosshair.isLabelVisible() ) {
                final String label = crosshair.getLabelGenerator().generateLabel ( crosshair );
                RectangleAnchor anchor = crosshair.getLabelAnchor();
                Point2D pt = this.calculateLabelPoint ( line, anchor, 5.0, 5.0 );
                float xx = ( float ) pt.getX();
                float yy = ( float ) pt.getY();
                TextAnchor alignPt = this.textAlignPtForLabelAnchorV ( anchor );
                Shape hotspot = TextUtilities.calculateRotatedStringBounds ( label, g2, xx, yy, alignPt, 0.0, TextAnchor.CENTER );
                if ( !dataArea.contains ( hotspot.getBounds2D() ) ) {
                    anchor = this.flipAnchorH ( anchor );
                    pt = this.calculateLabelPoint ( line, anchor, 5.0, 5.0 );
                    xx = ( float ) pt.getX();
                    yy = ( float ) pt.getY();
                    alignPt = this.textAlignPtForLabelAnchorV ( anchor );
                    hotspot = TextUtilities.calculateRotatedStringBounds ( label, g2, xx, yy, alignPt, 0.0, TextAnchor.CENTER );
                }
                g2.setPaint ( crosshair.getLabelBackgroundPaint() );
                g2.fill ( hotspot );
                g2.setPaint ( crosshair.getLabelOutlinePaint() );
                g2.setStroke ( crosshair.getLabelOutlineStroke() );
                g2.draw ( hotspot );
                TextUtilities.drawAlignedString ( label, g2, xx, yy, alignPt );
            }
            g2.setPaint ( savedPaint );
            g2.setStroke ( savedStroke );
        }
    }
    private Point2D calculateLabelPoint ( final Line2D line, final RectangleAnchor anchor, final double deltaX, final double deltaY ) {
        final boolean left = anchor == RectangleAnchor.BOTTOM_LEFT || anchor == RectangleAnchor.LEFT || anchor == RectangleAnchor.TOP_LEFT;
        final boolean right = anchor == RectangleAnchor.BOTTOM_RIGHT || anchor == RectangleAnchor.RIGHT || anchor == RectangleAnchor.TOP_RIGHT;
        final boolean top = anchor == RectangleAnchor.TOP_LEFT || anchor == RectangleAnchor.TOP || anchor == RectangleAnchor.TOP_RIGHT;
        final boolean bottom = anchor == RectangleAnchor.BOTTOM_LEFT || anchor == RectangleAnchor.BOTTOM || anchor == RectangleAnchor.BOTTOM_RIGHT;
        final Rectangle rect = line.getBounds();
        double x;
        double y;
        if ( line.getX1() == line.getX2() ) {
            x = line.getX1();
            y = ( line.getY1() + line.getY2() ) / 2.0;
            if ( left ) {
                x -= deltaX;
            }
            if ( right ) {
                x += deltaX;
            }
            if ( top ) {
                y = Math.min ( line.getY1(), line.getY2() ) + deltaY;
            }
            if ( bottom ) {
                y = Math.max ( line.getY1(), line.getY2() ) - deltaY;
            }
        } else {
            x = ( line.getX1() + line.getX2() ) / 2.0;
            y = line.getY1();
            if ( left ) {
                x = Math.min ( line.getX1(), line.getX2() ) + deltaX;
            }
            if ( right ) {
                x = Math.max ( line.getX1(), line.getX2() ) - deltaX;
            }
            if ( top ) {
                y -= deltaY;
            }
            if ( bottom ) {
                y += deltaY;
            }
        }
        return new Point2D.Double ( x, y );
    }
    private TextAnchor textAlignPtForLabelAnchorV ( final RectangleAnchor anchor ) {
        TextAnchor result = TextAnchor.CENTER;
        if ( anchor.equals ( ( Object ) RectangleAnchor.TOP_LEFT ) ) {
            result = TextAnchor.TOP_RIGHT;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.TOP ) ) {
            result = TextAnchor.TOP_CENTER;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.TOP_RIGHT ) ) {
            result = TextAnchor.TOP_LEFT;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.LEFT ) ) {
            result = TextAnchor.HALF_ASCENT_RIGHT;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.RIGHT ) ) {
            result = TextAnchor.HALF_ASCENT_LEFT;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.BOTTOM_LEFT ) ) {
            result = TextAnchor.BOTTOM_RIGHT;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.BOTTOM ) ) {
            result = TextAnchor.BOTTOM_CENTER;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.BOTTOM_RIGHT ) ) {
            result = TextAnchor.BOTTOM_LEFT;
        }
        return result;
    }
    private TextAnchor textAlignPtForLabelAnchorH ( final RectangleAnchor anchor ) {
        TextAnchor result = TextAnchor.CENTER;
        if ( anchor.equals ( ( Object ) RectangleAnchor.TOP_LEFT ) ) {
            result = TextAnchor.BOTTOM_LEFT;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.TOP ) ) {
            result = TextAnchor.BOTTOM_CENTER;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.TOP_RIGHT ) ) {
            result = TextAnchor.BOTTOM_RIGHT;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.LEFT ) ) {
            result = TextAnchor.HALF_ASCENT_LEFT;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.RIGHT ) ) {
            result = TextAnchor.HALF_ASCENT_RIGHT;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.BOTTOM_LEFT ) ) {
            result = TextAnchor.TOP_LEFT;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.BOTTOM ) ) {
            result = TextAnchor.TOP_CENTER;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.BOTTOM_RIGHT ) ) {
            result = TextAnchor.TOP_RIGHT;
        }
        return result;
    }
    private RectangleAnchor flipAnchorH ( final RectangleAnchor anchor ) {
        RectangleAnchor result = anchor;
        if ( anchor.equals ( ( Object ) RectangleAnchor.TOP_LEFT ) ) {
            result = RectangleAnchor.TOP_RIGHT;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.TOP_RIGHT ) ) {
            result = RectangleAnchor.TOP_LEFT;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.LEFT ) ) {
            result = RectangleAnchor.RIGHT;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.RIGHT ) ) {
            result = RectangleAnchor.LEFT;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.BOTTOM_LEFT ) ) {
            result = RectangleAnchor.BOTTOM_RIGHT;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.BOTTOM_RIGHT ) ) {
            result = RectangleAnchor.BOTTOM_LEFT;
        }
        return result;
    }
    private RectangleAnchor flipAnchorV ( final RectangleAnchor anchor ) {
        RectangleAnchor result = anchor;
        if ( anchor.equals ( ( Object ) RectangleAnchor.TOP_LEFT ) ) {
            result = RectangleAnchor.BOTTOM_LEFT;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.TOP_RIGHT ) ) {
            result = RectangleAnchor.BOTTOM_RIGHT;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.TOP ) ) {
            result = RectangleAnchor.BOTTOM;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.BOTTOM ) ) {
            result = RectangleAnchor.TOP;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.BOTTOM_LEFT ) ) {
            result = RectangleAnchor.TOP_LEFT;
        } else if ( anchor.equals ( ( Object ) RectangleAnchor.BOTTOM_RIGHT ) ) {
            result = RectangleAnchor.TOP_RIGHT;
        }
        return result;
    }
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof CrosshairOverlay ) ) {
            return false;
        }
        final CrosshairOverlay that = ( CrosshairOverlay ) obj;
        return this.xCrosshairs.equals ( that.xCrosshairs ) && this.yCrosshairs.equals ( that.yCrosshairs );
    }
    public Object clone() throws CloneNotSupportedException {
        final CrosshairOverlay clone = ( CrosshairOverlay ) super.clone();
        clone.xCrosshairs = ( List ) ObjectUtilities.deepClone ( ( Collection ) this.xCrosshairs );
        clone.yCrosshairs = ( List ) ObjectUtilities.deepClone ( ( Collection ) this.yCrosshairs );
        return clone;
    }
}
