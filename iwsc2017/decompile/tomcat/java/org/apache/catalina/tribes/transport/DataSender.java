package org.apache.catalina.tribes.transport;
import java.io.IOException;
public interface DataSender {
    void connect() throws IOException;
    void disconnect();
    boolean isConnected();
    void setRxBufSize ( int p0 );
    void setTxBufSize ( int p0 );
    boolean keepalive();
    void setTimeout ( long p0 );
    void setKeepAliveCount ( int p0 );
    void setKeepAliveTime ( long p0 );
    int getRequestCount();
    long getConnectTime();
}
