package org.apache.coyote;
import org.apache.tomcat.InstanceManager;
import javax.servlet.http.HttpUpgradeHandler;
import org.apache.tomcat.ContextBind;
public final class UpgradeToken {
    private final ContextBind contextBind;
    private final HttpUpgradeHandler httpUpgradeHandler;
    private final InstanceManager instanceManager;
    public UpgradeToken ( final HttpUpgradeHandler httpUpgradeHandler, final ContextBind contextBind, final InstanceManager instanceManager ) {
        this.contextBind = contextBind;
        this.httpUpgradeHandler = httpUpgradeHandler;
        this.instanceManager = instanceManager;
    }
    public final ContextBind getContextBind() {
        return this.contextBind;
    }
    public final HttpUpgradeHandler getHttpUpgradeHandler() {
        return this.httpUpgradeHandler;
    }
    public final InstanceManager getInstanceManager() {
        return this.instanceManager;
    }
}
