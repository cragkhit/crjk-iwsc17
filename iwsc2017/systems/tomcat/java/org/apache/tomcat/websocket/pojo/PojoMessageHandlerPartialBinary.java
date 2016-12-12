package org.apache.tomcat.websocket.pojo;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import javax.websocket.Session;
public class PojoMessageHandlerPartialBinary
    extends PojoMessageHandlerPartialBase<ByteBuffer> {
    public PojoMessageHandlerPartialBinary ( Object pojo, Method method,
            Session session, Object[] params, int indexPayload, boolean convert,
            int indexBoolean, int indexSession, long maxMessageSize ) {
        super ( pojo, method, session, params, indexPayload, convert, indexBoolean,
                indexSession, maxMessageSize );
    }
}
