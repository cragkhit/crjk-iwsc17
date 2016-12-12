package javax.servlet.http;
import java.util.EventListener;
public interface HttpSessionAttributeListener extends EventListener {
default void attributeAdded ( HttpSessionBindingEvent se ) {
    }
default void attributeRemoved ( HttpSessionBindingEvent se ) {
    }
default void attributeReplaced ( HttpSessionBindingEvent se ) {
    }
}
