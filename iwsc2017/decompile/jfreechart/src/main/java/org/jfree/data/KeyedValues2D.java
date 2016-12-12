package org.jfree.data;
import java.util.List;
public interface KeyedValues2D extends Values2D {
    Comparable getRowKey ( int p0 );
    int getRowIndex ( Comparable p0 );
    List getRowKeys();
    Comparable getColumnKey ( int p0 );
    int getColumnIndex ( Comparable p0 );
    List getColumnKeys();
    Number getValue ( Comparable p0, Comparable p1 );
}
