package org.apache.tomcat.dbcp.pool2.impl;
import java.util.Timer;
import java.security.PrivilegedAction;
private static class PrivilegedNewEvictionTimer implements PrivilegedAction<Timer> {
    @Override
    public Timer run() {
        return new Timer ( "commons-pool-EvictionTimer", true );
    }
}
