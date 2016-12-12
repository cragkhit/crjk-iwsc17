package org.jfree.chart.block;
import java.util.List;
import java.awt.geom.Rectangle2D;
import org.jfree.ui.Size2D;
import java.awt.Graphics2D;
import java.io.Serializable;
public class CenterArrangement implements Arrangement, Serializable {
    private static final long serialVersionUID = -353308149220382047L;
    @Override
    public void add ( final Block block, final Object key ) {
    }
    @Override
    public Size2D arrange ( final BlockContainer container, final Graphics2D g2, final RectangleConstraint constraint ) {
        final LengthConstraintType w = constraint.getWidthConstraintType();
        final LengthConstraintType h = constraint.getHeightConstraintType();
        if ( w == LengthConstraintType.NONE ) {
            if ( h == LengthConstraintType.NONE ) {
                return this.arrangeNN ( container, g2 );
            }
            if ( h == LengthConstraintType.FIXED ) {
                throw new RuntimeException ( "Not implemented." );
            }
            if ( h == LengthConstraintType.RANGE ) {
                throw new RuntimeException ( "Not implemented." );
            }
        } else if ( w == LengthConstraintType.FIXED ) {
            if ( h == LengthConstraintType.NONE ) {
                return this.arrangeFN ( container, g2, constraint );
            }
            if ( h == LengthConstraintType.FIXED ) {
                throw new RuntimeException ( "Not implemented." );
            }
            if ( h == LengthConstraintType.RANGE ) {
                throw new RuntimeException ( "Not implemented." );
            }
        } else if ( w == LengthConstraintType.RANGE ) {
            if ( h == LengthConstraintType.NONE ) {
                return this.arrangeRN ( container, g2, constraint );
            }
            if ( h == LengthConstraintType.FIXED ) {
                return this.arrangeRF ( container, g2, constraint );
            }
            if ( h == LengthConstraintType.RANGE ) {
                return this.arrangeRR ( container, g2, constraint );
            }
        }
        throw new IllegalArgumentException ( "Unknown LengthConstraintType." );
    }
    protected Size2D arrangeFN ( final BlockContainer container, final Graphics2D g2, final RectangleConstraint constraint ) {
        final List blocks = container.getBlocks();
        final Block b = blocks.get ( 0 );
        final Size2D s = b.arrange ( g2, RectangleConstraint.NONE );
        final double width = constraint.getWidth();
        final Rectangle2D bounds = new Rectangle2D.Double ( ( width - s.width ) / 2.0, 0.0, s.width, s.height );
        b.setBounds ( bounds );
        return new Size2D ( ( width - s.width ) / 2.0, s.height );
    }
    protected Size2D arrangeFR ( final BlockContainer container, final Graphics2D g2, final RectangleConstraint constraint ) {
        final Size2D s = this.arrangeFN ( container, g2, constraint );
        if ( constraint.getHeightRange().contains ( s.height ) ) {
            return s;
        }
        final RectangleConstraint c = constraint.toFixedHeight ( constraint.getHeightRange().constrain ( s.getHeight() ) );
        return this.arrangeFF ( container, g2, c );
    }
    protected Size2D arrangeFF ( final BlockContainer container, final Graphics2D g2, final RectangleConstraint constraint ) {
        return this.arrangeFN ( container, g2, constraint );
    }
    protected Size2D arrangeRR ( final BlockContainer container, final Graphics2D g2, final RectangleConstraint constraint ) {
        final Size2D s1 = this.arrangeNN ( container, g2 );
        if ( constraint.getWidthRange().contains ( s1.width ) ) {
            return s1;
        }
        final RectangleConstraint c = constraint.toFixedWidth ( constraint.getWidthRange().getUpperBound() );
        return this.arrangeFR ( container, g2, c );
    }
    protected Size2D arrangeRF ( final BlockContainer container, final Graphics2D g2, final RectangleConstraint constraint ) {
        final Size2D s = this.arrangeNF ( container, g2, constraint );
        if ( constraint.getWidthRange().contains ( s.width ) ) {
            return s;
        }
        final RectangleConstraint c = constraint.toFixedWidth ( constraint.getWidthRange().constrain ( s.getWidth() ) );
        return this.arrangeFF ( container, g2, c );
    }
    protected Size2D arrangeRN ( final BlockContainer container, final Graphics2D g2, final RectangleConstraint constraint ) {
        final Size2D s1 = this.arrangeNN ( container, g2 );
        if ( constraint.getWidthRange().contains ( s1.width ) ) {
            return s1;
        }
        final RectangleConstraint c = constraint.toFixedWidth ( constraint.getWidthRange().getUpperBound() );
        return this.arrangeFN ( container, g2, c );
    }
    protected Size2D arrangeNN ( final BlockContainer container, final Graphics2D g2 ) {
        final List blocks = container.getBlocks();
        final Block b = blocks.get ( 0 );
        final Size2D s = b.arrange ( g2, RectangleConstraint.NONE );
        b.setBounds ( new Rectangle2D.Double ( 0.0, 0.0, s.width, s.height ) );
        return new Size2D ( s.width, s.height );
    }
    protected Size2D arrangeNF ( final BlockContainer container, final Graphics2D g2, final RectangleConstraint constraint ) {
        return this.arrangeNN ( container, g2 );
    }
    @Override
    public void clear() {
    }
    @Override
    public boolean equals ( final Object obj ) {
        return obj == this || obj instanceof CenterArrangement;
    }
}
