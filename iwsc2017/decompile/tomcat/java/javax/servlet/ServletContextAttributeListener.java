package javax.servlet;
import java.util.EventListener;
public interface ServletContextAttributeListener extends EventListener {
default void attributeAdded ( ServletContextAttributeEvent scae ) {
    }
default void attributeRemoved ( ServletContextAttributeEvent scae ) {
    }
default void attributeReplaced ( ServletContextAttributeEvent scae ) {
    }
}
