package javax.security.auth.message.config;
import java.security.PrivilegedExceptionAction;
static final class AuthConfigFactory$1 implements PrivilegedExceptionAction<AuthConfigFactory> {
    final   String val$className;
    @Override
    public AuthConfigFactory run() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        final Class<?> clazz = Class.forName ( this.val$className );
        return ( AuthConfigFactory ) clazz.newInstance();
    }
}
