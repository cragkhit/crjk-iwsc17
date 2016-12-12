package org.apache.jasper.el;
import java.util.Locale;
import javax.el.VariableMapper;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.ELContext;
public final class ELContextWrapper extends ELContext {
    private final ELContext target;
    private final FunctionMapper fnMapper;
    public ELContextWrapper ( final ELContext target, final FunctionMapper fnMapper ) {
        this.target = target;
        this.fnMapper = fnMapper;
    }
    public ELResolver getELResolver() {
        return this.target.getELResolver();
    }
    public FunctionMapper getFunctionMapper() {
        if ( this.fnMapper != null ) {
            return this.fnMapper;
        }
        return this.target.getFunctionMapper();
    }
    public VariableMapper getVariableMapper() {
        return this.target.getVariableMapper();
    }
    public Object getContext ( final Class key ) {
        return this.target.getContext ( key );
    }
    public Locale getLocale() {
        return this.target.getLocale();
    }
    public boolean isPropertyResolved() {
        return this.target.isPropertyResolved();
    }
    public void putContext ( final Class key, final Object contextObject ) throws NullPointerException {
        this.target.putContext ( key, contextObject );
    }
    public void setLocale ( final Locale locale ) {
        this.target.setLocale ( locale );
    }
    public void setPropertyResolved ( final boolean resolved ) {
        this.target.setPropertyResolved ( resolved );
    }
}
