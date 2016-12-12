package javax.servlet.http;
import java.util.EventListener;
public interface HttpSessionBindingListener extends EventListener {
public default void valueBound ( HttpSessionBindingEvent event ) {
    }
public default void valueUnbound ( HttpSessionBindingEvent event ) {
    }
}
