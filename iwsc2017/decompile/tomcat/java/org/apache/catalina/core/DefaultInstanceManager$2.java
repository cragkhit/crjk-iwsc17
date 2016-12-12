package org.apache.catalina.core;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;
static final class DefaultInstanceManager$2 implements PrivilegedAction<Method> {
    final   Class val$clazz;
    final   AnnotationCacheEntry val$entry;
    @Override
    public Method run() {
        Method result = null;
        try {
            result = this.val$clazz.getDeclaredMethod ( this.val$entry.getAccessibleObjectName(), ( Class[] ) this.val$entry.getParamTypes() );
        } catch ( NoSuchMethodException ex ) {}
        return result;
    }
}
