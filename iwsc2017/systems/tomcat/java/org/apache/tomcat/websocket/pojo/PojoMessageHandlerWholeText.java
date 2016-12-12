package org.apache.tomcat.websocket.pojo;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.Decoder.Text;
import javax.websocket.Decoder.TextStream;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.websocket.Util;
public class PojoMessageHandlerWholeText
    extends PojoMessageHandlerWholeBase<String> {
    private static final StringManager sm =
        StringManager.getManager ( PojoMessageHandlerWholeText.class );
    private final List<Decoder> decoders = new ArrayList<>();
    private final Class<?> primitiveType;
    public PojoMessageHandlerWholeText ( Object pojo, Method method,
                                         Session session, EndpointConfig config,
                                         List<Class<? extends Decoder>> decoderClazzes, Object[] params,
                                         int indexPayload, boolean convert, int indexSession,
                                         long maxMessageSize ) {
        super ( pojo, method, session, params, indexPayload, convert,
                indexSession, maxMessageSize );
        if ( maxMessageSize > -1 && maxMessageSize > session.getMaxTextMessageBufferSize() ) {
            if ( maxMessageSize > Integer.MAX_VALUE ) {
                throw new IllegalArgumentException ( sm.getString (
                        "pojoMessageHandlerWhole.maxBufferSize" ) );
            }
            session.setMaxTextMessageBufferSize ( ( int ) maxMessageSize );
        }
        Class<?> type = method.getParameterTypes() [indexPayload];
        if ( Util.isPrimitive ( type ) ) {
            primitiveType = type;
            return;
        } else {
            primitiveType = null;
        }
        try {
            if ( decoderClazzes != null ) {
                for ( Class<? extends Decoder> decoderClazz : decoderClazzes ) {
                    if ( Text.class.isAssignableFrom ( decoderClazz ) ) {
                        Text<?> decoder = ( Text<?> ) decoderClazz.newInstance();
                        decoder.init ( config );
                        decoders.add ( decoder );
                    } else if ( TextStream.class.isAssignableFrom (
                                    decoderClazz ) ) {
                        TextStream<?> decoder =
                            ( TextStream<?> ) decoderClazz.newInstance();
                        decoder.init ( config );
                        decoders.add ( decoder );
                    } else {
                    }
                }
            }
        } catch ( IllegalAccessException | InstantiationException e ) {
            throw new IllegalArgumentException ( e );
        }
    }
    @Override
    protected Object decode ( String message ) throws DecodeException {
        if ( primitiveType != null ) {
            return Util.coerceToType ( primitiveType, message );
        }
        for ( Decoder decoder : decoders ) {
            if ( decoder instanceof Text ) {
                if ( ( ( Text<?> ) decoder ).willDecode ( message ) ) {
                    return ( ( Text<?> ) decoder ).decode ( message );
                }
            } else {
                StringReader r = new StringReader ( message );
                try {
                    return ( ( TextStream<?> ) decoder ).decode ( r );
                } catch ( IOException ioe ) {
                    throw new DecodeException ( message, sm.getString (
                                                    "pojoMessageHandlerWhole.decodeIoFail" ), ioe );
                }
            }
        }
        return null;
    }
    @Override
    protected Object convert ( String message ) {
        return new StringReader ( message );
    }
    @Override
    protected void onClose() {
        for ( Decoder decoder : decoders ) {
            decoder.destroy();
        }
    }
}
