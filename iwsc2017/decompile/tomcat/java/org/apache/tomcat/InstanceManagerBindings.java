package org.apache.tomcat;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
public final class InstanceManagerBindings {
    private static final Map<ClassLoader, InstanceManager> bindings;
    public static final void bind ( final ClassLoader classLoader, final InstanceManager instanceManager ) {
        InstanceManagerBindings.bindings.put ( classLoader, instanceManager );
    }
    public static final void unbind ( final ClassLoader classLoader ) {
        InstanceManagerBindings.bindings.remove ( classLoader );
    }
    public static final InstanceManager get ( final ClassLoader classLoader ) {
        return InstanceManagerBindings.bindings.get ( classLoader );
    }
    static {
        bindings = new ConcurrentHashMap<ClassLoader, InstanceManager>();
    }
}
