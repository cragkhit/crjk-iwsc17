package org.apache.catalina.tribes.tipis;
import java.util.Iterator;
import java.util.Map;
import org.apache.catalina.tribes.ChannelException;
import java.util.Arrays;
import org.apache.catalina.tribes.RemoteProcessException;
import java.util.ArrayList;
import java.io.Serializable;
import org.apache.catalina.tribes.Member;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.tribes.Channel;
import org.apache.juli.logging.Log;
public class ReplicatedMap<K, V> extends AbstractReplicatedMap<K, V> {
    private static final long serialVersionUID = 1L;
    private final Log log;
    public ReplicatedMap ( final MapOwner owner, final Channel channel, final long timeout, final String mapContextName, final int initialCapacity, final float loadFactor, final ClassLoader[] cls ) {
        super ( owner, channel, timeout, mapContextName, initialCapacity, loadFactor, 2, cls, true );
        this.log = LogFactory.getLog ( ReplicatedMap.class );
    }
    public ReplicatedMap ( final MapOwner owner, final Channel channel, final long timeout, final String mapContextName, final int initialCapacity, final ClassLoader[] cls ) {
        super ( owner, channel, timeout, mapContextName, initialCapacity, 0.75f, 2, cls, true );
        this.log = LogFactory.getLog ( ReplicatedMap.class );
    }
    public ReplicatedMap ( final MapOwner owner, final Channel channel, final long timeout, final String mapContextName, final ClassLoader[] cls ) {
        super ( owner, channel, timeout, mapContextName, 16, 0.75f, 2, cls, true );
        this.log = LogFactory.getLog ( ReplicatedMap.class );
    }
    public ReplicatedMap ( final MapOwner owner, final Channel channel, final long timeout, final String mapContextName, final ClassLoader[] cls, final boolean terminate ) {
        super ( owner, channel, timeout, mapContextName, 16, 0.75f, 2, cls, terminate );
        this.log = LogFactory.getLog ( ReplicatedMap.class );
    }
    @Override
    protected int getStateMessageType() {
        return 10;
    }
    @Override
    protected int getReplicateMessageType() {
        return 9;
    }
    @Override
    protected Member[] publishEntryInfo ( final Object key, final Object value ) throws ChannelException {
        if ( ! ( key instanceof Serializable ) || ! ( value instanceof Serializable ) ) {
            return new Member[0];
        }
        Member[] backup = this.getMapMembers();
        if ( backup == null || backup.length == 0 ) {
            return null;
        }
        try {
            final MapMessage msg = new MapMessage ( this.getMapContextName(), 9, false, ( Serializable ) key, ( Serializable ) value, null, this.channel.getLocalMember ( false ), backup );
            this.getChannel().send ( backup, msg, this.getChannelSendOptions() );
        } catch ( ChannelException e ) {
            final ChannelException.FaultyMember[] faultyMembers = e.getFaultyMembers();
            if ( faultyMembers.length == 0 ) {
                throw e;
            }
            final ArrayList<Member> faulty = new ArrayList<Member>();
            for ( final ChannelException.FaultyMember faultyMember : faultyMembers ) {
                if ( ! ( faultyMember.getCause() instanceof RemoteProcessException ) ) {
                    faulty.add ( faultyMember.getMember() );
                }
            }
            final Member[] realFaultyMembers = faulty.toArray ( new Member[faulty.size()] );
            if ( realFaultyMembers.length != 0 ) {
                backup = this.excludeFromSet ( realFaultyMembers, backup );
                if ( backup.length == 0 ) {
                    throw e;
                }
                if ( this.log.isWarnEnabled() ) {
                    this.log.warn ( ReplicatedMap.sm.getString ( "replicatedMap.unableReplicate.completely", key, Arrays.toString ( backup ), Arrays.toString ( realFaultyMembers ) ), e );
                }
            }
        }
        return backup;
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
            this.log.info ( ReplicatedMap.sm.getString ( "replicatedMap.member.disappeared", member ) );
        }
        final long start = System.currentTimeMillis();
        for ( final Map.Entry<K, MapEntry<K, V>> e : this.innerMap.entrySet() ) {
            final MapEntry<K, V> entry = this.innerMap.get ( e.getKey() );
            if ( entry == null ) {
                continue;
            }
            if ( entry.isPrimary() ) {
                try {
                    final Member[] backup = this.getMapMembers();
                    if ( backup.length > 0 ) {
                        final MapMessage msg = new MapMessage ( this.getMapContextName(), 12, false, ( Serializable ) entry.getKey(), null, null, this.channel.getLocalMember ( false ), backup );
                        this.getChannel().send ( backup, msg, this.getChannelSendOptions() );
                    }
                    entry.setBackupNodes ( backup );
                    entry.setPrimary ( this.channel.getLocalMember ( false ) );
                } catch ( ChannelException x ) {
                    this.log.error ( ReplicatedMap.sm.getString ( "replicatedMap.unable.relocate", entry.getKey() ), x );
                }
            } else if ( member.equals ( entry.getPrimary() ) ) {
                entry.setPrimary ( null );
            }
            if ( entry.getPrimary() != null || !entry.isCopy() || entry.getBackupNodes() == null || entry.getBackupNodes().length <= 0 || !entry.getBackupNodes() [0].equals ( this.channel.getLocalMember ( false ) ) ) {
                continue;
            }
            try {
                entry.setPrimary ( this.channel.getLocalMember ( false ) );
                entry.setBackup ( false );
                entry.setProxy ( false );
                entry.setCopy ( false );
                final Member[] backup = this.getMapMembers();
                if ( backup.length > 0 ) {
                    final MapMessage msg = new MapMessage ( this.getMapContextName(), 12, false, ( Serializable ) entry.getKey(), null, null, this.channel.getLocalMember ( false ), backup );
                    this.getChannel().send ( backup, msg, this.getChannelSendOptions() );
                }
                entry.setBackupNodes ( backup );
                if ( this.mapOwner == null ) {
                    continue;
                }
                this.mapOwner.objectMadePrimary ( entry.getKey(), entry.getValue() );
            } catch ( ChannelException x ) {
                this.log.error ( ReplicatedMap.sm.getString ( "replicatedMap.unable.relocate", entry.getKey() ), x );
            }
        }
        final long complete = System.currentTimeMillis() - start;
        if ( this.log.isInfoEnabled() ) {
            this.log.info ( ReplicatedMap.sm.getString ( "replicatedMap.relocate.complete", Long.toString ( complete ) ) );
        }
    }
    @Override
    public void mapMemberAdded ( final Member member ) {
        if ( member.equals ( this.getChannel().getLocalMember ( false ) ) ) {
            return;
        }
        boolean memberAdded = false;
        synchronized ( this.mapMembers ) {
            if ( !this.mapMembers.containsKey ( member ) ) {
                this.mapMembers.put ( member, System.currentTimeMillis() );
                memberAdded = true;
            }
        }
        if ( memberAdded ) {
            synchronized ( this.stateMutex ) {
                final Member[] backup = this.getMapMembers();
                for ( final Map.Entry<K, MapEntry<K, V>> e : this.innerMap.entrySet() ) {
                    final MapEntry<K, V> entry = this.innerMap.get ( e.getKey() );
                    if ( entry == null ) {
                        continue;
                    }
                    if ( !entry.isPrimary() || this.inSet ( member, entry.getBackupNodes() ) ) {
                        continue;
                    }
                    entry.setBackupNodes ( backup );
                }
            }
        }
    }
}
