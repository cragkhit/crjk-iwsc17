// 
// Decompiled by Procyon v0.5.29
// 

package websocket.snake;

import org.apache.juli.logging.LogFactory;
import java.util.TimerTask;
import java.util.Iterator;
import java.util.Collections;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Timer;
import org.apache.juli.logging.Log;

public class SnakeTimer
{
    private static final Log log;
    private static Timer gameTimer;
    private static final long TICK_DELAY = 100L;
    private static final ConcurrentHashMap<Integer, Snake> snakes;
    
    protected static synchronized void addSnake(final Snake snake) {
        if (SnakeTimer.snakes.size() == 0) {
            startTimer();
        }
        SnakeTimer.snakes.put(snake.getId(), snake);
    }
    
    protected static Collection<Snake> getSnakes() {
        return Collections.unmodifiableCollection((Collection<? extends Snake>)SnakeTimer.snakes.values());
    }
    
    protected static synchronized void removeSnake(final Snake snake) {
        SnakeTimer.snakes.remove(snake.getId());
        if (SnakeTimer.snakes.size() == 0) {
            stopTimer();
        }
    }
    
    protected static void tick() {
        final StringBuilder sb = new StringBuilder();
        final Iterator<Snake> iterator = getSnakes().iterator();
        while (iterator.hasNext()) {
            final Snake snake = iterator.next();
            snake.update(getSnakes());
            sb.append(snake.getLocationsJson());
            if (iterator.hasNext()) {
                sb.append(',');
            }
        }
        broadcast(String.format("{\"type\": \"update\", \"data\" : [%s]}", sb.toString()));
    }
    
    protected static void broadcast(final String message) {
        for (final Snake snake : getSnakes()) {
            try {
                snake.sendMessage(message);
            }
            catch (IllegalStateException ex) {}
        }
    }
    
    public static void startTimer() {
        (SnakeTimer.gameTimer = new Timer(SnakeTimer.class.getSimpleName() + " Timer")).scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    SnakeTimer.tick();
                }
                catch (RuntimeException e) {
                    SnakeTimer.log.error((Object)"Caught to prevent timer from shutting down", (Throwable)e);
                }
            }
        }, 100L, 100L);
    }
    
    public static void stopTimer() {
        if (SnakeTimer.gameTimer != null) {
            SnakeTimer.gameTimer.cancel();
        }
    }
    
    static {
        log = LogFactory.getLog((Class)SnakeTimer.class);
        SnakeTimer.gameTimer = null;
        snakes = new ConcurrentHashMap<Integer, Snake>();
    }
}
