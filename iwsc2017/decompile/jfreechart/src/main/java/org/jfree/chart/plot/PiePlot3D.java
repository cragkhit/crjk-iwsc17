package org.jfree.chart.plot;
import org.jfree.chart.util.PaintAlpha;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.entity.EntityCollection;
import java.awt.Stroke;
import java.util.Iterator;
import java.util.List;
import java.awt.FontMetrics;
import org.jfree.ui.RectangleInsets;
import java.awt.image.ImageObserver;
import java.awt.Image;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.PieSectionEntity;
import java.awt.Paint;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.awt.Composite;
import java.awt.AlphaComposite;
import java.awt.Font;
import org.jfree.data.general.DatasetUtilities;
import java.util.Map;
import java.awt.image.BufferedImage;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import org.jfree.data.general.PieDataset;
import java.io.Serializable;
public class PiePlot3D extends PiePlot implements Serializable {
    private static final long serialVersionUID = 3408984188945161432L;
    private double depthFactor;
    private boolean darkerSides;
    public PiePlot3D() {
        this ( null );
    }
    public PiePlot3D ( final PieDataset dataset ) {
        super ( dataset );
        this.depthFactor = 0.12;
        this.setCircular ( this.darkerSides = false, false );
    }
    public double getDepthFactor() {
        return this.depthFactor;
    }
    public void setDepthFactor ( final double factor ) {
        this.depthFactor = factor;
        this.fireChangeEvent();
    }
    public boolean getDarkerSides() {
        return this.darkerSides;
    }
    public void setDarkerSides ( final boolean darker ) {
        this.darkerSides = darker;
        this.fireChangeEvent();
    }
    @Override
    public void draw ( Graphics2D g2, Rectangle2D plotArea, final Point2D anchor, final PlotState parentState, final PlotRenderingInfo info ) {
        final RectangleInsets insets = this.getInsets();
        insets.trim ( plotArea );
        Rectangle2D originalPlotArea = ( Rectangle2D ) plotArea.clone();
        if ( info != null ) {
            info.setPlotArea ( plotArea );
            info.setDataArea ( plotArea );
        }
        this.drawBackground ( g2, plotArea );
        final Shape savedClip = g2.getClip();
        g2.clip ( plotArea );
        final Graphics2D savedG2 = g2;
        BufferedImage dataImage = null;
        if ( this.getShadowGenerator() != null ) {
            dataImage = new BufferedImage ( ( int ) plotArea.getWidth(), ( int ) plotArea.getHeight(), 2 );
            g2 = dataImage.createGraphics();
            g2.translate ( -plotArea.getX(), -plotArea.getY() );
            g2.setRenderingHints ( savedG2.getRenderingHints() );
            originalPlotArea = ( Rectangle2D ) plotArea.clone();
        }
        final double gapPercent = this.getInteriorGap();
        double labelPercent = 0.0;
        if ( this.getLabelGenerator() != null ) {
            labelPercent = this.getLabelGap() + this.getMaximumLabelWidth();
        }
        final double gapHorizontal = plotArea.getWidth() * ( gapPercent + labelPercent ) * 2.0;
        final double gapVertical = plotArea.getHeight() * gapPercent * 2.0;
        double linkX = plotArea.getX() + gapHorizontal / 2.0;
        double linkY = plotArea.getY() + gapVertical / 2.0;
        double linkW = plotArea.getWidth() - gapHorizontal;
        double linkH = plotArea.getHeight() - gapVertical;
        if ( this.isCircular() ) {
            final double min = Math.min ( linkW, linkH ) / 2.0;
            linkX = ( linkX + linkX + linkW ) / 2.0 - min;
            linkY = ( linkY + linkY + linkH ) / 2.0 - min;
            linkW = 2.0 * min;
            linkH = 2.0 * min;
        }
        final PiePlotState state = this.initialise ( g2, plotArea, this, null, info );
        final Rectangle2D linkAreaXX = new Rectangle2D.Double ( linkX, linkY, linkW, linkH * ( 1.0 - this.depthFactor ) );
        state.setLinkArea ( linkAreaXX );
        final double hh = linkW * this.getLabelLinkMargin();
        final double vv = linkH * this.getLabelLinkMargin();
        final Rectangle2D explodeArea = new Rectangle2D.Double ( linkX + hh / 2.0, linkY + vv / 2.0, linkW - hh, linkH - vv );
        state.setExplodedPieArea ( explodeArea );
        final double maximumExplodePercent = this.getMaximumExplodePercent();
        final double percent = maximumExplodePercent / ( 1.0 + maximumExplodePercent );
        final double h1 = explodeArea.getWidth() * percent;
        final double v1 = explodeArea.getHeight() * percent;
        final Rectangle2D pieArea = new Rectangle2D.Double ( explodeArea.getX() + h1 / 2.0, explodeArea.getY() + v1 / 2.0, explodeArea.getWidth() - h1, explodeArea.getHeight() - v1 );
        final int depth = ( int ) ( pieArea.getHeight() * this.depthFactor );
        final Rectangle2D linkArea = new Rectangle2D.Double ( linkX, linkY, linkW, linkH - depth );
        state.setLinkArea ( linkArea );
        state.setPieArea ( pieArea );
        state.setPieCenterX ( pieArea.getCenterX() );
        state.setPieCenterY ( pieArea.getCenterY() - depth / 2.0 );
        state.setPieWRadius ( pieArea.getWidth() / 2.0 );
        state.setPieHRadius ( ( pieArea.getHeight() - depth ) / 2.0 );
        final PieDataset dataset = this.getDataset();
        if ( DatasetUtilities.isEmptyOrNull ( this.getDataset() ) ) {
            this.drawNoDataMessage ( g2, plotArea );
            g2.setClip ( savedClip );
            this.drawOutline ( g2, plotArea );
            return;
        }
        if ( dataset.getKeys().size() > plotArea.getWidth() ) {
            final String text = PiePlot3D.localizationResources.getString ( "Too_many_elements" );
            final Font sfont = new Font ( "dialog", 1, 10 );
            g2.setFont ( sfont );
            final FontMetrics fm = g2.getFontMetrics ( sfont );
            final int stringWidth = fm.stringWidth ( text );
            g2.drawString ( text, ( int ) ( plotArea.getX() + ( plotArea.getWidth() - stringWidth ) / 2.0 ), ( int ) ( plotArea.getY() + plotArea.getHeight() / 2.0 ) );
            return;
        }
        if ( this.isCircular() ) {
            final double min2 = Math.min ( plotArea.getWidth(), plotArea.getHeight() ) / 2.0;
            plotArea = new Rectangle2D.Double ( plotArea.getCenterX() - min2, plotArea.getCenterY() - min2, 2.0 * min2, 2.0 * min2 );
        }
        final List sectionKeys = dataset.getKeys();
        if ( sectionKeys.isEmpty() ) {
            return;
        }
        final double arcX = pieArea.getX();
        final double arcY = pieArea.getY();
        final Composite originalComposite = g2.getComposite();
        g2.setComposite ( AlphaComposite.getInstance ( 3, this.getForegroundAlpha() ) );
        final double totalValue = DatasetUtilities.calculatePieDatasetTotal ( dataset );
        double runningTotal = 0.0;
        if ( depth < 0 ) {
            return;
        }
        final ArrayList arcList = new ArrayList();
        for ( final Comparable currentKey : sectionKeys ) {
            final Number dataValue = dataset.getValue ( currentKey );
            if ( dataValue == null ) {
                arcList.add ( null );
            } else {
                final double value = dataValue.doubleValue();
                if ( value <= 0.0 ) {
                    arcList.add ( null );
                } else {
                    final double startAngle = this.getStartAngle();
                    final double direction = this.getDirection().getFactor();
                    final double angle1 = startAngle + direction * ( runningTotal * 360.0 ) / totalValue;
                    final double angle2 = startAngle + direction * ( runningTotal + value ) * 360.0 / totalValue;
                    if ( Math.abs ( angle2 - angle1 ) > this.getMinimumArcAngleToDraw() ) {
                        arcList.add ( new Arc2D.Double ( arcX, arcY + depth, pieArea.getWidth(), pieArea.getHeight() - depth, angle1, angle2 - angle1, 2 ) );
                    } else {
                        arcList.add ( null );
                    }
                    runningTotal += value;
                }
            }
        }
        final Shape oldClip = g2.getClip();
        final Ellipse2D top = new Ellipse2D.Double ( pieArea.getX(), pieArea.getY(), pieArea.getWidth(), pieArea.getHeight() - depth );
        final Ellipse2D bottom = new Ellipse2D.Double ( pieArea.getX(), pieArea.getY() + depth, pieArea.getWidth(), pieArea.getHeight() - depth );
        final Rectangle2D lower = new Rectangle2D.Double ( top.getX(), top.getCenterY(), pieArea.getWidth(), bottom.getMaxY() - top.getCenterY() );
        final Rectangle2D upper = new Rectangle2D.Double ( pieArea.getX(), top.getY(), pieArea.getWidth(), bottom.getCenterY() - top.getY() );
        final Area a = new Area ( top );
        a.add ( new Area ( lower ) );
        final Area b = new Area ( bottom );
        b.add ( new Area ( upper ) );
        final Area pie = new Area ( a );
        pie.intersect ( b );
        final Area front = new Area ( pie );
        front.subtract ( new Area ( top ) );
        final Area back = new Area ( pie );
        back.subtract ( new Area ( bottom ) );
        final int categoryCount = arcList.size();
        for ( int categoryIndex = 0; categoryIndex < categoryCount; ++categoryIndex ) {
            final Arc2D.Double arc = arcList.get ( categoryIndex );
            if ( arc != null ) {
                final Comparable key = this.getSectionKey ( categoryIndex );
                final Paint paint = this.lookupSectionPaint ( key );
                final Paint outlinePaint = this.lookupSectionOutlinePaint ( key );
                final Stroke outlineStroke = this.lookupSectionOutlineStroke ( key );
                g2.setPaint ( paint );
                g2.fill ( arc );
                g2.setPaint ( outlinePaint );
                g2.setStroke ( outlineStroke );
                g2.draw ( arc );
                g2.setPaint ( paint );
                final Point2D p1 = arc.getStartPoint();
                final int[] xs = { ( int ) arc.getCenterX(), ( int ) arc.getCenterX(), ( int ) p1.getX(), ( int ) p1.getX() };
                final int[] ys = { ( int ) arc.getCenterY(), ( int ) arc.getCenterY() - depth, ( int ) p1.getY() - depth, ( int ) p1.getY() };
                final Polygon polygon = new Polygon ( xs, ys, 4 );
                g2.setPaint ( Color.lightGray );
                g2.fill ( polygon );
                g2.setPaint ( outlinePaint );
                g2.setStroke ( outlineStroke );
                g2.draw ( polygon );
                g2.setPaint ( paint );
            }
        }
        g2.setPaint ( Color.gray );
        g2.fill ( back );
        g2.fill ( front );
        int cat = 0;
        for ( final Arc2D segment : arcList ) {
            if ( segment != null ) {
                final Comparable key2 = this.getSectionKey ( cat );
                final Paint paint = this.lookupSectionPaint ( key2 );
                final Paint outlinePaint = this.lookupSectionOutlinePaint ( key2 );
                final Stroke outlineStroke = this.lookupSectionOutlineStroke ( key2 );
                this.drawSide ( g2, pieArea, segment, front, back, paint, outlinePaint, outlineStroke, false, true );
            }
            ++cat;
        }
        cat = 0;
        for ( final Arc2D segment : arcList ) {
            if ( segment != null ) {
                final Comparable key2 = this.getSectionKey ( cat );
                final Paint paint = this.lookupSectionPaint ( key2 );
                final Paint outlinePaint = this.lookupSectionOutlinePaint ( key2 );
                final Stroke outlineStroke = this.lookupSectionOutlineStroke ( key2 );
                this.drawSide ( g2, pieArea, segment, front, back, paint, outlinePaint, outlineStroke, true, false );
            }
            ++cat;
        }
        g2.setClip ( oldClip );
        for ( int sectionIndex = 0; sectionIndex < categoryCount; ++sectionIndex ) {
            final Arc2D.Double arc = arcList.get ( sectionIndex );
            if ( arc != null ) {
                final Arc2D upperArc = new Arc2D.Double ( arcX, arcY, pieArea.getWidth(), pieArea.getHeight() - depth, arc.getAngleStart(), arc.getAngleExtent(), 2 );
                final Comparable currentKey2 = sectionKeys.get ( sectionIndex );
                final Paint paint = this.lookupSectionPaint ( currentKey2, true );
                final Paint outlinePaint = this.lookupSectionOutlinePaint ( currentKey2 );
                final Stroke outlineStroke = this.lookupSectionOutlineStroke ( currentKey2 );
                g2.setPaint ( paint );
                g2.fill ( upperArc );
                g2.setStroke ( outlineStroke );
                g2.setPaint ( outlinePaint );
                g2.draw ( upperArc );
                if ( info != null ) {
                    final EntityCollection entities = info.getOwner().getEntityCollection();
                    if ( entities != null ) {
                        String tip = null;
                        final PieToolTipGenerator tipster = this.getToolTipGenerator();
                        if ( tipster != null ) {
                            tip = tipster.generateToolTip ( dataset, currentKey2 );
                        }
                        String url = null;
                        if ( this.getURLGenerator() != null ) {
                            url = this.getURLGenerator().generateURL ( dataset, currentKey2, this.getPieIndex() );
                        }
                        final PieSectionEntity entity = new PieSectionEntity ( upperArc, dataset, this.getPieIndex(), sectionIndex, currentKey2, tip, url );
                        entities.add ( entity );
                    }
                }
            }
        }
        final List keys = dataset.getKeys();
        final Rectangle2D adjustedPlotArea = new Rectangle2D.Double ( originalPlotArea.getX(), originalPlotArea.getY(), originalPlotArea.getWidth(), originalPlotArea.getHeight() - depth );
        if ( this.getSimpleLabels() ) {
            this.drawSimpleLabels ( g2, keys, totalValue, adjustedPlotArea, linkArea, state );
        } else {
            this.drawLabels ( g2, keys, totalValue, adjustedPlotArea, linkArea, state );
        }
        if ( this.getShadowGenerator() != null ) {
            final BufferedImage shadowImage = this.getShadowGenerator().createDropShadow ( dataImage );
            g2 = savedG2;
            g2.drawImage ( shadowImage, ( int ) plotArea.getX() + this.getShadowGenerator().calculateOffsetX(), ( int ) plotArea.getY() + this.getShadowGenerator().calculateOffsetY(), null );
            g2.drawImage ( dataImage, ( int ) plotArea.getX(), ( int ) plotArea.getY(), null );
        }
        g2.setClip ( savedClip );
        g2.setComposite ( originalComposite );
        this.drawOutline ( g2, originalPlotArea );
    }
    protected void drawSide ( final Graphics2D g2, final Rectangle2D plotArea, final Arc2D arc, final Area front, final Area back, Paint paint, final Paint outlinePaint, final Stroke outlineStroke, final boolean drawFront, final boolean drawBack ) {
        if ( this.getDarkerSides() ) {
            paint = PaintAlpha.darker ( paint );
        }
        final double start = arc.getAngleStart();
        final double extent = arc.getAngleExtent();
        final double end = start + extent;
        g2.setStroke ( outlineStroke );
        if ( extent < 0.0 ) {
            if ( this.isAngleAtFront ( start ) ) {
                if ( !this.isAngleAtBack ( end ) ) {
                    if ( extent > -180.0 ) {
                        if ( drawFront ) {
                            final Area side = new Area ( new Rectangle2D.Double ( arc.getEndPoint().getX(), plotArea.getY(), arc.getStartPoint().getX() - arc.getEndPoint().getX(), plotArea.getHeight() ) );
                            side.intersect ( front );
                            g2.setPaint ( paint );
                            g2.fill ( side );
                            g2.setPaint ( outlinePaint );
                            g2.draw ( side );
                        }
                    } else {
                        final Area side2 = new Area ( new Rectangle2D.Double ( plotArea.getX(), plotArea.getY(), arc.getStartPoint().getX() - plotArea.getX(), plotArea.getHeight() ) );
                        side2.intersect ( front );
                        final Area side3 = new Area ( new Rectangle2D.Double ( arc.getEndPoint().getX(), plotArea.getY(), plotArea.getMaxX() - arc.getEndPoint().getX(), plotArea.getHeight() ) );
                        side3.intersect ( front );
                        g2.setPaint ( paint );
                        if ( drawFront ) {
                            g2.fill ( side2 );
                            g2.fill ( side3 );
                        }
                        if ( drawBack ) {
                            g2.fill ( back );
                        }
                        g2.setPaint ( outlinePaint );
                        if ( drawFront ) {
                            g2.draw ( side2 );
                            g2.draw ( side3 );
                        }
                        if ( drawBack ) {
                            g2.draw ( back );
                        }
                    }
                } else {
                    if ( drawBack ) {
                        final Area side4 = new Area ( new Rectangle2D.Double ( plotArea.getX(), plotArea.getY(), arc.getEndPoint().getX() - plotArea.getX(), plotArea.getHeight() ) );
                        side4.intersect ( back );
                        g2.setPaint ( paint );
                        g2.fill ( side4 );
                        g2.setPaint ( outlinePaint );
                        g2.draw ( side4 );
                    }
                    if ( drawFront ) {
                        final Area side2 = new Area ( new Rectangle2D.Double ( plotArea.getX(), plotArea.getY(), arc.getStartPoint().getX() - plotArea.getX(), plotArea.getHeight() ) );
                        side2.intersect ( front );
                        g2.setPaint ( paint );
                        g2.fill ( side2 );
                        g2.setPaint ( outlinePaint );
                        g2.draw ( side2 );
                    }
                }
            } else if ( !this.isAngleAtFront ( end ) ) {
                if ( extent > -180.0 ) {
                    if ( drawBack ) {
                        final Area side = new Area ( new Rectangle2D.Double ( arc.getStartPoint().getX(), plotArea.getY(), arc.getEndPoint().getX() - arc.getStartPoint().getX(), plotArea.getHeight() ) );
                        side.intersect ( back );
                        g2.setPaint ( paint );
                        g2.fill ( side );
                        g2.setPaint ( outlinePaint );
                        g2.draw ( side );
                    }
                } else {
                    final Area side2 = new Area ( new Rectangle2D.Double ( arc.getStartPoint().getX(), plotArea.getY(), plotArea.getMaxX() - arc.getStartPoint().getX(), plotArea.getHeight() ) );
                    side2.intersect ( back );
                    final Area side3 = new Area ( new Rectangle2D.Double ( plotArea.getX(), plotArea.getY(), arc.getEndPoint().getX() - plotArea.getX(), plotArea.getHeight() ) );
                    side3.intersect ( back );
                    g2.setPaint ( paint );
                    if ( drawBack ) {
                        g2.fill ( side2 );
                        g2.fill ( side3 );
                    }
                    if ( drawFront ) {
                        g2.fill ( front );
                    }
                    g2.setPaint ( outlinePaint );
                    if ( drawBack ) {
                        g2.draw ( side2 );
                        g2.draw ( side3 );
                    }
                    if ( drawFront ) {
                        g2.draw ( front );
                    }
                }
            } else {
                if ( drawBack ) {
                    final Area side2 = new Area ( new Rectangle2D.Double ( arc.getStartPoint().getX(), plotArea.getY(), plotArea.getMaxX() - arc.getStartPoint().getX(), plotArea.getHeight() ) );
                    side2.intersect ( back );
                    g2.setPaint ( paint );
                    g2.fill ( side2 );
                    g2.setPaint ( outlinePaint );
                    g2.draw ( side2 );
                }
                if ( drawFront ) {
                    final Area side4 = new Area ( new Rectangle2D.Double ( arc.getEndPoint().getX(), plotArea.getY(), plotArea.getMaxX() - arc.getEndPoint().getX(), plotArea.getHeight() ) );
                    side4.intersect ( front );
                    g2.setPaint ( paint );
                    g2.fill ( side4 );
                    g2.setPaint ( outlinePaint );
                    g2.draw ( side4 );
                }
            }
        } else if ( extent > 0.0 ) {
            if ( this.isAngleAtFront ( start ) ) {
                if ( !this.isAngleAtBack ( end ) ) {
                    if ( extent < 180.0 ) {
                        if ( drawFront ) {
                            final Area side = new Area ( new Rectangle2D.Double ( arc.getStartPoint().getX(), plotArea.getY(), arc.getEndPoint().getX() - arc.getStartPoint().getX(), plotArea.getHeight() ) );
                            side.intersect ( front );
                            g2.setPaint ( paint );
                            g2.fill ( side );
                            g2.setPaint ( outlinePaint );
                            g2.draw ( side );
                        }
                    } else {
                        final Area side2 = new Area ( new Rectangle2D.Double ( arc.getStartPoint().getX(), plotArea.getY(), plotArea.getMaxX() - arc.getStartPoint().getX(), plotArea.getHeight() ) );
                        side2.intersect ( front );
                        final Area side3 = new Area ( new Rectangle2D.Double ( plotArea.getX(), plotArea.getY(), arc.getEndPoint().getX() - plotArea.getX(), plotArea.getHeight() ) );
                        side3.intersect ( front );
                        g2.setPaint ( paint );
                        if ( drawFront ) {
                            g2.fill ( side2 );
                            g2.fill ( side3 );
                        }
                        if ( drawBack ) {
                            g2.fill ( back );
                        }
                        g2.setPaint ( outlinePaint );
                        if ( drawFront ) {
                            g2.draw ( side2 );
                            g2.draw ( side3 );
                        }
                        if ( drawBack ) {
                            g2.draw ( back );
                        }
                    }
                } else {
                    if ( drawBack ) {
                        final Area side4 = new Area ( new Rectangle2D.Double ( arc.getEndPoint().getX(), plotArea.getY(), plotArea.getMaxX() - arc.getEndPoint().getX(), plotArea.getHeight() ) );
                        side4.intersect ( back );
                        g2.setPaint ( paint );
                        g2.fill ( side4 );
                        g2.setPaint ( outlinePaint );
                        g2.draw ( side4 );
                    }
                    if ( drawFront ) {
                        final Area side2 = new Area ( new Rectangle2D.Double ( arc.getStartPoint().getX(), plotArea.getY(), plotArea.getMaxX() - arc.getStartPoint().getX(), plotArea.getHeight() ) );
                        side2.intersect ( front );
                        g2.setPaint ( paint );
                        g2.fill ( side2 );
                        g2.setPaint ( outlinePaint );
                        g2.draw ( side2 );
                    }
                }
            } else if ( !this.isAngleAtFront ( end ) ) {
                if ( extent < 180.0 ) {
                    if ( drawBack ) {
                        final Area side = new Area ( new Rectangle2D.Double ( arc.getEndPoint().getX(), plotArea.getY(), arc.getStartPoint().getX() - arc.getEndPoint().getX(), plotArea.getHeight() ) );
                        side.intersect ( back );
                        g2.setPaint ( paint );
                        g2.fill ( side );
                        g2.setPaint ( outlinePaint );
                        g2.draw ( side );
                    }
                } else {
                    final Area side2 = new Area ( new Rectangle2D.Double ( arc.getStartPoint().getX(), plotArea.getY(), plotArea.getX() - arc.getStartPoint().getX(), plotArea.getHeight() ) );
                    side2.intersect ( back );
                    final Area side3 = new Area ( new Rectangle2D.Double ( arc.getEndPoint().getX(), plotArea.getY(), plotArea.getMaxX() - arc.getEndPoint().getX(), plotArea.getHeight() ) );
                    side3.intersect ( back );
                    g2.setPaint ( paint );
                    if ( drawBack ) {
                        g2.fill ( side2 );
                        g2.fill ( side3 );
                    }
                    if ( drawFront ) {
                        g2.fill ( front );
                    }
                    g2.setPaint ( outlinePaint );
                    if ( drawBack ) {
                        g2.draw ( side2 );
                        g2.draw ( side3 );
                    }
                    if ( drawFront ) {
                        g2.draw ( front );
                    }
                }
            } else {
                if ( drawBack ) {
                    final Area side2 = new Area ( new Rectangle2D.Double ( plotArea.getX(), plotArea.getY(), arc.getStartPoint().getX() - plotArea.getX(), plotArea.getHeight() ) );
                    side2.intersect ( back );
                    g2.setPaint ( paint );
                    g2.fill ( side2 );
                    g2.setPaint ( outlinePaint );
                    g2.draw ( side2 );
                }
                if ( drawFront ) {
                    final Area side4 = new Area ( new Rectangle2D.Double ( plotArea.getX(), plotArea.getY(), arc.getEndPoint().getX() - plotArea.getX(), plotArea.getHeight() ) );
                    side4.intersect ( front );
                    g2.setPaint ( paint );
                    g2.fill ( side4 );
                    g2.setPaint ( outlinePaint );
                    g2.draw ( side4 );
                }
            }
        }
    }
    @Override
    public String getPlotType() {
        return PiePlot3D.localizationResources.getString ( "Pie_3D_Plot" );
    }
    private boolean isAngleAtFront ( final double angle ) {
        return Math.sin ( Math.toRadians ( angle ) ) < 0.0;
    }
    private boolean isAngleAtBack ( final double angle ) {
        return Math.sin ( Math.toRadians ( angle ) ) > 0.0;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof PiePlot3D ) ) {
            return false;
        }
        final PiePlot3D that = ( PiePlot3D ) obj;
        return this.depthFactor == that.depthFactor && this.darkerSides == that.darkerSides && super.equals ( obj );
    }
}
