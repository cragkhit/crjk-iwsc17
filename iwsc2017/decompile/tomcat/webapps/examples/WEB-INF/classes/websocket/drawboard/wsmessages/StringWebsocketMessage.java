// 
// Decompiled by Procyon v0.5.29
// 

package websocket.drawboard.wsmessages;

public final class StringWebsocketMessage extends AbstractWebsocketMessage
{
    private final String string;
    
    public StringWebsocketMessage(final String string) {
        this.string = string;
    }
    
    public String getString() {
        return this.string;
    }
}
