package org.apache.catalina.authenticator.jaspic;
import org.apache.juli.logging.LogFactory;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import org.apache.catalina.realm.GenericPrincipal;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import javax.security.auth.Subject;
import java.security.Principal;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.callback.Callback;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import javax.security.auth.callback.CallbackHandler;
public class CallbackHandlerImpl implements CallbackHandler {
    private static final Log log;
    private static final StringManager sm;
    private static CallbackHandler instance;
    public static CallbackHandler getInstance() {
        return CallbackHandlerImpl.instance;
    }
    @Override
    public void handle ( final Callback[] callbacks ) throws IOException, UnsupportedCallbackException {
        String name = null;
        Principal principal = null;
        Subject subject = null;
        String[] groups = null;
        if ( callbacks != null ) {
            for ( final Callback callback : callbacks ) {
                if ( callback instanceof CallerPrincipalCallback ) {
                    final CallerPrincipalCallback cpc = ( CallerPrincipalCallback ) callback;
                    name = cpc.getName();
                    principal = cpc.getPrincipal();
                    subject = cpc.getSubject();
                } else if ( callback instanceof GroupPrincipalCallback ) {
                    final GroupPrincipalCallback gpc = ( GroupPrincipalCallback ) callback;
                    groups = gpc.getGroups();
                } else {
                    CallbackHandlerImpl.log.error ( CallbackHandlerImpl.sm.getString ( "callbackHandlerImpl.jaspicCallbackMissing", callback.getClass().getName() ) );
                }
            }
            final Principal gp = this.getPrincipal ( principal, name, groups );
            if ( subject != null && gp != null ) {
                subject.getPrivateCredentials().add ( gp );
            }
        }
    }
    private Principal getPrincipal ( final Principal principal, String name, final String[] groups ) {
        if ( principal instanceof GenericPrincipal ) {
            return principal;
        }
        if ( name == null && principal != null ) {
            name = principal.getName();
        }
        if ( name == null ) {
            return null;
        }
        List<String> roles;
        if ( groups == null || groups.length == 0 ) {
            roles = Collections.emptyList();
        } else {
            roles = Arrays.asList ( groups );
        }
        return new GenericPrincipal ( name, null, roles, principal );
    }
    static {
        log = LogFactory.getLog ( CallbackHandlerImpl.class );
        sm = StringManager.getManager ( CallbackHandlerImpl.class );
        CallbackHandlerImpl.instance = new CallbackHandlerImpl();
    }
}
