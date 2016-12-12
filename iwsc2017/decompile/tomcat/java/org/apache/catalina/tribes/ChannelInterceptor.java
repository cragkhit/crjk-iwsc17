package org.apache.catalina.tribes;
import org.apache.catalina.tribes.group.InterceptorPayload;
public interface ChannelInterceptor extends MembershipListener, Heartbeat {
    int getOptionFlag();
    void setOptionFlag ( int p0 );
    void setNext ( ChannelInterceptor p0 );
    ChannelInterceptor getNext();
    void setPrevious ( ChannelInterceptor p0 );
    ChannelInterceptor getPrevious();
    void sendMessage ( Member[] p0, ChannelMessage p1, InterceptorPayload p2 ) throws ChannelException;
    void messageReceived ( ChannelMessage p0 );
    void heartbeat();
    boolean hasMembers();
    Member[] getMembers();
    Member getLocalMember ( boolean p0 );
    Member getMember ( Member p0 );
    void start ( int p0 ) throws ChannelException;
    void stop ( int p0 ) throws ChannelException;
    void fireInterceptorEvent ( InterceptorEvent p0 );
    Channel getChannel();
    void setChannel ( Channel p0 );
    public interface InterceptorEvent {
        int getEventType();
        String getEventTypeDesc();
        ChannelInterceptor getInterceptor();
    }
}
