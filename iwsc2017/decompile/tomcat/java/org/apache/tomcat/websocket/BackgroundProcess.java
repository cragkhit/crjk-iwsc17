package org.apache.tomcat.websocket;
public interface BackgroundProcess {
    void backgroundProcess();
    void setProcessPeriod ( int p0 );
    int getProcessPeriod();
}
