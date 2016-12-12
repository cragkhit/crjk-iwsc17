package org.apache.tomcat.websocket.pojo;
import java.util.Iterator;
import javax.websocket.Decoder;
import java.util.HashSet;
import javax.websocket.DecodeException;
import javax.websocket.MessageHandler;
import java.util.Set;
import javax.websocket.EndpointConfig;
import java.lang.annotation.Annotation;
import javax.websocket.OnMessage;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.io.Reader;
import javax.websocket.server.PathParam;
import java.util.HashMap;
import org.apache.tomcat.websocket.DecoderEntry;
import java.util.List;
import org.apache.tomcat.websocket.Util;
import java.util.Map;
import java.lang.reflect.Method;
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
                        throw new IllegalArgumentException ( PojoMethodMapping.access$100().getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
                    }
                    this.indexString = i;
                } else if ( Reader.class.isAssignableFrom ( types[i] ) ) {
                    if ( this.indexReader != -1 ) {
                        throw new IllegalArgumentException ( PojoMethodMapping.access$100().getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
                    }
                    this.indexReader = i;
                } else if ( Boolean.TYPE == types[i] ) {
                    if ( this.indexBoolean != -1 ) {
                        throw new IllegalArgumentException ( PojoMethodMapping.access$100().getString ( "pojoMethodMapping.duplicateLastParam", m.getName(), m.getDeclaringClass().getName() ) );
                    }
                    this.indexBoolean = i;
                } else if ( ByteBuffer.class.isAssignableFrom ( types[i] ) ) {
                    if ( this.indexByteBuffer != -1 ) {
                        throw new IllegalArgumentException ( PojoMethodMapping.access$100().getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
                    }
                    this.indexByteBuffer = i;
                } else if ( byte[].class == types[i] ) {
                    if ( this.indexByteArray != -1 ) {
                        throw new IllegalArgumentException ( PojoMethodMapping.access$100().getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
                    }
                    this.indexByteArray = i;
                } else if ( InputStream.class.isAssignableFrom ( types[i] ) ) {
                    if ( this.indexInputStream != -1 ) {
                        throw new IllegalArgumentException ( PojoMethodMapping.access$100().getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
                    }
                    this.indexInputStream = i;
                } else if ( Util.isPrimitive ( types[i] ) ) {
                    if ( this.indexPrimitive != -1 ) {
                        throw new IllegalArgumentException ( PojoMethodMapping.access$100().getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
                    }
                    this.indexPrimitive = i;
                    this.primitiveType = types[i];
                } else if ( Session.class.isAssignableFrom ( types[i] ) ) {
                    if ( this.indexSession != -1 ) {
                        throw new IllegalArgumentException ( PojoMethodMapping.access$100().getString ( "pojoMethodMapping.duplicateSessionParam", m.getName(), m.getDeclaringClass().getName() ) );
                    }
                    this.indexSession = i;
                } else if ( PongMessage.class.isAssignableFrom ( types[i] ) ) {
                    if ( this.indexPong != -1 ) {
                        throw new IllegalArgumentException ( PojoMethodMapping.access$100().getString ( "pojoMethodMapping.duplicatePongMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
                    }
                    this.indexPong = i;
                } else {
                    if ( this.decoderMatch != null && this.decoderMatch.hasMatches() ) {
                        throw new IllegalArgumentException ( PojoMethodMapping.access$100().getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
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
                throw new IllegalArgumentException ( PojoMethodMapping.access$100().getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
            }
            this.indexPayload = this.indexString;
        }
        if ( this.indexReader != -1 ) {
            if ( this.indexPayload != -1 ) {
                throw new IllegalArgumentException ( PojoMethodMapping.access$100().getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
            }
            this.indexPayload = this.indexReader;
        }
        if ( this.indexByteArray != -1 ) {
            if ( this.indexPayload != -1 ) {
                throw new IllegalArgumentException ( PojoMethodMapping.access$100().getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
            }
            this.indexPayload = this.indexByteArray;
        }
        if ( this.indexByteBuffer != -1 ) {
            if ( this.indexPayload != -1 ) {
                throw new IllegalArgumentException ( PojoMethodMapping.access$100().getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
            }
            this.indexPayload = this.indexByteBuffer;
        }
        if ( this.indexInputStream != -1 ) {
            if ( this.indexPayload != -1 ) {
                throw new IllegalArgumentException ( PojoMethodMapping.access$100().getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
            }
            this.indexPayload = this.indexInputStream;
        }
        if ( this.indexPrimitive != -1 ) {
            if ( this.indexPayload != -1 ) {
                throw new IllegalArgumentException ( PojoMethodMapping.access$100().getString ( "pojoMethodMapping.duplicateMessageParam", m.getName(), m.getDeclaringClass().getName() ) );
            }
            this.indexPayload = this.indexPrimitive;
        }
        if ( this.indexPong != -1 ) {
            if ( this.indexPayload != -1 ) {
                throw new IllegalArgumentException ( PojoMethodMapping.access$100().getString ( "pojoMethodMapping.pongWithPayload", m.getName(), m.getDeclaringClass().getName() ) );
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
            throw new IllegalArgumentException ( PojoMethodMapping.access$100().getString ( "pojoMethodMapping.noPayload", m.getName(), m.getDeclaringClass().getName() ) );
        }
        if ( this.indexPong != -1 && this.indexBoolean != -1 ) {
            throw new IllegalArgumentException ( PojoMethodMapping.access$100().getString ( "pojoMethodMapping.partialPong", m.getName(), m.getDeclaringClass().getName() ) );
        }
        if ( this.indexReader != -1 && this.indexBoolean != -1 ) {
            throw new IllegalArgumentException ( PojoMethodMapping.access$100().getString ( "pojoMethodMapping.partialReader", m.getName(), m.getDeclaringClass().getName() ) );
        }
        if ( this.indexInputStream != -1 && this.indexBoolean != -1 ) {
            throw new IllegalArgumentException ( PojoMethodMapping.access$100().getString ( "pojoMethodMapping.partialInputStream", m.getName(), m.getDeclaringClass().getName() ) );
        }
        if ( this.decoderMatch != null && this.decoderMatch.hasMatches() && this.indexBoolean != -1 ) {
            throw new IllegalArgumentException ( PojoMethodMapping.access$100().getString ( "pojoMethodMapping.partialObject", m.getName(), m.getDeclaringClass().getName() ) );
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
                final DecodeException de = new DecodeException ( valueString, PojoMethodMapping.access$100().getString ( "pojoMethodMapping.decodePathParamFail", valueString, pathParam.getType() ), ( Throwable ) e );
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
