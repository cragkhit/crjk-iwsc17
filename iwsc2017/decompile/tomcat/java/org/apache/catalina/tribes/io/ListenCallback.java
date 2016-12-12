package org.apache.catalina.tribes.io;
import org.apache.catalina.tribes.ChannelMessage;
public interface ListenCallback {
    void messageDataReceived ( ChannelMessage p0 );
}
