package org.apache.coyote;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.tomcat.util.collections.SynchronizedStack;
protected static class RecycledProcessors extends SynchronizedStack<Processor> {
    private final transient ConnectionHandler<?> handler;
    protected final AtomicInteger size;
    public RecycledProcessors ( final ConnectionHandler<?> handler ) {
        this.size = new AtomicInteger ( 0 );
        this.handler = handler;
    }
    @Override
    public boolean push ( final Processor processor ) {
        final int cacheSize = this.handler.getProtocol().getProcessorCache();
        final boolean offer = cacheSize == -1 || this.size.get() < cacheSize;
        boolean result = false;
        if ( offer ) {
            result = super.push ( processor );
            if ( result ) {
                this.size.incrementAndGet();
            }
        }
        if ( !result ) {
            this.handler.unregister ( processor );
        }
        return result;
    }
    @Override
    public Processor pop() {
        final Processor result = super.pop();
        if ( result != null ) {
            this.size.decrementAndGet();
        }
        return result;
    }
    @Override
    public synchronized void clear() {
        for ( Processor next = this.pop(); next != null; next = this.pop() ) {
            this.handler.unregister ( next );
        }
        super.clear();
        this.size.set ( 0 );
    }
}
