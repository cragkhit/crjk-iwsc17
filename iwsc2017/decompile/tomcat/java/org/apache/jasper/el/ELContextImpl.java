package org.apache.jasper.el;
import java.util.HashMap;
import javax.el.ValueExpression;
import java.util.Map;
import javax.el.ELManager;
import java.lang.reflect.Method;
import javax.el.BeanELResolver;
import javax.el.ArrayELResolver;
import javax.el.ListELResolver;
import javax.el.ResourceBundleELResolver;
import javax.el.MapELResolver;
import javax.el.StaticFieldELResolver;
import javax.el.CompositeELResolver;
import org.apache.jasper.Constants;
import javax.el.ExpressionFactory;
import javax.el.VariableMapper;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.ELContext;
public class ELContextImpl extends ELContext {
    private static final FunctionMapper NullFunctionMapper;
    private static final ELResolver DefaultResolver;
    private final ELResolver resolver;
    private FunctionMapper functionMapper;
    private VariableMapper variableMapper;
    public ELContextImpl ( final ExpressionFactory factory ) {
        this ( getDefaultResolver ( factory ) );
    }
    public ELContextImpl ( final ELResolver resolver ) {
        this.functionMapper = ELContextImpl.NullFunctionMapper;
        this.resolver = resolver;
    }
    public ELResolver getELResolver() {
        return this.resolver;
    }
    public FunctionMapper getFunctionMapper() {
        return this.functionMapper;
    }
    public VariableMapper getVariableMapper() {
        if ( this.variableMapper == null ) {
            this.variableMapper = new VariableMapperImpl();
        }
        return this.variableMapper;
    }
    public void setFunctionMapper ( final FunctionMapper functionMapper ) {
        this.functionMapper = functionMapper;
    }
    public void setVariableMapper ( final VariableMapper variableMapper ) {
        this.variableMapper = variableMapper;
    }
    public static ELResolver getDefaultResolver ( final ExpressionFactory factory ) {
        if ( Constants.IS_SECURITY_ENABLED ) {
            final CompositeELResolver defaultResolver = new CompositeELResolver();
            defaultResolver.add ( factory.getStreamELResolver() );
            defaultResolver.add ( ( ELResolver ) new StaticFieldELResolver() );
            defaultResolver.add ( ( ELResolver ) new MapELResolver() );
            defaultResolver.add ( ( ELResolver ) new ResourceBundleELResolver() );
            defaultResolver.add ( ( ELResolver ) new ListELResolver() );
            defaultResolver.add ( ( ELResolver ) new ArrayELResolver() );
            defaultResolver.add ( ( ELResolver ) new BeanELResolver() );
            return ( ELResolver ) defaultResolver;
        }
        return ELContextImpl.DefaultResolver;
    }
    static {
        NullFunctionMapper = new FunctionMapper() {
            public Method resolveFunction ( final String prefix, final String localName ) {
                return null;
            }
        };
        if ( Constants.IS_SECURITY_ENABLED ) {
            DefaultResolver = null;
        } else {
            DefaultResolver = ( ELResolver ) new CompositeELResolver();
            ( ( CompositeELResolver ) ELContextImpl.DefaultResolver ).add ( ELManager.getExpressionFactory().getStreamELResolver() );
            ( ( CompositeELResolver ) ELContextImpl.DefaultResolver ).add ( ( ELResolver ) new StaticFieldELResolver() );
            ( ( CompositeELResolver ) ELContextImpl.DefaultResolver ).add ( ( ELResolver ) new MapELResolver() );
            ( ( CompositeELResolver ) ELContextImpl.DefaultResolver ).add ( ( ELResolver ) new ResourceBundleELResolver() );
            ( ( CompositeELResolver ) ELContextImpl.DefaultResolver ).add ( ( ELResolver ) new ListELResolver() );
            ( ( CompositeELResolver ) ELContextImpl.DefaultResolver ).add ( ( ELResolver ) new ArrayELResolver() );
            ( ( CompositeELResolver ) ELContextImpl.DefaultResolver ).add ( ( ELResolver ) new BeanELResolver() );
        }
    }
    private static final class VariableMapperImpl extends VariableMapper {
        private Map<String, ValueExpression> vars;
        public ValueExpression resolveVariable ( final String variable ) {
            if ( this.vars == null ) {
                return null;
            }
            return this.vars.get ( variable );
        }
        public ValueExpression setVariable ( final String variable, final ValueExpression expression ) {
            if ( this.vars == null ) {
                this.vars = new HashMap<String, ValueExpression>();
            }
            if ( expression == null ) {
                return this.vars.remove ( variable );
            }
            return this.vars.put ( variable, expression );
        }
    }
}
