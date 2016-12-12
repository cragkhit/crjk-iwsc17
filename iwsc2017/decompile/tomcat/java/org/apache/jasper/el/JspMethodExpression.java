package org.apache.jasper.el;
import java.io.ObjectInput;
import java.io.IOException;
import java.io.ObjectOutput;
import javax.el.ELException;
import javax.el.PropertyNotFoundException;
import javax.el.MethodNotFoundException;
import javax.el.MethodInfo;
import javax.el.ELContext;
import java.io.Externalizable;
import javax.el.MethodExpression;
public final class JspMethodExpression extends MethodExpression implements Externalizable {
    private String mark;
    private MethodExpression target;
    public JspMethodExpression() {
    }
    public JspMethodExpression ( final String mark, final MethodExpression target ) {
        this.target = target;
        this.mark = mark;
    }
    public MethodInfo getMethodInfo ( final ELContext context ) throws NullPointerException, PropertyNotFoundException, MethodNotFoundException, ELException {
        context.notifyBeforeEvaluation ( this.getExpressionString() );
        try {
            final MethodInfo result = this.target.getMethodInfo ( context );
            context.notifyAfterEvaluation ( this.getExpressionString() );
            return result;
        } catch ( MethodNotFoundException e ) {
            if ( e instanceof JspMethodNotFoundException ) {
                throw e;
            }
            throw new JspMethodNotFoundException ( this.mark, e );
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
    public Object invoke ( final ELContext context, final Object[] params ) throws NullPointerException, PropertyNotFoundException, MethodNotFoundException, ELException {
        context.notifyBeforeEvaluation ( this.getExpressionString() );
        try {
            final Object result = this.target.invoke ( context, params );
            context.notifyAfterEvaluation ( this.getExpressionString() );
            return result;
        } catch ( MethodNotFoundException e ) {
            if ( e instanceof JspMethodNotFoundException ) {
                throw e;
            }
            throw new JspMethodNotFoundException ( this.mark, e );
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
        this.target = ( MethodExpression ) in.readObject();
    }
}
