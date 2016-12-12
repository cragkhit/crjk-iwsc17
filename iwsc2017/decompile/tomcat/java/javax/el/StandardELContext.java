package javax.el;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
public class StandardELContext extends ELContext {
    private final ELContext wrappedContext;
    private final VariableMapper variableMapper;
    private final FunctionMapper functionMapper;
    private final CompositeELResolver standardResolver;
    private final CompositeELResolver customResolvers;
    private final Map<String, Object> localBeans;
    public StandardELContext ( final ExpressionFactory factory ) {
        this.localBeans = new HashMap<String, Object>();
        this.wrappedContext = null;
        this.variableMapper = new StandardVariableMapper();
        this.functionMapper = new StandardFunctionMapper ( factory.getInitFunctionMap() );
        this.standardResolver = new CompositeELResolver();
        this.customResolvers = new CompositeELResolver();
        final ELResolver streamResolver = factory.getStreamELResolver();
        this.standardResolver.add ( new BeanNameELResolver ( new StandardBeanNameResolver ( this.localBeans ) ) );
        this.standardResolver.add ( this.customResolvers );
        if ( streamResolver != null ) {
            this.standardResolver.add ( streamResolver );
        }
        this.standardResolver.add ( new StaticFieldELResolver() );
        this.standardResolver.add ( new MapELResolver() );
        this.standardResolver.add ( new ResourceBundleELResolver() );
        this.standardResolver.add ( new ListELResolver() );
        this.standardResolver.add ( new ArrayELResolver() );
        this.standardResolver.add ( new BeanELResolver() );
    }
    public StandardELContext ( final ELContext context ) {
        this.localBeans = new HashMap<String, Object>();
        this.wrappedContext = context;
        this.variableMapper = context.getVariableMapper();
        this.functionMapper = context.getFunctionMapper();
        this.standardResolver = new CompositeELResolver();
        this.customResolvers = new CompositeELResolver();
        this.standardResolver.add ( new BeanNameELResolver ( new StandardBeanNameResolver ( this.localBeans ) ) );
        this.standardResolver.add ( this.customResolvers );
        this.standardResolver.add ( context.getELResolver() );
    }
    @Override
    public void putContext ( final Class key, final Object contextObject ) {
        if ( this.wrappedContext == null ) {
            super.putContext ( key, contextObject );
        } else {
            this.wrappedContext.putContext ( key, contextObject );
        }
    }
    @Override
    public Object getContext ( final Class key ) {
        if ( this.wrappedContext == null ) {
            return super.getContext ( key );
        }
        return this.wrappedContext.getContext ( key );
    }
    @Override
    public ELResolver getELResolver() {
        return this.standardResolver;
    }
    public void addELResolver ( final ELResolver resolver ) {
        this.customResolvers.add ( resolver );
    }
    @Override
    public FunctionMapper getFunctionMapper() {
        return this.functionMapper;
    }
    @Override
    public VariableMapper getVariableMapper() {
        return this.variableMapper;
    }
    Map<String, Object> getLocalBeans() {
        return this.localBeans;
    }
    private static class StandardVariableMapper extends VariableMapper {
        private Map<String, ValueExpression> vars;
        @Override
        public ValueExpression resolveVariable ( final String variable ) {
            if ( this.vars == null ) {
                return null;
            }
            return this.vars.get ( variable );
        }
        @Override
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
    private static class StandardBeanNameResolver extends BeanNameResolver {
        private final Map<String, Object> beans;
        public StandardBeanNameResolver ( final Map<String, Object> beans ) {
            this.beans = beans;
        }
        @Override
        public boolean isNameResolved ( final String beanName ) {
            return this.beans.containsKey ( beanName );
        }
        @Override
        public Object getBean ( final String beanName ) {
            return this.beans.get ( beanName );
        }
        @Override
        public void setBeanValue ( final String beanName, final Object value ) throws PropertyNotWritableException {
            this.beans.put ( beanName, value );
        }
        @Override
        public boolean isReadOnly ( final String beanName ) {
            return false;
        }
        @Override
        public boolean canCreateBean ( final String beanName ) {
            return true;
        }
    }
    private static class StandardFunctionMapper extends FunctionMapper {
        private final Map<String, Method> methods;
        public StandardFunctionMapper ( final Map<String, Method> initFunctionMap ) {
            this.methods = new HashMap<String, Method>();
            if ( initFunctionMap != null ) {
                this.methods.putAll ( initFunctionMap );
            }
        }
        @Override
        public Method resolveFunction ( final String prefix, final String localName ) {
            final String key = prefix + ':' + localName;
            return this.methods.get ( key );
        }
        @Override
        public void mapFunction ( final String prefix, final String localName, final Method method ) {
            final String key = prefix + ':' + localName;
            if ( method == null ) {
                this.methods.remove ( key );
            } else {
                this.methods.put ( key, method );
            }
        }
    }
}
