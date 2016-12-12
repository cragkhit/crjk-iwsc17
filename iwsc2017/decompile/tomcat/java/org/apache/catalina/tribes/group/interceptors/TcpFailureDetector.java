package org.apache.catalina.tribes.group.interceptors;
import org.apache.juli.logging.LogFactory;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.io.ChannelData;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.Socket;
import org.apache.catalina.tribes.membership.StaticMember;
import java.util.Arrays;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.RemoteProcessException;
import org.apache.catalina.tribes.group.InterceptorPayload;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import java.util.HashMap;
import org.apache.catalina.tribes.membership.Membership;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.tribes.group.ChannelInterceptorBase;
public class TcpFailureDetector extends ChannelInterceptorBase {
    private static final Log log;
    protected static final StringManager sm;
    protected static final byte[] TCP_FAIL_DETECT;
    protected long connectTimeout;
    protected boolean performSendTest;
    protected boolean performReadTest;
    protected long readTestTimeout;
    protected Membership membership;
    protected final HashMap<Member, Long> removeSuspects;
    protected final HashMap<Member, Long> addSuspects;
    protected int removeSuspectsTimeout;
    public TcpFailureDetector() {
        this.connectTimeout = 1000L;
        this.performSendTest = true;
        this.performReadTest = false;
        this.readTestTimeout = 5000L;
        this.membership = null;
        this.removeSuspects = new HashMap<Member, Long>();
        this.addSuspects = new HashMap<Member, Long>();
        this.removeSuspectsTimeout = 300;
    }
    @Override
    public void sendMessage ( final Member[] destination, final ChannelMessage msg, final InterceptorPayload payload ) throws ChannelException {
        try {
            super.sendMessage ( destination, msg, payload );
        } catch ( ChannelException cx ) {
            final ChannelException.FaultyMember[] mbrs = cx.getFaultyMembers();
            for ( int i = 0; i < mbrs.length; ++i ) {
                if ( mbrs[i].getCause() != null && ! ( mbrs[i].getCause() instanceof RemoteProcessException ) ) {
                    this.memberDisappeared ( mbrs[i].getMember() );
                }
            }
            throw cx;
        }
    }
    @Override
    public void messageReceived ( final ChannelMessage msg ) {
        boolean process = true;
        if ( this.okToProcess ( msg.getOptions() ) ) {
            process = ( msg.getMessage().getLength() != TcpFailureDetector.TCP_FAIL_DETECT.length || !Arrays.equals ( TcpFailureDetector.TCP_FAIL_DETECT, msg.getMessage().getBytes() ) );
        }
        if ( process ) {
            super.messageReceived ( msg );
        } else if ( TcpFailureDetector.log.isDebugEnabled() ) {
            TcpFailureDetector.log.debug ( "Received a failure detector packet:" + msg );
        }
    }
    @Override
    public void memberAdded ( final Member member ) {
        if ( this.membership == null ) {
            this.setupMembership();
        }
        boolean notify = false;
        synchronized ( this.membership ) {
            if ( this.removeSuspects.containsKey ( member ) ) {
                this.removeSuspects.remove ( member );
            } else if ( this.membership.getMember ( member ) == null ) {
                if ( this.memberAlive ( member ) ) {
                    this.membership.memberAlive ( member );
                    notify = true;
                } else {
                    this.addSuspects.put ( member, System.currentTimeMillis() );
                }
            }
        }
        if ( notify ) {
            super.memberAdded ( member );
        }
    }
    @Override
    public void memberDisappeared ( final Member member ) {
        if ( this.membership == null ) {
            this.setupMembership();
        }
        final boolean shutdown = Arrays.equals ( member.getCommand(), Member.SHUTDOWN_PAYLOAD );
        if ( shutdown ) {
            synchronized ( this.membership ) {
                if ( !this.membership.contains ( member ) ) {
                    return;
                }
                this.membership.removeMember ( member );
                this.removeSuspects.remove ( member );
                if ( member instanceof StaticMember ) {
                    this.addSuspects.put ( member, System.currentTimeMillis() );
                }
            }
            super.memberDisappeared ( member );
        } else {
            boolean notify = false;
            if ( TcpFailureDetector.log.isInfoEnabled() ) {
                TcpFailureDetector.log.info ( TcpFailureDetector.sm.getString ( "tcpFailureDetector.memberDisappeared.verify", member ) );
            }
            synchronized ( this.membership ) {
                if ( !this.membership.contains ( member ) ) {
                    if ( TcpFailureDetector.log.isInfoEnabled() ) {
                        TcpFailureDetector.log.info ( TcpFailureDetector.sm.getString ( "tcpFailureDetector.already.disappeared", member ) );
                    }
                    return;
                }
                if ( !this.memberAlive ( member ) ) {
                    this.membership.removeMember ( member );
                    this.removeSuspects.remove ( member );
                    if ( member instanceof StaticMember ) {
                        this.addSuspects.put ( member, System.currentTimeMillis() );
                    }
                    notify = true;
                } else {
                    this.removeSuspects.put ( member, System.currentTimeMillis() );
                }
            }
            if ( notify ) {
                if ( TcpFailureDetector.log.isInfoEnabled() ) {
                    TcpFailureDetector.log.info ( TcpFailureDetector.sm.getString ( "tcpFailureDetector.member.disappeared", member ) );
                }
                super.memberDisappeared ( member );
            } else if ( TcpFailureDetector.log.isInfoEnabled() ) {
                TcpFailureDetector.log.info ( TcpFailureDetector.sm.getString ( "tcpFailureDetector.still.alive", member ) );
            }
        }
    }
    @Override
    public boolean hasMembers() {
        if ( this.membership == null ) {
            this.setupMembership();
        }
        return this.membership.hasMembers();
    }
    @Override
    public Member[] getMembers() {
        if ( this.membership == null ) {
            this.setupMembership();
        }
        return this.membership.getMembers();
    }
    @Override
    public Member getMember ( final Member mbr ) {
        if ( this.membership == null ) {
            this.setupMembership();
        }
        return this.membership.getMember ( mbr );
    }
    @Override
    public Member getLocalMember ( final boolean incAlive ) {
        return super.getLocalMember ( incAlive );
    }
    @Override
    public void heartbeat() {
        super.heartbeat();
        this.checkMembers ( false );
    }
    public void checkMembers ( final boolean checkAll ) {
        try {
            if ( this.membership == null ) {
                this.setupMembership();
            }
            synchronized ( this.membership ) {
                if ( !checkAll ) {
                    this.performBasicCheck();
                } else {
                    this.performForcedCheck();
                }
            }
        } catch ( Exception x ) {
            TcpFailureDetector.log.warn ( TcpFailureDetector.sm.getString ( "tcpFailureDetector.heartbeat.failed" ), x );
        }
    }
    protected void performForcedCheck() {
        final Member[] members = super.getMembers();
        for ( int i = 0; members != null && i < members.length; ++i ) {
            if ( this.memberAlive ( members[i] ) ) {
                if ( this.membership.memberAlive ( members[i] ) ) {
                    super.memberAdded ( members[i] );
                }
                this.addSuspects.remove ( members[i] );
            } else if ( this.membership.getMember ( members[i] ) != null ) {
                this.membership.removeMember ( members[i] );
                this.removeSuspects.remove ( members[i] );
                if ( members[i] instanceof StaticMember ) {
                    this.addSuspects.put ( members[i], System.currentTimeMillis() );
                }
                super.memberDisappeared ( members[i] );
            }
        }
    }
    protected void performBasicCheck() {
        final Member[] members = super.getMembers();
        for ( int i = 0; members != null && i < members.length; ++i ) {
            if ( !this.addSuspects.containsKey ( members[i] ) || this.membership.getMember ( members[i] ) != null ) {
                if ( this.membership.memberAlive ( members[i] ) ) {
                    if ( this.memberAlive ( members[i] ) ) {
                        TcpFailureDetector.log.warn ( TcpFailureDetector.sm.getString ( "tcpFailureDetector.performBasicCheck.memberAdded", members[i] ) );
                        super.memberAdded ( members[i] );
                    } else {
                        this.membership.removeMember ( members[i] );
                    }
                }
            }
        }
        Member[] keys = this.removeSuspects.keySet().toArray ( new Member[this.removeSuspects.size()] );
        for ( int j = 0; j < keys.length; ++j ) {
            final Member m = keys[j];
            if ( this.membership.getMember ( m ) != null && !this.memberAlive ( m ) ) {
                this.membership.removeMember ( m );
                if ( m instanceof StaticMember ) {
                    this.addSuspects.put ( m, System.currentTimeMillis() );
                }
                super.memberDisappeared ( m );
                this.removeSuspects.remove ( m );
                if ( TcpFailureDetector.log.isInfoEnabled() ) {
                    TcpFailureDetector.log.info ( TcpFailureDetector.sm.getString ( "tcpFailureDetector.suspectMember.dead", m ) );
                }
            } else if ( this.removeSuspectsTimeout > 0 ) {
                final long timeNow = System.currentTimeMillis();
                final int timeIdle = ( int ) ( ( timeNow - this.removeSuspects.get ( m ) ) / 1000L );
                if ( timeIdle > this.removeSuspectsTimeout ) {
                    this.removeSuspects.remove ( m );
                }
            }
        }
        keys = this.addSuspects.keySet().toArray ( new Member[this.addSuspects.size()] );
        for ( int j = 0; j < keys.length; ++j ) {
            final Member m = keys[j];
            if ( this.membership.getMember ( m ) == null && this.memberAlive ( m ) ) {
                this.membership.memberAlive ( m );
                super.memberAdded ( m );
                this.addSuspects.remove ( m );
                if ( TcpFailureDetector.log.isInfoEnabled() ) {
                    TcpFailureDetector.log.info ( TcpFailureDetector.sm.getString ( "tcpFailureDetector.suspectMember.alive", m ) );
                }
            }
        }
    }
    protected synchronized void setupMembership() {
        if ( this.membership == null ) {
            this.membership = new Membership ( super.getLocalMember ( true ) );
        }
    }
    protected boolean memberAlive ( final Member mbr ) {
        return this.memberAlive ( mbr, TcpFailureDetector.TCP_FAIL_DETECT, this.performSendTest, this.performReadTest, this.readTestTimeout, this.connectTimeout, this.getOptionFlag() );
    }
    protected boolean memberAlive ( final Member mbr, final byte[] msgData, final boolean sendTest, final boolean readTest, final long readTimeout, final long conTimeout, final int optionFlag ) {
        if ( Arrays.equals ( mbr.getCommand(), Member.SHUTDOWN_PAYLOAD ) ) {
            return false;
        }
        try ( final Socket socket = new Socket() ) {
            final InetAddress ia = InetAddress.getByAddress ( mbr.getHost() );
            final InetSocketAddress addr = new InetSocketAddress ( ia, mbr.getPort() );
            socket.setSoTimeout ( ( int ) readTimeout );
            socket.connect ( addr, ( int ) conTimeout );
            if ( sendTest ) {
                final ChannelData data = new ChannelData ( true );
                data.setAddress ( this.getLocalMember ( false ) );
                data.setMessage ( new XByteBuffer ( msgData, false ) );
                data.setTimestamp ( System.currentTimeMillis() );
                int options = optionFlag | 0x1;
                if ( readTest ) {
                    options |= 0x2;
                } else {
                    options &= 0xFFFFFFFD;
                }
                data.setOptions ( options );
                final byte[] message = XByteBuffer.createDataPackage ( data );
                socket.getOutputStream().write ( message );
                if ( readTest ) {
                    final int length = socket.getInputStream().read ( message );
                    return length > 0;
                }
            }
            return true;
        } catch ( SocketTimeoutException ex ) {}
        catch ( ConnectException ex2 ) {}
        catch ( Exception x ) {
            TcpFailureDetector.log.error ( TcpFailureDetector.sm.getString ( "tcpFailureDetector.failureDetection.failed" ), x );
        }
        return false;
    }
    public long getReadTestTimeout() {
        return this.readTestTimeout;
    }
    public boolean getPerformSendTest() {
        return this.performSendTest;
    }
    public boolean getPerformReadTest() {
        return this.performReadTest;
    }
    public long getConnectTimeout() {
        return this.connectTimeout;
    }
    public int getRemoveSuspectsTimeout() {
        return this.removeSuspectsTimeout;
    }
    public void setPerformReadTest ( final boolean performReadTest ) {
        this.performReadTest = performReadTest;
    }
    public void setPerformSendTest ( final boolean performSendTest ) {
        this.performSendTest = performSendTest;
    }
    public void setReadTestTimeout ( final long readTestTimeout ) {
        this.readTestTimeout = readTestTimeout;
    }
    public void setConnectTimeout ( final long connectTimeout ) {
        this.connectTimeout = connectTimeout;
    }
    public void setRemoveSuspectsTimeout ( final int removeSuspectsTimeout ) {
        this.removeSuspectsTimeout = removeSuspectsTimeout;
    }
    static {
        log = LogFactory.getLog ( TcpFailureDetector.class );
        sm = StringManager.getManager ( TcpFailureDetector.class );
        TCP_FAIL_DETECT = new byte[] { 79, -89, 115, 72, 121, -126, 67, -55, -97, 111, -119, -128, -95, 91, 7, 20, 125, -39, 82, 91, -21, -15, 67, -102, -73, 126, -66, -113, -127, 103, 30, -74, 55, 21, -66, -121, 69, 126, 76, -88, -65, 10, 77, 19, 83, 56, 21, 50, 85, -10, -108, -73, 58, -6, 64, 120, -111, 4, 125, -41, 114, -124, -64, -43 };
    }
}
