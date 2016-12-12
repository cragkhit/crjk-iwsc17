// 
// Decompiled by Procyon v0.5.29
// 

package websocket.snake;

import java.util.Iterator;
import java.util.Collection;
import java.io.IOException;
import javax.websocket.CloseReason;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.websocket.Session;

public class Snake
{
    private static final int DEFAULT_LENGTH = 5;
    private final int id;
    private final Session session;
    private Direction direction;
    private int length;
    private Location head;
    private final Deque<Location> tail;
    private final String hexColor;
    
    public Snake(final int id, final Session session) {
        this.length = 5;
        this.tail = new ArrayDeque<Location>();
        this.id = id;
        this.session = session;
        this.hexColor = SnakeAnnotation.getRandomHexColor();
        this.resetState();
    }
    
    private void resetState() {
        this.direction = Direction.NONE;
        this.head = SnakeAnnotation.getRandomLocation();
        this.tail.clear();
        this.length = 5;
    }
    
    private synchronized void kill() {
        this.resetState();
        this.sendMessage("{\"type\": \"dead\"}");
    }
    
    private synchronized void reward() {
        ++this.length;
        this.sendMessage("{\"type\": \"kill\"}");
    }
    
    protected void sendMessage(final String msg) {
        try {
            this.session.getBasicRemote().sendText(msg);
        }
        catch (IOException ioe) {
            final CloseReason cr = new CloseReason((CloseReason.CloseCode)CloseReason.CloseCodes.CLOSED_ABNORMALLY, ioe.getMessage());
            try {
                this.session.close(cr);
            }
            catch (IOException ex) {}
        }
    }
    
    public synchronized void update(final Collection<Snake> snakes) {
        final Location nextLocation = this.head.getAdjacentLocation(this.direction);
        if (nextLocation.x >= 640) {
            nextLocation.x = 0;
        }
        if (nextLocation.y >= 480) {
            nextLocation.y = 0;
        }
        if (nextLocation.x < 0) {
            nextLocation.x = 640;
        }
        if (nextLocation.y < 0) {
            nextLocation.y = 480;
        }
        if (this.direction != Direction.NONE) {
            this.tail.addFirst(this.head);
            if (this.tail.size() > this.length) {
                this.tail.removeLast();
            }
            this.head = nextLocation;
        }
        this.handleCollisions(snakes);
    }
    
    private void handleCollisions(final Collection<Snake> snakes) {
        for (final Snake snake : snakes) {
            final boolean headCollision = this.id != snake.id && snake.getHead().equals(this.head);
            final boolean tailCollision = snake.getTail().contains(this.head);
            if (headCollision || tailCollision) {
                this.kill();
                if (this.id == snake.id) {
                    continue;
                }
                snake.reward();
            }
        }
    }
    
    public synchronized Location getHead() {
        return this.head;
    }
    
    public synchronized Collection<Location> getTail() {
        return this.tail;
    }
    
    public synchronized void setDirection(final Direction direction) {
        this.direction = direction;
    }
    
    public synchronized String getLocationsJson() {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("{\"x\": %d, \"y\": %d}", this.head.x, this.head.y));
        for (final Location location : this.tail) {
            sb.append(',');
            sb.append(String.format("{\"x\": %d, \"y\": %d}", location.x, location.y));
        }
        return String.format("{\"id\":%d,\"body\":[%s]}", this.id, sb.toString());
    }
    
    public int getId() {
        return this.id;
    }
    
    public String getHexColor() {
        return this.hexColor;
    }
}
