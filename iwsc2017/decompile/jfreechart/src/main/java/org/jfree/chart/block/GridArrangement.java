package org.jfree.chart.block;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;
import org.jfree.ui.Size2D;
import java.awt.Graphics2D;
import java.io.Serializable;
public class GridArrangement implements Arrangement, Serializable {
    private static final long serialVersionUID = -2563758090144655938L;
    private int rows;
    private int columns;
    public GridArrangement ( final int rows, final int columns ) {
        this.rows = rows;
        this.columns = columns;
    }
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
                return this.arrangeNF ( container, g2, constraint );
            }
            if ( h == LengthConstraintType.RANGE ) {
                return this.arrangeNR ( container, g2, constraint );
            }
        } else if ( w == LengthConstraintType.FIXED ) {
            if ( h == LengthConstraintType.NONE ) {
                return this.arrangeFN ( container, g2, constraint );
            }
            if ( h == LengthConstraintType.FIXED ) {
                return this.arrangeFF ( container, g2, constraint );
            }
            if ( h == LengthConstraintType.RANGE ) {
                return this.arrangeFR ( container, g2, constraint );
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
        throw new RuntimeException ( "Should never get to here!" );
    }
    protected Size2D arrangeNN ( final BlockContainer container, final Graphics2D g2 ) {
        double maxW = 0.0;
        double maxH = 0.0;
        final List blocks = container.getBlocks();
        for ( final Block b : blocks ) {
            if ( b != null ) {
                final Size2D s = b.arrange ( g2, RectangleConstraint.NONE );
                maxW = Math.max ( maxW, s.width );
                maxH = Math.max ( maxH, s.height );
            }
        }
        final double width = this.columns * maxW;
        final double height = this.rows * maxH;
        final RectangleConstraint c = new RectangleConstraint ( width, height );
        return this.arrangeFF ( container, g2, c );
    }
    protected Size2D arrangeFF ( final BlockContainer container, final Graphics2D g2, final RectangleConstraint constraint ) {
        final double width = constraint.getWidth() / this.columns;
        final double height = constraint.getHeight() / this.rows;
        final List blocks = container.getBlocks();
        for ( int c = 0; c < this.columns; ++c ) {
            for ( int r = 0; r < this.rows; ++r ) {
                final int index = r * this.columns + c;
                if ( index >= blocks.size() ) {
                    break;
                }
                final Block b = blocks.get ( index );
                if ( b != null ) {
                    b.setBounds ( new Rectangle2D.Double ( c * width, r * height, width, height ) );
                }
            }
        }
        return new Size2D ( this.columns * width, this.rows * height );
    }
    protected Size2D arrangeFR ( final BlockContainer container, final Graphics2D g2, final RectangleConstraint constraint ) {
        final RectangleConstraint c1 = constraint.toUnconstrainedHeight();
        final Size2D size1 = this.arrange ( container, g2, c1 );
        if ( constraint.getHeightRange().contains ( size1.getHeight() ) ) {
            return size1;
        }
        final double h = constraint.getHeightRange().constrain ( size1.getHeight() );
        final RectangleConstraint c2 = constraint.toFixedHeight ( h );
        return this.arrange ( container, g2, c2 );
    }
    protected Size2D arrangeRF ( final BlockContainer container, final Graphics2D g2, final RectangleConstraint constraint ) {
        final RectangleConstraint c1 = constraint.toUnconstrainedWidth();
        final Size2D size1 = this.arrange ( container, g2, c1 );
        if ( constraint.getWidthRange().contains ( size1.getWidth() ) ) {
            return size1;
        }
        final double w = constraint.getWidthRange().constrain ( size1.getWidth() );
        final RectangleConstraint c2 = constraint.toFixedWidth ( w );
        return this.arrange ( container, g2, c2 );
    }
    protected Size2D arrangeRN ( final BlockContainer container, final Graphics2D g2, final RectangleConstraint constraint ) {
        final RectangleConstraint c1 = constraint.toUnconstrainedWidth();
        final Size2D size1 = this.arrange ( container, g2, c1 );
        if ( constraint.getWidthRange().contains ( size1.getWidth() ) ) {
            return size1;
        }
        final double w = constraint.getWidthRange().constrain ( size1.getWidth() );
        final RectangleConstraint c2 = constraint.toFixedWidth ( w );
        return this.arrange ( container, g2, c2 );
    }
    protected Size2D arrangeNR ( final BlockContainer container, final Graphics2D g2, final RectangleConstraint constraint ) {
        final RectangleConstraint c1 = constraint.toUnconstrainedHeight();
        final Size2D size1 = this.arrange ( container, g2, c1 );
        if ( constraint.getHeightRange().contains ( size1.getHeight() ) ) {
            return size1;
        }
        final double h = constraint.getHeightRange().constrain ( size1.getHeight() );
        final RectangleConstraint c2 = constraint.toFixedHeight ( h );
        return this.arrange ( container, g2, c2 );
    }
    protected Size2D arrangeRR ( final BlockContainer container, final Graphics2D g2, final RectangleConstraint constraint ) {
        final Size2D size1 = this.arrange ( container, g2, RectangleConstraint.NONE );
        if ( constraint.getWidthRange().contains ( size1.getWidth() ) ) {
            if ( constraint.getHeightRange().contains ( size1.getHeight() ) ) {
                return size1;
            }
            final double h = constraint.getHeightRange().constrain ( size1.getHeight() );
            final RectangleConstraint cc = new RectangleConstraint ( size1.getWidth(), h );
            return this.arrangeFF ( container, g2, cc );
        } else {
            if ( constraint.getHeightRange().contains ( size1.getHeight() ) ) {
                final double w = constraint.getWidthRange().constrain ( size1.getWidth() );
                final RectangleConstraint cc = new RectangleConstraint ( w, size1.getHeight() );
                return this.arrangeFF ( container, g2, cc );
            }
            final double w = constraint.getWidthRange().constrain ( size1.getWidth() );
            final double h2 = constraint.getHeightRange().constrain ( size1.getHeight() );
            final RectangleConstraint cc2 = new RectangleConstraint ( w, h2 );
            return this.arrangeFF ( container, g2, cc2 );
        }
    }
    protected Size2D arrangeFN ( final BlockContainer container, final Graphics2D g2, final RectangleConstraint constraint ) {
        final double width = constraint.getWidth() / this.columns;
        final RectangleConstraint bc = constraint.toFixedWidth ( width );
        final List blocks = container.getBlocks();
        double maxH = 0.0;
        for ( int r = 0; r < this.rows; ++r ) {
            for ( int c = 0; c < this.columns; ++c ) {
                final int index = r * this.columns + c;
                if ( index >= blocks.size() ) {
                    break;
                }
                final Block b = blocks.get ( index );
                if ( b != null ) {
                    final Size2D s = b.arrange ( g2, bc );
                    maxH = Math.max ( maxH, s.getHeight() );
                }
            }
        }
        final RectangleConstraint cc = constraint.toFixedHeight ( maxH * this.rows );
        return this.arrange ( container, g2, cc );
    }
    protected Size2D arrangeNF ( final BlockContainer container, final Graphics2D g2, final RectangleConstraint constraint ) {
        final double height = constraint.getHeight() / this.rows;
        final RectangleConstraint bc = constraint.toFixedHeight ( height );
        final List blocks = container.getBlocks();
        double maxW = 0.0;
        for ( int r = 0; r < this.rows; ++r ) {
            for ( int c = 0; c < this.columns; ++c ) {
                final int index = r * this.columns + c;
                if ( index >= blocks.size() ) {
                    break;
                }
                final Block b = blocks.get ( index );
                if ( b != null ) {
                    final Size2D s = b.arrange ( g2, bc );
                    maxW = Math.max ( maxW, s.getWidth() );
                }
            }
        }
        final RectangleConstraint cc = constraint.toFixedWidth ( maxW * this.columns );
        return this.arrange ( container, g2, cc );
    }
    @Override
    public void clear() {
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof GridArrangement ) ) {
            return false;
        }
        final GridArrangement that = ( GridArrangement ) obj;
        return this.columns == that.columns && this.rows == that.rows;
    }
}
