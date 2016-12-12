package org.apache.catalina.tribes;
import java.io.IOException;
public interface ChannelSender extends Heartbeat {
    public void add ( Member member );
    public void remove ( Member member );
    public void start() throws IOException;
    public void stop();
    @Override
    public void heartbeat() ;
    public void sendMessage ( ChannelMessage message, Member[] destination ) throws ChannelException;
    public Channel getChannel();
    public void setChannel ( Channel channel );
}
