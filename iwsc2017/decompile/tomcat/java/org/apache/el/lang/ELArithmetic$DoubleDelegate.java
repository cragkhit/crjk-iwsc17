package org.apache.el.lang;
import java.math.BigInteger;
import java.math.BigDecimal;
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
