package org.apache.catalina.tribes.membership;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Properties;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.MembershipListener;
import org.apache.catalina.tribes.MembershipService;
import org.apache.catalina.tribes.MessageListener;
import org.apache.catalina.tribes.io.ChannelData;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.util.Arrays;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.catalina.tribes.util.UUIDGenerator;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class McastService implements MembershipService, MembershipListener, MessageListener {
    private static final Log log = LogFactory.getLog ( McastService.class );
    protected static final StringManager sm = StringManager.getManager ( Constants.Package );
    protected Properties properties = new Properties();
    protected McastServiceImpl impl;
    protected volatile MembershipListener listener;
    protected MessageListener msglistener;
    protected MemberImpl localMember ;
    private int mcastSoTimeout;
    private int mcastTTL;
    protected byte[] payload;
    protected byte[] domain;
    private Channel channel;
    public McastService() {
        properties.setProperty ( "mcastPort", "45564" );
        properties.setProperty ( "mcastAddress", "228.0.0.4" );
        properties.setProperty ( "memberDropTime", "3000" );
        properties.setProperty ( "mcastFrequency", "500" );
    }
    @Override
    public void setProperties ( Properties properties ) {
        hasProperty ( properties, "mcastPort" );
        hasProperty ( properties, "mcastAddress" );
        hasProperty ( properties, "memberDropTime" );
        hasProperty ( properties, "mcastFrequency" );
        hasProperty ( properties, "tcpListenPort" );
        hasProperty ( properties, "tcpListenHost" );
        this.properties = properties;
    }
    @Override
    public Properties getProperties() {
        return properties;
    }
    public String getLocalMemberName() {
        return localMember.toString() ;
    }
    @Override
    public Member getLocalMember ( boolean alive ) {
        if ( alive && localMember != null && impl != null ) {
            localMember.setMemberAliveTime ( System.currentTimeMillis() - impl.getServiceStartTime() );
        }
        return localMember;
    }
    @Override
    public void setLocalMemberProperties ( String listenHost, int listenPort, int securePort, int udpPort ) {
        properties.setProperty ( "tcpListenHost", listenHost );
        properties.setProperty ( "tcpListenPort", String.valueOf ( listenPort ) );
        properties.setProperty ( "udpListenPort", String.valueOf ( udpPort ) );
        properties.setProperty ( "tcpSecurePort", String.valueOf ( securePort ) );
        try {
            if ( localMember != null ) {
                localMember.setHostname ( listenHost );
                localMember.setPort ( listenPort );
            } else {
                localMember = new MemberImpl ( listenHost, listenPort, 0 );
                localMember.setUniqueId ( UUIDGenerator.randomUUID ( true ) );
                localMember.setPayload ( getPayload() );
                localMember.setDomain ( getDomain() );
                localMember.setLocal ( true );
            }
            localMember.setSecurePort ( securePort );
            localMember.setUdpPort ( udpPort );
            localMember.getData ( true, true );
        } catch ( IOException x ) {
            throw new IllegalArgumentException ( x );
        }
    }
    public void setAddress ( String addr ) {
        properties.setProperty ( "mcastAddress", addr );
    }
    public String getAddress() {
        return properties.getProperty ( "mcastAddress" );
    }
    public void setMcastBindAddress ( String bindaddr ) {
        setBind ( bindaddr );
    }
    public void setBind ( String bindaddr ) {
        properties.setProperty ( "mcastBindAddress", bindaddr );
    }
    public String getBind() {
        return properties.getProperty ( "mcastBindAddress" );
    }
    public void setPort ( int port ) {
        properties.setProperty ( "mcastPort", String.valueOf ( port ) );
    }
    public void setRecoveryCounter ( int recoveryCounter ) {
        properties.setProperty ( "recoveryCounter", String.valueOf ( recoveryCounter ) );
    }
    public int getRecoveryCounter() {
        String p = properties.getProperty ( "recoveryCounter" );
        if ( p != null ) {
            return Integer.parseInt ( p );
        }
        return -1;
    }
    public void setRecoveryEnabled ( boolean recoveryEnabled ) {
        properties.setProperty ( "recoveryEnabled", String.valueOf ( recoveryEnabled ) );
    }
    public boolean getRecoveryEnabled() {
        String p = properties.getProperty ( "recoveryEnabled" );
        if ( p != null ) {
            return Boolean.parseBoolean ( p );
        }
        return false;
    }
    public void setRecoverySleepTime ( long recoverySleepTime ) {
        properties.setProperty ( "recoverySleepTime", String.valueOf ( recoverySleepTime ) );
    }
    public long getRecoverySleepTime() {
        String p = properties.getProperty ( "recoverySleepTime" );
        if ( p != null ) {
            return Long.parseLong ( p );
        }
        return -1;
    }
    public void setLocalLoopbackDisabled ( boolean localLoopbackDisabled ) {
        properties.setProperty ( "localLoopbackDisabled", String.valueOf ( localLoopbackDisabled ) );
    }
    public boolean getLocalLoopbackDisabled() {
        String p = properties.getProperty ( "localLoopbackDisabled" );
        if ( p != null ) {
            return Boolean.parseBoolean ( p );
        }
        return false;
    }
    public int getPort() {
        String p = properties.getProperty ( "mcastPort" );
        return Integer.parseInt ( p );
    }
    public void setFrequency ( long time ) {
        properties.setProperty ( "mcastFrequency", String.valueOf ( time ) );
    }
    public long getFrequency() {
        String p = properties.getProperty ( "mcastFrequency" );
        return Long.parseLong ( p );
    }
    public void setMcastDropTime ( long time ) {
        setDropTime ( time );
    }
    public void setDropTime ( long time ) {
        properties.setProperty ( "memberDropTime", String.valueOf ( time ) );
    }
    public long getDropTime() {
        String p = properties.getProperty ( "memberDropTime" );
        return Long.parseLong ( p );
    }
    protected void hasProperty ( Properties properties, String name ) {
        if ( properties.getProperty ( name ) == null ) {
            throw new IllegalArgumentException ( sm.getString ( "mcastService.missing.property", name ) );
        }
    }
    @Override
    public void start() throws java.lang.Exception {
        start ( MembershipService.MBR_RX );
        start ( MembershipService.MBR_TX );
    }
    @Override
    public void start ( int level ) throws java.lang.Exception {
        hasProperty ( properties, "mcastPort" );
        hasProperty ( properties, "mcastAddress" );
        hasProperty ( properties, "memberDropTime" );
        hasProperty ( properties, "mcastFrequency" );
        hasProperty ( properties, "tcpListenPort" );
        hasProperty ( properties, "tcpListenHost" );
        hasProperty ( properties, "tcpSecurePort" );
        hasProperty ( properties, "udpListenPort" );
        if ( impl != null ) {
            impl.start ( level );
            return;
        }
        String host = getProperties().getProperty ( "tcpListenHost" );
        int port = Integer.parseInt ( getProperties().getProperty ( "tcpListenPort" ) );
        int securePort = Integer.parseInt ( getProperties().getProperty ( "tcpSecurePort" ) );
        int udpPort = Integer.parseInt ( getProperties().getProperty ( "udpListenPort" ) );
        if ( localMember == null ) {
            localMember = new MemberImpl ( host, port, 100 );
            localMember.setUniqueId ( UUIDGenerator.randomUUID ( true ) );
            localMember.setLocal ( true );
        } else {
            localMember.setHostname ( host );
            localMember.setPort ( port );
            localMember.setMemberAliveTime ( 100 );
        }
        localMember.setSecurePort ( securePort );
        localMember.setUdpPort ( udpPort );
        if ( this.payload != null ) {
            localMember.setPayload ( payload );
        }
        if ( this.domain != null ) {
            localMember.setDomain ( domain );
        }
        localMember.setServiceStartTime ( System.currentTimeMillis() );
        java.net.InetAddress bind = null;
        if ( properties.getProperty ( "mcastBindAddress" ) != null ) {
            bind = java.net.InetAddress.getByName ( properties.getProperty ( "mcastBindAddress" ) );
        }
        int ttl = -1;
        int soTimeout = -1;
        if ( properties.getProperty ( "mcastTTL" ) != null ) {
            try {
                ttl = Integer.parseInt ( properties.getProperty ( "mcastTTL" ) );
            } catch ( Exception x ) {
                log.error ( sm.getString ( "McastService.parseTTL",
                                           properties.getProperty ( "mcastTTL" ) ), x );
            }
        }
        if ( properties.getProperty ( "mcastSoTimeout" ) != null ) {
            try {
                soTimeout = Integer.parseInt ( properties.getProperty ( "mcastSoTimeout" ) );
            } catch ( Exception x ) {
                log.error ( sm.getString ( "McastService.parseSoTimeout",
                                           properties.getProperty ( "mcastSoTimeout" ) ), x );
            }
        }
        impl = new McastServiceImpl ( localMember, Long.parseLong ( properties.getProperty ( "mcastFrequency" ) ),
                                      Long.parseLong ( properties.getProperty ( "memberDropTime" ) ),
                                      Integer.parseInt ( properties.getProperty ( "mcastPort" ) ),
                                      bind,
                                      java.net.InetAddress.getByName ( properties.getProperty ( "mcastAddress" ) ),
                                      ttl,
                                      soTimeout,
                                      this,
                                      this,
                                      Boolean.parseBoolean ( properties.getProperty ( "localLoopbackDisabled", "false" ) ) );
        String value = properties.getProperty ( "recoveryEnabled", "true" );
        boolean recEnabled = Boolean.parseBoolean ( value );
        impl.setRecoveryEnabled ( recEnabled );
        int recCnt = Integer.parseInt ( properties.getProperty ( "recoveryCounter", "10" ) );
        impl.setRecoveryCounter ( recCnt );
        long recSlpTime = Long.parseLong ( properties.getProperty ( "recoverySleepTime", "5000" ) );
        impl.setRecoverySleepTime ( recSlpTime );
        impl.setChannel ( channel );
        impl.start ( level );
    }
    @Override
    public void stop ( int svc ) {
        try  {
            if ( impl != null && impl.stop ( svc ) ) {
                impl.setChannel ( null );
                impl = null;
                channel = null;
            }
        } catch ( Exception x )  {
            log.error ( sm.getString (
                            "McastService.stopFail", Integer.valueOf ( svc ) ), x );
        }
    }
    @Override
    public String[] getMembersByName() {
        Member[] currentMembers = getMembers();
        String [] membernames ;
        if ( currentMembers != null ) {
            membernames = new String[currentMembers.length];
            for ( int i = 0; i < currentMembers.length; i++ ) {
                membernames[i] = currentMembers[i].toString() ;
            }
        } else {
            membernames = new String[0] ;
        }
        return membernames ;
    }
    @Override
    public Member findMemberByName ( String name ) {
        Member[] currentMembers = getMembers();
        for ( int i = 0; i < currentMembers.length; i++ ) {
            if ( name.equals ( currentMembers[i].toString() ) ) {
                return currentMembers[i];
            }
        }
        return null;
    }
    @Override
    public boolean hasMembers() {
        if ( impl == null || impl.membership == null ) {
            return false;
        }
        return impl.membership.hasMembers();
    }
    @Override
    public Member getMember ( Member mbr ) {
        if ( impl == null || impl.membership == null ) {
            return null;
        }
        return impl.membership.getMember ( mbr );
    }
    protected static final Member[]EMPTY_MEMBERS = new Member[0];
    @Override
    public Member[] getMembers() {
        if ( impl == null || impl.membership == null ) {
            return EMPTY_MEMBERS;
        }
        return impl.membership.getMembers();
    }
    @Override
    public void setMembershipListener ( MembershipListener listener ) {
        this.listener = listener;
    }
    public void setMessageListener ( MessageListener listener ) {
        this.msglistener = listener;
    }
    public void removeMessageListener() {
        this.msglistener = null;
    }
    @Override
    public void removeMembershipListener() {
        listener = null;
    }
    @Override
    public void memberAdded ( Member member ) {
        MembershipListener listener = this.listener;
        if ( listener != null ) {
            listener.memberAdded ( member );
        }
    }
    @Override
    public void memberDisappeared ( Member member ) {
        MembershipListener listener = this.listener;
        if ( listener != null ) {
            listener.memberDisappeared ( member );
        }
    }
    @Override
    public void messageReceived ( ChannelMessage msg ) {
        if ( msglistener != null && msglistener.accept ( msg ) ) {
            msglistener.messageReceived ( msg );
        }
    }
    @Override
    public boolean accept ( ChannelMessage msg ) {
        return true;
    }
    @Override
    public void broadcast ( ChannelMessage message ) throws ChannelException {
        if ( impl == null || ( impl.startLevel & Channel.MBR_TX_SEQ ) != Channel.MBR_TX_SEQ ) {
            throw new ChannelException ( sm.getString ( "mcastService.noStart" ) );
        }
        byte[] data = XByteBuffer.createDataPackage ( ( ChannelData ) message );
        if ( data.length > McastServiceImpl.MAX_PACKET_SIZE ) {
            throw new ChannelException ( sm.getString ( "mcastService.exceed.maxPacketSize",
                                         Integer.toString ( data.length ) ,
                                         Integer.toString ( McastServiceImpl.MAX_PACKET_SIZE ) ) );
        }
        DatagramPacket packet = new DatagramPacket ( data, 0, data.length );
        try {
            impl.send ( false, packet );
        } catch ( Exception x ) {
            throw new ChannelException ( x );
        }
    }
    public int getSoTimeout() {
        return mcastSoTimeout;
    }
    public void setSoTimeout ( int mcastSoTimeout ) {
        this.mcastSoTimeout = mcastSoTimeout;
        properties.setProperty ( "mcastSoTimeout", String.valueOf ( mcastSoTimeout ) );
    }
    public int getTtl() {
        return mcastTTL;
    }
    public byte[] getPayload() {
        return payload;
    }
    public byte[] getDomain() {
        return domain;
    }
    public void setTtl ( int mcastTTL ) {
        this.mcastTTL = mcastTTL;
        properties.setProperty ( "mcastTTL", String.valueOf ( mcastTTL ) );
    }
    @Override
    public void setPayload ( byte[] payload ) {
        this.payload = payload;
        if ( localMember != null ) {
            localMember.setPayload ( payload );
            try {
                if ( impl != null ) {
                    impl.send ( false );
                }
            } catch ( Exception x ) {
                log.error ( sm.getString ( "McastService.payload" ), x );
            }
        }
    }
    @Override
    public void setDomain ( byte[] domain ) {
        this.domain = domain;
        if ( localMember != null ) {
            localMember.setDomain ( domain );
            try {
                if ( impl != null ) {
                    impl.send ( false );
                }
            } catch ( Exception x ) {
                log.error ( sm.getString ( "McastService.domain" ), x );
            }
        }
    }
    public void setDomain ( String domain ) {
        if ( domain == null ) {
            return;
        }
        if ( domain.startsWith ( "{" ) ) {
            setDomain ( Arrays.fromString ( domain ) );
        } else {
            setDomain ( Arrays.convert ( domain ) );
        }
    }
    @Override
    public Channel getChannel() {
        return channel;
    }
    @Override
    public void setChannel ( Channel channel ) {
        this.channel = channel;
    }
    public static void main ( String args[] ) throws Exception {
        McastService service = new McastService();
        java.util.Properties p = new java.util.Properties();
        p.setProperty ( "mcastPort", "5555" );
        p.setProperty ( "mcastAddress", "224.10.10.10" );
        p.setProperty ( "mcastClusterDomain", "catalina" );
        p.setProperty ( "bindAddress", "localhost" );
        p.setProperty ( "memberDropTime", "3000" );
        p.setProperty ( "mcastFrequency", "500" );
        p.setProperty ( "tcpListenPort", "4000" );
        p.setProperty ( "tcpListenHost", "127.0.0.1" );
        p.setProperty ( "tcpSecurePort", "4100" );
        p.setProperty ( "udpListenPort", "4200" );
        service.setProperties ( p );
        service.start();
        Thread.sleep ( 60 * 1000 * 60 );
    }
}
