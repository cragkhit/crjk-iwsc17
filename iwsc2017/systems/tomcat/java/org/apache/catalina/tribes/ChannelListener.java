package org.apache.catalina.tribes;
import java.io.Serializable;
public interface ChannelListener {
    public void messageReceived ( Serializable msg, Member sender );
    public boolean accept ( Serializable msg, Member sender );
    @Override
    public boolean equals ( Object listener );
    @Override
    public int hashCode();
}
