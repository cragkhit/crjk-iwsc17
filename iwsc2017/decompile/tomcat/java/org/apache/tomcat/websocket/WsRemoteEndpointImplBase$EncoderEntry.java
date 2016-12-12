package org.apache.tomcat.websocket;
import javax.websocket.Encoder;
private static class EncoderEntry {
    private final Class<?> clazz;
    private final Encoder encoder;
    public EncoderEntry ( final Class<?> clazz, final Encoder encoder ) {
        this.clazz = clazz;
        this.encoder = encoder;
    }
    public Class<?> getClazz() {
        return this.clazz;
    }
    public Encoder getEncoder() {
        return this.encoder;
    }
}
