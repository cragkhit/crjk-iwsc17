package org.apache.catalina.tribes.group.interceptors;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.tribes.group.InterceptorPayload;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.io.ChannelData;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ChannelInterceptor;
import java.util.Iterator;
import org.apache.catalina.tribes.group.AbsoluteOrder;
import java.util.Arrays;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import java.util.ArrayList;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.tribes.group.ChannelInterceptorBase;
public class StaticMembershipInterceptor extends ChannelInterceptorBase {
    private static final Log log;
    protected static final StringManager sm;
    protected static final byte[] MEMBER_START;
    protected static final byte[] MEMBER_STOP;
    protected final ArrayList<Member> members;
    protected Member localMember;
    public StaticMembershipInterceptor() {
        this.members = new ArrayList<Member>();
        this.localMember = null;
    }
    public void addStaticMember ( final Member member ) {
        synchronized ( this.members ) {
            if ( !this.members.contains ( member ) ) {
                this.members.add ( member );
            }
        }
    }
    public void removeStaticMember ( final Member member ) {
        synchronized ( this.members ) {
            if ( this.members.contains ( member ) ) {
                this.members.remove ( member );
            }
        }
    }
    public void setLocalMember ( final Member member ) {
        ( this.localMember = member ).setLocal ( true );
    }
    @Override
    public void messageReceived ( final ChannelMessage msg ) {
        if ( msg.getMessage().getLength() == StaticMembershipInterceptor.MEMBER_START.length && Arrays.equals ( StaticMembershipInterceptor.MEMBER_START, msg.getMessage().getBytes() ) ) {
            final Member member = this.getMember ( msg.getAddress() );
            if ( member != null ) {
                super.memberAdded ( member );
            }
        } else if ( msg.getMessage().getLength() == StaticMembershipInterceptor.MEMBER_STOP.length && Arrays.equals ( StaticMembershipInterceptor.MEMBER_STOP, msg.getMessage().getBytes() ) ) {
            final Member member = this.getMember ( msg.getAddress() );
            if ( member != null ) {
                try {
                    member.setCommand ( Member.SHUTDOWN_PAYLOAD );
                    super.memberDisappeared ( member );
                } finally {
                    member.setCommand ( new byte[0] );
                }
            }
        } else {
            super.messageReceived ( msg );
        }
    }
    @Override
    public boolean hasMembers() {
        return super.hasMembers() || this.members.size() > 0;
    }
    @Override
    public Member[] getMembers() {
        if ( this.members.size() == 0 ) {
            return super.getMembers();
        }
        synchronized ( this.members ) {
            final Member[] others = super.getMembers();
            final Member[] result = new Member[this.members.size() + others.length];
            for ( int i = 0; i < others.length; ++i ) {
                result[i] = others[i];
            }
            for ( int i = 0; i < this.members.size(); ++i ) {
                result[i + others.length] = this.members.get ( i );
            }
            AbsoluteOrder.absoluteOrder ( result );
            return result;
        }
    }
    @Override
    public Member getMember ( final Member mbr ) {
        if ( this.members.contains ( mbr ) ) {
            return this.members.get ( this.members.indexOf ( mbr ) );
        }
        return super.getMember ( mbr );
    }
    @Override
    public Member getLocalMember ( final boolean incAlive ) {
        if ( this.localMember != null ) {
            return this.localMember;
        }
        return super.getLocalMember ( incAlive );
    }
    @Override
    public void start ( final int svc ) throws ChannelException {
        if ( ( 0x1 & svc ) == 0x1 ) {
            super.start ( 1 );
        }
        if ( ( 0x2 & svc ) == 0x2 ) {
            super.start ( 2 );
        }
        for ( final Member member : this.members ) {
            final Thread t = new Thread() {
                final   ChannelInterceptorBase val$base;
                @Override
                public void run() {
                    this.val$base.memberAdded ( member );
                    if ( StaticMembershipInterceptor.this.getfirstInterceptor().getMember ( member ) != null ) {
                        StaticMembershipInterceptor.this.sendLocalMember ( new Member[] { member } );
                    }
                }
            };
            t.start();
        }
        super.start ( svc & 0xFFFFFFFE & 0xFFFFFFFD );
        TcpFailureDetector failureDetector = null;
        TcpPingInterceptor pingInterceptor = null;
        for ( ChannelInterceptor prev = this.getPrevious(); prev != null; prev = prev.getPrevious() ) {
            if ( prev instanceof TcpFailureDetector ) {
                failureDetector = ( TcpFailureDetector ) prev;
            }
            if ( prev instanceof TcpPingInterceptor ) {
                pingInterceptor = ( TcpPingInterceptor ) prev;
            }
        }
        if ( failureDetector == null ) {
            StaticMembershipInterceptor.log.warn ( StaticMembershipInterceptor.sm.getString ( "staticMembershipInterceptor.no.failureDetector" ) );
        }
        if ( pingInterceptor == null ) {
            StaticMembershipInterceptor.log.warn ( StaticMembershipInterceptor.sm.getString ( "staticMembershipInterceptor.no.pingInterceptor" ) );
        }
    }
    @Override
    public void stop ( final int svc ) throws ChannelException {
        final Member[] members = this.getfirstInterceptor().getMembers();
        this.sendShutdown ( members );
        super.stop ( svc );
    }
    protected void sendLocalMember ( final Member[] members ) {
        try {
            this.sendMemberMessage ( members, StaticMembershipInterceptor.MEMBER_START );
        } catch ( ChannelException cx ) {
            StaticMembershipInterceptor.log.warn ( StaticMembershipInterceptor.sm.getString ( "staticMembershipInterceptor.sendLocalMember.failed" ), cx );
        }
    }
    protected void sendShutdown ( final Member[] members ) {
        try {
            this.sendMemberMessage ( members, StaticMembershipInterceptor.MEMBER_STOP );
        } catch ( ChannelException cx ) {
            StaticMembershipInterceptor.log.warn ( StaticMembershipInterceptor.sm.getString ( "staticMembershipInterceptor.sendShutdown.failed" ), cx );
        }
    }
    protected ChannelInterceptor getfirstInterceptor() {
        ChannelInterceptor result = null;
        ChannelInterceptor now = this;
        do {
            result = now;
            now = now.getPrevious();
        } while ( now.getPrevious() != null );
        return result;
    }
    protected void sendMemberMessage ( final Member[] members, final byte[] message ) throws ChannelException {
        if ( members == null || members.length == 0 ) {
            return;
        }
        final ChannelData data = new ChannelData ( true );
        data.setAddress ( this.getLocalMember ( false ) );
        data.setTimestamp ( System.currentTimeMillis() );
        data.setOptions ( this.getOptionFlag() );
        data.setMessage ( new XByteBuffer ( message, false ) );
        super.sendMessage ( members, data, null );
    }
    static {
        log = LogFactory.getLog ( StaticMembershipInterceptor.class );
        sm = StringManager.getManager ( StaticMembershipInterceptor.class );
        MEMBER_START = new byte[] { 76, 111, 99, 97, 108, 32, 83, 116, 97, 116, 105, 99, 77, 101, 109, 98, 101, 114, 32, 78, 111, 116, 105, 102, 105, 99, 97, 116, 105, 111, 110, 32, 68, 97, 116, 97 };
        MEMBER_STOP = new byte[] { 76, 111, 99, 97, 108, 32, 83, 116, 97, 116, 105, 99, 77, 101, 109, 98, 101, 114, 32, 83, 104, 117, 116, 100, 111, 119, 110, 32, 68, 97, 116, 97 };
    }
}
