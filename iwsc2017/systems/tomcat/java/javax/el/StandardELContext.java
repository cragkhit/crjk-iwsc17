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
    private final Map<String, Object> localBeans = new HashMap<>();
    public StandardELContext ( ExpressionFactory factory ) {
        wrappedContext = null;
        variableMapper = new StandardVariableMapper();
        functionMapper =
            new StandardFunctionMapper ( factory.getInitFunctionMap() );
        standardResolver = new CompositeELResolver();
        customResolvers = new CompositeELResolver();
        ELResolver streamResolver = factory.getStreamELResolver();
        standardResolver.add ( new BeanNameELResolver (
                                   new StandardBeanNameResolver ( localBeans ) ) );
        standardResolver.add ( customResolvers );
        if ( streamResolver != null ) {
            standardResolver.add ( streamResolver );
        }
        standardResolver.add ( new StaticFieldELResolver() );
        standardResolver.add ( new MapELResolver() );
        standardResolver.add ( new ResourceBundleELResolver() );
        standardResolver.add ( new ListELResolver() );
        standardResolver.add ( new ArrayELResolver() );
        standardResolver.add ( new BeanELResolver() );
    }
    public StandardELContext ( ELContext context ) {
        wrappedContext = context;
        variableMapper = context.getVariableMapper();
        functionMapper = context.getFunctionMapper();
        standardResolver = new CompositeELResolver();
        customResolvers = new CompositeELResolver();
        standardResolver.add ( new BeanNameELResolver (
                                   new StandardBeanNameResolver ( localBeans ) ) );
        standardResolver.add ( customResolvers );
        standardResolver.add ( context.getELResolver() );
    }
    @Override
    public void putContext ( @SuppressWarnings ( "rawtypes" ) Class key,
                             Object contextObject ) {
        if ( wrappedContext == null ) {
            super.putContext ( key, contextObject );
        } else {
            wrappedContext.putContext ( key, contextObject );
        }
    }
    @Override
    public Object getContext ( @SuppressWarnings ( "rawtypes" ) Class key ) {
        if ( wrappedContext == null ) {
            return super.getContext ( key );
        } else {
            return wrappedContext.getContext ( key );
        }
    }
    @Override
    public ELResolver getELResolver() {
        return standardResolver;
    }
    public void addELResolver ( ELResolver resolver ) {
        customResolvers.add ( resolver );
    }
    @Override
    public FunctionMapper getFunctionMapper() {
        return functionMapper;
    }
    @Override
    public VariableMapper getVariableMapper() {
        return variableMapper;
    }
    Map<String, Object> getLocalBeans() {
        return localBeans;
    }
    private static class StandardVariableMapper extends VariableMapper {
        private Map<String, ValueExpression> vars;
        @Override
        public ValueExpression resolveVariable ( String variable ) {
            if ( vars == null ) {
                return null;
            }
            return vars.get ( variable );
        }
        @Override
        public ValueExpression setVariable ( String variable,
                                             ValueExpression expression ) {
            if ( vars == null ) {
                vars = new HashMap<>();
            }
            if ( expression == null ) {
                return vars.remove ( variable );
            } else {
                return vars.put ( variable, expression );
            }
        }
    }
    private static class StandardBeanNameResolver extends BeanNameResolver {
        private final Map<String, Object> beans;
        public StandardBeanNameResolver ( Map<String, Object> beans ) {
            this.beans = beans;
        }
        @Override
        public boolean isNameResolved ( String beanName ) {
            return beans.containsKey ( beanName );
        }
        @Override
        public Object getBean ( String beanName ) {
            return beans.get ( beanName );
        }
        @Override
        public void setBeanValue ( String beanName, Object value )
        throws PropertyNotWritableException {
            beans.put ( beanName, value );
        }
        @Override
        public boolean isReadOnly ( String beanName ) {
            return false;
        }
        @Override
        public boolean canCreateBean ( String beanName ) {
            return true;
        }
    }
    private static class StandardFunctionMapper extends FunctionMapper {
        private final Map<String, Method> methods = new HashMap<>();
        public StandardFunctionMapper ( Map<String, Method> initFunctionMap ) {
            if ( initFunctionMap != null ) {
                methods.putAll ( initFunctionMap );
            }
        }
        @Override
        public Method resolveFunction ( String prefix, String localName ) {
            String key = prefix + ':' + localName;
            return methods.get ( key );
        }
        @Override
        public void mapFunction ( String prefix, String localName,
                                  Method method ) {
            String key = prefix + ':' + localName;
            if ( method == null ) {
                methods.remove ( key );
            } else {
                methods.put ( key, method );
            }
        }
    }
}
