package org.apache.tomcat.websocket;
import java.util.Iterator;
import java.util.ArrayList;
import javax.websocket.Decoder;
import java.util.List;
public static class DecoderMatch {
    private final List<Class<? extends Decoder>> textDecoders;
    private final List<Class<? extends Decoder>> binaryDecoders;
    private final Class<?> target;
    public DecoderMatch ( final Class<?> target, final List<DecoderEntry> decoderEntries ) {
        this.textDecoders = new ArrayList<Class<? extends Decoder>>();
        this.binaryDecoders = new ArrayList<Class<? extends Decoder>>();
        this.target = target;
        for ( final DecoderEntry decoderEntry : decoderEntries ) {
            if ( decoderEntry.getClazz().isAssignableFrom ( target ) ) {
                if ( Decoder.Binary.class.isAssignableFrom ( decoderEntry.getDecoderClazz() ) ) {
                    this.binaryDecoders.add ( decoderEntry.getDecoderClazz() );
                } else {
                    if ( Decoder.BinaryStream.class.isAssignableFrom ( decoderEntry.getDecoderClazz() ) ) {
                        this.binaryDecoders.add ( decoderEntry.getDecoderClazz() );
                        break;
                    }
                    if ( Decoder.Text.class.isAssignableFrom ( decoderEntry.getDecoderClazz() ) ) {
                        this.textDecoders.add ( decoderEntry.getDecoderClazz() );
                    } else {
                        if ( Decoder.TextStream.class.isAssignableFrom ( decoderEntry.getDecoderClazz() ) ) {
                            this.textDecoders.add ( decoderEntry.getDecoderClazz() );
                            break;
                        }
                        throw new IllegalArgumentException ( Util.access$000().getString ( "util.unknownDecoderType" ) );
                    }
                }
            }
        }
    }
    public List<Class<? extends Decoder>> getTextDecoders() {
        return this.textDecoders;
    }
    public List<Class<? extends Decoder>> getBinaryDecoders() {
        return this.binaryDecoders;
    }
    public Class<?> getTarget() {
        return this.target;
    }
    public boolean hasMatches() {
        return this.textDecoders.size() > 0 || this.binaryDecoders.size() > 0;
    }
}
