package org.apache.tomcat.dbcp.pool2.impl;
public interface DefaultPooledObjectInfoMBean {
    long getCreateTime();
    String getCreateTimeFormatted();
    long getLastBorrowTime();
    String getLastBorrowTimeFormatted();
    String getLastBorrowTrace();
    long getLastReturnTime();
    String getLastReturnTimeFormatted();
    String getPooledObjectType();
    String getPooledObjectToString();
    long getBorrowedCount();
}
