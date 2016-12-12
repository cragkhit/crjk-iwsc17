package org.apache.el.lang;
import java.security.PrivilegedAction;
static final class ELSupport$1 implements PrivilegedAction<String> {
    @Override
    public String run() {
        return System.getProperty ( "org.apache.el.parser.COERCE_TO_ZERO", "false" );
    }
}
