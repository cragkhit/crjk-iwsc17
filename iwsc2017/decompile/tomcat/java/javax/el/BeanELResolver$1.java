package javax.el;
import java.security.PrivilegedAction;
static final class BeanELResolver$1 implements PrivilegedAction<String> {
    @Override
    public String run() {
        return System.getProperty ( "org.apache.el.BeanELResolver.CACHE_SIZE", "1000" );
    }
}
