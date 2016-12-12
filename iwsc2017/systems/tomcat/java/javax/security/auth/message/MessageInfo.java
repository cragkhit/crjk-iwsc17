package javax.security.auth.message;
import java.util.Map;
public interface MessageInfo {
    Object getRequestMessage();
    Object getResponseMessage();
    void setRequestMessage ( Object request );
    void setResponseMessage ( Object response );
    @SuppressWarnings ( "rawtypes" )
    Map getMap();
}
