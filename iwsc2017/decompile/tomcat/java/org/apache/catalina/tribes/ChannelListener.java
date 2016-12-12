package org.apache.catalina.tribes;
import java.io.Serializable;
public interface ChannelListener {
    void messageReceived ( Serializable p0, Member p1 );
    boolean accept ( Serializable p0, Member p1 );
    boolean equals ( Object p0 );
    int hashCode();
}
