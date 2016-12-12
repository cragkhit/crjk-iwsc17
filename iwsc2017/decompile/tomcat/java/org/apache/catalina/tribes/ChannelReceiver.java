package org.apache.catalina.tribes;
import java.io.IOException;
public interface ChannelReceiver extends Heartbeat {
    public static final int MAX_UDP_SIZE = 65535;
    void start() throws IOException;
    void stop();
    String getHost();
    int getPort();
    int getSecurePort();
    int getUdpPort();
    void setMessageListener ( MessageListener p0 );
    MessageListener getMessageListener();
    Channel getChannel();
    void setChannel ( Channel p0 );
}
