package org.apache.catalina.tribes.util;
import java.security.PrivilegedAction;
class TcclThreadFactory$1 implements PrivilegedAction<Void> {
    final   Thread val$t;
    @Override
    public Void run() {
        this.val$t.setContextClassLoader ( this.getClass().getClassLoader() );
        return null;
    }
}
