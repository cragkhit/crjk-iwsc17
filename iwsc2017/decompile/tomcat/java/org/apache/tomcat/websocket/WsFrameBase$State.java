package org.apache.tomcat.websocket;
private enum State {
    NEW_FRAME,
    PARTIAL_HEADER,
    DATA;
}
