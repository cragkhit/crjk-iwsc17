package org.apache.catalina.ha;
import java.io.IOException;
import org.apache.catalina.tribes.io.ReplicationStream;
import org.apache.catalina.Manager;
public interface ClusterManager extends Manager {
    void messageDataReceived ( ClusterMessage p0 );
    ClusterMessage requestCompleted ( String p0 );
    String[] getInvalidatedSessions();
    String getName();
    void setName ( String p0 );
    CatalinaCluster getCluster();
    void setCluster ( CatalinaCluster p0 );
    ReplicationStream getReplicationStream ( byte[] p0 ) throws IOException;
    ReplicationStream getReplicationStream ( byte[] p0, int p1, int p2 ) throws IOException;
    boolean isNotifyListenersOnReplication();
    ClusterManager cloneFromTemplate();
}
