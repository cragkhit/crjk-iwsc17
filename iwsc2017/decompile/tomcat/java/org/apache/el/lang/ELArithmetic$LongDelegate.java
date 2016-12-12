package org.apache.el.lang;
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
