package org.apache.catalina.tribes;
import java.util.Iterator;
public interface ManagedChannel extends Channel {
    void setChannelSender ( ChannelSender p0 );
    void setChannelReceiver ( ChannelReceiver p0 );
    void setMembershipService ( MembershipService p0 );
    ChannelSender getChannelSender();
    ChannelReceiver getChannelReceiver();
    MembershipService getMembershipService();
    Iterator<ChannelInterceptor> getInterceptors();
}
