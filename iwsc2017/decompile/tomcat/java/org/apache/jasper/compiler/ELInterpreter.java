package org.apache.jasper.compiler;
import org.apache.jasper.JspCompilationContext;
public interface ELInterpreter {
    String interpreterCall ( JspCompilationContext p0, boolean p1, String p2, Class<?> p3, String p4 );
}
