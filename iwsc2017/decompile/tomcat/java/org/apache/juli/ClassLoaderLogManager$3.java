package org.apache.juli;
import java.util.logging.Logger;
import java.security.PrivilegedAction;
static final class ClassLoaderLogManager$3 implements PrivilegedAction<Void> {
    final   Logger val$logger;
    final   Logger val$parent;
    @Override
    public Void run() {
        this.val$logger.setParent ( this.val$parent );
        return null;
    }
}
