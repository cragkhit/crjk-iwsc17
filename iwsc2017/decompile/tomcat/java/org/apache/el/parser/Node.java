package org.apache.el.parser;
import javax.el.ValueReference;
import javax.el.MethodInfo;
import javax.el.ELException;
import org.apache.el.lang.EvaluationContext;
public interface Node {
    void jjtOpen();
    void jjtClose();
    void jjtSetParent ( Node p0 );
    Node jjtGetParent();
    void jjtAddChild ( Node p0, int p1 );
    Node jjtGetChild ( int p0 );
    int jjtGetNumChildren();
    String getImage();
    Object getValue ( EvaluationContext p0 ) throws ELException;
    void setValue ( EvaluationContext p0, Object p1 ) throws ELException;
    Class<?> getType ( EvaluationContext p0 ) throws ELException;
    boolean isReadOnly ( EvaluationContext p0 ) throws ELException;
    void accept ( NodeVisitor p0 ) throws Exception;
    MethodInfo getMethodInfo ( EvaluationContext p0, Class<?>[] p1 ) throws ELException;
    Object invoke ( EvaluationContext p0, Class<?>[] p1, Object[] p2 ) throws ELException;
    ValueReference getValueReference ( EvaluationContext p0 );
    boolean isParametersProvided();
}
