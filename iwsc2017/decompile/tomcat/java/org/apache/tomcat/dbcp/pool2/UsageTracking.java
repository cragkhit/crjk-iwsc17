package org.apache.tomcat.dbcp.pool2;
public interface UsageTracking<T> {
    void use ( T p0 );
}
