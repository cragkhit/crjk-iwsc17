// 
// Decompiled by Procyon v0.5.29
// 

package websocket.drawboard;

public enum MessageType
{
    ERROR('0'), 
    DRAW_MESSAGE('1'), 
    IMAGE_MESSAGE('2'), 
    PLAYER_CHANGED('3');
    
    private final char flag;
    
    private MessageType(final char flag) {
        this.flag = flag;
    }
}
