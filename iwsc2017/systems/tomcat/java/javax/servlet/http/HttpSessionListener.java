package javax.servlet.http;
import java.util.EventListener;
public interface HttpSessionListener extends EventListener {
public default void sessionCreated ( HttpSessionEvent se ) {
    }
public default void sessionDestroyed ( HttpSessionEvent se ) {
    }
}
