package org.apache.catalina.tribes;
public interface MembershipService {
    public static final int MBR_RX = Channel.MBR_RX_SEQ;
    public static final int MBR_TX = Channel.MBR_TX_SEQ;
    public void setProperties ( java.util.Properties properties );
    public java.util.Properties getProperties();
    public void start() throws java.lang.Exception;
    public void start ( int level ) throws java.lang.Exception;
    public void stop ( int level );
    public boolean hasMembers();
    public Member getMember ( Member mbr );
    public Member[] getMembers();
    public Member getLocalMember ( boolean incAliveTime );
    public String[] getMembersByName();
    public Member findMemberByName ( String name );
    public void setLocalMemberProperties ( String listenHost, int listenPort, int securePort, int udpPort );
    public void setMembershipListener ( MembershipListener listener );
    public void removeMembershipListener();
    public void setPayload ( byte[] payload );
    public void setDomain ( byte[] domain );
    public void broadcast ( ChannelMessage message ) throws ChannelException;
    public Channel getChannel();
    public void setChannel ( Channel channel );
}
