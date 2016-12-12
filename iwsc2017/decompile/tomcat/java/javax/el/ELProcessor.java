package javax.el;
import java.util.HashSet;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;
public class ELProcessor {
    private static final Set<String> PRIMITIVES;
    private static final String[] EMPTY_STRING_ARRAY;
    private final ELManager manager;
    private final ELContext context;
    private final ExpressionFactory factory;
    public ELProcessor() {
        this.manager = new ELManager();
        this.context = this.manager.getELContext();
        this.factory = ELManager.getExpressionFactory();
    }
    public ELManager getELManager() {
        return this.manager;
    }
    public Object eval ( final String expression ) {
        return this.getValue ( expression, Object.class );
    }
    public Object getValue ( final String expression, final Class<?> expectedType ) {
        final ValueExpression ve = this.factory.createValueExpression ( this.context, bracket ( expression ), expectedType );
        return ve.getValue ( this.context );
    }
    public void setValue ( final String expression, final Object value ) {
        final ValueExpression ve = this.factory.createValueExpression ( this.context, bracket ( expression ), Object.class );
        ve.setValue ( this.context, value );
    }
    public void setVariable ( final String variable, final String expression ) {
        if ( expression == null ) {
            this.manager.setVariable ( variable, null );
        } else {
            final ValueExpression ve = this.factory.createValueExpression ( this.context, bracket ( expression ), Object.class );
            this.manager.setVariable ( variable, ve );
        }
    }
    public void defineFunction ( final String prefix, String function, final String className, final String methodName ) throws ClassNotFoundException, NoSuchMethodException {
        if ( prefix == null || function == null || className == null || methodName == null ) {
            throw new NullPointerException ( Util.message ( this.context, "elProcessor.defineFunctionNullParams", new Object[0] ) );
        }
        Class<?> clazz = this.context.getImportHandler().resolveClass ( className );
        if ( clazz == null ) {
            clazz = Class.forName ( className, true, Thread.currentThread().getContextClassLoader() );
        }
        if ( !Modifier.isPublic ( clazz.getModifiers() ) ) {
            throw new ClassNotFoundException ( Util.message ( this.context, "elProcessor.defineFunctionInvalidClass", className ) );
        }
        final MethodSignature sig = new MethodSignature ( this.context, methodName, className );
        if ( function.length() == 0 ) {
            function = sig.getName();
        }
        final Method[] methods2;
        final Method[] methods = methods2 = clazz.getMethods();
        for ( final Method method : methods2 ) {
            if ( Modifier.isStatic ( method.getModifiers() ) ) {
                if ( method.getName().equals ( sig.getName() ) ) {
                    if ( sig.getParamTypeNames() == null ) {
                        this.manager.mapFunction ( prefix, function, method );
                        return;
                    }
                    if ( sig.getParamTypeNames().length == method.getParameterTypes().length ) {
                        if ( sig.getParamTypeNames().length == 0 ) {
                            this.manager.mapFunction ( prefix, function, method );
                            return;
                        }
                        final Class<?>[] types = method.getParameterTypes();
                        final String[] typeNames = sig.getParamTypeNames();
                        if ( types.length == typeNames.length ) {
                            boolean match = true;
                            for ( int i = 0; i < types.length; ++i ) {
                                if ( i == types.length - 1 && method.isVarArgs() ) {
                                    String typeName = typeNames[i];
                                    if ( typeName.endsWith ( "..." ) ) {
                                        typeName = typeName.substring ( 0, typeName.length() - 3 );
                                        if ( !typeName.equals ( types[i].getName() ) ) {
                                            match = false;
                                        }
                                    } else {
                                        match = false;
                                    }
                                } else if ( !types[i].getName().equals ( typeNames[i] ) ) {
                                    match = false;
                                    break;
                                }
                            }
                            if ( match ) {
                                this.manager.mapFunction ( prefix, function, method );
                                return;
                            }
                        }
                    }
                }
            }
        }
        throw new NoSuchMethodException ( Util.message ( this.context, "elProcessor.defineFunctionNoMethod", methodName, className ) );
    }
    public void defineFunction ( final String prefix, final String function, final Method method ) throws NoSuchMethodException {
        if ( prefix == null || function == null || method == null ) {
            throw new NullPointerException ( Util.message ( this.context, "elProcessor.defineFunctionNullParams", new Object[0] ) );
        }
        final int modifiers = method.getModifiers();
        if ( !Modifier.isStatic ( modifiers ) || !Modifier.isPublic ( modifiers ) ) {
            throw new NoSuchMethodException ( Util.message ( this.context, "elProcessor.defineFunctionInvalidMethod", method.getName(), method.getDeclaringClass().getName() ) );
        }
        this.manager.mapFunction ( prefix, function, method );
    }
    public void defineBean ( final String name, final Object bean ) {
        this.manager.defineBean ( name, bean );
    }
    private static String bracket ( final String expression ) {
        return "${" + expression + "}";
    }
    static {
        ( PRIMITIVES = new HashSet<String>() ).add ( "boolean" );
        ELProcessor.PRIMITIVES.add ( "byte" );
        ELProcessor.PRIMITIVES.add ( "char" );
        ELProcessor.PRIMITIVES.add ( "double" );
        ELProcessor.PRIMITIVES.add ( "float" );
        ELProcessor.PRIMITIVES.add ( "int" );
        ELProcessor.PRIMITIVES.add ( "long" );
        ELProcessor.PRIMITIVES.add ( "short" );
        EMPTY_STRING_ARRAY = new String[0];
    }
    private static class MethodSignature {
        private final String name;
        private final String[] parameterTypeNames;
        public MethodSignature ( final ELContext context, final String methodName, final String className ) throws NoSuchMethodException {
            final int paramIndex = methodName.indexOf ( 40 );
            if ( paramIndex == -1 ) {
                this.name = methodName.trim();
                this.parameterTypeNames = null;
            } else {
                final String returnTypeAndName = methodName.substring ( 0, paramIndex ).trim();
                int wsPos = -1;
                for ( int i = 0; i < returnTypeAndName.length(); ++i ) {
                    if ( Character.isWhitespace ( returnTypeAndName.charAt ( i ) ) ) {
                        wsPos = i;
                        break;
                    }
                }
                if ( wsPos == -1 ) {
                    throw new NoSuchMethodException();
                }
                this.name = returnTypeAndName.substring ( wsPos ).trim();
                String paramString = methodName.substring ( paramIndex ).trim();
                if ( !paramString.endsWith ( ")" ) ) {
                    throw new NoSuchMethodException ( Util.message ( context, "elProcessor.defineFunctionInvalidParameterList", paramString, methodName, className ) );
                }
                paramString = paramString.substring ( 1, paramString.length() - 1 ).trim();
                if ( paramString.length() == 0 ) {
                    this.parameterTypeNames = ELProcessor.EMPTY_STRING_ARRAY;
                } else {
                    this.parameterTypeNames = paramString.split ( "," );
                    final ImportHandler importHandler = context.getImportHandler();
                    for ( int j = 0; j < this.parameterTypeNames.length; ++j ) {
                        String parameterTypeName = this.parameterTypeNames[j].trim();
                        int dimension = 0;
                        int bracketPos = parameterTypeName.indexOf ( 91 );
                        if ( bracketPos > -1 ) {
                            final String parameterTypeNameOnly = parameterTypeName.substring ( 0, bracketPos ).trim();
                            while ( bracketPos > -1 ) {
                                ++dimension;
                                bracketPos = parameterTypeName.indexOf ( 91, bracketPos + 1 );
                            }
                            parameterTypeName = parameterTypeNameOnly;
                        }
                        boolean varArgs = false;
                        if ( parameterTypeName.endsWith ( "..." ) ) {
                            varArgs = true;
                            dimension = 1;
                            parameterTypeName = parameterTypeName.substring ( 0, parameterTypeName.length() - 3 ).trim();
                        }
                        final boolean isPrimitive = ELProcessor.PRIMITIVES.contains ( parameterTypeName );
                        if ( isPrimitive && dimension > 0 ) {
                            final String s = parameterTypeName;
                            switch ( s ) {
                            case "boolean": {
                                parameterTypeName = "Z";
                                break;
                            }
                            case "byte": {
                                parameterTypeName = "B";
                                break;
                            }
                            case "char": {
                                parameterTypeName = "C";
                                break;
                            }
                            case "double": {
                                parameterTypeName = "D";
                                break;
                            }
                            case "float": {
                                parameterTypeName = "F";
                                break;
                            }
                            case "int": {
                                parameterTypeName = "I";
                                break;
                            }
                            case "long": {
                                parameterTypeName = "J";
                                break;
                            }
                            case "short": {
                                parameterTypeName = "S";
                                break;
                            }
                            }
                        } else if ( !isPrimitive && !parameterTypeName.contains ( "." ) ) {
                            final Class<?> clazz = importHandler.resolveClass ( parameterTypeName );
                            if ( clazz == null ) {
                                throw new NoSuchMethodException ( Util.message ( context, "elProcessor.defineFunctionInvalidParameterTypeName", this.parameterTypeNames[j], methodName, className ) );
                            }
                            parameterTypeName = clazz.getName();
                        }
                        if ( dimension > 0 ) {
                            final StringBuilder sb = new StringBuilder();
                            for ( int k = 0; k < dimension; ++k ) {
                                sb.append ( '[' );
                            }
                            if ( !isPrimitive ) {
                                sb.append ( 'L' );
                            }
                            sb.append ( parameterTypeName );
                            if ( !isPrimitive ) {
                                sb.append ( ';' );
                            }
                            parameterTypeName = sb.toString();
                        }
                        if ( varArgs ) {
                            parameterTypeName += "...";
                        }
                        this.parameterTypeNames[j] = parameterTypeName;
                    }
                }
            }
        }
        public String getName() {
            return this.name;
        }
        public String[] getParamTypeNames() {
            return this.parameterTypeNames;
        }
    }
}
