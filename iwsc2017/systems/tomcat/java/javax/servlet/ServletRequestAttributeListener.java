package javax.servlet;
import java.util.EventListener;
public interface ServletRequestAttributeListener extends EventListener {
public default void attributeAdded ( ServletRequestAttributeEvent srae ) {
    }
public default void attributeRemoved ( ServletRequestAttributeEvent srae ) {
    }
public default void attributeReplaced ( ServletRequestAttributeEvent srae ) {
    }
}
