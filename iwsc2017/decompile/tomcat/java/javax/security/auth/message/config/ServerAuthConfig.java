package javax.security.auth.message.config;
import javax.security.auth.message.AuthException;
import java.util.Map;
import javax.security.auth.Subject;
public interface ServerAuthConfig extends AuthConfig {
    ServerAuthContext getAuthContext ( String p0, Subject p1, Map p2 ) throws AuthException;
}
