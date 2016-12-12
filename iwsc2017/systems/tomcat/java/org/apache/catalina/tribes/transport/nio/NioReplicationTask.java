package org.apache.catalina.tribes.transport.nio;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.ChannelReceiver;
import org.apache.catalina.tribes.RemoteProcessException;
import org.apache.catalina.tribes.UniqueId;
import org.apache.catalina.tribes.io.BufferPool;
import org.apache.catalina.tribes.io.ChannelData;
import org.apache.catalina.tribes.io.ListenCallback;
import org.apache.catalina.tribes.io.ObjectReader;
import org.apache.catalina.tribes.transport.AbstractRxTask;
import org.apache.catalina.tribes.transport.Constants;
import org.apache.catalina.tribes.util.Logs;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class NioReplicationTask extends AbstractRxTask {
    private static final Log log = LogFactory.getLog ( NioReplicationTask.class );
    protected static final StringManager sm = StringManager.getManager ( NioReplicationTask.class );
    private ByteBuffer buffer = null;
    private SelectionKey key;
    private int rxBufSize;
    private final NioReceiver receiver;
    public NioReplicationTask ( ListenCallback callback, NioReceiver receiver ) {
        super ( callback );
        this.receiver = receiver;
    }
    @Override
    public synchronized void run() {
        if ( buffer == null ) {
            int size = getRxBufSize();
            if ( key.channel() instanceof DatagramChannel ) {
                size = ChannelReceiver.MAX_UDP_SIZE;
            }
            if ( ( getOptions() & OPTION_DIRECT_BUFFER ) == OPTION_DIRECT_BUFFER ) {
                buffer = ByteBuffer.allocateDirect ( size );
            } else {
                buffer = ByteBuffer.allocate ( size );
            }
        } else {
            buffer.clear();
        }
        if ( key == null ) {
            return;
        }
        if ( log.isTraceEnabled() ) {
            log.trace ( "Servicing key:" + key );
        }
        try {
            ObjectReader reader = ( ObjectReader ) key.attachment();
            if ( reader == null ) {
                if ( log.isTraceEnabled() ) {
                    log.trace ( "No object reader, cancelling:" + key );
                }
                cancelKey ( key );
            } else {
                if ( log.isTraceEnabled() ) {
                    log.trace ( "Draining channel:" + key );
                }
                drainChannel ( key, reader );
            }
        } catch ( Exception e ) {
            if ( e instanceof CancelledKeyException ) {
            } else if ( e instanceof IOException ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( "IOException in replication worker, unable to drain channel. Probable cause: Keep alive socket closed[" + e.getMessage() + "].", e );
                } else {
                    log.warn ( sm.getString ( "nioReplicationTask.unable.drainChannel.ioe", e.getMessage() ) );
                }
            } else if ( log.isErrorEnabled() ) {
                log.error ( sm.getString ( "nioReplicationTask.exception.drainChannel" ), e );
            }
            cancelKey ( key );
        }
        key = null;
        getTaskPool().returnWorker ( this );
    }
    public synchronized void serviceChannel ( SelectionKey key ) {
        if ( log.isTraceEnabled() ) {
            log.trace ( "About to service key:" + key );
        }
        ObjectReader reader = ( ObjectReader ) key.attachment();
        if ( reader != null ) {
            reader.setLastAccess ( System.currentTimeMillis() );
        }
        this.key = key;
        key.interestOps ( key.interestOps() & ( ~SelectionKey.OP_READ ) );
        key.interestOps ( key.interestOps() & ( ~SelectionKey.OP_WRITE ) );
    }
    protected void drainChannel ( final SelectionKey key, ObjectReader reader ) throws Exception {
        reader.access();
        ReadableByteChannel channel = ( ReadableByteChannel ) key.channel();
        int count = -1;
        buffer.clear();
        SocketAddress saddr = null;
        if ( channel instanceof SocketChannel ) {
            while ( ( count = channel.read ( buffer ) ) > 0 ) {
                buffer.flip();
                if ( buffer.hasArray() ) {
                    reader.append ( buffer.array(), 0, count, false );
                } else {
                    reader.append ( buffer, count, false );
                }
                buffer.clear();
                if ( reader.hasPackage() ) {
                    break;
                }
            }
        } else if ( channel instanceof DatagramChannel ) {
            DatagramChannel dchannel = ( DatagramChannel ) channel;
            saddr = dchannel.receive ( buffer );
            buffer.flip();
            if ( buffer.hasArray() ) {
                reader.append ( buffer.array(), 0, buffer.limit() - buffer.position(), false );
            } else {
                reader.append ( buffer, buffer.limit() - buffer.position(), false );
            }
            buffer.clear();
            count = reader.hasPackage() ? 1 : -1;
        }
        int pkgcnt = reader.count();
        if ( count < 0 && pkgcnt == 0 ) {
            remoteEof ( key );
            return;
        }
        ChannelMessage[] msgs = pkgcnt == 0 ? ChannelData.EMPTY_DATA_ARRAY : reader.execute();
        registerForRead ( key, reader );
        for ( int i = 0; i < msgs.length; i++ ) {
            if ( ChannelData.sendAckAsync ( msgs[i].getOptions() ) ) {
                sendAck ( key, ( WritableByteChannel ) channel, Constants.ACK_COMMAND, saddr );
            }
            try {
                if ( Logs.MESSAGES.isTraceEnabled() ) {
                    try {
                        Logs.MESSAGES.trace ( "NioReplicationThread - Received msg:" + new UniqueId ( msgs[i].getUniqueId() ) + " at " + new java.sql.Timestamp ( System.currentTimeMillis() ) );
                    } catch ( Throwable t ) {}
                }
                getCallback().messageDataReceived ( msgs[i] );
                if ( ChannelData.sendAckSync ( msgs[i].getOptions() ) ) {
                    sendAck ( key, ( WritableByteChannel ) channel, Constants.ACK_COMMAND, saddr );
                }
            } catch ( RemoteProcessException e ) {
                if ( log.isDebugEnabled() ) {
                    log.error ( sm.getString ( "nioReplicationTask.process.clusterMsg.failed" ), e );
                }
                if ( ChannelData.sendAckSync ( msgs[i].getOptions() ) ) {
                    sendAck ( key, ( WritableByteChannel ) channel, Constants.FAIL_ACK_COMMAND, saddr );
                }
            } catch ( Exception e ) {
                log.error ( sm.getString ( "nioReplicationTask.process.clusterMsg.failed" ), e );
                if ( ChannelData.sendAckSync ( msgs[i].getOptions() ) ) {
                    sendAck ( key, ( WritableByteChannel ) channel, Constants.FAIL_ACK_COMMAND, saddr );
                }
            }
            if ( getUseBufferPool() ) {
                BufferPool.getBufferPool().returnBuffer ( msgs[i].getMessage() );
                msgs[i].setMessage ( null );
            }
        }
        if ( count < 0 ) {
            remoteEof ( key );
            return;
        }
    }
    private void remoteEof ( SelectionKey key ) {
        if ( log.isDebugEnabled() ) {
            log.debug ( "Channel closed on the remote end, disconnecting" );
        }
        cancelKey ( key );
    }
    protected void registerForRead ( final SelectionKey key, ObjectReader reader ) {
        if ( log.isTraceEnabled() ) {
            log.trace ( "Adding key for read event:" + key );
        }
        reader.finish();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    if ( key.isValid() ) {
                        key.selector().wakeup();
                        int resumeOps = key.interestOps() | SelectionKey.OP_READ;
                        key.interestOps ( resumeOps );
                        if ( log.isTraceEnabled() ) {
                            log.trace ( "Registering key for read:" + key );
                        }
                    }
                } catch ( CancelledKeyException ckx ) {
                    NioReceiver.cancelledKey ( key );
                    if ( log.isTraceEnabled() ) {
                        log.trace ( "CKX Cancelling key:" + key );
                    }
                } catch ( Exception x ) {
                    log.error ( sm.getString ( "nioReplicationTask.error.register.key", key ), x );
                }
            }
        };
        receiver.addEvent ( r );
    }
    private void cancelKey ( final SelectionKey key ) {
        if ( log.isTraceEnabled() ) {
            log.trace ( "Adding key for cancel event:" + key );
        }
        ObjectReader reader = ( ObjectReader ) key.attachment();
        if ( reader != null ) {
            reader.setCancelled ( true );
            reader.finish();
        }
        Runnable cx = new Runnable() {
            @Override
            public void run() {
                if ( log.isTraceEnabled() ) {
                    log.trace ( "Cancelling key:" + key );
                }
                NioReceiver.cancelledKey ( key );
            }
        };
        receiver.addEvent ( cx );
    }
    protected void sendAck ( SelectionKey key, WritableByteChannel channel, byte[] command, SocketAddress udpaddr ) {
        try {
            ByteBuffer buf = ByteBuffer.wrap ( command );
            int total = 0;
            if ( channel instanceof DatagramChannel ) {
                DatagramChannel dchannel = ( DatagramChannel ) channel;
                while ( total < command.length ) {
                    total += dchannel.send ( buf, udpaddr );
                }
            } else {
                while ( total < command.length ) {
                    total += channel.write ( buf );
                }
            }
            if ( log.isTraceEnabled() ) {
                log.trace ( "ACK sent to " +
                            ( ( channel instanceof SocketChannel ) ?
                              ( ( SocketChannel ) channel ).socket().getInetAddress() :
                              ( ( DatagramChannel ) channel ).socket().getInetAddress() ) );
            }
        } catch ( java.io.IOException x ) {
            log.warn ( sm.getString ( "nioReplicationTask.unable.ack", x.getMessage() ) );
        }
    }
    public void setRxBufSize ( int rxBufSize ) {
        this.rxBufSize = rxBufSize;
    }
    public int getRxBufSize() {
        return rxBufSize;
    }
}
