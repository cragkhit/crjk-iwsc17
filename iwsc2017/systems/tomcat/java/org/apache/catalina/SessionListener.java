package org.apache.catalina;
import java.util.EventListener;
public interface SessionListener extends EventListener {
    public void sessionEvent ( SessionEvent event );
}
