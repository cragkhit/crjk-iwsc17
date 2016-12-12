package org.apache.jasper.el;
import java.io.ObjectInput;
import java.io.IOException;
import java.io.ObjectOutput;
import javax.el.PropertyNotWritableException;
import javax.el.ELException;
import javax.el.PropertyNotFoundException;
import javax.el.ELContext;
import java.io.Externalizable;
import javax.el.ValueExpression;
public final class JspValueExpression extends ValueExpression implements Externalizable {
    private ValueExpression target;
    private String mark;
    public JspValueExpression() {
    }
    public JspValueExpression ( final String mark, final ValueExpression target ) {
        this.target = target;
        this.mark = mark;
    }
    public Class<?> getExpectedType() {
        return ( Class<?> ) this.target.getExpectedType();
    }
    public Class<?> getType ( final ELContext context ) throws NullPointerException, PropertyNotFoundException, ELException {
        context.notifyBeforeEvaluation ( this.getExpressionString() );
        try {
            final Class<?> result = ( Class<?> ) this.target.getType ( context );
            context.notifyAfterEvaluation ( this.getExpressionString() );
            return result;
        } catch ( PropertyNotFoundException e ) {
            if ( e instanceof JspPropertyNotFoundException ) {
                throw e;
            }
            throw new JspPropertyNotFoundException ( this.mark, e );
        } catch ( ELException e2 ) {
            if ( e2 instanceof JspELException ) {
                throw e2;
            }
            throw new JspELException ( this.mark, e2 );
        }
    }
    public boolean isReadOnly ( final ELContext context ) throws NullPointerException, PropertyNotFoundException, ELException {
        context.notifyBeforeEvaluation ( this.getExpressionString() );
        try {
            final boolean result = this.target.isReadOnly ( context );
            context.notifyAfterEvaluation ( this.getExpressionString() );
            return result;
        } catch ( PropertyNotFoundException e ) {
            if ( e instanceof JspPropertyNotFoundException ) {
                throw e;
            }
            throw new JspPropertyNotFoundException ( this.mark, e );
        } catch ( ELException e2 ) {
            if ( e2 instanceof JspELException ) {
                throw e2;
            }
            throw new JspELException ( this.mark, e2 );
        }
    }
    public void setValue ( final ELContext context, final Object value ) throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException, ELException {
        context.notifyBeforeEvaluation ( this.getExpressionString() );
        try {
            this.target.setValue ( context, value );
            context.notifyAfterEvaluation ( this.getExpressionString() );
        } catch ( PropertyNotWritableException e ) {
            if ( e instanceof JspPropertyNotWritableException ) {
                throw e;
            }
            throw new JspPropertyNotWritableException ( this.mark, e );
        } catch ( PropertyNotFoundException e2 ) {
            if ( e2 instanceof JspPropertyNotFoundException ) {
                throw e2;
            }
            throw new JspPropertyNotFoundException ( this.mark, e2 );
        } catch ( ELException e3 ) {
            if ( e3 instanceof JspELException ) {
                throw e3;
            }
            throw new JspELException ( this.mark, e3 );
        }
    }
    public Object getValue ( final ELContext context ) throws NullPointerException, PropertyNotFoundException, ELException {
        context.notifyBeforeEvaluation ( this.getExpressionString() );
        try {
            final Object result = this.target.getValue ( context );
            context.notifyAfterEvaluation ( this.getExpressionString() );
            return result;
        } catch ( PropertyNotFoundException e ) {
            if ( e instanceof JspPropertyNotFoundException ) {
                throw e;
            }
            throw new JspPropertyNotFoundException ( this.mark, e );
        } catch ( ELException e2 ) {
            if ( e2 instanceof JspELException ) {
                throw e2;
            }
            throw new JspELException ( this.mark, e2 );
        }
    }
    public boolean equals ( final Object obj ) {
        return this.target.equals ( obj );
    }
    public int hashCode() {
        return this.target.hashCode();
    }
    public String getExpressionString() {
        return this.target.getExpressionString();
    }
    public boolean isLiteralText() {
        return this.target.isLiteralText();
    }
    public void writeExternal ( final ObjectOutput out ) throws IOException {
        out.writeUTF ( this.mark );
        out.writeObject ( this.target );
    }
    public void readExternal ( final ObjectInput in ) throws IOException, ClassNotFoundException {
        this.mark = in.readUTF();
        this.target = ( ValueExpression ) in.readObject();
    }
}
