package org.apache.catalina.mapper;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.Context;
protected static final class ContextVersion extends MapElement<Context> {
    public final String path;
    public final int slashCount;
    public final WebResourceRoot resources;
    public String[] welcomeResources;
    public MappedWrapper defaultWrapper;
    public MappedWrapper[] exactWrappers;
    public MappedWrapper[] wildcardWrappers;
    public MappedWrapper[] extensionWrappers;
    public int nesting;
    private volatile boolean paused;
    public ContextVersion ( final String version, final String path, final int slashCount, final Context context, final WebResourceRoot resources, final String[] welcomeResources ) {
        super ( version, context );
        this.defaultWrapper = null;
        this.exactWrappers = new MappedWrapper[0];
        this.wildcardWrappers = new MappedWrapper[0];
        this.extensionWrappers = new MappedWrapper[0];
        this.nesting = 0;
        this.path = path;
        this.slashCount = slashCount;
        this.resources = resources;
        this.welcomeResources = welcomeResources;
    }
    public boolean isPaused() {
        return this.paused;
    }
    public void markPaused() {
        this.paused = true;
    }
}
