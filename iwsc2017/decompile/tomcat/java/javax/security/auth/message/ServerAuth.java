package javax.security.auth.message;
import javax.security.auth.Subject;
public interface ServerAuth {
    AuthStatus validateRequest ( MessageInfo p0, Subject p1, Subject p2 ) throws AuthException;
    AuthStatus secureResponse ( MessageInfo p0, Subject p1 ) throws AuthException;
    void cleanSubject ( MessageInfo p0, Subject p1 ) throws AuthException;
}
