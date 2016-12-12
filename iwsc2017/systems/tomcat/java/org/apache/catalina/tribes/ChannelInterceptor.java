package org.apache.catalina.tribes;
import org.apache.catalina.tribes.group.InterceptorPayload;
public interface ChannelInterceptor extends MembershipListener, Heartbeat {
    public int getOptionFlag();
    public void setOptionFlag ( int flag );
    public void setNext ( ChannelInterceptor next ) ;
    public ChannelInterceptor getNext();
    public void setPrevious ( ChannelInterceptor previous );
    public ChannelInterceptor getPrevious();
    public void sendMessage ( Member[] destination, ChannelMessage msg, InterceptorPayload payload ) throws ChannelException;
    public void messageReceived ( ChannelMessage data );
    @Override
    public void heartbeat();
    public boolean hasMembers() ;
    public Member[] getMembers() ;
    public Member getLocalMember ( boolean incAliveTime ) ;
    public Member getMember ( Member mbr );
    public void start ( int svc ) throws ChannelException;
    public void stop ( int svc ) throws ChannelException;
    public void fireInterceptorEvent ( InterceptorEvent event );
    public Channel getChannel();
    public void setChannel ( Channel channel );
    interface InterceptorEvent {
        int getEventType();
        String getEventTypeDesc();
        ChannelInterceptor getInterceptor();
    }
}
