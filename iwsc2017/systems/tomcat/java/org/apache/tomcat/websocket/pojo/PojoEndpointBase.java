package org.apache.tomcat.websocket.pojo;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.res.StringManager;
public abstract class PojoEndpointBase extends Endpoint {
    private static final Log log = LogFactory.getLog ( PojoEndpointBase.class );
    private static final StringManager sm = StringManager.getManager ( PojoEndpointBase.class );
    private Object pojo;
    private Map<String, String> pathParameters;
    private PojoMethodMapping methodMapping;
    protected final void doOnOpen ( Session session, EndpointConfig config ) {
        PojoMethodMapping methodMapping = getMethodMapping();
        Object pojo = getPojo();
        Map<String, String> pathParameters = getPathParameters();
        for ( MessageHandler mh : methodMapping.getMessageHandlers ( pojo,
                pathParameters, session, config ) ) {
            session.addMessageHandler ( mh );
        }
        if ( methodMapping.getOnOpen() != null ) {
            try {
                methodMapping.getOnOpen().invoke ( pojo,
                                                   methodMapping.getOnOpenArgs (
                                                       pathParameters, session, config ) );
            } catch ( IllegalAccessException e ) {
                log.error ( sm.getString (
                                "pojoEndpointBase.onOpenFail",
                                pojo.getClass().getName() ), e );
                handleOnOpenOrCloseError ( session, e );
                return;
            } catch ( InvocationTargetException e ) {
                Throwable cause = e.getCause();
                handleOnOpenOrCloseError ( session, cause );
                return;
            } catch ( Throwable t ) {
                handleOnOpenOrCloseError ( session, t );
                return;
            }
        }
    }
    private void handleOnOpenOrCloseError ( Session session, Throwable t ) {
        ExceptionUtils.handleThrowable ( t );
        onError ( session, t );
        try {
            session.close();
        } catch ( IOException ioe ) {
            log.warn ( sm.getString ( "pojoEndpointBase.closeSessionFail" ), ioe );
        }
    }
    @Override
    public final void onClose ( Session session, CloseReason closeReason ) {
        if ( methodMapping.getOnClose() != null ) {
            try {
                methodMapping.getOnClose().invoke ( pojo,
                                                    methodMapping.getOnCloseArgs ( pathParameters, session, closeReason ) );
            } catch ( Throwable t ) {
                log.error ( sm.getString ( "pojoEndpointBase.onCloseFail",
                                           pojo.getClass().getName() ), t );
                handleOnOpenOrCloseError ( session, t );
            }
        }
        Set<MessageHandler> messageHandlers = session.getMessageHandlers();
        for ( MessageHandler messageHandler : messageHandlers ) {
            if ( messageHandler instanceof PojoMessageHandlerWholeBase<?> ) {
                ( ( PojoMessageHandlerWholeBase<?> ) messageHandler ).onClose();
            }
        }
    }
    @Override
    public final void onError ( Session session, Throwable throwable ) {
        if ( methodMapping.getOnError() == null ) {
            log.error ( sm.getString ( "pojoEndpointBase.onError",
                                       pojo.getClass().getName() ), throwable );
        } else {
            try {
                methodMapping.getOnError().invoke (
                    pojo,
                    methodMapping.getOnErrorArgs ( pathParameters, session,
                                                   throwable ) );
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                log.error ( sm.getString ( "pojoEndpointBase.onErrorFail",
                                           pojo.getClass().getName() ), t );
            }
        }
    }
    protected Object getPojo() {
        return pojo;
    }
    protected void setPojo ( Object pojo ) {
        this.pojo = pojo;
    }
    protected Map<String, String> getPathParameters() {
        return pathParameters;
    }
    protected void setPathParameters ( Map<String, String> pathParameters ) {
        this.pathParameters = pathParameters;
    }
    protected PojoMethodMapping getMethodMapping() {
        return methodMapping;
    }
    protected void setMethodMapping ( PojoMethodMapping methodMapping ) {
        this.methodMapping = methodMapping;
    }
}
