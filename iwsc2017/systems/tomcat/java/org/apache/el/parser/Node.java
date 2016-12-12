package org.apache.el.parser;
import javax.el.ELException;
import javax.el.MethodInfo;
import javax.el.ValueReference;
import org.apache.el.lang.EvaluationContext;
@SuppressWarnings ( "all" )
public interface Node {
    public void jjtOpen();
    public void jjtClose();
    public void jjtSetParent ( Node n );
    public Node jjtGetParent();
    public void jjtAddChild ( Node n, int i );
    public Node jjtGetChild ( int i );
    public int jjtGetNumChildren();
    public String getImage();
    public Object getValue ( EvaluationContext ctx ) throws ELException;
    public void setValue ( EvaluationContext ctx, Object value ) throws ELException;
    public Class<?> getType ( EvaluationContext ctx ) throws ELException;
    public boolean isReadOnly ( EvaluationContext ctx ) throws ELException;
    public void accept ( NodeVisitor visitor ) throws Exception;
    public MethodInfo getMethodInfo ( EvaluationContext ctx, Class<?>[] paramTypes )
    throws ELException;
    public Object invoke ( EvaluationContext ctx, Class<?>[] paramTypes,
                           Object[] paramValues ) throws ELException;
    public ValueReference getValueReference ( EvaluationContext ctx );
    public boolean isParametersProvided();
}
