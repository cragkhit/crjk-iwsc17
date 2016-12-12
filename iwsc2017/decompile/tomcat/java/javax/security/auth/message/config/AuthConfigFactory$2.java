package javax.security.auth.message.config;
import java.security.Security;
import java.security.PrivilegedAction;
static final class AuthConfigFactory$2 implements PrivilegedAction<String> {
    @Override
    public String run() {
        return Security.getProperty ( "authconfigprovider.factory" );
    }
}
