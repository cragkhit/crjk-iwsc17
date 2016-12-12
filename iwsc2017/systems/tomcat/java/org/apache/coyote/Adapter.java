package org.apache.coyote;
import org.apache.tomcat.util.net.SocketEvent;
public interface Adapter {
    public void service ( Request req, Response res ) throws Exception;
    public boolean prepare ( Request req, Response res ) throws Exception;
    public boolean asyncDispatch ( Request req, Response res, SocketEvent status )
    throws Exception;
    public void log ( Request req, Response res, long time );
    public void checkRecycled ( Request req, Response res );
    public String getDomain();
}
