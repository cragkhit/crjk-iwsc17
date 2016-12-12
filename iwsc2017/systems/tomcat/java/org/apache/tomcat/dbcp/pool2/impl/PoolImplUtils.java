package org.apache.tomcat.dbcp.pool2.impl;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import org.apache.tomcat.dbcp.pool2.PooledObjectFactory;
class PoolImplUtils {
    @SuppressWarnings ( "rawtypes" )
    static Class<?> getFactoryType ( final Class<? extends PooledObjectFactory> factory ) {
        return ( Class<?> ) getGenericType ( PooledObjectFactory.class, factory );
    }
    private static <T> Object getGenericType ( final Class<T> type,
            final Class<? extends T> clazz ) {
        final Type[] interfaces = clazz.getGenericInterfaces();
        for ( final Type iface : interfaces ) {
            if ( iface instanceof ParameterizedType ) {
                final ParameterizedType pi = ( ParameterizedType ) iface;
                if ( pi.getRawType() instanceof Class ) {
                    if ( type.isAssignableFrom ( ( Class<?> ) pi.getRawType() ) ) {
                        return getTypeParameter (
                                   clazz, pi.getActualTypeArguments() [0] );
                    }
                }
            }
        }
        @SuppressWarnings ( "unchecked" )
        final
        Class<? extends T> superClazz =
            ( Class<? extends T> ) clazz.getSuperclass();
        final Object result = getGenericType ( type, superClazz );
        if ( result instanceof Class<?> ) {
            return result;
        } else if ( result instanceof Integer ) {
            final ParameterizedType superClassType =
                ( ParameterizedType ) clazz.getGenericSuperclass();
            return getTypeParameter ( clazz,
                                      superClassType.getActualTypeArguments() [
                                          ( ( Integer ) result ).intValue()] );
        } else {
            return null;
        }
    }
    private static Object getTypeParameter ( final Class<?> clazz, final Type argType ) {
        if ( argType instanceof Class<?> ) {
            return argType;
        }
        final TypeVariable<?>[] tvs = clazz.getTypeParameters();
        for ( int i = 0; i < tvs.length; i++ ) {
            if ( tvs[i].equals ( argType ) ) {
                return Integer.valueOf ( i );
            }
        }
        return null;
    }
}
