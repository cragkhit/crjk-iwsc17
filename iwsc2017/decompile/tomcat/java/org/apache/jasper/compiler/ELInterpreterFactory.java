package org.apache.jasper.compiler;
import org.apache.jasper.JspCompilationContext;
import javax.servlet.ServletContext;
public class ELInterpreterFactory {
    public static final String EL_INTERPRETER_CLASS_NAME;
    private static final ELInterpreter DEFAULT_INSTANCE;
    public static ELInterpreter getELInterpreter ( final ServletContext context ) throws Exception {
        ELInterpreter result = null;
        final Object attribute = context.getAttribute ( ELInterpreterFactory.EL_INTERPRETER_CLASS_NAME );
        if ( attribute instanceof ELInterpreter ) {
            return ( ELInterpreter ) attribute;
        }
        if ( attribute instanceof String ) {
            result = createInstance ( context, ( String ) attribute );
        }
        if ( result == null ) {
            final String className = context.getInitParameter ( ELInterpreterFactory.EL_INTERPRETER_CLASS_NAME );
            if ( className != null ) {
                result = createInstance ( context, className );
            }
        }
        if ( result == null ) {
            result = ELInterpreterFactory.DEFAULT_INSTANCE;
        }
        context.setAttribute ( ELInterpreterFactory.EL_INTERPRETER_CLASS_NAME, ( Object ) result );
        return result;
    }
    private static ELInterpreter createInstance ( final ServletContext context, final String className ) throws Exception {
        return ( ELInterpreter ) context.getClassLoader().loadClass ( className ).newInstance();
    }
    static {
        EL_INTERPRETER_CLASS_NAME = ELInterpreter.class.getName();
        DEFAULT_INSTANCE = new DefaultELInterpreter();
    }
    public static class DefaultELInterpreter implements ELInterpreter {
        @Override
        public String interpreterCall ( final JspCompilationContext context, final boolean isTagFile, final String expression, final Class<?> expectedType, final String fnmapvar ) {
            return JspUtil.interpreterCall ( isTagFile, expression, expectedType, fnmapvar );
        }
    }
}
