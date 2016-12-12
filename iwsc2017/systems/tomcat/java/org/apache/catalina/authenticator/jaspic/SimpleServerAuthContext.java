package org.apache.catalina.authenticator.jaspic;
import java.util.List;
import javax.security.auth.Subject;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.ServerAuthContext;
import javax.security.auth.message.module.ServerAuthModule;
public class SimpleServerAuthContext implements ServerAuthContext {
    private final List<ServerAuthModule> modules;
    public SimpleServerAuthContext ( List<ServerAuthModule> modules ) {
        this.modules = modules;
    }
    @SuppressWarnings ( "unchecked" )
    @Override
    public AuthStatus validateRequest ( MessageInfo messageInfo, Subject clientSubject,
                                        Subject serviceSubject ) throws AuthException {
        for ( int moduleIndex = 0; moduleIndex < modules.size(); moduleIndex++ ) {
            ServerAuthModule module = modules.get ( moduleIndex );
            AuthStatus result = module.validateRequest ( messageInfo, clientSubject, serviceSubject );
            if ( result != AuthStatus.SEND_FAILURE ) {
                messageInfo.getMap().put ( "moduleIndex", Integer.valueOf ( moduleIndex ) );
                return result;
            }
        }
        return AuthStatus.SEND_FAILURE;
    }
    @Override
    public AuthStatus secureResponse ( MessageInfo messageInfo, Subject serviceSubject )
    throws AuthException {
        ServerAuthModule module = modules.get ( ( ( Integer ) messageInfo.getMap().get ( "moduleIndex" ) ).intValue() );
        return module.secureResponse ( messageInfo, serviceSubject );
    }
    @Override
    public void cleanSubject ( MessageInfo messageInfo, Subject subject ) throws AuthException {
        for ( ServerAuthModule module : modules ) {
            module.cleanSubject ( messageInfo, subject );
        }
    }
}
