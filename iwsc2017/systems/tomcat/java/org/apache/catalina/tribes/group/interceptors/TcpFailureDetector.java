package org.apache.catalina.tribes.group.interceptors;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ChannelException.FaultyMember;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.RemoteProcessException;
import org.apache.catalina.tribes.group.ChannelInterceptorBase;
import org.apache.catalina.tribes.group.InterceptorPayload;
import org.apache.catalina.tribes.io.ChannelData;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.membership.Membership;
import org.apache.catalina.tribes.membership.StaticMember;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class TcpFailureDetector extends ChannelInterceptorBase {
    private static final Log log = LogFactory.getLog ( TcpFailureDetector.class );
    protected static final StringManager sm = StringManager.getManager ( TcpFailureDetector.class );
    protected static final byte[] TCP_FAIL_DETECT = new byte[] {
        79, -89, 115, 72, 121, -126, 67, -55, -97, 111, -119, -128, -95, 91, 7, 20,
        125, -39, 82, 91, -21, -15, 67, -102, -73, 126, -66, -113, -127, 103, 30, -74,
        55, 21, -66, -121, 69, 126, 76, -88, -65, 10, 77, 19, 83, 56, 21, 50,
        85, -10, -108, -73, 58, -6, 64, 120, -111, 4, 125, -41, 114, -124, -64, -43
    };
    protected long connectTimeout = 1000;
    protected boolean performSendTest = true;
    protected boolean performReadTest = false;
    protected long readTestTimeout = 5000;
    protected Membership membership = null;
    protected final HashMap<Member, Long> removeSuspects = new HashMap<>();
    protected final HashMap<Member, Long> addSuspects = new HashMap<>();
    protected int removeSuspectsTimeout = 300;
    @Override
    public void sendMessage ( Member[] destination, ChannelMessage msg, InterceptorPayload payload ) throws ChannelException {
        try {
            super.sendMessage ( destination, msg, payload );
        } catch ( ChannelException cx ) {
            FaultyMember[] mbrs = cx.getFaultyMembers();
            for ( int i = 0; i < mbrs.length; i++ ) {
                if ( mbrs[i].getCause() != null &&
                        ( ! ( mbrs[i].getCause() instanceof RemoteProcessException ) ) ) {
                    this.memberDisappeared ( mbrs[i].getMember() );
                }
            }
            throw cx;
        }
    }
    @Override
    public void messageReceived ( ChannelMessage msg ) {
        boolean process = true;
        if ( okToProcess ( msg.getOptions() ) ) {
            process = ( ( msg.getMessage().getLength() != TCP_FAIL_DETECT.length ) ||
                        ( !Arrays.equals ( TCP_FAIL_DETECT, msg.getMessage().getBytes() ) ) );
        }
        if ( process ) {
            super.messageReceived ( msg );
        } else if ( log.isDebugEnabled() ) {
            log.debug ( "Received a failure detector packet:" + msg );
        }
    }
    @Override
    public void memberAdded ( Member member ) {
        if ( membership == null ) {
            setupMembership();
        }
        boolean notify = false;
        synchronized ( membership ) {
            if ( removeSuspects.containsKey ( member ) ) {
                removeSuspects.remove ( member );
            } else if ( membership.getMember ( member ) == null ) {
                if ( memberAlive ( member ) ) {
                    membership.memberAlive ( member );
                    notify = true;
                } else {
                    addSuspects.put ( member, Long.valueOf ( System.currentTimeMillis() ) );
                }
            }
        }
        if ( notify ) {
            super.memberAdded ( member );
        }
    }
    @Override
    public void memberDisappeared ( Member member ) {
        if ( membership == null ) {
            setupMembership();
        }
        boolean shutdown = Arrays.equals ( member.getCommand(), Member.SHUTDOWN_PAYLOAD );
        if ( shutdown ) {
            synchronized ( membership ) {
                if ( !membership.contains ( member ) ) {
                    return;
                }
                membership.removeMember ( member );
                removeSuspects.remove ( member );
                if ( member instanceof StaticMember ) {
                    addSuspects.put ( member, Long.valueOf ( System.currentTimeMillis() ) );
                }
            }
            super.memberDisappeared ( member );
        } else {
            boolean notify = false;
            if ( log.isInfoEnabled() ) {
                log.info ( sm.getString ( "tcpFailureDetector.memberDisappeared.verify", member ) );
            }
            synchronized ( membership ) {
                if ( !membership.contains ( member ) ) {
                    if ( log.isInfoEnabled() ) {
                        log.info ( sm.getString ( "tcpFailureDetector.already.disappeared", member ) );
                    }
                    return;
                }
                if ( !memberAlive ( member ) ) {
                    membership.removeMember ( member );
                    removeSuspects.remove ( member );
                    if ( member instanceof StaticMember ) {
                        addSuspects.put ( member, Long.valueOf ( System.currentTimeMillis() ) );
                    }
                    notify = true;
                } else {
                    removeSuspects.put ( member, Long.valueOf ( System.currentTimeMillis() ) );
                }
            }
            if ( notify ) {
                if ( log.isInfoEnabled() ) {
                    log.info ( sm.getString ( "tcpFailureDetector.member.disappeared", member ) );
                }
                super.memberDisappeared ( member );
            } else {
                if ( log.isInfoEnabled() ) {
                    log.info ( sm.getString ( "tcpFailureDetector.still.alive", member ) );
                }
            }
        }
    }
    @Override
    public boolean hasMembers() {
        if ( membership == null ) {
            setupMembership();
        }
        return membership.hasMembers();
    }
    @Override
    public Member[] getMembers() {
        if ( membership == null ) {
            setupMembership();
        }
        return membership.getMembers();
    }
    @Override
    public Member getMember ( Member mbr ) {
        if ( membership == null ) {
            setupMembership();
        }
        return membership.getMember ( mbr );
    }
    @Override
    public Member getLocalMember ( boolean incAlive ) {
        return super.getLocalMember ( incAlive );
    }
    @Override
    public void heartbeat() {
        super.heartbeat();
        checkMembers ( false );
    }
    public void checkMembers ( boolean checkAll ) {
        try {
            if ( membership == null ) {
                setupMembership();
            }
            synchronized ( membership ) {
                if ( !checkAll ) {
                    performBasicCheck();
                } else {
                    performForcedCheck();
                }
            }
        } catch ( Exception x ) {
            log.warn ( sm.getString ( "tcpFailureDetector.heartbeat.failed" ), x );
        }
    }
    protected void performForcedCheck() {
        Member[] members = super.getMembers();
        for ( int i = 0; members != null && i < members.length; i++ ) {
            if ( memberAlive ( members[i] ) ) {
                if ( membership.memberAlive ( members[i] ) ) {
                    super.memberAdded ( members[i] );
                }
                addSuspects.remove ( members[i] );
            } else {
                if ( membership.getMember ( members[i] ) != null ) {
                    membership.removeMember ( members[i] );
                    removeSuspects.remove ( members[i] );
                    if ( members[i] instanceof StaticMember ) {
                        addSuspects.put ( members[i], Long.valueOf ( System.currentTimeMillis() ) );
                    }
                    super.memberDisappeared ( members[i] );
                }
            }
        }
    }
    protected void performBasicCheck() {
        Member[] members = super.getMembers();
        for ( int i = 0; members != null && i < members.length; i++ ) {
            if ( addSuspects.containsKey ( members[i] ) && membership.getMember ( members[i] ) == null ) {
                continue;
            }
            if ( membership.memberAlive ( members[i] ) ) {
                if ( memberAlive ( members[i] ) ) {
                    log.warn ( sm.getString ( "tcpFailureDetector.performBasicCheck.memberAdded", members[i] ) );
                    super.memberAdded ( members[i] );
                } else {
                    membership.removeMember ( members[i] );
                }
            }
        }
        Member[] keys = removeSuspects.keySet().toArray ( new Member[removeSuspects.size()] );
        for ( int i = 0; i < keys.length; i++ ) {
            Member m = keys[i];
            if ( membership.getMember ( m ) != null && ( !memberAlive ( m ) ) ) {
                membership.removeMember ( m );
                if ( m instanceof StaticMember ) {
                    addSuspects.put ( m, Long.valueOf ( System.currentTimeMillis() ) );
                }
                super.memberDisappeared ( m );
                removeSuspects.remove ( m );
                if ( log.isInfoEnabled() ) {
                    log.info ( sm.getString ( "tcpFailureDetector.suspectMember.dead", m ) );
                }
            } else {
                if ( removeSuspectsTimeout > 0 ) {
                    long timeNow = System.currentTimeMillis();
                    int timeIdle = ( int ) ( ( timeNow - removeSuspects.get ( m ).longValue() ) / 1000L );
                    if ( timeIdle > removeSuspectsTimeout ) {
                        removeSuspects.remove ( m );
                    }
                }
            }
        }
        keys = addSuspects.keySet().toArray ( new Member[addSuspects.size()] );
        for ( int i = 0; i < keys.length; i++ ) {
            Member m = keys[i];
            if ( membership.getMember ( m ) == null && ( memberAlive ( m ) ) ) {
                membership.memberAlive ( m );
                super.memberAdded ( m );
                addSuspects.remove ( m );
                if ( log.isInfoEnabled() ) {
                    log.info ( sm.getString ( "tcpFailureDetector.suspectMember.alive", m ) );
                }
            }
        }
    }
    protected synchronized void setupMembership() {
        if ( membership == null ) {
            membership = new Membership ( super.getLocalMember ( true ) );
        }
    }
    protected boolean memberAlive ( Member mbr ) {
        return memberAlive ( mbr, TCP_FAIL_DETECT, performSendTest, performReadTest, readTestTimeout, connectTimeout, getOptionFlag() );
    }
    protected boolean memberAlive ( Member mbr, byte[] msgData,
                                    boolean sendTest, boolean readTest,
                                    long readTimeout, long conTimeout,
                                    int optionFlag ) {
        if ( Arrays.equals ( mbr.getCommand(), Member.SHUTDOWN_PAYLOAD ) ) {
            return false;
        }
        try ( Socket socket = new Socket() ) {
            InetAddress ia = InetAddress.getByAddress ( mbr.getHost() );
            InetSocketAddress addr = new InetSocketAddress ( ia, mbr.getPort() );
            socket.setSoTimeout ( ( int ) readTimeout );
            socket.connect ( addr, ( int ) conTimeout );
            if ( sendTest ) {
                ChannelData data = new ChannelData ( true );
                data.setAddress ( getLocalMember ( false ) );
                data.setMessage ( new XByteBuffer ( msgData, false ) );
                data.setTimestamp ( System.currentTimeMillis() );
                int options = optionFlag | Channel.SEND_OPTIONS_BYTE_MESSAGE;
                if ( readTest ) {
                    options = ( options | Channel.SEND_OPTIONS_USE_ACK );
                } else {
                    options = ( options & ( ~Channel.SEND_OPTIONS_USE_ACK ) );
                }
                data.setOptions ( options );
                byte[] message = XByteBuffer.createDataPackage ( data );
                socket.getOutputStream().write ( message );
                if ( readTest ) {
                    int length = socket.getInputStream().read ( message );
                    return length > 0;
                }
            }
            return true;
        } catch ( SocketTimeoutException sx ) {
        } catch ( ConnectException cx ) {
        } catch ( Exception x ) {
            log.error ( sm.getString ( "tcpFailureDetector.failureDetection.failed" ), x );
        }
        return false;
    }
    public long getReadTestTimeout() {
        return readTestTimeout;
    }
    public boolean getPerformSendTest() {
        return performSendTest;
    }
    public boolean getPerformReadTest() {
        return performReadTest;
    }
    public long getConnectTimeout() {
        return connectTimeout;
    }
    public int getRemoveSuspectsTimeout() {
        return removeSuspectsTimeout;
    }
    public void setPerformReadTest ( boolean performReadTest ) {
        this.performReadTest = performReadTest;
    }
    public void setPerformSendTest ( boolean performSendTest ) {
        this.performSendTest = performSendTest;
    }
    public void setReadTestTimeout ( long readTestTimeout ) {
        this.readTestTimeout = readTestTimeout;
    }
    public void setConnectTimeout ( long connectTimeout ) {
        this.connectTimeout = connectTimeout;
    }
    public void setRemoveSuspectsTimeout ( int removeSuspectsTimeout ) {
        this.removeSuspectsTimeout = removeSuspectsTimeout;
    }
}