package org.apache.tomcat.util.security;
import java.security.PrivilegedAction;
public class PrivilegedSetTccl implements PrivilegedAction<Void> {
    private ClassLoader cl;
    public PrivilegedSetTccl ( final ClassLoader cl ) {
        this.cl = cl;
    }
    @Override
    public Void run() {
        Thread.currentThread().setContextClassLoader ( this.cl );
        return null;
    }
}
