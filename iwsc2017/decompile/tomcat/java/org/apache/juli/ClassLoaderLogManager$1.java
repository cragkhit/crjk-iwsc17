package org.apache.juli;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.PrivilegedAction;
class ClassLoaderLogManager$1 implements PrivilegedAction<Void> {
    final   Logger val$logger;
    final   String val$levelString;
    @Override
    public Void run() {
        this.val$logger.setLevel ( Level.parse ( this.val$levelString.trim() ) );
        return null;
    }
}
