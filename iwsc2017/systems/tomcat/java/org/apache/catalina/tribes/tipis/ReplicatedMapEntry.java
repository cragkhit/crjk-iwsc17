package org.apache.catalina.tribes.tipis;
import java.io.IOException;
import java.io.Serializable;
public interface ReplicatedMapEntry extends Serializable {
    public boolean isDirty();
    public boolean isDiffable();
    public byte[] getDiff() throws IOException;
    public void applyDiff ( byte[] diff, int offset, int length ) throws IOException, ClassNotFoundException;
    public void resetDiff();
    public void lock();
    public void unlock();
    public void setOwner ( Object owner );
    public long getVersion();
    public void setVersion ( long version );
    public long getLastTimeReplicated();
    public void setLastTimeReplicated ( long lastTimeReplicated );
    public boolean isAccessReplicate();
    public void accessEntry();
}
