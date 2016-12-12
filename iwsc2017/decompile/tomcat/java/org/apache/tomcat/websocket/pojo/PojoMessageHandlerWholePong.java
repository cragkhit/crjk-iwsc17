package org.apache.tomcat.websocket.pojo;
import javax.websocket.DecodeException;
import javax.websocket.Session;
import java.lang.reflect.Method;
import javax.websocket.PongMessage;
public class PojoMessageHandlerWholePong extends PojoMessageHandlerWholeBase<PongMessage> {
    public PojoMessageHandlerWholePong ( final Object pojo, final Method method, final Session session, final Object[] params, final int indexPayload, final boolean convert, final int indexSession ) {
        super ( pojo, method, session, params, indexPayload, convert, indexSession, -1L );
    }
    @Override
    protected Object decode ( final PongMessage message ) {
        return null;
    }
    @Override
    protected void onClose() {
    }
}
