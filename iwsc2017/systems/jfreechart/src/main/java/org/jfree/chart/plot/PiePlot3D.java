

package org.jfree.chart.plot;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.util.PaintAlpha;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.PieDataset;
import org.jfree.ui.RectangleInsets;


public class PiePlot3D extends PiePlot implements Serializable {


    private static final long serialVersionUID = 3408984188945161432L;


    private double depthFactor = 0.12;


    private boolean darkerSides = false;


    public PiePlot3D() {
        this ( null );
    }


    public PiePlot3D ( PieDataset dataset ) {
        super ( dataset );
        setCircular ( false, false );
    }


    public double getDepthFactor() {
        return this.depthFactor;
    }


    public void setDepthFactor ( double factor ) {
        this.depthFactor = factor;
        fireChangeEvent();
    }


    public boolean getDarkerSides() {
        return this.darkerSides;
    }


    public void setDarkerSides ( boolean darker ) {
        this.darkerSides = darker;
        fireChangeEvent();
    }


    @Override
    public void draw ( Graphics2D g2, Rectangle2D plotArea, Point2D anchor,
                       PlotState parentState, PlotRenderingInfo info ) {

        RectangleInsets insets = getInsets();
        insets.trim ( plotArea );

        Rectangle2D originalPlotArea = ( Rectangle2D ) plotArea.clone();
        if ( info != null ) {
            info.setPlotArea ( plotArea );
            info.setDataArea ( plotArea );
        }

        drawBackground ( g2, plotArea );

        Shape savedClip = g2.getClip();
        g2.clip ( plotArea );

        Graphics2D savedG2 = g2;
        BufferedImage dataImage = null;
        if ( getShadowGenerator() != null ) {
            dataImage = new BufferedImage ( ( int ) plotArea.getWidth(),
                                            ( int ) plotArea.getHeight(), BufferedImage.TYPE_INT_ARGB );
            g2 = dataImage.createGraphics();
            g2.translate ( -plotArea.getX(), -plotArea.getY() );
            g2.setRenderingHints ( savedG2.getRenderingHints() );
            originalPlotArea = ( Rectangle2D ) plotArea.clone();
        }
        double gapPercent = getInteriorGap();
        double labelPercent = 0.0;
        if ( getLabelGenerator() != null ) {
            labelPercent = getLabelGap() + getMaximumLabelWidth();
        }
        double gapHorizontal = plotArea.getWidth() * ( gapPercent
                               + labelPercent ) * 2.0;
        double gapVertical = plotArea.getHeight() * gapPercent * 2.0;

        if ( DEBUG_DRAW_INTERIOR ) {
            double hGap = plotArea.getWidth() * getInteriorGap();
            double vGap = plotArea.getHeight() * getInteriorGap();
            double igx1 = plotArea.getX() + hGap;
            double igx2 = plotArea.getMaxX() - hGap;
            double igy1 = plotArea.getY() + vGap;
            double igy2 = plotArea.getMaxY() - vGap;
            g2.setPaint ( Color.lightGray );
            g2.draw ( new Rectangle2D.Double ( igx1, igy1, igx2 - igx1,
                                               igy2 - igy1 ) );
        }

        double linkX = plotArea.getX() + gapHorizontal / 2;
        double linkY = plotArea.getY() + gapVertical / 2;
        double linkW = plotArea.getWidth() - gapHorizontal;
        double linkH = plotArea.getHeight() - gapVertical;

        if ( isCircular() ) {
            double min = Math.min ( linkW, linkH ) / 2;
            linkX = ( linkX + linkX + linkW ) / 2 - min;
            linkY = ( linkY + linkY + linkH ) / 2 - min;
            linkW = 2 * min;
            linkH = 2 * min;
        }

        PiePlotState state = initialise ( g2, plotArea, this, null, info );

        Rectangle2D linkAreaXX = new Rectangle2D.Double ( linkX, linkY, linkW,
                linkH * ( 1 - this.depthFactor ) );
        state.setLinkArea ( linkAreaXX );

        if ( DEBUG_DRAW_LINK_AREA ) {
            g2.setPaint ( Color.blue );
            g2.draw ( linkAreaXX );
            g2.setPaint ( Color.yellow );
            g2.draw ( new Ellipse2D.Double ( linkAreaXX.getX(), linkAreaXX.getY(),
                                             linkAreaXX.getWidth(), linkAreaXX.getHeight() ) );
        }

        double hh = linkW * getLabelLinkMargin();
        double vv = linkH * getLabelLinkMargin();
        Rectangle2D explodeArea = new Rectangle2D.Double ( linkX + hh / 2.0,
                linkY + vv / 2.0, linkW - hh, linkH - vv );

        state.setExplodedPieArea ( explodeArea );

        double maximumExplodePercent = getMaximumExplodePercent();
        double percent = maximumExplodePercent / ( 1.0 + maximumExplodePercent );

        double h1 = explodeArea.getWidth() * percent;
        double v1 = explodeArea.getHeight() * percent;
        Rectangle2D pieArea = new Rectangle2D.Double ( explodeArea.getX()
                + h1 / 2.0, explodeArea.getY() + v1 / 2.0,
                explodeArea.getWidth() - h1, explodeArea.getHeight() - v1 );

        int depth = ( int ) ( pieArea.getHeight() * this.depthFactor );
        Rectangle2D linkArea = new Rectangle2D.Double ( linkX, linkY, linkW,
                linkH - depth );
        state.setLinkArea ( linkArea );

        state.setPieArea ( pieArea );
        state.setPieCenterX ( pieArea.getCenterX() );
        state.setPieCenterY ( pieArea.getCenterY() - depth / 2.0 );
        state.setPieWRadius ( pieArea.getWidth() / 2.0 );
        state.setPieHRadius ( ( pieArea.getHeight() - depth ) / 2.0 );

        PieDataset dataset = getDataset();
        if ( DatasetUtilities.isEmptyOrNull ( getDataset() ) ) {
            drawNoDataMessage ( g2, plotArea );
            g2.setClip ( savedClip );
            drawOutline ( g2, plotArea );
            return;
        }

        if ( dataset.getKeys().size() > plotArea.getWidth() ) {
            String text = localizationResources.getString ( "Too_many_elements" );
            Font sfont = new Font ( "dialog", Font.BOLD, 10 );
            g2.setFont ( sfont );
            FontMetrics fm = g2.getFontMetrics ( sfont );
            int stringWidth = fm.stringWidth ( text );

            g2.drawString ( text, ( int ) ( plotArea.getX() + ( plotArea.getWidth()
                                            - stringWidth ) / 2 ), ( int ) ( plotArea.getY()
                                                    + ( plotArea.getHeight() / 2 ) ) );
            return;
        }
        if ( isCircular() ) {
            double min = Math.min ( plotArea.getWidth(),
                                    plotArea.getHeight() ) / 2;
            plotArea = new Rectangle2D.Double ( plotArea.getCenterX() - min,
                                                plotArea.getCenterY() - min, 2 * min, 2 * min );
        }
        List sectionKeys = dataset.getKeys();

        if ( sectionKeys.isEmpty() ) {
            return;
        }

        double arcX = pieArea.getX();
        double arcY = pieArea.getY();

        Composite originalComposite = g2.getComposite();
        g2.setComposite ( AlphaComposite.getInstance ( AlphaComposite.SRC_OVER,
                          getForegroundAlpha() ) );

        double totalValue = DatasetUtilities.calculatePieDatasetTotal ( dataset );
        double runningTotal = 0;
        if ( depth < 0 ) {
            return;
        }

        ArrayList arcList = new ArrayList();
        Arc2D.Double arc;
        Paint paint;
        Paint outlinePaint;
        Stroke outlineStroke;

        Iterator iterator = sectionKeys.iterator();
        while ( iterator.hasNext() ) {

            Comparable currentKey = ( Comparable ) iterator.next();
            Number dataValue = dataset.getValue ( currentKey );
            if ( dataValue == null ) {
                arcList.add ( null );
                continue;
            }
            double value = dataValue.doubleValue();
            if ( value <= 0 ) {
                arcList.add ( null );
                continue;
            }
            double startAngle = getStartAngle();
            double direction = getDirection().getFactor();
            double angle1 = startAngle + ( direction * ( runningTotal * 360 ) )
                            / totalValue;
            double angle2 = startAngle + ( direction * ( runningTotal + value )
                                           * 360 ) / totalValue;
            if ( Math.abs ( angle2 - angle1 ) > getMinimumArcAngleToDraw() ) {
                arcList.add ( new Arc2D.Double ( arcX, arcY + depth,
                                                 pieArea.getWidth(), pieArea.getHeight() - depth,
                                                 angle1, angle2 - angle1, Arc2D.PIE ) );
            } else {
                arcList.add ( null );
            }
            runningTotal += value;
        }

        Shape oldClip = g2.getClip();

        Ellipse2D top = new Ellipse2D.Double ( pieArea.getX(), pieArea.getY(),
                                               pieArea.getWidth(), pieArea.getHeight() - depth );

        Ellipse2D bottom = new Ellipse2D.Double ( pieArea.getX(), pieArea.getY()
                + depth, pieArea.getWidth(), pieArea.getHeight() - depth );

        Rectangle2D lower = new Rectangle2D.Double ( top.getX(),
                top.getCenterY(), pieArea.getWidth(), bottom.getMaxY()
                - top.getCenterY() );

        Rectangle2D upper = new Rectangle2D.Double ( pieArea.getX(), top.getY(),
                pieArea.getWidth(), bottom.getCenterY() - top.getY() );

        Area a = new Area ( top );
        a.add ( new Area ( lower ) );
        Area b = new Area ( bottom );
        b.add ( new Area ( upper ) );
        Area pie = new Area ( a );
        pie.intersect ( b );

        Area front = new Area ( pie );
        front.subtract ( new Area ( top ) );

        Area back = new Area ( pie );
        back.subtract ( new Area ( bottom ) );

        int[] xs;
        int[] ys;

        int categoryCount = arcList.size();
        for ( int categoryIndex = 0; categoryIndex < categoryCount;
                categoryIndex++ ) {
            arc = ( Arc2D.Double ) arcList.get ( categoryIndex );
            if ( arc == null ) {
                continue;
            }
            Comparable key = getSectionKey ( categoryIndex );
            paint = lookupSectionPaint ( key );
            outlinePaint = lookupSectionOutlinePaint ( key );
            outlineStroke = lookupSectionOutlineStroke ( key );
            g2.setPaint ( paint );
            g2.fill ( arc );
            g2.setPaint ( outlinePaint );
            g2.setStroke ( outlineStroke );
            g2.draw ( arc );
            g2.setPaint ( paint );

            Point2D p1 = arc.getStartPoint();

            xs = new int[] { ( int ) arc.getCenterX(), ( int ) arc.getCenterX(),
                             ( int ) p1.getX(), ( int ) p1.getX()
                           };
            ys = new int[] { ( int ) arc.getCenterY(), ( int ) arc.getCenterY()
                             - depth, ( int ) p1.getY() - depth, ( int ) p1.getY()
                           };
            Polygon polygon = new Polygon ( xs, ys, 4 );
            g2.setPaint ( java.awt.Color.lightGray );
            g2.fill ( polygon );
            g2.setPaint ( outlinePaint );
            g2.setStroke ( outlineStroke );
            g2.draw ( polygon );
            g2.setPaint ( paint );

        }

        g2.setPaint ( Color.gray );
        g2.fill ( back );
        g2.fill ( front );

        int cat = 0;
        iterator = arcList.iterator();
        while ( iterator.hasNext() ) {
            Arc2D segment = ( Arc2D ) iterator.next();
            if ( segment != null ) {
                Comparable key = getSectionKey ( cat );
                paint = lookupSectionPaint ( key );
                outlinePaint = lookupSectionOutlinePaint ( key );
                outlineStroke = lookupSectionOutlineStroke ( key );
                drawSide ( g2, pieArea, segment, front, back, paint,
                           outlinePaint, outlineStroke, false, true );
            }
            cat++;
        }

        cat = 0;
        iterator = arcList.iterator();
        while ( iterator.hasNext() ) {
            Arc2D segment = ( Arc2D ) iterator.next();
            if ( segment != null ) {
                Comparable key = getSectionKey ( cat );
                paint = lookupSectionPaint ( key );
                outlinePaint = lookupSectionOutlinePaint ( key );
                outlineStroke = lookupSectionOutlineStroke ( key );
                drawSide ( g2, pieArea, segment, front, back, paint,
                           outlinePaint, outlineStroke, true, false );
            }
            cat++;
        }

        g2.setClip ( oldClip );

        Arc2D upperArc;
        for ( int sectionIndex = 0; sectionIndex < categoryCount;
                sectionIndex++ ) {
            arc = ( Arc2D.Double ) arcList.get ( sectionIndex );
            if ( arc == null ) {
                continue;
            }
            upperArc = new Arc2D.Double ( arcX, arcY, pieArea.getWidth(),
                                          pieArea.getHeight() - depth, arc.getAngleStart(),
                                          arc.getAngleExtent(), Arc2D.PIE );

            Comparable currentKey = ( Comparable ) sectionKeys.get ( sectionIndex );
            paint = lookupSectionPaint ( currentKey, true );
            outlinePaint = lookupSectionOutlinePaint ( currentKey );
            outlineStroke = lookupSectionOutlineStroke ( currentKey );
            g2.setPaint ( paint );
            g2.fill ( upperArc );
            g2.setStroke ( outlineStroke );
            g2.setPaint ( outlinePaint );
            g2.draw ( upperArc );

            if ( info != null ) {
                EntityCollection entities
                    = info.getOwner().getEntityCollection();
                if ( entities != null ) {
                    String tip = null;
                    PieToolTipGenerator tipster = getToolTipGenerator();
                    if ( tipster != null ) {
                        tip = tipster.generateToolTip ( dataset, currentKey );
                    }
                    String url = null;
                    if ( getURLGenerator() != null ) {
                        url = getURLGenerator().generateURL ( dataset, currentKey,
                                                              getPieIndex() );
                    }
                    PieSectionEntity entity = new PieSectionEntity (
                        upperArc, dataset, getPieIndex(), sectionIndex,
                        currentKey, tip, url );
                    entities.add ( entity );
                }
            }
        }

        List keys = dataset.getKeys();
        Rectangle2D adjustedPlotArea = new Rectangle2D.Double (
            originalPlotArea.getX(), originalPlotArea.getY(),
            originalPlotArea.getWidth(), originalPlotArea.getHeight()
            - depth );
        if ( getSimpleLabels() ) {
            drawSimpleLabels ( g2, keys, totalValue, adjustedPlotArea,
                               linkArea, state );
        } else {
            drawLabels ( g2, keys, totalValue, adjustedPlotArea, linkArea,
                         state );
        }

        if ( getShadowGenerator() != null ) {
            BufferedImage shadowImage
                = getShadowGenerator().createDropShadow ( dataImage );
            g2 = savedG2;
            g2.drawImage ( shadowImage, ( int ) plotArea.getX()
                           + getShadowGenerator().calculateOffsetX(),
                           ( int ) plotArea.getY()
                           + getShadowGenerator().calculateOffsetY(), null );
            g2.drawImage ( dataImage, ( int ) plotArea.getX(),
                           ( int ) plotArea.getY(), null );
        }

        g2.setClip ( savedClip );
        g2.setComposite ( originalComposite );
        drawOutline ( g2, originalPlotArea );

    }


    protected void drawSide ( Graphics2D g2,
                              Rectangle2D plotArea,
                              Arc2D arc,
                              Area front,
                              Area back,
                              Paint paint,
                              Paint outlinePaint,
                              Stroke outlineStroke,
                              boolean drawFront,
                              boolean drawBack ) {

        if ( getDarkerSides() ) {
            paint = PaintAlpha.darker ( paint );
        }

        double start = arc.getAngleStart();
        double extent = arc.getAngleExtent();
        double end = start + extent;

        g2.setStroke ( outlineStroke );

        if ( extent < 0.0 ) {

            if ( isAngleAtFront ( start ) ) {

                if ( !isAngleAtBack ( end ) ) {

                    if ( extent > -180.0 ) {
                        if ( drawFront ) {
                            Area side = new Area ( new Rectangle2D.Double (
                                                       arc.getEndPoint().getX(), plotArea.getY(),
                                                       arc.getStartPoint().getX()
                                                       - arc.getEndPoint().getX(),
                                                       plotArea.getHeight() ) );
                            side.intersect ( front );
                            g2.setPaint ( paint );
                            g2.fill ( side );
                            g2.setPaint ( outlinePaint );
                            g2.draw ( side );
                        }
                    } else {
                        Area side1 = new Area ( new Rectangle2D.Double (
                                                    plotArea.getX(), plotArea.getY(),
                                                    arc.getStartPoint().getX() - plotArea.getX(),
                                                    plotArea.getHeight() ) );
                        side1.intersect ( front );

                        Area side2 = new Area ( new Rectangle2D.Double (
                                                    arc.getEndPoint().getX(), plotArea.getY(),
                                                    plotArea.getMaxX() - arc.getEndPoint().getX(),
                                                    plotArea.getHeight() ) );

                        side2.intersect ( front );
                        g2.setPaint ( paint );
                        if ( drawFront ) {
                            g2.fill ( side1 );
                            g2.fill ( side2 );
                        }

                        if ( drawBack ) {
                            g2.fill ( back );
                        }

                        g2.setPaint ( outlinePaint );
                        if ( drawFront ) {
                            g2.draw ( side1 );
                            g2.draw ( side2 );
                        }

                        if ( drawBack ) {
                            g2.draw ( back );
                        }

                    }
                } else {

                    if ( drawBack ) {
                        Area side2 = new Area ( new Rectangle2D.Double (
                                                    plotArea.getX(), plotArea.getY(),
                                                    arc.getEndPoint().getX() - plotArea.getX(),
                                                    plotArea.getHeight() ) );
                        side2.intersect ( back );
                        g2.setPaint ( paint );
                        g2.fill ( side2 );
                        g2.setPaint ( outlinePaint );
                        g2.draw ( side2 );
                    }

                    if ( drawFront ) {
                        Area side1 = new Area ( new Rectangle2D.Double (
                                                    plotArea.getX(), plotArea.getY(),
                                                    arc.getStartPoint().getX() - plotArea.getX(),
                                                    plotArea.getHeight() ) );
                        side1.intersect ( front );
                        g2.setPaint ( paint );
                        g2.fill ( side1 );
                        g2.setPaint ( outlinePaint );
                        g2.draw ( side1 );
                    }
                }
            } else {

                if ( !isAngleAtFront ( end ) ) {
                    if ( extent > -180.0 ) {
                        if ( drawBack ) {
                            Area side = new Area ( new Rectangle2D.Double (
                                                       arc.getStartPoint().getX(), plotArea.getY(),
                                                       arc.getEndPoint().getX()
                                                       - arc.getStartPoint().getX(),
                                                       plotArea.getHeight() ) );
                            side.intersect ( back );
                            g2.setPaint ( paint );
                            g2.fill ( side );
                            g2.setPaint ( outlinePaint );
                            g2.draw ( side );
                        }
                    } else {
                        Area side1 = new Area ( new Rectangle2D.Double (
                                                    arc.getStartPoint().getX(), plotArea.getY(),
                                                    plotArea.getMaxX() - arc.getStartPoint().getX(),
                                                    plotArea.getHeight() ) );
                        side1.intersect ( back );

                        Area side2 = new Area ( new Rectangle2D.Double (
                                                    plotArea.getX(), plotArea.getY(),
                                                    arc.getEndPoint().getX() - plotArea.getX(),
                                                    plotArea.getHeight() ) );

                        side2.intersect ( back );

                        g2.setPaint ( paint );
                        if ( drawBack ) {
                            g2.fill ( side1 );
                            g2.fill ( side2 );
                        }

                        if ( drawFront ) {
                            g2.fill ( front );
                        }

                        g2.setPaint ( outlinePaint );
                        if ( drawBack ) {
                            g2.draw ( side1 );
                            g2.draw ( side2 );
                        }

                        if ( drawFront ) {
                            g2.draw ( front );
                        }

                    }
                } else {

                    if ( drawBack ) {
                        Area side1 = new Area ( new Rectangle2D.Double (
                                                    arc.getStartPoint().getX(), plotArea.getY(),
                                                    plotArea.getMaxX() - arc.getStartPoint().getX(),
                                                    plotArea.getHeight() ) );
                        side1.intersect ( back );
                        g2.setPaint ( paint );
                        g2.fill ( side1 );
                        g2.setPaint ( outlinePaint );
                        g2.draw ( side1 );
                    }

                    if ( drawFront ) {
                        Area side2 = new Area ( new Rectangle2D.Double (
                                                    arc.getEndPoint().getX(), plotArea.getY(),
                                                    plotArea.getMaxX() - arc.getEndPoint().getX(),
                                                    plotArea.getHeight() ) );
                        side2.intersect ( front );
                        g2.setPaint ( paint );
                        g2.fill ( side2 );
                        g2.setPaint ( outlinePaint );
                        g2.draw ( side2 );
                    }

                }
            }
        } else if ( extent > 0.0 ) {

            if ( isAngleAtFront ( start ) ) {

                if ( !isAngleAtBack ( end ) ) {

                    if ( extent < 180.0 ) {
                        if ( drawFront ) {
                            Area side = new Area ( new Rectangle2D.Double (
                                                       arc.getStartPoint().getX(), plotArea.getY(),
                                                       arc.getEndPoint().getX()
                                                       - arc.getStartPoint().getX(),
                                                       plotArea.getHeight() ) );
                            side.intersect ( front );
                            g2.setPaint ( paint );
                            g2.fill ( side );
                            g2.setPaint ( outlinePaint );
                            g2.draw ( side );
                        }
                    } else {
                        Area side1 = new Area ( new Rectangle2D.Double (
                                                    arc.getStartPoint().getX(), plotArea.getY(),
                                                    plotArea.getMaxX() - arc.getStartPoint().getX(),
                                                    plotArea.getHeight() ) );
                        side1.intersect ( front );

                        Area side2 = new Area ( new Rectangle2D.Double (
                                                    plotArea.getX(), plotArea.getY(),
                                                    arc.getEndPoint().getX() - plotArea.getX(),
                                                    plotArea.getHeight() ) );
                        side2.intersect ( front );

                        g2.setPaint ( paint );
                        if ( drawFront ) {
                            g2.fill ( side1 );
                            g2.fill ( side2 );
                        }

                        if ( drawBack ) {
                            g2.fill ( back );
                        }

                        g2.setPaint ( outlinePaint );
                        if ( drawFront ) {
                            g2.draw ( side1 );
                            g2.draw ( side2 );
                        }

                        if ( drawBack ) {
                            g2.draw ( back );
                        }

                    }
                } else {
                    if ( drawBack ) {
                        Area side2 = new Area ( new Rectangle2D.Double (
                                                    arc.getEndPoint().getX(), plotArea.getY(),
                                                    plotArea.getMaxX() - arc.getEndPoint().getX(),
                                                    plotArea.getHeight() ) );
                        side2.intersect ( back );
                        g2.setPaint ( paint );
                        g2.fill ( side2 );
                        g2.setPaint ( outlinePaint );
                        g2.draw ( side2 );
                    }

                    if ( drawFront ) {
                        Area side1 = new Area ( new Rectangle2D.Double (
                                                    arc.getStartPoint().getX(), plotArea.getY(),
                                                    plotArea.getMaxX() - arc.getStartPoint().getX(),
                                                    plotArea.getHeight() ) );
                        side1.intersect ( front );
                        g2.setPaint ( paint );
                        g2.fill ( side1 );
                        g2.setPaint ( outlinePaint );
                        g2.draw ( side1 );
                    }
                }
            } else {

                if ( !isAngleAtFront ( end ) ) {
                    if ( extent < 180.0 ) {
                        if ( drawBack ) {
                            Area side = new Area ( new Rectangle2D.Double (
                                                       arc.getEndPoint().getX(), plotArea.getY(),
                                                       arc.getStartPoint().getX()
                                                       - arc.getEndPoint().getX(),
                                                       plotArea.getHeight() ) );
                            side.intersect ( back );
                            g2.setPaint ( paint );
                            g2.fill ( side );
                            g2.setPaint ( outlinePaint );
                            g2.draw ( side );
                        }
                    } else {
                        Area side1 = new Area ( new Rectangle2D.Double (
                                                    arc.getStartPoint().getX(), plotArea.getY(),
                                                    plotArea.getX() - arc.getStartPoint().getX(),
                                                    plotArea.getHeight() ) );
                        side1.intersect ( back );

                        Area side2 = new Area ( new Rectangle2D.Double (
                                                    arc.getEndPoint().getX(), plotArea.getY(),
                                                    plotArea.getMaxX() - arc.getEndPoint().getX(),
                                                    plotArea.getHeight() ) );
                        side2.intersect ( back );

                        g2.setPaint ( paint );
                        if ( drawBack ) {
                            g2.fill ( side1 );
                            g2.fill ( side2 );
                        }

                        if ( drawFront ) {
                            g2.fill ( front );
                        }

                        g2.setPaint ( outlinePaint );
                        if ( drawBack ) {
                            g2.draw ( side1 );
                            g2.draw ( side2 );
                        }

                        if ( drawFront ) {
                            g2.draw ( front );
                        }

                    }
                } else {
                    if ( drawBack ) {
                        Area side1 = new Area ( new Rectangle2D.Double (
                                                    plotArea.getX(), plotArea.getY(),
                                                    arc.getStartPoint().getX() - plotArea.getX(),
                                                    plotArea.getHeight() ) );
                        side1.intersect ( back );
                        g2.setPaint ( paint );
                        g2.fill ( side1 );
                        g2.setPaint ( outlinePaint );
                        g2.draw ( side1 );
                    }

                    if ( drawFront ) {
                        Area side2 = new Area ( new Rectangle2D.Double (
                                                    plotArea.getX(), plotArea.getY(),
                                                    arc.getEndPoint().getX() - plotArea.getX(),
                                                    plotArea.getHeight() ) );
                        side2.intersect ( front );
                        g2.setPaint ( paint );
                        g2.fill ( side2 );
                        g2.setPaint ( outlinePaint );
                        g2.draw ( side2 );
                    }
                }
            }

        }

    }


    @Override
    public String getPlotType() {
        return localizationResources.getString ( "Pie_3D_Plot" );
    }


    private boolean isAngleAtFront ( double angle ) {
        return ( Math.sin ( Math.toRadians ( angle ) ) < 0.0 );
    }


    private boolean isAngleAtBack ( double angle ) {
        return ( Math.sin ( Math.toRadians ( angle ) ) > 0.0 );
    }


    @Override
    public boolean equals ( Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof PiePlot3D ) ) {
            return false;
        }
        PiePlot3D that = ( PiePlot3D ) obj;
        if ( this.depthFactor != that.depthFactor ) {
            return false;
        }
        if ( this.darkerSides != that.darkerSides ) {
            return false;
        }
        return super.equals ( obj );
    }

}
