package org.apache.catalina.session;
import javax.servlet.http.HttpSession;
import java.security.PrivilegedAction;
class StandardSession$1 implements PrivilegedAction<StandardSessionFacade> {
    @Override
    public StandardSessionFacade run() {
        return new StandardSessionFacade ( ( HttpSession ) StandardSession.this );
    }
}
