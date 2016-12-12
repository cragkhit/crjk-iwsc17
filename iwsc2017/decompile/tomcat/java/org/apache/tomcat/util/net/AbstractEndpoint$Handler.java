package org.apache.tomcat.util.net;
import java.util.Set;
public interface Handler<S> {
    SocketState process ( SocketWrapperBase<S> p0, SocketEvent p1 );
    Object getGlobal();
    Set<S> getOpenSockets();
    void release ( SocketWrapperBase<S> p0 );
    void pause();
    void recycle();
    public enum SocketState {
        OPEN,
        CLOSED,
        LONG,
        ASYNC_END,
        SENDFILE,
        UPGRADING,
        UPGRADED;
    }
}
