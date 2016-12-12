package org.apache.tomcat;
public interface ContextBind {
    ClassLoader bind ( boolean p0, ClassLoader p1 );
    void unbind ( boolean p0, ClassLoader p1 );
}
