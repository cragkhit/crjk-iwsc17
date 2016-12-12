package org.apache.tomcat.websocket;
import javax.websocket.MessageHandler;
public class MessageHandlerResult {
    private final MessageHandler handler;
    private final MessageHandlerResultType type;
    public MessageHandlerResult ( final MessageHandler handler, final MessageHandlerResultType type ) {
        this.handler = handler;
        this.type = type;
    }
    public MessageHandler getHandler() {
        return this.handler;
    }
    public MessageHandlerResultType getType() {
        return this.type;
    }
}
