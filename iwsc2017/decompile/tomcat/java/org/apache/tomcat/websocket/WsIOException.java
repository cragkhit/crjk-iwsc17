package org.apache.tomcat.websocket;
import javax.websocket.CloseReason;
import java.io.IOException;
public class WsIOException extends IOException {
    private static final long serialVersionUID = 1L;
    private final CloseReason closeReason;
    public WsIOException ( final CloseReason closeReason ) {
        this.closeReason = closeReason;
    }
    public CloseReason getCloseReason() {
        return this.closeReason;
    }
}
