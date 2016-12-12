package org.apache.catalina.startup;
import java.util.HashMap;
import java.util.LinkedHashMap;
protected static class DeployedApplication {
    public final String name;
    public final boolean hasDescriptor;
    public final LinkedHashMap<String, Long> redeployResources;
    public final HashMap<String, Long> reloadResources;
    public long timestamp;
    public boolean loggedDirWarning;
    public DeployedApplication ( final String name, final boolean hasDescriptor ) {
        this.redeployResources = new LinkedHashMap<String, Long>();
        this.reloadResources = new HashMap<String, Long>();
        this.timestamp = System.currentTimeMillis();
        this.loggedDirWarning = false;
        this.name = name;
        this.hasDescriptor = hasDescriptor;
    }
}
