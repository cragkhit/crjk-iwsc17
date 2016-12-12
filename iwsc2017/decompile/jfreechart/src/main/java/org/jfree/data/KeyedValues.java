package org.jfree.data;
import java.util.List;
public interface KeyedValues extends Values {
    Comparable getKey ( int p0 );
    int getIndex ( Comparable p0 );
    List getKeys();
    Number getValue ( Comparable p0 );
}
