package org.apache.tomcat.websocket.pojo;
import javax.websocket.PongMessage;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.io.Reader;
import java.util.HashMap;
import javax.websocket.server.PathParam;
import java.util.Collection;
import java.util.HashSet;
import javax.websocket.MessageHandler;
import java.util.Set;
import javax.websocket.DecodeException;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.util.Map;
import java.util.Arrays;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import org.apache.tomcat.websocket.DecoderEntry;
import java.lang.annotation.Annotation;
import javax.websocket.OnMessage;
import javax.websocket.OnError;
import javax.websocket.OnClose;
import javax.websocket.DeploymentException;
import javax.websocket.OnOpen;
import org.apache.tomcat.websocket.Util;
import java.util.ArrayList;
import javax.websocket.Decoder;
import java.util.List;
import java.lang.reflect.Method;
import org.apache.tomcat.util.res.StringManager;
public class PojoMethodMapping {
    private static final StringManager sm;
    private final Method onOpen;
    private final Method onClose;
    private final Method onError;
    private final PojoPathParam[] onOpenParams;
    private final PojoPathParam[] onCloseParams;
    private final PojoPathParam[] onErrorParams;
    private final List<MessageHandlerInfo> onMessage;
    private final String wsPath;
    public PojoMethodMapping ( final Class<?> clazzPojo, final List<Class<? extends Decoder>> decoderClazzes, final String wsPath ) throws DeploymentException {
        this.onMessage = new ArrayList<MessageHandlerInfo>();
        this.wsPath = wsPath;
        final List<DecoderEntry> decoders = Util.getDecoders ( decoderClazzes );
        Method open = null;
        Method close = null;
        Method error = null;
        Method[] clazzPojoMethods = null;
        for ( Class<?> currentClazz = clazzPojo; !currentClazz.equals ( Object.class ); currentClazz = currentClazz.getSuperclass() ) {
            final Method[] currentClazzMethods = currentClazz.getDeclaredMethods();
            if ( currentClazz == clazzPojo ) {
                clazzPojoMethods = currentClazzMethods;
            }
            for ( final Method method : currentClazzMethods ) {
                if ( method.getAnnotation ( OnOpen.class ) != null ) {
                    this.checkPublic ( method );
                    if ( open == null ) {
                        open = method;
                    } else if ( currentClazz == clazzPojo || !this.isMethodOverride ( open, method ) ) {
                        throw new DeploymentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.duplicateAnnotation", OnOpen.class, currentClazz ) );
                    }
                } else if ( method.getAnnotation ( OnClose.class ) != null ) {
                    this.checkPublic ( method );
                    if ( close == null ) {
                        close = method;
                    } else if ( currentClazz == clazzPojo || !this.isMethodOverride ( close, method ) ) {
                        throw new DeploymentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.duplicateAnnotation", OnClose.class, currentClazz ) );
                    }
                } else if ( method.getAnnotation ( OnError.class ) != null ) {
                    this.checkPublic ( method );
                    if ( error == null ) {
                        error = method;
                    } else if ( currentClazz == clazzPojo || !this.isMethodOverride ( error, method ) ) {
                        throw new DeploymentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.duplicateAnnotation", OnError.class, currentClazz ) );
                    }
                } else if ( method.getAnnotation ( OnMessage.class ) != null ) {
                    this.checkPublic ( method );
                    final MessageHandlerInfo messageHandler = new MessageHandlerInfo ( method, decoders );
                    boolean found = false;
                    for ( final MessageHandlerInfo otherMessageHandler : this.onMessage ) {
                        if ( messageHandler.targetsSameWebSocketMessageType ( otherMessageHandler ) ) {
                            found = true;
                            if ( currentClazz == clazzPojo || !this.isMethodOverride ( messageHandler.m, otherMessageHandler.m ) ) {
                                throw new DeploymentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.duplicateAnnotation", OnMessage.class, currentClazz ) );
                            }
                            continue;
                        }
                    }
                    if ( !found ) {
                        this.onMessage.add ( messageHandler );
                    }
                }
            }
        }
        if ( open != null && open.getDeclaringClass() != clazzPojo && this.isOverridenWithoutAnnotation ( clazzPojoMethods, open, ( Class<? extends Annotation> ) OnOpen.class ) ) {
            open = null;
        }
        if ( close != null && close.getDeclaringClass() != clazzPojo && this.isOverridenWithoutAnnotation ( clazzPojoMethods, close, ( Class<? extends Annotation> ) OnClose.class ) ) {
            close = null;
        }
        if ( error != null && error.getDeclaringClass() != clazzPojo && this.isOverridenWithoutAnnotation ( clazzPojoMethods, error, ( Class<? extends Annotation> ) OnError.class ) ) {
            error = null;
        }
        final List<MessageHandlerInfo> overriddenOnMessage = new ArrayList<MessageHandlerInfo>();
        for ( final MessageHandlerInfo messageHandler2 : this.onMessage ) {
            if ( messageHandler2.m.getDeclaringClass() != clazzPojo && this.isOverridenWithoutAnnotation ( clazzPojoMethods, messageHandler2.m, ( Class<? extends Annotation> ) OnMessage.class ) ) {
                overriddenOnMessage.add ( messageHandler2 );
            }
        }
        for ( final MessageHandlerInfo messageHandler2 : overriddenOnMessage ) {
            this.onMessage.remove ( messageHandler2 );
        }
        this.onOpen = open;
        this.onClose = close;
        this.onError = error;
        this.onOpenParams = getPathParams ( this.onOpen, MethodType.ON_OPEN );
        this.onCloseParams = getPathParams ( this.onClose, MethodType.ON_CLOSE );
        this.onErrorParams = getPathParams ( this.onError, MethodType.ON_ERROR );
    }
    private void checkPublic ( final Method m ) throws DeploymentException {
        if ( !Modifier.isPublic ( m.getModifiers() ) ) {
            throw new DeploymentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.methodNotPublic", m.getName() ) );
        }
    }
    private boolean isMethodOverride ( final Method method1, final Method method2 ) {
        return method1.getName().equals ( method2.getName() ) && method1.getReturnType().equals ( method2.getReturnType() ) && Arrays.equals ( method1.getParameterTypes(), method2.getParameterTypes() );
    }
    private boolean isOverridenWithoutAnnotation ( final Method[] methods, final Method superclazzMethod, final Class<? extends Annotation> annotation ) {
        for ( final Method method : methods ) {
            if ( this.isMethodOverride ( method, superclazzMethod ) && method.getAnnotation ( annotation ) == null ) {
                return true;
            }
        }
        return false;
    }
    public String getWsPath() {
        return this.wsPath;
    }
    public Method getOnOpen() {
        return this.onOpen;
    }
    public Object[] getOnOpenArgs ( final Map<String, String> pathParameters, final Session session, final EndpointConfig config ) throws DecodeException {
        return buildArgs ( this.onOpenParams, pathParameters, session, config, null, null );
    }
    public Method getOnClose() {
        return this.onClose;
    }
    public Object[] getOnCloseArgs ( final Map<String, String> pathParameters, final Session session, final CloseReason closeReason ) throws DecodeException {
        return buildArgs ( this.onCloseParams, pathParameters, session, null, null, closeReason );
    }
    public Method getOnError() {
        return this.onError;
    }
    public Object[] getOnErrorArgs ( final Map<String, String> pathParameters, final Session session, final Throwable throwable ) throws DecodeException {
        return buildArgs ( this.onErrorParams, pathParameters, session, null, throwable, null );
    }
    public boolean hasMessageHandlers() {
        return !this.onMessage.isEmpty();
    }
    public Set<MessageHandler> getMessageHandlers ( final Object pojo, final Map<String, String> pathParameters, final Session session, final EndpointConfig config ) {
        final Set<MessageHandler> result = new HashSet<MessageHandler>();
        for ( final MessageHandlerInfo messageMethod : this.onMessage ) {
            result.addAll ( messageMethod.getMessageHandlers ( pojo, pathParameters, session, config ) );
        }
        return result;
    }
    private static PojoPathParam[] getPathParams ( final Method m, final MethodType methodType ) throws DeploymentException {
        if ( m == null ) {
            return new PojoPathParam[0];
        }
        boolean foundThrowable = false;
        final Class<?>[] types = m.getParameterTypes();
        final Annotation[][] paramsAnnotations = m.getParameterAnnotations();
        final PojoPathParam[] result = new PojoPathParam[types.length];
        for ( int i = 0; i < types.length; ++i ) {
            final Class<?> type = types[i];
            if ( type.equals ( Session.class ) ) {
                result[i] = new PojoPathParam ( type, null );
            } else if ( methodType == MethodType.ON_OPEN && type.equals ( EndpointConfig.class ) ) {
                result[i] = new PojoPathParam ( type, null );
            } else if ( methodType == MethodType.ON_ERROR && type.equals ( Throwable.class ) ) {
                foundThrowable = true;
                result[i] = new PojoPathParam ( type, null );
            } else if ( methodType == MethodType.ON_CLOSE && type.equals ( CloseReason.class ) ) {
                result[i] = new PojoPathParam ( type, null );
            } else {
                final Annotation[] array;
                final Annotation[] paramAnnotations = array = paramsAnnotations[i];
                for ( final Annotation paramAnnotation : array ) {
                    if ( paramAnnotation.annotationType().equals ( PathParam.class ) ) {
                        try {
                            Util.coerceToType ( type, "0" );
                        } catch ( IllegalArgumentException iae ) {
                            throw new DeploymentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.invalidPathParamType" ), ( Throwable ) iae );
                        }
                        result[i] = new PojoPathParam ( type, ( ( PathParam ) paramAnnotation ).value() );
                        break;
                    }
                }
                if ( result[i] == null ) {
                    throw new DeploymentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.paramWithoutAnnotation", type, m.getName(), m.getClass().getName() ) );
                }
            }
        }
        if ( methodType == MethodType.ON_ERROR && !foundThrowable ) {
            throw new DeploymentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.onErrorNoThrowable", m.getName(), m.getDeclaringClass().getName() ) );
        }
        return result;
    }
    private static Object[] buildArgs ( final PojoPathParam[] pathParams, final Map<String, String> pathParameters, final Session session, final EndpointConfig config, final Throwable throwable, final CloseReason closeReason ) throws DecodeException {
        final Object[] result = new Object[pathParams.length];
        for ( int i = 0; i < pathParams.length; ++i ) {
            final Class<?> type = pathParams[i].getType();
            if ( type.equals ( Session.class ) ) {
                result[i] = session;
            } else if ( type.equals ( EndpointConfig.class ) ) {
                result[i] = config;
            } else if ( type.equals ( Throwable.class ) ) {
                result[i] = throwable;
            } else if ( type.equals ( CloseReason.class ) ) {
                result[i] = closeReason;
            } else {
                final String name = pathParams[i].getName();
                final String value = pathParameters.get ( name );
                try {
                    result[i] = Util.coerceToType ( type, value );
                } catch ( Exception e ) {
                    throw new DecodeException ( value, PojoMethodMapping.sm.getString ( "pojoMethodMapping.decodePathParamFail", value, type ), ( Throwable ) e );
                }
            }
        }
        return result;
    }
    static {
        sm = StringManager.getManager ( PojoMethodMapping.class );
    }
    private static class MessageHandlerInfo {
        private final Method m;
        private int indexString;
        private int indexByteArray;
        private int indexByteBuffer;
        private int indexPong;
        private int indexBoolean;
        private int indexSession;
        private int indexInputStream;
        private int indexReader;
        private int indexPrimitive;
        private Class<?> primitiveType;
        private Map<Integer, PojoPathParam> indexPathParams;
        private int indexPayload;
        private Util.DecoderMatch decoderMatch;
        private long maxMessageSize;
        public MessageHandlerInfo ( final Method m, final List<DecoderEntry> decoderEntries ) {
            this.indexString = -1;
            this.indexByteArray = -1;
            this.indexByteBuffer = -1;
            this.indexPong = -1;
            this.indexBoolean = -1;
            this.indexSession = -1;
            this.indexInputStream = -1;
            this.indexReader = -1;
            this.indexPrimitive = -1;
            this.primitiveType = null;
            this.indexPathParams = new HashMap<Integer, PojoPathParam>();
            this.indexPayload = -1;
            this.decoderMatch = null;
            this.maxMessageSize = -1L;
            this.m = m;
            final Class<?>[] types = m.getParameterTypes();
            final Annotation[][] paramsAnnotations = m.getParameterAnnotations();
            for ( int i = 0; i < types.length; ++i ) {
                boolean paramFound = false;
                final Annotation[] array;
                final Annotation[] paramAnnotations = array = paramsAnnotations[i];
                for ( final Annotation paramAnnotation : array ) {
                    if ( paramAnnotation.annotationType().equals ( PathParam.class ) ) {
                        this.indexPathParams.put ( i, new PojoPathParam ( types[i], ( ( PathParam ) paramAnnotation ).value() ) );
                        paramFound = true;
                        break;
                    }
                }
                if ( !paramFound ) {
                    if ( String.class.isAssignableFrom ( types[i] ) ) {
                        if ( this.indexString != -1 ) {
                            throw new IllegalArgumentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
                        }
                        this.indexString = i;
                    } else if ( Reader.class.isAssignableFrom ( types[i] ) ) {
                        if ( this.indexReader != -1 ) {
                            throw new IllegalArgumentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
                        }
                        this.indexReader = i;
                    } else if ( Boolean.TYPE == types[i] ) {
                        if ( this.indexBoolean != -1 ) {
                            throw new IllegalArgumentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.duplicateLastParam", m.getName(), m.getDeclaringClass().getName() ) );
                        }
                        this.indexBoolean = i;
                    } else if ( ByteBuffer.class.isAssignableFrom ( types[i] ) ) {
                        if ( this.indexByteBuffer != -1 ) {
                            throw new IllegalArgumentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
                        }
                        this.indexByteBuffer = i;
                    } else if ( byte[].class == types[i] ) {
                        if ( this.indexByteArray != -1 ) {
                            throw new IllegalArgumentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
                        }
                        this.indexByteArray = i;
                    } else if ( InputStream.class.isAssignableFrom ( types[i] ) ) {
                        if ( this.indexInputStream != -1 ) {
                            throw new IllegalArgumentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
                        }
                        this.indexInputStream = i;
                    } else if ( Util.isPrimitive ( types[i] ) ) {
                        if ( this.indexPrimitive != -1 ) {
                            throw new IllegalArgumentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
                        }
                        this.indexPrimitive = i;
                        this.primitiveType = types[i];
                    } else if ( Session.class.isAssignableFrom ( types[i] ) ) {
                        if ( this.indexSession != -1 ) {
                            throw new IllegalArgumentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.duplicateSessionParam", m.getName(), m.getDeclaringClass().getName() ) );
                        }
                        this.indexSession = i;
                    } else if ( PongMessage.class.isAssignableFrom ( types[i] ) ) {
                        if ( this.indexPong != -1 ) {
                            throw new IllegalArgumentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.duplicatePongMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
                        }
                        this.indexPong = i;
                    } else {
                        if ( this.decoderMatch != null && this.decoderMatch.hasMatches() ) {
                            throw new IllegalArgumentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
                        }
                        this.decoderMatch = new Util.DecoderMatch ( types[i], decoderEntries );
                        if ( this.decoderMatch.hasMatches() ) {
                            this.indexPayload = i;
                        }
                    }
                }
            }
            if ( this.indexString != -1 ) {
                if ( this.indexPayload != -1 ) {
                    throw new IllegalArgumentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
                }
                this.indexPayload = this.indexString;
            }
            if ( this.indexReader != -1 ) {
                if ( this.indexPayload != -1 ) {
                    throw new IllegalArgumentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
                }
                this.indexPayload = this.indexReader;
            }
            if ( this.indexByteArray != -1 ) {
                if ( this.indexPayload != -1 ) {
                    throw new IllegalArgumentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
                }
                this.indexPayload = this.indexByteArray;
            }
            if ( this.indexByteBuffer != -1 ) {
                if ( this.indexPayload != -1 ) {
                    throw new IllegalArgumentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
                }
                this.indexPayload = this.indexByteBuffer;
            }
            if ( this.indexInputStream != -1 ) {
                if ( this.indexPayload != -1 ) {
                    throw new IllegalArgumentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
                }
                this.indexPayload = this.indexInputStream;
            }
            if ( this.indexPrimitive != -1 ) {
                if ( this.indexPayload != -1 ) {
                    throw new IllegalArgumentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
                }
                this.indexPayload = this.indexPrimitive;
            }
            if ( this.indexPong != -1 ) {
                if ( this.indexPayload != -1 ) {
                    throw new IllegalArgumentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.pongWithPayload", m.getName(), m.getDeclaringClass().getName() ) );
                }
                this.indexPayload = this.indexPong;
            }
            if ( this.indexPayload == -1 && this.indexPrimitive == -1 && this.indexBoolean != -1 ) {
                this.indexPayload = this.indexBoolean;
                this.indexPrimitive = this.indexBoolean;
                this.primitiveType = Boolean.TYPE;
                this.indexBoolean = -1;
            }
            if ( this.indexPayload == -1 ) {
                throw new IllegalArgumentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.noPayload", m.getName(), m.getDeclaringClass().getName() ) );
            }
            if ( this.indexPong != -1 && this.indexBoolean != -1 ) {
                throw new IllegalArgumentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.partialPong", m.getName(), m.getDeclaringClass().getName() ) );
            }
            if ( this.indexReader != -1 && this.indexBoolean != -1 ) {
                throw new IllegalArgumentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.partialReader", m.getName(), m.getDeclaringClass().getName() ) );
            }
            if ( this.indexInputStream != -1 && this.indexBoolean != -1 ) {
                throw new IllegalArgumentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.partialInputStream", m.getName(), m.getDeclaringClass().getName() ) );
            }
            if ( this.decoderMatch != null && this.decoderMatch.hasMatches() && this.indexBoolean != -1 ) {
                throw new IllegalArgumentException ( PojoMethodMapping.sm.getString ( "pojoMethodMapping.partialObject", m.getName(), m.getDeclaringClass().getName() ) );
            }
            this.maxMessageSize = m.getAnnotation ( OnMessage.class ).maxMessageSize();
        }
        public boolean targetsSameWebSocketMessageType ( final MessageHandlerInfo otherHandler ) {
            return otherHandler != null && ( ( this.indexByteArray >= 0 && otherHandler.indexByteArray >= 0 ) || ( this.indexByteBuffer >= 0 && otherHandler.indexByteBuffer >= 0 ) || ( this.indexInputStream >= 0 && otherHandler.indexInputStream >= 0 ) || ( this.indexPong >= 0 && otherHandler.indexPong >= 0 ) || ( this.indexPrimitive >= 0 && otherHandler.indexPrimitive >= 0 && this.primitiveType == otherHandler.primitiveType ) || ( this.indexReader >= 0 && otherHandler.indexReader >= 0 ) || ( this.indexString >= 0 && otherHandler.indexString >= 0 ) || ( this.decoderMatch != null && otherHandler.decoderMatch != null && this.decoderMatch.getTarget().equals ( otherHandler.decoderMatch.getTarget() ) ) );
        }
        public Set<MessageHandler> getMessageHandlers ( final Object pojo, final Map<String, String> pathParameters, final Session session, final EndpointConfig config ) {
            Object[] params = new Object[this.m.getParameterTypes().length];
            for ( final Map.Entry<Integer, PojoPathParam> entry : this.indexPathParams.entrySet() ) {
                final PojoPathParam pathParam = entry.getValue();
                final String valueString = pathParameters.get ( pathParam.getName() );
                Object value = null;
                try {
                    value = Util.coerceToType ( pathParam.getType(), valueString );
                } catch ( Exception e ) {
                    final DecodeException de = new DecodeException ( valueString, PojoMethodMapping.sm.getString ( "pojoMethodMapping.decodePathParamFail", valueString, pathParam.getType() ), ( Throwable ) e );
                    params = new Object[] { de };
                }
                params[entry.getKey()] = value;
            }
            final Set<MessageHandler> results = new HashSet<MessageHandler> ( 2 );
            if ( this.indexBoolean == -1 ) {
                if ( this.indexString != -1 || this.indexPrimitive != -1 ) {
                    final MessageHandler mh = ( MessageHandler ) new PojoMessageHandlerWholeText ( pojo, this.m, session, config, null, params, this.indexPayload, false, this.indexSession, this.maxMessageSize );
                    results.add ( mh );
                } else if ( this.indexReader != -1 ) {
                    final MessageHandler mh = ( MessageHandler ) new PojoMessageHandlerWholeText ( pojo, this.m, session, config, null, params, this.indexReader, true, this.indexSession, this.maxMessageSize );
                    results.add ( mh );
                } else if ( this.indexByteArray != -1 ) {
                    final MessageHandler mh = ( MessageHandler ) new PojoMessageHandlerWholeBinary ( pojo, this.m, session, config, null, params, this.indexByteArray, true, this.indexSession, false, this.maxMessageSize );
                    results.add ( mh );
                } else if ( this.indexByteBuffer != -1 ) {
                    final MessageHandler mh = ( MessageHandler ) new PojoMessageHandlerWholeBinary ( pojo, this.m, session, config, null, params, this.indexByteBuffer, false, this.indexSession, false, this.maxMessageSize );
                    results.add ( mh );
                } else if ( this.indexInputStream != -1 ) {
                    final MessageHandler mh = ( MessageHandler ) new PojoMessageHandlerWholeBinary ( pojo, this.m, session, config, null, params, this.indexInputStream, true, this.indexSession, true, this.maxMessageSize );
                    results.add ( mh );
                } else if ( this.decoderMatch != null && this.decoderMatch.hasMatches() ) {
                    if ( this.decoderMatch.getBinaryDecoders().size() > 0 ) {
                        final MessageHandler mh = ( MessageHandler ) new PojoMessageHandlerWholeBinary ( pojo, this.m, session, config, this.decoderMatch.getBinaryDecoders(), params, this.indexPayload, true, this.indexSession, true, this.maxMessageSize );
                        results.add ( mh );
                    }
                    if ( this.decoderMatch.getTextDecoders().size() > 0 ) {
                        final MessageHandler mh = ( MessageHandler ) new PojoMessageHandlerWholeText ( pojo, this.m, session, config, this.decoderMatch.getTextDecoders(), params, this.indexPayload, true, this.indexSession, this.maxMessageSize );
                        results.add ( mh );
                    }
                } else {
                    final MessageHandler mh = ( MessageHandler ) new PojoMessageHandlerWholePong ( pojo, this.m, session, params, this.indexPong, false, this.indexSession );
                    results.add ( mh );
                }
            } else if ( this.indexString != -1 ) {
                final MessageHandler mh = ( MessageHandler ) new PojoMessageHandlerPartialText ( pojo, this.m, session, params, this.indexString, false, this.indexBoolean, this.indexSession, this.maxMessageSize );
                results.add ( mh );
            } else if ( this.indexByteArray != -1 ) {
                final MessageHandler mh = ( MessageHandler ) new PojoMessageHandlerPartialBinary ( pojo, this.m, session, params, this.indexByteArray, true, this.indexBoolean, this.indexSession, this.maxMessageSize );
                results.add ( mh );
            } else {
                final MessageHandler mh = ( MessageHandler ) new PojoMessageHandlerPartialBinary ( pojo, this.m, session, params, this.indexByteBuffer, false, this.indexBoolean, this.indexSession, this.maxMessageSize );
                results.add ( mh );
            }
            return results;
        }
    }
    private enum MethodType {
        ON_OPEN,
        ON_CLOSE,
        ON_ERROR;
    }
}
