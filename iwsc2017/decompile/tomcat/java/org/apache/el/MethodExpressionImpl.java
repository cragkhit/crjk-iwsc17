package org.apache.el;
import java.io.ObjectOutput;
import java.io.IOException;
import org.apache.el.util.ReflectionUtil;
import java.io.ObjectInput;
import org.apache.el.lang.ExpressionBuilder;
import javax.el.ELException;
import javax.el.MethodNotFoundException;
import javax.el.PropertyNotFoundException;
import org.apache.el.lang.EvaluationContext;
import javax.el.MethodInfo;
import javax.el.ELContext;
import org.apache.el.parser.Node;
import javax.el.VariableMapper;
import javax.el.FunctionMapper;
import java.io.Externalizable;
import javax.el.MethodExpression;
public final class MethodExpressionImpl extends MethodExpression implements Externalizable {
    private Class<?> expectedType;
    private String expr;
    private FunctionMapper fnMapper;
    private VariableMapper varMapper;
    private transient Node node;
    private Class<?>[] paramTypes;
    public MethodExpressionImpl() {
    }
    public MethodExpressionImpl ( final String expr, final Node node, final FunctionMapper fnMapper, final VariableMapper varMapper, final Class<?> expectedType, final Class<?>[] paramTypes ) {
        this.expr = expr;
        this.node = node;
        this.fnMapper = fnMapper;
        this.varMapper = varMapper;
        this.expectedType = expectedType;
        this.paramTypes = paramTypes;
    }
    public boolean equals ( final Object obj ) {
        return obj instanceof MethodExpressionImpl && obj.hashCode() == this.hashCode();
    }
    public String getExpressionString() {
        return this.expr;
    }
    public MethodInfo getMethodInfo ( final ELContext context ) throws PropertyNotFoundException, MethodNotFoundException, ELException {
        final Node n = this.getNode();
        final EvaluationContext ctx = new EvaluationContext ( context, this.fnMapper, this.varMapper );
        ctx.notifyBeforeEvaluation ( this.getExpressionString() );
        final MethodInfo result = n.getMethodInfo ( ctx, this.paramTypes );
        ctx.notifyAfterEvaluation ( this.getExpressionString() );
        return result;
    }
    private Node getNode() throws ELException {
        if ( this.node == null ) {
            this.node = ExpressionBuilder.createNode ( this.expr );
        }
        return this.node;
    }
    public int hashCode() {
        return this.expr.hashCode();
    }
    public Object invoke ( final ELContext context, final Object[] params ) throws PropertyNotFoundException, MethodNotFoundException, ELException {
        final EvaluationContext ctx = new EvaluationContext ( context, this.fnMapper, this.varMapper );
        ctx.notifyBeforeEvaluation ( this.getExpressionString() );
        final Object result = this.getNode().invoke ( ctx, this.paramTypes, params );
        ctx.notifyAfterEvaluation ( this.getExpressionString() );
        return result;
    }
    public void readExternal ( final ObjectInput in ) throws IOException, ClassNotFoundException {
        this.expr = in.readUTF();
        final String type = in.readUTF();
        if ( !"".equals ( type ) ) {
            this.expectedType = ReflectionUtil.forName ( type );
        }
        this.paramTypes = ReflectionUtil.toTypeArray ( ( String[] ) in.readObject() );
        this.fnMapper = ( FunctionMapper ) in.readObject();
        this.varMapper = ( VariableMapper ) in.readObject();
    }
    public void writeExternal ( final ObjectOutput out ) throws IOException {
        out.writeUTF ( this.expr );
        out.writeUTF ( ( this.expectedType != null ) ? this.expectedType.getName() : "" );
        out.writeObject ( ReflectionUtil.toTypeNameArray ( this.paramTypes ) );
        out.writeObject ( this.fnMapper );
        out.writeObject ( this.varMapper );
    }
    public boolean isLiteralText() {
        return false;
    }
    public boolean isParametersProvided() {
        return this.getNode().isParametersProvided();
    }
    public boolean isParmetersProvided() {
        return this.getNode().isParametersProvided();
    }
}
