package org.apache.jasper.runtime;
import javax.el.ELResolver;
import org.apache.jasper.el.ELContextImpl;
import java.security.PrivilegedAction;
class JspApplicationContextImpl$1 implements PrivilegedAction<ELContextImpl> {
    final   ELResolver val$r;
    @Override
    public ELContextImpl run() {
        return new ELContextImpl ( this.val$r );
    }
}
