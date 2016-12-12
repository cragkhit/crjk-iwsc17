package org.apache.catalina.tribes.transport;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.ChannelSender;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.transport.nio.PooledParallelSender;
public class ReplicationTransmitter implements ChannelSender {
    private Channel channel;
    public ReplicationTransmitter() {
    }
    private MultiPointSender transport = new PooledParallelSender();
    public MultiPointSender getTransport() {
        return transport;
    }
    public void setTransport ( MultiPointSender transport ) {
        this.transport = transport;
    }
    @Override
    public void sendMessage ( ChannelMessage message, Member[] destination ) throws ChannelException {
        MultiPointSender sender = getTransport();
        sender.sendMessage ( destination, message );
    }
    @Override
    public void start() throws java.io.IOException {
        getTransport().connect();
    }
    @Override
    public synchronized void stop() {
        getTransport().disconnect();
        channel = null;
    }
    @Override
    public void heartbeat() {
        if ( getTransport() != null ) {
            getTransport().keepalive();
        }
    }
    @Override
    public synchronized void add ( Member member ) {
        getTransport().add ( member );
    }
    @Override
    public synchronized void remove ( Member member ) {
        getTransport().remove ( member );
    }
    @Override
    public Channel getChannel() {
        return channel;
    }
    @Override
    public void setChannel ( Channel channel ) {
        this.channel = channel;
    }
}
