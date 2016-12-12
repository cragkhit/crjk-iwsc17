package org.jfree.chart.util;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import org.jfree.ui.TextAnchor;
public class TextUtils {
    public static Rectangle2D drawAlignedString ( String text,
            Graphics2D g2, float x, float y, TextAnchor anchor ) {
        Rectangle2D textBounds = new Rectangle2D.Double();
        float[] adjust = deriveTextBoundsAnchorOffsets ( g2, text, anchor,
                         textBounds );
        textBounds.setRect ( x + adjust[0], y + adjust[1] + adjust[2],
                             textBounds.getWidth(), textBounds.getHeight() );
        g2.drawString ( text, x + adjust[0], y + adjust[1] );
        return textBounds;
    }
    public static Rectangle2D calcAlignedStringBounds ( String text,
            Graphics2D g2, float x, float y, TextAnchor anchor ) {
        Rectangle2D textBounds = new Rectangle2D.Double();
        float[] adjust = deriveTextBoundsAnchorOffsets ( g2, text, anchor,
                         textBounds );
        textBounds.setRect ( x + adjust[0], y + adjust[1] + adjust[2],
                             textBounds.getWidth(), textBounds.getHeight() );
        return textBounds;
    }
    private static float[] deriveTextBoundsAnchorOffsets ( Graphics2D g2,
            String text, TextAnchor anchor ) {
        float[] result = new float[2];
        FontRenderContext frc = g2.getFontRenderContext();
        Font f = g2.getFont();
        FontMetrics fm = g2.getFontMetrics ( f );
        Rectangle2D bounds = getTextBounds ( text, fm );
        LineMetrics metrics = f.getLineMetrics ( text, frc );
        float ascent = metrics.getAscent();
        float halfAscent = ascent / 2.0f;
        float descent = metrics.getDescent();
        float leading = metrics.getLeading();
        float xAdj = 0.0f;
        float yAdj = 0.0f;
        if ( anchor.isHorizontalCenter() ) {
            xAdj = ( float ) - bounds.getWidth() / 2.0f;
        } else if ( anchor.isRight() ) {
            xAdj = ( float ) - bounds.getWidth();
        }
        if ( anchor.isTop() ) {
            yAdj = -descent - leading + ( float ) bounds.getHeight();
        } else if ( anchor.isHalfAscent() ) {
            yAdj = halfAscent;
        } else if ( anchor.isVerticalCenter() ) {
            yAdj = -descent - leading + ( float ) ( bounds.getHeight() / 2.0 );
        } else if ( anchor.isBaseline() ) {
            yAdj = 0.0f;
        } else if ( anchor.isBottom() ) {
            yAdj = -metrics.getDescent() - metrics.getLeading();
        }
        result[0] = xAdj;
        result[1] = yAdj;
        return result;
    }
    private static float[] deriveTextBoundsAnchorOffsets ( Graphics2D g2,
            String text, TextAnchor anchor, Rectangle2D textBounds ) {
        float[] result = new float[3];
        FontRenderContext frc = g2.getFontRenderContext();
        Font f = g2.getFont();
        FontMetrics fm = g2.getFontMetrics ( f );
        Rectangle2D bounds = getTextBounds ( text, fm );
        LineMetrics metrics = f.getLineMetrics ( text, frc );
        float ascent = metrics.getAscent();
        result[2] = -ascent;
        float halfAscent = ascent / 2.0f;
        float descent = metrics.getDescent();
        float leading = metrics.getLeading();
        float xAdj = 0.0f;
        float yAdj = 0.0f;
        if ( anchor.isHorizontalCenter() ) {
            xAdj = ( float ) - bounds.getWidth() / 2.0f;
        } else if ( anchor.isRight() ) {
            xAdj = ( float ) - bounds.getWidth();
        }
        if ( anchor.isTop() ) {
            yAdj = -descent - leading + ( float ) bounds.getHeight();
        } else if ( anchor.isHalfAscent() ) {
            yAdj = halfAscent;
        } else if ( anchor.isHorizontalCenter() ) {
            yAdj = -descent - leading + ( float ) ( bounds.getHeight() / 2.0 );
        } else if ( anchor.isBaseline() ) {
            yAdj = 0.0f;
        } else if ( anchor.isBottom() ) {
            yAdj = -metrics.getDescent() - metrics.getLeading();
        }
        if ( textBounds != null ) {
            textBounds.setRect ( bounds );
        }
        result[0] = xAdj;
        result[1] = yAdj;
        return result;
    }
    public static Rectangle2D getTextBounds ( String text, FontMetrics fm ) {
        return getTextBounds ( text, 0.0, 0.0, fm );
    }
    public static Rectangle2D getTextBounds ( String text, double x, double y,
            FontMetrics fm ) {
        ParamChecks.nullNotPermitted ( text, "text" );
        ParamChecks.nullNotPermitted ( fm, "fm" );
        double width = fm.stringWidth ( text );
        double height = fm.getHeight();
        return new Rectangle2D.Double ( x, y - fm.getAscent(), width, height );
    }
}
