package org.apache.catalina.tribes.tipis;
import java.io.Serializable;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.UniqueId;
import org.apache.catalina.tribes.util.Arrays;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class LazyReplicatedMap<K, V> extends AbstractReplicatedMap<K, V> {
    private static final long serialVersionUID = 1L;
    private final Log log = LogFactory.getLog ( LazyReplicatedMap.class );
    public LazyReplicatedMap ( MapOwner owner, Channel channel, long timeout, String mapContextName, int initialCapacity, float loadFactor, ClassLoader[] cls ) {
        super ( owner, channel, timeout, mapContextName, initialCapacity, loadFactor, Channel.SEND_OPTIONS_DEFAULT, cls, true );
    }
    public LazyReplicatedMap ( MapOwner owner, Channel channel, long timeout, String mapContextName, int initialCapacity, ClassLoader[] cls ) {
        super ( owner, channel, timeout, mapContextName, initialCapacity, AbstractReplicatedMap.DEFAULT_LOAD_FACTOR, Channel.SEND_OPTIONS_DEFAULT, cls, true );
    }
    public LazyReplicatedMap ( MapOwner owner, Channel channel, long timeout, String mapContextName, ClassLoader[] cls ) {
        super ( owner, channel, timeout, mapContextName, AbstractReplicatedMap.DEFAULT_INITIAL_CAPACITY, AbstractReplicatedMap.DEFAULT_LOAD_FACTOR, Channel.SEND_OPTIONS_DEFAULT, cls, true );
    }
    public LazyReplicatedMap ( MapOwner owner, Channel channel, long timeout, String mapContextName, ClassLoader[] cls, boolean terminate ) {
        super ( owner, channel, timeout, mapContextName, AbstractReplicatedMap.DEFAULT_INITIAL_CAPACITY,
                AbstractReplicatedMap.DEFAULT_LOAD_FACTOR, Channel.SEND_OPTIONS_DEFAULT, cls, terminate );
    }
    @Override
    protected int getStateMessageType() {
        return AbstractReplicatedMap.MapMessage.MSG_STATE;
    }
    @Override
    protected int getReplicateMessageType() {
        return AbstractReplicatedMap.MapMessage.MSG_BACKUP;
    }
    @Override
    protected Member[] publishEntryInfo ( Object key, Object value ) throws ChannelException {
        if ( ! ( key instanceof Serializable && value instanceof Serializable ) ) {
            return new Member[0];
        }
        Member[] members = getMapMembers();
        int firstIdx = getNextBackupIndex();
        int nextIdx = firstIdx;
        Member[] backup = new Member[0];
        if ( members.length == 0 || firstIdx == -1 ) {
            return backup;
        }
        boolean success = false;
        do {
            Member next = members[nextIdx];
            nextIdx = nextIdx + 1;
            if ( nextIdx >= members.length ) {
                nextIdx = 0;
            }
            if ( next == null ) {
                continue;
            }
            MapMessage msg = null;
            try {
                Member[] tmpBackup = wrap ( next );
                msg = new MapMessage ( getMapContextName(), MapMessage.MSG_BACKUP, false,
                                       ( Serializable ) key, ( Serializable ) value, null, channel.getLocalMember ( false ), tmpBackup );
                if ( log.isTraceEnabled() ) {
                    log.trace ( "Publishing backup data:" + msg + " to: " + next.getName() );
                }
                UniqueId id = getChannel().send ( tmpBackup, msg, getChannelSendOptions() );
                if ( log.isTraceEnabled() ) {
                    log.trace ( "Data published:" + msg + " msg Id:" + id );
                }
                success = true;
                backup = tmpBackup;
            } catch ( ChannelException x ) {
                log.error ( sm.getString ( "lazyReplicatedMap.unableReplicate.backup", key, next, x.getMessage() ), x );
                continue;
            }
            try {
                Member[] proxies = excludeFromSet ( backup, getMapMembers() );
                if ( success && proxies.length > 0 ) {
                    msg = new MapMessage ( getMapContextName(), MapMessage.MSG_PROXY, false,
                                           ( Serializable ) key, null, null, channel.getLocalMember ( false ), backup );
                    if ( log.isTraceEnabled() ) {
                        log.trace ( "Publishing proxy data:" + msg + " to: " + Arrays.toNameString ( proxies ) );
                    }
                    getChannel().send ( proxies, msg, getChannelSendOptions() );
                }
            } catch ( ChannelException x ) {
                log.error ( sm.getString ( "lazyReplicatedMap.unableReplicate.proxy", key, next, x.getMessage() ), x );
            }
        } while ( !success && ( firstIdx != nextIdx ) );
        return backup;
    }
}
