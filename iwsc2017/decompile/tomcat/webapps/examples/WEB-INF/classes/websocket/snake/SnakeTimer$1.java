// 
// Decompiled by Procyon v0.5.29
// 

package websocket.snake;

import java.util.TimerTask;

static final class SnakeTimer$1 extends TimerTask {
    @Override
    public void run() {
        try {
            SnakeTimer.tick();
        }
        catch (RuntimeException e) {
            SnakeTimer.access$000().error((Object)"Caught to prevent timer from shutting down", (Throwable)e);
        }
    }
}