package org.apache.tomcat.websocket.server;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.tomcat.websocket.BackgroundProcess;
import org.apache.tomcat.websocket.BackgroundProcessManager;
public class WsWriteTimeout implements BackgroundProcess {
    private final Set<WsRemoteEndpointImplServer> endpoints =
        new ConcurrentSkipListSet<> ( new EndpointComparator() );
    private final AtomicInteger count = new AtomicInteger ( 0 );
    private int backgroundProcessCount = 0;
    private volatile int processPeriod = 1;
    @Override
    public void backgroundProcess() {
        backgroundProcessCount ++;
        if ( backgroundProcessCount >= processPeriod ) {
            backgroundProcessCount = 0;
            long now = System.currentTimeMillis();
            Iterator<WsRemoteEndpointImplServer> iter = endpoints.iterator();
            while ( iter.hasNext() ) {
                WsRemoteEndpointImplServer endpoint = iter.next();
                if ( endpoint.getTimeoutExpiry() < now ) {
                    endpoint.onTimeout ( false );
                } else {
                    break;
                }
            }
        }
    }
    @Override
    public void setProcessPeriod ( int period ) {
        this.processPeriod = period;
    }
    @Override
    public int getProcessPeriod() {
        return processPeriod;
    }
    public void register ( WsRemoteEndpointImplServer endpoint ) {
        boolean result = endpoints.add ( endpoint );
        if ( result ) {
            int newCount = count.incrementAndGet();
            if ( newCount == 1 ) {
                BackgroundProcessManager.getInstance().register ( this );
            }
        }
    }
    public void unregister ( WsRemoteEndpointImplServer endpoint ) {
        boolean result = endpoints.remove ( endpoint );
        if ( result ) {
            int newCount = count.decrementAndGet();
            if ( newCount == 0 ) {
                BackgroundProcessManager.getInstance().unregister ( this );
            }
        }
    }
    private static class EndpointComparator implements
        Comparator<WsRemoteEndpointImplServer> {
        @Override
        public int compare ( WsRemoteEndpointImplServer o1,
                             WsRemoteEndpointImplServer o2 ) {
            long t1 = o1.getTimeoutExpiry();
            long t2 = o2.getTimeoutExpiry();
            if ( t1 < t2 ) {
                return -1;
            } else if ( t1 == t2 ) {
                return 0;
            } else {
                return 1;
            }
        }
    }
}
