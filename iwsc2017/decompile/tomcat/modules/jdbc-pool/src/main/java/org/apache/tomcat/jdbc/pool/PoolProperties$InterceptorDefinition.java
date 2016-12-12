// 
// Decompiled by Procyon v0.5.29
// 

package org.apache.tomcat.jdbc.pool;

import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;

public static class InterceptorDefinition implements Serializable
{
    private static final long serialVersionUID = 1L;
    protected String className;
    protected Map<String, InterceptorProperty> properties;
    protected volatile Class<?> clazz;
    
    public InterceptorDefinition(final String className) {
        this.properties = new HashMap<String, InterceptorProperty>();
        this.clazz = null;
        this.className = className;
    }
    
    public InterceptorDefinition(final Class<?> cl) {
        this(cl.getName());
        this.clazz = cl;
    }
    
    public String getClassName() {
        return this.className;
    }
    
    public void addProperty(final String name, final String value) {
        final InterceptorProperty p = new InterceptorProperty(name, value);
        this.addProperty(p);
    }
    
    public void addProperty(final InterceptorProperty p) {
        this.properties.put(p.getName(), p);
    }
    
    public Map<String, InterceptorProperty> getProperties() {
        return this.properties;
    }
    
    public Class<? extends JdbcInterceptor> getInterceptorClass() throws ClassNotFoundException {
        if (this.clazz == null) {
            if (this.getClassName().indexOf(46) < 0) {
                if (PoolProperties.access$000().isDebugEnabled()) {
                    PoolProperties.access$000().debug((Object)("Loading interceptor class:org.apache.tomcat.jdbc.pool.interceptor." + this.getClassName()));
                }
                this.clazz = ClassLoaderUtil.loadClass("org.apache.tomcat.jdbc.pool.interceptor." + this.getClassName(), PoolProperties.class.getClassLoader(), Thread.currentThread().getContextClassLoader());
            }
            else {
                if (PoolProperties.access$000().isDebugEnabled()) {
                    PoolProperties.access$000().debug((Object)("Loading interceptor class:" + this.getClassName()));
                }
                this.clazz = ClassLoaderUtil.loadClass(this.getClassName(), PoolProperties.class.getClassLoader(), Thread.currentThread().getContextClassLoader());
            }
        }
        return (Class<? extends JdbcInterceptor>)this.clazz;
    }
}
