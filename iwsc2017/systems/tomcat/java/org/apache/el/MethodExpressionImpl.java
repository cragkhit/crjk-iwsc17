package org.apache.el;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.FunctionMapper;
import javax.el.MethodExpression;
import javax.el.MethodInfo;
import javax.el.MethodNotFoundException;
import javax.el.PropertyNotFoundException;
import javax.el.VariableMapper;
import org.apache.el.lang.EvaluationContext;
import org.apache.el.lang.ExpressionBuilder;
import org.apache.el.parser.Node;
import org.apache.el.util.ReflectionUtil;
public final class MethodExpressionImpl extends MethodExpression implements
    Externalizable {
    private Class<?> expectedType;
    private String expr;
    private FunctionMapper fnMapper;
    private VariableMapper varMapper;
    private transient Node node;
    private Class<?>[] paramTypes;
    public MethodExpressionImpl() {
        super();
    }
    public MethodExpressionImpl ( String expr, Node node,
                                  FunctionMapper fnMapper, VariableMapper varMapper,
                                  Class<?> expectedType, Class<?>[] paramTypes ) {
        super();
        this.expr = expr;
        this.node = node;
        this.fnMapper = fnMapper;
        this.varMapper = varMapper;
        this.expectedType = expectedType;
        this.paramTypes = paramTypes;
    }
    @Override
    public boolean equals ( Object obj ) {
        return ( obj instanceof MethodExpressionImpl && obj.hashCode() == this
                 .hashCode() );
    }
    @Override
    public String getExpressionString() {
        return this.expr;
    }
    @Override
    public MethodInfo getMethodInfo ( ELContext context )
    throws PropertyNotFoundException, MethodNotFoundException,
        ELException {
        Node n = this.getNode();
        EvaluationContext ctx = new EvaluationContext ( context, this.fnMapper,
                this.varMapper );
        ctx.notifyBeforeEvaluation ( getExpressionString() );
        MethodInfo result = n.getMethodInfo ( ctx, this.paramTypes );
        ctx.notifyAfterEvaluation ( getExpressionString() );
        return result;
    }
    private Node getNode() throws ELException {
        if ( this.node == null ) {
            this.node = ExpressionBuilder.createNode ( this.expr );
        }
        return this.node;
    }
    @Override
    public int hashCode() {
        return this.expr.hashCode();
    }
    @Override
    public Object invoke ( ELContext context, Object[] params )
    throws PropertyNotFoundException, MethodNotFoundException,
        ELException {
        EvaluationContext ctx = new EvaluationContext ( context, this.fnMapper,
                this.varMapper );
        ctx.notifyBeforeEvaluation ( getExpressionString() );
        Object result = this.getNode().invoke ( ctx, this.paramTypes, params );
        ctx.notifyAfterEvaluation ( getExpressionString() );
        return result;
    }
    @Override
    public void readExternal ( ObjectInput in ) throws IOException,
        ClassNotFoundException {
        this.expr = in.readUTF();
        String type = in.readUTF();
        if ( !"".equals ( type ) ) {
            this.expectedType = ReflectionUtil.forName ( type );
        }
        this.paramTypes = ReflectionUtil.toTypeArray ( ( ( String[] ) in
                          .readObject() ) );
        this.fnMapper = ( FunctionMapper ) in.readObject();
        this.varMapper = ( VariableMapper ) in.readObject();
    }
    @Override
    public void writeExternal ( ObjectOutput out ) throws IOException {
        out.writeUTF ( this.expr );
        out.writeUTF ( ( this.expectedType != null ) ? this.expectedType.getName()
                       : "" );
        out.writeObject ( ReflectionUtil.toTypeNameArray ( this.paramTypes ) );
        out.writeObject ( this.fnMapper );
        out.writeObject ( this.varMapper );
    }
    @Override
    public boolean isLiteralText() {
        return false;
    }
    @Override
    public boolean isParametersProvided() {
        return this.getNode().isParametersProvided();
    }
    @Override
    public boolean isParmetersProvided() {
        return this.getNode().isParametersProvided();
    }
}
