// 
// Decompiled by Procyon v0.5.29
// 

package websocket.drawboard;

import websocket.drawboard.wsmessages.StringWebsocketMessage;
import java.util.Objects;
import java.util.Iterator;
import websocket.drawboard.wsmessages.AbstractWebsocketMessage;
import websocket.drawboard.wsmessages.BinaryWebsocketMessage;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.OutputStream;
import java.awt.image.RenderedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.awt.Color;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.TimerTask;
import java.util.Timer;
import java.util.concurrent.locks.ReentrantLock;

public final class Room
{
    private final ReentrantLock roomLock;
    private volatile boolean closed;
    private static final boolean BUFFER_DRAW_MESSAGES = true;
    private final Timer drawmessageBroadcastTimer;
    private static final int TIMER_DELAY = 30;
    private TimerTask activeBroadcastTimerTask;
    private final BufferedImage roomImage;
    private final Graphics2D roomGraphics;
    private static final int MAX_PLAYER_COUNT = 100;
    private final List<Player> players;
    private List<Runnable> cachedRunnables;
    
    public Room() {
        this.roomLock = new ReentrantLock();
        this.closed = false;
        this.drawmessageBroadcastTimer = new Timer();
        this.roomImage = new BufferedImage(900, 600, 1);
        this.roomGraphics = this.roomImage.createGraphics();
        this.players = new ArrayList<Player>();
        this.cachedRunnables = null;
        this.roomGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        this.roomGraphics.setBackground(Color.WHITE);
        this.roomGraphics.clearRect(0, 0, this.roomImage.getWidth(), this.roomImage.getHeight());
    }
    
    private TimerTask createBroadcastTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                Room.this.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        Room.this.broadcastTimerTick();
                    }
                });
            }
        };
    }
    
    public Player createAndAddPlayer(final Client client) {
        if (this.players.size() >= 100) {
            throw new IllegalStateException("Maximum player count (100) has been reached.");
        }
        final Player p = new Player(this, client);
        this.broadcastRoomMessage(MessageType.PLAYER_CHANGED, "+");
        this.players.add(p);
        if (this.activeBroadcastTimerTask == null) {
            this.activeBroadcastTimerTask = this.createBroadcastTimerTask();
            this.drawmessageBroadcastTimer.schedule(this.activeBroadcastTimerTask, 30L, 30L);
        }
        final String content = String.valueOf(this.players.size());
        p.sendRoomMessage(MessageType.IMAGE_MESSAGE, content);
        final ByteArrayOutputStream bout = new ByteArrayOutputStream();
        try {
            ImageIO.write(this.roomImage, "PNG", bout);
        }
        catch (IOException ex) {}
        final BinaryWebsocketMessage msg = new BinaryWebsocketMessage(ByteBuffer.wrap(bout.toByteArray()));
        p.getClient().sendMessage(msg);
        return p;
    }
    
    private void internalRemovePlayer(final Player p) {
        final boolean removed = this.players.remove(p);
        assert removed;
        if (this.players.size() == 0) {
            this.activeBroadcastTimerTask.cancel();
            this.activeBroadcastTimerTask = null;
        }
        this.broadcastRoomMessage(MessageType.PLAYER_CHANGED, "-");
    }
    
    private void internalHandleDrawMessage(final Player p, final DrawMessage msg, final long msgId) {
        p.setLastReceivedMessageId(msgId);
        msg.draw(this.roomGraphics);
        this.broadcastDrawMessage(msg);
    }
    
    private void broadcastRoomMessage(final MessageType type, final String content) {
        for (final Player p : this.players) {
            p.sendRoomMessage(type, content);
        }
    }
    
    private void broadcastDrawMessage(final DrawMessage msg) {
        for (final Player p : this.players) {
            p.getBufferedDrawMessages().add(msg);
        }
    }
    
    private void broadcastTimerTick() {
        for (final Player p : this.players) {
            final StringBuilder sb = new StringBuilder();
            final List<DrawMessage> drawMessages = p.getBufferedDrawMessages();
            if (drawMessages.size() > 0) {
                for (int i = 0; i < drawMessages.size(); ++i) {
                    final DrawMessage msg = drawMessages.get(i);
                    final String s = String.valueOf(p.getLastReceivedMessageId()) + "," + msg.toString();
                    if (i > 0) {
                        sb.append("|");
                    }
                    sb.append(s);
                }
                drawMessages.clear();
                p.sendRoomMessage(MessageType.DRAW_MESSAGE, sb.toString());
            }
        }
    }
    
    public void invokeAndWait(final Runnable task) {
        if (this.roomLock.isHeldByCurrentThread()) {
            if (this.cachedRunnables == null) {
                this.cachedRunnables = new ArrayList<Runnable>();
            }
            this.cachedRunnables.add(task);
        }
        else {
            this.roomLock.lock();
            try {
                this.cachedRunnables = null;
                if (!this.closed) {
                    task.run();
                }
                if (this.cachedRunnables != null) {
                    for (int i = 0; i < this.cachedRunnables.size(); ++i) {
                        if (!this.closed) {
                            this.cachedRunnables.get(i).run();
                        }
                    }
                    this.cachedRunnables = null;
                }
            }
            finally {
                this.roomLock.unlock();
            }
        }
    }
    
    public void shutdown() {
        this.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                Room.this.closed = true;
                Room.this.drawmessageBroadcastTimer.cancel();
                Room.this.roomGraphics.dispose();
            }
        });
    }
    
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
    
    public final class Player
    {
        private Room room;
        private long lastReceivedMessageId;
        private final Client client;
        private final List<DrawMessage> bufferedDrawMessages;
        
        private List<DrawMessage> getBufferedDrawMessages() {
            return this.bufferedDrawMessages;
        }
        
        private Player(final Room room, final Client client) {
            this.lastReceivedMessageId = 0L;
            this.bufferedDrawMessages = new ArrayList<DrawMessage>();
            this.room = room;
            this.client = client;
        }
        
        public Room getRoom() {
            return this.room;
        }
        
        public Client getClient() {
            return this.client;
        }
        
        public void removeFromRoom() {
            if (this.room != null) {
                this.room.internalRemovePlayer(this);
                this.room = null;
            }
        }
        
        private long getLastReceivedMessageId() {
            return this.lastReceivedMessageId;
        }
        
        private void setLastReceivedMessageId(final long value) {
            this.lastReceivedMessageId = value;
        }
        
        public void handleDrawMessage(final DrawMessage msg, final long msgId) {
            this.room.internalHandleDrawMessage(this, msg, msgId);
        }
        
        private void sendRoomMessage(final MessageType type, final String content) {
            Objects.requireNonNull(content);
            Objects.requireNonNull(type);
            final String completeMsg = String.valueOf(type.flag) + content;
            this.client.sendMessage(new StringWebsocketMessage(completeMsg));
        }
    }
}
