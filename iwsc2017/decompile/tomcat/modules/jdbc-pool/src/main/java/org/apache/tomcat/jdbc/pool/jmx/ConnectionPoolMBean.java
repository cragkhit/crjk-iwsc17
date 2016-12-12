// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool.jmx;

import org.apache.tomcat.jdbc.pool.PoolConfiguration;

public interface ConnectionPoolMBean extends PoolConfiguration
{
    int getSize();
    
    int getIdle();
    
    int getActive();
    
    int getNumIdle();
    
    int getNumActive();
    
    int getWaitCount();
    
    void checkIdle();
    
    void checkAbandoned();
    
    void testIdle();
    
    void purge();
    
    void purgeOnReturn();
}
