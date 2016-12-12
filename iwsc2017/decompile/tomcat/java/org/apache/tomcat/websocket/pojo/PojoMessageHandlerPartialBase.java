package org.apache.tomcat.websocket.pojo;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import org.apache.tomcat.websocket.WsSession;
import javax.websocket.DecodeException;
import javax.websocket.Session;
import java.lang.reflect.Method;
import javax.websocket.MessageHandler;
public abstract class PojoMessageHandlerPartialBase<T> extends PojoMessageHandlerBase<T> implements MessageHandler.Partial<T> {
    private final int indexBoolean;
    public PojoMessageHandlerPartialBase ( final Object pojo, final Method method, final Session session, final Object[] params, final int indexPayload, final boolean convert, final int indexBoolean, final int indexSession, final long maxMessageSize ) {
        super ( pojo, method, session, params, indexPayload, convert, indexSession, maxMessageSize );
        this.indexBoolean = indexBoolean;
    }
    public final void onMessage ( final T message, final boolean last ) {
        if ( this.params.length == 1 && this.params[0] instanceof DecodeException ) {
            ( ( WsSession ) this.session ).getLocal().onError ( this.session, ( Throwable ) this.params[0] );
            return;
        }
        final Object[] parameters = this.params.clone();
        if ( this.indexBoolean != -1 ) {
            parameters[this.indexBoolean] = last;
        }
        if ( this.indexSession != -1 ) {
            parameters[this.indexSession] = this.session;
        }
        if ( this.convert ) {
            parameters[this.indexPayload] = ( ( ByteBuffer ) message ).array();
        } else {
            parameters[this.indexPayload] = message;
        }
        Object result = null;
        try {
            result = this.method.invoke ( this.pojo, parameters );
        } catch ( IllegalAccessException | InvocationTargetException e ) {
            this.handlePojoMethodException ( e );
        }
        this.processResult ( result );
    }
}
