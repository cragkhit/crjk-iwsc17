package org.apache.catalina.tribes.group;
import java.util.ArrayList;
import org.apache.juli.logging.LogFactory;
import java.util.Arrays;
import org.apache.catalina.tribes.UniqueId;
import org.apache.catalina.tribes.ErrorHandler;
import org.apache.catalina.tribes.ChannelException;
import org.apache.catalina.tribes.util.UUIDGenerator;
import java.io.Serializable;
import org.apache.catalina.tribes.Member;
import java.util.HashMap;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.tribes.ChannelListener;
public class RpcChannel implements ChannelListener {
    private static final Log log;
    protected static final StringManager sm;
    public static final int FIRST_REPLY = 1;
    public static final int MAJORITY_REPLY = 2;
    public static final int ALL_REPLY = 3;
    public static final int NO_REPLY = 4;
    private Channel channel;
    private RpcCallback callback;
    private byte[] rpcId;
    private int replyMessageOptions;
    private final HashMap<RpcCollectorKey, RpcCollector> responseMap;
    public RpcChannel ( final byte[] rpcId, final Channel channel, final RpcCallback callback ) {
        this.replyMessageOptions = 0;
        this.responseMap = new HashMap<RpcCollectorKey, RpcCollector>();
        this.channel = channel;
        this.callback = callback;
        this.rpcId = rpcId;
        channel.addChannelListener ( this );
    }
    public Response[] send ( final Member[] destination, final Serializable message, final int rpcOptions, final int channelOptions, final long timeout ) throws ChannelException {
        if ( destination == null || destination.length == 0 ) {
            return new Response[0];
        }
        final int sendOptions = channelOptions & 0xFFFFFFFB;
        final RpcCollectorKey key = new RpcCollectorKey ( UUIDGenerator.randomUUID ( false ) );
        final RpcCollector collector = new RpcCollector ( key, rpcOptions, destination.length );
        try {
            synchronized ( collector ) {
                if ( rpcOptions != 4 ) {
                    this.responseMap.put ( key, collector );
                }
                final RpcMessage rmsg = new RpcMessage ( this.rpcId, key.id, message );
                this.channel.send ( destination, rmsg, sendOptions );
                if ( rpcOptions != 4 ) {
                    collector.wait ( timeout );
                }
            }
        } catch ( InterruptedException ix ) {
            Thread.currentThread().interrupt();
        } finally {
            this.responseMap.remove ( key );
        }
        return collector.getResponses();
    }
    @Override
    public void messageReceived ( final Serializable msg, final Member sender ) {
        final RpcMessage rmsg = ( RpcMessage ) msg;
        final RpcCollectorKey key = new RpcCollectorKey ( rmsg.uuid );
        if ( rmsg.reply ) {
            final RpcCollector collector = this.responseMap.get ( key );
            if ( collector == null ) {
                this.callback.leftOver ( rmsg.message, sender );
            } else {
                synchronized ( collector ) {
                    if ( this.responseMap.containsKey ( key ) ) {
                        if ( rmsg instanceof RpcMessage.NoRpcChannelReply ) {
                            --collector.destcnt;
                        } else {
                            collector.addResponse ( rmsg.message, sender );
                        }
                        if ( collector.isComplete() ) {
                            collector.notifyAll();
                        }
                    } else if ( ! ( rmsg instanceof RpcMessage.NoRpcChannelReply ) ) {
                        this.callback.leftOver ( rmsg.message, sender );
                    }
                }
            }
        } else {
            boolean finished = false;
            final ExtendedRpcCallback excallback = ( this.callback instanceof ExtendedRpcCallback ) ? ( ( ExtendedRpcCallback ) this.callback ) : null;
            final boolean asyncReply = ( this.replyMessageOptions & 0x8 ) == 0x8;
            final Serializable reply = this.callback.replyRequest ( rmsg.message, sender );
            ErrorHandler handler = null;
            final Serializable response = reply;
            if ( excallback != null && asyncReply ) {
                handler = new ErrorHandler() {
                    @Override
                    public void handleError ( final ChannelException x, final UniqueId id ) {
                        excallback.replyFailed ( msg, response, sender, x );
                    }
                    @Override
                    public void handleCompletion ( final UniqueId id ) {
                        excallback.replySucceeded ( msg, response, sender );
                    }
                };
            }
            rmsg.reply = true;
            rmsg.message = reply;
            try {
                if ( handler != null ) {
                    this.channel.send ( new Member[] { sender }, rmsg, this.replyMessageOptions & 0xFFFFFFFB, handler );
                } else {
                    this.channel.send ( new Member[] { sender }, rmsg, this.replyMessageOptions & 0xFFFFFFFB );
                }
                finished = true;
            } catch ( Exception x ) {
                if ( excallback != null && !asyncReply ) {
                    excallback.replyFailed ( rmsg.message, reply, sender, x );
                } else {
                    RpcChannel.log.error ( RpcChannel.sm.getString ( "rpcChannel.replyFailed" ), x );
                }
            }
            if ( finished && excallback != null && !asyncReply ) {
                excallback.replySucceeded ( rmsg.message, reply, sender );
            }
        }
    }
    public void breakdown() {
        this.channel.removeChannelListener ( this );
    }
    public void finalize() throws Throwable {
        this.breakdown();
        super.finalize();
    }
    @Override
    public boolean accept ( final Serializable msg, final Member sender ) {
        if ( msg instanceof RpcMessage ) {
            final RpcMessage rmsg = ( RpcMessage ) msg;
            return Arrays.equals ( rmsg.rpcId, this.rpcId );
        }
        return false;
    }
    public Channel getChannel() {
        return this.channel;
    }
    public RpcCallback getCallback() {
        return this.callback;
    }
    public byte[] getRpcId() {
        return this.rpcId;
    }
    public void setChannel ( final Channel channel ) {
        this.channel = channel;
    }
    public void setCallback ( final RpcCallback callback ) {
        this.callback = callback;
    }
    public void setRpcId ( final byte[] rpcId ) {
        this.rpcId = rpcId;
    }
    public int getReplyMessageOptions() {
        return this.replyMessageOptions;
    }
    public void setReplyMessageOptions ( final int replyMessageOptions ) {
        this.replyMessageOptions = replyMessageOptions;
    }
    static {
        log = LogFactory.getLog ( RpcChannel.class );
        sm = StringManager.getManager ( RpcChannel.class );
    }
    public static class RpcCollector {
        public final ArrayList<Response> responses;
        public final RpcCollectorKey key;
        public final int options;
        public int destcnt;
        public RpcCollector ( final RpcCollectorKey key, final int options, final int destcnt ) {
            this.responses = new ArrayList<Response>();
            this.key = key;
            this.options = options;
            this.destcnt = destcnt;
        }
        public void addResponse ( final Serializable message, final Member sender ) {
            final Response resp = new Response ( sender, message );
            this.responses.add ( resp );
        }
        public boolean isComplete() {
            if ( this.destcnt <= 0 ) {
                return true;
            }
            switch ( this.options ) {
            case 3: {
                return this.destcnt == this.responses.size();
            }
            case 2: {
                final float perc = this.responses.size() / this.destcnt;
                return perc >= 0.5f;
            }
            case 1: {
                return this.responses.size() > 0;
            }
            default: {
                return false;
            }
            }
        }
        @Override
        public int hashCode() {
            return this.key.hashCode();
        }
        @Override
        public boolean equals ( final Object o ) {
            if ( o instanceof RpcCollector ) {
                final RpcCollector r = ( RpcCollector ) o;
                return r.key.equals ( this.key );
            }
            return false;
        }
        public Response[] getResponses() {
            return this.responses.toArray ( new Response[this.responses.size()] );
        }
    }
    public static class RpcCollectorKey {
        final byte[] id;
        public RpcCollectorKey ( final byte[] id ) {
            this.id = id;
        }
        @Override
        public int hashCode() {
            return this.id[0] + this.id[1] + this.id[2] + this.id[3];
        }
        @Override
        public boolean equals ( final Object o ) {
            if ( o instanceof RpcCollectorKey ) {
                final RpcCollectorKey r = ( RpcCollectorKey ) o;
                return Arrays.equals ( this.id, r.id );
            }
            return false;
        }
    }
}
