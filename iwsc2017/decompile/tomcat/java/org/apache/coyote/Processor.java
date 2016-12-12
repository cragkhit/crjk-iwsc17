package org.apache.coyote;
import java.nio.ByteBuffer;
import org.apache.tomcat.util.net.SSLSupport;
import java.io.IOException;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SocketEvent;
import org.apache.tomcat.util.net.SocketWrapperBase;
public interface Processor {
    AbstractEndpoint.Handler.SocketState process ( SocketWrapperBase<?> p0, SocketEvent p1 ) throws IOException;
    UpgradeToken getUpgradeToken();
    boolean isUpgrade();
    boolean isAsync();
    void timeoutAsync ( long p0 );
    Request getRequest();
    void recycle();
    void setSslSupport ( SSLSupport p0 );
    ByteBuffer getLeftoverInput();
    void pause();
}
