// 
// Decompiled by Procyon v0.5.29
// 

package websocket.drawboard;

import java.util.TimerTask;

class Room$1 extends TimerTask {
    @Override
    public void run() {
        Room.this.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                Room.access$000(Room.this);
            }
        });
    }
}