package org.apache.tomcat.websocket.pojo;
import javax.websocket.Session;
import java.lang.reflect.Method;
public class PojoMessageHandlerPartialText extends PojoMessageHandlerPartialBase<String> {
    public PojoMessageHandlerPartialText ( final Object pojo, final Method method, final Session session, final Object[] params, final int indexPayload, final boolean convert, final int indexBoolean, final int indexSession, final long maxMessageSize ) {
        super ( pojo, method, session, params, indexPayload, convert, indexBoolean, indexSession, maxMessageSize );
    }
}
