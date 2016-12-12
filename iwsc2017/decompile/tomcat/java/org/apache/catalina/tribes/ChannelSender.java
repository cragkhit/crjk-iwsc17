package org.apache.catalina.tribes;
import java.io.IOException;
public interface ChannelSender extends Heartbeat {
    void add ( Member p0 );
    void remove ( Member p0 );
    void start() throws IOException;
    void stop();
    void heartbeat();
    void sendMessage ( ChannelMessage p0, Member[] p1 ) throws ChannelException;
    Channel getChannel();
    void setChannel ( Channel p0 );
}
