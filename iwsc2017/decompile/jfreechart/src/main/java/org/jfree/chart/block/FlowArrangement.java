package org.jfree.chart.block;
import java.util.List;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import org.jfree.ui.Size2D;
import java.awt.Graphics2D;
import org.jfree.ui.VerticalAlignment;
import org.jfree.ui.HorizontalAlignment;
import java.io.Serializable;
public class FlowArrangement implements Arrangement, Serializable {
    private static final long serialVersionUID = 4543632485478613800L;
    private HorizontalAlignment horizontalAlignment;
    private VerticalAlignment verticalAlignment;
    private double horizontalGap;
    private double verticalGap;
    public FlowArrangement() {
        this ( HorizontalAlignment.CENTER, VerticalAlignment.CENTER, 2.0, 2.0 );
    }
    public FlowArrangement ( final HorizontalAlignment hAlign, final VerticalAlignment vAlign, final double hGap, final double vGap ) {
        this.horizontalAlignment = hAlign;
        this.verticalAlignment = vAlign;
        this.horizontalGap = hGap;
        this.verticalGap = vGap;
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
                throw new RuntimeException ( "Not implemented." );
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
        throw new RuntimeException ( "Unrecognised constraint type." );
    }
    protected Size2D arrangeFN ( final BlockContainer container, final Graphics2D g2, final RectangleConstraint constraint ) {
        final List blocks = container.getBlocks();
        final double width = constraint.getWidth();
        double x = 0.0;
        double y = 0.0;
        double maxHeight = 0.0;
        final List itemsInRow = new ArrayList();
        for ( int i = 0; i < blocks.size(); ++i ) {
            final Block block = blocks.get ( i );
            final Size2D size = block.arrange ( g2, RectangleConstraint.NONE );
            if ( x + size.width <= width ) {
                itemsInRow.add ( block );
                block.setBounds ( new Rectangle2D.Double ( x, y, size.width, size.height ) );
                x = x + size.width + this.horizontalGap;
                maxHeight = Math.max ( maxHeight, size.height );
            } else if ( itemsInRow.isEmpty() ) {
                block.setBounds ( new Rectangle2D.Double ( x, y, Math.min ( size.width, width - x ), size.height ) );
                x = 0.0;
                y = y + size.height + this.verticalGap;
            } else {
                itemsInRow.clear();
                x = 0.0;
                y = y + maxHeight + this.verticalGap;
                maxHeight = size.height;
                block.setBounds ( new Rectangle2D.Double ( x, y, Math.min ( size.width, width ), size.height ) );
                x = size.width + this.horizontalGap;
                itemsInRow.add ( block );
            }
        }
        return new Size2D ( constraint.getWidth(), y + maxHeight );
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
        double x = 0.0;
        double width = 0.0;
        double maxHeight = 0.0;
        final List blocks = container.getBlocks();
        final int blockCount = blocks.size();
        if ( blockCount > 0 ) {
            final Size2D[] sizes = new Size2D[blocks.size()];
            for ( int i = 0; i < blocks.size(); ++i ) {
                final Block block = blocks.get ( i );
                sizes[i] = block.arrange ( g2, RectangleConstraint.NONE );
                width += sizes[i].getWidth();
                maxHeight = Math.max ( sizes[i].height, maxHeight );
                block.setBounds ( new Rectangle2D.Double ( x, 0.0, sizes[i].width, sizes[i].height ) );
                x = x + sizes[i].width + this.horizontalGap;
            }
            if ( blockCount > 1 ) {
                width += this.horizontalGap * ( blockCount - 1 );
            }
            if ( this.verticalAlignment != VerticalAlignment.TOP ) {
                for ( int i = 0; i < blocks.size(); ++i ) {
                    if ( this.verticalAlignment != VerticalAlignment.CENTER ) {
                        if ( this.verticalAlignment == VerticalAlignment.BOTTOM ) {}
                    }
                }
            }
        }
        return new Size2D ( width, maxHeight );
    }
    protected Size2D arrangeNF ( final BlockContainer container, final Graphics2D g2, final RectangleConstraint constraint ) {
        return this.arrangeNN ( container, g2 );
    }
    @Override
    public void clear() {
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof FlowArrangement ) ) {
            return false;
        }
        final FlowArrangement that = ( FlowArrangement ) obj;
        return this.horizontalAlignment == that.horizontalAlignment && this.verticalAlignment == that.verticalAlignment && this.horizontalGap == that.horizontalGap && this.verticalGap == that.verticalGap;
    }
}
