package org.apache.jasper.el;
import javax.servlet.jsp.el.ELException;
import javax.el.ELContext;
import javax.servlet.jsp.el.VariableResolver;
@Deprecated
public final class VariableResolverImpl implements VariableResolver {
    private final ELContext ctx;
    public VariableResolverImpl ( final ELContext ctx ) {
        this.ctx = ctx;
    }
    public Object resolveVariable ( final String pName ) throws ELException {
        return this.ctx.getELResolver().getValue ( this.ctx, ( Object ) null, ( Object ) pName );
    }
}
