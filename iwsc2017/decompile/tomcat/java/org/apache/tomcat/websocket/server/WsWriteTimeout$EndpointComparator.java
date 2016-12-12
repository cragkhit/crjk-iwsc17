package org.apache.tomcat.websocket.server;
import java.util.Comparator;
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
