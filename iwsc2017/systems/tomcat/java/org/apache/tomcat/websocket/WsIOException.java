package org.apache.tomcat.websocket;
import java.io.IOException;
import javax.websocket.CloseReason;
public class WsIOException extends IOException {
    private static final long serialVersionUID = 1L;
    private final CloseReason closeReason;
    public WsIOException ( CloseReason closeReason ) {
        this.closeReason = closeReason;
    }
    public CloseReason getCloseReason() {
        return closeReason;
    }
}
