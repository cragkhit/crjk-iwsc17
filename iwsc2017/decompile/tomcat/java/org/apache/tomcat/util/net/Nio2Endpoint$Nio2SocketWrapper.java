package org.apache.tomcat.util.net;
import javax.net.ssl.SSLEngine;
import org.apache.tomcat.util.net.jsse.JSSESupport;
import javax.net.ssl.SSLSession;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.nio.file.OpenOption;
import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutionException;
import java.nio.channels.WritePendingException;
import java.nio.channels.ReadPendingException;
import java.util.Iterator;
import org.apache.tomcat.util.buf.ByteBufferHolder;
import java.util.ArrayList;
import java.nio.channels.AsynchronousCloseException;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.io.EOFException;
import java.nio.channels.ClosedChannelException;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.atomic.AtomicInteger;
public static class Nio2SocketWrapper extends SocketWrapperBase<Nio2Channel> {
    private static final ThreadLocal<AtomicInteger> nestedWriteCompletionCount;
    private SendfileData sendfileData;
    private final CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>> readCompletionHandler;
    private final Semaphore readPending;
    private boolean readInterest;
    private final CompletionHandler<Integer, ByteBuffer> writeCompletionHandler;
    private final CompletionHandler<Long, ByteBuffer[]> gatheringWriteCompletionHandler;
    private final Semaphore writePending;
    private boolean writeInterest;
    private boolean writeNotify;
    private CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>> awaitBytesHandler;
    private CompletionHandler<Integer, SendfileData> sendfileHandler;
    public Nio2SocketWrapper ( final Nio2Channel channel, final Nio2Endpoint endpoint ) {
        super ( channel, endpoint );
        this.sendfileData = null;
        this.readPending = new Semaphore ( 1 );
        this.readInterest = false;
        this.writePending = new Semaphore ( 1 );
        this.writeInterest = false;
        this.writeNotify = false;
        this.awaitBytesHandler = new CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>>() {
            @Override
            public void completed ( final Integer nBytes, final SocketWrapperBase<Nio2Channel> attachment ) {
                if ( nBytes < 0 ) {
                    this.failed ( ( Throwable ) new ClosedChannelException(), attachment );
                    return;
                }
                Nio2SocketWrapper.this.getEndpoint().processSocket ( attachment, SocketEvent.OPEN_READ, Nio2Endpoint.isInline() );
            }
            @Override
            public void failed ( final Throwable exc, final SocketWrapperBase<Nio2Channel> attachment ) {
                Nio2SocketWrapper.this.getEndpoint().processSocket ( attachment, SocketEvent.DISCONNECT, true );
            }
        };
        this.sendfileHandler = new CompletionHandler<Integer, SendfileData>() {
            @Override
            public void completed ( final Integer nWrite, final SendfileData attachment ) {
                if ( nWrite < 0 ) {
                    this.failed ( ( Throwable ) new EOFException(), attachment );
                    return;
                }
                attachment.pos += nWrite;
                final ByteBuffer buffer = Nio2SocketWrapper.this.getSocket().getBufHandler().getWriteBuffer();
                if ( !buffer.hasRemaining() ) {
                    if ( attachment.length <= 0L ) {
                        Nio2SocketWrapper.this.setSendfileData ( null );
                        try {
                            attachment.fchannel.close();
                        } catch ( IOException ex ) {}
                        if ( attachment.keepAlive ) {
                            if ( !Nio2Endpoint.isInline() ) {
                                Nio2SocketWrapper.this.awaitBytes();
                            } else {
                                attachment.doneInline = true;
                            }
                        } else if ( !Nio2Endpoint.isInline() ) {
                            Nio2SocketWrapper.this.getEndpoint().processSocket ( Nio2SocketWrapper.this, SocketEvent.DISCONNECT, false );
                        } else {
                            attachment.doneInline = true;
                        }
                        return;
                    }
                    Nio2SocketWrapper.this.getSocket().getBufHandler().configureWriteBufferForWrite();
                    int nRead = -1;
                    try {
                        nRead = attachment.fchannel.read ( buffer );
                    } catch ( IOException e ) {
                        this.failed ( ( Throwable ) e, attachment );
                        return;
                    }
                    if ( nRead <= 0 ) {
                        this.failed ( ( Throwable ) new EOFException(), attachment );
                        return;
                    }
                    Nio2SocketWrapper.this.getSocket().getBufHandler().configureWriteBufferForRead();
                    if ( attachment.length < buffer.remaining() ) {
                        buffer.limit ( buffer.limit() - buffer.remaining() + ( int ) attachment.length );
                    }
                    attachment.length -= nRead;
                }
                Nio2SocketWrapper.this.getSocket().write ( buffer, Nio2SocketWrapper.this.getNio2WriteTimeout(), TimeUnit.MILLISECONDS, attachment, this );
            }
            @Override
            public void failed ( final Throwable exc, final SendfileData attachment ) {
                try {
                    attachment.fchannel.close();
                } catch ( IOException ex ) {}
                if ( !Nio2Endpoint.isInline() ) {
                    Nio2SocketWrapper.this.getEndpoint().processSocket ( Nio2SocketWrapper.this, SocketEvent.ERROR, false );
                } else {
                    attachment.doneInline = true;
                    attachment.error = true;
                }
            }
        };
        this.socketBufferHandler = channel.getBufHandler();
        this.readCompletionHandler = new CompletionHandler<Integer, SocketWrapperBase<Nio2Channel>>() {
            @Override
            public void completed ( final Integer nBytes, final SocketWrapperBase<Nio2Channel> attachment ) {
                boolean notify = false;
                if ( Nio2Endpoint.access$300().isDebugEnabled() ) {
                    Nio2Endpoint.access$300().debug ( "Socket: [" + attachment + "], Interest: [" + Nio2SocketWrapper.this.readInterest + "]" );
                }
                synchronized ( Nio2SocketWrapper.this.readCompletionHandler ) {
                    if ( nBytes < 0 ) {
                        this.failed ( ( Throwable ) new EOFException(), attachment );
                    } else if ( Nio2SocketWrapper.this.readInterest && !Nio2Endpoint.isInline() ) {
                        Nio2SocketWrapper.this.readInterest = false;
                        notify = true;
                    } else {
                        Nio2SocketWrapper.this.readPending.release();
                    }
                }
                if ( notify ) {
                    Nio2SocketWrapper.this.getEndpoint().processSocket ( attachment, SocketEvent.OPEN_READ, false );
                }
            }
            @Override
            public void failed ( final Throwable exc, final SocketWrapperBase<Nio2Channel> attachment ) {
                IOException ioe;
                if ( exc instanceof IOException ) {
                    ioe = ( IOException ) exc;
                } else {
                    ioe = new IOException ( exc );
                }
                Nio2SocketWrapper.this.setError ( ioe );
                if ( exc instanceof AsynchronousCloseException ) {
                    Nio2SocketWrapper.this.readPending.release();
                    return;
                }
                Nio2SocketWrapper.this.getEndpoint().processSocket ( attachment, SocketEvent.ERROR, true );
            }
        };
        this.writeCompletionHandler = new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed ( final Integer nBytes, final ByteBuffer attachment ) {
                Nio2SocketWrapper.this.writeNotify = false;
                synchronized ( Nio2SocketWrapper.this.writeCompletionHandler ) {
                    if ( nBytes < 0 ) {
                        this.failed ( ( Throwable ) new EOFException ( SocketWrapperBase.sm.getString ( "iob.failedwrite" ) ), attachment );
                    } else if ( Nio2SocketWrapper.this.bufferedWrites.size() > 0 ) {
                        Nio2SocketWrapper.nestedWriteCompletionCount.get().incrementAndGet();
                        final ArrayList<ByteBuffer> arrayList = new ArrayList<ByteBuffer>();
                        if ( attachment.hasRemaining() ) {
                            arrayList.add ( attachment );
                        }
                        for ( final ByteBufferHolder buffer : Nio2SocketWrapper.this.bufferedWrites ) {
                            buffer.flip();
                            arrayList.add ( buffer.getBuf() );
                        }
                        Nio2SocketWrapper.this.bufferedWrites.clear();
                        final ByteBuffer[] array = arrayList.toArray ( new ByteBuffer[arrayList.size()] );
                        Nio2SocketWrapper.this.getSocket().write ( array, 0, array.length, Nio2SocketWrapper.this.getNio2WriteTimeout(), TimeUnit.MILLISECONDS, array, Nio2SocketWrapper.this.gatheringWriteCompletionHandler );
                        Nio2SocketWrapper.nestedWriteCompletionCount.get().decrementAndGet();
                    } else if ( attachment.hasRemaining() ) {
                        Nio2SocketWrapper.nestedWriteCompletionCount.get().incrementAndGet();
                        Nio2SocketWrapper.this.getSocket().write ( attachment, Nio2SocketWrapper.this.getNio2WriteTimeout(), TimeUnit.MILLISECONDS, attachment, Nio2SocketWrapper.this.writeCompletionHandler );
                        Nio2SocketWrapper.nestedWriteCompletionCount.get().decrementAndGet();
                    } else {
                        if ( Nio2SocketWrapper.this.writeInterest ) {
                            Nio2SocketWrapper.this.writeInterest = false;
                            Nio2SocketWrapper.this.writeNotify = true;
                        }
                        Nio2SocketWrapper.this.writePending.release();
                    }
                }
                if ( Nio2SocketWrapper.this.writeNotify && Nio2SocketWrapper.nestedWriteCompletionCount.get().get() == 0 ) {
                    endpoint.processSocket ( Nio2SocketWrapper.this, SocketEvent.OPEN_WRITE, Nio2Endpoint.isInline() );
                }
            }
            @Override
            public void failed ( final Throwable exc, final ByteBuffer attachment ) {
                IOException ioe;
                if ( exc instanceof IOException ) {
                    ioe = ( IOException ) exc;
                } else {
                    ioe = new IOException ( exc );
                }
                Nio2SocketWrapper.this.setError ( ioe );
                Nio2SocketWrapper.this.writePending.release();
                endpoint.processSocket ( Nio2SocketWrapper.this, SocketEvent.ERROR, true );
            }
        };
        this.gatheringWriteCompletionHandler = new CompletionHandler<Long, ByteBuffer[]>() {
            @Override
            public void completed ( final Long nBytes, final ByteBuffer[] attachment ) {
                Nio2SocketWrapper.this.writeNotify = false;
                synchronized ( Nio2SocketWrapper.this.writeCompletionHandler ) {
                    if ( nBytes < 0L ) {
                        this.failed ( ( Throwable ) new EOFException ( SocketWrapperBase.sm.getString ( "iob.failedwrite" ) ), attachment );
                    } else if ( Nio2SocketWrapper.this.bufferedWrites.size() > 0 || arrayHasData ( attachment ) ) {
                        Nio2SocketWrapper.nestedWriteCompletionCount.get().incrementAndGet();
                        final ArrayList<ByteBuffer> arrayList = new ArrayList<ByteBuffer>();
                        for ( final ByteBuffer buffer : attachment ) {
                            if ( buffer.hasRemaining() ) {
                                arrayList.add ( buffer );
                            }
                        }
                        for ( final ByteBufferHolder buffer2 : Nio2SocketWrapper.this.bufferedWrites ) {
                            buffer2.flip();
                            arrayList.add ( buffer2.getBuf() );
                        }
                        Nio2SocketWrapper.this.bufferedWrites.clear();
                        final ByteBuffer[] array = arrayList.toArray ( new ByteBuffer[arrayList.size()] );
                        Nio2SocketWrapper.this.getSocket().write ( array, 0, array.length, Nio2SocketWrapper.this.getNio2WriteTimeout(), TimeUnit.MILLISECONDS, array, Nio2SocketWrapper.this.gatheringWriteCompletionHandler );
                        Nio2SocketWrapper.nestedWriteCompletionCount.get().decrementAndGet();
                    } else {
                        if ( Nio2SocketWrapper.this.writeInterest ) {
                            Nio2SocketWrapper.this.writeInterest = false;
                            Nio2SocketWrapper.this.writeNotify = true;
                        }
                        Nio2SocketWrapper.this.writePending.release();
                    }
                }
                if ( Nio2SocketWrapper.this.writeNotify && Nio2SocketWrapper.nestedWriteCompletionCount.get().get() == 0 ) {
                    endpoint.processSocket ( Nio2SocketWrapper.this, SocketEvent.OPEN_WRITE, Nio2Endpoint.isInline() );
                }
            }
            @Override
            public void failed ( final Throwable exc, final ByteBuffer[] attachment ) {
                IOException ioe;
                if ( exc instanceof IOException ) {
                    ioe = ( IOException ) exc;
                } else {
                    ioe = new IOException ( exc );
                }
                Nio2SocketWrapper.this.setError ( ioe );
                Nio2SocketWrapper.this.writePending.release();
                endpoint.processSocket ( Nio2SocketWrapper.this, SocketEvent.ERROR, true );
            }
        };
    }
    private static boolean arrayHasData ( final ByteBuffer[] byteBuffers ) {
        for ( final ByteBuffer byteBuffer : byteBuffers ) {
            if ( byteBuffer.hasRemaining() ) {
                return true;
            }
        }
        return false;
    }
    public void setSendfileData ( final SendfileData sf ) {
        this.sendfileData = sf;
    }
    public SendfileData getSendfileData() {
        return this.sendfileData;
    }
    @Override
    public boolean isReadyForRead() throws IOException {
        synchronized ( this.readCompletionHandler ) {
            if ( !this.readPending.tryAcquire() ) {
                this.readInterest = true;
                return false;
            }
            if ( !this.socketBufferHandler.isReadBufferEmpty() ) {
                this.readPending.release();
                return true;
            }
            final int nRead = this.fillReadBuffer ( false );
            final boolean isReady = nRead > 0;
            if ( !isReady ) {
                this.readInterest = true;
            }
            return isReady;
        }
    }
    @Override
    public int read ( final boolean block, final byte[] b, final int off, final int len ) throws IOException {
        this.checkError();
        if ( Nio2Endpoint.access$300().isDebugEnabled() ) {
            Nio2Endpoint.access$300().debug ( "Socket: [" + this + "], block: [" + block + "], length: [" + len + "]" );
        }
        if ( this.socketBufferHandler == null ) {
            throw new IOException ( Nio2SocketWrapper.sm.getString ( "socket.closed" ) );
        }
        Label_0170: {
            if ( block ) {
                try {
                    this.readPending.acquire();
                    break Label_0170;
                } catch ( InterruptedException e ) {
                    throw new IOException ( e );
                }
            }
            if ( !this.readPending.tryAcquire() ) {
                if ( Nio2Endpoint.access$300().isDebugEnabled() ) {
                    Nio2Endpoint.access$300().debug ( "Socket: [" + this + "], Read in progress. Returning [0]" );
                }
                return 0;
            }
        }
        int nRead = this.populateReadBuffer ( b, off, len );
        if ( nRead > 0 ) {
            this.readPending.release();
            return nRead;
        }
        synchronized ( this.readCompletionHandler ) {
            nRead = this.fillReadBuffer ( block );
            if ( nRead > 0 ) {
                this.socketBufferHandler.configureReadBufferForRead();
                nRead = Math.min ( nRead, len );
                this.socketBufferHandler.getReadBuffer().get ( b, off, nRead );
            } else if ( nRead == 0 && !block ) {
                this.readInterest = true;
            }
            if ( Nio2Endpoint.access$300().isDebugEnabled() ) {
                Nio2Endpoint.access$300().debug ( "Socket: [" + this + "], Read: [" + nRead + "]" );
            }
            return nRead;
        }
    }
    @Override
    public int read ( final boolean block, final ByteBuffer to ) throws IOException {
        this.checkError();
        if ( this.socketBufferHandler == null ) {
            throw new IOException ( Nio2SocketWrapper.sm.getString ( "socket.closed" ) );
        }
        Label_0106: {
            if ( block ) {
                try {
                    this.readPending.acquire();
                    break Label_0106;
                } catch ( InterruptedException e ) {
                    throw new IOException ( e );
                }
            }
            if ( !this.readPending.tryAcquire() ) {
                if ( Nio2Endpoint.access$300().isDebugEnabled() ) {
                    Nio2Endpoint.access$300().debug ( "Socket: [" + this + "], Read in progress. Returning [0]" );
                }
                return 0;
            }
        }
        int nRead = this.populateReadBuffer ( to );
        if ( nRead > 0 ) {
            this.readPending.release();
            return nRead;
        }
        synchronized ( this.readCompletionHandler ) {
            final int limit = this.socketBufferHandler.getReadBuffer().capacity();
            if ( block && to.remaining() >= limit ) {
                to.limit ( to.position() + limit );
                nRead = this.fillReadBuffer ( block, to );
            } else {
                nRead = this.fillReadBuffer ( block );
                if ( nRead > 0 ) {
                    nRead = this.populateReadBuffer ( to );
                } else if ( nRead == 0 && !block ) {
                    this.readInterest = true;
                }
            }
            return nRead;
        }
    }
    @Override
    public void close() throws IOException {
        this.getSocket().close();
    }
    @Override
    public boolean isClosed() {
        return !this.getSocket().isOpen();
    }
    @Override
    public boolean hasAsyncIO() {
        return false;
    }
    @Override
    public <A> CompletionState read ( final ByteBuffer[] dsts, final int offset, final int length, final boolean block, final long timeout, final TimeUnit unit, final A attachment, final CompletionCheck check, final CompletionHandler<Long, ? super A> handler ) {
        final OperationState<A> state = new OperationState<A> ( dsts, offset, length, timeout, unit, ( Object ) attachment, check, ( CompletionHandler ) handler );
        try {
            if ( ( block || !this.readPending.tryAcquire() ) && ( !block || !this.readPending.tryAcquire ( timeout, unit ) ) ) {
                throw new ReadPendingException();
            }
            Nio2Endpoint.startInline();
            this.getSocket().read ( dsts, offset, length, timeout, unit, state, ( CompletionHandler<Long, ? super OperationState<A>> ) new ScatterReadCompletionHandler() );
            Nio2Endpoint.endInline();
            if ( block && ( ( OperationState<Object> ) state ).state == CompletionState.PENDING && this.readPending.tryAcquire ( timeout, unit ) ) {
                this.readPending.release();
            }
        } catch ( InterruptedException e ) {
            handler.failed ( ( Throwable ) e, ( Object ) attachment );
        }
        return ( ( OperationState<Object> ) state ).state;
    }
    @Override
    public boolean isWritePending() {
        synchronized ( this.writeCompletionHandler ) {
            return this.writePending.availablePermits() == 0;
        }
    }
    @Override
    public <A> CompletionState write ( final ByteBuffer[] srcs, final int offset, final int length, final boolean block, final long timeout, final TimeUnit unit, final A attachment, final CompletionCheck check, final CompletionHandler<Long, ? super A> handler ) {
        final OperationState<A> state = new OperationState<A> ( srcs, offset, length, timeout, unit, ( Object ) attachment, check, ( CompletionHandler ) handler );
        try {
            if ( ( block || !this.writePending.tryAcquire() ) && ( !block || !this.writePending.tryAcquire ( timeout, unit ) ) ) {
                throw new WritePendingException();
            }
            Nio2Endpoint.startInline();
            this.getSocket().write ( srcs, offset, length, timeout, unit, state, ( CompletionHandler<Long, ? super OperationState<A>> ) new GatherWriteCompletionHandler() );
            Nio2Endpoint.endInline();
            if ( block && ( ( OperationState<Object> ) state ).state == CompletionState.PENDING && this.writePending.tryAcquire ( timeout, unit ) ) {
                this.writePending.release();
            }
        } catch ( InterruptedException e ) {
            handler.failed ( ( Throwable ) e, ( Object ) attachment );
        }
        return ( ( OperationState<Object> ) state ).state;
    }
    private int fillReadBuffer ( final boolean block ) throws IOException {
        this.socketBufferHandler.configureReadBufferForWrite();
        return this.fillReadBuffer ( block, this.socketBufferHandler.getReadBuffer() );
    }
    private int fillReadBuffer ( final boolean block, final ByteBuffer to ) throws IOException {
        int nRead = 0;
        Future<Integer> integer = null;
        if ( block ) {
            try {
                integer = this.getSocket().read ( to );
                nRead = integer.get ( this.getNio2ReadTimeout(), TimeUnit.MILLISECONDS );
            } catch ( ExecutionException e ) {
                if ( e.getCause() instanceof IOException ) {
                    throw ( IOException ) e.getCause();
                }
                throw new IOException ( e );
            } catch ( InterruptedException e2 ) {
                throw new IOException ( e2 );
            } catch ( TimeoutException e3 ) {
                integer.cancel ( true );
                throw new SocketTimeoutException();
            } finally {
                this.readPending.release();
            }
        } else {
            Nio2Endpoint.startInline();
            this.getSocket().read ( to, this.getNio2ReadTimeout(), TimeUnit.MILLISECONDS, this, this.readCompletionHandler );
            Nio2Endpoint.endInline();
            if ( this.readPending.availablePermits() == 1 ) {
                nRead = to.position();
            }
        }
        return nRead;
    }
    @Override
    protected void writeNonBlocking ( final byte[] buf, int off, int len ) throws IOException {
        synchronized ( this.writeCompletionHandler ) {
            if ( this.writePending.tryAcquire() ) {
                this.socketBufferHandler.configureWriteBufferForWrite();
                final int thisTime = SocketWrapperBase.transfer ( buf, off, len, this.socketBufferHandler.getWriteBuffer() );
                len -= thisTime;
                off += thisTime;
                if ( len > 0 ) {
                    this.addToBuffers ( buf, off, len );
                }
                this.flushNonBlocking ( true );
            } else {
                this.addToBuffers ( buf, off, len );
            }
        }
    }
    @Override
    protected void writeNonBlocking ( final ByteBuffer from ) throws IOException {
        synchronized ( this.writeCompletionHandler ) {
            if ( this.writePending.tryAcquire() ) {
                this.socketBufferHandler.configureWriteBufferForWrite();
                SocketWrapperBase.transfer ( from, this.socketBufferHandler.getWriteBuffer() );
                if ( from.remaining() > 0 ) {
                    this.addToBuffers ( from );
                }
                this.flushNonBlocking ( true );
            } else {
                this.addToBuffers ( from );
            }
        }
    }
    @Override
    protected void doWrite ( final boolean block, final ByteBuffer from ) throws IOException {
        Future<Integer> integer = null;
        try {
            do {
                integer = this.getSocket().write ( from );
                if ( integer.get ( this.getNio2WriteTimeout(), TimeUnit.MILLISECONDS ) < 0 ) {
                    throw new EOFException ( Nio2SocketWrapper.sm.getString ( "iob.failedwrite" ) );
                }
            } while ( from.hasRemaining() );
        } catch ( ExecutionException e ) {
            if ( e.getCause() instanceof IOException ) {
                throw ( IOException ) e.getCause();
            }
            throw new IOException ( e );
        } catch ( InterruptedException e2 ) {
            throw new IOException ( e2 );
        } catch ( TimeoutException e3 ) {
            integer.cancel ( true );
            throw new SocketTimeoutException();
        }
    }
    @Override
    protected void flushBlocking() throws IOException {
        this.checkError();
        try {
            if ( !this.writePending.tryAcquire ( this.getNio2WriteTimeout(), TimeUnit.MILLISECONDS ) ) {
                throw new SocketTimeoutException();
            }
            this.writePending.release();
        } catch ( InterruptedException ex ) {}
        super.flushBlocking();
    }
    @Override
    protected boolean flushNonBlocking() throws IOException {
        return this.flushNonBlocking ( false );
    }
    private boolean flushNonBlocking ( final boolean hasPermit ) throws IOException {
        this.checkError();
        synchronized ( this.writeCompletionHandler ) {
            if ( hasPermit || this.writePending.tryAcquire() ) {
                this.socketBufferHandler.configureWriteBufferForRead();
                if ( this.bufferedWrites.size() > 0 ) {
                    final ArrayList<ByteBuffer> arrayList = new ArrayList<ByteBuffer>();
                    if ( this.socketBufferHandler.getWriteBuffer().hasRemaining() ) {
                        arrayList.add ( this.socketBufferHandler.getWriteBuffer() );
                    }
                    for ( final ByteBufferHolder buffer : this.bufferedWrites ) {
                        buffer.flip();
                        arrayList.add ( buffer.getBuf() );
                    }
                    this.bufferedWrites.clear();
                    final ByteBuffer[] array = arrayList.toArray ( new ByteBuffer[arrayList.size()] );
                    Nio2Endpoint.startInline();
                    this.getSocket().write ( array, 0, array.length, this.getNio2WriteTimeout(), TimeUnit.MILLISECONDS, array, this.gatheringWriteCompletionHandler );
                    Nio2Endpoint.endInline();
                } else if ( this.socketBufferHandler.getWriteBuffer().hasRemaining() ) {
                    Nio2Endpoint.startInline();
                    this.getSocket().write ( this.socketBufferHandler.getWriteBuffer(), this.getNio2WriteTimeout(), TimeUnit.MILLISECONDS, this.socketBufferHandler.getWriteBuffer(), this.writeCompletionHandler );
                    Nio2Endpoint.endInline();
                } else if ( !hasPermit ) {
                    this.writePending.release();
                }
            }
            return this.hasDataToWrite();
        }
    }
    @Override
    public boolean hasDataToWrite() {
        synchronized ( this.writeCompletionHandler ) {
            return !this.socketBufferHandler.isWriteBufferEmpty() || this.bufferedWrites.size() > 0 || this.getError() != null;
        }
    }
    @Override
    public boolean isReadPending() {
        synchronized ( this.readCompletionHandler ) {
            return this.readPending.availablePermits() == 0;
        }
    }
    @Override
    public boolean awaitReadComplete ( final long timeout, final TimeUnit unit ) {
        try {
            if ( this.readPending.tryAcquire ( timeout, unit ) ) {
                this.readPending.release();
            }
        } catch ( InterruptedException e ) {
            return false;
        }
        return true;
    }
    @Override
    public boolean awaitWriteComplete ( final long timeout, final TimeUnit unit ) {
        try {
            if ( this.writePending.tryAcquire ( timeout, unit ) ) {
                this.writePending.release();
            }
        } catch ( InterruptedException e ) {
            return false;
        }
        return true;
    }
    void releaseReadPending() {
        synchronized ( this.readCompletionHandler ) {
            if ( this.readPending.availablePermits() == 0 ) {
                this.readPending.release();
            }
        }
    }
    @Override
    public void registerReadInterest() {
        synchronized ( this.readCompletionHandler ) {
            if ( this.readPending.availablePermits() == 0 ) {
                this.readInterest = true;
            } else {
                this.awaitBytes();
            }
        }
    }
    @Override
    public void registerWriteInterest() {
        synchronized ( this.writeCompletionHandler ) {
            if ( this.writePending.availablePermits() == 0 ) {
                this.writeInterest = true;
            } else {
                this.getEndpoint().processSocket ( this, SocketEvent.OPEN_WRITE, true );
            }
        }
    }
    public void awaitBytes() {
        if ( this.readPending.tryAcquire() ) {
            this.getSocket().getBufHandler().configureReadBufferForWrite();
            Nio2Endpoint.startInline();
            this.getSocket().read ( this.getSocket().getBufHandler().getReadBuffer(), this.getNio2ReadTimeout(), TimeUnit.MILLISECONDS, this, this.awaitBytesHandler );
            Nio2Endpoint.endInline();
        }
    }
    @Override
    public SendfileDataBase createSendfileData ( final String filename, final long pos, final long length ) {
        return new SendfileData ( filename, pos, length );
    }
    @Override
    public SendfileState processSendfile ( final SendfileDataBase sendfileData ) {
        final SendfileData data = ( SendfileData ) sendfileData;
        this.setSendfileData ( data );
        if ( data.fchannel == null || !data.fchannel.isOpen() ) {
            final Path path = new File ( sendfileData.fileName ).toPath();
            try {
                data.fchannel = FileChannel.open ( path, StandardOpenOption.READ ).position ( sendfileData.pos );
            } catch ( IOException e ) {
                return SendfileState.ERROR;
            }
        }
        this.getSocket().getBufHandler().configureWriteBufferForWrite();
        final ByteBuffer buffer = this.getSocket().getBufHandler().getWriteBuffer();
        int nRead = -1;
        try {
            nRead = data.fchannel.read ( buffer );
        } catch ( IOException e2 ) {
            return SendfileState.ERROR;
        }
        if ( nRead < 0 ) {
            return SendfileState.ERROR;
        }
        final SendfileData sendfileData2 = data;
        sendfileData2.length -= nRead;
        this.getSocket().getBufHandler().configureWriteBufferForRead();
        Nio2Endpoint.startInline();
        this.getSocket().write ( buffer, this.getNio2WriteTimeout(), TimeUnit.MILLISECONDS, data, this.sendfileHandler );
        Nio2Endpoint.endInline();
        if ( !data.doneInline ) {
            return SendfileState.PENDING;
        }
        if ( data.error ) {
            return SendfileState.ERROR;
        }
        return SendfileState.DONE;
    }
    private long getNio2ReadTimeout() {
        final long readTimeout = this.getReadTimeout();
        if ( readTimeout > 0L ) {
            return readTimeout;
        }
        return Long.MAX_VALUE;
    }
    private long getNio2WriteTimeout() {
        final long writeTimeout = this.getWriteTimeout();
        if ( writeTimeout > 0L ) {
            return writeTimeout;
        }
        return Long.MAX_VALUE;
    }
    @Override
    protected void populateRemoteAddr() {
        SocketAddress socketAddress = null;
        try {
            socketAddress = this.getSocket().getIOChannel().getRemoteAddress();
        } catch ( IOException ex ) {}
        if ( socketAddress instanceof InetSocketAddress ) {
            this.remoteAddr = ( ( InetSocketAddress ) socketAddress ).getAddress().getHostAddress();
        }
    }
    @Override
    protected void populateRemoteHost() {
        SocketAddress socketAddress = null;
        try {
            socketAddress = this.getSocket().getIOChannel().getRemoteAddress();
        } catch ( IOException e ) {
            Nio2Endpoint.access$300().warn ( Nio2SocketWrapper.sm.getString ( "endpoint.warn.noRemoteHost", ( ( SocketWrapperBase<Object> ) this ).getSocket() ), e );
        }
        if ( socketAddress instanceof InetSocketAddress ) {
            this.remoteHost = ( ( InetSocketAddress ) socketAddress ).getAddress().getHostName();
            if ( this.remoteAddr == null ) {
                this.remoteAddr = ( ( InetSocketAddress ) socketAddress ).getAddress().getHostAddress();
            }
        }
    }
    @Override
    protected void populateRemotePort() {
        SocketAddress socketAddress = null;
        try {
            socketAddress = this.getSocket().getIOChannel().getRemoteAddress();
        } catch ( IOException e ) {
            Nio2Endpoint.access$300().warn ( Nio2SocketWrapper.sm.getString ( "endpoint.warn.noRemotePort", ( ( SocketWrapperBase<Object> ) this ).getSocket() ), e );
        }
        if ( socketAddress instanceof InetSocketAddress ) {
            this.remotePort = ( ( InetSocketAddress ) socketAddress ).getPort();
        }
    }
    @Override
    protected void populateLocalName() {
        SocketAddress socketAddress = null;
        try {
            socketAddress = this.getSocket().getIOChannel().getLocalAddress();
        } catch ( IOException e ) {
            Nio2Endpoint.access$300().warn ( Nio2SocketWrapper.sm.getString ( "endpoint.warn.noLocalName", ( ( SocketWrapperBase<Object> ) this ).getSocket() ), e );
        }
        if ( socketAddress instanceof InetSocketAddress ) {
            this.localName = ( ( InetSocketAddress ) socketAddress ).getHostName();
        }
    }
    @Override
    protected void populateLocalAddr() {
        SocketAddress socketAddress = null;
        try {
            socketAddress = this.getSocket().getIOChannel().getLocalAddress();
        } catch ( IOException e ) {
            Nio2Endpoint.access$300().warn ( Nio2SocketWrapper.sm.getString ( "endpoint.warn.noLocalAddr", ( ( SocketWrapperBase<Object> ) this ).getSocket() ), e );
        }
        if ( socketAddress instanceof InetSocketAddress ) {
            this.localAddr = ( ( InetSocketAddress ) socketAddress ).getAddress().getHostAddress();
        }
    }
    @Override
    protected void populateLocalPort() {
        SocketAddress socketAddress = null;
        try {
            socketAddress = this.getSocket().getIOChannel().getLocalAddress();
        } catch ( IOException e ) {
            Nio2Endpoint.access$300().warn ( Nio2SocketWrapper.sm.getString ( "endpoint.warn.noLocalPort", ( ( SocketWrapperBase<Object> ) this ).getSocket() ), e );
        }
        if ( socketAddress instanceof InetSocketAddress ) {
            this.localPort = ( ( InetSocketAddress ) socketAddress ).getPort();
        }
    }
    @Override
    public SSLSupport getSslSupport ( final String clientCertProvider ) {
        if ( this.getSocket() instanceof SecureNio2Channel ) {
            final SecureNio2Channel ch = ( ( SocketWrapperBase<SecureNio2Channel> ) this ).getSocket();
            final SSLSession session = ch.getSslEngine().getSession();
            return ( ( Nio2Endpoint ) this.getEndpoint() ).getSslImplementation().getSSLSupport ( session );
        }
        return null;
    }
    @Override
    public void doClientAuth ( final SSLSupport sslSupport ) {
        final SecureNio2Channel sslChannel = ( ( SocketWrapperBase<SecureNio2Channel> ) this ).getSocket();
        final SSLEngine engine = sslChannel.getSslEngine();
        if ( !engine.getNeedClientAuth() ) {
            engine.setNeedClientAuth ( true );
            try {
                sslChannel.rehandshake();
                ( ( JSSESupport ) sslSupport ).setSession ( engine.getSession() );
            } catch ( IOException ioe ) {
                Nio2Endpoint.access$300().warn ( Nio2SocketWrapper.sm.getString ( "socket.sslreneg" ), ioe );
            }
        }
    }
    @Override
    public void setAppReadBufHandler ( final ApplicationBufferHandler handler ) {
        this.getSocket().setAppReadBufHandler ( handler );
    }
    static {
        nestedWriteCompletionCount = new ThreadLocal<AtomicInteger>() {
            @Override
            protected AtomicInteger initialValue() {
                return new AtomicInteger ( 0 );
            }
        };
    }
    private static class OperationState<A> {
        private final ByteBuffer[] buffers;
        private final int offset;
        private final int length;
        private final A attachment;
        private final long timeout;
        private final TimeUnit unit;
        private final CompletionCheck check;
        private final CompletionHandler<Long, ? super A> handler;
        private volatile long nBytes;
        private volatile CompletionState state;
        private OperationState ( final ByteBuffer[] buffers, final int offset, final int length, final long timeout, final TimeUnit unit, final A attachment, final CompletionCheck check, final CompletionHandler<Long, ? super A> handler ) {
            this.nBytes = 0L;
            this.state = CompletionState.PENDING;
            this.buffers = buffers;
            this.offset = offset;
            this.length = length;
            this.timeout = timeout;
            this.unit = unit;
            this.attachment = attachment;
            this.check = check;
            this.handler = handler;
        }
    }
    private class ScatterReadCompletionHandler<A> implements CompletionHandler<Long, OperationState<A>> {
        @Override
        public void completed ( final Long nBytes, final OperationState<A> state ) {
            if ( ( int ) ( Object ) nBytes < 0 ) {
                this.failed ( ( Throwable ) new EOFException(), state );
            } else {
                ( ( OperationState<Object> ) state ).nBytes += nBytes;
                final CompletionState currentState = Nio2Endpoint.isInline() ? CompletionState.INLINE : CompletionState.DONE;
                boolean complete = true;
                boolean completion = true;
                if ( ( ( OperationState<Object> ) state ).check != null ) {
                    switch ( ( ( OperationState<Object> ) state ).check.callHandler ( currentState, ( ( OperationState<Object> ) state ).buffers, ( ( OperationState<Object> ) state ).offset, ( ( OperationState<Object> ) state ).length ) ) {
                    case CONTINUE: {
                        complete = false;
                    }
                    case NONE: {
                        completion = false;
                        break;
                    }
                    }
                }
                if ( complete ) {
                    Nio2SocketWrapper.this.readPending.release();
                    ( ( OperationState<Object> ) state ).state = currentState;
                    if ( completion && ( ( OperationState<Object> ) state ).handler != null ) {
                        ( ( OperationState<Object> ) state ).handler.completed ( ( ( OperationState<Object> ) state ).nBytes, ( ( OperationState<Object> ) state ).attachment );
                    }
                } else {
                    Nio2SocketWrapper.this.getSocket().read ( ( ( OperationState<Object> ) state ).buffers, ( ( OperationState<Object> ) state ).offset, ( ( OperationState<Object> ) state ).length, ( ( OperationState<Object> ) state ).timeout, ( ( OperationState<Object> ) state ).unit, state, this );
                }
            }
        }
        @Override
        public void failed ( final Throwable exc, final OperationState<A> state ) {
            IOException ioe;
            if ( exc instanceof IOException ) {
                ioe = ( IOException ) exc;
            } else {
                ioe = new IOException ( exc );
            }
            Nio2SocketWrapper.this.setError ( ioe );
            Nio2SocketWrapper.this.readPending.release();
            if ( exc instanceof AsynchronousCloseException ) {
                return;
            }
            ( ( OperationState<Object> ) state ).state = ( Nio2Endpoint.isInline() ? CompletionState.ERROR : CompletionState.DONE );
            if ( ( ( OperationState<Object> ) state ).handler != null ) {
                ( ( OperationState<Object> ) state ).handler.failed ( ioe, ( ( OperationState<Object> ) state ).attachment );
            }
        }
    }
    private class GatherWriteCompletionHandler<A> implements CompletionHandler<Long, OperationState<A>> {
        @Override
        public void completed ( final Long nBytes, final OperationState<A> state ) {
            if ( nBytes < 0L ) {
                this.failed ( ( Throwable ) new EOFException(), state );
            } else {
                ( ( OperationState<Object> ) state ).nBytes += nBytes;
                final CompletionState currentState = Nio2Endpoint.isInline() ? CompletionState.INLINE : CompletionState.DONE;
                boolean complete = true;
                boolean completion = true;
                if ( ( ( OperationState<Object> ) state ).check != null ) {
                    switch ( ( ( OperationState<Object> ) state ).check.callHandler ( currentState, ( ( OperationState<Object> ) state ).buffers, ( ( OperationState<Object> ) state ).offset, ( ( OperationState<Object> ) state ).length ) ) {
                    case CONTINUE: {
                        complete = false;
                    }
                    case NONE: {
                        completion = false;
                        break;
                    }
                    }
                }
                if ( complete ) {
                    Nio2SocketWrapper.this.writePending.release();
                    ( ( OperationState<Object> ) state ).state = currentState;
                    if ( completion && ( ( OperationState<Object> ) state ).handler != null ) {
                        ( ( OperationState<Object> ) state ).handler.completed ( ( ( OperationState<Object> ) state ).nBytes, ( ( OperationState<Object> ) state ).attachment );
                    }
                } else {
                    Nio2SocketWrapper.this.getSocket().write ( ( ( OperationState<Object> ) state ).buffers, ( ( OperationState<Object> ) state ).offset, ( ( OperationState<Object> ) state ).length, ( ( OperationState<Object> ) state ).timeout, ( ( OperationState<Object> ) state ).unit, state, this );
                }
            }
        }
        @Override
        public void failed ( final Throwable exc, final OperationState<A> state ) {
            IOException ioe;
            if ( exc instanceof IOException ) {
                ioe = ( IOException ) exc;
            } else {
                ioe = new IOException ( exc );
            }
            Nio2SocketWrapper.this.setError ( ioe );
            Nio2SocketWrapper.this.writePending.release();
            ( ( OperationState<Object> ) state ).state = ( Nio2Endpoint.isInline() ? CompletionState.ERROR : CompletionState.DONE );
            if ( ( ( OperationState<Object> ) state ).handler != null ) {
                ( ( OperationState<Object> ) state ).handler.failed ( ioe, ( ( OperationState<Object> ) state ).attachment );
            }
        }
    }
}
