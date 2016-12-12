package org.apache.catalina.tribes.group;
import org.apache.catalina.tribes.transport.SenderState;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.MembershipListener;
import org.apache.catalina.tribes.membership.StaticMember;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.util.Arrays;
import java.sql.Timestamp;
import org.apache.catalina.tribes.UniqueId;
import org.apache.catalina.tribes.util.Logs;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.membership.McastService;
import org.apache.catalina.tribes.transport.ReplicationTransmitter;
import org.apache.catalina.tribes.transport.nio.NioReceiver;
import org.apache.catalina.tribes.MembershipService;
import org.apache.catalina.tribes.ChannelSender;
import org.apache.catalina.tribes.ChannelReceiver;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.catalina.tribes.MessageListener;
public class ChannelCoordinator extends ChannelInterceptorBase implements MessageListener {
    protected static final StringManager sm;
    private ChannelReceiver clusterReceiver;
    private ChannelSender clusterSender;
    private MembershipService membershipService;
    private int startLevel;
    public ChannelCoordinator() {
        this ( new NioReceiver(), new ReplicationTransmitter(), new McastService() );
    }
    public ChannelCoordinator ( final ChannelReceiver receiver, final ChannelSender sender, final MembershipService service ) {
        this.startLevel = 0;
        this.optionFlag = 7;
        this.setClusterReceiver ( receiver );
        this.setClusterSender ( sender );
        this.setMembershipService ( service );
    }
    @Override
    public void sendMessage ( Member[] destination, final ChannelMessage msg, final InterceptorPayload payload ) throws ChannelException {
        if ( destination == null ) {
            destination = this.membershipService.getMembers();
        }
        if ( ( msg.getOptions() & 0x40 ) == 0x40 ) {
            this.membershipService.broadcast ( msg );
        } else {
            this.clusterSender.sendMessage ( msg, destination );
        }
        if ( Logs.MESSAGES.isTraceEnabled() ) {
            Logs.MESSAGES.trace ( "ChannelCoordinator - Sent msg:" + new UniqueId ( msg.getUniqueId() ) + " at " + new Timestamp ( System.currentTimeMillis() ) + " to " + Arrays.toNameString ( destination ) );
        }
    }
    @Override
    public void start ( final int svc ) throws ChannelException {
        this.internalStart ( svc );
    }
    @Override
    public void stop ( final int svc ) throws ChannelException {
        this.internalStop ( svc );
    }
    protected synchronized void internalStart ( int svc ) throws ChannelException {
        try {
            boolean valid = false;
            svc &= 0xF;
            if ( this.startLevel == 15 ) {
                return;
            }
            if ( svc == 0 ) {
                return;
            }
            if ( svc == ( svc & this.startLevel ) ) {
                throw new ChannelException ( ChannelCoordinator.sm.getString ( "channelCoordinator.alreadyStarted", Integer.toString ( svc ) ) );
            }
            if ( 0x1 == ( svc & 0x1 ) ) {
                this.clusterReceiver.setMessageListener ( this );
                this.clusterReceiver.setChannel ( this.getChannel() );
                this.clusterReceiver.start();
                final Member localMember = this.getChannel().getLocalMember ( false );
                if ( localMember instanceof StaticMember ) {
                    final StaticMember staticMember = ( StaticMember ) localMember;
                    staticMember.setHost ( this.getClusterReceiver().getHost() );
                    staticMember.setPort ( this.getClusterReceiver().getPort() );
                    staticMember.setSecurePort ( this.getClusterReceiver().getSecurePort() );
                } else {
                    this.membershipService.setLocalMemberProperties ( this.getClusterReceiver().getHost(), this.getClusterReceiver().getPort(), this.getClusterReceiver().getSecurePort(), this.getClusterReceiver().getUdpPort() );
                }
                valid = true;
            }
            if ( 0x2 == ( svc & 0x2 ) ) {
                this.clusterSender.setChannel ( this.getChannel() );
                this.clusterSender.start();
                valid = true;
            }
            if ( 0x4 == ( svc & 0x4 ) ) {
                this.membershipService.setMembershipListener ( this );
                this.membershipService.setChannel ( this.getChannel() );
                if ( this.membershipService instanceof McastService ) {
                    ( ( McastService ) this.membershipService ).setMessageListener ( this );
                }
                this.membershipService.start ( 4 );
                valid = true;
            }
            if ( 0x8 == ( svc & 0x8 ) ) {
                this.membershipService.setChannel ( this.getChannel() );
                this.membershipService.start ( 8 );
                valid = true;
            }
            if ( !valid ) {
                throw new IllegalArgumentException ( ChannelCoordinator.sm.getString ( "channelCoordinator.invalid.startLevel" ) );
            }
            this.startLevel |= svc;
        } catch ( ChannelException cx ) {
            throw cx;
        } catch ( Exception x ) {
            throw new ChannelException ( x );
        }
    }
    protected synchronized void internalStop ( int svc ) throws ChannelException {
        try {
            svc &= 0xF;
            if ( this.startLevel == 0 ) {
                return;
            }
            if ( svc == 0 ) {
                return;
            }
            boolean valid = false;
            if ( 0x1 == ( svc & 0x1 ) ) {
                this.clusterReceiver.stop();
                this.clusterReceiver.setMessageListener ( null );
                valid = true;
            }
            if ( 0x2 == ( svc & 0x2 ) ) {
                this.clusterSender.stop();
                valid = true;
            }
            if ( 0x4 == ( svc & 0x4 ) ) {
                this.membershipService.stop ( 4 );
                this.membershipService.setMembershipListener ( null );
                valid = true;
            }
            if ( 0x8 == ( svc & 0x8 ) ) {
                valid = true;
                this.membershipService.stop ( 8 );
            }
            if ( !valid ) {
                throw new IllegalArgumentException ( ChannelCoordinator.sm.getString ( "channelCoordinator.invalid.startLevel" ) );
            }
            this.startLevel &= ~svc;
            this.setChannel ( null );
        } catch ( Exception x ) {
            throw new ChannelException ( x );
        }
    }
    @Override
    public void memberAdded ( final Member member ) {
        SenderState.getSenderState ( member );
        super.memberAdded ( member );
    }
    @Override
    public void memberDisappeared ( final Member member ) {
        SenderState.removeSenderState ( member );
        super.memberDisappeared ( member );
    }
    @Override
    public void messageReceived ( final ChannelMessage msg ) {
        if ( Logs.MESSAGES.isTraceEnabled() ) {
            Logs.MESSAGES.trace ( "ChannelCoordinator - Received msg:" + new UniqueId ( msg.getUniqueId() ) + " at " + new Timestamp ( System.currentTimeMillis() ) + " from " + msg.getAddress().getName() );
        }
        super.messageReceived ( msg );
    }
    @Override
    public boolean accept ( final ChannelMessage msg ) {
        return true;
    }
    public ChannelReceiver getClusterReceiver() {
        return this.clusterReceiver;
    }
    public ChannelSender getClusterSender() {
        return this.clusterSender;
    }
    public MembershipService getMembershipService() {
        return this.membershipService;
    }
    public void setClusterReceiver ( final ChannelReceiver clusterReceiver ) {
        if ( clusterReceiver != null ) {
            ( this.clusterReceiver = clusterReceiver ).setMessageListener ( this );
        } else {
            if ( this.clusterReceiver != null ) {
                this.clusterReceiver.setMessageListener ( null );
            }
            this.clusterReceiver = null;
        }
    }
    public void setClusterSender ( final ChannelSender clusterSender ) {
        this.clusterSender = clusterSender;
    }
    public void setMembershipService ( final MembershipService membershipService ) {
        ( this.membershipService = membershipService ).setMembershipListener ( this );
    }
    @Override
    public void heartbeat() {
        if ( this.clusterSender != null ) {
            this.clusterSender.heartbeat();
        }
        super.heartbeat();
    }
    @Override
    public boolean hasMembers() {
        return this.getMembershipService().hasMembers();
    }
    @Override
    public Member[] getMembers() {
        return this.getMembershipService().getMembers();
    }
    @Override
    public Member getMember ( final Member mbr ) {
        return this.getMembershipService().getMember ( mbr );
    }
    @Override
    public Member getLocalMember ( final boolean incAlive ) {
        return this.getMembershipService().getLocalMember ( incAlive );
    }
    static {
        sm = StringManager.getManager ( ChannelCoordinator.class );
    }
}
