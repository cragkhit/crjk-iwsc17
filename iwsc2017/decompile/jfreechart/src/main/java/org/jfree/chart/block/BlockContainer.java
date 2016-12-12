package org.jfree.chart.block;
import java.util.Iterator;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.StandardEntityCollection;
import java.awt.geom.Rectangle2D;
import org.jfree.ui.Size2D;
import java.awt.Graphics2D;
import java.util.Collections;
import java.util.ArrayList;
import org.jfree.chart.util.ParamChecks;
import java.util.List;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class BlockContainer extends AbstractBlock implements Block, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 8199508075695195293L;
    private List blocks;
    private Arrangement arrangement;
    public BlockContainer() {
        this ( new BorderArrangement() );
    }
    public BlockContainer ( final Arrangement arrangement ) {
        ParamChecks.nullNotPermitted ( arrangement, "arrangement" );
        this.arrangement = arrangement;
        this.blocks = new ArrayList();
    }
    public Arrangement getArrangement() {
        return this.arrangement;
    }
    public void setArrangement ( final Arrangement arrangement ) {
        ParamChecks.nullNotPermitted ( arrangement, "arrangement" );
        this.arrangement = arrangement;
    }
    public boolean isEmpty() {
        return this.blocks.isEmpty();
    }
    public List getBlocks() {
        return Collections.unmodifiableList ( ( List<?> ) this.blocks );
    }
    public void add ( final Block block ) {
        this.add ( block, null );
    }
    public void add ( final Block block, final Object key ) {
        this.blocks.add ( block );
        this.arrangement.add ( block, key );
    }
    public void clear() {
        this.blocks.clear();
        this.arrangement.clear();
    }
    @Override
    public Size2D arrange ( final Graphics2D g2, final RectangleConstraint constraint ) {
        return this.arrangement.arrange ( this, g2, constraint );
    }
    public void draw ( final Graphics2D g2, final Rectangle2D area ) {
        this.draw ( g2, area, null );
    }
    @Override
    public Object draw ( final Graphics2D g2, final Rectangle2D area, final Object params ) {
        StandardEntityCollection sec = null;
        if ( params instanceof EntityBlockParams ) {
            final EntityBlockParams ebp = ( EntityBlockParams ) params;
            if ( ebp.getGenerateEntities() ) {
                sec = new StandardEntityCollection();
            }
        }
        Rectangle2D contentArea = ( Rectangle2D ) area.clone();
        contentArea = this.trimMargin ( contentArea );
        this.drawBorder ( g2, contentArea );
        contentArea = this.trimBorder ( contentArea );
        contentArea = this.trimPadding ( contentArea );
        for ( final Block block : this.blocks ) {
            final Rectangle2D bounds = block.getBounds();
            final Rectangle2D drawArea = new Rectangle2D.Double ( bounds.getX() + area.getX(), bounds.getY() + area.getY(), bounds.getWidth(), bounds.getHeight() );
            final Object r = block.draw ( g2, drawArea, params );
            if ( sec != null && r instanceof EntityBlockResult ) {
                final EntityBlockResult ebr = ( EntityBlockResult ) r;
                final EntityCollection ec = ebr.getEntityCollection();
                sec.addAll ( ec );
            }
        }
        BlockResult result = null;
        if ( sec != null ) {
            result = new BlockResult();
            result.setEntityCollection ( sec );
        }
        return result;
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( ! ( obj instanceof BlockContainer ) ) {
            return false;
        }
        if ( !super.equals ( obj ) ) {
            return false;
        }
        final BlockContainer that = ( BlockContainer ) obj;
        return this.arrangement.equals ( that.arrangement ) && this.blocks.equals ( that.blocks );
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        final BlockContainer clone = ( BlockContainer ) super.clone();
        return clone;
    }
}
