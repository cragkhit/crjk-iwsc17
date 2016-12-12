package org.apache.tomcat.util.net;
public enum SocketState {
    OPEN,
    CLOSED,
    LONG,
    ASYNC_END,
    SENDFILE,
    UPGRADING,
    UPGRADED;
}
