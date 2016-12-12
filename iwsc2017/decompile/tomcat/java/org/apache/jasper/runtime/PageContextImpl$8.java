package org.apache.jasper.runtime;
import java.util.Enumeration;
import java.security.PrivilegedAction;
class PageContextImpl$8 implements PrivilegedAction<Enumeration<String>> {
    final   int val$scope;
    @Override
    public Enumeration<String> run() {
        return ( Enumeration<String> ) PageContextImpl.access$700 ( PageContextImpl.this, this.val$scope );
    }
}
