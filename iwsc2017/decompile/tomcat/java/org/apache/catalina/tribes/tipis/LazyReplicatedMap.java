package org.apache.catalina.tribes.tipis;
import org.apache.catalina.tribes.UniqueId;
import org.apache.catalina.tribes.util.Arrays;
import org.apache.catalina.tribes.ChannelException;
import java.io.Serializable;
import org.apache.catalina.tribes.Member;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.tribes.Channel;
import org.apache.juli.logging.Log;
public class LazyReplicatedMap<K, V> extends AbstractReplicatedMap<K, V> {
    private static final long serialVersionUID = 1L;
    private final Log log;
    public LazyReplicatedMap ( final MapOwner owner, final Channel channel, final long timeout, final String mapContextName, final int initialCapacity, final float loadFactor, final ClassLoader[] cls ) {
        super ( owner, channel, timeout, mapContextName, initialCapacity, loadFactor, 2, cls, true );
        this.log = LogFactory.getLog ( LazyReplicatedMap.class );
    }
    public LazyReplicatedMap ( final MapOwner owner, final Channel channel, final long timeout, final String mapContextName, final int initialCapacity, final ClassLoader[] cls ) {
        super ( owner, channel, timeout, mapContextName, initialCapacity, 0.75f, 2, cls, true );
        this.log = LogFactory.getLog ( LazyReplicatedMap.class );
    }
    public LazyReplicatedMap ( final MapOwner owner, final Channel channel, final long timeout, final String mapContextName, final ClassLoader[] cls ) {
        super ( owner, channel, timeout, mapContextName, 16, 0.75f, 2, cls, true );
        this.log = LogFactory.getLog ( LazyReplicatedMap.class );
    }
    public LazyReplicatedMap ( final MapOwner owner, final Channel channel, final long timeout, final String mapContextName, final ClassLoader[] cls, final boolean terminate ) {
        super ( owner, channel, timeout, mapContextName, 16, 0.75f, 2, cls, terminate );
        this.log = LogFactory.getLog ( LazyReplicatedMap.class );
    }
    @Override
    protected int getStateMessageType() {
        return 5;
    }
    @Override
    protected int getReplicateMessageType() {
        return 1;
    }
    @Override
    protected Member[] publishEntryInfo ( final Object key, final Object value ) throws ChannelException {
        if ( ! ( key instanceof Serializable ) || ! ( value instanceof Serializable ) ) {
            return new Member[0];
        }
        final Member[] members = this.getMapMembers();
        int nextIdx;
        final int firstIdx = nextIdx = this.getNextBackupIndex();
        Member[] backup = new Member[0];
        if ( members.length == 0 || firstIdx == -1 ) {
            return backup;
        }
        boolean success = false;
        do {
            final Member next = members[nextIdx];
            ++nextIdx;
            if ( nextIdx >= members.length ) {
                nextIdx = 0;
            }
            if ( next == null ) {
                continue;
            }
            MapMessage msg = null;
            try {
                final Member[] tmpBackup = this.wrap ( next );
                msg = new MapMessage ( this.getMapContextName(), 1, false, ( Serializable ) key, ( Serializable ) value, null, this.channel.getLocalMember ( false ), tmpBackup );
                if ( this.log.isTraceEnabled() ) {
                    this.log.trace ( "Publishing backup data:" + msg + " to: " + next.getName() );
                }
                final UniqueId id = this.getChannel().send ( tmpBackup, msg, this.getChannelSendOptions() );
                if ( this.log.isTraceEnabled() ) {
                    this.log.trace ( "Data published:" + msg + " msg Id:" + id );
                }
                success = true;
                backup = tmpBackup;
            } catch ( ChannelException x ) {
                this.log.error ( LazyReplicatedMap.sm.getString ( "lazyReplicatedMap.unableReplicate.backup", key, next, x.getMessage() ), x );
                continue;
            }
            try {
                final Member[] proxies = this.excludeFromSet ( backup, this.getMapMembers() );
                if ( !success || proxies.length <= 0 ) {
                    continue;
                }
                msg = new MapMessage ( this.getMapContextName(), 3, false, ( Serializable ) key, null, null, this.channel.getLocalMember ( false ), backup );
                if ( this.log.isTraceEnabled() ) {
                    this.log.trace ( "Publishing proxy data:" + msg + " to: " + Arrays.toNameString ( proxies ) );
                }
                this.getChannel().send ( proxies, msg, this.getChannelSendOptions() );
            } catch ( ChannelException x ) {
                this.log.error ( LazyReplicatedMap.sm.getString ( "lazyReplicatedMap.unableReplicate.proxy", key, next, x.getMessage() ), x );
            }
        } while ( !success && firstIdx != nextIdx );
        return backup;
    }
}
