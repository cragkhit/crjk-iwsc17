package javax.el;
import java.util.Map;
import java.lang.reflect.Method;
public class ELManager {
    private StandardELContext context;
    public ELManager() {
        this.context = null;
    }
    public static ExpressionFactory getExpressionFactory() {
        return Util.getExpressionFactory();
    }
    public StandardELContext getELContext() {
        if ( this.context == null ) {
            this.context = new StandardELContext ( getExpressionFactory() );
        }
        return this.context;
    }
    public ELContext setELContext ( final ELContext context ) {
        final StandardELContext oldContext = this.context;
        this.context = new StandardELContext ( context );
        return oldContext;
    }
    public void addBeanNameResolver ( final BeanNameResolver beanNameResolver ) {
        this.getELContext().addELResolver ( new BeanNameELResolver ( beanNameResolver ) );
    }
    public void addELResolver ( final ELResolver resolver ) {
        this.getELContext().addELResolver ( resolver );
    }
    public void mapFunction ( final String prefix, final String function, final Method method ) {
        this.getELContext().getFunctionMapper().mapFunction ( prefix, function, method );
    }
    public void setVariable ( final String variable, final ValueExpression expression ) {
        this.getELContext().getVariableMapper().setVariable ( variable, expression );
    }
    public void importStatic ( final String staticMemberName ) throws ELException {
        this.getELContext().getImportHandler().importStatic ( staticMemberName );
    }
    public void importClass ( final String className ) throws ELException {
        this.getELContext().getImportHandler().importClass ( className );
    }
    public void importPackage ( final String packageName ) {
        this.getELContext().getImportHandler().importPackage ( packageName );
    }
    public Object defineBean ( final String name, final Object bean ) {
        final Map<String, Object> localBeans = this.getELContext().getLocalBeans();
        if ( bean == null ) {
            return localBeans.remove ( name );
        }
        return localBeans.put ( name, bean );
    }
    public void addEvaluationListener ( final EvaluationListener listener ) {
        this.getELContext().addEvaluationListener ( listener );
    }
}
