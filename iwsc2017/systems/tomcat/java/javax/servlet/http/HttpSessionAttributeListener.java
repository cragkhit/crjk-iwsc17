package javax.servlet.http;
import java.util.EventListener;
public interface HttpSessionAttributeListener extends EventListener {
public default void attributeAdded ( HttpSessionBindingEvent se ) {
    }
public default void attributeRemoved ( HttpSessionBindingEvent se ) {
    }
public default void attributeReplaced ( HttpSessionBindingEvent se ) {
    }
}
