package org.apache.jasper.runtime;
import org.apache.jasper.compiler.Localizer;
import java.security.PrivilegedAction;
class JspWriterImpl$1 implements PrivilegedAction<String> {
    final   String val$message;
    @Override
    public String run() {
        return Localizer.getMessage ( this.val$message );
    }
}
