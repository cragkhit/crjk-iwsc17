package org.apache.catalina.tribes;
import java.io.Serializable;
public interface Channel {
    public static final int DEFAULT = 15;
    public static final int SND_RX_SEQ = 1;
    public static final int SND_TX_SEQ = 2;
    public static final int MBR_RX_SEQ = 4;
    public static final int MBR_TX_SEQ = 8;
    public static final int SEND_OPTIONS_BYTE_MESSAGE = 1;
    public static final int SEND_OPTIONS_USE_ACK = 2;
    public static final int SEND_OPTIONS_SYNCHRONIZED_ACK = 4;
    public static final int SEND_OPTIONS_ASYNCHRONOUS = 8;
    public static final int SEND_OPTIONS_SECURE = 16;
    public static final int SEND_OPTIONS_UDP = 32;
    public static final int SEND_OPTIONS_MULTICAST = 64;
    public static final int SEND_OPTIONS_DEFAULT = 2;
    void addInterceptor ( ChannelInterceptor p0 );
    void start ( int p0 ) throws ChannelException;
    void stop ( int p0 ) throws ChannelException;
    UniqueId send ( Member[] p0, Serializable p1, int p2 ) throws ChannelException;
    UniqueId send ( Member[] p0, Serializable p1, int p2, ErrorHandler p3 ) throws ChannelException;
    void heartbeat();
    void setHeartbeat ( boolean p0 );
    void addMembershipListener ( MembershipListener p0 );
    void addChannelListener ( ChannelListener p0 );
    void removeMembershipListener ( MembershipListener p0 );
    void removeChannelListener ( ChannelListener p0 );
    boolean hasMembers();
    Member[] getMembers();
    Member getLocalMember ( boolean p0 );
    Member getMember ( Member p0 );
    String getName();
    void setName ( String p0 );
}
