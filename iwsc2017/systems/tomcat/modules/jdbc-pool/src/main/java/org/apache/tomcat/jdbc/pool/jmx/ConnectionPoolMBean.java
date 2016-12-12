package org.apache.tomcat.jdbc.pool.jmx;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
public interface ConnectionPoolMBean extends PoolConfiguration  {
    public int getSize();
    public int getIdle();
    public int getActive();
    public int getNumIdle();
    public int getNumActive();
    public int getWaitCount();
    public void checkIdle();
    public void checkAbandoned();
    public void testIdle();
    public void purge();
    public void purgeOnReturn();
}
