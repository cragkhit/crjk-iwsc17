package org.apache.catalina.tribes.transport.bio;
import org.apache.juli.logging.LogFactory;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.catalina.tribes.io.BufferPool;
import org.apache.catalina.tribes.transport.Constants;
import org.apache.catalina.tribes.io.ChannelData;
import org.apache.catalina.tribes.io.ListenCallback;
import org.apache.catalina.tribes.io.ObjectReader;
import java.net.Socket;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.tribes.transport.AbstractRxTask;
public class BioReplicationTask extends AbstractRxTask {
    private static final Log log;
    protected static final StringManager sm;
    protected Socket socket;
    protected ObjectReader reader;
    public BioReplicationTask ( final ListenCallback callback ) {
        super ( callback );
    }
    @Override
    public synchronized void run() {
        if ( this.socket == null ) {
            return;
        }
        try {
            this.drainSocket();
        } catch ( Exception x ) {
            BioReplicationTask.log.error ( BioReplicationTask.sm.getString ( "bioReplicationTask.unable.service" ), x );
        } finally {
            try {
                this.socket.close();
            } catch ( Exception e ) {
                if ( BioReplicationTask.log.isDebugEnabled() ) {
                    BioReplicationTask.log.debug ( BioReplicationTask.sm.getString ( "bioReplicationTask.socket.closeFailed" ), e );
                }
            }
            try {
                this.reader.close();
            } catch ( Exception e ) {
                if ( BioReplicationTask.log.isDebugEnabled() ) {
                    BioReplicationTask.log.debug ( BioReplicationTask.sm.getString ( "bioReplicationTask.reader.closeFailed" ), e );
                }
            }
            this.reader = null;
            this.socket = null;
        }
        if ( this.getTaskPool() != null ) {
            this.getTaskPool().returnWorker ( this );
        }
    }
    public synchronized void serviceSocket ( final Socket socket, final ObjectReader reader ) {
        this.socket = socket;
        this.reader = reader;
    }
    protected void execute ( final ObjectReader reader ) throws Exception {
        final int pkgcnt = reader.count();
        if ( pkgcnt > 0 ) {
            final ChannelMessage[] msgs = reader.execute();
            for ( int i = 0; i < msgs.length; ++i ) {
                if ( ChannelData.sendAckAsync ( msgs[i].getOptions() ) ) {
                    this.sendAck ( Constants.ACK_COMMAND );
                }
                try {
                    this.getCallback().messageDataReceived ( msgs[i] );
                    if ( ChannelData.sendAckSync ( msgs[i].getOptions() ) ) {
                        this.sendAck ( Constants.ACK_COMMAND );
                    }
                } catch ( Exception x ) {
                    if ( ChannelData.sendAckSync ( msgs[i].getOptions() ) ) {
                        this.sendAck ( Constants.FAIL_ACK_COMMAND );
                    }
                    BioReplicationTask.log.error ( BioReplicationTask.sm.getString ( "bioReplicationTask.messageDataReceived.error" ), x );
                }
                if ( this.getUseBufferPool() ) {
                    BufferPool.getBufferPool().returnBuffer ( msgs[i].getMessage() );
                    msgs[i].setMessage ( null );
                }
            }
        }
    }
    protected void drainSocket() throws Exception {
        final InputStream in = this.socket.getInputStream();
        final byte[] buf = new byte[1024];
        for ( int length = in.read ( buf ); length >= 0; length = in.read ( buf ) ) {
            final int count = this.reader.append ( buf, 0, length, true );
            if ( count > 0 ) {
                this.execute ( this.reader );
            }
        }
    }
    protected void sendAck ( final byte[] command ) {
        try {
            final OutputStream out = this.socket.getOutputStream();
            out.write ( command );
            out.flush();
            if ( BioReplicationTask.log.isTraceEnabled() ) {
                BioReplicationTask.log.trace ( "ACK sent to " + this.socket.getPort() );
            }
        } catch ( IOException x ) {
            BioReplicationTask.log.warn ( BioReplicationTask.sm.getString ( "bioReplicationTask.unable.sendAck", x.getMessage() ) );
        }
    }
    @Override
    public void close() {
        this.setDoRun ( false );
        try {
            this.socket.close();
        } catch ( Exception e ) {
            if ( BioReplicationTask.log.isDebugEnabled() ) {
                BioReplicationTask.log.debug ( BioReplicationTask.sm.getString ( "bioReplicationTask.socket.closeFailed" ), e );
            }
        }
        try {
            this.reader.close();
        } catch ( Exception e ) {
            if ( BioReplicationTask.log.isDebugEnabled() ) {
                BioReplicationTask.log.debug ( BioReplicationTask.sm.getString ( "bioReplicationTask.reader.closeFailed" ), e );
            }
        }
        this.reader = null;
        this.socket = null;
        super.close();
    }
    static {
        log = LogFactory.getLog ( BioReplicationTask.class );
        sm = StringManager.getManager ( BioReplicationTask.class );
    }
}
