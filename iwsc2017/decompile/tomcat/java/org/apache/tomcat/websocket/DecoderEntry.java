package org.apache.tomcat.websocket;
import javax.websocket.Decoder;
public class DecoderEntry {
    private final Class<?> clazz;
    private final Class<? extends Decoder> decoderClazz;
    public DecoderEntry ( final Class<?> clazz, final Class<? extends Decoder> decoderClazz ) {
        this.clazz = clazz;
        this.decoderClazz = decoderClazz;
    }
    public Class<?> getClazz() {
        return this.clazz;
    }
    public Class<? extends Decoder> getDecoderClazz() {
        return this.decoderClazz;
    }
}
