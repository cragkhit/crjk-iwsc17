package org.apache.catalina.core;
import java.util.Arrays;
import org.apache.tomcat.util.descriptor.web.FilterMap;
private static final class ContextFilterMaps {
    private final Object lock;
    private FilterMap[] array;
    private int insertPoint;
    private ContextFilterMaps() {
        this.lock = new Object();
        this.array = new FilterMap[0];
        this.insertPoint = 0;
    }
    public FilterMap[] asArray() {
        synchronized ( this.lock ) {
            return this.array;
        }
    }
    public void add ( final FilterMap filterMap ) {
        synchronized ( this.lock ) {
            final FilterMap[] results = Arrays.copyOf ( this.array, this.array.length + 1 );
            results[this.array.length] = filterMap;
            this.array = results;
        }
    }
    public void addBefore ( final FilterMap filterMap ) {
        synchronized ( this.lock ) {
            final FilterMap[] results = new FilterMap[this.array.length + 1];
            System.arraycopy ( this.array, 0, results, 0, this.insertPoint );
            System.arraycopy ( this.array, this.insertPoint, results, this.insertPoint + 1, this.array.length - this.insertPoint );
            results[this.insertPoint] = filterMap;
            this.array = results;
            ++this.insertPoint;
        }
    }
    public void remove ( final FilterMap filterMap ) {
        synchronized ( this.lock ) {
            int n = -1;
            for ( int i = 0; i < this.array.length; ++i ) {
                if ( this.array[i] == filterMap ) {
                    n = i;
                    break;
                }
            }
            if ( n < 0 ) {
                return;
            }
            final FilterMap[] results = new FilterMap[this.array.length - 1];
            System.arraycopy ( this.array, 0, results, 0, n );
            System.arraycopy ( this.array, n + 1, results, n, this.array.length - 1 - n );
            this.array = results;
            if ( n < this.insertPoint ) {
                --this.insertPoint;
            }
        }
    }
}
