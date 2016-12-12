package javax.servlet.http;
import java.util.EventListener;
public interface HttpSessionIdListener extends EventListener {
    void sessionIdChanged ( HttpSessionEvent p0, String p1 );
}
