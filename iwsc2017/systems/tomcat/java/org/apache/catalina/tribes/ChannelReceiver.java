package org.apache.catalina.tribes;
public interface ChannelReceiver extends Heartbeat {
    public static final int MAX_UDP_SIZE = 65535;
    public void start() throws java.io.IOException;
    public void stop();
    public String getHost();
    public int getPort();
    public int getSecurePort();
    public int getUdpPort();
    public void setMessageListener ( MessageListener listener );
    public MessageListener getMessageListener();
    public Channel getChannel();
    public void setChannel ( Channel channel );
}
