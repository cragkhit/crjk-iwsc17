package org.apache.catalina.tribes.transport.bio;
import org.apache.juli.logging.LogFactory;
import org.apache.catalina.tribes.RemoteProcessException;
import java.util.Arrays;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import org.apache.catalina.tribes.transport.SenderState;
import java.io.IOException;
import org.apache.catalina.tribes.transport.Constants;
import org.apache.catalina.tribes.io.XByteBuffer;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.tribes.transport.AbstractSender;
public class BioSender extends AbstractSender {
    private static final Log log;
    protected static final StringManager sm;
    private Socket socket;
    private OutputStream soOut;
    private InputStream soIn;
    protected final XByteBuffer ackbuf;
    public BioSender() {
        this.socket = null;
        this.soOut = null;
        this.soIn = null;
        this.ackbuf = new XByteBuffer ( Constants.ACK_COMMAND.length, true );
    }
    @Override
    public void connect() throws IOException {
        this.openSocket();
    }
    @Override
    public void disconnect() {
        final boolean connect = this.isConnected();
        this.closeSocket();
        if ( connect && BioSender.log.isDebugEnabled() ) {
            BioSender.log.debug ( BioSender.sm.getString ( "bioSender.disconnect", this.getAddress().getHostAddress(), this.getPort(), 0L ) );
        }
    }
    public void sendMessage ( final byte[] data, final boolean waitForAck ) throws IOException {
        IOException exception = null;
        this.setAttempt ( 0 );
        try {
            this.pushMessage ( data, false, waitForAck );
        } catch ( IOException x ) {
            SenderState.getSenderState ( this.getDestination() ).setSuspect();
            exception = x;
            if ( BioSender.log.isTraceEnabled() ) {
                BioSender.log.trace ( BioSender.sm.getString ( "bioSender.send.again", this.getAddress().getHostAddress(), this.getPort() ), x );
            }
            while ( this.getAttempt() < this.getMaxRetryAttempts() ) {
                try {
                    this.setAttempt ( this.getAttempt() + 1 );
                    this.pushMessage ( data, true, waitForAck );
                    exception = null;
                } catch ( IOException xx ) {
                    exception = xx;
                    this.closeSocket();
                }
            }
        } finally {
            this.setRequestCount ( this.getRequestCount() + 1 );
            this.keepalive();
            if ( exception != null ) {
                throw exception;
            }
        }
    }
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder ( "DataSender[(" );
        buf.append ( super.toString() ).append ( ")" );
        buf.append ( this.getAddress() ).append ( ":" ).append ( this.getPort() ).append ( "]" );
        return buf.toString();
    }
    protected void openSocket() throws IOException {
        if ( this.isConnected() ) {
            return;
        }
        try {
            this.socket = new Socket();
            final InetSocketAddress sockaddr = new InetSocketAddress ( this.getAddress(), this.getPort() );
            this.socket.connect ( sockaddr, ( int ) this.getTimeout() );
            this.socket.setSendBufferSize ( this.getTxBufSize() );
            this.socket.setReceiveBufferSize ( this.getRxBufSize() );
            this.socket.setSoTimeout ( ( int ) this.getTimeout() );
            this.socket.setTcpNoDelay ( this.getTcpNoDelay() );
            this.socket.setKeepAlive ( this.getSoKeepAlive() );
            this.socket.setReuseAddress ( this.getSoReuseAddress() );
            this.socket.setOOBInline ( this.getOoBInline() );
            this.socket.setSoLinger ( this.getSoLingerOn(), this.getSoLingerTime() );
            this.socket.setTrafficClass ( this.getSoTrafficClass() );
            this.setConnected ( true );
            this.soOut = this.socket.getOutputStream();
            this.soIn = this.socket.getInputStream();
            this.setRequestCount ( 0 );
            this.setConnectTime ( System.currentTimeMillis() );
            if ( BioSender.log.isDebugEnabled() ) {
                BioSender.log.debug ( BioSender.sm.getString ( "bioSender.openSocket", this.getAddress().getHostAddress(), this.getPort(), 0L ) );
            }
        } catch ( IOException ex1 ) {
            SenderState.getSenderState ( this.getDestination() ).setSuspect();
            if ( BioSender.log.isDebugEnabled() ) {
                BioSender.log.debug ( BioSender.sm.getString ( "bioSender.openSocket.failure", this.getAddress().getHostAddress(), this.getPort(), 0L ), ex1 );
            }
            throw ex1;
        }
    }
    protected void closeSocket() {
        if ( this.isConnected() ) {
            if ( this.socket != null ) {
                try {
                    this.socket.close();
                } catch ( IOException ex ) {}
                finally {
                    this.socket = null;
                    this.soOut = null;
                    this.soIn = null;
                }
            }
            this.setRequestCount ( 0 );
            this.setConnected ( false );
            if ( BioSender.log.isDebugEnabled() ) {
                BioSender.log.debug ( BioSender.sm.getString ( "bioSender.closeSocket", this.getAddress().getHostAddress(), this.getPort(), 0L ) );
            }
        }
    }
    protected void pushMessage ( final byte[] data, final boolean reconnect, final boolean waitForAck ) throws IOException {
        this.keepalive();
        if ( reconnect ) {
            this.closeSocket();
        }
        if ( !this.isConnected() ) {
            this.openSocket();
        }
        this.soOut.write ( data );
        this.soOut.flush();
        if ( waitForAck ) {
            this.waitForAck();
        }
        SenderState.getSenderState ( this.getDestination() ).setReady();
    }
    protected void waitForAck() throws IOException {
        try {
            boolean ackReceived = false;
            boolean failAckReceived = false;
            this.ackbuf.clear();
            int bytesRead;
            int i;
            for ( bytesRead = 0, i = this.soIn.read(); i != -1 && bytesRead < Constants.ACK_COMMAND.length; i = this.soIn.read() ) {
                ++bytesRead;
                final byte d = ( byte ) i;
                this.ackbuf.append ( d );
                if ( this.ackbuf.doesPackageExist() ) {
                    final byte[] ackcmd = this.ackbuf.extractDataPackage ( true ).getBytes();
                    ackReceived = Arrays.equals ( ackcmd, Constants.ACK_DATA );
                    failAckReceived = Arrays.equals ( ackcmd, Constants.FAIL_ACK_DATA );
                    ackReceived = ( ackReceived || failAckReceived );
                    break;
                }
            }
            if ( !ackReceived ) {
                if ( i == -1 ) {
                    throw new IOException ( BioSender.sm.getString ( "bioSender.ack.eof", this.getAddress(), this.socket.getLocalPort() ) );
                }
                throw new IOException ( BioSender.sm.getString ( "bioSender.ack.wrong", this.getAddress(), this.socket.getLocalPort() ) );
            } else if ( failAckReceived && this.getThrowOnFailedAck() ) {
                throw new RemoteProcessException ( BioSender.sm.getString ( "bioSender.fail.AckReceived" ) );
            }
        } catch ( IOException x ) {
            final String errmsg = BioSender.sm.getString ( "bioSender.ack.missing", this.getAddress(), this.socket.getLocalPort(), this.getTimeout() );
            if ( SenderState.getSenderState ( this.getDestination() ).isReady() ) {
                SenderState.getSenderState ( this.getDestination() ).setSuspect();
                if ( BioSender.log.isWarnEnabled() ) {
                    BioSender.log.warn ( errmsg, x );
                }
            } else if ( BioSender.log.isDebugEnabled() ) {
                BioSender.log.debug ( errmsg, x );
            }
            throw x;
        } finally {
            this.ackbuf.clear();
        }
    }
    static {
        log = LogFactory.getLog ( BioSender.class );
        sm = StringManager.getManager ( BioSender.class );
    }
}
