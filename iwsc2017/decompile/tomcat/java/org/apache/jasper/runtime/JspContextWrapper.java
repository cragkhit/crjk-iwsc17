package org.apache.jasper.runtime;
import javax.el.VariableMapper;
import javax.el.FunctionMapper;
import javax.el.ELResolver;
import java.util.List;
import javax.el.EvaluationListener;
import java.util.Locale;
import java.util.Set;
import javax.el.ImportHandler;
import java.util.Iterator;
import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.ExpressionEvaluator;
import java.io.Writer;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import org.apache.jasper.compiler.Localizer;
import java.io.IOException;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;
import javax.servlet.Servlet;
import javax.servlet.jsp.JspContext;
import javax.el.ELContext;
import javax.servlet.ServletContext;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.jsp.tagext.JspTag;
import javax.servlet.jsp.el.VariableResolver;
import javax.servlet.jsp.PageContext;
public class JspContextWrapper extends PageContext implements VariableResolver {
    private final JspTag jspTag;
    private final PageContext invokingJspCtxt;
    private final transient HashMap<String, Object> pageAttributes;
    private final ArrayList<String> nestedVars;
    private final ArrayList<String> atBeginVars;
    private final ArrayList<String> atEndVars;
    private final Map<String, String> aliases;
    private final HashMap<String, Object> originalNestedVars;
    private ServletContext servletContext;
    private ELContext elContext;
    private final PageContext rootJspCtxt;
    public JspContextWrapper ( final JspTag jspTag, final JspContext jspContext, final ArrayList<String> nestedVars, final ArrayList<String> atBeginVars, final ArrayList<String> atEndVars, final Map<String, String> aliases ) {
        this.servletContext = null;
        this.elContext = null;
        this.jspTag = jspTag;
        this.invokingJspCtxt = ( PageContext ) jspContext;
        if ( jspContext instanceof JspContextWrapper ) {
            this.rootJspCtxt = ( ( JspContextWrapper ) jspContext ).rootJspCtxt;
        } else {
            this.rootJspCtxt = this.invokingJspCtxt;
        }
        this.nestedVars = nestedVars;
        this.atBeginVars = atBeginVars;
        this.atEndVars = atEndVars;
        this.pageAttributes = new HashMap<String, Object> ( 16 );
        this.aliases = aliases;
        if ( nestedVars != null ) {
            this.originalNestedVars = new HashMap<String, Object> ( nestedVars.size() );
        } else {
            this.originalNestedVars = null;
        }
        this.syncBeginTagFile();
    }
    public void initialize ( final Servlet servlet, final ServletRequest request, final ServletResponse response, final String errorPageURL, final boolean needsSession, final int bufferSize, final boolean autoFlush ) throws IOException, IllegalStateException, IllegalArgumentException {
    }
    public Object getAttribute ( final String name ) {
        if ( name == null ) {
            throw new NullPointerException ( Localizer.getMessage ( "jsp.error.attribute.null_name" ) );
        }
        return this.pageAttributes.get ( name );
    }
    public Object getAttribute ( final String name, final int scope ) {
        if ( name == null ) {
            throw new NullPointerException ( Localizer.getMessage ( "jsp.error.attribute.null_name" ) );
        }
        if ( scope == 1 ) {
            return this.pageAttributes.get ( name );
        }
        return this.rootJspCtxt.getAttribute ( name, scope );
    }
    public void setAttribute ( final String name, final Object value ) {
        if ( name == null ) {
            throw new NullPointerException ( Localizer.getMessage ( "jsp.error.attribute.null_name" ) );
        }
        if ( value != null ) {
            this.pageAttributes.put ( name, value );
        } else {
            this.removeAttribute ( name, 1 );
        }
    }
    public void setAttribute ( final String name, final Object value, final int scope ) {
        if ( name == null ) {
            throw new NullPointerException ( Localizer.getMessage ( "jsp.error.attribute.null_name" ) );
        }
        if ( scope == 1 ) {
            if ( value != null ) {
                this.pageAttributes.put ( name, value );
            } else {
                this.removeAttribute ( name, 1 );
            }
        } else {
            this.rootJspCtxt.setAttribute ( name, value, scope );
        }
    }
    public Object findAttribute ( final String name ) {
        if ( name == null ) {
            throw new NullPointerException ( Localizer.getMessage ( "jsp.error.attribute.null_name" ) );
        }
        Object o = this.pageAttributes.get ( name );
        if ( o == null ) {
            o = this.rootJspCtxt.getAttribute ( name, 2 );
            if ( o == null ) {
                if ( this.getSession() != null ) {
                    o = this.rootJspCtxt.getAttribute ( name, 3 );
                }
                if ( o == null ) {
                    o = this.rootJspCtxt.getAttribute ( name, 4 );
                }
            }
        }
        return o;
    }
    public void removeAttribute ( final String name ) {
        if ( name == null ) {
            throw new NullPointerException ( Localizer.getMessage ( "jsp.error.attribute.null_name" ) );
        }
        this.pageAttributes.remove ( name );
        this.rootJspCtxt.removeAttribute ( name, 2 );
        if ( this.getSession() != null ) {
            this.rootJspCtxt.removeAttribute ( name, 3 );
        }
        this.rootJspCtxt.removeAttribute ( name, 4 );
    }
    public void removeAttribute ( final String name, final int scope ) {
        if ( name == null ) {
            throw new NullPointerException ( Localizer.getMessage ( "jsp.error.attribute.null_name" ) );
        }
        if ( scope == 1 ) {
            this.pageAttributes.remove ( name );
        } else {
            this.rootJspCtxt.removeAttribute ( name, scope );
        }
    }
    public int getAttributesScope ( final String name ) {
        if ( name == null ) {
            throw new NullPointerException ( Localizer.getMessage ( "jsp.error.attribute.null_name" ) );
        }
        if ( this.pageAttributes.get ( name ) != null ) {
            return 1;
        }
        return this.rootJspCtxt.getAttributesScope ( name );
    }
    public Enumeration<String> getAttributeNamesInScope ( final int scope ) {
        if ( scope == 1 ) {
            return Collections.enumeration ( this.pageAttributes.keySet() );
        }
        return ( Enumeration<String> ) this.rootJspCtxt.getAttributeNamesInScope ( scope );
    }
    public void release() {
        this.invokingJspCtxt.release();
    }
    public JspWriter getOut() {
        return this.rootJspCtxt.getOut();
    }
    public HttpSession getSession() {
        return this.rootJspCtxt.getSession();
    }
    public Object getPage() {
        return this.invokingJspCtxt.getPage();
    }
    public ServletRequest getRequest() {
        return this.invokingJspCtxt.getRequest();
    }
    public ServletResponse getResponse() {
        return this.rootJspCtxt.getResponse();
    }
    public Exception getException() {
        return this.invokingJspCtxt.getException();
    }
    public ServletConfig getServletConfig() {
        return this.invokingJspCtxt.getServletConfig();
    }
    public ServletContext getServletContext() {
        if ( this.servletContext == null ) {
            this.servletContext = this.rootJspCtxt.getServletContext();
        }
        return this.servletContext;
    }
    public void forward ( final String relativeUrlPath ) throws ServletException, IOException {
        this.invokingJspCtxt.forward ( relativeUrlPath );
    }
    public void include ( final String relativeUrlPath ) throws ServletException, IOException {
        this.invokingJspCtxt.include ( relativeUrlPath );
    }
    public void include ( final String relativeUrlPath, final boolean flush ) throws ServletException, IOException {
        this.invokingJspCtxt.include ( relativeUrlPath, false );
    }
    @Deprecated
    public VariableResolver getVariableResolver() {
        return ( VariableResolver ) this;
    }
    public BodyContent pushBody() {
        return this.invokingJspCtxt.pushBody();
    }
    public JspWriter pushBody ( final Writer writer ) {
        return this.invokingJspCtxt.pushBody ( writer );
    }
    public JspWriter popBody() {
        return this.invokingJspCtxt.popBody();
    }
    @Deprecated
    public ExpressionEvaluator getExpressionEvaluator() {
        return this.invokingJspCtxt.getExpressionEvaluator();
    }
    public void handlePageException ( final Exception ex ) throws IOException, ServletException {
        this.handlePageException ( ( Throwable ) ex );
    }
    public void handlePageException ( final Throwable t ) throws IOException, ServletException {
        this.invokingJspCtxt.handlePageException ( t );
    }
    @Deprecated
    public Object resolveVariable ( final String pName ) throws ELException {
        final ELContext ctx = this.getELContext();
        return ctx.getELResolver().getValue ( ctx, ( Object ) null, ( Object ) pName );
    }
    public void syncBeginTagFile() {
        this.saveNestedVariables();
    }
    public void syncBeforeInvoke() {
        this.copyTagToPageScope ( 0 );
        this.copyTagToPageScope ( 1 );
    }
    public void syncEndTagFile() {
        this.copyTagToPageScope ( 1 );
        this.copyTagToPageScope ( 2 );
        this.restoreNestedVariables();
    }
    private void copyTagToPageScope ( final int scope ) {
        Iterator<String> iter = null;
        switch ( scope ) {
        case 0: {
            if ( this.nestedVars != null ) {
                iter = this.nestedVars.iterator();
                break;
            }
            break;
        }
        case 1: {
            if ( this.atBeginVars != null ) {
                iter = this.atBeginVars.iterator();
                break;
            }
            break;
        }
        case 2: {
            if ( this.atEndVars != null ) {
                iter = this.atEndVars.iterator();
                break;
            }
            break;
        }
        }
        while ( iter != null && iter.hasNext() ) {
            String varName = iter.next();
            final Object obj = this.getAttribute ( varName );
            varName = this.findAlias ( varName );
            if ( obj != null ) {
                this.invokingJspCtxt.setAttribute ( varName, obj );
            } else {
                this.invokingJspCtxt.removeAttribute ( varName, 1 );
            }
        }
    }
    private void saveNestedVariables() {
        if ( this.nestedVars != null ) {
            for ( String varName : this.nestedVars ) {
                varName = this.findAlias ( varName );
                final Object obj = this.invokingJspCtxt.getAttribute ( varName );
                if ( obj != null ) {
                    this.originalNestedVars.put ( varName, obj );
                }
            }
        }
    }
    private void restoreNestedVariables() {
        if ( this.nestedVars != null ) {
            for ( String varName : this.nestedVars ) {
                varName = this.findAlias ( varName );
                final Object obj = this.originalNestedVars.get ( varName );
                if ( obj != null ) {
                    this.invokingJspCtxt.setAttribute ( varName, obj );
                } else {
                    this.invokingJspCtxt.removeAttribute ( varName, 1 );
                }
            }
        }
    }
    private String findAlias ( final String varName ) {
        if ( this.aliases == null ) {
            return varName;
        }
        final String alias = this.aliases.get ( varName );
        if ( alias == null ) {
            return varName;
        }
        return alias;
    }
    public ELContext getELContext() {
        if ( this.elContext == null ) {
            this.elContext = new ELContextWrapper ( this.rootJspCtxt.getELContext(), this.jspTag, ( PageContext ) this );
        }
        return this.elContext;
    }
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
}
