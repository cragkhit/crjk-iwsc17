package javax.servlet.http;
import java.util.EventListener;
public interface HttpSessionActivationListener extends EventListener {
public default void sessionWillPassivate ( HttpSessionEvent se ) {
    }
public default void sessionDidActivate ( HttpSessionEvent se ) {
    }
}
