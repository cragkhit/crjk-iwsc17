package org.apache.el.lang;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.lang.reflect.Array;
import java.beans.PropertyEditor;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.beans.PropertyEditorManager;
import javax.el.ELException;
import org.apache.el.util.MessageFactory;
import java.math.BigInteger;
import java.math.BigDecimal;
import javax.el.ELContext;
public class ELSupport {
    private static final Long ZERO;
    private static final boolean IS_SECURITY_ENABLED;
    protected static final boolean COERCE_TO_ZERO;
    public static final int compare ( final ELContext ctx, final Object obj0, final Object obj1 ) throws ELException {
        if ( obj0 == obj1 || equals ( ctx, obj0, obj1 ) ) {
            return 0;
        }
        if ( isBigDecimalOp ( obj0, obj1 ) ) {
            final BigDecimal bd0 = ( BigDecimal ) coerceToNumber ( ctx, obj0, BigDecimal.class );
            final BigDecimal bd = ( BigDecimal ) coerceToNumber ( ctx, obj1, BigDecimal.class );
            return bd0.compareTo ( bd );
        }
        if ( isDoubleOp ( obj0, obj1 ) ) {
            final Double d0 = ( Double ) coerceToNumber ( ctx, obj0, Double.class );
            final Double d = ( Double ) coerceToNumber ( ctx, obj1, Double.class );
            return d0.compareTo ( d );
        }
        if ( isBigIntegerOp ( obj0, obj1 ) ) {
            final BigInteger bi0 = ( BigInteger ) coerceToNumber ( ctx, obj0, BigInteger.class );
            final BigInteger bi = ( BigInteger ) coerceToNumber ( ctx, obj1, BigInteger.class );
            return bi0.compareTo ( bi );
        }
        if ( isLongOp ( obj0, obj1 ) ) {
            final Long l0 = ( Long ) coerceToNumber ( ctx, obj0, Long.class );
            final Long l = ( Long ) coerceToNumber ( ctx, obj1, Long.class );
            return l0.compareTo ( l );
        }
        if ( obj0 instanceof String || obj1 instanceof String ) {
            return coerceToString ( ctx, obj0 ).compareTo ( coerceToString ( ctx, obj1 ) );
        }
        if ( obj0 instanceof Comparable ) {
            final Comparable<Object> comparable = ( Comparable<Object> ) obj0;
            return ( obj1 != null ) ? comparable.compareTo ( obj1 ) : 1;
        }
        if ( obj1 instanceof Comparable ) {
            final Comparable<Object> comparable = ( Comparable<Object> ) obj1;
            return ( obj0 != null ) ? ( -comparable.compareTo ( obj0 ) ) : -1;
        }
        throw new ELException ( MessageFactory.get ( "error.compare", obj0, obj1 ) );
    }
    public static final boolean equals ( final ELContext ctx, final Object obj0, final Object obj1 ) throws ELException {
        if ( obj0 == obj1 ) {
            return true;
        }
        if ( obj0 == null || obj1 == null ) {
            return false;
        }
        if ( isBigDecimalOp ( obj0, obj1 ) ) {
            final BigDecimal bd0 = ( BigDecimal ) coerceToNumber ( ctx, obj0, BigDecimal.class );
            final BigDecimal bd = ( BigDecimal ) coerceToNumber ( ctx, obj1, BigDecimal.class );
            return bd0.equals ( bd );
        }
        if ( isDoubleOp ( obj0, obj1 ) ) {
            final Double d0 = ( Double ) coerceToNumber ( ctx, obj0, Double.class );
            final Double d = ( Double ) coerceToNumber ( ctx, obj1, Double.class );
            return d0.equals ( d );
        }
        if ( isBigIntegerOp ( obj0, obj1 ) ) {
            final BigInteger bi0 = ( BigInteger ) coerceToNumber ( ctx, obj0, BigInteger.class );
            final BigInteger bi = ( BigInteger ) coerceToNumber ( ctx, obj1, BigInteger.class );
            return bi0.equals ( bi );
        }
        if ( isLongOp ( obj0, obj1 ) ) {
            final Long l0 = ( Long ) coerceToNumber ( ctx, obj0, Long.class );
            final Long l = ( Long ) coerceToNumber ( ctx, obj1, Long.class );
            return l0.equals ( l );
        }
        if ( obj0 instanceof Boolean || obj1 instanceof Boolean ) {
            return coerceToBoolean ( ctx, obj0, false ).equals ( coerceToBoolean ( ctx, obj1, false ) );
        }
        if ( obj0.getClass().isEnum() ) {
            return obj0.equals ( coerceToEnum ( ctx, obj1, obj0.getClass() ) );
        }
        if ( obj1.getClass().isEnum() ) {
            return obj1.equals ( coerceToEnum ( ctx, obj0, obj1.getClass() ) );
        }
        if ( obj0 instanceof String || obj1 instanceof String ) {
            final int lexCompare = coerceToString ( ctx, obj0 ).compareTo ( coerceToString ( ctx, obj1 ) );
            return lexCompare == 0;
        }
        return obj0.equals ( obj1 );
    }
    public static final Enum<?> coerceToEnum ( final ELContext ctx, final Object obj, final Class type ) {
        if ( ctx != null ) {
            final boolean originalIsPropertyResolved = ctx.isPropertyResolved();
            try {
                final Object result = ctx.getELResolver().convertToType ( ctx, obj, type );
                if ( ctx.isPropertyResolved() ) {
                    return ( Enum<?> ) result;
                }
            } finally {
                ctx.setPropertyResolved ( originalIsPropertyResolved );
            }
        }
        if ( obj == null || "".equals ( obj ) ) {
            return null;
        }
        if ( type.isAssignableFrom ( obj.getClass() ) ) {
            return ( Enum<?> ) obj;
        }
        if ( ! ( obj instanceof String ) ) {
            throw new ELException ( MessageFactory.get ( "error.convert", obj, obj.getClass(), type ) );
        }
        Enum<?> result2;
        try {
            result2 = Enum.valueOf ( ( Class<Enum<?>> ) type, ( String ) obj );
        } catch ( IllegalArgumentException iae ) {
            throw new ELException ( MessageFactory.get ( "error.convert", obj, obj.getClass(), type ) );
        }
        return result2;
    }
    public static final Boolean coerceToBoolean ( final ELContext ctx, final Object obj, final boolean primitive ) throws ELException {
        if ( ctx != null ) {
            final boolean originalIsPropertyResolved = ctx.isPropertyResolved();
            try {
                final Object result = ctx.getELResolver().convertToType ( ctx, obj, ( Class ) Boolean.class );
                if ( ctx.isPropertyResolved() ) {
                    return ( Boolean ) result;
                }
            } finally {
                ctx.setPropertyResolved ( originalIsPropertyResolved );
            }
        }
        if ( !ELSupport.COERCE_TO_ZERO && !primitive && obj == null ) {
            return null;
        }
        if ( obj == null || "".equals ( obj ) ) {
            return Boolean.FALSE;
        }
        if ( obj instanceof Boolean ) {
            return ( Boolean ) obj;
        }
        if ( obj instanceof String ) {
            return Boolean.valueOf ( ( String ) obj );
        }
        throw new ELException ( MessageFactory.get ( "error.convert", obj, obj.getClass(), Boolean.class ) );
    }
    private static final Character coerceToCharacter ( final ELContext ctx, final Object obj ) throws ELException {
        if ( ctx != null ) {
            final boolean originalIsPropertyResolved = ctx.isPropertyResolved();
            try {
                final Object result = ctx.getELResolver().convertToType ( ctx, obj, ( Class ) Character.class );
                if ( ctx.isPropertyResolved() ) {
                    return ( Character ) result;
                }
            } finally {
                ctx.setPropertyResolved ( originalIsPropertyResolved );
            }
        }
        if ( obj == null || "".equals ( obj ) ) {
            return '\0';
        }
        if ( obj instanceof String ) {
            return ( ( String ) obj ).charAt ( 0 );
        }
        if ( ELArithmetic.isNumber ( obj ) ) {
            return ( char ) ( ( Number ) obj ).shortValue();
        }
        final Class<?> objType = obj.getClass();
        if ( obj instanceof Character ) {
            return ( Character ) obj;
        }
        throw new ELException ( MessageFactory.get ( "error.convert", obj, objType, Character.class ) );
    }
    protected static final Number coerceToNumber ( final Number number, final Class<?> type ) throws ELException {
        if ( Long.TYPE == type || Long.class.equals ( type ) ) {
            return number.longValue();
        }
        if ( Double.TYPE == type || Double.class.equals ( type ) ) {
            return number.doubleValue();
        }
        if ( Integer.TYPE == type || Integer.class.equals ( type ) ) {
            return number.intValue();
        }
        if ( BigInteger.class.equals ( type ) ) {
            if ( number instanceof BigDecimal ) {
                return ( ( BigDecimal ) number ).toBigInteger();
            }
            if ( number instanceof BigInteger ) {
                return number;
            }
            return BigInteger.valueOf ( number.longValue() );
        } else if ( BigDecimal.class.equals ( type ) ) {
            if ( number instanceof BigDecimal ) {
                return number;
            }
            if ( number instanceof BigInteger ) {
                return new BigDecimal ( ( BigInteger ) number );
            }
            return new BigDecimal ( number.doubleValue() );
        } else {
            if ( Byte.TYPE == type || Byte.class.equals ( type ) ) {
                return number.byteValue();
            }
            if ( Short.TYPE == type || Short.class.equals ( type ) ) {
                return number.shortValue();
            }
            if ( Float.TYPE == type || Float.class.equals ( type ) ) {
                return number.floatValue();
            }
            if ( Number.class.equals ( type ) ) {
                return number;
            }
            throw new ELException ( MessageFactory.get ( "error.convert", number, number.getClass(), type ) );
        }
    }
    public static final Number coerceToNumber ( final ELContext ctx, final Object obj, final Class<?> type ) throws ELException {
        if ( ctx != null ) {
            final boolean originalIsPropertyResolved = ctx.isPropertyResolved();
            try {
                final Object result = ctx.getELResolver().convertToType ( ctx, obj, ( Class ) type );
                if ( ctx.isPropertyResolved() ) {
                    return ( Number ) result;
                }
            } finally {
                ctx.setPropertyResolved ( originalIsPropertyResolved );
            }
        }
        if ( !ELSupport.COERCE_TO_ZERO && obj == null && !type.isPrimitive() ) {
            return null;
        }
        if ( obj == null || "".equals ( obj ) ) {
            return coerceToNumber ( ELSupport.ZERO, type );
        }
        if ( obj instanceof String ) {
            return coerceToNumber ( ( String ) obj, type );
        }
        if ( ELArithmetic.isNumber ( obj ) ) {
            return coerceToNumber ( ( Number ) obj, type );
        }
        if ( obj instanceof Character ) {
            return coerceToNumber ( ( short ) ( char ) obj, type );
        }
        throw new ELException ( MessageFactory.get ( "error.convert", obj, obj.getClass(), type ) );
    }
    protected static final Number coerceToNumber ( final String val, final Class<?> type ) throws ELException {
        Label_0052: {
            if ( Long.TYPE != type ) {
                if ( !Long.class.equals ( type ) ) {
                    break Label_0052;
                }
            }
            try {
                return Long.valueOf ( val );
            } catch ( NumberFormatException nfe ) {
                throw new ELException ( MessageFactory.get ( "error.convert", val, String.class, type ) );
            }
        }
        Label_0104: {
            if ( Integer.TYPE != type ) {
                if ( !Integer.class.equals ( type ) ) {
                    break Label_0104;
                }
            }
            try {
                return Integer.valueOf ( val );
            } catch ( NumberFormatException nfe ) {
                throw new ELException ( MessageFactory.get ( "error.convert", val, String.class, type ) );
            }
        }
        Label_0156: {
            if ( Double.TYPE != type ) {
                if ( !Double.class.equals ( type ) ) {
                    break Label_0156;
                }
            }
            try {
                return Double.valueOf ( val );
            } catch ( NumberFormatException nfe ) {
                throw new ELException ( MessageFactory.get ( "error.convert", val, String.class, type ) );
            }
        }
        if ( BigInteger.class.equals ( type ) ) {
            try {
                return new BigInteger ( val );
            } catch ( NumberFormatException nfe ) {
                throw new ELException ( MessageFactory.get ( "error.convert", val, String.class, type ) );
            }
        }
        if ( BigDecimal.class.equals ( type ) ) {
            try {
                return new BigDecimal ( val );
            } catch ( NumberFormatException nfe ) {
                throw new ELException ( MessageFactory.get ( "error.convert", val, String.class, type ) );
            }
        }
        Label_0306: {
            if ( Byte.TYPE != type ) {
                if ( !Byte.class.equals ( type ) ) {
                    break Label_0306;
                }
            }
            try {
                return Byte.valueOf ( val );
            } catch ( NumberFormatException nfe ) {
                throw new ELException ( MessageFactory.get ( "error.convert", val, String.class, type ) );
            }
        }
        Label_0358: {
            if ( Short.TYPE != type ) {
                if ( !Short.class.equals ( type ) ) {
                    break Label_0358;
                }
            }
            try {
                return Short.valueOf ( val );
            } catch ( NumberFormatException nfe ) {
                throw new ELException ( MessageFactory.get ( "error.convert", val, String.class, type ) );
            }
        }
        if ( Float.TYPE != type ) {
            if ( !Float.class.equals ( type ) ) {
                throw new ELException ( MessageFactory.get ( "error.convert", val, String.class, type ) );
            }
        }
        try {
            return Float.valueOf ( val );
        } catch ( NumberFormatException nfe ) {
            throw new ELException ( MessageFactory.get ( "error.convert", val, String.class, type ) );
        }
        throw new ELException ( MessageFactory.get ( "error.convert", val, String.class, type ) );
    }
    public static final String coerceToString ( final ELContext ctx, final Object obj ) {
        if ( ctx != null ) {
            final boolean originalIsPropertyResolved = ctx.isPropertyResolved();
            try {
                final Object result = ctx.getELResolver().convertToType ( ctx, obj, ( Class ) String.class );
                if ( ctx.isPropertyResolved() ) {
                    return ( String ) result;
                }
            } finally {
                ctx.setPropertyResolved ( originalIsPropertyResolved );
            }
        }
        if ( obj == null ) {
            return "";
        }
        if ( obj instanceof String ) {
            return ( String ) obj;
        }
        if ( obj instanceof Enum ) {
            return ( ( Enum ) obj ).name();
        }
        return obj.toString();
    }
    public static final Object coerceToType ( final ELContext ctx, final Object obj, final Class<?> type ) throws ELException {
        if ( ctx != null ) {
            final boolean originalIsPropertyResolved = ctx.isPropertyResolved();
            try {
                final Object result = ctx.getELResolver().convertToType ( ctx, obj, ( Class ) type );
                if ( ctx.isPropertyResolved() ) {
                    return result;
                }
            } finally {
                ctx.setPropertyResolved ( originalIsPropertyResolved );
            }
        }
        if ( type == null || Object.class.equals ( type ) || ( obj != null && type.isAssignableFrom ( obj.getClass() ) ) ) {
            return obj;
        }
        if ( !ELSupport.COERCE_TO_ZERO && obj == null && !type.isPrimitive() && !String.class.isAssignableFrom ( type ) ) {
            return null;
        }
        if ( String.class.equals ( type ) ) {
            return coerceToString ( ctx, obj );
        }
        if ( ELArithmetic.isNumberType ( type ) ) {
            return coerceToNumber ( ctx, obj, type );
        }
        if ( Character.class.equals ( type ) || Character.TYPE == type ) {
            return coerceToCharacter ( ctx, obj );
        }
        if ( Boolean.class.equals ( type ) || Boolean.TYPE == type ) {
            return coerceToBoolean ( ctx, obj, Boolean.TYPE == type );
        }
        if ( type.isEnum() ) {
            return coerceToEnum ( ctx, obj, type );
        }
        if ( obj == null ) {
            return null;
        }
        if ( obj instanceof String ) {
            final PropertyEditor editor = PropertyEditorManager.findEditor ( type );
            if ( editor == null ) {
                if ( "".equals ( obj ) ) {
                    return null;
                }
                throw new ELException ( MessageFactory.get ( "error.convert", obj, obj.getClass(), type ) );
            } else {
                try {
                    editor.setAsText ( ( String ) obj );
                    return editor.getValue();
                } catch ( RuntimeException e ) {
                    if ( "".equals ( obj ) ) {
                        return null;
                    }
                    throw new ELException ( MessageFactory.get ( "error.convert", obj, obj.getClass(), type ), ( Throwable ) e );
                }
            }
        }
        if ( obj instanceof Set && type == Map.class && ( ( Set ) obj ).isEmpty() ) {
            return Collections.EMPTY_MAP;
        }
        if ( type.isArray() && obj.getClass().isArray() ) {
            return coerceToArray ( ctx, obj, type );
        }
        throw new ELException ( MessageFactory.get ( "error.convert", obj, obj.getClass(), type ) );
    }
    private static Object coerceToArray ( final ELContext ctx, final Object obj, final Class<?> type ) {
        final int size = Array.getLength ( obj );
        final Class<?> componentType = type.getComponentType();
        final Object result = Array.newInstance ( componentType, size );
        for ( int i = 0; i < size; ++i ) {
            Array.set ( result, i, coerceToType ( ctx, Array.get ( obj, i ), componentType ) );
        }
        return result;
    }
    public static final boolean isBigDecimalOp ( final Object obj0, final Object obj1 ) {
        return obj0 instanceof BigDecimal || obj1 instanceof BigDecimal;
    }
    public static final boolean isBigIntegerOp ( final Object obj0, final Object obj1 ) {
        return obj0 instanceof BigInteger || obj1 instanceof BigInteger;
    }
    public static final boolean isDoubleOp ( final Object obj0, final Object obj1 ) {
        return obj0 instanceof Double || obj1 instanceof Double || obj0 instanceof Float || obj1 instanceof Float;
    }
    public static final boolean isLongOp ( final Object obj0, final Object obj1 ) {
        return obj0 instanceof Long || obj1 instanceof Long || obj0 instanceof Integer || obj1 instanceof Integer || obj0 instanceof Character || obj1 instanceof Character || obj0 instanceof Short || obj1 instanceof Short || obj0 instanceof Byte || obj1 instanceof Byte;
    }
    public static final boolean isStringFloat ( final String str ) {
        final int len = str.length();
        if ( len > 1 ) {
            int i = 0;
            while ( i < len ) {
                switch ( str.charAt ( i ) ) {
                case 'E': {
                    return true;
                }
                case 'e': {
                    return true;
                }
                case '.': {
                    return true;
                }
                default: {
                    ++i;
                    continue;
                }
                }
            }
        }
        return false;
    }
    static {
        ZERO = 0L;
        IS_SECURITY_ENABLED = ( System.getSecurityManager() != null );
        String coerceToZeroStr;
        if ( ELSupport.IS_SECURITY_ENABLED ) {
            coerceToZeroStr = AccessController.doPrivileged ( ( PrivilegedAction<String> ) new PrivilegedAction<String>() {
                @Override
                public String run() {
                    return System.getProperty ( "org.apache.el.parser.COERCE_TO_ZERO", "false" );
                }
            } );
        } else {
            coerceToZeroStr = System.getProperty ( "org.apache.el.parser.COERCE_TO_ZERO", "false" );
        }
        COERCE_TO_ZERO = Boolean.parseBoolean ( coerceToZeroStr );
    }
}
