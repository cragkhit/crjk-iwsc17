package javax.websocket;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;
public interface Session extends Closeable {
    WebSocketContainer getContainer();
    void addMessageHandler ( MessageHandler handler ) throws IllegalStateException;
    Set<MessageHandler> getMessageHandlers();
    void removeMessageHandler ( MessageHandler listener );
    String getProtocolVersion();
    String getNegotiatedSubprotocol();
    List<Extension> getNegotiatedExtensions();
    boolean isSecure();
    boolean isOpen();
    long getMaxIdleTimeout();
    void setMaxIdleTimeout ( long timeout );
    void setMaxBinaryMessageBufferSize ( int max );
    int getMaxBinaryMessageBufferSize();
    void setMaxTextMessageBufferSize ( int max );
    int getMaxTextMessageBufferSize();
    RemoteEndpoint.Async getAsyncRemote();
    RemoteEndpoint.Basic getBasicRemote();
    String getId();
    @Override
    void close() throws IOException;
    void close ( CloseReason closeReason ) throws IOException;
    URI getRequestURI();
    Map<String, List<String>> getRequestParameterMap();
    String getQueryString();
    Map<String, String> getPathParameters();
    Map<String, Object> getUserProperties();
    Principal getUserPrincipal();
    Set<Session> getOpenSessions();
    <T> void addMessageHandler ( Class<T> clazz, MessageHandler.Partial<T> handler )
    throws IllegalStateException;
    <T> void addMessageHandler ( Class<T> clazz, MessageHandler.Whole<T> handler )
    throws IllegalStateException;
}
