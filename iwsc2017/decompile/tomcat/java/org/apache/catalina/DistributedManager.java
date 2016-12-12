package org.apache.catalina;
import java.util.Set;
public interface DistributedManager {
    int getActiveSessionsFull();
    Set<String> getSessionIdsFull();
}
