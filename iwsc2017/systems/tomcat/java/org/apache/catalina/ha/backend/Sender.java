package org.apache.catalina.ha.backend;
public interface Sender {
    public void init ( HeartbeatListener config ) throws Exception;
    public int send ( String mess ) throws Exception;
}
