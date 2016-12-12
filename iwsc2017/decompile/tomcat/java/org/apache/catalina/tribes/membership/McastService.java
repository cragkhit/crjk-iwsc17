package org.apache.catalina.tribes.membership;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.tribes.util.Arrays;
import java.net.DatagramPacket;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.io.ChannelData;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ChannelMessage;
import java.net.InetAddress;
import java.io.IOException;
import org.apache.catalina.tribes.util.UUIDGenerator;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.Channel;
import java.util.Properties;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.tribes.MessageListener;
import org.apache.catalina.tribes.MembershipListener;
import org.apache.catalina.tribes.MembershipService;
public class McastService implements MembershipService, MembershipListener, MessageListener {
    private static final Log log;
    protected static final StringManager sm;
    protected Properties properties;
    protected McastServiceImpl impl;
    protected volatile MembershipListener listener;
    protected MessageListener msglistener;
    protected MemberImpl localMember;
    private int mcastSoTimeout;
    private int mcastTTL;
    protected byte[] payload;
    protected byte[] domain;
    private Channel channel;
    protected static final Member[] EMPTY_MEMBERS;
    public McastService() {
        ( this.properties = new Properties() ).setProperty ( "mcastPort", "45564" );
        this.properties.setProperty ( "mcastAddress", "228.0.0.4" );
        this.properties.setProperty ( "memberDropTime", "3000" );
        this.properties.setProperty ( "mcastFrequency", "500" );
    }
    @Override
    public void setProperties ( final Properties properties ) {
        this.hasProperty ( properties, "mcastPort" );
        this.hasProperty ( properties, "mcastAddress" );
        this.hasProperty ( properties, "memberDropTime" );
        this.hasProperty ( properties, "mcastFrequency" );
        this.hasProperty ( properties, "tcpListenPort" );
        this.hasProperty ( properties, "tcpListenHost" );
        this.properties = properties;
    }
    @Override
    public Properties getProperties() {
        return this.properties;
    }
    public String getLocalMemberName() {
        return this.localMember.toString();
    }
    @Override
    public Member getLocalMember ( final boolean alive ) {
        if ( alive && this.localMember != null && this.impl != null ) {
            this.localMember.setMemberAliveTime ( System.currentTimeMillis() - this.impl.getServiceStartTime() );
        }
        return this.localMember;
    }
    @Override
    public void setLocalMemberProperties ( final String listenHost, final int listenPort, final int securePort, final int udpPort ) {
        this.properties.setProperty ( "tcpListenHost", listenHost );
        this.properties.setProperty ( "tcpListenPort", String.valueOf ( listenPort ) );
        this.properties.setProperty ( "udpListenPort", String.valueOf ( udpPort ) );
        this.properties.setProperty ( "tcpSecurePort", String.valueOf ( securePort ) );
        try {
            if ( this.localMember != null ) {
                this.localMember.setHostname ( listenHost );
                this.localMember.setPort ( listenPort );
            } else {
                ( this.localMember = new MemberImpl ( listenHost, listenPort, 0L ) ).setUniqueId ( UUIDGenerator.randomUUID ( true ) );
                this.localMember.setPayload ( this.getPayload() );
                this.localMember.setDomain ( this.getDomain() );
                this.localMember.setLocal ( true );
            }
            this.localMember.setSecurePort ( securePort );
            this.localMember.setUdpPort ( udpPort );
            this.localMember.getData ( true, true );
        } catch ( IOException x ) {
            throw new IllegalArgumentException ( x );
        }
    }
    public void setAddress ( final String addr ) {
        this.properties.setProperty ( "mcastAddress", addr );
    }
    public String getAddress() {
        return this.properties.getProperty ( "mcastAddress" );
    }
    public void setMcastBindAddress ( final String bindaddr ) {
        this.setBind ( bindaddr );
    }
    public void setBind ( final String bindaddr ) {
        this.properties.setProperty ( "mcastBindAddress", bindaddr );
    }
    public String getBind() {
        return this.properties.getProperty ( "mcastBindAddress" );
    }
    public void setPort ( final int port ) {
        this.properties.setProperty ( "mcastPort", String.valueOf ( port ) );
    }
    public void setRecoveryCounter ( final int recoveryCounter ) {
        this.properties.setProperty ( "recoveryCounter", String.valueOf ( recoveryCounter ) );
    }
    public int getRecoveryCounter() {
        final String p = this.properties.getProperty ( "recoveryCounter" );
        if ( p != null ) {
            return Integer.parseInt ( p );
        }
        return -1;
    }
    public void setRecoveryEnabled ( final boolean recoveryEnabled ) {
        this.properties.setProperty ( "recoveryEnabled", String.valueOf ( recoveryEnabled ) );
    }
    public boolean getRecoveryEnabled() {
        final String p = this.properties.getProperty ( "recoveryEnabled" );
        return p != null && Boolean.parseBoolean ( p );
    }
    public void setRecoverySleepTime ( final long recoverySleepTime ) {
        this.properties.setProperty ( "recoverySleepTime", String.valueOf ( recoverySleepTime ) );
    }
    public long getRecoverySleepTime() {
        final String p = this.properties.getProperty ( "recoverySleepTime" );
        if ( p != null ) {
            return Long.parseLong ( p );
        }
        return -1L;
    }
    public void setLocalLoopbackDisabled ( final boolean localLoopbackDisabled ) {
        this.properties.setProperty ( "localLoopbackDisabled", String.valueOf ( localLoopbackDisabled ) );
    }
    public boolean getLocalLoopbackDisabled() {
        final String p = this.properties.getProperty ( "localLoopbackDisabled" );
        return p != null && Boolean.parseBoolean ( p );
    }
    public int getPort() {
        final String p = this.properties.getProperty ( "mcastPort" );
        return Integer.parseInt ( p );
    }
    public void setFrequency ( final long time ) {
        this.properties.setProperty ( "mcastFrequency", String.valueOf ( time ) );
    }
    public long getFrequency() {
        final String p = this.properties.getProperty ( "mcastFrequency" );
        return Long.parseLong ( p );
    }
    public void setMcastDropTime ( final long time ) {
        this.setDropTime ( time );
    }
    public void setDropTime ( final long time ) {
        this.properties.setProperty ( "memberDropTime", String.valueOf ( time ) );
    }
    public long getDropTime() {
        final String p = this.properties.getProperty ( "memberDropTime" );
        return Long.parseLong ( p );
    }
    protected void hasProperty ( final Properties properties, final String name ) {
        if ( properties.getProperty ( name ) == null ) {
            throw new IllegalArgumentException ( McastService.sm.getString ( "mcastService.missing.property", name ) );
        }
    }
    @Override
    public void start() throws Exception {
        this.start ( 4 );
        this.start ( 8 );
    }
    @Override
    public void start ( final int level ) throws Exception {
        this.hasProperty ( this.properties, "mcastPort" );
        this.hasProperty ( this.properties, "mcastAddress" );
        this.hasProperty ( this.properties, "memberDropTime" );
        this.hasProperty ( this.properties, "mcastFrequency" );
        this.hasProperty ( this.properties, "tcpListenPort" );
        this.hasProperty ( this.properties, "tcpListenHost" );
        this.hasProperty ( this.properties, "tcpSecurePort" );
        this.hasProperty ( this.properties, "udpListenPort" );
        if ( this.impl != null ) {
            this.impl.start ( level );
            return;
        }
        final String host = this.getProperties().getProperty ( "tcpListenHost" );
        final int port = Integer.parseInt ( this.getProperties().getProperty ( "tcpListenPort" ) );
        final int securePort = Integer.parseInt ( this.getProperties().getProperty ( "tcpSecurePort" ) );
        final int udpPort = Integer.parseInt ( this.getProperties().getProperty ( "udpListenPort" ) );
        if ( this.localMember == null ) {
            ( this.localMember = new MemberImpl ( host, port, 100L ) ).setUniqueId ( UUIDGenerator.randomUUID ( true ) );
            this.localMember.setLocal ( true );
        } else {
            this.localMember.setHostname ( host );
            this.localMember.setPort ( port );
            this.localMember.setMemberAliveTime ( 100L );
        }
        this.localMember.setSecurePort ( securePort );
        this.localMember.setUdpPort ( udpPort );
        if ( this.payload != null ) {
            this.localMember.setPayload ( this.payload );
        }
        if ( this.domain != null ) {
            this.localMember.setDomain ( this.domain );
        }
        this.localMember.setServiceStartTime ( System.currentTimeMillis() );
        InetAddress bind = null;
        if ( this.properties.getProperty ( "mcastBindAddress" ) != null ) {
            bind = InetAddress.getByName ( this.properties.getProperty ( "mcastBindAddress" ) );
        }
        int ttl = -1;
        int soTimeout = -1;
        if ( this.properties.getProperty ( "mcastTTL" ) != null ) {
            try {
                ttl = Integer.parseInt ( this.properties.getProperty ( "mcastTTL" ) );
            } catch ( Exception x ) {
                McastService.log.error ( McastService.sm.getString ( "McastService.parseTTL", this.properties.getProperty ( "mcastTTL" ) ), x );
            }
        }
        if ( this.properties.getProperty ( "mcastSoTimeout" ) != null ) {
            try {
                soTimeout = Integer.parseInt ( this.properties.getProperty ( "mcastSoTimeout" ) );
            } catch ( Exception x ) {
                McastService.log.error ( McastService.sm.getString ( "McastService.parseSoTimeout", this.properties.getProperty ( "mcastSoTimeout" ) ), x );
            }
        }
        this.impl = new McastServiceImpl ( this.localMember, Long.parseLong ( this.properties.getProperty ( "mcastFrequency" ) ), Long.parseLong ( this.properties.getProperty ( "memberDropTime" ) ), Integer.parseInt ( this.properties.getProperty ( "mcastPort" ) ), bind, InetAddress.getByName ( this.properties.getProperty ( "mcastAddress" ) ), ttl, soTimeout, this, this, Boolean.parseBoolean ( this.properties.getProperty ( "localLoopbackDisabled", "false" ) ) );
        final String value = this.properties.getProperty ( "recoveryEnabled", "true" );
        final boolean recEnabled = Boolean.parseBoolean ( value );
        this.impl.setRecoveryEnabled ( recEnabled );
        final int recCnt = Integer.parseInt ( this.properties.getProperty ( "recoveryCounter", "10" ) );
        this.impl.setRecoveryCounter ( recCnt );
        final long recSlpTime = Long.parseLong ( this.properties.getProperty ( "recoverySleepTime", "5000" ) );
        this.impl.setRecoverySleepTime ( recSlpTime );
        this.impl.setChannel ( this.channel );
        this.impl.start ( level );
    }
    @Override
    public void stop ( final int svc ) {
        try {
            if ( this.impl != null && this.impl.stop ( svc ) ) {
                this.impl.setChannel ( null );
                this.impl = null;
                this.channel = null;
            }
        } catch ( Exception x ) {
            McastService.log.error ( McastService.sm.getString ( "McastService.stopFail", svc ), x );
        }
    }
    @Override
    public String[] getMembersByName() {
        final Member[] currentMembers = this.getMembers();
        String[] membernames;
        if ( currentMembers != null ) {
            membernames = new String[currentMembers.length];
            for ( int i = 0; i < currentMembers.length; ++i ) {
                membernames[i] = currentMembers[i].toString();
            }
        } else {
            membernames = new String[0];
        }
        return membernames;
    }
    @Override
    public Member findMemberByName ( final String name ) {
        final Member[] currentMembers = this.getMembers();
        for ( int i = 0; i < currentMembers.length; ++i ) {
            if ( name.equals ( currentMembers[i].toString() ) ) {
                return currentMembers[i];
            }
        }
        return null;
    }
    @Override
    public boolean hasMembers() {
        return this.impl != null && this.impl.membership != null && this.impl.membership.hasMembers();
    }
    @Override
    public Member getMember ( final Member mbr ) {
        if ( this.impl == null || this.impl.membership == null ) {
            return null;
        }
        return this.impl.membership.getMember ( mbr );
    }
    @Override
    public Member[] getMembers() {
        if ( this.impl == null || this.impl.membership == null ) {
            return McastService.EMPTY_MEMBERS;
        }
        return this.impl.membership.getMembers();
    }
    @Override
    public void setMembershipListener ( final MembershipListener listener ) {
        this.listener = listener;
    }
    public void setMessageListener ( final MessageListener listener ) {
        this.msglistener = listener;
    }
    public void removeMessageListener() {
        this.msglistener = null;
    }
    @Override
    public void removeMembershipListener() {
        this.listener = null;
    }
    @Override
    public void memberAdded ( final Member member ) {
        final MembershipListener listener = this.listener;
        if ( listener != null ) {
            listener.memberAdded ( member );
        }
    }
    @Override
    public void memberDisappeared ( final Member member ) {
        final MembershipListener listener = this.listener;
        if ( listener != null ) {
            listener.memberDisappeared ( member );
        }
    }
    @Override
    public void messageReceived ( final ChannelMessage msg ) {
        if ( this.msglistener != null && this.msglistener.accept ( msg ) ) {
            this.msglistener.messageReceived ( msg );
        }
    }
    @Override
    public boolean accept ( final ChannelMessage msg ) {
        return true;
    }
    @Override
    public void broadcast ( final ChannelMessage message ) throws ChannelException {
        if ( this.impl == null || ( this.impl.startLevel & 0x8 ) != 0x8 ) {
            throw new ChannelException ( McastService.sm.getString ( "mcastService.noStart" ) );
        }
        final byte[] data = XByteBuffer.createDataPackage ( ( ChannelData ) message );
        if ( data.length > 65535 ) {
            throw new ChannelException ( McastService.sm.getString ( "mcastService.exceed.maxPacketSize", Integer.toString ( data.length ), Integer.toString ( 65535 ) ) );
        }
        final DatagramPacket packet = new DatagramPacket ( data, 0, data.length );
        try {
            this.impl.send ( false, packet );
        } catch ( Exception x ) {
            throw new ChannelException ( x );
        }
    }
    public int getSoTimeout() {
        return this.mcastSoTimeout;
    }
    public void setSoTimeout ( final int mcastSoTimeout ) {
        this.mcastSoTimeout = mcastSoTimeout;
        this.properties.setProperty ( "mcastSoTimeout", String.valueOf ( mcastSoTimeout ) );
    }
    public int getTtl() {
        return this.mcastTTL;
    }
    public byte[] getPayload() {
        return this.payload;
    }
    public byte[] getDomain() {
        return this.domain;
    }
    public void setTtl ( final int mcastTTL ) {
        this.mcastTTL = mcastTTL;
        this.properties.setProperty ( "mcastTTL", String.valueOf ( mcastTTL ) );
    }
    @Override
    public void setPayload ( final byte[] payload ) {
        this.payload = payload;
        if ( this.localMember != null ) {
            this.localMember.setPayload ( payload );
            try {
                if ( this.impl != null ) {
                    this.impl.send ( false );
                }
            } catch ( Exception x ) {
                McastService.log.error ( McastService.sm.getString ( "McastService.payload" ), x );
            }
        }
    }
    @Override
    public void setDomain ( final byte[] domain ) {
        this.domain = domain;
        if ( this.localMember != null ) {
            this.localMember.setDomain ( domain );
            try {
                if ( this.impl != null ) {
                    this.impl.send ( false );
                }
            } catch ( Exception x ) {
                McastService.log.error ( McastService.sm.getString ( "McastService.domain" ), x );
            }
        }
    }
    public void setDomain ( final String domain ) {
        if ( domain == null ) {
            return;
        }
        if ( domain.startsWith ( "{" ) ) {
            this.setDomain ( Arrays.fromString ( domain ) );
        } else {
            this.setDomain ( Arrays.convert ( domain ) );
        }
    }
    @Override
    public Channel getChannel() {
        return this.channel;
    }
    @Override
    public void setChannel ( final Channel channel ) {
        this.channel = channel;
    }
    public static void main ( final String[] args ) throws Exception {
        final McastService service = new McastService();
        final Properties p = new Properties();
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
        Thread.sleep ( 3600000L );
    }
    static {
        log = LogFactory.getLog ( McastService.class );
        sm = StringManager.getManager ( "org.apache.catalina.tribes.membership" );
        EMPTY_MEMBERS = new Member[0];
    }
}
