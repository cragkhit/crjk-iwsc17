package javax.servlet;
import java.util.EventListener;
public interface ServletContextListener extends EventListener {
public default void contextInitialized ( ServletContextEvent sce ) {
    }
public default void contextDestroyed ( ServletContextEvent sce ) {
    }
}
