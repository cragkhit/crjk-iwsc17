package org.apache.catalina.ha;
import java.io.File;
import java.io.IOException;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.tribes.ChannelListener;
public interface ClusterDeployer extends ChannelListener {
    public void start() throws Exception;
    public void stop() throws LifecycleException;
    public void install ( String contextName, File webapp ) throws IOException;
    public void remove ( String contextName, boolean undeploy ) throws IOException;
    public void backgroundProcess();
    public CatalinaCluster getCluster();
    public void setCluster ( CatalinaCluster cluster );
}
