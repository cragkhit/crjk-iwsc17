package org.apache.catalina.tribes.io;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
class BufferPool15Impl implements BufferPool.BufferPoolAPI {
    protected int maxSize;
    protected final AtomicInteger size;
    protected final ConcurrentLinkedQueue<XByteBuffer> queue;
    BufferPool15Impl() {
        this.size = new AtomicInteger ( 0 );
        this.queue = new ConcurrentLinkedQueue<XByteBuffer>();
    }
    @Override
    public void setMaxSize ( final int bytes ) {
        this.maxSize = bytes;
    }
    @Override
    public XByteBuffer getBuffer ( final int minSize, final boolean discard ) {
        XByteBuffer buffer = this.queue.poll();
        if ( buffer != null ) {
            this.size.addAndGet ( -buffer.getCapacity() );
        }
        if ( buffer == null ) {
            buffer = new XByteBuffer ( minSize, discard );
        } else if ( buffer.getCapacity() <= minSize ) {
            buffer.expand ( minSize );
        }
        buffer.setDiscard ( discard );
        buffer.reset();
        return buffer;
    }
    @Override
    public void returnBuffer ( final XByteBuffer buffer ) {
        if ( this.size.get() + buffer.getCapacity() <= this.maxSize ) {
            this.size.addAndGet ( buffer.getCapacity() );
            this.queue.offer ( buffer );
        }
    }
    @Override
    public void clear() {
        this.queue.clear();
        this.size.set ( 0 );
    }
    public int getMaxSize() {
        return this.maxSize;
    }
}
