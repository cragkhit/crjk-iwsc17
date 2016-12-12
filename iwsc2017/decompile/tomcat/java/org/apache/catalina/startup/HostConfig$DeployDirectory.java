package org.apache.catalina.startup;
import java.io.File;
import org.apache.catalina.util.ContextName;
private static class DeployDirectory implements Runnable {
    private HostConfig config;
    private ContextName cn;
    private File dir;
    public DeployDirectory ( final HostConfig config, final ContextName cn, final File dir ) {
        this.config = config;
        this.cn = cn;
        this.dir = dir;
    }
    @Override
    public void run() {
        this.config.deployDirectory ( this.cn, this.dir );
    }
}
