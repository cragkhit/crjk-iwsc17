package org.apache.el.util;
import java.security.PrivilegedAction;
static final class Validation$1 implements PrivilegedAction<String> {
    @Override
    public String run() {
        return System.getProperty ( "org.apache.el.parser.SKIP_IDENTIFIER_CHECK", "false" );
    }
}
