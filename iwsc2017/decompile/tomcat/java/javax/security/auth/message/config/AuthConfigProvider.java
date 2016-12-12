package javax.security.auth.message.config;
import javax.security.auth.message.AuthException;
import javax.security.auth.callback.CallbackHandler;
public interface AuthConfigProvider {
    ClientAuthConfig getClientAuthConfig ( String p0, String p1, CallbackHandler p2 ) throws AuthException;
    ServerAuthConfig getServerAuthConfig ( String p0, String p1, CallbackHandler p2 ) throws AuthException;
    void refresh();
}
