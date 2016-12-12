// 
// Decompiled by Procyon v0.5.29
// 

package websocket.snake;

import javax.websocket.OnError;
import java.io.EOFException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import java.util.Iterator;
import javax.websocket.Session;
import java.awt.Color;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/websocket/snake")
public class SnakeAnnotation
{
    public static final int PLAYFIELD_WIDTH = 640;
    public static final int PLAYFIELD_HEIGHT = 480;
    public static final int GRID_SIZE = 10;
    private static final AtomicInteger snakeIds;
    private static final Random random;
    private final int id;
    private Snake snake;
    
    public static String getRandomHexColor() {
        final float hue = SnakeAnnotation.random.nextFloat();
        final float saturation = (SnakeAnnotation.random.nextInt(2000) + 1000) / 10000.0f;
        final float luminance = 0.9f;
        final Color color = Color.getHSBColor(hue, saturation, luminance);
        return '#' + Integer.toHexString((color.getRGB() & 0xFFFFFF) | 0x1000000).substring(1);
    }
    
    public static Location getRandomLocation() {
        final int x = roundByGridSize(SnakeAnnotation.random.nextInt(640));
        final int y = roundByGridSize(SnakeAnnotation.random.nextInt(480));
        return new Location(x, y);
    }
    
    private static int roundByGridSize(int value) {
        value += 5;
        value /= 10;
        value *= 10;
        return value;
    }
    
    public SnakeAnnotation() {
        this.id = SnakeAnnotation.snakeIds.getAndIncrement();
    }
    
    @OnOpen
    public void onOpen(final Session session) {
        SnakeTimer.addSnake(this.snake = new Snake(this.id, session));
        final StringBuilder sb = new StringBuilder();
        final Iterator<Snake> iterator = SnakeTimer.getSnakes().iterator();
        while (iterator.hasNext()) {
            final Snake snake = iterator.next();
            sb.append(String.format("{\"id\": %d, \"color\": \"%s\"}", snake.getId(), snake.getHexColor()));
            if (iterator.hasNext()) {
                sb.append(',');
            }
        }
        SnakeTimer.broadcast(String.format("{\"type\": \"join\",\"data\":[%s]}", sb.toString()));
    }
    
    @OnMessage
    public void onTextMessage(final String message) {
        if ("west".equals(message)) {
            this.snake.setDirection(Direction.WEST);
        }
        else if ("north".equals(message)) {
            this.snake.setDirection(Direction.NORTH);
        }
        else if ("east".equals(message)) {
            this.snake.setDirection(Direction.EAST);
        }
        else if ("south".equals(message)) {
            this.snake.setDirection(Direction.SOUTH);
        }
    }
    
    @OnClose
    public void onClose() {
        SnakeTimer.removeSnake(this.snake);
        SnakeTimer.broadcast(String.format("{\"type\": \"leave\", \"id\": %d}", this.id));
    }
    
    @OnError
    public void onError(final Throwable t) throws Throwable {
        int count;
        Throwable root;
        for (count = 0, root = t; root.getCause() != null && count < 20; root = root.getCause(), ++count) {}
        if (root instanceof EOFException) {
            return;
        }
        throw t;
    }
    
    static {
        snakeIds = new AtomicInteger(0);
        random = new Random();
    }
}
