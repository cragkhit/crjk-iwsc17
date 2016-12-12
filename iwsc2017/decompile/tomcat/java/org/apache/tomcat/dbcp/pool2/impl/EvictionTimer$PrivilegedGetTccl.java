package org.apache.tomcat.dbcp.pool2.impl;
import java.security.PrivilegedAction;
private static class PrivilegedGetTccl implements PrivilegedAction<ClassLoader> {
    @Override
    public ClassLoader run() {
        return Thread.currentThread().getContextClassLoader();
    }
}
