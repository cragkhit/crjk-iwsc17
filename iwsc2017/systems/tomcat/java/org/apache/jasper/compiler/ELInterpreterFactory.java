package org.apache.jasper.compiler;
import javax.servlet.ServletContext;
import org.apache.jasper.JspCompilationContext;
public class ELInterpreterFactory {
    public static final String EL_INTERPRETER_CLASS_NAME =
        ELInterpreter.class.getName();
    private static final ELInterpreter DEFAULT_INSTANCE =
        new DefaultELInterpreter();
    public static ELInterpreter getELInterpreter ( ServletContext context )
    throws Exception {
        ELInterpreter result = null;
        Object attribute = context.getAttribute ( EL_INTERPRETER_CLASS_NAME );
        if ( attribute instanceof ELInterpreter ) {
            return ( ELInterpreter ) attribute;
        } else if ( attribute instanceof String ) {
            result = createInstance ( context, ( String ) attribute );
        }
        if ( result == null ) {
            String className =
                context.getInitParameter ( EL_INTERPRETER_CLASS_NAME );
            if ( className != null ) {
                result = createInstance ( context, className );
            }
        }
        if ( result == null ) {
            result = DEFAULT_INSTANCE;
        }
        context.setAttribute ( EL_INTERPRETER_CLASS_NAME, result );
        return result;
    }
    private static ELInterpreter createInstance ( ServletContext context,
            String className ) throws Exception {
        return ( ELInterpreter ) context.getClassLoader().loadClass (
                   className ).newInstance();
    }
    private ELInterpreterFactory() {
    }
    public static class DefaultELInterpreter implements ELInterpreter {
        @Override
        public String interpreterCall ( JspCompilationContext context,
                                        boolean isTagFile, String expression,
                                        Class<?> expectedType, String fnmapvar ) {
            return JspUtil.interpreterCall ( isTagFile, expression, expectedType,
                                             fnmapvar );
        }
    }
}
