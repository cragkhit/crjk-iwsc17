package javax.el;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Objects;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Locale;
public abstract class ELContext {
    private Locale locale;
    private Map<Class<?>, Object> map;
    private boolean resolved;
    private ImportHandler importHandler;
    private List<EvaluationListener> listeners;
    private Deque<Map<String, Object>> lambdaArguments;
    public ELContext() {
        this.importHandler = null;
        this.listeners = new ArrayList<EvaluationListener>();
        this.lambdaArguments = new LinkedList<Map<String, Object>>();
        this.resolved = false;
    }
    public void setPropertyResolved ( final boolean resolved ) {
        this.resolved = resolved;
    }
    public void setPropertyResolved ( final Object base, final Object property ) {
        this.setPropertyResolved ( true );
        this.notifyPropertyResolved ( base, property );
    }
    public boolean isPropertyResolved() {
        return this.resolved;
    }
    public void putContext ( final Class key, final Object contextObject ) {
        Objects.requireNonNull ( key );
        Objects.requireNonNull ( contextObject );
        if ( this.map == null ) {
            this.map = new HashMap<Class<?>, Object>();
        }
        this.map.put ( key, contextObject );
    }
    public Object getContext ( final Class key ) {
        Objects.requireNonNull ( key );
        if ( this.map == null ) {
            return null;
        }
        return this.map.get ( key );
    }
    public abstract ELResolver getELResolver();
    public ImportHandler getImportHandler() {
        if ( this.importHandler == null ) {
            this.importHandler = new ImportHandler();
        }
        return this.importHandler;
    }
    public abstract FunctionMapper getFunctionMapper();
    public Locale getLocale() {
        return this.locale;
    }
    public void setLocale ( final Locale locale ) {
        this.locale = locale;
    }
    public abstract VariableMapper getVariableMapper();
    public void addEvaluationListener ( final EvaluationListener listener ) {
        this.listeners.add ( listener );
    }
    public List<EvaluationListener> getEvaluationListeners() {
        return this.listeners;
    }
    public void notifyBeforeEvaluation ( final String expression ) {
        for ( final EvaluationListener listener : this.listeners ) {
            try {
                listener.beforeEvaluation ( this, expression );
            } catch ( Throwable t ) {
                Util.handleThrowable ( t );
            }
        }
    }
    public void notifyAfterEvaluation ( final String expression ) {
        for ( final EvaluationListener listener : this.listeners ) {
            try {
                listener.afterEvaluation ( this, expression );
            } catch ( Throwable t ) {
                Util.handleThrowable ( t );
            }
        }
    }
    public void notifyPropertyResolved ( final Object base, final Object property ) {
        for ( final EvaluationListener listener : this.listeners ) {
            try {
                listener.propertyResolved ( this, base, property );
            } catch ( Throwable t ) {
                Util.handleThrowable ( t );
            }
        }
    }
    public boolean isLambdaArgument ( final String name ) {
        for ( final Map<String, Object> arguments : this.lambdaArguments ) {
            if ( arguments.containsKey ( name ) ) {
                return true;
            }
        }
        return false;
    }
    public Object getLambdaArgument ( final String name ) {
        for ( final Map<String, Object> arguments : this.lambdaArguments ) {
            final Object result = arguments.get ( name );
            if ( result != null ) {
                return result;
            }
        }
        return null;
    }
    public void enterLambdaScope ( final Map<String, Object> arguments ) {
        this.lambdaArguments.push ( arguments );
    }
    public void exitLambdaScope() {
        this.lambdaArguments.pop();
    }
    public Object convertToType ( final Object obj, final Class<?> type ) {
        final boolean originalResolved = this.isPropertyResolved();
        this.setPropertyResolved ( false );
        try {
            final ELResolver resolver = this.getELResolver();
            if ( resolver != null ) {
                final Object result = resolver.convertToType ( this, obj, type );
                if ( this.isPropertyResolved() ) {
                    return result;
                }
            }
        } finally {
            this.setPropertyResolved ( originalResolved );
        }
        return ELManager.getExpressionFactory().coerceToType ( obj, type );
    }
}
