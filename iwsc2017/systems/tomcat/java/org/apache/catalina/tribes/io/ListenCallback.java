package org.apache.catalina.tribes.io;
import org.apache.catalina.tribes.ChannelMessage;
public interface ListenCallback {
    public void messageDataReceived ( ChannelMessage data );
}
