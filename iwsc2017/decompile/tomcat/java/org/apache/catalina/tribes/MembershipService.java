package org.apache.catalina.tribes;
import java.util.Properties;
public interface MembershipService {
    public static final int MBR_RX = 4;
    public static final int MBR_TX = 8;
    void setProperties ( Properties p0 );
    Properties getProperties();
    void start() throws Exception;
    void start ( int p0 ) throws Exception;
    void stop ( int p0 );
    boolean hasMembers();
    Member getMember ( Member p0 );
    Member[] getMembers();
    Member getLocalMember ( boolean p0 );
    String[] getMembersByName();
    Member findMemberByName ( String p0 );
    void setLocalMemberProperties ( String p0, int p1, int p2, int p3 );
    void setMembershipListener ( MembershipListener p0 );
    void removeMembershipListener();
    void setPayload ( byte[] p0 );
    void setDomain ( byte[] p0 );
    void broadcast ( ChannelMessage p0 ) throws ChannelException;
    Channel getChannel();
    void setChannel ( Channel p0 );
}
