package org.apache.catalina.tribes.tipis;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.ChannelException.FaultyMember;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.RemoteProcessException;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class ReplicatedMap<K, V> extends AbstractReplicatedMap<K, V> {
    private static final long serialVersionUID = 1L;
    private final Log log = LogFactory.getLog ( ReplicatedMap.class );
    public ReplicatedMap ( MapOwner owner, Channel channel, long timeout, String mapContextName, int initialCapacity, float loadFactor, ClassLoader[] cls ) {
        super ( owner, channel, timeout, mapContextName, initialCapacity, loadFactor, Channel.SEND_OPTIONS_DEFAULT, cls, true );
    }
    public ReplicatedMap ( MapOwner owner, Channel channel, long timeout, String mapContextName, int initialCapacity, ClassLoader[] cls ) {
        super ( owner, channel, timeout, mapContextName, initialCapacity, AbstractReplicatedMap.DEFAULT_LOAD_FACTOR, Channel.SEND_OPTIONS_DEFAULT, cls, true );
    }
    public ReplicatedMap ( MapOwner owner, Channel channel, long timeout, String mapContextName, ClassLoader[] cls ) {
        super ( owner, channel, timeout, mapContextName, AbstractReplicatedMap.DEFAULT_INITIAL_CAPACITY, AbstractReplicatedMap.DEFAULT_LOAD_FACTOR, Channel.SEND_OPTIONS_DEFAULT, cls, true );
    }
    public ReplicatedMap ( MapOwner owner, Channel channel, long timeout, String mapContextName, ClassLoader[] cls, boolean terminate ) {
        super ( owner, channel, timeout, mapContextName, AbstractReplicatedMap.DEFAULT_INITIAL_CAPACITY,
                AbstractReplicatedMap.DEFAULT_LOAD_FACTOR, Channel.SEND_OPTIONS_DEFAULT, cls, terminate );
    }
    @Override
    protected int getStateMessageType() {
        return AbstractReplicatedMap.MapMessage.MSG_STATE_COPY;
    }
    @Override
    protected int getReplicateMessageType() {
        return AbstractReplicatedMap.MapMessage.MSG_COPY;
    }
    @Override
    protected Member[] publishEntryInfo ( Object key, Object value ) throws ChannelException {
        if ( ! ( key instanceof Serializable && value instanceof Serializable ) ) {
            return new Member[0];
        }
        Member[] backup = getMapMembers();
        if ( backup == null || backup.length == 0 ) {
            return null;
        }
        try {
            MapMessage msg = new MapMessage ( getMapContextName(), MapMessage.MSG_COPY, false,
                                              ( Serializable ) key, ( Serializable ) value, null, channel.getLocalMember ( false ), backup );
            getChannel().send ( backup, msg, getChannelSendOptions() );
        } catch ( ChannelException e ) {
            FaultyMember[] faultyMembers = e.getFaultyMembers();
            if ( faultyMembers.length == 0 ) {
                throw e;
            }
            ArrayList<Member> faulty = new ArrayList<>();
            for ( FaultyMember faultyMember : faultyMembers ) {
                if ( ! ( faultyMember.getCause() instanceof RemoteProcessException ) ) {
                    faulty.add ( faultyMember.getMember() );
                }
            }
            Member[] realFaultyMembers = faulty.toArray ( new Member[faulty.size()] );
            if ( realFaultyMembers.length != 0 ) {
                backup = excludeFromSet ( realFaultyMembers, backup );
                if ( backup.length == 0 ) {
                    throw e;
                } else {
                    if ( log.isWarnEnabled() ) {
                        log.warn ( sm.getString ( "replicatedMap.unableReplicate.completely", key,
                                                  Arrays.toString ( backup ), Arrays.toString ( realFaultyMembers ) ), e );
                    }
                }
            }
        }
        return backup;
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
            log.info ( sm.getString ( "replicatedMap.member.disappeared", member ) );
        }
        long start = System.currentTimeMillis();
        Iterator<Map.Entry<K, MapEntry<K, V>>> i = innerMap.entrySet().iterator();
        while ( i.hasNext() ) {
            Map.Entry<K, MapEntry<K, V>> e = i.next();
            MapEntry<K, V> entry = innerMap.get ( e.getKey() );
            if ( entry == null ) {
                continue;
            }
            if ( entry.isPrimary() ) {
                try {
                    Member[] backup = getMapMembers();
                    if ( backup.length > 0 ) {
                        MapMessage msg = new MapMessage ( getMapContextName(), MapMessage.MSG_NOTIFY_MAPMEMBER, false,
                                                          ( Serializable ) entry.getKey(), null, null, channel.getLocalMember ( false ), backup );
                        getChannel().send ( backup, msg, getChannelSendOptions() );
                    }
                    entry.setBackupNodes ( backup );
                    entry.setPrimary ( channel.getLocalMember ( false ) );
                } catch ( ChannelException x ) {
                    log.error ( sm.getString ( "replicatedMap.unable.relocate", entry.getKey() ), x );
                }
            } else if ( member.equals ( entry.getPrimary() ) ) {
                entry.setPrimary ( null );
            }
            if ( entry.getPrimary() == null &&
                    entry.isCopy() &&
                    entry.getBackupNodes() != null &&
                    entry.getBackupNodes().length > 0 &&
                    entry.getBackupNodes() [0].equals ( channel.getLocalMember ( false ) ) ) {
                try {
                    entry.setPrimary ( channel.getLocalMember ( false ) );
                    entry.setBackup ( false );
                    entry.setProxy ( false );
                    entry.setCopy ( false );
                    Member[] backup = getMapMembers();
                    if ( backup.length > 0 ) {
                        MapMessage msg = new MapMessage ( getMapContextName(), MapMessage.MSG_NOTIFY_MAPMEMBER, false,
                                                          ( Serializable ) entry.getKey(), null, null, channel.getLocalMember ( false ), backup );
                        getChannel().send ( backup, msg, getChannelSendOptions() );
                    }
                    entry.setBackupNodes ( backup );
                    if ( mapOwner != null ) {
                        mapOwner.objectMadePrimary ( entry.getKey(), entry.getValue() );
                    }
                } catch ( ChannelException x ) {
                    log.error ( sm.getString ( "replicatedMap.unable.relocate", entry.getKey() ), x );
                }
            }
        }
        long complete = System.currentTimeMillis() - start;
        if ( log.isInfoEnabled() ) log.info ( sm.getString ( "replicatedMap.relocate.complete",
                                                  Long.toString ( complete ) ) );
    }
    @Override
    public void mapMemberAdded ( Member member ) {
        if ( member.equals ( getChannel().getLocalMember ( false ) ) ) {
            return;
        }
        boolean memberAdded = false;
        synchronized ( mapMembers ) {
            if ( !mapMembers.containsKey ( member ) ) {
                mapMembers.put ( member, Long.valueOf ( System.currentTimeMillis() ) );
                memberAdded = true;
            }
        }
        if ( memberAdded ) {
            synchronized ( stateMutex ) {
                Member[] backup = getMapMembers();
                Iterator<Map.Entry<K, MapEntry<K, V>>> i = innerMap.entrySet().iterator();
                while ( i.hasNext() ) {
                    Map.Entry<K, MapEntry<K, V>> e = i.next();
                    MapEntry<K, V> entry = innerMap.get ( e.getKey() );
                    if ( entry == null ) {
                        continue;
                    }
                    if ( entry.isPrimary() && !inSet ( member, entry.getBackupNodes() ) ) {
                        entry.setBackupNodes ( backup );
                    }
                }
            }
        }
    }
}
