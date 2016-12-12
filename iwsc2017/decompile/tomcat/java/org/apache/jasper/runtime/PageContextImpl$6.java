package org.apache.jasper.runtime;
import java.security.PrivilegedAction;
class PageContextImpl$6 implements PrivilegedAction<Integer> {
    final   String val$name;
    @Override
    public Integer run() {
        return PageContextImpl.access$500 ( PageContextImpl.this, this.val$name );
    }
}
