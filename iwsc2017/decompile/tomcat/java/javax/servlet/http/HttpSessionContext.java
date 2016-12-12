package javax.servlet.http;
import java.util.Enumeration;
@Deprecated
public interface HttpSessionContext {
    @Deprecated
    HttpSession getSession ( String p0 );
    @Deprecated
    Enumeration<String> getIds();
}
