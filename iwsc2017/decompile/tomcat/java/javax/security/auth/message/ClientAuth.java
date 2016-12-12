package javax.security.auth.message;
import javax.security.auth.Subject;
public interface ClientAuth {
    AuthStatus secureRequest ( MessageInfo p0, Subject p1 ) throws AuthException;
    AuthStatus validateResponse ( MessageInfo p0, Subject p1, Subject p2 ) throws AuthException;
    void cleanSubject ( MessageInfo p0, Subject p1 ) throws AuthException;
}
