package org.apache.jasper.runtime;
import javax.el.VariableMapper;
import javax.el.FunctionMapper;
import javax.el.ELResolver;
import java.util.Map;
import java.util.List;
import javax.el.EvaluationListener;
import java.util.Locale;
import java.util.Iterator;
import java.util.Set;
import javax.servlet.jsp.JspContext;
import javax.el.ImportHandler;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.JspTag;
import javax.el.ELContext;
static class ELContextWrapper extends ELContext {
    private final ELContext wrapped;
    private final JspTag jspTag;
    private final PageContext pageContext;
    private ImportHandler importHandler;
    private ELContextWrapper ( final ELContext wrapped, final JspTag jspTag, final PageContext pageContext ) {
        this.wrapped = wrapped;
        this.jspTag = jspTag;
        this.pageContext = pageContext;
    }
    ELContext getWrappedELContext() {
        return this.wrapped;
    }
    public void setPropertyResolved ( final boolean resolved ) {
        this.wrapped.setPropertyResolved ( resolved );
    }
    public void setPropertyResolved ( final Object base, final Object property ) {
        this.wrapped.setPropertyResolved ( base, property );
    }
    public boolean isPropertyResolved() {
        return this.wrapped.isPropertyResolved();
    }
    public void putContext ( final Class key, final Object contextObject ) {
        this.wrapped.putContext ( key, contextObject );
    }
    public Object getContext ( final Class key ) {
        if ( key == JspContext.class ) {
            return this.pageContext;
        }
        return this.wrapped.getContext ( key );
    }
    public ImportHandler getImportHandler() {
        if ( this.importHandler == null ) {
            this.importHandler = new ImportHandler();
            if ( this.jspTag instanceof JspSourceImports ) {
                final Set<String> packageImports = ( ( JspSourceImports ) this.jspTag ).getPackageImports();
                if ( packageImports != null ) {
                    for ( final String packageImport : packageImports ) {
                        this.importHandler.importPackage ( packageImport );
                    }
                }
                final Set<String> classImports = ( ( JspSourceImports ) this.jspTag ).getClassImports();
                if ( classImports != null ) {
                    for ( final String classImport : classImports ) {
                        this.importHandler.importClass ( classImport );
                    }
                }
            }
        }
        return this.importHandler;
    }
    public Locale getLocale() {
        return this.wrapped.getLocale();
    }
    public void setLocale ( final Locale locale ) {
        this.wrapped.setLocale ( locale );
    }
    public void addEvaluationListener ( final EvaluationListener listener ) {
        this.wrapped.addEvaluationListener ( listener );
    }
    public List<EvaluationListener> getEvaluationListeners() {
        return ( List<EvaluationListener> ) this.wrapped.getEvaluationListeners();
    }
    public void notifyBeforeEvaluation ( final String expression ) {
        this.wrapped.notifyBeforeEvaluation ( expression );
    }
    public void notifyAfterEvaluation ( final String expression ) {
        this.wrapped.notifyAfterEvaluation ( expression );
    }
    public void notifyPropertyResolved ( final Object base, final Object property ) {
        this.wrapped.notifyPropertyResolved ( base, property );
    }
    public boolean isLambdaArgument ( final String name ) {
        return this.wrapped.isLambdaArgument ( name );
    }
    public Object getLambdaArgument ( final String name ) {
        return this.wrapped.getLambdaArgument ( name );
    }
    public void enterLambdaScope ( final Map<String, Object> arguments ) {
        this.wrapped.enterLambdaScope ( ( Map ) arguments );
    }
    public void exitLambdaScope() {
        this.wrapped.exitLambdaScope();
    }
    public Object convertToType ( final Object obj, final Class<?> type ) {
        return this.wrapped.convertToType ( obj, ( Class ) type );
    }
    public ELResolver getELResolver() {
        return this.wrapped.getELResolver();
    }
    public FunctionMapper getFunctionMapper() {
        return this.wrapped.getFunctionMapper();
    }
    public VariableMapper getVariableMapper() {
        return this.wrapped.getVariableMapper();
    }
}
