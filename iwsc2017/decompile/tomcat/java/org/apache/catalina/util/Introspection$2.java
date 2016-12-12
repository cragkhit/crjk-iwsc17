package org.apache.catalina.util;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;
static final class Introspection$2 implements PrivilegedAction<Method[]> {
    final   Class val$clazz;
    @Override
    public Method[] run() {
        return this.val$clazz.getDeclaredMethods();
    }
}
