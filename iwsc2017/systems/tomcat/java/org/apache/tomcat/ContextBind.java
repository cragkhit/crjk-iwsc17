package org.apache.tomcat;
public interface ContextBind {
    ClassLoader bind ( boolean usePrivilegedAction, ClassLoader originalClassLoader );
    void unbind ( boolean usePrivilegedAction, ClassLoader originalClassLoader );
}
