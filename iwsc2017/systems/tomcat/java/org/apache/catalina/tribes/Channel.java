package org.apache.catalina.tribes;
import java.io.Serializable;
public interface Channel {
    public static final int DEFAULT = 15;
    public static final int SND_RX_SEQ = 1;
    public static final int SND_TX_SEQ = 2;
    public static final int MBR_RX_SEQ = 4;
    public static final int MBR_TX_SEQ = 8;
    public static final int SEND_OPTIONS_BYTE_MESSAGE = 0x0001;
    public static final int SEND_OPTIONS_USE_ACK = 0x0002;
    public static final int SEND_OPTIONS_SYNCHRONIZED_ACK = 0x0004;
    public static final int SEND_OPTIONS_ASYNCHRONOUS = 0x0008;
    public static final int SEND_OPTIONS_SECURE = 0x0010;
    public static final int SEND_OPTIONS_UDP =  0x0020;
    public static final int SEND_OPTIONS_MULTICAST =  0x0040;
    public static final int SEND_OPTIONS_DEFAULT = SEND_OPTIONS_USE_ACK;
    public void addInterceptor ( ChannelInterceptor interceptor );
    public void start ( int svc ) throws ChannelException;
    public void stop ( int svc ) throws ChannelException;
    public UniqueId send ( Member[] destination, Serializable msg, int options ) throws ChannelException;
    public UniqueId send ( Member[] destination, Serializable msg, int options, ErrorHandler handler ) throws ChannelException;
    public void heartbeat();
    public void setHeartbeat ( boolean enable );
    public void addMembershipListener ( MembershipListener listener );
    public void addChannelListener ( ChannelListener listener );
    public void removeMembershipListener ( MembershipListener listener );
    public void removeChannelListener ( ChannelListener listener );
    public boolean hasMembers() ;
    public Member[] getMembers() ;
    public Member getLocalMember ( boolean incAlive );
    public Member getMember ( Member mbr );
    public String getName();
    public void setName ( String name );
}
