package org.apache.coyote.http11.upgrade;
import java.nio.ByteBuffer;
import org.apache.coyote.Request;
import java.io.IOException;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SocketWrapperBase;
import org.apache.coyote.UpgradeToken;
import javax.servlet.http.WebConnection;
import org.apache.coyote.AbstractProcessorLight;
public abstract class UpgradeProcessorBase extends AbstractProcessorLight implements WebConnection {
    protected static final int INFINITE_TIMEOUT = -1;
    private final UpgradeToken upgradeToken;
    public UpgradeProcessorBase ( final UpgradeToken upgradeToken ) {
        this.upgradeToken = upgradeToken;
    }
    public final boolean isUpgrade() {
        return true;
    }
    public UpgradeToken getUpgradeToken() {
        return this.upgradeToken;
    }
    public final void recycle() {
    }
    public final AbstractEndpoint.Handler.SocketState service ( final SocketWrapperBase<?> socketWrapper ) throws IOException {
        return null;
    }
    public final AbstractEndpoint.Handler.SocketState asyncPostProcess() {
        return null;
    }
    public final boolean isAsync() {
        return false;
    }
    public final Request getRequest() {
        return null;
    }
    public ByteBuffer getLeftoverInput() {
        return null;
    }
    public void timeoutAsync ( final long now ) {
    }
}
