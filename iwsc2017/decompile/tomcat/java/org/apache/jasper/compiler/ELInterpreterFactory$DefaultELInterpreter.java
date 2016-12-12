package org.apache.jasper.compiler;
import org.apache.jasper.JspCompilationContext;
public static class DefaultELInterpreter implements ELInterpreter {
    @Override
    public String interpreterCall ( final JspCompilationContext context, final boolean isTagFile, final String expression, final Class<?> expectedType, final String fnmapvar ) {
        return JspUtil.interpreterCall ( isTagFile, expression, expectedType, fnmapvar );
    }
}
