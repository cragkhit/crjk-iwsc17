package org.apache.catalina.core;
import org.apache.catalina.Container;
import java.security.PrivilegedAction;
protected class PrivilegedAddChild implements PrivilegedAction<Void> {
    private final Container child;
    PrivilegedAddChild ( final Container child ) {
        this.child = child;
    }
    @Override
    public Void run() {
        ContainerBase.access$000 ( ContainerBase.this, this.child );
        return null;
    }
}
