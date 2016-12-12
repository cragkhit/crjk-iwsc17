package javax.el;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
public abstract class ELContext {
    private Locale locale;
    private Map<Class<?>, Object> map;
    private boolean resolved;
    private ImportHandler importHandler = null;
    private List<EvaluationListener> listeners = new ArrayList<>();
    private Deque<Map<String, Object>> lambdaArguments = new LinkedList<>();
    public ELContext() {
        this.resolved = false;
    }
    public void setPropertyResolved ( boolean resolved ) {
        this.resolved = resolved;
    }
    public void setPropertyResolved ( Object base, Object property ) {
        setPropertyResolved ( true );
        notifyPropertyResolved ( base, property );
    }
    public boolean isPropertyResolved() {
        return this.resolved;
    }
    public void putContext ( @SuppressWarnings ( "rawtypes" ) Class key,
                             Object contextObject ) {
        Objects.requireNonNull ( key );
        Objects.requireNonNull ( contextObject );
        if ( this.map == null ) {
            this.map = new HashMap<>();
        }
        this.map.put ( key, contextObject );
    }
    public Object getContext ( @SuppressWarnings ( "rawtypes" ) Class key ) {
        Objects.requireNonNull ( key );
        if ( this.map == null ) {
            return null;
        }
        return this.map.get ( key );
    }
    public abstract ELResolver getELResolver();
    public ImportHandler getImportHandler() {
        if ( importHandler == null ) {
            importHandler = new ImportHandler();
        }
        return importHandler;
    }
    public abstract FunctionMapper getFunctionMapper();
    public Locale getLocale() {
        return this.locale;
    }
    public void setLocale ( Locale locale ) {
        this.locale = locale;
    }
    public abstract VariableMapper getVariableMapper();
    public void addEvaluationListener ( EvaluationListener listener ) {
        listeners.add ( listener );
    }
    public List<EvaluationListener> getEvaluationListeners() {
        return listeners;
    }
    public void notifyBeforeEvaluation ( String expression ) {
        for ( EvaluationListener listener : listeners ) {
            try {
                listener.beforeEvaluation ( this, expression );
            } catch ( Throwable t ) {
                Util.handleThrowable ( t );
            }
        }
    }
    public void notifyAfterEvaluation ( String expression ) {
        for ( EvaluationListener listener : listeners ) {
            try {
                listener.afterEvaluation ( this, expression );
            } catch ( Throwable t ) {
                Util.handleThrowable ( t );
            }
        }
    }
    public void notifyPropertyResolved ( Object base, Object property ) {
        for ( EvaluationListener listener : listeners ) {
            try {
                listener.propertyResolved ( this, base, property );
            } catch ( Throwable t ) {
                Util.handleThrowable ( t );
            }
        }
    }
    public boolean isLambdaArgument ( String name ) {
        for ( Map<String, Object> arguments : lambdaArguments ) {
            if ( arguments.containsKey ( name ) ) {
                return true;
            }
        }
        return false;
    }
    public Object getLambdaArgument ( String name ) {
        for ( Map<String, Object> arguments : lambdaArguments ) {
            Object result = arguments.get ( name );
            if ( result != null ) {
                return result;
            }
        }
        return null;
    }
    public void enterLambdaScope ( Map<String, Object> arguments ) {
        lambdaArguments.push ( arguments );
    }
    public void exitLambdaScope() {
        lambdaArguments.pop();
    }
    public Object convertToType ( Object obj, Class<?> type ) {
        boolean originalResolved = isPropertyResolved();
        setPropertyResolved ( false );
        try {
            ELResolver resolver = getELResolver();
            if ( resolver != null ) {
                Object result = resolver.convertToType ( this, obj, type );
                if ( isPropertyResolved() ) {
                    return result;
                }
            }
        } finally {
            setPropertyResolved ( originalResolved );
        }
        return ELManager.getExpressionFactory().coerceToType ( obj, type );
    }
}
