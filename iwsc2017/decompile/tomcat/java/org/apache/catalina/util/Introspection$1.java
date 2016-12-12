package org.apache.catalina.util;
import java.lang.reflect.Field;
import java.security.PrivilegedAction;
static final class Introspection$1 implements PrivilegedAction<Field[]> {
    final   Class val$clazz;
    @Override
    public Field[] run() {
        return this.val$clazz.getDeclaredFields();
    }
}
