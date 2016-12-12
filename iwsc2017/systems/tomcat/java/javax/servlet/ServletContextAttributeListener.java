package javax.servlet;
import java.util.EventListener;
public interface ServletContextAttributeListener extends EventListener {
public default void attributeAdded ( ServletContextAttributeEvent scae ) {
    }
public default void attributeRemoved ( ServletContextAttributeEvent scae ) {
    }
public default void attributeReplaced ( ServletContextAttributeEvent scae ) {
    }
}
