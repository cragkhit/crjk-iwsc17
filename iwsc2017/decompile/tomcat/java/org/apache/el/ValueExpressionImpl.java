package org.apache.el;
import javax.el.ValueReference;
import java.io.ObjectOutput;
import javax.el.PropertyNotWritableException;
import java.io.IOException;
import org.apache.el.util.ReflectionUtil;
import java.io.ObjectInput;
import org.apache.el.parser.AstLiteralExpression;
import javax.el.PropertyNotFoundException;
import org.apache.el.lang.EvaluationContext;
import javax.el.ELContext;
import javax.el.ELException;
import org.apache.el.lang.ExpressionBuilder;
import org.apache.el.parser.Node;
import javax.el.VariableMapper;
import javax.el.FunctionMapper;
import java.io.Externalizable;
import javax.el.ValueExpression;
public final class ValueExpressionImpl extends ValueExpression implements Externalizable {
    private Class<?> expectedType;
    private String expr;
    private FunctionMapper fnMapper;
    private VariableMapper varMapper;
    private transient Node node;
    public ValueExpressionImpl() {
    }
    public ValueExpressionImpl ( final String expr, final Node node, final FunctionMapper fnMapper, final VariableMapper varMapper, final Class<?> expectedType ) {
        this.expr = expr;
        this.node = node;
        this.fnMapper = fnMapper;
        this.varMapper = varMapper;
        this.expectedType = expectedType;
    }
    public boolean equals ( final Object obj ) {
        return obj instanceof ValueExpressionImpl && obj.hashCode() == this.hashCode();
    }
    public Class<?> getExpectedType() {
        return this.expectedType;
    }
    public String getExpressionString() {
        return this.expr;
    }
    private Node getNode() throws ELException {
        if ( this.node == null ) {
            this.node = ExpressionBuilder.createNode ( this.expr );
        }
        return this.node;
    }
    public Class<?> getType ( final ELContext context ) throws PropertyNotFoundException, ELException {
        final EvaluationContext ctx = new EvaluationContext ( context, this.fnMapper, this.varMapper );
        context.notifyBeforeEvaluation ( this.getExpressionString() );
        final Class<?> result = this.getNode().getType ( ctx );
        context.notifyAfterEvaluation ( this.getExpressionString() );
        return result;
    }
    public Object getValue ( final ELContext context ) throws PropertyNotFoundException, ELException {
        final EvaluationContext ctx = new EvaluationContext ( context, this.fnMapper, this.varMapper );
        context.notifyBeforeEvaluation ( this.getExpressionString() );
        Object value = this.getNode().getValue ( ctx );
        if ( this.expectedType != null ) {
            value = context.convertToType ( value, ( Class ) this.expectedType );
        }
        context.notifyAfterEvaluation ( this.getExpressionString() );
        return value;
    }
    public int hashCode() {
        return this.getNode().hashCode();
    }
    public boolean isLiteralText() {
        try {
            return this.getNode() instanceof AstLiteralExpression;
        } catch ( ELException ele ) {
            return false;
        }
    }
    public boolean isReadOnly ( final ELContext context ) throws PropertyNotFoundException, ELException {
        final EvaluationContext ctx = new EvaluationContext ( context, this.fnMapper, this.varMapper );
        context.notifyBeforeEvaluation ( this.getExpressionString() );
        final boolean result = this.getNode().isReadOnly ( ctx );
        context.notifyAfterEvaluation ( this.getExpressionString() );
        return result;
    }
    public void readExternal ( final ObjectInput in ) throws IOException, ClassNotFoundException {
        this.expr = in.readUTF();
        final String type = in.readUTF();
        if ( !"".equals ( type ) ) {
            this.expectedType = ReflectionUtil.forName ( type );
        }
        this.fnMapper = ( FunctionMapper ) in.readObject();
        this.varMapper = ( VariableMapper ) in.readObject();
    }
    public void setValue ( final ELContext context, final Object value ) throws PropertyNotFoundException, PropertyNotWritableException, ELException {
        final EvaluationContext ctx = new EvaluationContext ( context, this.fnMapper, this.varMapper );
        context.notifyBeforeEvaluation ( this.getExpressionString() );
        this.getNode().setValue ( ctx, value );
        context.notifyAfterEvaluation ( this.getExpressionString() );
    }
    public void writeExternal ( final ObjectOutput out ) throws IOException {
        out.writeUTF ( this.expr );
        out.writeUTF ( ( this.expectedType != null ) ? this.expectedType.getName() : "" );
        out.writeObject ( this.fnMapper );
        out.writeObject ( this.varMapper );
    }
    public String toString() {
        return "ValueExpression[" + this.expr + "]";
    }
    public ValueReference getValueReference ( final ELContext context ) {
        final EvaluationContext ctx = new EvaluationContext ( context, this.fnMapper, this.varMapper );
        context.notifyBeforeEvaluation ( this.getExpressionString() );
        final ValueReference result = this.getNode().getValueReference ( ctx );
        context.notifyAfterEvaluation ( this.getExpressionString() );
        return result;
    }
}
