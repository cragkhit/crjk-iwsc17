package org.apache.el.lang;
import java.security.PrivilegedAction;
static final class ExpressionBuilder$1 implements PrivilegedAction<String> {
    @Override
    public String run() {
        return System.getProperty ( "org.apache.el.ExpressionBuilder.CACHE_SIZE", "5000" );
    }
}
