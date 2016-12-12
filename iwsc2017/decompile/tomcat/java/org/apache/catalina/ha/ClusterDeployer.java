package org.apache.catalina.ha;
import java.io.IOException;
import java.io.File;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.tribes.ChannelListener;
public interface ClusterDeployer extends ChannelListener {
    void start() throws Exception;
    void stop() throws LifecycleException;
    void install ( String p0, File p1 ) throws IOException;
    void remove ( String p0, boolean p1 ) throws IOException;
    void backgroundProcess();
    CatalinaCluster getCluster();
    void setCluster ( CatalinaCluster p0 );
}
