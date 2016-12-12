package javax.security.auth.message;
import java.util.Map;
public interface MessageInfo {
    Object getRequestMessage();
    Object getResponseMessage();
    void setRequestMessage ( Object p0 );
    void setResponseMessage ( Object p0 );
    Map getMap();
}
