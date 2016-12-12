package org.apache.el.lang;
import java.math.BigDecimal;
import java.math.BigInteger;
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
