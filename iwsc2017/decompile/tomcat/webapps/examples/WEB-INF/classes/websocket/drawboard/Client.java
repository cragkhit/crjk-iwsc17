// 
// Decompiled by Procyon v0.5.29
// 

package websocket.drawboard;

import websocket.drawboard.wsmessages.BinaryWebsocketMessage;
import websocket.drawboard.wsmessages.StringWebsocketMessage;
import javax.websocket.CloseReason;
import websocket.drawboard.wsmessages.CloseWebsocketMessage;
import java.io.IOException;
import javax.websocket.SendResult;
import javax.websocket.SendHandler;
import websocket.drawboard.wsmessages.AbstractWebsocketMessage;
import java.util.LinkedList;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

public class Client
{
    private final Session session;
    private final RemoteEndpoint.Async async;
    private final LinkedList<AbstractWebsocketMessage> messagesToSend;
    private volatile boolean isSendingMessage;
    private volatile boolean isClosing;
    private volatile long messagesToSendLength;
    private final SendHandler sendHandler;
    
    public Client(final Session session) {
        this.messagesToSend = new LinkedList<AbstractWebsocketMessage>();
        this.isSendingMessage = false;
        this.isClosing = false;
        this.messagesToSendLength = 0L;
        this.sendHandler = (SendHandler)new SendHandler() {
            public void onResult(final SendResult result) {
                if (!result.isOK()) {
                    try {
                        Client.this.session.close();
                    }
                    catch (IOException ex) {}
                }
                synchronized (Client.this.messagesToSend) {
                    if (!Client.this.messagesToSend.isEmpty()) {
                        final AbstractWebsocketMessage msg = Client.this.messagesToSend.remove();
                        Client.this.messagesToSendLength -= Client.this.calculateMessageLength(msg);
                        Client.this.internalSendMessageAsync(msg);
                    }
                    else {
                        Client.this.isSendingMessage = false;
                    }
                }
            }
        };
        this.session = session;
        this.async = session.getAsyncRemote();
    }
    
    public void close() {
        this.sendMessage(new CloseWebsocketMessage());
    }
    
    public void sendMessage(AbstractWebsocketMessage msg) {
        synchronized (this.messagesToSend) {
            if (!this.isClosing) {
                if (msg instanceof CloseWebsocketMessage) {
                    this.isClosing = true;
                }
                if (this.isSendingMessage) {
                    if (this.messagesToSend.size() >= 1000 || this.messagesToSendLength >= 1000000L) {
                        this.isClosing = true;
                        final CloseReason cr = new CloseReason((CloseReason.CloseCode)CloseReason.CloseCodes.VIOLATED_POLICY, "Send Buffer exceeded");
                        try {
                            this.session.close(cr);
                        }
                        catch (IOException ex) {}
                    }
                    else {
                        if (msg instanceof StringWebsocketMessage && !this.messagesToSend.isEmpty() && this.messagesToSend.getLast() instanceof StringWebsocketMessage) {
                            final StringWebsocketMessage ms = this.messagesToSend.removeLast();
                            this.messagesToSendLength -= this.calculateMessageLength(ms);
                            final String concatenated = ms.getString() + ";" + ((StringWebsocketMessage)msg).getString();
                            msg = new StringWebsocketMessage(concatenated);
                        }
                        this.messagesToSend.add(msg);
                        this.messagesToSendLength += this.calculateMessageLength(msg);
                    }
                }
                else {
                    this.isSendingMessage = true;
                    this.internalSendMessageAsync(msg);
                }
            }
        }
    }
    
    private long calculateMessageLength(final AbstractWebsocketMessage msg) {
        if (msg instanceof BinaryWebsocketMessage) {
            return ((BinaryWebsocketMessage)msg).getBytes().capacity();
        }
        if (msg instanceof StringWebsocketMessage) {
            return ((StringWebsocketMessage)msg).getString().length() * 2;
        }
        return 0L;
    }
    
    private void internalSendMessageAsync(final AbstractWebsocketMessage msg) {
        try {
            if (msg instanceof StringWebsocketMessage) {
                final StringWebsocketMessage sMsg = (StringWebsocketMessage)msg;
                this.async.sendText(sMsg.getString(), this.sendHandler);
            }
            else if (msg instanceof BinaryWebsocketMessage) {
                final BinaryWebsocketMessage bMsg = (BinaryWebsocketMessage)msg;
                this.async.sendBinary(bMsg.getBytes(), this.sendHandler);
            }
            else if (msg instanceof CloseWebsocketMessage) {
                this.session.close();
            }
        }
        catch (IllegalStateException) {}
        catch (IOException ex) {}
    }
}
