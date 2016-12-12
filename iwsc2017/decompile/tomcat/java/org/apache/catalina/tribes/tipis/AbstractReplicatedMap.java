package org.apache.catalina.tribes.tipis;
import org.apache.catalina.tribes.io.XByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.catalina.tribes.group.Response;
import java.io.IOException;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.tribes.Member;
import java.util.HashMap;
import org.apache.catalina.tribes.group.RpcChannel;
import org.apache.catalina.tribes.Channel;
import java.util.concurrent.ConcurrentMap;
import org.apache.juli.logging.Log;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.catalina.tribes.Heartbeat;
import org.apache.catalina.tribes.MembershipListener;
import org.apache.catalina.tribes.ChannelListener;
import org.apache.catalina.tribes.group.RpcCallback;
import java.io.Serializable;
import java.util.Map;
public abstract class AbstractReplicatedMap<K, V> implements Map<K, V>, Serializable, RpcCallback, ChannelListener, MembershipListener, Heartbeat {
    private static final long serialVersionUID = 1L;
    protected static final StringManager sm;
    private final Log log;
    public static final int DEFAULT_INITIAL_CAPACITY = 16;
    public static final float DEFAULT_LOAD_FACTOR = 0.75f;
    protected final ConcurrentMap<K, MapEntry<K, V>> innerMap;
    protected transient long rpcTimeout;
    protected transient Channel channel;
    protected transient RpcChannel rpcChannel;
    protected transient byte[] mapContextName;
    protected transient boolean stateTransferred;
    protected final transient Object stateMutex;
    protected final transient HashMap<Member, Long> mapMembers;
    protected transient int channelSendOptions;
    protected transient MapOwner mapOwner;
    protected transient ClassLoader[] externalLoaders;
    protected transient int currentNode;
    protected transient long accessTimeout;
    protected transient String mapname;
    private transient volatile State state;
    protected abstract int getStateMessageType();
    protected abstract int getReplicateMessageType();
    public AbstractReplicatedMap ( final MapOwner owner, final Channel channel, final long timeout, final String mapContextName, final int initialCapacity, final float loadFactor, final int channelSendOptions, final ClassLoader[] cls, final boolean terminate ) {
        this.log = LogFactory.getLog ( AbstractReplicatedMap.class );
        this.rpcTimeout = 5000L;
        this.stateTransferred = false;
        this.stateMutex = new Object();
        this.mapMembers = new HashMap<Member, Long>();
        this.channelSendOptions = 2;
        this.currentNode = 0;
        this.accessTimeout = 5000L;
        this.mapname = "";
        this.state = State.NEW;
        this.innerMap = new ConcurrentHashMap<K, MapEntry<K, V>> ( initialCapacity, loadFactor, 15 );
        this.init ( owner, channel, mapContextName, timeout, channelSendOptions, cls, terminate );
    }
    protected Member[] wrap ( final Member m ) {
        if ( m == null ) {
            return new Member[0];
        }
        return new Member[] { m };
    }
    protected void init ( final MapOwner owner, final Channel channel, final String mapContextName, final long timeout, final int channelSendOptions, final ClassLoader[] cls, final boolean terminate ) {
        final long start = System.currentTimeMillis();
        if ( this.log.isInfoEnabled() ) {
            this.log.info ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.init.start", mapContextName ) );
        }
        this.mapOwner = owner;
        this.externalLoaders = cls;
        this.channelSendOptions = channelSendOptions;
        this.channel = channel;
        this.rpcTimeout = timeout;
        this.mapname = mapContextName;
        this.mapContextName = mapContextName.getBytes ( StandardCharsets.ISO_8859_1 );
        if ( this.log.isTraceEnabled() ) {
            this.log.trace ( "Created Lazy Map with name:" + mapContextName + ", bytes:" + Arrays.toString ( this.mapContextName ) );
        }
        this.rpcChannel = new RpcChannel ( this.mapContextName, channel, this );
        this.channel.addChannelListener ( this );
        this.channel.addMembershipListener ( this );
        try {
            this.broadcast ( 8, true );
            this.transferState();
            this.broadcast ( 6, true );
        } catch ( ChannelException x ) {
            this.log.warn ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.unableSend.startMessage" ) );
            if ( terminate ) {
                this.breakdown();
                throw new RuntimeException ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.unableStart" ), x );
            }
        }
        this.state = State.INITIALIZED;
        final long complete = System.currentTimeMillis() - start;
        if ( this.log.isInfoEnabled() ) {
            this.log.info ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.init.completed", mapContextName, Long.toString ( complete ) ) );
        }
    }
    protected void ping ( final long timeout ) throws ChannelException {
        final MapMessage msg = new MapMessage ( this.mapContextName, 13, false, null, null, null, this.channel.getLocalMember ( false ), null );
        if ( this.channel.getMembers().length > 0 ) {
            try {
                final Response[] resp = this.rpcChannel.send ( this.channel.getMembers(), msg, 3, this.channelSendOptions, ( int ) this.accessTimeout );
                for ( int i = 0; i < resp.length; ++i ) {
                    final MapMessage mapMsg = ( MapMessage ) resp[i].getMessage();
                    try {
                        mapMsg.deserialize ( this.getExternalLoaders() );
                        final State state = ( State ) mapMsg.getValue();
                        if ( state.isAvailable() ) {
                            this.memberAlive ( resp[i].getSource() );
                        } else if ( this.log.isInfoEnabled() ) {
                            this.log.info ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.mapMember.unavailable", resp[i].getSource() ) );
                        }
                    } catch ( ClassNotFoundException | IOException e ) {
                        this.log.error ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.unable.deserialize.MapMessage" ), e );
                    }
                }
            } catch ( ChannelException ce ) {
                final ChannelException.FaultyMember[] faultyMembers2;
                final ChannelException.FaultyMember[] faultyMembers = faultyMembers2 = ce.getFaultyMembers();
                for ( final ChannelException.FaultyMember faultyMember : faultyMembers2 ) {
                    this.memberDisappeared ( faultyMember.getMember() );
                }
                throw ce;
            }
        }
        synchronized ( this.mapMembers ) {
            final Member[] members = this.mapMembers.keySet().toArray ( new Member[this.mapMembers.size()] );
            final long now = System.currentTimeMillis();
            for ( final Member member : members ) {
                final long access = this.mapMembers.get ( member );
                if ( now - access > timeout ) {
                    this.log.warn ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.ping.timeout", member, this.mapname ) );
                    this.memberDisappeared ( member );
                }
            }
        }
    }
    protected void memberAlive ( final Member member ) {
        this.mapMemberAdded ( member );
        synchronized ( this.mapMembers ) {
            this.mapMembers.put ( member, System.currentTimeMillis() );
        }
    }
    protected void broadcast ( final int msgtype, final boolean rpc ) throws ChannelException {
        final Member[] members = this.channel.getMembers();
        if ( members.length == 0 ) {
            return;
        }
        final MapMessage msg = new MapMessage ( this.mapContextName, msgtype, false, null, null, null, this.channel.getLocalMember ( false ), null );
        if ( rpc ) {
            final Response[] resp = this.rpcChannel.send ( members, msg, 1, this.channelSendOptions, this.rpcTimeout );
            if ( resp.length > 0 ) {
                for ( int i = 0; i < resp.length; ++i ) {
                    this.mapMemberAdded ( resp[i].getSource() );
                    this.messageReceived ( resp[i].getMessage(), resp[i].getSource() );
                }
            } else {
                this.log.warn ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.broadcast.noReplies" ) );
            }
        } else {
            this.channel.send ( this.channel.getMembers(), msg, this.channelSendOptions );
        }
    }
    public void breakdown() {
        this.state = State.DESTROYED;
        if ( this.rpcChannel != null ) {
            this.rpcChannel.breakdown();
        }
        if ( this.channel != null ) {
            try {
                this.broadcast ( 7, false );
            } catch ( Exception ex ) {}
            this.channel.removeChannelListener ( this );
            this.channel.removeMembershipListener ( this );
        }
        this.rpcChannel = null;
        this.channel = null;
        this.mapMembers.clear();
        this.innerMap.clear();
        this.stateTransferred = false;
        this.externalLoaders = null;
    }
    public void finalize() throws Throwable {
        try {
            this.breakdown();
        } finally {
            super.finalize();
        }
    }
    @Override
    public int hashCode() {
        return Arrays.hashCode ( this.mapContextName );
    }
    @Override
    public boolean equals ( final Object o ) {
        if ( ! ( o instanceof AbstractReplicatedMap ) ) {
            return false;
        }
        if ( !o.getClass().equals ( this.getClass() ) ) {
            return false;
        }
        final AbstractReplicatedMap<K, V> other = ( AbstractReplicatedMap<K, V> ) o;
        return Arrays.equals ( this.mapContextName, other.mapContextName );
    }
    public Member[] getMapMembers ( final HashMap<Member, Long> members ) {
        synchronized ( members ) {
            final Member[] result = new Member[members.size()];
            members.keySet().toArray ( result );
            return result;
        }
    }
    public Member[] getMapMembers() {
        return this.getMapMembers ( this.mapMembers );
    }
    public Member[] getMapMembersExcl ( final Member[] exclude ) {
        if ( exclude == null ) {
            return null;
        }
        synchronized ( this.mapMembers ) {
            final HashMap<Member, Long> list = ( HashMap<Member, Long> ) this.mapMembers.clone();
            for ( int i = 0; i < exclude.length; ++i ) {
                list.remove ( exclude[i] );
            }
            return this.getMapMembers ( list );
        }
    }
    public void replicate ( final Object key, final boolean complete ) {
        if ( this.log.isTraceEnabled() ) {
            this.log.trace ( "Replicate invoked on key:" + key );
        }
        final MapEntry<K, V> entry = this.innerMap.get ( key );
        if ( entry == null ) {
            return;
        }
        if ( !entry.isSerializable() ) {
            return;
        }
        if ( entry.isPrimary() && entry.getBackupNodes() != null && entry.getBackupNodes().length > 0 ) {
            ReplicatedMapEntry rentry = null;
            if ( entry.getValue() instanceof ReplicatedMapEntry ) {
                rentry = ( ReplicatedMapEntry ) entry.getValue();
            }
            final boolean isDirty = rentry != null && rentry.isDirty();
            final boolean isAccess = rentry != null && rentry.isAccessReplicate();
            final boolean repl = complete || isDirty || isAccess;
            if ( !repl ) {
                if ( this.log.isTraceEnabled() ) {
                    this.log.trace ( "Not replicating:" + key + ", no change made" );
                }
                return;
            }
            MapMessage msg = null;
            if ( rentry != null && rentry.isDiffable() && ( isDirty || complete ) ) {
                rentry.lock();
                try {
                    msg = new MapMessage ( this.mapContextName, this.getReplicateMessageType(), true, ( Serializable ) entry.getKey(), null, rentry.getDiff(), entry.getPrimary(), entry.getBackupNodes() );
                    rentry.resetDiff();
                } catch ( IOException x ) {
                    this.log.error ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.unable.diffObject" ), x );
                } finally {
                    rentry.unlock();
                }
            }
            if ( msg == null && complete ) {
                msg = new MapMessage ( this.mapContextName, this.getReplicateMessageType(), false, ( Serializable ) entry.getKey(), ( Serializable ) entry.getValue(), null, entry.getPrimary(), entry.getBackupNodes() );
            }
            if ( msg == null ) {
                msg = new MapMessage ( this.mapContextName, 11, false, ( Serializable ) entry.getKey(), null, null, entry.getPrimary(), entry.getBackupNodes() );
            }
            try {
                if ( this.channel != null && entry.getBackupNodes() != null && entry.getBackupNodes().length > 0 ) {
                    if ( rentry != null ) {
                        rentry.setLastTimeReplicated ( System.currentTimeMillis() );
                    }
                    this.channel.send ( entry.getBackupNodes(), msg, this.channelSendOptions );
                }
            } catch ( ChannelException x2 ) {
                this.log.error ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.unable.replicate" ), x2 );
            }
        }
    }
    public void replicate ( final boolean complete ) {
        for ( final Entry<?, ?> e : this.innerMap.entrySet() ) {
            this.replicate ( e.getKey(), complete );
        }
    }
    public void transferState() {
        try {
            final Member[] members = this.getMapMembers();
            final Member backup = ( members.length > 0 ) ? members[0] : null;
            if ( backup != null ) {
                MapMessage msg = new MapMessage ( this.mapContextName, this.getStateMessageType(), false, null, null, null, null, null );
                final Response[] resp = this.rpcChannel.send ( new Member[] { backup }, msg, 1, this.channelSendOptions, this.rpcTimeout );
                if ( resp.length > 0 ) {
                    synchronized ( this.stateMutex ) {
                        msg = ( MapMessage ) resp[0].getMessage();
                        msg.deserialize ( this.getExternalLoaders() );
                        final ArrayList<?> list = ( ArrayList<?> ) msg.getValue();
                        for ( int i = 0; i < list.size(); ++i ) {
                            this.messageReceived ( ( Serializable ) list.get ( i ), resp[0].getSource() );
                        }
                    }
                    this.stateTransferred = true;
                } else {
                    this.log.warn ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.transferState.noReplies" ) );
                }
            }
        } catch ( ChannelException x ) {
            this.log.error ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.unable.transferState" ), x );
        } catch ( IOException x2 ) {
            this.log.error ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.unable.transferState" ), x2 );
        } catch ( ClassNotFoundException x3 ) {
            this.log.error ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.unable.transferState" ), x3 );
        }
    }
    @Override
    public Serializable replyRequest ( final Serializable msg, final Member sender ) {
        if ( ! ( msg instanceof MapMessage ) ) {
            return null;
        }
        final MapMessage mapmsg = ( MapMessage ) msg;
        if ( mapmsg.getMsgType() == 8 ) {
            mapmsg.setPrimary ( this.channel.getLocalMember ( false ) );
            return mapmsg;
        }
        if ( mapmsg.getMsgType() == 6 ) {
            mapmsg.setPrimary ( this.channel.getLocalMember ( false ) );
            this.mapMemberAdded ( sender );
            return mapmsg;
        }
        if ( mapmsg.getMsgType() == 2 ) {
            final MapEntry<K, V> entry = this.innerMap.get ( mapmsg.getKey() );
            if ( entry == null || !entry.isSerializable() ) {
                return null;
            }
            mapmsg.setValue ( ( Serializable ) entry.getValue() );
            return mapmsg;
        } else {
            if ( mapmsg.getMsgType() == 5 || mapmsg.getMsgType() == 10 ) {
                synchronized ( this.stateMutex ) {
                    final ArrayList<MapMessage> list = new ArrayList<MapMessage>();
                    for ( final Entry<?, ?> e : this.innerMap.entrySet() ) {
                        final MapEntry<K, V> entry2 = this.innerMap.get ( e.getKey() );
                        if ( entry2 != null && entry2.isSerializable() ) {
                            final boolean copy = mapmsg.getMsgType() == 10;
                            final MapMessage me = new MapMessage ( this.mapContextName, copy ? 9 : 3, false, ( Serializable ) entry2.getKey(), copy ? ( ( Serializable ) entry2.getValue() ) : null, null, entry2.getPrimary(), entry2.getBackupNodes() );
                            list.add ( me );
                        }
                    }
                    mapmsg.setValue ( list );
                    return mapmsg;
                }
            }
            if ( mapmsg.getMsgType() == 13 ) {
                mapmsg.setValue ( this.state );
                mapmsg.setPrimary ( this.channel.getLocalMember ( false ) );
                return mapmsg;
            }
            return null;
        }
    }
    @Override
    public void leftOver ( final Serializable msg, final Member sender ) {
        if ( ! ( msg instanceof MapMessage ) ) {
            return;
        }
        final MapMessage mapmsg = ( MapMessage ) msg;
        try {
            mapmsg.deserialize ( this.getExternalLoaders() );
            if ( mapmsg.getMsgType() == 6 ) {
                this.mapMemberAdded ( mapmsg.getPrimary() );
            } else if ( mapmsg.getMsgType() == 8 || mapmsg.getMsgType() == 13 ) {
                this.memberAlive ( mapmsg.getPrimary() );
            } else if ( this.log.isInfoEnabled() ) {
                this.log.info ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.leftOver.ignored", mapmsg.getTypeDesc() ) );
            }
        } catch ( IOException x ) {
            this.log.error ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.unable.deserialize.MapMessage" ), x );
        } catch ( ClassNotFoundException x2 ) {
            this.log.error ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.unable.deserialize.MapMessage" ), x2 );
        }
    }
    @Override
    public void messageReceived ( final Serializable msg, final Member sender ) {
        if ( ! ( msg instanceof MapMessage ) ) {
            return;
        }
        final MapMessage mapmsg = ( MapMessage ) msg;
        if ( this.log.isTraceEnabled() ) {
            this.log.trace ( "Map[" + this.mapname + "] received message:" + mapmsg );
        }
        try {
            mapmsg.deserialize ( this.getExternalLoaders() );
        } catch ( IOException x ) {
            this.log.error ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.unable.deserialize.MapMessage" ), x );
            return;
        } catch ( ClassNotFoundException x2 ) {
            this.log.error ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.unable.deserialize.MapMessage" ), x2 );
            return;
        }
        if ( this.log.isTraceEnabled() ) {
            this.log.trace ( "Map message received from:" + sender.getName() + " msg:" + mapmsg );
        }
        if ( mapmsg.getMsgType() == 6 ) {
            this.mapMemberAdded ( mapmsg.getPrimary() );
        }
        if ( mapmsg.getMsgType() == 7 ) {
            this.memberDisappeared ( mapmsg.getPrimary() );
        }
        if ( mapmsg.getMsgType() == 3 ) {
            MapEntry<K, V> entry = this.innerMap.get ( mapmsg.getKey() );
            if ( entry == null ) {
                entry = new MapEntry<K, V> ( ( K ) mapmsg.getKey(), ( V ) mapmsg.getValue() );
                final MapEntry<K, V> old = this.innerMap.putIfAbsent ( entry.getKey(), entry );
                if ( old != null ) {
                    entry = old;
                }
            }
            entry.setProxy ( true );
            entry.setBackup ( false );
            entry.setCopy ( false );
            entry.setBackupNodes ( mapmsg.getBackupNodes() );
            entry.setPrimary ( mapmsg.getPrimary() );
        }
        if ( mapmsg.getMsgType() == 4 ) {
            this.innerMap.remove ( mapmsg.getKey() );
        }
        if ( mapmsg.getMsgType() == 1 || mapmsg.getMsgType() == 9 ) {
            MapEntry<K, V> entry = this.innerMap.get ( mapmsg.getKey() );
            if ( entry == null ) {
                entry = new MapEntry<K, V> ( ( K ) mapmsg.getKey(), ( V ) mapmsg.getValue() );
                entry.setBackup ( mapmsg.getMsgType() == 1 );
                entry.setProxy ( false );
                entry.setCopy ( mapmsg.getMsgType() == 9 );
                entry.setBackupNodes ( mapmsg.getBackupNodes() );
                entry.setPrimary ( mapmsg.getPrimary() );
                if ( mapmsg.getValue() instanceof ReplicatedMapEntry ) {
                    ( ( ReplicatedMapEntry ) mapmsg.getValue() ).setOwner ( this.getMapOwner() );
                }
            } else {
                entry.setBackup ( mapmsg.getMsgType() == 1 );
                entry.setProxy ( false );
                entry.setCopy ( mapmsg.getMsgType() == 9 );
                entry.setBackupNodes ( mapmsg.getBackupNodes() );
                entry.setPrimary ( mapmsg.getPrimary() );
                if ( entry.getValue() instanceof ReplicatedMapEntry ) {
                    final ReplicatedMapEntry diff = ( ReplicatedMapEntry ) entry.getValue();
                    if ( mapmsg.isDiff() ) {
                        diff.lock();
                        try {
                            diff.applyDiff ( mapmsg.getDiffValue(), 0, mapmsg.getDiffValue().length );
                        } catch ( Exception x3 ) {
                            this.log.error ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.unableApply.diff", entry.getKey() ), x3 );
                        } finally {
                            diff.unlock();
                        }
                    } else if ( mapmsg.getValue() != null ) {
                        if ( mapmsg.getValue() instanceof ReplicatedMapEntry ) {
                            final ReplicatedMapEntry re = ( ReplicatedMapEntry ) mapmsg.getValue();
                            re.setOwner ( this.getMapOwner() );
                            entry.setValue ( ( V ) re );
                        } else {
                            entry.setValue ( ( V ) mapmsg.getValue() );
                        }
                    } else {
                        ( ( ReplicatedMapEntry ) entry.getValue() ).setOwner ( this.getMapOwner() );
                    }
                } else if ( mapmsg.getValue() instanceof ReplicatedMapEntry ) {
                    final ReplicatedMapEntry re2 = ( ReplicatedMapEntry ) mapmsg.getValue();
                    re2.setOwner ( this.getMapOwner() );
                    entry.setValue ( ( V ) re2 );
                } else if ( mapmsg.getValue() != null ) {
                    entry.setValue ( ( V ) mapmsg.getValue() );
                }
            }
            this.innerMap.put ( entry.getKey(), entry );
        }
        if ( mapmsg.getMsgType() == 11 ) {
            final MapEntry<K, V> entry = this.innerMap.get ( mapmsg.getKey() );
            if ( entry != null ) {
                entry.setBackupNodes ( mapmsg.getBackupNodes() );
                entry.setPrimary ( mapmsg.getPrimary() );
                if ( entry.getValue() instanceof ReplicatedMapEntry ) {
                    ( ( ReplicatedMapEntry ) entry.getValue() ).accessEntry();
                }
            }
        }
        if ( mapmsg.getMsgType() == 12 ) {
            final MapEntry<K, V> entry = this.innerMap.get ( mapmsg.getKey() );
            if ( entry != null ) {
                entry.setBackupNodes ( mapmsg.getBackupNodes() );
                entry.setPrimary ( mapmsg.getPrimary() );
            }
        }
    }
    @Override
    public boolean accept ( final Serializable msg, final Member sender ) {
        boolean result = false;
        if ( msg instanceof MapMessage ) {
            if ( this.log.isTraceEnabled() ) {
                this.log.trace ( "Map[" + this.mapname + "] accepting...." + msg );
            }
            result = Arrays.equals ( this.mapContextName, ( ( MapMessage ) msg ).getMapId() );
            if ( this.log.isTraceEnabled() ) {
                this.log.trace ( "Msg[" + this.mapname + "] accepted[" + result + "]...." + msg );
            }
        }
        return result;
    }
    public void mapMemberAdded ( final Member member ) {
        if ( member.equals ( this.getChannel().getLocalMember ( false ) ) ) {
            return;
        }
        boolean memberAdded = false;
        final Member mapMember = this.getChannel().getMember ( member );
        if ( mapMember == null ) {
            this.log.warn ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.mapMemberAdded.nullMember", member ) );
            return;
        }
        synchronized ( this.mapMembers ) {
            if ( !this.mapMembers.containsKey ( mapMember ) ) {
                if ( this.log.isInfoEnabled() ) {
                    this.log.info ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.mapMemberAdded.added", mapMember ) );
                }
                this.mapMembers.put ( mapMember, System.currentTimeMillis() );
                memberAdded = true;
            }
        }
        if ( memberAdded ) {
            synchronized ( this.stateMutex ) {
                for ( final Entry<K, MapEntry<K, V>> e : this.innerMap.entrySet() ) {
                    final MapEntry<K, V> entry = this.innerMap.get ( e.getKey() );
                    if ( entry == null ) {
                        continue;
                    }
                    if ( !entry.isPrimary() ) {
                        continue;
                    }
                    if ( entry.getBackupNodes() != null ) {
                        if ( entry.getBackupNodes().length != 0 ) {
                            continue;
                        }
                    }
                    try {
                        final Member[] backup = this.publishEntryInfo ( entry.getKey(), entry.getValue() );
                        entry.setBackupNodes ( backup );
                        entry.setPrimary ( this.channel.getLocalMember ( false ) );
                    } catch ( ChannelException x ) {
                        this.log.error ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.unableSelect.backup" ), x );
                    }
                }
            }
        }
    }
    public boolean inSet ( final Member m, final Member[] set ) {
        if ( set == null ) {
            return false;
        }
        boolean result = false;
        for ( int i = 0; i < set.length && !result; ++i ) {
            if ( m.equals ( set[i] ) ) {
                result = true;
            }
        }
        return result;
    }
    public Member[] excludeFromSet ( final Member[] mbrs, final Member[] set ) {
        final ArrayList<Member> result = new ArrayList<Member>();
        for ( int i = 0; i < set.length; ++i ) {
            boolean include = true;
            for ( int j = 0; j < mbrs.length && include; ++j ) {
                if ( mbrs[j].equals ( set[i] ) ) {
                    include = false;
                }
            }
            if ( include ) {
                result.add ( set[i] );
            }
        }
        return result.toArray ( new Member[result.size()] );
    }
    @Override
    public void memberAdded ( final Member member ) {
    }
    @Override
    public void memberDisappeared ( final Member member ) {
        boolean removed = false;
        synchronized ( this.mapMembers ) {
            removed = ( this.mapMembers.remove ( member ) != null );
            if ( !removed ) {
                if ( this.log.isDebugEnabled() ) {
                    this.log.debug ( "Member[" + member + "] disappeared, but was not present in the map." );
                }
                return;
            }
        }
        if ( this.log.isInfoEnabled() ) {
            this.log.info ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.member.disappeared", member ) );
        }
        final long start = System.currentTimeMillis();
        final Iterator<Entry<K, MapEntry<K, V>>> i = ( Iterator<Entry<K, MapEntry<K, V>>> ) this.innerMap.entrySet().iterator();
        while ( i.hasNext() ) {
            final Entry<K, MapEntry<K, V>> e = i.next();
            final MapEntry<K, V> entry = this.innerMap.get ( e.getKey() );
            if ( entry == null ) {
                continue;
            }
            if ( entry.isPrimary() && this.inSet ( member, entry.getBackupNodes() ) ) {
                if ( this.log.isDebugEnabled() ) {
                    this.log.debug ( "[1] Primary choosing a new backup" );
                }
                try {
                    final Member[] backup = this.publishEntryInfo ( entry.getKey(), entry.getValue() );
                    entry.setBackupNodes ( backup );
                    entry.setPrimary ( this.channel.getLocalMember ( false ) );
                } catch ( ChannelException x ) {
                    this.log.error ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.unable.relocate", entry.getKey() ), x );
                }
            } else if ( member.equals ( entry.getPrimary() ) ) {
                if ( this.log.isDebugEnabled() ) {
                    this.log.debug ( "[2] Primary disappeared" );
                }
                entry.setPrimary ( null );
            }
            if ( entry.isProxy() && entry.getPrimary() == null && entry.getBackupNodes() != null && entry.getBackupNodes().length == 1 && entry.getBackupNodes() [0].equals ( member ) ) {
                if ( this.log.isDebugEnabled() ) {
                    this.log.debug ( "[3] Removing orphaned proxy" );
                }
                i.remove();
            } else {
                if ( entry.getPrimary() != null || !entry.isBackup() || entry.getBackupNodes() == null || entry.getBackupNodes().length != 1 || !entry.getBackupNodes() [0].equals ( this.channel.getLocalMember ( false ) ) ) {
                    continue;
                }
                try {
                    if ( this.log.isDebugEnabled() ) {
                        this.log.debug ( "[4] Backup becoming primary" );
                    }
                    entry.setPrimary ( this.channel.getLocalMember ( false ) );
                    entry.setBackup ( false );
                    entry.setProxy ( false );
                    entry.setCopy ( false );
                    final Member[] backup = this.publishEntryInfo ( entry.getKey(), entry.getValue() );
                    entry.setBackupNodes ( backup );
                    if ( this.mapOwner == null ) {
                        continue;
                    }
                    this.mapOwner.objectMadePrimary ( entry.getKey(), entry.getValue() );
                } catch ( ChannelException x ) {
                    this.log.error ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.unable.relocate", entry.getKey() ), x );
                }
            }
        }
        final long complete = System.currentTimeMillis() - start;
        if ( this.log.isInfoEnabled() ) {
            this.log.info ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.relocate.complete", Long.toString ( complete ) ) );
        }
    }
    public int getNextBackupIndex() {
        final int size = this.mapMembers.size();
        if ( this.mapMembers.size() == 0 ) {
            return -1;
        }
        int node = this.currentNode++;
        if ( node >= size ) {
            node = 0;
            this.currentNode = 0;
        }
        return node;
    }
    public Member getNextBackupNode() {
        final Member[] members = this.getMapMembers();
        int node = this.getNextBackupIndex();
        if ( members.length == 0 || node == -1 ) {
            return null;
        }
        if ( node >= members.length ) {
            node = 0;
        }
        return members[node];
    }
    protected abstract Member[] publishEntryInfo ( final Object p0, final Object p1 ) throws ChannelException;
    @Override
    public void heartbeat() {
        try {
            if ( this.state.isAvailable() ) {
                this.ping ( this.accessTimeout );
            }
        } catch ( Exception x ) {
            this.log.error ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.heartbeat.failed" ), x );
        }
    }
    @Override
    public V remove ( final Object key ) {
        return this.remove ( key, true );
    }
    public V remove ( final Object key, final boolean notify ) {
        final MapEntry<K, V> entry = this.innerMap.remove ( key );
        try {
            if ( this.getMapMembers().length > 0 && notify ) {
                final MapMessage msg = new MapMessage ( this.getMapContextName(), 4, false, ( Serializable ) key, null, null, null, null );
                this.getChannel().send ( this.getMapMembers(), msg, this.getChannelSendOptions() );
            }
        } catch ( ChannelException x ) {
            this.log.error ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.unable.remove" ), x );
        }
        return ( entry != null ) ? entry.getValue() : null;
    }
    public MapEntry<K, V> getInternal ( final Object key ) {
        return this.innerMap.get ( key );
    }
    @Override
    public V get ( final Object key ) {
        final MapEntry<K, V> entry = this.innerMap.get ( key );
        if ( this.log.isTraceEnabled() ) {
            this.log.trace ( "Requesting id:" + key + " entry:" + entry );
        }
        if ( entry == null ) {
            return null;
        }
        if ( !entry.isPrimary() ) {
            try {
                Member[] backup = null;
                MapMessage msg = null;
                if ( entry.isBackup() ) {
                    backup = this.publishEntryInfo ( key, entry.getValue() );
                } else if ( entry.isProxy() ) {
                    msg = new MapMessage ( this.getMapContextName(), 2, false, ( Serializable ) key, null, null, null, null );
                    final Response[] resp = this.getRpcChannel().send ( entry.getBackupNodes(), msg, 1, this.getChannelSendOptions(), this.getRpcTimeout() );
                    if ( resp == null || resp.length == 0 || resp[0].getMessage() == null ) {
                        this.log.warn ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.unable.retrieve", key ) );
                        return null;
                    }
                    msg = ( MapMessage ) resp[0].getMessage();
                    msg.deserialize ( this.getExternalLoaders() );
                    backup = entry.getBackupNodes();
                    if ( msg.getValue() != null ) {
                        entry.setValue ( ( V ) msg.getValue() );
                    }
                    msg = new MapMessage ( this.getMapContextName(), 12, false, ( Serializable ) entry.getKey(), null, null, this.channel.getLocalMember ( false ), backup );
                    if ( backup != null && backup.length > 0 ) {
                        this.getChannel().send ( backup, msg, this.getChannelSendOptions() );
                    }
                    msg = new MapMessage ( this.getMapContextName(), 3, false, ( Serializable ) key, null, null, this.channel.getLocalMember ( false ), backup );
                    final Member[] dest = this.getMapMembersExcl ( backup );
                    if ( dest != null && dest.length > 0 ) {
                        this.getChannel().send ( dest, msg, this.getChannelSendOptions() );
                    }
                    if ( entry.getValue() instanceof ReplicatedMapEntry ) {
                        final ReplicatedMapEntry val = ( ReplicatedMapEntry ) entry.getValue();
                        val.setOwner ( this.getMapOwner() );
                    }
                } else if ( entry.isCopy() ) {
                    backup = this.getMapMembers();
                    if ( backup.length > 0 ) {
                        msg = new MapMessage ( this.getMapContextName(), 12, false, ( Serializable ) key, null, null, this.channel.getLocalMember ( false ), backup );
                        this.getChannel().send ( backup, msg, this.getChannelSendOptions() );
                    }
                }
                entry.setPrimary ( this.channel.getLocalMember ( false ) );
                entry.setBackupNodes ( backup );
                entry.setBackup ( false );
                entry.setProxy ( false );
                entry.setCopy ( false );
                if ( this.getMapOwner() != null ) {
                    this.getMapOwner().objectMadePrimary ( key, entry.getValue() );
                }
            } catch ( Exception x ) {
                this.log.error ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.unable.get" ), x );
                return null;
            }
        }
        if ( this.log.isTraceEnabled() ) {
            this.log.trace ( "Requesting id:" + key + " result:" + entry.getValue() );
        }
        return entry.getValue();
    }
    protected void printMap ( final String header ) {
        try {
            System.out.println ( "\nDEBUG MAP:" + header );
            System.out.println ( "Map[" + new String ( this.mapContextName, StandardCharsets.ISO_8859_1 ) + ", Map Size:" + this.innerMap.size() );
            final Member[] mbrs = this.getMapMembers();
            for ( int i = 0; i < mbrs.length; ++i ) {
                System.out.println ( "Mbr[" + ( i + 1 ) + "=" + mbrs[i].getName() );
            }
            final Iterator<Entry<K, MapEntry<K, V>>> j = ( Iterator<Entry<K, MapEntry<K, V>>> ) this.innerMap.entrySet().iterator();
            int cnt = 0;
            while ( j.hasNext() ) {
                final Entry<?, ?> e = j.next();
                System.out.println ( ++cnt + ". " + this.innerMap.get ( e.getKey() ) );
            }
            System.out.println ( "EndMap]\n\n" );
        } catch ( Exception ignore ) {
            ignore.printStackTrace();
        }
    }
    @Override
    public boolean containsKey ( final Object key ) {
        return this.innerMap.containsKey ( key );
    }
    @Override
    public V put ( final K key, final V value ) {
        return this.put ( key, value, true );
    }
    public V put ( final K key, final V value, final boolean notify ) {
        final MapEntry<K, V> entry = new MapEntry<K, V> ( key, value );
        entry.setBackup ( false );
        entry.setProxy ( false );
        entry.setCopy ( false );
        entry.setPrimary ( this.channel.getLocalMember ( false ) );
        V old = null;
        if ( this.containsKey ( key ) ) {
            old = this.remove ( key );
        }
        try {
            if ( notify ) {
                final Member[] backup = this.publishEntryInfo ( key, value );
                entry.setBackupNodes ( backup );
            }
        } catch ( ChannelException x ) {
            this.log.error ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.unable.put" ), x );
        }
        this.innerMap.put ( key, entry );
        return old;
    }
    @Override
    public void putAll ( final Map<? extends K, ? extends V> m ) {
        for ( final Entry<K, V> entry : m.entrySet() ) {
            this.put ( entry.getKey(), entry.getValue() );
        }
    }
    @Override
    public void clear() {
        this.clear ( true );
    }
    public void clear ( final boolean notify ) {
        if ( notify ) {
            final Iterator<K> keys = this.keySet().iterator();
            while ( keys.hasNext() ) {
                this.remove ( keys.next() );
            }
        } else {
            this.innerMap.clear();
        }
    }
    @Override
    public boolean containsValue ( final Object value ) {
        Objects.requireNonNull ( value );
        for ( final Entry<K, MapEntry<K, V>> e : this.innerMap.entrySet() ) {
            final MapEntry<K, V> entry = this.innerMap.get ( e.getKey() );
            if ( entry != null && entry.isActive() && value.equals ( entry.getValue() ) ) {
                return true;
            }
        }
        return false;
    }
    public Object clone() {
        throw new UnsupportedOperationException ( AbstractReplicatedMap.sm.getString ( "abstractReplicatedMap.unsupport.operation" ) );
    }
    public Set<Entry<K, MapEntry<K, V>>> entrySetFull() {
        return this.innerMap.entrySet();
    }
    public Set<K> keySetFull() {
        return this.innerMap.keySet();
    }
    public int sizeFull() {
        return this.innerMap.size();
    }
    @Override
    public Set<Entry<K, V>> entrySet() {
        final LinkedHashSet<Entry<K, V>> set = new LinkedHashSet<Entry<K, V>> ( this.innerMap.size() );
        for ( final Entry<?, ?> e : this.innerMap.entrySet() ) {
            final Object key = e.getKey();
            final MapEntry<K, V> entry = this.innerMap.get ( key );
            if ( entry != null && entry.isActive() ) {
                set.add ( entry );
            }
        }
        return Collections.unmodifiableSet ( ( Set<? extends Entry<K, V>> ) set );
    }
    @Override
    public Set<K> keySet() {
        final LinkedHashSet<K> set = new LinkedHashSet<K> ( this.innerMap.size() );
        for ( final Entry<K, MapEntry<K, V>> e : this.innerMap.entrySet() ) {
            final K key = e.getKey();
            final MapEntry<K, V> entry = this.innerMap.get ( key );
            if ( entry != null && entry.isActive() ) {
                set.add ( key );
            }
        }
        return Collections.unmodifiableSet ( ( Set<? extends K> ) set );
    }
    @Override
    public int size() {
        int counter = 0;
        final Iterator<Entry<K, MapEntry<K, V>>> it = ( Iterator<Entry<K, MapEntry<K, V>>> ) this.innerMap.entrySet().iterator();
        while ( it != null && it.hasNext() ) {
            final Entry<?, ?> e = it.next();
            if ( e != null ) {
                final MapEntry<K, V> entry = this.innerMap.get ( e.getKey() );
                if ( entry == null || !entry.isActive() || entry.getValue() == null ) {
                    continue;
                }
                ++counter;
            }
        }
        return counter;
    }
    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }
    @Override
    public Collection<V> values() {
        final ArrayList<V> values = new ArrayList<V>();
        for ( final Entry<K, MapEntry<K, V>> e : this.innerMap.entrySet() ) {
            final MapEntry<K, V> entry = this.innerMap.get ( e.getKey() );
            if ( entry != null && entry.isActive() && entry.getValue() != null ) {
                values.add ( entry.getValue() );
            }
        }
        return Collections.unmodifiableCollection ( ( Collection<? extends V> ) values );
    }
    public Channel getChannel() {
        return this.channel;
    }
    public byte[] getMapContextName() {
        return this.mapContextName;
    }
    public RpcChannel getRpcChannel() {
        return this.rpcChannel;
    }
    public long getRpcTimeout() {
        return this.rpcTimeout;
    }
    public Object getStateMutex() {
        return this.stateMutex;
    }
    public boolean isStateTransferred() {
        return this.stateTransferred;
    }
    public MapOwner getMapOwner() {
        return this.mapOwner;
    }
    public ClassLoader[] getExternalLoaders() {
        return this.externalLoaders;
    }
    public int getChannelSendOptions() {
        return this.channelSendOptions;
    }
    public long getAccessTimeout() {
        return this.accessTimeout;
    }
    public void setMapOwner ( final MapOwner mapOwner ) {
        this.mapOwner = mapOwner;
    }
    public void setExternalLoaders ( final ClassLoader[] externalLoaders ) {
        this.externalLoaders = externalLoaders;
    }
    public void setChannelSendOptions ( final int channelSendOptions ) {
        this.channelSendOptions = channelSendOptions;
    }
    public void setAccessTimeout ( final long accessTimeout ) {
        this.accessTimeout = accessTimeout;
    }
    static {
        sm = StringManager.getManager ( AbstractReplicatedMap.class );
    }
    public static class MapEntry<K, V> implements Entry<K, V> {
        private boolean backup;
        private boolean proxy;
        private boolean copy;
        private Member[] backupNodes;
        private Member primary;
        private K key;
        private V value;
        public MapEntry ( final K key, final V value ) {
            this.setKey ( key );
            this.setValue ( value );
        }
        public boolean isKeySerializable() {
            return this.key == null || this.key instanceof Serializable;
        }
        public boolean isValueSerializable() {
            return this.value == null || this.value instanceof Serializable;
        }
        public boolean isSerializable() {
            return this.isKeySerializable() && this.isValueSerializable();
        }
        public boolean isBackup() {
            return this.backup;
        }
        public void setBackup ( final boolean backup ) {
            this.backup = backup;
        }
        public boolean isProxy() {
            return this.proxy;
        }
        public boolean isPrimary() {
            return !this.proxy && !this.backup && !this.copy;
        }
        public boolean isActive() {
            return !this.proxy;
        }
        public void setProxy ( final boolean proxy ) {
            this.proxy = proxy;
        }
        public boolean isCopy() {
            return this.copy;
        }
        public void setCopy ( final boolean copy ) {
            this.copy = copy;
        }
        public boolean isDiffable() {
            return this.value instanceof ReplicatedMapEntry && ( ( ReplicatedMapEntry ) this.value ).isDiffable();
        }
        public void setBackupNodes ( final Member[] nodes ) {
            this.backupNodes = nodes;
        }
        public Member[] getBackupNodes() {
            return this.backupNodes;
        }
        public void setPrimary ( final Member m ) {
            this.primary = m;
        }
        public Member getPrimary() {
            return this.primary;
        }
        @Override
        public V getValue() {
            return this.value;
        }
        @Override
        public V setValue ( final V value ) {
            final V old = this.value;
            this.value = value;
            return old;
        }
        @Override
        public K getKey() {
            return this.key;
        }
        public K setKey ( final K key ) {
            final K old = this.key;
            this.key = key;
            return old;
        }
        @Override
        public int hashCode() {
            return this.key.hashCode();
        }
        @Override
        public boolean equals ( final Object o ) {
            return this.key.equals ( o );
        }
        public void apply ( final byte[] data, final int offset, final int length, final boolean diff ) throws IOException, ClassNotFoundException {
            if ( this.isDiffable() && diff ) {
                final ReplicatedMapEntry rentry = ( ReplicatedMapEntry ) this.value;
                rentry.lock();
                try {
                    rentry.applyDiff ( data, offset, length );
                } finally {
                    rentry.unlock();
                }
            } else if ( length == 0 ) {
                this.value = null;
                this.proxy = true;
            } else {
                this.value = ( V ) XByteBuffer.deserialize ( data, offset, length );
            }
        }
        @Override
        public String toString() {
            final StringBuilder buf = new StringBuilder ( "MapEntry[key:" );
            buf.append ( this.getKey() ).append ( "; " );
            buf.append ( "value:" ).append ( this.getValue() ).append ( "; " );
            buf.append ( "primary:" ).append ( this.isPrimary() ).append ( "; " );
            buf.append ( "backup:" ).append ( this.isBackup() ).append ( "; " );
            buf.append ( "proxy:" ).append ( this.isProxy() ).append ( ";]" );
            return buf.toString();
        }
    }
    public static class MapMessage implements Serializable {
        private static final long serialVersionUID = 1L;
        public static final int MSG_BACKUP = 1;
        public static final int MSG_RETRIEVE_BACKUP = 2;
        public static final int MSG_PROXY = 3;
        public static final int MSG_REMOVE = 4;
        public static final int MSG_STATE = 5;
        public static final int MSG_START = 6;
        public static final int MSG_STOP = 7;
        public static final int MSG_INIT = 8;
        public static final int MSG_COPY = 9;
        public static final int MSG_STATE_COPY = 10;
        public static final int MSG_ACCESS = 11;
        public static final int MSG_NOTIFY_MAPMEMBER = 12;
        public static final int MSG_PING = 13;
        private final byte[] mapId;
        private final int msgtype;
        private final boolean diff;
        private transient Serializable key;
        private transient Serializable value;
        private byte[] valuedata;
        private byte[] keydata;
        private final byte[] diffvalue;
        private final Member[] nodes;
        private Member primary;
        @Override
        public String toString() {
            final StringBuilder buf = new StringBuilder ( "MapMessage[context=" );
            buf.append ( new String ( this.mapId ) );
            buf.append ( "; type=" );
            buf.append ( this.getTypeDesc() );
            buf.append ( "; key=" );
            buf.append ( this.key );
            buf.append ( "; value=" );
            buf.append ( this.value );
            return buf.toString();
        }
        public String getTypeDesc() {
            switch ( this.msgtype ) {
            case 1: {
                return "MSG_BACKUP";
            }
            case 2: {
                return "MSG_RETRIEVE_BACKUP";
            }
            case 3: {
                return "MSG_PROXY";
            }
            case 4: {
                return "MSG_REMOVE";
            }
            case 5: {
                return "MSG_STATE";
            }
            case 6: {
                return "MSG_START";
            }
            case 7: {
                return "MSG_STOP";
            }
            case 8: {
                return "MSG_INIT";
            }
            case 10: {
                return "MSG_STATE_COPY";
            }
            case 9: {
                return "MSG_COPY";
            }
            case 11: {
                return "MSG_ACCESS";
            }
            case 12: {
                return "MSG_NOTIFY_MAPMEMBER";
            }
            case 13: {
                return "MSG_PING";
            }
            default: {
                return "UNKNOWN";
            }
            }
        }
        public MapMessage ( final byte[] mapId, final int msgtype, final boolean diff, final Serializable key, final Serializable value, final byte[] diffvalue, final Member primary, final Member[] nodes ) {
            this.mapId = mapId;
            this.msgtype = msgtype;
            this.diff = diff;
            this.key = key;
            this.value = value;
            this.diffvalue = diffvalue;
            this.nodes = nodes;
            this.primary = primary;
            this.setValue ( value );
            this.setKey ( key );
        }
        public void deserialize ( final ClassLoader[] cls ) throws IOException, ClassNotFoundException {
            this.key ( cls );
            this.value ( cls );
        }
        public int getMsgType() {
            return this.msgtype;
        }
        public boolean isDiff() {
            return this.diff;
        }
        public Serializable getKey() {
            try {
                return this.key ( null );
            } catch ( Exception x ) {
                throw new RuntimeException ( AbstractReplicatedMap.sm.getString ( "mapMessage.deserialize.error.key" ), x );
            }
        }
        public Serializable key ( final ClassLoader[] cls ) throws IOException, ClassNotFoundException {
            if ( this.key != null ) {
                return this.key;
            }
            if ( this.keydata == null || this.keydata.length == 0 ) {
                return null;
            }
            this.key = XByteBuffer.deserialize ( this.keydata, 0, this.keydata.length, cls );
            this.keydata = null;
            return this.key;
        }
        public byte[] getKeyData() {
            return this.keydata;
        }
        public Serializable getValue() {
            try {
                return this.value ( null );
            } catch ( Exception x ) {
                throw new RuntimeException ( AbstractReplicatedMap.sm.getString ( "mapMessage.deserialize.error.value" ), x );
            }
        }
        public Serializable value ( final ClassLoader[] cls ) throws IOException, ClassNotFoundException {
            if ( this.value != null ) {
                return this.value;
            }
            if ( this.valuedata == null || this.valuedata.length == 0 ) {
                return null;
            }
            this.value = XByteBuffer.deserialize ( this.valuedata, 0, this.valuedata.length, cls );
            this.valuedata = null;
            return this.value;
        }
        public byte[] getValueData() {
            return this.valuedata;
        }
        public byte[] getDiffValue() {
            return this.diffvalue;
        }
        public Member[] getBackupNodes() {
            return this.nodes;
        }
        public Member getPrimary() {
            return this.primary;
        }
        private void setPrimary ( final Member m ) {
            this.primary = m;
        }
        public byte[] getMapId() {
            return this.mapId;
        }
        public void setValue ( final Serializable value ) {
            try {
                if ( value != null ) {
                    this.valuedata = XByteBuffer.serialize ( value );
                }
                this.value = value;
            } catch ( IOException x ) {
                throw new RuntimeException ( x );
            }
        }
        public void setKey ( final Serializable key ) {
            try {
                if ( key != null ) {
                    this.keydata = XByteBuffer.serialize ( key );
                }
                this.key = key;
            } catch ( IOException x ) {
                throw new RuntimeException ( x );
            }
        }
        public Object clone() {
            final MapMessage msg = new MapMessage ( this.mapId, this.msgtype, this.diff, this.key, this.value, this.diffvalue, this.primary, this.nodes );
            msg.keydata = this.keydata;
            msg.valuedata = this.valuedata;
            return msg;
        }
    }
    private enum State {
        NEW ( false ),
        INITIALIZED ( true ),
        DESTROYED ( false );
        private final boolean available;
        private State ( final boolean available ) {
            this.available = available;
        }
        public boolean isAvailable() {
            return this.available;
        }
    }
    public interface MapOwner {
        void objectMadePrimary ( Object p0, Object p1 );
    }
}
