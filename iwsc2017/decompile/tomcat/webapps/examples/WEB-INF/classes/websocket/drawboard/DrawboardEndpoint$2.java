// 
// Decompiled by Procyon v0.5.29
// 

package websocket.drawboard;

class DrawboardEndpoint$2 implements Runnable {
    @Override
    public void run() {
        try {
            if (DrawboardEndpoint.access$000(DrawboardEndpoint.this) != null) {
                DrawboardEndpoint.access$000(DrawboardEndpoint.this).removeFromRoom();
                DrawboardEndpoint.access$002(DrawboardEndpoint.this, null);
            }
        }
        catch (RuntimeException ex) {
            DrawboardEndpoint.access$100().error((Object)("Unexpected exception: " + ex.toString()), (Throwable)ex);
        }
    }
}