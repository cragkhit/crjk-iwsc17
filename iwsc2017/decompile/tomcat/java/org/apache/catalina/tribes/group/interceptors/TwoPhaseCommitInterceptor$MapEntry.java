package org.apache.catalina.tribes.group.interceptors;
import org.apache.catalina.tribes.UniqueId;
import org.apache.catalina.tribes.ChannelMessage;
public static class MapEntry {
    public final ChannelMessage msg;
    public final UniqueId id;
    public final long timestamp;
    public MapEntry ( final ChannelMessage msg, final UniqueId id, final long timestamp ) {
        this.msg = msg;
        this.id = id;
        this.timestamp = timestamp;
    }
    public boolean expired ( final long now, final long expiration ) {
        return now - this.timestamp > expiration;
    }
}
