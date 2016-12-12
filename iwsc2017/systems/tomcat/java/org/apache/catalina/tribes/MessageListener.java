package org.apache.catalina.tribes;
public interface MessageListener {
    public void messageReceived ( ChannelMessage msg );
    public boolean accept ( ChannelMessage msg );
}
