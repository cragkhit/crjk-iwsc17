package org.apache.el.lang;
import java.util.Map;
import java.util.List;
import javax.el.EvaluationListener;
import javax.el.ImportHandler;
import java.util.Locale;
import javax.el.ELResolver;
import javax.el.VariableMapper;
import javax.el.FunctionMapper;
import javax.el.ELContext;
public final class EvaluationContext extends ELContext {
    private final ELContext elContext;
    private final FunctionMapper fnMapper;
    private final VariableMapper varMapper;
    public EvaluationContext ( final ELContext elContext, final FunctionMapper fnMapper, final VariableMapper varMapper ) {
        this.elContext = elContext;
        this.fnMapper = fnMapper;
        this.varMapper = varMapper;
    }
    public ELContext getELContext() {
        return this.elContext;
    }
    public FunctionMapper getFunctionMapper() {
        return this.fnMapper;
    }
    public VariableMapper getVariableMapper() {
        return this.varMapper;
    }
    public Object getContext ( final Class key ) {
        return this.elContext.getContext ( key );
    }
    public ELResolver getELResolver() {
        return this.elContext.getELResolver();
    }
    public boolean isPropertyResolved() {
        return this.elContext.isPropertyResolved();
    }
    public void putContext ( final Class key, final Object contextObject ) {
        this.elContext.putContext ( key, contextObject );
    }
    public void setPropertyResolved ( final boolean resolved ) {
        this.elContext.setPropertyResolved ( resolved );
    }
    public Locale getLocale() {
        return this.elContext.getLocale();
    }
    public void setLocale ( final Locale locale ) {
        this.elContext.setLocale ( locale );
    }
    public void setPropertyResolved ( final Object base, final Object property ) {
        this.elContext.setPropertyResolved ( base, property );
    }
    public ImportHandler getImportHandler() {
        return this.elContext.getImportHandler();
    }
    public void addEvaluationListener ( final EvaluationListener listener ) {
        this.elContext.addEvaluationListener ( listener );
    }
    public List<EvaluationListener> getEvaluationListeners() {
        return ( List<EvaluationListener> ) this.elContext.getEvaluationListeners();
    }
    public void notifyBeforeEvaluation ( final String expression ) {
        this.elContext.notifyBeforeEvaluation ( expression );
    }
    public void notifyAfterEvaluation ( final String expression ) {
        this.elContext.notifyAfterEvaluation ( expression );
    }
    public void notifyPropertyResolved ( final Object base, final Object property ) {
        this.elContext.notifyPropertyResolved ( base, property );
    }
    public boolean isLambdaArgument ( final String name ) {
        return this.elContext.isLambdaArgument ( name );
    }
    public Object getLambdaArgument ( final String name ) {
        return this.elContext.getLambdaArgument ( name );
    }
    public void enterLambdaScope ( final Map<String, Object> arguments ) {
        this.elContext.enterLambdaScope ( ( Map ) arguments );
    }
    public void exitLambdaScope() {
        this.elContext.exitLambdaScope();
    }
    public Object convertToType ( final Object obj, final Class<?> type ) {
        return this.elContext.convertToType ( obj, ( Class ) type );
    }
}
