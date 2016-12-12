package org.jfree.chart.block;
import org.jfree.util.ObjectUtilities;
import org.jfree.data.Range;
import java.awt.geom.Rectangle2D;
import org.jfree.ui.Size2D;
import java.awt.Graphics2D;
import org.jfree.ui.RectangleEdge;
import java.io.Serializable;
public class BorderArrangement implements Arrangement, Serializable {
    private static final long serialVersionUID = 506071142274883745L;
    private Block centerBlock;
    private Block topBlock;
    private Block bottomBlock;
    private Block leftBlock;
    private Block rightBlock;
    @Override
    public void add ( final Block block, final Object key ) {
        if ( ! ( key instanceof RectangleEdge ) ) {
            this.centerBlock = block;
        } else {
            final RectangleEdge edge = ( RectangleEdge ) key;
            if ( edge == RectangleEdge.TOP ) {
                this.topBlock = block;
            } else if ( edge == RectangleEdge.BOTTOM ) {
                this.bottomBlock = block;
            } else if ( edge == RectangleEdge.LEFT ) {
                this.leftBlock = block;
            } else if ( edge == RectangleEdge.RIGHT ) {
                this.rightBlock = block;
            }
        }
    }
    @Override
    public Size2D arrange ( final BlockContainer container, final Graphics2D g2, final RectangleConstraint constraint ) {
        final RectangleConstraint contentConstraint = container.toContentConstraint ( constraint );
        Size2D contentSize = null;
        final LengthConstraintType w = contentConstraint.getWidthConstraintType();
        final LengthConstraintType h = contentConstraint.getHeightConstraintType();
        if ( w == LengthConstraintType.NONE ) {
            if ( h == LengthConstraintType.NONE ) {
                contentSize = this.arrangeNN ( container, g2 );
            } else {
                if ( h == LengthConstraintType.FIXED ) {
                    throw new RuntimeException ( "Not implemented." );
                }
                if ( h == LengthConstraintType.RANGE ) {
                    throw new RuntimeException ( "Not implemented." );
                }
            }
        } else if ( w == LengthConstraintType.FIXED ) {
            if ( h == LengthConstraintType.NONE ) {
                contentSize = this.arrangeFN ( container, g2, constraint.getWidth() );
            } else if ( h == LengthConstraintType.FIXED ) {
                contentSize = this.arrangeFF ( container, g2, constraint );
            } else if ( h == LengthConstraintType.RANGE ) {
                contentSize = this.arrangeFR ( container, g2, constraint );
            }
        } else if ( w == LengthConstraintType.RANGE ) {
            if ( h == LengthConstraintType.NONE ) {
                throw new RuntimeException ( "Not implemented." );
            }
            if ( h == LengthConstraintType.FIXED ) {
                throw new RuntimeException ( "Not implemented." );
            }
            if ( h == LengthConstraintType.RANGE ) {
                contentSize = this.arrangeRR ( container, constraint.getWidthRange(), constraint.getHeightRange(), g2 );
            }
        }
        assert contentSize != null;
        return new Size2D ( container.calculateTotalWidth ( contentSize.getWidth() ), container.calculateTotalHeight ( contentSize.getHeight() ) );
    }
    protected Size2D arrangeNN ( final BlockContainer container, final Graphics2D g2 ) {
        final double[] w = new double[5];
        final double[] h = new double[5];
        if ( this.topBlock != null ) {
            final Size2D size = this.topBlock.arrange ( g2, RectangleConstraint.NONE );
            w[0] = size.width;
            h[0] = size.height;
        }
        if ( this.bottomBlock != null ) {
            final Size2D size = this.bottomBlock.arrange ( g2, RectangleConstraint.NONE );
            w[1] = size.width;
            h[1] = size.height;
        }
        if ( this.leftBlock != null ) {
            final Size2D size = this.leftBlock.arrange ( g2, RectangleConstraint.NONE );
            w[2] = size.width;
            h[2] = size.height;
        }
        if ( this.rightBlock != null ) {
            final Size2D size = this.rightBlock.arrange ( g2, RectangleConstraint.NONE );
            w[3] = size.width;
            h[3] = size.height;
        }
        h[3] = ( h[2] = Math.max ( h[2], h[3] ) );
        if ( this.centerBlock != null ) {
            final Size2D size = this.centerBlock.arrange ( g2, RectangleConstraint.NONE );
            w[4] = size.width;
            h[4] = size.height;
        }
        final double width = Math.max ( w[0], Math.max ( w[1], w[2] + w[4] + w[3] ) );
        final double centerHeight = Math.max ( h[2], Math.max ( h[3], h[4] ) );
        final double height = h[0] + h[1] + centerHeight;
        if ( this.topBlock != null ) {
            this.topBlock.setBounds ( new Rectangle2D.Double ( 0.0, 0.0, width, h[0] ) );
        }
        if ( this.bottomBlock != null ) {
            this.bottomBlock.setBounds ( new Rectangle2D.Double ( 0.0, height - h[1], width, h[1] ) );
        }
        if ( this.leftBlock != null ) {
            this.leftBlock.setBounds ( new Rectangle2D.Double ( 0.0, h[0], w[2], centerHeight ) );
        }
        if ( this.rightBlock != null ) {
            this.rightBlock.setBounds ( new Rectangle2D.Double ( width - w[3], h[0], w[3], centerHeight ) );
        }
        if ( this.centerBlock != null ) {
            this.centerBlock.setBounds ( new Rectangle2D.Double ( w[2], h[0], width - w[2] - w[3], centerHeight ) );
        }
        return new Size2D ( width, height );
    }
    protected Size2D arrangeFR ( final BlockContainer container, final Graphics2D g2, final RectangleConstraint constraint ) {
        final Size2D size1 = this.arrangeFN ( container, g2, constraint.getWidth() );
        if ( constraint.getHeightRange().contains ( size1.getHeight() ) ) {
            return size1;
        }
        final double h = constraint.getHeightRange().constrain ( size1.getHeight() );
        final RectangleConstraint c2 = constraint.toFixedHeight ( h );
        return this.arrange ( container, g2, c2 );
    }
    protected Size2D arrangeFN ( final BlockContainer container, final Graphics2D g2, final double width ) {
        final double[] w = new double[5];
        final double[] h = new double[5];
        final RectangleConstraint c1 = new RectangleConstraint ( width, null, LengthConstraintType.FIXED, 0.0, null, LengthConstraintType.NONE );
        if ( this.topBlock != null ) {
            final Size2D size = this.topBlock.arrange ( g2, c1 );
            w[0] = size.width;
            h[0] = size.height;
        }
        if ( this.bottomBlock != null ) {
            final Size2D size = this.bottomBlock.arrange ( g2, c1 );
            w[1] = size.width;
            h[1] = size.height;
        }
        final RectangleConstraint c2 = new RectangleConstraint ( 0.0, new Range ( 0.0, width ), LengthConstraintType.RANGE, 0.0, null, LengthConstraintType.NONE );
        if ( this.leftBlock != null ) {
            final Size2D size2 = this.leftBlock.arrange ( g2, c2 );
            w[2] = size2.width;
            h[2] = size2.height;
        }
        if ( this.rightBlock != null ) {
            final double maxW = Math.max ( width - w[2], 0.0 );
            final RectangleConstraint c3 = new RectangleConstraint ( 0.0, new Range ( Math.min ( w[2], maxW ), maxW ), LengthConstraintType.RANGE, 0.0, null, LengthConstraintType.NONE );
            final Size2D size3 = this.rightBlock.arrange ( g2, c3 );
            w[3] = size3.width;
            h[3] = size3.height;
        }
        h[3] = ( h[2] = Math.max ( h[2], h[3] ) );
        if ( this.centerBlock != null ) {
            final RectangleConstraint c4 = new RectangleConstraint ( width - w[2] - w[3], null, LengthConstraintType.FIXED, 0.0, null, LengthConstraintType.NONE );
            final Size2D size4 = this.centerBlock.arrange ( g2, c4 );
            w[4] = size4.width;
            h[4] = size4.height;
        }
        final double height = h[0] + h[1] + Math.max ( h[2], Math.max ( h[3], h[4] ) );
        return this.arrange ( container, g2, new RectangleConstraint ( width, height ) );
    }
    protected Size2D arrangeRR ( final BlockContainer container, final Range widthRange, final Range heightRange, final Graphics2D g2 ) {
        final double[] w = new double[5];
        final double[] h = new double[5];
        if ( this.topBlock != null ) {
            final RectangleConstraint c1 = new RectangleConstraint ( widthRange, heightRange );
            final Size2D size = this.topBlock.arrange ( g2, c1 );
            w[0] = size.width;
            h[0] = size.height;
        }
        if ( this.bottomBlock != null ) {
            final Range heightRange2 = Range.shift ( heightRange, -h[0], false );
            final RectangleConstraint c2 = new RectangleConstraint ( widthRange, heightRange2 );
            final Size2D size2 = this.bottomBlock.arrange ( g2, c2 );
            w[1] = size2.width;
            h[1] = size2.height;
        }
        final Range heightRange3 = Range.shift ( heightRange, - ( h[0] + h[1] ) );
        if ( this.leftBlock != null ) {
            final RectangleConstraint c3 = new RectangleConstraint ( widthRange, heightRange3 );
            final Size2D size2 = this.leftBlock.arrange ( g2, c3 );
            w[2] = size2.width;
            h[2] = size2.height;
        }
        final Range widthRange2 = Range.shift ( widthRange, -w[2], false );
        if ( this.rightBlock != null ) {
            final RectangleConstraint c4 = new RectangleConstraint ( widthRange2, heightRange3 );
            final Size2D size3 = this.rightBlock.arrange ( g2, c4 );
            w[3] = size3.width;
            h[3] = size3.height;
        }
        h[3] = ( h[2] = Math.max ( h[2], h[3] ) );
        final Range widthRange3 = Range.shift ( widthRange, - ( w[2] + w[3] ), false );
        if ( this.centerBlock != null ) {
            final RectangleConstraint c5 = new RectangleConstraint ( widthRange3, heightRange3 );
            final Size2D size4 = this.centerBlock.arrange ( g2, c5 );
            w[4] = size4.width;
            h[4] = size4.height;
        }
        final double width = Math.max ( w[0], Math.max ( w[1], w[2] + w[4] + w[3] ) );
        final double height = h[0] + h[1] + Math.max ( h[2], Math.max ( h[3], h[4] ) );
        if ( this.topBlock != null ) {
            this.topBlock.setBounds ( new Rectangle2D.Double ( 0.0, 0.0, width, h[0] ) );
        }
        if ( this.bottomBlock != null ) {
            this.bottomBlock.setBounds ( new Rectangle2D.Double ( 0.0, height - h[1], width, h[1] ) );
        }
        if ( this.leftBlock != null ) {
            this.leftBlock.setBounds ( new Rectangle2D.Double ( 0.0, h[0], w[2], h[2] ) );
        }
        if ( this.rightBlock != null ) {
            this.rightBlock.setBounds ( new Rectangle2D.Double ( width - w[3], h[0], w[3], h[3] ) );
        }
        if ( this.centerBlock != null ) {
            this.centerBlock.setBounds ( new Rectangle2D.Double ( w[2], h[0], width - w[2] - w[3], height - h[0] - h[1] ) );
        }
        return new Size2D ( width, height );
    }
    protected Size2D arrangeFF ( final BlockContainer container, final Graphics2D g2, final RectangleConstraint constraint ) {
        final double[] w = new double[5];
        final double[] h = new double[5];
        w[0] = constraint.getWidth();
        if ( this.topBlock != null ) {
            final RectangleConstraint c1 = new RectangleConstraint ( w[0], null, LengthConstraintType.FIXED, 0.0, new Range ( 0.0, constraint.getHeight() ), LengthConstraintType.RANGE );
            final Size2D size = this.topBlock.arrange ( g2, c1 );
            h[0] = size.height;
        }
        w[1] = w[0];
        if ( this.bottomBlock != null ) {
            final RectangleConstraint c2 = new RectangleConstraint ( w[0], null, LengthConstraintType.FIXED, 0.0, new Range ( 0.0, constraint.getHeight() - h[0] ), LengthConstraintType.RANGE );
            final Size2D size = this.bottomBlock.arrange ( g2, c2 );
            h[1] = size.height;
        }
        h[2] = constraint.getHeight() - h[1] - h[0];
        if ( this.leftBlock != null ) {
            final RectangleConstraint c3 = new RectangleConstraint ( 0.0, new Range ( 0.0, constraint.getWidth() ), LengthConstraintType.RANGE, h[2], null, LengthConstraintType.FIXED );
            final Size2D size = this.leftBlock.arrange ( g2, c3 );
            w[2] = size.width;
        }
        h[3] = h[2];
        if ( this.rightBlock != null ) {
            final RectangleConstraint c4 = new RectangleConstraint ( 0.0, new Range ( 0.0, Math.max ( constraint.getWidth() - w[2], 0.0 ) ), LengthConstraintType.RANGE, h[2], null, LengthConstraintType.FIXED );
            final Size2D size = this.rightBlock.arrange ( g2, c4 );
            w[3] = size.width;
        }
        h[4] = h[2];
        w[4] = constraint.getWidth() - w[3] - w[2];
        final RectangleConstraint c5 = new RectangleConstraint ( w[4], h[4] );
        if ( this.centerBlock != null ) {
            this.centerBlock.arrange ( g2, c5 );
        }
        if ( this.topBlock != null ) {
            this.topBlock.setBounds ( new Rectangle2D.Double ( 0.0, 0.0, w[0], h[0] ) );
        }
        if ( this.bottomBlock != null ) {
            this.bottomBlock.setBounds ( new Rectangle2D.Double ( 0.0, h[0] + h[2], w[1], h[1] ) );
        }
        if ( this.leftBlock != null ) {
            this.leftBlock.setBounds ( new Rectangle2D.Double ( 0.0, h[0], w[2], h[2] ) );
        }
        if ( this.rightBlock != null ) {
            this.rightBlock.setBounds ( new Rectangle2D.Double ( w[2] + w[4], h[0], w[3], h[3] ) );
        }
        if ( this.centerBlock != null ) {
            this.centerBlock.setBounds ( new Rectangle2D.Double ( w[2], h[0], w[4], h[4] ) );
        }
        return new Size2D ( constraint.getWidth(), constraint.getHeight() );
    }
    @Override
    public void clear() {
        this.centerBlock = null;
        this.topBlock = null;
        this.bottomBlock = null;
        this.leftBlock = null;
        this.rightBlock = null;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof BorderArrangement ) ) {
            return false;
        }
        final BorderArrangement that = ( BorderArrangement ) obj;
        return ObjectUtilities.equal ( ( Object ) this.topBlock, ( Object ) that.topBlock ) && ObjectUtilities.equal ( ( Object ) this.bottomBlock, ( Object ) that.bottomBlock ) && ObjectUtilities.equal ( ( Object ) this.leftBlock, ( Object ) that.leftBlock ) && ObjectUtilities.equal ( ( Object ) this.rightBlock, ( Object ) that.rightBlock ) && ObjectUtilities.equal ( ( Object ) this.centerBlock, ( Object ) that.centerBlock );
    }
}
