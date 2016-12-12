package org.apache.catalina.authenticator.jaspic;
import java.util.Iterator;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.Subject;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.module.ServerAuthModule;
import java.util.List;
import javax.security.auth.message.config.ServerAuthContext;
public class SimpleServerAuthContext implements ServerAuthContext {
    private final List<ServerAuthModule> modules;
    public SimpleServerAuthContext ( final List<ServerAuthModule> modules ) {
        this.modules = modules;
    }
    public AuthStatus validateRequest ( final MessageInfo messageInfo, final Subject clientSubject, final Subject serviceSubject ) throws AuthException {
        for ( int moduleIndex = 0; moduleIndex < this.modules.size(); ++moduleIndex ) {
            final ServerAuthModule module = this.modules.get ( moduleIndex );
            final AuthStatus result = module.validateRequest ( messageInfo, clientSubject, serviceSubject );
            if ( result != AuthStatus.SEND_FAILURE ) {
                messageInfo.getMap().put ( "moduleIndex", moduleIndex );
                return result;
            }
        }
        return AuthStatus.SEND_FAILURE;
    }
    public AuthStatus secureResponse ( final MessageInfo messageInfo, final Subject serviceSubject ) throws AuthException {
        final ServerAuthModule module = this.modules.get ( messageInfo.getMap().get ( "moduleIndex" ) );
        return module.secureResponse ( messageInfo, serviceSubject );
    }
    public void cleanSubject ( final MessageInfo messageInfo, final Subject subject ) throws AuthException {
        for ( final ServerAuthModule module : this.modules ) {
            module.cleanSubject ( messageInfo, subject );
        }
    }
}
