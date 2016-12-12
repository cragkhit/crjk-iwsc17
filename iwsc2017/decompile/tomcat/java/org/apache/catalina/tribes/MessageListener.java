package org.apache.catalina.tribes;
public interface MessageListener {
    void messageReceived ( ChannelMessage p0 );
    boolean accept ( ChannelMessage p0 );
}
