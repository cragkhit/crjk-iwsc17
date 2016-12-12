package org.apache.tomcat.dbcp.pool2;
import java.util.Timer;
static class TimerHolder {
    static final Timer MIN_IDLE_TIMER;
    static {
        MIN_IDLE_TIMER = new Timer ( true );
    }
}
