package org.apache.catalina.tribes.transport.bio;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.apache.catalina.tribes.ChannelMessage;
import org.apache.catalina.tribes.io.BufferPool;
import org.apache.catalina.tribes.io.ChannelData;
import org.apache.catalina.tribes.io.ListenCallback;
import org.apache.catalina.tribes.io.ObjectReader;
import org.apache.catalina.tribes.transport.AbstractRxTask;
import org.apache.catalina.tribes.transport.Constants;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
public class BioReplicationTask extends AbstractRxTask {
    private static final Log log = LogFactory.getLog ( BioReplicationTask.class );
    protected static final StringManager sm = StringManager.getManager ( BioReplicationTask.class );
    protected Socket socket;
    protected ObjectReader reader;
    public BioReplicationTask ( ListenCallback callback ) {
        super ( callback );
    }
    @Override
    public synchronized void run() {
        if ( socket == null ) {
            return;
        }
        try {
            drainSocket();
        } catch ( Exception x ) {
            log.error ( sm.getString ( "bioReplicationTask.unable.service" ), x );
        } finally {
            try {
                socket.close();
            } catch ( Exception e ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "bioReplicationTask.socket.closeFailed" ), e );
                }
            }
            try {
                reader.close();
            } catch ( Exception e ) {
                if ( log.isDebugEnabled() ) {
                    log.debug ( sm.getString ( "bioReplicationTask.reader.closeFailed" ), e );
                }
            }
            reader = null;
            socket = null;
        }
        if ( getTaskPool() != null ) {
            getTaskPool().returnWorker ( this );
        }
    }
    public synchronized void serviceSocket ( Socket socket, ObjectReader reader ) {
        this.socket = socket;
        this.reader = reader;
    }
    protected void execute ( ObjectReader reader ) throws Exception {
        int pkgcnt = reader.count();
        if ( pkgcnt > 0 ) {
            ChannelMessage[] msgs = reader.execute();
            for ( int i = 0; i < msgs.length; i++ ) {
                if ( ChannelData.sendAckAsync ( msgs[i].getOptions() ) ) {
                    sendAck ( Constants.ACK_COMMAND );
                }
                try {
                    getCallback().messageDataReceived ( msgs[i] );
                    if ( ChannelData.sendAckSync ( msgs[i].getOptions() ) ) {
                        sendAck ( Constants.ACK_COMMAND );
                    }
                } catch ( Exception x ) {
                    if ( ChannelData.sendAckSync ( msgs[i].getOptions() ) ) {
                        sendAck ( Constants.FAIL_ACK_COMMAND );
                    }
                    log.error ( sm.getString ( "bioReplicationTask.messageDataReceived.error" ), x );
                }
                if ( getUseBufferPool() ) {
                    BufferPool.getBufferPool().returnBuffer ( msgs[i].getMessage() );
                    msgs[i].setMessage ( null );
                }
            }
        }
    }
    protected void drainSocket() throws Exception {
        InputStream in = socket.getInputStream();
        byte[] buf = new byte[1024];
        int length = in.read ( buf );
        while ( length >= 0 ) {
            int count = reader.append ( buf, 0, length, true );
            if ( count > 0 ) {
                execute ( reader );
            }
            length = in.read ( buf );
        }
    }
    protected void sendAck ( byte[] command ) {
        try {
            OutputStream out = socket.getOutputStream();
            out.write ( command );
            out.flush();
            if ( log.isTraceEnabled() ) {
                log.trace ( "ACK sent to " + socket.getPort() );
            }
        } catch ( java.io.IOException x ) {
            log.warn ( sm.getString ( "bioReplicationTask.unable.sendAck", x.getMessage() ) );
        }
    }
    @Override
    public void close() {
        setDoRun ( false );
        try {
            socket.close();
        } catch ( Exception e ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "bioReplicationTask.socket.closeFailed" ), e );
            }
        }
        try {
            reader.close();
        } catch ( Exception e ) {
            if ( log.isDebugEnabled() ) {
                log.debug ( sm.getString ( "bioReplicationTask.reader.closeFailed" ), e );
            }
        }
        reader = null;
        socket = null;
        super.close();
    }
}
