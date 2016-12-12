package org.jfree.chart.entity;
import org.jfree.util.ObjectUtilities;
import java.util.Iterator;
import java.util.Collections;
import java.util.Collection;
import org.jfree.chart.util.ParamChecks;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;
import org.jfree.util.PublicCloneable;
public class StandardEntityCollection implements EntityCollection, Cloneable, PublicCloneable, Serializable {
    private static final long serialVersionUID = 5384773031184897047L;
    private List entities;
    public StandardEntityCollection() {
        this.entities = new ArrayList();
    }
    @Override
    public int getEntityCount() {
        return this.entities.size();
    }
    @Override
    public ChartEntity getEntity ( final int index ) {
        return this.entities.get ( index );
    }
    @Override
    public void clear() {
        this.entities.clear();
    }
    @Override
    public void add ( final ChartEntity entity ) {
        ParamChecks.nullNotPermitted ( entity, "entity" );
        this.entities.add ( entity );
    }
    @Override
    public void addAll ( final EntityCollection collection ) {
        this.entities.addAll ( collection.getEntities() );
    }
    @Override
    public ChartEntity getEntity ( final double x, final double y ) {
        final int entityCount = this.entities.size();
        for ( int i = entityCount - 1; i >= 0; --i ) {
            final ChartEntity entity = this.entities.get ( i );
            if ( entity.getArea().contains ( x, y ) ) {
                return entity;
            }
        }
        return null;
    }
    @Override
    public Collection getEntities() {
        return Collections.unmodifiableCollection ( ( Collection<?> ) this.entities );
    }
    @Override
    public Iterator iterator() {
        return this.entities.iterator();
    }
    @Override
    public boolean equals ( final Object obj ) {
        if ( obj == this ) {
            return true;
        }
        if ( obj instanceof StandardEntityCollection ) {
            final StandardEntityCollection that = ( StandardEntityCollection ) obj;
            return ObjectUtilities.equal ( ( Object ) this.entities, ( Object ) that.entities );
        }
        return false;
    }
    public Object clone() throws CloneNotSupportedException {
        final StandardEntityCollection clone = ( StandardEntityCollection ) super.clone();
        clone.entities = new ArrayList ( this.entities.size() );
        for ( int i = 0; i < this.entities.size(); ++i ) {
            final ChartEntity entity = this.entities.get ( i );
            clone.entities.add ( entity.clone() );
        }
        return clone;
    }
}
