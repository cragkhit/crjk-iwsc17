package org.apache.tomcat.dbcp.pool2;
public interface SwallowedExceptionListener {
    void onSwallowException ( Exception e );
}
