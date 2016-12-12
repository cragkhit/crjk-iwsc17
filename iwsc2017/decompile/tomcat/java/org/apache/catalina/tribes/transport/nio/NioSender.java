package org.apache.catalina.tribes.transport.nio;
import org.apache.juli.logging.LogFactory;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.io.EOFException;
import org.apache.catalina.tribes.RemoteProcessException;
import java.util.Arrays;
import org.apache.catalina.tribes.transport.Constants;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import org.apache.catalina.tribes.io.XByteBuffer;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.Selector;
import org.apache.catalina.tribes.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.catalina.tribes.transport.AbstractSender;
public class NioSender extends AbstractSender {
    private static final Log log;
    protected static final StringManager sm;
    protected Selector selector;
    protected SocketChannel socketChannel;
    protected DatagramChannel dataChannel;
    protected ByteBuffer readbuf;
    protected ByteBuffer writebuf;
    protected volatile byte[] current;
    protected final XByteBuffer ackbuf;
    protected int remaining;
    protected boolean complete;
    protected boolean connecting;
    public NioSender() {
        this.socketChannel = null;
        this.dataChannel = null;
        this.readbuf = null;
        this.writebuf = null;
        this.current = null;
        this.ackbuf = new XByteBuffer ( 128, true );
        this.remaining = 0;
        this.connecting = false;
    }
    public boolean process ( final SelectionKey key, final boolean waitForAck ) throws IOException {
        final int ops = key.readyOps();
        key.interestOps ( key.interestOps() & ~ops );
        if ( !this.isConnected() && !this.connecting ) {
            throw new IOException ( NioSender.sm.getString ( "nioSender.sender.disconnected" ) );
        }
        if ( !key.isValid() ) {
            throw new IOException ( NioSender.sm.getString ( "nioSender.key.inValid" ) );
        }
        if ( !key.isConnectable() ) {
            if ( key.isWritable() ) {
                final boolean writecomplete = this.write();
                if ( writecomplete ) {
                    if ( !waitForAck ) {
                        this.read();
                        this.setRequestCount ( this.getRequestCount() + 1 );
                        return true;
                    }
                    key.interestOps ( key.interestOps() | 0x1 );
                } else {
                    key.interestOps ( key.interestOps() | 0x4 );
                }
            } else {
                if ( !key.isReadable() ) {
                    NioSender.log.warn ( NioSender.sm.getString ( "nioSender.unknown.state", Integer.toString ( ops ) ) );
                    throw new IOException ( NioSender.sm.getString ( "nioSender.unknown.state", Integer.toString ( ops ) ) );
                }
                final boolean readcomplete = this.read();
                if ( readcomplete ) {
                    this.setRequestCount ( this.getRequestCount() + 1 );
                    return true;
                }
                key.interestOps ( key.interestOps() | 0x1 );
            }
            return false;
        }
        if ( this.socketChannel.finishConnect() ) {
            this.completeConnect();
            if ( this.current != null ) {
                key.interestOps ( key.interestOps() | 0x4 );
            }
            return false;
        }
        key.interestOps ( key.interestOps() | 0x8 );
        return false;
    }
    private void configureSocket() throws IOException {
        if ( this.socketChannel != null ) {
            this.socketChannel.configureBlocking ( false );
            this.socketChannel.socket().setSendBufferSize ( this.getTxBufSize() );
            this.socketChannel.socket().setReceiveBufferSize ( this.getRxBufSize() );
            this.socketChannel.socket().setSoTimeout ( ( int ) this.getTimeout() );
            this.socketChannel.socket().setSoLinger ( this.getSoLingerOn(), this.getSoLingerOn() ? this.getSoLingerTime() : 0 );
            this.socketChannel.socket().setTcpNoDelay ( this.getTcpNoDelay() );
            this.socketChannel.socket().setKeepAlive ( this.getSoKeepAlive() );
            this.socketChannel.socket().setReuseAddress ( this.getSoReuseAddress() );
            this.socketChannel.socket().setOOBInline ( this.getOoBInline() );
            this.socketChannel.socket().setSoLinger ( this.getSoLingerOn(), this.getSoLingerTime() );
            this.socketChannel.socket().setTrafficClass ( this.getSoTrafficClass() );
        } else if ( this.dataChannel != null ) {
            this.dataChannel.configureBlocking ( false );
            this.dataChannel.socket().setSendBufferSize ( this.getUdpTxBufSize() );
            this.dataChannel.socket().setReceiveBufferSize ( this.getUdpRxBufSize() );
            this.dataChannel.socket().setSoTimeout ( ( int ) this.getTimeout() );
            this.dataChannel.socket().setReuseAddress ( this.getSoReuseAddress() );
            this.dataChannel.socket().setTrafficClass ( this.getSoTrafficClass() );
        }
    }
    private void completeConnect() {
        this.setConnected ( true );
        this.connecting = false;
        this.setRequestCount ( 0 );
        this.setConnectTime ( System.currentTimeMillis() );
    }
    protected boolean read() throws IOException {
        if ( this.current == null ) {
            return true;
        }
        final int read = this.isUdpBased() ? this.dataChannel.read ( this.readbuf ) : this.socketChannel.read ( this.readbuf );
        if ( read == -1 ) {
            throw new IOException ( NioSender.sm.getString ( "nioSender.unable.receive.ack" ) );
        }
        if ( read == 0 ) {
            return false;
        }
        this.readbuf.flip();
        this.ackbuf.append ( this.readbuf, read );
        this.readbuf.clear();
        if ( !this.ackbuf.doesPackageExist() ) {
            return false;
        }
        final byte[] ackcmd = this.ackbuf.extractDataPackage ( true ).getBytes();
        final boolean ack = Arrays.equals ( ackcmd, Constants.ACK_DATA );
        final boolean fack = Arrays.equals ( ackcmd, Constants.FAIL_ACK_DATA );
        if ( fack && this.getThrowOnFailedAck() ) {
            throw new RemoteProcessException ( NioSender.sm.getString ( "nioSender.receive.failedAck" ) );
        }
        return ack || fack;
    }
    protected boolean write() throws IOException {
        if ( !this.isConnected() || ( this.socketChannel == null && this.dataChannel == null ) ) {
            throw new IOException ( NioSender.sm.getString ( "nioSender.not.connected" ) );
        }
        if ( this.current != null ) {
            if ( this.remaining > 0 ) {
                final int byteswritten = this.isUdpBased() ? this.dataChannel.write ( this.writebuf ) : this.socketChannel.write ( this.writebuf );
                if ( byteswritten == -1 ) {
                    throw new EOFException();
                }
                this.remaining -= byteswritten;
                if ( this.remaining < 0 ) {
                    this.remaining = 0;
                }
            }
            return this.remaining == 0;
        }
        return true;
    }
    @Override
    public synchronized void connect() throws IOException {
        if ( this.connecting || this.isConnected() ) {
            return;
        }
        this.connecting = true;
        if ( this.isConnected() ) {
            throw new IOException ( NioSender.sm.getString ( "nioSender.already.connected" ) );
        }
        if ( this.readbuf == null ) {
            this.readbuf = this.getReadBuffer();
        } else {
            this.readbuf.clear();
        }
        if ( this.writebuf == null ) {
            this.writebuf = this.getWriteBuffer();
        } else {
            this.writebuf.clear();
        }
        if ( this.isUdpBased() ) {
            final InetSocketAddress daddr = new InetSocketAddress ( this.getAddress(), this.getUdpPort() );
            if ( this.dataChannel != null ) {
                throw new IOException ( NioSender.sm.getString ( "nioSender.datagram.already.established" ) );
            }
            this.dataChannel = DatagramChannel.open();
            this.configureSocket();
            this.dataChannel.connect ( daddr );
            this.completeConnect();
            this.dataChannel.register ( this.getSelector(), 4, this );
        } else {
            final InetSocketAddress addr = new InetSocketAddress ( this.getAddress(), this.getPort() );
            if ( this.socketChannel != null ) {
                throw new IOException ( NioSender.sm.getString ( "nioSender.socketChannel.already.established" ) );
            }
            this.socketChannel = SocketChannel.open();
            this.configureSocket();
            if ( this.socketChannel.connect ( addr ) ) {
                this.completeConnect();
                this.socketChannel.register ( this.getSelector(), 4, this );
            } else {
                this.socketChannel.register ( this.getSelector(), 8, this );
            }
        }
    }
    @Override
    public void disconnect() {
        try {
            this.setConnected ( this.connecting = false );
            if ( this.socketChannel != null ) {
                try {
                    try {
                        this.socketChannel.socket().close();
                    } catch ( Exception ex ) {}
                    try {
                        this.socketChannel.close();
                    } catch ( Exception ex2 ) {}
                } finally {
                    this.socketChannel = null;
                }
            }
            if ( this.dataChannel != null ) {
                try {
                    try {
                        this.dataChannel.socket().close();
                    } catch ( Exception ex3 ) {}
                    try {
                        this.dataChannel.close();
                    } catch ( Exception ex4 ) {}
                } finally {
                    this.dataChannel = null;
                }
            }
        } catch ( Exception x ) {
            NioSender.log.error ( NioSender.sm.getString ( "nioSender.unable.disconnect", x.getMessage() ) );
            if ( NioSender.log.isDebugEnabled() ) {
                NioSender.log.debug ( NioSender.sm.getString ( "nioSender.unable.disconnect", x.getMessage() ), x );
            }
        }
    }
    public void reset() {
        if ( this.isConnected() && this.readbuf == null ) {
            this.readbuf = this.getReadBuffer();
        }
        if ( this.readbuf != null ) {
            this.readbuf.clear();
        }
        if ( this.writebuf != null ) {
            this.writebuf.clear();
        }
        this.current = null;
        this.ackbuf.clear();
        this.remaining = 0;
        this.complete = false;
        this.setAttempt ( 0 );
        this.setUdpBased ( false );
    }
    private ByteBuffer getReadBuffer() {
        return this.getBuffer ( this.getRxBufSize() );
    }
    private ByteBuffer getWriteBuffer() {
        return this.getBuffer ( this.getTxBufSize() );
    }
    private ByteBuffer getBuffer ( final int size ) {
        return this.getDirectBuffer() ? ByteBuffer.allocateDirect ( size ) : ByteBuffer.allocate ( size );
    }
    public void setMessage ( final byte[] data ) throws IOException {
        this.setMessage ( data, 0, data.length );
    }
    public void setMessage ( final byte[] data, final int offset, final int length ) throws IOException {
        if ( data != null ) {
            synchronized ( this ) {
                this.current = data;
                this.remaining = length;
                this.ackbuf.clear();
                if ( this.writebuf != null ) {
                    this.writebuf.clear();
                } else {
                    this.writebuf = this.getBuffer ( length );
                }
                if ( this.writebuf.capacity() < length ) {
                    this.writebuf = this.getBuffer ( length );
                }
                this.writebuf.put ( data, offset, length );
                this.writebuf.flip();
                if ( this.isConnected() ) {
                    if ( this.isUdpBased() ) {
                        this.dataChannel.register ( this.getSelector(), 4, this );
                    } else {
                        this.socketChannel.register ( this.getSelector(), 4, this );
                    }
                }
            }
        }
    }
    public byte[] getMessage() {
        return this.current;
    }
    public boolean isComplete() {
        return this.complete;
    }
    public Selector getSelector() {
        return this.selector;
    }
    public void setSelector ( final Selector selector ) {
        this.selector = selector;
    }
    public void setComplete ( final boolean complete ) {
        this.complete = complete;
    }
    static {
        log = LogFactory.getLog ( NioSender.class );
        sm = StringManager.getManager ( NioSender.class );
    }
}
