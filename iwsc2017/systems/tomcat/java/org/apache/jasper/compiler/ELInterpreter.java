package org.apache.jasper.compiler;
import org.apache.jasper.JspCompilationContext;
public interface ELInterpreter {
    public String interpreterCall ( JspCompilationContext context,
                                    boolean isTagFile, String expression,
                                    Class<?> expectedType, String fnmapvar );
}
