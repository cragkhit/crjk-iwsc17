package org.jfree.chart.util;
import org.jfree.ui.TextAnchor;
import java.awt.geom.AffineTransform;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.text.AttributedString;
public class AttrStringUtils {
    public static Rectangle2D getTextBounds ( final AttributedString text, final Graphics2D g2 ) {
        final TextLayout tl = new TextLayout ( text.getIterator(), g2.getFontRenderContext() );
        return tl.getBounds();
    }
    public static void drawRotatedString ( final AttributedString text, final Graphics2D g2, final double angle, final float x, final float y ) {
        drawRotatedString ( text, g2, x, y, angle, x, y );
    }
    public static void drawRotatedString ( final AttributedString text, final Graphics2D g2, final float textX, final float textY, final double angle, final float rotateX, final float rotateY ) {
        ParamChecks.nullNotPermitted ( text, "text" );
        final AffineTransform saved = g2.getTransform();
        final AffineTransform rotate = AffineTransform.getRotateInstance ( angle, rotateX, rotateY );
        g2.transform ( rotate );
        final TextLayout tl = new TextLayout ( text.getIterator(), g2.getFontRenderContext() );
        tl.draw ( g2, textX, textY );
        g2.setTransform ( saved );
    }
    public static void drawRotatedString ( final AttributedString text, final Graphics2D g2, final float x, final float y, final TextAnchor textAnchor, final double angle, final float rotationX, final float rotationY ) {
        ParamChecks.nullNotPermitted ( text, "text" );
        final float[] textAdj = deriveTextBoundsAnchorOffsets ( g2, text, textAnchor, null );
        drawRotatedString ( text, g2, x + textAdj[0], y + textAdj[1], angle, rotationX, rotationY );
    }
    public static void drawRotatedString ( final AttributedString text, final Graphics2D g2, final float x, final float y, final TextAnchor textAnchor, final double angle, final TextAnchor rotationAnchor ) {
        ParamChecks.nullNotPermitted ( text, "text" );
        final float[] textAdj = deriveTextBoundsAnchorOffsets ( g2, text, textAnchor, null );
        final float[] rotateAdj = deriveRotationAnchorOffsets ( g2, text, rotationAnchor );
        drawRotatedString ( text, g2, x + textAdj[0], y + textAdj[1], angle, x + textAdj[0] + rotateAdj[0], y + textAdj[1] + rotateAdj[1] );
    }
    private static float[] deriveTextBoundsAnchorOffsets ( final Graphics2D g2, final AttributedString text, final TextAnchor anchor, final Rectangle2D textBounds ) {
        final TextLayout layout = new TextLayout ( text.getIterator(), g2.getFontRenderContext() );
        final Rectangle2D bounds = layout.getBounds();
        final float[] result = new float[3];
        final float ascent = layout.getAscent();
        result[2] = -ascent;
        final float halfAscent = ascent / 2.0f;
        final float descent = layout.getDescent();
        final float leading = layout.getLeading();
        float xAdj = 0.0f;
        float yAdj = 0.0f;
        if ( isHorizontalCenter ( anchor ) ) {
            xAdj = ( float ) ( -bounds.getWidth() ) / 2.0f;
        } else if ( isHorizontalRight ( anchor ) ) {
            xAdj = ( float ) ( -bounds.getWidth() );
        }
        if ( isTop ( anchor ) ) {
            yAdj = ( float ) bounds.getHeight();
        } else if ( isHalfAscent ( anchor ) ) {
            yAdj = halfAscent;
        } else if ( isHalfHeight ( anchor ) ) {
            yAdj = -descent - leading + ( float ) ( bounds.getHeight() / 2.0 );
        } else if ( isBaseline ( anchor ) ) {
            yAdj = 0.0f;
        } else if ( isBottom ( anchor ) ) {
            yAdj = -descent - leading;
        }
        if ( textBounds != null ) {
            textBounds.setRect ( bounds );
        }
        result[0] = xAdj;
        result[1] = yAdj;
        return result;
    }
    private static float[] deriveRotationAnchorOffsets ( final Graphics2D g2, final AttributedString text, final TextAnchor anchor ) {
        final float[] result = new float[2];
        final TextLayout layout = new TextLayout ( text.getIterator(), g2.getFontRenderContext() );
        final Rectangle2D bounds = layout.getBounds();
        final float ascent = layout.getAscent();
        final float halfAscent = ascent / 2.0f;
        final float descent = layout.getDescent();
        final float leading = layout.getLeading();
        float xAdj = 0.0f;
        float yAdj = 0.0f;
        if ( isHorizontalLeft ( anchor ) ) {
            xAdj = 0.0f;
        } else if ( isHorizontalCenter ( anchor ) ) {
            xAdj = ( float ) bounds.getWidth() / 2.0f;
        } else if ( isHorizontalRight ( anchor ) ) {
            xAdj = ( float ) bounds.getWidth();
        }
        if ( isTop ( anchor ) ) {
            yAdj = descent + leading - ( float ) bounds.getHeight();
        } else if ( isHalfHeight ( anchor ) ) {
            yAdj = descent + leading - ( float ) ( bounds.getHeight() / 2.0 );
        } else if ( isHalfAscent ( anchor ) ) {
            yAdj = -halfAscent;
        } else if ( isBaseline ( anchor ) ) {
            yAdj = 0.0f;
        } else if ( isBottom ( anchor ) ) {
            yAdj = descent + leading;
        }
        result[0] = xAdj;
        result[1] = yAdj;
        return result;
    }
    private static boolean isTop ( final TextAnchor anchor ) {
        return anchor.equals ( ( Object ) TextAnchor.TOP_LEFT ) || anchor.equals ( ( Object ) TextAnchor.TOP_CENTER ) || anchor.equals ( ( Object ) TextAnchor.TOP_RIGHT );
    }
    private static boolean isBaseline ( final TextAnchor anchor ) {
        return anchor.equals ( ( Object ) TextAnchor.BASELINE_LEFT ) || anchor.equals ( ( Object ) TextAnchor.BASELINE_CENTER ) || anchor.equals ( ( Object ) TextAnchor.BASELINE_RIGHT );
    }
    private static boolean isHalfAscent ( final TextAnchor anchor ) {
        return anchor.equals ( ( Object ) TextAnchor.HALF_ASCENT_LEFT ) || anchor.equals ( ( Object ) TextAnchor.HALF_ASCENT_CENTER ) || anchor.equals ( ( Object ) TextAnchor.HALF_ASCENT_RIGHT );
    }
    private static boolean isHalfHeight ( final TextAnchor anchor ) {
        return anchor.equals ( ( Object ) TextAnchor.CENTER_LEFT ) || anchor.equals ( ( Object ) TextAnchor.CENTER ) || anchor.equals ( ( Object ) TextAnchor.CENTER_RIGHT );
    }
    private static boolean isBottom ( final TextAnchor anchor ) {
        return anchor.equals ( ( Object ) TextAnchor.BOTTOM_LEFT ) || anchor.equals ( ( Object ) TextAnchor.BOTTOM_CENTER ) || anchor.equals ( ( Object ) TextAnchor.BOTTOM_RIGHT );
    }
    private static boolean isHorizontalLeft ( final TextAnchor anchor ) {
        return anchor.equals ( ( Object ) TextAnchor.TOP_LEFT ) || anchor.equals ( ( Object ) TextAnchor.CENTER_LEFT ) || anchor.equals ( ( Object ) TextAnchor.HALF_ASCENT_LEFT ) || anchor.equals ( ( Object ) TextAnchor.BASELINE_LEFT ) || anchor.equals ( ( Object ) TextAnchor.BOTTOM_LEFT );
    }
    private static boolean isHorizontalCenter ( final TextAnchor anchor ) {
        return anchor.equals ( ( Object ) TextAnchor.TOP_CENTER ) || anchor.equals ( ( Object ) TextAnchor.CENTER ) || anchor.equals ( ( Object ) TextAnchor.HALF_ASCENT_CENTER ) || anchor.equals ( ( Object ) TextAnchor.BASELINE_CENTER ) || anchor.equals ( ( Object ) TextAnchor.BOTTOM_CENTER );
    }
    private static boolean isHorizontalRight ( final TextAnchor anchor ) {
        return anchor.equals ( ( Object ) TextAnchor.TOP_RIGHT ) || anchor.equals ( ( Object ) TextAnchor.CENTER_RIGHT ) || anchor.equals ( ( Object ) TextAnchor.HALF_ASCENT_RIGHT ) || anchor.equals ( ( Object ) TextAnchor.BASELINE_RIGHT ) || anchor.equals ( ( Object ) TextAnchor.BOTTOM_RIGHT );
    }
}
