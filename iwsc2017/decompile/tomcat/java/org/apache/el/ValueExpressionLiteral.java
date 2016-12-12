package org.apache.el;
import org.apache.el.util.ReflectionUtil;
import java.io.ObjectInput;
import java.io.IOException;
import java.io.ObjectOutput;
import javax.el.PropertyNotWritableException;
import org.apache.el.util.MessageFactory;
import javax.el.ELContext;
import java.io.Externalizable;
import javax.el.ValueExpression;
public final class ValueExpressionLiteral extends ValueExpression implements Externalizable {
    private static final long serialVersionUID = 1L;
    private Object value;
    private String valueString;
    private Class<?> expectedType;
    public ValueExpressionLiteral() {
    }
    public ValueExpressionLiteral ( final Object value, final Class<?> expectedType ) {
        this.value = value;
        this.expectedType = expectedType;
    }
    public Object getValue ( final ELContext context ) {
        context.notifyBeforeEvaluation ( this.getExpressionString() );
        Object result;
        if ( this.expectedType != null ) {
            result = context.convertToType ( this.value, ( Class ) this.expectedType );
        } else {
            result = this.value;
        }
        context.notifyAfterEvaluation ( this.getExpressionString() );
        return result;
    }
    public void setValue ( final ELContext context, final Object value ) {
        context.notifyBeforeEvaluation ( this.getExpressionString() );
        throw new PropertyNotWritableException ( MessageFactory.get ( "error.value.literal.write", this.value ) );
    }
    public boolean isReadOnly ( final ELContext context ) {
        context.notifyBeforeEvaluation ( this.getExpressionString() );
        context.notifyAfterEvaluation ( this.getExpressionString() );
        return true;
    }
    public Class<?> getType ( final ELContext context ) {
        context.notifyBeforeEvaluation ( this.getExpressionString() );
        final Class<?> result = ( this.value != null ) ? this.value.getClass() : null;
        context.notifyAfterEvaluation ( this.getExpressionString() );
        return result;
    }
    public Class<?> getExpectedType() {
        return this.expectedType;
    }
    public String getExpressionString() {
        if ( this.valueString == null ) {
            this.valueString = ( ( this.value != null ) ? this.value.toString() : null );
        }
        return this.valueString;
    }
    public boolean equals ( final Object obj ) {
        return obj instanceof ValueExpressionLiteral && this.equals ( ( ValueExpressionLiteral ) obj );
    }
    public boolean equals ( final ValueExpressionLiteral ve ) {
        return ve != null && this.value != null && ve.value != null && ( this.value == ve.value || this.value.equals ( ve.value ) );
    }
    public int hashCode() {
        return ( this.value != null ) ? this.value.hashCode() : 0;
    }
    public boolean isLiteralText() {
        return true;
    }
    public void writeExternal ( final ObjectOutput out ) throws IOException {
        out.writeObject ( this.value );
        out.writeUTF ( ( this.expectedType != null ) ? this.expectedType.getName() : "" );
    }
    public void readExternal ( final ObjectInput in ) throws IOException, ClassNotFoundException {
        this.value = in.readObject();
        final String type = in.readUTF();
        if ( !"".equals ( type ) ) {
            this.expectedType = ReflectionUtil.forName ( type );
        }
    }
}
