package org.apache.catalina.ha;
import org.apache.catalina.tribes.Member;
import java.io.Serializable;
public interface ClusterMessage extends Serializable {
    Member getAddress();
    void setAddress ( Member p0 );
    String getUniqueId();
    long getTimestamp();
    void setTimestamp ( long p0 );
}
