package javax.servlet.http;
import java.util.Enumeration;
@Deprecated
public interface HttpSessionContext {
    @Deprecated
    public HttpSession getSession ( String sessionId );
    @Deprecated
    public Enumeration<String> getIds();
}
