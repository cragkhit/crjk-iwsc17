package org.apache.tomcat.websocket.pojo;
import java.io.IOException;
import javax.websocket.DecodeException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import org.apache.tomcat.websocket.Util;
import java.util.ArrayList;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.lang.reflect.Method;
import javax.websocket.Decoder;
import java.util.List;
import org.apache.tomcat.util.res.StringManager;
public class PojoMessageHandlerWholeText extends PojoMessageHandlerWholeBase<String> {
    private static final StringManager sm;
    private final List<Decoder> decoders;
    private final Class<?> primitiveType;
    public PojoMessageHandlerWholeText ( final Object pojo, final Method method, final Session session, final EndpointConfig config, final List<Class<? extends Decoder>> decoderClazzes, final Object[] params, final int indexPayload, final boolean convert, final int indexSession, final long maxMessageSize ) {
        super ( pojo, method, session, params, indexPayload, convert, indexSession, maxMessageSize );
        this.decoders = new ArrayList<Decoder>();
        if ( maxMessageSize > -1L && maxMessageSize > session.getMaxTextMessageBufferSize() ) {
            if ( maxMessageSize > 2147483647L ) {
                throw new IllegalArgumentException ( PojoMessageHandlerWholeText.sm.getString ( "pojoMessageHandlerWhole.maxBufferSize" ) );
            }
            session.setMaxTextMessageBufferSize ( ( int ) maxMessageSize );
        }
        final Class<?> type = method.getParameterTypes() [indexPayload];
        if ( Util.isPrimitive ( type ) ) {
            this.primitiveType = type;
            return;
        }
        this.primitiveType = null;
        try {
            if ( decoderClazzes != null ) {
                for ( final Class<? extends Decoder> decoderClazz : decoderClazzes ) {
                    if ( Decoder.Text.class.isAssignableFrom ( decoderClazz ) ) {
                        final Decoder.Text<?> decoder = ( Decoder.Text<?> ) decoderClazz.newInstance();
                        decoder.init ( config );
                        this.decoders.add ( ( Decoder ) decoder );
                    } else {
                        if ( !Decoder.TextStream.class.isAssignableFrom ( decoderClazz ) ) {
                            continue;
                        }
                        final Decoder.TextStream<?> decoder2 = ( Decoder.TextStream<?> ) decoderClazz.newInstance();
                        decoder2.init ( config );
                        this.decoders.add ( ( Decoder ) decoder2 );
                    }
                }
            }
        } catch ( IllegalAccessException | InstantiationException e ) {
            throw new IllegalArgumentException ( e );
        }
    }
    @Override
    protected Object decode ( final String message ) throws DecodeException {
        if ( this.primitiveType != null ) {
            return Util.coerceToType ( this.primitiveType, message );
        }
        for ( final Decoder decoder : this.decoders ) {
            if ( decoder instanceof Decoder.Text ) {
                if ( ( ( Decoder.Text ) decoder ).willDecode ( message ) ) {
                    return ( ( Decoder.Text ) decoder ).decode ( message );
                }
                continue;
            } else {
                final StringReader r = new StringReader ( message );
                try {
                    return ( ( Decoder.TextStream ) decoder ).decode ( ( Reader ) r );
                } catch ( IOException ioe ) {
                    throw new DecodeException ( message, PojoMessageHandlerWholeText.sm.getString ( "pojoMessageHandlerWhole.decodeIoFail" ), ( Throwable ) ioe );
                }
            }
        }
        return null;
    }
    @Override
    protected Object convert ( final String message ) {
        return new StringReader ( message );
    }
    @Override
    protected void onClose() {
        for ( final Decoder decoder : this.decoders ) {
            decoder.destroy();
        }
    }
    static {
        sm = StringManager.getManager ( PojoMessageHandlerWholeText.class );
    }
}
