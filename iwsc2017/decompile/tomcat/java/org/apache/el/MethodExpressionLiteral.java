package org.apache.el;
import java.io.ObjectOutput;
import java.io.IOException;
import org.apache.el.util.ReflectionUtil;
import java.io.ObjectInput;
import javax.el.ELException;
import javax.el.MethodInfo;
import javax.el.ELContext;
import java.io.Externalizable;
import javax.el.MethodExpression;
public class MethodExpressionLiteral extends MethodExpression implements Externalizable {
    private Class<?> expectedType;
    private String expr;
    private Class<?>[] paramTypes;
    public MethodExpressionLiteral() {
    }
    public MethodExpressionLiteral ( final String expr, final Class<?> expectedType, final Class<?>[] paramTypes ) {
        this.expr = expr;
        this.expectedType = expectedType;
        this.paramTypes = paramTypes;
    }
    public MethodInfo getMethodInfo ( final ELContext context ) throws ELException {
        context.notifyBeforeEvaluation ( this.getExpressionString() );
        final MethodInfo result = new MethodInfo ( this.expr, ( Class ) this.expectedType, ( Class[] ) this.paramTypes );
        context.notifyAfterEvaluation ( this.getExpressionString() );
        return result;
    }
    public Object invoke ( final ELContext context, final Object[] params ) throws ELException {
        context.notifyBeforeEvaluation ( this.getExpressionString() );
        Object result;
        if ( this.expectedType != null ) {
            result = context.convertToType ( ( Object ) this.expr, ( Class ) this.expectedType );
        } else {
            result = this.expr;
        }
        context.notifyAfterEvaluation ( this.getExpressionString() );
        return result;
    }
    public String getExpressionString() {
        return this.expr;
    }
    public boolean equals ( final Object obj ) {
        return obj instanceof MethodExpressionLiteral && this.hashCode() == obj.hashCode();
    }
    public int hashCode() {
        return this.expr.hashCode();
    }
    public boolean isLiteralText() {
        return true;
    }
    public void readExternal ( final ObjectInput in ) throws IOException, ClassNotFoundException {
        this.expr = in.readUTF();
        final String type = in.readUTF();
        if ( !"".equals ( type ) ) {
            this.expectedType = ReflectionUtil.forName ( type );
        }
        this.paramTypes = ReflectionUtil.toTypeArray ( ( String[] ) in.readObject() );
    }
    public void writeExternal ( final ObjectOutput out ) throws IOException {
        out.writeUTF ( this.expr );
        out.writeUTF ( ( this.expectedType != null ) ? this.expectedType.getName() : "" );
        out.writeObject ( ReflectionUtil.toTypeNameArray ( this.paramTypes ) );
    }
}
