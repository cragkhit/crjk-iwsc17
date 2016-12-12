package org.apache.tomcat.dbcp.pool2;
public interface PooledObjectFactory<T> {
    PooledObject<T> makeObject() throws Exception;
    void destroyObject ( PooledObject<T> p ) throws Exception;
    boolean validateObject ( PooledObject<T> p );
    void activateObject ( PooledObject<T> p ) throws Exception;
    void passivateObject ( PooledObject<T> p ) throws Exception;
}
