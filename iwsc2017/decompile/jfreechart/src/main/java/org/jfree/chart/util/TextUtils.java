package org.jfree.chart.util;
import java.awt.font.LineMetrics;
import java.awt.FontMetrics;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import org.jfree.ui.TextAnchor;
import java.awt.Graphics2D;
public class TextUtils {
    public static Rectangle2D drawAlignedString ( final String text, final Graphics2D g2, final float x, final float y, final TextAnchor anchor ) {
        final Rectangle2D textBounds = new Rectangle2D.Double();
        final float[] adjust = deriveTextBoundsAnchorOffsets ( g2, text, anchor, textBounds );
        textBounds.setRect ( x + adjust[0], y + adjust[1] + adjust[2], textBounds.getWidth(), textBounds.getHeight() );
        g2.drawString ( text, x + adjust[0], y + adjust[1] );
        return textBounds;
    }
    public static Rectangle2D calcAlignedStringBounds ( final String text, final Graphics2D g2, final float x, final float y, final TextAnchor anchor ) {
        final Rectangle2D textBounds = new Rectangle2D.Double();
        final float[] adjust = deriveTextBoundsAnchorOffsets ( g2, text, anchor, textBounds );
        textBounds.setRect ( x + adjust[0], y + adjust[1] + adjust[2], textBounds.getWidth(), textBounds.getHeight() );
        return textBounds;
    }
    private static float[] deriveTextBoundsAnchorOffsets ( final Graphics2D g2, final String text, final TextAnchor anchor ) {
        final float[] result = new float[2];
        final FontRenderContext frc = g2.getFontRenderContext();
        final Font f = g2.getFont();
        final FontMetrics fm = g2.getFontMetrics ( f );
        final Rectangle2D bounds = getTextBounds ( text, fm );
        final LineMetrics metrics = f.getLineMetrics ( text, frc );
        final float ascent = metrics.getAscent();
        final float halfAscent = ascent / 2.0f;
        final float descent = metrics.getDescent();
        final float leading = metrics.getLeading();
        float xAdj = 0.0f;
        float yAdj = 0.0f;
        if ( anchor.isHorizontalCenter() ) {
            xAdj = ( float ) ( -bounds.getWidth() ) / 2.0f;
        } else if ( anchor.isRight() ) {
            xAdj = ( float ) ( -bounds.getWidth() );
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
    private static float[] deriveTextBoundsAnchorOffsets ( final Graphics2D g2, final String text, final TextAnchor anchor, final Rectangle2D textBounds ) {
        final float[] result = new float[3];
        final FontRenderContext frc = g2.getFontRenderContext();
        final Font f = g2.getFont();
        final FontMetrics fm = g2.getFontMetrics ( f );
        final Rectangle2D bounds = getTextBounds ( text, fm );
        final LineMetrics metrics = f.getLineMetrics ( text, frc );
        final float ascent = metrics.getAscent();
        result[2] = -ascent;
        final float halfAscent = ascent / 2.0f;
        final float descent = metrics.getDescent();
        final float leading = metrics.getLeading();
        float xAdj = 0.0f;
        float yAdj = 0.0f;
        if ( anchor.isHorizontalCenter() ) {
            xAdj = ( float ) ( -bounds.getWidth() ) / 2.0f;
        } else if ( anchor.isRight() ) {
            xAdj = ( float ) ( -bounds.getWidth() );
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
    public static Rectangle2D getTextBounds ( final String text, final FontMetrics fm ) {
        return getTextBounds ( text, 0.0, 0.0, fm );
    }
    public static Rectangle2D getTextBounds ( final String text, final double x, final double y, final FontMetrics fm ) {
        ParamChecks.nullNotPermitted ( text, "text" );
        ParamChecks.nullNotPermitted ( fm, "fm" );
        final double width = fm.stringWidth ( text );
        final double height = fm.getHeight();
        return new Rectangle2D.Double ( x, y - fm.getAscent(), width, height );
    }
}
