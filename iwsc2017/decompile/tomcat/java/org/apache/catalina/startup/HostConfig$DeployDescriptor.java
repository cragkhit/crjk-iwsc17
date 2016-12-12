package org.apache.catalina.startup;
import java.io.File;
import org.apache.catalina.util.ContextName;
private static class DeployDescriptor implements Runnable {
    private HostConfig config;
    private ContextName cn;
    private File descriptor;
    public DeployDescriptor ( final HostConfig config, final ContextName cn, final File descriptor ) {
        this.config = config;
        this.cn = cn;
        this.descriptor = descriptor;
    }
    @Override
    public void run() {
        this.config.deployDescriptor ( this.cn, this.descriptor );
    }
}
