package org.apache.tomcat.websocket.pojo;
import java.lang.reflect.InvocationTargetException;
import org.apache.tomcat.websocket.WsSession;
import javax.websocket.DecodeException;
import javax.websocket.Session;
import java.lang.reflect.Method;
import javax.websocket.MessageHandler;
public abstract class PojoMessageHandlerWholeBase<T> extends PojoMessageHandlerBase<T> implements MessageHandler.Whole<T> {
    public PojoMessageHandlerWholeBase ( final Object pojo, final Method method, final Session session, final Object[] params, final int indexPayload, final boolean convert, final int indexSession, final long maxMessageSize ) {
        super ( pojo, method, session, params, indexPayload, convert, indexSession, maxMessageSize );
    }
    public final void onMessage ( final T message ) {
        if ( this.params.length == 1 && this.params[0] instanceof DecodeException ) {
            ( ( WsSession ) this.session ).getLocal().onError ( this.session, ( Throwable ) this.params[0] );
            return;
        }
        Object payload;
        try {
            payload = this.decode ( message );
        } catch ( DecodeException de ) {
            ( ( WsSession ) this.session ).getLocal().onError ( this.session, ( Throwable ) de );
            return;
        }
        if ( payload == null ) {
            if ( this.convert ) {
                payload = this.convert ( message );
            } else {
                payload = message;
            }
        }
        final Object[] parameters = this.params.clone();
        if ( this.indexSession != -1 ) {
            parameters[this.indexSession] = this.session;
        }
        parameters[this.indexPayload] = payload;
        Object result = null;
        try {
            result = this.method.invoke ( this.pojo, parameters );
        } catch ( IllegalAccessException | InvocationTargetException e ) {
            this.handlePojoMethodException ( e );
        }
        this.processResult ( result );
    }
    protected Object convert ( final T message ) {
        return message;
    }
    protected abstract Object decode ( final T p0 ) throws DecodeException;
    protected abstract void onClose();
}
