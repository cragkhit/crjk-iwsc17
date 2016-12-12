package org.jfree.chart.entity;
import java.util.Iterator;
import java.util.Collection;
public interface EntityCollection {
    void clear();
    void add ( ChartEntity p0 );
    void addAll ( EntityCollection p0 );
    ChartEntity getEntity ( double p0, double p1 );
    ChartEntity getEntity ( int p0 );
    int getEntityCount();
    Collection getEntities();
    Iterator iterator();
}
