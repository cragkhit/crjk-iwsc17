package javax.el;
import java.io.File;
import java.security.PrivilegedAction;
static final class ExpressionFactory$1 implements PrivilegedAction<String> {
    @Override
    public String run() {
        return System.getProperty ( "java.home" ) + File.separator + "lib" + File.separator + "el.properties";
    }
}
