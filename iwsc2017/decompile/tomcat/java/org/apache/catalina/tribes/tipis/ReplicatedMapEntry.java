package org.apache.catalina.tribes.tipis;
import java.io.IOException;
import java.io.Serializable;
public interface ReplicatedMapEntry extends Serializable {
    boolean isDirty();
    boolean isDiffable();
    byte[] getDiff() throws IOException;
    void applyDiff ( byte[] p0, int p1, int p2 ) throws IOException, ClassNotFoundException;
    void resetDiff();
    void lock();
    void unlock();
    void setOwner ( Object p0 );
    long getVersion();
    void setVersion ( long p0 );
    long getLastTimeReplicated();
    void setLastTimeReplicated ( long p0 );
    boolean isAccessReplicate();
    void accessEntry();
}
