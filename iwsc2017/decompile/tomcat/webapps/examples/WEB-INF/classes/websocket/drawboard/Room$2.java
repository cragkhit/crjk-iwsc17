// 
// Decompiled by Procyon v0.5.29
// 

package websocket.drawboard;

class Room$2 implements Runnable {
    @Override
    public void run() {
        Room.access$602(Room.this, true);
        Room.access$700(Room.this).cancel();
        Room.access$800(Room.this).dispose();
    }
}