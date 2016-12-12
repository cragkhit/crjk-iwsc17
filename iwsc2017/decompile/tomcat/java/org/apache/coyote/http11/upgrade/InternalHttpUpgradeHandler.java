package org.apache.coyote.http11.upgrade;
import org.apache.tomcat.util.net.SSLSupport;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SocketEvent;
import javax.servlet.http.HttpUpgradeHandler;
public interface InternalHttpUpgradeHandler extends HttpUpgradeHandler {
    AbstractEndpoint.Handler.SocketState upgradeDispatch ( SocketEvent p0 );
    void setSocketWrapper ( SocketWrapperBase<?> p0 );
    void setSslSupport ( SSLSupport p0 );
    void pause();
}
