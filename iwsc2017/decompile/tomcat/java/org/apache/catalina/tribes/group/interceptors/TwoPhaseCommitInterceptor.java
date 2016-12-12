package org.apache.catalina.tribes.group.interceptors;
import org.apache.juli.logging.LogFactory;
import java.util.Map;
import org.apache.catalina.tribes.util.Arrays;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.util.UUIDGenerator;
import org.apache.catalina.tribes.group.InterceptorPayload;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.UniqueId;
import java.util.HashMap;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.tribes.group.ChannelInterceptorBase;
public class TwoPhaseCommitInterceptor extends ChannelInterceptorBase {
    private static final byte[] START_DATA;
    private static final byte[] END_DATA;
    private static final Log log;
    protected static final StringManager sm;
    protected final HashMap<UniqueId, MapEntry> messages;
    protected long expire;
    protected boolean deepclone;
    public TwoPhaseCommitInterceptor() {
        this.messages = new HashMap<UniqueId, MapEntry>();
        this.expire = 60000L;
        this.deepclone = true;
    }
    @Override
    public void sendMessage ( final Member[] destination, final ChannelMessage msg, final InterceptorPayload payload ) throws ChannelException {
        if ( this.okToProcess ( msg.getOptions() ) ) {
            super.sendMessage ( destination, msg, null );
            ChannelMessage confirmation = null;
            if ( this.deepclone ) {
                confirmation = ( ChannelMessage ) msg.deepclone();
            } else {
                confirmation = ( ChannelMessage ) msg.clone();
            }
            confirmation.getMessage().reset();
            UUIDGenerator.randomUUID ( false, confirmation.getUniqueId(), 0 );
            confirmation.getMessage().append ( TwoPhaseCommitInterceptor.START_DATA, 0, TwoPhaseCommitInterceptor.START_DATA.length );
            confirmation.getMessage().append ( msg.getUniqueId(), 0, msg.getUniqueId().length );
            confirmation.getMessage().append ( TwoPhaseCommitInterceptor.END_DATA, 0, TwoPhaseCommitInterceptor.END_DATA.length );
            super.sendMessage ( destination, confirmation, payload );
        } else {
            super.sendMessage ( destination, msg, payload );
        }
    }
    @Override
    public void messageReceived ( final ChannelMessage msg ) {
        if ( this.okToProcess ( msg.getOptions() ) ) {
            if ( msg.getMessage().getLength() == TwoPhaseCommitInterceptor.START_DATA.length + msg.getUniqueId().length + TwoPhaseCommitInterceptor.END_DATA.length && Arrays.contains ( msg.getMessage().getBytesDirect(), 0, TwoPhaseCommitInterceptor.START_DATA, 0, TwoPhaseCommitInterceptor.START_DATA.length ) && Arrays.contains ( msg.getMessage().getBytesDirect(), TwoPhaseCommitInterceptor.START_DATA.length + msg.getUniqueId().length, TwoPhaseCommitInterceptor.END_DATA, 0, TwoPhaseCommitInterceptor.END_DATA.length ) ) {
                final UniqueId id = new UniqueId ( msg.getMessage().getBytesDirect(), TwoPhaseCommitInterceptor.START_DATA.length, msg.getUniqueId().length );
                final MapEntry original = this.messages.get ( id );
                if ( original != null ) {
                    super.messageReceived ( original.msg );
                    this.messages.remove ( id );
                } else {
                    TwoPhaseCommitInterceptor.log.warn ( TwoPhaseCommitInterceptor.sm.getString ( "twoPhaseCommitInterceptor.originalMessage.missing", Arrays.toString ( id.getBytes() ) ) );
                }
            } else {
                final UniqueId id = new UniqueId ( msg.getUniqueId() );
                final MapEntry entry = new MapEntry ( ( ChannelMessage ) msg.deepclone(), id, System.currentTimeMillis() );
                this.messages.put ( id, entry );
            }
        } else {
            super.messageReceived ( msg );
        }
    }
    public boolean getDeepclone() {
        return this.deepclone;
    }
    public long getExpire() {
        return this.expire;
    }
    public void setDeepclone ( final boolean deepclone ) {
        this.deepclone = deepclone;
    }
    public void setExpire ( final long expire ) {
        this.expire = expire;
    }
    @Override
    public void heartbeat() {
        try {
            final long now = System.currentTimeMillis();
            final Map.Entry<UniqueId, MapEntry>[] entries = this.messages.entrySet().toArray ( new Map.Entry[this.messages.size()] );
            for ( int i = 0; i < entries.length; ++i ) {
                final MapEntry entry = entries[i].getValue();
                if ( entry.expired ( now, this.expire ) ) {
                    if ( TwoPhaseCommitInterceptor.log.isInfoEnabled() ) {
                        TwoPhaseCommitInterceptor.log.info ( "Message [" + entry.id + "] has expired. Removing." );
                    }
                    this.messages.remove ( entry.id );
                }
            }
        } catch ( Exception x ) {
            TwoPhaseCommitInterceptor.log.warn ( TwoPhaseCommitInterceptor.sm.getString ( "twoPhaseCommitInterceptor.heartbeat.failed" ), x );
        } finally {
            super.heartbeat();
        }
    }
    static {
        START_DATA = new byte[] { 113, 1, -58, 2, -34, -60, 75, -78, -101, -12, 32, -29, 32, 111, -40, 4 };
        END_DATA = new byte[] { 54, -13, 90, 110, 47, -31, 75, -24, -81, -29, 36, 52, -58, 77, -110, 56 };
        log = LogFactory.getLog ( TwoPhaseCommitInterceptor.class );
        sm = StringManager.getManager ( TwoPhaseCommitInterceptor.class );
    }
    public static class MapEntry {
        public final ChannelMessage msg;
        public final UniqueId id;
        public final long timestamp;
        public MapEntry ( final ChannelMessage msg, final UniqueId id, final long timestamp ) {
            this.msg = msg;
            this.id = id;
            this.timestamp = timestamp;
        }
        public boolean expired ( final long now, final long expiration ) {
            return now - this.timestamp > expiration;
        }
    }
}
