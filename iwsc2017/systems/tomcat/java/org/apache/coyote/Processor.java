package org.apache.coyote;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.tomcat.util.net.AbstractEndpoint.Handler.SocketState;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.net.SocketEvent;
import org.apache.tomcat.util.net.SocketWrapperBase;
public interface Processor {
    SocketState process ( SocketWrapperBase<?> socketWrapper, SocketEvent status ) throws IOException;
    UpgradeToken getUpgradeToken();
    boolean isUpgrade();
    boolean isAsync();
    void timeoutAsync ( long now );
    Request getRequest();
    void recycle();
    void setSslSupport ( SSLSupport sslSupport );
    ByteBuffer getLeftoverInput();
    void pause();
}
