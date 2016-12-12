package org.apache.jasper.runtime;
import java.security.PrivilegedAction;
class PageContextImpl$3 implements PrivilegedAction<Void> {
    final   String val$name;
    final   Object val$attribute;
    @Override
    public Void run() {
        PageContextImpl.access$200 ( PageContextImpl.this, this.val$name, this.val$attribute );
        return null;
    }
}
