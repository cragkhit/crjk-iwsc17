package org.apache.jasper.runtime;
import java.security.PrivilegedAction;
class PageContextImpl$2 implements PrivilegedAction<Object> {
    final   String val$name;
    final   int val$scope;
    @Override
    public Object run() {
        return PageContextImpl.access$100 ( PageContextImpl.this, this.val$name, this.val$scope );
    }
}
