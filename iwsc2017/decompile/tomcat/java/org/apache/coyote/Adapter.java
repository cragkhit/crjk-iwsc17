package org.apache.coyote;
import org.apache.tomcat.util.net.SocketEvent;
public interface Adapter {
    void service ( Request p0, Response p1 ) throws Exception;
    boolean prepare ( Request p0, Response p1 ) throws Exception;
    boolean asyncDispatch ( Request p0, Response p1, SocketEvent p2 ) throws Exception;
    void log ( Request p0, Response p1, long p2 );
    void checkRecycled ( Request p0, Response p1 );
    String getDomain();
}
