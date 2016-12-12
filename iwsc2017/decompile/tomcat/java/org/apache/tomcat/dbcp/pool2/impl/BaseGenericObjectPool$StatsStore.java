package org.apache.tomcat.dbcp.pool2.impl;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
private class StatsStore {
    private final AtomicLong[] values;
    private final int size;
    private int index;
    public StatsStore ( final int size ) {
        this.size = size;
        this.values = new AtomicLong[size];
        for ( int i = 0; i < size; ++i ) {
            this.values[i] = new AtomicLong ( -1L );
        }
    }
    public synchronized void add ( final long value ) {
        this.values[this.index].set ( value );
        ++this.index;
        if ( this.index == this.size ) {
            this.index = 0;
        }
    }
    public long getMean() {
        double result = 0.0;
        int counter = 0;
        for ( int i = 0; i < this.size; ++i ) {
            final long value = this.values[i].get();
            if ( value != -1L ) {
                ++counter;
                result = result * ( ( counter - 1 ) / counter ) + value / counter;
            }
        }
        return ( long ) result;
    }
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append ( "StatsStore [values=" );
        builder.append ( Arrays.toString ( this.values ) );
        builder.append ( ", size=" );
        builder.append ( this.size );
        builder.append ( ", index=" );
        builder.append ( this.index );
        builder.append ( "]" );
        return builder.toString();
    }
}
