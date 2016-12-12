package org.apache.tomcat.util.net;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import org.apache.tomcat.jni.Error;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.jni.Poll;
import org.apache.tomcat.jni.Pool;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.tomcat.jni.SSLSocket;
import org.apache.tomcat.jni.Sockaddr;
import org.apache.tomcat.jni.Address;
import org.apache.tomcat.jni.Status;
import org.apache.tomcat.util.buf.ByteBufferUtils;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;
import java.io.EOFException;
import org.apache.tomcat.jni.OS;
import java.net.SocketTimeoutException;
import org.apache.tomcat.jni.Socket;
import java.io.IOException;
import java.nio.ByteBuffer;
public static class AprSocketWrapper extends SocketWrapperBase<Long> {
    private static final int SSL_OUTPUT_BUFFER_SIZE = 8192;
    private final ByteBuffer sslOutputBuffer;
    private final Object closedLock;
    private volatile boolean closed;
    private int pollerFlags;
    public AprSocketWrapper ( final Long socket, final AprEndpoint endpoint ) {
        super ( socket, endpoint );
        this.closedLock = new Object();
        this.closed = false;
        this.pollerFlags = 0;
        if ( endpoint.isSSLEnabled() ) {
            ( this.sslOutputBuffer = ByteBuffer.allocateDirect ( 8192 ) ).position ( 8192 );
        } else {
            this.sslOutputBuffer = null;
        }
        this.socketBufferHandler = new SocketBufferHandler ( 9000, 9000, true );
    }
    @Override
    public int read ( final boolean block, final byte[] b, final int off, final int len ) throws IOException {
        int nRead = this.populateReadBuffer ( b, off, len );
        if ( nRead > 0 ) {
            return nRead;
        }
        nRead = this.fillReadBuffer ( block );
        if ( nRead > 0 ) {
            this.socketBufferHandler.configureReadBufferForRead();
            nRead = Math.min ( nRead, len );
            this.socketBufferHandler.getReadBuffer().get ( b, off, nRead );
        }
        return nRead;
    }
    @Override
    public int read ( final boolean block, final ByteBuffer to ) throws IOException {
        int nRead = this.populateReadBuffer ( to );
        if ( nRead > 0 ) {
            return nRead;
        }
        final int limit = this.socketBufferHandler.getReadBuffer().capacity();
        if ( to.isDirect() && to.remaining() >= limit ) {
            to.limit ( to.position() + limit );
            nRead = this.fillReadBuffer ( block, to );
        } else {
            nRead = this.fillReadBuffer ( block );
            if ( nRead > 0 ) {
                nRead = this.populateReadBuffer ( to );
            }
        }
        return nRead;
    }
    private int fillReadBuffer ( final boolean block ) throws IOException {
        this.socketBufferHandler.configureReadBufferForWrite();
        return this.fillReadBuffer ( block, this.socketBufferHandler.getReadBuffer() );
    }
    private int fillReadBuffer ( final boolean block, final ByteBuffer to ) throws IOException {
        if ( this.closed ) {
            throw new IOException ( AprSocketWrapper.sm.getString ( "socket.apr.closed", ( ( SocketWrapperBase<Object> ) this ).getSocket() ) );
        }
        final Lock readLock = this.getBlockingStatusReadLock();
        final ReentrantReadWriteLock.WriteLock writeLock = this.getBlockingStatusWriteLock();
        boolean readDone = false;
        int result = 0;
        readLock.lock();
        try {
            if ( this.getBlockingStatus() == block ) {
                if ( block ) {
                    Socket.timeoutSet ( this.getSocket(), this.getReadTimeout() * 1000L );
                }
                result = Socket.recvb ( this.getSocket(), to, to.position(), to.remaining() );
                readDone = true;
            }
        } finally {
            readLock.unlock();
        }
        if ( !readDone ) {
            writeLock.lock();
            try {
                this.setBlockingStatus ( block );
                if ( block ) {
                    Socket.timeoutSet ( this.getSocket(), this.getReadTimeout() * 1000L );
                } else {
                    Socket.timeoutSet ( this.getSocket(), 0L );
                }
                readLock.lock();
                try {
                    writeLock.unlock();
                    result = Socket.recvb ( this.getSocket(), to, to.position(), to.remaining() );
                } finally {
                    readLock.unlock();
                }
            } finally {
                if ( writeLock.isHeldByCurrentThread() ) {
                    writeLock.unlock();
                }
            }
        }
        if ( result > 0 ) {
            to.position ( to.position() + result );
            return result;
        }
        if ( result == 0 || -result == 120002 ) {
            return 0;
        }
        if ( -result == 20014 && this.isSecure() ) {
            if ( AprEndpoint.access$200().isDebugEnabled() ) {
                AprEndpoint.access$200().debug ( AprSocketWrapper.sm.getString ( "socket.apr.read.sslGeneralError", ( ( SocketWrapperBase<Object> ) this ).getSocket(), this ) );
            }
            return 0;
        }
        if ( -result == 120005 || -result == 120001 ) {
            if ( block ) {
                throw new SocketTimeoutException ( AprSocketWrapper.sm.getString ( "iib.readtimeout" ) );
            }
            return 0;
        } else {
            if ( -result == 70014 ) {
                return -1;
            }
            if ( ( OS.IS_WIN32 || OS.IS_WIN64 ) && -result == 730053 ) {
                throw new EOFException ( AprSocketWrapper.sm.getString ( "socket.apr.clientAbort" ) );
            }
            throw new IOException ( AprSocketWrapper.sm.getString ( "socket.apr.read.error", -result, ( ( SocketWrapperBase<Object> ) this ).getSocket(), this ) );
        }
    }
    @Override
    public boolean isReadyForRead() throws IOException {
        this.socketBufferHandler.configureReadBufferForRead();
        if ( this.socketBufferHandler.getReadBuffer().remaining() > 0 ) {
            return true;
        }
        this.fillReadBuffer ( false );
        final boolean isReady = this.socketBufferHandler.getReadBuffer().position() > 0;
        return isReady;
    }
    @Override
    public void close() {
        synchronized ( this.closedLock ) {
            if ( this.closed ) {
                return;
            }
            this.closed = true;
            if ( this.sslOutputBuffer != null ) {
                ByteBufferUtils.cleanDirectBuffer ( this.sslOutputBuffer );
            }
            ( ( AprEndpoint ) this.getEndpoint() ).getPoller().close ( this.getSocket() );
        }
    }
    @Override
    public boolean isClosed() {
        synchronized ( this.closedLock ) {
            return this.closed;
        }
    }
    @Override
    protected void writeByteBufferBlocking ( final ByteBuffer from ) throws IOException {
        if ( from.isDirect() ) {
            super.writeByteBufferBlocking ( from );
        } else {
            final ByteBuffer writeBuffer = this.socketBufferHandler.getWriteBuffer();
            final int limit = writeBuffer.capacity();
            while ( from.remaining() >= limit ) {
                this.socketBufferHandler.configureWriteBufferForWrite();
                SocketWrapperBase.transfer ( from, writeBuffer );
                this.doWrite ( true );
            }
            if ( from.remaining() > 0 ) {
                this.socketBufferHandler.configureWriteBufferForWrite();
                SocketWrapperBase.transfer ( from, writeBuffer );
            }
        }
    }
    @Override
    protected boolean writeByteBufferNonBlocking ( final ByteBuffer from ) throws IOException {
        if ( from.isDirect() ) {
            return super.writeByteBufferNonBlocking ( from );
        }
        final ByteBuffer writeBuffer = this.socketBufferHandler.getWriteBuffer();
        final int limit = writeBuffer.capacity();
        while ( from.remaining() >= limit ) {
            this.socketBufferHandler.configureWriteBufferForWrite();
            SocketWrapperBase.transfer ( from, writeBuffer );
            final int newPosition = writeBuffer.position() + limit;
            this.doWrite ( false );
            if ( writeBuffer.position() != newPosition ) {
                return true;
            }
        }
        if ( from.remaining() > 0 ) {
            this.socketBufferHandler.configureWriteBufferForWrite();
            SocketWrapperBase.transfer ( from, writeBuffer );
        }
        return false;
    }
    @Override
    protected void doWrite ( final boolean block, final ByteBuffer from ) throws IOException {
        if ( this.closed ) {
            throw new IOException ( AprSocketWrapper.sm.getString ( "socket.apr.closed", ( ( SocketWrapperBase<Object> ) this ).getSocket() ) );
        }
        final Lock readLock = this.getBlockingStatusReadLock();
        final ReentrantReadWriteLock.WriteLock writeLock = this.getBlockingStatusWriteLock();
        readLock.lock();
        try {
            if ( this.getBlockingStatus() == block ) {
                if ( block ) {
                    Socket.timeoutSet ( this.getSocket(), this.getWriteTimeout() * 1000L );
                }
                this.doWriteInternal ( from );
                return;
            }
        } finally {
            readLock.unlock();
        }
        writeLock.lock();
        try {
            this.setBlockingStatus ( block );
            if ( block ) {
                Socket.timeoutSet ( this.getSocket(), this.getWriteTimeout() * 1000L );
            } else {
                Socket.timeoutSet ( this.getSocket(), 0L );
            }
            readLock.lock();
            try {
                writeLock.unlock();
                this.doWriteInternal ( from );
            } finally {
                readLock.unlock();
            }
        } finally {
            if ( writeLock.isHeldByCurrentThread() ) {
                writeLock.unlock();
            }
        }
    }
    private void doWriteInternal ( final ByteBuffer from ) throws IOException {
        int thisTime;
        do {
            thisTime = 0;
            if ( this.getEndpoint().isSSLEnabled() ) {
                if ( this.sslOutputBuffer.remaining() == 0 ) {
                    this.sslOutputBuffer.clear();
                    SocketWrapperBase.transfer ( from, this.sslOutputBuffer );
                    this.sslOutputBuffer.flip();
                }
                thisTime = Socket.sendb ( this.getSocket(), this.sslOutputBuffer, this.sslOutputBuffer.position(), this.sslOutputBuffer.limit() );
                if ( thisTime > 0 ) {
                    this.sslOutputBuffer.position ( this.sslOutputBuffer.position() + thisTime );
                }
            } else {
                thisTime = Socket.sendb ( this.getSocket(), from, from.position(), from.remaining() );
                if ( thisTime > 0 ) {
                    from.position ( from.position() + thisTime );
                }
            }
            if ( Status.APR_STATUS_IS_EAGAIN ( -thisTime ) ) {
                thisTime = 0;
            } else {
                if ( -thisTime == 70014 ) {
                    throw new EOFException ( AprSocketWrapper.sm.getString ( "socket.apr.clientAbort" ) );
                }
                if ( ( OS.IS_WIN32 || OS.IS_WIN64 ) && -thisTime == 730053 ) {
                    throw new EOFException ( AprSocketWrapper.sm.getString ( "socket.apr.clientAbort" ) );
                }
                if ( thisTime < 0 ) {
                    throw new IOException ( AprSocketWrapper.sm.getString ( "socket.apr.write.error", -thisTime, ( ( SocketWrapperBase<Object> ) this ).getSocket(), this ) );
                }
                continue;
            }
        } while ( ( thisTime > 0 || this.getBlockingStatus() ) && from.hasRemaining() );
    }
    @Override
    public void registerReadInterest() {
        synchronized ( this.closedLock ) {
            if ( this.closed ) {
                return;
            }
            final Poller p = ( ( AprEndpoint ) this.getEndpoint() ).getPoller();
            if ( p != null ) {
                p.add ( this.getSocket(), this.getReadTimeout(), 1 );
            }
        }
    }
    @Override
    public void registerWriteInterest() {
        synchronized ( this.closedLock ) {
            if ( this.closed ) {
                return;
            }
            ( ( AprEndpoint ) this.getEndpoint() ).getPoller().add ( this.getSocket(), this.getWriteTimeout(), 4 );
        }
    }
    @Override
    public SendfileDataBase createSendfileData ( final String filename, final long pos, final long length ) {
        return new SendfileData ( filename, pos, length );
    }
    @Override
    public SendfileState processSendfile ( final SendfileDataBase sendfileData ) {
        ( ( SendfileData ) sendfileData ).socket = this.getSocket();
        return ( ( AprEndpoint ) this.getEndpoint() ).getSendfile().add ( ( SendfileData ) sendfileData );
    }
    @Override
    protected void populateRemoteAddr() {
        if ( this.closed ) {
            return;
        }
        try {
            final long socket = this.getSocket();
            final long sa = Address.get ( 1, socket );
            this.remoteAddr = Address.getip ( sa );
        } catch ( Exception e ) {
            AprEndpoint.access$200().warn ( AprSocketWrapper.sm.getString ( "endpoint.warn.noRemoteAddr", ( ( SocketWrapperBase<Object> ) this ).getSocket() ), e );
        }
    }
    @Override
    protected void populateRemoteHost() {
        if ( this.closed ) {
            return;
        }
        try {
            final long socket = this.getSocket();
            final long sa = Address.get ( 1, socket );
            this.remoteHost = Address.getnameinfo ( sa, 0 );
            if ( this.remoteAddr == null ) {
                this.remoteAddr = Address.getip ( sa );
            }
        } catch ( Exception e ) {
            AprEndpoint.access$200().warn ( AprSocketWrapper.sm.getString ( "endpoint.warn.noRemoteHost", ( ( SocketWrapperBase<Object> ) this ).getSocket() ), e );
        }
    }
    @Override
    protected void populateRemotePort() {
        if ( this.closed ) {
            return;
        }
        try {
            final long socket = this.getSocket();
            final long sa = Address.get ( 1, socket );
            final Sockaddr addr = Address.getInfo ( sa );
            this.remotePort = addr.port;
        } catch ( Exception e ) {
            AprEndpoint.access$200().warn ( AprSocketWrapper.sm.getString ( "endpoint.warn.noRemotePort", ( ( SocketWrapperBase<Object> ) this ).getSocket() ), e );
        }
    }
    @Override
    protected void populateLocalName() {
        if ( this.closed ) {
            return;
        }
        try {
            final long socket = this.getSocket();
            final long sa = Address.get ( 0, socket );
            this.localName = Address.getnameinfo ( sa, 0 );
        } catch ( Exception e ) {
            AprEndpoint.access$200().warn ( AprSocketWrapper.sm.getString ( "endpoint.warn.noLocalName" ), e );
        }
    }
    @Override
    protected void populateLocalAddr() {
        if ( this.closed ) {
            return;
        }
        try {
            final long socket = this.getSocket();
            final long sa = Address.get ( 0, socket );
            this.localAddr = Address.getip ( sa );
        } catch ( Exception e ) {
            AprEndpoint.access$200().warn ( AprSocketWrapper.sm.getString ( "endpoint.warn.noLocalAddr" ), e );
        }
    }
    @Override
    protected void populateLocalPort() {
        if ( this.closed ) {
            return;
        }
        try {
            final long socket = this.getSocket();
            final long sa = Address.get ( 0, socket );
            final Sockaddr addr = Address.getInfo ( sa );
            this.localPort = addr.port;
        } catch ( Exception e ) {
            AprEndpoint.access$200().warn ( AprSocketWrapper.sm.getString ( "endpoint.warn.noLocalPort" ), e );
        }
    }
    @Override
    public SSLSupport getSslSupport ( final String clientCertProvider ) {
        if ( this.getEndpoint().isSSLEnabled() ) {
            return new AprSSLSupport ( this, clientCertProvider );
        }
        return null;
    }
    @Override
    public void doClientAuth ( final SSLSupport sslSupport ) {
        final long socket = this.getSocket();
        SSLSocket.setVerify ( socket, 2, -1 );
        SSLSocket.renegotiate ( socket );
    }
    @Override
    public void setAppReadBufHandler ( final ApplicationBufferHandler handler ) {
    }
}
