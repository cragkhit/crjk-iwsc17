package org.apache.tomcat.websocket;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.websocket.Extension;
import java.lang.reflect.Method;
import org.apache.tomcat.websocket.pojo.PojoMessageHandlerWholeText;
import java.io.Reader;
import java.io.InputStream;
import org.apache.tomcat.websocket.pojo.PojoMessageHandlerPartialBinary;
import org.apache.tomcat.websocket.pojo.PojoMessageHandlerWholeBinary;
import javax.websocket.PongMessage;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import javax.websocket.Session;
import javax.websocket.EndpointConfig;
import java.util.Iterator;
import javax.websocket.DeploymentException;
import java.util.ArrayList;
import java.util.List;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;
import javax.websocket.Encoder;
import javax.websocket.Decoder;
import javax.websocket.MessageHandler;
import java.security.NoSuchAlgorithmException;
import javax.websocket.CloseReason;
import java.security.SecureRandom;
import java.util.Queue;
import org.apache.tomcat.util.res.StringManager;
public class Util {
    private static final StringManager sm;
    private static final Queue<SecureRandom> randoms;
    static boolean isControl ( final byte opCode ) {
        return ( opCode & 0x8 ) > 0;
    }
    static boolean isText ( final byte opCode ) {
        return opCode == 1;
    }
    static boolean isContinuation ( final byte opCode ) {
        return opCode == 0;
    }
    static CloseReason.CloseCode getCloseCode ( final int code ) {
        if ( code > 2999 && code < 5000 ) {
            return CloseReason.CloseCodes.getCloseCode ( code );
        }
        switch ( code ) {
        case 1000: {
            return ( CloseReason.CloseCode ) CloseReason.CloseCodes.NORMAL_CLOSURE;
        }
        case 1001: {
            return ( CloseReason.CloseCode ) CloseReason.CloseCodes.GOING_AWAY;
        }
        case 1002: {
            return ( CloseReason.CloseCode ) CloseReason.CloseCodes.PROTOCOL_ERROR;
        }
        case 1003: {
            return ( CloseReason.CloseCode ) CloseReason.CloseCodes.CANNOT_ACCEPT;
        }
        case 1004: {
            return ( CloseReason.CloseCode ) CloseReason.CloseCodes.PROTOCOL_ERROR;
        }
        case 1005: {
            return ( CloseReason.CloseCode ) CloseReason.CloseCodes.PROTOCOL_ERROR;
        }
        case 1006: {
            return ( CloseReason.CloseCode ) CloseReason.CloseCodes.PROTOCOL_ERROR;
        }
        case 1007: {
            return ( CloseReason.CloseCode ) CloseReason.CloseCodes.NOT_CONSISTENT;
        }
        case 1008: {
            return ( CloseReason.CloseCode ) CloseReason.CloseCodes.VIOLATED_POLICY;
        }
        case 1009: {
            return ( CloseReason.CloseCode ) CloseReason.CloseCodes.TOO_BIG;
        }
        case 1010: {
            return ( CloseReason.CloseCode ) CloseReason.CloseCodes.NO_EXTENSION;
        }
        case 1011: {
            return ( CloseReason.CloseCode ) CloseReason.CloseCodes.UNEXPECTED_CONDITION;
        }
        case 1012: {
            return ( CloseReason.CloseCode ) CloseReason.CloseCodes.PROTOCOL_ERROR;
        }
        case 1013: {
            return ( CloseReason.CloseCode ) CloseReason.CloseCodes.PROTOCOL_ERROR;
        }
        case 1015: {
            return ( CloseReason.CloseCode ) CloseReason.CloseCodes.PROTOCOL_ERROR;
        }
        default: {
            return ( CloseReason.CloseCode ) CloseReason.CloseCodes.PROTOCOL_ERROR;
        }
        }
    }
    static byte[] generateMask() {
        SecureRandom sr = Util.randoms.poll();
        if ( sr == null ) {
            try {
                sr = SecureRandom.getInstance ( "SHA1PRNG" );
            } catch ( NoSuchAlgorithmException e ) {
                sr = new SecureRandom();
            }
        }
        final byte[] result = new byte[4];
        sr.nextBytes ( result );
        Util.randoms.add ( sr );
        return result;
    }
    static Class<?> getMessageType ( final MessageHandler listener ) {
        return getGenericType ( MessageHandler.class, listener.getClass() ).getClazz();
    }
    private static Class<?> getDecoderType ( final Class<? extends Decoder> decoder ) {
        return getGenericType ( Decoder.class, decoder ).getClazz();
    }
    static Class<?> getEncoderType ( final Class<? extends Encoder> encoder ) {
        return getGenericType ( Encoder.class, encoder ).getClazz();
    }
    private static <T> TypeResult getGenericType ( final Class<T> type, final Class<? extends T> clazz ) {
        final Type[] genericInterfaces;
        final Type[] interfaces = genericInterfaces = clazz.getGenericInterfaces();
        for ( final Type iface : genericInterfaces ) {
            if ( iface instanceof ParameterizedType ) {
                final ParameterizedType pi = ( ParameterizedType ) iface;
                if ( pi.getRawType() instanceof Class && type.isAssignableFrom ( ( Class<?> ) pi.getRawType() ) ) {
                    return getTypeParameter ( clazz, pi.getActualTypeArguments() [0] );
                }
            }
        }
        final Class<? extends T> superClazz = ( Class<? extends T> ) clazz.getSuperclass();
        if ( superClazz == null ) {
            return null;
        }
        TypeResult superClassTypeResult = getGenericType ( ( Class<Object> ) type, superClazz );
        final int dimension = superClassTypeResult.getDimension();
        if ( superClassTypeResult.getIndex() == -1 && dimension == 0 ) {
            return superClassTypeResult;
        }
        if ( superClassTypeResult.getIndex() > -1 ) {
            final ParameterizedType superClassType = ( ParameterizedType ) clazz.getGenericSuperclass();
            final TypeResult result = getTypeParameter ( clazz, superClassType.getActualTypeArguments() [superClassTypeResult.getIndex()] );
            result.incrementDimension ( superClassTypeResult.getDimension() );
            if ( result.getClazz() == null || result.getDimension() <= 0 ) {
                return result;
            }
            superClassTypeResult = result;
        }
        if ( superClassTypeResult.getDimension() > 0 ) {
            final StringBuilder className = new StringBuilder();
            for ( int i = 0; i < dimension; ++i ) {
                className.append ( '[' );
            }
            className.append ( 'L' );
            className.append ( superClassTypeResult.getClazz().getCanonicalName() );
            className.append ( ';' );
            Class<?> arrayClazz;
            try {
                arrayClazz = Class.forName ( className.toString() );
            } catch ( ClassNotFoundException e ) {
                throw new IllegalArgumentException ( e );
            }
            return new TypeResult ( arrayClazz, -1, 0 );
        }
        return null;
    }
    private static TypeResult getTypeParameter ( final Class<?> clazz, final Type argType ) {
        if ( argType instanceof Class ) {
            return new TypeResult ( ( Class<?> ) argType, -1, 0 );
        }
        if ( argType instanceof ParameterizedType ) {
            return new TypeResult ( ( Class<?> ) ( ( ParameterizedType ) argType ).getRawType(), -1, 0 );
        }
        if ( argType instanceof GenericArrayType ) {
            final Type arrayElementType = ( ( GenericArrayType ) argType ).getGenericComponentType();
            final TypeResult result = getTypeParameter ( clazz, arrayElementType );
            result.incrementDimension ( 1 );
            return result;
        }
        final TypeVariable<?>[] tvs = clazz.getTypeParameters();
        for ( int i = 0; i < tvs.length; ++i ) {
            if ( tvs[i].equals ( argType ) ) {
                return new TypeResult ( null, i, 0 );
            }
        }
        return null;
    }
    public static boolean isPrimitive ( final Class<?> clazz ) {
        return clazz.isPrimitive() || ( clazz.equals ( Boolean.class ) || clazz.equals ( Byte.class ) || clazz.equals ( Character.class ) || clazz.equals ( Double.class ) || clazz.equals ( Float.class ) || clazz.equals ( Integer.class ) || clazz.equals ( Long.class ) || clazz.equals ( Short.class ) );
    }
    public static Object coerceToType ( final Class<?> type, final String value ) {
        if ( type.equals ( String.class ) ) {
            return value;
        }
        if ( type.equals ( Boolean.TYPE ) || type.equals ( Boolean.class ) ) {
            return Boolean.valueOf ( value );
        }
        if ( type.equals ( Byte.TYPE ) || type.equals ( Byte.class ) ) {
            return Byte.valueOf ( value );
        }
        if ( type.equals ( Character.TYPE ) || type.equals ( Character.class ) ) {
            return value.charAt ( 0 );
        }
        if ( type.equals ( Double.TYPE ) || type.equals ( Double.class ) ) {
            return Double.valueOf ( value );
        }
        if ( type.equals ( Float.TYPE ) || type.equals ( Float.class ) ) {
            return Float.valueOf ( value );
        }
        if ( type.equals ( Integer.TYPE ) || type.equals ( Integer.class ) ) {
            return Integer.valueOf ( value );
        }
        if ( type.equals ( Long.TYPE ) || type.equals ( Long.class ) ) {
            return Long.valueOf ( value );
        }
        if ( type.equals ( Short.TYPE ) || type.equals ( Short.class ) ) {
            return Short.valueOf ( value );
        }
        throw new IllegalArgumentException ( Util.sm.getString ( "util.invalidType", value, type.getName() ) );
    }
    public static List<DecoderEntry> getDecoders ( final List<Class<? extends Decoder>> decoderClazzes ) throws DeploymentException {
        final List<DecoderEntry> result = new ArrayList<DecoderEntry>();
        if ( decoderClazzes != null ) {
            for ( final Class<? extends Decoder> decoderClazz : decoderClazzes ) {
                try {
                    final Decoder instance = ( Decoder ) decoderClazz.newInstance();
                } catch ( InstantiationException | IllegalAccessException e ) {
                    throw new DeploymentException ( Util.sm.getString ( "pojoMethodMapping.invalidDecoder", decoderClazz.getName() ), ( Throwable ) e );
                }
                final DecoderEntry entry = new DecoderEntry ( getDecoderType ( decoderClazz ), decoderClazz );
                result.add ( entry );
            }
        }
        return result;
    }
    static Set<MessageHandlerResult> getMessageHandlers ( final Class<?> target, final MessageHandler listener, final EndpointConfig endpointConfig, final Session session ) {
        final Set<MessageHandlerResult> results = new HashSet<MessageHandlerResult> ( 2 );
        if ( String.class.isAssignableFrom ( target ) ) {
            final MessageHandlerResult result = new MessageHandlerResult ( listener, MessageHandlerResultType.TEXT );
            results.add ( result );
        } else if ( ByteBuffer.class.isAssignableFrom ( target ) ) {
            final MessageHandlerResult result = new MessageHandlerResult ( listener, MessageHandlerResultType.BINARY );
            results.add ( result );
        } else if ( PongMessage.class.isAssignableFrom ( target ) ) {
            final MessageHandlerResult result = new MessageHandlerResult ( listener, MessageHandlerResultType.PONG );
            results.add ( result );
        } else if ( byte[].class.isAssignableFrom ( target ) ) {
            final boolean whole = MessageHandler.Whole.class.isAssignableFrom ( listener.getClass() );
            final MessageHandlerResult result2 = new MessageHandlerResult ( ( MessageHandler ) ( whole ? new PojoMessageHandlerWholeBinary ( listener, getOnMessageMethod ( listener ), session, endpointConfig, matchDecoders ( target, endpointConfig, true ), new Object[1], 0, true, -1, false, -1L ) : new PojoMessageHandlerPartialBinary ( listener, getOnMessagePartialMethod ( listener ), session, new Object[2], 0, true, 1, -1, -1L ) ), MessageHandlerResultType.BINARY );
            results.add ( result2 );
        } else if ( InputStream.class.isAssignableFrom ( target ) ) {
            final MessageHandlerResult result = new MessageHandlerResult ( ( MessageHandler ) new PojoMessageHandlerWholeBinary ( listener, getOnMessageMethod ( listener ), session, endpointConfig, matchDecoders ( target, endpointConfig, true ), new Object[1], 0, true, -1, true, -1L ), MessageHandlerResultType.BINARY );
            results.add ( result );
        } else if ( Reader.class.isAssignableFrom ( target ) ) {
            final MessageHandlerResult result = new MessageHandlerResult ( ( MessageHandler ) new PojoMessageHandlerWholeText ( listener, getOnMessageMethod ( listener ), session, endpointConfig, matchDecoders ( target, endpointConfig, false ), new Object[1], 0, true, -1, -1L ), MessageHandlerResultType.TEXT );
            results.add ( result );
        } else {
            final DecoderMatch decoderMatch = matchDecoders ( target, endpointConfig );
            final Method m = getOnMessageMethod ( listener );
            if ( decoderMatch.getBinaryDecoders().size() > 0 ) {
                final MessageHandlerResult result3 = new MessageHandlerResult ( ( MessageHandler ) new PojoMessageHandlerWholeBinary ( listener, m, session, endpointConfig, decoderMatch.getBinaryDecoders(), new Object[1], 0, false, -1, false, -1L ), MessageHandlerResultType.BINARY );
                results.add ( result3 );
            }
            if ( decoderMatch.getTextDecoders().size() > 0 ) {
                final MessageHandlerResult result3 = new MessageHandlerResult ( ( MessageHandler ) new PojoMessageHandlerWholeText ( listener, m, session, endpointConfig, decoderMatch.getTextDecoders(), new Object[1], 0, false, -1, -1L ), MessageHandlerResultType.TEXT );
                results.add ( result3 );
            }
        }
        if ( results.size() == 0 ) {
            throw new IllegalArgumentException ( Util.sm.getString ( "wsSession.unknownHandler", listener, target ) );
        }
        return results;
    }
    private static List<Class<? extends Decoder>> matchDecoders ( final Class<?> target, final EndpointConfig endpointConfig, final boolean binary ) {
        final DecoderMatch decoderMatch = matchDecoders ( target, endpointConfig );
        if ( binary ) {
            if ( decoderMatch.getBinaryDecoders().size() > 0 ) {
                return decoderMatch.getBinaryDecoders();
            }
        } else if ( decoderMatch.getTextDecoders().size() > 0 ) {
            return decoderMatch.getTextDecoders();
        }
        return null;
    }
    private static DecoderMatch matchDecoders ( final Class<?> target, final EndpointConfig endpointConfig ) {
        DecoderMatch decoderMatch;
        try {
            final List<Class<? extends Decoder>> decoders = ( List<Class<? extends Decoder>> ) endpointConfig.getDecoders();
            final List<DecoderEntry> decoderEntries = getDecoders ( decoders );
            decoderMatch = new DecoderMatch ( target, decoderEntries );
        } catch ( DeploymentException e ) {
            throw new IllegalArgumentException ( ( Throwable ) e );
        }
        return decoderMatch;
    }
    public static void parseExtensionHeader ( final List<Extension> extensions, final String header ) {
        final String[] split;
        final String[] unparsedExtensions = split = header.split ( "," );
        for ( final String unparsedExtension : split ) {
            final String[] unparsedParameters = unparsedExtension.split ( ";" );
            final WsExtension extension = new WsExtension ( unparsedParameters[0].trim() );
            for ( int i = 1; i < unparsedParameters.length; ++i ) {
                final int equalsPos = unparsedParameters[i].indexOf ( 61 );
                String name;
                String value;
                if ( equalsPos == -1 ) {
                    name = unparsedParameters[i].trim();
                    value = null;
                } else {
                    name = unparsedParameters[i].substring ( 0, equalsPos ).trim();
                    value = unparsedParameters[i].substring ( equalsPos + 1 ).trim();
                    final int len = value.length();
                    if ( len > 1 && value.charAt ( 0 ) == '\"' && value.charAt ( len - 1 ) == '\"' ) {
                        value = value.substring ( 1, value.length() - 1 );
                    }
                }
                if ( containsDelims ( name ) || containsDelims ( value ) ) {
                    throw new IllegalArgumentException ( Util.sm.getString ( "util.notToken", name, value ) );
                }
                if ( value != null && ( value.indexOf ( 44 ) > -1 || value.indexOf ( 59 ) > -1 || value.indexOf ( 34 ) > -1 || value.indexOf ( 61 ) > -1 ) ) {
                    throw new IllegalArgumentException ( Util.sm.getString ( "", value ) );
                }
                extension.addParameter ( ( Extension.Parameter ) new WsExtensionParameter ( name, value ) );
            }
            extensions.add ( ( Extension ) extension );
        }
    }
    private static boolean containsDelims ( final String input ) {
        if ( input == null || input.length() == 0 ) {
            return false;
        }
        final char[] charArray = input.toCharArray();
        final int length = charArray.length;
        int i = 0;
        while ( i < length ) {
            final char c = charArray[i];
            switch ( c ) {
            case '\"':
            case ',':
            case ';':
            case '=': {
                return true;
            }
            default: {
                ++i;
                continue;
            }
            }
        }
        return false;
    }
    private static Method getOnMessageMethod ( final MessageHandler listener ) {
        try {
            return listener.getClass().getMethod ( "onMessage", Object.class );
        } catch ( NoSuchMethodException | SecurityException e ) {
            throw new IllegalArgumentException ( Util.sm.getString ( "util.invalidMessageHandler" ), e );
        }
    }
    private static Method getOnMessagePartialMethod ( final MessageHandler listener ) {
        try {
            return listener.getClass().getMethod ( "onMessage", Object.class, Boolean.TYPE );
        } catch ( NoSuchMethodException | SecurityException e ) {
            throw new IllegalArgumentException ( Util.sm.getString ( "util.invalidMessageHandler" ), e );
        }
    }
    static {
        sm = StringManager.getManager ( Util.class );
        randoms = new ConcurrentLinkedQueue<SecureRandom>();
    }
    public static class DecoderMatch {
        private final List<Class<? extends Decoder>> textDecoders;
        private final List<Class<? extends Decoder>> binaryDecoders;
        private final Class<?> target;
        public DecoderMatch ( final Class<?> target, final List<DecoderEntry> decoderEntries ) {
            this.textDecoders = new ArrayList<Class<? extends Decoder>>();
            this.binaryDecoders = new ArrayList<Class<? extends Decoder>>();
            this.target = target;
            for ( final DecoderEntry decoderEntry : decoderEntries ) {
                if ( decoderEntry.getClazz().isAssignableFrom ( target ) ) {
                    if ( Decoder.Binary.class.isAssignableFrom ( decoderEntry.getDecoderClazz() ) ) {
                        this.binaryDecoders.add ( decoderEntry.getDecoderClazz() );
                    } else {
                        if ( Decoder.BinaryStream.class.isAssignableFrom ( decoderEntry.getDecoderClazz() ) ) {
                            this.binaryDecoders.add ( decoderEntry.getDecoderClazz() );
                            break;
                        }
                        if ( Decoder.Text.class.isAssignableFrom ( decoderEntry.getDecoderClazz() ) ) {
                            this.textDecoders.add ( decoderEntry.getDecoderClazz() );
                        } else {
                            if ( Decoder.TextStream.class.isAssignableFrom ( decoderEntry.getDecoderClazz() ) ) {
                                this.textDecoders.add ( decoderEntry.getDecoderClazz() );
                                break;
                            }
                            throw new IllegalArgumentException ( Util.sm.getString ( "util.unknownDecoderType" ) );
                        }
                    }
                }
            }
        }
        public List<Class<? extends Decoder>> getTextDecoders() {
            return this.textDecoders;
        }
        public List<Class<? extends Decoder>> getBinaryDecoders() {
            return this.binaryDecoders;
        }
        public Class<?> getTarget() {
            return this.target;
        }
        public boolean hasMatches() {
            return this.textDecoders.size() > 0 || this.binaryDecoders.size() > 0;
        }
    }
    private static class TypeResult {
        private final Class<?> clazz;
        private final int index;
        private int dimension;
        public TypeResult ( final Class<?> clazz, final int index, final int dimension ) {
            this.clazz = clazz;
            this.index = index;
            this.dimension = dimension;
        }
        public Class<?> getClazz() {
            return this.clazz;
        }
        public int getIndex() {
            return this.index;
        }
        public int getDimension() {
            return this.dimension;
        }
        public void incrementDimension ( final int inc ) {
            this.dimension += inc;
        }
    }
}
