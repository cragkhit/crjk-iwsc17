package org.apache.catalina.startup;
import java.io.File;
import org.apache.catalina.util.ContextName;
private static class DeployWar implements Runnable {
    private HostConfig config;
    private ContextName cn;
    private File war;
    public DeployWar ( final HostConfig config, final ContextName cn, final File war ) {
        this.config = config;
        this.cn = cn;
        this.war = war;
    }
    @Override
    public void run() {
        this.config.deployWAR ( this.cn, this.war );
    }
}
