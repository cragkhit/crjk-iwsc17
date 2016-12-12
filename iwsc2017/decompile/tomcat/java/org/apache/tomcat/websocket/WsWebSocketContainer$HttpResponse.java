package org.apache.tomcat.websocket;
import javax.websocket.HandshakeResponse;
private static class HttpResponse {
    private final int status;
    private final HandshakeResponse handshakeResponse;
    public HttpResponse ( final int status, final HandshakeResponse handshakeResponse ) {
        this.status = status;
        this.handshakeResponse = handshakeResponse;
    }
    public int getStatus() {
        return this.status;
    }
    public HandshakeResponse getHandshakeResponse() {
        return this.handshakeResponse;
    }
}
