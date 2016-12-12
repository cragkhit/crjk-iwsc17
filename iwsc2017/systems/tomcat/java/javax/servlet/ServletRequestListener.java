package javax.servlet;
import java.util.EventListener;
public interface ServletRequestListener extends EventListener {
public default void requestDestroyed ( ServletRequestEvent sre ) {
    }
public default void requestInitialized ( ServletRequestEvent sre ) {
    }
}
