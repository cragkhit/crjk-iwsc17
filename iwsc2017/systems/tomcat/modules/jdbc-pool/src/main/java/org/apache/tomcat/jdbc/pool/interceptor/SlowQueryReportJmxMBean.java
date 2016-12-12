package org.apache.tomcat.jdbc.pool.interceptor;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.OpenDataException;
public interface SlowQueryReportJmxMBean {
    public CompositeData[] getSlowQueriesCD() throws OpenDataException;
}
