package org.apache.el.lang;
import java.math.BigInteger;
import java.math.BigDecimal;
import org.apache.el.util.MessageFactory;
public abstract class ELArithmetic {
    public static final BigDecimalDelegate BIGDECIMAL;
    public static final BigIntegerDelegate BIGINTEGER;
    public static final DoubleDelegate DOUBLE;
    public static final LongDelegate LONG;
    private static final Long ZERO;
    public static final Number add ( final Object obj0, final Object obj1 ) {
        final ELArithmetic delegate = findDelegate ( obj0, obj1 );
        if ( delegate == null ) {
            return 0L;
        }
        final Number num0 = delegate.coerce ( obj0 );
        final Number num = delegate.coerce ( obj1 );
        return delegate.add ( num0, num );
    }
    public static final Number mod ( final Object obj0, final Object obj1 ) {
        if ( obj0 == null && obj1 == null ) {
            return 0L;
        }
        ELArithmetic delegate;
        if ( ELArithmetic.BIGDECIMAL.matches ( obj0, obj1 ) ) {
            delegate = ELArithmetic.DOUBLE;
        } else if ( ELArithmetic.DOUBLE.matches ( obj0, obj1 ) ) {
            delegate = ELArithmetic.DOUBLE;
        } else if ( ELArithmetic.BIGINTEGER.matches ( obj0, obj1 ) ) {
            delegate = ELArithmetic.BIGINTEGER;
        } else {
            delegate = ELArithmetic.LONG;
        }
        final Number num0 = delegate.coerce ( obj0 );
        final Number num = delegate.coerce ( obj1 );
        return delegate.mod ( num0, num );
    }
    public static final Number subtract ( final Object obj0, final Object obj1 ) {
        final ELArithmetic delegate = findDelegate ( obj0, obj1 );
        if ( delegate == null ) {
            return 0L;
        }
        final Number num0 = delegate.coerce ( obj0 );
        final Number num = delegate.coerce ( obj1 );
        return delegate.subtract ( num0, num );
    }
    public static final Number divide ( final Object obj0, final Object obj1 ) {
        if ( obj0 == null && obj1 == null ) {
            return ELArithmetic.ZERO;
        }
        ELArithmetic delegate;
        if ( ELArithmetic.BIGDECIMAL.matches ( obj0, obj1 ) ) {
            delegate = ELArithmetic.BIGDECIMAL;
        } else if ( ELArithmetic.BIGINTEGER.matches ( obj0, obj1 ) ) {
            delegate = ELArithmetic.BIGDECIMAL;
        } else {
            delegate = ELArithmetic.DOUBLE;
        }
        final Number num0 = delegate.coerce ( obj0 );
        final Number num = delegate.coerce ( obj1 );
        return delegate.divide ( num0, num );
    }
    public static final Number multiply ( final Object obj0, final Object obj1 ) {
        final ELArithmetic delegate = findDelegate ( obj0, obj1 );
        if ( delegate == null ) {
            return 0L;
        }
        final Number num0 = delegate.coerce ( obj0 );
        final Number num = delegate.coerce ( obj1 );
        return delegate.multiply ( num0, num );
    }
    private static ELArithmetic findDelegate ( final Object obj0, final Object obj1 ) {
        if ( obj0 == null && obj1 == null ) {
            return null;
        }
        if ( ELArithmetic.BIGDECIMAL.matches ( obj0, obj1 ) ) {
            return ELArithmetic.BIGDECIMAL;
        }
        if ( ELArithmetic.DOUBLE.matches ( obj0, obj1 ) ) {
            if ( ELArithmetic.BIGINTEGER.matches ( obj0, obj1 ) ) {
                return ELArithmetic.BIGDECIMAL;
            }
            return ELArithmetic.DOUBLE;
        } else {
            if ( ELArithmetic.BIGINTEGER.matches ( obj0, obj1 ) ) {
                return ELArithmetic.BIGINTEGER;
            }
            return ELArithmetic.LONG;
        }
    }
    public static final boolean isNumber ( final Object obj ) {
        return obj != null && isNumberType ( obj.getClass() );
    }
    public static final boolean isNumberType ( final Class<?> type ) {
        return type == Long.TYPE || type == Double.TYPE || type == Byte.TYPE || type == Short.TYPE || type == Integer.TYPE || type == Float.TYPE || Number.class.isAssignableFrom ( type );
    }
    protected abstract Number add ( final Number p0, final Number p1 );
    protected abstract Number multiply ( final Number p0, final Number p1 );
    protected abstract Number subtract ( final Number p0, final Number p1 );
    protected abstract Number mod ( final Number p0, final Number p1 );
    protected abstract Number coerce ( final Number p0 );
    protected final Number coerce ( final Object obj ) {
        if ( isNumber ( obj ) ) {
            return this.coerce ( ( Number ) obj );
        }
        if ( obj == null || "".equals ( obj ) ) {
            return this.coerce ( ELArithmetic.ZERO );
        }
        if ( obj instanceof String ) {
            return this.coerce ( ( String ) obj );
        }
        if ( obj instanceof Character ) {
            return this.coerce ( ( short ) ( char ) obj );
        }
        throw new IllegalArgumentException ( MessageFactory.get ( "error.convert", obj, obj.getClass(), "Number" ) );
    }
    protected abstract Number coerce ( final String p0 );
    protected abstract Number divide ( final Number p0, final Number p1 );
    protected abstract boolean matches ( final Object p0, final Object p1 );
    static {
        BIGDECIMAL = new BigDecimalDelegate();
        BIGINTEGER = new BigIntegerDelegate();
        DOUBLE = new DoubleDelegate();
        LONG = new LongDelegate();
        ZERO = 0L;
    }
    public static final class BigDecimalDelegate extends ELArithmetic {
        @Override
        protected Number add ( final Number num0, final Number num1 ) {
            return ( ( BigDecimal ) num0 ).add ( ( BigDecimal ) num1 );
        }
        @Override
        protected Number coerce ( final Number num ) {
            if ( num instanceof BigDecimal ) {
                return num;
            }
            if ( num instanceof BigInteger ) {
                return new BigDecimal ( ( BigInteger ) num );
            }
            return new BigDecimal ( num.doubleValue() );
        }
        @Override
        protected Number coerce ( final String str ) {
            return new BigDecimal ( str );
        }
        @Override
        protected Number divide ( final Number num0, final Number num1 ) {
            return ( ( BigDecimal ) num0 ).divide ( ( BigDecimal ) num1, 4 );
        }
        @Override
        protected Number subtract ( final Number num0, final Number num1 ) {
            return ( ( BigDecimal ) num0 ).subtract ( ( BigDecimal ) num1 );
        }
        @Override
        protected Number mod ( final Number num0, final Number num1 ) {
            return num0.doubleValue() % num1.doubleValue();
        }
        @Override
        protected Number multiply ( final Number num0, final Number num1 ) {
            return ( ( BigDecimal ) num0 ).multiply ( ( BigDecimal ) num1 );
        }
        public boolean matches ( final Object obj0, final Object obj1 ) {
            return obj0 instanceof BigDecimal || obj1 instanceof BigDecimal;
        }
    }
    public static final class BigIntegerDelegate extends ELArithmetic {
        @Override
        protected Number add ( final Number num0, final Number num1 ) {
            return ( ( BigInteger ) num0 ).add ( ( BigInteger ) num1 );
        }
        @Override
        protected Number coerce ( final Number num ) {
            if ( num instanceof BigInteger ) {
                return num;
            }
            return new BigInteger ( num.toString() );
        }
        @Override
        protected Number coerce ( final String str ) {
            return new BigInteger ( str );
        }
        @Override
        protected Number divide ( final Number num0, final Number num1 ) {
            return new BigDecimal ( ( BigInteger ) num0 ).divide ( new BigDecimal ( ( BigInteger ) num1 ), 4 );
        }
        @Override
        protected Number multiply ( final Number num0, final Number num1 ) {
            return ( ( BigInteger ) num0 ).multiply ( ( BigInteger ) num1 );
        }
        @Override
        protected Number mod ( final Number num0, final Number num1 ) {
            return ( ( BigInteger ) num0 ).mod ( ( BigInteger ) num1 );
        }
        @Override
        protected Number subtract ( final Number num0, final Number num1 ) {
            return ( ( BigInteger ) num0 ).subtract ( ( BigInteger ) num1 );
        }
        public boolean matches ( final Object obj0, final Object obj1 ) {
            return obj0 instanceof BigInteger || obj1 instanceof BigInteger;
        }
    }
    public static final class DoubleDelegate extends ELArithmetic {
        @Override
        protected Number add ( final Number num0, final Number num1 ) {
            if ( num0 instanceof BigDecimal ) {
                return ( ( BigDecimal ) num0 ).add ( new BigDecimal ( num1.doubleValue() ) );
            }
            if ( num1 instanceof BigDecimal ) {
                return new BigDecimal ( num0.doubleValue() ).add ( ( BigDecimal ) num1 );
            }
            return num0.doubleValue() + num1.doubleValue();
        }
        @Override
        protected Number coerce ( final Number num ) {
            if ( num instanceof Double ) {
                return num;
            }
            if ( num instanceof BigInteger ) {
                return new BigDecimal ( ( BigInteger ) num );
            }
            return num.doubleValue();
        }
        @Override
        protected Number coerce ( final String str ) {
            return Double.valueOf ( str );
        }
        @Override
        protected Number divide ( final Number num0, final Number num1 ) {
            return num0.doubleValue() / num1.doubleValue();
        }
        @Override
        protected Number mod ( final Number num0, final Number num1 ) {
            return num0.doubleValue() % num1.doubleValue();
        }
        @Override
        protected Number subtract ( final Number num0, final Number num1 ) {
            if ( num0 instanceof BigDecimal ) {
                return ( ( BigDecimal ) num0 ).subtract ( new BigDecimal ( num1.doubleValue() ) );
            }
            if ( num1 instanceof BigDecimal ) {
                return new BigDecimal ( num0.doubleValue() ).subtract ( ( BigDecimal ) num1 );
            }
            return num0.doubleValue() - num1.doubleValue();
        }
        @Override
        protected Number multiply ( final Number num0, final Number num1 ) {
            if ( num0 instanceof BigDecimal ) {
                return ( ( BigDecimal ) num0 ).multiply ( new BigDecimal ( num1.doubleValue() ) );
            }
            if ( num1 instanceof BigDecimal ) {
                return new BigDecimal ( num0.doubleValue() ).multiply ( ( BigDecimal ) num1 );
            }
            return num0.doubleValue() * num1.doubleValue();
        }
        public boolean matches ( final Object obj0, final Object obj1 ) {
            return obj0 instanceof Double || obj1 instanceof Double || obj0 instanceof Float || obj1 instanceof Float || ( obj0 instanceof String && ELSupport.isStringFloat ( ( String ) obj0 ) ) || ( obj1 instanceof String && ELSupport.isStringFloat ( ( String ) obj1 ) );
        }
    }
    public static final class LongDelegate extends ELArithmetic {
        @Override
        protected Number add ( final Number num0, final Number num1 ) {
            return num0.longValue() + num1.longValue();
        }
        @Override
        protected Number coerce ( final Number num ) {
            if ( num instanceof Long ) {
                return num;
            }
            return num.longValue();
        }
        @Override
        protected Number coerce ( final String str ) {
            return Long.valueOf ( str );
        }
        @Override
        protected Number divide ( final Number num0, final Number num1 ) {
            return num0.longValue() / num1.longValue();
        }
        @Override
        protected Number mod ( final Number num0, final Number num1 ) {
            return num0.longValue() % num1.longValue();
        }
        @Override
        protected Number subtract ( final Number num0, final Number num1 ) {
            return num0.longValue() - num1.longValue();
        }
        @Override
        protected Number multiply ( final Number num0, final Number num1 ) {
            return num0.longValue() * num1.longValue();
        }
        public boolean matches ( final Object obj0, final Object obj1 ) {
            return obj0 instanceof Long || obj1 instanceof Long;
        }
    }
}
