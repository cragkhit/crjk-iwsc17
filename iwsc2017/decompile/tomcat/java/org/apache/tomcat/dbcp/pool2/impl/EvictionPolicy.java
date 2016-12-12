package org.apache.tomcat.dbcp.pool2.impl;
import org.apache.tomcat.dbcp.pool2.PooledObject;
public interface EvictionPolicy<T> {
    boolean evict ( EvictionConfig p0, PooledObject<T> p1, int p2 );
}
