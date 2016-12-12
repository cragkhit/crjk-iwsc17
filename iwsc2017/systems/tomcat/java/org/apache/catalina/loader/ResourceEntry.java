package org.apache.catalina.loader;
public class ResourceEntry {
    public long lastModified = -1;
    public volatile Class<?> loadedClass = null;
}
