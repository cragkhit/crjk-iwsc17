// 
// Decompiled by Procyon v0.5.29
// 

package websocket.drawboard;

import websocket.drawboard.wsmessages.AbstractWebsocketMessage;
import java.io.IOException;
import javax.websocket.SendResult;
import javax.websocket.SendHandler;

class Client$1 implements SendHandler {
    public void onResult(final SendResult result) {
        if (!result.isOK()) {
            try {
                Client.access$000(Client.this).close();
            }
            catch (IOException ex) {}
        }
        synchronized (Client.access$100(Client.this)) {
            if (!Client.access$100(Client.this).isEmpty()) {
                final AbstractWebsocketMessage msg = Client.access$100(Client.this).remove();
                Client.access$202(Client.this, Client.access$200(Client.this) - Client.access$300(Client.this, msg));
                Client.access$400(Client.this, msg);
            }
            else {
                Client.access$502(Client.this, false);
            }
        }
    }
}