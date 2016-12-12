package org.apache.jasper.runtime;
import java.security.PrivilegedExceptionAction;
class PageContextImpl$10 implements PrivilegedExceptionAction<Void> {
    final   String val$relativeUrlPath;
    final   boolean val$flush;
    @Override
    public Void run() throws Exception {
        PageContextImpl.access$900 ( PageContextImpl.this, this.val$relativeUrlPath, this.val$flush );
        return null;
    }
}
