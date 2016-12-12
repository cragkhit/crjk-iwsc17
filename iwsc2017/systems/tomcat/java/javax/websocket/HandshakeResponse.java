package javax.websocket;
import java.util.List;
import java.util.Map;
public interface HandshakeResponse {
    public static final String SEC_WEBSOCKET_ACCEPT = "Sec-WebSocket-Accept";
    Map<String, List<String>> getHeaders();
}
