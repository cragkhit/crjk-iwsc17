package org.apache.catalina.ha;
import org.apache.catalina.tribes.Member;
public abstract class ClusterMessageBase implements ClusterMessage {
    private static final long serialVersionUID = 1L;
    private long timestamp;
    protected transient Member address;
    @Override
    public Member getAddress() {
        return this.address;
    }
    @Override
    public long getTimestamp() {
        return this.timestamp;
    }
    @Override
    public void setAddress ( final Member member ) {
        this.address = member;
    }
    @Override
    public void setTimestamp ( final long timestamp ) {
        this.timestamp = timestamp;
    }
}
