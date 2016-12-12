package org.apache.catalina.ha.backend;
public interface Sender {
    void init ( HeartbeatListener p0 ) throws Exception;
    int send ( String p0 ) throws Exception;
}
