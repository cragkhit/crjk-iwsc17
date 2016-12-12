package org.apache.tomcat.websocket.server;
private static class Segment {
    private final int parameterIndex;
    private final String value;
    public Segment ( final int parameterIndex, final String value ) {
        this.parameterIndex = parameterIndex;
        this.value = value;
    }
    public int getParameterIndex() {
        return this.parameterIndex;
    }
    public String getValue() {
        return this.value;
    }
}
