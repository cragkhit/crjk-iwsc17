package org.apache.catalina.loader;
public class ResourceEntry {
    public long lastModified;
    public volatile Class<?> loadedClass;
    public ResourceEntry() {
        this.lastModified = -1L;
        this.loadedClass = null;
    }
}
