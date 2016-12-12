package org.apache.catalina.tribes;
import java.util.Iterator;
public interface ManagedChannel extends Channel {
    public void setChannelSender ( ChannelSender sender );
    public void setChannelReceiver ( ChannelReceiver receiver );
    public void setMembershipService ( MembershipService service );
    public ChannelSender getChannelSender();
    public ChannelReceiver getChannelReceiver();
    public MembershipService getMembershipService();
    public Iterator<ChannelInterceptor> getInterceptors();
}
