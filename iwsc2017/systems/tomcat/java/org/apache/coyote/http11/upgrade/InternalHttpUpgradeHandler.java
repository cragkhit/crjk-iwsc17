package org.apache.coyote.http11.upgrade;
import javax.servlet.http.HttpUpgradeHandler;
import org.apache.tomcat.util.net.AbstractEndpoint.Handler.SocketState;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.net.SocketEvent;
import org.apache.tomcat.util.net.SocketWrapperBase;
public interface InternalHttpUpgradeHandler extends HttpUpgradeHandler {
    SocketState upgradeDispatch ( SocketEvent status );
    void setSocketWrapper ( SocketWrapperBase<?> wrapper );
    void setSslSupport ( SSLSupport sslSupport );
    void pause();
}
