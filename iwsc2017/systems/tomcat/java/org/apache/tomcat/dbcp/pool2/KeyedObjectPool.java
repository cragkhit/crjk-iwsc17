package org.apache.tomcat.dbcp.pool2;
import java.util.NoSuchElementException;
public interface KeyedObjectPool<K, V> {
    V borrowObject ( K key ) throws Exception, NoSuchElementException, IllegalStateException;
    void returnObject ( K key, V obj ) throws Exception;
    void invalidateObject ( K key, V obj ) throws Exception;
    void addObject ( K key ) throws Exception, IllegalStateException,
             UnsupportedOperationException;
    int getNumIdle ( K key );
    int getNumActive ( K key );
    int getNumIdle();
    int getNumActive();
    void clear() throws Exception, UnsupportedOperationException;
    void clear ( K key ) throws Exception, UnsupportedOperationException;
    void close();
}
