package org.apache.tomcat.websocket.pojo;
import java.io.IOException;
import javax.websocket.DecodeException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.ArrayList;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.lang.reflect.Method;
import javax.websocket.Decoder;
import java.util.List;
import org.apache.tomcat.util.res.StringManager;
import java.nio.ByteBuffer;
public class PojoMessageHandlerWholeBinary extends PojoMessageHandlerWholeBase<ByteBuffer> {
    private static final StringManager sm;
    private final List<Decoder> decoders;
    private final boolean isForInputStream;
    public PojoMessageHandlerWholeBinary ( final Object pojo, final Method method, final Session session, final EndpointConfig config, final List<Class<? extends Decoder>> decoderClazzes, final Object[] params, final int indexPayload, final boolean convert, final int indexSession, final boolean isForInputStream, final long maxMessageSize ) {
        super ( pojo, method, session, params, indexPayload, convert, indexSession, maxMessageSize );
        this.decoders = new ArrayList<Decoder>();
        if ( maxMessageSize > -1L && maxMessageSize > session.getMaxBinaryMessageBufferSize() ) {
            if ( maxMessageSize > 2147483647L ) {
                throw new IllegalArgumentException ( PojoMessageHandlerWholeBinary.sm.getString ( "pojoMessageHandlerWhole.maxBufferSize" ) );
            }
            session.setMaxBinaryMessageBufferSize ( ( int ) maxMessageSize );
        }
        try {
            if ( decoderClazzes != null ) {
                for ( final Class<? extends Decoder> decoderClazz : decoderClazzes ) {
                    if ( Decoder.Binary.class.isAssignableFrom ( decoderClazz ) ) {
                        final Decoder.Binary<?> decoder = ( Decoder.Binary<?> ) decoderClazz.newInstance();
                        decoder.init ( config );
                        this.decoders.add ( ( Decoder ) decoder );
                    } else {
                        if ( !Decoder.BinaryStream.class.isAssignableFrom ( decoderClazz ) ) {
                            continue;
                        }
                        final Decoder.BinaryStream<?> decoder2 = ( Decoder.BinaryStream<?> ) decoderClazz.newInstance();
                        decoder2.init ( config );
                        this.decoders.add ( ( Decoder ) decoder2 );
                    }
                }
            }
        } catch ( IllegalAccessException | InstantiationException e ) {
            throw new IllegalArgumentException ( e );
        }
        this.isForInputStream = isForInputStream;
    }
    @Override
    protected Object decode ( final ByteBuffer message ) throws DecodeException {
        for ( final Decoder decoder : this.decoders ) {
            if ( decoder instanceof Decoder.Binary ) {
                if ( ( ( Decoder.Binary ) decoder ).willDecode ( message ) ) {
                    return ( ( Decoder.Binary ) decoder ).decode ( message );
                }
                continue;
            } else {
                final byte[] array = new byte[message.limit() - message.position()];
                message.get ( array );
                final ByteArrayInputStream bais = new ByteArrayInputStream ( array );
                try {
                    return ( ( Decoder.BinaryStream ) decoder ).decode ( ( InputStream ) bais );
                } catch ( IOException ioe ) {
                    throw new DecodeException ( message, PojoMessageHandlerWholeBinary.sm.getString ( "pojoMessageHandlerWhole.decodeIoFail" ), ( Throwable ) ioe );
                }
            }
        }
        return null;
    }
    @Override
    protected Object convert ( final ByteBuffer message ) {
        final byte[] array = new byte[message.remaining()];
        message.get ( array );
        if ( this.isForInputStream ) {
            return new ByteArrayInputStream ( array );
        }
        return array;
    }
    @Override
    protected void onClose() {
        for ( final Decoder decoder : this.decoders ) {
            decoder.destroy();
        }
    }
    static {
        sm = StringManager.getManager ( PojoMessageHandlerWholeBinary.class );
    }
}
