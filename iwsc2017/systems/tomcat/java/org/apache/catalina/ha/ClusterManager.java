package org.apache.catalina.ha;
import java.io.IOException;
import org.apache.catalina.Manager;
import org.apache.catalina.tribes.io.ReplicationStream;
public interface ClusterManager extends Manager {
    public void messageDataReceived ( ClusterMessage msg );
    public ClusterMessage requestCompleted ( String sessionId );
    public String[] getInvalidatedSessions();
    public String getName();
    public void setName ( String name );
    public CatalinaCluster getCluster();
    public void setCluster ( CatalinaCluster cluster );
    public ReplicationStream getReplicationStream ( byte[] data ) throws IOException;
    public ReplicationStream getReplicationStream ( byte[] data, int offset, int length ) throws IOException;
    public boolean isNotifyListenersOnReplication();
    public ClusterManager cloneFromTemplate();
}
