package org.apache.catalina.ha.deploy;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.ha.ClusterMessage;
public class UndeployMessage implements ClusterMessage {
    private static final long serialVersionUID = 2L;
    private Member address;
    private long timestamp;
    private String uniqueId;
    private final String contextName;
    public UndeployMessage ( final Member address, final long timestamp, final String uniqueId, final String contextName ) {
        this.address = address;
        this.timestamp = timestamp;
        this.uniqueId = uniqueId;
        this.contextName = contextName;
    }
    @Override
    public Member getAddress() {
        return this.address;
    }
    @Override
    public void setAddress ( final Member address ) {
        this.address = address;
    }
    @Override
    public long getTimestamp() {
        return this.timestamp;
    }
    @Override
    public void setTimestamp ( final long timestamp ) {
        this.timestamp = timestamp;
    }
    @Override
    public String getUniqueId() {
        return this.uniqueId;
    }
    public String getContextName() {
        return this.contextName;
    }
}
