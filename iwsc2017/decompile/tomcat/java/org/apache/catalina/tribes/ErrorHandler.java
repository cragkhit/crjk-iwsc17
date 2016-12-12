package org.apache.catalina.tribes;
public interface ErrorHandler {
    void handleError ( ChannelException p0, UniqueId p1 );
    void handleCompletion ( UniqueId p0 );
}
