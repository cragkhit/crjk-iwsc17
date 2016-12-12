// 
// Decompiled by Procyon v0.5.29
// 

package websocket.drawboard;

import websocket.drawboard.wsmessages.AbstractWebsocketMessage;
import websocket.drawboard.wsmessages.StringWebsocketMessage;

class DrawboardEndpoint$1 implements Runnable {
    final /* synthetic */ Room val$room;
    final /* synthetic */ Client val$client;
    
    @Override
    public void run() {
        try {
            try {
                DrawboardEndpoint.access$002(DrawboardEndpoint.this, this.val$room.createAndAddPlayer(this.val$client));
            }
            catch (IllegalStateException ex) {
                this.val$client.sendMessage(new StringWebsocketMessage("0" + ex.getLocalizedMessage()));
                this.val$client.close();
            }
        }
        catch (RuntimeException ex2) {
            DrawboardEndpoint.access$100().error((Object)("Unexpected exception: " + ex2.toString()), (Throwable)ex2);
        }
    }
}