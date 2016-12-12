package org.apache.jasper.runtime;
import org.apache.jasper.compiler.Localizer;
import java.security.PrivilegedAction;
class PageContextImpl$7 implements PrivilegedAction<Object> {
    final   String val$name;
    @Override
    public Object run() {
        if ( this.val$name == null ) {
            throw new NullPointerException ( Localizer.getMessage ( "jsp.error.attribute.null_name" ) );
        }
        return PageContextImpl.access$600 ( PageContextImpl.this, this.val$name );
    }
}
