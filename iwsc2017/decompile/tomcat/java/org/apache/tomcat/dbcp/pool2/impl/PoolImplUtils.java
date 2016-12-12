package org.apache.tomcat.dbcp.pool2.impl;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;
import org.apache.tomcat.dbcp.pool2.PooledObjectFactory;
class PoolImplUtils {
    static Class<?> getFactoryType ( final Class<? extends PooledObjectFactory> factory ) {
        return ( Class<?> ) getGenericType ( PooledObjectFactory.class, factory );
    }
    private static <T> Object getGenericType ( final Class<T> type, final Class<? extends T> clazz ) {
        final Type[] genericInterfaces;
        final Type[] interfaces = genericInterfaces = clazz.getGenericInterfaces();
        for ( final Type iface : genericInterfaces ) {
            if ( iface instanceof ParameterizedType ) {
                final ParameterizedType pi = ( ParameterizedType ) iface;
                if ( pi.getRawType() instanceof Class && type.isAssignableFrom ( ( Class<?> ) pi.getRawType() ) ) {
                    return getTypeParameter ( clazz, pi.getActualTypeArguments() [0] );
                }
            }
        }
        final Class<? extends T> superClazz = ( Class<? extends T> ) clazz.getSuperclass();
        final Object result = getGenericType ( ( Class<Object> ) type, superClazz );
        if ( result instanceof Class ) {
            return result;
        }
        if ( result instanceof Integer ) {
            final ParameterizedType superClassType = ( ParameterizedType ) clazz.getGenericSuperclass();
            return getTypeParameter ( clazz, superClassType.getActualTypeArguments() [ ( int ) result] );
        }
        return null;
    }
    private static Object getTypeParameter ( final Class<?> clazz, final Type argType ) {
        if ( argType instanceof Class ) {
            return argType;
        }
        final TypeVariable<?>[] tvs = clazz.getTypeParameters();
        for ( int i = 0; i < tvs.length; ++i ) {
            if ( tvs[i].equals ( argType ) ) {
                return i;
            }
        }
        return null;
    }
}
