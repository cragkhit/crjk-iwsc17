package javax.security.auth.message.module;
import javax.security.auth.message.AuthException;
import java.util.Map;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.ServerAuth;
public interface ServerAuthModule extends ServerAuth {
    void initialize ( MessagePolicy p0, MessagePolicy p1, CallbackHandler p2, Map p3 ) throws AuthException;
    Class[] getSupportedMessageTypes();
}
