package org.apache.catalina.tribes.tipis;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ChannelException.FaultyMember;
import org.apache.catalina.tribes.ChannelListener;
import org.apache.catalina.tribes.Heartbeat;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.MembershipListener;
import org.apache.catalina.tribes.group.Response;
import org.apache.catalina.tribes.group.RpcCallback;
import org.apache.catalina.tribes.group.RpcChannel;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.util.Arrays;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public abstract class AbstractReplicatedMap<K, V>
    implements Map<K, V>, Serializable, RpcCallback, ChannelListener,
    MembershipListener, Heartbeat {
    private static final long serialVersionUID = 1L;
    protected static final StringManager sm = StringManager.getManager ( AbstractReplicatedMap.class );
    private final Log log = LogFactory.getLog ( AbstractReplicatedMap.class );
    public static final int DEFAULT_INITIAL_CAPACITY = 16;
    public static final float DEFAULT_LOAD_FACTOR = 0.75f;
    protected final ConcurrentMap<K, MapEntry<K, V>> innerMap;
    protected abstract int getStateMessageType();
    protected abstract int getReplicateMessageType();
    protected transient long rpcTimeout = 5000;
    protected transient Channel channel;
    protected transient RpcChannel rpcChannel;
    protected transient byte[] mapContextName;
    protected transient boolean stateTransferred = false;
    protected final transient Object stateMutex = new Object();
    protected final transient HashMap<Member, Long> mapMembers = new HashMap<>();
    protected transient int channelSendOptions = Channel.SEND_OPTIONS_DEFAULT;
    protected transient MapOwner mapOwner;
    protected transient ClassLoader[] externalLoaders;
    protected transient int currentNode = 0;
    protected transient long accessTimeout = 5000;
    protected transient String mapname = "";
    private transient volatile State state = State.NEW;
    public static interface MapOwner {
        public void objectMadePrimary ( Object key, Object value );
    }
    public AbstractReplicatedMap ( MapOwner owner,
                                   Channel channel,
                                   long timeout,
                                   String mapContextName,
                                   int initialCapacity,
                                   float loadFactor,
                                   int channelSendOptions,
                                   ClassLoader[] cls,
                                   boolean terminate ) {
        innerMap = new ConcurrentHashMap<> ( initialCapacity, loadFactor, 15 );
        init ( owner, channel, mapContextName, timeout, channelSendOptions, cls, terminate );
    }
    protected Member[] wrap ( Member m ) {
        if ( m == null ) {
            return new Member[0];
        } else return new Member[] {m};
    }
    protected void init ( MapOwner owner, Channel channel, String mapContextName,
                          long timeout, int channelSendOptions, ClassLoader[] cls, boolean terminate ) {
        long start = System.currentTimeMillis();
        if ( log.isInfoEnabled() ) {
            log.info ( sm.getString ( "abstractReplicatedMap.init.start", mapContextName ) );
        }
        this.mapOwner = owner;
        this.externalLoaders = cls;
        this.channelSendOptions = channelSendOptions;
        this.channel = channel;
        this.rpcTimeout = timeout;
        this.mapname = mapContextName;
        this.mapContextName = mapContextName.getBytes ( StandardCharsets.ISO_8859_1 );
        if ( log.isTraceEnabled() ) {
            log.trace ( "Created Lazy Map with name:" + mapContextName + ", bytes:" + Arrays.toString ( this.mapContextName ) );
        }
        this.rpcChannel = new RpcChannel ( this.mapContextName, channel, this );
        this.channel.addChannelListener ( this );
        this.channel.addMembershipListener ( this );
        try {
            broadcast ( MapMessage.MSG_INIT, true );
            transferState();
            broadcast ( MapMessage.MSG_START, true );
        } catch ( ChannelException x ) {
            log.warn ( sm.getString ( "abstractReplicatedMap.unableSend.startMessage" ) );
            if ( terminate ) {
                breakdown();
                throw new RuntimeException ( sm.getString ( "abstractReplicatedMap.unableStart" ), x );
            }
        }
        this.state = State.INITIALIZED;
        long complete = System.currentTimeMillis() - start;
        if ( log.isInfoEnabled() )
            log.info ( sm.getString ( "abstractReplicatedMap.init.completed",
                                      mapContextName, Long.toString ( complete ) ) );
    }
    protected void ping ( long timeout ) throws ChannelException {
        MapMessage msg = new MapMessage ( this.mapContextName,
                                          MapMessage.MSG_PING,
                                          false,
                                          null,
                                          null,
                                          null,
                                          channel.getLocalMember ( false ),
                                          null );
        if ( channel.getMembers().length > 0 ) {
            try {
                Response[] resp = rpcChannel.send ( channel.getMembers(),
                                                    msg, RpcChannel.ALL_REPLY,
                                                    ( channelSendOptions ),
                                                    ( int ) accessTimeout );
                for ( int i = 0; i < resp.length; i++ ) {
                    MapMessage mapMsg = ( MapMessage ) resp[i].getMessage();
                    try {
                        mapMsg.deserialize ( getExternalLoaders() );
                        State state = ( State ) mapMsg.getValue();
                        if ( state.isAvailable() ) {
                            memberAlive ( resp[i].getSource() );
                        } else {
                            if ( log.isInfoEnabled() )
                                log.info ( sm.getString ( "abstractReplicatedMap.mapMember.unavailable",
                                                          resp[i].getSource() ) );
                        }
                    } catch ( ClassNotFoundException | IOException e ) {
                        log.error ( sm.getString ( "abstractReplicatedMap.unable.deserialize.MapMessage" ), e );
                    }
                }
            } catch ( ChannelException ce ) {
                FaultyMember[] faultyMembers = ce.getFaultyMembers();
                for ( FaultyMember faultyMember : faultyMembers ) {
                    memberDisappeared ( faultyMember.getMember() );
                }
                throw ce;
            }
        }
        synchronized ( mapMembers ) {
            Member[] members = mapMembers.keySet().toArray ( new Member[mapMembers.size()] );
            long now = System.currentTimeMillis();
            for ( Member member : members ) {
                long access = mapMembers.get ( member ).longValue();
                if ( ( now - access ) > timeout ) {
                    log.warn ( sm.getString ( "abstractReplicatedMap.ping.timeout", member, mapname ) );
                    memberDisappeared ( member );
                }
            }
        }
    }
    protected void memberAlive ( Member member ) {
        mapMemberAdded ( member );
        synchronized ( mapMembers ) {
            mapMembers.put ( member, Long.valueOf ( System.currentTimeMillis() ) );
        }
    }
    protected void broadcast ( int msgtype, boolean rpc ) throws ChannelException {
        Member[] members = channel.getMembers();
        if ( members.length == 0 ) {
            return;
        }
        MapMessage msg = new MapMessage ( this.mapContextName, msgtype,
                                          false, null, null, null, channel.getLocalMember ( false ), null );
        if ( rpc ) {
            Response[] resp = rpcChannel.send ( members, msg,
                                                RpcChannel.FIRST_REPLY, ( channelSendOptions ), rpcTimeout );
            if ( resp.length > 0 ) {
                for ( int i = 0; i < resp.length; i++ ) {
                    mapMemberAdded ( resp[i].getSource() );
                    messageReceived ( resp[i].getMessage(), resp[i].getSource() );
                }
            } else {
                log.warn ( sm.getString ( "abstractReplicatedMap.broadcast.noReplies" ) );
            }
        } else {
            channel.send ( channel.getMembers(), msg, channelSendOptions );
        }
    }
    public void breakdown() {
        this.state = State.DESTROYED;
        if ( this.rpcChannel != null ) {
            this.rpcChannel.breakdown();
        }
        if ( this.channel != null ) {
            try {
                broadcast ( MapMessage.MSG_STOP, false );
            } catch ( Exception ignore ) {}
            this.channel.removeChannelListener ( this );
            this.channel.removeMembershipListener ( this );
        }
        this.rpcChannel = null;
        this.channel = null;
        this.mapMembers.clear();
        innerMap.clear();
        this.stateTransferred = false;
        this.externalLoaders = null;
    }
    @Override
    public void finalize() throws Throwable {
        try {
            breakdown();
        } finally {
            super.finalize();
        }
    }
    @Override
    public int hashCode() {
        return Arrays.hashCode ( this.mapContextName );
    }
    @Override
    public boolean equals ( Object o ) {
        if ( ! ( o instanceof AbstractReplicatedMap ) ) {
            return false;
        }
        if ( ! ( o.getClass().equals ( this.getClass() ) ) ) {
            return false;
        }
        @SuppressWarnings ( "unchecked" )
        AbstractReplicatedMap<K, V> other = ( AbstractReplicatedMap<K, V> ) o;
        return Arrays.equals ( mapContextName, other.mapContextName );
    }
    public Member[] getMapMembers ( HashMap<Member, Long> members ) {
        synchronized ( members ) {
            Member[] result = new Member[members.size()];
            members.keySet().toArray ( result );
            return result;
        }
    }
    public Member[] getMapMembers() {
        return getMapMembers ( this.mapMembers );
    }
    public Member[] getMapMembersExcl ( Member[] exclude ) {
        if ( exclude == null ) {
            return null;
        }
        synchronized ( mapMembers ) {
            @SuppressWarnings ( "unchecked" )
            HashMap<Member, Long> list = ( HashMap<Member, Long> ) mapMembers.clone();
            for ( int i = 0; i < exclude.length; i++ ) {
                list.remove ( exclude[i] );
            }
            return getMapMembers ( list );
        }
    }
    public void replicate ( Object key, boolean complete ) {
        if ( log.isTraceEnabled() ) {
            log.trace ( "Replicate invoked on key:" + key );
        }
        MapEntry<K, V> entry = innerMap.get ( key );
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
            boolean isDirty = rentry != null && rentry.isDirty();
            boolean isAccess = rentry != null && rentry.isAccessReplicate();
            boolean repl = complete || isDirty || isAccess;
            if ( !repl ) {
                if ( log.isTraceEnabled() ) {
                    log.trace ( "Not replicating:" + key + ", no change made" );
                }
                return;
            }
            MapMessage msg = null;
            if ( rentry != null && rentry.isDiffable() && ( isDirty || complete ) ) {
                rentry.lock();
                try {
                    msg = new MapMessage ( mapContextName, getReplicateMessageType(),
                                           true, ( Serializable ) entry.getKey(), null,
                                           rentry.getDiff(),
                                           entry.getPrimary(),
                                           entry.getBackupNodes() );
                    rentry.resetDiff();
                } catch ( IOException x ) {
                    log.error ( sm.getString ( "abstractReplicatedMap.unable.diffObject" ), x );
                } finally {
                    rentry.unlock();
                }
            }
            if ( msg == null && complete ) {
                msg = new MapMessage ( mapContextName, getReplicateMessageType(),
                                       false, ( Serializable ) entry.getKey(),
                                       ( Serializable ) entry.getValue(),
                                       null, entry.getPrimary(), entry.getBackupNodes() );
            }
            if ( msg == null ) {
                msg = new MapMessage ( mapContextName, MapMessage.MSG_ACCESS,
                                       false, ( Serializable ) entry.getKey(), null, null, entry.getPrimary(),
                                       entry.getBackupNodes() );
            }
            try {
                if ( channel != null && entry.getBackupNodes() != null && entry.getBackupNodes().length > 0 ) {
                    if ( rentry != null ) {
                        rentry.setLastTimeReplicated ( System.currentTimeMillis() );
                    }
                    channel.send ( entry.getBackupNodes(), msg, channelSendOptions );
                }
            } catch ( ChannelException x ) {
                log.error ( sm.getString ( "abstractReplicatedMap.unable.replicate" ), x );
            }
        }
    }
    public void replicate ( boolean complete ) {
        Iterator<Map.Entry<K, MapEntry<K, V>>> i = innerMap.entrySet().iterator();
        while ( i.hasNext() ) {
            Map.Entry<?, ?> e = i.next();
            replicate ( e.getKey(), complete );
        }
    }
    public void transferState() {
        try {
            Member[] members = getMapMembers();
            Member backup = members.length > 0 ? ( Member ) members[0] : null;
            if ( backup != null ) {
                MapMessage msg = new MapMessage ( mapContextName, getStateMessageType(), false,
                                                  null, null, null, null, null );
                Response[] resp = rpcChannel.send ( new Member[] {backup}, msg, RpcChannel.FIRST_REPLY, channelSendOptions, rpcTimeout );
                if ( resp.length > 0 ) {
                    synchronized ( stateMutex ) {
                        msg = ( MapMessage ) resp[0].getMessage();
                        msg.deserialize ( getExternalLoaders() );
                        ArrayList<?> list = ( ArrayList<?> ) msg.getValue();
                        for ( int i = 0; i < list.size(); i++ ) {
                            messageReceived ( ( Serializable ) list.get ( i ), resp[0].getSource() );
                        }
                    }
                    stateTransferred = true;
                } else {
                    log.warn ( sm.getString ( "abstractReplicatedMap.transferState.noReplies" ) );
                }
            }
        } catch ( ChannelException x ) {
            log.error ( sm.getString ( "abstractReplicatedMap.unable.transferState" ), x );
        } catch ( IOException x ) {
            log.error ( sm.getString ( "abstractReplicatedMap.unable.transferState" ), x );
        } catch ( ClassNotFoundException x ) {
            log.error ( sm.getString ( "abstractReplicatedMap.unable.transferState" ), x );
        }
    }
    @Override
    public Serializable replyRequest ( Serializable msg, final Member sender ) {
        if ( ! ( msg instanceof MapMessage ) ) {
            return null;
        }
        MapMessage mapmsg = ( MapMessage ) msg;
        if ( mapmsg.getMsgType() == MapMessage.MSG_INIT ) {
            mapmsg.setPrimary ( channel.getLocalMember ( false ) );
            return mapmsg;
        }
        if ( mapmsg.getMsgType() == MapMessage.MSG_START ) {
            mapmsg.setPrimary ( channel.getLocalMember ( false ) );
            mapMemberAdded ( sender );
            return mapmsg;
        }
        if ( mapmsg.getMsgType() == MapMessage.MSG_RETRIEVE_BACKUP ) {
            MapEntry<K, V> entry = innerMap.get ( mapmsg.getKey() );
            if ( entry == null || ( !entry.isSerializable() ) ) {
                return null;
            }
            mapmsg.setValue ( ( Serializable ) entry.getValue() );
            return mapmsg;
        }
        if ( mapmsg.getMsgType() == MapMessage.MSG_STATE || mapmsg.getMsgType() == MapMessage.MSG_STATE_COPY ) {
            synchronized ( stateMutex ) {
                ArrayList<MapMessage> list = new ArrayList<>();
                Iterator<Map.Entry<K, MapEntry<K, V>>> i = innerMap.entrySet().iterator();
                while ( i.hasNext() ) {
                    Map.Entry<?, ?> e = i.next();
                    MapEntry<K, V> entry = innerMap.get ( e.getKey() );
                    if ( entry != null && entry.isSerializable() ) {
                        boolean copy = ( mapmsg.getMsgType() == MapMessage.MSG_STATE_COPY );
                        MapMessage me = new MapMessage ( mapContextName,
                                                         copy ? MapMessage.MSG_COPY : MapMessage.MSG_PROXY,
                                                         false, ( Serializable ) entry.getKey(), copy ? ( Serializable ) entry.getValue() : null, null, entry.getPrimary(), entry.getBackupNodes() );
                        list.add ( me );
                    }
                }
                mapmsg.setValue ( list );
                return mapmsg;
            }
        }
        if ( mapmsg.getMsgType() == MapMessage.MSG_PING ) {
            mapmsg.setValue ( state );
            mapmsg.setPrimary ( channel.getLocalMember ( false ) );
            return mapmsg;
        }
        return null;
    }
    @Override
    public void leftOver ( Serializable msg, Member sender ) {
        if ( ! ( msg instanceof MapMessage ) ) {
            return;
        }
        MapMessage mapmsg = ( MapMessage ) msg;
        try {
            mapmsg.deserialize ( getExternalLoaders() );
            if ( mapmsg.getMsgType() == MapMessage.MSG_START ) {
                mapMemberAdded ( mapmsg.getPrimary() );
            } else if ( mapmsg.getMsgType() == MapMessage.MSG_INIT
                        || mapmsg.getMsgType() == MapMessage.MSG_PING ) {
                memberAlive ( mapmsg.getPrimary() );
            } else {
                if ( log.isInfoEnabled() )
                    log.info ( sm.getString ( "abstractReplicatedMap.leftOver.ignored",
                                              mapmsg.getTypeDesc() ) );
            }
        } catch ( IOException x ) {
            log.error ( sm.getString ( "abstractReplicatedMap.unable.deserialize.MapMessage" ), x );
        } catch ( ClassNotFoundException x ) {
            log.error ( sm.getString ( "abstractReplicatedMap.unable.deserialize.MapMessage" ), x );
        }
    }
    @SuppressWarnings ( "unchecked" )
    @Override
    public void messageReceived ( Serializable msg, Member sender ) {
        if ( ! ( msg instanceof MapMessage ) ) {
            return;
        }
        MapMessage mapmsg = ( MapMessage ) msg;
        if ( log.isTraceEnabled() ) {
            log.trace ( "Map[" + mapname + "] received message:" + mapmsg );
        }
        try {
            mapmsg.deserialize ( getExternalLoaders() );
        } catch ( IOException x ) {
            log.error ( sm.getString ( "abstractReplicatedMap.unable.deserialize.MapMessage" ), x );
            return;
        } catch ( ClassNotFoundException x ) {
            log.error ( sm.getString ( "abstractReplicatedMap.unable.deserialize.MapMessage" ), x );
            return;
        }
        if ( log.isTraceEnabled() ) {
            log.trace ( "Map message received from:" + sender.getName() + " msg:" + mapmsg );
        }
        if ( mapmsg.getMsgType() == MapMessage.MSG_START ) {
            mapMemberAdded ( mapmsg.getPrimary() );
        }
        if ( mapmsg.getMsgType() == MapMessage.MSG_STOP ) {
            memberDisappeared ( mapmsg.getPrimary() );
        }
        if ( mapmsg.getMsgType() == MapMessage.MSG_PROXY ) {
            MapEntry<K, V> entry = innerMap.get ( mapmsg.getKey() );
            if ( entry == null ) {
                entry = new MapEntry<> ( ( K ) mapmsg.getKey(), ( V ) mapmsg.getValue() );
                MapEntry<K, V> old = innerMap.putIfAbsent ( entry.getKey(), entry );
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
        if ( mapmsg.getMsgType() == MapMessage.MSG_REMOVE ) {
            innerMap.remove ( mapmsg.getKey() );
        }
        if ( mapmsg.getMsgType() == MapMessage.MSG_BACKUP || mapmsg.getMsgType() == MapMessage.MSG_COPY ) {
            MapEntry<K, V> entry = innerMap.get ( mapmsg.getKey() );
            if ( entry == null ) {
                entry = new MapEntry<> ( ( K ) mapmsg.getKey(), ( V ) mapmsg.getValue() );
                entry.setBackup ( mapmsg.getMsgType() == MapMessage.MSG_BACKUP );
                entry.setProxy ( false );
                entry.setCopy ( mapmsg.getMsgType() == MapMessage.MSG_COPY );
                entry.setBackupNodes ( mapmsg.getBackupNodes() );
                entry.setPrimary ( mapmsg.getPrimary() );
                if ( mapmsg.getValue() instanceof ReplicatedMapEntry ) {
                    ( ( ReplicatedMapEntry ) mapmsg.getValue() ).setOwner ( getMapOwner() );
                }
            } else {
                entry.setBackup ( mapmsg.getMsgType() == MapMessage.MSG_BACKUP );
                entry.setProxy ( false );
                entry.setCopy ( mapmsg.getMsgType() == MapMessage.MSG_COPY );
                entry.setBackupNodes ( mapmsg.getBackupNodes() );
                entry.setPrimary ( mapmsg.getPrimary() );
                if ( entry.getValue() instanceof ReplicatedMapEntry ) {
                    ReplicatedMapEntry diff = ( ReplicatedMapEntry ) entry.getValue();
                    if ( mapmsg.isDiff() ) {
                        diff.lock();
                        try {
                            diff.applyDiff ( mapmsg.getDiffValue(), 0, mapmsg.getDiffValue().length );
                        } catch ( Exception x ) {
                            log.error ( sm.getString ( "abstractReplicatedMap.unableApply.diff", entry.getKey() ), x );
                        } finally {
                            diff.unlock();
                        }
                    } else {
                        if ( mapmsg.getValue() != null ) {
                            if ( mapmsg.getValue() instanceof ReplicatedMapEntry ) {
                                ReplicatedMapEntry re = ( ReplicatedMapEntry ) mapmsg.getValue();
                                re.setOwner ( getMapOwner() );
                                entry.setValue ( ( V ) re );
                            } else {
                                entry.setValue ( ( V ) mapmsg.getValue() );
                            }
                        } else {
                            ( ( ReplicatedMapEntry ) entry.getValue() ).setOwner ( getMapOwner() );
                        }
                    }
                } else if ( mapmsg.getValue() instanceof ReplicatedMapEntry ) {
                    ReplicatedMapEntry re = ( ReplicatedMapEntry ) mapmsg.getValue();
                    re.setOwner ( getMapOwner() );
                    entry.setValue ( ( V ) re );
                } else {
                    if ( mapmsg.getValue() != null ) {
                        entry.setValue ( ( V ) mapmsg.getValue() );
                    }
                }
            }
            innerMap.put ( entry.getKey(), entry );
        }
        if ( mapmsg.getMsgType() == MapMessage.MSG_ACCESS ) {
            MapEntry<K, V> entry = innerMap.get ( mapmsg.getKey() );
            if ( entry != null ) {
                entry.setBackupNodes ( mapmsg.getBackupNodes() );
                entry.setPrimary ( mapmsg.getPrimary() );
                if ( entry.getValue() instanceof ReplicatedMapEntry ) {
                    ( ( ReplicatedMapEntry ) entry.getValue() ).accessEntry();
                }
            }
        }
        if ( mapmsg.getMsgType() == MapMessage.MSG_NOTIFY_MAPMEMBER ) {
            MapEntry<K, V> entry = innerMap.get ( mapmsg.getKey() );
            if ( entry != null ) {
                entry.setBackupNodes ( mapmsg.getBackupNodes() );
                entry.setPrimary ( mapmsg.getPrimary() );
            }
        }
    }
    @Override
    public boolean accept ( Serializable msg, Member sender ) {
        boolean result = false;
        if ( msg instanceof MapMessage ) {
            if ( log.isTraceEnabled() ) {
                log.trace ( "Map[" + mapname + "] accepting...." + msg );
            }
            result = Arrays.equals ( mapContextName, ( ( MapMessage ) msg ).getMapId() );
            if ( log.isTraceEnabled() ) {
                log.trace ( "Msg[" + mapname + "] accepted[" + result + "]...." + msg );
            }
        }
        return result;
    }
    public void mapMemberAdded ( Member member ) {
        if ( member.equals ( getChannel().getLocalMember ( false ) ) ) {
            return;
        }
        boolean memberAdded = false;
        Member mapMember = getChannel().getMember ( member );
        if ( mapMember == null ) {
            log.warn ( sm.getString ( "abstractReplicatedMap.mapMemberAdded.nullMember", member ) );
            return;
        }
        synchronized ( mapMembers ) {
            if ( !mapMembers.containsKey ( mapMember ) ) {
                if ( log.isInfoEnabled() ) {
                    log.info ( sm.getString ( "abstractReplicatedMap.mapMemberAdded.added", mapMember ) );
                }
                mapMembers.put ( mapMember, Long.valueOf ( System.currentTimeMillis() ) );
                memberAdded = true;
            }
        }
        if ( memberAdded ) {
            synchronized ( stateMutex ) {
                Iterator<Map.Entry<K, MapEntry<K, V>>> i = innerMap.entrySet().iterator();
                while ( i.hasNext() ) {
                    Map.Entry<K, MapEntry<K, V>> e = i.next();
                    MapEntry<K, V> entry = innerMap.get ( e.getKey() );
                    if ( entry == null ) {
                        continue;
                    }
                    if ( entry.isPrimary() && ( entry.getBackupNodes() == null || entry.getBackupNodes().length == 0 ) ) {
                        try {
                            Member[] backup = publishEntryInfo ( entry.getKey(), entry.getValue() );
                            entry.setBackupNodes ( backup );
                            entry.setPrimary ( channel.getLocalMember ( false ) );
                        } catch ( ChannelException x ) {
                            log.error ( sm.getString ( "abstractReplicatedMap.unableSelect.backup" ), x );
                        }
                    }
                }
            }
        }
    }
    public boolean inSet ( Member m, Member[] set ) {
        if ( set == null ) {
            return false;
        }
        boolean result = false;
        for ( int i = 0; i < set.length && ( !result ); i++ )
            if ( m.equals ( set[i] ) ) {
                result = true;
            }
        return result;
    }
    public Member[] excludeFromSet ( Member[] mbrs, Member[] set ) {
        ArrayList<Member> result = new ArrayList<>();
        for ( int i = 0; i < set.length; i++ ) {
            boolean include = true;
            for ( int j = 0; j < mbrs.length && include; j++ )
                if ( mbrs[j].equals ( set[i] ) ) {
                    include = false;
                }
            if ( include ) {
                result.add ( set[i] );
            }
        }
        return result.toArray ( new Member[result.size()] );
    }
    @Override
    public void memberAdded ( Member member ) {
    }
    @Override
    public void memberDisappeared ( Member member ) {
        boolean removed = false;
        synchronized ( mapMembers ) {
            removed = ( mapMembers.remove ( member ) != null );
            if ( !removed ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "Member[" + member + "] disappeared, but was not present in the map." );
                }
                return;
            }
        }
        if ( log.isInfoEnabled() ) {
            log.info ( sm.getString ( "abstractReplicatedMap.member.disappeared", member ) );
        }
        long start = System.currentTimeMillis();
        Iterator<Map.Entry<K, MapEntry<K, V>>> i = innerMap.entrySet().iterator();
        while ( i.hasNext() ) {
            Map.Entry<K, MapEntry<K, V>> e = i.next();
            MapEntry<K, V> entry = innerMap.get ( e.getKey() );
            if ( entry == null ) {
                continue;
            }
            if ( entry.isPrimary() && inSet ( member, entry.getBackupNodes() ) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "[1] Primary choosing a new backup" );
                }
                try {
                    Member[] backup = publishEntryInfo ( entry.getKey(), entry.getValue() );
                    entry.setBackupNodes ( backup );
                    entry.setPrimary ( channel.getLocalMember ( false ) );
                } catch ( ChannelException x ) {
                    log.error ( sm.getString ( "abstractReplicatedMap.unable.relocate", entry.getKey() ), x );
                }
            } else if ( member.equals ( entry.getPrimary() ) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "[2] Primary disappeared" );
                }
                entry.setPrimary ( null );
            }
            if ( entry.isProxy() &&
                    entry.getPrimary() == null &&
                    entry.getBackupNodes() != null &&
                    entry.getBackupNodes().length == 1 &&
                    entry.getBackupNodes() [0].equals ( member ) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "[3] Removing orphaned proxy" );
                }
                i.remove();
            } else if ( entry.getPrimary() == null &&
                        entry.isBackup() &&
                        entry.getBackupNodes() != null &&
                        entry.getBackupNodes().length == 1 &&
                        entry.getBackupNodes() [0].equals ( channel.getLocalMember ( false ) ) ) {
                try {
                    if ( log.isDebugEnabled() ) {
                        log.debug ( "[4] Backup becoming primary" );
                    }
                    entry.setPrimary ( channel.getLocalMember ( false ) );
                    entry.setBackup ( false );
                    entry.setProxy ( false );
                    entry.setCopy ( false );
                    Member[] backup = publishEntryInfo ( entry.getKey(), entry.getValue() );
                    entry.setBackupNodes ( backup );
                    if ( mapOwner != null ) {
                        mapOwner.objectMadePrimary ( entry.getKey(), entry.getValue() );
                    }
                } catch ( ChannelException x ) {
                    log.error ( sm.getString ( "abstractReplicatedMap.unable.relocate", entry.getKey() ), x );
                }
            }
        }
        long complete = System.currentTimeMillis() - start;
        if ( log.isInfoEnabled() ) log.info ( sm.getString ( "abstractReplicatedMap.relocate.complete",
                                                  Long.toString ( complete ) ) );
    }
    public int getNextBackupIndex() {
        int size = mapMembers.size();
        if ( mapMembers.size() == 0 ) {
            return -1;
        }
        int node = currentNode++;
        if ( node >= size ) {
            node = 0;
            currentNode = 0;
        }
        return node;
    }
    public Member getNextBackupNode() {
        Member[] members = getMapMembers();
        int node = getNextBackupIndex();
        if ( members.length == 0 || node == -1 ) {
            return null;
        }
        if ( node >= members.length ) {
            node = 0;
        }
        return members[node];
    }
    protected abstract Member[] publishEntryInfo ( Object key, Object value ) throws ChannelException;
    @Override
    public void heartbeat() {
        try {
            if ( this.state.isAvailable() ) {
                ping ( accessTimeout );
            }
        } catch ( Exception x ) {
            log.error ( sm.getString ( "abstractReplicatedMap.heartbeat.failed" ), x );
        }
    }
    @Override
    public V remove ( Object key ) {
        return remove ( key, true );
    }
    public V remove ( Object key, boolean notify ) {
        MapEntry<K, V> entry = innerMap.remove ( key );
        try {
            if ( getMapMembers().length > 0 && notify ) {
                MapMessage msg = new MapMessage ( getMapContextName(), MapMessage.MSG_REMOVE, false, ( Serializable ) key, null, null, null, null );
                getChannel().send ( getMapMembers(), msg, getChannelSendOptions() );
            }
        } catch ( ChannelException x ) {
            log.error ( sm.getString ( "abstractReplicatedMap.unable.remove" ), x );
        }
        return entry != null ? entry.getValue() : null;
    }
    public MapEntry<K, V> getInternal ( Object key ) {
        return innerMap.get ( key );
    }
    @SuppressWarnings ( "unchecked" )
    @Override
    public V get ( Object key ) {
        MapEntry<K, V> entry = innerMap.get ( key );
        if ( log.isTraceEnabled() ) {
            log.trace ( "Requesting id:" + key + " entry:" + entry );
        }
        if ( entry == null ) {
            return null;
        }
        if ( !entry.isPrimary() ) {
            try {
                Member[] backup = null;
                MapMessage msg = null;
                if ( entry.isBackup() ) {
                    backup = publishEntryInfo ( key, entry.getValue() );
                } else if ( entry.isProxy() ) {
                    msg = new MapMessage ( getMapContextName(), MapMessage.MSG_RETRIEVE_BACKUP, false,
                                           ( Serializable ) key, null, null, null, null );
                    Response[] resp = getRpcChannel().send ( entry.getBackupNodes(), msg, RpcChannel.FIRST_REPLY, getChannelSendOptions(), getRpcTimeout() );
                    if ( resp == null || resp.length == 0 || resp[0].getMessage() == null ) {
                        log.warn ( sm.getString ( "abstractReplicatedMap.unable.retrieve", key ) );
                        return null;
                    }
                    msg = ( MapMessage ) resp[0].getMessage();
                    msg.deserialize ( getExternalLoaders() );
                    backup = entry.getBackupNodes();
                    if ( msg.getValue() != null ) {
                        entry.setValue ( ( V ) msg.getValue() );
                    }
                    msg = new MapMessage ( getMapContextName(), MapMessage.MSG_NOTIFY_MAPMEMBER, false,
                                           ( Serializable ) entry.getKey(), null, null, channel.getLocalMember ( false ), backup );
                    if ( backup != null && backup.length > 0 ) {
                        getChannel().send ( backup, msg, getChannelSendOptions() );
                    }
                    msg = new MapMessage ( getMapContextName(), MapMessage.MSG_PROXY, false, ( Serializable ) key, null, null, channel.getLocalMember ( false ), backup );
                    Member[] dest = getMapMembersExcl ( backup );
                    if ( dest != null && dest.length > 0 ) {
                        getChannel().send ( dest, msg, getChannelSendOptions() );
                    }
                    if ( entry.getValue() instanceof ReplicatedMapEntry ) {
                        ReplicatedMapEntry val = ( ReplicatedMapEntry ) entry.getValue();
                        val.setOwner ( getMapOwner() );
                    }
                } else if ( entry.isCopy() ) {
                    backup = getMapMembers();
                    if ( backup.length > 0 ) {
                        msg = new MapMessage ( getMapContextName(), MapMessage.MSG_NOTIFY_MAPMEMBER, false,
                                               ( Serializable ) key, null, null, channel.getLocalMember ( false ), backup );
                        getChannel().send ( backup, msg, getChannelSendOptions() );
                    }
                }
                entry.setPrimary ( channel.getLocalMember ( false ) );
                entry.setBackupNodes ( backup );
                entry.setBackup ( false );
                entry.setProxy ( false );
                entry.setCopy ( false );
                if ( getMapOwner() != null ) {
                    getMapOwner().objectMadePrimary ( key, entry.getValue() );
                }
            } catch ( Exception x ) {
                log.error ( sm.getString ( "abstractReplicatedMap.unable.get" ), x );
                return null;
            }
        }
        if ( log.isTraceEnabled() ) {
            log.trace ( "Requesting id:" + key + " result:" + entry.getValue() );
        }
        return entry.getValue();
    }
    protected void printMap ( String header ) {
        try {
            System.out.println ( "\nDEBUG MAP:" + header );
            System.out.println ( "Map[" +
                                 new String ( mapContextName, StandardCharsets.ISO_8859_1 ) +
                                 ", Map Size:" + innerMap.size() );
            Member[] mbrs = getMapMembers();
            for ( int i = 0; i < mbrs.length; i++ ) {
                System.out.println ( "Mbr[" + ( i + 1 ) + "=" + mbrs[i].getName() );
            }
            Iterator<Map.Entry<K, MapEntry<K, V>>> i = innerMap.entrySet().iterator();
            int cnt = 0;
            while ( i.hasNext() ) {
                Map.Entry<?, ?> e = i.next();
                System.out.println ( ( ++cnt ) + ". " + innerMap.get ( e.getKey() ) );
            }
            System.out.println ( "EndMap]\n\n" );
        } catch ( Exception ignore ) {
            ignore.printStackTrace();
        }
    }
    @Override
    public boolean containsKey ( Object key ) {
        return innerMap.containsKey ( key );
    }
    @Override
    public V put ( K key, V value ) {
        return put ( key, value, true );
    }
    public V put ( K key, V value, boolean notify ) {
        MapEntry<K, V> entry = new MapEntry<> ( key, value );
        entry.setBackup ( false );
        entry.setProxy ( false );
        entry.setCopy ( false );
        entry.setPrimary ( channel.getLocalMember ( false ) );
        V old = null;
        if ( containsKey ( key ) ) {
            old = remove ( key );
        }
        try {
            if ( notify ) {
                Member[] backup = publishEntryInfo ( key, value );
                entry.setBackupNodes ( backup );
            }
        } catch ( ChannelException x ) {
            log.error ( sm.getString ( "abstractReplicatedMap.unable.put" ), x );
        }
        innerMap.put ( key, entry );
        return old;
    }
    @Override
    public void putAll ( Map<? extends K, ? extends V> m ) {
        Iterator<?> i = m.entrySet().iterator();
        while ( i.hasNext() ) {
            @SuppressWarnings ( "unchecked" )
            Map.Entry<K, V> entry = ( Map.Entry<K, V> ) i.next();
            put ( entry.getKey(), entry.getValue() );
        }
    }
    @Override
    public void clear() {
        clear ( true );
    }
    public void clear ( boolean notify ) {
        if ( notify ) {
            Iterator<K> keys = keySet().iterator();
            while ( keys.hasNext() ) {
                remove ( keys.next() );
            }
        } else {
            innerMap.clear();
        }
    }
    @Override
    public boolean containsValue ( Object value ) {
        Objects.requireNonNull ( value );
        Iterator<Map.Entry<K, MapEntry<K, V>>> i = innerMap.entrySet().iterator();
        while ( i.hasNext() ) {
            Map.Entry<K, MapEntry<K, V>> e = i.next();
            MapEntry<K, V> entry = innerMap.get ( e.getKey() );
            if ( entry != null && entry.isActive() && value.equals ( entry.getValue() ) ) {
                return true;
            }
        }
        return false;
    }
    @Override
    public Object clone() {
        throw new UnsupportedOperationException ( sm.getString ( "abstractReplicatedMap.unsupport.operation" ) );
    }
    public Set<Map.Entry<K, MapEntry<K, V>>> entrySetFull() {
        return innerMap.entrySet();
    }
    public Set<K> keySetFull() {
        return innerMap.keySet();
    }
    public int sizeFull() {
        return innerMap.size();
    }
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        LinkedHashSet<Map.Entry<K, V>> set = new LinkedHashSet<> ( innerMap.size() );
        Iterator<Map.Entry<K, MapEntry<K, V>>> i = innerMap.entrySet().iterator();
        while ( i.hasNext() ) {
            Map.Entry<?, ?> e = i.next();
            Object key = e.getKey();
            MapEntry<K, V> entry = innerMap.get ( key );
            if ( entry != null && entry.isActive() ) {
                set.add ( entry );
            }
        }
        return Collections.unmodifiableSet ( set );
    }
    @Override
    public Set<K> keySet() {
        LinkedHashSet<K> set = new LinkedHashSet<> ( innerMap.size() );
        Iterator<Map.Entry<K, MapEntry<K, V>>> i = innerMap.entrySet().iterator();
        while ( i.hasNext() ) {
            Map.Entry<K, MapEntry<K, V>> e = i.next();
            K key = e.getKey();
            MapEntry<K, V> entry = innerMap.get ( key );
            if ( entry != null && entry.isActive() ) {
                set.add ( key );
            }
        }
        return Collections.unmodifiableSet ( set );
    }
    @Override
    public int size() {
        int counter = 0;
        Iterator<Map.Entry<K, MapEntry<K, V>>> it = innerMap.entrySet().iterator();
        while ( it != null && it.hasNext() ) {
            Map.Entry<?, ?> e = it.next();
            if ( e != null ) {
                MapEntry<K, V> entry = innerMap.get ( e.getKey() );
                if ( entry != null && entry.isActive() && entry.getValue() != null ) {
                    counter++;
                }
            }
        }
        return counter;
    }
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    @Override
    public Collection<V> values() {
        ArrayList<V> values = new ArrayList<>();
        Iterator<Map.Entry<K, MapEntry<K, V>>> i = innerMap.entrySet().iterator();
        while ( i.hasNext() ) {
            Map.Entry<K, MapEntry<K, V>> e = i.next();
            MapEntry<K, V> entry = innerMap.get ( e.getKey() );
            if ( entry != null && entry.isActive() && entry.getValue() != null ) {
                values.add ( entry.getValue() );
            }
        }
        return Collections.unmodifiableCollection ( values );
    }
    public static class MapEntry<K, V> implements Map.Entry<K, V> {
        private boolean backup;
        private boolean proxy;
        private boolean copy;
        private Member[] backupNodes;
        private Member primary;
        private K key;
        private V value;
        public MapEntry ( K key, V value ) {
            setKey ( key );
            setValue ( value );
        }
        public boolean isKeySerializable() {
            return ( key == null ) || ( key instanceof Serializable );
        }
        public boolean isValueSerializable() {
            return ( value == null ) || ( value instanceof Serializable );
        }
        public boolean isSerializable() {
            return isKeySerializable() && isValueSerializable();
        }
        public boolean isBackup() {
            return backup;
        }
        public void setBackup ( boolean backup ) {
            this.backup = backup;
        }
        public boolean isProxy() {
            return proxy;
        }
        public boolean isPrimary() {
            return ( !proxy && !backup && !copy );
        }
        public boolean isActive() {
            return !proxy;
        }
        public void setProxy ( boolean proxy ) {
            this.proxy = proxy;
        }
        public boolean isCopy() {
            return copy;
        }
        public void setCopy ( boolean copy ) {
            this.copy = copy;
        }
        public boolean isDiffable() {
            return ( value instanceof ReplicatedMapEntry ) &&
                   ( ( ReplicatedMapEntry ) value ).isDiffable();
        }
        public void setBackupNodes ( Member[] nodes ) {
            this.backupNodes = nodes;
        }
        public Member[] getBackupNodes() {
            return backupNodes;
        }
        public void setPrimary ( Member m ) {
            primary = m;
        }
        public Member getPrimary() {
            return primary;
        }
        @Override
        public V getValue() {
            return value;
        }
        @Override
        public V setValue ( V value ) {
            V old = this.value;
            this.value = value;
            return old;
        }
        @Override
        public K getKey() {
            return key;
        }
        public K setKey ( K key ) {
            K old = this.key;
            this.key = key;
            return old;
        }
        @Override
        public int hashCode() {
            return key.hashCode();
        }
        @Override
        public boolean equals ( Object o ) {
            return key.equals ( o );
        }
        @SuppressWarnings ( "unchecked" )
        public void apply ( byte[] data, int offset, int length, boolean diff ) throws IOException, ClassNotFoundException {
            if ( isDiffable() && diff ) {
                ReplicatedMapEntry rentry = ( ReplicatedMapEntry ) value;
                rentry.lock();
                try {
                    rentry.applyDiff ( data, offset, length );
                } finally {
                    rentry.unlock();
                }
            } else if ( length == 0 ) {
                value = null;
                proxy = true;
            } else {
                value = ( V ) XByteBuffer.deserialize ( data, offset, length );
            }
        }
        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder ( "MapEntry[key:" );
            buf.append ( getKey() ).append ( "; " );
            buf.append ( "value:" ).append ( getValue() ).append ( "; " );
            buf.append ( "primary:" ).append ( isPrimary() ).append ( "; " );
            buf.append ( "backup:" ).append ( isBackup() ).append ( "; " );
            buf.append ( "proxy:" ).append ( isProxy() ).append ( ";]" );
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
            StringBuilder buf = new StringBuilder ( "MapMessage[context=" );
            buf.append ( new String ( mapId ) );
            buf.append ( "; type=" );
            buf.append ( getTypeDesc() );
            buf.append ( "; key=" );
            buf.append ( key );
            buf.append ( "; value=" );
            buf.append ( value );
            return buf.toString();
        }
        public String getTypeDesc() {
            switch ( msgtype ) {
            case MSG_BACKUP:
                return "MSG_BACKUP";
            case MSG_RETRIEVE_BACKUP:
                return "MSG_RETRIEVE_BACKUP";
            case MSG_PROXY:
                return "MSG_PROXY";
            case MSG_REMOVE:
                return "MSG_REMOVE";
            case MSG_STATE:
                return "MSG_STATE";
            case MSG_START:
                return "MSG_START";
            case MSG_STOP:
                return "MSG_STOP";
            case MSG_INIT:
                return "MSG_INIT";
            case MSG_STATE_COPY:
                return "MSG_STATE_COPY";
            case MSG_COPY:
                return "MSG_COPY";
            case MSG_ACCESS:
                return "MSG_ACCESS";
            case MSG_NOTIFY_MAPMEMBER:
                return "MSG_NOTIFY_MAPMEMBER";
            case MSG_PING:
                return "MSG_PING";
            default :
                return "UNKNOWN";
            }
        }
        public MapMessage ( byte[] mapId, int msgtype, boolean diff,
                            Serializable key, Serializable value,
                            byte[] diffvalue, Member primary, Member[] nodes )  {
            this.mapId = mapId;
            this.msgtype = msgtype;
            this.diff = diff;
            this.key = key;
            this.value = value;
            this.diffvalue = diffvalue;
            this.nodes = nodes;
            this.primary = primary;
            setValue ( value );
            setKey ( key );
        }
        public void deserialize ( ClassLoader[] cls ) throws IOException, ClassNotFoundException {
            key ( cls );
            value ( cls );
        }
        public int getMsgType() {
            return msgtype;
        }
        public boolean isDiff() {
            return diff;
        }
        public Serializable getKey() {
            try {
                return key ( null );
            } catch ( Exception x ) {
                throw new RuntimeException ( sm.getString ( "mapMessage.deserialize.error.key" ), x );
            }
        }
        public Serializable key ( ClassLoader[] cls ) throws IOException, ClassNotFoundException {
            if ( key != null ) {
                return key;
            }
            if ( keydata == null || keydata.length == 0 ) {
                return null;
            }
            key = XByteBuffer.deserialize ( keydata, 0, keydata.length, cls );
            keydata = null;
            return key;
        }
        public byte[] getKeyData() {
            return keydata;
        }
        public Serializable getValue() {
            try {
                return value ( null );
            } catch ( Exception x ) {
                throw new RuntimeException ( sm.getString ( "mapMessage.deserialize.error.value" ), x );
            }
        }
        public Serializable value ( ClassLoader[] cls ) throws IOException, ClassNotFoundException  {
            if ( value != null ) {
                return value;
            }
            if ( valuedata == null || valuedata.length == 0 ) {
                return null;
            }
            value = XByteBuffer.deserialize ( valuedata, 0, valuedata.length, cls );
            valuedata = null;
            return value;
        }
        public byte[] getValueData() {
            return valuedata;
        }
        public byte[] getDiffValue() {
            return diffvalue;
        }
        public Member[] getBackupNodes() {
            return nodes;
        }
        public Member getPrimary() {
            return primary;
        }
        private void setPrimary ( Member m ) {
            primary = m;
        }
        public byte[] getMapId() {
            return mapId;
        }
        public void setValue ( Serializable value ) {
            try {
                if ( value != null ) {
                    valuedata = XByteBuffer.serialize ( value );
                }
                this.value = value;
            } catch ( IOException x ) {
                throw new RuntimeException ( x );
            }
        }
        public void setKey ( Serializable key ) {
            try {
                if ( key != null ) {
                    keydata = XByteBuffer.serialize ( key );
                }
                this.key = key;
            } catch ( IOException x ) {
                throw new RuntimeException ( x );
            }
        }
        @Override
        public Object clone() {
            MapMessage msg = new MapMessage ( this.mapId, this.msgtype, this.diff, this.key, this.value, this.diffvalue, this.primary, this.nodes );
            msg.keydata = this.keydata;
            msg.valuedata = this.valuedata;
            return msg;
        }
    }
    public Channel getChannel() {
        return channel;
    }
    public byte[] getMapContextName() {
        return mapContextName;
    }
    public RpcChannel getRpcChannel() {
        return rpcChannel;
    }
    public long getRpcTimeout() {
        return rpcTimeout;
    }
    public Object getStateMutex() {
        return stateMutex;
    }
    public boolean isStateTransferred() {
        return stateTransferred;
    }
    public MapOwner getMapOwner() {
        return mapOwner;
    }
    public ClassLoader[] getExternalLoaders() {
        return externalLoaders;
    }
    public int getChannelSendOptions() {
        return channelSendOptions;
    }
    public long getAccessTimeout() {
        return accessTimeout;
    }
    public void setMapOwner ( MapOwner mapOwner ) {
        this.mapOwner = mapOwner;
    }
    public void setExternalLoaders ( ClassLoader[] externalLoaders ) {
        this.externalLoaders = externalLoaders;
    }
    public void setChannelSendOptions ( int channelSendOptions ) {
        this.channelSendOptions = channelSendOptions;
    }
    public void setAccessTimeout ( long accessTimeout ) {
        this.accessTimeout = accessTimeout;
    }
    private static enum State {
        NEW ( false ),
        INITIALIZED ( true ),
        DESTROYED ( false );
        private final boolean available;
        private State ( boolean available ) {
            this.available = available;
        }
        public boolean isAvailable() {
            return available;
        }
    }
}
