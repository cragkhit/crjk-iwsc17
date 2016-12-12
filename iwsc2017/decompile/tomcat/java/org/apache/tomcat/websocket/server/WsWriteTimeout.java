package org.apache.tomcat.websocket.server;
import org.apache.tomcat.websocket.BackgroundProcessManager;
import java.util.Iterator;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Set;
import org.apache.tomcat.websocket.BackgroundProcess;
public class WsWriteTimeout implements BackgroundProcess {
    private final Set<WsRemoteEndpointImplServer> endpoints;
    private final AtomicInteger count;
    private int backgroundProcessCount;
    private volatile int processPeriod;
    public WsWriteTimeout() {
        this.endpoints = new ConcurrentSkipListSet<WsRemoteEndpointImplServer> ( new EndpointComparator() );
        this.count = new AtomicInteger ( 0 );
        this.backgroundProcessCount = 0;
        this.processPeriod = 1;
    }
    @Override
    public void backgroundProcess() {
        ++this.backgroundProcessCount;
        if ( this.backgroundProcessCount >= this.processPeriod ) {
            this.backgroundProcessCount = 0;
            final long now = System.currentTimeMillis();
            for ( final WsRemoteEndpointImplServer endpoint : this.endpoints ) {
                if ( endpoint.getTimeoutExpiry() >= now ) {
                    break;
                }
                endpoint.onTimeout ( false );
            }
        }
    }
    @Override
    public void setProcessPeriod ( final int period ) {
        this.processPeriod = period;
    }
    @Override
    public int getProcessPeriod() {
        return this.processPeriod;
    }
    public void register ( final WsRemoteEndpointImplServer endpoint ) {
        final boolean result = this.endpoints.add ( endpoint );
        if ( result ) {
            final int newCount = this.count.incrementAndGet();
            if ( newCount == 1 ) {
                BackgroundProcessManager.getInstance().register ( this );
            }
        }
    }
    public void unregister ( final WsRemoteEndpointImplServer endpoint ) {
        final boolean result = this.endpoints.remove ( endpoint );
        if ( result ) {
            final int newCount = this.count.decrementAndGet();
            if ( newCount == 0 ) {
                BackgroundProcessManager.getInstance().unregister ( this );
            }
        }
    }
    private static class EndpointComparator implements Comparator<WsRemoteEndpointImplServer> {
        @Override
        public int compare ( final WsRemoteEndpointImplServer o1, final WsRemoteEndpointImplServer o2 ) {
            final long t1 = o1.getTimeoutExpiry();
            final long t2 = o2.getTimeoutExpiry();
            if ( t1 < t2 ) {
                return -1;
            }
            if ( t1 == t2 ) {
                return 0;
            }
            return 1;
        }
    }
}
