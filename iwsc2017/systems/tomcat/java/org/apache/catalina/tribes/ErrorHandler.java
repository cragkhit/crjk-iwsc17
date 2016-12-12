package org.apache.catalina.tribes;
public interface ErrorHandler {
    public void handleError ( ChannelException x, UniqueId id );
    public void handleCompletion ( UniqueId id );
}
