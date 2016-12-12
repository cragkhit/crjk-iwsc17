package org.apache.tomcat.websocket.pojo;
import java.lang.reflect.Method;
import javax.websocket.PongMessage;
import javax.websocket.Session;
public class PojoMessageHandlerWholePong
    extends PojoMessageHandlerWholeBase<PongMessage> {
    public PojoMessageHandlerWholePong ( Object pojo, Method method,
                                         Session session, Object[] params, int indexPayload, boolean convert,
                                         int indexSession ) {
        super ( pojo, method, session, params, indexPayload, convert,
                indexSession, -1 );
    }
    @Override
    protected Object decode ( PongMessage message ) {
        return null;
    }
    @Override
    protected void onClose() {
    }
}
