package org.jfree.chart.title;
import org.jfree.ui.TextAnchor;
import java.awt.geom.Rectangle2D;
import java.awt.FontMetrics;
import org.jfree.text.TextUtilities;
import org.jfree.data.Range;
import org.jfree.chart.block.LengthConstraintType;
import org.jfree.ui.Size2D;
import org.jfree.chart.block.RectangleConstraint;
import java.awt.Graphics2D;
public class ShortTextTitle extends TextTitle {
    public ShortTextTitle ( final String text ) {
        this.setText ( text );
    }
    @Override
    public Size2D arrange ( final Graphics2D g2, final RectangleConstraint constraint ) {
        final RectangleConstraint cc = this.toContentConstraint ( constraint );
        final LengthConstraintType w = cc.getWidthConstraintType();
        final LengthConstraintType h = cc.getHeightConstraintType();
        Size2D contentSize = null;
        if ( w == LengthConstraintType.NONE ) {
            if ( h == LengthConstraintType.NONE ) {
                contentSize = this.arrangeNN ( g2 );
            } else {
                if ( h == LengthConstraintType.RANGE ) {
                    throw new RuntimeException ( "Not yet implemented." );
                }
                if ( h == LengthConstraintType.FIXED ) {
                    throw new RuntimeException ( "Not yet implemented." );
                }
            }
        } else if ( w == LengthConstraintType.RANGE ) {
            if ( h == LengthConstraintType.NONE ) {
                contentSize = this.arrangeRN ( g2, cc.getWidthRange() );
            } else if ( h == LengthConstraintType.RANGE ) {
                contentSize = this.arrangeRR ( g2, cc.getWidthRange(), cc.getHeightRange() );
            } else if ( h == LengthConstraintType.FIXED ) {
                throw new RuntimeException ( "Not yet implemented." );
            }
        } else if ( w == LengthConstraintType.FIXED ) {
            if ( h == LengthConstraintType.NONE ) {
                contentSize = this.arrangeFN ( g2, cc.getWidth() );
            } else {
                if ( h == LengthConstraintType.RANGE ) {
                    throw new RuntimeException ( "Not yet implemented." );
                }
                if ( h == LengthConstraintType.FIXED ) {
                    throw new RuntimeException ( "Not yet implemented." );
                }
            }
        }
        assert contentSize != null;
        if ( contentSize.width <= 0.0 || contentSize.height <= 0.0 ) {
            return new Size2D ( 0.0, 0.0 );
        }
        return new Size2D ( this.calculateTotalWidth ( contentSize.getWidth() ), this.calculateTotalHeight ( contentSize.getHeight() ) );
    }
    @Override
    protected Size2D arrangeNN ( final Graphics2D g2 ) {
        final Range max = new Range ( 0.0, 3.4028234663852886E38 );
        return this.arrangeRR ( g2, max, max );
    }
    @Override
    protected Size2D arrangeRN ( final Graphics2D g2, final Range widthRange ) {
        final Size2D s = this.arrangeNN ( g2 );
        if ( widthRange.contains ( s.getWidth() ) ) {
            return s;
        }
        final double ww = widthRange.constrain ( s.getWidth() );
        return this.arrangeFN ( g2, ww );
    }
    @Override
    protected Size2D arrangeFN ( final Graphics2D g2, final double w ) {
        g2.setFont ( this.getFont() );
        final FontMetrics fm = g2.getFontMetrics ( this.getFont() );
        final Rectangle2D bounds = TextUtilities.getTextBounds ( this.getText(), g2, fm );
        if ( bounds.getWidth() <= w ) {
            return new Size2D ( w, bounds.getHeight() );
        }
        return new Size2D ( 0.0, 0.0 );
    }
    @Override
    protected Size2D arrangeRR ( final Graphics2D g2, final Range widthRange, final Range heightRange ) {
        g2.setFont ( this.getFont() );
        final FontMetrics fm = g2.getFontMetrics ( this.getFont() );
        final Rectangle2D bounds = TextUtilities.getTextBounds ( this.getText(), g2, fm );
        if ( bounds.getWidth() <= widthRange.getUpperBound() && bounds.getHeight() <= heightRange.getUpperBound() ) {
            return new Size2D ( bounds.getWidth(), bounds.getHeight() );
        }
        return new Size2D ( 0.0, 0.0 );
    }
    @Override
    public Object draw ( final Graphics2D g2, Rectangle2D area, final Object params ) {
        if ( area.isEmpty() ) {
            return null;
        }
        area = this.trimMargin ( area );
        this.drawBorder ( g2, area );
        area = this.trimBorder ( area );
        area = this.trimPadding ( area );
        g2.setFont ( this.getFont() );
        g2.setPaint ( this.getPaint() );
        TextUtilities.drawAlignedString ( this.getText(), g2, ( float ) area.getMinX(), ( float ) area.getMinY(), TextAnchor.TOP_LEFT );
        return null;
    }
}
