// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import java.util.Timer;
import java.security.PrivilegedAction;

private static class PrivilegedNewTimer implements PrivilegedAction<Timer>
{
    @Override
    public Timer run() {
        return new Timer("Tomcat JDBC Pool Cleaner[" + System.identityHashCode(ConnectionPool.class.getClassLoader()) + ":" + System.currentTimeMillis() + "]", true);
    }
}
