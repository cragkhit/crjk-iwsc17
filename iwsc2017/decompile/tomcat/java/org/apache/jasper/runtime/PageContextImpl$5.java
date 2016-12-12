package org.apache.jasper.runtime;
import java.security.PrivilegedAction;
class PageContextImpl$5 implements PrivilegedAction<Void> {
    final   String val$name;
    final   int val$scope;
    @Override
    public Void run() {
        PageContextImpl.access$400 ( PageContextImpl.this, this.val$name, this.val$scope );
        return null;
    }
}
