package org.apache.catalina.tribes.group.interceptors;
import org.apache.catalina.tribes.membership.MemberImpl;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.juli.logging.LogFactory;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.Socket;
import org.apache.catalina.tribes.io.ChannelData;
import org.apache.catalina.tribes.group.InterceptorPayload;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.util.Arrays;
import org.apache.catalina.tribes.Member;
import java.util.Comparator;
import org.apache.catalina.tribes.group.AbsoluteOrder;
import org.apache.catalina.tribes.util.UUIDGenerator;
import org.apache.catalina.tribes.ChannelInterceptor;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.catalina.tribes.UniqueId;
import org.apache.catalina.tribes.membership.Membership;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.tribes.group.ChannelInterceptorBase;
public class NonBlockingCoordinator extends ChannelInterceptorBase {
    private static final Log log;
    protected static final StringManager sm;
    protected static final byte[] COORD_HEADER;
    protected static final byte[] COORD_REQUEST;
    protected static final byte[] COORD_CONF;
    protected static final byte[] COORD_ALIVE;
    protected final long waitForCoordMsgTimeout = 15000L;
    protected volatile Membership view;
    protected UniqueId viewId;
    protected Membership membership;
    protected UniqueId suggestedviewId;
    protected volatile Membership suggestedView;
    protected volatile boolean started;
    protected final int startsvc = 65535;
    protected final Object electionMutex;
    protected final AtomicBoolean coordMsgReceived;
    public NonBlockingCoordinator() {
        this.view = null;
        this.membership = null;
        this.started = false;
        this.electionMutex = new Object();
        this.coordMsgReceived = new AtomicBoolean ( false );
    }
    public void startElection ( final boolean force ) throws ChannelException {
        synchronized ( this.electionMutex ) {
            final Member local = this.getLocalMember ( false );
            final Member[] others = this.membership.getMembers();
            this.fireInterceptorEvent ( new CoordinationEvent ( 4, this, "Election initated" ) );
            if ( others.length == 0 ) {
                this.viewId = new UniqueId ( UUIDGenerator.randomUUID ( false ) );
                this.view = new Membership ( local, AbsoluteOrder.comp, true );
                this.handleViewConf ( this.createElectionMsg ( local, others, local ), this.view );
                return;
            }
            if ( this.suggestedviewId != null ) {
                if ( this.view != null && Arrays.diff ( this.view, this.suggestedView, local ).length == 0 && Arrays.diff ( this.suggestedView, this.view, local ).length == 0 ) {
                    this.suggestedviewId = null;
                    this.suggestedView = null;
                    this.fireInterceptorEvent ( new CoordinationEvent ( 13, this, "Election abandoned, running election matches view" ) );
                } else {
                    this.fireInterceptorEvent ( new CoordinationEvent ( 13, this, "Election abandoned, election running" ) );
                }
                return;
            }
            if ( this.view != null && Arrays.diff ( this.view, this.membership, local ).length == 0 && Arrays.diff ( this.membership, this.view, local ).length == 0 ) {
                this.fireInterceptorEvent ( new CoordinationEvent ( 13, this, "Election abandoned, view matches membership" ) );
                return;
            }
            final int prio = AbsoluteOrder.comp.compare ( local, others[0] );
            final Member leader = ( prio < 0 ) ? local : others[0];
            if ( local.equals ( leader ) || force ) {
                final CoordinationMessage msg = this.createElectionMsg ( local, others, leader );
                this.suggestedviewId = msg.getId();
                Arrays.fill ( this.suggestedView = new Membership ( local, AbsoluteOrder.comp, true ), msg.getMembers() );
                this.fireInterceptorEvent ( new CoordinationEvent ( 5, this, "Election, sending request" ) );
                this.sendElectionMsg ( local, others[0], msg );
            } else {
                try {
                    this.coordMsgReceived.set ( false );
                    this.fireInterceptorEvent ( new CoordinationEvent ( 9, this, "Election, waiting for request" ) );
                    this.electionMutex.wait ( 15000L );
                } catch ( InterruptedException x ) {
                    Thread.currentThread().interrupt();
                }
                String msg2;
                if ( this.suggestedviewId == null && !this.coordMsgReceived.get() ) {
                    if ( Thread.interrupted() ) {
                        msg2 = "Election abandoned, waiting interrupted.";
                    } else {
                        msg2 = "Election abandoned, waiting timed out.";
                    }
                } else {
                    msg2 = "Election abandoned, received a message";
                }
                this.fireInterceptorEvent ( new CoordinationEvent ( 13, this, msg2 ) );
            }
        }
    }
    private CoordinationMessage createElectionMsg ( final Member local, final Member[] others, final Member leader ) {
        final Membership m = new Membership ( local, AbsoluteOrder.comp, true );
        Arrays.fill ( m, others );
        final Member[] mbrs = m.getMembers();
        m.reset();
        final CoordinationMessage msg = new CoordinationMessage ( leader, local, mbrs, new UniqueId ( UUIDGenerator.randomUUID ( true ) ), NonBlockingCoordinator.COORD_REQUEST );
        return msg;
    }
    protected void sendElectionMsg ( final Member local, final Member next, final CoordinationMessage msg ) throws ChannelException {
        this.fireInterceptorEvent ( new CoordinationEvent ( 10, this, "Sending election message to(" + next.getName() + ")" ) );
        super.sendMessage ( new Member[] { next }, this.createData ( msg, local ), null );
    }
    protected void sendElectionMsgToNextInline ( final Member local, final CoordinationMessage msg ) throws ChannelException {
        int current;
        final int next = current = Arrays.nextIndex ( local, msg.getMembers() );
        msg.leader = msg.getMembers() [0];
        boolean sent = false;
        while ( !sent && current >= 0 ) {
            try {
                this.sendElectionMsg ( local, msg.getMembers() [current], msg );
                sent = true;
            } catch ( ChannelException x ) {
                NonBlockingCoordinator.log.warn ( NonBlockingCoordinator.sm.getString ( "nonBlockingCoordinator.electionMessage.sendfailed", msg.getMembers() [current] ) );
                current = Arrays.nextIndex ( msg.getMembers() [current], msg.getMembers() );
                if ( current == next ) {
                    throw x;
                }
                continue;
            }
        }
    }
    public ChannelData createData ( final CoordinationMessage msg, final Member local ) {
        msg.write();
        final ChannelData data = new ChannelData ( true );
        data.setAddress ( local );
        data.setMessage ( msg.getBuffer() );
        data.setOptions ( 2 );
        data.setTimestamp ( System.currentTimeMillis() );
        return data;
    }
    protected boolean alive ( final Member mbr ) {
        return this.memberAlive ( mbr, 15000L );
    }
    protected boolean memberAlive ( final Member mbr, final long conTimeout ) {
        if ( Arrays.equals ( mbr.getCommand(), Member.SHUTDOWN_PAYLOAD ) ) {
            return false;
        }
        try ( final Socket socket = new Socket() ) {
            final InetAddress ia = InetAddress.getByAddress ( mbr.getHost() );
            final InetSocketAddress addr = new InetSocketAddress ( ia, mbr.getPort() );
            socket.connect ( addr, ( int ) conTimeout );
            return true;
        } catch ( SocketTimeoutException ex ) {}
        catch ( ConnectException ex2 ) {}
        catch ( Exception x ) {
            NonBlockingCoordinator.log.error ( NonBlockingCoordinator.sm.getString ( "nonBlockingCoordinator.memberAlive.failed" ), x );
        }
        return false;
    }
    protected Membership mergeOnArrive ( final CoordinationMessage msg ) {
        this.fireInterceptorEvent ( new CoordinationEvent ( 7, this, "Pre merge" ) );
        final Member local = this.getLocalMember ( false );
        final Membership merged = new Membership ( local, AbsoluteOrder.comp, true );
        Arrays.fill ( merged, msg.getMembers() );
        Arrays.fill ( merged, this.getMembers() );
        final Member[] diff = Arrays.diff ( merged, this.membership, local );
        for ( int i = 0; i < diff.length; ++i ) {
            if ( !this.alive ( diff[i] ) ) {
                merged.removeMember ( diff[i] );
            } else {
                this.memberAdded ( diff[i], false );
            }
        }
        this.fireInterceptorEvent ( new CoordinationEvent ( 8, this, "Post merge" ) );
        return merged;
    }
    protected void processCoordMessage ( final CoordinationMessage msg ) throws ChannelException {
        if ( !this.coordMsgReceived.get() ) {
            this.coordMsgReceived.set ( true );
            synchronized ( this.electionMutex ) {
                this.electionMutex.notifyAll();
            }
        }
        final Membership merged = this.mergeOnArrive ( msg );
        if ( this.isViewConf ( msg ) ) {
            this.handleViewConf ( msg, merged );
        } else {
            this.handleToken ( msg, merged );
        }
    }
    protected void handleToken ( final CoordinationMessage msg, final Membership merged ) throws ChannelException {
        final Member local = this.getLocalMember ( false );
        if ( local.equals ( msg.getSource() ) ) {
            this.handleMyToken ( local, msg, merged );
        } else {
            this.handleOtherToken ( local, msg, merged );
        }
    }
    protected void handleMyToken ( final Member local, final CoordinationMessage msg, final Membership merged ) throws ChannelException {
        if ( local.equals ( msg.getLeader() ) ) {
            if ( Arrays.sameMembers ( msg.getMembers(), merged.getMembers() ) ) {
                msg.type = NonBlockingCoordinator.COORD_CONF;
                super.sendMessage ( Arrays.remove ( msg.getMembers(), local ), this.createData ( msg, local ), null );
                this.handleViewConf ( msg, merged );
            } else {
                this.suggestedView = new Membership ( local, AbsoluteOrder.comp, true );
                this.suggestedviewId = msg.getId();
                Arrays.fill ( this.suggestedView, merged.getMembers() );
                msg.view = merged.getMembers();
                this.sendElectionMsgToNextInline ( local, msg );
            }
        } else {
            this.suggestedView = null;
            this.suggestedviewId = null;
            msg.view = merged.getMembers();
            this.sendElectionMsgToNextInline ( local, msg );
        }
    }
    protected void handleOtherToken ( final Member local, final CoordinationMessage msg, final Membership merged ) throws ChannelException {
        if ( !local.equals ( msg.getLeader() ) ) {
            msg.view = merged.getMembers();
            this.sendElectionMsgToNextInline ( local, msg );
        }
    }
    protected void handleViewConf ( final CoordinationMessage msg, final Membership merged ) throws ChannelException {
        if ( this.viewId != null && msg.getId().equals ( this.viewId ) ) {
            return;
        }
        Arrays.fill ( this.view = new Membership ( this.getLocalMember ( false ), AbsoluteOrder.comp, true ), msg.getMembers() );
        this.viewId = msg.getId();
        if ( this.viewId.equals ( this.suggestedviewId ) ) {
            this.suggestedView = null;
            this.suggestedviewId = null;
        }
        if ( this.suggestedView != null && AbsoluteOrder.comp.compare ( this.suggestedView.getMembers() [0], merged.getMembers() [0] ) < 0 ) {
            this.suggestedView = null;
            this.suggestedviewId = null;
        }
        this.fireInterceptorEvent ( new CoordinationEvent ( 12, this, "Accepted View" ) );
        if ( this.suggestedviewId == null && this.hasHigherPriority ( merged.getMembers(), this.membership.getMembers() ) ) {
            this.startElection ( false );
        }
    }
    protected boolean isViewConf ( final CoordinationMessage msg ) {
        return Arrays.contains ( msg.getType(), 0, NonBlockingCoordinator.COORD_CONF, 0, NonBlockingCoordinator.COORD_CONF.length );
    }
    protected boolean hasHigherPriority ( final Member[] complete, final Member[] local ) {
        if ( local == null || local.length == 0 ) {
            return false;
        }
        if ( complete == null || complete.length == 0 ) {
            return true;
        }
        AbsoluteOrder.absoluteOrder ( complete );
        AbsoluteOrder.absoluteOrder ( local );
        return AbsoluteOrder.comp.compare ( complete[0], local[0] ) > 0;
    }
    public Member getCoordinator() {
        return ( this.view != null && this.view.hasMembers() ) ? this.view.getMembers() [0] : null;
    }
    public Member[] getView() {
        return ( this.view != null && this.view.hasMembers() ) ? this.view.getMembers() : new Member[0];
    }
    public UniqueId getViewId() {
        return this.viewId;
    }
    protected void halt() {
    }
    protected void release() {
    }
    protected void waitForRelease() {
    }
    @Override
    public void start ( final int svc ) throws ChannelException {
        if ( this.membership == null ) {
            this.setupMembership();
        }
        if ( this.started ) {
            return;
        }
        this.fireInterceptorEvent ( new CoordinationEvent ( 1, this, "Before start" ) );
        super.start ( 65535 );
        this.started = true;
        if ( this.view == null ) {
            this.view = new Membership ( super.getLocalMember ( true ), AbsoluteOrder.comp, true );
        }
        this.fireInterceptorEvent ( new CoordinationEvent ( 1, this, "After start" ) );
        this.startElection ( false );
    }
    @Override
    public void stop ( final int svc ) throws ChannelException {
        try {
            this.halt();
            synchronized ( this.electionMutex ) {
                if ( !this.started ) {
                    return;
                }
                this.started = false;
                this.fireInterceptorEvent ( new CoordinationEvent ( 11, this, "Before stop" ) );
                super.stop ( 65535 );
                this.view = null;
                this.viewId = null;
                this.suggestedView = null;
                this.suggestedviewId = null;
                this.membership.reset();
                this.fireInterceptorEvent ( new CoordinationEvent ( 11, this, "After stop" ) );
            }
        } finally {
            this.release();
        }
    }
    @Override
    public void sendMessage ( final Member[] destination, final ChannelMessage msg, final InterceptorPayload payload ) throws ChannelException {
        this.waitForRelease();
        super.sendMessage ( destination, msg, payload );
    }
    @Override
    public void messageReceived ( final ChannelMessage msg ) {
        if ( Arrays.contains ( msg.getMessage().getBytesDirect(), 0, NonBlockingCoordinator.COORD_ALIVE, 0, NonBlockingCoordinator.COORD_ALIVE.length ) ) {
            this.fireInterceptorEvent ( new CoordinationEvent ( 6, this, "Alive Message" ) );
        } else if ( Arrays.contains ( msg.getMessage().getBytesDirect(), 0, NonBlockingCoordinator.COORD_HEADER, 0, NonBlockingCoordinator.COORD_HEADER.length ) ) {
            try {
                final CoordinationMessage cmsg = new CoordinationMessage ( msg.getMessage() );
                final Member[] cmbr = cmsg.getMembers();
                this.fireInterceptorEvent ( new CoordinationEvent ( 6, this, "Coord Msg Arrived(" + Arrays.toNameString ( cmbr ) + ")" ) );
                this.processCoordMessage ( cmsg );
            } catch ( ChannelException x ) {
                NonBlockingCoordinator.log.error ( NonBlockingCoordinator.sm.getString ( "nonBlockingCoordinator.processCoordinationMessage.failed" ), x );
            }
        } else {
            super.messageReceived ( msg );
        }
    }
    @Override
    public void memberAdded ( final Member member ) {
        this.memberAdded ( member, true );
    }
    public void memberAdded ( final Member member, final boolean elect ) {
        if ( this.membership == null ) {
            this.setupMembership();
        }
        if ( this.membership.memberAlive ( member ) ) {
            super.memberAdded ( member );
        }
        try {
            this.fireInterceptorEvent ( new CoordinationEvent ( 2, this, "Member add(" + member.getName() + ")" ) );
            if ( this.started && elect ) {
                this.startElection ( false );
            }
        } catch ( ChannelException x ) {
            NonBlockingCoordinator.log.error ( NonBlockingCoordinator.sm.getString ( "nonBlockingCoordinator.memberAdded.failed" ), x );
        }
    }
    @Override
    public void memberDisappeared ( final Member member ) {
        this.membership.removeMember ( member );
        super.memberDisappeared ( member );
        try {
            this.fireInterceptorEvent ( new CoordinationEvent ( 3, this, "Member remove(" + member.getName() + ")" ) );
            if ( this.started && ( this.isCoordinator() || this.isHighest() ) ) {
                this.startElection ( true );
            }
        } catch ( ChannelException x ) {
            NonBlockingCoordinator.log.error ( NonBlockingCoordinator.sm.getString ( "nonBlockingCoordinator.memberDisappeared.failed" ), x );
        }
    }
    public boolean isHighest() {
        final Member local = this.getLocalMember ( false );
        return this.membership.getMembers().length == 0 || AbsoluteOrder.comp.compare ( local, this.membership.getMembers() [0] ) <= 0;
    }
    public boolean isCoordinator() {
        final Member coord = this.getCoordinator();
        return coord != null && this.getLocalMember ( false ).equals ( coord );
    }
    @Override
    public void heartbeat() {
        try {
            final Member local = this.getLocalMember ( false );
            if ( this.view != null && ( Arrays.diff ( this.view, this.membership, local ).length != 0 || Arrays.diff ( this.membership, this.view, local ).length != 0 ) && this.isHighest() ) {
                this.fireInterceptorEvent ( new CoordinationEvent ( 4, this, NonBlockingCoordinator.sm.getString ( "nonBlockingCoordinator.heartbeat.inconsistency" ) ) );
                this.startElection ( true );
            }
        } catch ( Exception x ) {
            NonBlockingCoordinator.log.error ( NonBlockingCoordinator.sm.getString ( "nonBlockingCoordinator.heartbeat.failed" ), x );
        } finally {
            super.heartbeat();
        }
    }
    @Override
    public boolean hasMembers() {
        return this.membership.hasMembers();
    }
    @Override
    public Member[] getMembers() {
        return this.membership.getMembers();
    }
    @Override
    public Member getMember ( final Member mbr ) {
        return this.membership.getMember ( mbr );
    }
    @Override
    public Member getLocalMember ( final boolean incAlive ) {
        final Member local = super.getLocalMember ( incAlive );
        if ( this.view == null && local != null ) {
            this.setupMembership();
        }
        return local;
    }
    protected synchronized void setupMembership() {
        if ( this.membership == null ) {
            this.membership = new Membership ( super.getLocalMember ( true ), AbsoluteOrder.comp, false );
        }
    }
    @Override
    public void fireInterceptorEvent ( final ChannelInterceptor.InterceptorEvent event ) {
        if ( event instanceof CoordinationEvent && ( ( CoordinationEvent ) event ).type == 12 ) {
            NonBlockingCoordinator.log.info ( event );
        }
    }
    static {
        log = LogFactory.getLog ( NonBlockingCoordinator.class );
        sm = StringManager.getManager ( NonBlockingCoordinator.class );
        COORD_HEADER = new byte[] { -86, 38, -34, -29, -98, 90, 65, 63, -81, -122, -6, -110, 99, -54, 13, 63 };
        COORD_REQUEST = new byte[] { 104, -95, -92, -42, 114, -36, 71, -19, -79, 20, 122, 101, -1, -48, -49, 30 };
        COORD_CONF = new byte[] { 67, 88, 107, -86, 69, 23, 76, -70, -91, -23, -87, -25, -125, 86, 75, 20 };
        COORD_ALIVE = new byte[] { 79, -121, -25, -15, -59, 5, 64, 94, -77, 113, -119, -88, 52, 114, -56, -46, -18, 102, 10, 34, -127, -9, 71, 115, -70, 72, -101, 88, 72, -124, 127, 111, 74, 76, -116, 50, 111, 103, 65, 3, -77, 51, -35, 0, 119, 117, 9, -26, 119, 50, -75, -105, -102, 36, 79, 37, -68, -84, -123, 15, -22, -109, 106, -55 };
    }
    public static class CoordinationMessage {
        protected final XByteBuffer buf;
        protected Member leader;
        protected Member source;
        protected Member[] view;
        protected UniqueId id;
        protected byte[] type;
        public CoordinationMessage ( final XByteBuffer buf ) {
            this.buf = buf;
            this.parse();
        }
        public CoordinationMessage ( final Member leader, final Member source, final Member[] view, final UniqueId id, final byte[] type ) {
            this.buf = new XByteBuffer ( 4096, false );
            this.leader = leader;
            this.source = source;
            this.view = view;
            this.id = id;
            this.type = type;
            this.write();
        }
        public byte[] getHeader() {
            return NonBlockingCoordinator.COORD_HEADER;
        }
        public Member getLeader() {
            if ( this.leader == null ) {
                this.parse();
            }
            return this.leader;
        }
        public Member getSource() {
            if ( this.source == null ) {
                this.parse();
            }
            return this.source;
        }
        public UniqueId getId() {
            if ( this.id == null ) {
                this.parse();
            }
            return this.id;
        }
        public Member[] getMembers() {
            if ( this.view == null ) {
                this.parse();
            }
            return this.view;
        }
        public byte[] getType() {
            if ( this.type == null ) {
                this.parse();
            }
            return this.type;
        }
        public XByteBuffer getBuffer() {
            return this.buf;
        }
        public void parse() {
            int offset = 16;
            final int ldrLen = XByteBuffer.toInt ( this.buf.getBytesDirect(), offset );
            offset += 4;
            final byte[] ldr = new byte[ldrLen];
            System.arraycopy ( this.buf.getBytesDirect(), offset, ldr, 0, ldrLen );
            this.leader = MemberImpl.getMember ( ldr );
            offset += ldrLen;
            final int srcLen = XByteBuffer.toInt ( this.buf.getBytesDirect(), offset );
            offset += 4;
            final byte[] src = new byte[srcLen];
            System.arraycopy ( this.buf.getBytesDirect(), offset, src, 0, srcLen );
            this.source = MemberImpl.getMember ( src );
            offset += srcLen;
            final int mbrCount = XByteBuffer.toInt ( this.buf.getBytesDirect(), offset );
            offset += 4;
            this.view = new Member[mbrCount];
            for ( int i = 0; i < this.view.length; ++i ) {
                final int mbrLen = XByteBuffer.toInt ( this.buf.getBytesDirect(), offset );
                offset += 4;
                final byte[] mbr = new byte[mbrLen];
                System.arraycopy ( this.buf.getBytesDirect(), offset, mbr, 0, mbrLen );
                this.view[i] = MemberImpl.getMember ( mbr );
                offset += mbrLen;
            }
            this.id = new UniqueId ( this.buf.getBytesDirect(), offset, 16 );
            offset += 16;
            this.type = new byte[16];
            System.arraycopy ( this.buf.getBytesDirect(), offset, this.type, 0, this.type.length );
            offset += 16;
        }
        public void write() {
            this.buf.reset();
            this.buf.append ( NonBlockingCoordinator.COORD_HEADER, 0, NonBlockingCoordinator.COORD_HEADER.length );
            byte[] ldr = this.leader.getData ( false, false );
            this.buf.append ( ldr.length );
            this.buf.append ( ldr, 0, ldr.length );
            ldr = null;
            byte[] src = this.source.getData ( false, false );
            this.buf.append ( src.length );
            this.buf.append ( src, 0, src.length );
            src = null;
            this.buf.append ( this.view.length );
            for ( int i = 0; i < this.view.length; ++i ) {
                final byte[] mbr = this.view[i].getData ( false, false );
                this.buf.append ( mbr.length );
                this.buf.append ( mbr, 0, mbr.length );
            }
            this.buf.append ( this.id.getBytes(), 0, this.id.getBytes().length );
            this.buf.append ( this.type, 0, this.type.length );
        }
    }
    public static class CoordinationEvent implements ChannelInterceptor.InterceptorEvent {
        public static final int EVT_START = 1;
        public static final int EVT_MBR_ADD = 2;
        public static final int EVT_MBR_DEL = 3;
        public static final int EVT_START_ELECT = 4;
        public static final int EVT_PROCESS_ELECT = 5;
        public static final int EVT_MSG_ARRIVE = 6;
        public static final int EVT_PRE_MERGE = 7;
        public static final int EVT_POST_MERGE = 8;
        public static final int EVT_WAIT_FOR_MSG = 9;
        public static final int EVT_SEND_MSG = 10;
        public static final int EVT_STOP = 11;
        public static final int EVT_CONF_RX = 12;
        public static final int EVT_ELECT_ABANDONED = 13;
        final int type;
        final ChannelInterceptor interceptor;
        final Member coord;
        final Member[] mbrs;
        final String info;
        final Membership view;
        final Membership suggestedView;
        public CoordinationEvent ( final int type, final ChannelInterceptor interceptor, final String info ) {
            this.type = type;
            this.interceptor = interceptor;
            this.coord = ( ( NonBlockingCoordinator ) interceptor ).getCoordinator();
            this.mbrs = ( ( NonBlockingCoordinator ) interceptor ).membership.getMembers();
            this.info = info;
            this.view = ( ( NonBlockingCoordinator ) interceptor ).view;
            this.suggestedView = ( ( NonBlockingCoordinator ) interceptor ).suggestedView;
        }
        @Override
        public int getEventType() {
            return this.type;
        }
        @Override
        public String getEventTypeDesc() {
            switch ( this.type ) {
            case 1: {
                return "EVT_START:" + this.info;
            }
            case 2: {
                return "EVT_MBR_ADD:" + this.info;
            }
            case 3: {
                return "EVT_MBR_DEL:" + this.info;
            }
            case 4: {
                return "EVT_START_ELECT:" + this.info;
            }
            case 5: {
                return "EVT_PROCESS_ELECT:" + this.info;
            }
            case 6: {
                return "EVT_MSG_ARRIVE:" + this.info;
            }
            case 7: {
                return "EVT_PRE_MERGE:" + this.info;
            }
            case 8: {
                return "EVT_POST_MERGE:" + this.info;
            }
            case 9: {
                return "EVT_WAIT_FOR_MSG:" + this.info;
            }
            case 10: {
                return "EVT_SEND_MSG:" + this.info;
            }
            case 11: {
                return "EVT_STOP:" + this.info;
            }
            case 12: {
                return "EVT_CONF_RX:" + this.info;
            }
            case 13: {
                return "EVT_ELECT_ABANDONED:" + this.info;
            }
            default: {
                return "Unknown";
            }
            }
        }
        @Override
        public ChannelInterceptor getInterceptor() {
            return this.interceptor;
        }
        @Override
        public String toString() {
            final StringBuilder buf = new StringBuilder ( "CoordinationEvent[type=" );
            buf.append ( this.type ).append ( "\n\tLocal:" );
            final Member local = this.interceptor.getLocalMember ( false );
            buf.append ( ( local != null ) ? local.getName() : "" ).append ( "\n\tCoord:" );
            buf.append ( ( this.coord != null ) ? this.coord.getName() : "" ).append ( "\n\tView:" );
            buf.append ( Arrays.toNameString ( ( Member[] ) ( ( this.view != null ) ? this.view.getMembers() : null ) ) ).append ( "\n\tSuggested View:" );
            buf.append ( Arrays.toNameString ( ( Member[] ) ( ( this.suggestedView != null ) ? this.suggestedView.getMembers() : null ) ) ).append ( "\n\tMembers:" );
            buf.append ( Arrays.toNameString ( this.mbrs ) ).append ( "\n\tInfo:" );
            buf.append ( this.info ).append ( "]" );
            return buf.toString();
        }
    }
}
