package org.apache.catalina.core;
import java.lang.reflect.Field;
import java.security.PrivilegedAction;
static final class DefaultInstanceManager$3 implements PrivilegedAction<Field> {
    final   Class val$clazz;
    final   AnnotationCacheEntry val$entry;
    @Override
    public Field run() {
        Field result = null;
        try {
            result = this.val$clazz.getDeclaredField ( this.val$entry.getAccessibleObjectName() );
        } catch ( NoSuchFieldException ex ) {}
        return result;
    }
}
