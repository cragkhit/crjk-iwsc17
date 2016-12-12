package org.apache.jasper.runtime;
import java.security.PrivilegedAction;
class PageContextImpl$1 implements PrivilegedAction<Object> {
    final   String val$name;
    @Override
    public Object run() {
        return PageContextImpl.access$000 ( PageContextImpl.this, this.val$name );
    }
}
