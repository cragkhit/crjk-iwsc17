package org.apache.tomcat.dbcp.pool2;
public abstract class BasePooledObjectFactory<T> extends BaseObject implements PooledObjectFactory<T> {
    public abstract T create() throws Exception;
    public abstract PooledObject<T> wrap ( T obj );
    @Override
    public PooledObject<T> makeObject() throws Exception {
        return wrap ( create() );
    }
    @Override
    public void destroyObject ( final PooledObject<T> p )
    throws Exception  {
    }
    @Override
    public boolean validateObject ( final PooledObject<T> p ) {
        return true;
    }
    @Override
    public void activateObject ( final PooledObject<T> p ) throws Exception {
    }
    @Override
    public void passivateObject ( final PooledObject<T> p )
    throws Exception {
    }
}
