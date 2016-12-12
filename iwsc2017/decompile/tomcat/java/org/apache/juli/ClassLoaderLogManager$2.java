package org.apache.juli;
import java.io.IOException;
import java.security.PrivilegedAction;
class ClassLoaderLogManager$2 implements PrivilegedAction<Void> {
    final   ClassLoader val$classLoaderParam;
    @Override
    public Void run() {
        try {
            ClassLoaderLogManager.this.readConfiguration ( this.val$classLoaderParam );
        } catch ( IOException ex ) {}
        return null;
    }
}
