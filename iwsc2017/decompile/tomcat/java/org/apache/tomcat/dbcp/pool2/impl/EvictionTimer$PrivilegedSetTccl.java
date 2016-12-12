package org.apache.tomcat.dbcp.pool2.impl;
import java.security.PrivilegedAction;
private static class PrivilegedSetTccl implements PrivilegedAction<Void> {
    private final ClassLoader classLoader;
    PrivilegedSetTccl ( final ClassLoader cl ) {
        this.classLoader = cl;
    }
    @Override
    public Void run() {
        Thread.currentThread().setContextClassLoader ( this.classLoader );
        return null;
    }
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append ( "PrivilegedSetTccl [classLoader=" );
        builder.append ( this.classLoader );
        builder.append ( "]" );
        return builder.toString();
    }
}
