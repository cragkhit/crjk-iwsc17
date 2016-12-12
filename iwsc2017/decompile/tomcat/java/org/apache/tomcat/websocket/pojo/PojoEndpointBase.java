package org.apache.tomcat.websocket.pojo;
import org.apache.juli.logging.LogFactory;
import java.util.Set;
import javax.websocket.CloseReason;
import java.io.IOException;
import org.apache.tomcat.util.ExceptionUtils;
import java.util.Iterator;
import java.lang.reflect.InvocationTargetException;
import javax.websocket.MessageHandler;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.util.Map;
import org.apache.tomcat.util.res.StringManager;
import org.apache.juli.logging.Log;
import javax.websocket.Endpoint;
public abstract class PojoEndpointBase extends Endpoint {
    private static final Log log;
    private static final StringManager sm;
    private Object pojo;
    private Map<String, String> pathParameters;
    private PojoMethodMapping methodMapping;
    protected final void doOnOpen ( final Session session, final EndpointConfig config ) {
        final PojoMethodMapping methodMapping = this.getMethodMapping();
        final Object pojo = this.getPojo();
        final Map<String, String> pathParameters = this.getPathParameters();
        for ( final MessageHandler mh : methodMapping.getMessageHandlers ( pojo, pathParameters, session, config ) ) {
            session.addMessageHandler ( mh );
        }
        if ( methodMapping.getOnOpen() != null ) {
            try {
                methodMapping.getOnOpen().invoke ( pojo, methodMapping.getOnOpenArgs ( pathParameters, session, config ) );
            } catch ( IllegalAccessException e ) {
                PojoEndpointBase.log.error ( PojoEndpointBase.sm.getString ( "pojoEndpointBase.onOpenFail", pojo.getClass().getName() ), e );
                this.handleOnOpenOrCloseError ( session, e );
            } catch ( InvocationTargetException e2 ) {
                final Throwable cause = e2.getCause();
                this.handleOnOpenOrCloseError ( session, cause );
            } catch ( Throwable t ) {
                this.handleOnOpenOrCloseError ( session, t );
            }
        }
    }
    private void handleOnOpenOrCloseError ( final Session session, final Throwable t ) {
        ExceptionUtils.handleThrowable ( t );
        this.onError ( session, t );
        try {
            session.close();
        } catch ( IOException ioe ) {
            PojoEndpointBase.log.warn ( PojoEndpointBase.sm.getString ( "pojoEndpointBase.closeSessionFail" ), ioe );
        }
    }
    public final void onClose ( final Session session, final CloseReason closeReason ) {
        if ( this.methodMapping.getOnClose() != null ) {
            try {
                this.methodMapping.getOnClose().invoke ( this.pojo, this.methodMapping.getOnCloseArgs ( this.pathParameters, session, closeReason ) );
            } catch ( Throwable t ) {
                PojoEndpointBase.log.error ( PojoEndpointBase.sm.getString ( "pojoEndpointBase.onCloseFail", this.pojo.getClass().getName() ), t );
                this.handleOnOpenOrCloseError ( session, t );
            }
        }
        final Set<MessageHandler> messageHandlers = ( Set<MessageHandler> ) session.getMessageHandlers();
        for ( final MessageHandler messageHandler : messageHandlers ) {
            if ( messageHandler instanceof PojoMessageHandlerWholeBase ) {
                ( ( PojoMessageHandlerWholeBase ) messageHandler ).onClose();
            }
        }
    }
    public final void onError ( final Session session, final Throwable throwable ) {
        if ( this.methodMapping.getOnError() == null ) {
            PojoEndpointBase.log.error ( PojoEndpointBase.sm.getString ( "pojoEndpointBase.onError", this.pojo.getClass().getName() ), throwable );
        } else {
            try {
                this.methodMapping.getOnError().invoke ( this.pojo, this.methodMapping.getOnErrorArgs ( this.pathParameters, session, throwable ) );
            } catch ( Throwable t ) {
                ExceptionUtils.handleThrowable ( t );
                PojoEndpointBase.log.error ( PojoEndpointBase.sm.getString ( "pojoEndpointBase.onErrorFail", this.pojo.getClass().getName() ), t );
            }
        }
    }
    protected Object getPojo() {
        return this.pojo;
    }
    protected void setPojo ( final Object pojo ) {
        this.pojo = pojo;
    }
    protected Map<String, String> getPathParameters() {
        return this.pathParameters;
    }
    protected void setPathParameters ( final Map<String, String> pathParameters ) {
        this.pathParameters = pathParameters;
    }
    protected PojoMethodMapping getMethodMapping() {
        return this.methodMapping;
    }
    protected void setMethodMapping ( final PojoMethodMapping methodMapping ) {
        this.methodMapping = methodMapping;
    }
    static {
        log = LogFactory.getLog ( PojoEndpointBase.class );
        sm = StringManager.getManager ( PojoEndpointBase.class );
    }
}
