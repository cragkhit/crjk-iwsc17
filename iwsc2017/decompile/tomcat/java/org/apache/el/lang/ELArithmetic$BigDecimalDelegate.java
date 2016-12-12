package org.apache.el.lang;
import java.math.BigInteger;
import java.math.BigDecimal;
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
