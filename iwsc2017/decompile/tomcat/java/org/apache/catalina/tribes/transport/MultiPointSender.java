package org.apache.catalina.tribes.transport;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
public interface MultiPointSender extends DataSender {
    void sendMessage ( Member[] p0, ChannelMessage p1 ) throws ChannelException;
    void setMaxRetryAttempts ( int p0 );
    void setDirectBuffer ( boolean p0 );
    void add ( Member p0 );
    void remove ( Member p0 );
}
